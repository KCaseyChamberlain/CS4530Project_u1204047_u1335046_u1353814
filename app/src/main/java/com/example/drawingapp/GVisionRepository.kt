package com.example.drawingapp
import android.content.Context
import android.net.Uri
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.client.call.*
import kotlinx.serialization.Serializable
import android.util.Base64
import android.util.Log
import androidx.compose.ui.input.key.Key
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//https://cloud.google.com/vision/docs/base64 image request layout information

//image we pass in to Google Vision
@Serializable
data class VisImage(
    val content: String
)

//features we define, and number of results
@Serializable
data class VisFeature(
    val type: String,
    val maxResults: Int = 5
)

//request with the image and features
@Serializable
data class VisRequest(
    val image: VisImage,
    val features: List<VisFeature>
)

//list of VisRequests
@Serializable
data class VisionRequests(
    val requests: List<VisRequest>
)

//result, with list of detected objects
//matching variable names with api names
@Serializable
data class VisionResult(
    val localizedObjectAnnotations: List<DetectedObj>? = emptyList(),
    val labelAnnotations: List<VisionLabel>? = emptyList()
)

//image description and confidence score
@Serializable
data class VisionLabel(
    val description: String,
    val score: Float
)

//detected objects, with a name, confidence score, and bounding box
@Serializable
data class DetectedObj(
    val name: String,
    val score: Float, //confidence
    val boundingPoly: BoundingBox
)

//bounding box, which is a list of points
@Serializable
data class BoundingBox(
    val normalizedVertices: List<Point>
)

//points of the box, (x,y)
@Serializable
data class Point(
    val x: Float? = null,
    val y: Float? = null
)

//list of results from google vision
@Serializable
data class VisionResponse(
    val responses: List<VisionResult>? = null
)


//https://cloud.google.com/vision/docs/base64 has more info on making requests
//using the http ktor client (in DrawingApp), we can use our imported image
//convert it to be uploaded, and create a request for Google Vision API.
//sends to api, then we store the response
class VisionRepository(private val client: HttpClient) {
    suspend fun analyzeImage(context: Context, imageUri: Uri, apiKey: String): VisionResponse {
        //uses image uri, sends to Google Vision
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes() ?: error("Failed to upload image")
        inputStream.close()
        val convertedImage = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        val request = VisionRequests(
            requests = listOf(
                VisRequest(
                    image = VisImage(content = convertedImage),
                    features = listOf(
                        VisFeature(type = "OBJECT_LOCALIZATION", maxResults = 10),
                        VisFeature(type = "LABEL_DETECTION", maxResults = 5)
                    )
                )
            )
        )
        //api access, using api key
        val response: VisionResponse = client.post(
            "https://vision.googleapis.com/v1/images:annotate?key=$apiKey"
        ) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return response
    }
}