package com.mobileprism.fishing.marketing

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class MarketingScreenshotExporter {

    @get:Rule
    val composeRule = createComposeRule()

    private val outDir: File by lazy {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        File(ctx.getExternalFilesDir(null), "marketing").apply { mkdirs() }
    }

    private fun export(
        fileName: String,
        density: Float,
        widthDp: Float,
        heightDp: Float,
        content: @Composable () -> Unit,
    ) {
        composeRule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(density)) {
                Box(
                    modifier = Modifier
                        .testTag("export")
                        .size(widthDp.dp, heightDp.dp),
                ) {
                    content()
                }
            }
        }
        composeRule.waitForIdle()
        var bitmap: Bitmap? = null
        var lastError: Throwable? = null
        repeat(8) {
            if (bitmap == null) {
                try {
                    bitmap = composeRule.onNodeWithTag("export").captureToImage().asAndroidBitmap()
                } catch (t: Throwable) {
                    lastError = t
                    composeRule.waitForIdle()
                    Thread.sleep(250)
                }
            }
        }
        val captured = bitmap ?: throw IllegalStateException("capture failed for $fileName", lastError)
        val file = File(outDir, fileName)
        FileOutputStream(file).use { captured.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("MARKETING_EXPORT ${file.absolutePath} ${captured.width}x${captured.height}")
    }

    @Test
    fun map() = export("01-map.png", 2.5f, 411f, 891f) { MapMarketingPreview() }

    @Test
    fun catch() = export("02-catch.png", 2.5f, 411f, 891f) { CatchDetailMarketingPreview() }

    @Test
    fun notes() = export("03-notes.png", 2.5f, 411f, 891f) { NotesListMarketingPreview() }

    @Test
    fun weather() = export("04-weather.png", 2.5f, 411f, 891f) { WeatherMarketingPreview() }
}
