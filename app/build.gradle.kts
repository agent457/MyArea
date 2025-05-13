plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.myarea"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myarea"
        minSdk = 30
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.navigation:navigation-fragment:2.6.0")
    implementation("androidx.navigation:navigation-ui:2.6.0")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Mapsforge
    implementation ("com.github.mapsforge.mapsforge:mapsforge-core:0.21.0")
    implementation ("com.github.mapsforge.mapsforge:mapsforge-map:0.21.0")
    implementation ("com.github.mapsforge.mapsforge:mapsforge-map-reader:0.21.0")
    implementation ("com.github.mapsforge.mapsforge:mapsforge-themes:0.21.0")
    implementation ("com.github.mapsforge.mapsforge:mapsforge-map-android:0.21.0")
    implementation ("com.caverock:androidsvg:1.4")
    implementation ("com.github.mapsforge.mapsforge:mapsforge-core:0.21.0")
    implementation ("com.github.mapsforge.mapsforge:mapsforge-poi:0.21.0")
    implementation ("com.github.mapsforge.mapsforge:mapsforge-poi-android:0.21.0")
    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}