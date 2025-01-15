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
  maven("https://oss.sonatype.org/content/repositories/snapshots/")
  maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
  checkstyle("ca.stellardrift:stylecheck:0.2.0")
  errorprone("com.google.errorprone:error_prone_core:2.18.0")
  compileOnlyApi("org.jetbrains:annotations:23.1.0")
}
