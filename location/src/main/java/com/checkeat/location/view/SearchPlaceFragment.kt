package com.checkeat.location.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.checkeat.location.R
import com.checkeat.location.model.Location

open class SearchPlaceFragment : Fragment() {

    private var onLocationRetrieved : (Location) -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_place, container, false)
    }

    companion object {

        fun newInstance(onLocationRetrieved : (Location) -> Unit) =
            SearchPlaceFragment().apply {
                this.onLocationRetrieved = onLocationRetrieved
            }
    }
}