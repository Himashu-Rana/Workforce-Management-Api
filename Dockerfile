FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy the Gradle configuration files
COPY build.gradle settings.gradle ./

# Copy the source code
COPY src ./src

# Install Gradle
RUN apt-get update && apt-get install -y wget unzip \
    && wget https://services.gradle.org/distributions/gradle-7.6.1-bin.zip \
    && unzip gradle-7.6.1-bin.zip \
    && rm gradle-7.6.1-bin.zip

# Set Gradle in PATH
ENV PATH="/app/gradle-7.6.1/bin:${PATH}"

# Build the application
RUN gradle clean build --no-daemon

EXPOSE 8080

# Run the application
CMD ["java", "-jar", "build/libs/workforcemgmt-0.0.1-SNAPSHOT.jar"]
