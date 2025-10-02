package com.example.drawingapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.drawingapp.R
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.testTag

@Composable
fun SplashScreen(navController: NavHostController) {
    SplashScreen(
        onTimeout = {
            navController.navigate("draw")
        }
    )
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true          // animate in
        delay(2000)             // hold on screen
        visible = false         // animate out
        delay(800)              // give time for exit animation
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .testTag("splash"),
        contentAlignment = Alignment.Center

    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(800)) + slideInHorizontally(
                initialOffsetX = { -it }, // start off screen left
                animationSpec = tween(800)
            ),
            exit = fadeOut(animationSpec = tween(800)) + slideOutHorizontally(
                targetOffsetX = { it },   // slide off screen right
                animationSpec = tween(800)
            )
        ) {
            Image(
                painter = painterResource(R.drawable.artlogo),
                contentDescription = "Splash logo",
                modifier = Modifier.size(128.dp)
            )
        }
    }
}