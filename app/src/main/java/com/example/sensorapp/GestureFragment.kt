package com.example.sensorapp

import android.app.Application
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

private const val TAG = "GestureFragment"
private const val NEW_WORK_THRESHOLD = 15
private const val GESTURE_TIME: Long = 15 * 1000 // ms

class ScrollingFragment : Fragment(), SensorEventListener {
    companion object{
        //Used by Widget to confirm it is the sender of the intent
        const val ACTION_WIDGET= "com.example.sensorapp.FROM_WIDGET"
    }

    private lateinit var binding: FragmentMainBinding
    private lateinit var viewModel: GestureViewModel

    // Actuator
    private lateinit var vibrator: Vibrator

    // Sensor
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)

    private var isLaunching: Boolean = false
    private var accumulatedWork: Int = 0
    private lateinit var timer: CountDownTimer

    private lateinit var requestedApplication: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)

        viewModel = GestureViewModel(context?.applicationContext as Application)

        binding.gestureViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        timer = object: CountDownTimer(GESTURE_TIME, GESTURE_TIME) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                teardown()
            }
        }

        return binding.root
    }

    private fun requestLaunch(packageName: String) {
        timer.cancel()
        requestedApplication = packageName
        accumulatedWork = 0
        isLaunching = true
        viewModel.randomGesture()
        viewModel.randomDifficulty()
        timer.start()
        binding.appIcon.setImageDrawable(context?.let { getIcon(it,requestedApplication) })
    }

    private fun teardown() {
        timer.cancel()
        accumulatedWork = 0
        isLaunching = false
        activity?.finishAndRemoveTask()
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

        // Inspects where the Intent that launched/resumed the parent Activity comes from
        // If it comes from the Widget (ACTION_WIDGET) then try to launch the application indicated by the Widget
        if(this.activity?.intent?.action.equals(ACTION_WIDGET)) {
            val appToLaunch = this.activity?.intent?.getStringExtra("appToLaunch")?: ""
            requestLaunch(appToLaunch)
        } else if(this.activity?.intent?.action.equals("android.intent.action.MAIN")) {
            teardown()
        }
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
                    val work = viewModel.calculateWork(accelerometerReading, event.values)
                    if (work > NEW_WORK_THRESHOLD) {
                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                        accumulatedWork += work
                    }

                    // Replace old values with new readings
                    System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
                }
            }

            Log.d("WORK", "Work: ${accumulatedWork}")
            if(viewModel.calculateSuccess(accumulatedWork) && isLaunching) {
                launchApp(requestedApplication)
            }

            viewModel.updateProgress(accumulatedWork)
        }
    }

    private fun launchApp(packageName: String) {
        val launchIntent: Intent? =
            context?.getPackageManager()?.getLaunchIntentForPackage(packageName)
        launchIntent?.let { startActivity(it) }
        teardown()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) { return }
}