package com.example.remember

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.CalendarContract.Calendars
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
          Greeting("Android")
        }
      }
    }
    setUpObserver()
    this.requestPermissions(arrayOf(Manifest.permission.READ_CALENDAR), 42)
    this.loginViewModel.getData(contentResolver = contentResolver)
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
          Timber.i("Logged in successfully")

        }
        setResult(RESULT_OK)
      }
    })
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
fun Greeting(name: String) {
  Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
  RememberTheme {
    Greeting("Android")
  }
}

data class GoogleCalendar(
  val event_id: Int,
  val title: String,
  val organizer: String,
)