package com.example.corider.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.corider.R
import com.example.corider.model.Ride

class RideAdapter(private var rideList: List<Ride>) : RecyclerView.Adapter<RideAdapter.RideViewHolder>() {

    class RideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rideTitle: TextView = itemView.findViewById(R.id.rideTitle)
        val rideDetails: TextView = itemView.findViewById(R.id.rideDetails)
        val carModel: TextView = itemView.findViewById(R.id.carModel)
        val departureDate: TextView = itemView.findViewById(R.id.departureDate)
        val departureTime: TextView = itemView.findViewById(R.id.departureTime)
        val pricePerSeat: TextView = itemView.findViewById(R.id.pricePerSeat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_serchride_result, parent, false)
        return RideViewHolder(view)
    }

    // Function to update the data in the adapter
    fun updateData(newRideList: List<Ride>) {
        rideList = newRideList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        val ride = rideList[position]
        holder.rideTitle.text = "Driver: ${ride.driverName}"
        holder.rideDetails.text = "Pickup: ${ride.pickupLocation}, Drop-off: ${ride.dropoffLocation}"
        holder.carModel.text = "Car Model: ${ride.carModel}"
        holder.departureDate.text = "Departure Date: ${ride.departureDate}"
        holder.departureTime.text = "Departure Time: ${ride.departureTime}"
        holder.pricePerSeat.text = "Price per Seat: $${ride.pricePerSeat}"
    }

    override fun getItemCount(): Int = rideList.size
}
