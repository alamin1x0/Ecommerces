package com.developeralamin.ecommerceapp.utils

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.developeralamin.ecommerceapp.model.LocationData
import com.google.gson.Gson
import java.io.IOException
import java.util.*


class GPSTracker(private val mContext: Context) : Service(), LocationListener {


    private var isGPSEnabled = false

    private var isNetworkEnabled = false

    private var canGetLocation = false

    private var location: Location? = null

    private var latitude = 0.0
    private var longitude = 0.0

    companion object {
        var city = ""
        var country = ""
    }
    var fullAddress: String? = null
    interface AddressResultListener {
        fun onAddressResult(address: String?)
    }

    // The minimum distance to change Updates in meters
    private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 100 // 10 meters

    // The minimum time between updates in milliseconds
    private val MIN_TIME_BW_UPDATES: Long = 1000 * 60 * 1 // 1 minute

    // Location Manager
    private var locationManager: LocationManager? = null

    init {
        getLocation()
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(): Location? {
        try {
            locationManager = mContext.getSystemService(LOCATION_SERVICE) as LocationManager

            // Getting GPS status
            isGPSEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true

            // Getting network status
            isNetworkEnabled = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true

            if (!isGPSEnabled && !isNetworkEnabled) {
                // No network provider is enabled
            } else {
                canGetLocation = true

                if (isNetworkEnabled) {
                    locationManager?.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                        this
                    )
                    Log.d("Network", "Network")

                    if (locationManager != null) {
                        location = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

                        location?.let { updateLocationData(it) }
                    }
                }

                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager?.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                            this
                        )
                        Log.d("GPS Enabled", "GPS Enabled")

                        if (locationManager != null) {
                            location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                            location?.let { updateLocationData(it) }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return location
    }

    @SuppressLint("Range")
    private fun updateLocationData(location: Location) {
        val geocoder = Geocoder(mContext, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 0)
        var fullAddress = ""
         city = addresses?.get(0).toString()
         country = addresses?.get(0)?.countryName ?: ""
        var locationModel: LocationData? = null
        if (!addresses.isNullOrEmpty()) {
            for (address in addresses) {
                for (i in 0..address.maxAddressLineIndex) {
                    val addressLine = address.getAddressLine(i) + "," + address.subLocality
                    fullAddress = addressLine
                    city = addressLine
                }
                val featureName = address.featureName
                val state = address.adminArea
                val country = address.countryName
                val postalCode = address.postalCode
                locationModel = LocationData(location.latitude.toString(), location.longitude.toString(), fullAddress, city, country, postalCode, state)
            }
        }

        latitude = location.latitude
        longitude = location.longitude
    }

    fun getAddressFromLocation(
        context: Context?,
        latitude: Double,
        longitude: Double,
        resultListener: AddressResultListener
    ) {
        val geocoder = Geocoder(context!!)
        if (Geocoder.isPresent()) {
            // Geocoder is enabled, proceed with geocoding requests
            Log.d("MyUtils", "Geocoder is enabled on this device")
        } else {
            // Geocoder is not available on this device
            Log.e("MyUtils", "Geocoder is not enabled on this device")
            // Handle the situation accordingly
        }
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val gson = Gson()
            val addressesJson = gson.toJson(addresses)
            assert(addresses != null)
            for (address in addresses!!) {
                for (i in 0..address.maxAddressLineIndex) {
                    val addressLine = address.getAddressLine(i) + "," + address.subLocality
                    fullAddress = addressLine
                }
                val city = address.subLocality
                val featureName = address.featureName
                val state = address.adminArea
                val country = address.countryName
                val postalCode = address.postalCode
                fullAddress?.let { Log.d("GPSAddress", it) }
            }
            resultListener.onAddressResult(fullAddress)
            if (addresses.isEmpty()) {
                resultListener.onAddressResult(null)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun stopUsingGPS() {
        locationManager?.removeUpdates(this)
    }

    fun getLatitude(): Double {
        return if (location != null) {
            location!!.latitude
        } else {
            latitude
        }
    }

    fun getLongitude(): Double {
        return if (location != null) {
            location!!.longitude
        } else {
            longitude
        }
    }

    fun getCurrentCity(): String {
        getLocation()
        return city
    }

    fun findCity(latitude: Double, longitude: Double): LocationData? {
        var locationModel: LocationData? = null
        try{
        val geocoder = Geocoder(mContext.applicationContext,Locale.ENGLISH)

        var addresses: List<Address?>
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 2)!!
            val gson = Gson()
            val addressesJson = gson.toJson(addresses)
            Log.d("addressModel", "findCity: "+addresses)
            assert(addresses != null)


            for (address in addresses) {
                for (i in 0..address!!.maxAddressLineIndex) {
                    val addressLine = address.getAddressLine(i) + "," + address.subLocality+","+address.locality
                    fullAddress= addressLine
                }
                val city = fullAddress
                val featureName = address.featureName
                val state = address.adminArea
                val country = address.countryName
                val postalCode = address.postalCode

                locationModel = LocationData(latitude.toString(), longitude.toString(), fullAddress, city, country, postalCode, state)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }}catch (e : Exception){
            locationModel = LocationData(latitude.toString(), longitude.toString(), "","","", "", "")
        }

        return locationModel
    }

    fun canGetLocation(): Boolean {
        return canGetLocation
    }



    override fun onLocationChanged(location: Location) {
        // TODO Auto-generated method stub
    }

    override fun onProviderDisabled(provider: String) {
        // TODO Auto-generated method stub
    }

    override fun onProviderEnabled(provider: String) {
        // TODO Auto-generated method stub
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
        // TODO Auto-generated method stub
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO Auto-generated method stub
        return null
    }
}
