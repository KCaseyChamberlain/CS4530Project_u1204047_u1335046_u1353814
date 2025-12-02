package com.example.drawingapp.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.drawingapp.AuthenticationVM
import com.example.drawingapp.DrawingApp

@Composable
fun LoginScreen(navController: NavHostController){
    val context = LocalContext.current.applicationContext as DrawingApp
    val repo = context.firebaseRepo
    val vm = remember { AuthenticationVM(repo) }

    //login information, sends to firebase
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) }

    LaunchedEffect(vm.thisUser) {
        //if we are already logged in, go directly to file selection screen
        if(vm.thisUser != null){
            navController.navigate("file_select")
        }
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = if (isLoginMode) "Login" else "Create Account",
                fontSize = 22.sp,
            )

            Spacer(Modifier.height(20.dp))

            //email and password text fields

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )

            //error message or success message
            vm.message?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = Color.Red)
            }

            Spacer(Modifier.height(24.dp))

            //based on mode, either registers user or logs them in
            Button(
                onClick = {
                    if (isLoginMode) {
                        vm.login(email, password)
                    } else {
                        vm.register(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text(text = if (isLoginMode) "Log In" else "Sign Up")
            }

            Spacer(Modifier.height(10.dp))

            TextButton(
                onClick = { isLoginMode = !isLoginMode }
            ) {
                Text(
                    if (isLoginMode)
                        "Need an account? Sign up"
                    else
                        "Already registered? Log in"
                )
            }
            //for now, while firebase isn't working, bypass button
            // we can probably remove this, but it doesn't break anything
            OutlinedButton(
                onClick = {
                    navController.navigate("file_select") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue Without Login")
            }
        }
    }
}



