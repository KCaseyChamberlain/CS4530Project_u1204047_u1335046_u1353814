package com.example.drawingapp.screens

import android.R.attr.bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.drawingapp.DrawingApp
import com.example.drawingapp.ImageRepository
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.asImageBitmap
import com.example.drawingapp.shareImageFile
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.absolutePadding
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun DrawingSelectionScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as DrawingApp
    val repo = app.repository
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    //using uri, create an import launcher if the user wants to use an image from another app.
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val bmp = BitmapFactory.decodeStream(input)
                if (bmp != null) {
                    // Save into app storage + DB, then it appears in the list
                    scope.launch {
                        repo.saveImage(context, bmp, "Imported_${System.currentTimeMillis()}")
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .absolutePadding(0.dp, 20.dp, 0.dp, 0.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Import button ABOVE "New Drawing"
        Button(
            onClick = { importLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Import from Gallery")
        }

        Button(
            onClick = { navController.navigate("draw/new") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("New Drawing")
        }

        DrawingList(
            repo,
            onTimeout = { navController.navigate("file_select") },
            navController
        )
    }
}


@Composable
fun DrawingList(repo: ImageRepository, onTimeout: () -> Unit, navController: NavHostController){
    val drawings by repo.allImages.collectAsState(initial = emptyList())
    val context = LocalContext.current   // <— ADD THIS

    //Lazy Column contains a list of all drawings, with
    // buttons to export, delete, and edit each image.
    LazyColumn (modifier = Modifier
        .fillMaxSize()
        .padding(12.dp)
    ) {
        items(drawings) { image ->
            val bitmap = BitmapFactory.decodeFile(image.filepath)
            Column {
                // image name
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Button(onClick = {
                        val encodedPath = Uri.encode(image.filepath)
                        navController.navigate("draw/$encodedPath")
                    }) {
                        Text(image.fileName)
                    }
                }

                // actions
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Button(onClick = { shareImageFile(context, image.filepath) }) { Text("Export") }
                    Button(
                        onClick = { repo.deleteImage(image.id) },
                        modifier = Modifier.padding(start = 8.dp)
                    ) { Text("Delete") }
                }

                // image
                Row {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = image.fileName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}