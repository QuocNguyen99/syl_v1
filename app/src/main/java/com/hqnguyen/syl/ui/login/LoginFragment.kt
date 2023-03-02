package com.hqnguyen.syl.ui.login

import androidx.fragment.app.activityViewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.hqnguyen.syl.R
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.databinding.FragmentLoginBinding

class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {

    private val userVM: UserViewModel by activityViewModels()

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    override fun onViewCreated() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        initEvent()
    }

    private fun initEvent() {
        binding.btnGogle.setOnClickListener {
            signIn()
        }
        binding.btnLogin.setOnClickListener {
            userVM.login()
            navController.navigate(R.id.action_loginFragment_to_homeFragment)
        }
    }

    private fun signIn() {
        //TODO
    }
}