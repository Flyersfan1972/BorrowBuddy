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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.app.DatePickerDialog
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import com.example.borrowbuddy.DataController.addItem
import com.example.borrowbuddy.DataController.addContact
import com.example.borrowbuddy.DataController.addLoan
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager

class LoanActivity : AppCompatActivity() {

    private lateinit var etItem: EditText
    private lateinit var etBorrower: EditText
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan)

        etItem = findViewById(R.id.etItem)
        etBorrower = findViewById(R.id.etBorrower)
        btnCalendar = findViewById(R.id.btnCalendar)
        btnCamera = findViewById(R.id.btnCamera)
        val btnLoanSubmit = findViewById<Button>(R.id.btnLoanSubmit)
        photoLayout = findViewById(R.id.photoLayout)
        ivPhoto = findViewById(R.id.ivPhoto)
        btnDelete = findViewById(R.id.btnDelete)
        dateLayout = findViewById(R.id.dateLayout)
        tvDate = findViewById(R.id.tvDate)
        btnDeleteDate = findViewById(R.id.btnDeleteDate)

        // Capitalize first character of item name as user types
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

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("LoanActivity", "Result received: code=${result.resultCode}, data=${result.data}")
            if (result.resultCode == RESULT_OK && photoUri != null) {
                Log.d("LoanActivity", "Photo URI: $photoUri")
                btnCamera.visibility = Button.GONE
                photoLayout.visibility = LinearLayout.VISIBLE
                ivPhoto.setImageURI(photoUri)
                Log.d("LoanActivity", "Photo displayed")
            } else {
                Log.d("LoanActivity", "Result not OK or no photo URI, code: ${result.resultCode}")
                photoUri = null // Reset if failed or canceled
            }
        }

        btnCalendar.setOnClickListener {
            Log.d("LoanActivity", "Calendar button clicked")
            // Clear focus and hide keyboard
            etBorrower.clearFocus()
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
                btnCalendar.visibility = Button.GONE
                dateLayout.visibility = LinearLayout.VISIBLE
                tvDate.text = returnDate
                Log.d("LoanActivity", "Date selected: $returnDate")
            }, year, month, day).show()
        }

        btnCamera.setOnClickListener {
            Log.d("LoanActivity", "Camera button clicked")
            // Clear focus and hide keyboard
            etBorrower.clearFocus()
            currentFocus?.let { view ->
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Log.e("LoanActivity", "Error creating file: ${ex.message}")
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
                        Log.d("LoanActivity", "No camera app available")
                        photoUri = null
                    }
                }
            } else {
                Log.d("LoanActivity", "Camera permission not granted, requesting")
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
            val itemName = etItem.text.toString()
            val borrowerName = etBorrower.text.toString()

            // Capitalize first character for storage
            val capitalizedItemName = if (itemName.isNotEmpty()) {
                itemName.substring(0, 1).uppercase() + itemName.substring(1)
            } else {
                itemName
            }

            // Use DataController for generic operations
            val itemId = addItem(this, capitalizedItemName, "Description TBD", photoUri?.toString())
            val contactId = addContact(this, borrowerName)
            val loanId = addLoan(this, itemId, contactId, returnDate, 1) // 1 = loaned

            Log.d("LoanActivity", "Loan submitted: LoanID=$loanId, ItemID=$itemId, ContactID=$contactId, ReturnDate=$returnDate")

            // Clear fields
            etItem.text.clear()
            etBorrower.text.clear()
            returnDate = null
            photoUri = null
            photoLayout.visibility = LinearLayout.GONE
            btnCamera.visibility = Button.VISIBLE
            dateLayout.visibility = LinearLayout.GONE
            btnCalendar.visibility = Button.VISIBLE

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
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(packageManager) != null) {
                Log.d("LoanActivity", "Permission granted, launching camera")
                takePictureLauncher.launch(intent)
            }
        } else {
            Log.d("LoanActivity", "Permission denied")
        }
    }
}