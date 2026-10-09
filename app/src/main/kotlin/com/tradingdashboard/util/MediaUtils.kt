package com.tradingdashboard.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Media utilities for camera and gallery operations
 */
object MediaUtils {

    // ============ Camera Utilities ============

    /**
     * Create a file for camera capture
     */
    fun createImageFile(context: Context): File {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val storageDir = context.cacheDir
            
            File.createTempFile("IMG_${timestamp}_", ".jpg", storageDir).also {
                Logger.d("Image file created: ${it.absolutePath}")
            }
        } catch (e: Exception) {
            Logger.e("Error creating image file", throwable = e)
            File(context.cacheDir, "temp_image.jpg")
        }
    }

    /**
     * Get URI for camera file
     */
    fun getCameraFileUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileProvider",
            file
        )
    }

    /**
     * Create camera intent URI
     */
    fun createCameraUri(context: Context, imageFile: File): Uri {
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileProvider",
                imageFile
            )
        } catch (e: Exception) {
            Logger.e("Error creating camera URI", throwable = e)
            Uri.fromFile(imageFile)
        }
    }

    // ============ Image Processing ============

    /**
     * Compress image to reduce file size
     */
    fun compressImage(
        imageFile: File,
        quality: Int = 85,
        maxWidth: Int = 1920,
        maxHeight: Int = 1080
    ): File {
        return try {
            Logger.d("Compressing image: ${imageFile.name}")
            
            // Read original bitmap
            val originalBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                ?: throw Exception("Failed to decode bitmap")
            
            // Scale if necessary
            val scaledBitmap = if (originalBitmap.width > maxWidth || originalBitmap.height > maxHeight) {
                val scale = minOf(
                    maxWidth.toFloat() / originalBitmap.width,
                    maxHeight.toFloat() / originalBitmap.height
                )
                val newWidth = (originalBitmap.width * scale).toInt()
                val newHeight = (originalBitmap.height * scale).toInt()
                
                Logger.d("Scaling image: ${originalBitmap.width}x${originalBitmap.height} -> ${newWidth}x${newHeight}")
                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            } else {
                originalBitmap
            }
            
            // Compress and save
            val compressedFile = File(imageFile.parent, "compressed_${imageFile.name}")
            FileOutputStream(compressedFile).use { fos ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
                fos.flush()
            }
            
            // Log compression result
            val originalSize = imageFile.length() / 1024
            val compressedSize = compressedFile.length() / 1024
            Logger.d(
                "Image compressed: ${originalSize}KB -> ${compressedSize}KB " +
                "(${((1 - compressedSize.toFloat() / originalSize) * 100).toInt()}% reduction)"
            )
            
            // Delete original if significantly smaller
            if (compressedSize < originalSize * 0.9f) {
                imageFile.delete()
                compressedFile.renameTo(imageFile)
            }
            
            compressedFile
        } catch (e: Exception) {
            Logger.e("Error compressing image", throwable = e)
            imageFile
        }
    }

    /**
     * Get bitmap from URI
     */
    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            Logger.d("Loading bitmap from URI: $uri")
            
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return null
            
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            Logger.d("Bitmap loaded: ${bitmap?.width}x${bitmap?.height}")
            bitmap
        } catch (e: Exception) {
            Logger.e("Error loading bitmap from URI", throwable = e)
            null
        }
    }

    /**
     * Save bitmap to file
     */
    fun saveBitmapToFile(bitmap: Bitmap, file: File, quality: Int = 85): Boolean {
        return try {
            Logger.d("Saving bitmap to: ${file.absolutePath}")
            
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos)
                fos.flush()
            }
            
            Logger.d("Bitmap saved successfully (${file.length() / 1024}KB)")
            true
        } catch (e: Exception) {
            Logger.e("Error saving bitmap", throwable = e)
            false
        }
    }

    /**
     * Rotate bitmap if needed
     */
    fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        
        return try {
            val matrix = android.graphics.Matrix().apply {
                postRotate(degrees.toFloat())
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            Logger.e("Error rotating bitmap", throwable = e)
            bitmap
        }
    }

    // ============ Gallery Operations ============

    /**
     * Get image path from URI
     */
    fun getImagePathFromUri(context: Context, uri: Uri): String? {
        return try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            
            val path = cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    it.getString(columnIndex)
                } else {
                    uri.path
                }
            } ?: uri.path
            
            Logger.d("Image path from URI: $path")
            path
        } catch (e: Exception) {
            Logger.e("Error getting image path from URI", throwable = e)
            uri.path
        }
    }

    /**
     * Copy image from URI to app cache
     */
    fun copyImageToCache(context: Context, sourceUri: Uri): File? {
        return try {
            Logger.d("Copying image from URI to cache")
            
            val bitmap = getBitmapFromUri(context, sourceUri) ?: return null
            val cacheFile = createImageFile(context)
            
            if (saveBitmapToFile(bitmap, cacheFile)) {
                Logger.d("Image copied to cache: ${cacheFile.absolutePath}")
                cacheFile
            } else {
                null
            }
        } catch (e: Exception) {
            Logger.e("Error copying image to cache", throwable = e)
            null
        }
    }

    /**
     * Delete image file
     */
    fun deleteImage(file: File): Boolean {
        return try {
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Logger.d("Image deleted: ${file.absolutePath}")
                }
                deleted
            } else {
                true
            }
        } catch (e: Exception) {
            Logger.e("Error deleting image", throwable = e)
            false
        }
    }

    /**
     * Get image dimensions without loading full bitmap
     */
    fun getImageDimensions(file: File): Pair<Int, Int>? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
            
            Pair(options.outWidth, options.outHeight)
        } catch (e: Exception) {
            Logger.e("Error getting image dimensions", throwable = e)
            null
        }
    }

    /**
     * Get file size in MB
     */
    fun getFileSizeMB(file: File): Double {
        return file.length() / (1024.0 * 1024.0)
    }

    /**
     * Check if image quality is acceptable
     */
    fun isImageQualityAcceptable(
        file: File,
        minWidth: Int = 400,
        minHeight: Int = 400,
        maxSizeMB: Double = Constants.OCR.MAX_IMAGE_SIZE_MB.toDouble()
    ): Boolean {
        return try {
            val sizeMB = getFileSizeMB(file)
            if (sizeMB > maxSizeMB) {
                Logger.d("Image too large: ${sizeMB}MB > ${maxSizeMB}MB")
                return false
            }
            
            val dimensions = getImageDimensions(file)
            if (dimensions == null) {
                Logger.d("Could not get image dimensions")
                return false
            }
            
            val (width, height) = dimensions
            if (width < minWidth || height < minHeight) {
                Logger.d("Image too small: ${width}x${height} < ${minWidth}x${minHeight}")
                return false
            }
            
            Logger.d("Image quality acceptable: ${width}x${height}, ${sizeMB}MB")
            true
        } catch (e: Exception) {
            Logger.e("Error checking image quality", throwable = e)
            false
        }
    }
}
