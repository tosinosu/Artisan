package com.tostech.artisan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.tostech.artisan.databinding.ActivityLogin2Binding

private lateinit var binding: ActivityLogin2Binding

private lateinit var mAuth: FirebaseAuth

private const val TAG = "Login"
private var firebaseUser: FirebaseUser? = null
private var firebaseUserID: String = ""
private lateinit var refUsers: DatabaseReference

class LoginActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogin2Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar!!.title = "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        mAuth = Firebase.auth

        binding.register.setOnClickListener {
            binding.layoutSignIn.isInvisible = true
            binding.layoutRegister.isVisible = true
        }

        binding.signin.setOnClickListener {
            binding.layoutRegister.isInvisible = true
            binding.layoutSignIn.isVisible = true
        }

        binding.btnRegister.setOnClickListener {
            signUpUser()
        }

            binding.login.setOnClickListener {
                signInUser()
            }

        binding.forgotPass.setOnClickListener {
            val intent = Intent(this@LoginActivity2,  ForgotPassActivity::class.java)
            startActivity(intent)

        }


    }

    private fun validateUsername(username:String?): Boolean {

        refUsers.child("User/user").setValue(username)
        return true
    }

    private fun show(message: String?){
        Toast.makeText(this@LoginActivity2, message, Toast.LENGTH_SHORT)
            .show()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        firebaseUser = mAuth.currentUser

        Log.v("on start", firebaseUser.toString())

        if (firebaseUser != null){
            updateUI(firebaseUser)

        }
    }

    private fun signUpUser() {
        val username = binding.username.text.toString()
        val emailReg = binding.emailReg.text.toString()
        val password = binding.passreg.text.toString()
        val cpassword = binding.cpassReg.text.toString()


        if (username == ""){
            show("Username should not be empty")

            binding.username.error = "Username should not be empty"
        }
        else if (password =="" || cpassword =="") {
            show( "Password Field should not be empty")

        } else if (password != cpassword) {
            show("Please enter the same values for password fields")

            binding.cpassReg.error = "Please enter the same values for password fields"

        } else if (password == cpassword) {
            if (password.length < 5 || cpassword.length < 5){
                show("Password length should be more than five")
                binding.passreg.error = "Password length should be more than five"
                binding.cpassReg.error = "Password length should be more than five"

            }else {
                val validateUser = validateUsername(username)

                if (validateUser) {
                    mAuth.createUserWithEmailAndPassword(emailReg, password)
                        .addOnCompleteListener(
                            this
                        ) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success")
                                firebaseUserID = mAuth.currentUser!!.uid
                                firebaseUser = mAuth.currentUser
                                refUsers =
                                    FirebaseDatabase.getInstance().reference.child("User").child(
                                        firebaseUserID
                                    ).child("user")
                                val userHashMap = HashMap<String, Any>()
                                userHashMap["username"] = username

                                refUsers.updateChildren(userHashMap)
                                    .addOnCompleteListener {
                                        task ->
                                        if(task.isSuccessful){
                                            updateUI(firebaseUser)

                                        }else{
                                            show("Registration failed")
                                        }
                                    }
                            }
                        else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        show(task.exception!!.localizedMessage)

                    }

                }
            }
                else {
                    show("Enter a unique username")
                    binding.username.error = "Enter a unique username"
                }
            }
        } else{
            show("Please fill the form correctly")
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        val intent = Intent(this@LoginActivity2, MainActivity::class.java)
    //    intent.putExtra("user", user)
        intent.flags =Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()

    }

    private fun signInUser() {
        val email = binding.emailSignIn.text.toString()

        val password = binding.passwordEdit.text.toString()

        if(email == ""){
            show("Email should not be empty")
            binding.emailSignIn.error = "Email should not be empty"

        }else if (password == "") {
            show("Password Field should not be empty")
            binding.passwordEdit.error = "Password Field should not be empty"

        }else{
            mAuth!!.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        firebaseUser = mAuth!!.currentUser
                        updateUI(firebaseUser)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        show(task.exception!!.localizedMessage)

                        binding.forgotPass.visibility = View.VISIBLE

                    }
                }
        }




    }
}





