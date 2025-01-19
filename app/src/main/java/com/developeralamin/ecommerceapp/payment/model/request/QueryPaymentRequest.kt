package com.developeralamin.ecommerceapp.payment.model.request

import com.google.gson.annotations.SerializedName

data class QueryPaymentRequest(
  @SerializedName("paymentID")
  var paymentID: String? = null,
  )