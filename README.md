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

## Development
In the future, it is planned to introduce advertising to provide a wider range of ChatGPT API users. It is also planned to add support for cloud saving and adding a recipe from text to a photo

## License
MIT

[RxJava3]: <https://github.com/ReactiveX/RxJava>
[Android Room]: <https://developer.android.com/jetpack/androidx/releases/room>
[Android ViewModel]: <https://developer.android.com/topic/libraries/architecture/viewmodel>
[Google Material Design]: <https://m3.material.io/>
[Glide]: <https://github.com/bumptech/glide>