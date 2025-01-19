package com.developeralamin.ecommerceapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.developeralamin.ecommerceapp.R
import com.developeralamin.ecommerceapp.databinding.ActivityProfileBinding
import com.developeralamin.ecommerceapp.model.UserModel
import com.developeralamin.ecommerceapp.utils.Constant
import com.developeralamin.ecommerceapp.utils.ToastHelper
import com.developeralamin.ecommerceapp.utils.Utils
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var db: FirebaseFirestore

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

        db = FirebaseFirestore.getInstance()


        val preferencesUserName =
            this.getSharedPreferences(
                Constant.PREFERENCE_NAME,
                MODE_PRIVATE
            )
        val userId = preferencesUserName.getString(Constant.PHONE_NUMBER, "")


        db.collection(Constant.KEY_COLLECTION_USER).document(userId!!)
            .addSnapshotListener { value, error ->
                try {
                    if (error != null) {
                        Log.e("Error", "Error fetching user document: ${error.message}")
                        return@addSnapshotListener
                    }

                    val userModel = value?.toObject(UserModel::class.java)

                    binding.userName.text = userModel!!.userName
                    binding.phoneNumber.text = userModel!!.phone
                    binding.email.text = userModel!!.email


                } catch (e: Exception) {
                    Log.e("Error", "Exception: ${e.message}")
                    e.printStackTrace()
                }
            }


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

        binding.passwordChangeLayout.setOnClickListener {
            startActivity(Intent(this, PasswordChangeActivity::class.java))
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