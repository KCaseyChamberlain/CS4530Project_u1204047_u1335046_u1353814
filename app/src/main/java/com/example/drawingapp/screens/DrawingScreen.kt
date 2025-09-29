package com.example.drawingapp.screens
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.drawingapp.CanvasDrawer
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.input.pointer.pointerInput


@Composable
fun DrawingScreen(navController: NavHostController){
    //placeholder pen menu
    val penOptions = listOf("Pen 1", "Pen 2", "Pen 3")
    var droppedDown by remember { mutableStateOf(false) }
    var selectedPen by rememberSaveable {mutableStateOf(penOptions[0])}

    //uses CanvasDrawer class, and copies bitmap so it can be redrawn every time a change is made
    val canvasDrawer = remember { CanvasDrawer(1000) }
    var bitmap by rememberSaveable { mutableStateOf(canvasDrawer.copyBitmap()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        //drop down pen menu, very basic
        //
        // this drop down can be removed later once there's a more encompassing UI class
        //
        //
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            //when pressed, droppedDown is true, and it shows the list of pens.
            Box {
                TextButton(onClick = {droppedDown = true}) {
                    Text(selectedPen)
                }

                DropdownMenu(
                    expanded = droppedDown,
                    onDismissRequest = {droppedDown = false}
                ) {
                    penOptions.forEach { pen ->
                        DropdownMenuItem(
                            text = {Text(pen)},
                            onClick = {
                                selectedPen = pen
                                droppedDown = false
                            }
                        )
                    }
                }
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
                            canvasDrawer.drawPen(
                                point.x,
                                point.y)
                            bitmap = canvasDrawer.copyBitmap()
                        },
                        onDrag = { change, point ->
                            canvasDrawer.drawPen(
                                change.position.x,
                                change.position.y)
                            bitmap = canvasDrawer.copyBitmap()
                        }
                    )
                }
        )
    }
}