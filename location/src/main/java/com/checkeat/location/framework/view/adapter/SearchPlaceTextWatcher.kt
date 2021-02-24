package com.checkeat.location.framework.view.adapter

import android.text.Editable
import android.text.TextWatcher

interface SearchPlaceTextWatcher : TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
    override fun afterTextChanged(s: Editable?) = Unit
}