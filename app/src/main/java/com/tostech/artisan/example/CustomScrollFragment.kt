package com.tostech.artisan.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import com.reddit.indicatorfastscroll.FastScrollerThumbView
import com.reddit.indicatorfastscroll.FastScrollerView
import com.tostech.artisan.ListItem
import com.tostech.artisan.R
//import com.tostech.artisan.SAMPLE_DATA_TEXT
import com.tostech.artisan.SampleAdapter

class CustomScrollFragment : Fragment() {

  private lateinit var recyclerView: RecyclerView
  private lateinit var fastScrollerView: FastScrollerView
  private lateinit var fastScrollerThumbView: FastScrollerThumbView

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    val view = inflater.inflate(R.layout.sample_basic, container, false)
      val dat =  resources.getStringArray(R.array.artisanList).toSortedSet().map {
          ListItem.DataItem(
              it
          )
      }

    val data = listOf(
        ListItem.DataItem(
        "Items will be scrolled to the top!",
        showInFastScroll = false
    )) + dat

    recyclerView = view.findViewById(R.id.sample_basic_recyclerview)
    val linearLayoutManager = LinearLayoutManager(context)
    recyclerView.apply {
      layoutManager = linearLayoutManager
      adapter = SampleAdapter(data)
    }

    fastScrollerView = view.findViewById(R.id.sample_basic_fastscroller)
    fastScrollerView.apply {
      setupWithRecyclerView(
          recyclerView,
          { position ->
            data[position]
                .takeIf(ListItem::showInFastScroll)
                ?.let { item ->
                  FastScrollItemIndicator.Text(
                      item
                          .title
                          .substring(0, 1)
                          .toUpperCase()
                  )
                }
          },
          useDefaultScroller = false
      )
      val smoothScroller: LinearSmoothScroller = object : LinearSmoothScroller(context) {
        override fun getVerticalSnapPreference(): Int = SNAP_TO_START
      }
      itemIndicatorSelectedCallbacks += object : FastScrollerView.ItemIndicatorSelectedCallback {
        override fun onItemIndicatorSelected(
            indicator: FastScrollItemIndicator,
            indicatorCenterY: Int,
            itemPosition: Int
        ) {
          recyclerView.stopScroll()
          smoothScroller.targetPosition = itemPosition
          linearLayoutManager.startSmoothScroll(smoothScroller)
        }
      }
    }

    fastScrollerThumbView = view.findViewById(R.id.sample_basic_fastscroller_thumb)
    fastScrollerThumbView.apply {
      setupWithFastScroller(fastScrollerView)
    }

    return view
  }

}
