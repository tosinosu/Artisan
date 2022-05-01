package com.tostech.artisan


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
//import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.flutterwave.raveandroid.RavePayActivity
import com.flutterwave.raveandroid.RaveUiManager
import com.flutterwave.raveandroid.data.Utils
import com.flutterwave.raveandroid.rave_java_commons.Meta
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants
import com.flutterwave.raveandroid.rave_java_commons.SubAccount
import com.flutterwave.raveandroid.rave_presentation.FeeCheckListener
import com.flutterwave.raveandroid.rave_presentation.card.CardPaymentCallback
import com.flutterwave.raveandroid.rave_presentation.card.SavedCardsListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.tostech.artisan.data.SubscriptionData
import com.tostech.artisan.databinding.SubscriptionBinding
import org.json.JSONException
import java.io.IOException
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class Subscription: Fragment() {

    private var _binding: SubscriptionBinding? = null
    private val binding get() = _binding!!
    var databaseRef = Firebase.database.reference
    var meta: List<Meta> = ArrayList()
    lateinit var mActivity: FragmentActivity

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
    var startPayBtn: MaterialButton? = null
    var subPlan: TextView? = null

    var country = listOf("Nigeria (Naira)", "Other Countries (US Dollars)")
    val firebaseUserID = Firebase.auth.currentUser?.uid


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = SubscriptionBinding.inflate(inflater, container, false)
        setUpToolbar()

         emailEt = binding.emailEt
         amountEt = binding.amountEt
         narrationEt = binding.narrationTV
         currencyEt = binding.currencyEt
         countrySpinner = binding.countryEt
         fNameEt = binding.fNameEt
         lNameEt = binding.lnameEt
         phoneNumberEt = binding.phoneNumberEt
        subPlan = binding.subMonths
        startPayBtn = binding.startPayBtn


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


        getSubDetails()
        startPayBtn!!.setOnClickListener {
            validateEntries()

    }

        return binding.root


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

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
          //  withTheme(R.style.TestNewTheme)
            showStagingLabel(true)
            setAmount(amount.toDouble())
            setCurrency(currency)
            setEmail(email)
            setfName(fName)
            setlName(lName)
            setPhoneNumber(phoneNumber, true)
            setNarration(narration)
            setPublicKey("FLWPUBK-2ea91acb74b7437f9a168ed5071777b5-X")
            setEncryptionKey("6db402949f37765bed3af5d1")
            setTxRef(txRef)
            onStagingEnv(false)
            isPreAuth(false)
            // setMeta()
            shouldDisplayFee(false)
        }
            ravePay.initialize()
        }

    private fun getSubDetails(){

            val ref = databaseRef.child("User/$firebaseUserID/subscribe")
                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val subData = snapshot.getValue<SubscriptionData>()
                            val subCreated = subData!!.createdAt

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                Log.d("ExpDate", subCreated.toString())

                                val formatter =
                                    LocalDateTime.parse(subCreated, DateTimeFormatter.ISO_DATE_TIME)
                                val formattedDate= formatter.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
                                val subExpire = formatter.plusMonths(3)
                                val currentDate = LocalDateTime.now()
  //                              Log.d("ExpDate", subExpire.toString())

                                if (subData!!.status == "successful" && currentDate <= subExpire) {
                                    binding.subscription.text = "active"
                                    binding.subscription.setTextColor(Color.GREEN)
                                    binding.date.setTextColor((resources.getColor(R.color.colorPrimary)))
                                    binding.expiration.setTextColor((resources.getColor(R.color.colorPrimary)))


                                } else {
                                    binding.subscription.text = "inactive"
                                    binding.subscription.setTextColor(Color.RED)
                                    binding.date.setTextColor(Color.RED)
                                    binding.expiration.setTextColor(Color.RED)
                                    databaseRef.child("User/$firebaseUserID/sub_status").setValue(3)

                                }
                                binding.date.text = formattedDate
                                //binding.expiration.text = subExpire.toString()
                            }
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                                val parser = SimpleDateFormat(("yyyy-MM-dd'T'HH:mm:ss'.000Z'"), Locale.getDefault())
                                val parser2 = SimpleDateFormat(("yyyy-MM-dd'T'HH:mm:ss"), Locale.getDefault())
                                val formatter = SimpleDateFormat(("yyyy-MM-dd HH:mm:ss"), Locale.getDefault())
    //                            Log.d("CurDate", subCreated.toString())
                                val output = parser.parse(subCreated!!)!!
