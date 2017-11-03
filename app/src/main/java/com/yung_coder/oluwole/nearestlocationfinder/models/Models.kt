package com.yung_coder.oluwole.nearestlocationfinder.models

/**
 * Created by yung on 10/6/17.
 */
class Models {

    data class locations(val name: String, val vicinity: String, val destination_latitude: String,
                         val destination_longitude: String, val reference: String, val icon: String, val rating: Double,
                         val origin_latitude: String, val origin_longitude: String)
}