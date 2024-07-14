plugins {
  id("sentinel.conventions")
}

dependencies {
  implementation(project(":sentinel-common"))
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
  implementation("com.google.guava:guava:33.2.1-jre")
  implementation("it.unimi.dsi:fastutil:8.5.13")
  implementation("org.mongodb:mongodb-driver-reactivestreams:5.0.1")
}
