# 🚀 Quick Start Guide - Android Development

## 1️⃣ Install Android Studio

Download Android Studio Giraffe or newer from:
https://developer.android.com/studio

## 2️⃣ Clone Repository

```bash
git clone <repository-url>
cd crypto-trading-android
```

## 3️⃣ Open in Android Studio

```bash
# Open Android Studio, then File → Open → Select project directory
# Or from terminal:
open -a Android\ Studio .
```

## 4️⃣ Install Android SDK

Android Studio will prompt to install SDK components:
- ✅ Android SDK Platform 34
- ✅ Android SDK Build-Tools 34.x
- ✅ Google Play services
- ✅ Emulator (optional, for testing on physical device instead)

## 5️⃣ Create/Configure Emulator (Optional)

```bash
# Open AVD Manager in Android Studio
# Create Pixel 4 or similar device
# Min API: 24 (Android 7.0)
```

## 6️⃣ Update Supabase Credentials

Edit `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "SUPABASE_URL", "\"https://your-supabase-url.supabase.co\"")
buildConfigField("String", "SUPABASE_ANON_KEY", "\"your-anon-key-here\"")
```

Get these from: Supabase Dashboard → Settings → API

## 7️⃣ Sync Gradle

In Android Studio:
- File → Sync Now
- Or: `./gradlew sync`

Wait for all dependencies to download (~5-10 min on first run)

## 8️⃣ Run App

### Option A: On Emulator
```bash
# Select AVD from Device Manager
# Click Run (▶️) button
# Or: ./gradlew installDebug
```

### Option B: On Physical Device
```bash
# Enable Developer Mode on device
# Enable USB Debugging
# Connect USB cable
# Click Run button in Android Studio
```

## 9️⃣ Verify App Works

You should see:
- ✅ Bottom navigation with 5 tabs
- ✅ Dashboard screen with metrics cards
- ✅ Trade list (initially empty)
- ✅ Dark theme with green accent

## 🔧 Troubleshooting

### Gradle Sync Fails
```bash
# Clean gradle cache
./gradlew clean
./gradlew build --refresh-dependencies
```

### Emulator Won't Start
```bash
# List available emulators
emulator -list-avds

# Start emulator
emulator -avd <emulator-name>
```

### App Crashes on Launch
- Check Android Studio logcat for errors
- Ensure Supabase URL/keys are correct
- Check minimum SDK is API 24+

### Dependencies Not Resolving
```bash
# Update gradle wrapper
./gradlew wrapper --gradle-version latest

# Then sync again
./gradlew sync
```

## 📝 Next Steps

### 1. Understand the Architecture
- Read: `README.md` - Full documentation
- Read: `ARCHITECTURE.md` (will be created)

### 2. Set Up IDE
- Open `app/build.gradle.kts` - Understand dependencies
- Open `app/src/main/AndroidManifest.xml` - Understand permissions
- Open `app/src/main/kotlin/com/tradingdashboard/MainActivity.kt` - Entry point

### 3. Explore Current Code
- **Data Layer:** `app/src/main/kotlin/com/tradingdashboard/data/`
- **UI Layer:** `app/src/main/kotlin/com/tradingdashboard/presentation/`
- **Theme:** `app/src/main/kotlin/com/tradingdashboard/presentation/theme/`

### 4. Make First Change
Try:
1. Change text in DashboardScreen
2. Rebuild and run
3. Observe the change in app

## 🧑‍💻 Development Tips

### Debug Mode
Android Studio includes:
- Debugger (set breakpoints)
- Profiler (CPU, Memory, Network)
- Logcat (view logs)
- Layout Inspector (inspect UI hierarchy)

### Hot Reload
Changes to Kotlin code:
- Android Studio auto-compiles on save
- Click "Run" to rebuild and deploy to device

### Database Inspector
- View local Room DB contents:
  - Run → Debug 'app'
  - Click "Database Inspector" tab
  - View tables and data

## 📚 Resources

**Learning:**
- [Android Basics - Google](https://developer.android.com/courses/android-basics-compose/course)
- [Kotlin by JetBrains](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose - Google](https://developer.android.com/jetpack/compose)

**Documentation:**
- [Android API Reference](https://developer.android.com/reference)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [MVVM Pattern](https://developer.android.com/jetpack/guide)

**Tools:**
- [Android Studio Docs](https://developer.android.com/studio/intro)
- [ADB (Android Debug Bridge)](https://developer.android.com/tools/adb)

## 🎯 Current Project Status

| Component | Status | Notes |
|-----------|--------|-------|
| Project Setup | ✅ Complete | Ready to develop |
| Database | ✅ Complete | Room DB configured |
| API Client | ✅ Complete | Supabase ready |
| UI Framework | ✅ Complete | Jetpack Compose setup |
| Navigation | ✅ Complete | Bottom nav working |
| Theming | ✅ Complete | Dark/light theme ready |
| Dashboard | 🟡 MVP | Metrics display working |
| Trades CRUD | 🟡 MVP | Basic structure ready |
| OCR | ❌ Pending | Phase 2 |
| Analytics | ❌ Pending | Phase 2 |
| Auth | ❌ Pending | Hardcoded user ID |
| Settings | ❌ Pending | Phase 2 |

## ✅ Verification Checklist

After setup, verify:
- [ ] Android Studio opens project without errors
- [ ] Gradle syncs successfully
- [ ] App builds without errors
- [ ] App launches on emulator/device
- [ ] Dashboard screen visible
- [ ] Bottom navigation responds to taps
- [ ] Logcat shows no critical errors
- [ ] Database Inspector shows `trades` table

## 🚀 Ready to Code!

You're all set! Start with:
1. Explore `DashboardScreen.kt` - understand UI structure
2. Explore `TradeRepository.kt` - understand data flow
3. Explore `DashboardViewModel.kt` - understand state management
4. Make a small change to test hot reload

Happy coding! 🎉

---

**Questions?** Check `README.md` or review the inline code comments.
