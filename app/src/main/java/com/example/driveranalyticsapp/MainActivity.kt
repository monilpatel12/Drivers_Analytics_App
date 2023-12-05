package com.example.driveranalyticsapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    // Data class for results
    data class ResultItem(
        val speed: Double,
        val acceleration: Double,
        val location: String,
        val score: Double,
        val rating: String
    )

    // Adapter for RecyclerView
    class ResultsAdapter(private val resultsList: List<ResultItem>) :
        RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val speedTextView: TextView = view.findViewById(R.id.inputField1)
            val accelerationTextView: TextView = view.findViewById(R.id.inputField2)
            val locationTextView: TextView = view.findViewById(R.id.inputField3)
            val scoreTextView: TextView = view.findViewById(R.id.outputField)
            val ratingTextView: TextView = view.findViewById(R.id.outputField)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.result_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = resultsList[position]
            holder.speedTextView.text = "Speed: ${item.speed}"
            holder.accelerationTextView.text = "Acceleration: ${item.acceleration}"
            holder.locationTextView.text = "Location: ${item.location}"
            holder.scoreTextView.text = "Score: ${item.score}"
            holder.ratingTextView.text = "Rating: ${item.rating}"
        }

        override fun getItemCount() = resultsList.size
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Auth
        val firebaseAuth = FirebaseAuth.getInstance()

        // Initialize the EditTexts and Button
        val speedInput = findViewById<EditText>(R.id.inputField1)
        val accelerationInput = findViewById<EditText>(R.id.inputField2)
        val locationInput = findViewById<EditText>(R.id.inputField3)
        val generateButton = findViewById<Button>(R.id.generateButton)
        val outputField = findViewById<TextView>(R.id.outputField)

        // Initialize RecyclerView and Adapter
        val recyclerView = findViewById<RecyclerView>(R.id.resultsRecyclerView)
        val resultsList = mutableListOf<ResultItem>() // Replace with actual data fetching logic
        val resultsAdapter = ResultsAdapter(resultsList)
        recyclerView.adapter = resultsAdapter

        // Setup the generate button click listener
        generateButton.setOnClickListener {
            val speed = speedInput.text.toString().toDoubleOrNull() ?: 0.0
            val acceleration = accelerationInput.text.toString().toDoubleOrNull() ?: 0.0
            val location = locationInput.text.toString()

            val score = calculateScore(speed, acceleration)
            val rating = categorizeRating(score)

            outputField.text = "Rating: $rating"

            // Store data in Firebase and update the RecyclerView
            storeDataInFirebase(speed, acceleration, location, score, rating)
            val resultItem = ResultItem(speed, acceleration, location, score, rating)
            resultsList.add(resultItem)
            resultsAdapter.notifyItemInserted(resultsList.size - 1)
        }

        // Setup the logout button
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            firebaseAuth.signOut() // Perform logout
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            finish() // Close the current activity
        }
    }

    private fun calculateScore(speed: Double, acceleration: Double): Double {
        // Implement your scoring logic here
        // Example: lower speed and acceleration lead to a higher score
        return 100 - (speed + acceleration) // Adjust this formula as needed
    }

    private fun categorizeRating(score: Double): String {
        // Categorize score into ratings
        return when {
            score > 80 -> "Good \uD83D\uDE00"
            score > 60 -> "Average \uD83D\uDE10"
            else -> "Bad \uD83D\uDE41"
        }
    }

    private fun storeDataInFirebase(speed: Double, acceleration: Double, location: String, score: Double, rating: String) {
        // Get a reference to the Firebase Database
        val databaseReference = FirebaseDatabase.getInstance().reference

        // Create a map of data to be stored
        val userData = hashMapOf(
            "speed" to speed,
            "acceleration" to acceleration,
            "location" to location,
            "score" to score,
            "rating" to rating
        )

        // Store the data under "userData" node in Firebase Database
        databaseReference.child("userData").push().setValue(userData)
    }
}
