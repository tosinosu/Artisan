package com.tostech.artisan.AdapterClasses

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.tostech.artisan.R
import com.tostech.artisan.ui.ImagePagerFragment
import com.tostech.artisan.ui.artisanlist.BusinessListFragment
import kotlinx.android.synthetic.main.cat_list.view.*
import kotlinx.android.synthetic.main.image_card.view.*
import java.util.concurrent.atomic.AtomicBoolean


/**
 * A fragment for displaying a grid of images.
 */
class GridAdapter(val arrayImage: ArrayList<String>?, val arrayText: ArrayList<String>?, val userId: String?, val context: Context) : RecyclerView.Adapter<ImageViewHolder>() {

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
        //holder.adText.text = arrayText!![position]

        holder.carView.setOnClickListener {

            val bundle = bundleOf("imageUrl" to arrayImage, "imageText" to arrayText, "userId" to userId, "position" to position)


            //  Toast.makeText(itemView.context, "You Clicked item # ${itemView.name.text}", Toast.LENGTH_SHORT).show()
            val activity = context as AppCompatActivity
         //   val imagePagerFragment = ImagePagerFragment()

            val fm = activity.supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<ImagePagerFragment>(R.id.constraint_main, args = bundle)
                    .addToBackStack(null)

            }
        }
    }


}
 class ImageViewHolder(view: View): RecyclerView.ViewHolder(view){

     val card_image = view.card_image
     val adText = view.adText
     val carView = view.card_view

/*     init {
         view.setOnClickListener { v ->

         }
     }*/

}