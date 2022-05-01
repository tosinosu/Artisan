package com.tostech.artisan

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.tostech.artisan.databinding.ActivityForgotPassBinding

class ForgotPassActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPassBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPassBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.toolbar.title = "Reset Password"
        binding.btnReset.setOnClickListener {
            val email: String = binding.emailReset.text.toString().trim { it <= ' ' }

            if (email == "")
                Toast.makeText(
                    this@ForgotPassActivity,
                    "Please enter a password",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@ForgotPassActivity,
                                "Check your email to reset password",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        } else {
                            Toast.makeText(
                                this@ForgotPassActivity,
                                task.exception!!.localizedMessage!!,
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
            }
        }


    }

}