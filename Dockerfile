FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /app

# Copy only pom.xml first (for caching)
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Copy full project
COPY . .

# Default command → run automation
CMD ["mvn", "test"]