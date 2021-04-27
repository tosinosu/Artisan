package com.tostech.artisan.AdapterClasses

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.tostech.artisan.R
import com.tostech.artisan.data.SearchData
import com.tostech.artisan.ui.home.HomeFragment
import de.hdodenhof.circleimageview.CircleImageView


class SearchRecyclerAdapter(options: FirebaseRecyclerOptions<SearchData>) : FirebaseRecyclerAdapter<SearchData, SearchViewHolder>(
    options
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        return SearchViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.artisan_search, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int, searchData: SearchData) {
        holder.business_name.setText(searchData.bus_name)
        holder.category.setText(searchData.category)
        holder.txt_id.setText(searchData.uid)

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_baseline_person_24)
            .error(R.drawable.ic_baseline_person_24)

        Glide.with(holder.itemView.context)
            .applyDefaultRequestOptions(requestOptions)
            .load(searchData.purl)
            .into(holder.search_pix)

/*
            holder.itemView.setOnClickListener(object: View.OnClickListener{


                override fun onClick(v: View?) {
                    val activity = v!!.context as FragmentActivity

                    val itemPosition = searchData[position].uid
                    val bundle = Bundle()
                    bundle.putString("signInID", itemPosition)


                    Log.v("SignInAdapter", itemPosition!!)

                    val homeFragment = HomeFragment()
                    homeFragment.arguments =  bundle
                    activity.supportFragmentManager.beginTransaction().replace(R.id.constraint_artisan_list, homeFragment)
                        .addToBackStack(null).commit()

                }
            })*/
    }
}



      class SearchViewHolder constructor(itemView: View): RecyclerView.ViewHolder(itemView){
          val business_name: TextView = itemView.findViewById(R.id.name)
          val search_pix: CircleImageView   = itemView.findViewById(R.id.pix)
          val category: TextView  = itemView.findViewById(R.id.category)
          val txt_id: TextView = itemView.findViewById(R.id.txt_id)

      }

/*

firebaseRecyclerAdapter =
object : FirebaseRecyclerAdapter<SearchDa, SearchViewHolder>(options) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewtype: Int
    ): SearchViewHolder {
        return SearchViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.search_list, parent, false)
        )
    }

    override fun onBindViewHolder(
        searchViewHolder: SearchViewHolder,
        position: Int
    ) {

        searchViewHolder.bus_name.text = searchData[position].bus_name
        searchViewHolder.category.text = searchData[position].category
        searchViewHolder.uid.text = searchData[position].uid

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_baseline_person_24)
            .error(R.drawable.ic_baseline_person_24)

        Glide.with(holder.itemView.context)
            .applyDefaultRequestOptions(requestOptions)
            .load(businessListData[position].purl)
            .into(holder.profile_pix)

    }

}*/

class SearchRecyclerAdapter2(search: ArrayList<SearchData>): RecyclerView.Adapter<SearchRecyclerAdapter2.SearchViewHolder2>() {
    var search: ArrayList<SearchData>? = null

    init{
        this.search = search
    }

    class SearchViewHolder2 constructor(itemView: View): RecyclerView.ViewHolder(itemView){
        var business_name: TextView? = null
        var search_pix: CircleImageView? = null
        var category: TextView? = null
        var txt_id: TextView? = null


        init {
             business_name = itemView.findViewById(R.id.name)
             search_pix = itemView.findViewById(R.id.pix)
             category = itemView.findViewById(R.id.category)
             txt_id = itemView.findViewById(R.id.txt_id)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): SearchViewHolder2 {
        return SearchViewHolder2(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.artisan_search, parent, false)
        )
    }

    override fun onBindViewHolder(searchViewHolder: SearchViewHolder2, position: Int) {
        searchViewHolder.business_name!!.setText(search!!.get(position).bus_name)
        searchViewHolder.category!!.setText(search!!.get(position).category)
        searchViewHolder.txt_id!!.setText(search!!.get(position).uid)

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.ic_baseline_person_24)
            .error(R.drawable.ic_baseline_person_24)

        searchViewHolder.search_pix?.let {
            Glide.with(searchViewHolder.itemView.context)
                .applyDefaultRequestOptions(requestOptions)
                .load(search!!.get(position).purl)
                .into(it)
        }

        searchViewHolder.itemView.setOnClickListener(object: View.OnClickListener{


            override fun onClick(v: View?) {
                val activity = v!!.context as FragmentActivity

                val itemPosition = search!!.get(position).uid
                val bundle = Bundle()
                bundle.putString("signInID", itemPosition)


                Log.v("SignInAdapter", itemPosition!!)

                val homeFragment = HomeFragment()
                homeFragment.arguments =  bundle
                activity.supportFragmentManager.beginTransaction().add(R.id.actSearch, homeFragment)
                    .addToBackStack(null).commit()

            }
        })

    }

    override fun getItemCount(): Int {
        return search!!.size
    }
}