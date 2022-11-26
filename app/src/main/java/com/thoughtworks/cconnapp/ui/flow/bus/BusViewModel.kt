package com.thoughtworks.cconnapp.ui.flow.bus

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BusViewModel @Inject constructor() : ViewModel() {
    companion object {
        private const val TAG = "BusViewModel"
    }
}