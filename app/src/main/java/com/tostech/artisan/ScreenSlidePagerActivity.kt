package com.tostech.artisan

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

import com.tostech.artisan.databinding.FragmentSlidingBinding
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.NullPointerException


/**
 * The number of pages (wizard steps) to show in this demo.
 */
private const val NUM_PAGES = 4

class ScreenSlidePagerActivity : FragmentActivity() {

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private lateinit var viewPager: ViewPager2
    private lateinit var binding: FragmentSlidingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentSlidingBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Instantiate a ViewPager2 and a PagerAdapter.
      //  viewPager = findViewById(R.id.pager)

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        binding.pager.adapter = pagerAdapter
    }

    override fun onBackPressed() {
        if (binding.pager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            binding.pager.currentItem = binding.pager.currentItem - 1
        }
    }


    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {

            when(position){

                0 -> Profile()
                1 -> Pictures()
               // 2 -> Description()
                2 -> Verify()
                3 -> Subscription()

            }
/*
                if (position == 1)
                    return Profile()
                if (position == 2)
                    return Pictures()
                if (position == 3)
                    return Description()
                if (position == 4)
                    return Verify()
                if (position == 5)
                    return Subscription()

*/
            return Fragment()
        }
    }
}
