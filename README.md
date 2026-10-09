# Trading Dashboard - Android App

A native Android application that mirrors the web-based crypto trading dashboard with full offline support, Supabase integration, and OCR-powered trade extraction.

## 🎯 Project Overview

**Status:** MVP - Core Foundation Ready  
**Platform:** Android 7.0+ (API 24+)  
**Language:** Kotlin  
**UI Framework:** Jetpack Compose  
**Database:** Room (SQLite)  
**Backend:** Supabase (PostgreSQL)

## ✨ Features

### Current (Phase 1 - MVP)
- ✅ Dashboard with metrics
- ✅ Trade list with CRUD operations
- ✅ Local database caching
- ✅ Supabase sync (ready to implement)
- ✅ Dark/Light theme support
- ✅ Bottom navigation (5 tabs)

### Planned (Phase 2+)
- 📷 Camera capture + OCR
- 📊 Advanced analytics & charts
- 🔔 Push notifications
- ⚙️ User settings
- 💾 Cloud backup & sync
- 🔐 Biometric authentication

## 🏗️ Architecture

```
app/
├── data/
│   ├── local/           # Room Database
│   │   ├── dao/         # Database Access Objects
│   │   └── database/    # Database configuration
│   ├── model/           # Data entities
│   ├── remote/          # Supabase client
│   └── repository/      # Data repository pattern
├── domain/              # Business logic (future)
├── presentation/
│   ├── screens/         # Composable screens
│   ├── viewmodel/       # MVVM ViewModels
│   ├── theme/           # UI theming
│   └── navigation/      # Navigation setup
└── di/                  # Dependency Injection (Hilt)
```

## 🛠️ Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Language | Kotlin | 1.9.20 |
| UI | Jetpack Compose | 1.6.0 |
| Architecture | MVVM + Repository | Latest |
| Database | Room | 2.5.2 |
| Backend | Supabase (Kotlin SDK) | 0.2.5 |
| Networking | Ktor Client | 2.3.6 |
| Dependency Injection | Hilt | 2.47 |
| OCR (Phase 2) | MLKit Text Recognition | 16.0.0 |
| Charts (Phase 2) | MPAndroidChart | 3.1.0 |

## 📋 Project Setup

### Prerequisites
- Android Studio Giraffe or newer
- JDK 17+
- Android SDK API 34
- Gradle 8.2+

### Installation

1. **Clone the repository**
```bash
git clone <repo-url>
cd crypto-trading-android
```

2. **Open in Android Studio**
```bash
open -a Android\ Studio .
```

3. **Sync Gradle**
- File → Sync Now
- Wait for dependencies to download

4. **Configure Supabase Credentials**
- Update `BuildConfig` in `app/build.gradle.kts`:
  - `SUPABASE_URL`: Your Supabase URL
  - `SUPABASE_ANON_KEY`: Your Supabase anon key

5. **Run the app**
```bash
# On emulator
./gradlew installDebug

# Or use Android Studio Run button
```

## 📱 Project Structure

### Data Layer
- **TradeEntity**: Room entity for local storage
- **TradeDao**: Database queries
- **TradeRepository**: Data access abstraction
- **SupabaseClient**: Remote API client

### Presentation Layer
- **DashboardScreen**: Metrics & overview
- **TradesScreen**: Trade management
- **UploadScreen**: Screenshot & OCR (placeholder)
- **AnalyticsScreen**: Charts & stats (placeholder)
- **SettingsScreen**: User preferences (placeholder)

### UI Components
- **MetricCard**: Reusable metric display
- **TradeItem**: Trade list item
- **BottomNavBar**: Tab navigation

## 🚀 Development Workflow

### 1. Database Operations
```kotlin
// Add trade
tradeRepository.addTrade(trade)

// Get trades
tradeRepository.getUserTradesFlow(userId).collect { trades ->
    // Update UI
}

// Get stats
val stats = tradeRepository.getTradeStats(userId)
```

