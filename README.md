# Animated Border Library

A lightweight Android library to animate traveling borders around MaterialCardView.

## 📦 Installation (via JitPack)

Add the JitPack repository to your root `build.gradle`:
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency:
```gradle
implementation 'com.github.altaf0550:animated-border:v1.0.0'
```

## 🛠️ Usage

```kotlin
val animator = BorderAnimator().animateCardBorder(myCardView, context)
// To cancel:
animator.cancel()
```

## 🧪 Preview
