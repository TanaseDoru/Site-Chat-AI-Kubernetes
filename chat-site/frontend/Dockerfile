# ─── BUILD STAGE ─────────────────────────────────────────────────────────────
FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# ─── RUNTIME STAGE ────────────────────────────────────────────────────────────
FROM nginx:alpine
# your custom nginx.conf should listen on 90
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 90
CMD ["nginx", "-g", "daemon off;"]
