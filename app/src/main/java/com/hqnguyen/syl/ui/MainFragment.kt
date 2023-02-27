package com.hqnguyen.syl.ui

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.hqnguyen.syl.R
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.databinding.FragmentMainBinding
import com.hqnguyen.syl.ui.login.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainFragment : BaseFragment<FragmentMainBinding>(FragmentMainBinding::inflate) {
    private val userVM: UserViewModel by activityViewModels()

    override fun onObserverLiveData() {
        userVM.user.observe(viewLifecycleOwner) { user ->
            lifecycleScope.launch {
                delay(500)
                if (!true) {
                    navigation(R.id.loginFragment)
                } else {
                    navigation(R.id.homeFragment)
                }
            }
        }
    }
}