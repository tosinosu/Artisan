package com.tostech.artisan.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tostech.artisan.MessageChatActivity
import com.tostech.artisan.R
import com.tostech.artisan.data.AdvertData
import com.tostech.artisan.data.Chat
import de.hdodenhof.circleimageview.CircleImageView

class AdvertAdapter (

    mContext: Context,
    mUser: List<AdvertData>,
    isChatCheck: Boolean,

    ): RecyclerView.Adapter<AdvertAdapter.ViewHolder?>() {

    private val mContext: Context
    private val mUser: List<AdvertData>
    private val isChatCheck: Boolean
    var lastMsg: String = ""

    init {
        this.mContext =mContext
        this.mUser = mUser
        this.isChatCheck = isChatCheck
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val view = LayoutInflater.from(mContext).inflate(R.layout.user_search, parent, false)
        return AdvertAdapter.ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = mUser[position]
        val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid

        if (user.uid.equals(firebaseUser)) {
            holder.busNameTxt.visibility = View.GONE
            holder.lastMessageTxt.visibility = View.GONE
            holder.offlineImageView.visibility = View.GONE
            holder.profileImageView.visibility = View.GONE
            holder.onlineImageView.visibility = View.GONE
        } else {

            holder.busNameTxt.text = user!!.bus_name
           // holder.lastMessageTxt = user!!.
            Glide.with(mContext).load(user.purl).placeholder(R.drawable.ic_baseline_person_24)
                .into(holder.profileImageView)

            holder.itemView.setOnClickListener {
                val options = arrayOf<CharSequence>(
                    "Message",
                    "Visit Profile"

                )
                val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
                builder.setTitle("What do you want?")
                builder.setItems(options, DialogInterface.OnClickListener { dialog, position ->
                    if (position == 0) {
                        val intent = Intent(mContext, MessageChatActivity::class.java)
                        intent.putExtra("visit_id", user.uid)
                        intent.putExtra("busName", user.bus_name)
                        mContext.startActivity(intent)
                    }
                    if (position == 1) {
                       // val intent = Intent(mContext, HomeFragment::class.java)
                        //intent.putExtra("visit_id", user.uid)
                        //mContext.startActivity(intent)
                    }

                })

                builder.show()
            }

            if (isChatCheck) {
                retrieveLasMessage(user.uid, holder.lastMessageTxt)
            } else {
                holder.lastMessageTxt.visibility = View.GONE
            }
            if (isChatCheck) {
                if (user.status == "online") {
                    holder.onlineImageView.visibility = View.VISIBLE
                    holder.offlineImageView.visibility = View.GONE
                } else {
                    holder.onlineImageView.visibility = View.GONE
                    holder.offlineImageView.visibility = View.VISIBLE
                }
            } else {
                holder.onlineImageView.visibility = View.GONE
                holder.offlineImageView.visibility = View.GONE

            }
        }
    }

    override fun getItemCount(): Int {
        return mUser.size
    }

    private fun retrieveLasMessage(onlineUserID: String?, lastMessageTxt: TextView) {
        lastMsg = "defaultMsg"

        val firebaseUser = FirebaseAuth.getInstance().currentUser

        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (data in snapshot.children){

                    val chat: Chat? = data.getValue(Chat::class.java)

                    if(chat!!.receiver.equals(firebaseUser!!.uid) && chat.sender.equals(onlineUserID) ||
                            chat.receiver.equals(onlineUserID) && chat!!.sender.equals(firebaseUser!!.uid)){
                        lastMsg = chat!!.message

                    }
/*                    if (firebaseUser !=null && chat != null){
                        if(chat.sender == firebaseUser!!.uid ){

                        }
                    }*/
                }
                when (lastMsg){
                    "defaultMsg" -> lastMessageTxt.text= "No Message"
                    else -> {
                        lastMessageTxt.text = lastMsg
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
    {
        var busNameTxt: TextView
        var profileImageView: CircleImageView
        var onlineImageView: CircleImageView
        var offlineImageView: CircleImageView
        var lastMessageTxt: TextView


        init {
            busNameTxt = itemView.findViewById(R.id.business_name)
            profileImageView = itemView.findViewById(R.id.search_pix)
            onlineImageView = itemView.findViewById(R.id.image_online)
            offlineImageView = itemView.findViewById(R.id.image_offline)
            lastMessageTxt = itemView.findViewById(R.id.message_last)

        }
    }


}