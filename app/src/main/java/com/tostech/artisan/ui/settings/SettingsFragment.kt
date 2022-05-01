package com.tostech.artisan.ui.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.tostech.artisan.Database
import com.tostech.artisan.ForgotPassActivity
import com.tostech.artisan.MainActivity
import com.tostech.artisan.R
import com.tostech.artisan.data.AdvertData
import com.tostech.artisan.data.BusinessListData
import com.tostech.artisan.data.ContactData
import com.tostech.artisan.data.RatingData
import com.tostech.artisan.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var auth: FirebaseAuth
    var switch: SwitchMaterial? = null
    var changePass: SwitchMaterial? = null
    var showProfile: SwitchMaterial? = null
    lateinit var mActivity: FragmentActivity
    private var dbRef = Firebase.database.reference
    var db = Database()
    var uid: String? = ""

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_settings, container, false)
        setUpToolbar()
         switch = binding.verifyMailSwitch
         changePass = binding.changePassSwitch
        showProfile = binding.showHideSwitch
        auth = Firebase.auth
        uid = auth.currentUser!!.uid

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
        val advertDB = dbRef.child("User").child(uid!!)

        showProfile!!.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                advertDB.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                                val advertData = snapshot.child("advert").getValue<AdvertData>()
                                val contactData = snapshot.child("contact").getValue<ContactData>()

                                try {

                                   // Log.d("settingsfr", advertData!!.bus_name!!)
                                    if (advertData!!.bus_name.isNullOrEmpty() || contactData!!.office_address.isNullOrEmpty()
                                        || advertData!!.description.isNullOrEmpty()) {
                                        show("Please edit your profile to show advert")
                                        setShowHide("false")
                                        !showProfile!!.isChecked

                                    } else {
                                        setShowHide("true")
                                        showProfile!!.isChecked

                                    }

                                } catch (ex: NullPointerException) {
                                    ex.printStackTrace()
                                    show("An error occurred: Set your profile and try again")
                                    setShowHide("false")
                                    !showProfile!!.isChecked

                                }

                          //  }
                        } else {
                            show("Data not exist")
                            setShowHide("false")
                            !showProfile!!.isChecked
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        show("An error occurred: Please try again")
                    }

                })
            }else{
                setShowHide("false")
            }
        }

        return binding.root
    }

    fun setShowHide(value: String?) {
        auth = Firebase.auth
        uid = auth.currentUser!!.uid

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


    private fun setUpToolbar() {
        val mainActivity = mActivity as MainActivity
        val  navigationView : NavigationView? = mActivity.findViewById(R.id.nav_view)
        mainActivity.setSupportActionBar(binding.toolbar)
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration =  mainActivity.appBarConfiguration
        NavigationUI.setupActionBarWithNavController(mainActivity, navController, appBarConfiguration!!)
        NavigationUI.setupWithNavController(navigationView!!,navController)

        setHasOptionsMenu(true)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let { mActivity = it}
    }

    fun show(message: String?){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}