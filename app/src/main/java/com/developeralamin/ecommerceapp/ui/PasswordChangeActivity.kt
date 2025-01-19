package com.developeralamin.ecommerceapp.ui

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.favre.lib.crypto.bcrypt.BCrypt
import com.developeralamin.ecommerceapp.R
import com.developeralamin.ecommerceapp.databinding.ActivityPasswordChangeBinding
import com.developeralamin.ecommerceapp.utils.Constant
import com.developeralamin.ecommerceapp.utils.InternetConnection
import com.developeralamin.ecommerceapp.utils.PreferenceManager
import com.developeralamin.ecommerceapp.utils.ToastHelper
import com.google.firebase.firestore.FirebaseFirestore

class PasswordChangeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPasswordChangeBinding
    private lateinit var db: FirebaseFirestore
    var phone: String = ""

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswordChangeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()

        preferenceManager = PreferenceManager(this)

        binding.toolbar.toolbarTittle.text = "Password Change"
        binding.toolbar.btnBack.setOnClickListener {
            finish()
        }

        db = FirebaseFirestore.getInstance()

        val preferencesUserName =
            this.getSharedPreferences(
                Constant.PREFERENCE_NAME,
                AppCompatActivity.MODE_PRIVATE
            )

        phone = preferencesUserName.getString(Constant.PHONE_NUMBER, "").toString()

        binding.passwordChangeBtn.setOnClickListener {
            val currentPassword = binding.oldPassword.text.toString().trim()
            val newPassword = binding.newPassword.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            if (currentPassword.isEmpty()) {
                binding.oldPassword.error = getString(R.string.old_password)
                binding.oldPassword.requestFocus()
            } else if (newPassword.isEmpty()) {
                binding.newPassword.error = getString(R.string.new_password)
                binding.newPassword.requestFocus()
            } else if (confirmPassword.isEmpty()) {
                binding.confirmPassword.error = getString(R.string.confirm_password)
                binding.confirmPassword.requestFocus()
            } else if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
            } else {
                if (InternetConnection.isOnline(this)) {
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setMessage("Please wait...")
                    progressDialog.setCancelable(false)
                    progressDialog.show()

                    val docRef = db.collection(Constant.KEY_COLLECTION_USER).document(phone)
                    docRef.get().addOnSuccessListener { documentSnapshot ->
                        try {
                            if (documentSnapshot.exists()) {
                                val passwordFromDB = documentSnapshot.getString("password")

                                if (passwordFromDB != null && BCrypt.verifyer().verify(
                                        currentPassword.toCharArray(),
                                        passwordFromDB
                                    ).verified
                                ) {

                                    val hashedPassword = BCrypt.withDefaults()
                                        .hashToString(12, newPassword.toCharArray())

                                    val userMap =
                                        hashMapOf<String, Any>("password" to hashedPassword)

                                    docRef.update(userMap)
                                        .addOnSuccessListener {
                                            // Dismiss progress dialog after success
                                            progressDialog.dismiss()

                                            preferenceManager.putString(
                                                Constant.PASSWORD,
                                                newPassword
                                            )
                                            ToastHelper.showSuccessToast(
                                                this,
                                                "Password updated successfully"
                                            )
                                            finish()
                                        }
                                        .addOnFailureListener { e ->
                                            // Dismiss progress dialog after failure
                                            progressDialog.dismiss()
                                            ToastHelper.showSuccessToast(this, e.message.toString())
                                        }
                                } else {
                                    // Dismiss progress dialog after incorrect password check
                                    progressDialog.dismiss()

                                    Toast.makeText(
                                        this,
                                        "Incorrect current password",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: Exception) {
                            // Dismiss progress dialog if exception occurs
                            progressDialog.dismiss()

                            Toast.makeText(this, e.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                    Toast.makeText(this, Constant.NO_INTERNET, Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

}