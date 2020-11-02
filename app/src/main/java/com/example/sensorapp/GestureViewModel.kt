package com.example.sensorapp

import android.app.Application
import androidx.lifecycle.*
import kotlin.math.abs
import kotlin.random.Random

private const val LOW_DIFFICULTY_THRESHOLD = 150
private const val MEDIUM_DIFFICULTY_THRESHOLD = 300
private const val HIGH_DIFFICULTY_THRESHOLD = 450
private const val EXTREME_DIFFICULTY_THRESHOLD = 900

class GestureViewModel(application: Application) : AndroidViewModel(application) {
    enum class Gesture {
        SHAKE, SHAKE_X, SHAKE_Y_Z
    }

    enum class Difficulty {
        LOW, MEDIUM, HIGH, EXTREME
    }

    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int>
        get() = _progress

    private val _gesture = MutableLiveData<Gesture>()
    val gesture: LiveData<Gesture>
        get() = _gesture

    private val _difficulty = MutableLiveData<Difficulty>()
    val difficulty: LiveData<Difficulty>
        get() = _difficulty

    val gestureText: LiveData<String> = Transformations.map(_gesture) { gesture ->
        with (getApplication<Application>().resources) {
            when (gesture) {
                Gesture.SHAKE -> getString(R.string.gesture_shake_all)
                Gesture.SHAKE_X -> getString(R.string.gesture_shake_x)
                Gesture.SHAKE_Y_Z -> getString(R.string.gesture_shake_y_z)
                else -> getString(R.string.gesture_shake_all)
            }
        }
    }

    private val difficultyThreshold: LiveData<Int> = Transformations.map(_difficulty) { difficulty ->
        when (difficulty) {
            Difficulty.LOW -> LOW_DIFFICULTY_THRESHOLD
            Difficulty.MEDIUM -> MEDIUM_DIFFICULTY_THRESHOLD
            Difficulty.HIGH -> HIGH_DIFFICULTY_THRESHOLD
            Difficulty.EXTREME -> EXTREME_DIFFICULTY_THRESHOLD
            else -> LOW_DIFFICULTY_THRESHOLD
        }
    }

    init {
        _progress.value = 0
        _gesture.value = Gesture.SHAKE
        _difficulty.value = Difficulty.LOW
    }

    fun updateProgress(accumulatedWork: Int) {
        val threshold = difficultyThreshold.value ?: LOW_DIFFICULTY_THRESHOLD
        _progress.value = ((accumulatedWork.toFloat() / threshold)*100).toInt()
    }

    fun randomGesture() {
        _gesture.value = when(Random.nextInt(0, 3)) {
            0 -> Gesture.SHAKE
            1 -> Gesture.SHAKE_X
            2 -> Gesture.SHAKE_Y_Z
            else -> Gesture.SHAKE
        }
    }

    fun randomDifficulty() {
        _difficulty.value = when(Random.nextInt(0, 4)) {
            0 -> Difficulty.LOW
            1 -> Difficulty.MEDIUM
            2 -> Difficulty.HIGH
            3 -> Difficulty.EXTREME
            else -> Difficulty.LOW
        }
    }

    fun calculateSuccess(accumulatedWork: Int): Boolean {
        val threshold = when(difficulty.value) {
            Difficulty.LOW -> LOW_DIFFICULTY_THRESHOLD
            Difficulty.MEDIUM -> MEDIUM_DIFFICULTY_THRESHOLD
            Difficulty.HIGH -> HIGH_DIFFICULTY_THRESHOLD
            Difficulty.EXTREME -> EXTREME_DIFFICULTY_THRESHOLD
            else -> LOW_DIFFICULTY_THRESHOLD
        }
        return (accumulatedWork > threshold)
    }

    fun calculateWork(oldReadings: FloatArray, readings: FloatArray): Int {
        if (oldReadings.size != 3)
            throw IllegalArgumentException("The oldReadings array do not contain three values," +
                    " there should be values for x, y, and z. Size: ${oldReadings.size}")
        if (readings.size != 3)
            throw IllegalArgumentException("The readings array do not contain three values," +
                    " there should be values for x, y, and z. Size: ${readings.size}")

        var work = 0
        val (X, Y, Z)  = arrayOf(0, 1, 2)
        when (gesture.value) {
            Gesture.SHAKE -> {
                val old = readings.sum()
                val new = oldReadings.sum()
                work = (abs(new - old)).toInt()
            }
            Gesture.SHAKE_X -> {
                work = (abs(readings[X] - oldReadings[X])).toInt()
            }
            Gesture.SHAKE_Y_Z -> {
                work = (abs(
                        readings[Y] + readings[Z]
                                - oldReadings[Y] - oldReadings[Z]
                )).toInt()
            }
        }

        return work
    }
}