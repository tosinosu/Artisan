package com.tostech.artisan.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.tostech.artisan.R
import com.tostech.artisan.ViewFullImage
import com.tostech.artisan.data.Chat
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter (
    mContext: Context,
    mChatList: List<Chat>,
    imageUrl: String

): RecyclerView.Adapter<ChatAdapter.ViewHolder?>(){

    private val mContext: Context
    private val mChatList: List<Chat>
    private val imageUrl: String
    var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser!!

    init {
        this.mChatList = mChatList
        this.mContext = mContext
        this.imageUrl = imageUrl
    }


    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        //position 1 message item left 0 messafe item 0
        return if (position == 1){
            val view = LayoutInflater.from(mContext).inflate(R.layout.message_item_right, parent, false)

            ViewHolder(view)
        }else{
            val view = LayoutInflater.from(mContext).inflate(R.layout.message_item_left, parent, false)

            ViewHolder(view)
        }
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat: Chat = mChatList[position]

        //images messages
        if ((chat.message).equals("sent you an image") && !chat.url.equals("")) {

            Glide.with(mContext).load(imageUrl).into(holder.profileImage!!)

            //image message right side
            if (chat.sender.equals(firebaseUser!!.uid)) {
                holder.show_text_message!!.visibility = View.GONE
                holder.right_image_message!!.visibility = View.VISIBLE
                Glide.with(mContext).load(chat.url).into(holder.right_image_message!!)
                holder.right_image_message!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Full Image",
                        "Delete Image",
                        "Cancel"
                    )
                    var builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Delete Image")
                    builder.setItems(
                        options,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            if (i == 0) {
                                val intent = Intent(mContext, ViewFullImage::class.java)
                                intent.putExtra("uri", chat.url)
                                mContext.startActivity(intent)
                                //finish()
                            } else if (i == 1) {
                                deleteSentMessage(position, holder)
                            }
                        })
                    builder.show()
                }
            }

            //image message left side
            else if (!chat.sender.equals(firebaseUser!!.uid)) {
                holder.show_text_message!!.visibility = View.GONE
                holder.left_image_message!!.visibility = View.VISIBLE
                Glide.with(mContext).load(chat.url).into(holder.left_image_message!!)

                holder.left_image_message!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "View Full Image",
                        "Cancel"
                    )
                    val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Delete Image")
                    builder.setItems(
                        options,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            if (i == 0) {
                                val intent = Intent(mContext, ViewFullImage::class.java)
                                intent.putExtra("uri", chat.url)
                                mContext.startActivity(intent)
                                //finish()
                            }
                        })
                    builder.show()

                }
            }
        }

        //text messages
        else {
            holder.show_text_message!!.text = chat.message

            if (firebaseUser!!.uid == chat.sender) {
                holder.show_text_message!!.setOnClickListener {
                    val options = arrayOf<CharSequence>(
                        "Delete Message",
                        "Cancel"
                    )
                    val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Delete Message")
                    builder.setItems(
                        options,
                        DialogInterface.OnClickListener { dialogInterface, i ->
                            if (i == 0) {

                                deleteSentMessage(position, holder)
                            }
                        })
                    builder.show()
                }
            }
        }
        //sent and seen message
        if(position == mChatList.size - 1){
            if(chat.isseen){

                holder.text_seen!!.text = "Seen"

                if ((chat.message).equals("sent you an image") && !chat.url.equals("")) {
                    val lp: RelativeLayout.LayoutParams? = holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?

                    lp!!.setMargins(0, 245, 10, 0)
                    holder.text_seen!!.layoutParams = lp
                }
            }else{

                holder.text_seen!!.text = "Sent"

                if ((chat.message).equals("sent you an image") && !chat.url.equals("")) {
                    val lp: RelativeLayout.LayoutParams? = holder.text_seen!!.layoutParams as RelativeLayout.LayoutParams?

                    lp!!.setMargins(0, 245, 10, 0)
                    holder.text_seen!!.layoutParams = lp
                }
            }
        }else{
            holder.text_seen!!.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    inner class ViewHolder(itemview: View): RecyclerView.ViewHolder(itemview){
        var profileImage: CircleImageView? = null
        var show_text_message: TextView? = null
        var left_image_message: ImageView? = null
        var right_image_message: ImageView? = null
        var text_seen: TextView? = null

        init {
            profileImage = itemview.findViewById(R.id.profile_image)
            show_text_message = itemview.findViewById(R.id.show_text_message)
            /*left_image_message = itemview.findViewById(R.id.left_image_view)*/
         //   right_image_message = itemview.findViewById(R.id.right_image_view)
            text_seen = itemview.findViewById(R.id.text_seen)

        }

    }

    override fun getItemViewType(position: Int): Int {
       // return super.getItemViewType(position)

     //   firebaseUser = FirebaseAuth.getInstance().currentUser

        return if (mChatList[position].sender.equals(firebaseUser!!.uid)){
            1
        }else{
            0
        }
    }
    private fun deleteSentMessage(position: Int, holder: ViewHolder){
        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(mChatList.get(position).messageID!!)
            .removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(holder.itemView.context, "Message Deleted", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(holder.itemView.context, "Message Not Deleted", Toast.LENGTH_SHORT).show()
                }
            }
    }


}