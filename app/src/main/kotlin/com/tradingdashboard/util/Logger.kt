package com.tradingdashboard.util

import android.util.Log

/**
 * Logging utility using Android's Log class
 * Can be easily swapped with Timber or other logging library
 */
object Logger {

    private const val DEFAULT_TAG = "TradingDashboard"

    enum class LogLevel {
        VERBOSE, DEBUG, INFO, WARNING, ERROR
    }

    /**
     * Log verbose message
     */
    fun v(message: String, tag: String = DEFAULT_TAG, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.v(tag, message, throwable)
            } else {
                Log.v(tag, message)
            }
        }
    }

    /**
     * Log debug message
     */
    fun d(message: String, tag: String = DEFAULT_TAG, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.d(tag, message, throwable)
            } else {
                Log.d(tag, message)
            }
        }
    }

    /**
     * Log info message
     */
    fun i(message: String, tag: String = DEFAULT_TAG, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.i(tag, message, throwable)
        } else {
            Log.i(tag, message)
        }
    }

    /**
     * Log warning message
     */
    fun w(message: String, tag: String = DEFAULT_TAG, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }

    /**
     * Log error message
     */
    fun e(message: String, tag: String = DEFAULT_TAG, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    /**
     * Log with object value
     */
    fun <T> d(obj: T, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, obj?.toString() ?: "null")
        }
    }

    /**
     * Log section header
     */
    fun section(title: String, tag: String = DEFAULT_TAG) {
        d("╔═════════════════════════════════════════", tag)
        d("║ $title", tag)
        d("╚═════════════════════════════════════════", tag)
    }

    /**
     * Log method entry
     */
    fun methodEntry(methodName: String, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) {
            d("→ $methodName()", tag)
        }
    }

    /**
     * Log method exit
     */
    fun methodExit(methodName: String, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) {
            d("← $methodName()", tag)
        }
    }

    /**
     * Log method exit with result
     */
    fun <T> methodExit(methodName: String, result: T, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) {
            d("← $methodName() = $result", tag)
        }
    }

    /**
     * Log performance timing
     */
    fun timingStart(operation: String, tag: String = DEFAULT_TAG): Long {
        if (BuildConfig.DEBUG) {
            d("⏱ Starting: $operation", tag)
        }
        return System.currentTimeMillis()
    }

    /**
     * Log performance timing end
     */
    fun timingEnd(operation: String, startTime: Long, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) {
            val duration = System.currentTimeMillis() - startTime
            d("⏱ Completed: $operation in ${duration}ms", tag)
        }
    }

    /**
     * Log network request
     */
    fun logRequest(url: String, method: String = "GET", tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) {
            d("🌐 $method $url", tag)
        }
    }

    /**
     * Log network response
     */
    fun logResponse(
        url: String,
        statusCode: Int,
        duration: Long,
        tag: String = DEFAULT_TAG
    ) {
        if (BuildConfig.DEBUG) {
            val emoji = when {
                statusCode in 200..299 -> "✓"
                statusCode in 300..399 -> "→"
                statusCode in 400..499 -> "⚠"
                else -> "✗"
            }
            d("$emoji $statusCode from $url (${duration}ms)", tag)
        }
    }

    /**
     * Log database operation
     */
    fun logDatabaseOperation(operation: String, table: String, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) {
            d("💾 $operation on $table", tag)
        }
    }

    /**
     * Log event
     */
    fun logEvent(event: String, data: Map<String, Any>? = null, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) {
            val dataStr = data?.let { " $it" } ?: ""
            d("📊 Event: $event$dataStr", tag)
        }
    }

    /**
     * Log screen view
     */
    fun logScreenView(screenName: String, tag: String = DEFAULT_TAG) {
        if (BuildConfig.DEBUG) {
            d("📱 Screen: $screenName", tag)
        }
    }

    /**
     * Log error with full stack trace
     */
    fun logError(message: String, throwable: Throwable, tag: String = DEFAULT_TAG) {
        e(message, tag, throwable)
        if (BuildConfig.DEBUG) {
            throwable.printStackTrace()
        }
    }

    /**
     * Log crash
     */
    fun logCrash(throwable: Throwable, tag: String = DEFAULT_TAG) {
        e("🚨 CRASH", tag, throwable)
        throwable.printStackTrace()
    }

    /**
     * Clear logs (useful for testing)
     */
    fun clear() {
        // Implement if using Timber or other library
    }
}

/**
 * Extension function for Result to log automatically
 */
fun <T> Result<T>.logResult(
    operation: String,
    tag: String = "TradingDashboard"
): Result<T> {
    when (this) {
        is Result.Success -> Logger.d("✓ $operation succeeded", tag)
        is Result.Error -> Logger.e("✗ $operation failed: ${this.getErrorMessage()}", tag)
        is Result.Loading -> Logger.d("⏳ $operation loading...", tag)
    }
    return this
}

/**
 * Extension function for trade operations logging
 */
fun logTradeOperation(
    operation: String,
    symbol: String,
    tag: String = "TradingDashboard"
) {
    Logger.d("🔄 $operation: $symbol", tag)
}

/**
 * Extension function for sync logging
 */
fun logSyncOperation(
    operation: String,
    success: Boolean,
    tag: String = "TradingDashboard"
) {
    val emoji = if (success) "✓" else "✗"
    Logger.d("$emoji Sync: $operation", tag)
}
