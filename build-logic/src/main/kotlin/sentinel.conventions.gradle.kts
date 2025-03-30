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
    target(21)
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
  maven("https://oss.sonatype.org/content/repositories/snapshots/")
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
  maven("https://jitpack.io")
}

dependencies {
  checkstyle("ca.stellardrift:stylecheck:0.2.1")
  errorprone("com.google.errorprone:error_prone_core:2.36.0")
  compileOnlyApi("org.jetbrains:annotations:26.0.1")
}
