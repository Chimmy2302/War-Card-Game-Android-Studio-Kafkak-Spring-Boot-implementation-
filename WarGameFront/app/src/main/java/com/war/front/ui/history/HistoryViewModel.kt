package com.war.front.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.war.front.network.RetrofitClient
import com.war.front.network.models.HistoryEntry
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {

    private val _history = MutableLiveData<List<HistoryEntry>>()
    val history: LiveData<List<HistoryEntry>> = _history

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadHistory() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getHistory()
                if (response.isSuccessful) {
                    _history.value = response.body() ?: emptyList()
                } else {
                    _error.value = "Server error: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load history: ${e.message}"
            }
        }
    }
}