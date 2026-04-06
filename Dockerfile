FROM maven:3.9.9-eclipse-temurin-21

WORKDIR /app

# Install FFmpeg
RUN apt-get update && \
    apt-get install -y ffmpeg && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY . .

RUN mvn clean package -DskipTests

EXPOSE 10000

CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]