package com.example.pawsitivelife.ui.mydogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawsitivelife.R
import com.example.pawsitivelife.model.WeightEntry

class WeightAdapter(
    private val entries: MutableList<WeightEntry>
) : RecyclerView.Adapter<WeightAdapter.WeightViewHolder>() {

    inner class WeightViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.item_weight_date)
        val weightText: TextView = view.findViewById(R.id.item_weight_value)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weight, parent, false)
        return WeightViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        val entry = entries[position]
        holder.dateText.text = entry.date
        holder.weightText.text = "${entry.weight} kg"
    }

    override fun getItemCount(): Int = entries.size

    fun addEntry(entry: WeightEntry) {
        entries.add(0, entry) // Add to top
        notifyItemInserted(0)
    }
}
