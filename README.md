# Recipes App
This project is a mobile recipe catalog, your personal electronic recipe book, but with convenient functionality.

## Main features
1. **Recipe management**:
- Adding new recipes.
- Editing existing recipes.
- Deleting recipes.
- Exporting recipes.

2. **Collections**:
- Adding dishes to collections.
- Availability of main system collections
- Creating custom collections
- Editing the name of custom collections
- Deleting custom collections
- Copying dishes from one collection to another
- Clearing a collection of dishes
- Getting a random dish from the collection by shaking the device

3. **Shopping lists**:
- Creating shopping lists.
- Editing shopping lists.
- Deleting shopping lists.
- Ability to generate a list based on selected recipes.

4. **ChatGPT API**:
- Translation of dish recipes.
- ChatGPT consultation via built-in chat.
- Ability to add ChatGPT suggested recipes to your collection.

> Unfortunately, the ChatGPT API is paid, so the number of translations and sent messages is limited.


## Tech
- **Programming language**: Java
- **Libraries**:
   - [RxJava3] for asynchronous work.
   - [Android Room] for database work.
   - [Android ViewModel] for state management.
   - [Google Material Design] for the interface.
   - [Glide] for asynchronous work with images
- **Tools**:
   - Android Studio Koala | 2024.1.1 Patch 1.

## Installation
To install the mobile application, you need to download the file _my_recipes.apk_ via the path _app/release_ and install it on the desired device.

## Code documentation
1. **General principles**
Our project uses a KDoc/JavaDoc-like syntax for documenting code. Proper documentation is crucial for understanding, maintaining, and extending the project.

2. **Documentation structure**
- Documenting methods
```java
/**
* Короткий опис призначення методу (що робить метод).
* За потреби додайте більш детальне пояснення в новому рядку.
*
* @param paramName Опис параметра
* @param anotherParam Опис іншого параметра
* @return Опис значення, що повертається
*/
```
- Documenting classes
```java
/**
 * @author Ім'я Прізвище
 * @version X.Y
 *
 * Короткий опис призначення класу.
 * Додаткові деталі про клас та його відповідальності.
 */
```

3. **Recommendations**
  - Be concise: Write clear and concise descriptions
  - Describe behavior: Focus on what the method does, not implementation details
  - Document exceptions: Use the @throws tag to describe possible exceptions
  - Mention side effects: If the method changes the state of the object, state this

4. **Documentation generation**
The Dokka plugin is used to generate documentation, which supports both Kotlin and Java code, including Android-specific classes.

To generate documentation, do the following:

```bash
./gradlew dokkaHtml
```

The generated documentation will be available in the directory `app/build/dokka/html`.

## Developer's Guide
1. **Required software**
   - Android Studio Koala | 2024.1.1 Patch 1
   - Java 17 or higher
   - Git
   - Gradle (built into Android Studio)
   - Android emulator or physical device

2. **Cloning a repository**
```bash
git clone https://github.com/DepressiveStar228/Recipes
cd Recipes
```

3. **Project launch**
   - Open the project in Android Studio.
   - Select Open an existing project and specify the path to the project folder.
   - Wait for Gradle to sync.
   - Select the desired emulator or physical device.
   - Click on the "Run" button (green triangle) in Android Studio.

## Development
In the future, it is planned to introduce advertising to provide a wider range of ChatGPT API users. It is also planned to add support for cloud saving and adding a recipe from text to a photo

## License
MIT

[RxJava3]: <https://github.com/ReactiveX/RxJava>
[Android Room]: <https://developer.android.com/jetpack/androidx/releases/room>
[Android ViewModel]: <https://developer.android.com/topic/libraries/architecture/viewmodel>
[Google Material Design]: <https://m3.material.io/>
[Glide]: <https://github.com/bumptech/glide>