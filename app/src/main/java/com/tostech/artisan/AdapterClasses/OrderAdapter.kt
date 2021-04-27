package com.tostech.artisan.AdapterClasses

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.tostech.artisan.*
import com.tostech.artisan.R
import com.tostech.artisan.data.Order
import de.hdodenhof.circleimageview.CircleImageView

class OrderAdapter(options: FirebaseRecyclerOptions<Order>) :  FirebaseRecyclerAdapter<Order, OrderAdapter.ViewHolder>(
    options
) {

    var handler: Handler? = Handler()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        orderData: Order
    ) {
        holder.bus_name!!.setText(orderData.username)
        holder.uid!!.setText(orderData.uid)
        GlideApp.with(holder.pix.context).load(orderData.purl).into(holder.pix)


    }

    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bus_name: TextView = itemView.findViewById(R.id.txtOrder)
        val pix: CircleImageView = itemView.findViewById(R.id.imgOrder)
        val uid: TextView = itemView.findViewById(R.id.idOrder)

    }

    fun undoView(position: Int) {
        handler?.postDelayed({
            notifyItemChanged(position)
        }, 1000)
    }

    /*fun removeView(position: Int, reference: DatabaseReference){
      //  val object1 = orderAdapter
      //  Log.v("orderid", orderData.uid)



        reference.orderByChild("uid").equalTo(orderData.uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hashMap = HashMap<String, Any>()
                hashMap["order_list"] = orderData!!.uid

                for (datasnap in snapshot.children){
                    reference2.child(orderData!!.uid).child("order_list").updateChildren(hashMap)

                    datasnap.ref.removeValue()

                }


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }*/
}