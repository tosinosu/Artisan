package com.tostech.artisan

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ViewFullImage: AppCompatActivity() {
    private var imageView: ImageView?= null
    private var imageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_view_full)

        imageUrl = intent.getStringExtra("url").toString()
        imageView = findViewById(R.id.full_image)

        Glide.with(this).load(imageUrl).into(imageView!!)
    }
}