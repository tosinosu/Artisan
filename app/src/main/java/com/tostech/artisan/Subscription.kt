package com.tostech.artisan


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.flutterwave.raveandroid.RavePayActivity
import com.flutterwave.raveandroid.RaveUiManager
import com.flutterwave.raveandroid.data.Utils
import com.flutterwave.raveandroid.rave_java_commons.Meta
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants
import com.flutterwave.raveandroid.rave_java_commons.SubAccount
import com.flutterwave.raveandroid.rave_presentation.FeeCheckListener
import com.flutterwave.raveandroid.rave_presentation.card.CardPaymentCallback
import com.flutterwave.raveandroid.rave_presentation.card.SavedCardsListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.tostech.artisan.databinding.SubscriptionBinding


class Subscription: Fragment(){

    private lateinit var binding: SubscriptionBinding

    var databaseRef = Firebase.database.reference
    var meta: List<Meta> = ArrayList()
    var progressDialog: ProgressDialog? = null

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
    var currencyEt: TextView? = null
    var  countrySpinner: Spinner? = null
    var fNameEt: EditText? = null
    var lNameEt: EditText? = null
    var phoneNumberEt: EditText? = null
    var durationEt: EditText? = null
    var frequencyEt: EditText? = null
    var startPayBtn: Button? = null
    var subPlan: TextView? = null

