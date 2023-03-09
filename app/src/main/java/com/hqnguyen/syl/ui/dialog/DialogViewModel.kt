package com.hqnguyen.syl.ui.dialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hqnguyen.syl.data.InfoDialog

class DialogViewModel : ViewModel() {

    private val _infoDialog = MutableLiveData<InfoDialog?>()
    val infoDialog: LiveData<InfoDialog?> = _infoDialog

    fun setDataDialog(data: InfoDialog) {
        _infoDialog.value = data
    }

    fun clearDialog() {
        _infoDialog.postValue(null)
    }
}