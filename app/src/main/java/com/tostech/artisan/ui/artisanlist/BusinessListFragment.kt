package com.tostech.artisan.ui.artisanlist

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.database.*
import com.tostech.artisan.*
import com.tostech.artisan.AdapterClasses.BusinessListRecyclerAdapter
import com.tostech.artisan.AdapterClasses.BusinessListRecyclerAdapter2
import com.tostech.artisan.R
import com.tostech.artisan.data.ContactData
import com.tostech.artisan.databinding.FragmentArtisanListBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

class BusinessListFragment : Fragment(){


    private lateinit var businessListViewModel: BusinessListViewModel
    private lateinit var businessListRecyclerAdapter: BusinessListRecyclerAdapter
    private lateinit var businessListRecyclerAdapter2: BusinessListRecyclerAdapter2
    lateinit var mDatabase: DatabaseReference

    private lateinit var binding: FragmentArtisanListBinding
    var business = ArrayList<BusinessListData>()

    var artisan_position: String? = ""
    var lgaValue  = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        businessListViewModel =
            ViewModelProviders.of(this).get(BusinessListViewModel::class.java)
        //   val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        // val textView: TextView = root.findViewById(R.id.text_gallery)
        businessListViewModel.text.observe(viewLifecycleOwner, Observer {
            //   textView.text = it
        })
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_artisan_list, container, false)

        mDatabase = FirebaseDatabase.getInstance().reference.child("User")

        artisan_position = arguments?.getString("artisan_position")

   //     val reference = FirebaseDatabase.getInstance().reference.child("User")

             business = ArrayList()



        binding.spinner3.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                lgaValue = p0!!.getItemAtPosition(p2).toString()

                if(!artisan_position.isNullOrEmpty()) {
                    mDatabase!!.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            business.clear()

                            for (datasnap in snapshot.children) {
                                val businessListData =
                                    datasnap.child("advert").getValue(BusinessListData::class.java)
                                val contactData =
                                    datasnap.child("contact").getValue(ContactData::class.java)

                                Log.v(
                                    "busData",
                                    businessListData!!.category + " ." + artisan_position
                                )

                                if (businessListData!!.profile == "false")
                                    continue
                                if (businessListData!!.category == artisan_position && contactData!!.lga == lgaValue) {


                                    business.add(businessListData)



                                }
                                else {

                                    show("No $artisan_position is available at $lgaValue yet")


                                }
                            }

                            binding.recyclerBusinessList.apply {
                                layoutManager = LinearLayoutManager(context)
                                val itemsDecoration = ItemDecoration(5)
                                addItemDecoration(itemsDecoration)
                                businessListRecyclerAdapter2 =
                                    BusinessListRecyclerAdapter2(business)
                                binding.recyclerBusinessList.adapter =
                                    businessListRecyclerAdapter2
                                binding.recyclerBusinessList.setHasFixedSize(true)
                                businessListRecyclerAdapter2.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
                                    }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        })
        if(artisan_position.isNullOrEmpty()){
            binding.spinner.visibility = View.GONE
            binding.spinner2.visibility = View.GONE
            binding.spinner3.visibility = View.GONE

            val options: FirebaseRecyclerOptions<BusinessListData> =
                FirebaseRecyclerOptions.Builder<BusinessListData>()
                    .setQuery(mDatabase, object : SnapshotParser<BusinessListData> {

                        override fun parseSnapshot(snapshot: DataSnapshot): BusinessListData {

                            val busData = BusinessListData(
                                snapshot.child("advert/uid").value.toString(),
                                snapshot.child("advert/purl").value.toString(),
                                snapshot.child("advert/bus_name").value.toString(),
                                snapshot.child("rating/rat_number").value.toString(),
                                snapshot.child("rating/rat_score").value.toString(),
                                snapshot.child("advert/profile").value.toString(),
                                snapshot.child("advert/category").value.toString()
                            )

                            Log.v("ratingbar", snapshot.child("rating/rat_number").value.toString())
                            Log.v("ratingbar", snapshot.child("rating/rat_score").value.toString())

                            return busData
                        }

                    })
                    .build()

        binding.recyclerBusinessList.apply {
            layoutManager = LinearLayoutManager(context)
            val itemsDecoration = ItemDecoration(5)
            addItemDecoration(itemsDecoration)
            businessListRecyclerAdapter = BusinessListRecyclerAdapter(options)
            binding.recyclerBusinessList.adapter = businessListRecyclerAdapter
            binding.recyclerBusinessList.setHasFixedSize(true)
            businessListRecyclerAdapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY


        }
}
        container!!.removeAllViews()

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

     //   var stateID: String

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            var stateList: ArrayList<String> = ArrayList()

            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

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

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Code to perform some action when nothing is selected
            }
        }

        binding.spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            val LGAList: ArrayList<String> = ArrayList()

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {

                val selectedState = binding.spinner2.selectedItem.toString()

                LGAList.clear()

                try {
                    // As we have JSON object, so we are getting the object
                    //Here we are calling a Method which is returning the JSON object
                    val obj = JSONObject(getJsonFromAsset(requireContext(), "cities.json")!!)
                    val objState = JSONObject(getJsonFromAsset(requireContext(), "states.json")!!)
                    // fetch JSONArray named users by using getJSONArray
                    val statesArray = objState.getJSONArray("states")
                    val citiesArray = obj.getJSONArray("cities")
                    // Get the users data using for loop i.e. id, name, email and so on

                    //loop through states
                    for(j in 0 until statesArray.length()) {
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

                binding.spinner3.setSelection(5)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }
        return binding.root
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

    override fun onStart() {
        super.onStart()
      //  Log.v("artpos", artisan_position.toString())
        if (artisan_position.isNullOrEmpty()) {
        //    Log.v("artpos", "listenining")

            businessListRecyclerAdapter!!.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        if (artisan_position.isNullOrEmpty())
        businessListRecyclerAdapter!!.stopListening()
    }

    private fun show(message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }


}