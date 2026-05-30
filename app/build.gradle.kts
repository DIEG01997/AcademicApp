plugins {
    id("com.android.application")
}

val configuredApiBaseUrl = providers.gradleProperty("ACADEMIC_APP_API_URL")
    .orElse(providers.environmentVariable("ACADEMIC_APP_API_URL"))

val debugApiBaseUrl = configuredApiBaseUrl.orElse("http://192.168.18.41:8080/")
val releaseApiBaseUrl = providers.provider {
    configuredApiBaseUrl.orNull
        ?: throw GradleException("Define ACADEMIC_APP_API_URL para compilar una build release.")
}

android {
    namespace = "com.example.academicapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.academicapp"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            manifestPlaceholders["usesCleartextTraffic"] = "true"
            buildConfigField("String", "API_BASE_URL", "\"${debugApiBaseUrl.get()}\"")
        }

        release {
            isMinifyEnabled = false
            manifestPlaceholders["usesCleartextTraffic"] = "false"
            buildConfigField("String", "API_BASE_URL", "\"${releaseApiBaseUrl.get()}\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.0")
    implementation ("com.google.code.gson:gson:2.10")
}
