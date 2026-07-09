FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml và source code
COPY pom.xml .
COPY src ./src

# Build project và bỏ qua chạy test để build nhanh hơn
RUN mvn clean package -DskipTests

# Stage 2: Chạy ứng dụng
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

ARG GIT_COMMIT=unknown
ARG IMAGE_TAG=latest
ARG BUILD_TIME=unknown

ENV APP_GIT_COMMIT=$GIT_COMMIT
ENV APP_IMAGE_TAG=$IMAGE_TAG
ENV APP_BUILD_TIME=$BUILD_TIME

LABEL org.opencontainers.image.revision=$GIT_COMMIT
LABEL org.opencontainers.image.version=$IMAGE_TAG
LABEL org.opencontainers.image.created=$BUILD_TIME

# Copy file jar từ bước build trước
COPY --from=build /app/target/*.jar app.jar

# Expose port backend
EXPOSE 8080

# Chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
