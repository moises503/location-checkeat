package com.checkeat.location.framework.view.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.checkeat.location.R
import com.checkeat.location.databinding.PlaceFoundItemBinding
import com.checkeat.location.util.gone
import com.checkeat.location.util.inflate
import com.google.android.libraries.places.api.model.AutocompletePrediction

internal class PlacesFoundAdapter(
    private var predictions: List<AutocompletePrediction> = emptyList(),
    private val onPredictionClicked: (AutocompletePrediction) -> Unit
) : RecyclerView.Adapter<PlacesFoundAdapter.SearchPlaceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPlaceViewHolder {
        return SearchPlaceViewHolder(parent.inflate(R.layout.place_found_item))
    }

    override fun onBindViewHolder(holder: SearchPlaceViewHolder, position: Int) {
        holder.bind(predictions[position], onPredictionClicked)
    }

    override fun getItemCount(): Int = predictions.size

    fun updateDataSet(predictions: List<AutocompletePrediction>) {
        this.predictions = predictions
        notifyDataSetChanged()
    }

    internal class SearchPlaceViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val placeFoundItemBinding = PlaceFoundItemBinding.bind(itemView)
        fun bind(
            prediction: AutocompletePrediction,
            onPredictionClicked: (AutocompletePrediction) -> Unit
        ) = with(placeFoundItemBinding) {
            root.setOnClickListener {
                onPredictionClicked(prediction)
            }
            imgFavorite.gone()
            txtPlaceFound.text = String.format(
                txtPlaceFound.context.getString(R.string.place_found),
                prediction.getPrimaryText(null), prediction.getSecondaryText(null)
            )
        }
    }

}