# Note: 'docker run' must be started with '-it' for this image otherwise it will crash because of spring shell

# Use the Oracal Java 8 as parent image
FROM java:8
MAINTAINER "bn21@cs.aau.dk"
LABEL vendor="FED"

# Set the working directory to /app
WORKDIR /app

# Copy the required files into the container at /app
ADD target/fman-proxy.jar /app/fman-proxy.jar

# Create a mount point on the native host.
VOLUME /tmp

# Port available to the world outside this container
EXPOSE 8089

# Start the application when the container launches
ENTRYPOINT ["java", "-Duser.timezone=CET", "-Dspring.profiles.active=container", "-jar", "fman-proxy.jar"]
