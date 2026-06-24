package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMessage(
    val sender: String, // "user" or "guru"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = LibraryDatabase.getDatabase(application)
    private val repository = LibraryRepository(database.libraryDao())

    // UI States
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    val seats: StateFlow<List<SeatEntity>> = repository.allSeats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payments: StateFlow<List<PaymentEntity>> = repository.allPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chatbot States
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("guru", "नमस्ते! I am Library Guru. Ask me anything about Shree Library timing, fees, seats, or Mau branch facilities!"))
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    // Auth messages / error notifications
    private val _authState = MutableStateFlow<String?>(null)
    val authState: StateFlow<String?> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateIfNeeded()
        }
    }

    fun clearAuthState() {
        _authState.value = null
    }

    // Authentication Logic
    fun login(email: String, onSuccess: (UserEntity) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email.trim().lowercase())
            if (user != null) {
                _currentUser.value = user
                onSuccess(user)
            } else {
                _authState.value = "User session credentials not found. Try 'admin' or 'student'!"
            }
        }
    }

    fun register(name: String, email: String, mobile: String, role: String, onSuccess: (UserEntity) -> Unit) {
        viewModelScope.launch {
            val cleanEmail = email.trim().lowercase()
            val existing = repository.getUserByEmail(cleanEmail)
            if (existing != null) {
                _authState.value = "An account with this email already exists!"
                return@launch
            }

            if (name.isBlank() || cleanEmail.isBlank() || mobile.isBlank()) {
                _authState.value = "Please fill in all requested fields!"
                return@launch
            }

            val newUser = UserEntity(
                name = name.trim(),
                email = cleanEmail,
                mobile = mobile.trim(),
                role = role
            )
            val id = repository.insertUser(newUser)
            val insertedUser = newUser.copy(id = id.toInt())
            _currentUser.value = insertedUser
            onSuccess(insertedUser)
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    // Chatbot actions
    fun askLibraryGuru(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage("user", prompt)
        _chatMessages.value = _chatMessages.value + userMsg
        _chatLoading.value = true

        viewModelScope.launch {
            val reply = withContext(Dispatchers.IO) {
                GeminiClient.askLibraryGuru(prompt)
            }
            _chatMessages.value = _chatMessages.value + ChatMessage("guru", reply)
            _chatLoading.value = false
        }
    }

    // Submit payment receipt
    fun submitPayment(planName: String, amount: Int, transactionId: String, onSubmitted: () -> Unit) {
        val user = _currentUser.value ?: return
        if (transactionId.isBlank()) return

        viewModelScope.launch {
            val payment = PaymentEntity(
                userId = user.id,
                userName = user.name,
                userEmail = user.email,
                planName = planName,
                amount = amount,
                transactionId = transactionId.trim().uppercase(),
                status = "Pending"
            )
            repository.insertPayment(payment)
            
            // Set user status to Pending
            val updatedUser = user.copy(plan = planName, planStatus = "Pending")
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
            
            onSubmitted()
        }
    }

    // Admin approve payment
    fun approvePayment(payment: PaymentEntity) {
        viewModelScope.launch {
            // Update payment status
            val updatedPayment = payment.copy(status = "Approved")
            repository.updatePayment(updatedPayment)

            // Update user plan details
            val user = repository.getUserById(payment.userId)
            if (user != null) {
                val validityDays = 30L
                val validUntilTime = System.currentTimeMillis() + (validityDays * 24 * 60 * 60 * 1000L)
                val updatedUser = user.copy(
                    plan = payment.planName,
                    planStatus = "Active",
                    validUntil = validUntilTime
                )
                repository.updateUser(updatedUser)
                // If it's the currently logged-in user, sync local state
                if (_currentUser.value?.id == user.id) {
                    _currentUser.value = updatedUser
                }
            }
        }
    }

    // Admin reject payment
    fun rejectPayment(payment: PaymentEntity) {
        viewModelScope.launch {
            val updatedPayment = payment.copy(status = "Rejected")
            repository.updatePayment(updatedPayment)

            val user = repository.getUserById(payment.userId)
            if (user != null) {
                val updatedUser = user.copy(
                    planStatus = "Expired"
                )
                repository.updateUser(updatedUser)
                if (_currentUser.value?.id == user.id) {
                    _currentUser.value = updatedUser
                }
            }
        }
    }

    // Admin / User Allocate Seat
    fun allocateSeat(seatNumber: Int, status: String, studentId: Int?, shift: String) {
        viewModelScope.launch {
            var studentName: String? = null
            
            if (status == "Occupied" && studentId != null) {
                val s = repository.getUserById(studentId)
                if (s != null) {
                    studentName = s.name
                    
                    // Unassign old seat if student had one
                    if (s.assignedSeat != null && s.assignedSeat != seatNumber) {
                        val oldSeat = seats.value.find { it.seatNumber == s.assignedSeat }
                        if (oldSeat != null) {
                            repository.updateSeat(oldSeat.copy(status = "Available", userId = null, userName = null))
                        }
                    }

                    // Assign new seat to user
                    repository.updateUser(s.copy(assignedSeat = seatNumber))
                    if (_currentUser.value?.id == s.id) {
                        _currentUser.value = _currentUser.value?.copy(assignedSeat = seatNumber)
                    }
                }
            } else if (status == "Available") {
                // If the seat was occupied, find the occupant and remove assignment
                val targetSeat = seats.value.find { it.seatNumber == seatNumber }
                if (targetSeat?.userId != null) {
                    val prevOccupant = repository.getUserById(targetSeat.userId)
                    if (prevOccupant != null && prevOccupant.assignedSeat == seatNumber) {
                        repository.updateUser(prevOccupant.copy(assignedSeat = null))
                        if (_currentUser.value?.id == prevOccupant.id) {
                            _currentUser.value = _currentUser.value?.copy(assignedSeat = null)
                        }
                    }
                }
            }

            val updatedSeat = SeatEntity(
                seatNumber = seatNumber,
                status = status,
                userId = if (status == "Available") null else studentId,
                userName = if (status == "Available") null else studentName,
                shift = shift
            )
            repository.updateSeat(updatedSeat)
        }
    }
}
