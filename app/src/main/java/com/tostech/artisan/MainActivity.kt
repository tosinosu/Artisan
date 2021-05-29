package com.tostech.artisan

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentTransaction
import com.flutterwave.raveandroid.RavePayActivity
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants
import com.google.android.gms.ads.MobileAds
import com.google.android.material.internal.NavigationMenuItemView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.tostech.artisan.data.Chat
import com.tostech.artisan.notification.FirebaseService
import com.tostech.artisan.ui.messages.MessagesFragment
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.system.exitProcess

 class MainActivity : AppCompatActivity() {

     var databaseRef = Firebase.database.reference
     var firebaseUserID = Firebase.auth.currentUser?.uid
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_category, R.id.nav_messages, R.id.nav_profile, R.id.nav_settings, R.id.nav_exit), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)



        val profpixNav = navView.getHeaderView(0).findViewById<CircleImageView>(R.id.profpix_nav)
        val bus_name_nav = navView.getHeaderView(0).findViewById<TextView>(R.id.bus_name_nav)
        val email_nav = navView.getHeaderView(0).findViewById<TextView>(R.id.email_nav)
        val refEmail = FirebaseAuth.getInstance().currentUser!!.email
        val navInboxItem = navView.menu.findItem(R.id.nav_messages)

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        //mRecyclerView = findViewById(R.id.search_recycler)

        val refName = Firebase.database.reference.child("User/$uid/advert/bus_name")
        val pixURL = Firebase.database.reference.child("User/$uid/advert/purl")
        MobileAds.initialize(this)

        FirebaseService.sharedPref = getSharedPreferences("sharedPref",Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token
        }

        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$uid")




        if (refEmail.isNullOrEmpty())
            email_nav.text = "Please register or sign in to your account"
        else
            email_nav.text = refEmail

        GetData().getAdvert(refName, bus_name_nav, applicationContext)
        GetData().getAdvertPix(pixURL, profpixNav, applicationContext)

        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                var unreadMessages = 0
                for (dataSnapshot in snapshot.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.receiver.equals(uid) && !chat.isseen) {
                        unreadMessages += 1
                    }
                }
                if (unreadMessages == 0) {
                    navInboxItem.title = "Inbox"

                 //   Toast.makeText(this@MainActivity, unreadMessages.toString(), Toast.LENGTH_SHORT).show()
                } else {
                    navInboxItem.title = "Inbox  $unreadMessages"
                   // Toast.makeText(this@MainActivity, unreadMessages.toString(), Toast.LENGTH_SHORT).show()
                }

            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.search_menu, menu)
        menuInflater.inflate(R.menu.main, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }

        return true
    }

    override fun onStart() {
        super.onStart()
        updateStatus("online")

    }
    fun toast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this@MainActivity, LoginActivity2::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
                return  true
            }

        }
        return false
    }



    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {

        super.onBackPressed()
    }

     override fun onPause() {
         super.onPause()
         updateStatus("offline")
     }

     override fun onResume() {
         super.onResume()
         updateStatus("online")
     }
    fun exitBtn(item: MenuItem){

        val builder = AlertDialog.Builder(this)

        builder.setMessage(R.string.exit_prompt)
            .setPositiveButton(
                R.string.yes,
                DialogInterface.OnClickListener { dialogInterface, i ->
                    this.finishAffinity()
                    exitProcess(0)

                })
            .setNegativeButton(
                R.string.no,
                DialogInterface.OnClickListener { dialogInterface, i ->

                })
        builder.create()
        builder.show()
    }

     private fun updateStatus(status: String){
         //val userId = FirebaseAuth.getInstance().currentUser!!.uid

             val ref = FirebaseDatabase.getInstance().reference.child("User").child(firebaseUserID!!).child("advert")
             val hashMap = HashMap<String, Any>()
             hashMap["status"] = status
             ref!!.updateChildren(hashMap)

     }
     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)

         Toast.makeText(this, "OnActivity is called", Toast.LENGTH_SHORT).show()
         if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
             val message = data.getStringExtra("response")
             if (message != null) {
                 val ref = databaseRef.child("User/$firebaseUserID/subscribe")
                 ref.addListenerForSingleValueEvent(object : ValueEventListener {
                     override fun onDataChange(snapshot: DataSnapshot) {
                         ref.child(message)
                     }

                     override fun onCancelled(error: DatabaseError) {

                     }
                 })

                 // Log.d("rave response", message)
             }
             when (resultCode) {
                 RavePayActivity.RESULT_SUCCESS -> {
                     Toast.makeText(this, "SUCCESS $message", Toast.LENGTH_SHORT).show()
                 }
                 RavePayActivity.RESULT_ERROR -> {
                     Toast.makeText(this, "ERROR $message", Toast.LENGTH_SHORT).show()
                 }
                 RavePayActivity.RESULT_CANCELLED -> {
                     Toast.makeText(this, "CANCELLED $message", Toast.LENGTH_SHORT)
                         .show()
                 }
             }
         }else{
             super.onActivityResult(requestCode, resultCode, data)
         }

         return super.onActivityResult(requestCode, resultCode, data)

         /* if (resultCode === RaveConstants.RESULT_SUCCESS) {
              when (requestCode) {
                  RaveConstants.PIN_REQUEST_CODE -> {
                      val pin = data!!.getStringExtra(PinFragment.EXTRA_PIN)
                      // Use the collected PIN
                      cardPayManager.submitPin(pin)
                  }
                  RaveConstants.ADDRESS_DETAILS_REQUEST_CODE -> {
                      val streetAddress = data!!.getStringExtra(AVSVBVFragment.EXTRA_ADDRESS)
                      val state = data.getStringExtra(AVSVBVFragment.EXTRA_STATE)
                      val city = data.getStringExtra(AVSVBVFragment.EXTRA_CITY)
                      val zipCode = data.getStringExtra(AVSVBVFragment.EXTRA_ZIPCODE)
                      val country = data.getStringExtra(AVSVBVFragment.EXTRA_COUNTRY)
                      val address = AddressDetails(streetAddress, city, state, zipCode, country)

                      // Use the address details
                      cardPayManager.submitAddress(address)
                  }
                  RaveConstants.WEB_VERIFICATION_REQUEST_CODE ->                     // Web authentication complete, proceed
                      cardPayManager.onWebpageAuthenticationComplete()
                  RaveConstants.OTP_REQUEST_CODE -> {
                      val otp = data!!.getStringExtra(OTPFragment.EXTRA_OTP)
                      // Use OTP
                      cardPayManager.submitOtp(otp)
                  }
              }
          }

          if (requestCode === RaveConstants.RAVE_REQUEST_CODE && data != null) {
              val message = data.getStringExtra("response")
              if (message != null) {
                  Log.d("rave response", message)
              }
              if (resultCode === RavePayActivity.RESULT_SUCCESS) {
                  Toast.makeText(this, "SUCCESS $message", Toast.LENGTH_SHORT).show()
              } else if (resultCode === RavePayActivity.RESULT_ERROR) {
                  Toast.makeText(this, "ERROR $message", Toast.LENGTH_SHORT).show()
              } else if (resultCode === RavePayActivity.RESULT_CANCELLED) {
                  Toast.makeText(this, "CANCELLED $message", Toast.LENGTH_SHORT).show()
              }
          } else if (requestCode === RaveConstants.WEB_VERIFICATION_REQUEST_CODE) {
              cardPayManager.onWebpageAuthenticationComplete()
          } else {
              super.onActivityResult(requestCode, resultCode, data)
          }*/
     }

 }

