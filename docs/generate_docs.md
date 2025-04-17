# Instructions for connecting auto-generation of documentation for the project

**Prerequisites**
- Gradle (Kotlin DSL or Groovy) is used
- Java/Kotlin code with comments in KDoc/Javadoc format

**Project settings**
- Adding Dokta to the project

In the build.gradle.kts file (at the module level) we add:

```kotlin
plugins {
    id("org.jetbrains.dokka") version "1.9.10"
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
```

- Launching the generation
Run the command:
```bash
./gradlew dokkaHtml
```
The generated documentation will be located in the build/dokka directory of the module.


        