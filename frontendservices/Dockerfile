FROM node:16-alpine

WORKDIR /app

RUN npm install -g http-server

COPY index.html .
COPY main.js .
COPY geocoding.js .
COPY styles.css .

EXPOSE 3000

CMD ["http-server", "-p", "3000", "--cors"]
