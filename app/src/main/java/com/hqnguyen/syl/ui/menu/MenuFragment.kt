package com.hqnguyen.syl.ui.menu

import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hqnguyen.syl.R
import com.hqnguyen.syl.base.BaseFragment
import com.hqnguyen.syl.databinding.FragmentMenuBinding
import com.hqnguyen.syl.ui.dialog.DialogUpdateImageFragment
import com.hqnguyen.syl.ui.login.UserViewModel
import com.hqnguyen.syl.ui.map.MapViewModel
import com.hqnguyen.syl.ui.map.MapViewModelFactory
import com.hqnguyen.syl.utils.DataHelper
import com.hqnguyen.syl.utils.convertToAvatar
import kotlinx.coroutines.launch

class MenuFragment : BaseFragment<FragmentMenuBinding>(FragmentMenuBinding::inflate) {
    private val userVM: UserViewModel by activityViewModels()
    private lateinit var mapVM: MapViewModel

    override fun onViewCreated() {
        mapVM = ViewModelProvider(
            requireActivity(),
            MapViewModelFactory(context = requireContext())
        )[MapViewModel::class.java]

        initView()
        initEvent()
        initData()
    }

    private fun initView() {
        binding.header.viewBack.isVisible = false
    }

    private fun initEvent() {
        binding.imgAvatar.setOnClickListener {
            val dialog = DialogUpdateImageFragment()
            dialog.show(childFragmentManager, "")
        }
        binding.cardList.setOnClickListener {
            navigation(R.id.action_homeFragment_to_detailListHistoryFragment)
        }
        binding.cardLogout.setOnClickListener {
            userVM.logout()
            navController.navigate(R.id.action_homeFragment_to_loginFragment)
        }
        binding.cardAuth.setOnClickListener { }
        binding.cardSupport.setOnClickListener { }
        binding.cardTheme.setOnClickListener { }
        binding.cardAbout.setOnClickListener { }
        binding.cardAccount.setOnClickListener { }
    }

    private fun initData() {

    }

    override fun onObserver() {
        userVM.uriAvatarUser.observe(viewLifecycleOwner) {
            it?.let { uri ->
                val bitmap = uri.convertToAvatar(requireContext())
                binding.imgAvatar.setImageBitmap(bitmap)
            }
        }

        lifecycleScope.launch {
            mapVM.getListLocationFormLocal().observe(viewLifecycleOwner) {
                it?.let {
                    val data = DataHelper.getInstance().convertData(it)
//                    binding.tvTotal.text = "Total: " + data.size.toString()
                }
            }
        }
    }
}
