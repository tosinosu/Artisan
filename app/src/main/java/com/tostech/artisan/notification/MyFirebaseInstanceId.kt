package com.tostech.artisan.notification

import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseInstanceId: FirebaseMessagingService() {
    var token1 = ""
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
         FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful)
                    return@OnCompleteListener

                 token1 = task.result
                if (firebaseUser != null){
                    updateToken(token1)
                }

            })
    }

    private fun updateToken(refreshToken: String?) {
        Toast.makeText(applicationContext, refreshToken, Toast.LENGTH_SHORT).show()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseDatabase.getInstance().getReference().child("Tokens")
        val token = Token(refreshToken)
        ref.child(firebaseUser!!.uid).setValue(token)
    }
}