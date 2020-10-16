package com.example.sensorapp

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.sensorapp.databinding.FragmentWidgetConfigBinding

class WidgetConfigFragment : Fragment(){

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var binding: FragmentWidgetConfigBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_widget_config, container, false)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        this.activity?.setResult(Activity.RESULT_CANCELED)

        this.setListener(binding.defaultButton, resources.getString(R.string.default_name))
        this.setListener(binding.leftButton, resources.getString(R.string.left_name))
        this.setListener(binding.rightButton, resources.getString(R.string.right_name))

        return binding.root
    }

    private fun setListener(view: View, text: String){
        view.setOnClickListener{
            // When the button is clicked, store the string locally
            val widgetText = text.toString()
            this.context?.let { it1 -> savePref(it1, appWidgetId, text) }

            // It is the responsibility of the configuration activity to update the app widget
            val appWidgetManager = AppWidgetManager.getInstance(this.context)
            this.context?.let { it1 -> updateAppWidget(it1, appWidgetManager, appWidgetId) }

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            this.activity?.setResult(Activity.RESULT_OK, resultValue)
            this.activity?.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Find the widget id from the intent.
        val intent = this.activity?.intent
        val extras = intent?.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            this.activity?.finish()
            return
        }
    }
}

private const val PREFS_NAME = "com.example.sensorapp.GestureAccessWidget"
private const val PREF_PREFIX_KEY = "appwidget_"

// Write the prefix to the SharedPreferences object for this widget
internal fun savePref(context: Context, appWidgetId: Int, text: String) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
    prefs.apply()
}

// Read the prefix from the SharedPreferences object for this widget.
// If there is no preference saved, get the default from a resource
internal fun loadPref(context: Context, appWidgetId: Int): String {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val prefValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
    return prefValue ?: context.getString(R.string.default_name)
}

internal fun deletePref(context: Context, appWidgetId: Int) {
    val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
    prefs.remove(PREF_PREFIX_KEY + appWidgetId)
    prefs.apply()
}