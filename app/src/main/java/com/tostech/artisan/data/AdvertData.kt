package com.tostech.artisan.data

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class AdvertData (

     var bus_name: String? = "",
     var category: String? = "",
     var uid: String? = "",
     var purl: String? = "",
     var status: String? ="",
     var description: String?  = ""
)