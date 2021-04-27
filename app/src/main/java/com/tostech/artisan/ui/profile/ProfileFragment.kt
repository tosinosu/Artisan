package com.tostech.artisan.ui.profile

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
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
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.profile.*
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.OutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask


/**
 * The number of pages (wizard steps) to show in this demo.
 */
private var mUri: Uri? = null
private val OPERATION_CAPTURE_PHOTO = 1
private val OPERATION_CHOOSE_PHOTO = 2


// Create a storage reference from our app
var storageRef = Firebase.storage.reference
var intent: Intent = Intent()

private const val NUM_PAGES = 3
var selected = false

class ProfileFragment : Fragment() {

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    // private lateinit var viewPager: ViewPager2

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


        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(this)

        GetData().getAdvertPix(profilePixUrl, profPix, requireContext())

        binding.pagerProfile.adapter = pagerAdapter


        val tabLayout = binding.tabLayout

        val names: Array<String> =
            arrayOf("Profile", "Advert Images", "Subscribe")

        TabLayoutMediator(tabLayout, binding.pagerProfile) { tab, position ->
            tab.text = names[position]
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.pagerProfile.currentItem = tab!!.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        }

        )

        binding.pixProfile.setOnClickListener {
            chooseCameraGallery()

        }
        binding.uploadProfilepix.setOnClickListener {
            uploadImage()
        }
        binding.deleteProfilepix.setOnClickListener {
            deleteImage("images/profile_pix.jpg")
        }

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


    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */

    private inner class ScreenSlidePagerAdapter(fm: Fragment) :
        FragmentStateAdapter(fm) {

        override fun getItemCount(): Int = NUM_PAGES


        override fun createFragment(position: Int): Fragment {
            when(position){

                0 -> return Profile()
                1 -> return Pictures()
                2 -> return Subscription()
            }
            return Profile()
        }

    }
}
