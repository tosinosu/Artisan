package com.tostech.artisan.AdapterClasses

import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.tostech.artisan.GlideApp
import com.tostech.artisan.R
import com.tostech.artisan.data.CommentData
import com.tostech.artisan.data.DeleteData
import com.tostech.artisan.data.Order
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter (options: FirebaseRecyclerOptions<String>) :  FirebaseRecyclerAdapter<String, CommentAdapter.ViewHolder>(
    options
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        commentData: String
    ) {
        holder.comment!!.text = commentData

    }

    class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val comment: TextView = itemView.findViewById(R.id.comment)

    }


}