// app/src/main/java/id/xetor/app/ui/mitra/MitraScreen.kt
package id.xetor.app.ui.mitra

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import android.view.ViewGroup
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import android.graphics.drawable.BitmapDrawable
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import android.graphics.Canvas
import android.view.MotionEvent
import android.util.Log
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import id.xetor.app.R
import id.xetor.app.data.remote.PublicPartnerResponse
import id.xetor.app.ui.components.CustomSnackbar
import id.xetor.app.ui.theme.GreenPrimary

enum class ViewType {
    MAP, LIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MitraScreen(
    viewModel: MitraViewModel,
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedViewType by remember { mutableStateOf(ViewType.MAP) }
    var selectedPartner by remember { mutableStateOf<PublicPartnerResponse?>(null) }
    var selectedPartnerPosition by remember { mutableStateOf<GeoPoint?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    
    // Filter partners yang punya koordinat
    val partnersWithLocation = remember(uiState.partners) {
        uiState.partners.filter { 
            it.getLatitude() != null && it.getLongitude() != null 
        }
    }
    
    // Default location: Indonesia center
    val defaultLocation = GeoPoint(-2.5489, 118.0149)
    
    // Calculate center point from all partners (average of all coordinates)
    val centerPoint = remember(partnersWithLocation) {
        if (partnersWithLocation.isNotEmpty()) {
            var totalLat = 0.0
            var totalLng = 0.0
            partnersWithLocation.forEach { partner ->
                totalLat += partner.getLatitude()!!
                totalLng += partner.getLongitude()!!
            }
            val avgLat = totalLat / partnersWithLocation.size
            val avgLng = totalLng / partnersWithLocation.size
            
            // Geser center point ke utara (tambah latitude) untuk mengkompensasi header
            val latOffset = 0.015 // Offset ke utara (sekitar 1.6 km)
            GeoPoint(avgLat + latOffset, avgLng)
        } else {
            defaultLocation
        }
    }
    
    // Calculate zoom level based on number of partners
    val zoomLevel = remember(partnersWithLocation) {
        if (partnersWithLocation.isEmpty()) {
            10.0
        } else if (partnersWithLocation.size == 1) {
            14.0 // Zoom lebih dekat untuk satu partner
        } else {
            13.0 // Zoom sedikit lebih jauh untuk multiple partners
        }
    }

    // Initialize OSMDroid configuration
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

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Map/Content area - full screen di belakang header, TANPA offset
        // Map langsung fillMaxSize tanpa offset agar MapView mendapatkan constraints yang benar
        Box(
            modifier = Modifier
                .fillMaxSize() // Langsung fillMaxSize tanpa offset
                .zIndex(1f) // Map di bawah header
        ) {
            if (uiState.isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else {
                when (selectedViewType) {
                    ViewType.MAP -> {
                        // Map view - full screen sampai bawah, tidak ada space kosong
                        PartnersMapView(
                            partners = uiState.partners,
                            onMapViewReady = { mapView ->
                                mapViewRef = mapView
                            },
                            onPartnerClick = { partner, geoPoint ->
                                Log.d("MitraScreen", "onPartnerClick called with: ${partner.businessName}")
                                selectedPartner = partner
                                selectedPartnerPosition = geoPoint
                                Log.d("MitraScreen", "selectedPartner set to: ${selectedPartner?.businessName}")
                            }
                        )
                    }
                    ViewType.LIST -> {
                        // List view (placeholder)
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "List mitra belum tersedia",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Header dengan background putih (di atas map dengan zIndex)
        // Pastikan header tidak menghalangi touch events ke MapView
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f) // Pastikan header di atas map
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(Color.White)
                    .padding(bottom = 60.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    // Row untuk logo dan action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Logo Xetor dengan teks Mitra
                        Column {
                            Image(
                                painter = painterResource(id = R.drawable.text_xetor_warna),
                                contentDescription = "Xetor Logo",
                                modifier = Modifier.height(28.dp),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                text = "Mitra",
                                color = Color.Black.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Row untuk search dan filter buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Search button
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Filter button
                            IconButton(
                                onClick = {
                                    Toast.makeText(context, "Fitur ini belum tersedia", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Switch button Map/List (di luar Box putih, dengan offset)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-60).dp)
                    .padding(horizontal = 20.dp)
                    .zIndex(3f) // Di atas header
            ) {
                ViewTypeToggle(
                    selectedType = selectedViewType,
                    onTypeSelected = { type ->
                        if (type == ViewType.LIST) {
                            Toast.makeText(context, "List mitra belum tersedia", Toast.LENGTH_SHORT).show()
                        } else {
                            selectedViewType = type
                        }
                    }
                )
            }
        } // Tutup Column header

        // Custom Zoom Buttons - pojok kanan, vertikal, di tengah area map (setelah header)
        if (mapViewRef != null && selectedViewType == ViewType.MAP) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(5f) // Di atas header (2f) dan modal (1.5f)
            ) {
                // Hitung tinggi header: Box putih (padding bottom 60dp) + Column (padding vertical 24dp) + logo/text (~40dp) + ViewTypeToggle (40dp dengan offset -60dp overlap)
                // Total efektif header: ~24 + 28 + 13 + 60 = ~125dp
                val headerHeight = 125.dp
                
                // Hitung tinggi zoom controls: 3 button (48dp each) + 2 spacing (4dp each) = 152dp
                val zoomControlsHeight = (48.dp * 3) + (4.dp * 2)
                
                // Hitung tinggi area map yang tersisa (setelah header)
                val mapAreaHeight = maxHeight - headerHeight
                
                // Hitung posisi top untuk tengah vertikal: headerHeight + (tinggi area map / 2) - (tinggi zoom controls / 2)
                // Kurangi lebih banyak untuk naikkan posisi (karena terlalu bawah)
                val centerTop = headerHeight + (mapAreaHeight / 2) - (zoomControlsHeight / 2) - 150.dp
                
                CustomZoomControls(
                    mapView = mapViewRef!!,
                    centerPoint = centerPoint,
                    zoomLevel = zoomLevel,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp, top = centerTop)
                )
            }
        }

        // Partner Info Popup - dengan zIndex di bawah header dan zoom buttons
        selectedPartner?.let { partner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1.5f) // Di atas map (1f) tapi di bawah header (2f) dan zoom buttons (5f)
            ) {
                PartnerInfoPopup(
                    partner = partner,
                    mapView = mapViewRef,
                    markerPosition = selectedPartnerPosition,
                    onDismiss = { 
                        selectedPartner = null
                        selectedPartnerPosition = null
                    },
                    onLihatProfilClick = {
                        Toast.makeText(context, "Profil belum tersedia", Toast.LENGTH_SHORT).show()
                    },
                    onKunjungiClick = {
                        openGoogleMaps(context, partner)
                        selectedPartner = null
                        selectedPartnerPosition = null
                    }
                )
            }
        }

        // Error Snackbar
        if (uiState.errorMessage != null) {
            CustomSnackbar(
                message = uiState.errorMessage ?: "",
                onDismiss = {
                    viewModel.clearError()
                    viewModel.refresh()
                },
                buttonText = "Coba Lagi"
            )
        }
    }
}

@Composable
fun ViewTypeToggle(
    selectedType: ViewType,
    onTypeSelected: (ViewType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F5F5))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Map Option
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (selectedType == ViewType.MAP) GreenPrimary else Color.Transparent
                )
                .clickable { onTypeSelected(ViewType.MAP) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Map",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedType == ViewType.MAP) Color.White else Color.Gray
            )
        }

        // List Option
        Box(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (selectedType == ViewType.LIST) GreenPrimary else Color.Transparent
                )
                .clickable { onTypeSelected(ViewType.LIST) },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "List",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedType == ViewType.LIST) Color.White else Color.Gray
            )
        }
    }
}

