package com.tostech.artisan

import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.tostech.artisan.data.AdvertData
import com.tostech.artisan.data.UserData

class Database {

    private lateinit var database: DatabaseReference

    val dataListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val data = snapshot.getValue<UserData>()
        }

        override fun onCancelled(error: DatabaseError) {
            Log.w("Database", "loadPost:onCancelled", error.toException())
        }
    }


    internal fun writeUser(
        id: String?,
        username: String?,
        fname: String?,
        lname: String?,
        phone: String?
    ) {
        database = Firebase.database.reference

       // val user = UserData(id, username, fname, lname, phone)
        if (id != null) {
            database.child("User").child(id)

        database.child("User").child(id).child("user").child("username").setValue(username)
        database.child("User").child(id).child("user").child("lname").setValue(lname)
        database.child("User").child(id).child("user").child("fname").setValue(fname)
     //   database.child(id).child("user").child("phone").setValue(phone)

        }
    }

    internal fun readUser() {
        val dataListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.getValue<UserData>()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        database.addValueEventListener(dataListener)
    }

    internal fun writeAdvert(id: String, category: String?, other: String?, busName: String?, advert: String?) {
        database = Firebase.database.reference

        database.child(id)
    //    if (category == "other" && other != ""){
            database.child("User").child(id).child("advert/other").setValue(other)
            database.child("User").child(id).child("advert/category").setValue(category)
        //}
      //  else if (category != "other" && other == "") {
           // database.child("User").child(id).child("advert/category").setValue(category)
        //}
        database.child("User").child(id).child("advert/bus_name").setValue(busName)
        database.child("User").child(id).child("advert/description").setValue(advert)
        database.child("User").child(id).child("advert/uid").setValue(id)


    }

    internal fun writeAdvertText(advert: String?, id:String, value:String?) {
        database = Firebase.database.reference

    //    if (category == "other" && other != ""){
            database.child("User").child(id).child("advert/$advert").setValue(value)

    }


  internal fun writeContacts(id: String?, address: String?, country: String?, state: String?, lga: String?, phoneNum: String?, facebook: String?, twitter: String?, whatsapp: String?, instagram: String?, other: String?) {
        database = Firebase.database.reference

      if (id != null) {
          database.child(id)

        database.child("User").child(id).child("contact/phone").setValue(phoneNum)
        database.child("User").child(id).child("contact/office_address").setValue(address)
        database.child("User").child(id).child("contact/country").setValue(country)
        database.child("User").child(id).child("contact/state").setValue(state)
        database.child("User").child(id).child("contact/lga").setValue(lga)
        database.child("User").child(id).child("contact/whatsapp").setValue(whatsapp)
        database.child("User").child(id).child("contact/facebook").setValue(facebook)
        database.child("User").child(id).child("contact/twitter").setValue(twitter)
        database.child("User").child(id).child("contact/instagram").setValue(instagram)
        database.child("User").child(id).child("contact/other").setValue(other)
    }
  }


    fun savePixUrl(id: String?, profileurl: String){

        if (id != null){

            database = Firebase.database.reference




                database.child("User").child(id).child("advert/purl").setValue(profileurl)

        }
    }

    fun readUserID(): String? {

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
}