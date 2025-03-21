FROM node:18-alpine

WORKDIR /app

# Copy package files first to take advantage of Docker caching
COPY package.json package-lock.json* ./
RUN npm install

# Copy all application files
COPY . .

EXPOSE 3000

# Use the npm start script from package.json
CMD ["npm", "start"]
