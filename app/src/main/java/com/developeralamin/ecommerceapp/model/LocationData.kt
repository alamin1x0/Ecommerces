package com.developeralamin.ecommerceapp.model


data class LocationData(
    var latitude: String?="",
    var longitude: String?="",
    var address: String?="",
    var city: String?="",
    var country: String?="",
    var postalCode: String?="",
    var state: String?=""
)