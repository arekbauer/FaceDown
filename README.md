<p align="center">
  <img src="assets/app_logo.png" width=140px alt="FaceDown Logo" />
</p>

# FaceDown


> **Current Status: Closed Testing**  
> FaceDown is currently in the **Google Play Closed Testing** phase.
> 
> I am actively looking for testers to help bring this app to production! If you would like to provide feedback, please [reach out to me](#contact).  
> For now, you can download the APK here **[here](app-release.apk)**.

> **Note**: This repository is a technical showcase for a private productivity application called "FaceDown". It contains sanitized snippets of the core engineering challenges to demonstrate architectural and technical capabilities.

FaceDown is a minimal timer that enforces focus by requiring the user to place their phone **face down** on a table. If the phone is picked up or flipped over, the timer pauses, ensuring a distraction-free environment.  


<p align="center">
  <img src="assets/home3.png" width="30%" alt="Timer Face Up" />
  <img src="assets/stats.png" width="30%" alt="Weekly Stats" />
  <img src="assets/settings2.png" width="30%" alt="Dial Setup" />
</p>

## Engineering Highlights

### 1. Sensor Fusion & Coroutines
The core mechanic relies on the device's accelerometer. I implemented a clean `Flow`-based repository that bridges the legacy `SensorManager` callback API with modern Kotlin Coroutines.
- **Debouncing**: Logic ensures minor vibrations don't trigger state changes.
- **Battery Efficiency**: The listener is automatically unregistered when the UI lifecycle stops collecting the Flow (via `awaitClose`).
  
*(See [SensorLogic.kt](snippets/SensorLogic.kt) for the full implementation)*

### 2. Custom Canvas Drawing
Instead of using a heavy charting library for the simple weekly statistics, I built a custom **Jetpack Compose Canvas** component.
- **Performance**: Draws the entire chart in a single pass.
- **Animation**: Uses independent `Animatable` states for each bar to create a staggered "wave" entrance effect.
- **Responsiveness**: Calculates geometry dynamically based on available width.  

*(See [WeeklyStatsChart.kt](snippets/WeeklyStatsChart.kt) for the drawing logic)*

### 3. Reactive Foreground Service
The core timer loop runs within a robust **Android Foreground Service** to guarantee execution even when the device is dozing.
- **State Machine**: This reactive stream powers the "Grace Period" logic, automatically triggering the 10-second recovery window when the `OrientationState` shifts to `FaceUp`.
- **Haptics & Audio**: interacting with `Vibrator` and `AudioPlayer` for completion alarms and success feedback, respecting user preferences via `SettingsRepository`.

### 4. System Integration (Do Not Disturb)
The app interacts directly with the Android System Services to mute notifications during deep focus. This requires handling runtime permissions (`ACCESS_NOTIFICATION_POLICY`) and managing the Interruption Filter state safely. 

## Architecture
The app follows the recommended **Clean Architecture** guidelines, separating concerns into Data, Domain, and UI layers.

```text
com.arekb.facedown
├── data                 # Data Layer (Repositories, Sources)
│   ├── database         # Room Entities & DAOs
│   ├── sensor           # Accelerometer Implementation
│   └── timer            # System Service Wrappers
├── domain               # Domain Layer (Pure Kotlin)
│   └── model            # Core Business Models (FocusSession, etc)
├── ui                   # UI Layer (Jetpack Compose)
│   ├── home             # Timer & Canvas Charts
│   ├── stats            # Statistics & History
│   └── settings         # Statistics & History
└── di                   # Hilt Dependency Injection Modules
```

### Tech Stack
- Languages: Kotlin
- UI: Jetpack Compose (Material 3 Expressive)
- Architecture: MVVM, Clean Architecture, Single Activity
- DI: Hilt
- Async: Coroutines & Flow
- Local Data: Room Database
- Hardware: Android SensorManager (Accelerometer)


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

### Contact
[LinkedIn Profile](https://www.linkedin.com/in/arkadiusz-bauer/) | [Portfolio Website](https://arekbauer.com)
