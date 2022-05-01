package com.tostech.artisan.AdapterClasses

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.kv.popupimageview.PopupImageView
import com.tostech.artisan.R
import com.tostech.artisan.ui.ImagePagerFragment
import com.tostech.artisan.ui.artisanlist.BusinessListFragment
import com.tostech.artisan.ui.category.CategoryFragmentDirections
import com.tostech.artisan.ui.home.HomeFragmentDirections
import kotlinx.android.synthetic.main.cat_list.view.*
import kotlinx.android.synthetic.main.image_card.view.*
import java.util.concurrent.atomic.AtomicBoolean


/**
 * A fragment for displaying a grid of images.
 */
class GridAdapter(val arrayImage: List<String>?, val arrayText: ArrayList<String>?, val userId: String?, val context: Context) : RecyclerView.Adapter<ImageViewHolder>() {

    override fun getItemCount(): Int {
        return arrayImage!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {

        val artisan = ImageViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.image_card,
                parent,
                false
            )
        )

        return artisan
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        Glide.with(context).load(arrayImage!![position]).into(holder.card_image)
        holder.carView.setOnClickListener { view ->
             val bundle = bundleOf("imageUrl" to arrayImage, "imageText" to arrayText, "userId" to userId, "position" to position)
             view.findNavController().navigate(R.id.action_homeFragment_to_imagePagerFragment, bundle)
           // PopupImageView(context, view, (arrayImage as ArrayList<String>),position).
        }
    }
}
 class ImageViewHolder(view: View): RecyclerView.ViewHolder(view){
     val card_image = view.card_image
     val adText = view.adText
     val carView = view.card_view
}
/*

class DetailedViewHolder(itemView:View): RecyclerView.ViewHolder(itemView) {
    constructor(parent: ViewGroup):
            this(LayoutInflater.from(parent.context).inflate(R.layout.cat_grid, parent, false))

    val blog_image: ImageView = itemView.findViewById(R.id.cat_item_image)
    val blog_title: TextView = itemView.findViewById(R.id.cat_item_title)

    init {
        itemView.setOnClickListener (object: View.OnClickListener{
            override fun onClick(v: View?) {
                val position = adapterPosition
                val bundle = Bundle()
                bundle.putString("artisan_position", itemView.cat_item_title.text.toString())

                //  Toast.makeText(itemView.context, "You Clicked item # ${itemView.name.text}", Toast.LENGTH_SHORT).show()
                val activity = v!!.context as AppCompatActivity
                val businessFragment = BusinessListFragment()
                businessFragment.arguments =  bundle
                val fm = activity.supportFragmentManager
                val ft = fm.beginTransaction()


                ft.replace(R.id.constraint_category, businessFragment, position.toString())
                    .addToBackStack(null).commit()


            }
        })
    }

}*/
