plugins {
  id("sentinel.conventions")
  id("org.springframework.boot") version "3.3.1"
  id("io.spring.dependency-management") version "1.1.6"
  id("com.gorylenko.gradle-git-properties") version "2.4.2"
}

tasks.named<Jar>(JavaPlugin.JAR_TASK_NAME) {
  indraGit.applyVcsInformationToManifest(manifest)
}

dependencies {
  implementation(project(":sentinel-common"))
  implementation("com.discord4j:discord4j-core:3.3.0-20240616.140938-135")
  implementation("com.google.guava:guava:33.2.1-jre")
  implementation("com.seiama:commons:1.0.0-SNAPSHOT")
  implementation("com.seiama:functional:1.0.0-SNAPSHOT")
  implementation("io.r2dbc:r2dbc-h2")
  implementation("it.unimi.dsi:fastutil:8.5.13")
  implementation("net.time4j:time4j-base:5.9.3")
  implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
  implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  testImplementation(platform("org.junit:junit-bom:5.10.1"))
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
