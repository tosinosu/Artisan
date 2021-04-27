package com.tostech.artisan.ui.artisan

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
import com.tostech.artisan.databinding.ArtisanListBinding
import com.tostech.artisan.databinding.FragmentArtisanBinding

class ArtisanFragment : Fragment() {

    private lateinit var artisanViewModel: ArtisanViewModel
    private var _binding: FragmentArtisanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        artisanViewModel =
                ViewModelProviders.of(this).get(ArtisanViewModel::class.java)

        artisanViewModel.text.observe(viewLifecycleOwner, Observer {

        })

        _binding = FragmentArtisanBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}