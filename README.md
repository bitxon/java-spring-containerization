# Compare Java Spring Boot containerization options

<!-- TOC -->
* [Compare Java Spring Boot containerization options](#compare-java-spring-boot-containerization-options)
  * [Application](#application)
  * [Containerization](#containerization)
    * [Gradle](#gradle)
      * [Dockerfile](#dockerfile)
      * [Buildpack](#buildpack)
      * [Jib](#jib)
    * [Maven](#maven)
      * [Dockerfile](#dockerfile-1)
      * [Buildpack](#buildpack-1)
      * [Jib](#jib-1)
  * [Memory Optimization](#memory-optimization)
    * [Classic Dockerfile build image optimization](#classic-dockerfile-build-image-optimization)
    * [Buildpack Memory Optimization](#buildpack-memory-optimization)
    * [Jib Memory Optimization](#jib-memory-optimization)
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
#### Dockerfile
```bash
./gradlew clean build
docker build -f docker/Dockerfile.gradle-jar -t containerized-app:gradle-jar .
```
```bash
docker build -f docker/Dockerfile.gradle-multistage -t containerized-app:gradle-multistage .
```
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

#### [Jib](https://github.com/GoogleContainerTools/jib/blob/master/jib-gradle-plugin/README.md)
```bash
./gradlew jibDockerBuild --image=containerized-app:gradle-jib
```

### Maven
#### Dockerfile
```bash
./mvnw clean verify
docker build -f docker/Dockerfile.mvn-jar -t containerized-app:mvn-jar .
```
```bash
docker build -f docker/Dockerfile.mvn-multistage -t containerized-app:mvn-multistage .
```
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

#### [Jib](https://github.com/GoogleContainerTools/jib/blob/master/jib-maven-plugin/README.md)
```bash
./mvnw compile jib:dockerBuild -Dimage=containerized-app:mvn-jib
```

---
## Memory Optimization

`Total Container Memory` = `Heap Memory` + `Non-Heap Memory`\
`Heap Memory` = `-Xmx`\
`Non-Heap Memory` = `-XX:MaxMetaspaceSize` + `-XX:ReservedCodeCacheSize` + `-XX:MaxDirectMemorySize` + (`-Xss` × `Thread Count`)

---
### Classic Dockerfile build image optimization

Buildpack Memory Calculator output for `2048 Mb` container memory with `250 threads`

| Component                     | Default         | My Custom |
|-------------------------------|-----------------|-----------|
| **MaxHeapSize**               | **512**         | **1024**  |
| **MaxMetaspaceSize**          | **No limit** ⚠️ | **256**   |
| ↳ CompressedClassSpace        | ↳               | ↳ 128     |
| ↳ Regular Metaspace           | ↳               | ↳ 128     |
| **ReservedCodeCacheSize**     | **240**         | **128**   |
| **MaxDirectMemorySize**       | **No limit** ⚠️ | **64**    |
| **ThreadStackSize** (250×2MB) | **500**         | **250**   |
| **Headroom**                  | **N/A** ⚠️      | **326**   |

**Summary**: ⚠️ Requires complete JVM tuning to fit in memory limits.

<details>
<summary>Scripts</summary>

```shell
docker run --rm -it --name jvm-default --memory=2g \
  -e 'JAVA_TOOL_OPTIONS=-XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal' \
  -p 8080:8080 \
  containerized-app:gradlew-multistage
```
```shell
docker run --rm -it --name jvm-custom --memory=2g \
  -e 'JAVA_TOOL_OPTIONS=-Xms512m -Xmx1024m -XX:MaxMetaspaceSize=256m -XX:CompressedClassSpaceSize=128m -XX:ReservedCodeCacheSize=128m -XX:MaxDirectMemorySize=64m -Xss1m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal' \
  -p 8081:8080 \
  containerized-app:gradlew-multistage
```

</details>

---
### Buildpack Memory Optimization
[Paketo Jvm config](https://paketo.io/docs/howto/java/#configure-the-jvm-at-runtime)\
[Paketo Memory Calculator](https://paketo.io/docs/reference/java-reference/#memory-calculator)\
[BellSoft Memory Calculator](https://github.com/paketo-buildpacks/bellsoft-liberica)

Buildpack Memory Calculator output for `2048 Mb` container memory with `250 threads`

| Component                     | Default   | Headroom 20 | My Custom | Build pack defaults                              |
|-------------------------------|-----------|-------------|-----------|--------------------------------------------------|
| **GC**                        | **G1**    | **G1**      | **G1**    |                                                  |
| **MaxHeapSize**               | **1466**  | **1056**    | **1024**  | ⚡️Calculated = `Total` - `Non-Heap` - `Headroom` |
| **MaxMetaspaceSize**          | **82** ⚠️ | **82** ⚠️   | **256**   | ⚡️Calculated based on class count                |
| ↳ CompressedClassSpace        | ↳ 80      | ↳ 80        | ↳ 128     |                                                  |
| ↳ Regular Metaspace           | ↳ 2       | ↳ 2         | ↳ 128     |                                                  |
| **ReservedCodeCacheSize**     | **240**   | **240**     | **128**   |                                                  |
| **MaxDirectMemorySize**       | **10**    | **10**      | **64**    |                                                  |
| **ThreadStackSize** (250×1MB) | **250**   | **250**     | **250**   |                                                  |
| **Headroom**                  | **0** ⚠️  | **410**     | **326**   |                                                  |

Buildpack Memory Calculator output for `1024 Mb` container memory with `250 threads`

| Component                     | Default        | Headroom 20    | My Custom | Build pack defaults                              |
|-------------------------------|----------------|----------------|-----------|--------------------------------------------------|
| **GC**                        | **Serial**  ⚠️ | **Serial**  ⚠️ | **G1**    |                                                  |
| **MaxHeapSize**               | **442**        | **237**        | **256**   | ⚡️Calculated = `Total` - `Non-Heap` - `Headroom` |
| **MaxMetaspaceSize**          | **82** ⚠️      | **82** ⚠️      | **256**   | ⚡️Calculated based on class count                |
| ↳ CompressedClassSpace        | ↳ 80           | ↳ 80           | ↳ 128     |                                                  |
| ↳ Regular Metaspace           | ↳ 2            | ↳ 2            | ↳ 128     |                                                  |
| **ReservedCodeCacheSize**     | **240**        | **240**        | **128**   |                                                  |
| **MaxDirectMemorySize**       | **10**         | **10**         | **64**    |                                                  |
| **ThreadStackSize** (250×1MB) | **250**        | **250**        | **250**   |                                                  |
| **Headroom**                  | **0** ⚠️       | **205**        | **70**    |                                                  |

**Summary**: ✅ Tuning size of Headroom might be enough for general use cases.

<details>
<summary>Scripts</summary>

```shell
docker run --rm -it --name jvm-default --memory=2g \
  -e 'JAVA_TOOL_OPTIONS=-XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal' \
  -p 8080:8080 \
  containerized-app:gradle-buildpack
```
```shell
docker run --rm -it --name jvm-headroom-20 --memory=2g \
  -e 'BPL_JVM_HEAD_ROOM=20' \
  -e 'JAVA_TOOL_OPTIONS=-XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal' \
  -p 8080:8080 \
  containerized-app:gradle-buildpack
```
```shell
docker run --rm -it --name jvm-custom --memory=2g \
  -e 'JAVA_TOOL_OPTIONS=-Xms512m -Xmx1024m -XX:MaxMetaspaceSize=256m -XX:CompressedClassSpaceSize=128m -XX:ReservedCodeCacheSize=128m -XX:MaxDirectMemorySize=64m -Xss1m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError -XX:+PrintCommandLineFlags -XX:+PrintFlagsFinal' \
  -p 8081:8080 \
  containerized-app:gradle-buildpack
```

</details>

---
### Jib Memory Optimization
TBD
