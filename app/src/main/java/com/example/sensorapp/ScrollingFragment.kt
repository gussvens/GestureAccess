package com.example.sensorapp

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.*
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

    private var newGyroValue: MutableList<Int> = mutableListOf(0,0,0)
    private var newAccelerometerValue: MutableList<Int> = mutableListOf(0,0,0)
    private var gyroValues = mutableListOf<MutableList<Int>>()
    private var accelerometerValues= mutableListOf<MutableList<Int>>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scolling, container, false)

        binding.startStopButton.setOnClickListener { view: View -> resume = !resume }
        binding.clearButton.setOnClickListener { view: View -> clearLog() }

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
            val values = mutableListOf<Int>(0,0,0)

            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    for(i in 0..newAccelerometerValue.size-1) { //expected is 0..2
                        val v = event.values[i].roundToInt()
                        values[i] = v
                        newAccelerometerValue[i] = v
                    }
                    accelerometerValues.add(values)
                }
                Sensor.TYPE_GYROSCOPE -> {
                    for(i in 0..newGyroValue.size-1) { //expected is 0..2
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

            updateBackground()
        }
    }

    //Number of values to check
    private val numsToCheck = 35
    //Thresholds to cross
    private val thresholdAX = 2
    private val thresholdGX = 2
    private val thresholdAY = 2
    private val thresholdGY = 2
    private val thresholdAZ = 2
    private val thresholdGZ = 2

    private fun updateBackground(){
        if(gyroValues.size < numsToCheck-1 || accelerometerValues.size < numsToCheck-1) return

        if(anyOver(thresholdAX, accelerometerValues[0].subList(0,numsToCheck)) &&
            anyOver(thresholdGX, gyroValues[0].subList(0,numsToCheck)) &&
            anyOver(thresholdAY, accelerometerValues[1].subList(0,numsToCheck)) &&
            anyOver(thresholdGY, gyroValues[1].subList(0,numsToCheck)) &&
            anyOver(thresholdAZ, accelerometerValues[2].subList(0,numsToCheck)) &&
            anyOver(thresholdGZ, gyroValues[2].subList(0,numsToCheck))) {
            binding.scrollView.setBackgroundColor(Color.GREEN)
        } else {binding.scrollView.setBackgroundColor(Color.WHITE)}
    }

    private fun anyOver(threshold: Int, list: List<Int>): Boolean {
        for(i in list) if (i>=threshold) return true
        return false
    }

    private fun clearLog() {
        for (i in 0..textFields.size-1) textFields[i].text = ""
        gyroValues.clear()
        accelerometerValues.clear()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) { return }
}