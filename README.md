# Distributed Locking with Spring Boot and MongoDB

This is the example code of a One Time Password(OTP) service using distributed locking as described in this foojay post:

## How to run

First start the services in the docker compose file. It will start a mongodb database server and a mongo express instance.

```
docker-compose -f docker-compose.yml -p distributed-locking-wicth-spring-and-mongodb up -d
```

Next you can start the OTP service either using the ide or gradle.

For gradle simply execute:
```
./gradlew run
```

## How to use

See file `lock-test.http`. 

If you are using intellij idea IDE, you can simply execute the http requests.

But before you must set the port of the application in the `http-client.env.json` file.

You can find the applications server port in the Spring logs, for example:
```
o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 50084 (http) with context path ''
```

## How to test
