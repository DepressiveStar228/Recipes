# Instructions for updating the mobile application
This project does not have production servers and does not use a CI/CD infrastructure. An update consists of creating a new APK build and publishing it to a GitHub repository.

## Preparing for the update
- Make sure all code changes are complete
- Make sure all tests are passing
- Make sure the app is working as expected

## Building the APK
- Android Studio > Build > Build Bundle(s) / APK(s) > Build APK(s)
- Get the **app-release.apk** file

## Publishing
- Add the compiled **.apk** to the repository in the **/releases** folder
- Commit changes to the **main** branch

## Update on device
- Download the new .apk version of the application to your mobile device
- Install the new version of the application