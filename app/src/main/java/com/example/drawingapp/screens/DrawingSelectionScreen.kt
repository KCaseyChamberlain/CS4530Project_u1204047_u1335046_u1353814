package com.example.drawingapp.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.drawingapp.DrawingApp
import com.example.drawingapp.ImageRepository
import java.util.concurrent.TimeoutException
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items

@Composable
fun DrawingSelectionScreen(navController: NavHostController) {
    val repo = (LocalContext.current.applicationContext as DrawingApp).repository
    DrawingList(
        repo,
        onTimeout = {
            navController.navigate("file_select")
        }
    )
}

@Composable
fun DrawingList(repo: ImageRepository, onTimeout: () -> Unit){
    val drawings by repo.allImages.collectAsState(initial = emptyList())
    LazyColumn (modifier = Modifier
        .fillMaxSize()
        .padding(12.dp)
    ) {
        items(drawings) { image ->
            ///fill in LazyColumn to show drawings, probably as Images?
        }
    }
}