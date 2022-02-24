package com.example.remember.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import com.example.remember.events.data.Event
import com.example.remember.convertToLocalDate

class RememberAlarmReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val event = intent.getParcelableExtra<Event>("event")

    event?.let {
      val alarmIntent: Intent = Intent(AlarmClock.ACTION_SET_ALARM)
        .putExtra(AlarmClock.EXTRA_MESSAGE, event.title)
        .putExtra(AlarmClock.EXTRA_HOUR, event.startTime.convertToLocalDate().hour)
        .putExtra(AlarmClock.EXTRA_MINUTES, event.startTime.convertToLocalDate().minute)
        .putExtra(AlarmClock.EXTRA_SKIP_UI, true)
      alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      context.startActivity(alarmIntent)
    }
  }
}
