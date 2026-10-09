package com.tradingdashboard.util

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlin.math.abs

/**
 * OCR Processor - handles text recognition from images
 * Extracts trade data from screenshots using MLKit
 */
object OcrProcessor {

    private val textRecognizer = TextRecognition.getClient(
        TextRecognizerOptions.Builder()
            .build()
    )

    data class ExtractedTradeData(
        val symbol: String? = null,
        val entryPrice: Double? = null,
        val exitPrice: Double? = null,
        val quantity: Double? = null,
        val pnlAmount: Double? = null,
        val pnlPercentage: Double? = null,
        val leverage: Double? = null,
        val position: String? = null,  // LONG or SHORT
        val status: String? = null,  // OPEN or CLOSED
        val confidenceScore: Float = 0f,
        val rawText: String = ""
    )

    /**
     * Extract trade data from image
     */
    suspend fun extractTradeData(bitmap: Bitmap): Result<ExtractedTradeData> {
        return try {
            Logger.methodEntry("extractTradeData")
            Logger.d("Starting OCR on image: ${bitmap.width}x${bitmap.height}")
            
            val image = InputImage.fromBitmap(bitmap, 0)
            
            // Perform text recognition
            val result = textRecognizer.process(image)
                .await()  // Kotlin Coroutine
            
            Logger.d("OCR completed, found ${result.textBlocks.size} text blocks")
            
            // Extract structured data from recognized text
            val extractedData = parseTradeData(result.text)
            
            Logger.methodExit("extractTradeData", result = extractedData)
            Result.success(extractedData)
        } catch (e: Exception) {
            Logger.e("Error during OCR extraction", throwable = e)
            Result.failure(e)
        }
    }

    /**
     * Parse recognized text to extract trade data
     */
    private fun parseTradeData(rawText: String): ExtractedTradeData {
        try {
            Logger.d("Parsing OCR text, length: ${rawText.length}")
            
            var symbol: String? = null
            var entryPrice: Double? = null
            var exitPrice: Double? = null
            var quantity: Double? = null
            var pnlAmount: Double? = null
            var pnlPercentage: Double? = null
            var leverage: Double? = null
            var position: String? = null
            var status: String? = null
            var confidence = 0f
            
            val lines = rawText.split("\n").map { it.trim() }
            
            for (line in lines) {
                when {
                    // Extract symbol (e.g., BTC/USDT, ETH/USDT)
                    symbol == null && line.matches(Regex("^[A-Z]{2,}/[A-Z]{3,}$")) -> {
                        symbol = line.uppercase()
                        confidence += 0.15f
                        Logger.d("Found symbol: $symbol")
                    }
                    
                    // Extract position (LONG or SHORT)
                    position == null && (line.contains("LONG", ignoreCase = true) || 
                                         line.contains("SHORT", ignoreCase = true)) -> {
                        position = if (line.contains("LONG", ignoreCase = true)) "LONG" else "SHORT"
                        confidence += 0.10f
                        Logger.d("Found position: $position")
                    }
                    
                    // Extract status (OPEN or CLOSED)
                    status == null && (line.contains("OPEN", ignoreCase = true) || 
                                       line.contains("CLOSED", ignoreCase = true)) -> {
                        status = if (line.contains("OPEN", ignoreCase = true)) "OPEN" else "CLOSED"
                        confidence += 0.10f
                        Logger.d("Found status: $status")
                    }
                    
                    // Extract leverage (e.g., 10x, 5x, 100x)
                    leverage == null && line.matches(Regex("^\\d+(\\.\\d+)?x?$")) -> {
                        leverage = line.replace("x", "", ignoreCase = true).toDoubleOrNull()
                        if (leverage != null) {
                            confidence += 0.05f
                            Logger.d("Found leverage: $leverage")
                        }
                    }
                    
                    // Extract prices and amounts
                    line.contains("Entry", ignoreCase = true) || 
                    line.contains("Open Price", ignoreCase = true) -> {
                        entryPrice = extractNumericValue(line)
                        if (entryPrice != null) {
                            confidence += 0.15f
                            Logger.d("Found entry price: $entryPrice")
                        }
                    }
                    
                    line.contains("Exit", ignoreCase = true) || 
                    line.contains("Close Price", ignoreCase = true) -> {
                        exitPrice = extractNumericValue(line)
                        if (exitPrice != null) {
                            confidence += 0.10f
                            Logger.d("Found exit price: $exitPrice")
                        }
                    }
                    
                    line.contains("Quantity", ignoreCase = true) || 
                    line.contains("Amount", ignoreCase = true) -> {
                        quantity = extractNumericValue(line)
                        if (quantity != null) {
                            confidence += 0.10f
                            Logger.d("Found quantity: $quantity")
                        }
                    }
                    
                    line.contains("P&L", ignoreCase = true) && !line.contains("%") -> {
                        pnlAmount = extractNumericValue(line)
                        if (pnlAmount != null) {
                            confidence += 0.15f
                            Logger.d("Found P&L amount: $pnlAmount")
                        }
                    }
                    
                    line.contains("P&L", ignoreCase = true) && line.contains("%") -> {
                        pnlPercentage = extractNumericValue(line)
                        if (pnlPercentage != null) {
                            confidence += 0.10f
                            Logger.d("Found P&L %: $pnlPercentage")
                        }
                    }
                }
            }
            
            // Validate extracted data
            confidence = minOf(confidence, 1f)  // Cap at 100%
            
            Logger.d("Extraction complete - Confidence: ${(confidence * 100).toInt()}%")
            
            return ExtractedTradeData(
                symbol = symbol,
                entryPrice = entryPrice,
                exitPrice = exitPrice,
                quantity = quantity,
                pnlAmount = pnlAmount,
                pnlPercentage = pnlPercentage,
                leverage = leverage,
                position = position,
                status = status,
                confidenceScore = confidence,
                rawText = rawText
            )
        } catch (e: Exception) {
            Logger.e("Error parsing OCR text", throwable = e)
            return ExtractedTradeData(
                rawText = rawText,
                confidenceScore = 0f
            )
        }
    }

