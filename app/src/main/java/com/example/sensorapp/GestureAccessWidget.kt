package com.example.sensorapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat.startActivity


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [GestureAccessWidgetConfigureActivity]
 */
class GestureAccessWidget : AppWidgetProvider() {
    companion object{
        final val ACTION_ICON_CLICKED = "com.example.sensorapp.ICON_CLICKED"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            Log.d("GestureAccessWidget", "onReceive() " + intent.getAction())
        };
        super.onReceive(context, intent);

        if (intent != null) {
            if (ACTION_ICON_CLICKED == intent.action)
            {
                val intent = Intent(Intent.ACTION_MAIN, null)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                val cn = ComponentName("com.example.sensorapp", "com.example.sensorapp.MainActivity")
                intent.component = cn
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                if (context != null) {
                    startActivity(context, intent, null)
                }
            }
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.gesture_access_widget)

    setupButton(context, views, R.id.button00, R.drawable.canvas_icon, "0")
    setupButton(context, views, R.id.button01, R.drawable.google_chrome_icon, "1")
    setupButton(context, views, R.id.button32, R.drawable.facebook_icon, "2")
    setupButton(context, views, R.id.button03, R.drawable.gmail_icon, "3")
    setupButton(context, views, R.id.button44, R.drawable.twitter_icon, "4")

    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

internal fun setupButton(
    context: Context,
    views: RemoteViews,
    buttonID: Int,
    iconID: Int,
    packageName: String
){
    views.setImageViewResource(buttonID, iconID)

    //val intent = Intent(context, GestureAccessWidget::class.java)
    val intent = Intent(GestureAccessWidget.ACTION_ICON_CLICKED)
    intent.putExtra("package", packageName)
    val componentName: ComponentName = ComponentName(context, GestureAccessWidget::class.java)
    intent.component = componentName
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
    views.setOnClickPendingIntent(buttonID, pendingIntent)

    /*
    Intent broadcast = new Intent(ClockWidgetProvider.WIDGET_DATA_CHANGED_ACTION);
    ComponentName componentName = new ComponentName(context, WorldClockWidgetProvider.class);
    broadcast.setComponent(componentName);
    context.sendBroadcast(broadcast);
    */
}