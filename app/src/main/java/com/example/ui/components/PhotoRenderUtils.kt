package com.example.ui.components

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.model.RetouchIntent

/**
 * Builds a realistic cool overcast "unretouched original" ColorMatrix
 * so the Before/After comparison aligns sub-pixel across the exact same portrait.
 */
fun createOriginalColorMatrix(): ColorMatrix {
    return ColorMatrix(
        floatArrayOf(
            0.84f, 0.08f, 0.04f, 0f, -10f,
            0.05f, 0.88f, 0.06f, 0f, -6f,
            0.03f, 0.08f, 0.96f, 0f, 6f,
            0f,    0f,    0f,    1f, 0f
        )
    )
}

/**
 * Builds an optical Leica film retouch ColorMatrix modulated dynamically
 * by active AI retouch intents and facial fidelity lock.
 */
fun createRetouchedColorMatrix(
    activeIntents: Set<RetouchIntent>,
    faceLockEnabled: Boolean
): ColorMatrix {
    val hasWarm = RetouchIntent.WARM_BACKLIGHT in activeIntents
    val hasSkin = RetouchIntent.NATURAL_SKIN in activeIntents
    val hasClean = RetouchIntent.REMOVE_PASSERSBY in activeIntents || RetouchIntent.SMOOTH_CLOTHES in activeIntents

    val redBoost = if (hasWarm) 1.08f else 1.02f
    val greenBoost = if (hasSkin) 1.03f else 1.00f
    val blueScale = if (hasWarm) 0.94f else 0.99f
    val lift = if (faceLockEnabled) 3f else 7f
    val contrastExtra = if (hasClean) 1.03f else 1.00f

    return ColorMatrix(
        floatArrayOf(
            redBoost * contrastExtra, 0.02f, 0f, 0f, lift,
            0f, greenBoost * contrastExtra, 0.01f, 0f, lift * 0.7f,
            0f, 0f, blueScale * contrastExtra, 0f, if (hasWarm) -4f else 0f,
            0f, 0f, 0f, 1f, 0f
        )
    )
}

@Composable
fun SiluPhotoLayer(
    @DrawableRes drawableRes: Int?,
    customBitmap: Bitmap?,
    contentDescription: String?,
    isOriginalPreview: Boolean = false,
    activeIntents: Set<RetouchIntent> = emptySet(),
    faceLockEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val matrix = remember(isOriginalPreview, activeIntents, faceLockEnabled) {
        if (isOriginalPreview) {
            createOriginalColorMatrix()
        } else if (activeIntents.isNotEmpty()) {
            createRetouchedColorMatrix(activeIntents, faceLockEnabled)
        } else {
            null
        }
    }
    val filter = remember(matrix) { matrix?.let { ColorFilter.colorMatrix(it) } }
    val imageBitmap = remember(customBitmap) { customBitmap?.asImageBitmap() }

    Box(modifier = modifier) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                colorFilter = filter,
                modifier = Modifier.fillMaxSize()
            )
        } else if (drawableRes != null) {
            Image(
                painter = painterResource(id = drawableRes),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                colorFilter = filter,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Golden-hour optical rim light bloom when warm backlight intent is active
        if (!isOriginalPreview && RetouchIntent.WARM_BACKLIGHT in activeIntents) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x2EFFB454),
                                Color(0x14FF9F43),
                                Color.Transparent
                            ),
                            radius = 900f
                        )
                    )
            )
        }

        // Cool overcast veil when previewing unretouched original
        if (isOriginalPreview) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x181B2430))
            )
        }
    }
}

fun decodeSampledBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 1440): Bitmap? {
    return runCatching {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, boundsOpts)
        }
        var sampleSize = 1
        while (boundsOpts.outWidth / sampleSize > maxDimension || boundsOpts.outHeight / sampleSize > maxDimension) {
            sampleSize *= 2
        }
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, decodeOpts)
        }
    }.getOrNull()
}

/**
 * Exports the retouched full-resolution portrait losslessly as PNG into the Android Gallery (MediaStore).
 */
fun savePhotoLosslessToGallery(
    context: Context,
    @DrawableRes drawableRes: Int?,
    customBitmap: Bitmap?,
    activeIntents: Set<RetouchIntent>,
    faceLockEnabled: Boolean
): Result<String> {
    return runCatching {
        val sourceBitmap = customBitmap ?: drawableRes?.let {
            BitmapFactory.decodeResource(context.resources, it)
        } ?: error("无法读取图像源")

        val outputBitmap = Bitmap.createBitmap(
            sourceBitmap.width,
            sourceBitmap.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(outputBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        if (activeIntents.isNotEmpty()) {
            val composeMatrix = createRetouchedColorMatrix(activeIntents, faceLockEnabled)
            paint.colorFilter = ColorMatrixColorFilter(android.graphics.ColorMatrix(composeMatrix.values))
        }
        canvas.drawBitmap(sourceBitmap, 0f, 0f, paint)

        val filename = "Silu_${System.currentTimeMillis()}.png"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Silu")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("无法创建相册文件")
            resolver.openOutputStream(uri)?.use { out ->
                outputBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
        } else {
            val file = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let {
                java.io.File(it, filename)
            } ?: java.io.File(context.cacheDir, filename)
            file.outputStream().use { out ->
                outputBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
        filename
    }
}
