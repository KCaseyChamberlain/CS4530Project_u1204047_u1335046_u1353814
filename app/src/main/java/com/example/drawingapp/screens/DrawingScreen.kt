package com.example.drawingapp.screens
import android.R.attr.bitmap
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.drawingapp.CanvasDrawer
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.drawingapp.ui.theme.background
import com.example.drawingapp.ui.theme.secondary
import com.example.drawingapp.ui.theme.textColor
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.AndroidViewModel
import com.example.drawingapp.DrawingApp
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap

/*
Class used for drawing data. Contains the Canvas Drawer, the bitmap storing the data,
and functions for changing the data, as well as the state of the pen.
 */
class DrawingViewModel(application: Application) : AndroidViewModel(application) {
    val canvasDrawer = CanvasDrawer(width = 1000)
    val dao=(application as DrawingApp).repository
    var bitmap = mutableStateOf(canvasDrawer.bitmap)
    fun setBitmap(newBitmap: Bitmap) {
        canvasDrawer.setMap(newBitmap)
        bitmap.value = canvasDrawer.bitmap
    }

    fun setNewBitMap(newBitmap: Bitmap) {
        canvasDrawer.setNewMap(newBitmap)
        bitmap.value = canvasDrawer.bitmap
    }

    data class PenState(
        val color: Color = Color.Black,
        val size: Float = 10f,
        val shape: String = "Circle" // "Circle" | "Square" | "Line"
    )
    private val _pen = MutableStateFlow(PenState())
    val pen: StateFlow<PenState> = _pen

    fun setColor(c: Color) {
        _pen.update { it.copy(color = c) }
        canvasDrawer.setPenColor(c)
    }
    fun setSize(s: Float) {
        _pen.update { it.copy(size = s) }
        canvasDrawer.setPenSize(s)
    }
    fun setShape(shape: String) {
        _pen.update { it.copy(shape = shape) }
        canvasDrawer.penShape = shape
    }

    fun draw(x: Float, y: Float) = canvasDrawer.drawPen(x, y)
    fun endStroke() = canvasDrawer.resetLine()
    fun clear() = canvasDrawer.clear()

    fun saveImage(context : Context, fileName: String) {
        dao.saveImage(context, canvasDrawer.bitmap, fileName)
    }
}

private fun fitIntoSquare(
    src: android.graphics.Bitmap,
    size: Int = 1000,
    bg: Int = android.graphics.Color.WHITE
): android.graphics.Bitmap {
    val dst = android.graphics.Bitmap.createBitmap(
        size, size, android.graphics.Bitmap.Config.ARGB_8888
    )
    val c = android.graphics.Canvas(dst)
    c.drawColor(bg)

    val scale = minOf(size.toFloat() / src.width, size.toFloat() / src.height)
    val w = (src.width * scale).toInt()
    val h = (src.height * scale).toInt()
    val left = (size - w) / 2
    val top = (size - h) / 2

    val dstRect = android.graphics.Rect(left, top, left + w, top + h)
    c.drawBitmap(src, null, dstRect, null)
    return dst
}


@Composable
fun DrawingScreen(navController: NavHostController, filePath: String? = null) {
    // VM + state from VM
    val vm: DrawingViewModel = viewModel()
    val pen by vm.pen.collectAsState()

    val bitmap by vm.bitmap
    LaunchedEffect(filePath) {
        val targetSize = 1000
        val raw = BitmapFactory.decodeFile(filePath)
        if (raw != null) {
            val fitted = fitIntoSquare(raw, targetSize, android.graphics.Color.WHITE)
            vm.setBitmap(fitted)
        } else {
            vm.setNewBitMap(createBitmap(targetSize, targetSize))
        }
    }

    val penOptions = listOf("Circle", "Square", "Line")
    var droppedDown by remember { mutableStateOf(false) }

    // dialog UI state
    var showColorDialog by remember { mutableStateOf(false) }
    val controller = rememberColorPickerController()
    var tempColor by remember { mutableStateOf(pen.color) }

    // recomposition
    var frame by rememberSaveable { mutableStateOf(0) }

    // For the save button and it's popup
    var showSaveDialog by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf("") }




    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(12.dp)
            .testTag("draw")
    ) {
        // user config options
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(secondary)
                .padding(50.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { droppedDown = true }) {
                Text(pen.shape, color = textColor, fontSize = 25.sp)
            }

            Spacer(modifier = Modifier.width(20.dp))

            DropdownMenu(
                expanded = droppedDown,
                onDismissRequest = { droppedDown = false }
            ) {
                penOptions.forEach { shape ->
                    DropdownMenuItem(
                        text = { Text(shape) },
                        onClick = {
                            vm.setShape(shape)
                            droppedDown = false
                        }
                    )
                }
            }

            // color preview
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(pen.color)
                    .clickable {
                        tempColor = pen.color
                        showColorDialog = true
                    }
            )

            Spacer(modifier = Modifier.width(20.dp))

            // pen size
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Size: ${pen.size.toInt()}", color = textColor)
                Slider(
                    value = pen.size,
                    onValueChange = { vm.setSize(it) },
                    valueRange = 1f..50f,
                    modifier = Modifier.width(150.dp)
                )
            }
        }

        // drawing area
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Draw here")
            Image(
                bitmap = bitmap.asImageBitmap(), // <- single shared bitmap
                contentDescription = "Drawing Area",
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { p ->
                                vm.draw(p.x, p.y)
                                frame++
                            },
                            onDrag = { change, _ ->
                                vm.draw(change.position.x, change.position.y)
                                frame++
                            },
                            onDragEnd = { vm.endStroke() }
                        )
                    }
            )
            if (frame < 0) Text("") // to keep Compose aware of the frame
            Button({showSaveDialog = true},
                colors = ButtonDefaults.buttonColors(
                    containerColor = secondary,
                    contentColor = Color.Black
                )) {
                Text("Save Image")
            }
            Button({navController.navigate("file_select")},
                colors = ButtonDefaults.buttonColors(
                    containerColor = secondary,
                    contentColor = Color.Black
                )) {
                Text("Back")
            }

        }
    }

    // color picker modal
    if (showColorDialog) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    vm.setColor(tempColor)
                    showColorDialog = false
                }) { Text("Confirm") }
            },
            title = { Text("Pick a color") },
            text = {
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    controller = controller,
                    onColorChanged = { env ->
                        tempColor = env.color
                        controller.wheelColor = tempColor
                    }
                )
            }
        )
    }

    // Popup for the save button.
    if (showSaveDialog) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Drawing") },
            text = {
                Column {
                    Text("Enter a file name:")
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        singleLine = true,
                        placeholder = { Text("e.g. MyDrawing1") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Call saveImage() with user-entered name
                    if (fileName.isNotBlank()) {
                        vm.saveImage(context, fileName)
                        showSaveDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

}