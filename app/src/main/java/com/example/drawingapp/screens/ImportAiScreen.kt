package com.example.drawingapp.screens

import android.app.Application
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.drawingapp.DrawingApp
import com.example.drawingapp.VisionRepository
import com.example.drawingapp.BuildConfig
import com.example.drawingapp.VisionResponse
import kotlinx.coroutines.launch
import java.io.File

class AiViewModel(application: Application) : AndroidViewModel(application) {
    val vision = (application as DrawingApp).visionRepository
    val apiKey = BuildConfig.GEMINI_API_KEY

    var visionResponse by mutableStateOf<VisionResponse?>(null)
        private set

    suspend fun analyzeImg(context: Context, image: Uri) {
        visionResponse = vision.analyzeImage(context, image, apiKey)
    }

    fun loopResults(){
        visionResponse?.responses?.forEach { result ->
            Log.w("Result", result.toString())
        }
    }
}
@Composable
fun ImportAiScreen(navController: NavHostController, filePath: String?) {
    val context = LocalContext.current
    val vm: AiViewModel = viewModel()

    if (filePath == null) {
        Text("No image found")
        return
    }
    val file = File(filePath)
    val imageUri = Uri.fromFile(file)
    LaunchedEffect(filePath) {
        try {
            vm.analyzeImg(context, imageUri)
            Log.d("ImportAiScreen", "Vision API called successfully")
        } catch (e: Exception) {
            Log.e("ImportAiScreen", "Vision API call failed", e)
        }
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

        // Display detected objects -- Temporarily
        vm.visionResponse?.responses?.forEach { result ->
            result.localizedObjectAnnotations?.forEach { obj ->
                Text("Detected: ${obj.name} (Confidence: ${obj.score})")

            }
        }
        Button(onClick = {
            navController.navigate("file_select")
        }) {
            Text("Back")
        }
    }
}
