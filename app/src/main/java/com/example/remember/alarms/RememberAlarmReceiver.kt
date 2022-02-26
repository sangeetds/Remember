package com.example.remember.alarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import com.example.remember.common.ALARM_FOR_TODAY
import com.example.remember.common.DAILY_ALARMS
import com.example.remember.common.EVENT
import com.example.remember.common.INTERVAL
import com.example.remember.common.convertToLocalDate
import com.example.remember.events.data.model.Event

class RememberAlarmReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    when (intent.getIntExtra(INTERVAL, ALARM_FOR_TODAY)) {
      ALARM_FOR_TODAY -> {
        val event = intent.getParcelableExtra<Event>(EVENT)
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
      DAILY_ALARMS -> {
        val alarmActivityIntent = AlarmActivity.getIntent(context = context)
        context.startActivity(alarmActivityIntent)
      }
    }
  }
}
