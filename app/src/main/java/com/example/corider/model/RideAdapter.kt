


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView

import android.widget.Toast
import com.example.corider.model.RideInfo

import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.corider.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction

// Data classes
data class RideInfo(
    val ride_id: Int,
    val user_id: String,
    val start_location: String,
    val end_location: String,
    val car_model: String,
    val departure_date: String,
    val departure_time: String,
    val price_per_seat: Double
)

data class BookingDetails(
    val rideId: String? = "",
    val userId: String = "",
    val seatsToBook: Int = 0,
    val bookingTime: Long = 0L
)

class RideAdapter(
    private var rideList: List<RideInfo>,
    private val context: Context,
    private val userId: String
) : RecyclerView.Adapter<RideAdapter.RideViewHolder>() {

    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

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
        holder.rideTitle.text = "Driver: ${ride.user_id}"
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
            val seatsToBook = edtSeats.text.toString().toIntOrNull()

            if (seatsToBook == null || seatsToBook <= 0) {
                Toast.makeText(context, "Please enter a valid number of seats", Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Check for available seats before proceeding with booking
                loadingSpinner.visibility = View.VISIBLE
                val rideId = ride.ride_id.toString()

                Log.d("RideInfo",ride.toString())
                // Directly access the ride using ride_id as the key
                val rideRef = database.child("RideInfo").child(rideId)

                rideRef.get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        // Extract availableSeats directly from the ride
                        val availableSeats = snapshot.child("available_seats").getValue(Int::class.java)

                        if (availableSeats != null && availableSeats >= seatsToBook) {
                            // Proceed with booking
                            val bookingDetails = BookingDetails(
                                rideId = ride.ride_id,
                                userId = ride.user_id,
                                seatsToBook = seatsToBook,
                                bookingTime = System.currentTimeMillis()
                            )

                            // Use a transaction to safely update available seats
                            rideRef.child("available_seats").runTransaction(object : Transaction.Handler {
                                override fun doTransaction(currentData: MutableData): Transaction.Result {
                                    val currentSeats = currentData.getValue(Int::class.java) ?: return Transaction.abort()
                                    return if (currentSeats >= seatsToBook) {
                                        currentData.value = currentSeats - seatsToBook
                                        Transaction.success(currentData)
                                    } else {
                                        Transaction.abort()
                                    }
                                }

                                override fun onComplete(
                                    databaseError: DatabaseError?,
                                    committed: Boolean,
                                    currentData: DataSnapshot?
                                ) {
                                    if (committed) {
                                        // Store booking details
                                        database.child("bookings").push().setValue(bookingDetails)
                                            .addOnSuccessListener {
                                                loadingSpinner.visibility = View.GONE
                                                Toast.makeText(context, "Booking successful!", Toast.LENGTH_LONG).show()
                                                dialog.dismiss()
                                            }
                                            .addOnFailureListener {
                                                loadingSpinner.visibility = View.GONE
                                                Toast.makeText(context, "Booking failed. Please try again later.", Toast.LENGTH_LONG).show()
                                            }
                                    } else {
                                        loadingSpinner.visibility = View.GONE
                                        Toast.makeText(context, "Not enough seats available. Try reducing the number of seats.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            })
                        } else {
                            loadingSpinner.visibility = View.GONE
                            Toast.makeText(context, "Not enough seats available. Try reducing the number of seats.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        loadingSpinner.visibility = View.GONE
                        Toast.makeText(context, "Ride not found.", Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener {
                    loadingSpinner.visibility = View.GONE
                    Toast.makeText(context, "Failed to check seat availability. Please try again later.", Toast.LENGTH_LONG).show()
                }
            }
            // Reference to ride's available seats (could be added to your RideInfo data model)
//                val availableSeatsRef =
//                    database.child("RideInfo").orderByChild("ride_id").equalTo(ride.ride_id.toString())
//                availableSeatsRef.get().addOnSuccessListener { snapshot ->
//                    val availableSeats = snapshot.getValue(Int::class.java) ?: 0
//                    if (availableSeats >= seatsToBook) {
//                        // If enough seats are available, proceed withthe booking
//                        val bookingDetails = BookingDetails(
//                            rideId = ride.ride_id,
//                            userId = ride.user_id,
//                            seatsToBook = seatsToBook,
//                            bookingTime = System.currentTimeMillis()
//                        )
//
//                        // Update available seats
//                        availableSeatsRef.setValue(availableSeats - seatsToBook)
//
//                        // Store booking details
//                        database.child("bookings").push().setValue(bookingDetails)
//                            .addOnSuccessListener {
//                                loadingSpinner.visibility = View.GONE
//                                Toast.makeText(context, "Booking successful!", Toast.LENGTH_LONG)
//                                    .show()
//                                dialog.dismiss()
//                            }
//                            .addOnFailureListener {
//                                loadingSpinner.visibility = View.GONE
//                                Toast.makeText(
//                                    context,
//                                    "Booking failed. Please try again later.",
//                                    Toast.LENGTH_LONG
//                                ).show()
//                            }
//                    } else {
//                        loadingSpinner.visibility = View.GONE
//                        Toast.makeText(
//                            context,
//                            "Not enough seats available. Try reducing the number of seats.",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }.addOnFailureListener {
//                    loadingSpinner.visibility = View.GONE
//                    Toast.makeText(
//                        context,
//                        "Failed to check seat availability. Please try again later.",
//                        Toast.LENGTH_LONG
//                    ).show()
//                }

        }

        dialog.show()
    }
}