# ─── BUILD STAGE ─────────────────────────────────────────────────────────────
FROM maven:3.8.4-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ─── RUNTIME STAGE ────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre
# install nginx
RUN apt-get update \
 && apt-get install -y nginx \
 && rm -rf /var/lib/apt/lists/*

# copy your custom nginx.conf
COPY nginx.conf /etc/nginx/nginx.conf

# copy the chat-server jar
COPY --from=builder /app/target/chat-server.jar /app/chat-server.jar

# helper script to launch both backend and nginx
COPY start.sh /start.sh
RUN chmod +x /start.sh

EXPOSE 88
CMD ["/start.sh"]
