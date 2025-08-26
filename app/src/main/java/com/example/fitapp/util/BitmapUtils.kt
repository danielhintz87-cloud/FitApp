package com.example.fitapp.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.WorkerThread

/**
 * Utility for loading a Bitmap from a Uri
 */
object BitmapUtils {
    
    /**
     * Load a Bitmap from Uri. Uses ImageDecoder for API 28+, MediaStore for older versions.
     * @param contentResolver ContentResolver to access the Uri
     * @param uri Uri of the image to load
     * @return Bitmap loaded from the Uri
     * @throws Exception if the image cannot be loaded
     */
    @WorkerThread
    fun loadBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        }
    }
}