package com.tostech.artisan.AdapterClasses

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.tostech.artisan.BusinessListData
import com.tostech.artisan.GlideApp
import com.tostech.artisan.R
import com.tostech.artisan.ui.artisanlist.BusinessListFragment
import com.tostech.artisan.ui.home.HomeFragment
import de.hdodenhof.circleimageview.CircleImageView


class BusinessListRecyclerAdapter(options: FirebaseRecyclerOptions<BusinessListData>) :
    FirebaseRecyclerAdapter<BusinessListData, BusinessListRecyclerAdapter.BusinessListViewHolder>(
        options
    ) {


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BusinessListViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(
            R.layout.artisan_list,
            viewGroup,
            false
        )
        return BusinessListViewHolder(view)


    }

    override fun onBindViewHolder(
        holder: BusinessListViewHolder,
        position: Int,
        businessListData: BusinessListData
    ) {
        if (businessListData.profile == "false") {
            //  removeAt(position)

            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
//            notifyDataSetChanged()
        }
        var rat_number = 0.0f
        var ratScore = 0.0f
        if(businessListData.rat_number == "null"){
            rat_number = 0.0f
        }

        if(businessListData.rat_score == "null"){
            ratScore = 0.0f
        }
        try {
            rat_number = businessListData.rat_number.toFloat()
            ratScore = businessListData.rat_score.toFloat()
        }catch (ex: NumberFormatException){
            rat_number = 0.0f
            ratScore = 0.0f
        }

        val rating = ratScore/rat_number

        Log.v("Rating", rating.toString())
        Log.v("Rating", ratScore.toString())
        Log.v("Rating", rat_number.toString())


        holder.bus_name!!.setText(businessListData.bus_name)
        holder.uid!!.setText(businessListData.uid)
        holder.category!!.setText(businessListData.category)

        holder.ratingBar!!.rating = rating

        GlideApp.with(holder.profile_pix.context).load(businessListData.purl).into(holder.profile_pix)


        holder.itemView.setOnClickListener(object : View.OnClickListener {


            override fun onClick(v: View?) {
                val activity = v!!.context as FragmentActivity

                val itemPosition = getRef(position).key
                val bundle = Bundle()
                bundle.putString("signInID", itemPosition)


                Log.v("SignInAdapter", itemPosition!!)

                val homeFragment = HomeFragment()
                homeFragment.arguments = bundle
                activity.supportFragmentManager.beginTransaction().replace(
                    R.id.constraint_artisan_list,
                    homeFragment
                )
                    .addToBackStack(null).commit()

            }
        })

    }




    class BusinessListViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bus_name: TextView? = itemView.findViewById(R.id.name)
        val profile_pix: CircleImageView = itemView.findViewById(R.id.pix)
        val category: TextView? = itemView.findViewById(R.id.category)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratShow)
        val uid: TextView? = itemView.findViewById(R.id.txt_id)
    }

    /**
     * The [AdViewHolder] class.
     */


    fun removeAt(position: Int) {
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
        notifyDataSetChanged()
    }

}


class BusinessListRecyclerAdapter2(private val businessListData: List<BusinessListData>) :
                        RecyclerView.Adapter<BusinessListRecyclerAdapter2.ImageViewHolder>() {

    private val TAG: String = "AppDebug"

    private var items: List<BusinessListData> = ArrayList()


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            LayoutInflater.from(viewGroup.context).inflate(R.layout.artisan_list, viewGroup, false)
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        var rat_number = 0.0f
        var ratScore = 0.0f
        holder.bus_name.text = businessListData[position].bus_name
        holder.category.text = businessListData[position].category
        holder.uid.text = businessListData[position].uid

        try {
            val rat_number = businessListData[position].rat_number.toFloat()
            val ratScore = businessListData[position].rat_score.toFloat()
        }catch (ex: NumberFormatException){
            rat_number = 0.0f
            ratScore = 0.0f
        }

        val rating = ratScore/rat_number

        Log.v("Rating", rating.toString())

        holder.ratingBar.rating = rating


        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_baseline_person_24)
            .error(R.drawable.ic_baseline_person_24)

        Glide.with(holder.itemView.context)
            .applyDefaultRequestOptions(requestOptions)
            .load(businessListData[position].purl)
            .into(holder.profile_pix)

        holder.itemView.setOnClickListener(object : View.OnClickListener {


            override fun onClick(v: View?) {
                val activity = v!!.context as FragmentActivity

                val itemPosition = businessListData[position].uid
                val bundle = Bundle()
                bundle.putString("signInID", itemPosition)


                Log.v("SignInAdapter", itemPosition!!)

                val homeFragment = HomeFragment()
                homeFragment.arguments = bundle
                activity.supportFragmentManager.beginTransaction().replace(
                    R.id.constraint_artisan_list,
                    homeFragment
                )
                    .addToBackStack(null).commit()

            }
        })
    }

    override fun getItemCount(): Int {
        return businessListData.size
    }



    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bus_name: TextView = itemView.findViewById(R.id.name)
        val profile_pix: CircleImageView   = itemView.findViewById(R.id.pix)
        val category: TextView  = itemView.findViewById(R.id.category)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratShow)
        val uid: TextView = itemView.findViewById(R.id.txt_id)

    }
}
