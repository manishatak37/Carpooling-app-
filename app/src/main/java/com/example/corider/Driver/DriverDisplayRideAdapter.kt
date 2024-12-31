package com.example.corider.Driver

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.corider.Fragment.RideInfo
import com.example.corider.R

class DriverDisplayRideAdapter(
    private val rideInfoList: ArrayList<RideInfo>
) : RecyclerView.Adapter<DriverDisplayRideAdapter.ViewHolder>() {

    // ViewHolder class to hold the view references
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fromTextView: TextView = itemView.findViewById(R.id.textFrom)
        val toTextView: TextView = itemView.findViewById(R.id.textTo)
        val dateTextView: TextView = itemView.findViewById(R.id.textDate)
        val timeTextView: TextView = itemView.findViewById(R.id.textTime)
        val priceTextView: TextView = itemView.findViewById(R.id.textPrice)
    }

    // Inflate the item layout and create the ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.driver_ride_item, parent, false)
        return ViewHolder(view)
    }

    // Bind data to the views
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ride = rideInfoList[position]
        holder.fromTextView.text = "From: ${ride.start_location}"
        holder.toTextView.text = "To: ${ride.end_location}"
        holder.dateTextView.text = "Date: ${ride.departure_date}"
        holder.timeTextView.text = "Time: ${ride.departure_time}"
        holder.priceTextView.text = "Price: ${ride.price_per_seat}"
    }

    // Return the total number of items
    override fun getItemCount(): Int {
        return rideInfoList.size
    }
}
