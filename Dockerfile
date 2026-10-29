# =========================
# Build
# =========================
FROM eclipse-temurin:25-jdk-jammy AS build

WORKDIR /app

# Copy Maven wrapper and configuration first for better caching
COPY --chmod=0755 mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Cache Maven dependencies
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -DskipTests

# Copy source
COPY src src
COPY username-template.csv .
# Build application
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests && \
    cp target/*.jar target/app.jar


# =========================
# Runtime
# =========================
FROM eclipse-temurin:25-jdk-jammy

WORKDIR /app

# Create non-root user
RUN useradd --system --create-home --uid 10001 appuser

USER appuser

COPY --from=build /app/target/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]