package com.example.corider.User

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import com.example.corider.Payment.RazorpayPayment
import android.widget.Toast
import com.example.corider.User.RideInfo

import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.corider.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import java.io.Serializable

// Data classes
data class RideInfo(
    var ride_id: String?= "",
    var driver_id: String ="",
    var start_latitude: Double = 0.0,
    var start_longitude: Double = 0.0,
    var end_latitude: Double = 0.0,
    var end_longitude: Double = 0.0,
    var departure_date: String = "",
    var departure_time: String = "",
    var available_seats: Int = 0,
    var price_per_seat: Double = 0.0,
    var ride_status: String = "",
    var start_location: String = "",
    var end_location: String = "",
    var car_model: String = ""
) : Serializable

data class BookingDetails(
    val rideId: String? = "",
    val driverId: String = "",
    val userId: String = "",
    val seatsToBook: Int = 0,
    val bookingTime: Long = 0L
)

public class RideAdapter(
    private var rideList: List<RideInfo>,
    private val context: Context,

) : RecyclerView.Adapter<RideAdapter.RideViewHolder>() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    // Custom click listener
    private var onItemClickListener: ((RideInfo, Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (RideInfo, Int) -> Unit) {
        onItemClickListener = listener
    }

    class RideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rideTitle: TextView = itemView.findViewById(R.id.rideTitle)
        val rideDetails: TextView = itemView.findViewById(R.id.rideDetails)
        val rideDetailDrop: TextView = itemView.findViewById(R.id.rideDetailDrop)
        val carModel: TextView = itemView.findViewById(R.id.carModel)
        val departureDate: TextView = itemView.findViewById(R.id.departureDate)
        val departureTime: TextView = itemView.findViewById(R.id.departureTime)
        val pricePerSeat: TextView = itemView.findViewById(R.id.pricePerSeat)
        val bookRideButton: Button = itemView.findViewById(R.id.bookRideButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_serchride_result, parent, false)
        return RideViewHolder(view)
    }

    fun updateData(newRideList: List<RideInfo>) {
        rideList = newRideList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        val ride = rideList[position]
        holder.rideTitle.text = "Driver: ${ride.driver_id}"
        holder.rideDetails.text = "Pickup: ${ride.start_location}"
        holder.rideDetailDrop.text = "Drop-off: ${ride.end_location}"
        holder.carModel.text = "Car Model: ${ride.car_model}"
        holder.departureDate.text = "Departure Date: ${ride.departure_date}"
        holder.departureTime.text = "Departure Time: ${ride.departure_time}"
        holder.pricePerSeat.text = "Price per Seat: $${ride.price_per_seat}"

        holder.bookRideButton.setOnClickListener {
            showBookingDialog(ride)


        }
    }

    override fun getItemCount(): Int = rideList.size

    private fun getUserIdFromPrefs(): String {
        val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userID", "") ?: ""
    }

    private fun storeRideDetails(pricePerSeat: Double, seatsToBook: Int) {
        // Calculate the total price
        val sharedPreferencesbooking = context.getSharedPreferences("BookRideDetails", Context.MODE_PRIVATE)
        val totalPrice = pricePerSeat * seatsToBook

        // Store the details in SharedPreferences
        val editor = sharedPreferencesbooking.edit()
        editor.putInt("seatsBooked", seatsToBook)
        editor.putFloat("totalPrice", totalPrice.toFloat())
        editor.apply()

        // Log for debugging
        Log.d("RideDetails", " Seats: $seatsToBook, Total Price: $totalPrice")
    }

    private fun showBookingDialog(ride: RideInfo) {
        val builder = AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_book_ride, null)

        val edtSeats = dialogView.findViewById<EditText>(R.id.edtSeats)
        val btnConfirmBooking = dialogView.findViewById<Button>(R.id.btnConfirmBooking)
        val loadingSpinner = dialogView.findViewById<ProgressBar>(R.id.loadingSpinner)
        val tvTotalPrice = dialogView.findViewById<TextView>(R.id.tvTotalPrice)

        builder.setView(dialogView)
        val dialog = builder.create()

        // Set an OnFocusChangeListener to update the total price when seats are changed
        edtSeats.setOnFocusChangeListener { _, _ ->
            val seatsToBook = edtSeats.text.toString().toIntOrNull()
            if (seatsToBook != null && seatsToBook > 0) {
                val totalPrice = ride.price_per_seat * seatsToBook
                tvTotalPrice.text = "Total Price: $${totalPrice}"
            }
        }

        btnConfirmBooking.setOnClickListener {

            // Retrieve seat count entered by user
            val seatsToBook = edtSeats.text.toString().toIntOrNull()


            // Validate the seat input
            if (seatsToBook == null || seatsToBook <= 0) {
                Toast.makeText(context, "Please enter a valid number of seats", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            storeRideDetails(ride.price_per_seat, seatsToBook)


            // Show loading spinner while processing the booking
            loadingSpinner.visibility = View.VISIBLE

            val rideId = ride.ride_id.toString()

            val userId = getUserIdFromPrefs()

            if (userId.isEmpty()) {
                Toast.makeText(context, "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show()
            }
            Log.d("USerid", userId)

            Log.d("RideInfo", ride.toString())
            // Directly access the ride using ride_id as the key in the database
            val rideRef = database.child("RideInfo").child(rideId)


            rideRef.get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Extract availableSeats directly from the ride
                    val availableSeats = snapshot.child("available_seats").getValue(Int::class.java)

                    if (availableSeats != null && availableSeats >= seatsToBook) {
                        // Proceed with booking if there are enough seats
                        val bookingDetails = BookingDetails(
                            rideId = ride.ride_id,
                            driverId = ride.driver_id,
                            userId = userId,
                            seatsToBook = seatsToBook,
                            bookingTime = System.currentTimeMillis()
                        )

                        // Perform a Firebase transaction to safely update available seats
                        rideRef.child("available_seats")
                            .runTransaction(object : Transaction.Handler {
                                override fun doTransaction(currentData: MutableData): Transaction.Result {
                                    val currentSeats = currentData.getValue(Int::class.java) ?: return Transaction.success(currentData)
                                    return if (currentSeats >= seatsToBook) {
                                        currentData.value = currentSeats - seatsToBook
                                        Transaction.success(currentData)
                                    } else {
                                        // Abort transaction if not enough seats are available
                                        return Transaction.abort()
                                    }
                                }

                                override fun onComplete(
                                    databaseError: DatabaseError?,
                                    committed: Boolean,
                                    currentData: DataSnapshot?
                                ) {
                                    loadingSpinner.visibility = View.GONE
                                    if (committed) {
                                        // Transaction committed: Store booking details in Firebase
                                        database.child("bookings").push()
                                            .setValue(bookingDetails)
                                            .addOnSuccessListener {
                                                // Successfully booked the seats

                                                val intent = Intent(context, RazorpayPayment::class.java)
                                                context.startActivity(intent)
                                                dialog.dismiss()
                                            }
                                            .addOnFailureListener {
                                                // Failed to store booking details
                                                Toast.makeText(context, "Booking failed. Please try again later.", Toast.LENGTH_LONG).show()
                                            }
                                    } else {
                                        // Transaction failed due to insufficient seats
                                        Toast.makeText(context, "Not enough seats available. Try reducing the number of seats.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            })
                    } else {
                        // Not enough seats available
                        loadingSpinner.visibility = View.GONE
                        Toast.makeText(context, "Not enough seats available. Try again.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Ride not found in the database
                    loadingSpinner.visibility = View.GONE
                    Toast.makeText(context, "Ride not found.", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener {
                // Failed to check seat availability from Firebase
                loadingSpinner.visibility = View.GONE
                Toast.makeText(context, "Failed to check seat availability. Please try again later.", Toast.LENGTH_LONG).show()
            }
        }

// Show the dialog to proceed with booking
        //dialog.show()



        dialog.show()
    }
}