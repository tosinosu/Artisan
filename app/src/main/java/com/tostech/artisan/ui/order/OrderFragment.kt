package com.tostech.artisan.ui.order

import android.content.Context
import android.os.Bundle
//import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.tostech.artisan.AdapterClasses.OrderAdapter
import com.tostech.artisan.MainActivity
import com.tostech.artisan.R
import com.tostech.artisan.SimpleDividerItemDecoration
import com.tostech.artisan.SwipeToDelete
import com.tostech.artisan.data.Order
import com.tostech.artisan.databinding.FragmentOrderBinding

class OrderFragment : Fragment() {

    private lateinit var binding: FragmentOrderBinding
    private lateinit var orderAdapter: OrderAdapter
    lateinit var mActivity: FragmentActivity
    var order = ArrayList<Order>()
    var firebaseUserID: String = ""
    var orderData = Order()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order, container, false)


        firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid


        val reference = FirebaseDatabase.getInstance().reference.child("User").child(firebaseUserID)
            .child("order")
        order = ArrayList()
        val options: FirebaseRecyclerOptions<Order> =
            FirebaseRecyclerOptions.Builder<Order>()
                .setQuery(reference, object : SnapshotParser<Order> {

                    override fun parseSnapshot(snapshot: DataSnapshot): Order {

                        orderData = Order(
                            snapshot.child("uid").value.toString(),
                            snapshot.child("username").value.toString(),
                            snapshot.child("status").value.toString(),
                            snapshot.child("purl").value.toString()
                        )

//                        Log.v("orderis", snapshot.child("purl").value.toString())

                        return orderData
                    }

                })
                .build()

        binding.recyclerOrder.apply {
            layoutManager = LinearLayoutManager(context)
            val simpleDividerItemDecoration = SimpleDividerItemDecoration(context)
            addItemDecoration(simpleDividerItemDecoration)
            orderAdapter = OrderAdapter(options)
            adapter = orderAdapter
        }
  //      Log.v("orderid", orderData.uid)

        val swipeToDeleteCallback =
            object :
                SwipeToDelete(requireContext(), 0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.bindingAdapterPosition
                    val orderID = orderAdapter.getRef(position).key

                    val reference =
                        FirebaseDatabase.getInstance().reference.child("User").child(firebaseUserID)

                    if (direction == ItemTouchHelper.LEFT) {

    //                    Log.v("orderid", orderID!!)

                        if (!orderID.isNullOrEmpty()) {
                            val pushValue = reference.child("accepted_order")
                            pushValue.child(firebaseUserID).child("id").setValue(orderID)
                            pushValue.child(firebaseUserID).child("rated").setValue(0)
                            pushValue.child(firebaseUserID).child("commented").setValue(0)
                            orderAdapter.getRef(position).removeValue()
                        }
                        //orderAdapter.notifyDataSetChanged()

                        //     orderAdapter.undoView(viewHolder.adapterPosition)
                    } else {
                        orderAdapter.getRef(position).removeValue()
                        orderAdapter.notifyDataSetChanged()
//                        orderAdapter.undoView(viewHolder.adapterPosition)
                    }
                }

            }

        //configure left swipe
        swipeToDeleteCallback.leftBG = ContextCompat.getColor(requireContext(), R.color.colorGreen)
        swipeToDeleteCallback.leftLabel = "Accept Order"
        swipeToDeleteCallback.leftIcon =
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_check_circle_24)

        //configure right swipe
        swipeToDeleteCallback.rightBG = ContextCompat.getColor(requireContext(), R.color.colorError)
        swipeToDeleteCallback.rightLabel = "Reject Order"
        swipeToDeleteCallback.rightIcon =
            AppCompatResources.getDrawable(requireContext(), R.drawable.ic_baseline_delete_sweep_24)

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)

        itemTouchHelper.attachToRecyclerView(binding.recyclerOrder)


        return binding.root
    }

    fun show(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        orderAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        orderAdapter.stopListening()
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
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let { mActivity = it}
    }

}