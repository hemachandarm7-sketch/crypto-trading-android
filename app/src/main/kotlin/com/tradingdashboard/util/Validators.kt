package com.tradingdashboard.util

/**
 * Input validation utilities
 */
object Validators {

    // ============ Trade Validators ============

    /**
     * Validate trade symbol
     * Returns error message or null if valid
     */
    fun validateSymbol(symbol: String): String? {
        return when {
            symbol.isEmpty() -> "Symbol cannot be empty"
            symbol.length < Constants.Validation.MIN_SYMBOL_LENGTH -> 
                "Symbol must be at least ${Constants.Validation.MIN_SYMBOL_LENGTH} characters"
            symbol.length > Constants.Validation.MAX_SYMBOL_LENGTH -> 
                "Symbol cannot exceed ${Constants.Validation.MAX_SYMBOL_LENGTH} characters"
            !symbol.matches(Regex(Constants.Validation.REGEX_SYMBOL)) -> 
                "Symbol contains invalid characters"
            else -> null
        }
    }

    /**
     * Validate leverage
     */
    fun validateLeverage(leverage: Double): String? {
        return when {
            leverage < Constants.Trade.MIN_LEVERAGE -> 
                "Leverage must be at least ${Constants.Trade.MIN_LEVERAGE}x"
            leverage > Constants.Trade.MAX_LEVERAGE -> 
                "Leverage cannot exceed ${Constants.Trade.MAX_LEVERAGE}x"
            else -> null
        }
    }

    /**
     * Validate quantity
     */
    fun validateQuantity(quantity: Double): String? {
        return when {
            quantity <= Constants.Trade.MIN_QUANTITY -> 
                "Quantity must be greater than ${Constants.Trade.MIN_QUANTITY}"
            quantity > Constants.Trade.MAX_QUANTITY -> 
                "Quantity exceeds maximum allowed"
            else -> null
        }
    }

    /**
     * Validate price
     */
    fun validatePrice(price: Double): String? {
        return when {
            price <= 0.0 -> "Price must be greater than 0"
            price.isInfinite() -> "Price is invalid"
            price.isNaN() -> "Price is invalid"
            else -> null
        }
    }

    /**
     * Validate entry and exit prices
     */
    fun validateEntryExitPrices(
        entryPrice: Double,
        exitPrice: Double?,
        position: String
    ): String? {
        val entryError = validatePrice(entryPrice)
        if (entryError != null) return entryError

        if (exitPrice != null) {
            val exitError = validatePrice(exitPrice)
            if (exitError != null) return exitError

            // Validate based on position
            return when {
                position == Constants.Trade.POSITION_LONG && exitPrice <= entryPrice -> 
                    "Exit price must be higher than entry price for LONG position"
                position == Constants.Trade.POSITION_SHORT && exitPrice >= entryPrice -> 
                    "Exit price must be lower than entry price for SHORT position"
                else -> null
            }
        }

        return null
    }

    /**
     * Validate margin
     */
    fun validateMargin(margin: Double, leverage: Double, entryPrice: Double): String? {
        if (margin <= 0.0) return "Margin must be greater than 0"

        // Expected margin = entryPrice * quantity / leverage
        // quantity = margin * leverage / entryPrice
        if (entryPrice <= 0.0) return "Entry price is invalid"
        
        return null
    }

    // ============ User Input Validators ============

    /**
     * Validate email
     */
    fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> "Email cannot be empty"
            !email.isValidEmail() -> "Invalid email format"
            else -> null
        }
    }

    /**
     * Validate password
     */
    fun validatePassword(password: String): String? {
        return when {
            password.isEmpty() -> "Password cannot be empty"
            password.length < Constants.Validation.MIN_PASSWORD_LENGTH -> 
                "Password must be at least ${Constants.Validation.MIN_PASSWORD_LENGTH} characters"
            password.length > Constants.Validation.MAX_PASSWORD_LENGTH -> 
                "Password is too long"
            else -> null
        }
    }

    /**
     * Validate password confirmation
     */
    fun validatePasswordMatch(password: String, confirmPassword: String): String? {
        return when {
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    // ============ Form Validators ============

    /**
     * Validate trade form
     * Returns map of field -> error message
     */
    fun validateTradeForm(
        symbol: String,
        position: String,
        leverage: Double,
        entryPrice: Double,
        exitPrice: Double?,
        quantity: Double,
        margin: Double
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        validateSymbol(symbol)?.let { errors["symbol"] = it }
        validateLeverage(leverage)?.let { errors["leverage"] = it }
        validatePrice(entryPrice)?.let { errors["entryPrice"] = it }
        validateQuantity(quantity)?.let { errors["quantity"] = it }
        validateMargin(margin, leverage, entryPrice)?.let { errors["margin"] = it }
        
        validateEntryExitPrices(entryPrice, exitPrice, position)?.let { 
            errors["prices"] = it 
        }

        return errors
    }

    /**
     * Check if trade form is valid
     */
    fun isTradeFormValid(
        symbol: String,
        position: String,
        leverage: Double,
        entryPrice: Double,
        exitPrice: Double?,
        quantity: Double,
        margin: Double
    ): Boolean {
        return validateTradeForm(
            symbol, position, leverage, entryPrice, exitPrice, quantity, margin
        ).isEmpty()
    }

    // ============ Range Validators ============

    /**
     * Check if value is within range
     */
    fun <T : Comparable<T>> isInRange(value: T, min: T, max: T): Boolean {
        return value in min..max
    }

    /**
     * Check if percentage is valid (0-100)
     */
    fun isValidPercentage(percentage: Double): Boolean {
        return percentage in 0.0..100.0
    }

    // ============ String Pattern Validators ============

    /**
     * Check if string contains only alphanumeric characters
     */
    fun isAlphanumeric(text: String): Boolean {
        return text.all { it.isLetterOrDigit() }
    }

    /**
     * Check if string contains only letters
     */
    fun isAlpha(text: String): Boolean {
        return text.all { it.isLetter() }
    }

    /**
     * Check if string contains only digits
     */
    fun isNumeric(text: String): Boolean {
        return text.all { it.isDigit() }
    }

    /**
     * Check if string matches pattern
     */
    fun matchesPattern(text: String, pattern: String): Boolean {
        return text.matches(Regex(pattern))
    }

    // ============ Collection Validators ============

    /**
     * Check if list is not empty
     */
    fun <T> isNotEmpty(list: List<T>?): Boolean {
        return list != null && list.isNotEmpty()
    }

    /**
     * Check if list contains at least one matching item
     */
    fun <T> containsAny(list: List<T>, predicate: (T) -> Boolean): Boolean {
        return list.any(predicate)
    }

    // ============ Nullable Validators ============

    /**
     * Check if value is not null
     */
    fun <T> isNotNull(value: T?): Boolean {
        return value != null
    }

    /**
     * Check if string is not empty or null
     */
    fun isNotBlank(text: String?): Boolean {
        return !text.isNullOrBlank()
    }

    /**
     * Check if string is empty or null
     */
    fun isBlank(text: String?): Boolean {
        return text.isNullOrBlank()
    }
}
