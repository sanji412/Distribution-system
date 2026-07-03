FROM docker.m.daocloud.io/library/node:22-alpine AS build

ARG VITE_API_BASE_URL=
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

WORKDIR /app
COPY package*.json ./
RUN npm ci

COPY . ./
RUN npm run build

FROM docker.m.daocloud.io/library/node:22-alpine

ENV NODE_ENV=production
ENV PORT=5173
ENV API_TARGET=http://gateway-service:9000

WORKDIR /app
COPY docker-server.js ./server.js
COPY --from=build /app/dist ./dist

EXPOSE 5173
CMD ["node", "server.js"]
