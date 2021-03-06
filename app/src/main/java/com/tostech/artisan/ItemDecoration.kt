package com.tostech.artisan


import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemDecoration (private val padding: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
      //  outRect.left = padding
        outRect.top = padding
    }


  /*  private var mDivider: Drawable? = null

    constructor(context: Context) {
        mDivider = ContextCompat.getDrawable(context, R.drawable.line_divider)
    }

    constructor(context: Context, drawable: Int) {
        mDivider = ContextCompat.getDrawable(context, drawable)
    }

    override fun onDrawOver(c: Canvas, recyclerView: RecyclerView, state: RecyclerView.State) {

        val left = recyclerView.paddingLeft
        val right = recyclerView.width

        val childCount = recyclerView.childCount
        for (i in 0 until childCount) {

            val child = recyclerView.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider!!.intrinsicHeight
            mDivider!!.setBounds(left, top, right, bottom)
            mDivider!!.draw(c)

        }
    }*/
}