package com.tostech.artisan.notification

data class PushNotification(
    var data: NotificationData,
    var to: String
)