package com.hqnguyen.syl.ui.login

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.regex.Matcher
import java.util.regex.Pattern

class UserViewModel : ViewModel() {
    private val _user = MutableLiveData<Boolean>(false)
    val user: LiveData<Boolean> = _user

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
        if (username.isNullOrEmpty()) {
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
            return
        }
    }
}