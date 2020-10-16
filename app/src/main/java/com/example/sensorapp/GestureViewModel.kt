package com.example.sensorapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GestureViewModel: ViewModel() {
    private val _progress = MutableLiveData<Int>()
    val progress: LiveData<Int>
        get() = _progress

    private val _isLaunching = MutableLiveData<Boolean>()
    val isLaunching: LiveData<Boolean>
        get() = _isLaunching

    init {
        _progress.value = 0
    }

    fun updateProgress(progress: Int) {
        _progress.value = progress
    }
}