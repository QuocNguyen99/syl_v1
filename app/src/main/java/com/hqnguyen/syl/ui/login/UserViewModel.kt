package com.hqnguyen.syl.ui.login

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hqnguyen.syl.data.local.DataStoreRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.regex.Pattern


class UserViewModel(private val dataStore: DataStoreRepositoryImpl) : ViewModel() {

    private val _user = MutableLiveData(false)
    val user: LiveData<Boolean> = _user

    private val _wasLogin = MutableLiveData(false)
    val wasLogin: LiveData<Boolean> = _wasLogin

    private val _errorLogin = MutableLiveData<String?>(null)
    val errorLogin: LiveData<String?> = _errorLogin

    private val _uriAvatarUser = MutableLiveData<Uri?>(null)
    val uriAvatarUser: LiveData<Uri?> = _uriAvatarUser

    fun updateAvatarUser(uri: Uri?) {
        uri?.let {
            _uriAvatarUser.value = it
        }
    }

    fun clearDataError() {
        _errorLogin.value = null
    }

    fun clearDataUser() {
        _user.value = false
    }

    fun login(username: String, password: String) {
        val p: Pattern =
            Pattern.compile("[^a-z0-9 [!@#\$%&*()_+=|<>?{}\\\\[\\\\]~-]]", Pattern.CASE_INSENSITIVE)

        if (username.isEmpty()) {
            _errorLogin.value = "Need enter all field"
            return
        }

        if (password.isEmpty()) {
            _errorLogin.value = "Need enter all field"
            return
        }

        if (password.toCharArray().size >= 50 || password.toCharArray().size < 8) {
            _errorLogin.value = "Password length 8-50 characters"
            return
        }

        if (p.matcher(username).find()) {
            _errorLogin.value = "Username have special character"
            return
        }

        if (p.matcher(password).find()) {
            _errorLogin.value = "Password have special character"
            return
        }

        if (username != LoginFragment.username || password != LoginFragment.password) {
            _errorLogin.value = "Username or password in valid"
            return
        }

        if (username == LoginFragment.username || password == LoginFragment.password) {
            _user.value = true
            saveString("login", "faketoken")
            return
        }
    }

    fun logout() {
        viewModelScope.launch {
            Timber.d("navController logout")
            saveString("login", "")
            _wasLogin.value = false
            _user.value = false
        }
    }

    private fun saveString(key: String, data: String) {
        viewModelScope.launch {
            dataStore.putString(key, data)
        }
    }

    fun getTokenAndCheckWasLogin() {
        viewModelScope.launch {
            val data = dataStore.getString("login")?.isNotEmpty()
            Timber.d("getTokenAndCheckWasLogin: $data")
            data?.let {
                withContext(Dispatchers.Main.immediate) {
                    Timber.d("navController getTokenAndCheckWasLogin")
                    _wasLogin.value = it
                }
            }
        }
    }
}
