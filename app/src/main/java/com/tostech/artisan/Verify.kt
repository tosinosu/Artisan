package com.tostech.artisan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tostech.artisan.databinding.DescriptionBinding
import com.tostech.artisan.databinding.VerifyBinding

class Verify: Fragment() {

    private lateinit var binding: VerifyBinding
    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.verify, container, false)

        /*val switch = binding.verifyMailSwitch
        auth = Firebase.auth
        val user = auth.currentUser!!

        if (user.isEmailVerified){
            Toast.makeText(context, "Email is verified", Toast.LENGTH_SHORT).show()
            switch.isChecked = true
            switch.isEnabled = false
        }


        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                verifymail(user)
            }

        }*/

        return binding.root
    }

    /*private fun verifymail(user: FirebaseUser){
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
    }*/
}