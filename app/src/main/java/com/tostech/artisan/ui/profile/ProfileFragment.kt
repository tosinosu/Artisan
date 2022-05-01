package com.tostech.artisan.ui.profile

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
//import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
import com.tostech.artisan.Database
import com.tostech.artisan.MainActivity
import com.tostech.artisan.R
import com.tostech.artisan.data.AddressData
import com.tostech.artisan.databinding.FragmentProfileBinding
import com.tostech.artisan.utils.checkSelfPermissionCompat
import com.tostech.artisan.utils.requestPermissionsCompat
import com.tostech.artisan.utils.shouldShowRequestPermissionRationaleCompat
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.Charset


class ProfileFragment : Fragment(),  AdapterView.OnItemSelectedListener {
    var firebaseUserID: String = ""

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private var mUri: Uri? = null
    private val OPERATION_CAPTURE_PHOTO = 1
    private val OPERATION_CHOOSE_PHOTO = 2
    lateinit var mActivity: FragmentActivity

    // Create a storage reference from our app
    var storageRef = Firebase.storage.reference
    var intent: Intent = Intent()


    var databaseRef = Firebase.database.reference
    private var _binding: FragmentProfileBinding? = null
    private lateinit var textInputUsername: TextInputLayout
    private lateinit var textInputFirstname: TextInputLayout
    private lateinit var textInputLastname: TextInputLayout
    private lateinit var textInputPhone: TextInputLayout
    private lateinit var textInputBusName: TextInputLayout
    private lateinit var textInputBusAdvert: TextInputLayout
    private lateinit var textInputOtherBus: TextInputLayout
    private lateinit var textInputAddress: TextInputLayout
    private lateinit var textInputFacebook: TextInputLayout
    private lateinit var textInputTwitter: TextInputLayout
    private lateinit var textInputInstagram: TextInputLayout
    private lateinit var textInputWhatsapp: TextInputLayout
    private lateinit var textInputOtherLink: TextInputLayout

    private lateinit var textEditUsername: TextInputEditText
    private lateinit var textEditFirstname: TextInputEditText
    private lateinit var textEditLastname: TextInputEditText
    private lateinit var textEditPhone: TextInputEditText
    private lateinit var textEditBusName: TextInputEditText
    private lateinit var textEditBusAdvert: TextInputEditText
    private lateinit var textEditOtherBus: TextInputEditText
    private lateinit var textEditAddress: TextInputEditText
    private lateinit var textEditFacebook: TextInputEditText
    private lateinit var textEditTwitter: TextInputEditText
    private lateinit var textEditInstagram: TextInputEditText
    private lateinit var textEditWhatsapp: TextInputEditText
    private lateinit var textEditOtherLink: TextInputEditText
    private lateinit var busType: Spinner
    private lateinit var state: Spinner
    private lateinit var country: Spinner
    private lateinit var lga: Spinner
    val dbSave = Database()
    private lateinit var uid: String
    var profPix:CircleImageView? = null
    private val binding get() = _binding!!




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        setUpToolbar()

        initialize()

        profPix = binding.pixProfile
        uid = FirebaseAuth.getInstance().currentUser!!.uid
        val profilePixUrl = databaseRef.child("User/$uid/advert/purl")
        dbSave.getAdvertPix(profilePixUrl, profPix!!, requireContext())
        textInputUsername.isEnabled = false
        textEditFirstname.isEnabled = false
        textEditLastname.isEnabled = false
         textEditPhone.isEnabled = false
        textEditBusName.isEnabled = false
         textEditBusAdvert.isEnabled = false
         textEditOtherBus.isEnabled = false
         textEditAddress.isEnabled = false
         textEditFacebook.isEnabled = false
        textEditTwitter.isEnabled = false
        textEditInstagram.isEnabled = false
         textEditWhatsapp.isEnabled = false
        textEditOtherLink.isEnabled = false

        profPix!!.setOnClickListener {
            chooseCameraGallery()
        }
        binding.uploadProfilepix.setOnClickListener {
            uploadImage()
        }
        binding.deleteProfilepix.setOnClickListener {
            val builder = AlertDialog.Builder(context)

            builder.setMessage("Select Image")

                .setPositiveButton("Camera", DialogInterface.OnClickListener { dialogInterface, i ->
                    deleteImage("images/profile_pix.jpg")
                    dialogInterface.dismiss()
                })
                .setNegativeButton(
                    "Gallery",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                       dialogInterface.dismiss()
                    })

