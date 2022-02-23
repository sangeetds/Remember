package com.example.remember

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun String.convertToLocalDate() =
  Instant.fromEpochMilliseconds(this.toLong()).toLocalDateTime(
    TimeZone.currentSystemDefault()
  )

const val startOfDay: String = "00:00:00"
const val endOfDay: String = "23:59:59"