package com.example.pawsitivelife.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pawsitivelife.R
import com.example.pawsitivelife.databinding.ItemDogBinding
import com.example.pawsitivelife.databinding.ItemAddDogBinding
import com.example.pawsitivelife.ui.mydogs.Dog

class DogAdapter(
    private val onDogClick: (Dog) -> Unit,
    private val onAddDogClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_DOG = 0
        private const val VIEW_TYPE_ADD = 1
    }

    private var dogs: List<Dog> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_DOG -> {
                val binding = ItemDogBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DogViewHolder(binding, onDogClick)
            }
            VIEW_TYPE_ADD -> {
                val binding = ItemAddDogBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AddDogViewHolder(binding, onAddDogClick)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DogViewHolder -> holder.bind(dogs[position])
            is AddDogViewHolder -> holder.bind()
        }
    }

    override fun getItemCount(): Int = dogs.size + 1 // +1 for Add Dog Card

    override fun getItemViewType(position: Int): Int {
        return if (position == dogs.size) VIEW_TYPE_ADD else VIEW_TYPE_DOG
    }

    fun submitList(newList: List<Dog>) {
        dogs = newList
        notifyDataSetChanged()
    }

    class DogViewHolder(
        private val binding: ItemDogBinding,
        private val onDogClick: (Dog) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dog: Dog) {
            binding.apply {
                dogName.text = dog.name
                dogAge.text = dog.dateOfBirth
                Glide.with(binding.root.context)
                    .load(if (dog.imageUrl.isNotEmpty()) dog.imageUrl else R.drawable.missing_img_dog)
                    .placeholder(R.drawable.missing_img_dog)
                    .into(dogImage)
                root.setOnClickListener { onDogClick(dog) }
            }
        }
    }

    class AddDogViewHolder(
        private val binding: ItemAddDogBinding,
        private val onAddDogClick: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.root.setOnClickListener { onAddDogClick() }
        }
    }
} 