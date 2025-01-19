package com.developeralamin.ecommerceapp.payment.model.request

import com.google.gson.annotations.SerializedName

data class ExecutePaymentRequest(
  @SerializedName("paymentID")
  var paymentID: String? = null,
  )