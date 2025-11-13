package com.example.drawingapp.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ImportAiScreen(navController: NavHostController, filePath: String?) {
    val context = LocalContext.current

    if (filePath == null) {
        Text("No image found")
        return
    }

    val bitmap = BitmapFactory.decodeFile(filePath)

    Column(modifier = Modifier.fillMaxSize()) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Button(onClick = {
            // TODO: send to AI model or API here
        }) {
            Text("Analyze with AI")
        }

        Button(onClick = {
            navController.navigate("file_select")
        }) {
            Text("Back")
        }
    }
}
