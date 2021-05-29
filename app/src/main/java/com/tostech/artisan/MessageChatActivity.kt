package com.tostech.artisan

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.toColor
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.internal.NavigationMenuItemView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.gson.Gson
import com.tostech.artisan.AdapterClasses.ChatAdapter
import com.tostech.artisan.data.AdvertData
import com.tostech.artisan.data.Chat
import com.tostech.artisan.data.ChatList
import com.tostech.artisan.data.UserData
import com.tostech.artisan.databinding.ActivityMessageChatBinding
import com.tostech.artisan.notification.*
import com.tostech.artisan.ui.messages.MessagesFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MessageChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessageChatBinding

    var userVisit: String = ""

    var userName: String = ""
    var firebaseUser: FirebaseUser? =null

    var chatAdapter: ChatAdapter? = null
    var mChatList: List<Chat>? = null
    var coverChecker: String? = null
    var seenListener: ValueEventListener? = null

    var reference: DatabaseReference? = null


    var topic = ""

    var notify = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageChatBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

      /*  val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_message_chat)
        setSupportActionBar(toolbar)
*/
     /*   supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
           *//* val intent = Intent(this@MessageChatActivity, MainActivity::class.java)
            intent.putExtra("messages", "messages")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()*//*
            val newFragment = MessagesFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction.replace(R.id.messageChatLayout, newFragment)
                .addToBackStack(null)
                .commit()

        }*/
        val imgBack = binding.imgBack
        val deleteImage = binding.deleteBtn

        deleteImage.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setTitle("Delete this chat?")
               // setIcon(R.drawable.ic_baseline_close_24)

                setMessage("You will loose all your chats with this user")
                setPositiveButton("Ok") { dialog, id ->
                    deleteAllChats()
                   // onBackPressed()
                }
                setNegativeButton("Cancel") { dialog, id ->
                   dialog.cancel()
                }
            }.create().show()



    }

        imgBack.setOnClickListener {
            onBackPressed()

        }



        intent = intent
        userVisit = intent.getStringExtra("visit_id").toString()
        userName = intent.getStringExtra("busName").toString()

        val reference = FirebaseDatabase.getInstance().reference
            .child("User").child(userVisit!!).child("advert")


        Log.v("Uservisit", userVisit)
        firebaseUser = FirebaseAuth.getInstance().currentUser

        binding.recyclerviewChat.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        binding.recyclerviewChat.layoutManager = linearLayoutManager


        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val advertData: AdvertData? = snapshot.getValue(AdvertData::class.java)

                binding.usernameMchat.text = advertData!!.bus_name
                Glide.with(applicationContext).load(advertData.purl).into(binding.profileImageMChat)

                retrieveMessages(firebaseUser!!.uid, userVisit!!, advertData.purl)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        binding.sendMessageBtn.setOnClickListener {
            val message = binding.showTextMessage.text.toString()
            notify = true

            if(message == ""){
                Toast.makeText(this@MessageChatActivity, "Please type a message", Toast.LENGTH_SHORT).show()
            }else{
                sendMessageToUser(firebaseUser!!.uid, userVisit, message)
                binding.showTextMessage.setText("")


            }

        }


        binding.attachImageFile.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Image"), 438)
        }

        seenMessage(userVisit)
    }

    override fun onStop() {
        super.onStop()
        try {
            reference!!.removeEventListener(seenListener!!)
            status("offline")
            currentUser("none")

        } catch (ex: NullPointerException){
            status("offline")
            currentUser("none")
            Log.d("removelistener", ex.message.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            reference!!.removeEventListener(seenListener!!)
            status("offline")
            currentUser("none")

        } catch (ex: NullPointerException){
            status("offline")
            currentUser("none")
            Log.d("removelistener", ex.message.toString())
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            reference!!.removeEventListener(seenListener!!)
            status("offline")
            currentUser("none")

        } catch (ex: NullPointerException){
            status("offline")
            currentUser("none")
            Log.d("removelistener", ex.message.toString())
        }

    }

    override fun onResume() {
        super.onResume()
        status("online")
        currentUser(userVisit)
    }

    fun status(status: String){
        val reference = FirebaseDatabase.getInstance().reference.child("User").child(firebaseUser!!.uid).child("advert")
        val hashMap = HashMap<String, Any>()
        hashMap.put("status", status)
        reference.updateChildren(hashMap)


    }

   private fun currentUser(userVisitId: String){
       val sharedPreferences = getSharedPreferences("PREFS", MODE_PRIVATE) ?: return
       with(sharedPreferences.edit()){
           putString("currentUser", userVisitId)
           apply()
       }

   }

    private fun seenMessage(userId: String){
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        seenListener = reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (datasnap in snapshot.children){
                    val chat = datasnap.getValue(Chat::class.java)
                    if(chat!!.receiver.equals(firebaseUser!!.uid) && chat!!.sender.equals(userId)){
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        datasnap.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })



    }


    private fun retrieveMessages(senderID: String, receiverId: String, receivedImageUrl: String?) {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (data in snapshot.children){
                    val chat = data.getValue(Chat::class.java)

                    if(chat!!.receiver.equals(senderID) && chat.sender.equals(receiverId)
                        || chat.receiver.equals(receiverId) && chat.sender.equals(senderID)){

                        (mChatList as ArrayList<Chat>).add(chat)
                    }

                    chatAdapter = ChatAdapter(this@MessageChatActivity, (mChatList as ArrayList<Chat>), receivedImageUrl!!)

                    binding.recyclerviewChat.adapter = chatAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == 438 && resultCode == RESULT_OK && data !=null && data!!.data != null){


            val loadingBar = ProgressDialog(this)
            loadingBar.setMessage("Image is Loading...")
            loadingBar.show()


            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")


            val uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)
            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if(!task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you a message"
                    messageHashMap["receiver"] = userVisit
                    messageHashMap["isseen"] = false
                   // messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId

                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)

                    loadingBar.dismiss()

               /*     if (coverChecker == "cover"){
                        val mapCovering = HashMap<String, Any>()
                        mapCovering["cover"] = url
                        usersReference!!.*/
                    }
                }
            }


        }


    private fun sendMessageToUser(senderID: String, receiverID: String?, message: String) {

         reference = FirebaseDatabase.getInstance().reference

        val messageKey = reference!!.push().key
      //  val uid = firebaseUser!!.uid

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderID
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverID
        messageHashMap["isseen"] = false
       // messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey

        reference!!.child("Chats").child(messageKey!!).setValue(messageHashMap)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val chatListReference = FirebaseDatabase.getInstance()
                        .reference.child("ChatList")
                        .child(firebaseUser!!.uid).child(userVisit!!)
                    val chatListReceiverReference = FirebaseDatabase.getInstance()
                        .reference.child("ChatList").child(userVisit!!).child(firebaseUser!!.uid)

                    chatListReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                                chatListReference.child("id").setValue(userVisit)
                                chatListReceiverReference.child("id").setValue(firebaseUser!!.uid)

                            }

                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

                   /* val chatListReceiverReference = FirebaseDatabase.getInstance()
                        .reference.child("ChatList").child(userVisit!!).child(firebaseUser!!.uid)
*/

                    chatListReceiverReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) {
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })


                    val reference1 = FirebaseDatabase.getInstance().reference.child("User").child(firebaseUser!!.uid).child("user")
                    val msg = message
                    //implement the push notification using fcm

                    topic = "/topics/$userVisit"
                    reference1.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val username = snapshot.child("username").value.toString()
                            Log.v("Username", username)
                            PushNotification(NotificationData(senderID,  "$username: $msg", "New Message",  receiverID),
                                topic).also {
                                sendNotification(it)
                            }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })


                }
            }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
         //   Log.e("noti", response.toString())

            if(response.isSuccessful) {
                Log.d("notisuccess", "Response: Success") //${Gson().toJson(response)}")
            } else {
                Log.e("notiError1", response.errorBody()!!.string())
            }
        } catch(e: Exception) {
            Log.e("noticatch", e.message.toString())
        }

    }

    private fun deleteAllChats() {
        val firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val referenceChat = Firebase.database.reference.child("Chats")
        val referenceChatList = Firebase.database.reference.child("ChatList").child(firebaseUserID)
        val referenceChatL = Firebase.database.reference.child("ChatList").child(userVisit)

/*
        referenceChat!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(datasnapshot in snapshot.children){
                    val chatData = datasnapshot.getValue(Chat::class.java)

                    if((chatData!!.receiver == firebaseUserID && chatData!!.sender == userVisit) ||
                        (chatData!!.receiver == userVisit && chatData!!.sender == firebaseUserID)){
                        referenceChat.child(firebaseUserID).ref.removeValue()
                    }
                }


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })*/

        referenceChatList!!.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(datasnapshot in snapshot.children){
                    val chatListData = datasnapshot.getValue(ChatList::class.java)

                    if(chatListData!!.id == userVisit ){
                        referenceChatList.child(userVisit).removeValue()
                       // referenceChatList.child(firebaseUserID).removeValue()

                    }
                }


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })




    }
}