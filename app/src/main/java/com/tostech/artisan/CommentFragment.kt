package com.tostech.artisan

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.database.SnapshotParser
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.tostech.artisan.AdapterClasses.CommentAdapter
import com.tostech.artisan.data.AcceptedData
import com.tostech.artisan.data.CommentData
import com.tostech.artisan.data.DeleteData
import com.tostech.artisan.databinding.ActivityCommentBinding

class CommentFragment : Fragment(R.layout.activity_comment) {

   // private var _binding: FragmentCommentBinding? =null
    private lateinit var commentList: ArrayList<CommentData>
    private lateinit var commentAdapter: CommentAdapter
     var database = Firebase.database.reference
    private var _binding: ActivityCommentBinding? = null
    private val binding get() = _binding!!
    lateinit var mActivity: FragmentActivity


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let { mActivity = it}
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ActivityCommentBinding.inflate(inflater, container, false)

        val view = binding.root

        val userVisit = arguments?.getString("visit_id")
        val myID = arguments?.getString("my_id")
        val comment = binding.edtComment
        val submit = binding.btnSubmit
        val commentRecycler = binding.rcyComment
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val commentRef = database.child("User").child(userVisit!!).child("comments")

        if (myID == uid){
            comment.isVisible = false
            submit.isVisible = false

        }
        val referenceAccepted = database.child("User").child(userVisit!!).child("accepted_order").child(userVisit!!)


        referenceAccepted.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val acceptedData = snapshot.getValue<AcceptedData>()
                    try {
                        if (acceptedData!!.id == uid) {
                            if (acceptedData!!.commented == 1) {
                                comment.isVisible = false
                                submit.isVisible = false
                            } else {
                                comment.isVisible = true
                                submit.isVisible =  true

                            }

                        }/*else{
                            comment.isVisible = false
                            submit.isVisible = false
                        }*/

                    } catch (ex: NullPointerException) {
                        comment.isVisible = false
                        submit.isVisible = false
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        displayComments(userVisit)


        submit.setOnClickListener {
            if (comment.text.equals(null) || comment.text.length < 2) {
                message("Please re-enter your comment")
            } else {
                val commentText: String = binding.edtComment.text.toString()
                message(commentText)
                commentRef.push().setValue(commentText)
                    .addOnCompleteListener {
                        referenceAccepted.child("commented").setValue(1)
                        deleteAccepted(userVisit)
                    }
            }
        }

        return view
    }

    private fun displayComments(userVisit: String){
        val query = database.child("User").child(userVisit!!).child("comments")

        commentList = ArrayList()
        val options: FirebaseRecyclerOptions<String> =
            FirebaseRecyclerOptions.Builder<String>()
                .setQuery(query, String::class.java)/*object : SnapshotParser<CommentData> {
                    override fun parseSnapshot(snapshot: DataSnapshot): CommentData {
                        val commentData = CommentData(snapshot.child("text").value.toString())
                        return commentData
                    }

                })*/
                .build()

        binding.rcyComment.apply {
            layoutManager = LinearLayoutManager(context)
            // val simpleDividerItemDecoration = SimpleDividerItemDecoration(context)
             //addItemDecoration(simpleDividerItemDecoration)
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
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        commentAdapter.startListening()

    }

    override fun onStop() {
        super.onStop()
        commentAdapter.stopListening()
    }

    override fun onResume() {
        super.onResume()
        commentAdapter.startListening()

    }
    override fun onPause() {
        super.onPause()
        commentAdapter.stopListening()

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
}