@Composable
fun CustomZoomControls(
    mapView: MapView,
    centerPoint: GeoPoint,
    zoomLevel: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Zoom In Button
        Card(
            onClick = {
                mapView.controller.zoomIn()
            },
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Zoom Out Button
        Card(
            onClick = {
                mapView.controller.zoomOut()
            },
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Reset Button dengan animasi smooth - pan dan zoom bersamaan
        Card(
            onClick = {
                // Animate ke center point dan zoom level target secara bersamaan (smooth)
                mapView.controller.animateTo(centerPoint, zoomLevel, 600L)
            },
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_reset),
                    contentDescription = "Reset",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun PartnersMapView(
    partners: List<PublicPartnerResponse>,
    onMapViewReady: (MapView) -> Unit = {},
    onPartnerClick: (PublicPartnerResponse, GeoPoint) -> Unit
) {
    val context = LocalContext.current
    
    // Filter partners yang punya koordinat
    val partnersWithLocation = remember(partners) {
        partners.filter { 
            it.getLatitude() != null && it.getLongitude() != null 
        }
    }

    // Default location: Indonesia center
    val defaultLocation = GeoPoint(-2.5489, 118.0149)
    
    // Calculate center point from all partners (average of all coordinates)
    // Kemudian geser ke selatan untuk mengkompensasi header yang menutupi bagian atas
    val centerPoint = remember(partnersWithLocation) {
        if (partnersWithLocation.isNotEmpty()) {
            var totalLat = 0.0
            var totalLng = 0.0
            partnersWithLocation.forEach { partner ->
                totalLat += partner.getLatitude()!!
                totalLng += partner.getLongitude()!!
            }
            val avgLat = totalLat / partnersWithLocation.size
            val avgLng = totalLng / partnersWithLocation.size
            
            // Geser center point ke utara (tambah latitude) untuk mengkompensasi header
            // Header menutupi bagian atas, jadi kita geser ke utara agar center visual lebih ke bawah
            val latOffset = 0.015 // Offset ke utara (sekitar 1.6 km)
            GeoPoint(avgLat + latOffset, avgLng)
        } else {
            defaultLocation
        }
    }
    
    // Calculate zoom level based on number of partners
    // If only one partner, zoom closer (14.0), if multiple, zoom out a bit (13.0)
    val zoomLevel = remember(partnersWithLocation) {
        if (partnersWithLocation.isEmpty()) {
            10.0
        } else if (partnersWithLocation.size == 1) {
            14.0 // Zoom lebih dekat untuk satu partner
        } else {
            13.0 // Zoom sedikit lebih jauh untuk multiple partners
        }
    }

    var mapViewRef by remember { mutableStateOf<org.osmdroid.views.MapView?>(null) }
    
    // Notify parent when MapView is ready
    LaunchedEffect(mapViewRef) {
        mapViewRef?.let { onMapViewReady(it) }
    }
    
    // Lifecycle management untuk MapView (OSMDroid memerlukan ini)
    val lifecycleOwner = LocalLifecycleOwner.current
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
            
            // Initial resume
            mapView.onResume()
            
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                mapView.onPause()
            }
        } else {
            onDispose { }
        }
    }

    // Refresh map setelah layout selesai untuk memastikan fillMaxSize
    DisposableEffect(Unit) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                mapViewRef?.let { mapView ->
                    mapView.post {
                        mapView.invalidate()
                        mapView.requestLayout()
                    }
                }
            }
        }
        
        // Delay untuk memastikan layout selesai
        handler.postDelayed(runnable, 200)
        handler.postDelayed(runnable, 500)
        handler.postDelayed(runnable, 1000)
        
        onDispose {
            handler.removeCallbacks(runnable)
        }
    }

    // Store callback reference yang bisa di-update
    val callbackRef = remember { mutableStateOf(onPartnerClick) }
    
    // Update callback reference setiap kali onPartnerClick berubah
    LaunchedEffect(onPartnerClick) {
        callbackRef.value = onPartnerClick
    }
    
    val density = LocalDensity.current
    
    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                mapViewRef = this
                
                // Set layout params secara eksplisit untuk memastikan MATCH_PARENT
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                setTileSource(TileSourceFactory.MAPNIK) // OpenStreetMap tiles
                setMultiTouchControls(true)
                
                // Disable built-in zoom controls (kita akan buat custom)
                setBuiltInZoomControls(false)
                setClickable(true)
                
                // Pastikan MapView bisa menerima click events
                isClickable = true
                isFocusable = true
                isFocusableInTouchMode = true
                
                // Test: Log untuk memastikan MapView dibuat
                Log.d("MitraScreen", "MapView created with ${partnersWithLocation.size} partners")
                
                // Load logo untuk marker - dibuat sekali dan di-reuse untuk semua marker
                val logoSize = (48 * ctx.resources.displayMetrics.density).toInt() // 48dp
                val logoBitmap = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(ctx.resources, R.drawable.pin_map_xetor),
                    logoSize,
                    logoSize,
                    true
                )
                val logoDrawable = BitmapDrawable(ctx.resources, logoBitmap)
                
                // Add markers for each partner
                partnersWithLocation.forEach { partner ->
                    val lat = partner.getLatitude()!!
                    val lng = partner.getLongitude()!!
                    val geoPoint = GeoPoint(lat, lng)
                    
                    val marker = Marker(this).apply {
                        position = geoPoint
                        title = partner.businessName
                        snippet = partner.getFullAddress()
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        
                        // Set custom icon dengan logo Xetor
                        setIcon(logoDrawable)
                        
                        // Set click listener - gunakan callback reference yang selalu terbaru
                        setOnMarkerClickListener { clickedMarker, _ ->
                            Log.d("MitraScreen", "Marker clicked: ${partner.businessName}")
                            callbackRef.value(partner, geoPoint)
                            true // Return true untuk menandakan event sudah di-handle
                        }
                    }
                    
                    overlays.add(marker)
                }
                
                // Set padding dan center setelah layout selesai
                // Ini penting agar padding diterapkan dengan benar dan center point bergeser ke bawah
                post {
                    try {
                        // Set padding top untuk mengakomodasi header agar center point lebih ke bawah
                        // Header tinggi ~180dp (termasuk toggle switch), kita set padding top
                        // agar center point benar-benar di bawah header dan modal tidak menutupi header
                        val headerHeightDp = 200f // Dinaikkan ke 200dp agar lebih ke bawah
                        val headerHeightPx = (headerHeightDp * ctx.resources.displayMetrics.density).toInt()
                        setPadding(0, headerHeightPx, 0, 0)
                        
                        // Set zoom dan center SETELAH padding diterapkan
                        controller.setZoom(zoomLevel)
                        controller.setCenter(centerPoint)
                        
                        invalidate()
                        requestLayout()
                    } catch (e: Exception) {
                        // Fallback jika ada masalah dengan resources
                        Log.e("MitraScreen", "Error setting padding: ${e.message}")
                        setPadding(0, 0, 0, 0)
                        controller.setZoom(zoomLevel)
                        controller.setCenter(centerPoint)
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxSize(), // Pastikan map fillMaxSize untuk mencapai bagian bawah
        update = { mapView ->
            // Update map if partners change
            mapView.overlays.clear()
            
            // Pastikan MapView bisa menerima click events
            mapView.isClickable = true
            mapView.isFocusable = true
            mapView.isFocusableInTouchMode = true
            
            // Load logo untuk marker (dibuat sekali dan di-reuse)
            val logoSize = (48 * context.resources.displayMetrics.density).toInt() // 48dp
            val logoBitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(context.resources, R.drawable.pin_map_xetor),
                logoSize,
                logoSize,
                true
            )
            val logoDrawable = BitmapDrawable(context.resources, logoBitmap)
            
            partnersWithLocation.forEach { partner ->
                val lat = partner.getLatitude()!!
                val lng = partner.getLongitude()!!
                val geoPoint = GeoPoint(lat, lng)
                
                val marker = Marker(mapView).apply {
                    position = geoPoint
                    title = partner.businessName
                    snippet = partner.getFullAddress()
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    
                    // Set custom icon dengan logo Xetor
                    setIcon(logoDrawable)
                    
                    // Set click listener - gunakan callback reference yang selalu terbaru
                    setOnMarkerClickListener { clickedMarker, _ ->
                        Log.d("MitraScreen", "Marker clicked: ${partner.businessName}")
                        callbackRef.value(partner, geoPoint)
                        true // Return true untuk menandakan event sudah di-handle
                    }
                }
                
                mapView.overlays.add(marker)
            }
            
            // JANGAN ubah zoom level atau center point di sini
            // Biarkan user mengontrol zoom level dan center point secara manual
            // Hanya update markers saja, jangan ganggu zoom/center yang sudah di-set user
            
            mapView.invalidate()
        }
    )
}

