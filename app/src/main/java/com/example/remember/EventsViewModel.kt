package com.example.remember

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.provider.CalendarContract.Events
import android.provider.CalendarContract.Events.CONTENT_URI
import androidx.activity.ComponentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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

    val result = mutableListOf<Event>()

    val cursor = contentResolver.query(
      CONTENT_URI,
      projection,
      selection,
      selectionArgs,
      null
    )
    if (cursor != null) {
      while (cursor.moveToNext()) {
        if (cursor.getString(0) != null && cursor.getString(1) != null && cursor.getString(2) != null) {
          result.add(Event(cursor.getString(0), cursor.getString(1), cursor.getString(2)))
        }
      }
      cursor.close()
    }

    _eventsResult.value = EventsResult(success = result)
  }

  fun getAlarm(context: Context, lists: List<Event>, mgrAlarm: AlarmManager) {
    // Get AlarmManager instance
    lists.forEachIndexed { i, event ->
      val intent = Intent(context, RememebrAlarmReceiver::class.java)
      intent.putExtra("time", event.startTime)
      val pendingIntent = PendingIntent.getBroadcast(context, i, intent, PendingIntent.FLAG_IMMUTABLE)
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
    return arrayOf(startDay.toString(), endDay.toString())
  }
}