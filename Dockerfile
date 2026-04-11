FROM maven:3.9.9-eclipse-temurin-21

WORKDIR /app

# Install FFmpeg + LibreOffice
RUN apt-get update && \
    apt-get install -y \
    ffmpeg \
    libreoffice \
    libreoffice-writer \
    && apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY . .

RUN mvn clean package -DskipTests

EXPOSE 8080

# ✅ Memory limit set here in CMD (not in application.properties)
CMD ["java", "-Xmx384m", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]