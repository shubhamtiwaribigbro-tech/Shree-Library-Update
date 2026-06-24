package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class LibraryRepository(private val libraryDao: LibraryDao) {

    val allUsers: Flow<List<UserEntity>> = libraryDao.getAllUsers()
    val allSeats: Flow<List<SeatEntity>> = libraryDao.getAllSeats()
    val allPayments: Flow<List<PaymentEntity>> = libraryDao.getAllPayments()

    suspend fun getUserByEmail(email: String): UserEntity? {
        return libraryDao.getUserByEmail(email)
    }

    suspend fun getUserById(id: Int): UserEntity? {
        return libraryDao.getUserById(id)
    }

    suspend fun insertUser(user: UserEntity): Long {
        return libraryDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity) {
        libraryDao.updateUser(user)
    }

    suspend fun updateSeat(seat: SeatEntity) {
        libraryDao.updateSeat(seat)
    }

    suspend fun insertPayment(payment: PaymentEntity): Long {
        return libraryDao.insertPayment(payment)
    }

    suspend fun updatePayment(payment: PaymentEntity) {
        libraryDao.updatePayment(payment)
    }

    // Prepopulate database if seats are empty
    suspend fun prepopulateIfNeeded() {
        val seats = libraryDao.getAllSeats().firstOrNull() ?: emptyList()
        if (seats.isEmpty()) {
            // Generate 125 seats
            val initialSeats = ArrayList<SeatEntity>()
            for (i in 1..125) {
                val status = when {
                    i == 42 -> "Occupied" // Our user's seat!
                    i % 7 == 0 -> "Reserved"
                    i % 3 == 0 -> "Occupied"
                    else -> "Available"
                }
                
                // Set some mock occupant names
                val userName = if (status == "Occupied") {
                    if (i == 42) "Demo Student" else "Student #$i"
                } else null
                
                val userId = if (status == "Occupied") {
                    if (i == 42) 2 else i + 10
                } else null

                initialSeats.add(
                    SeatEntity(
                        seatNumber = i,
                        status = status,
                        userId = userId,
                        userName = userName,
                        shift = if (i % 2 == 0) "24x7" else "Morning"
                    )
                )
            }
            libraryDao.insertSeats(initialSeats)

            // Insert default admin
            val adminExists = libraryDao.getUserByEmail("admin")
            if (adminExists == null) {
                libraryDao.insertUser(
                    UserEntity(
                        id = 1,
                        name = "Shubham Tiwari (Admin)",
                        email = "admin",
                        mobile = "9555529599",
                        role = "admin",
                        plan = "VIP",
                        planStatus = "Active"
                    )
                )
            }

            // Insert default student
            val studentExists = libraryDao.getUserByEmail("student")
            if (studentExists == null) {
                libraryDao.insertUser(
                    UserEntity(
                        id = 2,
                        name = "Amit Kumar",
                        email = "student",
                        mobile = "9555529599",
                        role = "student",
                        plan = "VIP",
                        planStatus = "Active",
                        assignedSeat = 42
                    )
                )
            }

            // Let's also add some initial pending payment requests to make the admin panel functional!
            val paymentsList = listOf(
                PaymentEntity(
                    userId = 2,
                    userName = "Amit Kumar",
                    userEmail = "student",
                    planName = "VIP",
                    amount = 1000,
                    transactionId = "TXN7392847194",
                    status = "Pending"
                ),
                PaymentEntity(
                    userId = 3,
                    userName = "Neha Sharma",
                    userEmail = "neha@gmail.com",
                    planName = "Premium",
                    amount = 750,
                    transactionId = "TXN9824719284",
                    status = "Approved"
                ),
                PaymentEntity(
                    userId = 4,
                    userName = "Rohan Gupta",
                    userEmail = "rohan@gmail.com",
                    planName = "Basic",
                    amount = 300,
                    transactionId = "TXN2847192837",
                    status = "Pending"
                )
            )
            for (p in paymentsList) {
                libraryDao.insertPayment(p)
            }
        }
    }
}
