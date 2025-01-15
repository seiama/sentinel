pluginManagement {
  includeBuild("build-logic")
  repositories {
    gradlePluginPortal()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version("0.2")
}

rootProject.name = "sentinel-parent"

sequenceOf(
  "sentinel-api",
  "sentinel-app",
  "sentinel-common",
  "sentinel-factoids",
  "sentinel-importer"
).forEach {
  include(it)
}
