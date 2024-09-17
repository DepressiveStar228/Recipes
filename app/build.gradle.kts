plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.recipes"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.recipes"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas".toString()
            }
        }
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
}

dependencies {
    implementation ("io.reactivex.rxjava3:rxjava:3.1.6")
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.2")
    implementation ("androidx.room:room-runtime:2.5.0")
    annotationProcessor ("androidx.room:room-compiler:2.5.0")
    implementation ("androidx.room:room-rxjava3:2.5.0")

    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.0")
    implementation ("com.google.code.gson:gson:2.8.8")
    implementation ("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation ("androidx.appcompat:appcompat:1.4.0")
    implementation ("androidx.recyclerview:recyclerview:1.2.1")
    implementation ("androidx.fragment:fragment:1.4.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.2.1")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation ("androidx.room:room-testing:2.5.0")
    androidTestImplementation ("androidx.test:runner:1.6.2")
    androidTestImplementation ("androidx.test:rules:1.6.1")
    testImplementation("androidx.test:core:1.6.1")

    testImplementation ("org.robolectric:robolectric:4.10.3")
    testImplementation ("io.reactivex.rxjava3:rxjava:3.1.9")
    testImplementation ("io.reactivex.rxjava3:rxandroid:3.0.2")
}