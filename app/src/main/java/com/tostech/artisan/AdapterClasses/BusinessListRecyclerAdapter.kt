package com.tostech.artisan.AdapterClasses

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.tostech.artisan.R
import com.tostech.artisan.data.BusinessListData
import com.tostech.artisan.data.RatingData
import de.hdodenhof.circleimageview.CircleImageView


class BusinessListCategoryRecyclerAdapter(
    private val businessListData: List<BusinessListData>, private val ratingData: List<RatingData>,
    private val fragmentName: String
) :
                        RecyclerView.Adapter<BusinessListCategoryRecyclerAdapter.ImageViewHolder>() {

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
        val firebaseUserID = Firebase.auth.currentUser!!.uid


        try {
             rat_number = ratingData[position].rat_number!!.toFloat()
             ratScore = ratingData[position].rat_score!!.toFloat()
        }catch (ex: NumberFormatException){
            rat_number = 0.0f
            ratScore = 0.0f
        }

        val rating = ratScore/rat_number

       // Log.v("Business UID is", businessListData[position].uid.toString())

      //  Log.v("Firebase UID is", firebaseUserID)

        if (businessListData[position].uid.toString() == firebaseUserID){
            holder.ratingBar.isVisible = false
        }else
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
                //  val activity = v!!.context as FragmentActivity

                val itemPosition = businessListData[position].uid
                val bundle = bundleOf("signInID" to itemPosition, "category" to "categoryFragment")

                Log.v("SignInAdapter", itemPosition!!)

                v!!.findNavController().navigate(R.id.action_busCatFragment_to_homeFragment, bundle)

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

class BusinessListRecyclerAdapter2(
    val businessListData: ArrayList<BusinessListData>,
    val ratingData: ArrayList<RatingData>,
    private val fragmentName: String
) :
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
        val firebaseUserID = Firebase.auth.currentUser!!.uid

        try {
             rat_number = ratingData[position].rat_number!!.toFloat()
             ratScore = ratingData[position].rat_score!!.toFloat()
        }catch (ex: NumberFormatException){
            Log.d("ratingscorerec", ex.message.toString())

            rat_number = 0.0f
            ratScore = 0.0f
        }

        val rating = ratScore/rat_number

        Log.v("Rating", rating.toString())

        if (businessListData[position].uid.toString() == firebaseUserID)
            holder.ratingBar.isVisible = false
        else
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

                val itemPosition = businessListData[position].uid
                val bundle = bundleOf("signInID" to itemPosition, "fragment" to fragmentName)
                //    bundle.putString("signInID", itemPosition)



                //  val action = BusinessListFragmentDirections.actionNavHomeToHomeFragment()
                v!!.findNavController().navigate(R.id.action_nav_home_to_homeFragment, bundle)
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

