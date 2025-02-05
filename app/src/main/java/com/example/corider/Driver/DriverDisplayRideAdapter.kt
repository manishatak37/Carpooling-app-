package com.example.corider.Driver

import com.example.corider.Driver.Successful_Ride_completed
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.corider.Admin.Admin_home_page
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
        val redirectButton: Button = itemView.findViewById(R.id.redirectButton)
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
        if (ride.category == "scheduled") {
            holder.redirectButton.visibility = View.VISIBLE
            holder.redirectButton.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, Successful_Ride_completed::class.java)
                intent.putExtra("driver_id", ride.driver_id)
                intent.putExtra("ride_id",ride.ride_id)
                intent.putExtra("destination", ride.end_location)
                intent.putExtra("rideDate", ride.departure_date) // Ensure ride date is passed
                context.startActivity(intent)
            }
        } else {
            holder.redirectButton.visibility = View.INVISIBLE
        }
    }

    // Return the total number of items
    override fun getItemCount(): Int {
        return rideInfoList.size
    }
}
