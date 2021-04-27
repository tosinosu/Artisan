package com.tostech.artisan


import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment


import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat

import androidx.annotation.Nullable;
import com.flutterwave.raveandroid.RavePayActivity
import com.flutterwave.raveandroid.RaveUiManager
import com.flutterwave.raveandroid.data.Utils
import com.flutterwave.raveandroid.rave_core.models.SavedCard
import com.flutterwave.raveandroid.rave_java_commons.Meta
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants
import com.flutterwave.raveandroid.rave_java_commons.SubAccount

import com.tostech.artisan.databinding.SubscriptionBinding



class Subscription: Fragment(){

    private lateinit var binding: SubscriptionBinding


    var meta: List<Meta> = ArrayList()

    var subAccounts: List<SubAccount> = ArrayList()
    var addSubaccountsLayout: LinearLayout? = null
    var expiryDetailsLayout: LinearLayout? = null
    private var vendorListTXT: TextView? = null


    var emailEt: EditText? = null
    var amountEt: TextView? = null
    var publicKeyEt: EditText? = null
    var encryptionKeyEt: EditText? = null
    var txRef: String? = null
    var narrationEt: EditText? = null
    var currencySpinner: Spinner? = null
    var  countrySpinner: Spinner? = null
    var fNameEt: EditText? = null
    var lNameEt: EditText? = null
    var phoneNumberEt: EditText? = null
    var durationEt: EditText? = null
    var frequencyEt: EditText? = null
    var startPayBtn: Button? = null

    var country = listOf<String>("Nigeria", "Other Countries")
    var currency = listOf<String>("NGN", "USSD")



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.subscription, container, false)

         emailEt = binding.emailEt
         amountEt = binding.amountEt
         narrationEt = binding.narrationTV
         currencySpinner = binding.currencyEt
         countrySpinner = binding.countryEt
         fNameEt = binding.fNameEt
         lNameEt = binding.lnameEt
         phoneNumberEt = binding.phoneNumberEt

        val countryAdapter = ArrayAdapter(
            requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            country
        )
        val currencyAdapter = ArrayAdapter(
            requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            currency
        )

        countrySpinner!!.adapter = countryAdapter

        currencySpinner!!.adapter = currencyAdapter

        val selectedCountry = binding.countryEt.selectedItem.toString()
        val selectedCurrency = binding.currencyEt.selectedItem.toString()

        if (selectedCurrency == "NGN"){
            amountEt?.setText("2500")
        }

        if (selectedCurrency == "USSD"){
            amountEt?.setText("$5")
        }



        binding.startPaymentBtn.setOnClickListener {
            validateEntries()
        }

        return binding.root
    }


/*    private fun clear() {
        subAccounts.clear()
        vendorListTXT!!.text = "Your current vendor refs are: "
        addSubaccountsLayout!!.visibility = View.GONE
        addSubAccountsSwitch.setChecked(false)
    }*/

    private fun validateEntries() {
        clearErrors()

        txRef = Database().readUserID().toString()

        val email: String = emailEt?.text.toString()
        var amount: String = amountEt?.text.toString()
        val country: String = countrySpinner?.selectedItem.toString()
        val narration: String = narrationEt?.text.toString()
        val currency: String = currencySpinner?.selectedItem.toString()
        val fName: String = fNameEt?.text.toString()
        val lName: String = lNameEt?.text.toString()
        val phoneNumber: String = phoneNumberEt?.text.toString()
        val accountDuration: String = durationEt?.text.toString()
        val accountPaymentFrequency: String = frequencyEt?.text.toString()
        var valid = true
        if (amount.length == 0) {
            amount = "0"
        }

        //isAmountValid for compulsory fields
        if (fName.length < 2) {
            valid = false
            fNameEt?.error = "A valid first name is required"
        }
        if (lName.length < 2) {
            valid = false
            fNameEt?.error = "A valid last name is required"
        }
    if (!Utils.isEmailValid(email)) {
                valid = false
                emailEt?.error = "A valid email is required"
            }

        if (txRef!!.length < 1) {
            valid = false
            Toast.makeText(requireContext(), "A valid txRef key is required, please register or sign in", Toast.LENGTH_SHORT)
                .show()
        }
        if (currency.length < 1) {
            valid = false
            Toast.makeText(
                requireContext(),
                "A valid currency code is required",
                Toast.LENGTH_SHORT
            ).show()
        }

        if (valid) {
            checkout(email, fName, lName, phoneNumber, narration, txRef, amount, currency)

        }

    }


    fun checkout(email: String, fName:  String, lName: String, phoneNumber: String, narration: String?, txRef: String?, amount: String, currency: String){

        val ravePay = RaveUiManager(activity)
                .acceptMpesaPayments(false)
                .acceptAccountPayments(true)
                .acceptCardPayments(true)
                .allowSaveCardFeature(false)
                .acceptAchPayments(false)
                .acceptGHMobileMoneyPayments(false)
                .acceptUgMobileMoneyPayments(false)
                .acceptZmMobileMoneyPayments(false)
                .acceptRwfMobileMoneyPayments(false)
                .acceptUkPayments(false)
                .acceptSaBankPayments(false)
              //  .acceptFrancMobileMoneyPayments(false)
                .acceptBankTransferPayments(false)
                .acceptUssdPayments(true)
                .acceptBarterPayments(true)
               // .withTheme(R.style.TestNewTheme)
                .showStagingLabel(true)
                .setAmount(amount.toDouble())
                .setCurrency(currency)
                .setEmail(email)
                .setfName(fName)
                .setlName(lName)
                .setPhoneNumber(phoneNumber, false)
                .setNarration(narration)
                .setPublicKey("FLWPUBK_TEST-f7dfa48b02cea0f2aaf1b4ff9817ee58-X")
                .setEncryptionKey("FLWSECK_TEST4b22990a7014")
                .setTxRef(txRef)
                .onStagingEnv(false)
//                .isPreAuth(false)
                //.setMeta(false)
                .shouldDisplayFee(true)

        ravePay.initialize()

        }



     override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            val message = data.getStringExtra("response")
            if (message != null) {
                Log.d("rave response", message)
            }
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                Toast.makeText(requireContext(), "SUCCESS $message", Toast.LENGTH_SHORT).show()
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(requireContext(), "ERROR $message", Toast.LENGTH_SHORT).show()
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(requireContext(), "CANCELLED $message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearErrors() {
        emailEt?.error = null
        amountEt?.error = null
        publicKeyEt?.error = null
        encryptionKeyEt?.error = null
       // txRefEt?.error = null
        narrationEt?.error = null
       // currencyEt?.error = null
       // countryEt?.error = null
        fNameEt?.error = null
        lNameEt?.error = null
        durationEt?.error = null
        frequencyEt?.error = null
    }
}