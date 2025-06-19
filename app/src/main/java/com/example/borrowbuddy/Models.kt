package com.example.borrowbuddy

import kotlinx.serialization.Serializable

@Serializable
data class Contact(val id: Int, val name: String)

@Serializable
data class Item(val id: Int, val name: String, val description: String, val photoUri: String? = null)

@Serializable
data class Loan(val id: Int, val itemId: Int, val contactId: Int, val returnDate: String?, val disposition: Int, val dateLoaned: String)

@Serializable
data class Borrow(val id: Int, val itemId: Int, val contactId: Int, val returnDate: String?, val disposition: Int, val dateBorrowed: String)