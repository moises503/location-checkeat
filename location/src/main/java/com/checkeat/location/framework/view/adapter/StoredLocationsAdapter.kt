package com.checkeat.location.framework.view.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.checkeat.location.R
import com.checkeat.location.databinding.PlaceFoundItemBinding
import com.checkeat.location.lib.model.Location
import com.checkeat.location.util.inflate

internal class StoredLocationsAdapter(
    private var locations: MutableList<Location> = mutableListOf(),
    private val locationSelected: (Location) -> Unit
) : RecyclerView.Adapter<StoredLocationsAdapter.StoredLocationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoredLocationViewHolder =
        StoredLocationViewHolder(parent.inflate(R.layout.place_found_item))

    override fun getItemCount(): Int = locations.size

    override fun onBindViewHolder(holder: StoredLocationViewHolder, position: Int) {
        holder.bind(locations[position], locationSelected)
    }

    fun updateDataSet(locations : MutableList<Location>) {
        this.locations = locations
        notifyDataSetChanged()
    }

    internal class StoredLocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val placeFoundItemBinding = PlaceFoundItemBinding.bind(view)
        fun bind(location: Location, locationSelected: (Location) -> Unit) = with(placeFoundItemBinding) {
            root.setOnClickListener {
                locationSelected(location)
            }
            txtPlaceFound.text = location.address
        }
    }
}