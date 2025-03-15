FROM node:16-alpine
WORKDIR /app
COPY . .
RUN npm install -g http-server
EXPOSE 3000
CMD ["http-server", "-p", "3000", "-c-1"]
