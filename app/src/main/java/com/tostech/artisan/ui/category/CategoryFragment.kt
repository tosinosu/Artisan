package com.tostech.artisan.ui.category


import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager

import com.reddit.indicatorfastscroll.FastScrollItemIndicator
import com.tostech.artisan.*
import com.tostech.artisan.AdapterClasses.CategoryRecyclerAdapter
import com.tostech.artisan.data.CategoryData
import com.tostech.artisan.databinding.FragmentCategoryBinding
import java.util.*

import androidx.recyclerview.widget.LinearLayoutManager
import com.reddit.indicatorfastscroll.FastScrollerThumbView
import com.reddit.indicatorfastscroll.FastScrollerView

import com.tostech.artisan.R
//import com.tostech.artisan.SAMPLE_DATA_TEXT


class CategoryFragment : Fragment(){

    private lateinit var binding: FragmentCategoryBinding
    private lateinit var categoryViewModel: CategoryViewModel
    private var categoryList = ArrayList<CategoryData>()
    private lateinit var categoryRecyclerAdapter: CategoryRecyclerAdapter



    private lateinit var fastScrollerView: FastScrollerView
    private lateinit var fastScrollerThumbView: FastScrollerThumbView
    private var layoutManager: GridLayoutManager? =null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        categoryViewModel =
                ViewModelProviders.of(this).get(CategoryViewModel::class.java)
       // val root = inflater.inflate(R.layout.fragment_gallery, container, false)
        //val textView: TextView = root.findViewById(R.id.text_gallery)
        categoryViewModel.text.observe(viewLifecycleOwner, Observer {
            //   textView.text = it
        })

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_category, container, false)

        initRecyclerView()


       // container!!.removeAllViews()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)

    }

    private fun initRecyclerView(){
        val data1 = resources.getStringArray(R.array.artisanList).toSortedSet().toList()/*.map {
            ListItem.DataItem(
                it
            )
        }*/
        val data = listOf(ListItem.DataItem(
            "Items will be scrolled to the top!",
            showInFastScroll = false
        )) + data1

        val recyclerCategory = binding.recycleCategory
        val fastScrollerView = binding.fastScroller
        val linearLayoutManager = LinearLayoutManager(context)


        recyclerCategory.apply {
             layoutManager = GridLayoutManager(context, 2)

            //val itemsDecoration = ItemDecoration(10)
            //addItemDecoration(itemsDecoration)
            recyclerCategory.layoutManager = layoutManager

            categoryRecyclerAdapter = CategoryRecyclerAdapter(data1)
            recyclerCategory.adapter = categoryRecyclerAdapter
        }
        fastScrollerView.apply {
            setupWithRecyclerView(
                recyclerCategory,
                { position ->
                    val item = data1[position]
                    FastScrollItemIndicator.Text(
                        item
                            //.title
                            .substring(0, 1)
                            .toUpperCase()
                    )}
            )}


        fastScrollerThumbView = binding.sampleBasicFastscrollerThumb
        fastScrollerThumbView.apply {
            setupWithFastScroller(fastScrollerView)
        }
    }

    private fun initRecyclerViewList(){
        val data1 = resources.getStringArray(R.array.artisanList).toSortedSet().toList()/*.map {
            ListItem.DataItem(
                it
            )
        }*/
        val data = listOf(ListItem.DataItem(
            "Items will be scrolled to the top!",
            showInFastScroll = false
        )) + data1

        val recyclerCategory = binding.recycleCategory
        val fastScrollerView = binding.fastScroller
        val linearLayoutManager = LinearLayoutManager(context)


        recyclerCategory.apply {
           // val layoutManager = LinearLayoutManager(context)

            //val itemsDecoration = ItemDecoration(10)
            //addItemDecoration(itemsDecoration)
            recyclerCategory.layoutManager = linearLayoutManager
            categoryRecyclerAdapter = CategoryRecyclerAdapter(data1)
            recyclerCategory.adapter = categoryRecyclerAdapter


        }
    /*    fastScrollerView.apply {
            setupWithRecyclerView(
                recyclerCategory,
                { position ->
                    val item = data1[position]
                    FastScrollItemIndicator.Text(
                        item
                            //.title
                            .substring(0, 1)
                            .toUpperCase()
                    )}
            )}


        fastScrollerThumbView = binding.sampleBasicFastscrollerThumb
        fastScrollerThumbView.apply {
            setupWithFastScroller(fastScrollerView)
        }*/
    }

   /* override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.category_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         when (item?.itemId){
             R.id.catGrid -> {
                 if(layoutManager?.spanCount == 1){
                     layoutManager?.spanCount = 3
                   //  initRecyclerView()
                     item.icon = requireContext().getDrawable(R.drawable.ic_baseline_list_24)
                   //  return true
                 }
                 else  {

                     layoutManager?.spanCount = 3
                   //  initRecyclerViewList()
                     item.icon = requireContext().getDrawable(R.drawable.ic_baseline_grid_on_24)                 }
                 categoryRecyclerAdapter?.notifyItemRangeChanged(0, categoryRecyclerAdapter?.itemCount ?:0)
               //  return true
             }

         }
      return super.onOptionsItemSelected(item)
    }*/
}