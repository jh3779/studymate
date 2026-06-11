import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.services)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}

val teamDebugKeystore = rootProject.file("team-debug.keystore")
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) load(keystorePropertiesFile.inputStream())
}
val teamDebugStorePassword = keystoreProperties.getProperty("storePassword")
val teamDebugKeyAlias = keystoreProperties.getProperty("keyAlias")
val teamDebugKeyPassword = keystoreProperties.getProperty("keyPassword")
val hasTeamDebugFiles = teamDebugKeystore.exists() || keystorePropertiesFile.exists()
val hasTeamDebugSigning = teamDebugKeystore.exists()
        && keystorePropertiesFile.exists()
        && !teamDebugStorePassword.isNullOrBlank()
        && !teamDebugKeyAlias.isNullOrBlank()
        && !teamDebugKeyPassword.isNullOrBlank()

if (hasTeamDebugFiles && !hasTeamDebugSigning) {
    throw GradleException(
        "Team debug signing is incomplete. Add both team-debug.keystore and " +
                "keystore.properties with storePassword, keyAlias, and keyPassword."
    )
}

android {
    namespace = "com.example.studymate"
    compileSdk = 36

    signingConfigs {
        getByName("debug") {
            if (hasTeamDebugSigning) {
                storeFile = teamDebugKeystore
                storePassword = teamDebugStorePassword
                keyAlias = teamDebugKeyAlias
                keyPassword = teamDebugKeyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "com.example.studymate"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "AI_BASE_URL",
            "\"${localProperties.getProperty("ai.base.url", "")}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
