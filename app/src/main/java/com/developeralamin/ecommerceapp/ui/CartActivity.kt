package com.developeralamin.ecommerceapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.developeralamin.ecommerceapp.Helper.ChangeNumberItemsListener
import com.developeralamin.ecommerceapp.Helper.ManagmentCart
import com.developeralamin.ecommerceapp.R
import com.developeralamin.ecommerceapp.adapter.CartAdapter
import com.developeralamin.ecommerceapp.databinding.ActivityCartBinding
import com.developeralamin.ecommerceapp.model.UserModel
import com.developeralamin.ecommerceapp.payment.Constants.liveData
import com.developeralamin.ecommerceapp.payment.Constants.merchantInvoiceNumber
import com.developeralamin.ecommerceapp.payment.Constants.paymentIDBkash
import com.developeralamin.ecommerceapp.payment.Constants.sessionIdToken
import com.developeralamin.ecommerceapp.payment.paymenthome.BottomSheetFragment
import com.developeralamin.ecommerceapp.payment.paymenthome.HomeViewModel
import com.developeralamin.ecommerceapp.payment.paymenthome.WebViewDialog
import com.developeralamin.ecommerceapp.utils.Constant
import com.developeralamin.ecommerceapp.utils.ToastHelper
import com.developeralamin.ecommerceapp.utils.Utils.Companion.generateRandomMerchantInvoiceNumber
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.io.IOException

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var managementCart: ManagmentCart
    private lateinit var db: FirebaseFirestore
    private var tax: Double = 0.0
    var total: Double = 0.0
    var totalAmount: Double = 0.0
    var userId: String? = null
    var userModel: UserModel? = null
    private var selectedPaymentMethod: String = "Offline Pay"
    private val viewModel by viewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()

        try {
            managementCart = ManagmentCart(this)
            setVariable()
            initCartList()
            calculatorCart()
            binding.checOutBtn.setOnClickListener {
                performCheckout()
            }

            liveData.value = false
            liveData.observe(this) {
                if (liveData.value == true) {
                    executeBkashPayment()
                    liveData.value = false
                }
            }
        } catch (e: NullPointerException) {
            Log.e("Error", "Null pointer encountered: ${e.message}")
            Toast.makeText(this, "A required data element is missing!", Toast.LENGTH_SHORT).show()
        } catch (e: ArithmeticException) {
            Log.e("Error", "Calculation error: ${e.message}")
            Toast.makeText(this, "Calculation error!", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            Log.e("Error", "Network error: ${e.message}")
            Toast.makeText(this, "Network error, please try again!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("Error", "An unexpected error occurred: ${e.message}")
            Toast.makeText(this, "Unexpected error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun performCheckout() {
        if (selectedPaymentMethod.isEmpty()) {
            ToastHelper.showErrorToast(this, "Please select a payment method")
        } else {
            if (selectedPaymentMethod == "Offline Pay") {
                //initSendToServer()
                Toast.makeText(this, "Proceeding with Offline Payment", Toast.LENGTH_SHORT).show()
            } else if (selectedPaymentMethod == "bKash") {
                Toast.makeText(this, "Proceeding with bKash Payment", Toast.LENGTH_SHORT).show()

                total.toString()
                val merchantInvoiceNumbers = "MT"
                val newMerchantInvoiceNumber =
                    generateRandomMerchantInvoiceNumber(merchantInvoiceNumbers)
                merchantInvoiceNumber = newMerchantInvoiceNumber

                grantBkashToken()

            }
        }
    }

    private fun grantBkashToken() {
        viewModel.getGrantTokenObserver().observe(this, Observer {
            if (it != null) {
                runCatching {
                    sessionIdToken = it.idToken.toString()
                    if (it.statusCode != "0000") {
                        Toast.makeText(this@CartActivity, it.statusMessage, Toast.LENGTH_SHORT)
                            .show()
                        return@Observer
                    } else {
                        createBkashPayment()
                    }
                }
            } else {
                Toast.makeText(this, "Error in getting data", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.grantTokenApiCall()
    }

    private fun createBkashPayment() {
        viewModel.getCreatePaymentObserver().observe(this, Observer { it ->
            if (it != null) {
                if (it.statusCode == "0000") {
                    paymentIDBkash = it.paymentID.toString()
                    val args = Bundle().apply {
                        putString("bKashUrl", it.bkashURL.toString())
                        putString("paymentID", it.paymentID.toString())
                    }

                    val webViewDialog = WebViewDialog().apply {
                        arguments = args
                    }
                    webViewDialog.show(supportFragmentManager, "WebViewDialog")

                } else {
                    Toast.makeText(this, it.statusMessage, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error in getting data", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.createPaymentApiCall()
    }

    private fun executeBkashPayment() {
        viewModel.getExecutePaymentObserver().observe(this) {
            if (it != null) {
                val args = Bundle().apply {
                    putString("statusMessage", it.statusMessage)
                    putString("trxID", it.trxID)
                    putString("statusCode", it.statusCode)
                    putString("amount", total.toString())
                    putString("paymentID", it.paymentID)
                    putString("customerMsisdn", it.customerMsisdn)
                }


                Toast.makeText(this, total.toString(), Toast.LENGTH_SHORT).show()

                val bottomSheetFragment = BottomSheetFragment().apply {
                    arguments = args
                }
                bottomSheetFragment.show(supportFragmentManager, "bottomSheetFragment")

            } else {
                Toast.makeText(this, "Error in getting data", Toast.LENGTH_SHORT).show()
                queryBkashPayment()
            }
        }
        viewModel.executePaymentApiCall()
    }

    private fun queryBkashPayment() {
        viewModel.getQueryPaymentObserver().observe(this) {
            if (it != null) {
                if (it.transactionStatus == "Initiated") {
                    grantBkashToken()
                } else if (it.transactionStatus == "Completed") {
                    val args = Bundle().apply {
                        putString("statusMessage", it.statusMessage)
                        putString("statusCode", it.statusCode)
                    }
                    val bottomSheetFragment = BottomSheetFragment().apply {
                        arguments = args
                    }
                    bottomSheetFragment.show(supportFragmentManager, "bottomSheetFragment")
                }
            } else {
                Toast.makeText(this, "Error in getting data", Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.queryPaymentApiCall()
    }

    private fun getNextOrderNumber(onOrderNumberGenerated: (String) -> Unit) {
        val newOrderNo = (100000..999999).random().toString()
        onOrderNumberGenerated(newOrderNo)
    }

    private fun initCartList() {
        binding.viewCart.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.viewCart.adapter =
            CartAdapter(
                managementCart.getListCart(),
                this,
                object : ChangeNumberItemsListener {
                    override fun onChanged() {
                        calculatorCart()
                    }

                })

        with(binding) {
            emptyTxt.visibility =
                if (managementCart.getListCart().isEmpty()) View.VISIBLE else View.GONE
            cashLayout.visibility =
                if (managementCart.getListCart().isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun setVariable() {
        binding.apply {
            backBtn.setOnClickListener { finish() }

            method1.setOnClickListener {

                selectPaymentMethod("Offline Pay")
//                method1.setBackgroundResource(R.drawable.green_bg_selected)
//                methodTitle1.setTextColor(resources.getColor(R.color.green))
//                methodSubTitle1.setTextColor(resources.getColor(R.color.green))
//
//                method2.setBackgroundResource(R.drawable.grey_bg_selected)
//                methodTitle2.setTextColor(resources.getColor(R.color.black))
//                methodSubTitle2.setTextColor(resources.getColor(R.color.grey))
            }

            method2.setOnClickListener {
                selectPaymentMethod("bKash")
//                method2.setBackgroundResource(R.drawable.green_bg_selected)
//                methodTitle2.setTextColor(resources.getColor(R.color.green))
//                methodSubTitle2.setTextColor(resources.getColor(R.color.green))
//
//                method1.setBackgroundResource(R.drawable.grey_bg_selected)
//                methodTitle1.setTextColor(resources.getColor(R.color.black))
//                methodSubTitle1.setTextColor(resources.getColor(R.color.grey))
            }

        }
    }

    private fun selectPaymentMethod(method: String) {
        selectedPaymentMethod = method

        if (method == "Offline Pay") {
            binding.method1.setBackgroundResource(R.drawable.green_bg_selected)
            binding.methodTitle1.setTextColor(resources.getColor(R.color.green))
            binding.methodSubTitle1.setTextColor(resources.getColor(R.color.green))

            binding.method2.setBackgroundResource(R.drawable.grey_bg_selected)
            binding.methodTitle2.setTextColor(resources.getColor(R.color.black))
            binding.methodSubTitle2.setTextColor(resources.getColor(R.color.grey))
        } else if (method == "bKash") {
            binding.method2.setBackgroundResource(R.drawable.green_bg_selected)
            binding.methodTitle2.setTextColor(resources.getColor(R.color.green))
            binding.methodSubTitle2.setTextColor(resources.getColor(R.color.green))

            binding.method1.setBackgroundResource(R.drawable.grey_bg_selected)
            binding.methodTitle1.setTextColor(resources.getColor(R.color.black))
            binding.methodSubTitle1.setTextColor(resources.getColor(R.color.grey))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun calculatorCart() {
        val percentTax = 0.00
        val delivery = 0.0
        val totalFee = managementCart.getTotalFee()

        tax = (Math.round(totalFee * percentTax * 100) / 100.0)
        val itemTotal = Math.round(totalFee * 100) / 100.0
        total = Math.round((totalFee + tax + delivery) * 100) / 100.0

        with(binding) {
            subtotalText.text = "${Constant.TAKA_SYMBOL}$itemTotal"
            taxText.text = "${Constant.TAKA_SYMBOL}$tax"
            feeDeliveryText.text = "${Constant.TAKA_SYMBOL}$delivery"
            totalText.text = "${Constant.TAKA_SYMBOL}$total"
        }

    }

}