package com.example.borrowbuddy

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.borrowbuddy.databinding.ActivityWhereBinding
import com.google.android.material.snackbar.Snackbar
import android.net.Uri
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class WhereActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWhereBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWhereBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loanedItems = DataController.getLoans().filter { it.disposition == 1 }.sortedBy { it.dateLoaned }
        if (loanedItems.isEmpty()) {
            Snackbar.make(binding.root, "No items currently loaned out", Snackbar.LENGTH_LONG).show()
            binding.btnClose.requestFocus() // Focus on close button for accessibility
        }

        binding.rvLoanedItems.apply {
            layoutManager = LinearLayoutManager(this@WhereActivity)
            adapter = LoanedItemsAdapter(loanedItems)
        }

        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private inner class LoanedItemsAdapter(private val loanedItems: List<Loan>) :
        RecyclerView.Adapter<LoanedItemsAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvItemName: TextView = itemView.findViewById(R.id.tvItemName)
            val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)
            val tvContactName: TextView = itemView.findViewById(R.id.tvContactName)
            val tvLoanDetails: TextView = itemView.findViewById(R.id.tvLoanDetails)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_loan, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val loan = loanedItems[position]
            val item = DataController.getItems().find { it.id == loan.itemId }
            val contact = DataController.getContacts().find { it.id == loan.contactId }

            if (item != null && contact != null) {
                Log.d("WhereActivity", "Loan ID: ${loan.id}, Item: ${item.name}, Contact: ${contact.name}, DateLoaned: ${loan.dateLoaned}, ReturnDate: ${loan.returnDate}")

                holder.tvItemName.text = item.name
                item.photoUri?.let { uri -> holder.ivPhoto.setImageURI(Uri.parse(uri)) }
                holder.tvContactName.text = contact.name

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val displayFormatter = DateTimeFormatter.ofPattern("MM-dd-yy")
                val daysLoaned = loan.returnDate?.let { returnDate ->
                    try {
                        val loanDate = LocalDate.parse(loan.dateLoaned, formatter)
                        val returnDateParsed = LocalDate.parse(returnDate, formatter)
                        val days = ChronoUnit.DAYS.between(loanDate, returnDateParsed).toInt()
                        Log.d("WhereActivity", "Loan ID: ${loan.id}, LoanDate: ${loan.dateLoaned}, ReturnDate: $returnDate, Days: $days")
                        days
                    } catch (e: Exception) {
                        Log.e("WhereActivity", "Date parse error for loan ID ${loan.id}: ${e.message}, LoanDate: ${loan.dateLoaned}, ReturnDate: $returnDate")
                        0
                    }
                } ?: 0
                val duration = when {
                    daysLoaned < 0 -> "Overdue by ${-daysLoaned} days"
                    else -> "$daysLoaned days"
                }
                val loanedDisplay = try {
                    LocalDate.parse(loan.dateLoaned, formatter).format(displayFormatter)
                } catch (e: Exception) {
                    Log.e("WhereActivity", "Error formatting dateLoaned for loan ID ${loan.id}: ${e.message}")
                    loan.dateLoaned
                }
                val dueBack = loan.returnDate?.let {
                    try {
                        "Due: ${LocalDate.parse(it, formatter).format(displayFormatter)}"
                    } catch (e: Exception) {
                        Log.e("WhereActivity", "Error formatting returnDate for loan ID ${loan.id}: ${e.message}")
                        "Due: $it"
                    }
                } ?: "No due date"
                holder.tvLoanDetails.text = "Loaned: $loanedDisplay, $dueBack, $duration"
            } else {
                Log.w("WhereActivity", "Missing item or contact for loan ID: ${loan.id}")
                holder.tvItemName.text = "Unknown Item"
                holder.tvContactName.text = "Unknown Contact"
                holder.tvLoanDetails.text = "Loan ID: ${loan.id}, Data missing"
            }
        }

        override fun getItemCount(): Int = loanedItems.size
    }
}