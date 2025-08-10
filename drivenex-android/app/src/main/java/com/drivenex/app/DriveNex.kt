package com.drivenex.app

import android.app.Application
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data classes and enums

data class EmergencyContact(val name: String, val phoneNumber: String, val relationship: String)

data class RideRequest(val id: String, val details: String)

data class TripSummary(
    val tripId: String,
    val pickupAddress: String,
    val dropoffAddress: String,
    val distance: Double,
    val duration: Int,
    val fare: Double,
    val driverName: String,
    val vehicleModel: String,
    val licensePlate: String,
    val driverRating: Double?
)

enum class PaymentStatus { PENDING, COMPLETED, FAILED, REFUNDED }

enum class LocationAccuracy { HIGH, MEDIUM, LOW }

// Managers and services (stubs)

class LocationManager(private val context: Context) {
    fun stopLocationUpdates() {}
}

class BiometricAuthManager(private val context: Context)

class EmergencyManager(private val context: Context)

object NetworkConfig {
    const val WEBSOCKET_URL: String = "wss://example.com"
    fun createApiService(): Any = Any()
}

class WebSocketManager(url: String)
class TokenManager(context: Context)
class DriveNexRepository(api: Any, ws: WebSocketManager, token: TokenManager)
class PermissionsManager(context: Context)

class BackgroundLocationService {
    fun startBackgroundLocationTracking() {}
    fun stopBackgroundLocationTracking() {}
    fun getCurrentLocationAccuracy(): LocationAccuracy = LocationAccuracy.HIGH
}

class DriveNexNotificationManager(private val context: Context) {
    fun showRideRequestNotification(request: RideRequest) {}
    fun showDriverArrivedNotification(driverName: String) {}
    fun showTripStartedNotification() {}
    fun showTripCompletedNotification(fare: Double) {}
    fun showEmergencyNotification() {}
    fun clearAllNotifications() {}
}

object AnalyticsManager {
    fun trackEvent(eventName: String, parameters: Map<String, Any>) {}
    fun trackScreen(screenName: String) {}
    fun setUserProperty(propertyName: String, value: String) {}
}

class DriveNexApplication : Application() {
    val apiService by lazy { NetworkConfig.createApiService() }
    val webSocketManager by lazy { WebSocketManager(NetworkConfig.WEBSOCKET_URL) }
    val tokenManager by lazy { TokenManager(this) }
    val repository by lazy { DriveNexRepository(apiService, webSocketManager, tokenManager) }
    val permissionsManager by lazy { PermissionsManager(this) }
    val emergencyManager by lazy { EmergencyManager(this) }
    val backgroundLocationService by lazy { BackgroundLocationService() }
    val analyticsManager by lazy { AnalyticsManager }

    override fun onCreate() {
        super.onCreate()
        analyticsManager.trackEvent("app_started", mapOf("timestamp" to System.currentTimeMillis()))
    }
}

// App root

@Composable
fun DriveNexApp() {
    Surface(modifier = Modifier.fillMaxSize()) {
        // Simple entry using PaymentProcessingScreen to verify UI compiles
        PaymentProcessingScreen(amount = 12.34) {}
    }
}

