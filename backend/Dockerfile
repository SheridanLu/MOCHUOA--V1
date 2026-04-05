# Build stage
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml ./
COPY mochu-common/pom.xml mochu-common/
COPY mochu-framework/pom.xml mochu-framework/
COPY mochu-system/pom.xml mochu-system/
COPY mochu-business/pom.xml mochu-business/
COPY mochu-admin/pom.xml mochu-admin/
RUN mvn dependency:go-offline -B || true
COPY . .
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/mochu-admin/target/mochu-admin.jar app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-jar", "app.jar"]
