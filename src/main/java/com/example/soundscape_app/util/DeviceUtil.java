package com.spotify.util;

import eu.bitwalker.useragentutils.UserAgent;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;


public class DeviceUtil {
    public static String getFullDeviceInfo(HttpServletRequest request) {
        String userAgentString = request.getHeader("Auth-Agent");
        if (userAgentString == null) {
            return "Unknown all information device";
        }

        UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);

        String os = userAgent.getOperatingSystem().getName(); // Hệ điều hành
        String browser = userAgent.getBrowser().getName(); // Trình duyệt
        String deviceType = userAgent.getOperatingSystem().getDeviceType().getName(); // Loại thiết bị

        return deviceType + " - " + os + " - " + browser;
    }


    public static String getDeviceId(HttpServletRequest request) {
        String userAgent = request.getHeader("Auth-Agent");
        String acceptLanguage = request.getHeader("Accept-Language");
        String acceptEncoding = request.getHeader("Accept-Encoding");


        String fingerprint = userAgent + acceptLanguage + acceptEncoding;
        return DigestUtils.sha256Hex(fingerprint);

    }
}
