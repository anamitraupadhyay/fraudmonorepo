FROM maven:3.9-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /build

# Copy Maven files for dependency caching
COPY pom.xml .
COPY .mvn/ ./.mvn/
COPY mvnw .
COPY mvnw.cmd .

# Fix line endings and make wrapper executable
RUN sed -i 's/\r$//' mvnw && \
    chmod +x mvnw

# Copy source code
COPY src/ ./src/

# Build using Maven directly instead of wrapper to avoid platform issues
RUN ls -la && mvn package -DskipTests

# Create runtime image
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.21

# Set environment variables
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

# Copy application from build stage
COPY --from=build /build/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build /build/target/quarkus-app/*.jar /deployments/
COPY --from=build /build/target/quarkus-app/app/ /deployments/app/
COPY --from=build /build/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080

CMD ["java", "-jar", "/deployments/quarkus-run.jar"]