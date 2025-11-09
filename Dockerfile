# Chọn base image có JDK
FROM eclipse-temurin:17-jdk-alpine

# Tạo thư mục app
WORKDIR /app

# Copy toàn bộ source code
COPY . .

# Cấp quyền thực thi cho mvnw
RUN chmod +x ./mvnw

# Build project bằng Maven
RUN ./mvnw clean package -DskipTests

# Chạy ứng dụng Spring Boot
CMD ["java", "-jar", "target/net-0.0.1-SNAPSHOT.war"]
