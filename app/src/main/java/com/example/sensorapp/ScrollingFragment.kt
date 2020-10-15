package com.example.sensorapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.sensorapp.databinding.FragmentScrollingBinding
import kotlin.math.abs
import kotlin.math.roundToInt

private const val WORK_THRESHOLD = 3000
private const val NEW_WORK_THRESHOLD = 20

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
        //TODO: Add more daily apps to prototype before data collection starts

        //Allows quick checking if the desired application is supported
        private val VALID_PACKAGES = listOf(
            FACEBOOK_PACKAGE,
            CHROME_PACKAGE,
            GMAIL_PACKAGE,
            TWITTER_PACKAGE
        )
        fun isValidPackage(pkg: String): Boolean {return VALID_PACKAGES.contains(pkg)}
    }

    private lateinit var binding: FragmentScrollingBinding
    private lateinit var mSensorManager : SensorManager
    private lateinit var textFields: MutableList<TextView>

    // Sensors to use
    private var mAccelerometer : Sensor?= null
    private var mGyroscope : Sensor?= null

    // Represents if the program cares about new values from sensors or not
    private var resume = false

    private var accumulatedWork: Int = 0
    private var isLaunching: Boolean = false

    private var newGyroValue: MutableList<Int> = mutableListOf(0, 0, 0)
    private var newAccelerometerValue: MutableList<Int> = mutableListOf(0, 0, 0)
    private var gyroValues = mutableListOf<MutableList<Int>>()
    private var accelerometerValues= mutableListOf<MutableList<Int>>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scrolling, container, false)

        binding.startStopButton.setOnClickListener { view: View -> resume = !resume }
        binding.clearButton.setOnClickListener { view: View -> clearLog() }
        binding.facebookButton.setOnClickListener { view: View ->
            binding.promptLayout.visibility = View.VISIBLE
            resume = true
            val timer = object: CountDownTimer(4000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    //TODO
                }
                override fun onFinish() {
                    binding.promptLayout.visibility = View.GONE
                    accumulatedWork = 0
                    resume = false
                    isLaunching = false
                }
            }
            timer.start()
        }

        textFields = mutableListOf(binding.leftText, binding.midText, binding.rightText)

        mSensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL)

        // Inspects where the Intent that launched/resumed the parent Activity comes from
        // If it comes from the Widget (ACTION_WIDGET) then try to launch the application indicated by the Widget
        if(this.activity?.intent?.action.equals(ACTION_WIDGET)) {
            val appToLaunch = this.activity?.intent?.getStringExtra("appToLaunch")
            if(appToLaunch != null && isValidPackage(appToLaunch)) {
                //TODO: Add relevant obstacle for the specified application
                //Could be random or customized by user
                //when (appToLaunch){
                //   (application package)  -> (create application specific obstacle)
                //}

                // Overcame obstacle
                launchApp(appToLaunch)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }

    fun resumeReading(view: View) { this.resume = true }

    fun pauseReading(view: View) { this.resume = false }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null && resume) {
            val values = mutableListOf<Int>(0, 0, 0)

            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    for (i in 0..newAccelerometerValue.size - 1) { //expected is 0..2
                        val v = event.values[i].roundToInt()
                        values[i] = v
                        newAccelerometerValue[i] = v
                    }
                    accelerometerValues.add(values)

                    val newWork = (
                            abs(event.values[0])
                                    + abs(event.values[1])
                                    + abs(event.values[2])
                            ).toInt()
                    Log.d("Scrolling Fragment", "Adding new work: $newWork")
                    if (newWork > NEW_WORK_THRESHOLD) {
                        accumulatedWork += newWork
                    }
                }
                Sensor.TYPE_GYROSCOPE -> {
                    for (i in 0 until newGyroValue.size) { //expected is 0..2
                        val v = event.values[i].roundToInt()
                        values[i] = v
                        newGyroValue[i] = v
                    }
                    gyroValues.add(values)
                }
            }

            for(i in 0..textFields.size-1) { //expected is 0..2
                val newText = newAccelerometerValue[i].toString() + " | " + newGyroValue[i].toString() + "\n" + textFields[i].text
                textFields[i].text = newText
            }

            if(values[0] > 10 || values[1] > 10 || values[2] > 10) {
                binding.scrollView.setBackgroundColor(Color.GREEN)
            } else {binding.scrollView.setBackgroundColor(Color.WHITE)}
            //updateBackground()

            Log.d("WORK", "Work: ${accumulatedWork}")
            if(accumulatedWork > WORK_THRESHOLD && !isLaunching) {
                isLaunching = true
                launchApp("com.facebook.katana")
            }
        }
    }

    /*
    //Number of values to check
    private val numsToCheck = 5

    //Thresholds to cross
    private val thresholdAX = 2
    private val thresholdGX = 2
    private val thresholdAY = 2
    private val thresholdGY = 2
    private val thresholdAZ = 2
    private val thresholdGZ = 2


    private fun updateBackground(){
        if(gyroValues.size < numsToCheck || accelerometerValues.size < numsToCheck) return

        for(i in 0..numsToCheck-1) if(xyzOver(
                        thresholdAX,
                        thresholdAY,
                        thresholdAZ,
                        accelerometerValues
                ) && xyzOver(thresholdGX, thresholdGY, thresholdGZ, gyroValues)) {
            binding.scrollView.setBackgroundColor(Color.GREEN)
        } else {binding.scrollView.setBackgroundColor(Color.WHITE)}
    }

    private fun xyzOver(thresholdX: Int, thresholdY: Int, thresholdZ: Int, list: List<List<Int>>): Boolean {
        return false
    }
    */

    private fun clearLog() {
        for (i in 0..textFields.size-1) textFields[i].text = ""
        gyroValues.clear()
        accelerometerValues.clear()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) { return }

    private fun launchApp(packageName: String){
        val launchIntent: Intent? =
            activity?.getPackageManager()?.getLaunchIntentForPackage(packageName)
        launchIntent?.let { startActivity(it) }
    }
}