// Composables

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEmergencyContactDialog(
    onDismiss: () -> Unit,
    onAdd: (EmergencyContact) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Emergency Contact") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = relationship,
                    onValueChange = { relationship = it },
                    label = { Text("Relationship") },
                    placeholder = { Text("e.g., Spouse, Parent, Friend") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phoneNumber.isNotBlank() && relationship.isNotBlank()) {
                        onAdd(EmergencyContact(name, phoneNumber, relationship))
                    }
                },
                enabled = name.isNotBlank() && phoneNumber.isNotBlank() && relationship.isNotBlank()
            ) {
                Text("Add Contact")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DriverVerificationScreen(
    onVerificationComplete: () -> Unit,
    onSkip: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    var licenseNumber by remember { mutableStateOf("") }
    var vehicleMake by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var vehicleYear by remember { mutableStateOf("") }
    var vehicleColor by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var documentsUploaded by remember { mutableStateOf(mapOf<String, Boolean>()) }

    val steps = listOf("Personal Info", "Vehicle Details", "Documents", "Review")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LinearProgressIndicator(
            progress = (currentStep + 1) / steps.size.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Step ${currentStep + 1} of ${steps.size}: ${steps[currentStep]}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        when (currentStep) {
            0 -> PersonalInfoStep(
                licenseNumber = licenseNumber,
                onLicenseNumberChange = { licenseNumber = it }
            )
            1 -> VehicleDetailsStep(
                make = vehicleMake,
                onMakeChange = { vehicleMake = it },
                model = vehicleModel,
                onModelChange = { vehicleModel = it },
                year = vehicleYear,
                onYearChange = { vehicleYear = it },
                color = vehicleColor,
                onColorChange = { vehicleColor = it },
                licensePlate = licensePlate,
                onLicensePlateChange = { licensePlate = it }
            )
            2 -> DocumentsStep(
                documentsUploaded = documentsUploaded,
                onDocumentUploaded = { docType, uploaded ->
                    documentsUploaded = documentsUploaded.toMutableMap().apply { this[docType] = uploaded }
                }
            )
            3 -> ReviewStep(
                licenseNumber = licenseNumber,
                vehicleMake = vehicleMake,
                vehicleModel = vehicleModel,
                vehicleYear = vehicleYear,
                vehicleColor = vehicleColor,
                licensePlate = licensePlate
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentStep > 0) {
                OutlinedButton(onClick = { currentStep-- }) { Text("Back") }
            } else {
                TextButton(onClick = onSkip) { Text("Skip for now") }
            }
            Button(
                onClick = {
                    if (currentStep < steps.size - 1) currentStep++ else onVerificationComplete()
                }
            ) { Text(if (currentStep < steps.size - 1) "Next" else "Submit") }
        }
    }
}

// Payment

data class PaymentTransaction(
    val id: String,
    val rideId: String,
    val amount: Double,
    val currency: String = "USD",
    val status: PaymentStatus,
    val timestamp: Long,
    val paymentMethodId: String
)

@Composable
fun PaymentProcessingScreen(
    amount: Double,
    onPaymentComplete: (Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var paymentResult by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Processing Payment...",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Please wait while we process your payment of \$${String.format("%.2f", amount)}",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else if (paymentResult != null) {
            val isSuccess = paymentResult == "success"
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isSuccess) Color.Green else Color.Red,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isSuccess) "Payment Successful!" else "Payment Failed",
                style = MaterialTheme.typography.headlineSmall,
                color = if (isSuccess) Color.Green else Color.Red
            )
            Text(
                text = if (isSuccess)
                    "Your payment of \$${String.format("%.2f", amount)} has been processed successfully."
                else "There was an error processing your payment. Please try again.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onPaymentComplete(isSuccess) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (isSuccess) "Continue" else "Try Again") }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Confirm Payment",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "\$${String.format("%.2f", amount)}",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ride fare",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            isProcessing = true
                            coroutineScope.launch {
                                delay(3000)
                                paymentResult = if (kotlin.random.Random.nextBoolean()) "success" else "failure"
                                isProcessing = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Pay Now") }
                    TextButton(
                        onClick = { onPaymentComplete(false) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) { Text("Cancel") }
                }
            }
        }
    }
}

@Composable
fun RideRatingDialog(
    driverName: String,
    onRatingSubmitted: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Rate Your Ride")
                Text(
                    text = "How was your trip with $driverName?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Star ${index + 1}",
                                tint = if (index < rating) Color(0xFFFFB000) else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                if (rating > 0) {
                    Text(
                        text = when (rating) {
                            1 -> "Poor"
                            2 -> "Fair"
                            3 -> "Good"
                            4 -> "Very Good"
                            5 -> "Excellent"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Add a comment (optional)") },
                    placeholder = { Text("Tell us about your experience...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onRatingSubmitted(rating, comment) }, enabled = rating > 0) {
                Text("Submit")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Skip") } }
    )
}

@Composable
fun TripSummaryScreen(
    tripDetails: TripSummary,
    onRateTrip: () -> Unit,
    onRequestReceipt: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Trip Complete",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Close") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RadioButtonChecked,
                        contentDescription = null,
                        tint = Color.Green,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tripDetails.pickupAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tripDetails.dropoffAddress,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Distance",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = "${tripDetails.distance} km", fontWeight = FontWeight.Medium)
                    }
                    Column {
                        Text(
                            text = "Time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(text = "${tripDetails.duration} min", fontWeight = FontWeight.Medium)
                    }
                    Column {
                        Text(
                            text = "Fare",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "\$${String.format("%.2f", tripDetails.fare)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tripDetails.driverName.take(1),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = tripDetails.driverName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "${tripDetails.vehicleModel} • ${tripDetails.licensePlate}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                tripDetails.driverRating?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFB000),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = it.toString(), fontSize = 14.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onRateTrip, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rate Your Trip")
            }
            OutlinedButton(onClick = onRequestReceipt, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Receipt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Get Receipt")
            }
        }
    }
}

// Minimal stubs for steps to allow compilation

@Composable
fun PersonalInfoStep(licenseNumber: String, onLicenseNumberChange: (String) -> Unit) {
    Column { Text("Personal Info" ) }
}

@Composable
fun VehicleDetailsStep(
    make: String, onMakeChange: (String) -> Unit,
    model: String, onModelChange: (String) -> Unit,
    year: String, onYearChange: (String) -> Unit,
    color: String, onColorChange: (String) -> Unit,
    licensePlate: String, onLicensePlateChange: (String) -> Unit
) { Column { Text("Vehicle Details") } }

@Composable
fun DocumentsStep(
    documentsUploaded: Map<String, Boolean>,
    onDocumentUploaded: (String, Boolean) -> Unit
) { Column { Text("Documents") } }

@Composable
fun ReviewStep(
    licenseNumber: String,
    vehicleMake: String,
    vehicleModel: String,
    vehicleYear: String,
    vehicleColor: String,
    licensePlate: String
) { Column { Text("Review") } }