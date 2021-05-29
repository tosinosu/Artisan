package com.tostech.artisan.ui.home


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ShareCompat
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
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
import com.tostech.artisan.*
import com.tostech.artisan.AdapterClasses.GridAdapter
import com.tostech.artisan.R
import com.tostech.artisan.data.*
import com.tostech.artisan.databinding.FragmentHomeBinding
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_home.*


class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var homeViewModel: HomeViewModel


    private var isLargeLayout: Boolean = false

    var storageRef = Firebase.storage.reference
    var databaseRef = Firebase.database.reference
    var userId: String? = null
     var countryy: String? =null
     var statee: String? =null
     var lgaa: String? =null
    private lateinit var messagesList: ArrayList<String>
    val myfirebaseUserID = FirebaseAuth.getInstance().uid
    private lateinit var advertText: Array<String>
    private lateinit var advertUrl: Array<String>
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

         _binding = FragmentHomeBinding.inflate(inflater, container, false).apply {

            var isToolbarShown = false


             (activity as AppCompatActivity?)!!.supportActionBar!!.hide()

            // hideAppBarFab(profilepix)

            // scroll change listener begins at Y = 0 when image is fully collapsed
            plantDetailScrollview.setOnScrollChangeListener(
                NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->

                    // User scrolled past image to height of toolbar and the title text is
                    // underneath the toolbar, so the toolbar should be shown.
                    val shouldShowToolbar = scrollY > toolbar.height
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

            toolbar.setNavigationOnClickListener { view ->
                val intent = Intent(context, MainActivity::class.java)
                startActivity(intent)
            }

            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_share -> {
                        createShareIntent()
                        true
                    }
                    else -> false
                }
            }
        }
        isLargeLayout = resources.getBoolean(R.bool.large_layout)
        val profilePix = binding.profilepix

        val logo = binding.logoPix


        userId = arguments?.getString("signInID")

        binding.btnComment.setOnClickListener {
            val bundle = bundleOf()
            val intent = Intent(requireContext(), CommentActivity::class.java)
            intent.putExtra("visit_id", userId)

            startActivity(intent)

        }



        val ref = databaseRef.child("User/$userId/accepted_order").child(userId!!)


        ref.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // accepted_list_array.clear()
                val acceptedData = dataSnapshot.getValue<AcceptedData>()
                /*for(IDs in dataSnapshot.children) {
                    val accept_list = IDs.getValue(String::class.java)
                    if (accept_list != null) {
                        accepted_list_array.add(accept_list)

                    }
                    else
                        Log.v("ordArray", "Array is empty")
                }*/
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
                    /* Log.v("ordArray", accepted_list_array.toString())
                for (i in accepted_list_array){
                    if (i == firebaseUser){
                        binding.rating.setIsIndicator(false)
                    }
                    else
                        binding.rating.setIsIndicator(true)
                }*/
                } catch (ex: NullPointerException) {
                    Log.v("RatingDebug", "Null pointer")

                    binding.rating.setIsIndicator(true)

                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.w("AcceptedOrder", "Cancelled", p0.toException())
            }
        })

        binding.rating.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { p0, rating, fromUser ->
                if (fromUser)
                rate(rating)
            }

        if (userId == myfirebaseUserID){
            fabMessage.visibility = View.GONE
            binding.btnOrder.visibility = View.GONE
            binding.rating.visibility = View.GONE
        }
                 storageRef.child("$userId/images/profile_pix.jpg").downloadUrl.addOnSuccessListener { i ->
                    Glide.with(requireContext()).load(i.toString())
                        .apply(RequestOptions().placeholder(R.drawable.ic_baseline_person_24))
                        .into(profilePix)
                 }



        if(userId != null) {

            // Call Advert database
            val busName = databaseRef.child("User/$userId/advert/bus_name")
            val advert = databaseRef.child("User/$userId/advert/description")
            val order = databaseRef.child("User/$userId/advert/order").child(userId!!)

            //Contact database
            val phone = databaseRef.child("User/$userId/contact/phone")
            val office_address = databaseRef.child("User/$userId/contact/office_address")
            val whatsapp = databaseRef.child("User/$userId/contact/whatsapp")
            val facebook =  databaseRef.child("User/$userId/contact/facebook")
            val twitter = databaseRef.child("User/$userId/contact/twitter")
            val instagram =  databaseRef.child("User/$userId/contact/instagram")
            val other =  databaseRef.child("User/$userId/contact/other")


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
        }
            else
            Toast.makeText(
                context,
                "Please Register or Sign In to your account",
                Toast.LENGTH_SHORT
            ).show()

            listFiles()

        val reference = FirebaseDatabase.getInstance().reference
            .child("User").child(userId!!).child("user").child("username")

        binding.btnOrder.setOnClickListener {
            order(userId!!)
        }

        binding.fabMessage.setOnClickListener {
            val intent = Intent(context, MessageChatActivity::class.java)
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.value.toString()

                    Log.d("username", username)
                    intent.putExtra("visit_id", userId)
                    intent.putExtra("username", username)
                    startActivity(intent)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        }


        binding.txtPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${binding.txtPhone.text}")
            try {
                startActivity(intent)
            }catch (ex: ActivityNotFoundException){
                toast("No application to display number")
            }
        }
        binding.txtAddress.setOnClickListener {
            val longLat = databaseRef.child("User").child(userId!!).child("contact")
            longLat.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val addressObj = snapshot.getValue<AddressData>()
                        Toast.makeText(
                            context,
                            "lat=${addressObj!!.latitude}, long=${addressObj!!.longitude}",
                            Toast.LENGTH_SHORT
                        ).show()

                        if (addressObj!!.latitude != 0.0 && addressObj!!.longitude != 0.0) {
                            populateLatLong(addressObj!!.latitude, addressObj!!.longitude)
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

                if (binding.txtFacebook.text.contains("fb://")){
                intent.data = Uri.parse("${binding.txtFacebook.text}")
            }
            if (binding.txtFacebook.text.contains("https://")){
                intent.data = Uri.parse("${binding.txtFacebook.text}")
            }
                startActivity(intent)
            }catch (ex: ActivityNotFoundException){

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

            }catch (ex: ActivityNotFoundException){

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
              if (binding.txtWhatsApp.text.contains("https://")){
                  intent.data = Uri.parse("${binding.txtWhatsApp.text}")
              }
              if (!binding.txtWhatsApp.text.contains("https://")){
                  intent.data = Uri.parse("https://${binding.txtWhatsApp.text}")
              }

              startActivity(intent)

          }catch (ex: ActivityNotFoundException){
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
                if (binding.txtInstagram.text.contains("https://")){
                    intent.data = Uri.parse("${binding.txtInstagram.text}")
                }
                if (binding.txtInstagram.text.contains("instagram.com/")){
                    intent.data = Uri.parse("https://${binding.txtInstagram.text}")
                }
                if (!binding.txtInstagram.text.contains("instagram.")){
                    intent.data = Uri.parse("https://instagram.com/_u/${binding.txtInstagram.text}")
                }

                startActivity(intent)

            }catch (ex: ActivityNotFoundException){
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

            }catch (ex: ActivityNotFoundException){
                toast("No browser installed")
            }
        }



       val refDb = databaseRef.child("User").child(userId!!).child("advert")
            refDb.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val advertImage = snapshot.getValue<AdvertImage>()
                    val advertText = snapshot.getValue<AdvertText>()
                    val imageAdverts = arrayListOf<String>(
                        advertImage!!.image1, advertImage!!.image2, advertImage!!.image3,
                        advertImage!!.image4, advertImage!!.image5
                    )
                    val textAdverts = arrayListOf<String>(
                        advertText!!.advert1, advertText!!.advert2, advertText!!.advert3,
                        advertText!!.advert4, advertText!!.advert5
                    )

                    if (advertImage!!.image1 == "null" && advertImage!!.image2 == "null" && advertImage!!.image3 == "null" &&
                    advertImage!!.image4 == "null" && advertImage!!.image5 == "null") {
                        binding.pixRecycle.visibility = View.GONE
                    } else {
                        showRec(imageAdverts, textAdverts)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })


        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(
                true
            ) {
                override fun handleOnBackPressed() {
                    val intent = Intent(context, MainActivity::class.java)
                    startActivity(intent)
                }
            })

        return binding.root
    }

    private fun populateLatLong(latitude: Double, longitude: Double) {
        val data = Uri.parse("geo:$latitude, $longitude")
        val intent = Intent(Intent.ACTION_VIEW, data)

        intent.`package` = "com.google.android.apps.maps"
       intent.resolveActivity(requireActivity().packageManager)?.let {
           startActivity(intent)
       }
    }


    fun showRec(arrayImage: ArrayList<String>, arrayText: ArrayList<String>){

        val gridLayout = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.pixRecycle.layoutManager = gridLayout

        binding.pixRecycle.adapter = GridAdapter(arrayImage, arrayText, userId!!, requireContext());

       // prepareTransitions()
       // postponeEnterTransition()
    }
    private fun toast(message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
/*
    override fun onDetach() {
        super.onDetach()
        _binding = null
    }*/

    private fun createShareIntent() {
        val shareText =
            "Artisan"


        val shareIntent = ShareCompat.IntentBuilder.from(requireActivity())
            .setText(shareText)
            .setType("text/plain")
            .createChooserIntent()
            .addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        startActivity(shareIntent)
    }

    private fun rate(rating: Float) {

        val referenceRating = databaseRef.child("User/$userId/rating")

        referenceRating.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val ratingData = mutableData.getValue(RatingData::class.java)

                if (mutableData.value != null) {
                    val ratTotScore = ratingData!!.rat_score + rating
                    val ratNumScore = ratingData!!.rat_number + 1

                    Log.i("rattotscore", ratTotScore.toString())
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
                        /* for (snapshot in dataSnapshot.children) {
                             if (snapshot.value == firebaseUser) {
                                 //Log.d("Idtosnap", snapshot.toString())
                                 //snapshot.ref.removeValue()
                                 //toast("ID has been removed")
                                 refDeleteOrder.child("Comment_Rat").child("rated").setValue(1)
                             }
                        }*/
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

private fun ratingScore(){

        val referenceRating = databaseRef.child("User/$userId/rating")

        val dataListenerRating = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ratingData = snapshot.getValue(RatingData::class.java)
               try {

                   Log.v("ratdata", ratingData!!.rat_number.toString())

                   val total_rating = ratingData!!.rat_score / ratingData!!.rat_number

                   binding.rating.rating = total_rating

               } catch (ex: NullPointerException){
                   Log.v("ExceptionData", ex.message.toString())
               } catch (ex: StorageException){
                   Log.v("Exceptiondata", ex.message.toString())
               }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        referenceRating.addListenerForSingleValueEvent(dataListenerRating)

    }

    private fun order(userId: String) {

        val referenceID = databaseRef.child("User/$myfirebaseUserID/advert/uid")


        val dataListenerID = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val uid = snapshot.getValue(String::class.java)
                databaseRef.child("User").child(userId!!).child("order").child(myfirebaseUserID!!).child(
                    "uid"
                ).setValue(uid)

            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        referenceID.addValueEventListener(dataListenerID)

        //reference username
        val referenceUser = databaseRef.child("User/$myfirebaseUserID/user/username")

        val dataListenerUser = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java)


                databaseRef.child("User").child(userId!!).child("order").child(myfirebaseUserID!!).child(
                    "username"
                ).setValue(username)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        referenceUser.addValueEventListener(dataListenerUser)

        databaseRef.child("User").child(userId!!).child("order").child(myfirebaseUserID!!).child("status").setValue(
            "requested"
        )

        val referencePurl = databaseRef.child("User/$myfirebaseUserID/advert/purl")

        val dataListenerPurl = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                 val purl = snapshot.getValue(String::class.java)
                databaseRef.child("User").child(userId!!).child("order").child(myfirebaseUserID!!).child(
                    "purl"
                ).setValue(purl)

            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        referencePurl.addValueEventListener(dataListenerPurl)

    }


    private fun updateStatus(status: String){
             if (userId != null) {
            var ref = FirebaseDatabase.getInstance().reference.child("User").child(userId!!).child("advert")
            val hashMap = HashMap<String, Any>()
            hashMap["status"] = status
            ref!!.updateChildren(hashMap)
        }
    }

    override fun onResume() {
        super.onResume()
        ratingScore()
        updateStatus("online")
    }

    override fun onStart() {
        super.onStart()
        ratingScore()
    }

    override fun onPause() {
        super.onPause()
        updateStatus("offline")
    }

    override fun onStop() {
        super.onStop()
        updateStatus("offline")
    }

        fun getAdvert(reference: DatabaseReference, view: TextView) {
               val dataListener = object: ValueEventListener {
                   override fun onDataChange(snapshot: DataSnapshot) {
                       val value = snapshot.getValue(String::class.java)
                       view.text = value

                   }
                   override fun onCancelled(error: DatabaseError) {
                       Log.w("Database", "loadPost:onCancelled", error.toException())
                   }
               }

               reference.addValueEventListener(dataListener)

       }
        fun getOrder(reference: DatabaseReference, view: Button) {

               val dataListener = object: ValueEventListener {
                   override fun onDataChange(snapshot: DataSnapshot) {
                       val order = snapshot.getValue(Order::class.java)
                        try {
                            view.text = order!!.status
                        }
                        catch (ex: NullPointerException){
                          view.text = "Request"
                        }

                   }
                   override fun onCancelled(error: DatabaseError) {
                       Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                       Log.w("Database", "loadPost:onCancelled", error.toException())
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
                               Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                               Log.w("Database", "loadPost:onCancelled", error.toException())
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
                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                        Log.w("Database", "loadPost:onCancelled", error.toException())
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
                               Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                               Log.w("Database", "loadPost:onCancelled", error.toException())
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
                               Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                               Log.w("Database", "loadPost:onCancelled", error.toException())
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

                    val advertText1 = dialogData.advert1
                    val advertText2 = dialogData.advert2
                    val advertText3 = dialogData.advert3
                    val advertText4 = dialogData.advert4
                    val advertText5 = dialogData.advert5

                    val arrayText = arrayListOf<String>(
                        advertText1,
                        advertText2,
                        advertText3,
                        advertText4,
                        advertText5
                    )

/*

                    advertText1.text = dialogData.advert1
                    advertText2.text = dialogData.advert2
                    advertText3.text = dialogData.advert3
                    advertText4.text = dialogData.advert4
                    advertText5.text = dialogData.advert5
*/

                    //    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
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

    fun showDialog(ref: String?, textInput: String?){

        val fragmentManager = requireActivity().supportFragmentManager

        val bundle = bundleOf(Pair("ref", ref), Pair("textInput", textInput))
        val newFragment = CustomDialogFragment()
        newFragment.arguments = bundle
        if (isLargeLayout){
            newFragment.show(fragmentManager, "dialog")
        }else{
            val transaction = fragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction.add(R.id.home_layout, newFragment)
                .addToBackStack(null)
                .commit()

        }
    }

    private fun hideAppBarFab(img: CircleImageView) {
        val params = img.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior as? FloatingActionButton.Behavior

        behavior?.isAutoHideEnabled = false
        img.isGone =true
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




}
