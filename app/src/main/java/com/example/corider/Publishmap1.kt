package com.example.corider

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.util.ArrayList

class Publishmap1 : AppCompatActivity() {

    private val ACCESS_TOKEN = "pk.f562cc1cfae004dc438fbe50ed3cdcbb" // Replace with your LocationIQ access token
    private lateinit var webView: WebView
    private lateinit var client: OkHttpClient
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var suggestionListView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private var suggestions: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publishmap1)

        // Initialize WebView
        webView = findViewById(R.id.webView)
        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        loadLocationIQMap()

        client = OkHttpClient()
        autoCompleteTextView = findViewById(R.id.autocompleteTextView)
        suggestionListView = findViewById(R.id.suggestionListView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, suggestions)
        suggestionListView.adapter = adapter

        autoCompleteTextView.setOnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long ->
            val selectedSuggestion = adapter.getItem(position)
            autoCompleteTextView.setText(selectedSuggestion)
            suggestionListView.visibility = View.GONE
        }

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    getAutocompleteSuggestions(query)
                } else {
                    suggestions.clear()
                    adapter.notifyDataSetChanged()
                    suggestionListView.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadLocationIQMap() {
        // Updated URL for LocationIQ static map
        val mapUrl = "https://maps.locationiq.com/v3/staticmap?key=$ACCESS_TOKEN&center=0,0&zoom=2&size=600x400"
        webView.loadUrl(mapUrl)
    }

    private fun getAutocompleteSuggestions(query: String) {
        // Corrected URL for LocationIQ autocomplete
        val url = "https://api.locationiq.com/v1/autocomplete.php?key=$ACCESS_TOKEN&q=$query&limit=5"

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                if (response.isSuccessful && responseData != null) {
                    parseAutocompleteResponse(responseData)
                } else {
                    runOnUiThread {
                        // Display a message or handle UI in case of failure
                    }
                }
            }
        })
    }

    private fun parseAutocompleteResponse(responseData: String) {
        try {
            val jsonArray = JSONArray(responseData)
            suggestions.clear()
            for (i in 0 until jsonArray.length()) {
                val place = jsonArray.getJSONObject(i)
                val displayName = place.getString("display_name")
                suggestions.add(displayName)
            }
            runOnUiThread {
                adapter.notifyDataSetChanged()
                suggestionListView.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
