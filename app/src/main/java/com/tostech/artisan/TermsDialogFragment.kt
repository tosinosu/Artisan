package com.tostech.artisan

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle

import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import java.io.IOException
import java.nio.charset.Charset

class TermsDialogFragment: DialogFragment() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val terms_condition = getJsonFromAsset(requireContext(), "term.txt")
        val inflater = requireActivity().layoutInflater
        val view =inflater.inflate(R.layout.terms_condition_dialog, null)
        val textViewTerms = view.findViewById<TextView>(R.id.dialogTxt)
        textViewTerms.text = terms_condition

        return activity?.let {
            val builder = AlertDialog.Builder(it)

            builder.setView(view)
                .setPositiveButton("Ok", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialog!!.cancel()
                })

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(resources.getColor(R.color.colorPrimary))
        (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
    }

    fun toast(message:String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    }

    fun getJsonFromAsset(context: Context, filename: String): String? {
        var terms: String? = ""
        val charset: Charset = Charsets.UTF_8
        try {
            val myTermsFile = context.assets.open(filename)
            val size = myTermsFile.available()
            val buffer = ByteArray(size)
            myTermsFile.read(buffer)
            myTermsFile.close()
            terms = String(buffer, charset)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return terms
    }
}