plugins {
   java
   id("org.springframework.boot") version "3.0.2"
   id("io.spring.dependency-management") version "1.1.0"
}

group = "de.sko.distributed-locking"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_19

repositories {
   mavenCentral()
}

dependencies {
   implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
   implementation("org.springframework.boot:spring-boot-starter-web")
   implementation("org.springframework.boot:spring-boot-starter-integration")
   implementation("org.springframework.integration:spring-integration-jdbc:6.0.2")
   developmentOnly("org.springframework.boot:spring-boot-devtools")
   testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
   useJUnitPlatform()
}
