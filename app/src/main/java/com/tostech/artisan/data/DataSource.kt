package com.tostech.artisan.data

import android.content.Context
import com.tostech.artisan.ImageData
import com.tostech.artisan.R

class DataSource {

    companion object{

        private lateinit var context: Context

        fun createDataSet(): ArrayList<ImageData>{
         //   val names = context.resources.getStringArray(R.array.artisan_names)
           // val image = context.resources.getStringArray(R.array.artisan_image)

            val list = ArrayList<ImageData>()
            /*
             list.add(
                 ImageData(names,
                 image
                 )
             )
             */

           list.add(
                 ImageData(
                     "Mechanic",
                     R.drawable.ic_launcher_foreground

                 )
             )
             list.add(
                 ImageData(
                     "Capenter",
                        R.drawable.background_right
                 )
             )

             list.add(
                 ImageData(
                     "Welder",
                    R.drawable.green_btn_curved
                 )
             )
             list.add(
                 ImageData(
                     "Plumber",
                         R.drawable.blue_btn_curved
                     )
             )
             list.add(
                 ImageData(
                     "Fashion Designer",
                   R.drawable.ic_baseline_phone_24
                 )
             )
             list.add(
                 ImageData(
                     "Vulcanizer",
                         R.drawable.collapsed
                 )
             )
             list.add(
                 ImageData(
                     "Furniture Maker",
                         R.drawable.black_background_curved
                 )
             )
             list.add(
                 ImageData(
                     "BrickLayer/Builder",
                         R.drawable.ic_baseline_person_24
                 )
             )
             list.add(
                 ImageData(
                     "Other",
                         R.drawable.common_google_signin_btn_icon_dark
                 )
             )
            return list
        }


    }
}