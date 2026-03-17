<p align="center">
  <img src="assets/app_logo.png" width=15% alt="FaceDown Logo" />
  <br>
  <h1 align="center">FaceDown</h1>
</p>

<p align="center">
<a href='https://play.google.com/store/apps/details?id=com.arekb.facedown'>
  <img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width="250"/>
</a>
</p>

> **Note**: This repository is a technical showcase for a private productivity application called "FaceDown". It contains sanitized snippets of the core engineering challenges to demonstrate architectural and technical capabilities.

<p align="center">
  <a href="https://www.youtube.com/watch?v=iY3FBMTA15A&t=327s">
    <img src="https://img.youtube.com/vi/iY3FBMTA15A/0.jpg" width="600px" alt="FaceDown featured on HowToMen" />
  </a>
  <br>
  <i>Featured in <b>HowToMen's</b> "Top 15 Best Android Apps" - "Honestly a really great app to help you stay more productive."</i>
</p>

---
FaceDown is a minimal timer that enforces focus by requiring the user to place their phone **face down** on a table. If the phone is picked up or flipped over, the timer pauses, ensuring a distraction-free environment.  

<p align="center">
  <img src="assets/home3.png" width="30%" alt="Timer Face Up" />
  <img src="assets/stats.png" width="30%" alt="Weekly Stats" />
  <img src="assets/settings2.png" width="30%" alt="Dial Setup" />
</p>

## Engineering Highlights

### 1. Robust Foreground Service & State Bridging
The core timer loop runs within an **Android Foreground Service** (using the new `SPECIAL_USE` type) to guarantee precision execution even when the device is dozing. 
- **In-Memory State Bus**: To communicate seamlessly with the UI, a singleton `TimerRepository` manages a `MutableStateFlow` that bridges the Service and any observing ViewModels.
- **Grace Period Logic**: The service reacts to a combined flow of the timer ticker and physical orientation, automatically triggering a 10-second recovery window when the user picks up the phone.
- **WakeLock Management**: Carefully manages `PARTIAL_WAKE_LOCK` across all pause, failure, and completion states to prevent battery drain.

### 2. Sensor Fusion & Coroutines
The core physical mechanic relies on the device's accelerometer. I implemented a clean `Flow`-based repository that bridges the legacy `SensorManager` callback API with modern Kotlin Coroutines.
- **Debouncing**: `distinctUntilChanged()` ensures minor vibrations don't trigger state updates.
- **Battery Efficiency**: The listener is registered as a cold `callbackFlow`, automatically unregistering when no longer collected (e.g., when the service stops).
  
*(See [SensorLogic.kt](snippets/SensorLogic.kt) for the full implementation)*

### 3. Glance AppWidgets & Lifecycle Sync
To provide quick access to focus stats without opening the app, I built home screen widgets using **Jetpack Glance**. 
- **Reactive Syncing**: Uses a `ProcessLifecycleOwner` observer to trigger a custom `SyncWidgetsUseCase` only when the app enters the foreground, ensuring widget data (daily minutes, current streak) stays perfectly in sync with the Room database without unnecessary background processing.

### 4. Custom Compose Drawing
Instead of relying on heavy charting libraries for the weekly statistics, I built custom **Jetpack Compose Canvas** components.
- **Performance**: Draws the entire chart geometry dynamically in a single pass based on available screen width.
- **Animation**: Uses independent `Animatable` states for each bar to create a staggered "wave" entrance effect.

*(See [WeeklyStatsChart.kt](snippets/WeeklyStatsChart.kt) for the drawing logic)*

### 5. Comprehensive Testing Infrastructure
The app's complex state rules are heavily tested across unit and integration layers:
- **Unit Testing**: Pure business logic (streak calculation, UseCases) and ViewModels are verified using `JUnit 4`, `MockK`, and `kotlinx-coroutines-test`.
- **UI & E2E Testing**: Composable screens and critical flows are tested using `ComposeTestRule` and `HiltTestRunner` on Android instrumentation.
- **Database Mocking**: Utilizes a custom `MockSessionInjector` for programmatic local database seeding during development and structured DB tests.

## Architecture
The app follows the recommended **Clean Architecture** guidelines and **MVVM** pattern, strictly separating concerns:

```text
com.arekb.facedown
├── data                 # Data Layer
│   ├── database         # Room Entities, DAOs, & Mocking Injector
│   ├── sensor           # Accelerometer callbackFlow wrappers
│   ├── timer            # ForegroundService & Repository State Bridges
│   └── widget           # DataStore Preferences for Widgets
├── domain               # Domain Layer
│   ├── model            # Sealed Interfaces for State (TimerState)
│   └── usecase          # Coordinated logic (e.g., SyncWidgetsUseCase)
├── ui                   # Presentation Layer (Jetpack Compose)
│   ├── home             # Timer UI & State Sub-components
│   ├── stats            # Statistics, History Paging, Canvas Charts
│   ├── settings         # User Preferences
│   └── widget           # Glance AppWidget Implementations
└── di                   # Hilt Dependency Injection Modules
```

### Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3 Expressive)
- **Widgets**: Jetpack Glance AppWidgets
- **Architecture**: MVVM, Clean Architecture
- **DI**: Hilt (Dagger) with KSP
- **Async**: Coroutines & Flow (`StateFlow` / `callbackFlow`)
- **Local Data**: Room Database (with Paging 3), Jetpack DataStore
- **Testing**: JUnit 4, MockK, Compose UI Tests, Hilt Android Testing
- **Hardware/System**: Android `SensorManager` (Accelerometer), Foreground Services, WakeLocks, `NotificationManager` (DnD)



## App Showcase
<details>
<summary>View Full Screenshot Gallery</summary>
<br>

<p align="center">
  <img src="assets/home0.png" width="30%" />
  <img src="assets/history.png" width="30%" />
  <img src="assets/timer_options.png" width="30%" />
  <img src="assets/warning.png" width="30%" />
  <img src="assets/stats_dark.png" width="30%" />
  <img src="assets/settings_dark.png" width="30%" />
</p>

</details>

## Credits & Contributiors
* UI/UX inspired by the [Tomato App](https://github.com/nsh07/Tomato).
* **Babrr**: New icon design

### Contact
[LinkedIn Profile](https://www.linkedin.com/in/arkadiusz-bauer/) | [Portfolio Website](https://arekbauer.com)
