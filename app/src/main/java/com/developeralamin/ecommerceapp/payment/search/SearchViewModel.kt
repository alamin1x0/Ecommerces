package com.developeralamin.ecommerceapp.payment.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developeralamin.ecommerceapp.payment.Constants
import com.developeralamin.ecommerceapp.payment.SingleLiveEvent
import com.developeralamin.ecommerceapp.payment.network.ApiInterface
import com.developeralamin.ecommerceapp.payment.network.BkashApiClient
import com.developeralamin.ecommerceapp.payment.model.request.SearchTransactionRequest
import com.developeralamin.ecommerceapp.payment.model.response.SearchTransactionResponse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val job = Job()
    private val searchPaymentLiveData = SingleLiveEvent<SearchTransactionResponse?>()
    fun getSearchPaymentObserver(): SingleLiveEvent<SearchTransactionResponse?> {
        return searchPaymentLiveData
    }
    fun searchPaymentApiCall() {
        viewModelScope.launch(Dispatchers.IO + job) {
            val bkashApiClient = BkashApiClient.client?.create(ApiInterface::class.java)
            val response  = bkashApiClient?.postSearchTransaction(
                authorization = "Bearer ${Constants.sessionIdToken}",
                xAppKey = Constants.bkashSandboxAppKey,
                SearchTransactionRequest(
                    trxID = Constants.searchTextInput
                )
            )
            searchPaymentLiveData.postValue(response)
        }
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, throwable ->
        throwable.printStackTrace()
    }
}