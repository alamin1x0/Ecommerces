package com.developeralamin.ecommerceapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.developeralamin.ecommerceapp.R
import com.developeralamin.ecommerceapp.databinding.ActivityProfileBinding
import com.developeralamin.ecommerceapp.utils.Constant
import com.developeralamin.ecommerceapp.utils.ToastHelper
import com.developeralamin.ecommerceapp.utils.Utils

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()
        click()

    }

    private fun click() {
        binding.toolbar.toolbarTittle.text = "Profile"
        binding.toolbar.btnBack.setOnClickListener { finish() }


        binding.logoutLayout.setOnClickListener {

            Utils.showBottomSheetDialog(
                context = this,
                title = "Are you sure to Logout?",
                positiveActionText = "Yes",
                negativeActionText = "No",
                positiveAction = {
                    logOut()
                },
                negativeAction = {

                }
            )


        }
    }


    private fun logOut() {
        val preferences = applicationContext.getSharedPreferences(
            Constant.PREFERENCE_NAME,
            Context.MODE_PRIVATE
        )
        preferences.edit().remove(Constant.PHONE_NUMBER).apply()
        ToastHelper.showSuccessToast(this, "Logout Successfully")
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}