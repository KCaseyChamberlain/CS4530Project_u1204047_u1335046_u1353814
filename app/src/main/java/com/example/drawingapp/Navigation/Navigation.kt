package com.example.navigationdemo.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavHost
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.example.drawingapp.screens.DrawingScreen
import com.example.drawingapp.screens.SplashScreen

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String="home")
{
    NavHost(navController=navController, startDestination=startDestination)
    {
        composable("home") {
            SplashScreen(navController)
        }

        composable("draw") {
            DrawingScreen(navController)
        }
    }
}