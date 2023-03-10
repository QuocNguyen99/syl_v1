package com.hqnguyen.syl.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hqnguyen.syl.data.local.DataStoreRepositoryImpl

class UserViewModelFactory constructor(
    private val dataStore: DataStoreRepositoryImpl
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            UserViewModel(dataStore) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}