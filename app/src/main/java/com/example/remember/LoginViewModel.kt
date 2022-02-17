package com.example.remember

import android.content.ContentResolver
import android.provider.CalendarContract.Events.CONTENT_URI
import android.provider.CalendarContract.Events.DTEND
import android.provider.CalendarContract.Events.DTSTART
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.copper.flow.mapToList
import app.cash.copper.flow.observeQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() :
  ViewModel() {

  private val _loginResult = MutableLiveData<LoginResult>()
  val loginResult: LiveData<LoginResult> = _loginResult

  fun getData(contentResolver: ContentResolver) = viewModelScope.launch {

  }
}

data class Event(val startTime: String, val endTime: String)