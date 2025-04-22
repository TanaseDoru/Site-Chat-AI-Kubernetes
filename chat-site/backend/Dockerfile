# Stage 1: Build the Java application
FROM maven:3.8.5-openjdk-11 AS build
WORKDIR /app
COPY src/ /app/src/
COPY pom.xml /app/
RUN mvn clean package

# Stage 2: Set up Nginx and Java runtime
FROM nginx:latest
RUN apt-get update && apt-get install -y openjdk-11-jre
COPY --from=build /app/target/chat-server.jar /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 88
CMD ["sh", "-c", "java -jar /usr/share/nginx/html/chat-server.jar & nginx -g 'daemon off;'"]
