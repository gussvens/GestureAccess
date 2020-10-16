package com.example.sensorapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.drawable.toBitmap

private const val TAG = "GestureAccessWidget"

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [GestureAccessWidgetConfigureActivity]
 */
class GestureAccessWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    // TODO: Template code, should be removed properly
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deletePref(context, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent);

        if (intent != null && intent.action != null) {
            val appToLaunch = intent.action.toString()
            if(ScrollingFragment.isValidPackage(appToLaunch)) launchPendingIntent(context, appToLaunch)
        }
    }

    private fun launchPendingIntent(context: Context?, nameOfAppToLaunch: String){
        val i = Intent(ScrollingFragment.ACTION_WIDGET, null)
        i.addCategory(Intent.CATEGORY_LAUNCHER)
        val cn = ComponentName("com.example.sensorapp", "com.example.sensorapp.MainActivity")
        i.component = cn
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        i.putExtra("appToLaunch", nameOfAppToLaunch)
        if (context != null) {
            startActivity(context, i, null)
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.gesture_access_widget)

    val pref: String = loadPref(context, appWidgetId)
    when(pref) {
        context.resources.getString(R.string.left_name) -> {
            //Andreas' test preferences
            setupButton(context, views, R.id.button00, ScrollingFragment.TIDAL_PACKAGE)
            setupButton(context, views, R.id.button01, ScrollingFragment.CHROME_PACKAGE)
            setupButton(context, views, R.id.button02, ScrollingFragment.GMAIL_PACKAGE)
            setupButton(context, views, R.id.button03, ScrollingFragment.AVANZA_PACKAGE)
            setupButton(context, views, R.id.button10, ScrollingFragment.FIREFOX_PACKAGE)
            setupButton(context, views, R.id.button11, ScrollingFragment.DISCORD_PACKAGE)
            setupButton(context, views, R.id.button12, ScrollingFragment.SWISH_PACKAGE)
            setupButton(context, views, R.id.button13, ScrollingFragment.MESSENGER_PACKAGE)
            //setupButton(context, views, R.id.button03, ScrollingFragment.NAME_OF_PACKAGE)
        }
        context.resources.getString(R.string.right_name) -> {
            //Gustav's test preferences
            setupButton(context, views, R.id.button00, ScrollingFragment.FIREFOX_PACKAGE)
            setupButton(context, views, R.id.button01, ScrollingFragment.CHROME_PACKAGE)
            setupButton(context, views, R.id.button02, ScrollingFragment.GMAIL_PACKAGE)
            setupButton(context, views, R.id.button03, ScrollingFragment.CANVAS_PACKAGE)
            setupButton(context, views, R.id.button04, ScrollingFragment.MESSENGER_PACKAGE)
            setupButton(context, views, R.id.button10, ScrollingFragment.NINEGAG_PACKAGE)
            setupButton(context, views, R.id.button11, ScrollingFragment.DISCORD_PACKAGE)
            setupButton(context, views, R.id.button12, ScrollingFragment.SWISH_PACKAGE)
            setupButton(context, views, R.id.button13, ScrollingFragment.TELEGRAM_PACKAGE)
            //setupButton(context, views, R.id.button04, ScrollingFragment.NAME_OF_PACKAGE)
        }
        context.resources.getString(R.string.default_name) -> {
            setupButton(context, views, R.id.button00, ScrollingFragment.CANVAS_PACKAGE)
            setupButton(context, views, R.id.button01, ScrollingFragment.CHROME_PACKAGE)
            setupButton(context, views, R.id.button32, ScrollingFragment.FACEBOOK_PACKAGE)
            setupButton(context, views, R.id.button03, ScrollingFragment.GMAIL_PACKAGE)
            setupButton(context, views, R.id.button44, ScrollingFragment.TWITTER_PACKAGE)
        }
    }

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

internal fun setupButton(context: Context, views: RemoteViews, buttonID: Int, appToLaunch: String){
    // Get the application icon or fallback to a default
    with (getIcon(context, appToLaunch)) {
        if (this == null) {
            views.setImageViewResource(buttonID, R.drawable.add_icon)
        } else {
            views.setImageViewBitmap(buttonID, this.toBitmap())
        }
    }

    // Create intent and set click listener
    val intent = Intent(context, GestureAccessWidget::class.java)
    intent.action = appToLaunch
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    views.setOnClickPendingIntent(buttonID, pendingIntent)
}

/**
 * Retrieves the default icon for an application based on a the package name as a Drawable,
 * or returns null if the application/icon is not found
 */
internal fun getIcon(context: Context, packageName: String): Drawable? {
    var icon: Drawable? = null
    try {
        val packageManager = context?.packageManager
        icon = packageManager.getApplicationIcon(packageName)
    } catch (nameException: PackageManager.NameNotFoundException) {
        Log.e(TAG, "Package name not found: ${nameException.message}")
    }
    return icon
}