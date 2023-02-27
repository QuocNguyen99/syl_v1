package com.hqnguyen.syl.ui.dialog

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hqnguyen.syl.databinding.FragmentDialogUpdateImageBinding
import com.hqnguyen.syl.ui.login.UserViewModel

class DialogUpdateImageFragment : BottomSheetDialogFragment() {

    private val userVM: UserViewModel by activityViewModels()

    private var _binding: FragmentDialogUpdateImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDialogUpdateImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewDismiss.setOnClickListener { dismissAllowingStateLoss() }
        binding.btnUpdateImage.setOnClickListener {
            getImageInLocal()
        }
    }

    private fun getImageInLocal() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        activityResultLauncher.launch(photoPickerIntent)
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.data?.let { uri ->
                    userVM.updateAvatarUser(uri)
                    dismissAllowingStateLoss()
                }
            }
        }
}