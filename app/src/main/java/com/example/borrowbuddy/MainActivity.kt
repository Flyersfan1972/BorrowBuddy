package com.example.borrowbuddy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
    }
}