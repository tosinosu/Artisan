package com.tostech.artisan.AdapterClasses


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tostech.artisan.R
import com.tostech.artisan.data.HomeData


class ImageViewRecyclerAdapter (val context:Context, val arrayList: ArrayList<HomeData>): RecyclerView.Adapter<ImageViewRecyclerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imaageView = itemView.findViewById<ImageView>(R.id.pix)
        val description = itemView.findViewById<TextView>(R.id.name)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.pix_list,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {

        holder.description.text = arrayList.get(position).advertText
        Glide.with(holder.itemView).load(arrayList.get(position).advertImageUrl)
            .apply(RequestOptions().placeholder(R.drawable.ic_baseline_person_24))
            .into(holder.imaageView)


    }
}
