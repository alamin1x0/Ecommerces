package com.developeralamin.ecommerceapp.utils

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.developeralamin.ecommerceapp.R


object ToastHelper {

    fun showSuccessToast(context: Context, message: String) {
        showToast(context, message, R.color.green) // Use a custom color resource for green
    }

    fun showErrorToast(context: Context, message: String) {
        showToast(context, message, R.color.red) // Use a custom color resource for red
    }

    private fun showToast(context: Context, message: String, bgColorResId: Int) {
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
}
