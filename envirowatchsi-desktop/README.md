This is a Kotlin Multiplatform project targeting Desktop (JVM).

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Desktop app:
  - Hot reload: `./gradlew :desktopApp:hotRun --auto`
  - Standard run: `./gradlew :desktopApp:run`

### Login and database writes

Creating, updating, and deleting records requires an admin JWT from the API. Use the `Login` screen in the desktop app with an admin user's email and password. The desktop app stores the returned token in memory and automatically sends it with database write requests.

For demos without logging in through the desktop UI, you can still copy `admin-token.example.txt` to `admin-token.txt` and paste an admin JWT there.

`admin-token.txt` is ignored by git, so secrets are not committed. You can also provide the same token through the `ENVIROWATCHSI_ADMIN_TOKEN` environment variable.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Desktop tests: `./gradlew :shared:jvmTest`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
