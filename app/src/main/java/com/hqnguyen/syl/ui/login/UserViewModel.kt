package com.hqnguyen.syl.ui.login

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    private val _user = MutableLiveData<String>(null)
    val user: LiveData<String> = _user

    private val _uriAvatarUser = MutableLiveData<Uri?>(null)
    val uriAvatarUser: LiveData<Uri?> = _uriAvatarUser

    fun updateAvatarUser(uri: Uri?) {
        uri?.let { it ->
            _uriAvatarUser.value = it
        }
    }
}