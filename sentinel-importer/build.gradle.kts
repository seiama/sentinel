plugins {
  id("sentinel.conventions")
}

dependencies {
  implementation(project(":sentinel-common"))
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")
  implementation("com.google.guava:guava:33.0.0-jre")
  implementation("it.unimi.dsi:fastutil:8.5.12")
  implementation("org.mongodb:mongodb-driver-reactivestreams:4.11.1")
}
