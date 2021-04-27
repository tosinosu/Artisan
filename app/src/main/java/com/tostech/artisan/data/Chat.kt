package com.tostech.artisan.data


data class Chat (
    var receiver: String = "",
    var sender: String = "",
    var message: String = "",
    var url: String = "",
    var isseen: Boolean = false,
    var messageID: String = ""

)