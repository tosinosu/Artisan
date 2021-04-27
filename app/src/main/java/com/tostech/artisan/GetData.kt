package com.tostech.artisan

import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class GetData {

    fun getAdvert(reference: DatabaseReference, view: TextView, context: Context) {
        val dataListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(String::class.java)
                view.text = value
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        reference.addValueEventListener(dataListener)


    }
    fun getAdvertPix(reference: DatabaseReference, view: ImageView, context: Context) {
        val dataListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(String::class.java)
                Glide.with(context).load(value).into(view)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        reference.addValueEventListener(dataListener)


    }
  /*  fun getAdvertSpinner(reference: DatabaseReference, view: Spinner, context: Context) {
        val dataListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(String::class.java)
                view.setSelection(position) = value
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                Log.w("Database", "loadPost:onCancelled", error.toException())
            }
        }

        reference.addValueEventListener(dataListener)

    }*/

}