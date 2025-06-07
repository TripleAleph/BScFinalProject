package com.example.pawsitivelife.adapter

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.pawsitivelife.R
import com.example.pawsitivelife.model.Reminder
import java.io.File
import java.time.format.DateTimeFormatter

class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val title: TextView = itemView.findViewById(R.id.reminder_title)
    private val time: TextView = itemView.findViewById(R.id.reminder_time)
    private val image: ImageView = itemView.findViewById(R.id.reminder_image)

    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    fun bind(reminder: Reminder) {
        title.text = reminder.title
        time.text = reminder.date.format(formatter)

        val imagePath = reminder.imagePath
        val file = if (!imagePath.isNullOrBlank()) File(imagePath) else null

        if (file != null && file.exists()) {
            Glide.with(itemView.context)
                .load(Uri.fromFile(file))
                .into(image)
        } else {
            image.setImageResource(R.drawable.missing_img_dog)
        }
    }


}
