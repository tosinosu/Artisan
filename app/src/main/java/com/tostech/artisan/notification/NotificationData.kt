package com.tostech.artisan.notification

data class NotificationData(
    val user: String? = "",
    val body: String? = "",
    val titles: String? = "",
    val senderId: String? = ""
)