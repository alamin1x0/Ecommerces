package com.developeralamin.ecommerceapp.payment.model.request

import com.google.gson.annotations.SerializedName

data class SearchTransactionRequest(
  @SerializedName("trxID")
  var trxID: String? = null,
  )