# HDRUK Workflow

This is a Java/Spring Boot application, which acts as a host/platform for the [Camunda BPMN](https://camunda.com/). Primarily adds BPMN functionality for DAR review processes. It negotiates with [gateway-api](https://github.com/HDRUK/gateway-api) to determine processes and provide metrics around DAR processes.

## Application technical stack
- Java 8
- Spring Boot
- Spring Web
- Spring Data and Hibernate
- Lombok
- Postgre SQL (to deploy database tables to Docker container for integration tests)
- Google Storage
- Google Secret Manager
- Docker (to run the Application, Redis, Postgre and SonarQ)
- SonarQ (Quality code analysis)


## Running the application locally

*Requirements*

- Java 1.8+ SDK installed
- Maven
- Docker
- Pg Admin (PostgreSQL web client)
- Postman or any other http client (to test API's on JSON format)


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

  hdrukWorkflow:
    image: hdruk-workflow:1.0
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
```

## License