    var country = listOf("Nigeria (Naira)", "Other Countries (US Dollars)")
    val firebaseUserID = Firebase.auth.currentUser?.uid


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.subscription, container, false)

        val act = requireActivity().baseContext.toString()
        Toast.makeText(context, act, Toast.LENGTH_SHORT).show()

         emailEt = binding.emailEt
         amountEt = binding.amountEt
         narrationEt = binding.narrationTV
         currencyEt = binding.currencyEt
         countrySpinner = binding.countryEt
         fNameEt = binding.fNameEt
         lNameEt = binding.lnameEt
         phoneNumberEt = binding.phoneNumberEt
        subPlan = binding.subMonths


        val countryAdapter = ArrayAdapter(
            requireActivity(),
            R.layout.support_simple_spinner_dropdown_item,
            country
        )


        countrySpinner!!.adapter = countryAdapter

        countrySpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

                if (position == 0){
                    amountEt?.text = "2000"
                    currencyEt?.text = "NGN"
                    subPlan?.text = "Valid for Three Months"
                }else{
                    currencyEt?.text = "USSD"
                    amountEt?.text = "5"
                    subPlan?.text = "Three Months Subscription"
                }

            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
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
        val currency: String = currencyEt?.text.toString()
        val fName: String = fNameEt?.text.toString()
        val lName: String = lNameEt?.text.toString()
        val phoneNumber: String = phoneNumberEt?.text.toString()
        val accountDuration: String = durationEt?.text.toString()
        val accountPaymentFrequency: String = frequencyEt?.text.toString()
        var valid = true

        if (amount.isEmpty()) {
            amount = "0"
        }

        //isAmountValid for compulsory fields
        if (fName.length < 2) {
            valid = false
            fNameEt?.error = "A valid first name is required"
        }
        if (lName.length < 2) {
            valid = false
            lNameEt?.error = "A valid last name is required"
        }
       if (!Utils.isEmailValid(email)) {
                valid = false
                emailEt?.error = "A valid email is required"
            }
        if (phoneNumber == "null" || phoneNumber.length < 5) {
                        valid = false
                        phoneNumberEt?.error = "A valid phone number is required"
        }

        if (txRef!!.isNullOrEmpty()) {
            valid = false
            Toast.makeText(
                requireContext(),
                "A valid user is required, please register or sign in",
                Toast.LENGTH_SHORT
            )
                .show()
        }

        if (valid) {
            checkout(email, fName, lName, phoneNumber, narration, txRef, amount, currency)

        }

    }


    fun checkout(
        email: String,
        fName: String,
        lName: String,
        phoneNumber: String?,
        narration: String?,
        txRef: String?,
        amount: String,
        currency: String
    ){


        val ravePay = RaveUiManager(this).apply {
            acceptMpesaPayments(false)
            acceptAccountPayments(true)
            acceptCardPayments(true)
            allowSaveCardFeature(false)
            acceptAchPayments(false)
            acceptGHMobileMoneyPayments(false)
            acceptUgMobileMoneyPayments(false)
            acceptZmMobileMoneyPayments(false)
            acceptRwfMobileMoneyPayments(false)
            acceptUkPayments(false)
            acceptSaBankPayments(false)
            acceptFrancMobileMoneyPayments(false, null)
            acceptBankTransferPayments(false)
            acceptUssdPayments(true)
            acceptBarterPayments(false)
            //withTheme(R.style.TestNewTheme)
            showStagingLabel(true)
            setAmount(amount.toDouble())
            setCurrency(currency)
            setEmail(email)
            setfName(fName)
            setlName(lName)
            setPhoneNumber(phoneNumber, true)
            setNarration(narration)
            setPublicKey("FLWPUBK_TEST-1b28a4b97c230106164e2d15060eb931-X")
            setEncryptionKey("FLWSECK_TEST8e2f00f82e1e")
            setTxRef(txRef)
            onStagingEnv(true)
            isPreAuth(false)
            // setMeta()
            shouldDisplayFee(false)
        }
            ravePay.initialize()
        }

    /* override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         super.onActivityResult(requestCode, resultCode, data)

         Toast.makeText(context, "OnActivity is called", Toast.LENGTH_SHORT).show()
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            val message = data.getStringExtra("response")
            if (message != null) {
                val ref = databaseRef.child("User/$firebaseUserID/subscribe")
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            ref.child(message)
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })

               // Log.d("rave response", message)
            }
            when (resultCode) {
                RavePayActivity.RESULT_SUCCESS -> {
                    Toast.makeText(requireContext(), "SUCCESS $message", Toast.LENGTH_SHORT).show()
                }
                RavePayActivity.RESULT_ERROR -> {
                    Toast.makeText(requireContext(), "ERROR $message", Toast.LENGTH_SHORT).show()
                }
                RavePayActivity.RESULT_CANCELLED -> {
                    Toast.makeText(requireContext(), "CANCELLED $message", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }

         return super.onActivityResult(requestCode, resultCode, data)

         *//* if (resultCode === RaveConstants.RESULT_SUCCESS) {
              when (requestCode) {
                  RaveConstants.PIN_REQUEST_CODE -> {
                      val pin = data!!.getStringExtra(PinFragment.EXTRA_PIN)
                      // Use the collected PIN
                      cardPayManager.submitPin(pin)
                  }
                  RaveConstants.ADDRESS_DETAILS_REQUEST_CODE -> {
                      val streetAddress = data!!.getStringExtra(AVSVBVFragment.EXTRA_ADDRESS)
                      val state = data.getStringExtra(AVSVBVFragment.EXTRA_STATE)
                      val city = data.getStringExtra(AVSVBVFragment.EXTRA_CITY)
                      val zipCode = data.getStringExtra(AVSVBVFragment.EXTRA_ZIPCODE)
                      val country = data.getStringExtra(AVSVBVFragment.EXTRA_COUNTRY)
                      val address = AddressDetails(streetAddress, city, state, zipCode, country)

                      // Use the address details
                      cardPayManager.submitAddress(address)
                  }
                  RaveConstants.WEB_VERIFICATION_REQUEST_CODE ->                     // Web authentication complete, proceed
                      cardPayManager.onWebpageAuthenticationComplete()
                  RaveConstants.OTP_REQUEST_CODE -> {
                      val otp = data!!.getStringExtra(OTPFragment.EXTRA_OTP)
                      // Use OTP
                      cardPayManager.submitOtp(otp)
                  }
              }
          }

          if (requestCode === RaveConstants.RAVE_REQUEST_CODE && data != null) {
              val message = data.getStringExtra("response")
              if (message != null) {
                  Log.d("rave response", message)
              }
              if (resultCode === RavePayActivity.RESULT_SUCCESS) {
                  Toast.makeText(this, "SUCCESS $message", Toast.LENGTH_SHORT).show()
              } else if (resultCode === RavePayActivity.RESULT_ERROR) {
                  Toast.makeText(this, "ERROR $message", Toast.LENGTH_SHORT).show()
              } else if (resultCode === RavePayActivity.RESULT_CANCELLED) {
                  Toast.makeText(this, "CANCELLED $message", Toast.LENGTH_SHORT).show()
              }
          } else if (requestCode === RaveConstants.WEB_VERIFICATION_REQUEST_CODE) {
              cardPayManager.onWebpageAuthenticationComplete()
          } else {
              super.onActivityResult(requestCode, resultCode, data)
          }*//*
    }
*/
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


   /* fun showProgressIndicator(active: Boolean) {
        try {
            if (isFinishing()) {
                return
            }
            if (progressDialog == null) {
                progressDialog = ProgressDialog(this)
                progressDialog!!.setCanceledOnTouchOutside(false)
                progressDialog!!.setMessage("Please wait...")
            }
            if (active && !progressDialog!!.isShowing()) {
                progressDialog!!.show()
            } else {
                progressDialog.dismiss()
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }*/
}