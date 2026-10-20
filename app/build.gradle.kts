plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.ebeep.mobile"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.ebeep.mobile"
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
    packaging {
        resources {
            excludes += setOf("META-INF/INDEX.LIST", "META-INF/DEPENDENCIES", "META-INF/*.kotlin_module")
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // MQTT client: the same protocol the beeper uses
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

    // Claude API, for the reply suggestion in the chat
    implementation("com.anthropic:anthropic-java:2.34.0")

    testImplementation("junit:junit:4.13.2")
}
