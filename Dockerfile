FROM node:22-alpine AS frontend
WORKDIR /workspace/frontend
COPY gym-occupancy-frontend/package.json gym-occupancy-frontend/package-lock.json ./
RUN npm ci
COPY gym-occupancy-frontend/ ./
RUN npm run build

FROM maven:3.9.16-eclipse-temurin-17 AS backend
WORKDIR /workspace
COPY pom.xml ./
RUN mvn -B dependency:go-offline
COPY src/ src/
COPY --from=frontend /workspace/frontend/build/ src/main/resources/static/
RUN mvn -B package -DskipTests

FROM eclipse-temurin:17-jre-jammy
RUN apt-get update \
    && apt-get install --yes --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --create-home --uid 10001 appuser
WORKDIR /app
COPY --from=backend /workspace/target/gym-occupancy-0.0.1-SNAPSHOT.jar app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl --fail --silent http://localhost:8080/actuator/health/readiness > /dev/null || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
