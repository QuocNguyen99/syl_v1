package com.hqnguyen.syl.ui.menu

import androidx.fragment.app.activityViewModels
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.convertToAvatar
import com.hqnguyen.syl.databinding.FragmentMenuBinding
import com.hqnguyen.syl.ui.dialog.DialogUpdateImageFragment
import com.hqnguyen.syl.ui.login.UserViewModel

class MenuFragment : BaseFragment<FragmentMenuBinding>(FragmentMenuBinding::inflate) {
    private val userVM: UserViewModel by activityViewModels()

    override fun onViewCreated() {
        binding.cardAvatar.setOnClickListener {
            val dialog = DialogUpdateImageFragment()
            dialog.show(childFragmentManager, "")
        }
    }

    override fun onObserverLiveData() {
        userVM.uriAvatarUser.observe(viewLifecycleOwner) {
            it?.let { uri ->
                val bitmap = uri.convertToAvatar(requireContext())
                binding.imgAvatar.setImageBitmap(bitmap)
            }
        }
    }
}