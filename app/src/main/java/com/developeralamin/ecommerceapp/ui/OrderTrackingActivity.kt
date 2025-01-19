package com.developeralamin.ecommerceapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.developeralamin.ecommerceapp.databinding.ActivityOrderTrackingBinding
import com.developeralamin.ecommerceapp.model.PendingOrder

class OrderTrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderTrackingBinding
    private lateinit var pendingOrderItem: PendingOrder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()
        click()

    }

    private fun click() {
        binding.toolbar.btnBack.setOnClickListener { finish() }
        binding.toolbar.toolbarTittle.text = "Order Tracking"

        pendingOrderItem = intent.getParcelableExtra("object")!!
        binding.textOrder.text= "#${pendingOrderItem.orderNo}"
        binding.textTime.text= "${pendingOrderItem.orderDate}"


    }
}