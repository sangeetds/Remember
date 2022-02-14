package com.socialbox.login.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.socialbox.login.data.UserRepository
import com.socialbox.common.enums.Result
import com.socialbox.login.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val userRepository: UserRepository) :
  ViewModel() {

  private val _loginResult = MutableLiveData<LoginResult>()
  val loginResult: LiveData<LoginResult> = _loginResult

  fun login(user: User) = viewModelScope.launch {
    Timber.i("Making request to the server for user: $user")
    val result = userRepository.login(user)
    _loginResult.value = when (result) {
      is Result.Success -> LoginResult(success = result.data)
      is Result.Created -> LoginResult(created = result.data)
      is Result.Error -> LoginResult(error = result.exception.localizedMessage)
    }
  }

  fun updateSettings(user: User) = viewModelScope.launch {
    Timber.i("Adding name and photo for user: $user")
    val result = userRepository.updateSettings(user)
    _loginResult.value = when (result) {
      is Result.Success -> LoginResult(success = result.data)
      is Result.Created -> LoginResult(created = result.data)
      is Result.Error -> LoginResult(error = result.exception.localizedMessage)
    }
  }
}