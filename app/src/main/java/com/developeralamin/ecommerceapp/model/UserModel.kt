package com.developeralamin.ecommerceapp.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserModel(
    val userName: String? = "",
    val phone: String? = "",
    val email: String? = "",
    val password: String? = "",
    val loginTime: String? = "",
    val fcmToken: String? = "",
    val status: Boolean? = false,
    val locationLat: String? = "",
    val locationLng: String? = "",
    val refNo:String?=""

) {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        "",
        false,
        "",
        "",
        ""
    )
}