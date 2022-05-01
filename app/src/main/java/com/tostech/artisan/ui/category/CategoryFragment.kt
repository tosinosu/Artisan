package com.tostech.artisan.ui.category


import android.content.Context
import android.content.res.TypedArray
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import com.reddit.indicatorfastscroll.FastScrollerThumbView
import com.reddit.indicatorfastscroll.FastScrollerView
import com.tostech.artisan.AdapterClasses.CategoryRecyclerAdapter
//import com.tostech.artisan.ListItem
import com.tostech.artisan.MainActivity
import com.tostech.artisan.R
import com.tostech.artisan.databinding.FragmentCategoryBinding


class CategoryFragment : Fragment(){

    private lateinit var binding: FragmentCategoryBinding
    private lateinit var categoryRecyclerAdapter: CategoryRecyclerAdapter
    lateinit var mActivity: FragmentActivity
    private lateinit var fastScrollerView: FastScrollerView
    private lateinit var fastScrollerThumbView: FastScrollerThumbView
    private var layoutManager: GridLayoutManager? =null
    private lateinit var mDatabase: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_category, container, false)

        setUpToolbar()
        initRecyclerView()

        if (!checkNetwork()) {
            show("No network connection")
        }


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

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let { mActivity = it}
    }

    private fun initRecyclerView(){
        val data1 = resources.getStringArray(R.array.artisanList)
        // categoryImages = resources.obtainTypedArray(R.array.artisanDrawables)

        val recyclerCategory = binding.recycleCategory
        val fastScrollerView = binding.fastScroller
        val linearLayoutManager = LinearLayoutManager(context)
        layoutManager = GridLayoutManager(context, 2)


      //  recyclerCategory.apply {

            //val itemsDecoration = ItemDecoration(10)
            //addItemDecoration(itemsDecoration)
            recyclerCategory.layoutManager = layoutManager

            categoryRecyclerAdapter = CategoryRecyclerAdapter(data1, /*categoryImages!!,*/ layoutManager)
            recyclerCategory.adapter = categoryRecyclerAdapter
        categoryRecyclerAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

        //  }
        fastScrollerView.apply {
            setupWithRecyclerView(
                recyclerCategory,
                { position ->
                    val item = data1[position]
                    FastScrollItemIndicator.Text(
                        item
                            //.title
                            .substring(0, 1)
                            .toUpperCase()
                    )}
            )}


        fastScrollerThumbView = binding.sampleBasicFastscrollerThumb
        fastScrollerThumbView.apply {
            setupWithFastScroller(fastScrollerView)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
   //     categoryImages!!.recycle()

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.category_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

   // @SuppressLint("UseCompatLoadingForDrawables")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         when (item?.itemId){
             R.id.menu_switch_layout -> {
                 if(layoutManager?.spanCount == 1){
                     layoutManager?.spanCount = 2
                     item.icon = requireContext().getDrawable(R.drawable.ic_baseline_list_24)
                   //  return true
                 }else{
                     layoutManager?.spanCount = 1

                     item.icon = requireContext().getDrawable(R.drawable.ic_baseline_grid_on_24)
                     //return true
                 }
                 categoryRecyclerAdapter.notifyItemRangeChanged(0, categoryRecyclerAdapter.itemCount ?:0)
             }
         }
      return super.onOptionsItemSelected(item)
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

    private fun checkNetwork(): Boolean{
        //  if (Build.VERSION.SDK_INT  <= Build.VERSION_CODES.Q) {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected = activeNetwork?.isConnectedOrConnecting == true

        return isConnected
    }

    private fun show(message: String?){
        try {
            Toast.makeText(requireContext(), message!!, Toast.LENGTH_SHORT).show()
        }catch (ex: IllegalStateException){
            // Log.d("ArtException", ex.message.toString())
        }
    }
}