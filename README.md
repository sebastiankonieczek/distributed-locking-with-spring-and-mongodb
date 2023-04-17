# Distributed Locking with Spring Boot and MongoDB

This is the example code of a One Time Password(OTP) service using distributed locking as described in this foojay post:

# How to run

First start the services in the docker compose file. It will start a mongodb database server and a mongo express instance.

```
docker-compose -f docker-compose.yml -p distributed-locking-wicth-spring-and-mongodb up -d
```

Next you can start the OTP service either using the ide or gradle.

For gradle simply execute:
```
./gradlew run
```
