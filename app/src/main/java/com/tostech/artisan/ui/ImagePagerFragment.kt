package com.tostech.artisan.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.tostech.artisan.AdapterClasses.ImagePagerAdapter
import com.tostech.artisan.R
import com.tostech.artisan.databinding.FragmentHomeBinding
import com.tostech.artisan.databinding.FragmentPagerBinding
import com.tostech.artisan.ui.home.HomeFragment


class ImagePagerFragment : Fragment() {
 //   private var viewPager: ViewPager? = null

    private var imageRes: ArrayList<String>? = null
    private var userId: String? = null
    private var txtRes: ArrayList<String>? = null
    var position: Int? = 0

    private var _binding: FragmentPagerBinding? = null
    private val mPager: ViewPager? = null
    private var currentPage = 0
    private val NUM_PAGES = 0
    private val binding get() = _binding!!
    private var backPressed = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPagerBinding.inflate(inflater, container, false)

            imageRes = arguments?.getStringArrayList("imageUrl")
            txtRes = arguments?.getStringArrayList("imageText")

            userId = arguments?.getString("userId")
            position = arguments?.getInt("position")


           val viewPager = binding.viewPager

            if (currentPage == NUM_PAGES) {
                currentPage = position!!
            }
            viewPager.setCurrentItem(currentPage++, true)

            viewPager.adapter = ImagePagerAdapter(requireContext(), imageRes!!, txtRes!!)
           viewPager.currentItem = position!!

            viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(pos: Int) {
                    currentPage = pos
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                }

                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                }
            })
        val  callback = object :OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if (backPressed) {
                    val activity = context as AppCompatActivity
                    val bundle = bundleOf("signInID" to userId)

                    val fm = activity.supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        add<HomeFragment>(R.id.constraint_main,  args = bundle)
                            .addToBackStack(null)

                    }
                } else {
                    backPressed = false

                }
            }}

                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

          return binding.root
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}