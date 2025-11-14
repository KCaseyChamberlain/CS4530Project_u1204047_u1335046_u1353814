package com.example.drawingapp

import androidx.compose.ui.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.drawingapp.screens.DrawingViewModel
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import org.junit.Rule
import androidx.test.core.app.ApplicationProvider
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.delay


@RunWith(AndroidJUnit4::class)
class InstrumentedTests {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun pen_size_changes_when_slider_moves() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = DrawingViewModel(app)
        vm.setSize(24f)
        assertEquals(24f, vm.pen.value.size, 0.0001f)
    }

    @Test
    fun pen_color_changes_when_selected() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = DrawingViewModel(app)
        val red = Color(0xFFFF0000)
        vm.setColor(red)
        assertEquals(red, vm.pen.value.color)
    }

    @Test
    fun pen_shape_changes_when_selected() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = DrawingViewModel(app)
        vm.setShape("Square")
        assertEquals("Square", vm.pen.value.shape)
    }

    @Test
    // utilizes testTag in Drawing.kt and SplashScreen.kt
    fun app_shows_splash_then_navigates_to_draw() {
        // 1) Splash is visible initially
        composeRule.onNodeWithTag("splash").assertIsDisplayed()
        composeRule.onAllNodesWithTag("draw", useUnmergedTree = true)
            .assertCountEquals(0)

        // 2) Fast-forward Splash (2_000 delay + ~800 exit anim)
        composeRule.mainClock.advanceTimeBy(3_000)
        composeRule.waitForIdle()

        // 3) On the selection screen, tap "New Drawing"
        composeRule.onNode(hasText("New Drawing"), useUnmergedTree = true).performClick()

        // 4) Wait for the draw screen to appear
        composeRule.waitUntil(timeoutMillis = 8_000) {
            composeRule.onAllNodesWithTag("draw", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 5) Assert draw visible and splash gone
        composeRule.onNodeWithTag("draw").assertIsDisplayed()
        composeRule.onAllNodesWithTag("splash", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun draw_changes_bitmap_pixels() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val vm = DrawingViewModel(app)
        val bm = vm.canvasDrawer.bitmap

        // take a snapshot of pre - bitmap
        val before = bm.copy(bm.config ?: android.graphics.Bitmap.Config.ARGB_8888, false)

        // small stroke near the center
        val cx = bm.width / 2f
        val cy = bm.height / 2f
        vm.setColor(Color.Red)
        vm.setSize(20f)
        vm.draw(cx, cy)
        vm.endStroke()

        // bitmap content should now differ from the snapshot
        assertFalse("Expected drawing to modify bitmap", bm.sameAs(before))
    }

    @Test
    fun import_creates_db_row_and_file() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>() as DrawingApp
        val repo = app.repository
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()

        // Make a tiny bitmap to import
        val bmp = android.graphics.Bitmap.createBitmap(
            8, 8, android.graphics.Bitmap.Config.ARGB_8888
        ).apply { eraseColor(android.graphics.Color.GREEN) }

        val name = "Imported_${System.currentTimeMillis()}"

        // save via repository
        repo.saveImage(ctx, bmp, name) { /* ignore path in test */ }

        val row = withTimeout(5_000) {
            var found: com.example.drawingapp.storage.ImageEntity? = null
            while (found == null) {
                val rows = app.db.imageDao().getAllImages().first()
                found = rows.firstOrNull { e -> e.fileName == name }
                if (found == null) delay(100)
            }
            found
        }!!

        // Assert DB + file system
        val f = java.io.File(row.filepath)
        assertTrue("Expected image file to exist at ${row.filepath}", f.exists())
        assertTrue("Expected non-empty image file", f.length() > 0L)
    }

    @Test
    fun export_whenProviderPresent() {
        val appCtx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val authority = appCtx.packageName + ".fileprovider"
        val hasProvider = appCtx.packageManager.resolveContentProvider(authority, 0) != null

        // Skip the test if no provider is configured
        org.junit.Assume.assumeTrue("Skipping: no FileProvider declared", hasProvider)

        // Create a tiny PNG in internal storage
        val bmp = android.graphics.Bitmap.createBitmap(8, 8, android.graphics.Bitmap.Config.ARGB_8888)
            .apply { eraseColor(android.graphics.Color.CYAN) }
        val file = java.io.File(appCtx.filesDir, "export_${System.currentTimeMillis()}.png")
        java.io.FileOutputStream(file).use {
            bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }

        // Use an Activity context so startActivity() is valid
        val scenario = androidx.test.core.app.ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            try {
                shareImageFile(activity, file.absolutePath) // should not throw
            } catch (t: Throwable) {
                fail("Export crashed: ${t::class.java.simpleName}: ${t.message}")
            }
        }
        scenario.close()
    }


    @Test
    fun vision_api_call_success() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>() as DrawingApp
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        val vm = com.example.drawingapp.screens.AiViewModel(app)

        // key check
        assertTrue(vm.apiKey.isNotBlank())

        // small pink bitmap
        val bmp = android.graphics.Bitmap.createBitmap(
            8,
            8,
            android.graphics.Bitmap.Config.ARGB_8888
        ).apply {
            eraseColor(android.graphics.Color.MAGENTA)
        }

        // save bitmap
        val file = java.io.File(ctx.filesDir, "vision_test.png")
        java.io.FileOutputStream(file).use {
            bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }

        val uri = android.net.Uri.fromFile(file)

        // just make sure this does not throw
        try {
            vm.analyzeImg(ctx, uri)
        } catch (e: Exception) {
            fail("vision api call crashed: ${e.message}")
        }
    }

}