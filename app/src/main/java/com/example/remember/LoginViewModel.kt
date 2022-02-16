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
    // val calendars = mutableListOf<GoogleCalendar>()
    // contentResolver.observeQuery(
    //   Calendars.CONTENT_URI,
    //   arrayOf(
    //     Calendars._ID,  // 0
    //     Calendars.ACCOUNT_NAME,  // 1
    //     Calendars.CALENDAR_DISPLAY_NAME,  // 2
    //   ), null, null, null
    // ).mapToList { cursor ->
    //   GoogleCalendar(cursor.getInt(0), cursor.getString(1), cursor.getString(2))
    // }.collect {
    //   Timber.i("Calendars: $it")
    //   calendars.addAll(it)
    // }
    val projection = arrayOf(DTSTART, DTEND)
    val calendar: Calendar = Calendar.getInstance()
    calendar.set(
      calendar.get(Calendar.YEAR),
      calendar.get(Calendar.MONTH),
      calendar.get(Calendar.DAY_OF_MONTH),
      0,
      0,
      0
    )
    val startDay: Long = calendar.timeInMillis
    calendar.set(
      calendar.get(Calendar.YEAR),
      calendar.get(Calendar.MONTH),
      calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59
    )
    val endDay: Long = calendar.timeInMillis
    val selection = "$DTSTART >= ? AND $DTSTART<= ?"
    val selectionArgs = arrayOf(startDay.toString(), endDay.toString())

    contentResolver.observeQuery(
      CONTENT_URI,
      projection,
      selection,
      selectionArgs,
      null
    ).mapToList { cursor ->
      Event(
        cursor.getString(0),
        cursor.getString(1),
      )
    }.collect {
      _loginResult.value = LoginResult(success = it)
    }
  }
}

data class Event(val startTime: String, val endTime: String)