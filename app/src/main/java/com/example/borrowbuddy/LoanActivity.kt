package com.example.borrowbuddy

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.app.DatePickerDialog
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import androidx.core.content.FileProvider
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

class LoanActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var etItem: EditText
    private lateinit var etBorrower: EditText
    private lateinit var etNotes: EditText
    private var returnDate: String? = null
    private var photoUri: Uri? = null
    private lateinit var takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var btnCamera: Button
    private lateinit var photoLayout: LinearLayout
    private lateinit var ivPhoto: ImageView
    private lateinit var btnDelete: Button
    private lateinit var btnCalendar: Button
    private lateinit var dateLayout: LinearLayout
    private lateinit var tvDate: TextView
    private lateinit var btnDeleteDate: Button
    private lateinit var btnLoanSubmit: Button
    private lateinit var scrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan)

        // Initialize UI elements
        try {
            tvTitle = findViewById(R.id.tvTitle)
            etItem = findViewById(R.id.etItem)
            etBorrower = findViewById(R.id.etBorrower)
            etNotes = findViewById(R.id.etNotes)
            btnCamera = findViewById(R.id.btnCamera)
            btnCalendar = findViewById(R.id.btnCalendar)
            btnLoanSubmit = findViewById(R.id.btnLoanSubmit)
            photoLayout = findViewById(R.id.photoLayout)
            ivPhoto = findViewById(R.id.ivPhoto)
            btnDelete = findViewById(R.id.btnDelete)
            dateLayout = findViewById(R.id.dateLayout)
            tvDate = findViewById(R.id.tvDate)
            btnDeleteDate = findViewById(R.id.btnDeleteDate)
            scrollView = findViewById(R.id.scrollView)
        } catch (e: Exception) {
            Log.e("LoanActivity", "Error initializing UI elements: ${e.message}")
            Toast.makeText(this, "Error loading UI", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Ensure initial visibility
        btnCamera.visibility = Button.VISIBLE
        btnCalendar.visibility = Button.VISIBLE
        photoLayout.visibility = LinearLayout.GONE
        dateLayout.visibility = LinearLayout.GONE

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("LoanActivity", "Camera result: code=${result.resultCode}, data=${result.data}")
            if (result.resultCode == RESULT_OK && photoUri != null) {
                photoLayout.visibility = LinearLayout.VISIBLE
                btnCamera.visibility = Button.GONE
                ivPhoto.setImageURI(photoUri)
                Log.d("LoanActivity", "Photo set: $photoUri")
            } else {
                Log.w("LoanActivity", "Camera result not OK or no photoUri")
                photoUri = null
            }
        }

        // Scroll to etNotes when focused
        etNotes.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                scrollView.post {
                    scrollView.smoothScrollTo(0, etNotes.top)
                }
            }
        }

        // Capitalize first character of item name
        etItem.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true
                val text = s.toString()
                if (text.isNotEmpty()) {
                    val capitalized = text.substring(0, 1).uppercase() + text.substring(1)
                    if (text != capitalized) {
                        etItem.setText(capitalized)
                        etItem.setSelection(capitalized.length)
                    }
                }
                isUpdating = false
            }
        })

        btnCalendar.setOnClickListener {
            Log.d("LoanActivity", "Calendar button clicked")
            currentFocus?.let { view ->
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                returnDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                dateLayout.visibility = LinearLayout.VISIBLE
                btnCalendar.visibility = Button.GONE
                tvDate.text = returnDate
                Log.d("LoanActivity", "Date set: $returnDate")
            }, year, month, day).show()
        }

        btnCamera.setOnClickListener {
            Log.d("LoanActivity", "Camera button clicked")
            currentFocus?.let { view ->
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e("LoanActivity", "Error creating image file: ${ex.message}")
                    Toast.makeText(this, "Error accessing camera", Toast.LENGTH_SHORT).show()
                    null
                }
                photoFile?.also {
                    photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    if (intent.resolveActivity(packageManager) != null) {
                        Log.d("LoanActivity", "Launching camera with URI: $photoUri")
                        takePictureLauncher.launch(intent)
                    } else {
                        Log.w("LoanActivity", "No camera app available")
                        photoUri = null
                        Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d("LoanActivity", "Requesting camera permission")
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)
            }
        }

        btnDelete.setOnClickListener {
            photoUri = null
            photoLayout.visibility = LinearLayout.GONE
            btnCamera.visibility = Button.VISIBLE
            Log.d("LoanActivity", "Photo deleted")
        }

        btnDeleteDate.setOnClickListener {
            returnDate = null
            dateLayout.visibility = LinearLayout.GONE
            btnCalendar.visibility = Button.VISIBLE
            Log.d("LoanActivity", "Date deleted")
        }

        btnLoanSubmit.setOnClickListener {
            val itemName = etItem.text.toString().trim()
            val borrowerName = etBorrower.text.toString().trim()
            val notes = etNotes.text.toString().trim().takeIf { it.isNotBlank() }

            if (itemName.isEmpty() || borrowerName.isEmpty()) {
                Log.w("LoanActivity", "Item or borrower name is empty")
                Toast.makeText(this, "Please enter item and borrower name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val capitalizedItemName = if (itemName.isNotEmpty()) {
                itemName.substring(0, 1).uppercase() + itemName.substring(1)
            } else {
                itemName
            }

            val itemId = DataController.addItem(this, capitalizedItemName, "Description TBD", photoUri?.toString())
            val contactId = DataController.addContact(this, borrowerName)
            val loanId = DataController.addLoan(this, itemId, contactId, returnDate, 1, notes)

            Log.d("LoanActivity", "Loan submitted: LoanID=$loanId, ItemID=$itemId, ContactID=$contactId, ReturnDate=$returnDate, Notes=$notes")

            // Pass item and borrower names to MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("ITEM_NAME", capitalizedItemName)
                putExtra("BORROWER_NAME", borrowerName)
            }
            startActivity(intent)
            finish()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("LoanActivity", "Camera permission granted")
            btnCamera.performClick()
        } else {
            Log.w("LoanActivity", "Camera permission denied")
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}