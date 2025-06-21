plugins {
  id("com.diffplug.spotless")
  id("net.kyori.indra")
  id("net.kyori.indra.checkstyle")
  id("net.kyori.indra.git")
  id("net.ltgt.errorprone")
}

indra {
  github("seiama", "sentinel") {
    ci(true)
  }

  mitLicense()

  javaVersions {
    target(17)
  }
}

spotless {
  java {
    endWithNewline()
    importOrderFile(rootProject.file(".spotless/seiama.importorder"))
    indentWithSpaces(2)
    trimTrailingWhitespace()
  }
}

repositories {
  mavenCentral()
  maven("https://central.sonatype.com/repository/maven-snapshots/")
  // TODO: https://central.sonatype.org/news/20250326_ossrh_sunset/
  maven("https://oss.sonatype.org/content/repositories/snapshots/") // TODO: When move to another D4J SNAPSHOT PLEASE REMOVE THIS!
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") // TODO: Need for commons of sentinel, first move that things
}

dependencies {
  checkstyle("ca.stellardrift:stylecheck:0.2.1")
  errorprone("com.google.errorprone:error_prone_core:2.36.0")
  compileOnlyApi("org.jetbrains:annotations:26.0.1")
}
