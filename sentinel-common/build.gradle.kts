plugins {
  id("sentinel.conventions")
}

dependencies {
  api("com.discord4j:discord4j-core:3.3.0-20230223.014557-69")
  api("com.fasterxml.jackson.core:jackson-databind:2.14.2")
  api("io.projectreactor.netty:reactor-netty-core:1.1.4")
  api("jakarta.persistence:jakarta.persistence-api:3.1.0")
  api("org.mongodb:bson:4.8.2")
  api("org.springframework.data:spring-data-commons:3.0.3")
  api("org.springframework.data:spring-data-mongodb:4.0.3")
}
