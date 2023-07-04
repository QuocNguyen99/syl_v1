package com.hqnguyen.syl.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.hqnguyen.syl.data.InfoDialog
import com.hqnguyen.syl.ui.dialog.DialogViewModel
import com.hqnguyen.syl.ui.dialog.NotifyDialog

typealias Inflate<T> = (LayoutInflater, ViewGroup?, Boolean) -> T

abstract class BaseFragment<VB : ViewBinding>(private val inflate: Inflate<VB>) : Fragment() {

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "login")

    val dialogVM: DialogViewModel by activityViewModels()

    private var _binding: VB? = null
    val binding get() = _binding!!
    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflate.invoke(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        onViewCreated()
        onObserver()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun navigation(@IdRes actionId: Int) {
        navController.navigate(actionId)
    }

    fun showDialog(info: InfoDialog) {
        dialogVM.setDataDialog(info)
        val dialog = NotifyDialog()
        dialog.show(childFragmentManager,"")
    }

    open fun onViewCreated() {}

    open fun onObserver() {}
}