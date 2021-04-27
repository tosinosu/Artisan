package com.tostech.artisan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tostech.artisan.AdapterClasses.BusinessListRecyclerAdapter
import com.tostech.artisan.databinding.ActivityBusinessListBinding

private lateinit var binding: ActivityBusinessListBinding

private lateinit var businessListRecyclerAdapter: BusinessListRecyclerAdapter

class BusinessList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBusinessListBinding.inflate(layoutInflater)

        val view = binding.root

        setContentView(view)

    //    initRecyclerView()
      //  addDataSet()

      //  return binding.root
    }


/*
    private fun addDataSet(){
        val data = DataSource.dummyBusinessList()
        businessListRecyclerAdapter.submitList(data)
    }

    private fun initRecyclerView(){

        binding.recyclerBusinessList.apply {
            layoutManager = LinearLayoutManager(context)
            val itemsDecoration = ItemDecoration(10)
            addItemDecoration(itemsDecoration)
            businessListRecyclerAdapter = BusinessListRecyclerAdapter()
            binding.recyclerBusinessList.adapter = businessListRecyclerAdapter
        }

    }
*/


}