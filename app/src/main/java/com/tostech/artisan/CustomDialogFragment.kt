package com.tostech.artisan

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isGone
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide

class CustomDialogFragment: DialogFragment() {
    private lateinit var close: ImageButton
    private lateinit var text : TextView
    private lateinit var image: ImageView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val inflate = inflater.inflate(R.layout.image_dialog, container, false)




         close = inflate.findViewById<ImageButton>(R.id.imageButton)
         image = inflate.findViewById<ImageView>(R.id.dialogImage)
         text = inflate.findViewById<TextView>(R.id.dialogTxt)


        val textInput = requireArguments().getString("textInput")
        val ref = requireArguments().getString("ref")

       // Log.v("dialogc", textInput!!+ " "+ ref!! +"null")
        Glide.with(requireContext()).load(ref).into(image)

        if (ref == "null")
            text.isGone = true
        text.text = textInput

        return inflate
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR)
        dialog.requestWindowFeature(Window.FEATURE_RIGHT_ICON)

        return dialog
    }

    fun toast(message:String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    }
}