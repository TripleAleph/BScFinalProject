package com.example.pawsitivelife.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.ItemDogCardBinding
import com.example.pawsitivelife.ui.mydogs.Dog

class DogCardAdapter(
    private val dogs: List<Dog>,
    private val onDogClick: (Dog) -> Unit,
    private val onAddDogClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_ADD = 0
        private const val VIEW_TYPE_DOG = 1
    }

    override fun getItemCount(): Int = dogs.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_ADD else VIEW_TYPE_DOG
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ADD) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add_dog_card, parent, false)
            AddDogViewHolder(view)
        } else {
            val binding = ItemDogCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            DogViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddDogViewHolder) {
            holder.itemView.setOnClickListener {
                onAddDogClick()
            }
        } else if (holder is DogViewHolder) {
            // Subtract 1 because the first item is the add card
            holder.bind(dogs[position - 1])
        }
    }

    inner class DogViewHolder(private val binding: ItemDogCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(dog: Dog) {
            binding.dogName.text = dog.name
            binding.dogAge.text = dog.dateOfBirth

            Glide.with(binding.root.context)
                .load(dog.imageUrl.takeIf { it.isNotEmpty() })
                .placeholder(R.drawable.missing_img_dog)
                .centerCrop()
                .into(binding.dogImage)


            binding.genderIcon.setImageResource(
                if (dog.gender == "Male") R.drawable.ic_male else R.drawable.ic_female
            )

            binding.root.setOnClickListener {
                onDogClick(dog)
            }
        }
    }

    inner class AddDogViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
