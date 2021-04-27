package com.tostech.artisan.notification

import com.squareup.okhttp.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

public interface APIService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:Key=AAAAc8RCPPk:APA91bHNHuDdEM0_E12J_o8Iu-p0_Q2Xd68H4MJrtCk_AAH84zK0VxtUtCqjIjVA4Kta42fPShactfecfgdtzdY2qemBUOQvEXZBjc8qLzRjQ753aM7u2BuKhYu866KrwWOsbmvzeQSY",


        )
    @POST("fcm/send")
    fun sendNotification(@Body notification: Sender): Call<MyResponse>

/* @Headers("Authorization: key=${Constants.SERVER_KEY}","Content-type:${Constants.CONTENT_TYPE}")
 @POST("fcm/send")
 suspend fun postNotification(
     @Body notification:PushNotification
 ): Response<okhttp3.ResponseBody>*/
}