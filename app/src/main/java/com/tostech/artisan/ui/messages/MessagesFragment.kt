package com.tostech.artisan.ui.messages

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.tostech.artisan.AdapterClasses.AdvertAdapter
import com.tostech.artisan.databinding.FragmentMessagesBinding
import com.tostech.artisan.notification.FirebaseService
import android.content.Intent
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tostech.artisan.AdapterClasses.MessageAdapter
import com.tostech.artisan.data.*
//import com.tostech.artisan.ui.profile.intent
import kotlinx.android.synthetic.main.fragment_messages.*


class MessagesFragment : Fragment() {


    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    private var messageAdapter: MessageAdapter? = null
    private var mUsers: List<MessageData>? = null
    private var usersChatList: List<ChatList>? = ArrayList()
    private var chats: List<Chat>? = ArrayList()

    private var firebaseUser: FirebaseUser? = null
    private lateinit var reference: DatabaseReference
    lateinit var recyclerChatList: RecyclerView


    val gson = Gson()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
         recyclerChatList = _binding!!.recyclerMessages
        val userVisit = arguments?.getStringArray("visit_id").toString()

        recyclerChatList.setHasFixedSize(true)
        recyclerChatList.layoutManager = LinearLayoutManager(context)


        firebaseUser = FirebaseAuth.getInstance().currentUser

       // Log.v("userVisist Messa", userVisit!!)

        usersChatList = ArrayList()
        chats = ArrayList()

        val reference = Firebase.database.reference.child("ChatList").child(firebaseUser!!.uid)

        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (usersChatList as ArrayList).clear()
                for(datasnapshot in snapshot.children){
                    val chatlist = datasnapshot.getValue(ChatList::class.java)
                    (usersChatList as ArrayList).add(chatlist!!)
                }

                retrieveChatList()
            }

            override fun onCancelled(error: DatabaseError) {

            }
    })




        FirebaseService.sharedPref = requireActivity().getSharedPreferences("sharedPref",Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token
        }


        return binding.root
    }

    private fun  retrieveChatList(){

      //  Log.v("ChatsArray", arrayList.size.toString())

        mUsers = ArrayList()

        reference = Firebase.database.reference.child("User")

        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList).clear()

            for (datasnapshot in snapshot.children){
                val user = datasnapshot.child("advert").getValue(MessageData::class.java)

                for (eachChat in usersChatList!!){

                    if(user!!.uid.equals(eachChat.id)){
                        (mUsers as ArrayList).add(user!!)
                    }
                }
             }
               // Log.v("snapsh", mUsers!!.toString())
                (mUsers as ArrayList<AdvertData>).asReversed()
                try {
                    messageAdapter = MessageAdapter(context!!, (mUsers as ArrayList<MessageData>), true)

                    recyclerChatList.adapter = messageAdapter
                    recyclerChatList.adapter!!.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.ALLOW
                  //  recyclerChatList.adapter!!.notifyDataSetChanged()

                } catch (ex: NullPointerException){
                    Log.e("Chat Exception", ex.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
