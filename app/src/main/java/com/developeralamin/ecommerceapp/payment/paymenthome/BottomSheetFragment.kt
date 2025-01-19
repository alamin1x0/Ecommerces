package com.developeralamin.ecommerceapp.payment.paymenthome

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.developeralamin.ecommerceapp.R
import com.developeralamin.ecommerceapp.databinding.BottomSheetDialogBinding
import com.developeralamin.ecommerceapp.utils.Constant
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var db: FirebaseFirestore
    private var paymentID: String? = null
    private var customerMsisdn: String? = null

    private val dateTime: String =
        SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val dialogBinding = BottomSheetDialogBinding.inflate(layoutInflater)

        val statusMessageBkash = arguments?.getString("statusMessage") ?: "Unknown status"
        val trxIDBkash = arguments?.getString("trxID")
        paymentID = arguments?.getString("paymentID")
        customerMsisdn = arguments?.getString("customerMsisdn")
        val statusCodeBkash = arguments?.getString("statusCode") ?: "Unknown code"
        val amount = arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0

        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        db = FirebaseFirestore.getInstance()

        val preferencesUserName = requireContext().getSharedPreferences(
            Constant.PREFERENCE_NAME,
            AppCompatActivity.MODE_PRIVATE
        )
        val userPhone = preferencesUserName.getString(Constant.PHONE_NUMBER, "") ?: ""


        setupDialogView(dialogBinding, statusCodeBkash, statusMessageBkash, trxIDBkash)

        return dialog
    }



    private fun setupDialogView(
        dialogBinding: BottomSheetDialogBinding,
        statusCode: String,
        statusMessage: String,
        trxID: String?) {
        when (statusCode) {
            "0000" -> {
                dialogBinding.iconSuccess.setImageResource(R.drawable.success)
                dialogBinding.titleSuccess.text = statusMessage
            }

            "2023" -> {
                dialogBinding.iconSuccess.setImageResource(R.drawable.no_balance)
                dialogBinding.titleSuccess.text = statusMessage
            }

            else -> {
                dialogBinding.iconSuccess.setImageResource(R.drawable.error)
                dialogBinding.titleSuccess.text = statusMessage
            }
        }
        trxID?.let {
            dialogBinding.subTitleSuccess.text = "trxID: $it"
        }

        dialogBinding.okayButton.setOnClickListener {
            dialog?.dismiss()
        }
    }

    override fun getTheme(): Int = R.style.SheetDialog
}