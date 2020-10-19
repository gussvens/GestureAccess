package com.example.sensorapp

import android.graphics.drawable.Drawable

data class ExternalApp(
        val packageName: String,
        val label: String,
        val icon: Drawable,
)
