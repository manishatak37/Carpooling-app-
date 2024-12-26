import android.content.Context
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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

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
    val rideId: Int = 0,
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_serchride_result, parent, false)
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

        builder.setView(dialogView)
        val dialog = builder.create()

        btnConfirmBooking.setOnClickListener {
            val seatsToBook = edtSeats.text.toString().toIntOrNull()

            if (seatsToBook == null || seatsToBook <= 0) {
                Toast.makeText(context, "Please enter a valid number of seats", Toast.LENGTH_SHORT).show()
            } else {
                loadingSpinner.visibility = View.VISIBLE

                val bookingDetails = BookingDetails(
                    rideId = ride.ride_id,
                    userId = userId,
                    seatsToBook = seatsToBook,
                    bookingTime = System.currentTimeMillis()
                )

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
            }
        }

        dialog.show()
    }
}
