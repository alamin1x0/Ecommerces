package com.developeralamin.ecommerceapp.ui

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import at.favre.lib.crypto.bcrypt.BCrypt
import com.developeralamin.ecommerceapp.databinding.ActivityRegistrationBinding
import com.developeralamin.ecommerceapp.model.UserModel
import com.developeralamin.ecommerceapp.utils.Constant
import com.developeralamin.ecommerceapp.utils.GPSTracker
import com.developeralamin.ecommerceapp.utils.InternetConnection
import com.developeralamin.ecommerceapp.utils.PreferenceManager
import com.developeralamin.ecommerceapp.utils.ToastHelper
import com.developeralamin.ecommerceapp.utils.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val REQUEST_CODE = 100
    private val PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private lateinit var locationManager: LocationManager
    private var loginTime: String =
        SimpleDateFormat("dd-MMM-yyyy KK:mm:ss a", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()

        requestRuntimePermission()

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

//        if (!isLocationEnabled) {
//            val locationSettingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//            startActivityForResult(locationSettingsIntent, REQUEST_CODE)
//        }


        db = FirebaseFirestore.getInstance()

        preferenceManager = PreferenceManager(this)

        binding.userLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }


        binding.signupBtn.setOnClickListener {
            val fullName = binding.fullName.text.toString().trim()
            val userPhone = binding.userPhone.text.toString().trim()
            val userEmail = binding.userEmail.text.toString().trim()
            val password = binding.userPassword.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()

            if (fullName.isEmpty()) {
                binding.fullName.setError("Enter your Full Name")
                binding.fullName.requestFocus()
            } else if (userPhone.isEmpty()) {
                binding.userPhone.setError("Enter your Phone Number")
                binding.userPhone.requestFocus()
            } else if (userPhone.length < 11) {
                Toast.makeText(this, "Please Enter Correct Phone Number", Toast.LENGTH_SHORT).show()
            } else if (userEmail.isEmpty()) {
                binding.userEmail.setError("Enter your Email Address")
                binding.userEmail.requestFocus()
            } else if (password.isEmpty()) {
                binding.userPassword.setError("Enter your Password")
                binding.userPassword.requestFocus()
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Password are not matching", Toast.LENGTH_SHORT).show()
            } else if (password.length < 3) {
                Toast.makeText(this, "Password minimum 3 characters", Toast.LENGTH_SHORT).show()
            } else {


                val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())
                val confirmPassword =
                    BCrypt.withDefaults().hashToString(12, confirmPassword.toCharArray())


                if (InternetConnection.isOnline(this)) {
                    loading(true)
                    hideKeyboard()

                    val docRef = db.collection(Constant.KEY_COLLECTION_USER).document(userPhone)

                    docRef.get().addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            loading(false)

                            ToastHelper.showErrorToast(this, "Already registered")
                        } else {

                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                if (!task.isSuccessful) {
                                    return@addOnCompleteListener
                                }

                                val token = task.result
                                val gpsTracker = GPSTracker(this)

                                val addressModel =
                                    gpsTracker.findCity(
                                        gpsTracker.getLatitude(),
                                        gpsTracker.getLongitude()
                                    )

                                if (addressModel != null) {

                                }

                                val userModel =
                                    UserModel(
                                        fullName,
                                        userPhone,
                                        userEmail,
                                        hashedPassword,
                                        loginTime,
                                        token,
                                        status = true,
                                        locationLat = gpsTracker.getLatitude().toString(),
                                        locationLng = gpsTracker.getLongitude().toString(),
                                        Utils.generateUniqueRefNo()
                                    )

                                db.collection(Constant.KEY_COLLECTION_USER).document(userPhone)
                                    .set(userModel)
                                    .addOnSuccessListener {

                                        ToastHelper.showSuccessToast(
                                            this,
                                            "Registration Successful"
                                        )

                                        preferenceManager.putString(
                                            Constant.PHONE_NUMBER,
                                            userPhone
                                        )

                                        preferenceManager.putString(
                                            Constant.PASSWORD,
                                            password
                                        )

                                        preferenceManager.putString(
                                            Constant.LOGINTIME,
                                            loginTime
                                        )

                                        preferenceManager.putString(Constant.FULL_NAME, fullName)
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        loading(false)
                                        ToastHelper.showErrorToast(this, "Registration failed: $e")
                                    }
                            }
                        }
                    }
                        .addOnFailureListener { e ->
                            loading(false)
                            ToastHelper.showErrorToast(this, "Error: $e")
                        }

                } else {
                    loading(false)
                    ToastHelper.showErrorToast(this, "No internet or data connection!")
                }
            }

        }
    }


    private fun requestRuntimePermission() {
        if (checkSelfPermission(PERMISSION_ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                PERMISSION_ACCESS_FINE_LOCATION
            )
        ) {
            AlertDialog.Builder(this)
                .setMessage("This app requires Final Location permission")
                .setTitle("Permission Granted")
                .setCancelable(false)
                .setPositiveButton("Ok") { dialogInterface: DialogInterface, _: Int ->
                    ActivityCompat.requestPermissions(
                        this@RegistrationActivity,
                        arrayOf(PERMISSION_ACCESS_FINE_LOCATION),
                        REQUEST_CODE
                    )
                    dialogInterface.dismiss()
                }
                .setNegativeButton("No") { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
                .show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(PERMISSION_ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    PERMISSION_ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])
            ) {
                AlertDialog.Builder(this)
                    .setMessage("Please Allow Location")
                    .setTitle("Permission Granted")
                    .setCancelable(false)
                    .setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int -> }
                    .setPositiveButton("Setting") { dialogInterface: DialogInterface, _: Int ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        val uri: Uri = Uri.fromParts("package", packageName, null)
                        intent.data = uri
                        startActivity(intent)
                        dialogInterface.dismiss()
                    }
                    .show()
            } else {
                requestRuntimePermission()
            }
        }
    }

    private fun hideKeyboard() {
        val imm = this?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.signupBtn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.signupBtn.visibility = View.VISIBLE
        }
    }
}