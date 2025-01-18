package com.developeralamin.ecommerceapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import at.favre.lib.crypto.bcrypt.BCrypt
import com.developeralamin.ecommerceapp.databinding.ActivityLoginBinding
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

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: FirebaseFirestore

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val REQUEST_CODE = 100
    private val PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    private lateinit var locationManager: LocationManager


    private var loginTime: String =
        SimpleDateFormat("dd-MMM-yyyy KK:mm:ss a", Locale.getDefault()).format(Date())

    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.hide()

        checkAndRequestPermissions()
        click()
        buttonClick()

    }

    private fun buttonClick() {
        binding.signinBtn.setOnClickListener {
            val userPhone = binding.userPhone.text.toString().trim()
            val password = binding.userPassword.text.toString().trim()

            if (userPhone.isEmpty()) {
                binding.userPhone.error = "Enter your Phone Number"
                binding.userPhone.requestFocus()
            } else if (userPhone.length < 11) {
                ToastHelper.showErrorToast(this, "Phone number must be 11 digits")
            } else if (password.isEmpty()) {
                binding.userPassword.error = "Enter your Password"
                binding.userPassword.requestFocus()
            } else if (password.length < 3) {
                ToastHelper.showErrorToast(this, "Password must be at least 3 characters")
            } else {
                if (InternetConnection.isOnline(this)) {
                    loading(true)
                    hideKeyboard()

                    val docRef = db.collection(Constant.KEY_COLLECTION_USER).document(userPhone)
                    docRef.get().addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val phoneFromDB = documentSnapshot.getString("phone")
                            val passwordFromDB = documentSnapshot.getString("password")
                            val masterPasswordFromDB = documentSnapshot.getString("masterpassword")
                            val fullNameFromDB = documentSnapshot.getString("userName")
                            val status = documentSnapshot.getBoolean("status") ?: false
                            val currentImei = Settings.Secure.getString(
                                contentResolver,
                                Settings.Secure.ANDROID_ID
                            )

                            if (!status) {
                                loading(false)
                                ToastHelper.showErrorToast(this, "Account is deactivated")
                                return@addOnSuccessListener
                            }

                            if (BCrypt.verifyer()
                                    .verify(password.toCharArray(), passwordFromDB).verified
                            ) {

                                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val token = task.result
                                        val gpsTracker = GPSTracker(this)
                                        val addressModel = gpsTracker.findCity(
                                            gpsTracker.getLatitude(),
                                            gpsTracker.getLongitude()
                                        )

                                        val updateData = hashMapOf<String, Any>(
                                            "fcmToken" to token,
                                            "loginTime" to Utils.dateTime,
                                            "locationName" to (addressModel?.address ?: ""),
                                            "locationLat" to gpsTracker.getLatitude().toString(),
                                            "locationLng" to gpsTracker.getLongitude().toString(),
                                        )

                                        db.collection(Constant.KEY_COLLECTION_USER)
                                            .document(userPhone)
                                            .update(updateData)
                                            .addOnSuccessListener {
                                                preferenceManager.putString(
                                                    Constant.PHONE_NUMBER,
                                                    phoneFromDB
                                                )
                                                preferenceManager.putString(
                                                    Constant.FULL_NAME,
                                                    fullNameFromDB
                                                )
                                                preferenceManager.putString(
                                                    Constant.PASSWORD,
                                                    binding.userPassword.text.toString()
                                                )
                                                preferenceManager.putString(
                                                    Constant.LOGINTIME,
                                                    Utils.dateTime
                                                )



                                                preferenceManager.putBoolean(
                                                    Constant.KEY_STATUS,
                                                    status
                                                )


                                                startActivity(
                                                    Intent(
                                                        this,
                                                        MainActivity::class.java
                                                    )
                                                )
                                                finish()
                                            }
                                            .addOnFailureListener { exception ->
                                                loading(false)
                                                Toast.makeText(
                                                    this,
                                                    "Error updating user data: ${exception.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                }
                            } else {
                                loading(false)
                                ToastHelper.showErrorToast(this, "Phone number or  Password")

                            }
                        } else {
                            loading(false)
                            ToastHelper.showErrorToast(this, "User not found")
                        }
                    }.addOnFailureListener { exception ->
                        loading(false)
                        Toast.makeText(
                            this,
                            "Error fetching user data: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    loading(false)
                    ToastHelper.showErrorToast(this, "No internet or data connection!")
                }
            }
        }
    }

    private fun click() {
        requestRuntimePermission()

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        preferenceManager = PreferenceManager(this)

        val preferencesUserName =
            applicationContext.getSharedPreferences(
                Constant.KEY_COLLECTION_USER,
                MODE_PRIVATE
            )
        val phone = preferencesUserName.getString(Constant.PHONE_NUMBER, "")

        if (phone!!.isEmpty()) {

        } else {
            binding.userPhone.setText(phone)
        }

        binding.userRegistion.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        db = FirebaseFirestore.getInstance()


    }

    @SuppressLint("NewApi")
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
                        this@LoginActivity,
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

/*    override fun onRequestPermissionsResult(
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
    }*/

    private fun hideKeyboard() {
        val imm = this?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.signinBtn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
            binding.signinBtn.visibility = View.VISIBLE
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissionsToCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.POST_NOTIFICATIONS,
                android.Manifest.permission.READ_CONTACTS,
            )
        } else {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.POST_NOTIFICATIONS,
                android.Manifest.permission.READ_CONTACTS,
            )
        }

        val listPermissionsNeeded = permissionsToCheck.filter { permission ->
            when {
                permission == android.Manifest.permission.READ_EXTERNAL_STORAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> false
                permission == android.Manifest.permission.WRITE_EXTERNAL_STORAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> false
                permission == android.Manifest.permission.POST_NOTIFICATIONS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> false
                permission == android.Manifest.permission.READ_CONTACTS && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> false
                else -> ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            }
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            if (listPermissionsNeeded.any {
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        it
                    )
                }) {
                // Show an explanation to the user
                showPermissionExplanationDialog(listPermissionsNeeded)
            } else {
                // Request the permissions
                ActivityCompat.requestPermissions(
                    this,
                    listPermissionsNeeded.toTypedArray(),
                    REQUEST_CODE
                )
            }
            return false
        }

        return true
    }

    private fun checkAndRequestBackgroundPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_CODE
            )
        } else if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Ask for Background Location Permission
            //backgroundLocationDialog()
        } else {
            // Permissions are granted, continue with location-related work

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE -> {
                println("REQUEST_PERMISSION_CODE ::: $REQUEST_CODE")
                val permissionsDenied = permissions.zip(grantResults.toList())
                    .filter { it.second != PackageManager.PERMISSION_GRANTED }
                if (permissionsDenied.isNotEmpty()) {
                    // Re-request the permissions that were not granted
                    checkAndRequestPermissions()
                } else {
                    // checkAndRequestBackgroundPermissions()
                }
            }

            REQUEST_CODE -> {
                println("BACKGROUND_LOCATION ::: $REQUEST_CODE")
                val backgroundLocationPermissionGranted =
                    grantResults.getOrNull(0) == PackageManager.PERMISSION_GRANTED
                if (backgroundLocationPermissionGranted) {
                    println("Background location permission granted")
                } else {
                    // checkAndRequestBackgroundPermissions()
                    println("Background location permission denied")
                }
            }

            else -> {
                // Handle other request codes if needed
            }
        }
    }

    private fun showPermissionExplanationDialog(permissionsNeededList: List<String>) {
        val message = buildPermissionExplanationMessage(permissionsNeededList)
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage(message)
            .setPositiveButton("Grant") { dialog, _ ->
                // Re-request the permissions
                dialog.dismiss()
                ActivityCompat.requestPermissions(
                    this,
                    permissionsNeededList.toTypedArray(),
                    REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton("Settings") { dialog, _ ->
                // Open app settings
                dialog.dismiss()
                openApplicationMainSettingsDialog()
            }
            .create()
            .show()
    }

    private fun buildPermissionExplanationMessage(permissionsNeeded: List<String>): String {
        val permissionReasons = permissionsNeeded.map {
            when (it) {
                android.Manifest.permission.READ_EXTERNAL_STORAGE -> "Reading external storage is needed to access photos, videos, or files."
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE -> "Writing to external storage is needed to save photos, videos, or files."
                android.Manifest.permission.POST_NOTIFICATIONS -> "Notification permission is needed to send you alerts and updates."
                android.Manifest.permission.READ_CONTACTS -> "Contact Numbers."
                else -> "This permission is needed for the app to function properly."
            }
        }

        return "The app requires the following permissions:\n\n" + permissionReasons.joinToString("\n\n") +
                "\n\nPlease grant them for a better experience."
    }

    // Permission Dialogs
    private fun openApplicationMainSettingsDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle("All the permissions must approve")
            .setMessage("All the permissions must approve for apps smooth functionality")
            .setPositiveButton("Go to Settings") { dialog, _ ->
                // Open the app's settings
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.setData(uri)
                startActivity(intent)
            }.show()
    }

}