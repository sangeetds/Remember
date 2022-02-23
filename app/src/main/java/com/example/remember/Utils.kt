package com.example.remember

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun String.convertToLocalDate() =
  Instant.fromEpochMilliseconds(this.toLong()).toLocalDateTime(
    TimeZone.currentSystemDefault()
  )