plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.ebeepmoblie"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.ebeepmoblie"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    testImplementation("junit:junit:4.13.2")
}
