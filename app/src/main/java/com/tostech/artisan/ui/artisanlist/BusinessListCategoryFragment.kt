package com.tostech.artisan.ui.artisanlist

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
//import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
//import com.clockbyte.admobadapter.bannerads.AdmobBannerRecyclerAdapterWrapper
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tostech.artisan.AdapterClasses.BusinessListCategoryRecyclerAdapter
import com.tostech.artisan.Countries
import com.tostech.artisan.ItemDecoration
import com.tostech.artisan.MainActivity
import com.tostech.artisan.R
import com.tostech.artisan.data.BusinessListData
import com.tostech.artisan.data.ContactData
import com.tostech.artisan.data.RatingData
import com.tostech.artisan.databinding.FragmentArtisanListCategoryBinding
//import com.tostech.artisan.rvadapter.AdmobNativeAdAdapter
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import java.util.*


class BusinessListCategoryFragment : Fragment() {

    private lateinit var businessListCategoryRecyclerAdapter: BusinessListCategoryRecyclerAdapter
    private lateinit var mDatabase: DatabaseReference
    private var _binding: FragmentArtisanListCategoryBinding? = null
    var business = ArrayList<BusinessListData>()
    var ratingArray = ArrayList<RatingData>()
    var businessAds = ArrayList<BusinessListData>()
    var ratingArrayAds = ArrayList<RatingData>()

    private val binding get() = _binding!!
    lateinit var mActivity: FragmentActivity

    var artisan_position: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentArtisanListCategoryBinding.inflate(
            inflater,
            container,
            false
        )
        if (!checkNetwork()) {
            show("No network connection")
        }

        //  if (checkNetwork()) {

        mDatabase = Firebase.database.reference.child("User")

        artisan_position = arguments?.getString("artisan_position")
        val coming = arguments?.getString("coming")

        var lgaValue: String? = ""
        // initUpdateAdsTimer()

        //    val testId = Arrays.asList("0188510711703001"/*, "04714259AM007470"*/)

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



        if (!artisan_position.isNullOrEmpty()) {
            binding.spinner3.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        adapterView: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        p3: Long
                    ) {
                        if (position >= 0)
                             lgaValue = adapterView!!.getItemAtPosition(position).toString()

                            loadArtisan(lgaValue)
                    }


