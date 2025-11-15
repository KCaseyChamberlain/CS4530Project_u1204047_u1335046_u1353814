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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.drawingapp.DrawingApp
import com.example.drawingapp.BuildConfig
import com.example.drawingapp.VisionResponse
import java.io.File
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp

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
    var isAnalyzing by remember { mutableStateOf(true) }

    if (filePath == null) {
        Text("No image found")
        return
    }
    val file = File(filePath)
    val imageUri = Uri.fromFile(file)
    LaunchedEffect(filePath) {
        try {
            isAnalyzing = true
            vm.analyzeImg(context, imageUri)
            Log.d("ImportAiScreen", "Vision API called successfully")
        } catch (e: Exception) {
            Log.e("ImportAiScreen", "Vision API call failed", e)
        } finally {
            isAnalyzing = false
        }
    }


    val bitmap = BitmapFactory.decodeFile(filePath)

    Column(modifier = Modifier.fillMaxSize()
        .padding(top = 48.dp)) {
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)   // Box matches image aspect ratio
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Fit   // no stretch, just fit exactly
            )

            Canvas(modifier = Modifier.matchParentSize()) {
                val responses = vm.visionResponse?.responses ?: emptyList()
                var objIndex = 1

                responses.forEach { result ->
                    result.localizedObjectAnnotations?.forEach { obj ->
                        val verts = obj.boundingPoly.normalizedVertices
                        val xs = verts.mapNotNull { it.x }
                        val ys = verts.mapNotNull { it.y }

                        if (xs.isNotEmpty() && ys.isNotEmpty()) {
                            val leftNorm = xs.minOrNull() ?: 0f
                            val rightNorm = xs.maxOrNull() ?: 0f
                            val topNorm = ys.minOrNull() ?: 0f
                            val bottomNorm = ys.maxOrNull() ?: 0f

                            val left = leftNorm * size.width
                            val right = rightNorm * size.width
                            val top = topNorm * size.height
                            val bottom = bottomNorm * size.height

                            drawRect(
                                color = Color.Red,
                                topLeft = Offset(left, top),
                                size = Size(right - left, bottom - top),
                                style = Stroke(width = 4f)
                            )
                            drawContext.canvas.nativeCanvas.drawText(
                                objIndex.toString(),
                                left,
                                top - 12f,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.RED
                                    textSize = 42f
                                    isFakeBoldText = true
                                }
                            )
                            objIndex++
                        }
                    }
                }
            }
        }
        LazyColumn   (
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(12.dp)
        ) {

            // Display detected objects + labels
            val responses = vm.visionResponse?.responses

            if (isAnalyzing) {
                item {
                    Text(
                        text = "Analyzing image...",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // if we have a response and NO objects anywhere -> show "Nothing Detected"
                val hasObjects =
                    responses?.any { !it.localizedObjectAnnotations.isNullOrEmpty() } == true

                if (responses != null && !hasObjects) {
                    item {
                        Text(
                            text = "Nothing Detected",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    var oIndex = 1
                    responses?.forEach { result ->
                        item {
                            // grab the top label (if any) once per result
                            val topLabel = result.labelAnnotations?.firstOrNull()

                            result.localizedObjectAnnotations?.forEach { obj ->
                                Text("Object $oIndex")
                                // 1) name
                                Text("Name: ${obj.name}")

                                // 2) confidence
                                val confidencePercent = (obj.score * 100).toInt()
                                Text("Confidence: $confidencePercent%")

                                // 3) label (top 1), only if something detected
                                topLabel?.let { label ->
                                    val labelConfidence = (label.score * 100).toInt()
                                    Text("Label: ${label.description} ($labelConfidence%)")
                                }

                                // 4) box coords coords (normalized bounding box)
                                val verts = obj.boundingPoly.normalizedVertices
                                val xs = verts.mapNotNull { it.x }
                                val ys = verts.mapNotNull { it.y }

                                if (xs.isNotEmpty() && ys.isNotEmpty()) {
                                    val left = xs.minOrNull() ?: 0f
                                    val right = xs.maxOrNull() ?: 0f
                                    val top = ys.minOrNull() ?: 0f
                                    val bottom = ys.maxOrNull() ?: 0f

                                    Text("Box Coordinates: left=$left, top=$top, right=$right, bottom=$bottom")
                                }
                                Spacer(Modifier.height(12.dp))
                                oIndex++
                            }
                        }
                    }
                }
            }
        }


        Button(onClick = {
            navController.navigate("file_select")
        },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)) {
            Text("Back")
        }
    }
}
