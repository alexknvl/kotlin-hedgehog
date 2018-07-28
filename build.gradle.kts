plugins {
  base
  kotlin("jvm") version "1.2.51"
  jacoco
}

repositories {
  mavenCentral()
}

group   = "com.alexknvl"
version = "0.0.1-SNAPSHOT"

jacoco {
  reportsDir = file("$buildDir/jacocoReportDir")
}

dependencies {
  compile(kotlin("stdlib-jdk8"))
  compile("io.arrow-kt:arrow-data:0.7.2")
  compile("io.arrow-kt:arrow-typeclasses:0.7.2")

  testCompile("junit:junit:4.12")
}