@Composable
fun PartnerInfoPopup(
    partner: PublicPartnerResponse,
    mapView: MapView?,
    markerPosition: GeoPoint?,
    onDismiss: () -> Unit,
    onLihatProfilClick: () -> Unit,
    onKunjungiClick: () -> Unit
) {
    val density = LocalDensity.current
    
    // State untuk menyimpan offset yang akan di-update secara real-time
    var popupOffsetX by remember { mutableStateOf<androidx.compose.ui.unit.Dp?>(null) }
    var popupOffsetY by remember { mutableStateOf<androidx.compose.ui.unit.Dp?>(null) }
    
    // Function untuk update position
    fun updatePosition() {
        if (mapView != null && markerPosition != null) {
            try {
                val projection = mapView.projection
                val markerPixels = projection.toPixels(markerPosition, null)
                // Center horizontally (popup width is ~300dp, so center it)
                val screenX = markerPixels.x
                popupOffsetX = with(density) { screenX.toDp() - 150.dp }
                
                // Position popup above marker - mepet dengan pin
                // Marker anchor is at bottom, jadi kita posisikan modal tepat di atas pin
                val screenY = markerPixels.y
                popupOffsetY = with(density) { screenY.toDp() - 260.dp } // 260dp above marker
                
                Log.d("PartnerInfoPopup", "Position updated: offsetX=${popupOffsetX}, offsetY=${popupOffsetY}")
            } catch (e: Exception) {
                Log.e("PartnerInfoPopup", "Error updating position: ${e.message}")
                popupOffsetX = null
                popupOffsetY = null
            }
        } else {
            Log.d("PartnerInfoPopup", "mapView or markerPosition is null")
            popupOffsetX = null
            popupOffsetY = null
        }
    }
    
    // Update position saat pertama kali modal dibuka - dengan post untuk memastikan MapView siap
    LaunchedEffect(mapView, markerPosition) {
        if (mapView != null && markerPosition != null) {
            // Gunakan post untuk memastikan MapView projection sudah siap
            mapView.post {
                updatePosition()
            }
        }
    }
    
    // Update position secara real-time saat map digeser/zoom menggunakan MapListener dan polling
    DisposableEffect(mapView, markerPosition) {
        if (mapView != null && markerPosition != null) {
            // MapListener untuk update saat scroll/zoom
            val mapListener = object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    mapView.post {
                        updatePosition()
                    }
                    return false
                }
                
                override fun onZoom(event: ZoomEvent?): Boolean {
                    mapView.post {
                        updatePosition()
                    }
                    return false
                }
            }
            
            mapView.addMapListener(mapListener)
            
            // Polling sebagai backup untuk memastikan update smooth (16ms = 60fps)
            val handler = android.os.Handler(android.os.Looper.getMainLooper())
            val runnable = object : Runnable {
                override fun run() {
                    updatePosition()
                    handler.postDelayed(this, 16) // Update setiap 16ms (60fps) untuk smooth animation
                }
            }
            handler.post(runnable)
            
            onDispose {
                mapView.removeMapListener(mapListener)
                handler.removeCallbacks(runnable)
            }
        } else {
            onDispose { }
        }
    }
    
    // Local variables untuk smart cast
    val offsetX = popupOffsetX
    val offsetY = popupOffsetY
    
    // Box untuk modal - tidak ada clickable agar map bisa digeser saat modal terbuka
    // Selalu gunakan TopStart untuk menghindari glitch saat contentAlignment berubah
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1.5f), // Di atas map (1f) tapi di bawah header (2f) dan zoom buttons (5f)
        contentAlignment = Alignment.TopStart
    ) {
        // Jangan render modal sampai offset sudah dihitung untuk menghindari glitch
        if (offsetX != null && offsetY != null) {
            Column(
                modifier = Modifier
                    .offset(x = offsetX, y = offsetY)
                    .widthIn(max = 300.dp)
            ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) { }, // Mencegah klik pada card menutup modal
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                // Header dengan close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = partner.businessName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color.Gray,
                            containerColor = Color.Transparent
                        )
                    ) {
                        Text(
                            text = "✕",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Address
                Text(
                    text = partner.getFullAddress().ifEmpty { "Alamat tidak tersedia" },
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Two buttons side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Button Lihat Profil
                    OutlinedButton(
                        onClick = onLihatProfilClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            Color.Gray.copy(alpha = 0.3f)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_profile_view),
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Lihat Profil",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Button Kunjungi
                    Button(
                        onClick = onKunjungiClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_navigation),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Kunjungi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                }
            }
            
                // Arrow pointing down - setelah Card
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(20.dp, 12.dp)
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val path = Path().apply {
                            moveTo(size.width / 2, size.height) // Mulai dari bawah (titik tengah)
                            lineTo(0f, 0f) // Ke kiri atas
                            lineTo(size.width, 0f) // Ke kanan atas
                            close()
                        }
                        drawPath(
                            path = path,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

fun openGoogleMaps(context: android.content.Context, partner: PublicPartnerResponse) {
    val latitude = partner.getLatitude()
    val longitude = partner.getLongitude()
    val address = partner.getFullAddress()

    if (latitude != null && longitude != null) {
        // Try to open in Google Maps app first
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(partner.businessName)})")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback to web browser
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri)
            context.startActivity(webIntent)
        }
    } else if (address.isNotEmpty()) {
        // Fallback: use address if coordinates not available
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(address)}")
        val webIntent = Intent(Intent.ACTION_VIEW, webUri)
        context.startActivity(webIntent)
    } else {
        Toast.makeText(context, "Lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
    }
}
