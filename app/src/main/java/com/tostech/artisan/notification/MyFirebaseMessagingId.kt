package com.tostech.artisan.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tostech.artisan.MessageChatActivity

class MyFirebaseMessagingId : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val  sented = remoteMessage.data["sented"]
        val  user = remoteMessage.data["user"]
        val  sharedPref = getSharedPreferences("currentUser", Context.MODE_PRIVATE)
        val  currentOnLineUser = sharedPref.getString("currentUser", "none")

        val firebaseUser = FirebaseAuth.getInstance().currentUser

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

    private fun sendNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val notification = remoteMessage.notification
        val j = user!!.replace("\\D".toRegex(), "").toInt()
        val intent = Intent(this, MessageChatActivity::class.java )
        val bundle = Bundle()
        bundle.putString("userid", user)

        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)


        val builder: NotificationCompat.Builder? = NotificationCompat.Builder(this)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setSmallIcon(icon!!.toInt())
            .setContentText(body)
            .setSound(defaultSound)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val noti = getSystemService((Context.NOTIFICATION_SERVICE)) as NotificationManager
        var i=0
        if(j > 0){
            i = j
        }

        noti.notify(i, builder!!.build())
    }


    private fun sendOreoNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]

        val notification = remoteMessage.notification
        val j = user!!.replace("\\D".toRegex(), "").toInt()

        val intent = Intent(this, MessageChatActivity::class.java )
         val bundle = Bundle()
        bundle.putString("userid", user)

        intent.putExtras(bundle)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, j, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val oreoNotification = OreoNotification(this)

        val builder: Notification.Builder = oreoNotification.getOreoNotification(title,body, pendingIntent, defaultSound)

        var i=0
        if(j > 0){
            i = j
        }

        oreoNotification.getManager!!.notify(i, builder.build())
    }
}