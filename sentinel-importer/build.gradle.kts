plugins {
  id("sentinel.conventions")
}

dependencies {
  implementation(project(":sentinel-common"))
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.12.7")
  implementation("com.google.guava:guava:31.1-jre")
  implementation("it.unimi.dsi:fastutil:8.5.6")
  implementation("org.mongodb:mongodb-driver-reactivestreams:4.7.0")
}
