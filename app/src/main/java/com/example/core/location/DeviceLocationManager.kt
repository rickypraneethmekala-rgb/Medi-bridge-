package com.example.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale

data class UserLocationData(
    val latitude: Double,
    val longitude: Double,
    val locality: String = "Current Location",
    val fullAddress: String = ""
)

object DeviceLocationManager {

    fun getLocalityName(context: Context, lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val subLocality = addr.subLocality
                val locality = addr.locality
                val area = when {
                    !subLocality.isNullOrBlank() -> subLocality
                    !locality.isNullOrBlank() -> locality
                    else -> addr.featureName ?: "Detected Location"
                }
                area
            } else {
                "Detected Location"
            }
        } catch (e: Exception) {
            "Detected Location"
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocationFlow(context: Context): Flow<UserLocationData?> = callbackFlow {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val area = getLocalityName(context, location.latitude, location.longitude)
                trySend(UserLocationData(location.latitude, location.longitude, area))
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            // Check last known locations first
            val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val bestLast = lastGps ?: lastNetwork
            if (bestLast != null) {
                val area = getLocalityName(context, bestLast.latitude, bestLast.longitude)
                trySend(UserLocationData(bestLast.latitude, bestLast.longitude, area))
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    10000L,
                    50f,
                    listener,
                    Looper.getMainLooper()
                )
            }
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000L,
                    50f,
                    listener,
                    Looper.getMainLooper()
                )
            }
        } catch (e: SecurityException) {
            trySend(null)
        } catch (e: Exception) {
            trySend(null)
        }

        awaitClose {
            try {
                locationManager.removeUpdates(listener)
            } catch (_: Exception) {}
        }
    }
}
