package com.example.pawsitivelife.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawsitivelife.R
import com.example.pawsitivelife.ui.mydogs.Dog

class DogInParkAdapter(
    private val dogs: List<Dog>,
    private val onRemoveClick: (Dog) -> Unit
) : RecyclerView.Adapter<DogInParkAdapter.DogViewHolder>() {

    inner class DogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dogIcon: ImageView = itemView.findViewById(R.id.dogIcon)
        val dogName: TextView = itemView.findViewById(R.id.dogName)
        val removeIcon: ImageView = itemView.findViewById(R.id.removeIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dog_in_park, parent, false)
        return DogViewHolder(view)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        val dog = dogs[position]

        holder.dogName.text = dog.name

        if (dog.isMine) {
            holder.removeIcon.visibility = View.VISIBLE
            holder.removeIcon.setOnClickListener {
                onRemoveClick(dog)
            }
        } else {
            holder.removeIcon.visibility = View.GONE
        }

        if (!dog.isMine) {
            holder.dogIcon.setColorFilter(android.graphics.Color.GRAY)
        } else {
            holder.dogIcon.clearColorFilter()
        }
    }

    override fun getItemCount(): Int = dogs.size
}
