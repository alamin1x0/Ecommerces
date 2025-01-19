package com.developeralamin.ecommerceapp.payment.paymenthome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeralamin.ecommerceapp.payment.Constants
import com.developeralamin.ecommerceapp.payment.Constants.amount
import com.developeralamin.ecommerceapp.payment.Constants.bkashSandboxAppKey
import com.developeralamin.ecommerceapp.payment.Constants.bkashSandboxAppSecret
import com.developeralamin.ecommerceapp.payment.Constants.bkashSandboxPassword
import com.developeralamin.ecommerceapp.payment.Constants.bkashSandboxUsername
import com.developeralamin.ecommerceapp.payment.Constants.callbackURL
import com.developeralamin.ecommerceapp.payment.Constants.currency
import com.developeralamin.ecommerceapp.payment.Constants.intents
import com.developeralamin.ecommerceapp.payment.Constants.merchantAssociationInfo
import com.developeralamin.ecommerceapp.payment.Constants.merchantInvoiceNumber
import com.developeralamin.ecommerceapp.payment.Constants.mode
import com.developeralamin.ecommerceapp.payment.Constants.payerReference
import com.developeralamin.ecommerceapp.payment.Constants.paymentIDBkash
import com.developeralamin.ecommerceapp.payment.Constants.sessionIdToken
import com.developeralamin.ecommerceapp.payment.SingleLiveEvent
import com.developeralamin.ecommerceapp.payment.network.ApiInterface
import com.developeralamin.ecommerceapp.payment.network.BkashApiClient
import com.developeralamin.ecommerceapp.payment.model.request.CreatePaymentRequest
import com.developeralamin.ecommerceapp.payment.model.request.ExecutePaymentRequest
import com.developeralamin.ecommerceapp.payment.model.request.GrantTokenRequest
import com.developeralamin.ecommerceapp.payment.model.request.QueryPaymentRequest
import com.developeralamin.ecommerceapp.payment.model.response.CreatePaymentResponse
import com.developeralamin.ecommerceapp.payment.model.response.ExecutePaymentResponse
import com.developeralamin.ecommerceapp.payment.model.response.GrantTokenResponse
import com.developeralamin.ecommerceapp.payment.model.response.QueryPaymentResponse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel() {
    private val job = Job()
    private val grantTokenLiveData = SingleLiveEvent<GrantTokenResponse?>()
    private val createPaymentLiveData = SingleLiveEvent<CreatePaymentResponse?>()
    private val executePaymentLiveData = SingleLiveEvent<ExecutePaymentResponse?>()
    private val queryPaymentLiveData = SingleLiveEvent<QueryPaymentResponse?>()
    private val errorLiveData = SingleLiveEvent<String?>() // For error messages

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Constants.pd?.dismiss()
        throwable.printStackTrace()
        errorLiveData.postValue(throwable.localizedMessage ?: "Unknown error occurred")
    }

    fun getGrantTokenObserver(): SingleLiveEvent<GrantTokenResponse?> = grantTokenLiveData
    fun getCreatePaymentObserver(): SingleLiveEvent<CreatePaymentResponse?> = createPaymentLiveData
    fun getExecutePaymentObserver(): SingleLiveEvent<ExecutePaymentResponse?> = executePaymentLiveData
    fun getQueryPaymentObserver(): SingleLiveEvent<QueryPaymentResponse?> = queryPaymentLiveData
    fun getErrorObserver(): SingleLiveEvent<String?> = errorLiveData

    fun grantTokenApiCall() {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler + job) {
            try {
                val bkashApiClient = BkashApiClient.client?.create(ApiInterface::class.java)
                val response = bkashApiClient?.postGrantToken(
                    username = bkashSandboxUsername,
                    password = bkashSandboxPassword,
                    GrantTokenRequest(
                        appKey = bkashSandboxAppKey,
                        appSecret = bkashSandboxAppSecret
                    )
                )
                if (response != null) {
                    grantTokenLiveData.postValue(response)
                } else {
                    errorLiveData.postValue("Grant token API returned null response")
                }
            } catch (e: Exception) {
                errorLiveData.postValue(e.localizedMessage ?: "Error in Grant Token API")
                e.printStackTrace()
            }
        }
    }

    fun createPaymentApiCall() {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler + job) {
            try {
                val bkashApiClient = BkashApiClient.client?.create(ApiInterface::class.java)
                val response = bkashApiClient?.postPaymentCreate(
                    authorization = "Bearer $sessionIdToken",
                    xAppKey = bkashSandboxAppKey,
                    CreatePaymentRequest(
                        mode = mode,
                        payerReference = payerReference,
                        callbackURL = callbackURL,
                        merchantAssociationInfo = merchantAssociationInfo,
                        amount = amount,
                        currency = currency,
                        intent = intents,
                        merchantInvoiceNumber = merchantInvoiceNumber,
                    )
                )
                if (response != null) {
                    createPaymentLiveData.postValue(response)
                } else {
                    errorLiveData.postValue("Create payment API returned null response")
                }
            } catch (e: Exception) {
                errorLiveData.postValue(e.localizedMessage ?: "Error in Create Payment API")
                e.printStackTrace()
            }
        }
    }

    fun executePaymentApiCall() {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler + job) {
            try {
                val bkashApiClient = BkashApiClient.client?.create(ApiInterface::class.java)
                val response = bkashApiClient?.postPaymentExecute(
                    authorization = "Bearer $sessionIdToken",
                    xAppKey = bkashSandboxAppKey,
                    ExecutePaymentRequest(paymentID = paymentIDBkash)
                )
                if (response != null) {
                    executePaymentLiveData.postValue(response)
                } else {
                    errorLiveData.postValue("Execute payment API returned null response")
                }
            } catch (e: Exception) {
                errorLiveData.postValue(e.localizedMessage ?: "Error in Execute Payment API")
                e.printStackTrace()
            }
        }
    }

    fun queryPaymentApiCall() {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler + job) {
            try {
                val bkashApiClient = BkashApiClient.client?.create(ApiInterface::class.java)
                val response = bkashApiClient?.postQueryPayment(
                    authorization = "Bearer $sessionIdToken",
                    xAppKey = bkashSandboxAppKey,
                    QueryPaymentRequest(paymentID = paymentIDBkash)
                )
                if (response != null) {
                    queryPaymentLiveData.postValue(response)
                } else {
                    errorLiveData.postValue("Query payment API returned null response")
                }
            } catch (e: Exception) {
                errorLiveData.postValue(e.localizedMessage ?: "Error in Query Payment API")
                e.printStackTrace()
            }
        }
    }
}


