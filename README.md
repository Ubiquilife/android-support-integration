# Android Support Integration

Drop-in Jetpack Compose support widget — floating help button + ticket
sheet, posts to a Ubiquilife Support backend.

## Install

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://jitpack.io") }
    }
}

// app/build.gradle.kts
dependencies {
    implementation("life.ubiqui:android-support-integration:0.1.0")
}
```

## Use

```kotlin
@Composable
fun App() {
    val client = remember {
        SupportClient(SupportConfig(
            apiBaseUrl = "https://support.ubiqui.life/external-api",
            apiKey = BuildConfig.SUPPORT_KEY,
            appName = "Acme Android",
        ))
    }

    Box(Modifier.fillMaxSize()) {
        YourScreen()
        SupportFAB(
            client = client,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
        )
    }
}
```

## Headless

```kotlin
val ok = supportClient.createTicket(SupportTicketDraft(
    title = "Crash on save",
    description = "Tapping save throws."
))
```

## Requirements

- Android API 24+
- Compose BOM 2024.02+ / Material 3
- Kotlin 1.9+

## Locales

`en, es, ja, tl, mi` via `res/values-*` resource folders.

## License

MIT.
