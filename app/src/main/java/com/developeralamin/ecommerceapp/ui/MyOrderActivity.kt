package com.developeralamin.ecommerceapp.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.developeralamin.ecommerceapp.R
import com.developeralamin.ecommerceapp.adapter.PendingOrderAdapter
import com.developeralamin.ecommerceapp.databinding.ActivityMyOrderBinding
import com.developeralamin.ecommerceapp.model.PendingOrder
import com.developeralamin.ecommerceapp.utils.Constant
import com.developeralamin.ecommerceapp.utils.InternetConnection
import com.developeralamin.ecommerceapp.utils.ToastHelper
import com.developeralamin.ecommerceapp.viewmodel.MainViewModel
import com.google.firebase.firestore.FirebaseFirestore

class MyOrderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyOrderBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var pendingOrderList: ArrayList<PendingOrder>
    private var phone: String? = null
    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()
        click()

        if (InternetConnection.isOnline(this)) {
            initList()
        } else {
            ToastHelper.showErrorToast(this, Constant.NO_INTERNET)
        }
    }

    private fun initList() {
        binding.apply {
            progressBar.visibility = View.VISIBLE
            viewModel.initPreferences(this@MyOrderActivity)
            var originalOrderList: List<PendingOrder> = emptyList()

            viewModel.pendingOrder.observe(this@MyOrderActivity, Observer { pendingOrderList ->
                if (pendingOrderList.isNullOrEmpty()) {
                    noDataFoundTxt.visibility = View.VISIBLE
                    orderRecyclerView.visibility = View.GONE
                    searchEditText.visibility = View.GONE
                } else {
                    //  originalOrderList = pendingOrderList // Save the original list
                    //originalOrderList = pendingOrderList.sortedBy { it.orderNo }
                    originalOrderList = pendingOrderList.sortedByDescending { it.refNo }
                    updateRecyclerView(originalOrderList) // Populate the RecyclerView
                    searchEditText.visibility = View.VISIBLE
                    setupSearchFunctionality(originalOrderList)
                }
                progressBar.visibility = View.GONE
            })

            viewModel.loadPendingOrder()
        }
    }

    private fun updateRecyclerView(orderList: List<PendingOrder>) {
        binding.apply {
            orderRecyclerView.adapter = PendingOrderAdapter(orderList, this@MyOrderActivity)
            noDataFoundTxt.visibility = if (orderList.isEmpty()) View.VISIBLE else View.GONE
            orderRecyclerView.visibility = if (orderList.isEmpty()) View.GONE else View.VISIBLE

            binding.totalTitle.text = "${orderList.size}"
            val deliveredList = orderList.filter { it.status == "Delivered" }
            binding.deliveredTitle.text = "${deliveredList.size}"
            binding.deliveredTitle.setTextColor(
                ContextCompat.getColor(
                    this@MyOrderActivity,
                    if (deliveredList.isNotEmpty()) R.color.green else R.color.red
                )
            )

            val pendingList = orderList.filter { it.status == "Pending" }
            binding.pendingTitle.text = "${pendingList.size}"
            binding.pendingTitle.setTextColor(
                ContextCompat.getColor(
                    this@MyOrderActivity,
                    if (pendingList.isNotEmpty()) R.color.royal_blue_dark else R.color.red
                )
            )

            val canceledList = orderList.filter { it.status == "Canceled" }
            binding.cancelTitle.text = "${canceledList.size}"
            binding.cancelTitle.setTextColor(
                ContextCompat.getColor(
                    this@MyOrderActivity,
                    if (canceledList.isNotEmpty()) R.color.red else R.color.red
                )
            )
        }
    }

    private fun setupSearchFunctionality(orderList: List<PendingOrder>) {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase().trim()
                val filteredList = orderList.filter {
                    it.orderType!!.lowercase().contains(query) || it.status!!.lowercase()
                        .contains(query) || it.amount.toString().lowercase().contains(query)
                }
                updateRecyclerView(filteredList)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun click() {
        binding.toolbar.toolbarTittle.text = "My Order"
        binding.toolbar.btnBack.setOnClickListener { finish() }
        db = FirebaseFirestore.getInstance()

        pendingOrderList = ArrayList()

        val preferencesUserName =
            this.getSharedPreferences(
                Constant.PREFERENCE_NAME,
                AppCompatActivity.MODE_PRIVATE
            )
        phone = preferencesUserName.getString(Constant.PHONE_NUMBER, "")

    }

}