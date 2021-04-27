package com.tostech.artisan

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.ContentLoadingProgressBar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.tostech.artisan.databinding.LogoBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception

class Pictures: Fragment() {

    private lateinit var binding: LogoBinding

    private var mUri: Uri? = null
    private val OPERATION_CAPTURE_PHOTO = 1
    private val OPERATION_CHOOSE_PHOTO = 2
    private var uid: String? = null




    // Create a storage reference from our app
    var storageRef = Firebase.storage.reference
    var advertRef = Firebase.database.reference

    var intent: Intent = Intent()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.logo, container, false)
         uid = Firebase.auth.currentUser?.uid
        val db = Database()
        loadAdvetImages()

        binding.imageView1.setOnClickListener {
            chooseCameraGallery("imageView1")
    }

        binding.imageView2.setOnClickListener {
            chooseCameraGallery("imageView2")
    }

        binding.imageView3.setOnClickListener {
            chooseCameraGallery("imageView3")
    }

        binding.imageView4.setOnClickListener {
            chooseCameraGallery("imageView4")
    }

        binding.imageView5.setOnClickListener {
            chooseCameraGallery("imageView5")
        }

        binding.imageView6.setOnClickListener {
            chooseCameraGallery("imageView6")
    }

              binding.button1.setOnClickListener {
                  uploadImage("logo", binding.imageView1)

        }
                binding.button2.setOnClickListener {
                    uploadImage("advert1", binding.imageView2)

        }
                binding.button3.setOnClickListener {
                    uploadImage("advert2", binding.imageView3)

        }
                binding.button4.setOnClickListener {
                    uploadImage("advert3", binding.imageView4)

        }
                binding.button5.setOnClickListener {
                    uploadImage("advert4", binding.imageView5)

        }
                binding.button6.setOnClickListener {
                    uploadImage("advert5", binding.imageView6)
                }
            binding.deleteLogo.setOnClickListener {
                deleteImage("image/logo")
            }
            binding.delete1.setOnClickListener {
                        deleteImage("image/advert1")
                    }
            binding.delete2.setOnClickListener {
                        deleteImage("image/advert2")
                    }
            binding.delete3.setOnClickListener {
                        deleteImage("image/advert3")
                    }
            binding.delete4.setOnClickListener {
                        deleteImage("image/advert4")
                    }
            binding.delete5.setOnClickListener {
                        deleteImage("image/advert5")
                    }

        return binding.root
    }

    fun loadAdvetImages(){

         storageRef.child("$uid/images/logo.jpg").downloadUrl.addOnSuccessListener (object : OnSuccessListener<Uri>{
             override fun onSuccess(uri: Uri?) {
                 context?.let { Glide.with(it).load(uri.toString()).apply(RequestOptions().placeholder(R.drawable.ic_baseline_person_24)).into(binding.imageView1) }

             }

         }).addOnFailureListener(object : OnFailureListener {
             override fun onFailure(ex: Exception) {
                 Toast.makeText(context, "An error has occured", Toast.LENGTH_SHORT).show()
             }

         })
         storageRef.child("$uid/images/advert1.jpg").downloadUrl.addOnSuccessListener (object : OnSuccessListener<Uri>{
             override fun onSuccess(uri: Uri?) {
                 context?.let { Glide.with(it).load(uri.toString()).apply(RequestOptions().placeholder(R.drawable.ic_baseline_person_24)).into(binding.imageView2) }

             }

         }).addOnFailureListener(object : OnFailureListener {
             override fun onFailure(ex: Exception) {
                 Toast.makeText(context, "An error has occured", Toast.LENGTH_SHORT).show()
             }

         })
         storageRef.child("$uid/images/advert2.jpg").downloadUrl.addOnSuccessListener (object : OnSuccessListener<Uri>{
             override fun onSuccess(uri: Uri?) {
                 context?.let { Glide.with(it).load(uri.toString()).apply(RequestOptions().placeholder(R.drawable.ic_baseline_person_24)).into(binding.imageView3) }

             }

         }).addOnFailureListener(object : OnFailureListener {
             override fun onFailure(ex: Exception) {
                 Toast.makeText(context, "An error has occured", Toast.LENGTH_SHORT).show()
             }

         })
         storageRef.child("$uid/images/advert3.jpg").downloadUrl.addOnSuccessListener (object : OnSuccessListener<Uri>{
             override fun onSuccess(uri: Uri?) {
                 context?.let { Glide.with(it).load(uri.toString()).apply(RequestOptions().placeholder(R.drawable.ic_baseline_person_24)).into(binding.imageView4) }

             }

         }).addOnFailureListener(object : OnFailureListener {
             override fun onFailure(ex: Exception) {
                 Toast.makeText(context, "An error has occured", Toast.LENGTH_SHORT).show()
             }

         })
         storageRef.child("$uid/images/advert4.jpg").downloadUrl.addOnSuccessListener (object : OnSuccessListener<Uri>{
             override fun onSuccess(uri: Uri?) {
                 context?.let { Glide.with(it).load(uri.toString()).apply(RequestOptions().placeholder(R.drawable.ic_baseline_person_24)).into(binding.imageView5) }

             }

         }).addOnFailureListener(object : OnFailureListener {
             override fun onFailure(ex: Exception) {
                 Toast.makeText(context, "An error has occured", Toast.LENGTH_SHORT).show()
             }

         })
         storageRef.child("$uid/images/advert5.jpg").downloadUrl.addOnSuccessListener (object : OnSuccessListener<Uri>{
             override fun onSuccess(uri: Uri?) {
                 context?.let { Glide.with(it).load(uri.toString()).apply(RequestOptions().placeholder(R.drawable.ic_baseline_person_24)).into(binding.imageView6) }

             }

         }).addOnFailureListener(object : OnFailureListener {
             override fun onFailure(ex: Exception) {
                 Toast.makeText(context, "An error has occured", Toast.LENGTH_SHORT).show()
             }

         })


    }


    fun chooseCameraGallery(imageNumber: String){

        val builder = AlertDialog.Builder(context)

        builder.setMessage("Select Image")

            .setPositiveButton("Camera", DialogInterface.OnClickListener { dialogInterface, i ->
                capturePhoto(imageNumber)
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
                        openGallery(imageNumber)
                    }
                })

        builder.create().show()
    }
    private fun uploadImage(buttonNum:String?, imageView: ImageView){
       // val storage = Firebase.storage

        val db = Database()
        val userID = storageRef.child(uid!!)
        // Create a child reference
        // imagesRef now points to "images"
        var imagesRef: StorageReference? = storageRef.child("images")

        if (userID != null) {
            // Child references can also take paths
            // spaceRef now points to "images/space.jpg
            // imagesRef still points to "images"
            var spaceRef = userID.child("images/${buttonNum}.jpg")

            imageView.isDrawingCacheEnabled = true
            imageView.buildDrawingCache()
            try {


                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                var uploadTask = spaceRef.putBytes(data)
                uploadTask.addOnFailureListener { ex ->
                    Log.d("ImageLog", ex.message.toString())
                }.addOnSuccessListener { taskSnapshot ->
                    // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                    Log.d("ImageLog", taskSnapshot.bytesTransferred.toString() + " uploaded")


                    if (buttonNum == "logo") {
                        binding.textView5.setText("Logo uploaded successfully")
                        binding.textView5.setTextColor(Color.GREEN)
                    }
                    if (buttonNum == "advert1") {
                        db.writeAdvertText("advert1", uid!!, binding.editTextTextMultiLine.text.toString())
                        binding.textView7.setText("Image uploaded successfully")
                        binding.textView7.setTextColor(Color.GREEN)
                    }
                    if (buttonNum == "advert2") {
                        db.writeAdvertText("advert2", uid!!, binding.editTextTextMultiLine2.text.toString())
                        binding.textView8.setText("Image uploaded successfully")
                        binding.textView8.setTextColor(Color.GREEN)
                    }
                    if (buttonNum == "advert3") {
                        db.writeAdvertText("advert3", uid!!, binding.editTextTextMultiLine3.text.toString())
                        binding.textView9.setText("Image uploaded successfully")
                        binding.textView9.setTextColor(Color.GREEN)
                    }
                    if (buttonNum == "advert4") {
                        db.writeAdvertText("advert4", uid!!, binding.editTextTextMultiLine4.text.toString())
                        binding.textView10.setText("Image uploaded successfully")
                        binding.textView10.setTextColor(Color.GREEN)
                    }
                    if (buttonNum == "advert5") {
                        db.writeAdvertText("advert5", uid!!, binding.editTextTextMultiLine5.text.toString())
                        binding.textView11.setText("Image uploaded successfully")
                        binding.textView11.setTextColor(Color.GREEN)
                    }
                }/*.addOnProgressListener { snapshot ->
                val progress = ((100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount)
                progressBar.setProgress(progress.toInt())

            }*/.addOnFailureListener { failure ->
                    show(failure.message!!)
                }
            } catch (e: ClassCastException) {
                show("Select an Image before uploading")
            }
        }
            else{
            show("You're not a registered user")
        }
    }

    private fun deleteImage(imagePath: String){
        val userID = Database().readUserID()
        val deleteRef = storageRef.child("$userID/$imagePath")
        val advertData = advertRef.child("User").child("advert")


        if(userID != null) {

            deleteRef.delete().addOnSuccessListener {
                if (imagePath == "logo") {
                    binding.textView5.setText(R.string.business_logo)
                }
                if (imagePath == "advert1") {
                    advertData.child("advert1").removeValue()
                    binding.textView7.setText(R.string.business_advert_1)
                }
                if (imagePath == "advert2") {
                    advertData.child("advert2").removeValue()
                    binding.textView8.setText(R.string.business_advert_2)
                }
                if (imagePath == "advert3") {
                    advertData.child("advert3").removeValue()
                    binding.textView9.setText(R.string.business_advert_3)
                }
                if (imagePath == "advert4") {
                    advertData.child("advert4").removeValue()
                    binding.textView10.setText(R.string.business_advert_4)
                }
                if (imagePath == "advert5") {
                    advertData.child("advert5").removeValue()
                    binding.textView11.setText(R.string.business_advert_5)
                }
            }.addOnFailureListener { failure ->
                show(failure.message!!)

            }.addOnFailureListener {
                show("Image cannot be deleted now" + it.message)
            }
        }else
            show("Please Register or Sign In")
    }

    private fun show(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun capturePhoto(imageView: String) {
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
        intent.putExtra("imageView", imageView)
        startActivityForResult(intent, OPERATION_CAPTURE_PHOTO)
    }

    private fun openGallery(imageView: String) {
        intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        intent.putExtra("imageView", imageView)
        Log.v("getintent", intent.getStringExtra("imageView")!!)

        startActivityForResult(intent, OPERATION_CHOOSE_PHOTO)
    }

    private fun renderImage(imagePath: String?, imageNumber: String) {
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
        //    bitmap.compress(Bitmap.CompressFormat.JPEG, 100)
            if (imageNumber == "imageView1")
                Glide.with(requireContext()).load(bitmap).into(binding.imageView1)
            //    binding.imageView1.setImageBitmap(bitmap)
            if (imageNumber == "imageView2")
                Glide.with(requireContext()).load(bitmap).into(binding.imageView2)

            //  binding.imageView2.setImageBitmap(bitmap)
            if (imageNumber == "imageView3")
                Glide.with(requireContext()).load(bitmap).into(binding.imageView3)

          //  binding.imageView3.setImageBitmap(bitmap)
            if (imageNumber == "imageView4")
                Glide.with(requireContext()).load(bitmap).into(binding.imageView4)

           // binding.imageView4.setImageBitmap(bitmap)
            if (imageNumber == "imageView5")
                Glide.with(requireContext()).load(bitmap).into(binding.imageView5)

            //  binding.imageView5.setImageBitmap(bitmap)
            if (imageNumber == "imageView6")
                Glide.with(requireContext()).load(bitmap).into(binding.imageView1)

//            binding.imageView6.setImageBitmap(bitmap)
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


    fun downloadImagetoApp(imageView: String){
        val ONEHUNDRED_KILOBYTE: Long = 1024 * 10

      //  val storageReference = Firebase.storage.reference
        val logo = storageRef.child("images/logo.jpg").getBytes(ONEHUNDRED_KILOBYTE)
        //val profile = storageRef.child("images/profile.jpg")
        val advert1 = storageRef.child("images/advert1.jpg")
        val advert2 = storageRef.child("images/advert2.jpg")
        val advert3 = storageRef.child("images/advert3.jpg")
        val advert4 = storageRef.child("images/advert4.jpg")
        val advert5 = storageRef.child("images/advert5.jpg")



        Glide.with(requireContext()).load(logo).into(binding.imageView1)
       // Glide.with(requireContext()).load(profile).into(bindingProfile.pixProfile)
        Glide.with(requireContext()).load(advert1).into(binding.imageView2)
        Glide.with(requireContext()).load(advert2).into(binding.imageView3)
        Glide.with(requireContext()).load(advert3).into(binding.imageView4)
        Glide.with(requireContext()).load(advert4).into(binding.imageView5)
        Glide.with(requireContext()).load(advert5).into(binding.imageView6)

    }

    @TargetApi(19)
    private fun handleImageOnKitkat(data: Intent?, imageView: String) {
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
        if (imageView == "imageView1")
            renderImage(imagePath, "imageView1")
        if (imageView == "imageView2")
            renderImage(imagePath, "imageView2")
        if (imageView == "imageView3")
            renderImage(imagePath, "imageView3")
        if (imageView == "imageView4")
            renderImage(imagePath, "imageView4")
        if (imageView == "imageView5")
            renderImage(imagePath, "imageView5")
        if (imageView == "imageView6")
            renderImage(imagePath, "imageView6")


    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantedResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)

        val value = intent.getStringExtra("imageView")
        Log.v("getintentonrequest", value!!)

        when (requestCode) {
            1 ->
                if (grantedResults.isNotEmpty() && grantedResults.get(0) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    openGallery(value!!)
                } else {
                    show("Unfortunately You are Denied Permission to Perform this Operataion.")
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        val imagetype = intent.getStringExtra("imageView")

        Log.v("getIntentOnActivity", imagetype!!)

        if (imagetype == "imageView1") {
            when (requestCode) {
                OPERATION_CAPTURE_PHOTO ->
                    if (resultCode == Activity.RESULT_OK) {

                        val bitmap = BitmapFactory.decodeStream(
                            mUri?.let { context?.getContentResolver()!!.openInputStream(it) })

                        Glide.with(requireContext()).load(bitmap).apply(RequestOptions().override(200, 200)).into(binding.imageView1)
                     //   binding.imageView1.setImageBitmap(bitmap)
                    }
                OPERATION_CHOOSE_PHOTO ->
                    if (resultCode == Activity.RESULT_OK) {
                        if (Build.VERSION.SDK_INT >= 19) {
                            handleImageOnKitkat(data, "imageView1")
                        }
                    }
            }
        } else if (imagetype == "imageView2") {

            when (requestCode) {
                OPERATION_CAPTURE_PHOTO ->
                    if (resultCode == Activity.RESULT_OK) {
                        val bitmap = BitmapFactory.decodeStream(
                            mUri?.let { context?.getContentResolver()!!.openInputStream(it) })
                        Glide.with(requireContext()).load(bitmap).apply(RequestOptions().override(200, 200)).into(binding.imageView2)
                        //binding.imageView2.setImageBitmap(bitmap)
                    }
                OPERATION_CHOOSE_PHOTO ->
                    if (resultCode == Activity.RESULT_OK) {
                        if (Build.VERSION.SDK_INT >= 19) {
                            handleImageOnKitkat(data, "imageView2")
                        }
                    }
            }
        } else if (imagetype == "imageView3") {

            when (requestCode) {
                OPERATION_CAPTURE_PHOTO ->
                    if (resultCode == Activity.RESULT_OK) {
                        val bitmap = BitmapFactory.decodeStream(
                            mUri?.let { activity?.getContentResolver()!!.openInputStream(it) })
                        Glide.with(requireContext()).load(bitmap).apply(RequestOptions().override(200, 200)).into(binding.imageView3)
                        //binding.imageView3.setImageBitmap(bitmap)
                    }
                OPERATION_CHOOSE_PHOTO ->
                    if (resultCode == Activity.RESULT_OK) {
                        if (Build.VERSION.SDK_INT >= 19) {
                            handleImageOnKitkat(data, "imageView3")
                        }
                    }
            }
        } else if (imagetype == "imageView4") {

            when (requestCode) {
                OPERATION_CAPTURE_PHOTO ->
                    if (resultCode == Activity.RESULT_OK) {
                        val bitmap = BitmapFactory.decodeStream(
                            mUri?.let { context?.getContentResolver()!!.openInputStream(it) })
                        Glide.with(requireContext()).load(bitmap).apply(RequestOptions().override(200, 200)).into(binding.imageView4)
                        //binding.imageView4.setImageBitmap(bitmap)
                    }
                OPERATION_CHOOSE_PHOTO ->
                    if (resultCode == Activity.RESULT_OK) {
                        if (Build.VERSION.SDK_INT >= 19) {
                            handleImageOnKitkat(data, "imageView4")
                        }
                    }
            }
        } else if (imagetype == "imageView5") {

            when (requestCode) {
                OPERATION_CAPTURE_PHOTO ->
                    if (resultCode == Activity.RESULT_OK) {
                        val bitmap = BitmapFactory.decodeStream(
                            mUri?.let { context?.getContentResolver()!!.openInputStream(it) })
                        Glide.with(requireContext()).load(bitmap).apply(RequestOptions().override(200, 200)).into(binding.imageView5)
                        //binding.imageView5.setImageBitmap(bitmap)
                    }
                OPERATION_CHOOSE_PHOTO ->
                    if (resultCode == Activity.RESULT_OK) {
                        if (Build.VERSION.SDK_INT >= 19) {
                            handleImageOnKitkat(data, "imageView5")
                        }
                    }
            }
        } else if (imagetype == "imageView6") {

            when (requestCode) {
                OPERATION_CAPTURE_PHOTO ->
                    if (resultCode == Activity.RESULT_OK) {
                        val bitmap = BitmapFactory.decodeStream(
                            mUri?.let { activity?.getContentResolver()!!.openInputStream(it) })
                        //Glide.with(requireContext()).load(bitmap).override(200, 200).centerCrop().into(binding.imageView6)
                        Glide.with(requireContext()).load(bitmap).apply(RequestOptions().override(200, 200)).into(binding.imageView6)

                        //binding.imageView6.setImageBitmap(bitmap)
                    }
                OPERATION_CHOOSE_PHOTO ->
                    if (resultCode == Activity.RESULT_OK) {
                        if (Build.VERSION.SDK_INT >= 19) {
                            handleImageOnKitkat(data, "imageView6")
                        }
                    }
            }
        } else {
            show("Image not chosen " + imagetype)
        }


    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun decodeSampledBitmapFromResource(
        res: Resources,
        resId: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(res, resId, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeResource(res, resId, this)
        }
    }

}