### 2. ViewModel Usage
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: TradeRepository
) : ViewModel() {
    val trades = repository.getUserTradesFlow(userId)
}
```

### 3. Compose UI
```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val trades by viewModel.trades.collectAsState()
    LazyColumn {
        items(trades) { trade ->
            TradeItem(trade)
        }
    }
}
```

## 📊 Database Schema

### trades table
| Column | Type | Notes |
|--------|------|-------|
| id | String | Primary key |
| userId | String | Foreign key |
| symbol | String | Trade pair (e.g., BTC/USDT) |
| position | String | LONG or SHORT |
| leverage | Double | Leverage multiplier |
| entryPrice | Double | Entry price |
| exitPrice | Double | Exit price |
| quantity | Double | Trade quantity |
| margin | Double | Margin used |
| pnlAmount | Double | Profit/Loss in USD |
| pnlPercentage | Double | P&L percentage |
| status | String | OPEN or CLOSED |
| openTime | Long | Timestamp |
| closeTime | Long | Timestamp |

### settings table
| Column | Type | Notes |
|--------|------|-------|
| userId | String | Primary key |
| currency | String | USD, INR, etc. |
| theme | String | DARK or LIGHT |
| biometricEnabled | Boolean | Fingerprint auth |
| autoSyncEnabled | Boolean | Auto-sync trades |

## 🔐 Security

- ✅ Encrypted SharedPreferences for sensitive data
- ✅ HTTPS only for Supabase
- ✅ Row-level security (RLS) in Supabase
- ✅ Build secrets in BuildConfig (no hardcoding)
- ✅ ProGuard obfuscation for release builds

## 📈 Performance

- ✅ Local database caching (offline-first)
- ✅ Pagination for large datasets
- ✅ LazyColumn for efficient scrolling
- ✅ Coroutines for async operations
- ✅ Flow for reactive data streams

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests (on device)
./gradlew connectedAndroidTest

# Run specific test
./gradlew test -Dtest=TradeRepositoryTest
```

## 📝 API Reference

### TradeRepository
```kotlin
// Get all trades
suspend fun getUserTrades(userId: String): List<Trade>

// Get flow of trades (reactive)
fun getUserTradesFlow(userId: String): Flow<List<Trade>>

// Add trade
suspend fun addTrade(trade: Trade): Result<Unit>

// Update trade
suspend fun updateTrade(trade: Trade): Result<Unit>

// Delete trade
suspend fun deleteTrade(tradeId: String): Result<Unit>

// Get statistics
suspend fun getTradeStats(userId: String): TradeStats

// Sync with server
suspend fun syncWithServer(userId: String): Result<Unit>
```

## 🐛 Known Issues & TODOs

- [ ] Authentication screen (currently hardcoded user ID)
- [ ] Trade detail screen
- [ ] OCR implementation (Phase 2)
- [ ] Analytics charts (Phase 2)
- [ ] Settings implementation
- [ ] Push notifications
- [ ] iOS version

## 📦 Build & Release

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Signed Release
```bash
./gradlew bundleRelease
```

## 🔄 Sync Strategy

The app uses an **offline-first** approach:

1. **Local writes**: All trades saved to Room DB immediately
2. **Background sync**: Syncs with Supabase periodically
3. **Conflict resolution**: Server data takes precedence
4. **Retry logic**: Failed syncs retry with exponential backoff

## 📚 Resources

- [Kotlin Docs](https://kotlinlang.org/docs/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt DI](https://developer.android.com/training/dependency-injection/hilt-android)
- [Supabase Kotlin SDK](https://supabase.com/docs/reference/kotlin/introduction)

## 🤝 Contributing

1. Create feature branch: `git checkout -b feature/your-feature`
2. Make changes
3. Commit: `git commit -am 'Add feature'`
4. Push: `git push origin feature/your-feature`
5. Open Pull Request

## 📄 License

MIT License - See LICENSE file for details

## 🆘 Support

For issues or questions:
1. Check GitHub Issues
2. Review Architecture Documentation
3. Contact development team

---

**Last Updated:** October 5, 2026  
**Version:** 1.0.0-MVP  
**Status:** Ready for Development
