package com.example.corider.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.corider.R
import com.example.corider.model.Notification

class NotificationAdapter(
    private val context: Context,
    private val notificationList: List<Notification>
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notificationList[position]
        holder.messageTextView.text = notification.message
        holder.dateTextView.text = notification.notificationDate
        holder.timeTextView.text = notification.notificationTime
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.messageText)
        val dateTextView: TextView = itemView.findViewById(R.id.notificationDate)
        val timeTextView: TextView = itemView.findViewById(R.id.notificationTime)
    }
}
