# Etapa de build
FROM node:22-alpine AS builder
WORKDIR /app

# Vazio = mesma origem (o Nginx abaixo encaminha /api para o backend)
ARG VITE_API_URL=
ENV VITE_API_URL=$VITE_API_URL

COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

COPY tsconfig.json tsconfig.app.json tsconfig.node.json vite.config.ts index.html eslint.config.js ./
COPY public ./public
COPY src ./src

RUN npm run build

# Etapa de execução
FROM nginx:1.27-alpine
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
