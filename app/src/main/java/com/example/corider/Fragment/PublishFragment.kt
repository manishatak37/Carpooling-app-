package com.example.corider.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.corider.R

class PublishFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_publish, container, false)

        // Find the button and set an onClickListener
        val button = view.findViewById<Button>(R.id.button_publish)
        button.setOnClickListener {
            // Start PublishActivity when button is clicked
            val intent = Intent(activity, com.example.corider.PublishActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
