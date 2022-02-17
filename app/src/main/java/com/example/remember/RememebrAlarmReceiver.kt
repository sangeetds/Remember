package com.example.remember

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import androidx.appcompat.app.AppCompatActivity
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class RememebrAlarmReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    val stringExtra = intent.getStringExtra("time")
    val i: Intent = Intent(AlarmClock.ACTION_SET_ALARM)
      .putExtra(AlarmClock.EXTRA_MESSAGE, "Alarm")
      .putExtra(
        AlarmClock.EXTRA_HOUR,
        Instant.fromEpochMilliseconds(stringExtra!!.toLong()).toLocalDateTime(
          TimeZone.currentSystemDefault()
        ).hour
      )
      .putExtra(
        AlarmClock.EXTRA_MINUTES,
        Instant.fromEpochMilliseconds(stringExtra.toLong()).toLocalDateTime(
          TimeZone.currentSystemDefault()
        ).minute
      )
      .putExtra(AlarmClock.EXTRA_SKIP_UI, true)
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(i)
  }
}
