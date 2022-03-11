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
import com.example.remember.events.data.EventsRepository
import com.example.remember.events.data.model.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class EventsViewModel @Inject constructor(private val eventsRepository: EventsRepository) :
  ViewModel() {

  private val _eventsResult = MutableLiveData<EventsResult>()
  val eventsResult: LiveData<EventsResult> = _eventsResult

  fun getEvents(contentResolver: ContentResolver) = viewModelScope.launch {
    if (eventsRepository.cacheEvents != null) {
      _eventsResult.value = EventsResult(success = eventsRepository.cacheEvents, loading = false)
    } else {
      val projection = arrayOf(Events.TITLE, Events.DTSTART, Events.DTEND)
      val selection = "${Events.DTSTART} >= ? AND ${Events.DTSTART}<= ?"
      val selectionArgs = getSelectionArgs()

      contentResolver.observeQuery(CONTENT_URI, projection, selection, selectionArgs, null)
        .mapToList { cursor ->
          Event(
            title = cursor.getString(0) ?: "",
            startTime = cursor.getString(1) ?: START_OF_DAY,
            endTime = cursor.getString(2) ?: END_OF_DAY
          )
        }.collect { events ->
          Timber.i("${events.size} events found.")
          when (events.isNotEmpty()) {
            true -> _eventsResult.value = EventsResult(success = events, loading = false)
            else -> _eventsResult.value = EventsResult(error = string.events_error, loading = false)
          }
          eventsRepository.saveEvents(events = events)
        }
    }
  }

  fun setAlarm(context: Context, events: List<Event>, mgrAlarm: AlarmManager) =
    viewModelScope.launch {
      events.forEachIndexed { i, event ->
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
        event.alarmSet = true
      }

      eventsRepository.updateEvents(events = events)
      eventsRepository.cacheEvents = null
    }

  fun setAlarmEveryday(context: Context, alarmManager: AlarmManager) {
    val intent = Intent(context, RememberAlarmReceiver::class.java)
    intent.putExtra(INTERVAL, DAILY_ALARMS)
    val calendar: Calendar = Calendar.getInstance()

    if (Calendar.getInstance()[Calendar.HOUR_OF_DAY] >= 20) {
      Timber.i("Alarm will schedule for next day!")
      calendar.add(Calendar.DAY_OF_YEAR, 1) // add, not set!
    } else {
      Timber.i("Alarm will schedule for today!")
    }
    calendar[Calendar.HOUR_OF_DAY] = 19
    calendar[Calendar.MINUTE] = 20
    calendar[Calendar.SECOND] = 0
    Timber.i("${calendar.timeInMillis}")
    val pendingIntent =
      PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    alarmManager.setInexactRepeating(
      AlarmManager.ELAPSED_REALTIME_WAKEUP,
      SystemClock.elapsedRealtime() + 200,
      24*60*60*1000,
      pendingIntent
    )
  }
}