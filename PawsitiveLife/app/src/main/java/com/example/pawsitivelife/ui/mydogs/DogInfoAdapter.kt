package com.example.pawsitivelife.ui.mydogs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pawsitivelife.ui.mydogs.DogInfoAdapter.DogInfoViewHolder
import com.example.pawsitivelife.databinding.ItemDogInfoBinding
data class DogInfoItem(
    val title: String,
    val value: String
)

class DogInfoAdapter(private val items: List<DogInfoItem>) : RecyclerView.Adapter<DogInfoViewHolder>() {

    inner class DogInfoViewHolder(private val binding: ItemDogInfoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DogInfoItem) {
            binding.itemLBLTitle.text = item.title
            binding.itemLBLValue.text = item.value
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogInfoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemDogInfoBinding.inflate(inflater, parent, false)
        return DogInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DogInfoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
