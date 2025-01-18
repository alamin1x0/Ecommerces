package com.developeralamin.ecommerceapp.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.developeralamin.ecommerceapp.R
import com.developeralamin.ecommerceapp.databinding.ActivitySplashScreenBinding
import com.developeralamin.ecommerceapp.model.UserModel
import com.developeralamin.ecommerceapp.utils.Constant
import com.developeralamin.ecommerceapp.utils.InternetConnection
import com.developeralamin.ecommerceapp.utils.ToastHelper
import com.developeralamin.ecommerceapp.utils.Utils
import com.google.firebase.firestore.FirebaseFirestore

class SplashScreen : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var preferenceManager: com.developeralamin.ecommerceapp.utils.PreferenceManager
    private lateinit var db: FirebaseFirestore
    private var userDetails: String? = null
    private var imeiMo: String? = null
    private var status: Boolean? = false
    private var userPhoneNumber: String? = null
    private var loginHour: Long = 0
    private var userModel: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        db = FirebaseFirestore.getInstance()
        preferenceManager = com.developeralamin.ecommerceapp.utils.PreferenceManager(this)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.splash_animation)
        binding.imageView.startAnimation(slideAnimation)

        if (InternetConnection.isOnline(this)){
            // initNotice()
        }else{

        }

        val preferences = getSharedPreferences(Constant.PREFERENCE_NAME, MODE_PRIVATE)
        userPhoneNumber = preferences.getString(Constant.PHONE_NUMBER, "")
        userDetails = preferences.getString(Constant.LOGINTIME, "")
        status = preferences.getBoolean("status", false)


        loginHour = Utils.calculateLoginTimeDifference(userDetails ?: "")
        Log.d("userDetails", "Calculated loginHour: $loginHour")

        if (InternetConnection.isOnline(this)) {
            if (!userPhoneNumber.isNullOrEmpty()) {
                fetchUserData()
            } else {
                Log.e("Error", "User phone number is empty.")
                navigateToLoginActivity()
            }
        } else {
            ToastHelper.showErrorToast(this, Constant.NO_INTERNET)
        }
    }

    private fun fetchUserData() {
        db.collection(Constant.KEY_COLLECTION_USER).document(userPhoneNumber!!)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    userModel = document.toObject(UserModel::class.java)
                    if (userModel != null) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            navigateBasedOnUserData()
                        }, 3000)
                    } else {
                        Log.e("userDetails", "User model is null")
                        navigateToLoginActivity()
                    }
                } else {
                    Log.e("Error", "User document does not exist")
                    navigateToLoginActivity()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Error", "Error fetching user document: ${exception.message}")
                navigateToLoginActivity()
            }
    }

    private fun navigateBasedOnUserData() {
        try {
            if (userPhoneNumber == userModel?.phone) {

                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                navigateToLoginActivity()
            }
        } catch (e: Exception) {
            Log.e("Error", "Error in navigateBasedOnUserData: ${e.message}", e)
            navigateToLoginActivity()
        }
    }

    private fun navigateToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
