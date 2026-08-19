package com.example.core.maps

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object GoogleMapsLauncher {

    /**
     * Opens Google Maps to search for nearby medical shops / pharmacies.
     * Can optionally filter by specific medicine name.
     */
    fun openNearbyMedicalShops(context: Context, medicineName: String? = null) {
        val query = if (!medicineName.isNullOrBlank()) {
            "pharmacy medical shop stocking $medicineName near me"
        } else {
            "medical shops and pharmacies near me"
        }

        val encodedQuery = Uri.encode(query)
        val geoUri = Uri.parse("geo:0,0?q=$encodedQuery")
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            // Fallback to web browser Google Maps search if Google Maps app is not installed
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedQuery")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            try {
                context.startActivity(webIntent)
            } catch (webEx: Exception) {
                Toast.makeText(context, "Unable to open Google Maps", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Opens Google Maps directed at a specific pharmacy with coordinates or address.
     */
    fun openPharmacyDirections(context: Context, pharmacyName: String, address: String? = null) {
        val query = if (!address.isNullOrBlank()) {
            "$pharmacyName, $address"
        } else {
            "$pharmacyName pharmacy"
        }

        val encodedQuery = Uri.encode(query)
        val geoUri = Uri.parse("geo:0,0?q=$encodedQuery")
        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
        }

        try {
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$encodedQuery")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            try {
                context.startActivity(webIntent)
            } catch (webEx: Exception) {
                Toast.makeText(context, "Unable to open Google Maps directions", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
