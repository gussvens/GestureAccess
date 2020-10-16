package com.example.sensorapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * The configuration screen for the [GestureAccessWidget] AppWidget.
 */
class GestureAccessWidgetConfigureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gesture_access_widget_configure)
    }
}