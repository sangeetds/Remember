package com.example.remember.events.ui

import android.Manifest.permission
import android.app.AlarmManager
import android.content.ContentResolver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remember.common.convertToLocalDate
import com.example.remember.events.EventsResult
import com.example.remember.events.ui.theme.RememberTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (checkSelfPermission(permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
      Timber.d("Permission is granted")
    } else {
      requestPermissions(arrayOf(permission.READ_CALENDAR), 42)
      Timber.d("Permission is revoked")
    }
    setContent {
      RememberTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
          Greeting()
        }
      }
    }
    darkModeConfigure()
  }

  private fun darkModeConfigure() {
    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
      Configuration.UI_MODE_NIGHT_YES -> {
        Timber.d("Configuring Dark Theme")
      }
      Configuration.UI_MODE_NIGHT_NO -> {
        Timber.d("Configuring Light Theme")
      }
    }
  }

  override fun onBackPressed() {
    super.onBackPressed()
    finish()
  }
}

@Composable
fun Greeting(eventsViewModel: EventsViewModel = viewModel()) {
  val eventsResult = eventsViewModel.eventsResult.observeAsState(initial = EventsResult())
  val context = LocalContext.current
  val contentResolver = context.contentResolver

  when {
    eventsResult.value.loading -> {
      Timber.i("Displaying Landing page.")
      val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
      DisplayButtons(eventsViewModel, contentResolver, context = context, alarmManager = alarmManager)
    }
    eventsResult.value.error != null -> {
      Timber.i("No events found for today.")
      val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
      Toast.makeText(context, context.getString(eventsResult.value.error!!), Toast.LENGTH_SHORT)
        .show()
      DisplayButtons(eventsViewModel, contentResolver, context = context, alarmManager = alarmManager)
    }
    else -> {
      Timber.i("Found ${eventsResult.value.success?.size ?: 0} events. Showing today's events.")
      val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
      DisplayTodaysEvents(eventsResult, eventsViewModel, context, alarmManager)
    }
  }
}

@Composable
private fun DisplayButtons(
  eventsViewModel: EventsViewModel,
  contentResolver: ContentResolver,
  context: Context,
  alarmManager: AlarmManager
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight(), verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Button(onClick = {
      Toast.makeText(context, "Loading today's events", Toast.LENGTH_SHORT).show()
      eventsViewModel.getEvents(contentResolver)
    }, modifier = Modifier.width(200.dp)) {
      Text(text = "See Today's Events")
    }
    Button(onClick = {
      Toast.makeText(context, "Loading everday events", Toast.LENGTH_SHORT).show()
      eventsViewModel.setAlarmEveryday(context = context, alarmManager = alarmManager)
    }, modifier = Modifier.width(200.dp)) {
      Text(text = "Set Alarms Everyday")
    }
  }
}

@Composable
private fun DisplayTodaysEvents(
  eventList: State<EventsResult>,
  eventsViewModel: EventsViewModel,
  context: Context,
  alarmManager: AlarmManager
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight()
      .padding(top = 70.dp, bottom = 70.dp, start = 40.dp, end = 40.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    eventList.value.success?.let { events ->
      LazyColumn {
        items(events) { event ->
          Text(text = "${event.title}: (${event.startTime.convertToLocalDate()}, " +
            "${event.endTime.convertToLocalDate()}), Alarm ${if (event.alarmSet) "Set" else "Not Set"}")
        }
      }
      if (events.none { it.alarmSet }) {
        Text(text = "No Alarms set for today.")
      }
      Button(
        onClick = { eventsViewModel.setAlarm(context, events, alarmManager) },
        modifier = Modifier.padding(top = 400.dp)
      ) {
        Text(text = "Set Today's Alarms")
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  RememberTheme {
    Greeting()
  }
}