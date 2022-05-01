package com.tostech.artisan

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
//import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuth.AuthStateListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.tostech.artisan.data.UserData
import com.tostech.artisan.databinding.ActivityLogin2Binding
import com.tostech.artisan.ui.settings.SettingsFragment


private lateinit var binding: ActivityLogin2Binding

private lateinit var mAuth: FirebaseAuth

private const val TAG = "LoginDebug"
private var firebaseUser: FirebaseUser? = null
private var firebaseUserID: String = ""
var refUsers = Firebase.database.reference
private var mAuthListener: AuthStateListener? = null
private var append: String? = null


class LoginActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogin2Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        setSupportActionBar(binding.toolbar)

        mAuth = Firebase.auth



        binding.register.setOnClickListener {
            binding.toolbar.title =  "Register"
            binding.layoutSignIn.isInvisible = true
            binding.layoutRegister.isVisible = true
            it.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
            binding.signin.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))
        }

        binding.signin.setOnClickListener { it ->
            binding.toolbar.title =  "Login"
            binding.layoutRegister.isInvisible = true
            binding.layoutSignIn.isVisible = true
            it.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorAccent))
            binding.register.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimary))

        }

        binding.btnRegister.setOnClickListener {
            signUpUser()
        }

            binding.login.setOnClickListener {
                signInUser()
            }

        binding.forgotPass.setOnClickListener {
            val intent = Intent(this@LoginActivity2, ForgotPassActivity::class.java)
            startActivity(intent)

        }

        binding.termCondition.setOnClickListener {
         val termFragment = TermsDialogFragment()
            termFragment.show(supportFragmentManager, "terms ")
        }

    }

    private fun validateUsername(username: String?): Boolean {
       // refUsers!!.child("User").child(firebaseUserID).child("user").setValue(username)
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
        if (firebaseUser != null){
            updateUI()
        }
    }

    private fun signUpUser() {
        val username = binding.username.text.toString()
        val emailReg = binding.emailReg.text.toString()
        val password = binding.passreg.text.toString()
        val cpassword = binding.cpassReg.text.toString()


        if (username == "") {
            show("Username should not be empty")

            binding.username.error = "Username should not be empty"
        }else if (emailReg == "") {
            show("Email field should not be empty")

            binding.emailReg.error = "Email should not be empty"
        } else if (password == "" || cpassword == "") {
            show("Password field should not be empty")

        } else if (password != cpassword) {
            show("Please enter same values for password fields")

            binding.cpassReg.error = "Please enter the same values for password fields"

        } else if (password == cpassword) {
            if (password.length < 5 || cpassword.length < 5) {
                show("Password length should be more than five")
                binding.passreg.error = "Password length should be more than five"
                binding.cpassReg.error = "Password length should be more than five"

            } else {
               //  checkUsername(username, emailReg, cpassword)
                val validateUser = validateUsername(username)
                val settingsFragment = SettingsFragment()

                if (validateUser) {
                     mAuth.createUserWithEmailAndPassword(emailReg, password)
                                .addOnCompleteListener(
                                    this
                                ) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        //Log.d(TAG, "createUserWithEmail:success")
                                        firebaseUserID = mAuth.currentUser!!.uid
                                        firebaseUser = mAuth.currentUser
                                        refUsers.child("User").child(firebaseUserID).child("sub_status").setValue(3)
                                        val usernameDB =
                                            refUsers.child("User")
                                                .child(
                                                    firebaseUserID
                                                ).child("user")
                                        refUsers.child("User").child(firebaseUserID).child("rating").child("rat_number").setValue(0.0)
                                        refUsers.child("User").child(firebaseUserID).child("rating").child("rat_score").setValue(0.0)


                                        val userHashMap = HashMap<String, Any>()
                                        userHashMap["username"] = username

                                        usernameDB!!.updateChildren(userHashMap)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    settingsFragment.setShowHide("false")
                                                    updateUI()

                                                }
                                            }
                                    } else {
                                        // If sign in fails, display a message to the user.
                                      //  Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                        show(task.exception!!.localizedMessage)

                                    }

                                }
                        } else {
                            show("Enter a unique username")
                            binding.username.error = "Enter a unique username"
                        }


                }
        }
    }

    fun onCheckBoxClicked(view: View){
        if (view is MaterialCheckBox){
            val checked: Boolean = view.isChecked

            when(view.id){
                R.id.termsCheck -> {
                    binding.btnRegister.isEnabled = checked
                }
            }
        }
    }

    private fun updateUI() {


                val intent = Intent(this@LoginActivity2, MainActivity::class.java)
                //    intent.putExtra("user", user)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()

    }

    private fun handleSendText(intent: Intent) {

        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {


        }
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
                       // Log.d(TAG, "signInWithEmail:success")
                      //  firebaseUser = mAuth!!.currentUser
                        updateUI()
                    } else {
                        // If sign in fails, display a message to the user.
                        //Log.w(TAG, "signInWithEmail:failure", task.exception)
                        show(task.exception!!.localizedMessage)

                        val slide = Slide()
                        slide.slideEdge = Gravity.END
                        TransitionManager.beginDelayedTransition(binding.constLogin, slide)
                        binding.forgotPass.visibility = View.VISIBLE

                    }
                }
        }

    }

   private fun checkIfUsernameExists(username: String?, datasnapshot: DataSnapshot): Boolean {
        //Log.d(TAG, "checkIfUsernameExists: checking if $username already exists.")
     //   val user = User()
        for (ds in datasnapshot.children) {
         //   Log.d(TAG, "checkIfUsernameExists: datasnapshot: $ds")
            val userName = ds.getValue<UserData>()?.username

           // Log.d(TAG, "checkIfUsernameExists: username: $userName")
            if (expandUsername(userName).equals(username)) {
             //   Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: $userName")
                return true
            }
        }
        return false
    }

    /**
     * Setup the firebase auth object
     */
    private fun checkUsername(username: String, emailReg:String, password: String) {
        var userName: String? = username
        val userNameDB = refUsers.child("User")
        val settingsFragment = SettingsFragment()

//        Log.d("LoginDebug", "setupFirebaseAuth: setting up firebase auth.")

                userNameDB.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists())  {

                            for (ds in dataSnapshot.children) {
                                val userData = ds.getValue<UserData>()
                                val userName = userData?.username

  //                              Log.d(TAG, "checkIfUsernameExists: sent username: $username")
    //                            Log.d(TAG, "checkIfUsernameExists: firebase username: $userName")
                                if (userName.equals(username)) {
                                   append = userNameDB.push().key?.substring(3, 10)
      //                              Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: $userName")
        //                            Log.d("LoginDebug", "append: $append")
                                    val usernameUse = userName + append
                                    show("Username is not unique try $usernameUse")
                                }else{
                                    mAuth.createUserWithEmailAndPassword(emailReg, password)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                // Sign in success, update UI with the signed-in user's information
                                    //            Log.d("LoginDebug", "createUserWithEmail:success")
                                                firebaseUserID = mAuth.currentUser!!.uid
                                                firebaseUser = mAuth.currentUser
                                                refUsers.child("User").child(firebaseUserID).child("sub_status").setValue(3)
                                                //val usernameDB =
                                                settingsFragment.setShowHide("false")
                                                refUsers.child("User").child(firebaseUserID).child("user").child("username").setValue(userName)
                                                updateUI()
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                //Log.w("LoginDebug createUserWithEmail:failure", task.exception)
                                                show(task.exception!!.localizedMessage)

                                            }

                                        }
                                }
                            }

                            //1st check: Make sure the username is not already in use
                            /*  if (checkIfUsernameExists(userName, dataSnapshot)) {
                            append = refUsers.push().key?.substring(3, 10)
                            Log.d(
                                "LoginDebug",
                                "onDataChange: username already exists. Appending random string to name: $append"
                            )

                            userName = append

                            val usernameUse = userName

                            show("You can try $usernameUse")

                        } else {
                            mAuth.createUserWithEmailAndPassword(emailReg, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("LoginDebug", "createUserWithEmail:success")
                                        firebaseUserID = mAuth.currentUser!!.uid
                                        firebaseUser = mAuth.currentUser
                                        refUsers.child("User").child(firebaseUserID).setValue(2)
                                        //val usernameDB =
                                        settingsFragment.setShowHide("false")
                                        refUsers.child("User").child(firebaseUserID).child("user").child("username").setValue(userName)
                                        updateUI()
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(
                                            "LoginDebug",
                                            "createUserWithEmail:failure",
                                            task.exception
                                        )
                                        show(task.exception!!.localizedMessage)

                                    }

                                }
                        }*/
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
           /* } else {
                // User is signed out
                Log.d("LoginDebug", "onAuthStateChanged:signed_out")
            }*/
      //  }
    }
    private fun expandUsername(username: String?): String? {
        return username!!.replace(".", " ")
    }

    private fun condenseUsername(username: String): String? {
        return username.replace(" ", ".")
    }
}





