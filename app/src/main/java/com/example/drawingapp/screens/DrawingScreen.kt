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
import androidx.compose.runtime.mutableFloatStateOf
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

class DrawingViewModel : ViewModel() {
    val canvasDrawer = CanvasDrawer(1000)
    var bitmap by mutableStateOf(canvasDrawer.copyBitmap())
}
@Composable
fun DrawingScreen(navController: NavHostController){
    //placeholder pen menu
    val penOptions = listOf("Circle", "Square", "Line")
    var droppedDown by remember { mutableStateOf(false) }
    var selectedPen by rememberSaveable {mutableStateOf(penOptions[0])}
    // Pen size state
    var penSize by rememberSaveable { mutableFloatStateOf(10f) }
    // Pen color state
    var selectedColor by remember { mutableStateOf(Color.Black) }
    var showColorDialog by remember { mutableStateOf(false) }
    val controller = rememberColorPickerController()

    //uses CanvasDrawer class, and copies bitmap so it can be redrawn every time a change is made
    val viewModel: DrawingViewModel = viewModel()
    val bitmap = viewModel.bitmap


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(12.dp)
    ) {
        //drop down pen menu, very basic
        //
        // this drop down can be removed later once there's a more encompassing UI class
        //
        //
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(secondary)
                .padding(50.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = { droppedDown = true }) {
                Text(selectedPen, color = textColor, fontSize = 25.sp)
            }

            Spacer(modifier = Modifier.width(20.dp))

            DropdownMenu(
                expanded = droppedDown,
                onDismissRequest = { droppedDown = false }
            ) {
                penOptions.forEach { pen ->
                    DropdownMenuItem(
                        text = { Text(pen) },
                        onClick = {
                            selectedPen = pen
                            viewModel.canvasDrawer.penShape = pen
                            droppedDown = false
                        }
                    )
                }
            }

            // Color preview circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(selectedColor)
                    .clickable { showColorDialog = true }
            )

            Spacer(modifier = Modifier.width(20.dp))

            // Slider for pen size
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Size: ${penSize.toInt()}",
                    color = textColor)
                Slider(
                    value = penSize,
                    onValueChange = { newSize ->
                        penSize = newSize
                    },
                    valueRange = 1f..50f, // min/max pen size
                    modifier = Modifier.width(150.dp)
                )
            }
        }
        }



    //drawing area, uses the CanvasDrawer class and its bitmap.
    //using pointerInput, tries to call drawPen from CanvasDrawer.
    Column (modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally){
        Text("Draw here")
        //image in the screen showing the drawing canvas, using bitmap from CanvasDrawer
        Image (
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Drawing Area",
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit){
                    detectDragGestures(
                        onDragStart = { point ->
                            viewModel.canvasDrawer.drawPen(
                                point.x,
                                point.y)
                            viewModel.canvasDrawer.setPenSize(penSize)
                            viewModel.bitmap = viewModel.canvasDrawer.copyBitmap()
                        },
                        onDrag = { change, point ->
                            viewModel.canvasDrawer.drawPen(
                                change.position.x,
                                change.position.y)
                            viewModel.canvasDrawer.setPenSize(penSize)
                            viewModel.bitmap = viewModel.canvasDrawer.copyBitmap()
                        },
                        onDragEnd = {
                            viewModel.canvasDrawer.resetLine()
                        }
                    )
                }
        )
    }

    if (showColorDialog) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.canvasDrawer.setPenColor(selectedColor)
                    showColorDialog = false
                }) {
                    Text("Confirm")
                }
            },
            title = { Text("Pick a color") },
            text = {
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    controller = controller,
                    onColorChanged = { envelope ->
                        selectedColor = envelope.color
                        controller.wheelColor = selectedColor
                    }
                )
            }
        )
    }
}