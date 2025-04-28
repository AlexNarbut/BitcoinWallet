plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kapt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "com.test.bitcoinapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.test.bitcoinapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "MEMPOOL_API_BASE_URL", "\"https://mempool.space/signet/api/\"")
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)

    implementation (libs.androidx.navigation.compose)
    implementation (libs.androidx.hilt.navigation.compose)

    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)

    implementation(projects.core.common)
    implementation(projects.core.commonExtens)
    implementation(projects.core.mempoolApi)
    implementation(projects.core.uikit)

    implementation(projects.feature.transaction.api)
    implementation(projects.feature.transaction.data)

    implementation(projects.feature.wallet.api)
    implementation(projects.feature.wallet.data)
    implementation(projects.feature.wallet.currentState)
    implementation(projects.feature.wallet.sendCoins)
}