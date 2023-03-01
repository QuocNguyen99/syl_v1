package com.hqnguyen.syl.ui.login

import android.content.Intent
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.databinding.FragmentLoginBinding


class LoginFragment : BaseFragment<FragmentLoginBinding>(FragmentLoginBinding::inflate) {

    override fun onViewCreated() {
        initEvent()
    }

    private fun initEvent() {
        binding.btnGogle.setOnClickListener {

            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.

            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

            // Build a GoogleSignInClient with the options specified by gso.
            val mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, 123)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == 123) {
            val acct = GoogleSignIn.getLastSignedInAccount(requireActivity())
            acct?.let {
                val personName: String? = it.displayName
                val personGivenName: String? = it.givenName
                val personFamilyName: String? = it.familyName
                val personEmail: String? = it.email
                val personId: String? = it.id
                val personPhoto: Uri? = it.photoUrl
            }
        }
    }
}