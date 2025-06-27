package com.example.borrowbuddy

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import android.view.animation.AlphaAnimation
import android.os.Handler
import android.os.Looper
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.borrowbuddy.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize DataController
        DataController.initialize(this)

        // Set up button click listeners
        binding.btnLoan.setOnClickListener {
            val intent = Intent(this, LoanActivity::class.java)
            startActivity(intent)
        }

        binding.btnBorrow.setOnClickListener {
            Snackbar.make(it, "Borrow button clicked", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnWhere.setOnClickListener {
            val intent = Intent(this, WhereActivity::class.java)
            startActivity(intent)
        }

        binding.btnReturn.setOnClickListener {
            Snackbar.make(it, "Return something clicked", Snackbar.LENGTH_SHORT).show()
        }

        // Check for loan notification
        intent.extras?.let { extras ->
            val itemName = extras.getString("ITEM_NAME")
            val borrowerName = extras.getString("BORROWER_NAME")
            if (itemName != null && borrowerName != null) {
                showFadingNotification("$itemName loaned to $borrowerName")
            }
        }
    }

    private fun showFadingNotification(message: String) {
        // Create a custom TextView for the notification
        val textView = TextView(this).apply {
            text = message
            textSize = 32f
            setPadding(32, 16, 32, 16)
            setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.white))
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, android.R.color.black))
            gravity = Gravity.CENTER
        }

        // Create a Dialog with transparent background
        val dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar).apply {
            setContentView(textView)
            window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window?.setGravity(Gravity.CENTER)
        }

        // Apply fade-out animation
        val fadeOut = AlphaAnimation(1.0f, 0.0f).apply {
            duration = 3000 // 3 seconds
            fillAfter = true
        }
        textView.startAnimation(fadeOut)

        // Show the Dialog
        dialog.show()

        // Dismiss the Dialog after 2 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 3000)
    }
}