/*
class HomeViewModel : ViewModel() {
    private val job = Job()
    private val grantTokenLiveData = SingleLiveEvent<GrantTokenResponse?>()
    private val createPaymentLiveData = SingleLiveEvent<CreatePaymentResponse?>()
    private val executePaymentLiveData = SingleLiveEvent<ExecutePaymentResponse?>()
    private val queryPaymentLiveData = SingleLiveEvent<QueryPaymentResponse?>()
    fun getGrantTokenObserver(): SingleLiveEvent<GrantTokenResponse?> {
        return grantTokenLiveData
    }
    fun getCreatePaymentObserver(): SingleLiveEvent<CreatePaymentResponse?> {
        return createPaymentLiveData
    }
    fun getExecutePaymentObserver(): SingleLiveEvent<ExecutePaymentResponse?> {
        return executePaymentLiveData
    }
    fun getQueryPaymentObserver(): SingleLiveEvent<QueryPaymentResponse?> {
        return queryPaymentLiveData
    }
    fun grantTokenApiCall() {
        viewModelScope.launch(Dispatchers.IO + job) {
            val bkashApiClient = BkashApiClient.client?.create(ApiInterface::class.java)
            val response  = bkashApiClient?.postGrantToken(
                username = bkashSandboxUsername,
                password = bkashSandboxPassword,
                GrantTokenRequest(
                    appKey = bkashSandboxAppKey,
                    appSecret = bkashSandboxAppSecret
                )
            )
            grantTokenLiveData.postValue(response)
        }
    }

    fun createPaymentApiCall() {
        viewModelScope.launch(Dispatchers.IO + job) {
            val bkashApiClient = BkashApiClient.client?.create(ApiInterface::class.java)
            val response  = bkashApiClient?.postPaymentCreate(
                authorization = "Bearer $sessionIdToken",
                xAppKey = bkashSandboxAppKey,
                CreatePaymentRequest(
                    mode = mode,
                    payerReference = payerReference,
                    callbackURL = callbackURL,
                    merchantAssociationInfo = merchantAssociationInfo,
                    amount = amount,
                    currency = currency,
                    intent = intents,
                    merchantInvoiceNumber = merchantInvoiceNumber,
                )
            )
            createPaymentLiveData.postValue(response)
        }
    }

    fun executePaymentApiCall() {
        viewModelScope.launch(Dispatchers.IO + job) {
            val bkashApiClient = BkashApiClient.client?.create(ApiInterface::class.java)
            val response  = bkashApiClient?.postPaymentExecute(
                authorization = "Bearer $sessionIdToken",
                xAppKey = bkashSandboxAppKey,
                ExecutePaymentRequest(
                    paymentID = paymentIDBkash
                )
            )
            executePaymentLiveData.postValue(response)
        }
    }

    fun queryPaymentApiCall() {
        viewModelScope.launch(Dispatchers.IO + job) {
            val bkashApiClient = BkashApiClient.client?.create(ApiInterface::class.java)
            val response  = bkashApiClient?.postQueryPayment(
                authorization = "Bearer $sessionIdToken",
                xAppKey = bkashSandboxAppKey,
                QueryPaymentRequest(
                    paymentID = paymentIDBkash
                )
            )
            queryPaymentLiveData.postValue(response)
        }
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
        Constants.pd?.dismiss()
        throwable.printStackTrace()
    }
}*/
