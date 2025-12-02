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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.drawingapp.ui.theme.background
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Divider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DrawingSelectionScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as DrawingApp
    val repo = app.repository
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fRepo = app.firebaseRepo
    val user = fRepo.thisUser
    val userName = user?.email

    var menuPoppedUp by remember { mutableStateOf(false) }


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
                        repo.saveImage(
                            context,
                            bmp,
                            "Imported_${System.currentTimeMillis()}"
                        ) { savedPath ->
                            val encodedPath = Uri.encode(savedPath)
                            CoroutineScope(Dispatchers.Main).launch {
                                navController.navigate("analyze/$encodedPath")
                            }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(12.dp)
            .absolutePadding(0.dp, 20.dp, 0.dp, 0.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (userName != null) {
                Text(userName)
            }
            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = { menuPoppedUp = true }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Account"
                )
            }

            DropdownMenu(
                expanded = menuPoppedUp,
                onDismissRequest = { menuPoppedUp = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Sign Out") },
                    onClick = {
                        fRepo.signout()
                        menuPoppedUp = false
                        navController.navigate("login_screen")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        // Import button ABOVE "New Drawing"
        Button(
            onClick = { importLauncher.launch("image/*")},
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

            val cutFileName = if (image.fileName.length > 8)
                image.fileName.take(8) + "..."
            else
                image.fileName

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = image.fileName,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 12.dp)
                )

                Button(
                    onClick = {
                        val encoded = Uri.encode(image.filepath)
                        navController.navigate("draw/$encoded")
                    },
                    modifier = Modifier.width(120.dp)
                ) {
                    Text(cutFileName)
                }
                Spacer(modifier = Modifier.weight(1f))

                Column(
                    horizontalAlignment = Alignment.End
                ) {

                    // Delete
                    Button(
                        onClick = { repo.deleteImage(image.id) },
                        modifier = Modifier.width(120.dp)
                    ) { Text("Delete") }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Export
                    Button(
                        onClick = { shareImageFile(context, image.filepath) },
                        modifier = Modifier.width(120.dp)
                    ) { Text("Export") }

                    Spacer(modifier = Modifier.height(2.dp))
                    // Analyze image
                    Button(
                        onClick = {
                            val encoded = Uri.encode(image.filepath)
                            navController.navigate("analyze/$encoded")
                        },
                        modifier = Modifier.width(120.dp)
                    ) { Text("Analyze") }
                }
            }
        }
    }
}