package com.example.drawingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.drawingapp.ui.theme.DrawingAppTheme
import com.example.navigationdemo.Navigation.AppNavHost


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingAppTheme {
                val myNavControll = rememberNavController()
                AppNavHost(myNavControll)
            }
        }
    }
}
