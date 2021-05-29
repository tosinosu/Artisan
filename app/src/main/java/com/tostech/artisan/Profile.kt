package com.tostech.artisan

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.flutterwave.raveandroid.data.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.tostech.artisan.data.AddressData
import com.tostech.artisan.databinding.ProfileBinding
import kotlinx.coroutines.*
import kotlin.concurrent.thread
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.RuntimeException
import java.lang.Thread.yield
import java.nio.charset.Charset
import kotlinx.coroutines.yield as yield1

class Profile: Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var binding: ProfileBinding
    var databaseRef = Firebase.database.reference
    var firebaseUserID: String = ""
    var valid = true



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.profile, container, false)

      //  enableViews()

        binding.edtTxtOther.isEnabled = false

        binding.profileBtnOk.setOnClickListener {
            verifyData()
            try {
                getLongLat()
            }catch (ex: RuntimeException){
                Toast.makeText(context, ex.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        runBlocking {
            launch {
        getData()
            }
        }

        val categoryArray = resources.getStringArray(R.array.artisanList)
        val adapter = ArrayAdapter(
            requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            categoryArray
        )
        binding.spinnerCategories.adapter = adapter

        binding.spinnerCategories.onItemSelectedListener = this


        spinner()


        return binding.root
    }


    fun enableViews(){

        binding.txtInputOther.setOnClickListener {
            binding.txtInputOther.isEnabled =true
        }
        binding.textInputLayout7.setOnClickListener {
            binding.textInputLayout7.isEnabled = true
        }
        binding.textInputLayout6.setOnClickListener {
            binding.textInputLayout6.isEnabled = true
        }
        binding.textInputLayout2.setOnClickListener {
            binding.textInputLayout2.isEnabled = true
        }
        binding.textInputLayout3.setOnClickListener {
            binding.textInputLayout3.isEnabled = true
        }

        binding.textInputLayout4.setOnClickListener {
            binding.textInputLayout4.isEnabled = true
        }
        binding.textInputLayout11.setOnClickListener {
            binding.textInputLayout11.isEnabled = true
        }
        binding.textInputLayout8.setOnClickListener {
            binding.textInputLayout8.isEnabled = true
        }
        binding.textInputLayout10.setOnClickListener {
            binding.textInputLayout10.isEnabled = true
        }
        binding.textInputLayout5.setOnClickListener {
            binding.textInputLayout5.isEnabled = true
        }
        binding.textInputLayout9.setOnClickListener {
            binding.textInputLayout9.isEnabled = true
        }
    }

    private suspend fun getData(){

        val userId = Database().readUserID()
        firebaseUserID = FirebaseAuth.getInstance().uid.toString()

            if (firebaseUserID != null) {
                // Call Advert database
                val userName = databaseRef.child("User").child(firebaseUserID).child("user/username")
                val fName = databaseRef.child("User").child(firebaseUserID).child("user/fname")
                val lName = databaseRef.child("User").child(firebaseUserID).child("user/lname")
                val user = databaseRef.child("User").child(firebaseUserID).child("user/lname")

                //Contact database
                val phone = databaseRef.child("User").child(firebaseUserID).child("contact/phone")
                val address = databaseRef.child("User").child(firebaseUserID).child("contact/address")
                val country = databaseRef.child("User").child(firebaseUserID).child("contact/country")
                val state = databaseRef.child("User").child(firebaseUserID).child("contact/state")
                val lga = databaseRef.child("User").child(firebaseUserID).child("contact/lga")
                val whatsapp = databaseRef.child("User").child(firebaseUserID).child("contact/whatsapp")
                val facebook = databaseRef.child("User").child(firebaseUserID).child("contact/facebook")
                val twitter = databaseRef.child("User").child(firebaseUserID).child("contact/twitter")
                val instagram = databaseRef.child("User").child(firebaseUserID).child("contact/instagram")
                val other = databaseRef.child("User").child(firebaseUserID).child("contact/other")
                val bus_name = databaseRef.child("User").child(firebaseUserID).child("advert/bus_name")

                val description = databaseRef.child("User").child(firebaseUserID).child("advert/description")


                withContext(Dispatchers.Default){

                GetData().getAdvert(userName, binding.user, requireContext())
                GetData().getAdvert(fName, binding.fname, requireContext())
                GetData().getAdvert(lName, binding.lname, requireContext())

                GetData().getAdvert(phone, binding.phone, requireContext())

                GetData().getAdvert(address, binding.edtOfficeAddr, requireContext())
                GetData().getAdvert(bus_name, binding.txtInpBusName, requireContext())
                GetData().getAdvert(description, binding.txtInpAdvert, requireContext())


                GetData().getAdvert(whatsapp, binding.edWhatsapp, requireContext())
                GetData().getAdvert(facebook, binding.edtFacebook, requireContext())
                GetData().getAdvert(twitter, binding.edTwitter, requireContext())
                GetData().getAdvert(instagram, binding.edInstagram, requireContext())
                GetData().getAdvert(other, binding.edtOtherAdd, requireContext())
                }
            }

    }

    private fun spinner() {
        val countriesList: ArrayList<String> = ArrayList()

                try {
            // As we have JSON object, so we are getting the object
            //Here we are calling a Method which is returning the JSON object
                    val jsonElement = JsonParser.parseString(getJsonFromAsset(requireContext(), "countries.json")!!)
                    var jObject = jsonElement.asJsonObject
                   // jObject = jObject.getAsJsonObject("countries")
                    val jsonArray = jObject.getAsJsonArray("countries")

                    Log.v("JSONSIZE", "Json size is ${jsonArray.size()}")

                    for (i in 0 until jsonArray.size()) {

                        jObject = jsonArray.get(i).asJsonObject
                        // Create a JSONObject for fetching single User's Data

                        //val user = usersArray.getJSONObject(i)
                        // Fetch id store it in variable
                        val id = jObject.get("id").asString
                        val name = jObject.get("name").asString

                        // Now add all the variables to the data model class and the data model class to the array list.
                        //  val countriesDetails = Countries(id, name)

                        // add the details in the list
                        countriesList.add(name)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

        val adapter = ArrayAdapter(
            requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            countriesList
        )
        binding.spinnerCountry.adapter = adapter
        binding.spinnerCountry.setSelection(159)

        var stateID: String


        binding.spinnerCountry.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                var stateList: ArrayList<String> = ArrayList()

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    stateList.clear()

                    try {

                        val jsonElementState = JsonParser.parseString(getJsonFromAsset(requireContext(), "states.json")!!)
                        var jObjectState = jsonElementState.asJsonObject
                        // jObject = jObject.getAsJsonObject("countries")
                        val jsonArray = jObjectState.getAsJsonArray("states")

                        Log.v("JSONSIZE", "Json size is ${jsonArray.size()}")

                        for (i in 0 until jsonArray.size()) {

                            jObjectState = jsonArray.get(i).asJsonObject
                            // Create a JSONObject for fetching single User's Data

                            //val user = usersArray.getJSONObject(i)
                            // Fetch id store it in variable
                            val countryid = jObjectState.get("country_id").asString
                            val name = jObjectState.get("name").asString
                            // Now add all the variables to the data model class and the data model class to the array list.

                            if ((position + 1).toString() == countryid) {

                                stateList.add(name)

                            }

                        }
                    } catch (e: JSONException) {
                        //exception
                        e.printStackTrace()
                    }

                    val adapter2 = ArrayAdapter(
                        requireActivity(),
                        R.layout.support_simple_spinner_dropdown_item,
                        stateList
                    )

                    binding.spinnerState.adapter = adapter2

                    //   binding.spinnerState.setSelection(24)

                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Code to perform some action when nothing is selected
                }
            }


        binding.spinnerState.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                val LGAList: ArrayList<String> = ArrayList()

                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    position: Int,
                    p3: Long
                ) {

                    val selectedState: String? = binding.spinnerState.selectedItem.toString()

                    LGAList.clear()

                    try {

                        val jElementLGA = JsonParser.parseString(
                            getJsonFromAsset(
                                requireContext(),
                                "cities.json"
                            )!!
                        )
                        val jElementState = JsonParser.parseString(
                            getJsonFromAsset(
                                requireContext(),
                                "states.json"
                            )!!
                        )


                        val jObjectState = jElementState.asJsonObject
                        val jObjectLGA = jElementLGA.asJsonObject

                        // jObject = jObject.getAsJsonObject("countries")
                        val statesArray = jObjectState.getAsJsonArray("states")
                        val citiesArray = jObjectLGA.getAsJsonArray("cities")


                        for (i in 0 until statesArray.size()) {
                            val state = statesArray.get(i).asJsonObject
                            val stateid = state.get("id").asString
                            val stateName = state.get("name").asString
                            if (selectedState == stateName) {

                                for (i in 0 until citiesArray.size()) {
                                    val cities = citiesArray.get(i).asJsonObject
                                    // Fetch id store it in variable
                                    val stateID2 = cities.get("state_id").asString
                                    val name = cities.get("name").asString

                                    if (stateid == stateID2) {

                                        // add the details in the list
                                        LGAList.add(name)
                                    }
                                }
                            }
                        }

                    } catch (e: JSONException) {
                        //exception
                        e.printStackTrace()
                    }
                    val adapterLGA = ArrayAdapter(
                        requireActivity(),
                        R.layout.support_simple_spinner_dropdown_item,
                        LGAList
                    )
                    yield()
                        binding.spinnerCities.adapter = adapterLGA
                        //  binding.spinnerCities.setSelection(5)


                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
    }
}

    private fun verifyData() {

        val currentUserId = Firebase.auth.uid.toString()

        val fname: String? = binding.fname.text.toString()
        val lname: String? = binding.lname.text.toString()
        val user: String? = binding.user.text.toString()
        val phone: String? = binding.phone.text.toString()
        val other: String? = binding.edtTxtOther.text.toString()
        val category: String? = binding.spinnerCategories.selectedItem.toString()
        val country: String? = binding.spinnerCountry.selectedItem.toString()

        val state: String? = binding.spinnerState.selectedItem.toString()
        
        val lga = try {
            binding.spinnerCities.selectedItem.toString()
        }catch (ie: java.lang.NullPointerException){
            "null"
        }
        val office: String? = binding.edtOfficeAddr.text.toString()
        val facebook: String? = binding.edtFacebook.text.toString()
        val twitter: String? = binding.edTwitter.text.toString()
        val whatsapp: String? = binding.edWhatsapp.text.toString()
        val instagram: String? = binding.edInstagram.text.toString()
        val otherLink: String? = binding.edtOtherAdd.text.toString()

        val busName: String? = binding.txtInpBusName.text.toString()
        val advert: String? = binding.txtInpAdvert.text.toString()



    val db = Database()

        if (fname == null || fname!!.length < 3) {
            valid = false
            binding.fname?.error = "A valid first name is required"
        }
        if (office!!.length < 5) {
            valid = false
            binding.edtOfficeAddr?.error = "Minimum of five characters are required for office address"
        }
        if(other!!.length < 3){
            valid = false
            binding.edtTxtOther?.error = "Enter a valid business"
        }

        if (busName == null || busName.length < 5){
            valid = false
            binding.txtInpBusName?.error = "A valid business name is required"
        }
        if (advert == null || advert.length < 10){
            valid = false
            binding.txtInpAdvert?.error = "A valid advert is required. Advert is too short"
        }
        if (advert == null || advert.length < 10){
            valid = false
            binding.txtInpAdvert?.error = "A valid advert is required. Advert is too short"
        }
        if (advert!!.length >= 100){
            valid = false
            binding.txtInpAdvert.error = "Maximum of 100 characters exceeded"
        }

        if (lname == null || lname.length < 3) {
            valid = false
            binding.lname.error = "A valid last name is required"
        }
        if (phone!!.length < 6) {
            valid = false
            binding.phone.error = "A valid phone number is required"
        }

        if (category == "Choose Business Category"){
            valid = false
            Toast.makeText(context, "Choose business category", Toast.LENGTH_SHORT).show()
        }
        if (facebook != "" && !(!facebook!!.contains("facebook") || facebook.length < 2)){
            valid = false
            binding.edtFacebook?.error = "Enter your valid facebook id"
        }
        if (twitter != "" && !(!twitter!!.contains("twitter") || twitter!!.length < 2)){
            valid = false
            binding.edTwitter?.error = "Enter your valid twitter id (e.g example)"
        }

        if (whatsapp != "" && whatsapp!!.contains("+") || !((whatsapp!!.contains("wa.me")) || whatsapp!!.contains("whatsapp.com"))){
            valid = false
            binding.edWhatsapp!!.error = "Enter your valid WhatsApp url"
        }
        if (instagram != "" && !(instagram!!.contains("instagram") || instagram!!.length < 2)){
            valid = false
            binding.edInstagram.error = "Enter your valid instagram id"
        }
        if (otherLink != "" && !(otherLink!!.contains("."))){
            valid = false
            binding.edtOtherAdd.error = "Enter a valid url"
        }

        if (!valid){
            Toast.makeText(context, "Error: Please review the form and submit", Toast.LENGTH_SHORT).show()
        }

        if (valid) {

            db.writeUser(currentUserId, user, fname, lname, phone)
            db.writeContacts(
                currentUserId,
                office,
                country,
                state,
                lga,
                phone,
                facebook,
                twitter,
                whatsapp,
                instagram,
                otherLink
            )

            db.writeAdvert(currentUserId, category, other, busName, advert)

            Toast.makeText(context, "Data is being saved", Toast.LENGTH_LONG).show()
        }



    }

    fun getJsonFromAsset(context: Context, filename: String): String? {
        var json: String? = null
        val charset: Charset = Charsets.UTF_8
        try {
            val myUsersJSONFile = context.assets.open(filename)
            val size = myUsersJSONFile.available()
            val buffer = ByteArray(size)
            myUsersJSONFile.read(buffer)
            myUsersJSONFile.close()
            json = String(buffer, charset)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        if (binding.spinnerCategories.selectedItem == "Other") {
            binding.edtTxtOther.isEnabled = true
            binding.edtTxtOther.text!!.clear()
        }else{
            binding.edtTxtOther.isEnabled = false
            binding.edtTxtOther.setText(binding.spinnerCategories.selectedItem.toString())
        }

        if (binding.spinnerCategories.selectedItem == "Choose Business Category") {

            binding.edtTxtOther.text!!.clear()
        }

        if (binding.spinnerCategories.selectedItem.toString() == "Other" && binding.edtTxtOther.text.toString() == "") {
            valid = false
            binding.edtTxtOther?.error = "Enter a valid business category"
        }else
            valid = true
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun getLongLat() {
        val geocoder = Geocoder(context)
        var location: MutableList<Address>? = null

        firebaseUserID = Firebase.auth.currentUser!!.uid

        val longLat = databaseRef.child("User").child(firebaseUserID).child("contact")

        longLat.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val addressObj = snapshot.getValue<AddressData>()
                    val mapAddress =
                        "${addressObj!!.officeNum}, ${addressObj!!.street}, ${addressObj!!.lga}, ${addressObj!!.state},  ${addressObj!!.country}"
                    location = geocoder.getFromLocationName(mapAddress, 1)
                    if (location != null && location!!.size > 0) {
                        val addressList = location!![0]
                        val longitude = addressList.longitude
                        val latitude = addressList.latitude

                        longLat.child("longitude").setValue(longitude)
                        longLat.child("latitude").setValue(latitude)

                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })


    }
}