                    override fun onNothingSelected(p0: AdapterView<*>?) {
                    }
                }
        }


        val countriesList: ArrayList<String> = ArrayList()

        try {
            // As we have JSON object, so we are getting the object
            //Here we are calling a Method which is returning the JSON object
            val obj = JSONObject(getJsonFromAsset(requireContext(), "countries.json")!!)
            // fetch JSONArray named users by using getJSONArray
            val usersArray = obj.getJSONArray("countries")
            // Get the users data using for loop i.e. id, name, email and so on

            for (i in 0 until usersArray.length()) {
                // Create a JSONObject for fetching single User's Data
                val user = usersArray.getJSONObject(i)
                // Fetch id store it in variable
                val id = user.getInt("id")
                val name = user.getString("name")


                // add the details in the list
                countriesList.add(name)
            }
        } catch (e: JSONException) {
            //exception
            e.printStackTrace()
        }


        val adapter = ArrayAdapter(
            requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            countriesList
        )
        binding.spinner.adapter = adapter
        binding.spinner.setSelection(159)

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            var stateList: ArrayList<String> = ArrayList()

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                stateList.clear()

                try {
                    // As we have JSON object, so we are getting the object
                    //Here we are calling a Method which is returning the JSON object
                    val obj = JSONObject(getJsonFromAsset(requireContext(), "states.json")!!)
                    // fetch JSONArray named users by using getJSONArray
                    val usersArray = obj.getJSONArray("states")
                    // Get the users data using for loop i.e. id, name, email and so on

                    for (i in 0 until usersArray.length()) {
                        // Create a JSONObject for fetching single User's Data
                        val user = usersArray.getJSONObject(i)
                        // Fetch id store it in variable
                        val countryid = user.getString("country_id")
                        val name = user.getString("name")


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
                binding.spinner2.adapter = adapter2

                binding.spinner2.setSelection(24)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        binding.spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            val LGAList: ArrayList<String> = ArrayList()

            override fun onItemSelected(
                p0: AdapterView<*>?,
                p1: View?,
                position: Int,
                p3: Long
            ) {

                val selectedState = binding.spinner2.selectedItem.toString()

                LGAList.clear()

                try {
                    // As we have JSON object, so we are getting the object
                    //Here we are calling a Method which is returning the JSON object
                    val obj = JSONObject(getJsonFromAsset(requireContext(), "cities.json")!!)
                    val objState =
                        JSONObject(getJsonFromAsset(requireContext(), "states.json")!!)
                    // fetch JSONArray named users by using getJSONArray
                    val statesArray = objState.getJSONArray("states")
                    val citiesArray = obj.getJSONArray("cities")
                    // Get the users data using for loop i.e. id, name, email and so on

                    //loop through states
                    for (j in 0 until statesArray.length()) {
                        val state = statesArray.getJSONObject(j)
                        // Fetch id store it in variable
                        val stateid = state.getString("id")
                        val stateName = state.getString("name")
                        if (selectedState == stateName) {
                            for (i in 0 until citiesArray.length()) {
                                // Create a JSONObject for fetching single User's Data
                                val cities = citiesArray.getJSONObject(i)
                                // Fetch id store it in variable
                                val stateID2 = cities.getString("state_id")
                                val name = cities.getString("name")


                                // Now add all the variables to the data model class and the data model class to the array list.
                                val countriesDetails = Countries(id, name)

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
                binding.spinner3.adapter = adapterLGA

                binding.spinner3.setSelection(1)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }

        return binding.root
    }

    fun loadArtisanAds(lgaValue: String?) {
        mDatabase!!.orderByChild("sub_status")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    businessAds.clear()
                    ratingArrayAds.clear()
                    businessListCategoryRecyclerAdapter.notifyDataSetChanged()

                    for (datasnap in snapshot.children) {
                        val businessListData =
                            datasnap.child("advert")
                                .getValue(BusinessListData::class.java)
                        val contactData =
                            datasnap.child("contact")
                                .getValue(ContactData::class.java)
                        val ratingData =
                            datasnap.child("rating")
                                .getValue(RatingData::class.java)

                        try {
                            if (businessListData!!.profile == "false")
                                continue
                            if (businessListData!!.category == artisan_position && contactData!!.lga == lgaValue!!) {
                                businessAds.add(businessListData)
                                ratingArrayAds.add(ratingData!!)
                                business(businessAds, ratingArrayAds)
                            } else {
                                show("No $artisan_position is available at $lgaValue yet")
                                continue
                            }
                        } catch (ex: NullPointerException) {
                            ex.printStackTrace()
                        }

                        businessListCategoryRecyclerAdapter.notifyItemInserted(businessAds.size)

                    }


                }

                override fun onCancelled(error: DatabaseError) {
                    //                                      Log.d("Error", error.message)
                }
            })


        businessListCategoryRecyclerAdapter =
            BusinessListCategoryRecyclerAdapter(
                businessAds, ratingArrayAds!!,
                "categoryFragment"
            )
        binding.recyclerBusinessList.setHasFixedSize(true)
        businessListCategoryRecyclerAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

    /*    admobNativeAdAdapter = AdmobNativeAdAdapter.Builder.with(
            NATIVE_AD_REAL, businessListCategoryRecyclerAdapter,
            "medium"
        ).adItemInterval(3).build()
*/
        binding.recyclerBusinessList.apply {
            layoutManager = LinearLayoutManager(context)
            val itemsDecoration = ItemDecoration(2)
            addItemDecoration(itemsDecoration)
        //    adapter =  admobNativeAdAdapter

        }


    }

    fun loadArtisan(lgaValue: String?) {

        mDatabase!!.orderByChild("sub_status")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    business.clear()
                    ratingArray.clear()
                    businessListCategoryRecyclerAdapter.notifyDataSetChanged()

                    for (datasnap in snapshot.children) {
                        val businessListData =
                            datasnap.child("advert")
                                .getValue(BusinessListData::class.java)
                        val contactData =
                            datasnap.child("contact")
                                .getValue(ContactData::class.java)
                        val ratingData =
                            datasnap.child("rating")
                                .getValue(RatingData::class.java)

                        try {
                            if (businessListData!!.profile == "false")
                                continue
                            if (businessListData!!.category == artisan_position && contactData!!.lga == lgaValue!!) {
                                business.add(businessListData)
                                ratingArray.add(ratingData!!)
                                business(business, ratingArray)
                            } else {
                                show("No $artisan_position is available at $lgaValue yet")
                                continue
                            }
                        } catch (ex: NullPointerException) {
                            ex.printStackTrace()
                        }

                        businessListCategoryRecyclerAdapter.notifyItemInserted(business.size)

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        businessListCategoryRecyclerAdapter =
            BusinessListCategoryRecyclerAdapter(
                business, ratingArray!!,
                "categoryFragment"
            )
        binding.recyclerBusinessList.adapter =
            businessListCategoryRecyclerAdapter
        binding.recyclerBusinessList.setHasFixedSize(true)
        businessListCategoryRecyclerAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        binding.recyclerBusinessList.apply {
            layoutManager = LinearLayoutManager(context)
            val itemsDecoration = ItemDecoration(2)
            addItemDecoration(itemsDecoration)


        }

    }

    fun toast(message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let { mActivity = it }
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

    private fun setUpToolbar() {
        val mainActivity = mActivity as MainActivity
        val navigationView: NavigationView? = mActivity.findViewById(R.id.nav_view)
        mainActivity.setSupportActionBar(binding.toolbar)
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration = mainActivity.appBarConfiguration
        NavigationUI.setupActionBarWithNavController(
            mainActivity,
            navController,
            appBarConfiguration!!
        )
        NavigationUI.setupWithNavController(navigationView!!, navController)

        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
    }

    private fun show(message: String?) {
        try {
            Toast.makeText(requireContext(), message!!, Toast.LENGTH_SHORT).show()
        } catch (ex: IllegalStateException) {
            //   Log.d("ArtException", ex.message.toString())
        }
    }






    override fun onDestroy() {
        binding.adViewCat.destroy()
        _binding = null
        
        super.onDestroy()

    }


    private fun checkNetwork(): Boolean {
        //  if (Build.VERSION.SDK_INT  <= Build.VERSION_CODES.Q) {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected = activeNetwork?.isConnectedOrConnecting == true

        return isConnected


    }


    private fun business(
        businessArray: ArrayList<BusinessListData>,
        rating: ArrayList<RatingData>
    ) {
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



}
