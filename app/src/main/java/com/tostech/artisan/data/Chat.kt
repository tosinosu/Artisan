package com.tostech.artisan.data

import com.google.gson.annotations.SerializedName


data class Chat (
    var receiver: String = "",
    var sender: String = "",
    var message: String = "",
    var url: String = "",
    var isseen: Boolean = false,
    var messageId: String = ""

)
data class ChatArray(
   // @SerializedName("array")
    var chat: ArrayList<Chat>? = null
)