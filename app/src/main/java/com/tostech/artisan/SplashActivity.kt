package com.tostech.artisan

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
//import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlin.NullPointerException

/**
 * Number of seconds to count down before showing the app open ad. This simulates the time needed
 * to load the app.
 */
private const val COUNTER_TIME = 5L

private const val LOG_TAG = "SplashActivity"
private lateinit var mDatabase: DatabaseReference
private lateinit var firebaseUserID: String


/** Splash Activity that inflates splash activity xml. */
class SplashActivity : AppCompatActivity() {

  private var secondsRemaining: Long = 0L

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_splash)
      val counterTextView: TextView = findViewById(R.id.network)

      MobileAds.initialize(this) {}
      val testDeviceIds = listOf("0188510711703001", "04714259AM007470", "8243AB1F26F0FA441BA02D4283E4DF8C")
      val config = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
      MobileAds.setRequestConfiguration(config)

      if (!checkNetwork()) {
          counterTextView.text  = "No network connection"
      }
      try {
          firebaseUserID = Firebase.auth.currentUser!!.uid
          // Create a timer so the SplashActivity will be displayed for a fixed amount of time.
          createTimer(COUNTER_TIME)
      }catch (ex: NullPointerException){
          startMainActivity()

      }


  }

  /**
   * Create the countdown timer, which counts down to zero and show the app open ad.
   *
   * @param seconds the number of seconds that the timer counts down from
   */
  private fun createTimer(seconds: Long) {

   // val counterTextView: TextView = findViewById(R.id.timer)
    val circularProgressIndicator: CircularProgressIndicator = findViewById(R.id.progress_bar)
      val handler = Handler()
      val mainExecutor = ContextCompat.getMainExecutor(this)

      circularProgressIndicator.progress = 0.5.toInt()
      circularProgressIndicator.max = 10

    val countDownTimer: CountDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            secondsRemaining = millisUntilFinished / 1000 + 1

            /*   Thread{
                // while (secondsRemaining < 0){
                handler.postDelayed(millisUntilFinished){
                    circularProgressIndicator.progress = secondsRemaining.toInt()
                }

                try{
                    Thread.sleep(millisUntilFinished)
                }catch (ex:InterruptedException){
                    ex.printStackTrace()
                }
                // }
            }.start()*/

            /*   Handler(Looper.getMainLooper()).postDelayed({
                circularProgressIndicator.progress = secondsRemaining.toInt()

            }, millisUntilFinished)
            }*/
            mainExecutor.execute {
                circularProgressIndicator.progress = secondsRemaining.toInt()

            }
        }

           // counterTextView.text = "App is done loading in: $secondsRemaining"
       // }

        override fun onFinish() {
            secondsRemaining = 0
            //counterTextView.text = "Done."
            circularProgressIndicator.max

            mDatabase = Firebase.database.reference.child("User")

                mDatabase.child(firebaseUserID!!)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val status = snapshot.child("sub_status").value.toString()

                            if (status != "2") {
                                val application = application as? MyApplication

                                // If the application is not an instance of MyApplication, log an error message and
                                // start the MainActivity without showing the app open ad.
                                if (application == null) {
                                    //Log.e(LOG_TAG, "Failed to cast application to MyApplication.")
                                    startMainActivity()
                                    return
                                }

                                // Show the app open ad.
                                application.showAdIfAvailable(
                                    this@SplashActivity,
                                    object : MyApplication.OnShowAdCompleteListener {
                                        override fun onShowAdComplete() {
                                            startMainActivity()
                                        }

                                    })
                                application.onMoveToForeground()
                            }else{
                                startMainActivity()
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })


        }
    }

    countDownTimer.start()
  }

  /** Start the MainActivity. */
  fun startMainActivity() {
    val intent = Intent(this, LoginActivity2::class.java)
    startActivity(intent)
  }

    private fun checkNetwork(): Boolean{
        //  if (Build.VERSION.SDK_INT  <= Build.VERSION_CODES.Q) {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected = activeNetwork?.isConnectedOrConnecting == true

        return isConnected
    }
}