            builder.create().show()
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
        saveOnebyOne()

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
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
              //  Log.d("Calling Image", "Successful " + snapshot)

                // Success!
                // ...
            }
            task.addOnFailureListener() { it ->
              //  Log.d("Calling Image", it.message.toString())

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
                     showGalleryPreview()

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

            profPix!!.isDrawingCacheEnabled = true
            profPix!!.buildDrawingCache()
            try {
                val bitmap = (profPix!!.drawable as BitmapDrawable).bitmap
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

//                        Log.v("snapshoturl", url)

                        Database().savePixUrl(userid, url)
                    }
                }
                    .addOnFailureListener { ex ->
  //                  Log.d("ImageLog", ex.message.toString())
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
        requestCode: Int, permissions: Array<String>, grantedResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)

        when (requestCode) {
            OPERATION_CHOOSE_PHOTO ->
                if (grantedResults.isNotEmpty() && grantedResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    openGallery()
                } else {
                    show("Unfortunately You are denied permission to perform this operation.")
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


        firebaseUserID = FirebaseAuth.getInstance().uid.toString()

        if (firebaseUserID != null) {
            // Call Advert database
            val userName = databaseRef.child("User").child(firebaseUserID).child("user/username")
            val fName = databaseRef.child("User").child(firebaseUserID).child("user/fname")
            val lName = databaseRef.child("User").child(firebaseUserID).child("user/lname")
            val user = databaseRef.child("User").child(firebaseUserID).child("user/lname")

            //Contact database
            val phone = databaseRef.child("User").child(firebaseUserID).child("contact/phone")
            val address = databaseRef.child("User").child(firebaseUserID).child("contact/office_address")
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

                dbSave.getAdvert(userName, binding.user, requireContext())
                dbSave.getAdvert(fName, binding.fname, requireContext())
                dbSave.getAdvert(lName, binding.lname, requireContext())

                dbSave.getAdvert(phone, binding.phone, requireContext())

                dbSave.getAdvert(address, binding.edtOfficeAddr, requireContext())
                dbSave.getAdvert(bus_name, binding.txtInpBusName, requireContext())
                dbSave.getAdvert(description, binding.txtInpAdvert, requireContext())


                dbSave.getAdvert(whatsapp, binding.edWhatsapp, requireContext())
                dbSave.getAdvert(facebook, binding.edtFacebook, requireContext())
                dbSave.getAdvert(twitter, binding.edTwitter, requireContext())
                dbSave.getAdvert(instagram, binding.edInstagram, requireContext())
                dbSave.getAdvert(other, binding.edtOtherAdd, requireContext())
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

//            Log.v("JSONSIZE", "Json size is ${jsonArray.size()}")

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

                        //Log.v("JSONSIZE", "Json size is ${jsonArray.size()}")

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
                  //  TODO("Not yet implemented")
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


        val validData = validate(fname, lname, phone, other, category,
                office, facebook, twitter, whatsapp, instagram, otherLink, busName,  advert)
        val db = Database()

        if (validData) {

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

    private fun validate(
        fname: String?,
        lname: String?,
        phone: String?,
        other: String?,
        category: String?,
        office: String?,
        facebook: String?,
        twitter: String?,
        whatsapp: String?,
        instagram: String?,
        otherLink: String?,
        busName: String?,
        advert: String?
    ): Boolean {

       var valid = true

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
      /*  if (advert == null || advert.length < 10){
            valid = false
            binding.txtInpAdvert?.error = "A valid advert is required. Advert is too short"
        }*/
        if (advert == null || advert.length < 10){
            valid = false
            binding.txtInpAdvert?.error = "A valid advert is required. Advert is too short"
        }
        else if (advert!!.length >= 100){
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

        if (whatsapp != "" && !(whatsapp!!.contains("+") || !((whatsapp!!.contains("wa.me")) || whatsapp!!.contains("whatsapp.com")))){
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

        return if (!valid){
            Toast.makeText(context, "Error: Please review the form and submit", Toast.LENGTH_SHORT).show()
            false
        }else
            true



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

/*
        if (binding.spinnerCategories.selectedItem.toString() == "Other") {
            if(textEditOtherBus.text.toString() == "") {
                valid = false
                binding.edtTxtOther?.error = "Enter a valid business category"
            }else
                valid = true
        }*/
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
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
                   try{
                    location = geocoder.getFromLocationName(mapAddress, 1)
                    if (location != null && location!!.size > 0) {
                        val addressList = location!![0]
                        val longitude = addressList.longitude
                        val latitude = addressList.latitude

                        longLat.child("longitude").setValue(longitude)
                        longLat.child("latitude").setValue(latitude)

                    }}catch (ex: IOException){
                       show("Unable to get location on map")
                   }catch (ex: RuntimeException){
                       show("Unable to get location on map")
                   }
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun initialize(){
        textInputUsername = binding.textInputLayout4
       textInputFirstname= binding.textInputLayout
       textInputLastname = binding.textInputLayout3
        textInputPhone = binding.textInputLayout2
        textInputBusName = binding.textInputLayout9
        textInputBusAdvert = binding.textInputLayout5
         textInputOtherBus = binding.textInputLayout12
        textInputAddress = binding.textInputLayout6
        textInputFacebook = binding.textInputLayout7
        textInputTwitter = binding.textInputLayout8
       textInputInstagram = binding.textInputLayout10
        textInputWhatsapp = binding.textInputLayout11
       textInputOtherLink = binding.txtInputOther

         textEditUsername = binding.user
         textEditFirstname = binding.fname
        textEditLastname = binding.lname
         textEditPhone = binding.phone
         textEditBusName = binding.txtInpBusName
         textEditBusAdvert = binding.txtInpAdvert
         textEditOtherBus = binding.edtTxtOther
         textEditAddress = binding.edtOfficeAddr
         textEditFacebook = binding.edtFacebook
         textEditTwitter = binding.edTwitter
         textEditInstagram = binding.edInstagram
        textEditWhatsapp = binding.edWhatsapp
         textEditOtherLink = binding.edtOtherAdd
         busType = binding.spinnerCategories
         state = binding.spinnerState
         country = binding.spinnerCountry
         lga = binding.spinnerCities
    }

    private fun saveOnebyOne(){
        val saveFirstName = databaseRef.child("User").child(uid).child("user/fname")
        val saveLastName = databaseRef.child("User").child(uid).child("user/lname")
        val saveBusName = databaseRef.child("User").child(uid).child("advert/bus_name")
        val savePhone = databaseRef.child("User").child(uid).child("contact/phone")

        val saveBusAdvert = databaseRef.child("User").child(uid).child("advert/description")
        val saveOtherCategory = databaseRef.child("User").child(uid).child("advert/other")
        val saveOfficeAdd = databaseRef.child("User").child(uid).child("contact/office_address")
        val saveFacebook = databaseRef.child("User").child(uid).child("contact/facebook")
        val saveTwitter = databaseRef.child("User").child(uid).child("contact/twitter")
        val saveWhatsApp = databaseRef.child("User").child(uid).child("contact/whatsapp")
        val saveInstagram = databaseRef.child("User").child(uid).child("contact/instagram")
        val saveOtherLink = databaseRef.child("User").child(uid).child("contact/other")

        val category: String? = binding.spinnerCategories.selectedItem.toString()

        textInputFirstname.setEndIconOnClickListener {
            textEditFirstname.isEnabled = true
            textInputFirstname.endIconMode = TextInputLayout.END_ICON_CUSTOM
            textInputFirstname.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_save_24, null)
            textInputFirstname.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
            textInputFirstname.endIconContentDescription = "save"

            textInputFirstname.setEndIconOnClickListener{
                if (textEditFirstname.text.isNullOrEmpty() || textEditFirstname.text.toString().length < 3) {
                    textInputFirstname.endIconMode = TextInputLayout.END_ICON_NONE
                    textEditFirstname.error = "A valid first name is required"

                }else {
                    saveFirstName.setValue(textEditFirstname.text.toString()).addOnSuccessListener {
                        show("First name saved")
                        textEditFirstname.isEnabled = false
                    }

                }
            }
        }

        textInputLastname.setEndIconOnClickListener {
            textEditLastname.isEnabled = true
            textInputLastname.endIconMode = TextInputLayout.END_ICON_CUSTOM
            textInputLastname.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_save_24, null)
            textInputLastname.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
            textInputLastname.endIconContentDescription = "save"

            textInputLastname.setEndIconOnClickListener{
                if (textEditLastname.text.isNullOrEmpty() || textEditLastname.text.toString().length < 3) {
                    textEditLastname.error = "A valid last name is required"
                }else {
                    saveLastName.setValue(textEditLastname.text.toString()).addOnSuccessListener {
                        show("Last name Saved")
                        textEditLastname.isEnabled = false

                        textInputLastname.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_edit_24, null)
                        textInputLastname.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
                        textInputLastname.endIconContentDescription = "edit"

                    }

                }
            }
        }

        textInputPhone.setEndIconOnClickListener {
            textEditPhone.isEnabled = true
            textInputPhone.endIconMode = TextInputLayout.END_ICON_CUSTOM
            textInputPhone.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_save_24, null)
            textInputPhone.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
            textInputPhone.endIconContentDescription = "save"

            textInputPhone.setEndIconOnClickListener{
                if (textEditPhone.text.toString().length < 5) {
                    textEditPhone?.error = "A valid phone number is required"
                }else {
                    savePhone.setValue(textEditPhone.text.toString()).addOnSuccessListener {
                        show("Phone Number Saved")
                        textEditPhone.isEnabled = false

                        //textInputPhone.endIconMode = TextInputLayout.END_ICON_CUSTOM
                        textInputPhone.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_edit_24, null)
                        textInputPhone.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
                        textInputPhone.endIconContentDescription = "edit"
                    }

                }
            }
        }

        textInputBusName.setEndIconOnClickListener {
            textEditBusName.isEnabled = true
            textInputBusName.endIconMode = TextInputLayout.END_ICON_CUSTOM
            textInputBusName.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_save_24, null)
            textInputBusName.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
            textInputBusName.endIconContentDescription = "save"

            textInputBusName.setEndIconOnClickListener{
                if (textEditBusName.text.isNullOrEmpty() || textEditBusName.text.toString().length < 5) {
                    textEditBusName?.error = "A valid business name is required"
                }else {
                    saveBusName.setValue(textEditBusName.text.toString()).addOnSuccessListener {
                        show("Business name Saved")
                        textEditBusName.isEnabled = false

                        textInputBusName.endIconMode = TextInputLayout.END_ICON_CUSTOM
                        textInputBusName.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_edit_24, null)
                        textInputBusName.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
                        textInputBusName.endIconContentDescription = "edit"
                    }

                }
            }
        }
        textInputAddress.setEndIconOnClickListener {
            textEditAddress.isEnabled = true
            textInputAddress.endIconMode = TextInputLayout.END_ICON_CUSTOM
            textInputAddress.endIconDrawable = ResourcesCompat.getDrawable(
                requireContext().resources,
                R.drawable.ic_baseline_save_24,
                null
            )
            textInputAddress.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
            textInputAddress.endIconContentDescription = "save"

            textInputAddress.setEndIconOnClickListener {
                if (textEditAddress.text.isNullOrEmpty() || textEditAddress.text.toString().length < 5) {
                    textEditAddress?.error = "A valid business name is required"
                } else {
                    saveBusName.setValue(textEditAddress.text.toString()).addOnSuccessListener {
                        show("Office address Saved")
                        textEditAddress.isEnabled = false

                        textInputAddress.endIconMode = TextInputLayout.END_ICON_CUSTOM
                        textInputAddress.endIconDrawable = ResourcesCompat.getDrawable(
                            requireContext().resources,
                            R.drawable.ic_baseline_edit_24,
                            null
                        )
                        textInputAddress.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
                        textInputAddress.endIconContentDescription = "edit"
                    }

                }
            }
        }

        textInputBusAdvert.setEndIconOnClickListener {
            textEditBusAdvert.isEnabled = true
            textInputBusAdvert.endIconMode = TextInputLayout.END_ICON_CUSTOM
            textInputBusAdvert.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_save_24, null)
            textInputBusAdvert.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
            textInputBusAdvert.endIconContentDescription = "save"

            textInputBusAdvert.setEndIconOnClickListener{
                if (textEditBusAdvert.text.toString().length > 100) {
                    textEditBusAdvert.error = "Maximum of 100 characters exceeded"
                }else {
                    saveBusAdvert.setValue(textEditBusAdvert.text.toString()).addOnSuccessListener {
                        show("Data Saved")
                        textEditBusAdvert.isEnabled = false
                    }

                }
            }
        }

        if (category != "Other")
            textInputOtherBus.endIconMode = TextInputLayout.END_ICON_NONE

        if (category == "Other") {
            textInputOtherBus.endIconMode = TextInputLayout.END_ICON_NONE

                textInputOtherBus.setEndIconOnClickListener {
                    if (textEditOtherBus.text.isNullOrEmpty() || textEditOtherBus.text.toString().length < 5) {
                        textInputOtherBus.endIconMode = TextInputLayout.END_ICON_NONE
                        textEditOtherBus?.error = "Choose business category"
                    } else {
                        saveOtherCategory.setValue(textEditOtherBus.text.toString())
                            .addOnSuccessListener {
                                show("Business category Saved")
                                textEditOtherBus.isEnabled = false

                                textInputOtherBus.endIconMode = TextInputLayout.END_ICON_CUSTOM
                                textInputOtherBus.endIconDrawable = ResourcesCompat.getDrawable(
                                    requireContext().resources,
                                    R.drawable.ic_baseline_edit_24,
                                    null
                                )
                                textInputOtherBus.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
                                textInputOtherBus.endIconContentDescription = "edit"

                            }

                    }
                }
        //    }
        }
        textInputFacebook.setEndIconOnClickListener {
            textEditFacebook.isEnabled = true
            textInputFacebook.endIconMode = TextInputLayout.END_ICON_CUSTOM
            textInputFacebook.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_save_24, null)
            textInputFacebook.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
            textInputFacebook.endIconContentDescription = "save"


            textInputFacebook.setEndIconOnClickListener{
                val facebook = textEditFacebook.text.toString()
                if (facebook != "" && !(!facebook.contains("facebook") || facebook.length < 2)) {
                    textEditFacebook.error = "A valid facebook id is required"
                }else {
                    saveFacebook.setValue(textEditFacebook.text.toString()).addOnSuccessListener {
                        show("Facebook link Saved")
                        textEditFacebook.isEnabled = false

                        textInputFacebook.endIconMode = TextInputLayout.END_ICON_CUSTOM
                        textInputFacebook.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_edit_24, null)
                        textInputFacebook.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
                        textInputFacebook.endIconContentDescription = "edit"

                    }

                }
            }
        }

        textInputTwitter.setEndIconOnClickListener {
            textEditTwitter.isEnabled = true
            textInputTwitter.endIconMode = TextInputLayout.END_ICON_CUSTOM
            textInputTwitter.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_save_24, null)
            textInputTwitter.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
            textInputTwitter.endIconContentDescription = "save"


            textInputTwitter.setEndIconOnClickListener{
                val twitter = textEditTwitter.text.toString()
                if (twitter != "" && !(!twitter!!.contains("twitter") || twitter!!.length < 2)) {
                    textEditTwitter.error = "A valid twitter id is required"
                }else {
                    saveTwitter.setValue(twitter).addOnSuccessListener {
                        show("twitter id saved")
                        textEditTwitter.isEnabled = false

                        textInputTwitter.endIconMode = TextInputLayout.END_ICON_CUSTOM
                        textInputTwitter.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_edit_24, null)
                        textInputTwitter.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
                        textInputTwitter.endIconContentDescription = "edit"

                    }

                }
            }
        }

        textInputWhatsapp.setEndIconOnClickListener {
            textEditWhatsapp.isEnabled = true
            textInputWhatsapp.endIconMode = TextInputLayout.END_ICON_CUSTOM
            textInputWhatsapp.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_save_24, null)
            textInputWhatsapp.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
            textInputWhatsapp.endIconContentDescription = "save"

            textInputWhatsapp.setEndIconOnClickListener{
                val whatsapp = textEditWhatsapp.text.toString()
                if (whatsapp != "" && !(whatsapp!!.contains("+") || !((whatsapp!!.contains("wa.me")) || whatsapp!!.contains("whatsapp.com")))){
                    textEditWhatsapp.error = "A valid whatsapp url is required"
                }else {
                    saveWhatsApp.setValue(whatsapp).addOnSuccessListener {
                        show("WhatsApp link Saved")
                        textEditFirstname.isEnabled = false

                        textInputWhatsapp.endIconMode = TextInputLayout.END_ICON_CUSTOM
                        textInputWhatsapp.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_edit_24, null)
                        textInputWhatsapp.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
                        textInputWhatsapp.endIconContentDescription = "edit"
                    }

                }
            }
        }

        textInputInstagram.setEndIconOnClickListener {
            textEditInstagram.isEnabled = true
            textInputInstagram.endIconMode = TextInputLayout.END_ICON_CUSTOM
            textInputInstagram.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_save_24, null)
            textInputInstagram.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
            textInputInstagram.endIconContentDescription = "save"

            textInputInstagram.setEndIconOnClickListener{
                val instagram = textEditInstagram.text.toString()
                if (instagram != "" && !(instagram!!.contains("instagram") || instagram!!.length < 2)) {
                    textEditInstagram.error = "A valid instagram url required"
                }else {
                    saveInstagram.setValue(instagram).addOnSuccessListener {
                        show("Instagram data Saved")
                        textEditInstagram.isEnabled = false
                        textInputInstagram.endIconMode = TextInputLayout.END_ICON_CUSTOM
                        textInputInstagram.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_edit_24, null)
                        textInputInstagram.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
                        textInputInstagram.endIconContentDescription = "edit"
                    }

                }
            }
        }
        textInputOtherLink.setEndIconOnClickListener {
            textEditOtherLink.isEnabled = true
            textInputOtherLink.endIconMode = TextInputLayout.END_ICON_CUSTOM
            textInputOtherLink.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_save_24, null)
            textInputOtherLink.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
            textInputOtherLink.endIconContentDescription = "save"

            textInputOtherLink.setEndIconOnClickListener{
                val otherLink = textEditOtherLink.text.toString()
                if (otherLink != "" && !(otherLink!!.contains("."))) {
                    textEditOtherLink.error = "A valid first name is required"
                }else {
                    saveOtherLink.setValue(otherLink).addOnSuccessListener {
                        show("Link Saved")
                        textEditOtherLink.isEnabled = false
                        textInputOtherLink.endIconMode = TextInputLayout.END_ICON_CUSTOM
                        textInputOtherLink.endIconDrawable = ResourcesCompat.getDrawable(requireContext().resources, R.drawable.ic_baseline_edit_24, null)
                        textInputOtherLink.setEndIconTintMode(PorterDuff.Mode.SRC_IN)
                        textInputOtherLink.endIconContentDescription = "edit"
                    }

                }
            }
        }
    }


    private fun showGalleryPreview() {
        // Check if the Camera permission has been granted
        if (checkSelfPermissionCompat(Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
            // layout.showSnackbar(R.string.camera_permission_available, Snackbar.LENGTH_SHORT)
            openGallery()
        } else {
            // Permission is missing and must be requested.
            requestGalleryPermission()
        }
    }

    private fun requestGalleryPermission() {
        // Permission has not been granted and must be requested.
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            //  binding.homeLayout.showSnackbar(R.string.app_name,
            //    Snackbar.LENGTH_INDEFINITE, R.string.ok)
            //{
            requestPermissionsCompat(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                OPERATION_CHOOSE_PHOTO
            )
            // }

        } else {
            // binding.homeLayout.showSnackbar("R.string.camera_permission_not_available", Snackbar.LENGTH_SHORT)
            show("Storage permission not available")

            // Request the permission. The result will be received in onRequestPermissionResult().
            requestPermissionsCompat(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), OPERATION_CHOOSE_PHOTO)
        }
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

}
