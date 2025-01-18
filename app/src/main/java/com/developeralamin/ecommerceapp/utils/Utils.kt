package com.developeralamin.ecommerceapp.utils

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.developeralamin.ecommerceapp.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class Utils {
    companion object {


        fun formatDate(inputDate: String): String {
            val inputFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.US)
            val outputFormat = SimpleDateFormat("dd-MMM-yyyy h:mm:ss a", Locale.US)

            val date = inputFormat.parse(inputDate)
            return outputFormat.format(date)
        }


        fun formatTimestamp(timestamp: Long): String {
            val date = Date(timestamp)
            val format = SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a", Locale.getDefault())
            return format.format(date)
        }

        fun main() {
            val timestamp = 1733401443252
            val formattedDate = formatTimestamp(timestamp)
            println(formattedDate) // Output: 05-Dec-2024 6:10 PM
        }

        fun calculateLoginTimeDifference(dateTimeString: String): Long {
            val dateFormat = SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a")
            try {
                val inputDate = dateFormat.parse(dateTimeString)
                val currentDate = Date()
                val timeDifferenceMillis = currentDate.time - inputDate.time
                return timeDifferenceMillis / (60 * 60 * 1000)
            } catch (e: Exception) {
                return -1
            }

            // Return a default value or handle the error case
            return -1
        }

        fun showErrorToast(context: Context, message: String) {
            showToasts(context, message, R.color.red) // Use a custom color resource for red
        }

        private fun showToasts(context: Context, message: String, bgColorResId: Int) {
            val inflater = LayoutInflater.from(context)
            val layout: View = inflater.inflate(R.layout.common_toast, null)

            val textView = layout.findViewById<TextView>(R.id.toastText)
            textView.text = message
            textView.setBackgroundColor(ContextCompat.getColor(context, bgColorResId))

            val toast = Toast(context)
            toast.duration = Toast.LENGTH_SHORT
            toast.view = layout
            toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            toast.show()
        }


        fun generateRandomMerchantInvoiceNumber(prefix: String): String {
            val randomNumber = Random.nextInt(1000, 9999)
            return "$prefix$randomNumber"
        }


        fun getLanguageFromSharedPreferences(context: Context): String {
            val sharedPreferences = context.getSharedPreferences("Settings", Activity.MODE_PRIVATE)
            return sharedPreferences.getString("My_Lang", "") ?: ""
        }

        var dateTime: String =
            SimpleDateFormat("dd-MMM-yy KK:mm:ss a", Locale.getDefault()).format(Date())

        var date: String =
            SimpleDateFormat("dd-MMM-yyyy", Locale.US).format(Date())

        fun roundToTwoDecimalPlaces(number: Double): Double {
            val bd = BigDecimal(number)
            return bd.setScale(2, RoundingMode.HALF_UP).toDouble()
        }

        fun generateTransactionId(existingIds: Set<String>): String {
            val characters = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            var transactionId: String
            do {
                transactionId = (1..11)
                    .map { _ -> characters.random() }
                    .joinToString("")
            } while (existingIds.contains(transactionId))
            return transactionId
        }

        fun generateUniqueRefNo(): String {
            val refNoDateTime =
                SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(Date())
            val randomNumber =
                (1000..9999).random() // Generates a random number between 1000 and 9999
            return "$refNoDateTime$randomNumber"
        }


        fun showBottomSheetDialog(
            context: Context,
            title: String,
            positiveActionText: String,
            negativeActionText: String,
            positiveAction: () -> Unit,
            negativeAction: () -> Unit,
        ) {
            val sheetDialog = BottomSheetDialog(context, R.style.BottomSheetStyleInvoice)
            val view = LayoutInflater.from(context).inflate(R.layout.delete_bottom_sheet, null)
            sheetDialog.setContentView(view)

            val titleTxt: TextView = view.findViewById(R.id.titleTxt)
            val yes: TextView = view.findViewById(R.id.delete_yes)
            val no: TextView = view.findViewById(R.id.delete_no)

            titleTxt.text = title
            yes.text = positiveActionText
            no.text = negativeActionText

            yes.setOnClickListener {
                positiveAction()
                sheetDialog.dismiss()
            }

            no.setOnClickListener {
                negativeAction()
                sheetDialog.dismiss()
            }

            sheetDialog.show()
        }


        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

    }

    fun removeTrailingZeros(value: String): String {
        return value.toDouble().toInt().toString()
    }
}