package com.example.drawingapp

import androidx.compose.ui.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.drawingapp.screens.DrawingViewModel

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

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
}