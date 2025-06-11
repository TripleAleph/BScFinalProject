package com.example.pawsitivelife.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pawsitivelife.R
import com.example.pawsitivelife.model.Vet

class VetAdapter(
    private val vets: List<Vet>,
    private val onClick: (Vet) -> Unit
) : RecyclerView.Adapter<VetAdapter.VetViewHolder>() {

    inner class VetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.vetName)
        val address: TextView = view.findViewById(R.id.vetAddress)
        val phone: TextView = view.findViewById(R.id.vetPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vet, parent, false)
        return VetViewHolder(view)
    }

    override fun onBindViewHolder(holder: VetViewHolder, position: Int) {
        val vet = vets[position]
        holder.name.text = vet.name
        holder.address.text = vet.address
        holder.phone.text = vet.phone

        holder.itemView.setOnClickListener { onClick(vet) }
    }

    override fun getItemCount(): Int = vets.size
}
