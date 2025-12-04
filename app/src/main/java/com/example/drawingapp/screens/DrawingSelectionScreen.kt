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
import androidx.compose.runtime.LaunchedEffect
import com.example.drawingapp.CloudDrawing
import com.example.drawingapp.SharedDrawing
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import java.net.URL

@Composable
fun DrawingSelectionScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as DrawingApp
    val repo = app.repository
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    //login info
    val fRepo = app.firebaseRepo
    val user = fRepo.thisUser
    val userName = user?.email

    //checks log out menu dropped down/up
    var menuPoppedUp by remember { mutableStateOf(false) }

    // --- Cloud drawings state (3.1) ---
    var cloudDrawings by remember { mutableStateOf<List<CloudDrawing>>(emptyList()) }
    var isCloudLoading by remember { mutableStateOf(false) }
    var cloudError by remember { mutableStateOf<String?>(null) }

    // --- Sharing state (3.2) ---
    var sharedWithMe by remember { mutableStateOf<List<SharedDrawing>>(emptyList()) }
    var sharedByMe by remember { mutableStateOf<List<SharedDrawing>>(emptyList()) }
    var sharedError by remember { mutableStateOf<String?>(null) }

    // share dialog state
    var shareTarget by remember { mutableStateOf<CloudDrawing?>(null) }
    var shareEmail by remember { mutableStateOf("") }
    var shareStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user?.uid) {
        if (user != null) {
            // load my cloud images
            isCloudLoading = true
            cloudError = null
            fRepo.getUserDrawings(
                onResult = { list ->
                    cloudDrawings = list
                    isCloudLoading = false
                },
                onError = { e ->
                    cloudError = e.message ?: "Unknown error"
                    isCloudLoading = false
                }
            )

            // load images shared WITH me
            sharedError = null
            fRepo.getSharedWithMe(
                onResult = { list ->
                    sharedWithMe = list
                },
                onError = {
                    sharedError = "Could not load shared images."
                }
            )

            // load images shared BY me
            fRepo.getSharedByMe(
                onResult = { list ->
                    sharedByMe = list
                },
                onError = {
                    sharedError = "Could not load shared images."
                }
            )
        } else {
            cloudDrawings = emptyList()
            sharedWithMe = emptyList()
            sharedByMe = emptyList()
        }
    }

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

    // helper to import a cloud/shared image URL into local storage and open in draw screen
    fun importCloudImage(imageUrl: String, defaultTitle: String) {
        scope.launch(Dispatchers.IO) {
            try {
                URL(imageUrl).openStream().use { input ->
                    val bmp = BitmapFactory.decodeStream(input)
                    if (bmp != null) {
                        repo.saveImage(
                            context,
                            bmp,
                            defaultTitle
                        ) { savedPath ->
                            CoroutineScope(Dispatchers.Main).launch {
                                val encoded = Uri.encode(savedPath)
                                navController.navigate("draw/$encoded")
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // for this assignment we can silently fail; no UI requirement given
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
        //display username,
        //and include dropdown menu to logout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            //keep null check so logging out doesn't crash app
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
                        //sign out through firebase repo
                        fRepo.signout()
                        menuPoppedUp = false
                        //go back to login screen
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

        // --- My Cloud Images + Share (3.1 + 3.2 + 4.0 import) ---
        if (user != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("My Cloud Images")

            when {
                isCloudLoading -> {
                    Text("Loading cloud images...")
                }
                cloudError != null -> {
                    Text("Error loading cloud images: $cloudError")
                }
                cloudDrawings.isEmpty() -> {
                    Text("No cloud images yet")
                }
                else -> {
                    cloudDrawings.forEach { cloud ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = cloud.title.ifBlank { "Untitled cloud image" },
                                modifier = Modifier.weight(1f)
                            )
                            // Import + Share buttons side by side
                            Row {
                                Button(
                                    onClick = {
                                        importCloudImage(
                                            cloud.imageUrl,
                                            cloud.title.ifBlank { "CloudImage" }
                                        )
                                    }
                                ) {
                                    Text("Import")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        shareTarget = cloud
                                        shareEmail = ""
                                        shareStatus = null
                                    }
                                ) {
                                    Text("Share")
                                }
                            }
                        }
                    }
                }
            }

            shareStatus?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(it)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shared With Me (now with Import)
            Text("Shared With Me")
            if (sharedError != null) {
                Text(sharedError!!)
            } else if (sharedWithMe.isEmpty()) {
                Text("No images shared with you yet")
            } else {
                sharedWithMe.forEach { shared ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Shared image",
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                importCloudImage(
                                    shared.imageUrl,
                                    "SharedImage"
                                )
                            }
                        ) {
                            Text("Import")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shared By Me
            Text("Shared By Me")
            if (sharedByMe.isEmpty()) {
                Text("You haven't shared any images yet")
            } else {
                sharedByMe.forEach { shared ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Shared with ${shared.receiverEmail}"
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                fRepo.unshareDrawing(shared.id) { ok, _ ->
                                    if (ok) {
                                        sharedByMe = sharedByMe.filterNot { it.id == shared.id }
                                    }
                                }
                            }
                        ) {
                            Text("Unshare")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DrawingList(
            repo,
            onTimeout = { navController.navigate("file_select") },
            navController
        )
    }

    // --- Share dialog (3.2) ---
    val target = shareTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { shareTarget = null },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = shareEmail.trim()
                        if (trimmed.isNotEmpty()) {
                            fRepo.shareDrawing(
                                imageUrl = target.imageUrl,
                                receiverEmail = trimmed
                            ) { ok, msg ->
                                if (ok) {
                                    shareStatus = "Shared with $trimmed"

                                    // refresh "Shared By Me" list after a successful share
                                    fRepo.getSharedByMe(
                                        onResult = { list -> sharedByMe = list },
                                        onError = {
                                            // optional: you could set sharedError here if you want
                                            // sharedError = "Could not refresh shared images."
                                        }
                                    )
                                } else {
                                    shareStatus = "Failed to share: ${msg ?: "unknown error"}"
                                }
                            }
                        }
                        shareTarget = null
                    }
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                Button(onClick = { shareTarget = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Share drawing") },
            text = {
                Column {
                    Text("Enter recipient email:")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = shareEmail,
                        onValueChange = { shareEmail = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@Composable
fun DrawingList(repo: ImageRepository, onTimeout: () -> Unit, navController: NavHostController){
    val drawings by repo.allImages.collectAsState(initial = emptyList())
    val context = LocalContext.current

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
