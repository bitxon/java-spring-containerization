# Compare Java Spring Boot containerization options

<!-- TOC -->
* [Compare Java Spring Boot containerization options](#compare-java-spring-boot-containerization-options)
  * [Application](#application)
  * [Containerization](#containerization)
    * [Gradle](#gradle)
    * [Maven](#maven)
<!-- TOC -->


---
## Application
this is simple hello-world application that expose two ports 8080, 8081

```bash
curl http://localhost:8080/hello
```
```bash
curl http://localhost:8081/actuator/health
```
