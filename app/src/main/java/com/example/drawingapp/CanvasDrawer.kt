package com.example.drawingapp
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.createBitmap
import androidx.compose.ui.graphics.toArgb

class CanvasDrawer (
    private val width: Int
) {
    val bitmap: Bitmap = createBitmap(width, width)
    private val canvas: Canvas = Canvas(bitmap)

    val penShapes = listOf("Circle", "Square", "Line")
    //figure out what to do with other shapes later...
    var penShape = penShapes[0]


    private val drawing = Paint().apply {
        color = Color.BLACK
        strokeWidth = 12f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    // Points used to draw a solid line.
    private var lastX: Float? = null
    private var lastY: Float? = null

    init {
        clear()
    }

    //call when user is resetting drawing
    fun clear(){
        canvas.drawColor(Color.WHITE)
    }

    // Used so the line doesn't keep connecting after each drag.
    fun resetLine(){
        lastX = null
        lastY = null
    }
    //call when user changes pen size
    fun setPenSize(size: Float){
        drawing.strokeWidth = size
    }

    //call when user changes pen color
    @RequiresApi(Build.VERSION_CODES.O)
    fun setPenColor(color: androidx.compose.ui.graphics.Color){
        drawing.color = color.toArgb()
    }

    //this is called on the DrawingScreen. different pen shapes change what is being drawn,
    //using canvas's own draw___ functions.
    fun drawPen(x: Float, y: Float){
        when (penShape) {
            "Square" -> canvas.drawRect(
                (x - drawing.strokeWidth / 2), (y - drawing.strokeWidth / 2),
                (x + drawing.strokeWidth / 2), (y + drawing.strokeWidth / 2),
                drawing
            )
            "Circle" -> canvas.drawCircle(
                x,
                y,
                drawing.strokeWidth / 2, drawing)
            "Line" -> {
                if (lastX != null && lastY != null) {
                    canvas.drawLine(lastX!!, lastY!!, x, y, drawing)
                }
                lastX = x
                lastY = y
            }
        }
    }

    //used so the DrawingScreen has a copy that can be recomposed
    fun copyBitmap(): Bitmap {
        return bitmap.copy(Bitmap.Config.ARGB_8888, true)
    }
}