    /**
     * Extract numeric value from a line of text
     */
    private fun extractNumericValue(line: String): Double? {
        return try {
            // Find all numbers in the line
            val numberPattern = Regex("""(\d+\.?\d*|\.\d+)""")
            val matches = numberPattern.findAll(line)
            
            // Return the largest number (usually the value we're looking for)
            matches.map { it.value.toDoubleOrNull() }
                .filterNotNull()
                .maxOrNull()
        } catch (e: Exception) {
            Logger.e("Error extracting numeric value", throwable = e)
            null
        }
    }

    /**
     * Validate extracted data quality
     */
    fun validateExtractedData(data: ExtractedTradeData): Boolean {
        return try {
            Logger.d("Validating extracted data, confidence: ${data.confidenceScore}")
            
            // Check minimum confidence threshold
            if (data.confidenceScore < Constants.OCR.MIN_CONFIDENCE) {
                Logger.d("Confidence too low: ${data.confidenceScore} < ${Constants.OCR.MIN_CONFIDENCE}")
                return false
            }
            
            // Check for required fields
            if (data.symbol == null) {
                Logger.d("Missing symbol")
                return false
            }
            
            if (data.entryPrice == null || data.entryPrice!! <= 0) {
                Logger.d("Invalid entry price")
                return false
            }
            
            // Validate that prices make sense
            if (data.exitPrice != null && data.entryPrice != null) {
                // Exit price should be different from entry (or null)
                if (abs(data.exitPrice!! - data.entryPrice!!) < 0.0001) {
                    Logger.d("Exit price too close to entry price")
                    return false
                }
            }
            
            Logger.d("Extracted data is valid")
            true
        } catch (e: Exception) {
            Logger.e("Error validating extracted data", throwable = e)
            false
        }
    }

    /**
     * Clean up recognizer (call when done)
     */
    fun cleanup() {
        try {
            textRecognizer.close()
            Logger.d("OCR recognizer cleaned up")
        } catch (e: Exception) {
            Logger.e("Error cleaning up recognizer", throwable = e)
        }
    }
}

// ============ Extension Functions ============

/**
 * Await extension for MLKit Tasks
 */
suspend inline fun <reified T> com.google.android.gms.tasks.Task<T>.await(): T {
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}
