// app/src/main/java/id/xetor/app/ui/setor/PickUpScreen.kt
package id.xetor.app.ui.setor

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import id.xetor.app.R
import id.xetor.app.data.remote.WasteDetailResponse
import id.xetor.app.ui.theme.GreenPrimary
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.DecimalFormat

// Data class untuk waste item yang ditambahkan user
data class WasteItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val wasteDetail: WasteDetailResponse,
    val weight: Double // dalam kg
) {
    fun getTotalXpoin(): Int {
        return (wasteDetail.xpoin * weight).toInt()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickUpScreen(
    viewModel: PickUpViewModel,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAddWasteModal by remember { mutableStateOf(false) }
    var showSuccessModal by remember { mutableStateOf(false) }
    var editingWasteItem by remember { mutableStateOf<WasteItem?>(null) }
    
    // Calculate total weight and total xpoin
    val totalWeight = uiState.wasteItems.sumOf { it.weight }
    val totalXpoin = uiState.wasteItems.sumOf { it.getTotalXpoin() }
    
    // Show success modal after confirmation
    LaunchedEffect(uiState.showSuccess) {
        if (uiState.showSuccess) {
            showSuccessModal = true
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Map Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                UserLocationMapView(
                    userLocation = uiState.userLocation,
                    onLocationUpdate = { location ->
                        viewModel.updateUserLocation(location)
                    }
                )
            }
            
            // Content Section (scrollable)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 100.dp) // Padding bottom untuk bottom bar
            ) {
                // Title
                Text(
                    text = "Setor Sampahmu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // List Sampah Section
                Text(
                    text = "List Sampah",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Waste Items List
                if (uiState.wasteItems.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Belum ada sampah ditambahkan",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        uiState.wasteItems.forEach { item ->
                            WasteItemCard(
                                item = item,
                                onEdit = {
                                    editingWasteItem = item
                                    showAddWasteModal = true
                                },
                                onDelete = {
                                    viewModel.removeWasteItem(item.id)
                                }
                            )
                        }
                    }
                }
                
                // Add Button
                Button(
                    onClick = {
                        editingWasteItem = null
                        showAddWasteModal = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+ Tambah",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
                
                // Informasi Tambahan Section
                Text(
                    text = "Informasi Tambahan",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                )
                
                OutlinedTextField(
                    value = uiState.additionalInfo,
                    onValueChange = { viewModel.setAdditionalInfo(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(bottom = 16.dp),
                    placeholder = { Text("Tambahkan informasi tambahan (opsional)", fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    maxLines = 4
                )
                
                // Image Upload Section (tampilan saja)
                Text(
                    text = "Upload Foto",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_profile),
                            contentDescription = "Upload Image",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = "Silakan upload foto sampah anda, Max 2MB",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Fitur upload foto belum tersedia", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GreenPrimary
                            ),
                            border = BorderStroke(1.dp, GreenPrimary)
                        ) {
                            Text("Choose File", fontSize = 12.sp)
                        }
                        Text(
                            text = "No File Chosen",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
        
        // Bottom Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .zIndex(3f),
            color = GreenPrimary,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Berat: ${DecimalFormat("#.##").format(totalWeight)} kg",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                
                Button(
                    onClick = {
                        if (uiState.wasteItems.isEmpty()) {
                            Toast.makeText(context, "Tambahkan sampah terlebih dahulu", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.confirmPickUp()
                        }
                    },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "Konfirmasi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                }
            }
        }
        
        // Add/Edit Waste Modal
        if (showAddWasteModal) {
            AddWasteModal(
                wasteDetails = uiState.wasteDetails,
                editingItem = editingWasteItem,
                onDismiss = {
                    showAddWasteModal = false
                    editingWasteItem = null
                },
                onAdd = { wasteDetail, weight ->
                    if (editingWasteItem != null) {
                        viewModel.updateWasteItem(editingWasteItem!!.id, wasteDetail, weight)
                    } else {
                        viewModel.addWasteItem(wasteDetail, weight)
                    }
                    showAddWasteModal = false
                    editingWasteItem = null
                },
                onDelete = {
                    if (editingWasteItem != null) {
                        viewModel.removeWasteItem(editingWasteItem!!.id)
                    }
                    showAddWasteModal = false
                    editingWasteItem = null
                }
            )
        }
        
        // Success Modal
        if (showSuccessModal) {
            SuccessModal(
                onDismiss = {
                    showSuccessModal = false
                    viewModel.resetSuccess()
                    onSuccess() // Navigate to home
                }
            )
        }
    }
}

@Composable
fun WasteItemCard(
    item: WasteItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.wasteDetail.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Berat: ${DecimalFormat("#.##").format(item.weight)} Kg",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            
            Text(
                text = "${formatNumber(item.getTotalXpoin())} Xp",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GreenPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWasteModal(
    wasteDetails: List<WasteDetailResponse>,
    editingItem: WasteItem?,
    onDismiss: () -> Unit,
    onAdd: (WasteDetailResponse, Double) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var selectedWasteDetail by remember { mutableStateOf<WasteDetailResponse?>(editingItem?.wasteDetail) }
    var weightText by remember { mutableStateOf(editingItem?.weight?.toString() ?: "") }
    var isExpanded by remember { mutableStateOf(false) }
    
    // Initialize selected waste detail
    LaunchedEffect(editingItem) {
        selectedWasteDetail = editingItem?.wasteDetail
        weightText = editingItem?.weight?.toString() ?: ""
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
            .zIndex(4f),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = if (editingItem != null) "Edit Jenis Sampah" else "Tambah Jenis Sampah",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                // Waste Type Dropdown
                Text(
                    text = "Nama Jenis Sampah",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                
                ExposedDropdownMenuBox(
                    expanded = isExpanded,
                    onExpandedChange = { isExpanded = !isExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedWasteDetail?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false }
                    ) {
                        wasteDetails.forEach { wasteDetail ->
                            DropdownMenuItem(
                                text = { Text(wasteDetail.name) },
                                onClick = {
                                    selectedWasteDetail = wasteDetail
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
                
                // Weight Input
                Text(
                    text = "Berat (Kg)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { newValue ->
                        // Only allow numbers and one decimal point
                        if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            weightText = newValue
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("0.0") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenPrimary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (editingItem != null) {
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red
                            ),
                            border = BorderStroke(1.dp, Color.Red)
                        ) {
                            Text("Hapus", fontSize = 14.sp)
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = GreenPrimary
                        ),
                        border = BorderStroke(1.dp, GreenPrimary)
                    ) {
                        Text("Batal", fontSize = 14.sp)
                    }
                    
                    Button(
                        onClick = {
                            val weight = weightText.toDoubleOrNull() ?: 0.0
                            if (selectedWasteDetail == null) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    Toast.makeText(context, "Pilih jenis sampah terlebih dahulu", Toast.LENGTH_SHORT).show()
                                }
                            } else if (weight <= 0) {
                                android.os.Handler(android.os.Looper.getMainLooper()).post {
                                    Toast.makeText(context, "Masukkan berat yang valid", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                onAdd(selectedWasteDetail!!, weight)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Text(if (editingItem != null) "Simpan" else "Tambah", fontSize = 14.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SuccessModal(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onDismiss() }
            .zIndex(5f),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Success Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GreenPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
                
                Text(
                    text = "Permintaan Berhasil",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text(
                        text = "Kembali",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun UserLocationMapView(
    userLocation: GeoPoint?,
    onLocationUpdate: (GeoPoint) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Default location: Indonesia center
    val defaultLocation = GeoPoint(-2.5489, 118.0149)
    val currentLocation = userLocation ?: defaultLocation
    
    // Initialize OSMDroid
    LaunchedEffect(Unit) {
        try {
            Configuration.getInstance().load(
                context,
                context.getSharedPreferences("osmdroid", android.content.Context.MODE_PRIVATE)
            )
            Configuration.getInstance().userAgentValue = context.packageName
        } catch (e: Exception) {
            // Handle configuration error silently
        }
    }
    
    // Request location updates
    var locationCallbackRef by remember { mutableStateOf<LocationCallback?>(null) }
    var fusedLocationClientRef by remember { mutableStateOf<FusedLocationProviderClient?>(null) }
    
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClientRef = fusedLocationClient
                
                // Get last known location first
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val geoPoint = GeoPoint(it.latitude, it.longitude)
                        onLocationUpdate(geoPoint)
                    }
                }
                
                // Request location updates
                val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                    .setMinUpdateIntervalMillis(500L)
                    .build()
                
                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let { location ->
                            val geoPoint = GeoPoint(location.latitude, location.longitude)
                            onLocationUpdate(geoPoint)
                        }
                    }
                }
                
                locationCallbackRef = locationCallback
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    android.os.Looper.getMainLooper()
                )
            } catch (e: Exception) {
                // Handle location error
                android.util.Log.e("PickUpScreen", "Location error: ${e.message}")
            }
        }
    }
    
    // Cleanup location updates
    DisposableEffect(locationCallbackRef, fusedLocationClientRef) {
        onDispose {
            locationCallbackRef?.let { callback ->
                fusedLocationClientRef?.removeLocationUpdates(callback)
            }
        }
    }
    
    // Lifecycle management for MapView
    DisposableEffect(mapViewRef, lifecycleOwner) {
        val mapView = mapViewRef
        if (mapView != null) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        mapView.onResume()
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        mapView.onPause()
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        mapView.onDetach()
                    }
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            mapView.onResume()
            
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                mapView.onPause()
            }
        } else {
            onDispose { }
        }
    }
    
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                mapViewRef = this
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                setBuiltInZoomControls(false)
                
                // Set initial location
                controller.setZoom(15.0)
                controller.setCenter(currentLocation)
                
                // Add user location marker
                val marker = Marker(this).apply {
                    position = currentLocation
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    // Use orange pin icon (you may need to add this drawable)
                    try {
                        val icon = android.graphics.drawable.BitmapDrawable(
                            ctx.resources,
                            android.graphics.BitmapFactory.decodeResource(ctx.resources, R.drawable.pin_map_xetor)
                        )
                        setIcon(icon)
                    } catch (e: Exception) {
                        // Use default marker if custom icon not found
                    }
                }
                overlays.add(marker)
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { mapView ->
            // Update marker position when location changes
            mapView.overlays.clear()
            val marker = Marker(mapView).apply {
                position = currentLocation
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                try {
                    val icon = android.graphics.drawable.BitmapDrawable(
                        context.resources,
                        android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.pin_map_xetor)
                    )
                    setIcon(icon)
                } catch (e: Exception) {
                    // Use default marker
                }
            }
            mapView.overlays.add(marker)
            mapView.controller.setCenter(currentLocation)
        }
    )
}

// Helper function
private fun formatNumber(value: Int): String {
    return String.format("%,d", value).replace(',', '.')
}

