package com.example.drawingapp

import com.google.firebase.auth.FirebaseAuth

class FirebaseRepo(private val auth : FirebaseAuth) {
    fun registerUser(email: String, password: String, onResult: (Boolean, String?) -> Unit){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { done ->
                onResult(done.isSuccessful, done.exception?.message)
            }
    }
    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { done ->
                onResult(done.isSuccessful, done.exception?.message)
            }
    }
    fun signout(){
        auth.signOut()
    }
    val thisUser get() = auth.currentUser
}