package com.tostech.artisan.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Color.WHITE
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.os.bundleOf
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tostech.artisan.MainActivity
import com.tostech.artisan.MessageChatActivity
import com.tostech.artisan.R
import com.tostech.artisan.notification.Constants.Companion.CHANNEL_ID
import com.tostech.artisan.notification.Constants.Companion.CHANNEL_NAME
//import okhttp3.internal.notify
import kotlin.random.Random

class FirebaseService : FirebaseMessagingService() {


    companion object{
        var sharedPref:SharedPreferences? = null

        var token:String?
            get(){
                return sharedPref?.getString("token","")
            }
            set(value){
                sharedPref?.edit()?.putString("token",value)?.apply()
            }
    }

    var token1 = ""

    override fun onNewToken(p0: String) {

        super.onNewToken(p0)
        token = p0


    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val  sented = remoteMessage.data["senderId"]
        val  user = remoteMessage.data["user"]
        Log.v("Sented", "sented is $sented username is $user")

        val  sharedPref = getSharedPreferences("PREFS", MODE_PRIVATE)
        val  currentOnLineUser = sharedPref.getString("currentUser", "none")

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        Log.v("Sented", "firebaseUser is $firebaseUser currentOnLineUser is $currentOnLineUser")
        Log.v("Sented", "currentOnLineUser is $currentOnLineUser User is $user")

        if (firebaseUser != null && sented == firebaseUser.uid)
        {
            if (!currentOnLineUser.equals(user)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOreoNotification(remoteMessage)
                } else {
                    sendNotification(remoteMessage)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){

        val channelName = "Artisan"
        val channel = NotificationChannel(CHANNEL_ID,channelName,IMPORTANCE_HIGH).apply {
            description="MY ARTISAN"
            enableLights(true)
            lightColor = getColor(R.color.colorWhite)
        }
        notificationManager.createNotificationChannel(channel)

    }

private fun sendNotification(remoteMessage: RemoteMessage) {


    val user = remoteMessage.data["user"]
    val title = remoteMessage.data["title"]
    val body = remoteMessage.data["body"]

    val notification = remoteMessage.notification
    val j = user!!.replace("\\D".toRegex(), "").toInt()
    val intent = Intent(this, MessageChatActivity::class.java )
    val bundle = bundleOf("visit_id" to user)
    val pintent = Intent(this, MessageChatActivity::class.java)
    val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
        addNextIntentWithParentStack(pintent)
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    pintent.putExtras(bundle)

    intent.putExtras(bundle)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)
    val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


    val notificationId = Random.nextInt()
    val builder: NotificationCompat.Builder? = NotificationCompat.Builder(this, CHANNEL_ID).apply {
        setContentIntent(resultPendingIntent)
        setContentTitle(title)
        setSmallIcon(R.drawable.ic_baseline_notifications_24)
        setContentText(body)
        setSound(defaultSound)
        setContentIntent(pendingIntent)
        setAutoCancel(true)
    }
    with(NotificationManagerCompat.from(this)) {
        notify(notificationId, builder!!.build())
    }
    val noti = getSystemService((Context.NOTIFICATION_SERVICE)) as NotificationManager
    /*var i=0
    if(j > 0){
        i = j
    }*/

  //  noti.notify(i, builder!!.build())
}

    @RequiresApi(Build.VERSION_CODES.O)
private fun sendOreoNotification(remoteMessage: RemoteMessage) {
    val user = remoteMessage.data["user"]
    val title = remoteMessage.data["title"]
    val body = remoteMessage.data["body"]
    Log.v("Sented", "sOreo username is $user")

    val pintent = Intent(this, MessageChatActivity::class.java)
    val bundle = bundleOf("visit_id" to user)
    pintent.putExtras(bundle)
    val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
        addNextIntentWithParentStack(pintent)
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    val notification = remoteMessage.notification
    val j = user!!.replace("\\D".toRegex(), "").toInt()

    /*val intent = Intent(this, MessageChatActivity::class.java )
    *//*val bundle = Bundle()
    bundle.putString()*//*


   // intent.putExtras(bundle)
    //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    //val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)
    val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

    val oreoNotification = OreoNotification(this)

    val builder: Notification.Builder = oreoNotification.getOreoNotification(title, body, resultPendingIntent, defaultSound)

    val notificationId = Random.nextInt()

    oreoNotification.getManager!!.notify(notificationId, builder.build())*/
        val notificationId = Random.nextInt()



    val channelName = "Artisan"
    val channel = NotificationChannel(CHANNEL_ID,channelName,IMPORTANCE_HIGH)
         channel.description = CHANNEL_NAME
        channel.enableLights(true)
        channel.lightColor = getColor(R.color.colorWhite)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.createNotificationChannel(channel)

        val notifBuilder = Notification.Builder(applicationContext, CHANNEL_ID).apply {
            setContentIntent(resultPendingIntent)
            setContentTitle(title)
            setSmallIcon(R.drawable.ic_baseline_notifications_24)
            setContentText(body)
            setAutoCancel(true)
        }
        with(NotificationManagerCompat.from(this)){
               notify(notificationId, notifBuilder.build())
           }


    }

}