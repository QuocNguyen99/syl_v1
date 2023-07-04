package com.hqnguyen.syl.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hqnguyen.syl.R
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.data.local.DataStoreRepositoryImpl
import com.hqnguyen.syl.databinding.FragmentMainBinding
import com.hqnguyen.syl.ui.login.UserViewModel
import com.hqnguyen.syl.ui.login.UserViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class MainFragment : BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {
    private lateinit var userVM: UserViewModel

    override fun onViewCreated() {
        val dataStore = DataStoreRepositoryImpl(requireContext())
        userVM = ViewModelProvider(
            requireActivity(),
            UserViewModelFactory(dataStore)
        )[UserViewModel::class.java]
        userVM.getTokenAndCheckWasLogin()
    }


    override fun onObserver() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(1000)
            userVM.wasLogin.observe(viewLifecycleOwner) { isLogin ->
                Timber.d("onObserverLiveData: $isLogin")
                lifecycleScope.launch {
                    delay(500)
                    if (!isLogin) {
                        navigation(R.id.action_mainFragment_to_loginFragment)
                    } else {
                        navigation(R.id.action_mainFragment_to_homeFragment)
                    }
                }
            }
        }
    }
}