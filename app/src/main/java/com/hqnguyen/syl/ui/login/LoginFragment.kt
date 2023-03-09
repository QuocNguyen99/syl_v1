package com.hqnguyen.syl.ui.login

import android.text.InputType
import android.text.method.PasswordTransformationMethod
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.hqnguyen.syl.R
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.data.InfoDialog
import com.hqnguyen.syl.databinding.FragmentLoginBinding


class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {

    private val userVM: UserViewModel by activityViewModels()
    private var isShowPassword = false

    companion object {
        const val username = "samsung"
        const val password = "samsung123"
    }

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    override fun onViewCreated() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        initEvent()
    }

    private fun initEvent() {
        binding.btnGogle.setOnClickListener {
            showDialog(InfoDialog("Google login", "Feature will coming soon", R.drawable.ic_google))
        }

        binding.btnFacebook.setOnClickListener {
            showDialog(
                InfoDialog(
                    "Facebook login",
                    "Feature will coming soon",
                    R.drawable.ic_facebook
                )
            )
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.edtUsername.text.toString()
            val password = binding.edtPassword.text.toString()
            userVM.login(username = username, password = password)
        }

        binding.edtUsername.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.layoutUsername.setBackgroundResource(R.drawable.edittext_outline_focus)
            } else {
                binding.layoutUsername.setBackgroundResource(R.drawable.edittext_outline)
            }
        }

        binding.edtPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.layoutPassword.setBackgroundResource(R.drawable.edittext_outline_focus)
            } else {
                binding.layoutPassword.setBackgroundResource(R.drawable.edittext_outline)
            }
        }

        binding.imgHide.setOnClickListener {
            if (isShowPassword) {
                binding.imgHide.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.lock_eye
                    )
                )
                binding.edtPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                binding.edtPassword.setSelection(
                    binding.edtPassword.text.toString().toCharArray().size
                )
            } else {
                binding.imgHide.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.eye
                    )
                )
                binding.edtPassword.inputType = InputType.TYPE_CLASS_TEXT
                binding.edtPassword.transformationMethod = null
                binding.edtPassword.setSelection(
                    binding.edtPassword.text.toString().toCharArray().size
                )
            }
            isShowPassword = !isShowPassword
        }
    }

    override fun onObserverLiveData() {
        userVM.user.observe(viewLifecycleOwner) {
            if (it) {
                navController.navigate(R.id.action_loginFragment_to_homeFragment)
            }
        }
    }
}