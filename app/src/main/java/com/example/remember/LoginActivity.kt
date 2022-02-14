package com.example.remember

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.socialbox.login.ui.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

  private val loginViewModel: LoginViewModel by viewModels()
  private lateinit var googleLoginButton: SignInButton // by lazy { findViewById(id.googleSignInButton) }
  private lateinit var progressIndicator: CircularProgressIndicator // by lazy { findViewById(id.loading_icon) }
  private lateinit var mGoogleSignInClient: GoogleSignInClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {

    }
    setUpObserver()
    darkModeConfigure()
    setUpButtons()
    mGoogleSignInClient = googleSignInClient()
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
      Toast.makeText(this@LoginActivity, "Logging in. Please Wait.", Toast.LENGTH_SHORT).show()
      Timber.i("User $displayName already signed in")
      progressIndicator.visibility = View.VISIBLE
    }

    return mGoogleSignInClient
  }

  private fun setUpObserver() {
    loginViewModel.loginResult.observe(this@LoginActivity, Observer {
      val loginResult = it ?: return@Observer

      loginResult.apply {
        error?.let {
          Timber.e("Login not successful")
          showLoginFailed(error)
        }
        success?.let {
          Timber.i("Logged in successfully")
          updateUiWithUser()
        }
        setResult(RESULT_OK)
      }
    })
  }

  private fun setUpButtons() {
    googleLoginButton.setOnClickListener {
      progressIndicator.visibility = View.VISIBLE
      Toast.makeText(this@LoginActivity, "Logging in. Please Wait.", Toast.LENGTH_SHORT).show()
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