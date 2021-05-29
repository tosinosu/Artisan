package com.tostech.artisan.AdapterClasses

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.view.*
import android.view.MotionEvent.ACTION_MOVE
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.tostech.artisan.R
import kotlin.math.max
import kotlin.math.min


class ImagePagerAdapter(context: Context,  imagesUrl: ArrayList<String>,  imagesText: ArrayList<String>) : PagerAdapter() {

    private var imagesUrl: ArrayList<String>? = null
    private var imagesText: ArrayList<String>? = null
    private var inflater: LayoutInflater? = null
    private var context: Context? = null
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f
    private var imageView: ImageView? = null

    private var imageLayout: View? = null


    init {
        this.context = context
        this.imagesUrl = imagesUrl
        this.imagesText = imagesText
        inflater = LayoutInflater.from(context)
    }

    override fun getCount(): Int {
        return imagesUrl!!.size
    }


  //  @SuppressLint("ClickableViewAccessibility")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
         imageLayout = inflater?.inflate(R.layout.slidingimages, container, false)


        val imageView = imageLayout?.findViewById<View>(R.id.image) as PhotoView

        val text = imageLayout?.findViewById<View>(R.id.text_advert) as TextView


        text.text = imagesText!!.get(position)
        Glide.with(imageView!!.context).load(imagesUrl?.get(position)).into(imageView!!)

       // scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        //imageView!!.setOnTouchListener { p0, p1 -> scaleGestureDetector.onTouchEvent(p1) }

        container.addView(imageLayout, 0)


        return imageLayout!!

    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}

    override fun saveState(): Parcelable? {
        return null
    }



private inner class ScaleListener :ScaleGestureDetector.SimpleOnScaleGestureListener(){

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        scaleFactor *= detector!!.scaleFactor
        scaleFactor = max(0.1f, min(scaleFactor, 10.0f))
        imageView!!.scaleX = scaleFactor
        imageView!!.scaleY = scaleFactor

        return true
    }
}

}