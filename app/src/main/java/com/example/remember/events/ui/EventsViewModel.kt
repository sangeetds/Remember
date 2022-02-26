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
import com.example.remember.R.string
import com.example.remember.alarms.RememberAlarmReceiver
import com.example.remember.common.ALARM_FOR_TODAY
import com.example.remember.common.DAILY_ALARMS
import com.example.remember.common.END_OF_DAY
import com.example.remember.common.EVENT
import com.example.remember.common.INTERVAL
import com.example.remember.common.START_OF_DAY
import com.example.remember.common.getSelectionArgs
import com.example.remember.events.EventsResult
import com.example.remember.events.data.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor() : ViewModel() {

  private val _eventsResult = MutableLiveData<EventsResult>()
  val eventsResult: LiveData<EventsResult> = _eventsResult

  fun getEvents(contentResolver: ContentResolver) = viewModelScope.launch {
    val projection = arrayOf(Events.TITLE, Events.DTSTART, Events.DTEND)
    val selection = "${Events.DTSTART} >= ? AND ${Events.DTSTART}<= ?"
    val selectionArgs = getSelectionArgs()

    contentResolver.observeQuery(CONTENT_URI, projection, selection, selectionArgs, null)
      .mapToList { cursor ->
        Event(
          title = cursor.getString(0),
          startTime = cursor.getString(1) ?: START_OF_DAY,
          endTime = cursor.getString(2) ?: END_OF_DAY
        )
      }.collect { events ->
        when (events.isNotEmpty()) {
          true -> _eventsResult.value = EventsResult(success = events, loading = false)
          else -> _eventsResult.value = EventsResult(error = string.events_error, loading = false)
        }
      }
  }

  fun getAlarm(context: Context, lists: List<Event>, mgrAlarm: AlarmManager) {
    lists.forEachIndexed { i, event ->
      val intent = Intent(context, RememberAlarmReceiver::class.java)
      intent.putExtra(EVENT, event)
      intent.putExtra(INTERVAL, ALARM_FOR_TODAY)
      val pendingIntent =
        PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_IMMUTABLE)
      mgrAlarm.set(
        AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() + 1000 * i,
        pendingIntent
      )
    }
  }

  fun setAlarmEveryday(context: Context, alarmManager: AlarmManager) {
    val intent = Intent(context, RememberAlarmReceiver::class.java)
    intent.putExtra(INTERVAL, DAILY_ALARMS)
    val calendar: Calendar = Calendar.getInstance()

    calendar.timeInMillis = System.currentTimeMillis()

    // if it's after or equal 9 am schedule for next day

    // if it's after or equal 9 am schedule for next day
    if (Calendar.getInstance()[Calendar.HOUR_OF_DAY] >= 9) {
      Timber.i("Alarm will schedule for next day!")
      calendar.add(Calendar.DAY_OF_YEAR, 1) // add, not set!
    } else {
      Timber.i("Alarm will schedule for today!")
    }
    calendar[Calendar.HOUR_OF_DAY] = 9
    calendar[Calendar.MINUTE] = 0
    calendar[Calendar.SECOND] = 0

    val pendingIntent =
      PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_IMMUTABLE)
    alarmManager.setInexactRepeating(
      AlarmManager.ELAPSED_REALTIME_WAKEUP,
      SystemClock.elapsedRealtime() + 1000L,
      AlarmManager.INTERVAL_DAY,
      pendingIntent
    )
  }
}