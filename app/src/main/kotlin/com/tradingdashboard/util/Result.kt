package com.tradingdashboard.util

/**
 * Result wrapper for better error handling
 * Similar to Kotlin's Result but with more control
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
    object Loading : Result<Nothing>()

    /**
     * Check if result is success
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Check if result is error
     */
    fun isError(): Boolean = this is Error

    /**
     * Check if result is loading
     */
    fun isLoading(): Boolean = this is Loading

    /**
     * Get data or null
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    /**
     * Get error or null
     */
    fun getErrorOrNull(): Exception? = when (this) {
        is Error -> exception
        else -> null
    }

    /**
     * Get error message
     */
    fun getErrorMessage(): String = when (this) {
        is Error -> exception.message ?: "Unknown error"
        else -> ""
    }

    /**
     * Apply transformation on success
     */
    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> Loading
    }

    /**
     * Apply transformation that returns Result
     */
    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(data)
        is Error -> this
        is Loading -> Loading
    }

    /**
     * Execute function on success
     */
    fun onSuccess(block: (T) -> Unit): Result<T> = apply {
        if (this is Success) block(data)
    }

    /**
     * Execute function on error
     */
    fun onError(block: (Exception) -> Unit): Result<T> = apply {
        if (this is Error) block(exception)
    }

    /**
     * Execute function on loading
     */
    fun onLoading(block: () -> Unit): Result<T> = apply {
        if (this is Loading) block()
    }

    /**
     * Get data or throw exception
     */
    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> throw IllegalStateException("Result is still loading")
    }

    /**
     * Get data or default value
     */
    fun getOrDefault(default: T): T = getOrNull() ?: default

    companion object {
        /**
         * Create success result
         */
        fun <T> success(data: T): Result<T> = Success(data)

        /**
         * Create error result
         */
        fun <T> error(exception: Exception): Result<T> = Error(exception)

        /**
         * Create error result from message
         */
        fun <T> error(message: String): Result<T> = Error(Exception(message))

        /**
         * Create loading result
         */
        fun <T> loading(): Result<T> = Loading as Result<T>

        /**
         * Execute block and wrap result
         */
        fun <T> tryBlock(block: () -> T): Result<T> = try {
            Success(block())
        } catch (e: Exception) {
            Error(e)
        }

        /**
         * Combine multiple results
         */
        fun <T> combine(vararg results: Result<T>): Result<List<T>> {
            val data = mutableListOf<T>()
            
            for (result in results) {
                when (result) {
                    is Success -> data.add(result.data)
                    is Error -> return result
                    is Loading -> return Loading
                }
            }
            
            return Success(data)
        }
    }
}

/**
 * Extension function to convert try-catch to Result
 */
inline fun <T> tryResult(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: Exception) {
    Result.error(e)
}

/**
 * Extension function for Result<T> to handle success and error
 */
inline fun <T> Result<T>.handle(
    onSuccess: (T) -> Unit = {},
    onError: (Exception) -> Unit = {},
    onLoading: () -> Unit = {}
) {
    when (this) {
        is Result.Success -> onSuccess(this.data)
        is Result.Error -> onError(this.exception)
        is Result.Loading -> onLoading()
    }
}

/**
 * Extension function to convert Result<T> to Flow
 */
fun <T> Result<T>.toMessage(): String = when (this) {
    is Result.Success -> "Success"
    is Result.Error -> this.getErrorMessage()
    is Result.Loading -> "Loading..."
}
