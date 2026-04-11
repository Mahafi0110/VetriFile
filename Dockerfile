FROM maven:3.9.9-eclipse-temurin-21

WORKDIR /app

RUN apt-get update && \
    apt-get install -y \
    ffmpeg \
    libreoffice \
    libreoffice-writer \
    && apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# ✅ Verify both installed correctly during build
RUN libreoffice --version && ffmpeg -version

COPY . .

RUN mvn clean package -DskipTests

EXPOSE 8080

# ✅ Memory limit for Render free tier
CMD ["java", "-Xmx256m", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]