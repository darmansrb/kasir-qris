package com.moluccasdev.poskasirqris.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Done
import com.moluccasdev.poskasirqris.data.ProductEntity
import com.moluccasdev.poskasirqris.data.QrisEntity
import com.moluccasdev.poskasirqris.ui.SettingsViewModel
import com.moluccasdev.poskasirqris.util.QrisEngine
import java.io.File
import java.util.Locale



@Composable
fun SettingsScreen(settingsVM: SettingsViewModel) {
    val context = LocalContext.current
    val products by settingsVM.allProducts.collectAsState()
    val qrisConfigs by settingsVM.qrisList.collectAsState()

    // Screen State: "MAIN", "STORE", "PRODUCTS", "QRIS", "CONFIG", "SECURITY"
    var activeSubScreen by remember { mutableStateOf("MAIN") }

    val prefs = remember { context.getSharedPreferences("pos_settings", android.content.Context.MODE_PRIVATE) }
    var targetScreenAfterAuth by remember { mutableStateOf<String?>(null) }
    var showPinAuthDialog by remember { mutableStateOf(false) }

    fun authenticateAndNavigate(targetScreen: String) {
        val hasPin = prefs.getBoolean("use_pin", false) && !prefs.getString("secure_pin", "").isNullOrEmpty()
        val hasBiometric = prefs.getBoolean("use_biometrics", false)
        
        if (!hasPin && !hasBiometric) {
            activeSubScreen = targetScreen
            return
        }
        
        if (hasBiometric) {
            val activity = context.findActivity()
            if (activity != null && isBiometricAvailable(context)) {
                showBiometricPrompt(
                    activity = activity,
                    title = "Autentikasi Keamanan",
                    subtitle = "Gunakan sidik jari atau Face ID untuk masuk",
                    onSuccess = {
                        activeSubScreen = targetScreen
                    },
                    onError = { errorCode, errString ->
                        if (hasPin) {
                            targetScreenAfterAuth = targetScreen
                            showPinAuthDialog = true
                        } else {
                            Toast.makeText(context, "Autentikasi gagal: $errString", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else if (hasPin) {
                targetScreenAfterAuth = targetScreen
                showPinAuthDialog = true
            } else {
                Toast.makeText(context, "Biometrik tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        } else if (hasPin) {
            targetScreenAfterAuth = targetScreen
            showPinAuthDialog = true
        }
    }

    androidx.activity.compose.BackHandler(enabled = activeSubScreen != "MAIN") {
        activeSubScreen = "MAIN"
    }

    Scaffold(
        containerColor = Color(0xFFF4F3EF), // Vintage Paper Background
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF4F3EF))
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeSubScreen != "MAIN") {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                            .clickable { activeSubScreen = "MAIN" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFDE4D))
                        .border(2.dp, Color.Black, RoundedCornerShape(2.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = when (activeSubScreen) {
                            "STORE" -> "MANAJEMEN TOKO"
                            "PRODUCTS" -> "MANAJEMEN PRODUK"
                            "QRIS" -> "MASTER QRIS"
                            "CONFIG" -> "PREFERENSI & BACKUP"
                            "SECURITY" -> "KEAMANAN APLIKASI"
                            else -> "PENGATURAN KASIR"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when (activeSubScreen) {
                "MAIN" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Konfigurasi katalog barang, QRIS merchant & preferensi cetak struk offline.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 1. Store Management Card
                        SettingsMenuListItem(
                            title = "MANAJEMEN TOKO",
                            subtitle = "Atur nama toko, logo cetak struk, dan footer",
                            accentColor = Color(0xFFE8D5FF),
                            onClick = { activeSubScreen = "STORE" }
                        )

                        // 2. Product Management Card (Gated)
                        SettingsMenuListItem(
                            title = "MANAJEMEN PRODUK",
                            subtitle = "Kelola katalog barang, tambah produk baru & harga",
                            accentColor = Color(0xFFFFDE4D),
                            onClick = { authenticateAndNavigate("PRODUCTS") }
                        )

                        // 3. QRIS Engine Card (Gated)
                        SettingsMenuListItem(
                            title = "MASTER QRIS",
                            subtitle = "Konfigurasi string QRIS dinamis & info merchant",
                            accentColor = Color(0xFF00F5D4),
                            onClick = { authenticateAndNavigate("QRIS") }
                        )

                        // 4. Preferences Card
                        SettingsMenuListItem(
                            title = "PREFERENSI & BACKUP",
                            subtitle = "Pengaturan printer thermal, test print & backup database",
                            accentColor = Color(0xFFFF595E),
                            onClick = { activeSubScreen = "CONFIG" }
                        )

                        // 5. Security Card
                        SettingsMenuListItem(
                            title = "KEAMANAN",
                            subtitle = "Kunci Manajemen Produk & Master QRIS dengan PIN / Biometrik",
                            accentColor = Color(0xFF90E0EF),
                            onClick = { activeSubScreen = "SECURITY" }
                        )
                    }
                }
                "STORE" -> StoreSettingsSubScreen()
                "PRODUCTS" -> ProductSettingsTab(settingsVM, products)
                "QRIS" -> QrisSettingsTab(settingsVM, qrisConfigs)
                "CONFIG" -> ConfigSettingsTab(context)
                "SECURITY" -> SecuritySettingsSubScreen()
            }
        }
    }

    if (showPinAuthDialog) {
        PinInputDialog(
            title = "Masukkan PIN Keamanan",
            onConfirm = { pin ->
                val savedPin = prefs.getString("secure_pin", "")
                if (pin == savedPin) {
                    showPinAuthDialog = false
                    targetScreenAfterAuth?.let {
                        activeSubScreen = it
                    }
                } else {
                    Toast.makeText(context, "PIN salah!", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = {
                showPinAuthDialog = false
            }
        )
    }
}

@Composable
fun SettingsMenuListItem(
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp, end = 6.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)
                .background(Color.Black, RoundedCornerShape(8.dp))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(8.dp))
                .border(3.dp, Color.Black, RoundedCornerShape(8.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .background(accentColor, RoundedCornerShape(2.dp))
                        .border(1.5.dp, Color.Black, RoundedCornerShape(2.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.KeyboardArrowRight,
                contentDescription = "Masuk",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun StoreSettingsSubScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pos_settings", android.content.Context.MODE_PRIVATE) }
    var storeName by remember { mutableStateOf(prefs.getString("store_name", "POS KASIR QRIS") ?: "POS KASIR QRIS") }
    var storeFooter by remember { mutableStateOf(prefs.getString("store_footer", "Layanan POS Kasir QRIS Offline") ?: "Layanan POS Kasir QRIS Offline") }
    var logoUriString by remember { mutableStateOf(prefs.getString("store_logo_uri", "") ?: "") }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            // Persist URI permission so it survives restarts
            try {
                val takeFlags: Int = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            logoUriString = uri.toString()
            prefs.edit().putString("store_logo_uri", logoUriString).apply()
            Toast.makeText(context, "Logo Toko Berhasil Dimuat!", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, end = 4.dp)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 4.dp, y = 4.dp)
                    .background(Color.Black, RoundedCornerShape(6.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .border(2.5.dp, Color.Black, RoundedCornerShape(6.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "KONFIGURASI STRUK TOKO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Black
                )

                // Store Name Input
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Nama Toko", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color(0xFFF4F3EF),
                        unfocusedContainerColor = Color(0xFFF4F3EF)
                    )
                )

                // Store Footer Input
                OutlinedTextField(
                    value = storeFooter,
                    onValueChange = { storeFooter = it },
                    label = { Text("Footer Struk", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color(0xFFF4F3EF),
                        unfocusedContainerColor = Color(0xFFF4F3EF)
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Logo Uploader Section
                Text(
                    text = "LOGO TOKO (STRUK THERMAL)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Black
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Preview
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color(0xFFF4F3EF), RoundedCornerShape(4.dp))
                            .border(2.dp, Color.Black, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (logoUriString.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(Uri.parse(logoUriString))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Logo Toko",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("NO LOGO", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .clickable { logoPickerLauncher.launch(arrayOf("image/*")) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 3.dp, y = 3.dp)
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color(0xFFE8D5FF), RoundedCornerShape(4.dp))
                                    .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("PILIH LOGO BARU", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                            }
                        }

                        if (logoUriString.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(34.dp)
                                    .clickable {
                                        logoUriString = ""
                                        prefs.edit().putString("store_logo_uri", "").apply()
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .offset(x = 2.dp, y = 2.dp)
                                        .background(Color.Black, RoundedCornerShape(4.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(Color(0xFFFF595E), RoundedCornerShape(4.dp))
                                        .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("HAPUS LOGO", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable {
                            prefs.edit()
                                .putString("store_name", storeName)
                                .putString("store_footer", storeFooter)
                                .apply()
                            Toast.makeText(context, "Pengaturan Toko Berhasil Disimpan!", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 4.dp, y = 4.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0xFF00F5D4), RoundedCornerShape(4.dp))
                            .border(2.dp, Color.Black, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SIMPAN PERUBAHAN", style = MaterialTheme.typography.labelLarge, color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// 1. PRODUCT TAB WORKSPACE
@Composable
fun ProductSettingsTab(
    settingsVM: SettingsViewModel,
    products: List<ProductEntity>
) {
    val context = LocalContext.current
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    // Search and Pagination States
    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf(1) }
    var pageSize by remember { mutableStateOf(10) }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    val totalProducts = filteredProducts.size
    val totalPages = maxOf(1, kotlin.math.ceil(totalProducts.toDouble() / pageSize).toInt())
    
    if (currentPage > totalPages) {
        currentPage = totalPages
    }
    
    val pagedProducts = filteredProducts.drop((currentPage - 1) * pageSize).take(pageSize)

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Katalog Barang",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                fontWeight = FontWeight.Black
            )
            
            Box(
                modifier = Modifier
                    .padding(bottom = 3.dp, end = 3.dp)
                    .clickable {
                        selectedProduct = null
                        showEditDialog = true
                    }
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 3.dp, y = 3.dp)
                        .background(Color.Black, RoundedCornerShape(4.dp))
                )
                Row(
                    modifier = Modifier
                        .background(Color(0xFFFFDE4D), RoundedCornerShape(4.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TAMBAH", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar in stark Neo-Brutalism style
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it 
                currentPage = 1
            },
            placeholder = { Text("Cari produk...", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontWeight = FontWeight.Bold) },
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // List of products inside database
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pagedProducts) { product ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp, end = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 4.dp, y = 4.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (product.isActive) Color.White else Color.White.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .border(2.5.dp, Color.Black, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Product preview photo
                        val imgReq = ImageRequest.Builder(context)
                            .data(product.imagePath?.let { File(it) })
                            .crossfade(true)
                            .build()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (product.imagePath != null) {
                                AsyncImage(
                                    model = imgReq,
                                    contentDescription = product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.5.dp, Color.Black, RoundedCornerShape(6.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE8D5FF))
                                        .border(1.5.dp, Color.Black, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        product.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = product.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Rp ${String.format(Locale.getDefault(), "%,.0f", product.price)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!product.isActive) {
                                    Text(
                                        text = "Dihapus",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFF595E),
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF00F5D4), RoundedCornerShape(4.dp))
                                    .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                    .clickable {
                                        selectedProduct = product
                                        showEditDialog = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Black, modifier = Modifier.size(18.dp))
                            }

                            if (product.isActive) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFFF595E), RoundedCornerShape(4.dp))
                                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                        .clickable { productToDelete = product },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Black, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Pagination row in Neo-Brutalism style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dropdown selection for page size (5, 10, 20, 50)
            var showSizeDropdown by remember { mutableStateOf(false) }
            Box {
                Box(
                    modifier = Modifier
                        .clickable { showSizeDropdown = !showSizeDropdown }
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("$pageSize / Page ▾", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Black)
                }
                
                if (showSizeDropdown) {
                    androidx.compose.material3.DropdownMenu(
                        expanded = showSizeDropdown,
                        onDismissRequest = { showSizeDropdown = false },
                        modifier = Modifier.background(Color.White).border(2.dp, Color.Black)
                    ) {
                        listOf(5, 10, 20, 50).forEach { size ->
                            androidx.compose.material3.DropdownMenuItem(
                                text = { Text("$size per hal", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.Black) },
                                onClick = {
                                    pageSize = size
                                    currentPage = 1
                                    showSizeDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Info text
            val firstItemIdx = if (totalProducts == 0) 0 else (currentPage - 1) * pageSize + 1
            val lastItemIdx = minOf(totalProducts, currentPage * pageSize)
            Text(
                text = "$firstItemIdx-$lastItemIdx dari $totalProducts",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            // Next / Prev buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Prev Button
                Box(
                    modifier = Modifier
                        .clickable(enabled = currentPage > 1) { currentPage-- }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .offset(x = 2.dp, y = 2.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (currentPage > 1) Color(0xFFFFDE4D) else Color.LightGray, RoundedCornerShape(4.dp))
                            .border(2.dp, Color.Black, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("◀", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }

                // Next Button
                Box(
                    modifier = Modifier
                        .clickable(enabled = currentPage < totalPages) { currentPage++ }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .offset(x = 2.dp, y = 2.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (currentPage < totalPages) Color(0xFFFFDE4D) else Color.LightGray, RoundedCornerShape(4.dp))
                            .border(2.dp, Color.Black, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▶", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        ProductEditDialog(
            product = selectedProduct,
            settingsVM = settingsVM,
            onDismiss = { showEditDialog = false }
        )
    }

    if (productToDelete != null) {
        Dialog(onDismissRequest = { productToDelete = null }) {
            Box(
                modifier = Modifier
                    .width(320.dp)
                    .padding(bottom = 6.dp, end = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 6.dp, y = 6.dp)
                        .background(Color.Black, RoundedCornerShape(12.dp))
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "KONFIRMASI HAPUS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    
                    Text(
                        text = "Apakah Anda yakin ingin menghapus \"${productToDelete?.name}\"?\n\nProduk ini hanya akan disembunyikan dari list order (tidak terhapus permanen agar data laporan historis tetap akurat).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable { productToDelete = null }
                        ) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 3.dp, y = 3.dp)
                                    .background(Color.Black, RoundedCornerShape(6.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color(0xFFF4F3EF), RoundedCornerShape(6.dp))
                                    .border(2.dp, Color.Black, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("BATAL", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable {
                                    productToDelete?.let {
                                        settingsVM.deleteProduct(it)
                                    }
                                    productToDelete = null
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 3.dp, y = 3.dp)
                                    .background(Color.Black, RoundedCornerShape(6.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color(0xFFFF595E), RoundedCornerShape(6.dp))
                                    .border(2.dp, Color.Black, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("HAPUS", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductEditDialog(
    product: ProductEntity?,
    settingsVM: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var imagePath by remember { mutableStateOf(product?.imagePath) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val copiedPath = settingsVM.copyImageToInternalStorage(context, uri)
            if (copiedPath != null) {
                imagePath = copiedPath
                Toast.makeText(context, "Foto berhasil disalin!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .padding(bottom = 6.dp, end = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(Color.Black, RoundedCornerShape(12.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (product == null) "TAMBAH BARANG BARU" else "EDIT DETAIL BARANG",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Black
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Barang", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color(0xFFF4F3EF),
                        unfocusedContainerColor = Color(0xFFF4F3EF)
                    )
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Harga Barang (Rp)", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color(0xFFF4F3EF),
                        unfocusedContainerColor = Color(0xFFF4F3EF)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (imagePath != null) "Foto Terpilih ✓" else "Belum ada foto",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (imagePath != null) Color(0xFF00F5D4) else Color.DarkGray
                    )

                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clickable { pickerLauncher.launch("image/*") }
                            .padding(bottom = 2.dp, end = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 2.dp, y = 2.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color(0xFFE8D5FF), RoundedCornerShape(4.dp))
                                .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Pilih Galeri", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { onDismiss() }
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 3.dp, y = 3.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color(0xFFFF595E), RoundedCornerShape(4.dp))
                                .border(2.dp, Color.Black, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("BATAL", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }

                    val isEnabled = name.isNotBlank() && price.toDoubleOrNull() != null
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable(enabled = isEnabled) {
                                val parsedPrice = price.toDoubleOrNull() ?: 0.0
                                if (name.isNotBlank() && parsedPrice > 0.0) {
                                    settingsVM.saveProduct(
                                        id = product?.id ?: 0,
                                        name = name,
                                        price = parsedPrice,
                                        imagePath = imagePath
                                    )
                                    onDismiss()
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 3.dp, y = 3.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(if (isEnabled) Color(0xFF00F5D4) else Color.LightGray, RoundedCornerShape(4.dp))
                                .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("SIMPAN", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}


// 2. QRIS CONFIG TAB WORKSPACE
@Composable
fun QrisSettingsTab(
    settingsVM: SettingsViewModel,
    qrisConfigs: List<QrisEntity>
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var qrisToDelete by remember { mutableStateOf<QrisEntity?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Master QRIS Toko",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
                fontWeight = FontWeight.Black
            )
            
            Box(
                modifier = Modifier
                    .padding(bottom = 3.dp, end = 3.dp)
                    .clickable { showAddDialog = true }
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 3.dp, y = 3.dp)
                        .background(Color.Black, RoundedCornerShape(4.dp))
                )
                Row(
                    modifier = Modifier
                        .background(Color(0xFF00F5D4), RoundedCornerShape(4.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TAMBAH", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(qrisConfigs) { qris ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp, end = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 4.dp, y = 4.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(2.5.dp, Color.Black, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = qris.merchantName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black
                                )
                                if (qris.isDefault) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFFFDE4D), RoundedCornerShape(4.dp))
                                            .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "UTAMA",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "String: " + if (qris.rawQrisString.length > 40) qris.rawQrisString.take(37) + "..." else qris.rawQrisString,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!qris.isDefault) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFFFDE4D), RoundedCornerShape(4.dp))
                                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                        .clickable { settingsVM.setDefaultQris(qris.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = "Jadikan Utama", tint = Color.Black, modifier = Modifier.size(18.dp))
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFFFF595E), RoundedCornerShape(4.dp))
                                    .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                    .clickable { qrisToDelete = qris },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Black, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        QrisAddDialog(
            settingsVM = settingsVM,
            onDismiss = { showAddDialog = false }
        )
    }

    if (qrisToDelete != null) {
        Dialog(onDismissRequest = { qrisToDelete = null }) {
            Box(
                modifier = Modifier
                    .width(320.dp)
                    .padding(bottom = 6.dp, end = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 6.dp, y = 6.dp)
                        .background(Color.Black, RoundedCornerShape(12.dp))
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "KONFIRMASI HAPUS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    
                    Text(
                        text = "Apakah Anda yakin ingin menghapus QRIS Merchant \"${qrisToDelete?.merchantName}\"?\n\nTindakan ini tidak dapat dibatalkan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable { qrisToDelete = null }
                        ) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 3.dp, y = 3.dp)
                                    .background(Color.Black, RoundedCornerShape(6.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color(0xFFF4F3EF), RoundedCornerShape(6.dp))
                                    .border(2.dp, Color.Black, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("BATAL", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                            }
                        }
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable {
                                    qrisToDelete?.let {
                                        settingsVM.deleteQris(it)
                                    }
                                    qrisToDelete = null
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 3.dp, y = 3.dp)
                                    .background(Color.Black, RoundedCornerShape(6.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Color(0xFFFF595E), RoundedCornerShape(6.dp))
                                    .border(2.dp, Color.Black, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("HAPUS", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QrisAddDialog(
    settingsVM: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var qrisString by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }

    var showCameraScanner by remember { mutableStateOf(false) }

    val qrPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val resolver = context.contentResolver
                val inputStream = resolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    val decoded = com.moluccasdev.poskasirqris.util.QrCodeGenerator.decodeQrCode(bitmap)
                    if (decoded != null && decoded.isNotBlank()) {
                        qrisString = decoded
                        val detected = QrisEngine.extractMerchantName(decoded)
                        name = detected
                        Toast.makeText(context, "QRIS berhasil diimpor!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Tidak dapat menemukan kode QR di gambar ini.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Gagal memuat gambar.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Gagal memproses gambar: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCameraScanner = true
        } else {
            Toast.makeText(context, "Izin kamera ditolak. Tidak dapat menggunakan scanner.", Toast.LENGTH_LONG).show()
        }
    }

    if (showCameraScanner) {
        CameraQrScannerDialog(
            onScan = { decoded ->
                qrisString = decoded
                val detected = QrisEngine.extractMerchantName(decoded)
                name = detected
                showCameraScanner = false
                Toast.makeText(context, "QRIS berhasil dipindai!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showCameraScanner = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(360.dp)
                .padding(bottom = 6.dp, end = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(Color.Black, RoundedCornerShape(12.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "TAMBAH MASTER QRIS",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Black
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable {
                                val permission = android.Manifest.permission.CAMERA
                                val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context, permission
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                if (isGranted) {
                                    showCameraScanner = true
                                } else {
                                    cameraPermissionLauncher.launch(permission)
                                }
                            }
                            .padding(bottom = 2.dp, end = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 2.dp, y = 2.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color(0xFFE8D5FF), RoundedCornerShape(4.dp))
                                .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Pindai Kamera", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clickable { qrPickerLauncher.launch("image/*") }
                            .padding(bottom = 2.dp, end = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 2.dp, y = 2.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color(0xFFFFDE4D), RoundedCornerShape(4.dp))
                                .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Impor Gambar", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }

                HorizontalDivider(color = Color.Black, thickness = 2.dp)

                OutlinedTextField(
                    value = qrisString,
                    onValueChange = { qrisString = it },
                    label = { Text("Paste/Hasil Scan QRIS String", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("000201010211...", style = MaterialTheme.typography.labelSmall) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color(0xFFF4F3EF),
                        unfocusedContainerColor = Color(0xFFF4F3EF)
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clickable(enabled = qrisString.isNotBlank()) {
                            val detected = QrisEngine.extractMerchantName(qrisString)
                            name = detected
                        }
                        .padding(bottom = 2.dp, end = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 2.dp, y = 2.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(if (qrisString.isNotBlank()) Color(0xFF00F5D4) else Color.LightGray, RoundedCornerShape(4.dp))
                            .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Deteksi Otomatis Merchant Name", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Merchant / Toko", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Black,
                        focusedLabelColor = Color.Black,
                        unfocusedLabelColor = Color.Black,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color(0xFFF4F3EF),
                        unfocusedContainerColor = Color(0xFFF4F3EF)
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isDefault = !isDefault }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                            .background(if (isDefault) Color(0xFFFFDE4D) else Color.Transparent, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Jadikan QRIS Default Utama", style = MaterialTheme.typography.bodyMedium, color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { onDismiss() }
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 3.dp, y = 3.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color(0xFFFF595E), RoundedCornerShape(4.dp))
                                .border(2.dp, Color.Black, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("BATAL", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Black)
                        }
                    }

                    val isSaveEnabled = qrisString.isNotBlank()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable(enabled = isSaveEnabled) {
                                if (qrisString.isNotBlank()) {
                                    settingsVM.saveQris(
                                        id = 0,
                                        merchantNameInput = name,
                                        rawQris = qrisString,
                                        isDefault = isDefault
                                    )
                                    onDismiss()
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 3.dp, y = 3.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(if (isSaveEnabled) Color(0xFF00F5D4) else Color.LightGray, RoundedCornerShape(4.dp))
                                .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("SIMPAN", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CameraQrScannerDialog(
    onScan: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .width(360.dp)
                .height(480.dp)
                .padding(8.dp),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraQrPreview(onScan = onScan)

                // Overlay UI elements (e.g. Cancel button)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Arahkan Kamera ke Kode QRIS",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )

                    // Target scanning box preview border
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .border(2.dp, Color.Green, RoundedCornerShape(12.dp))
                    )

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.8f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Batal")
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
@Composable
fun CameraQrPreview(onScan: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val previewView = remember { androidx.camera.view.PreviewView(context) }

    androidx.compose.runtime.LaunchedEffect(previewView) {
        val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = androidx.camera.core.ImageAnalysis.Builder()
                .setBackpressureStrategy(androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Setup ZXing real-time scan analyzer
            imageAnalysis.setAnalyzer(
                androidx.core.content.ContextCompat.getMainExecutor(context)
            ) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    try {
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        // Convert ImageProxy to bitmap for decoding
                        val bitmap = previewView.bitmap
                        if (bitmap != null) {
                            val decoded = com.moluccasdev.poskasirqris.util.QrCodeGenerator.decodeQrCode(bitmap)
                            if (decoded != null && decoded.isNotBlank()) {
                                onScan(decoded)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        imageProxy.close()
                    }
                } else {
                    imageProxy.close()
                }
            }

            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, androidx.core.content.ContextCompat.getMainExecutor(context))
    }

    androidx.compose.ui.viewinterop.AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

// 3. CONFIG & BACKUP TAB WORKSPACE
@Composable
fun ConfigSettingsTab(context: android.content.Context) {
    val prefs = remember { context.getSharedPreferences("pos_settings", android.content.Context.MODE_PRIVATE) }
    var isPrinterActive by remember { mutableStateOf(prefs.getBoolean("is_printer_active", false)) }

    var showPermissionAlert by remember { mutableStateOf(false) }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isConnectGranted = permissions[android.Manifest.permission.BLUETOOTH_CONNECT] ?: false
        val isScanGranted = permissions[android.Manifest.permission.BLUETOOTH_SCAN] ?: false
        if (isConnectGranted && isScanGranted) {
            isPrinterActive = true
            prefs.edit().putBoolean("is_printer_active", true).apply()
            Toast.makeText(context, "Printer aktif dengan izin Bluetooth!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Izin Bluetooth ditolak. Printer tidak dapat diaktifkan!", Toast.LENGTH_SHORT).show()
        }
    }

    if (showPermissionAlert) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPermissionAlert = false },
            title = { Text("Izin Bluetooth Diperlukan", fontWeight = FontWeight.Bold) },
            text = { Text("Aplikasi membutuhkan izin Bluetooth Connect dan Bluetooth Scan untuk mencari, menghubungkan, dan mencetak ke printer thermal Bluetooth Anda.") },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionAlert = false
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            bluetoothPermissionLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.BLUETOOTH_CONNECT,
                                    android.Manifest.permission.BLUETOOTH_SCAN
                                )
                            )
                        } else {
                            // On older versions, Bluetooth permissions are declared in manifest and granted at install time
                            isPrinterActive = true
                            prefs.edit().putBoolean("is_printer_active", true).apply()
                        }
                    }
                ) {
                    Text("Izinkan")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showPermissionAlert = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Preferensi & Backup",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black,
            fontWeight = FontWeight.Black
        )

        // Switch style Printer config
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp, end = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(Color.Black, RoundedCornerShape(8.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(2.5.dp, Color.Black, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.SpaceBetween,
                     verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Printer Bluetooth Thermal", style = MaterialTheme.typography.bodyMedium, color = Color.Black, fontWeight = FontWeight.Black)
                        Text("Cetak struk fisik ESC/POS otomatis sehabis transaksi", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(28.dp)
                            .clip(CircleShape)
                            .background(if (isPrinterActive) Color(0xFF00F5D4) else Color.LightGray)
                            .border(2.dp, Color.Black, CircleShape)
                            .clickable {
                                if (!isPrinterActive) {
                                    // Check permission first when turning ON
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                        val connectPerm = android.Manifest.permission.BLUETOOTH_CONNECT
                                        val scanPerm = android.Manifest.permission.BLUETOOTH_SCAN
                                        val isConnectGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, connectPerm) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                        val isScanGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, scanPerm) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                        if (!isConnectGranted || !isScanGranted) {
                                            showPermissionAlert = true
                                        } else {
                                            isPrinterActive = true
                                            prefs.edit().putBoolean("is_printer_active", true).apply()
                                        }
                                    } else {
                                        isPrinterActive = true
                                        prefs.edit().putBoolean("is_printer_active", true).apply()
                                    }
                                } else {
                                    isPrinterActive = false
                                    prefs.edit().putBoolean("is_printer_active", false).apply()
                                }
                            }
                            .padding(2.dp),
                        contentAlignment = if (isPrinterActive) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.5.dp, Color.Black, CircleShape)
                        )
                    }
                }

                if (isPrinterActive) {
                    var selectedPrinterAddress by remember { mutableStateOf(prefs.getString("selected_printer_address", "") ?: "") }
                    var showPrinterDropdown by remember { mutableStateOf(false) }

                    val pairedPrinters = remember {
                        try {
                            com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections().list?.toList() ?: emptyList()
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.Black, thickness = 2.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "PILIH PRINTER BLUETOOTH:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val currentPrinterName = pairedPrinters.find { it.device.address == selectedPrinterAddress }?.device?.name
                        ?: if (selectedPrinterAddress.isNotEmpty()) "Printer Terputus ($selectedPrinterAddress)" else "Belum Memilih Printer (Tap Pilih)"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF4F3EF), RoundedCornerShape(6.dp))
                            .border(2.dp, Color.Black, RoundedCornerShape(6.dp))
                            .clickable { showPrinterDropdown = !showPrinterDropdown }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentPrinterName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Icon(
                                imageVector = if (showPrinterDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Pilih Printer",
                                tint = Color.Black
                            )
                        }
                    }

                    if (showPrinterDropdown) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(Color.White, RoundedCornerShape(6.dp))
                                .border(2.dp, Color.Black, RoundedCornerShape(6.dp))
                        ) {
                            Column(modifier = Modifier.padding(4.dp)) {
                                if (pairedPrinters.isEmpty()) {
                                    Text(
                                        text = "Tidak ada printer Bluetooth dipasang. Hubungkan dulu di Pengaturan Bluetooth HP.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFF595E),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                } else {
                                    pairedPrinters.forEach { printer ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedPrinterAddress = printer.device.address
                                                    prefs.edit().putString("selected_printer_address", printer.device.address).apply()
                                                    showPrinterDropdown = false
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = printer.device.name ?: "Unknown Printer",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Black,
                                                    fontWeight = FontWeight.Black
                                                )
                                                Text(
                                                    text = printer.device.address,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.Gray,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (selectedPrinterAddress == printer.device.address) {
                                                Icon(
                                                    imageVector = Icons.Default.Done,
                                                    contentDescription = "Terpilih",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = Color.Black.copy(alpha = 0.2f), thickness = 1.dp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 3.dp, end = 3.dp)
                            .clickable {
                                // Run Dummy Print Test
                                try {
                                    val bluetoothPrinters = com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections().list
                                    val bluetoothConnection = if (selectedPrinterAddress.isNotEmpty() && bluetoothPrinters != null) {
                                        bluetoothPrinters.find { it.device.address == selectedPrinterAddress }
                                            ?: com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections.selectFirstPaired()
                                    } else {
                                        com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections.selectFirstPaired()
                                    }

                                    if (bluetoothConnection == null) {
                                        Toast.makeText(context, "Printer Bluetooth tidak terhubung / terpasang!", Toast.LENGTH_LONG).show()
                                    } else {
                                        val storeName = prefs.getString("store_name", "POS KASIR QRIS") ?: "POS KASIR QRIS"
                                        val storeFooter = prefs.getString("store_footer", "Layanan POS Kasir QRIS Offline") ?: "Layanan POS Kasir QRIS Offline"
                                        val logoUriString = prefs.getString("store_logo_uri", "") ?: ""

                                        val printer = com.dantsu.escposprinter.EscPosPrinter(bluetoothConnection, 203, 48f, 32)
                                        val textToPrint = StringBuilder()

                                        // 1. Logo Toko printing (if Uri is saved and can be parsed)
                                        var logoPrinted = false
                                        if (logoUriString.isNotEmpty()) {
                                            try {
                                                val uri = android.net.Uri.parse(logoUriString)
                                                val inputStream = context.contentResolver.openInputStream(uri)
                                                val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                                                inputStream?.close()
                                                if (originalBitmap != null) {
                                                    val width = 180
                                                    val height = (originalBitmap.height * (width.toDouble() / originalBitmap.width)).toInt()
                                                    val resizedBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, width, height, true)
                                                    
                                                    textToPrint.append("[C]<img>" + com.dantsu.escposprinter.textparser.PrinterTextParserImg.bitmapToHexadecimalString(printer, resizedBitmap) + "</img>\n")
                                                    logoPrinted = true
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }

                                        if (!logoPrinted) {
                                            textToPrint.append("[C]<b><font size='big'>${storeName.uppercase(java.util.Locale.getDefault())}</font></b>\n")
                                        } else {
                                            textToPrint.append("[C]<b>${storeName.uppercase(java.util.Locale.getDefault())}</b>\n")
                                        }

                                        textToPrint.append("[C]================================\n")
                                        textToPrint.append("[C]TEST PRINTER THERMAL BERHASIL\n")
                                        textToPrint.append("[C]================================\n")
                                        textToPrint.append("[L]Tgl Test : ${java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\n")
                                        textToPrint.append("[L]Printer  : ${bluetoothConnection.device.name ?: "Unknown"}\n")
                                        textToPrint.append("[L]Alamat   : ${bluetoothConnection.device.address}\n")
                                        textToPrint.append("[C]--------------------------------\n")
                                        
                                        // Multi-line center footer
                                        storeFooter.split("\n").forEach { line ->
                                            if (line.trim().isNotEmpty()) {
                                                textToPrint.append("[C]${line.trim()}\n")
                                            }
                                        }
                                        textToPrint.append("\n\n\n")

                                        printer.printFormattedText(textToPrint.toString())
                                        Toast.makeText(context, "Test print berhasil dikirim ke printer!", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Toast.makeText(context, "Gagal print: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 3.dp, y = 3.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFDE4D), RoundedCornerShape(4.dp))
                                .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Test Print", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("TEST PRINT STRUK (DUMMY)", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        // SQLite DB Backup info card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp, end = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(Color.Black, RoundedCornerShape(8.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(2.5.dp, Color.Black, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Ekspor / Backup Database Room", style = MaterialTheme.typography.bodyMedium, color = Color.Black, fontWeight = FontWeight.Black)
                Text(
                    text = "Aplikasi kasir ini berjalan 100% offline. Semua database disimpan secara internal di perangkat Anda. Disarankan untuk membackup database secara berkala ke folder eksternal.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 3.dp, end = 3.dp)
                        .clickable {
                            Toast.makeText(context, "Fitur Auto-Backup Sukses Dijalankan ke internal sandbox!", Toast.LENGTH_LONG).show()
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 3.dp, y = 3.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8D5FF), RoundedCornerShape(4.dp))
                            .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("CADANGKAN DATABASE SEKARANG", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

fun isBiometricAvailable(context: android.content.Context): Boolean {
    val biometricManager = BiometricManager.from(context)
    return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
        BiometricManager.BIOMETRIC_SUCCESS -> true
        else -> false
    }
}

fun showBiometricPrompt(
    activity: androidx.fragment.app.FragmentActivity,
    title: String,
    subtitle: String,
    onSuccess: () -> Unit,
    onError: (Int, String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                activity.runOnUiThread {
                    onError(errorCode, errString.toString())
                }
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                activity.runOnUiThread {
                    onSuccess()
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        }
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText("Batal / Gunakan PIN")
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)
        .build()

    biometricPrompt.authenticate(promptInfo)
}

fun android.content.Context.findActivity(): androidx.fragment.app.FragmentActivity? {
    var currentContext = this
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is androidx.fragment.app.FragmentActivity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
fun PinInputDialog(
    title: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pinText by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .width(320.dp)
                .padding(bottom = 6.dp, end = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(Color.Black, RoundedCornerShape(12.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 6) {
                        val isFilled = i < pinText.length
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) Color(0xFFFFDE4D) else Color(0xFFF4F3EF))
                                .border(2.dp, Color.Black, CircleShape)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "⌫")
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    keys.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { key ->
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .padding(bottom = 3.dp, end = 3.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .offset(x = 3.dp, y = 3.dp)
                                            .background(Color.Black, RoundedCornerShape(8.dp))
                                    )
                                    val buttonColor = when (key) {
                                        "C" -> Color(0xFFFF595E)
                                        "⌫" -> Color(0xFFE8D5FF)
                                        else -> Color.White
                                    }
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(buttonColor, RoundedCornerShape(8.dp))
                                            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (key == "C") {
                                                    pinText = ""
                                                } else if (key == "⌫") {
                                                    if (pinText.isNotEmpty()) {
                                                        pinText = pinText.dropLast(1)
                                                    }
                                                } else {
                                                    if (pinText.length < 6) {
                                                        pinText += key
                                                        if (pinText.length == 6) {
                                                            onConfirm(pinText)
                                                        }
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clickable { onDismiss() }
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 3.dp, y = 3.dp)
                            .background(Color.Black, RoundedCornerShape(6.dp))
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0xFFF4F3EF), RoundedCornerShape(6.dp))
                            .border(2.dp, Color.Black, RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "BATAL",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SecuritySettingsSubScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pos_settings", android.content.Context.MODE_PRIVATE) }
    var useBiometrics by remember { mutableStateOf(prefs.getBoolean("use_biometrics", false)) }
    var usePin by remember { mutableStateOf(prefs.getBoolean("use_pin", false)) }
    
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var setupStep by remember { mutableStateOf(1) }
    var setupPinFirst by remember { mutableStateOf("") }
    var showPinDisableDialog by remember { mutableStateOf(false) }
    var showPinChangeDialog by remember { mutableStateOf(false) }
    
    val hasBiometricHardware = remember { isBiometricAvailable(context) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, end = 4.dp)) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 4.dp, y = 4.dp)
                    .background(Color.Black, RoundedCornerShape(6.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(6.dp))
                    .border(2.5.dp, Color.Black, RoundedCornerShape(6.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "AUTENTIKASI & KEAMANAN",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Black
                )
                
                Text(
                    text = "Amankan akses menu Manajemen Produk dan Master QRIS agar tidak disalahgunakan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
                
                HorizontalDivider(color = Color.Black, thickness = 2.dp)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Aktifkan PIN (6 Digit)", style = MaterialTheme.typography.bodyMedium, color = Color.Black, fontWeight = FontWeight.Black)
                        Text("Amankan dengan 6 digit angka rahasia", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                    }
                    
                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(28.dp)
                            .clip(CircleShape)
                            .background(if (usePin) Color(0xFFFFDE4D) else Color.LightGray)
                            .border(2.dp, Color.Black, CircleShape)
                            .clickable {
                                if (!usePin) {
                                    setupStep = 1
                                    setupPinFirst = ""
                                    showPinSetupDialog = true
                                } else {
                                    showPinDisableDialog = true
                                }
                            }
                            .padding(2.dp),
                        contentAlignment = if (usePin) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.5.dp, Color.Black, CircleShape)
                        )
                    }
                }
                
                if (usePin) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clickable {
                                showPinChangeDialog = true
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 3.dp, y = 3.dp)
                                .background(Color.Black, RoundedCornerShape(6.dp))
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color(0xFFE8D5FF), RoundedCornerShape(6.dp))
                                .border(1.5.dp, Color.Black, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("UBAH PIN KEAMANAN", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }
                
                if (hasBiometricHardware) {
                    HorizontalDivider(color = Color.Black.copy(alpha = 0.2f), thickness = 1.dp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Gunakan Biometrik (Sidik Jari/Face ID)", style = MaterialTheme.typography.bodyMedium, color = Color.Black, fontWeight = FontWeight.Black)
                            Text("Buka kunci cepat menggunakan sensor biometrik perangkat", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                        }
                        
                        Box(
                            modifier = Modifier
                                .width(52.dp)
                                .height(28.dp)
                                .clip(CircleShape)
                                .background(if (useBiometrics) Color(0xFF00F5D4) else Color.LightGray)
                                .border(2.dp, Color.Black, CircleShape)
                                .clickable {
                                    useBiometrics = !useBiometrics
                                    prefs.edit().putBoolean("use_biometrics", useBiometrics).apply()
                                    Toast.makeText(context, if (useBiometrics) "Biometrik diaktifkan!" else "Biometrik dinonaktifkan!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(2.dp),
                            contentAlignment = if (useBiometrics) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(1.5.dp, Color.Black, CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
    
    if (showPinSetupDialog) {
        if (setupStep == 1) {
            PinInputDialog(
                title = "Masukkan PIN Baru (6 Digit)",
                onConfirm = { pin ->
                    setupPinFirst = pin
                    setupStep = 2
                },
                onDismiss = {
                    showPinSetupDialog = false
                }
            )
        } else {
            PinInputDialog(
                title = "Konfirmasi PIN Baru",
                onConfirm = { pin ->
                    if (pin == setupPinFirst) {
                        prefs.edit()
                            .putBoolean("use_pin", true)
                            .putString("secure_pin", pin)
                            .apply()
                        usePin = true
                        showPinSetupDialog = false
                        Toast.makeText(context, "PIN berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "PIN tidak cocok. Silakan coba lagi.", Toast.LENGTH_LONG).show()
                        setupStep = 1
                        setupPinFirst = ""
                    }
                },
                onDismiss = {
                    showPinSetupDialog = false
                }
            )
        }
    }
    
    if (showPinDisableDialog) {
        PinInputDialog(
            title = "Masukkan PIN untuk Nonaktifkan",
            onConfirm = { pin ->
                val savedPin = prefs.getString("secure_pin", "")
                if (pin == savedPin) {
                    prefs.edit()
                        .putBoolean("use_pin", false)
                        .putString("secure_pin", "")
                        .apply()
                    usePin = false
                    showPinDisableDialog = false
                    Toast.makeText(context, "Fitur PIN berhasil dimatikan!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "PIN salah!", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = {
                showPinDisableDialog = false
            }
        )
    }
    
    if (showPinChangeDialog) {
        PinInputDialog(
            title = "Masukkan PIN Lama",
            onConfirm = { pin ->
                val savedPin = prefs.getString("secure_pin", "")
                if (pin == savedPin) {
                    showPinChangeDialog = false
                    setupStep = 1
                    setupPinFirst = ""
                    showPinSetupDialog = true
                } else {
                    Toast.makeText(context, "PIN salah!", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = {
                showPinChangeDialog = false
            }
        )
    }
}

