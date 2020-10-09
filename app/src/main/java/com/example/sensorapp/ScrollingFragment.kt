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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.sensorapp.databinding.FragmentScollingBinding
import kotlin.math.roundToInt


class ScrollingFragment : Fragment(), SensorEventListener {

    private lateinit var binding: FragmentScollingBinding
    private lateinit var mSensorManager : SensorManager
    private lateinit var textFields: MutableList<TextView>

    private var mAccelerometer : Sensor?= null
    private var mGyroscope : Sensor?= null
    private var resume = false

    private var newGyroValue: MutableList<Int> = mutableListOf(0, 0, 0)
    private var newAccelerometerValue: MutableList<Int> = mutableListOf(0, 0, 0)
    private var gyroValues = mutableListOf<MutableList<Int>>()
    private var accelerometerValues= mutableListOf<MutableList<Int>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scolling, container, false)

        binding.startStopButton.setOnClickListener { view: View -> resume = !resume }
        binding.clearButton.setOnClickListener { view: View -> clearLog() }
        binding.app0Button.setOnClickListener { view: View ->
            //binding.loggingLayout.visibility = View.VISIBLE
            //binding.iconLayout.visibility = View.GONE
            binding.promptLayout.visibility = View.VISIBLE
            resume = true
            val timer = object: CountDownTimer(15000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    //TODO
                }
                override fun onFinish() {
                    binding.promptLayout.visibility = View.GONE
                    resume = false
                }
            }
            timer.start()
        }
        //binding.whiteBox.setOnClickListener { launchApp("com.facebook.katana")}

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
                }
                Sensor.TYPE_GYROSCOPE -> {
                    for (i in 0..newGyroValue.size - 1) { //expected is 0..2
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
            launchIfShaken()
        }
    }

    //Number of values to check
    private val numsToCheck = 5
    //Thresholds to cross
    private val thresholdAX = 2
    private val thresholdGX = 2
    private val thresholdAY = 2
    private val thresholdGY = 2
    private val thresholdAZ = 2
    private val thresholdGZ = 2

    private fun launchIfShaken(){

        launchApp("com.facebook.katana")
        binding.whiteBox.setBackgroundColor(Color.GREEN)
    }

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