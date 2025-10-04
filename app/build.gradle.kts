// build.gradle.kts (módulo 'app')
plugins {
    // ❌ ELIMINA el 'alias' si quieres evitar que cargue la versión antigua
    // alias(libs.plugins.android.application)

    // ✅ REEMPLAZA por el ID sin versión. Gradle usará la versión 8.3.0 de la raíz.
    id("com.android.application")

    id("org.jetbrains.kotlin.android") // o el alias de Kotlin sin apply false
    id("kotlin-parcelize")
}

android {
    namespace = "com.universidad.uniway"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.universidad.uniway"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    
    // Gson para serialización JSON
    implementation("com.google.code.gson:gson:2.10.1")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

dependencies {
    // ... dependencias existentes ...
    
    // Retrofit para llamadas HTTP
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // OkHttp para logging y interceptores
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines para operaciones asíncronas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}