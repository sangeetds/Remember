package com.example.remember.events.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.provider.CalendarContract.Events
import android.provider.CalendarContract.Events.CONTENT_URI
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.copper.flow.mapToList
import app.cash.copper.flow.observeQuery
import com.example.remember.common.END_OF_DAY
import com.example.remember.events.data.Event
import com.example.remember.events.EventsResult
import com.example.remember.R.string
import com.example.remember.common.START_OF_DAY
import com.example.remember.alarms.RememberAlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor() :
  ViewModel() {

  private val _eventsResult = MutableLiveData<EventsResult>()
  val eventsResult: LiveData<EventsResult> = _eventsResult

  fun getEvents(contentResolver: ContentResolver) = viewModelScope.launch {
    val projection = arrayOf(Events.TITLE, Events.DTSTART, Events.DTEND)
    val selection = "${Events.DTSTART} >= ? AND ${Events.DTSTART}<= ?"
    val selectionArgs = getSelectionArgs()

    contentResolver.observeQuery(CONTENT_URI, projection, selection, selectionArgs, null)
      .mapToList { cursor ->
        Event(
          cursor.getString(0),
          cursor.getString(1) ?: START_OF_DAY,
          cursor.getString(2) ?: END_OF_DAY
        )
      }.collect { events ->
        when (events.isNotEmpty()) {
          true -> _eventsResult.value = EventsResult(success = events, loading = false)
          else -> _eventsResult.value = EventsResult(error = string.events_error, loading = false)
        }
      }
  }

  fun getAlarm(context: Context, lists: List<Event>, mgrAlarm: AlarmManager) {
    // Get AlarmManager instance
    lists.forEachIndexed { i, event ->
      val intent = Intent(context, RememberAlarmReceiver::class.java)
      intent.putExtra("event", event)
      val pendingIntent =
        PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_IMMUTABLE)
      mgrAlarm.set(
        AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() + 1000 * i,
        pendingIntent
      )
    }
  }

  private fun getSelectionArgs(): Array<String> {
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

    val selectionArgs = arrayOf(startDay.toString(), endDay.toString())
    Timber.i("Selection arg is $selectionArgs")
    return selectionArgs
  }
}