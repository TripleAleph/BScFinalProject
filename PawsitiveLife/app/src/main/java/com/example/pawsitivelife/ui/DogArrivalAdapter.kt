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

class DogArrivalAdapter(
    private val dogs: List<DogArrival>,
    private val onClick: (Dog) -> Unit
) : RecyclerView.Adapter<DogArrivalAdapter.DogViewHolder>() {

    inner class DogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgDog: ImageView = itemView.findViewById(R.id.item_IMG_dog)
        val txtName: TextView = itemView.findViewById(R.id.item_TXT_name)
        val txtTime: TextView = itemView.findViewById(R.id.item_TXT_time)

        fun bind(dogArrival: DogArrival) {
            val dog = dogArrival.dog
            Glide.with(itemView.context)
                .load(dog.imageUrl)
                .placeholder(R.drawable.img_chubbie)
                .into(imgDog)

            txtName.text = dog.name
            txtTime.text = "Arriving at ${dogArrival.arrivalTime}"
            itemView.setOnClickListener { onClick(dog) }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dog_arrival, parent, false)
        return DogViewHolder(view)
    }

    override fun onBindViewHolder(holder: DogViewHolder, position: Int) {
        holder.bind(dogs[position])
    }

    override fun getItemCount(): Int = dogs.size
}
