package com.tostech.artisan.AdapterClasses


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.tostech.artisan.R
import com.tostech.artisan.ui.artisanlist.BusinessListFragment
import kotlinx.android.synthetic.main.cat_list.view.*


class CategoryRecyclerAdapter(private var categoryList: List<String>)  : RecyclerView.Adapter<CategoryRecyclerAdapter.CategoryViewHolder>() {

    private val TAG: String = "AppDebug"

   // private var items: List<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val itemview =  LayoutInflater.from(parent.context).inflate(R.layout.cat_list, parent, false)
        return CategoryViewHolder(itemview)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val cat = categoryList[position]
        holder.blog_title.text = cat
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }


   /* fun submitList(imageList: List<String>) {
        categoryList = imageList
    }*/

    inner class CategoryViewHolder constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val blog_image:ImageView = itemView.findViewById(R.id.cat_item_image)
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


        /*fun bind(imageData: String) {
            val requestOptions = RequestOptions()
                .placeholder(R.drawable.passport1_background)
                .error(R.drawable.passport1_background)

            Glide.with(itemView.context)
                .applyDefaultRequestOptions(requestOptions)

                .load(imageData.image)
                .into(blog_image)
                 blog_title.setText(imageData.title)

        }*/


    }
}

