package com.tostech.artisan.ui.profile

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
//import android.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.tostech.artisan.*
import com.tostech.artisan.databinding.FragmentProfileBinding
import com.tostech.artisan.databinding.ProfileBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.google.gson.JsonParser
import com.tostech.artisan.data.AddressData
import kotlinx.android.synthetic.main.profile.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.json.JSONException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.lang.RuntimeException
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask



class ProfileFragment : Fragment(),  AdapterView.OnItemSelectedListener {
    var firebaseUserID: String = ""

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private var mUri: Uri? = null
    private val OPERATION_CAPTURE_PHOTO = 1
    private val OPERATION_CHOOSE_PHOTO = 2


    // Create a storage reference from our app
    var storageRef = Firebase.storage.reference
    var intent: Intent = Intent()

  //  private const val NUM_PAGES = 3
    //var selected = false
    var valid = true
    var databaseRef = Firebase.database.reference
    private lateinit var slideshowViewModel: ProfileViewModel
    private lateinit var binding: FragmentProfileBinding



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        slideshowViewModel =
            ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        slideshowViewModel.text.observe(viewLifecycleOwner, Observer {

        })

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        val profPix = binding.pixProfile
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val profilePixUrl = Firebase.database.reference.child("User/$uid/advert/purl")



        GetData().getAdvertPix(profilePixUrl, profPix, requireContext())

        binding.pixProfile.setOnClickListener {
            chooseCameraGallery()

        }
        binding.uploadProfilepix.setOnClickListener {
            uploadImage()
        }
        binding.deleteProfilepix.setOnClickListener {
            deleteImage("images/profile_pix.jpg")
        }

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

    private fun deleteImage(imagePath: String) {
        val userID = Database().readUserID()

        if (userID != null) {
            val deleteRef = storageRef.child("$userID/$imagePath")


            deleteRef.delete().addOnSuccessListener {
                show("Profile Picture Deleted")
            }.addOnFailureListener {
                show("Image cannot be deleted now" + it.message)
            }
        } else
            show("Please Register or Sign In")


    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // If there's a download in progress, save the reference so you can query it later
        outState.putString("reference", storageRef.toString())
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)

        // If there was a download in progress, get its reference and create a new StorageReference
        val stringRef = savedInstanceState?.getString("reference") ?: return

        storageRef = Firebase.storage.getReferenceFromUrl(stringRef)

        // Find all DownloadTasks under this StorageReference (in this example, there should be one)
        val tasks = storageRef.activeDownloadTasks

