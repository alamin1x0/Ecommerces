package com.developeralamin.ecommerceapp.payment.network

import com.developeralamin.ecommerceapp.payment.model.request.CreatePaymentRequest
import com.developeralamin.ecommerceapp.payment.model.request.ExecutePaymentRequest
import com.developeralamin.ecommerceapp.payment.model.request.GrantTokenRequest
import com.developeralamin.ecommerceapp.payment.model.request.QueryPaymentRequest
import com.developeralamin.ecommerceapp.payment.model.request.SearchTransactionRequest
import com.developeralamin.ecommerceapp.payment.model.response.CreatePaymentResponse
import com.developeralamin.ecommerceapp.payment.model.response.ExecutePaymentResponse
import com.developeralamin.ecommerceapp.payment.model.response.GrantTokenResponse
import com.developeralamin.ecommerceapp.payment.model.response.QueryPaymentResponse
import com.developeralamin.ecommerceapp.payment.model.response.SearchTransactionResponse
import retrofit2.http.*


interface ApiInterface {
    @POST("/v1.2.0-beta/tokenized/checkout/token/grant")
    suspend fun postGrantToken(
        @Header("username") username: String?,
        @Header("password") password: String?,
        @Body grantTokenRequest: GrantTokenRequest
    ): GrantTokenResponse

    @POST("/v1.2.0-beta/tokenized/checkout/create")
    suspend fun postPaymentCreate(
        @Header("authorization") authorization: String?,
        @Header("x-app-key") xAppKey: String?,
        @Body createPaymentRequest: CreatePaymentRequest
    ): CreatePaymentResponse

    @POST("/v1.2.0-beta/tokenized/checkout/execute")
    suspend fun postPaymentExecute(
        @Header("authorization") authorization: String?,
        @Header("x-app-key") xAppKey: String?,
        @Body executePaymentRequest: ExecutePaymentRequest
    ): ExecutePaymentResponse

    @POST("/v1.2.0-beta/tokenized/checkout/general/searchTransaction")
    suspend fun postSearchTransaction(
        @Header("authorization") authorization: String?,
        @Header("x-app-key") xAppKey: String?,
        @Body searchTransactionRequest: SearchTransactionRequest
    ): SearchTransactionResponse

    @POST("/v1.2.0-beta/tokenized/checkout/payment/status")
    suspend fun postQueryPayment(
        @Header("authorization") authorization: String?,
        @Header("x-app-key") xAppKey: String?,
        @Body queryPaymentRequest: QueryPaymentRequest
    ): QueryPaymentResponse
}