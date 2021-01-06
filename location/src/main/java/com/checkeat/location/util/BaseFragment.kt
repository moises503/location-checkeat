package com.checkeat.location.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

abstract class BaseFragment<ScreenState> : Fragment() {
    abstract fun bindViews()
    abstract fun bindFragmentView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): View

    open fun renderScreenState(screenState: ScreenState) = Unit
    open fun attachObservers() = Unit
    open fun showError(message : String) = Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return bindFragmentView(inflater, container)
    }
}