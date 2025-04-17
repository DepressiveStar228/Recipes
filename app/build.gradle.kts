plugins {
    id("com.android.application")
    id("org.jetbrains.dokka") version "1.9.10"
}

android {
    namespace = "com.example.recipes"
    compileSdk = 34

    packaging {
        resources {
            excludes += setOf("META-INF/INDEX.LIST")
            excludes += setOf("META-INF/io.netty.versions.properties")
        }
    }

    defaultConfig {
        applicationId = "com.example.recipes"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.4"

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
        sourceCompatibility = JavaVersion.VERSION_16
        targetCompatibility = JavaVersion.VERSION_16
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
    moduleName.set(project.name)

    dokkaSourceSets {
        create("main") {
            moduleName.set("app")
            sourceRoots.from("src/main/java")

            includeNonPublic.set(false)
            skipEmptyPackages.set(true)
            suppressInheritedMembers.set(true)

            externalDocumentationLink {
                url.set(uri("https://developer.android.com/reference/").toURL())
                packageListUrl.set(uri("https://developer.android.com/reference/androidx/package-list").toURL())
            }

            jdkVersion.set(16)

            noAndroidSdkLink.set(false)
        }
    }
}

dependencies {
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // RxJava
    implementation("io.reactivex.rxjava3:rxjava:3.1.9")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")

    // Room
    implementation("androidx.room:room-runtime:2.5.0")
    annotationProcessor("androidx.room:room-compiler:2.5.0")
    implementation("androidx.room:room-rxjava3:2.5.0")

    // Azure OpenAI
    implementation("com.azure:azure-ai-openai-assistants:1.0.0-beta.3")

    // Other libraries
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.fragment:fragment:1.4.0")
    implementation("androidx.preference:preference:1.2.1")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.2")

    // Testing
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.room:room-testing:2.5.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    testImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("org.mockito:mockito-android:5.5.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
    testImplementation("org.robolectric:robolectric:4.10.3")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata:2.6.1")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.1")
    implementation("androidx.lifecycle:lifecycle-reactivestreams:2.6.1")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")

    // Flexbox
    implementation("com.google.android.flexbox:flexbox:3.0.0")
}