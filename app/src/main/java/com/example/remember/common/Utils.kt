package com.example.remember.common

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import java.util.Calendar

fun String.convertToLocalDate() =
  Instant.fromEpochMilliseconds(this.toLong()).toLocalDateTime(
    TimeZone.currentSystemDefault()
  )

fun getSelectionArgs(): Array<String> {
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
  Timber.i("Selection arg is ${selectionArgs.toList()}")
  return selectionArgs
}

const val START_OF_DAY: String = "00:00:00"
const val END_OF_DAY: String = "23:59:59"
const val DAILY_ALARMS: Int = 1
const val ALARM_FOR_TODAY: Int = 0
const val EVENT: String = "event"
const val INTERVAL: String = "interval"
