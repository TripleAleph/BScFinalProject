package com.example.pawsitivelife.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawsitivelife.R
import com.example.pawsitivelife.NearbyPlace


class NearbyPlaceAdapter(private val places: List<NearbyPlace>) :
    RecyclerView.Adapter<NearbyPlaceAdapter.PlaceViewHolder>() {

    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.item_IMG_icon)
        val name: TextView = itemView.findViewById(R.id.item_TXT_name)
        val address: TextView = itemView.findViewById(R.id.item_TXT_address)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_nearby_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.name.text = place.name
        holder.address.text = place.address
        holder.icon.setImageResource(place.iconResId)
    }

    override fun getItemCount(): Int = places.size
}
