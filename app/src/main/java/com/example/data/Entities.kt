package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val mobile: String,
    val role: String, // "student" or "admin"
    val plan: String = "None", // "None", "Basic", "Premium", "VIP"
    val planStatus: String = "Pending", // "Active", "Expired", "Pending"
    val validUntil: Long? = null,
    val assignedSeat: Int? = null
)

@Entity(tableName = "seats")
data class SeatEntity(
    @PrimaryKey val seatNumber: Int,
    val status: String, // "Available", "Occupied", "Reserved"
    val userId: Int? = null,
    val userName: String? = null,
    val shift: String = "24x7" // "24x7", "Morning", "Evening"
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val userName: String,
    val userEmail: String,
    val planName: String,
    val amount: Int,
    val transactionId: String,
    val status: String, // "Pending", "Approved", "Rejected"
    val timestamp: Long = System.currentTimeMillis()
)
