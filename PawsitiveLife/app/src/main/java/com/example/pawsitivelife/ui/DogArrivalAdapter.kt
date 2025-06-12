package com.example.pawsitivelife.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pawsitivelife.R
import com.example.pawsitivelife.ui.mydogs.Dog
import java.text.SimpleDateFormat
import java.util.*

class DogArrivalAdapter(
    private val dogs: List<DogArrival>,
    private val onClick: (Dog) -> Unit
) : RecyclerView.Adapter<DogArrivalAdapter.DogViewHolder>() {

    inner class DogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgDog: ImageView = itemView.findViewById(R.id.item_IMG_dog)
        private val txtName: TextView = itemView.findViewById(R.id.item_TXT_name)
        private val txtTime: TextView = itemView.findViewById(R.id.item_TXT_time)

        fun bind(dogArrival: DogArrival) {
            val dog = dogArrival.dog

            Glide.with(itemView.context)
                .load(if (dog.imageUrl.isNotEmpty()) dog.imageUrl else R.drawable.missing_img_dog)
                .placeholder(R.drawable.missing_img_dog)
                .centerCrop()
                .into(imgDog)

            txtName.text = dog.name
            txtTime.text = "Arriving at ${formatTime(dogArrival.arrivalTime)}"

            itemView.setOnClickListener { onClick(dog) }
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dog_arrival, parent, false)
        return DogViewHolder(view)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        holder.bind(dogs[position])
    }

    override fun getItemCount(): Int = dogs.size
}
