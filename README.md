# Compare Java Spring Boot containerization options

<!-- TOC -->
* [Compare Java Spring Boot containerization options](#compare-java-spring-boot-containerization-options)
  * [Application](#application)
  * [Containerization](#containerization)
    * [Gradle](#gradle)
      * [From Jar](#from-jar)
      * [Multistage](#multistage)
      * [Multistage (Gradle Wrapper)](#multistage-gradle-wrapper)
      * [Buildpack](#buildpack)
    * [Maven](#maven)
      * [From Jar](#from-jar-1)
      * [Multistage](#multistage-1)
      * [Multistage (Maven Wrapper)](#multistage-maven-wrapper)
      * [Buildpack](#buildpack-1)
<!-- TOC -->


---
## Application
this is simple hello-world application that expose two ports 8080, 8081

```bash
curl http://localhost:8080/hello
curl http://localhost:8081/actuator/health
curl http://localhost:8081/actuator/info
```

---
## Containerization

### Gradle
#### From Jar
```bash
./gradlew clean build
docker build -f docker/Dockerfile.gradle-jar -t containerized-app:gradle-jar .
```

#### Multistage
```bash
docker build -f docker/Dockerfile.gradle-multistage -t containerized-app:gradle-multistage .
```

#### Multistage (Gradle Wrapper)
```bash
docker build -f docker/Dockerfile.gradlew-multistage -t containerized-app:gradlew-multistage .
```

#### [Buildpack](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/#build-image.examples)
```bash
./gradlew bootBuildImage --imageName=containerized-app:gradle-buildpack
```
```bash
# Build image with git commit id as version
./gradlew bootBuildImage
```

### Maven
#### From Jar
```bash
./mvnw clean verify
docker build -f docker/Dockerfile.mvn-jar -t containerized-app:mvn-jar .
```

#### Multistage
```bash
docker build -f docker/Dockerfile.mvn-multistage -t containerized-app:mvn-multistage .
```

#### Multistage (Maven Wrapper)
```bash
docker build -f docker/Dockerfile.mvnw-multistage -t containerized-app:mvnw-multistage .
```

#### [Buildpack](https://docs.spring.io/spring-boot/docs/current/maven-plugin/reference/htmlsingle/#build-image.examples)
```bash
./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=containerized-app:mvn-buildpack
```
```bash
# Build image with git commit id as version
./mvnw spring-boot:build-image
```
