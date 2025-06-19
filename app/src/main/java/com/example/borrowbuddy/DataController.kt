package com.example.borrowbuddy

import android.content.Context
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DataController {
    private const val CONTACTS_FILE = "contacts.json"
    private const val ITEMS_FILE = "items.json"
    private const val LOANS_FILE = "loans.json"
    private const val BORROWS_FILE = "borrows.json"

    private val contacts = mutableListOf<Contact>()
    private val items = mutableListOf<Item>()
    private val loans = mutableListOf<Loan>()
    private val borrows = mutableListOf<Borrow>()

    fun initialize(context: Context) {
        loadContacts(context)
        loadItems(context)
        loadLoans(context)
        loadBorrows(context)
    }

    // Contacts
    fun saveContacts(context: Context) {
        try {
            val json = Json.encodeToString(contacts)
            context.openFileOutput(CONTACTS_FILE, Context.MODE_PRIVATE).use { it.write(json.toByteArray()) }
        } catch (e: IOException) {
            Log.e("DataController", "Error saving contacts: ${e.message}")
        }
    }

    fun loadContacts(context: Context): List<Contact> {
        val file = File(context.filesDir, CONTACTS_FILE)
        if (file.exists()) {
            try {
                val json = file.readText()
                return Json.decodeFromString<List<Contact>>(json).also { contacts.clear(); contacts.addAll(it) }
            } catch (e: IOException) {
                Log.e("DataController", "Error loading contacts: ${e.message}")
            } catch (e: Exception) {
                Log.e("DataController", "Error parsing contacts: ${e.message}")
            }
        }
        return contacts
    }

    fun addContact(context: Context, name: String): Int {
        val id = contacts.maxOfOrNull { it.id }?.plus(1) ?: 1
        val contact = Contact(id, name)
        contacts.add(contact)
        saveContacts(context)
        return id
    }

    fun getContacts(): List<Contact> = contacts

    // Items
    fun saveItems(context: Context) {
        try {
            val json = Json.encodeToString(items)
            context.openFileOutput(ITEMS_FILE, Context.MODE_PRIVATE).use { it.write(json.toByteArray()) }
        } catch (e: IOException) {
            Log.e("DataController", "Error saving items: ${e.message}")
        }
    }

    fun loadItems(context: Context): List<Item> {
        val file = File(context.filesDir, ITEMS_FILE)
        if (file.exists()) {
            try {
                val json = file.readText()
                return Json.decodeFromString<List<Item>>(json).also { items.clear(); items.addAll(it) }
            } catch (e: IOException) {
                Log.e("DataController", "Error loading items: ${e.message}")
            } catch (e: Exception) {
                Log.e("DataController", "Error parsing items: ${e.message}")
            }
        }
        return items
    }

    fun addItem(context: Context, name: String, description: String, photoUri: String? = null): Int {
        val id = items.maxOfOrNull { it.id }?.plus(1) ?: 1
        val item = Item(id, name, description, photoUri)
        items.add(item)
        saveItems(context)
        return id
    }

    fun getItems(): List<Item> = items

    // Loans
    fun saveLoans(context: Context) {
        try {
            val json = Json.encodeToString(loans)
            context.openFileOutput(LOANS_FILE, Context.MODE_PRIVATE).use { it.write(json.toByteArray()) }
        } catch (e: IOException) {
            Log.e("DataController", "Error saving loans: ${e.message}")
        }
    }

    fun loadLoans(context: Context): List<Loan> {
        val file = File(context.filesDir, LOANS_FILE)
        if (file.exists()) {
            try {
                val json = file.readText()
                return Json.decodeFromString<List<Loan>>(json).also { loans.clear(); loans.addAll(it) }
            } catch (e: IOException) {
                Log.e("DataController", "Error loading loans: ${e.message}")
            } catch (e: Exception) {
                Log.e("DataController", "Error parsing loans: ${e.message}")
            }
        }
        return loans
    }

    fun addLoan(context: Context, itemId: Int, contactId: Int, returnDate: String?, disposition: Int): Int {
        val id = loans.maxOfOrNull { it.id }?.plus(1) ?: 1
        val dateLoaned = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val formattedReturnDate = returnDate?.let {
            try {
                val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                LocalDate.parse(it, inputFormatter).format(outputFormatter)
            } catch (e: Exception) {
                Log.e("DataController", "Error formatting returnDate: ${e.message}, Input: $it")
                null // Return null if parsing fails
            }
        }
        val loan = Loan(id, itemId, contactId, formattedReturnDate, disposition, dateLoaned)
        loans.add(loan)
        saveLoans(context)
        Log.d("DataController", "Loan added: ID=$id, ItemID=$itemId, ContactID=$contactId, LoanDate=$dateLoaned, ReturnDate=$formattedReturnDate")
        return id
    }

    fun getLoans(): List<Loan> = loans

    // Borrows
    fun saveBorrows(context: Context) {
        try {
            val json = Json.encodeToString(borrows)
            context.openFileOutput(BORROWS_FILE, Context.MODE_PRIVATE).use { it.write(json.toByteArray()) }
        } catch (e: IOException) {
            Log.e("DataController", "Error saving borrows: ${e.message}")
        }
    }

    fun loadBorrows(context: Context): List<Borrow> {
        val file = File(context.filesDir, BORROWS_FILE)
        if (file.exists()) {
            try {
                val json = file.readText()
                return Json.decodeFromString<List<Borrow>>(json).also { borrows.clear(); borrows.addAll(it) }
            } catch (e: IOException) {
                Log.e("DataController", "Error loading borrows: ${e.message}")
            } catch (e: Exception) {
                Log.e("DataController", "Error parsing borrows: ${e.message}")
            }
        }
        return borrows
    }

    fun addBorrow(context: Context, itemId: Int, contactId: Int, returnDate: String?, disposition: Int): Int {
        val id = borrows.maxOfOrNull { it.id }?.plus(1) ?: 1
        val dateBorrowed = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val formattedReturnDate = returnDate?.let {
            try {
                val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                LocalDate.parse(it, inputFormatter).format(outputFormatter)
            } catch (e: Exception) {
                Log.e("DataController", "Error formatting returnDate: ${e.message}, Input: $it")
                null
            }
        }
        val borrow = Borrow(id, itemId, contactId, formattedReturnDate, disposition, dateBorrowed)
        borrows.add(borrow)
        saveBorrows(context)
        return id
    }

    fun getBorrows(): List<Borrow> = borrows
}