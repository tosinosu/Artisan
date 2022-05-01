package com.tostech.artisan.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties

data class ContactData (

    val phone: String? = "",
    val country: String? = "",
    val lga: String? = "",
    val state: String? = "",
    val office_address: String? = "",
    val instagram: String? = "",
    val facebook: String? = "",
    val twitter: String? = "",
    val linkedIn: String? = "",
    val whatsApp: String? = "",
    val others: String? = ""

)