        if (tasks.size > 0) {
            // Get the task monitoring the download
            val task = tasks[0]

            // Add new listeners to the task using an Activity scope
            task.addOnSuccessListener() { snapshot ->
                Log.d("Calling Image", "Successful " + snapshot)

                // Success!
                // ...
            }
            task.addOnFailureListener() { it ->
                Log.d("Calling Image", it.message.toString())

            }
        }
    }

    fun chooseCameraGallery() {

        val builder = AlertDialog.Builder(context)

        builder.setMessage("Select Image")

            .setPositiveButton("Camera", DialogInterface.OnClickListener { dialogInterface, i ->
                capturePhoto()

            })
            .setNegativeButton(
                "Gallery",
                DialogInterface.OnClickListener { dialogInterface, i ->
                    val checkSelfPermission = ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                        Log.v("PErmission", "Inside permission")
                        //Requests permissions to be granted to this application at runtime
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
                        )
                    } else {
                        Log.v("PErmission", "Inside open gallery")
                        openGallery()

                    }
                })

        builder.create().show()
    }

    private fun uploadImage() {
        val userid = Firebase.auth.currentUser?.uid

        val userID = storageRef.child(userid!!)
        // Create a child reference
        // imagesRef now points to "images"
        var imagesRef: StorageReference? = storageRef.child("images")

        if (userID != null) {
            // Child references can also take paths
            // spaceRef now points to "images/space.jpg
            // imagesRef still points to "images"

            val spaceRef = userID.child("images/profile_pix.jpg")

                 //   val uploadTask = spaceRef.putFile(file)

            binding.pixProfile.isDrawingCacheEnabled = true
            binding.pixProfile.buildDrawingCache()
            try {

                val bitmap = (binding.pixProfile.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()



                val uploadTask = spaceRef.putBytes(data)



                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->

                    if (!task.isSuccessful){
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation spaceRef.downloadUrl

                }).addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        val downloadurl = task.result
                        val url = downloadurl.toString()

                        Log.v("snapshoturl", url)

                        Database().savePixUrl(userid, url)
                    }
                }
                    .addOnFailureListener { ex ->
                    Log.d("ImageLog", ex.message.toString())
                }.addOnSuccessListener { taskSnapshot ->
                    // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                   // Log.d("ImageLog", taskSnapshot.bytesTransferred.toString() + " uploaded")

                    show("Profile Picture Uploaded Successfully")

                }.addOnFailureListener { failure ->
                    show(failure.message!!)
                }
            } catch (e: ClassCastException) {
                show("Select an Image before uploading")
            }
        } else
            show("You're not a registered user")


    }

    private fun show(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun capturePhoto() {
        val capturedImage = File(context?.externalCacheDir, "My_Captured_Photo.jpg")
        if (capturedImage.exists()) {
            capturedImage.delete()
        }
        capturedImage.createNewFile()
        mUri = if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(
                requireContext(), "com.tostech.artisan.fileprovider",
                capturedImage
            )
        } else {
            Uri.fromFile(capturedImage)
        }

        intent = Intent("android.media.action.IMAGE_CAPTURE")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
        startActivityForResult(intent, OPERATION_CAPTURE_PHOTO)
    }

    private fun openGallery() {
        intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"

        startActivityForResult(intent, OPERATION_CHOOSE_PHOTO)
    }

    private fun renderImage(imagePath: String?) {

        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100)
            Glide.with(requireContext()).load(bitmap).into(binding.pixProfile)

        } else {
            show("ImagePath is null")
        }
    }

    private fun getImagePath(uri: Uri?, selection: String?): String {
        var path: String? = null
        val cursor = uri?.let { context?.contentResolver?.query(it, null, selection, null, null) }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }


    fun downloadImagetoApp(imageView: String) {
        val ONEHUNDRED_KILOBYTE: Long = 1024 * 10

        //  val storageReference = Firebase.storage.reference

        val profile = storageRef.child("images/profile.jpg")


        Glide.with(requireContext()).load(profile).into(binding.pixProfile)
        uploadImage()

    }

    @TargetApi(19)
    private fun handleImageOnKitkat(data: Intent?) {
        var imagePath: String? = null

        val uri = data!!.data
        //DocumentsContract defines the contract between a documents provider and the platform.
        if (DocumentsContract.isDocumentUri(requireContext(), uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            if (uri != null) {
                if ("com.android.providers.media.documents" == uri.authority) {
                    val id = docId.split(":")[1]
                    val selection = MediaStore.Images.Media._ID + "=" + id
                    imagePath = getImagePath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        selection
                    )
                } else if ("com.android.providers.downloads.documents" == uri!!.authority) {
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse(
                            "content://downloads/public_downloads"
                        ), java.lang.Long.valueOf(docId)
                    )
                    imagePath = getImagePath(contentUri, null)
                }
            }
        } else if (uri != null) {
            if ("content".equals(uri.scheme, ignoreCase = true)) {
                imagePath = getImagePath(uri, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                imagePath = uri.path
            }
        }


        renderImage(imagePath)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantedResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)


        when (requestCode) {
            1 ->
                if (grantedResults.isNotEmpty() && grantedResults.get(0) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    openGallery()
                } else {
                    show("Unfortunately You are Denied Permission to Perform this Operataion.")
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            OPERATION_CAPTURE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {

                    val bitmap = BitmapFactory.decodeStream(
                        mUri?.let { context?.getContentResolver()!!.openInputStream(it) })

                    Glide.with(requireContext()).load(bitmap)
                        .apply(RequestOptions().override(200, 200)).into(binding.pixProfile)

                }
            OPERATION_CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitkat(data)

                    }
                }
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
                    Thread.yield()
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

        longLat.addListenerForSingleValueEvent(object : ValueEventListener {
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
