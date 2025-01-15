plugins {
  id("sentinel.conventions")
}

dependencies {
  implementation(project(":sentinel-common"))
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.2")
  implementation("com.google.guava:guava:33.4.0-jre")
  implementation("it.unimi.dsi:fastutil:8.5.15")
  implementation("org.mongodb:mongodb-driver-reactivestreams:5.0.1")
}
