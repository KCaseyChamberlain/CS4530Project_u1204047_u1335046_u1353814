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

data class SharedDrawing(
    val id: String,
    val imageUrl: String,
    val title: String,
    val senderId: String,
    val receiverEmail: String,
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

    // save a cloud backup of an image
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

    // share a drawing with another user by email
    fun shareDrawing(
        imageUrl: String,
        title: String,
        receiverEmail: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val user = thisUser
        if (user == null) {
            onComplete(false, "User not logged in")
            return
        }

        val data = hashMapOf(
            "imageUrl" to imageUrl,
            "title" to title,
            "senderId" to user.uid,
            "receiverEmail" to receiverEmail.trim().lowercase(),
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("shared_drawings")
            .add(data)
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }

    // list drawings I have shared
    fun getSharedByMe(
        onResult: (List<SharedDrawing>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val user = thisUser
        if (user == null) {
            onError(IllegalStateException("User not logged in"))
            return
        }

        db.collection("shared_drawings")
            .whereEqualTo("senderId", user.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val imageUrl = doc.getString("imageUrl")
                    val title = doc.getString("title") ?: ""
                    val senderId = doc.getString("senderId")
                    val receiverEmail = doc.getString("receiverEmail")
                    val timestamp = doc.getLong("timestamp") ?: 0L

                    if (imageUrl == null || senderId == null || receiverEmail == null) {
                        null
                    } else {
                        SharedDrawing(
                            id = doc.id,
                            imageUrl = imageUrl,
                            title = title,
                            senderId = senderId,
                            receiverEmail = receiverEmail,
                            timestamp = timestamp
                        )
                    }
                }
                onResult(list)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    // list drawings shared *to* me
    fun getSharedWithMe(
        onResult: (List<SharedDrawing>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val user = thisUser
        val email = user?.email?.trim()?.lowercase()
        if (email == null) {
            onError(IllegalStateException("User not logged in or email missing"))
            return
        }

        db.collection("shared_drawings")
            .whereEqualTo("receiverEmail", email)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val imageUrl = doc.getString("imageUrl")
                    val title = doc.getString("title") ?: ""
                    val senderId = doc.getString("senderId")
                    val receiverEmail = doc.getString("receiverEmail")
                    val timestamp = doc.getLong("timestamp") ?: 0L

                    if (imageUrl == null || senderId == null || receiverEmail == null) {
                        null
                    } else {
                        SharedDrawing(
                            id = doc.id,
                            imageUrl = imageUrl,
                            title = title,
                            senderId = senderId,
                            receiverEmail = receiverEmail,
                            timestamp = timestamp
                        )
                    }
                }
                onResult(list)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    // unshare (delete a shared_drawings doc)
    fun unshareDrawing(
        sharedId: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        db.collection("shared_drawings")
            .document(sharedId)
            .delete()
            .addOnSuccessListener {
                onComplete(true, null)
            }
            .addOnFailureListener { e ->
                onComplete(false, e.message)
            }
    }
}
