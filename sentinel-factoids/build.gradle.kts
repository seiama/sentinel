plugins {
  id("sentinel.conventions")
  id("org.springframework.boot") version "3.4.1"
  id("io.spring.dependency-management") version "1.1.7"
  id("com.gorylenko.gradle-git-properties") version "2.4.2"
}

tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME) {
  indraGit.applyVcsInformationToManifest(manifest)
}

dependencies {
  implementation(project(":sentinel-common"))
  implementation("com.discord4j:discord4j-core:3.3.0-SNAPSHOT")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
  implementation("com.google.guava:guava:33.4.0-jre")
  implementation("it.unimi.dsi:fastutil:8.5.15")
  implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.jsoup:jsoup:1.19.1")
  implementation("com.github.Doc94:javadoc-search:bb80b75a3a")
  implementation("com.github.freya022:remark-java:4a18f84801")
  testImplementation(platform("org.junit:junit-bom:5.11.4"))
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
