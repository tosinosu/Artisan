package com.tostech.artisan.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.tostech.artisan.R
import com.tostech.artisan.databinding.FragmentAboutBinding
import com.tostech.artisan.databinding.VerifyBinding

class AboutFragment : Fragment() {

    private lateinit var categoryViewModel: AboutViewModel
    private lateinit var binding: FragmentAboutBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_about, container, false)


        return binding.root
    }
}