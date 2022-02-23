package com.example.remember

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
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.remember.ui.theme.RememberTheme
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val eventsViewModel: EventsViewModel by viewModels()
  private lateinit var progressIndicator: CircularProgressIndicator // by lazy { findViewById(id.loading_icon) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (checkSelfPermission(permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
      Timber.d("Permission is granted")
    } else {
      requestPermissions(arrayOf(permission.READ_CALENDAR), 42);
      Timber.d("Permission is revoked")
    }
    setContent {
      RememberTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
          Greeting()
        }
      }
    }
    // darkModeConfigure()
    // setUpObserver()
  }

  private fun darkModeConfigure() {
    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
      Configuration.UI_MODE_NIGHT_YES -> {
      }
      Configuration.UI_MODE_NIGHT_NO -> {
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
  val displayAlarmButton = remember { mutableStateOf(false) }
  val eventList = eventsViewModel.eventsResult.observeAsState(initial = EventsResult())
  val context = LocalContext.current
  val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
  val contentResolver = context.contentResolver

  if (displayAlarmButton.value) {
    DisplayTodaysEvents(eventList, eventsViewModel, context, alarmManager)
  } else {
    DisplayButtons(eventsViewModel, contentResolver, eventList, displayAlarmButton, context)
  }
}

@Composable
private fun DisplayButtons(
  eventsViewModel: EventsViewModel,
  contentResolver: ContentResolver,
  eventList: State<EventsResult>,
  displayAlarmButton: MutableState<Boolean>,
  context: Context
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight(), verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Button(onClick = {
      eventsViewModel.getEvents(contentResolver)
      val eventsResult = eventList.value
      when {
        eventsResult.success != null && !eventsResult.loading -> displayAlarmButton.value = true
        eventsResult.success == null && !eventsResult.loading -> Toast.makeText(context, eventsResult.error, Toast.LENGTH_SHORT).show()
      }
    }) {
      Text(text = "Get Today's Events")
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
      LazyColumn() {
        items(events) { event ->
          Text(text = "${event.title}: (${event.startTime.convertToLocalDate()}, ${event.endTime.convertToLocalDate()})")
        }
      }
      Button(
        onClick = { eventsViewModel.getAlarm(context, events, alarmManager) },
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