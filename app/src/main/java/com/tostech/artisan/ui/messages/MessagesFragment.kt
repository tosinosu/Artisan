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
import com.tostech.artisan.data.AdvertData
import com.tostech.artisan.data.ChatList
import com.tostech.artisan.databinding.FragmentMessagesBinding
import com.tostech.artisan.notification.FirebaseService
import com.tostech.artisan.notification.MyFirebaseInstanceId
import com.tostech.artisan.notification.Token
import kotlinx.android.synthetic.main.fragment_messages.*


class MessagesFragment : Fragment() {


    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!

    private var advertAdapter: AdvertAdapter? = null
    private var mUsers: List<AdvertData>? = null
    private var usersChatList: List<ChatList>? = null

    private var firebaseUser: FirebaseUser? = null
    private lateinit var reference: DatabaseReference
    lateinit var recyclerChatList: RecyclerView




    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
         recyclerChatList = _binding!!.recyclerMessages

        recyclerChatList.setHasFixedSize(true)
        recyclerChatList.layoutManager = LinearLayoutManager(context)


        firebaseUser = FirebaseAuth.getInstance().currentUser

         reference = Firebase.database.reference.child("ChatList").child(firebaseUser!!.uid)
        usersChatList = ArrayList()

        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (usersChatList as ArrayList).clear()
                for(datasnapshot in snapshot.children){
                    val chatlist = datasnapshot.getValue(ChatList::class.java)
                    (usersChatList as ArrayList).add(chatlist!!)
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
    })
        retrieveChatList()

        FirebaseService.sharedPref = requireActivity().getSharedPreferences("sharedPref",Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token
        }


        return binding.root
    }

    private fun  retrieveChatList(){

        mUsers = ArrayList()

        reference = Firebase.database.reference.child("User")

        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mUsers as ArrayList).clear()

            for (datasnapshot in snapshot.children){
                val user = datasnapshot.child("advert").getValue(AdvertData::class.java)
              // Log.v("snapsh", user.toString())

                for (eachChat in usersChatList!!){

                    if(user!!.uid.equals(eachChat.id)){
                        (mUsers as ArrayList).add(user!!)
                    }
                }
            }
               // Log.v("snapsh", mUsers!!.toString())
                (mUsers as ArrayList<AdvertData>).asReversed()
                try {
                    advertAdapter = AdvertAdapter(context!!, (mUsers as ArrayList<AdvertData>), true)

                    recyclerChatList.adapter = advertAdapter
                    recyclerChatList.adapter!!.notifyDataSetChanged()

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
