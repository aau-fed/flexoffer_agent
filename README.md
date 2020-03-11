### Installation using docker

It's very easy to setup and run the app with docker. All you need to do is build a docker image, then either run it in a container locally or push the image to a remote server and run it there. You must install `docker` and `docker-compose` both locally and on remote server if not installed already. 

* Install Docker on [Ubuntu 16](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-16-04) or [Ubuntu 18](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-on-ubuntu-18-04)

* Install Docker Compose

  ```bash
  sudo curl -L "https://github.com/docker/compose/releases/download/1.23.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
  sudo chmod +x /usr/local/bin/docker-compose
  docker-compose --version
  ```````

#### Build Project

```bash
mvn clean install -DskipTests
```

#### Build docker image

```bash
docker-compose build
```

#### Push docker image to remote private docker registry

```bash
# one-time login to private docker registry
docker login repo.treescale.com # it will ask for username and password

docker-compose push
```

#### On remote server, pull and run docker image from private registry

```bash
# copy docker-compose.yml to remote server
scp docker-compose.yml <username>@<remote-server-address>:/remote/dir/path

# login to remote server and navigate to the directory containing docker-compose.yml
ssh <username>@<remote-server-address>
cd /remote/dir/path

# one-time login to private docker registry
docker login repo.treescale.com # you will need to install docker if not installed already
docker-compose pull # you will need to install docker-compose if not installed already

# launch a container (you must be inside the directory containing docker-compose.yml)
docker-compose -d up
```

Alternatively, you can perform one-time login to the private docker registry on the remote server as shown above, then
directly deploy container from your local machine as follows:
```bash
# Note: you must be logged in to the private docker registry to run these commands
cat docker-compose | ssh <username>@<remote-server-address> "docker-compose -f - pull"
cat docker-compose | ssh <username>@<remote-server-address> "docker-compose -f - up -d"
```

#### Connect/Disconnect to/from a container

```bash
# attach to a running container
docker attach <container-name>

# detach from the running container
CTRL-p, CTRL-q
```

After the container is up and running, the services can be accessed on ports configured in `docker-compose.yml`

#### Redeployment after changing/updating code
When you make changes to your app code, remember to rebuild your image and recreate your appâ€™s containers.
To redeploy a service, use the following command the following commands.
The first rebuilds the image for the service and then stop, destroy, and recreate just the web service.
The --no-deps flag prevents `docker-compose` from also recreating any services which this service depends on.

```
docker-compose build --pull <service-name>
docker-compose up --no-deps -d <service-name>
```

#### Remove unnecessary docker images

```bash
# locally
docker system prune -f

# from remote server
ssh <username>@<remote-server-address> "docker system prune -f"
```

