## Use an official Node runtime as a parent image
#FROM node:lts-alpine
#
## Set the working directory in the container
#WORKDIR /app
#
## Copy package.json and package-lock.json to the working directory
#COPY package*.json ./
#
## Install project dependencies
#RUN npm install
#
## Copy the rest of your app's source code from your host to your image filesystem.
#COPY . .
#
## Build the app for production
#RUN npm run build
#
## Set the command to start the app
#CMD [ "npm", "run", "dev" ]

# Stage 1: Build the Vue application
FROM node:lts-alpine as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Serve the app using nginx
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
# Optional: If you have a custom nginx.conf, you can uncomment the following line
# COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
