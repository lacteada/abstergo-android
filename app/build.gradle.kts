import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
}

extensions.configure<ApplicationExtension>("android") {
    namespace = "cl.lacteada.abstergo"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "cl.lacteada.abstergo"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // Host de desarrollo, con certificado autofirmado.
            buildConfigField("String", "BASE_URL", "\"https://18.209.251.129/\"")
        }
        release {
            // Cuando exista el dominio de producción, se cambia acá.
            buildConfigField("String", "BASE_URL", "\"https://18.209.251.129/\"")
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    implementation("com.airbnb.android:lottie:6.7.1")
    implementation("com.github.f0ris.sweetalert:library:1.6.2")

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.security.crypto)
}
