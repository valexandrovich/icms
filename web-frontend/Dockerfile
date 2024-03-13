# Use an official Node runtime as a parent image
FROM node:lts-alpine

# Set the working directory in the container
WORKDIR /app

# Copy package.json and package-lock.json to the working directory
COPY package*.json ./

# Install project dependencies
RUN npm install

# Copy the rest of your app's source code from your host to your image filesystem.
COPY . .

# Build the app for production
RUN npm run build

# Set the command to start the app
CMD [ "npm", "run", "dev" ]
