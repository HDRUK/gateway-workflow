# HDRUK Workflow

This is a Java/Spring Boot application, which acts as a host/platform for the [Camunda BPMN](https://camunda.com/). Primarily adds BPMN functionality for DAR review processes. It negotiates with [gateway-api](https://github.com/HDRUK/gateway-api) to determine processes and provide metrics around DAR processes.

## Application technical stack
- IDE: Intellij IDEA (For editing configuration and source code only)
- Java 8
- Spring Boot
- Spring Web
- Spring Data and Hibernate
- Lombok
- Postgre SQL (to deploy database tables to Docker container for integration tests)
- Google Secret Manager
- Docker (to run the Application, Redis, Postgre and SonarQ)
- SonarQ (Quality code analysis)


## Running the application locally

*Requirements*

- IDE: Intellij IDEA (For editing configuration and source code only)
- Java 1.8+ SDK installed
- Maven
- Docker
- Pg Admin (PostgreSQL web client)
- Postman or any other http client (to test API's on JSON format)

### Install Maven
Maven can be installed via brew or downloaded from the maven site directly

#### Brew
```bash
brew install maven
```

#### Maven Archive
Download https://maven.apache.org/download.cgi

Extract the contents of the downloaded archive to a folder on your computer.
Once done add the path to the extracted version of maven to the 'PATH' environment variable.
```bash
export PATH=$PATH:<MAVEN HOME FOLDER HERE>/bin
``` 

#### Check if maven has been installed

To check that maven has been installed from either of the two steps above you can run
```bash
mvn -v
```

### Install SDKMan to manage your java versions
SDKMan https://sdkman.io/install

#### Install an SDK version
```bash
sdk install java 11.0.8.hs-adpt
```

## Installation (Minimum Setup - Local Only)

#### Step 1
Clone the workflow repository
```bash
git clone https://github.com/HDRUK/hdruk-workflow
```

#### Step 2
Compiling source code
```bash
mvn clean package
```

#### Step 3
Build docker image for docker compose file
```bash
docker build -t hdruk-workflow:1.0 .
```

#### Step 4
Configure docker-compose.yml
```yml
version: '3.1'

services:
  postgresDb:
    image: postgres
    ports:
      - 5432:5432
    expose:
      - 5432
    environment:
      - POSTGRES_USER=YOUR_USER_HERE
      - POSTGRES_PASSWORD=YOUR_PASSWORD_HERE
    volumes:
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql

  sonarqube:
    image: sonarqube
    depends_on:
      - postgresDb
    ports:
      - "9000:9000"
    environment:
      - sonar.jdbc.username=YOUR_USER_HERE
      - sonar.jdbc.password=YOUR_PASSWORD_HERE
      - sonar.jdbc.url=jdbc:postgresql://postgresDb:5432/sonar

  redis: 
    image: redis
    ports:
      - 6379:6379

  gatewayworkflow:
    image: gateway-workflow:1.0
    depends_on:
      - postgresDb
      - redis
    ports: 
      - 8081:8080
    environment:
      - ENV=default
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - DATA_SOURCE=postgresDb
      - DATA_USER=YOUR_USER_HERE
      - DATA_PASS=YOUR_PASS_HERE
      - GATEWAY_API=http://YourIPV4Address:3001/
      - SECRET_JWT=JWT_HERE
      - VALIDATOR_CLASS=com.gateway.workflow.util.ValidatorJwt
      - SYSTEM_USER=SYSTEM_USER_OBJECTID
```
## Create Database initialization script
Create a init.sql file and put it in the same directory as the compose file. 
During the compose up the init.sql script will be used to create the databases.
 
```sql
CREATE DATABASE sonar;
CREATE DATABASE gateway_workflow;
```

## Running the docker-compose script
Using a terminal navigate to the dockerCompose folder in the project.

To start the containers run the following command in a terminal.
```bash
docker-compose up
```

To stop all containers and remove any stored data run
```bash
docker-compose down
```

## Validate Running of containers
To validate the containers are running execute. You should see 4 images listed; gateway-workflow:1.0, sonarqube, redis, postgres
```bash
docker ps
```

- Camunda portal - http://localhost:8081/camunda/app/welcome/default/
- Sonarqube - http://localhost:9000/
- PGAdmin - This can be gotten from the PGAdmin application. Enter the host, port, username and password to access the instance.

## License