FROM nginx
MAINTAINER "bn21@cs.aau.dk"
LABEL vendor="FED"

# copy project files
COPY app /usr/share/nginx/html 

# Copy the default nginx.conf
#COPY nginx.conf /etc/nginx/conf.d/default.conf

# Port available to the world outside this container
EXPOSE 80
