package com.tostech.artisan.AdapterClasses


import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tostech.artisan.R
import kotlinx.android.synthetic.main.cat_list.view.*
import kotlinx.android.synthetic.main.image_card.view.*

class CategoryRecyclerAdapter(private var categoryText: Array<String>, /*private var categoryImages: TypedArray,*/
                              private var layoutManager: GridLayoutManager? = null)
    : RecyclerView.Adapter<CategoryRecyclerAdapter.CategoryViewHolder>() {

    enum class ViewType{
        GRID,
        LIST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {

        return when(viewType){
            ViewType.GRID.ordinal -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.cat_grid, parent, false)
                CategoryViewHolder(view, viewType)
            }
            else -> {
               val view = LayoutInflater.from(parent.context).inflate(R.layout.cat_list, parent, false)
                CategoryViewHolder(view, viewType)
            }
        }

    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int){
        val category_text = categoryText[position]
       // val category_image = categoryImages.getDrawable(position)
        holder.blog_title!!.text = category_text
       /* if(getItemViewType(position) == ViewType.GRID.ordinal)
               Glide.with(holder.itemView.context).load(category_image).into(holder.blog_image_grid!!)
        else
            Glide.with(holder.itemView.context).load(category_image).into(holder.blog_image_list!!)
*/
    }

    override fun getItemCount(): Int {
        return categoryText.size
    }

    override fun getItemViewType(position: Int): Int {
        val spanCount = layoutManager?.spanCount

        return if (spanCount == 2) ViewType.GRID.ordinal
        else ViewType.LIST.ordinal
    }

    inner class CategoryViewHolder constructor(
        itemView: View, viewType: Int
    ) : RecyclerView.ViewHolder(itemView) {

       // var blog_image_list: ImageView? = null
        var blog_title: TextView? = null
        //var blog_image_grid: ImageView? = null

        init {
            if (viewType == ViewType.GRID.ordinal) {
           //     blog_image_grid = itemView.findViewById(R.id.cat_item_image_grid)
                blog_title = itemView.findViewById(R.id.cat_item_title)
            }else{
             //   blog_image_list = itemView.findViewById(R.id.cat_item_image_list)
                blog_title = itemView.findViewById(R.id.cat_item_title)
            }

            itemView.setOnClickListener { v ->
            //    val artisanType = itemView.cat_item_title.text.toString()
               val artisanType = itemView.cat_item_title.text.toString()

                val bundle = bundleOf("artisan_position" to artisanType)

                v.findNavController().navigate(R.id.action_nav_category_to_busCatFragment, bundle)
            }
        }
    }
}
