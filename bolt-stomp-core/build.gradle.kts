plugins {
  id("java-library")
  id("org.jetbrains.kotlin.jvm")
}

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
  api(project(":bolt-core"))
  api(libs.okhttp)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.uuid)
}