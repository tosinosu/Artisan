package com.tostech.artisan

import android.app.SearchManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
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
//import com.tostech.artisan.nativeadd.TemplateView
import com.tostech.artisan.notification.FirebaseService
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.HashMap
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

     var databaseRef = Firebase.database.reference
     var firebaseUserID = Firebase.auth.currentUser?.uid
      lateinit var appBarConfiguration: AppBarConfiguration
      lateinit var navController: NavController
     val dbSave = Database()
        lateinit var drawerLayout: DrawerLayout
    private var pressedTime: Long = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

         drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        val nav_host = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
         navController = nav_host.navController
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)

/*        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_home, R.id.nav_category, R.id.nav_messages, R.id.nav_order,
                    R.id.nav_profile, R.id.nav_advert_image, R.id.nav_subscribe, R.id.nav_settings, R.id.nav_about, R.id.nav_exit), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration!!)
        navView.setupWithNavController(navController)
        navController = Navigation.findNavController(this, R.id.nav_host_fragment)*/

        findViewById<NavigationView>(R.id.nav_view).setupWithNavController(navController)

        //openFromIntent(user)

        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance())


        val profpixNav = navView.getHeaderView(0).findViewById<CircleImageView>(R.id.profpix_nav)
        val bus_name_nav = navView.getHeaderView(0).findViewById<TextView>(R.id.bus_name_nav)
        val email_nav = navView.getHeaderView(0).findViewById<TextView>(R.id.email_nav)
        val refEmail = FirebaseAuth.getInstance().currentUser!!.email
        val navInboxItem = navView.menu.findItem(R.id.nav_messages)

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        //mRecyclerView = findViewById(R.id.search_recycler)

        val refName = Firebase.database.reference.child("User/$uid/advert/bus_name")
        val pixURL = Firebase.database.reference.child("User/$uid/advert/purl")
       // MobileAds.initialize(this, getString(R.string.real_admob_add_id))


        FirebaseService.sharedPref = getSharedPreferences("sharedPref",Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token
        }

        FirebaseMessaging.getInstance().subscribeToTopic("/topics/$uid")



        if (refEmail.isNullOrEmpty())
            email_nav.text = "Please register or sign in to your account"
        else
            email_nav.text = refEmail

        dbSave.getAdvert(refName, bus_name_nav, applicationContext)
        dbSave.getAdvertPix(pixURL, profpixNav, applicationContext)

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
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.action_sign_out -> {
                val builder = AlertDialog.Builder(this)

                builder.setMessage("You want to log out?")
                    .setPositiveButton(
                        R.string.yes,
                        DialogInterface.OnClickListener { dialogInterface, i ->

                            FirebaseAuth.getInstance().signOut()

                            val intent = Intent(this@MainActivity, LoginActivity2::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()

                        })
                    .setNegativeButton(
                        R.string.no,
                        DialogInterface.OnClickListener { dialogInterface, i ->

                        })
                builder.create()
                builder.show()
                true

            }
          else ->
                false

        }
    }


    override fun onSupportNavigateUp(): Boolean {
     //   val navController = findNavController(R.id.nav_host_fragment)
        //return navController.navigateUp(appBarConfiguration!!) || super.onSupportNavigateUp()

        return NavigationUI.navigateUp(navController, appBarConfiguration)// || super.onSupportNavigateUp()

    }

    override fun onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            if(pressedTime + 2000 > System.currentTimeMillis()){
                super.onBackPressed()
                finish()
            }else{
                 Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
            }
            pressedTime = System.currentTimeMillis()
        }

        // moveTaskToBack(true)

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

             val ref = databaseRef.child("User").child(firebaseUserID!!).child("advert")
             val hashMap = HashMap<String, Any>()
             hashMap["status"] = status
             ref!!.updateChildren(hashMap)

     }


 }

