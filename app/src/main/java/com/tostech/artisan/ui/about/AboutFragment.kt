package com.tostech.artisan.ui.about

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.gms.ads.AdRequest
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tostech.artisan.MainActivity
import com.tostech.artisan.R
import com.tostech.artisan.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private lateinit var binding: FragmentAboutBinding
    lateinit var mActivity: FragmentActivity
    private lateinit var mDatabase: DatabaseReference

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false)

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
                        binding.adView.loadAd(adRequest)
                    }else{
                        binding.adView.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        return binding.root
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let { mActivity = it}
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
        binding.adView.pause()
        super.onPause()
    }

    // Called when returning to the activity
    public override fun onResume() {
        super.onResume()
        binding.adView.resume()
    }

    // Called before the activity is destroyed
    public override fun onDestroy() {
        binding.adView.destroy()
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