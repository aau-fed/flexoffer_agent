### FlexOffer Agent (FOA)

The FlexOffer Agent (FOA) is an extensible and highly customizable software component of the Flexibility Modeling, Management, and Trading System that is responsible for the generation and execution of FOs for one or multiple flexible loads (both production and consumption). It forms individual FlexOffers, delivers the individual FlexOffers to the aggregator software (FMAN), receives disaggregated schedules from the FMAN, and activates the flexible loads according to the received schedules. 

### What is this repository for? ###

In this repository, you will be able to find the source code of the FOA back-end and front-end sub-systems, installation and deployment instructions, 
and a user manual. The repository contains the following sub-systems:

 1. `foa-app` gathers power consumption data from individual flexible loads, aggregates it, and delivers it to the aggregator software (FMAN). It also receives disaggregated schedules from the FMAN, and activates the flexible loads (i.e., turning them on/off) according to the received schedules. 
 2. `fo-generator` generates and delivers FlexOffers to the aggregator software (FMAN). 
 3. `foa-frontend` offers functionality to register new prosumers, add prosumer loads and configure load flexibility parameters. It communicates with `foa-app` backend to fetch and visualize flexible load live power consumtion and state as well as historical power consumption. 
 4. `fman-proxy` enables FOA to connect with multiple FMAN instances. In fact, all communication between FOA and one or more FMANs is handled by `fman-proxy`.
 5. `sys-monitor` monitors all flexible loads to ensure that they are restored to their normal power state after schedule execution. Normally, `foa-app` should handle device state restoration, but `sys-monitor` serves as an added safety mechnism since failure to restore device state can cause significant user discomfort. 

### How do I get set up? ###

It is recommended to first get `foa-app` and `foa-frontend` up and running. 
This way a user can add some flexible loads for testing (see [Smartplug Installation Guide](./manuals/TP-Link_%20SmartPlug_%20Installation_Guide.pdf)) and play around with the UI (see [FOA manual](./manuals/FOA_Manual.pdf)). 
After adding flexible loads, the next step would be setting up `fo-generator` to start generating FlexOffers and use the UI to configure load flexibility parameters (see [Flexibility Configuration Manual](./manuals/Load_Flexibility_Generation_And_Configuration_Manual.pdf)). 
Then, at some point one may want to send generated FlexOffers to an aggregator (FMAN). 
This can be done by setting up and running `fman-proxy` and providing it with the `url` of an FMAN. The default url used by `fman-proxy` is `http://localhost:8085`. 
Later, when FMAN starts delivering execution schedules, `foa-app` will start sending `on/off` signals to
the flexible loads to deliver the offered flexibility. At this point, `sys-monitor` should be set up 
so that flexible loads are not allowed to operate in manner that will cause discomfort.

#### A. Manually setting-up and running FOA ####

##### Common steps for all back-end systems #####

1. Configure `JDK` and `Apache Maven` environments. FOA requires `JDK 8` which can be found [here](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html). Instructions for installing `Apache Maven` are available [here](https://maven.apache.org/install.html).

2. Install, configure, and run `MySQL` database management system. Instructions are available [here](https://dev.mysql.com/doc/mysql-installation-excerpt/8.0/en/installing.html).


##### Setting-up `foa-app`

1. Update configuration file `/foa-app/src/main/resources/application.properties`. At the minimum, you will need to update the following:
 ```
 spring.datasource.url=jdbc:mysql://localhost:3306/foa?useSSL=false&serverTimezone=CET
 spring.datasource.username=fed
 spring.datasource.password=password
 ```
 The default db is `foa` which should be created if it does not exist. Also, the default password is `password`, which should obviously be changed.  

2. Go to the root folder and run: 
 ```mvn clean compile package```
	
3. Run the FMAN back-end using the command:
 ```java -jar foa-app/target/foa-app.jar```

##### Setting-up `fo-generator`, `fman-proxy`, and `sys-monitor` #####

Instructions for setting up these sub-systems are similar to the instructions for setting up `foa-app` provided above. You will need to change `foa-app` with one of the other sub-systems.

##### Setting-up the front-end #####
 
1. Configure `Node.js` and the `Node Package Manage (npm)` tool. Download instructions can be found [here](https://nodejs.org/en/download/).

2. Go to the `/foa-frontend` and run the front-end application using the command:
   ```npx http-server -o app/#```

#### B. Automated set-up and running using `docker`

It's easy to setup and run the app with docker. You must install `docker` and `docker-compose`. Installation instruction are available [here](https://docs.docker.com/docker-for-windows/install/)

Open `.env` file and update the following with the correct values
```
MYSQL_ROOT_PASSWORD=
MYSQL_USER=
MYSQL_PASSWORD=
```

##### Pull and run pre-built docker images

```bash
docker-compose pull # you will need to install docker-compose if not installed already

# to launch a service, you must be in the same directory containing docker-compose.yml

# run `foa-app`
docker-compose up foa-app

# see `foa-app` logs
docker-compose logs -f foa-app

# run `fo-generator`
docker-compose up fo-generator

# run `fman-proxy`
docker-compose up fman-proxy

# run `sys-monitor`
docker-compose up sys-monitor

# run `fman-frontend`
docker-compose up foa-frontend

# run all
docker-compose up
```

##### Build and run docker images locally

```bash
# compile 
mvn clean compile package -DskipTests

# build docker image
docker-compose build
```

After building docker images, you can run them using the above commands

##### Connect/Disconnect to/from a container

```bash
# attach to a running container
docker attach <container-name>

# detach from the running container
CTRL-p, CTRL-q
```

After the container is up and running, the services can be accessed on ports configured in `docker-compose.yml`

##### Redeployment after changing/updating code
When you make changes to your app code, remember to rebuild your image and recreate your docker containers.
To redeploy a service, use the following command the following commands.
The first rebuilds the image for the service and then stop, destroy, and recreate just the web service.
The --no-deps flag prevents `docker-compose` from also recreating any services which this service depends on.

```
docker-compose build --pull <service-name>
docker-compose up --no-deps -d <service-name>
```

##### Remove unnecessary docker images

```bash
docker system prune -f
```

### Manuals
1. [Smartplug Installation Guide](./manuals/TP-Link_%20SmartPlug_%20Installation_Guide.pdf)
2. [FlexOffer Agent Manual](./manuals/FOA_Manual.pdf).
3. [Load Flexibility Generation and Configuration Manual](./manuals/Load_Flexibility_Generation_And_Configuration_Manual.pdf).

### Acknowledgments

This project is supported by Flexible Energy Denmark (FED) - a Danish digitization project aimed at turning Danish electricity consumption flexible to enable excess power production from wind turbines and solar cells. The project is funded by Innovation Fund Denmark.
