plugins {
   java
   application
   id("org.springframework.boot") version "3.0.2"
   id("io.spring.dependency-management") version "1.1.0"
}

group = "de.sko.distributed-locking"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_19

repositories {
   mavenCentral()
}

application {
   mainClass.set("de.sko.distributedlocking.DistributedLockingWithSpringAndMongodbApplication")
}


dependencies {
   implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
   implementation("org.springframework.boot:spring-boot-starter-web")
   implementation("org.springframework.boot:spring-boot-starter-integration")
   implementation("org.springframework.integration:spring-integration-jdbc:6.0.2")

   developmentOnly("org.springframework.boot:spring-boot-devtools")

   testImplementation("org.springframework.boot:spring-boot-starter-test")
   testImplementation("org.testcontainers:junit-jupiter:1.17.6")
   testImplementation("org.testcontainers:mongodb:1.17.6")
   testImplementation("org.testcontainers:testcontainers:1.17.6")
}

tasks.withType<Test> {
   useJUnitPlatform()
}
