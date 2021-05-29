package com.tostech.artisan

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.tostech.artisan.AdapterClasses.CommentAdapter
import com.tostech.artisan.data.AcceptedData
import com.tostech.artisan.data.CommentData
import com.tostech.artisan.data.DeleteData
import com.tostech.artisan.databinding.ActivityCommentBinding
import com.tostech.artisan.ui.home.HomeFragment

class CommentActivity : AppCompatActivity(R.layout.activity_comment) {

   // private var _binding: FragmentCommentBinding? =null
    private lateinit var commentList: ArrayList<CommentData>
    private lateinit var commentAdapter: CommentAdapter
     var database = Firebase.database.reference
    private lateinit var binding: ActivityCommentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCommentBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_comment)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Comment"
           supportActionBar!!.setDisplayHomeAsUpEnabled(true)
           toolbar.setNavigationOnClickListener {
              /* val intent = Intent(this@MessageChatActivity, MainActivity::class.java)
            intent.putExtra("messages", "messages")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            val newFragment = HomeFragment()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            transaction.replace(R.id.layout_comment, newFragment)
                .addToBackStack(null)
                .commit()*/

               onBackPressed()

        }


        intent = intent
        val userVisit = intent.getStringExtra("visit_id")
        val comment = binding.edtComment
        val submit = binding.btnSubmit
        val commentRecycler = binding.rcyComment
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val commentRef = database.child("User").child(userVisit!!).child("comments")

        val referenceAccepted = database.child("User").child(userVisit!!).child("accepted_order").child(userVisit!!)


        referenceAccepted.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val acceptedData = snapshot.getValue<AcceptedData>()
               try {

                if (acceptedData!!.id == uid){
                    if (acceptedData!!.commented == 1){
                        comment.isVisible = true
                        submit.isVisible = true
                    }else{
                        comment.isEnabled
                        submit.isEnabled

                        submit.setOnClickListener {
                            if (comment.text.equals(null) || comment.text.length < 2) {
                                message("Please enter your comment")
                            }else {
                                val commentText: String = binding.edtComment.text.toString()
                                message(commentText)
                                commentRef.push().setValue(commentText).addOnCompleteListener {
                                    referenceAccepted.child("commented").setValue(1)
                                    deleteAccepted(userVisit)
                                }


                            }
                        }
                    }

                }

               }catch (ex: NullPointerException){
                   comment.isVisible = true
                   submit.isVisible = true
               }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        displayComments(userVisit)


    }

    private fun displayComments(userVisit: String){
        val query = database.child("User").child(userVisit!!).child("comments")

        commentList = ArrayList()
        val options: FirebaseRecyclerOptions<CommentData> =
            FirebaseRecyclerOptions.Builder<CommentData>()
                .setQuery(query, CommentData::class.java)


                .build()

        binding.rcyComment.apply {
            layoutManager = LinearLayoutManager(context)
             val simpleDividerItemDecoration = SimpleDividerItemDecoration(context)
             addItemDecoration(simpleDividerItemDecoration)
            commentAdapter = CommentAdapter(options)
            adapter = commentAdapter
        }

    }
   private fun deleteAccepted(userId: String?){
       val refDeleteOrder = database.child("User").child(userId!!).child("accepted_order").child(userId!!)

       refDeleteOrder.addValueEventListener(object : ValueEventListener{
           override fun onDataChange(snapshot: DataSnapshot) {
               for (dataSnapshot in snapshot.children) {
                   val deleteData = snapshot.getValue(DeleteData::class.java)
                   if (deleteData!!.commented == 1 && deleteData!!.rated == 1) {
                       refDeleteOrder.removeValue()

                   }
               }
           }

           override fun onCancelled(error: DatabaseError) {

           }
       })
    }

    private fun message(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        commentAdapter.startListening()

    }

    override fun onStop() {
        super.onStop()
        commentAdapter.stopListening()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

}