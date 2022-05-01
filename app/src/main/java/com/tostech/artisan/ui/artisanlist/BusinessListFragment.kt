package com.tostech.artisan.ui.artisanlist


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.tostech.artisan.AdapterClasses.BusinessListRecyclerAdapter2
import com.tostech.artisan.ItemDecoration
import com.tostech.artisan.MainActivity
import com.tostech.artisan.R
import com.tostech.artisan.data.BusinessListData
import com.tostech.artisan.data.RatingData
import com.tostech.artisan.databinding.FragmentArtisanListBinding

//import com.tostech.artisan.rvadapter.AdmobNativeAdAdapter

class BusinessListFragment : Fragment(){

  // private lateinit var admobNativeAdAdapter: AdmobNativeAdAdapter

    private lateinit var businessListRecyclerAdapter2: BusinessListRecyclerAdapter2
    private lateinit var mDatabase: DatabaseReference

    private var _binding: FragmentArtisanListBinding? = null
    var business = ArrayList<BusinessListData>()
    var ratingArray = ArrayList<RatingData>()
    private val binding get() = _binding!!
    lateinit var mActivity: FragmentActivity


    private var mListItems: ArrayList<Any> = ArrayList()
    private var rateListItems: ArrayList<Any> = ArrayList()
    private var NATIVE_AD_REAL: String = "ca-app-pub-2578519456532696/6949357748"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentArtisanListBinding.inflate(
            inflater,
            container,
            false
        )

/*
        MobileAds.initialize(requireContext())
        val testDeviceIds = listOf(
            "0188510711703001",
            "04714259AM007470",
            "8243AB1F26F0FA441BA02D4283E4DF8C"
        )
        val config = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(config)*/

        if (!checkNetwork()) {
            show("No network connection")
        }

        // artisan_position = arguments?.getString("artisan_position")
        val fragName = arguments?.getString("fragName")

        business = ArrayList()
        ratingArray = ArrayList()

        // Create an ad request.
        val adRequest = AdRequest.Builder().build()
        mDatabase = Firebase.database.reference.child("User")
        val firebaseUserID = Firebase.auth.currentUser!!.uid



        mDatabase.child(firebaseUserID)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.child("sub_status").value.toString()

                    if (status != "2") {
                        // Start loading the ad in the background.
                        binding.adViewCat.loadAd(adRequest)
                    }else{
                        binding.adViewCat.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

            val sharedPreferences =
                activity?.getSharedPreferences("ArtisanLocation", Context.MODE_PRIVATE)
            val country = sharedPreferences!!.getString("countryPref", "USA")


        loadData(country)


        return binding.root
    }

    private fun loadData(country: String?) {
        mDatabase!!.orderByChild("sub_status")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    business.clear()
                    ratingArray.clear()

                    businessListRecyclerAdapter2.notifyDataSetChanged()
                    for (datasnap in snapshot.children) {
                        val businessListData =
                            datasnap.child("advert").getValue<BusinessListData>()
                        //Log.d("ratingde", datasnap.child("rating").value.toString())

                        val ratingData =
                            datasnap.child("rating").getValue(RatingData::class.java)

                        try {
                            if (businessListData!!.profile == "false")
                                continue
                            if (businessListData!!.country == country) {
                                business.add(businessListData)
                                ratingArray.add(ratingData!!)
                            } else {
                                business.add(businessListData)
                                ratingArray.add(ratingData!!)

                            }

                            business(business, ratingArray)

                        } catch (ex: NullPointerException) {
                            ex.printStackTrace()
                        }


                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    //Log.d("Error", error.message)
                }
            })

        businessListRecyclerAdapter2 = BusinessListRecyclerAdapter2(
            business,
            ratingArray,
            "listFragment"
        )


        binding.recyclerBusinessList.setHasFixedSize(true)
        businessListRecyclerAdapter2.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        businessListRecyclerAdapter2.notifyDataSetChanged()

        binding.recyclerBusinessList.apply {
            layoutManager = LinearLayoutManager(context)
            val itemsDecoration = ItemDecoration(2)
            addItemDecoration(itemsDecoration)
            adapter = businessListRecyclerAdapter2
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
    }

    private fun setUpToolbar() {
        val mainActivity = mActivity as MainActivity
        val  navigationView : NavigationView? = mActivity.findViewById(R.id.nav_view)
        mainActivity.setSupportActionBar(binding.toolbar)
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration =  mainActivity.appBarConfiguration
        NavigationUI.setupActionBarWithNavController(
            mainActivity,
            navController,
            appBarConfiguration!!
        )
        NavigationUI.setupWithNavController(navigationView!!, navController)

        setHasOptionsMenu(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let { mActivity = it}
    }

    private fun show(message: String?){
        try {
            Toast.makeText(requireContext(), message!!, Toast.LENGTH_SHORT).show()
        }catch (ex: IllegalStateException){
           // Log.d("ArtException", ex.message.toString())
        }
    }

   private fun checkNetwork(): Boolean{
     //  if (Build.VERSION.SDK_INT  <= Build.VERSION_CODES.Q) {
           val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
           val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
           val isConnected = activeNetwork?.isConnectedOrConnecting == true

           return isConnected
   }


    private fun business(businessArray: ArrayList<BusinessListData>, rating: ArrayList<RatingData>){
        business = businessArray
        ratingArray = rating
    }

    // Called when leaving the activity
    public override fun onPause() {
        binding.adViewCat.pause()
        super.onPause()
    }

    // Called when returning to the activity
    public override fun onResume() {
        super.onResume()
        binding.adViewCat.resume()
    }

    // Called before the activity is destroyed
    public override fun onDestroy() {
        binding.adViewCat.destroy()
        super.onDestroy()
    }

}
