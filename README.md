# DirectAppUpdate 📲

DirectAppUpdate is an open-source in-app update library using Jetpack Compose to update your Android app without relying on the Play Store or any other app store. Instead, updates are managed via a private server using a configurable JSON URL.

<p align="center">
  <img src="https://user-images.githubusercontent.com/12345678/DirectAppUpdate-1.gif" alt="DirectAppUpdate Demo 1" width="45%" />
  <img src="https://user-images.githubusercontent.com/12345678/DirectAppUpdate-2.gif" alt="DirectAppUpdate Demo 2" width="45%" />
</p>

## Getting Started 🚀

### Library Setup

#### In your `settings.gradle`
```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

#### In your project `build.gradle` 
[![](https://jitpack.io/v/Micoder-dev/DirectAppUpdate.svg)](https://jitpack.io/#Micoder-dev/DirectAppUpdate)
```gradle
dependencies {
    implementation 'com.github.Micoder-dev:DirectAppUpdate:Tag'
}
```

### JSON Config Setup

Create a configuration file for your app updates:

```json
{
  "apkFileName": "new_release.apk",
  "appName": "My App",
  "downloadUrl": "https://example/new_release.apk",
  "immediateUpdate": false,
  "releaseNotes": "- Exciting Update\n - Bug Fixes",
  "versionCode": 2,
  "versionName": "2.0.0"
}
```

## Implementation Methods 🔧

### 1st Way: Simple Implementation using Jetpack Compose

```kotlin
val configUrl = "https://example/micoder.json"
DirectAppUpdate(activity = this@MainActivity, configUrl = configUrl, appIcon = R.mipmap.ic_launcher)
```

The `DirectAppUpdate` is a composable function that can be used within your main activity's composable theme or any other composable function where you can check for update status.

### 2nd Way: Custom Implementation

```kotlin
@Composable
fun DirectAppUpdate(activity: Activity, configUrl: String, notificationViewModel: NotificationViewModel = hiltViewModel(), appIcon: Int) {

    val updateDialogState = remember { mutableStateOf(UpdateDialogState()) }

    val directAppUpdateManager = remember { DirectAppUpdateManager.Builder(activity) }

    LaunchedEffect(key1 = true) {
        directAppUpdateManager.fetchUpdateConfig(
            configUrl = configUrl,
            onSuccess = { builder ->
                builder.setDirectUpdateListener(object : DirectUpdateListener {
                    override fun onImmediateUpdateAvailable() {
                        updateDialogState.value = updateDialogState.value.copy(
                            visible = true,
                            updateType = UpdateType.Immediate,
                            status = "Immediate Update Available",
                            showUpdateButton = true
                        )
                    }

                    override fun onFlexibleUpdateAvailable() {
                        updateDialogState.value = updateDialogState.value.copy(
                            visible = true,
                            updateType = UpdateType.Flexible,
                            status = "Flexible Update Available",
                            showUpdateButton = true
                        )
                    }

                    override fun onAlreadyUpToDate() {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Already up to date",
                            showUpdateButton = false
                        )
                    }

                    override fun onDownloadStart() {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Download started",
                            showUpdateButton = false
                        )
                    }

                    override fun onProgress(progress: Float) {
                        notificationViewModel.showProgress(progress = progress.toInt(), icon = appIcon)
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Downloading: $progress%",
                            progress = progress
                        )
                    }

                    override fun onDownloadComplete() {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Download complete",
                            showUpdateButton = false
                        )
                    }

                    override fun onDownloadFailed(error: String) {
                        updateDialogState.value = updateDialogState.value.copy(
                            status = "Download failed: $error",
                            showUpdateButton = false
                        )
                    }
                }).build().checkForUpdate()
            },
            onError = { error ->
                Toast.makeText(activity, error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    UpdateDialog(
        dialogState = updateDialogState.value,
        onUpdateClick = { directAppUpdateManager.build().startUpdate() },
        onCancelClick = {
            if (updateDialogState.value.updateType == UpdateType.Flexible) {
                updateDialogState.value = updateDialogState.value.copy(visible = false)
            }
        }
    )

}
```

This method provides more customization options. Users can create custom dialogs or use `DirectUpdateListener` overrides as per their needs.

## Contributions 🙌

We welcome contributions! Please fork the repository, create a new branch, and submit a pull request. For major changes, please open an issue first to discuss what you would like to change.

Don't forget to give this repo a ⭐ if you found it useful!

## License 📄

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

Happy Coding! 💻
