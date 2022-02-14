package com.socialbox.login.ui

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.transition.Fade
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.airbnb.deeplinkdispatch.DeepLink
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.socialbox.R.drawable
import com.socialbox.R.id
import com.socialbox.R.layout
import com.socialbox.R.string
import com.socialbox.group.ui.GroupActivity
import com.socialbox.login.data.model.User
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
@DeepLink("https://social-boxx.herokuapp.com/invite/{random}/{id}")
class LoginActivity : AppCompatActivity() {

  private val loginViewModel: LoginViewModel by viewModels()
  private val appImage: ImageView by lazy { findViewById(id.appImage) }
  private val googleLoginButton: SignInButton by lazy { findViewById(id.googleSignInButton) }
  private val progressIndicator: CircularProgressIndicator by lazy { findViewById(id.loading_icon) }
  private val signInButton: MaterialButton by lazy { findViewById(id.signInButton) }
  private val loginLayout: ConstraintLayout by lazy { findViewById(id.loginView) }
  private val displayName: EditText by lazy { findViewById(id.displayName) }
  private lateinit var mGoogleSignInClient: GoogleSignInClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (intent.extras?.getBoolean(DeepLink.IS_DEEP_LINK, false) == true) {
      Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show()
    }
    setContentView(layout.activity_login)
    setUpObserver()
    darkModeConfigure()
    setUpButtons()
    mGoogleSignInClient = googleSignInClient()
  }

  private fun darkModeConfigure() {
    when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
      Configuration.UI_MODE_NIGHT_YES -> {
        Picasso.get().load(drawable.app_logo_dark).into(appImage)
      }
      Configuration.UI_MODE_NIGHT_NO -> {
        Picasso.get().load(drawable.app_logo).into(appImage)
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
      val user = User(name = displayName ?: "", email = email)
      loginViewModel.login(user)
      progressIndicator.visibility = View.VISIBLE
    }

    if (account == null) signInButton.isEnabled = true
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
        created?.let {
          Timber.i("Created user.")
          updateDisplaySettings(created)
        }
        success?.let {
          Timber.i("Logged in successfully")
          updateUiWithUser(success as User)
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

    signInButton.setOnClickListener {
      appImage.animate().alpha(0.4f)

      val transition: Transition = Fade()
      transition.duration = 600
      transition.addTarget(id.loginView)
      transition.addTarget(id.signInButton)
      TransitionManager.beginDelayedTransition(findViewById(id.loginConstraintLayout), transition)

      it.visibility = View.GONE
      loginLayout.visibility = View.VISIBLE
    }
  }

  override fun onBackPressed() {
    super.onBackPressed()
    finish()
  }

  private fun updateUiWithUser(model: User) {
    Timber.i("Login successful for $model")
    Timber.i("Starting HomeActivity with user: $model")
    Toast.makeText(applicationContext, "Welcome ${model.displayName}!", Toast.LENGTH_LONG).show()

    val invitedGroupId = intent.extras?.getString("id")?.toInt()
    val deepLink = invitedGroupId != null
    val groupIntent = GroupActivity.createIntent(this, model, invitedGroupId, deepLink)
    startActivity(groupIntent)
    finish()
  }

  private fun updateDisplaySettings(created: User) {
    appImage.animate().alpha(0.2f)
    progressIndicator.visibility = View.GONE

    val transition: Transition = Fade()
    transition.duration = 600
    transition.addTarget(id.loginView)
    transition.addTarget(id.signInButton)
    transition.addTarget(id.displayLayout)
    TransitionManager.beginDelayedTransition(findViewById(id.loginConstraintLayout), transition)

    loginLayout.visibility = View.GONE
    signInButton.text = getString(string.continueLogin)
    signInButton.visibility = View.VISIBLE
    signInButton.isEnabled = false
    findViewById<ConstraintLayout>(id.displayLayout).visibility = View.VISIBLE

    displayName.doOnTextChanged { text, _, _, _ ->
      signInButton.isEnabled = text?.isNotBlank() ?: false
    }

    signInButton.setOnClickListener {
      progressIndicator.visibility = View.VISIBLE
      loginViewModel.updateSettings(
        User(
          created.id,
          displayName = displayName.text.toString(),
          photoURL = null,
          groups = created.groups
        )
      )
    }
  }

  private fun showLoginFailed(errorString: String) {
    Timber.d("Logging failed.")
    Toast.makeText(applicationContext, errorString, Toast.LENGTH_LONG).show()
    progressIndicator.visibility = View.GONE
    if (displayName.visibility == View.VISIBLE) {
      displayName.error = errorString
    }
    signInButton.isEnabled = true
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
      val user = User(name = account?.displayName ?: "", email = account?.email)
      loginViewModel.login(user)
    } catch (e: ApiException) {
      Timber.e("signInResult:failed code = ${e.statusCode} with message: ${e.message}")
      showLoginFailed(e.message ?: "Error Connecting to Google")
    }
  }
}