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
  implementation("com.discord4j:discord4j-core:3.3.0-RC2")
  implementation("com.seiama:commons:1.0.0-SNAPSHOT")
  implementation("com.seiama:functional:1.0.0-SNAPSHOT")
  implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
}
