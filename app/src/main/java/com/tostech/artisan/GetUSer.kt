package com.tostech.artisan

import android.net.Uri
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class GetUSer {


    fun getUserId(): String? {
        val user = Firebase.auth.currentUser

        var uid: String? = null

        if (user != null) {
            // Name, email address, and profile photo Url
            val name = user.displayName
            val email = user.email
            val photoUrl: Uri? = user.photoUrl

            // Check if user's email is verified
            val emailVerified = user.isEmailVerified

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
             uid = user.uid


        }
        return uid
    }

        fun getUserEmail(): String? {
            val user = Firebase.auth.currentUser

            var email: String? = null

            if (user != null) {
                // Name, email address, and profile photo Url
                val name = user.displayName
                 email = user.email
                val photoUrl: Uri? = user.photoUrl

                // Check if user's email is verified
                val emailVerified = user.isEmailVerified

                // The user's ID, unique to the Firebase project. Do NOT use this value to
                // authenticate with your backend server, if you have one. Use
                // FirebaseUser.getIdToken() instead.
                val uid = user.uid

            }

            return email
    }
}