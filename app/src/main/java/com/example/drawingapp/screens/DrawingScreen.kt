package com.example.drawingapp.screens
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class DrawingViewModel : ViewModel() {
    val canvasDrawer = CanvasDrawer(width = 1000)

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
}

@Composable
fun DrawingScreen(navController: NavHostController) {
    // VM + state from VM
    val vm: DrawingViewModel = viewModel()
    val pen by vm.pen.collectAsState()

    val penOptions = listOf("Circle", "Square", "Line")
    var droppedDown by remember { mutableStateOf(false) }

    // dialog UI state
    var showColorDialog by remember { mutableStateOf(false) }
    val controller = rememberColorPickerController()
    var tempColor by remember { mutableStateOf(pen.color) }

    // recomposition
    var frame by rememberSaveable { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(12.dp)
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
                bitmap = vm.canvasDrawer.bitmap.asImageBitmap(), // <- single shared bitmap
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
}