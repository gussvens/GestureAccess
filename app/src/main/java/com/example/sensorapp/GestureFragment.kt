package com.example.sensorapp

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.sensorapp.databinding.FragmentMainBinding
import kotlin.math.abs

private const val TAG = "GestureFragment"
private const val WORK_THRESHOLD = 200
private const val NEW_WORK_THRESHOLD = 15
private const val GESTURE_TIME: Long = 15 * 1000 // ms

class ScrollingFragment : Fragment(), SensorEventListener {
    companion object{
        //Used by Widget to confirm it is the sender of the intent
        const val ACTION_WIDGET= "com.example.sensorapp.FROM_WIDGET"

        // Package names for apps in prototype, should preferably
        // be loaded in dynamically during runtime and be paired
        // with app name and icon uri in a custom settings file generated on
        // installation.
        const val FACEBOOK_PACKAGE = "com.facebook.katana"
        const val CHROME_PACKAGE = "com.android.chrome"
        const val GMAIL_PACKAGE = "com.google.android.gm"
        const val TWITTER_PACKAGE = "com.twitter.android"
        const val CANVAS_PACKAGE = "com.instructure.candroid"
        //TODO: Add more daily apps to prototype before data collection starts

        //Allows quick checking if the desired application is supported
        private val VALID_PACKAGES = listOf(
            FACEBOOK_PACKAGE,
            CHROME_PACKAGE,
            GMAIL_PACKAGE,
            TWITTER_PACKAGE,
            CANVAS_PACKAGE
        )
        fun isValidPackage(pkg: String): Boolean {return VALID_PACKAGES.contains(pkg)}
    }

    private lateinit var binding: FragmentMainBinding

    // Actuator
    private lateinit var vibrator: Vibrator

    // Sensors
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private var gyroReading = FloatArray(3)

    private var isLaunching: Boolean = false
    private var accumulatedWork: Int = 0

    private val timer = object: CountDownTimer(GESTURE_TIME, 100) {
        override fun onTick(millisUntilFinished: Long) {
            if (!isLaunching) {
                teardown()
                cancel()
            }
        }
        override fun onFinish() {
            teardown()
        }
    }

    private fun teardown() {
        accumulatedWork = 0
        isLaunching = false
        activity?.finishAndRemoveTask()
    }

    private lateinit var requestedApplication: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)

        vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        return binding.root
    }

    private fun startTimer(){
        isLaunching = true
        timer.start()
    }

    override fun onResume() {
        super.onResume()
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        // Inspects where the Intent that launched/resumed the parent Activity comes from
        // If it comes from the Widget (ACTION_WIDGET) then try to launch the application indicated by the Widget
        if(this.activity?.intent?.action.equals(ACTION_WIDGET)) {
            val appToLaunch = this.activity?.intent?.getStringExtra("appToLaunch")
            if(appToLaunch != null && isValidPackage(appToLaunch)) {
                requestedApplication = appToLaunch
                initRequestUI()
                isLaunching = true
                timer.start()
                //startTimer()
                // Overcame obstacle
                //launchApp(appToLaunch)
            }
        }
    }

    private fun initRequestUI(){
        binding.appIcon.setImageDrawable(context?.let { getIcon(it,requestedApplication) })
        binding.promptText.text = "Replace me ${resources.getString(R.string.gesture_promt_suffix)}"
        binding.difficultyText.text = "${resources.getString(R.string.difficulty_prefix)} Replace me"
        updateProgress(0)
    }

    //TODO replace with LiveData
    private  fun updateProgress(progress: Int){
        binding.progressText.text = "${resources.getString(R.string.progress_prefix)} $progress${resources.getString(R.string.progress_suffix)}"
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null)
            return

        if (isLaunching) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val previousReading = accelerometerReading.sum()
                    // Replace old values with new readings
                    System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                    val currentReading = accelerometerReading.sum()

                    val diff = abs(event.values.sum() - previousReading)

                    if (diff > NEW_WORK_THRESHOLD) {
                        accumulatedWork += diff.toInt()
                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
                Sensor.TYPE_GYROSCOPE -> {
                    System.arraycopy(event.values, 0, gyroReading, 0, gyroReading.size)
                    // Log.d(TAG, "Gyro values: ${gyroReading[0]} ${gyroReading[1]} ${gyroReading[2]}")
                }
            }

            Log.d("WORK", "Work: ${accumulatedWork}")
            if(accumulatedWork > WORK_THRESHOLD && isLaunching) {
                launchApp(requestedApplication)
            }
            else {
                updateProgress(accumulatedWork / WORK_THRESHOLD)
            }
        }
    }

    private fun launchApp(packageName: String) {
        isLaunching = false
        accumulatedWork = 0
        val launchIntent: Intent? =
            context?.getPackageManager()?.getLaunchIntentForPackage(packageName)
        launchIntent?.let { startActivity(it) }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) { return }
}