//                                Log.d("output", output.toString())


                                val cal = Calendar.getInstance()
                                val currentCalenderDate = Calendar.getInstance().time

                                cal.time = output
                                cal.add(Calendar.MONTH, 3)
                                val expireCalendarDate = cal.time
  //                              Log.d("ExpDate", expireCalendarDate.toString())

                                if (subData!!.status == "successful" && currentCalenderDate <= expireCalendarDate) {
                                    binding.subscription.text = "active"
                                    binding.subscription.setTextColor(Color.GREEN)
                                    binding.date.setTextColor((resources.getColor(R.color.colorPrimary)))
                                    binding.expiration.setTextColor((resources.getColor(R.color.colorPrimary)))

                                } else {
                                    binding.subscription.text = "inactive"
                                    binding.subscription.setTextColor(Color.RED)
                                    binding.date.setTextColor(Color.RED)
                                    binding.expiration.setTextColor(Color.RED)

                                    databaseRef.child("User/$firebaseUserID/sub_status").setValue(3)

                                }
                                binding.date.text = formatter.format(output)
                              //  binding.expiration.text = formatter.format(expireCalendarDate)

                            }
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let { mActivity = it}

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val hashMapSubscription = HashMap<String, String?>()
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            val message = data.getStringExtra("response")
//            Log.d("SubData", message!!)

            try{
                val jsonElement = JsonParser.parseString(message)
                val jObj = jsonElement.asJsonObject
                val  jObject = jObj.getAsJsonObject("data")

                // Fetch id store it in variable
                val id = jObject.get("id").asString
                val createdAt = jObject.get("createdAt").asString
                val amount = jObject.get("amount").asString
                val charged_amount = jObject.get("charged_amount").asString
                val AccountId = jObject.get("AccountId").asString
                val orderRef = jObject.get("orderRef").asString
                val flwRef = jObject.get("flwRef").asString
                val narration = jObject.get("narration").asString
                val chargeResponseCode = jObject.get("chargeResponseCode").asString
                val appfee = jObject.get("appfee").asString
                val status = jObject.get("status").asString
                val updatedAt = jObject.get("updatedAt").asString
              //  val phone = jObject.get("customer.phone").asString
               // val fullname = jObject.get("customer.fullName").asString
                //val email = jObject.get("customer.email").asString
                val fraud_status = jObject.get("fraud_status").asString


                hashMapSubscription["id"] = id
                hashMapSubscription["AccountId"] = AccountId
                hashMapSubscription["amount"] = amount
                hashMapSubscription["charged_amount"] = charged_amount
                hashMapSubscription["orderRef"] = orderRef
                hashMapSubscription["flwRef"] = flwRef
                hashMapSubscription["chargeResponseCode"] = chargeResponseCode
                hashMapSubscription["narration"] = narration
                hashMapSubscription["createdAt"] = createdAt
                hashMapSubscription["appfee"] = appfee
                hashMapSubscription["status"] = status
                hashMapSubscription["updatedAt"] = updatedAt
              //  hashMapSubscription["phone"] = phone
               // hashMapSubscription["fullname"] = fullname
                //hashMapSubscription["email"] = email
                hashMapSubscription["fraud_status"] = fraud_status

            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (ex: NullPointerException){
                ex.printStackTrace()
            }

            if (message != null) {

                val ref = databaseRef.child("User/$firebaseUserID/subscribe")

                ref.setValue(hashMapSubscription)
            }
            when (resultCode) {
                RavePayActivity.RESULT_SUCCESS -> {
                    if (firebaseUserID != null) {
                        databaseRef.child("User").child(firebaseUserID).child("sub_status").setValue(2).addOnSuccessListener {
                            Toast.makeText(requireContext(), "PAYMENT SUCCESSFULLY MADE", Toast.LENGTH_SHORT).show()
                        }
                    }else{
                        Toast.makeText(requireContext(), "PLEASE REGISTER", Toast.LENGTH_SHORT).show()

                    }
                }
                RavePayActivity.RESULT_ERROR -> {
                    Toast.makeText(requireContext(), "PAYMENT NOT SUCCESSFUL", Toast.LENGTH_SHORT).show()
                }
                RavePayActivity.RESULT_CANCELLED -> {
                    Toast.makeText(requireContext(), "PAYMENT CANCELLED", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }

        return super.onActivityResult(requestCode, resultCode, data)

    }

    private fun setUpToolbar() {
        val mainActivity = mActivity as MainActivity
        val  navigationView : NavigationView? = mActivity.findViewById(R.id.nav_view)
        mainActivity.setSupportActionBar(binding.toolbar)
        val navController = NavHostFragment.findNavController(this)
        val appBarConfiguration =  mainActivity.appBarConfiguration
        NavigationUI.setupActionBarWithNavController(mainActivity, navController, appBarConfiguration!!)
        NavigationUI.setupWithNavController(navigationView!!,navController)

        setHasOptionsMenu(true)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()
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