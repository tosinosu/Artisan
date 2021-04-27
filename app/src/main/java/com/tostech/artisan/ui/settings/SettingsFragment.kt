package com.tostech.artisan.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tostech.artisan.Database
import com.tostech.artisan.ForgotPassActivity
import com.tostech.artisan.R
import com.tostech.artisan.databinding.FragmentSettingsBinding
import com.tostech.artisan.databinding.VerifyBinding
import kotlinx.android.synthetic.main.fragment_settings.*

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var auth: FirebaseAuth
     var switch: SwitchMaterial? = null
     var changePass: SwitchMaterial? = null
    var showProfile: SwitchMaterial? = null
    private var dbRef = Firebase.database.reference
    var db = Database()
    var uid = db.readUserID()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)



         switch = binding.verifyMailSwitch
         changePass = binding.changePassSwitch
        showProfile = binding.showHideSwitch
        auth = Firebase.auth
        val user = auth.currentUser!!

        val dbProfile = dbRef.child("User").child(uid!!).child("advert")
        dbProfile.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.child("profile").value.toString()
                if (value.isNullOrEmpty() || value == "false")
                    showProfile!!.isChecked = false
                if (value == "true")
                    showProfile!!.isChecked = true

            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        if (user.isEmailVerified){
            switch!!.isChecked = true
            switch!!.isEnabled = false
        }



        switch!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                verifymail(user)
            }

        }
        changePass!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                resetPass()
            }

        }

        showProfile!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setShowHide("true")
            } else{
                setShowHide("false")

            }

        }

        return binding.root
    }

    private fun setShowHide(value: String?) {

        db.writeAdvertText("profile", uid!!, value)
    }

    private fun verifymail(user: FirebaseUser){
        //   val user = auth.currentUser!!
        user.sendEmailVerification()
            .addOnCompleteListener(){ task ->

                if (task.isSuccessful){
                    Toast.makeText(context, "Verification mail sent to ${user.email}" +
                            "Please check your mail to confirm", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(context, "Fail to send verification mail to ${user.email}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun resetPass(){
            val intent = Intent(context,  ForgotPassActivity::class.java)
            startActivity(intent)
    }
}