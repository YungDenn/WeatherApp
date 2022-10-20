package com.example.weatherapp.fragments

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.fragment.app.Fragment

fun Fragment.isPermissionGranted(name:String): Boolean {
    return ContextCompat.checkSelfPermission(
        activity as AppCompatActivity,
        name
    ) == PackageManager.PERMISSION_GRANTED
}