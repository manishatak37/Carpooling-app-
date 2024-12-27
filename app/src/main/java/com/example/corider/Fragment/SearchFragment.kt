package com.example.corider.Fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.corider.R
import com.example.corider.model.SearchRideActivity

class SearchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Initialize the button
        val button = view.findViewById<Button>(R.id.searchButton)
        button.setOnClickListener {
            // Navigate to PublishActivity
            val intent = Intent(activity, SearchRideActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
