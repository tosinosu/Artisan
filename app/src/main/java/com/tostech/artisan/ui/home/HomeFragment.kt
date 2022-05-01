package com.tostech.artisan.ui.home


import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
//import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import com.kv.popupimageview.PopupImageView
import com.tostech.artisan.AdapterClasses.GridAdapter
import com.tostech.artisan.MessageChatActivity
import com.tostech.artisan.R
import com.tostech.artisan.data.*
import com.tostech.artisan.databinding.FragmentHomeBinding
import com.tostech.artisan.utils.checkSelfPermissionCompat
import com.tostech.artisan.utils.requestPermissionsCompat
import com.tostech.artisan.utils.shouldShowRequestPermissionRationaleCompat
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_profile.*

const val PERMISSION_REQUEST_PHONE = 0

class HomeFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback {

    private var isLargeLayout: Boolean = false

    var storageRef = Firebase.storage.reference
    var databaseRef = Firebase.database.reference
    var userId: String? = null
    var countryy: String? = null
    var statee: String? = null
    var lgaa: String? = null
    val myfirebaseUserID = FirebaseAuth.getInstance().uid
    private lateinit var advertText: Array<String>
    private lateinit var advertUrl: Array<String>
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    var profilePix: CircleImageView? = null
    var logo: ImageView? = null



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false).apply {
           // hideAppBarFab(fabMessage)
           // hideAppBarFab(btnOrder)

            var isToolbarShown = false
            userId = arguments?.getString("signInID")

            // scroll change listener begins at Y = 0 when image is fully collapsed
            plantDetailScrollview.setOnScrollChangeListener(
                NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->

                    // User scrolled past image to height of toolbar and the title text is
                    // underneath the toolbar, so the toolbar should be shown.
                    val shouldShowToolbar = scrollY > binding.toolbar.height
                    if (shouldShowToolbar) {
                        binding.profilepix.isGone = true
                    } else {
                        binding.profilepix.isVisible = true
                    }

                    // The new state of the toolbar differs from the previous state; update
                    // appbar and toolbar attributes.
                    if (isToolbarShown != shouldShowToolbar) {
                        isToolbarShown = shouldShowToolbar
                        // Use shadow animator to add elevation if toolbar is shown
                        appbar.isActivated = shouldShowToolbar

                        // Show the plant name if toolbar is shown
                        toolbarLayout.isTitleEnabled = shouldShowToolbar
                    }
                }
            )
            //val fragment = arguments?.getString("category")
          //      Log.d("fragmentdebug", fragment.toString())

            toolbar.setNavigationOnClickListener { view ->
                view.findNavController().navigateUp()
            }

            if (!checkNetwork()){
                toast("No network connection")
            }

            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_share -> {
                        databaseRef.child("User/$userId/advert/bus_name").addValueEventListener( object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.exists()) {
                                    val businessName = snapshot.getValue(String::class.java)
                                    createShareIntent(businessName)
                                }else{
                                    toast("Cannot share at this time")
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                //  Log.w("Database", "loadPost:onCancelled", error.toException())
                            }
                        })
                        true
                    }
                    else -> false
                }
            }

        }


        isLargeLayout = resources.getBoolean(R.bool.large_layout)
        val imageLayout = layoutInflater.inflate(R.layout.pix_list, null)
        val bannerLayout = layoutInflater.inflate(R.layout.pix_banner, null)
        val builder = AlertDialog.Builder(requireActivity())
         profilePix = binding.profilepix
        val imageView = imageLayout.findViewById<ImageView>(R.id.pix)

         logo = binding.logoPix

        profilePix!!.setOnClickListener {
            storageRef.child("$userId/images/profile_pix.jpg").downloadUrl.addOnSuccessListener { i ->
                PopupImageView(requireContext(), view, i.toString())

            }


        }
        logo!!.setOnClickListener {
            storageRef.child("$userId/images/logo.jpg").downloadUrl.addOnSuccessListener { i ->
                PopupImageView(requireContext(), view, i.toString())

            }
        }


        binding.btnComment.setOnClickListener {
           // val action = HomeFragmentDirections
            val bundle = bundleOf("visit_id" to userId, "my_id" to myfirebaseUserID)
            findNavController().navigate(R.id.action_homeFragment_to_commentFragment, bundle)


        }

        val ref = databaseRef.child("User/$userId/accepted_order").child(userId!!)

        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // accepted_list_array.clear()
                val acceptedData = dataSnapshot.getValue<AcceptedData>()
                try {
                    if (acceptedData!!.rated == 1) {
                        binding.rating.setIsIndicator(true)
                    } else {
                        if (acceptedData!!.id == myfirebaseUserID) {
                            binding.rating.setIsIndicator(false)

                        } else {
                            binding.rating.setIsIndicator(true)
                        }
                    }

                } catch (ex: NullPointerException) {
                //    Log.v("RatingDebug", "Null pointer")

                    binding.rating.setIsIndicator(true)

                }
            }

            override fun onCancelled(p0: DatabaseError) {
                //Log.w("AcceptedOrder", "Cancelled", p0.toException())
            }
        })



            binding.rating.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { p0, rating, fromUser ->
                if (fromUser)
                    rate(rating)
            }

        if (userId == myfirebaseUserID) {
            binding.fabMessage.visibility = View.GONE
            binding.btnOrder.visibility = View.GONE
            binding.rating.visibility = View.GONE
        }

        storageRef.child("$userId/images/profile_pix.jpg").downloadUrl.addOnSuccessListener { i ->

                showImageDialog(i, profilePix)

          }

        storageRef.child("$userId/images/logo.jpg").downloadUrl.addOnSuccessListener { i ->
            showImageDialog(i, logo)
            }

        if (userId != null) {
            // Call Advert database
           // val username = databaseRef.child("User/$userId/user/fname")
            val busName = databaseRef.child("User/$userId/advert/bus_name")
            val advert = databaseRef.child("User/$userId/advert/description")
            val order = databaseRef.child("User/$userId/advert/order").child(userId!!)
            //Contact database
            val phone = databaseRef.child("User/$userId/contact/phone")
            val office_address = databaseRef.child("User/$userId/contact/office_address")
            val whatsapp = databaseRef.child("User/$userId/contact/whatsapp")
            val facebook = databaseRef.child("User/$userId/contact/facebook")
            val twitter = databaseRef.child("User/$userId/contact/twitter")
            val instagram = databaseRef.child("User/$userId/contact/instagram")
            val other = databaseRef.child("User/$userId/contact/other")

            getArtisanName(busName)
            getOrder(order, binding.btnOrder)
            getAdvert(busName, binding.businessNametxt)
            getAdvert(advert, binding.workDescription)
            getAdvert(phone, binding.txtPhone)
            getAddress(office_address)
            getAdvert(whatsapp, binding.txtWhatsApp)
            getAdvert(facebook, binding.txtFacebook)
            getAdvert(twitter, binding.txtTwitter)
            getAdvert(instagram, binding.txtInstagram)
            getAdvert(other, binding.txtOtherLink)
        } else
            Toast.makeText(
                context,
                "Please Register or Sign In to your account",
                Toast.LENGTH_SHORT
            ).show()

        listFiles()

        val reference = FirebaseDatabase.getInstance().reference
            .child("User").child(userId!!).child("user").child("username")

        val referenceID = databaseRef.child("User/$userId/order/$myfirebaseUserID/uid")
        val dataListenerID = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val uid = snapshot.getValue(String::class.java)

                if(uid == myfirebaseUserID){
                    try {
                        binding.btnOrder.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                        toast("Service Requested")
                    }catch (ex: NullPointerException){
                        ex.printStackTrace()
                    }

                }else{
                    try {
                    binding.btnOrder.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorOrange))
                    }catch (ex: NullPointerException){
                        ex.printStackTrace()
                    }
                    }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                //Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        referenceID.addValueEventListener(dataListenerID)

        binding.btnOrder.setOnClickListener {
            order(userId!!)
        }

        binding.fabMessage.setOnClickListener {
            val intent = Intent(context, MessageChatActivity::class.java)
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.value.toString()

                  //  Log.d("username", username)
                    intent.putExtra("visit_id", userId)
                    intent.putExtra("username", username)
                    startActivity(intent)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        }


        binding.txtPhone.setOnClickListener {
           showPhonePreview()
        }
        binding.txtAddress.setOnClickListener {
            val longLat = databaseRef.child("User").child(userId!!).child("contact")
            longLat.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val addressObj = snapshot.getValue<AddressData>()
                       /* Toast.makeText(
                            context,
                            "lat=${addressObj!!.latitude}, long=${addressObj!!.longitude}",
                            Toast.LENGTH_SHORT
                        ).show()*/
                        val latitude = addressObj!!.latitude

                        val longitude = addressObj!!.longitude

                        if (latitude != 0.0 && longitude != 0.0) {
                            populateLatLong(latitude, longitude)
                        } else {
                            Toast.makeText(context, "Address not found on map", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        }
        binding.txtFacebook.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            try {
                intent.`package` = "com.facebook.katana"
                intent.data = Uri.parse("fb://facewebmodal/f?href=${binding.txtFacebook.text}")

                if (binding.txtFacebook.text.contains("fb://")) {
                    intent.data = Uri.parse("${binding.txtFacebook.text}")
                }
                if (binding.txtFacebook.text.contains("https://")) {
                    intent.data = Uri.parse("${binding.txtFacebook.text}")
                }
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {

                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://${binding.txtFacebook.text}")
                    )
                )
            }

        }
        binding.txtTwitter.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            try {
                intent.`package` = "com.twitter.android"
                intent.data = Uri.parse("twitter://user?screen_name=${binding.txtTwitter.text}")

                startActivity(intent)

            } catch (ex: ActivityNotFoundException) {

                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://twitter.com/${binding.txtTwitter.text}")
                    )
                )

            }
        }
        binding.txtWhatsApp.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            try {
                intent.`package` = "com.whatsapp"
                if (binding.txtWhatsApp.text.contains("https://")) {
                    intent.data = Uri.parse("${binding.txtWhatsApp.text}")
                }
                if (!binding.txtWhatsApp.text.contains("https://")) {
                    intent.data = Uri.parse("https://${binding.txtWhatsApp.text}")
                }

                startActivity(intent)

            } catch (ex: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://${binding.txtWhatsApp.text}")
                    )
                )

            }

        }
        binding.txtInstagram.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            try {
                intent.`package` = "com.whatsapp"
                if (binding.txtInstagram.text.contains("https://")) {
                    intent.data = Uri.parse("${binding.txtInstagram.text}")
                }
                if (binding.txtInstagram.text.contains("instagram.com/")) {
                    intent.data = Uri.parse("https://${binding.txtInstagram.text}")
                }
                if (!binding.txtInstagram.text.contains("instagram.")) {
                    intent.data = Uri.parse("https://instagram.com/_u/${binding.txtInstagram.text}")
                }

                startActivity(intent)

            } catch (ex: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://${binding.txtInstagram.text}")
                    )
                )

            }
        }
        binding.txtOtherLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)

            intent.data = Uri.parse("http://${binding.txtOtherLink.text}")
            try {
                startActivity(intent)

            } catch (ex: ActivityNotFoundException) {
                toast("No browser installed")
            }
        }


        val refDb = databaseRef.child("User").child(userId!!).child("advert")
        refDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val advertImage = snapshot.getValue<AdvertImage>()
                val advertText = snapshot.getValue<AdvertText>()
                var imageAdverts : ArrayList<String>? = ArrayList()
                var textAdverts : ArrayList<String>? = ArrayList()

                  try{
                 imageAdverts = arrayListOf(
                    advertImage!!.image1, advertImage.image2, advertImage.image3,
                    advertImage.image4, advertImage.image5
                )

                 textAdverts = arrayListOf<String>(
                    advertText!!.advert1, advertText!!.advert2, advertText!!.advert3,
                    advertText!!.advert4, advertText!!.advert5
                )
                  }catch (ex: java.lang.NullPointerException){
                      //Log.d("Homedebug", ex.printStackTrace().toString())
                  }
                try {
                    if (advertImage!!.image1.isNullOrBlank() && advertImage!!.image2.isNullOrBlank() && advertImage!!.image3.isNullOrBlank() &&
                        advertImage!!.image4.isNullOrBlank() && advertImage!!.image5.isNullOrBlank()
                    ) {
                        binding.pixRecycle.visibility = View.GONE
                    } else {
                        showRec(imageAdverts, textAdverts)
                    }
                }catch (ex: java.lang.NullPointerException){
                  //  Log.d("HomeDebug", ex.printStackTrace().toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
        return binding.root
    }

    private fun getArtisanName(databaseReference: DatabaseReference) {
        val dataListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               val  value = snapshot.getValue(String::class.java)
               binding.toolbarLayout.title = value

            }
            override fun onCancelled(error: DatabaseError) {
              //  Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        databaseReference.addValueEventListener(dataListener)

    }

    private fun showImageDialog(i: Uri?, pix: ImageView?): String? {
        try {
        Glide.with(this).load(i.toString())
            .apply(RequestOptions().placeholder(R.drawable.ic_baseline_person_24))
            .into(pix!!)
        }catch(ex: IllegalStateException){
            //Log.d("Error", ex.message.toString())
        }catch(ex: NullPointerException){
        //    Log.d("Error", ex.message.toString())
        }

        return i.toString()
    }

    private fun requestPhone() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${binding.txtPhone.text}")
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            toast("No application to display number")
        }
    }

    private fun populateLatLong(latitude: Double, longitude: Double) {
        val data = Uri.parse("geo:$latitude, $longitude?q=$latitude, $longitude")
        val intent = Intent(Intent.ACTION_VIEW, data)

        intent.`package` = "com.google.android.apps.maps"
        intent.resolveActivity(requireActivity().packageManager)?.let {
            startActivity(intent)
        }
    }


    fun showRec(arrayImage: ArrayList<String>?, arrayText: ArrayList<String>?) {

        val gridLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.pixRecycle.layoutManager = gridLayout

        binding.pixRecycle.adapter = GridAdapter(arrayImage, arrayText, userId!!, requireContext());

    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    private fun rate(rating: Float) {

        val referenceRating = databaseRef.child("User/$userId/rating")

        referenceRating.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val ratingData = mutableData.getValue(RatingData::class.java)

                if (mutableData.value != null) {
                    val ratTotScore = ratingData!!.rat_score + rating
                    val ratNumScore = ratingData!!.rat_number + 1

          //          Log.i("rattotscore", ratTotScore.toString())
                    databaseRef.child("User/$userId").child("rating").child("rat_score")
                        .setValue(ratTotScore)
                    databaseRef.child("User/$userId").child("rating").child("rat_number")
                        .setValue(ratNumScore)
                } else {
                    databaseRef.child("User/$userId").child("rating").child("rat_score")
                        .setValue(rating)
                    databaseRef.child("User/$userId").child("rating").child("rat_number")
                        .setValue(1)

                }

                return Transaction.success(mutableData)
            }

            override fun onComplete(p0: DatabaseError?, p1: Boolean, p2: DataSnapshot?) {
                //  toast("In oncomplete now")
                val refDeleteOrder =
                    databaseRef.child("User").child(userId!!).child("accepted_order")
                val userQuery = refDeleteOrder.child(userId!!)

                userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        val acceptedData = dataSnapshot.getValue(AcceptedData::class.java)
                        if (myfirebaseUserID == acceptedData!!.id) {
                            userQuery.child("rated").setValue(1)

                            deleteAccepted(userId)
                        }

                    }

                    override fun onCancelled(dbError: DatabaseError) {
                        toast(dbError.toString())
                    }

                })
            }
        })
    }

    private fun ratingScore() {

        val referenceRating = databaseRef.child("User/$userId/rating")

        val dataListenerRating = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ratingData = snapshot.getValue(RatingData::class.java)
                try {

            //        Log.v("ratdata", ratingData!!.rat_number.toString())

                    val total_rating = ratingData!!.rat_score / ratingData!!.rat_number

                    binding.rating.rating = total_rating

                } catch (ex: NullPointerException) {
              //      Log.v("ExceptionData", ex.message.toString())
                } catch (ex: StorageException) {
                //    Log.v("Exceptiondata", ex.message.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                //Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        referenceRating.addListenerForSingleValueEvent(dataListenerRating)

    }

    private fun order(userId: String) {

        val referenceID = databaseRef.child("User/$myfirebaseUserID/advert/uid")
        val dataListenerID = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val uid = snapshot.getValue(String::class.java)
                databaseRef.child("User").child(userId!!).child("order").child(myfirebaseUserID!!)
                    .child(
                        "uid"
                    ).setValue(uid).addOnSuccessListener {
                        try{
                        binding.btnOrder.backgroundTintList = ColorStateList.valueOf(Color.GREEN)
                        toast("Service Requested")
                    }catch (ex: NullPointerException){
                    ex.printStackTrace()
                }
                    }

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "There was an error: Please try again", Toast.LENGTH_SHORT).show()
//                Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        referenceID.addValueEventListener(dataListenerID)

        //reference username
        val referenceUser = databaseRef.child("User/$myfirebaseUserID/user/username")

        val dataListenerUser = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java)


                databaseRef.child("User").child(userId!!).child("order").child(myfirebaseUserID!!)
                    .child(
                        "username"
                    ).setValue(username)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
  //              Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        referenceUser.addValueEventListener(dataListenerUser)

        databaseRef.child("User").child(userId!!).child("order").child(myfirebaseUserID!!)
            .child("status").setValue(
            "requested"
        )

        val referencePurl = databaseRef.child("User/$myfirebaseUserID/advert/purl")

        val dataListenerPurl = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val purl = snapshot.getValue(String::class.java)
                databaseRef.child("User").child(userId!!).child("order").child(myfirebaseUserID!!)
                    .child(
                        "purl"
                    ).setValue(purl)

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
    //            Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        referencePurl.addValueEventListener(dataListenerPurl)

    }

    override fun onResume() {
        super.onResume()
        ratingScore()
    }

    override fun onStart() {
        super.onStart()
        ratingScore()
    }
    fun getAdvert(reference: DatabaseReference, view: TextView) {
               val dataListener = object: ValueEventListener {
                   override fun onDataChange(snapshot: DataSnapshot) {
                       val value = snapshot.getValue(String::class.java)
                       view.text = value

                   }
                   override fun onCancelled(error: DatabaseError) {
    //                   Log.w("Database", "loadPost:onCancelled", error.toException())
                   }
               }

               reference.addValueEventListener(dataListener)

       }
        fun getOrder(reference: DatabaseReference, view: FloatingActionButton) {

               val dataListener = object: ValueEventListener {
                   override fun onDataChange(snapshot: DataSnapshot) {
                       val order = snapshot.getValue(Order::class.java)
                        try {
                            view.contentDescription = order!!.status
                        }
                        catch (ex: NullPointerException){
                          view.contentDescription = "Request"
                        }

                   }
                   override fun onCancelled(error: DatabaseError) {
      //                 Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
        //               Log.w("Database", "loadPost:onCancelled", error.toException())
                   }
               }

               reference.addValueEventListener(dataListener)

       }
        fun getLGA(reference: DatabaseReference) {
                       val dataListener = object: ValueEventListener {
                           override fun onDataChange(snapshot: DataSnapshot) {
                               val value = snapshot.getValue(String::class.java)
                               lgaa = value

                           }
                           override fun onCancelled(error: DatabaseError) {
          //                     Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
            //                   Log.w("Database", "loadPost:onCancelled", error.toException())
                           }
                       }

                       reference.addValueEventListener(dataListener)

               }
        fun getAddress(reference: DatabaseReference) {
          //  val userId = Database().readUserID()

            if(userId != null) {
                val country = databaseRef.child("User/$userId/contact/country")
                val state = databaseRef.child("User/$userId/contact/state")

                val lga = databaseRef.child("User/$userId/contact/lga")

               getCountry(country)
               getState(state)
                getLGA(lga)

                //getAdvert(state, binding.workDescription)

                val dataListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val value = snapshot.getValue(String::class.java)
                        binding.txtAddress.text = "$value, $lgaa, $statee, $countryy"

                    }

                    override fun onCancelled(error: DatabaseError) {
              //          Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                //        Log.w("Database", "loadPost:onCancelled", error.toException())
                    }
                }

                reference.addValueEventListener(dataListener)
            }

               }
        fun getState(reference: DatabaseReference) {
                       val dataListener = object: ValueEventListener {
                           override fun onDataChange(snapshot: DataSnapshot) {
                               val value = snapshot.getValue(String::class.java)
                               statee = value

                           }
                           override fun onCancelled(error: DatabaseError) {
                  //             Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                    //           Log.w("Database", "loadPost:onCancelled", error.toException())
                           }
                       }

                       reference.addValueEventListener(dataListener)

               }
        fun getCountry(reference: DatabaseReference) {

                       val dataListener = object: ValueEventListener {
                           override fun onDataChange(snapshot: DataSnapshot) {
                               val value = snapshot.getValue(String::class.java)
                               countryy = value

                           }
                           override fun onCancelled(error: DatabaseError) {
                      //         Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                        //       Log.w("Database", "loadPost:onCancelled", error.toException())
                           }
                       }

                       reference.addValueEventListener(dataListener)

               }
        fun listFiles() {

            val refDB = databaseRef.child("User").child(userId!!).child("advert")

            val requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_baseline_person_24)
                .error(R.drawable.ic_baseline_person_24)


            var dialogData = DialogData()
            refDB.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    //  for (snap in snapshot.children) {
                    if (snapshot.exists())
                        dialogData = snapshot.getValue<DialogData>()!!
                    else
                        toast("Data does not exist")

                }

                override fun onCancelled(p0: DatabaseError) {
                }
            })

            storageRef.child("$userId/images/advert1.jpg").downloadUrl.addOnSuccessListener { i ->
                refDB.child("image1").setValue(i.toString())

            }
            storageRef.child("$userId/images/advert2.jpg").downloadUrl.addOnSuccessListener { i ->
                refDB.child("image2").setValue(i.toString())

            }
            storageRef.child("$userId/images/advert3.jpg").downloadUrl.addOnSuccessListener { i ->
                refDB.child("image3").setValue(i.toString())

            }
            storageRef.child("$userId/images/advert4.jpg").downloadUrl.addOnSuccessListener { i ->
                refDB.child("image4").setValue(i.toString())

            }
            storageRef.child("$userId/images/advert5.jpg").downloadUrl.addOnSuccessListener { i ->
                refDB.child("image5").setValue(i.toString())
            }
        }

    fun arraylist(): ArrayList<HomeData> {
     //   advertText = emptyArray()
       // advertUrl = emptyArray()
        val array  = ArrayList<HomeData>()
        for (i in 0..advertText.size-1){
            val homeData = HomeData()
            homeData.advertText = advertText[i]
            homeData.advertImageUrl = advertUrl[i].toString()

            array.add(homeData)
        }
        return array
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    private fun deleteAccepted(userId: String?){
        val refDeleteOrder = databaseRef.child("User").child(userId!!).child("accepted_order").child(
            userId!!
        )

        refDeleteOrder.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val deleteData = snapshot.getValue(DeleteData::class.java)
                    if (deleteData!!.commented == 1 && deleteData!!.rated == 1) {
                        refDeleteOrder.removeValue()

                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_PHONE) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
              //  layout.showSnackbar(R.string.camera_permission_granted, Snackbar.LENGTH_SHORT)
                requestPhone()
            } else {
                toast("Phone permission request was denied.")
              //  layout.showSnackbar(R.string.camera_permission_denied, Snackbar.LENGTH_SHORT)
            }
        }

    }

    private fun showPhonePreview() {
        // Check if the Camera permission has been granted
        if (checkSelfPermissionCompat(Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
           // layout.showSnackbar(R.string.camera_permission_available, Snackbar.LENGTH_SHORT)
            requestPhone()
        } else {
            // Permission is missing and must be requested.
            requestPhonePermission()
        }
    }

    private fun requestPhonePermission() {
        // Permission has not been granted and must be requested.
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.READ_PHONE_STATE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
          //  binding.homeLayout.showSnackbar(R.string.app_name,
            //    Snackbar.LENGTH_INDEFINITE, R.string.ok)
            //{
                requestPermissionsCompat(arrayOf(Manifest.permission.READ_PHONE_STATE),
                    PERMISSION_REQUEST_PHONE)
           // }

        } else {
           // binding.homeLayout.showSnackbar("R.string.camera_permission_not_available", Snackbar.LENGTH_SHORT)
            toast("Phone permission not available")

            // Request the permission. The result will be received in onRequestPermissionResult().
            requestPermissionsCompat(arrayOf(Manifest.permission.READ_PHONE_STATE), PERMISSION_REQUEST_PHONE)
        }
    }

    private fun createShareIntent(businessName:String?) {
        val shareText = "\"$businessName\" is on Artisan. You can download Artisan App on Google Play App Store https://bit.ly/therealartisan"
        val shareIntent = ShareCompat.IntentBuilder.from(requireActivity())
            .setText(shareText)
            .setType("text/plain")
            .createChooserIntent()
            .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        startActivity(shareIntent)
    }

    // FloatingActionButtons anchored to AppBarLayouts have their visibility controlled by the scroll position.
    // We want to turn this behavior off to hide the FAB when it is clicked.
    //
    // This is adapted from Chris Banes' Stack Overflow answer: https://stackoverflow.com/a/41442923
    private fun hideAppBarFab(fab: FloatingActionButton) {
        val params = fab.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as FloatingActionButton.Behavior
        behavior.isAutoHideEnabled = false
        fab.hide()
    }

    private fun checkNetwork(): Boolean{
        //  if (Build.VERSION.SDK_INT  <= Build.VERSION_CODES.Q) {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected = activeNetwork?.isConnectedOrConnecting == true

        return isConnected


    }
}
