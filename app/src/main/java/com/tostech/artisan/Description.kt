package com.tostech.artisan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.tostech.artisan.databinding.DescriptionBinding
import com.tostech.artisan.databinding.ProfileBinding

class Description: Fragment() {

    private lateinit var binding: DescriptionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.description, container, false)


/*
        binding.btnSubmitDescription.setOnClickListener {
            val userID = GetUSer().getUserId()
            val busName: String? = binding.edBusinessName.text.toString()
            val advert: String? = binding.edMultiAdvert.text.toString()

            if ( busName != null && advert != null){

                if (userID != null){

              //      Database().writeAdvert(userID, busName, advert)
                }else
                    Toast.makeText(context, "Please register/login to your account", Toast.LENGTH_SHORT).show()

            }else
                Toast.makeText(context, "Bussiness name or advert is null", Toast.LENGTH_SHORT).show()
        }
*/



        return binding.root
    }


}