package com.tostech.artisan.notification

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

public class Client {
     private var retrofit: Retrofit? = null

    public fun getClient(url: String): Retrofit?{
         if(retrofit == null){
             retrofit = Retrofit.Builder()
                 .baseUrl(url)
                 .addConverterFactory(GsonConverterFactory.create())
                 .build()
         }
         return retrofit
     }

 }