# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Tải dependency trước để tận dụng cache layer
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -B dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app
ENV TZ=Asia/Ho_Chi_Minh \
    APP_UPLOAD_DIR=/app/uploads
RUN mkdir -p /app/uploads/products /app/uploads/files
COPY --from=build /build/target/net-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8000
ENTRYPOINT ["java", "-Xms256m", "-Xmx768m", "-jar", "app.jar"]
