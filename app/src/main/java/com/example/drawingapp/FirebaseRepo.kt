package com.example.drawingapp

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime

class FirebaseRepo(private val auth : FirebaseAuth) {
    private val db = Firebase.firestore
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveImage(title: String, image: String){
        val newData = hashMapOf(
            "User: " to thisUser?.uid,
            "imageUrl: " to image,
            "Timestamp: " to LocalDateTime.now().toString(),
            "Title: " to title
        )

        db.collection("user_drawings")
            .add(newData)
            .addOnSuccessListener {
                Log.e("Success", "Data saved with ID: ${it.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Failure", "Error saving data: ${e.message}")
            }
    }
    val thisUser get() = auth.currentUser
}