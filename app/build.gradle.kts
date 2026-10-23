import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// Private values (broker address, API key) come from secrets.properties, which is not in git
val secrets = Properties()
val secretsFile = rootProject.file("secrets.properties")
if (secretsFile.exists()) {
    secretsFile.inputStream().use { secrets.load(it) }
}

fun secret(name: String): String = "\"" + secrets.getProperty(name, "") + "\""

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

        // These become constants in the generated BuildConfig class
        buildConfigField("String", "BROKER_HOST", secret("BROKER_HOST"))
        buildConfigField("String", "BROKER_PORT", secret("BROKER_PORT"))
        buildConfigField("String", "GOOGLE_API_KEY", secret("GOOGLE_API_KEY"))
    }

    buildFeatures {
        buildConfig = true
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
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // MQTT client: the same protocol the beeper uses
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")

    testImplementation("junit:junit:4.13.2")
}
