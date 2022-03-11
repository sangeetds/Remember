package com.example.remember.alarms

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.remember.events.ui.EventsViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

  private val eventsViewModel: EventsViewModel by viewModels()
  private val alarmManager: AlarmManager by lazy { this.getSystemService(ALARM_SERVICE) as AlarmManager }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    eventsViewModel.eventsResult.observe(this@AlarmActivity, Observer {
      val eventResult = it ?: return@Observer

      eventResult.success?.let { events ->
        Timber.i("${events.size} events found")
        eventsViewModel.setAlarm(this@AlarmActivity, events, alarmManager)
      }

      eventResult.error?.let {
        Timber.e("No Events found for today.")
      }
      finish()
    })
    eventsViewModel.getEvents(contentResolver = contentResolver)
  }

  companion object {
    fun getIntent(context: Context): Intent {
      val intent = Intent(context, AlarmActivity::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      return intent
    }
  }
}