package com.example.pawsitivelife.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pawsitivelife.R
import com.example.pawsitivelife.model.Reminder


class ReminderAdapter(
    private val onReminderClick: ((Reminder) -> Unit)? = null
) : RecyclerView.Adapter<ReminderViewHolder>() {
    private var reminders: List<Reminder> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view, onReminderClick)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.bind(reminder)
    }

    override fun getItemCount(): Int = reminders.size

    fun submitList(newList: List<Reminder>) {
        reminders = newList
        notifyDataSetChanged()
    }

    fun getReminderAt(position: Int): Reminder {
        return reminders[position]
    }


    fun removeAt(position: Int) {
        val mutableList = reminders.toMutableList()
        if (position in reminders.indices) {
            mutableList.removeAt(position)
            submitList(mutableList)
        }
    }

    fun insertAt(position: Int, reminder: Reminder) {
        val mutableList = reminders.toMutableList()
        mutableList.add(position.coerceIn(0, mutableList.size), reminder)
        submitList(mutableList)
    }






}