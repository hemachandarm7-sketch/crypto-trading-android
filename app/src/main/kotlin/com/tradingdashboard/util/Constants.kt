package com.tradingdashboard.util

import com.tradingdashboard.presentation.viewmodel.Currency

/**
 * App-wide constants and configuration
 */
object Constants {

    // Supabase Configuration
    object Supabase {
        const val URL = "https://hqjdcqofotscnhuppqgd.supabase.co"
        const val ANON_KEY = "sb_publishable_L1EgmZf6JURBj9Uggoisrg_tHNYAib"
        
        // Table names
        const val TABLE_TRADES = "trades"
        const val TABLE_SETTINGS = "user_settings"
        const val TABLE_SCREENSHOTS = "screenshots"
        const val TABLE_TRADE_EVENTS = "trade_events"
    }

    // Database Configuration
    object Database {
        const val NAME = "trading_dashboard_db"
        const val VERSION = 1
    }

    // User Configuration
    object User {
        const val DEFAULT_USER_ID = "user_1" // TODO: Replace with auth user ID
        const val DEFAULT_CURRENCY = "USD"
        const val DEFAULT_THEME = "DARK"
    }

    // Trade Configuration
    object Trade {
        const val MIN_LEVERAGE = 1.0
        const val MAX_LEVERAGE = 100.0
        const val DEFAULT_LEVERAGE = 10.0
        
        const val MIN_QUANTITY = 0.0001
        const val MAX_QUANTITY = 1000000.0
        
        const val POSITION_LONG = "LONG"
        const val POSITION_SHORT = "SHORT"
        
        const val STATUS_OPEN = "OPEN"
        const val STATUS_CLOSED = "CLOSED"
    }

    // Currency Configuration
    object CurrencyConfig {
        val SUPPORTED_CURRENCIES = listOf(
            Currency.USD,
            Currency.EUR,
            Currency.GBP,
            Currency.INR,
            Currency.JPY
        )
        
        val CURRENCY_SYMBOLS = mapOf(
            Currency.USD to "$",
            Currency.EUR to "€",
            Currency.GBP to "£",
            Currency.INR to "₹",
            Currency.JPY to "¥"
        )
        
        val CURRENCY_NAMES = mapOf(
            Currency.USD to "US Dollar",
            Currency.EUR to "Euro",
            Currency.GBP to "British Pound",
            Currency.INR to "Indian Rupee",
            Currency.JPY to "Japanese Yen"
        )
    }

    // UI Configuration
    object UI {
        const val ANIMATION_DURATION_SHORT = 200 // ms
        const val ANIMATION_DURATION_MEDIUM = 300 // ms
        const val ANIMATION_DURATION_LONG = 500 // ms
        
        const val DECIMAL_PLACES_PRICE = 4
        const val DECIMAL_PLACES_AMOUNT = 2
        const val DECIMAL_PLACES_PERCENTAGE = 1
        
        const val EMPTY_STATE_ICON = "📊"
        const val LOADING_MESSAGE = "Loading..."
        const val ERROR_MESSAGE_DEFAULT = "Something went wrong. Please try again."
    }

    // Network Configuration
    object Network {
        const val TIMEOUT_SECONDS = 30L
        const val RETRY_ATTEMPT_COUNT = 3
        const val RETRY_DELAY_MS = 1000L
        
        const val CACHE_SIZE_MB = 50L
    }

    // Pagination
    object Pagination {
        const val PAGE_SIZE = 20
        const val PREFETCH_DISTANCE = 5
    }

    // Sync Configuration
    object Sync {
        const val AUTO_SYNC_INTERVAL_MINUTES = 5
        const val MANUAL_SYNC_TIMEOUT_SECONDS = 30
    }

    // OCR Configuration
    object OCR {
        const val MIN_CONFIDENCE = 0.6f
        const val MAX_IMAGE_SIZE_MB = 10
        const val SUPPORTED_FORMATS = listOf("jpg", "jpeg", "png", "webp")
    }

    // Date/Time Configuration
    object DateTime {
        const val DATE_FORMAT = "MMM d, yyyy"
        const val TIME_FORMAT = "HH:mm:ss"
        const val DATETIME_FORMAT = "MMM d, yyyy HH:mm"
        const val ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    }

    // Preferences Keys
    object PreferencesKeys {
        const val KEY_USER_ID = "user_id"
        const val KEY_THEME = "theme"
        const val KEY_CURRENCY = "currency"
        const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        const val KEY_AUTO_SYNC_ENABLED = "auto_sync_enabled"
        const val KEY_LAST_SYNC = "last_sync"
        const val KEY_FIRST_LAUNCH = "first_launch"
    }

    // Error Messages
    object ErrorMessages {
        const val NETWORK_ERROR = "Network error. Please check your connection."
        const val INVALID_INPUT = "Invalid input. Please check your data."
        const val TRADE_NOT_FOUND = "Trade not found."
        const val SYNC_FAILED = "Failed to sync with server."
        const val OCR_FAILED = "Failed to extract trade data. Please try again."
        const val CAMERA_ERROR = "Camera error. Please check permissions."
        const val PHOTO_PICKER_ERROR = "Failed to pick photo."
        const val DATABASE_ERROR = "Database error. Please try again."
        const val UNKNOWN_ERROR = "Unknown error occurred."
    }

    // Success Messages
    object SuccessMessages {
        const val TRADE_SAVED = "Trade saved successfully."
        const val TRADE_UPDATED = "Trade updated successfully."
        const val TRADE_DELETED = "Trade deleted successfully."
        const val SETTINGS_SAVED = "Settings saved successfully."
        const val SYNC_COMPLETE = "Sync completed successfully."
    }

    // Validation Rules
    object Validation {
        const val MIN_SYMBOL_LENGTH = 2
        const val MAX_SYMBOL_LENGTH = 20
        
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_LENGTH = 128
        
        const val REGEX_EMAIL = "^[A-Za-z0-9+_.-]+@(.+)$"
        const val REGEX_SYMBOL = "^[A-Za-z0-9/]+$"
    }

    // Feature Flags
    object Features {
        const val ENABLE_BIOMETRIC = true
        const val ENABLE_PUSH_NOTIFICATIONS = false
        const val ENABLE_OFFLINE_MODE = true
        const val ENABLE_CLOUD_BACKUP = false
    }
}
