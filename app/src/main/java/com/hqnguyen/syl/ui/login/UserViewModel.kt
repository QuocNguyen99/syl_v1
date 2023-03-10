package com.hqnguyen.syl.ui.login

import android.net.Uri
import androidx.lifecycle.*
import com.hqnguyen.syl.data.local.DataStoreRepositoryImpl
import com.hqnguyen.syl.ui.map.MapViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.regex.Pattern

class UserViewModel(private val dataStore: DataStoreRepositoryImpl) : ViewModel() {

    init {
        getTokenAndCheckWasLogin()
    }

    private val _user = MutableLiveData(false)
    val user: LiveData<Boolean> = _user

    private val _wasLogin = MutableLiveData(false)
    val wasLogin: LiveData<Boolean> = _wasLogin

    private val _errorLogin = MutableLiveData<String>(null)
    val errorLogin: LiveData<String> = _errorLogin

    private val _uriAvatarUser = MutableLiveData<Uri?>(null)
    val uriAvatarUser: LiveData<Uri?> = _uriAvatarUser

    fun updateAvatarUser(uri: Uri?) {
        uri?.let { it ->
            _uriAvatarUser.value = it
        }
    }

    fun login(username: String, password: String) {
        if (username.isEmpty()) {
            _errorLogin.value = "Need fill username"
            return
        }

        if (password.toCharArray().size >= 50 || password.toCharArray().size < 8) {
            _errorLogin.value = "Password length 8-50 characters"
            return
        }

        val p: Pattern = Pattern.compile("[^a-z0-9 [!@#\$%&*()_+=|<>?{}\\\\[\\\\]~-]]", Pattern.CASE_INSENSITIVE)

        if (p.matcher(username).find() || p.matcher(password).find()) {
            _errorLogin.value = "Username or password have special character"
            return
        }

        if (username != LoginFragment.username || password != LoginFragment.password) {
            _errorLogin.value = "Username or password in valid"
            return
        }


        if (username == LoginFragment.username || password == LoginFragment.password) {
            _user.value = true
            viewModelScope.launch {
                saveTokenLogin("login", "faketoken")
            }
            return
        }
    }

    private suspend fun saveTokenLogin(key: String, data: String) {
        dataStore.putString(key, data)
    }

    private fun getTokenAndCheckWasLogin(){
        viewModelScope.launch {
            val data = dataStore.getString("login")?.isNotEmpty()
            Timber.d("onObserverLiveData  data: $data")
            data?.let {
                _wasLogin.postValue(it)
            }
        }
    }
}
