package com.hqnguyen.syl.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.hqnguyen.syl.databinding.DialogNotiBinding

class NotifyDialog : BottomSheetDialogFragment() {

    private val dialogVM: DialogViewModel by activityViewModels()

    private var _binding: DialogNotiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DialogNotiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEvent()
        onObserverLiveData()
    }

    private fun initEvent() {
        binding.btnOk.setOnClickListener {
            dismiss()
        }
        binding.viewDismiss.setOnClickListener {
            dismiss()
        }
    }

    private fun onObserverLiveData() {
        dialogVM.infoDialog.observe(viewLifecycleOwner) {
            it?.let {
                binding.tvTitle.text = it.title
                binding.tvMessage.text = it.message
                binding.logoDialog.setImageResource(it.image)
            }
        }
    }
}

//
//abstract class BaseDialogFragment<VB : ViewBinding> : DialogFragment(),
//    FragmentView {
//
//    protected val log: Logger by lazy { Logger(this::class) }
//
//    protected val vb: VB by lazyViewBinding()
//
//    var dismissWhenTouchOutSize: Boolean = true
//
//
//    /**
//     * [DialogFragment] implements
//     */
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        requireActivity().onBackPressedDispatcher.addCallback(this, backPressedCallback)
//    }
//
//    fun string(@StringRes res: Int): String {
//        return requireContext().getString(res)
//    }
//
//    override fun getTheme(): Int {
//        return R.style.App_Dialog_FullScreen_Floating
//    }
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = object : Dialog(requireActivity(), theme) {
//            override fun onBackPressed() {
//                this@BaseDialogFragment.onBackPressed()
//            }
//        }
//        dialog.window?.also {
//
//            onWindowConfig(it)
//        }
//        dialog.setOnDismissListener {
//            println("onDismiss")
//        }
//        return dialog
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = vb.root
//        onCreateView()
//        return view
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        log.d("${this::class.java.simpleName} onViewCreated")
//        view.setOnClickListener {
//            if (dismissWhenTouchOutSize) {
//                dismiss()
//            }
//        }
//        lightStatusBarWidgets()
//        onViewCreated()
//        onLiveDataObserve()
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        log.d("${this::class.java.simpleName} onDestroyView")
//    }
//
//    override fun onStart() {
//        super.onStart()
//        when (theme) {
//            R.style.App_Dialog_FullScreen,
//            R.style.App_Dialog_FullScreen_Floating,
//            R.style.App_Dialog_FullScreen_Transparent,
//            -> dialog?.window?.apply {
//                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//            }
//        }
//    }
//
//    override fun onPause() {
//        super.onPause()
//        requireActivity().overridePendingTransition(0, 0)
//        dialog?.window?.attributes?.windowAnimations = R.style.App_DialogNoAnimation
//    }
//
//    override fun onDismiss(dialog: DialogInterface) {
//        hideKeyboard()
//        super.onDismiss(dialog)
//    }
//
//    /**
//     * [FragmentView] implements
//     */
//    final override var animatedDialogTag: String? = null
//
//    final override val jobs = mutableMapOf<String, Job>()
//
//    override val backPressedCallback: OnBackPressedCallback by lazy { getBackPressCallBack() }
//
//    override fun onBackPressed() {
//        backPressedCallback.remove()
//        dismissAllowingStateLoss()
//    }
//
//    /**
//     * [BaseDialogFragment] properties
//     */
//    protected open fun onWindowConfig(window: Window) {
//        window.decorView.setBackgroundColor(Color.TRANSPARENT)
//        window.attributes.windowAnimations = R.style.App_DialogAnim
//        window.lightStatusBarWidgets()
//        window.statusBarColor(color(R.color.colorDialogBackground))
//    }
//
//}
