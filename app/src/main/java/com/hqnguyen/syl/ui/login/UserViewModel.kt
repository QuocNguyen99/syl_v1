package com.hqnguyen.syl.ui.login

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hqnguyen.syl.addBorder
import com.hqnguyen.syl.getCroppedBitmap
import java.io.ByteArrayOutputStream

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