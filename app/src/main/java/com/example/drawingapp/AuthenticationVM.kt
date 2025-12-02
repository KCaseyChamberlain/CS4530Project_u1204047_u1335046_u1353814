package com.example.drawingapp

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
class AuthenticationVM(private val repo: FirebaseRepo): ViewModel() {
    var message by mutableStateOf<String?>(null)
        private set
    val thisUser get() = repo.thisUser

    fun register(email: String, password: String){
        repo.registerUser(email, password){ work, fail ->
            message = if(work){
                "Successfully created account!"
            }
            else{
                fail
            }
        }
    }
    fun login(email: String, password: String){
        repo.login(email, password){ work, fail ->
            message = if(work){
                "Login Successful!"
            }
            else{
                fail
            }
        }
    }
    fun logout() {
        repo.signout()
        message = "Signed out"
    }
}