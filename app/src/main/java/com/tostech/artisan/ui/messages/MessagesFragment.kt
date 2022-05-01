package com.tostech.artisan.ui.messages

import android.content.Context
import android.os.Bundle
//import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.tostech.artisan.AdapterClasses.MessageAdapter
import com.tostech.artisan.MainActivity
import com.tostech.artisan.R
import com.tostech.artisan.data.AdvertData
import com.tostech.artisan.data.Chat
import com.tostech.artisan.data.ChatList
import com.tostech.artisan.data.MessageData
import com.tostech.artisan.databinding.FragmentMessagesBinding
import com.tostech.artisan.notification.FirebaseService


class MessagesFragment : Fragment() {


    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    lateinit var mActivity: FragmentActivity
    private var messageAdapter: MessageAdapter? = null
    private var mUsers: List<MessageData>? = null
    private var usersChatList: List<ChatList>? = ArrayList()
    private var chats: List<Chat>? = ArrayList()
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

    private fun setUpToolbar() {
        val mainActivity = mActivity as MainActivity
        val  navigationView : NavigationView? = mActivity.findViewById(R.id.nav_view)
        mainActivity.setSupportActionBar(binding.toolbar)
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration =  mainActivity.appBarConfiguration
        NavigationUI.setupActionBarWithNavController(mainActivity, navController, appBarConfiguration!!)
        NavigationUI.setupWithNavController(navigationView!!,navController)

        setHasOptionsMenu(true)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let { mActivity = it}
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
                    //Log.e("Chat Exception", ex.toString())
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
