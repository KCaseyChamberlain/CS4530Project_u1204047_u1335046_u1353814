package com.example.navigationdemo.Navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.drawingapp.screens.DrawingScreen
import com.example.drawingapp.screens.SplashScreen
import com.example.drawingapp.screens.DrawingSelectionScreen

@Composable
fun AppNavHost(navController: NavHostController, startDestination: String="home")
{
    NavHost(navController=navController, startDestination=startDestination)
    {
        composable("home") {
            SplashScreen(navController)
        }

        composable(
            route = "draw/{filePath}",
            arguments = listOf(navArgument("filePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("filePath")
            val filePath = if (path == "new") null else path
            DrawingScreen(navController, filePath)
        }

        composable("file_select"){
            DrawingSelectionScreen(navController)
        }
    }
}