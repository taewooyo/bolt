import com.taewooyo.buildsrc.Configuration

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
  id(libs.plugins.android.application.get().pluginId)
  id(libs.plugins.kotlin.android.get().pluginId)
}

android {
  namespace = "com.taewooyo.bolt"
  compileSdk = Configuration.compileSdk

  defaultConfig {
    applicationId = "com.taewooyo.bolt"
    minSdk = Configuration.minSdk
    targetSdk = Configuration.targetSdk
    versionCode = Configuration.versionCode
    versionName = Configuration.versionName
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  buildFeatures {
    compose = true
  }

  composeOptions {
    kotlinCompilerExtensionVersion = libs.versions.androidxComposeCompiler.get()
  }

  lint {
    abortOnError = false
  }
}

dependencies {

  implementation(libs.androidx.material)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(libs.androidx.compose.material)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.compose.constraintlayout)

  implementation("com.squareup.okhttp3:okhttp:4.9.2")
  implementation(project(":bolt-okhttp"))
  implementation(project(":bolt-stomp-core"))

//  implementation("org.hildan.krossbow:krossbow-stomp-core:5.7.0")
//  implementation("org.hildan.krossbow:krossbow-websocket-okhttp:5.7.0")
}