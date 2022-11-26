package com.thoughtworks.cconnapp.ui.flow.client

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor() : ViewModel() {
    companion object {
        private const val TAG = "ClientViewModel"
    }
}