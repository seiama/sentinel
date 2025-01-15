plugins {
  id("sentinel.conventions")
}

dependencies {
  api("com.discord4j:discord4j-core:3.3.0-M2")
  api("com.fasterxml.jackson.core:jackson-databind:2.18.2")
  implementation("com.google.guava:guava:33.4.0-jre")
  api("com.seiama:commons:1.0.0-SNAPSHOT")
  api("com.seiama:functional:1.0.0-SNAPSHOT")
  api("io.projectreactor.netty:reactor-netty-core:1.2.1")
  api("jakarta.persistence:jakarta.persistence-api:3.2.0")
  implementation("net.time4j:time4j-base:5.9.4")
  api("org.mongodb:bson:5.2.1")
  api("org.springframework.data:spring-data-mongodb:4.4.1")
  testImplementation(platform("org.junit:junit-bom:5.11.4"))
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
