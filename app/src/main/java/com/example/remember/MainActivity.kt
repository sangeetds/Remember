package com.example.remember

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.SystemClock
import android.provider.CalendarContract.Events
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Observer
import com.example.remember.ui.theme.RememberTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.progressindicator.CircularProgressIndicator
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Calendar

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val loginViewModel: LoginViewModel by viewModels()
  private lateinit var googleLoginButton: SignInButton // by lazy { findViewById(id.googleSignInButton) }
  private lateinit var progressIndicator: CircularProgressIndicator // by lazy { findViewById(id.loading_icon) }
  private lateinit var mGoogleSignInClient: GoogleSignInClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      RememberTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
          Greeting(setAlarms)
        }
      }
    }
  }

  val setAlarms = {
    this.requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR), 42)

    val projection = arrayOf(Events.DTSTART, Events.DTEND)
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
    val selection = "${Events.DTSTART} >= ? AND ${Events.DTSTART}<= ?"
    val selectionArgs = arrayOf(startDay.toString(), endDay.toString())
    val result = mutableListOf<Event>()
    val cursor = contentResolver.query(
      Events.CONTENT_URI,
      projection,
      selection,
      selectionArgs,
      null
    )
    if (cursor != null) {
      while (cursor.moveToNext()) {
        result.add(
          Event(
            cursor.getString(0),
            cursor.getString(1),
          )
        )
      }
      cursor.close()
    }
    getAlarm(result)
    // darkModeConfigure()
    // setUpButtons()
    // mGoogleSignInClient = googleSignInClient()
  }

  private fun darkModeConfigure() {
    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
      Configuration.UI_MODE_NIGHT_YES -> {
      }
      Configuration.UI_MODE_NIGHT_NO -> {
      }
    }
  }

  private fun googleSignInClient(): GoogleSignInClient {
    val gso = Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestId()
      .requestEmail()
      .requestProfile()
      .build()

    val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    val account = GoogleSignIn.getLastSignedInAccount(this)

    account?.apply {
      Toast.makeText(this@MainActivity, "Logging in. Please Wait.", Toast.LENGTH_SHORT).show()
      Timber.i("User $displayName already signed in")
      progressIndicator.visibility = View.VISIBLE
    }

    return mGoogleSignInClient
  }

  private fun setUpObserver() {
    loginViewModel.loginResult.observe(this@MainActivity, Observer {
      val loginResult = it ?: return@Observer

      loginResult.apply {
        success?.let {

        }
        setResult(RESULT_OK)
      }
    })
  }

  private fun getAlarm(lists: List<Event>) {
    // Get AlarmManager instance
    val mgrAlarm = this.getSystemService(ALARM_SERVICE) as AlarmManager
    lists.forEachIndexed { i, event ->
      val intent = Intent(this, RememebrAlarmReceiver::class.java)
      intent.putExtra("time", event.startTime)
      // Loop counter `i` is used as a `requestCode`
      val pendingIntent = PendingIntent.getBroadcast(this, i, intent, 0)
      // Single alarms in 1, 2, ..., 10 minutes (in `i` minutes)
      mgrAlarm.set(
        AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime() + 1000 * 2,
        pendingIntent
      )
    }
  }

  private fun setUpButtons() {
    googleLoginButton.setOnClickListener {
      progressIndicator.visibility = View.VISIBLE
      Toast.makeText(this@MainActivity, "Logging in. Please Wait.", Toast.LENGTH_SHORT).show()
      val signInIntent: Intent = mGoogleSignInClient.signInIntent
      startActivityForResult(signInIntent, 1)
    }
  }

  override fun onBackPressed() {
    super.onBackPressed()
    finish()
  }

  private fun updateUiWithUser() {
  }

  private fun showLoginFailed(errorString: String) {
    Timber.d("Logging failed.")
    Toast.makeText(applicationContext, errorString, Toast.LENGTH_LONG).show()
    progressIndicator.visibility = View.GONE
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == 1) {
      val task = GoogleSignIn.getSignedInAccountFromIntent(data)
      handleSignInResult(task)
    }
  }

  private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
    try {
      val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
    } catch (e: ApiException) {
      Timber.e("signInResult:failed code = ${e.statusCode} with message: ${e.message}")
      showLoginFailed(e.message ?: "Error Connecting to Google")
    }
  }
}

@Composable
fun Greeting(startAlarms: () -> Unit) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight(), verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Button(onClick = startAlarms) {
      Text(text = "Set Today's Alarms")
    }
  }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  RememberTheme {
    Greeting(startAlarms = { })
  }
}

data class GoogleCalendar(
  val event_id: Int,
  val title: String,
  val organizer: String,
)