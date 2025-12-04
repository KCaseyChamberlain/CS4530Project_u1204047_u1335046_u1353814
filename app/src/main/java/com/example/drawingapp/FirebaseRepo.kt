package com.example.drawingapp

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class CloudDrawing(
    val id: String,
    val imageUrl: String,
    val title: String,
    val timestamp: Long
)

class FirebaseRepo(private val auth: FirebaseAuth) {

    private val db = Firebase.firestore

    val thisUser get() = auth.currentUser

    fun registerUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { done ->
                onResult(done.isSuccessful, done.exception?.message)
            }
    }

    fun login(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { done ->
                onResult(done.isSuccessful, done.exception?.message)
            }
    }

    fun signout() {
        auth.signOut()
    }

    fun saveImage(title: String, imageUrl: String) {
        val user = thisUser
        if (user == null) {
            Log.e("FirebaseRepo", "Tried to save image with no logged-in user")
            return
        }

        val newData = hashMapOf(
            "userId" to user.uid,
            "imageUrl" to imageUrl,
            "timestamp" to System.currentTimeMillis(),
            "title" to title
        )

        db.collection("user_drawings")
            .add(newData)
            .addOnSuccessListener { doc ->
                Log.d("FirebaseRepo", "Cloud image saved with ID: ${doc.id}")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseRepo", "Error saving cloud image: ${e.message}")
            }
    }

    fun getUserDrawings(
        onResult: (List<CloudDrawing>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val user = thisUser
        if (user == null) {
            onError(IllegalStateException("User not logged in"))
            return
        }

        db.collection("user_drawings")
            .whereEqualTo("userId", user.uid)
            .get()

            .addOnSuccessListener { snapshot ->
                val drawings = snapshot.documents.mapNotNull { doc ->
                    val imageUrl = doc.getString("imageUrl")
                    val title = doc.getString("title") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: 0L

                    if (imageUrl == null) {
                        null
                    } else {
                        CloudDrawing(
                            id = doc.id,
                            imageUrl = imageUrl,
                            title = title,
                            timestamp = timestamp
                        )
                    }
                }
                onResult(drawings)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
}
