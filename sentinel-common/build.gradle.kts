plugins {
  id("sentinel.conventions")
}

dependencies {
  api("com.discord4j:discord4j-core:3.3.0-20230516.003010-85")
  api("com.fasterxml.jackson.core:jackson-databind:2.14.2")
  api("com.google.guava:guava:31.1-jre")
  api("com.seiama:commons:1.0.0-SNAPSHOT")
  api("io.projectreactor.netty:reactor-netty-core:1.1.4")
  api("jakarta.persistence:jakarta.persistence-api:3.1.0")
  api("org.mongodb:bson:4.8.2")
  api("org.springframework.data:spring-data-mongodb:4.0.3")
  testImplementation(platform("org.junit:junit-bom:5.8.2"))
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("org.junit.jupiter:junit-jupiter-params")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
