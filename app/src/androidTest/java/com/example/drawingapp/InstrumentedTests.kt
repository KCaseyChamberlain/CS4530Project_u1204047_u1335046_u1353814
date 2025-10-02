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

@RunWith(AndroidJUnit4::class)
class InstrumentedTests {
    @Test
    fun pen_size_changes_when_slider_moves() {
        val vm = DrawingViewModel()
        vm.setSize(24f)
        assertEquals(24f, vm.pen.value.size, 0.0001f)
    }

    @Test
    fun pen_color_changes_when_selected() {
        val vm = DrawingViewModel()
        val red = Color(0xFFFF0000)
        vm.setColor(red)
        assertEquals(red, vm.pen.value.color)
    }

    @Test
    fun pen_shape_changes_when_selected() {
        val vm = DrawingViewModel()
        vm.setShape("Square")
        assertEquals("Square", vm.pen.value.shape)
    }

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    // untilizes testTag in Drawing.kt and SplashScreen.kt
    fun app_shows_splash_then_navigates_to_draw() {
        // "splash" visible, "draw" not present yet
        composeRule.onNodeWithTag("splash").assertIsDisplayed()
        composeRule.onAllNodesWithTag("draw", useUnmergedTree = true)
            .assertCountEquals(0)

        // wait until at least one "draw" node exists
        composeRule.waitUntil(timeoutMillis = 2_500) {
            composeRule.onAllNodesWithTag("draw", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // "draw" is visible and "splash" is gone
        composeRule.onNodeWithTag("draw").assertIsDisplayed()
        composeRule.onAllNodesWithTag("splash", useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun draw_changes_bitmap_pixels() {
        val vm = DrawingViewModel()
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
}