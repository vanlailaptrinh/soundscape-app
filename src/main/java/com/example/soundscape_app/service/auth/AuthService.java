package com.spotify.service.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.GenericData;
import com.spotify.dto.request.auth.LoginRequest;
import com.spotify.dto.request.auth.RegisterRequest;
import com.spotify.dto.response.global.MessageResponse;
import com.spotify.dto.response.user.AuthResponse;
import com.spotify.entity.auth.Auth;
import com.spotify.entity.auth.RefreshToken;
import com.spotify.entity.auth.Role;
import com.spotify.entity.auth.UserProvider;
import com.spotify.enums.AccountStatusEnum;
import com.spotify.enums.AuthProviderEnum;
import com.spotify.enums.RoleEnum;
import com.spotify.exception.EmailAlreadyExistsException;
import com.spotify.exception.InvalidTokenException;
import com.spotify.repository.auth.AuthRepository;
import com.spotify.repository.auth.RoleRepository;
import com.spotify.repository.auth.UserAuthProviderRepository;
import com.spotify.security.CustomUserDetailsService;
import com.spotify.service.common.InitValueService;
import com.spotify.util.DeviceUtil;
import com.spotify.util.EmailUtil;
import com.spotify.util.JwtUtil;
import com.spotify.util.S3Util;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final long TEMP_PASSWORD_EXPIRATION = 10; // 10 minutes
    private final AuthRepository authRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtBlacklistService jwtBlacklistService;
    private final SendAndVerifyCodeService sendAndVerifyCodeService;
    private final StringRedisTemplate redisTemplate;
    private final UserAuthProviderRepository userAuthProviderRepository;
    private final S3Util s3Util;
    private final InitValueService initValueService;
    private final HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${google.client.secret}")
    private String googleClientSecret;

    @Value("${google.callback.url}")
    private String googleCallbackUrl;

    public void storeTemporaryPassword(String email, String encodedPassword) {
        redisTemplate.opsForValue().set(getPasswordKey(email), encodedPassword, TEMP_PASSWORD_EXPIRATION, TimeUnit.MINUTES);
    }

    public String retrieveTemporaryPassword(String email) {
        String password = redisTemplate.opsForValue().get(getPasswordKey(email));
        if (password != null) {
            redisTemplate.delete(getPasswordKey(email));
        }
        return password;
    }

    private String getPasswordKey(String email) {
        return "temp_password:" + email;
    }

    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        EmailUtil.valid(request.getEmail());

        Auth auth = getUserByEmail(request.getEmail());

        if (auth.getPassword() == null) {
            throw new BadCredentialsException("Invalid email or password");
        }

        validateActiveAccount(auth);
        authenticateUser(request.getEmail(), request.getPassword());

        return buildAuthResponse(auth, httpRequest, httpResponse, "Auth logged in successfully!");
    }

    public MessageResponse initiateRegistration(RegisterRequest request) {
        EmailUtil.valid(request.getEmail());

        Optional<Auth> user = authRepository.findByEmail(request.getEmail());
        if (user.isPresent() && user.get().getPassword() != null) {
            throw new EmailAlreadyExistsException("Email already exists!");
        }
        storeTemporaryPassword(request.getEmail(), passwordEncoder.encode(request.getPassword()));
        sendAndVerifyCodeService.sendVerificationCode(request.getEmail());
        return new MessageResponse("Verification code sent successfully! Please verify your email.");
    }

    public AuthResponse completeRegistration(String email, String verificationCode, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (!sendAndVerifyCodeService.verifyCode(email, verificationCode)) {
            throw new IllegalArgumentException("Invalid or expired verification code!");
        }

        // Lấy lại mật khẩu đã lưu tạm thời từ Redis
        String encodedPassword = retrieveTemporaryPassword(email);
        if (encodedPassword == null) {
            throw new IllegalArgumentException("No registration request found. Please start over.");
        }
        Optional<Auth> optionUser = authRepository.findByEmail(email);
        Auth auth;
        if (optionUser.isPresent() && optionUser.get().getPassword() == null) {
            auth = addPassWord(optionUser.get(), encodedPassword);
        } else {
            List<RoleEnum> roleEnums = List.of(RoleEnum.USER);
            auth = createUserWithHashedPassword(email, encodedPassword, roleEnums);
        }
        initValueService.initPlayListForUser(auth);
        return buildAuthResponse(auth, httpRequest, httpResponse, "Auth registered successfully!");
    }

    public MessageResponse logout(String authorizationHeader, HttpServletRequest request) {
        String accessToken = extractToken(authorizationHeader);
        jwtUtil.validateToken(accessToken);

        Auth auth = getAuthFromAccessToken(authorizationHeader);
        refreshTokenService.deleteByUserIdAndDeviceId(auth, DeviceUtil.getDeviceId(request));
        jwtBlacklistService.blacklistToken(accessToken, jwtUtil.getExpirationFromToken(accessToken).getTime() - System.currentTimeMillis());

        return new MessageResponse("Auth logged out successfully!");
    }

    private void authenticateUser(String email, String rawPassword) {
        Auth auth = authRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Auth not found!"));

        if (!passwordEncoder.matches(rawPassword, auth.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, rawPassword));
    }

    private Auth getUserByEmail(String email) {
        return authRepository.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
    }

    public Auth createUserWithHashedPassword(String email, String hashedPassword, List<RoleEnum> roleEnums) {
        Auth auth = new Auth();
        auth.setEmail(email);
        auth.setPassword(hashedPassword);

        Set<Role> roleEntities = roleEnums.stream()
                .map(roleEnum -> roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new RuntimeException("Role '" + roleEnum.name() + "' not found")))
                .collect(Collectors.toSet());

        auth.setRoleEntities(roleEntities);
        return authRepository.save(auth);
    }

    private Auth addPassWord(Auth auth, String hashedPass) {
        auth.setPassword(hashedPass);
        return authRepository.save(auth);
    }

    // set default auth has role auth
    private Auth createUserOAuth(String email, String username, String urlAvatar) {
        // Tạo đối tượng Auth mới
        Auth auth = new Auth();
        auth.setEmail(email);
        auth.setUsername(username);
        auth.setUrlAvatar(urlAvatar);
        auth.setRoleEntities(Set.of(roleRepository.findByName(RoleEnum.USER)
                .orElseThrow(() -> new RuntimeException("Role not found"))));

        Auth savedAuth = authRepository.save(auth);
        initValueService.initPlayListForUser(savedAuth);

        return savedAuth;
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token format");
        }
        return authorizationHeader.substring(7);
    }

    public AuthResponse loginOrRegisterWithGoogle(String code, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            String decodedCode = URLDecoder.decode(code);
            String idTokenString = exchangeCodeForIdToken(decodedCode);

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), JacksonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new InvalidTokenException("Invalid or expired Google token.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleUserId = payload.getSubject();
            String email = payload.getEmail();
            String username = (String) payload.get("name");
            String avatarUrl = (String) payload.get("picture");


            // Kiểm tra auth đã tồn tại hay chưa
            Auth auth = userAuthProviderRepository.findByProviderAndProviderId(AuthProviderEnum.GOOGLE, googleUserId)
                    .map(UserProvider::getAuth)
                    .orElseGet(() -> authRepository.findByEmail(email).orElseGet(() -> {
                        // Tải và lưu ảnh nếu có avatarUrl
                        String savedAvatarPath = avatarUrl != null ? s3Util.uploadImageFromUrl(avatarUrl) : null;
                        return createUserOAuth(email, username, savedAvatarPath);
                    }));

            validateActiveAccount(auth);

            if (avatarUrl != null && (auth.getUrlAvatar() == null || auth.getUrlAvatar().isEmpty())) {
                String savedAvatarPath = s3Util.uploadImageFromUrl(avatarUrl);
                auth.setUrlAvatar(savedAvatarPath);
                authRepository.save(auth);
            }

            //Lưu thông tin đăng nhập Google nếu lần đầu
            userAuthProviderRepository.findByProviderAndProviderId(AuthProviderEnum.GOOGLE, googleUserId)
                    .orElseGet(() -> userAuthProviderRepository.save(new UserProvider(auth, AuthProviderEnum.GOOGLE, googleUserId)));

            return buildAuthResponse(auth, httpRequest, httpResponse, "Auth registered successfully!");

        } catch (InvalidTokenException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (GeneralSecurityException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google token verification failed.");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error: Unable to verify Google token.");
        }
    }


    public String exchangeCodeForIdToken(String code) throws IOException {
        try {
            HttpContent content = new UrlEncodedContent(new GenericData() {{
                put("code", code);
                put("client_id", googleClientId);
                put("client_secret", googleClientSecret);
                put("redirect_uri", googleCallbackUrl);
                put("grant_type", "authorization_code");
            }});

            GenericUrl tokenUrl = new GenericUrl(TOKEN_URL);
            HttpRequest request = requestFactory.buildPostRequest(tokenUrl, content);
            request.setThrowExceptionOnExecuteError(false); // Prevent auto-throwing on non-2xx responses
            HttpResponse response = request.execute();

            int statusCode = response.getStatusCode();
            String responseBody = response.parseAsString();

            if (!response.isSuccessStatusCode()) {
                System.err.println("Request failed with status: " + statusCode + ", body: " + responseBody);
                throw new IOException("Error exchanging code: " + statusCode + " - " + responseBody);
            }

            Map<String, Object> responseMap = JSON_FACTORY.fromString(responseBody, Map.class);
            String idToken = (String) responseMap.get("id_token");

            if (idToken == null || idToken.isEmpty()) {
                System.err.println("No id_token in response: " + responseBody);
                throw new IOException("No id_token found in Google API response: " + responseBody);
            }

            return idToken;

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Unexpected error while exchanging code: " + e.getMessage(), e);
        }
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is required!");
        }

        RefreshToken storedToken = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token!"));

        if (!refreshTokenService.validateToken(storedToken.getToken())) {
            refreshTokenService.delete(storedToken);
            throw new IllegalArgumentException("Refresh token has expired. Please log in again.");
        }

        Auth auth = storedToken.getAuth();
        if (auth == null) {
            throw new IllegalArgumentException("Auth associated with this token not found!");
        }

        String accessToken = jwtUtil.generateAccessToken(userDetailsService.loadUserById(auth.getId()));
        return new AuthResponse(accessToken, "Access token refreshed successfully.");
    }

    private AuthResponse buildAuthResponse(Auth auth, HttpServletRequest request, HttpServletResponse response, String message) {
        String accessToken = jwtUtil.generateAccessToken(
                userDetailsService.loadUserByUsername(auth.getEmail())
        );
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(auth, DeviceUtil.getDeviceId(request));
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(true)
                .secure(true) // Bật nếu dùng HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60) // 7 ngày
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return new AuthResponse(accessToken, message);
    }

    public Auth getAuthFromAccessToken(String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        Long userId = jwtUtil.extractUserId(token);
        return authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Auth not found from token"));
    }

    public Auth getById(Long userId) {
        return authRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Auth not found"));
    }

    private void validateActiveAccount(Auth auth) {
        if (auth.getStatus() != AccountStatusEnum.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account is not active. Please contact support."
            );
        }
    }

}
