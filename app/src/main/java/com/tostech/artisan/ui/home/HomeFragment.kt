package com.tostech.artisan.ui.home


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import com.tostech.artisan.*
import com.tostech.artisan.R
import com.tostech.artisan.data.*

import com.tostech.artisan.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var binding: FragmentHomeBinding
    private var isLargeLayout: Boolean = false

    var storageRef = Firebase.storage.reference
    var databaseRef = Firebase.database.reference
    var userId: String? = null
     var countryy: String? =null
     var statee: String? =null
     var lgaa: String? =null
    private lateinit var messagesList: ArrayList<String>
    val firebaseUser = FirebaseAuth.getInstance().uid
    private lateinit var advertText: Array<String>
    private lateinit var advertUrl: Array<String>




    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        isLargeLayout = resources.getBoolean(R.bool.large_layout)
        val profilePix = binding.profilepix

        val logo = binding.logoPix


                 userId = arguments?.getString("signInID")
                //val userId = Database().readUserID()
                Log.v("UserString", userId!!)


        val ref = databaseRef.child("User/$userId/accepted_order")
        var accepted_list_array: ArrayList<String?> = ArrayList()

        val showRatingListener =object : ValueEventListener{

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                accepted_list_array.clear()
                for(IDs in dataSnapshot.children) {
                    val accept_list = IDs.getValue(String::class.java)
                    if (accept_list != null) {
                        accepted_list_array.add(accept_list)

                    }
                    else
                        Log.v("ordArray", "Array is empty")
                }

                Log.v("ordArray", accepted_list_array.toString())
                for (i in accepted_list_array){
                    if (i == firebaseUser){
                        binding.rating.setIsIndicator(false)
                    }
                    else
                        binding.rating.setIsIndicator(true)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.w("AcceptedOrder", "Cancelled", p0.toException())
            }
        }

        ref.addValueEventListener(showRatingListener)

        binding.rating.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { p0, rating, fromUser ->
                if (fromUser)
                rate(rating)
            }

        if (userId == firebaseUser){
            binding.fabMessage.visibility = View.GONE
            binding.btnOrder.visibility = View.GONE
            binding.rating.visibility = View.GONE
        }
                 storageRef.child("$userId/images/profile_pix.jpg").downloadUrl.addOnSuccessListener { i ->
                    Glide.with(requireContext()).load(i.toString())
                        .apply(RequestOptions().placeholder(R.drawable.ic_baseline_person_24))
                        .into(profilePix)

                     profilePix.setOnClickListener { showDialog(i.toString(), "null") }
                }
                 storageRef.child("$userId/images/logo.jpg").downloadUrl.addOnSuccessListener { i ->
                    Glide.with(requireContext()).load(i.toString()).placeholder(R.drawable.ic_baseline_person_24).into(logo)
                     profilePix.setOnClickListener { showDialog(i.toString(), "null") }
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
            Toast.makeText(context, "Please Register or Sign In to your account", Toast.LENGTH_SHORT).show()

            listFiles()

        val reference = FirebaseDatabase.getInstance().reference
            .child("User").child(userId!!).child("user").child("username")

        binding.btnOrder.setOnClickListener {
            order(userId!!)
        }

        binding.fabMessage.setOnClickListener {
            val intent = Intent(context, MessageChatActivity::class.java)
            reference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.value.toString()

                    Log.d("username", username)
                    intent.putExtra("visit_id", userId)
                    intent.putExtra("username", username)
                    startActivity(intent)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        }


        binding.txtPhone.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.setData(Uri.parse("tel:${binding.txtPhone.text}"))
            startActivity(intent)
        }
        binding.txtAddress.setOnClickListener {
            val intent = Intent(Intent.CATEGORY_APP_MAPS)
            intent.setData(Uri.parse("tel:${binding.txtAddress.text}"))
            startActivity(intent)
        }
        binding.txtFacebook.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse("http://${binding.txtFacebook.text}"))
            startActivity(intent)
        }
        binding.txtTwitter.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse("http://${binding.txtTwitter.text}"))
            startActivity(intent)
        }
        binding.txtWhatsApp.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse("http://${binding.txtWhatsApp.text}"))
            startActivity(intent)
        }
        binding.txtInstagram.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse("http://${binding.txtInstagram.text}"))
            startActivity(intent)
        }
        binding.txtOtherLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setData(Uri.parse("http://${binding.txtOtherLink.text}"))
            startActivity(intent)
        }

        return binding.root
    }
    private fun toast(message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun rate(rating: Float) {

        val referenceRating = databaseRef.child("User/$userId/rating")
        val refDeleteOrder = databaseRef.child("User").child(userId!!).child("accepted_order")

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
                toast("In oncomplete now")

                val userQuery = refDeleteOrder.orderByKey()

                userQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                             if (snapshot.value == firebaseUser) {
                                 Log.d("Idtosnap", snapshot.toString())
                                 snapshot.ref.removeValue()
                                 toast("ID has been removed")
                             }
                        }
                    }

                    override fun onCancelled(dbError: DatabaseError) {
                        toast( dbError.toString())
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

        val referenceID = databaseRef.child("User/$firebaseUser/advert/uid")


        val dataListenerID = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val uid = snapshot.getValue(String::class.java)
                databaseRef.child("User").child(userId!!).child("order").child(firebaseUser!!).child("uid").setValue(uid)

            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        referenceID.addValueEventListener(dataListenerID)

        //reference username
        val referenceUser = databaseRef.child("User/$firebaseUser/user/username")

        val dataListenerUser = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java)


                databaseRef.child("User").child(userId!!).child("order").child(firebaseUser!!).child("username").setValue(username)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        referenceUser.addValueEventListener(dataListenerUser)

        databaseRef.child("User").child(userId!!).child("order").child(firebaseUser!!).child("status").setValue("requested")

        val referencePurl = databaseRef.child("User/$firebaseUser/advert/purl")

        val dataListenerPurl = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                 val purl = snapshot.getValue(String::class.java)
                databaseRef.child("User").child(userId!!).child("order").child(firebaseUser!!).child("purl").setValue(purl)

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
           /* var adve1 = ""
            var adve2 = ""
            var adve3 = ""
            var adve4 = ""
            var adve5 = ""*/
            val advertPix1 = binding.advertPix1
            val advertPix2 = binding.advertPix2
            val advertPix3 = binding.advertPix3
            val advertPix4 = binding.advertPix4
            val advertPix5 = binding.advertPix5

            var advertText1 = binding.advertText1
            var advertText2 = binding.advertText2
            var advertText3 = binding.advertText3
            var advertText4 = binding.advertText4
            var advertText5 = binding.advertText5

            val requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_baseline_person_24)
                .error(R.drawable.ic_baseline_person_24)


            var dialogData = DialogData()
            refDB.addListenerForSingleValueEvent(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {

                    //  for (snap in snapshot.children) {
                    if (snapshot.exists())
                        dialogData = snapshot.getValue<DialogData>()!!
                    else
                        toast("Data does not exist")

                    advertText1.text = dialogData.advert1
                    advertText2.text = dialogData.advert2
                    advertText3.text = dialogData.advert3
                    advertText4.text = dialogData.advert4
                    advertText5.text = dialogData.advert5

                    //    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

            storageRef.child("$userId/images/advert1.jpg").downloadUrl.addOnSuccessListener { i ->
              //  adve1 = i.toString()

                 Glide.with(requireContext()).load(i.toString()).apply(requestOptions).into(advertPix1)

                 advertPix1.setOnClickListener { showDialog(i.toString(), advertText1.text.toString()) }

            }
             storageRef.child("$userId/images/advert2.jpg").downloadUrl.addOnSuccessListener { i ->
                // adve2 = i.toString()
                 Glide.with(requireContext()).load(i.toString()).apply(requestOptions).into(advertPix2)
                 advertPix2.setOnClickListener { showDialog(i.toString(), advertText2.text.toString()) }


             }
             storageRef.child("$userId/images/advert3.jpg").downloadUrl.addOnSuccessListener { i ->
             //    adve3 = i.toString()
                 advertPix3.setOnClickListener { showDialog(i.toString(), advertText3.text.toString()) }

                 Glide.with(requireContext()).load(i.toString()).apply(requestOptions).into(advertPix3)

             }
            storageRef.child("$userId/images/advert4.jpg").downloadUrl.addOnSuccessListener { i ->
               // adve4 = i.toString()
                advertPix4.setOnClickListener { showDialog(i.toString(), advertText4.text.toString()) }
                Glide.with(requireContext()).load(i.toString()).apply(requestOptions).into(advertPix4)

            }
            storageRef.child("$userId/images/advert5.jpg").downloadUrl.addOnSuccessListener { i ->
              //  adve5 = i.toString()
                Glide.with(requireContext()).load(i.toString()).apply(requestOptions).into(advertPix5)
                advertPix5.setOnClickListener { showDialog(i.toString(), advertText5.text.toString()) }
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

/*    @SuppressLint("ResourceType")
    fun showDialog(ref: StorageReference, textInput: String?){
        val builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val inflate = inflater.inflate(R.layout.image_dialog, null)

        val close = inflate.findViewById<ImageButton>(R.id.imageButton)
        val image = inflate.findViewById<ImageView>(R.id.dialogImage)
        val text = inflate.findViewById<TextView>(R.id.dialogTxt)


        Glide.with(requireContext()).load(ref).into(image)
        text.text = textInput

       builder.setView(inflate)
           .setNegativeButton(""
           ) { dialogInterface, i -> close.setOnClickListener { j -> dialogInterface.cancel() }
           }

        builder.create()
        builder.show()


    }*/
    fun showDialog(ref:String?, textInput: String?){

        val fragmentManager = requireActivity().supportFragmentManager

        val bundle = bundleOf(Pair("ref", ref), Pair("textInput", textInput))
        val newFragment = CustomDialogFragment()
        newFragment.arguments = bundle
        if (isLargeLayout){
            newFragment.show(fragmentManager, "dialog")
        }else{
            val transaction = fragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction.replace(android.R.id.content, newFragment)
                .addToBackStack(null)
                .commit()

        }
    }
}