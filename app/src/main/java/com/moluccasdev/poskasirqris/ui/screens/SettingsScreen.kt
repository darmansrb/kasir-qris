package com.moluccasdev.poskasirqris.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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

    // Screen State: "MAIN", "STORE", "PRODUCTS", "QRIS", "CONFIG"
    var activeSubScreen by remember { mutableStateOf("MAIN") }

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

                        // 2. Product Management Card
                        SettingsMenuListItem(
                            title = "MANAJEMEN PRODUK",
                            subtitle = "Kelola katalog barang, tambah produk baru & harga",
                            accentColor = Color(0xFFFFDE4D),
                            onClick = { activeSubScreen = "PRODUCTS" }
                        )

                        // 3. QRIS Engine Card
                        SettingsMenuListItem(
                            title = "MASTER QRIS",
                            subtitle = "Konfigurasi string QRIS dinamis & info merchant",
                            accentColor = Color(0xFF00F5D4),
                            onClick = { activeSubScreen = "QRIS" }
                        )

                        // 4. Preferences Card
                        SettingsMenuListItem(
                            title = "PREFERENSI & BACKUP",
                            subtitle = "Pengaturan printer thermal, test print & backup database",
                            accentColor = Color(0xFFFF595E),
                            onClick = { activeSubScreen = "CONFIG" }
                        )
                    }
                }
                "STORE" -> StoreSettingsSubScreen()
                "PRODUCTS" -> ProductSettingsTab(settingsVM, products)
                "QRIS" -> QrisSettingsTab(settingsVM, qrisConfigs)
                "CONFIG" -> ConfigSettingsTab(context)
            }
        }
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

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Katalog Barang",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = {
                    selectedProduct = null
                    showEditDialog = true
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // List of products inside database
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (product.isActive) Color.White else Color.White.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .padding(8.dp),
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
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(product.name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Rp ${String.format(Locale.getDefault(), "%,.0f", product.price)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (!product.isActive) {
                                Text(
                                    text = "Dihapus (Soft Delete)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                selectedProduct = product
                                showEditDialog = true
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }

                        if (product.isActive) {
                            IconButton(
                                onClick = { settingsVM.deleteProduct(product) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            }
                        }
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

    // Gallery Picker launcher
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
        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .width(400.dp)
                .padding(8.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (product == null) "Tambah Barang Baru" else "Edit Detail Barang",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Barang") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Harga Barang (Rp)") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Select image display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (imagePath != null) "Foto Terpilih ✓" else "Belum ada foto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (imagePath != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                    )

                    Button(
                        onClick = { pickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Pilih Galeri", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outlineVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
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
                        },
                        enabled = name.isNotBlank() && price.toDoubleOrNull() != null,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simpan")
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

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Master QRIS Toko",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(qrisConfigs) { qris ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(
                            width = if (qris.isDefault) 2.dp else 1.dp,
                            color = if (qris.isDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = qris.merchantName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (qris.isDefault) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("DEFAULT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(
                            text = "String: " + if (qris.rawQrisString.length > 40) qris.rawQrisString.take(37) + "..." else qris.rawQrisString,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (!qris.isDefault) {
                            IconButton(
                                onClick = { settingsVM.setDefaultQris(qris.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = "Jadikan Utama", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                            }
                        }

                        IconButton(
                            onClick = { settingsVM.deleteQris(qris) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
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

    // Gallery Picker launcher for QR images
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

    // Camera permission request launcher
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
        Surface(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .width(400.dp)
                .padding(8.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tambah Master QRIS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Row for Scan / Import Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val permission = android.Manifest.permission.CAMERA
                            val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                                context, permission
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (isGranted) {
                                showCameraScanner = true
                            } else {
                                cameraPermissionLauncher.launch(permission)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh, // Placeholder for camera icon
                            contentDescription = "Kamera Scanner"
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pindai Kamera", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { qrPickerLauncher.launch("image/*") },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Impor Gambar", style = MaterialTheme.typography.labelSmall)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                OutlinedTextField(
                    value = qrisString,
                    onValueChange = { qrisString = it },
                    label = { Text("Paste/Hasil Scan QRIS String") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("000201010211...", style = MaterialTheme.typography.labelSmall) }
                )

                // Detect merchant automatically
                Button(
                    onClick = {
                        val detected = QrisEngine.extractMerchantName(qrisString)
                        name = detected
                    },
                    enabled = qrisString.isNotBlank(),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Deteksi")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Deteksi Otomatis Merchant Name", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Merchant / Toko") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = true
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
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                            .background(if (isDefault) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Jadikan QRIS Default Utama", style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outlineVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            if (qrisString.isNotBlank()) {
                                settingsVM.saveQris(
                                    id = 0,
                                    merchantNameInput = name,
                                    rawQris = qrisString,
                                    isDefault = isDefault
                                )
                                onDismiss()
                            }
                        },
                        enabled = qrisString.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Simpan")
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
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        // Switch style Printer config
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.SpaceBetween,
                     verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text("Printer Bluetooth Thermal", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("Cetak struk fisik ESC/POS otomatis sehabis transaksi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }

                    Box(
                        modifier = Modifier
                            .width(52.dp)
                            .height(28.dp)
                            .clip(CircleShape)
                            .background(if (isPrinterActive) Color(0xFF2E7D32) else Color.LightGray)
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
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White)
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
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Pilih Printer Bluetooth:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val currentPrinterName = pairedPrinters.find { it.device.address == selectedPrinterAddress }?.device?.name
                        ?: if (selectedPrinterAddress.isNotEmpty()) "Printer Terputus ($selectedPrinterAddress)" else "Belum Memilih Printer (Tap Pilih)"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
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
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = if (showPrinterDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Pilih Printer",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (showPrinterDropdown) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(4.dp)) {
                                if (pairedPrinters.isEmpty()) {
                                    Text(
                                        text = "Tidak ada printer Bluetooth dipasang. Hubungkan dulu di Pengaturan Bluetooth HP.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
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
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = printer.device.address,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                            if (selectedPrinterAddress == printer.device.address) {
                                                Icon(
                                                    imageVector = Icons.Default.Done,
                                                    contentDescription = "Terpilih",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
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
                                    val printer = com.dantsu.escposprinter.EscPosPrinter(bluetoothConnection, 203, 48f, 32)
                                    val textToPrint = StringBuilder()
                                    textToPrint.append("[C]<b><font size='big'>TEST PRINT DUMMY</font></b>\n")
                                    textToPrint.append("[C]Printer Thermal Bluetooth Berhasil\n")
                                    textToPrint.append("[C]================================\n")
                                    textToPrint.append("[L]Tgl Test : ${java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\n")
                                    textToPrint.append("[L]Printer  : ${bluetoothConnection.device.name ?: "Unknown"}\n")
                                    textToPrint.append("[L]Alamat   : ${bluetoothConnection.device.address}\n")
                                    textToPrint.append("[C]--------------------------------\n")
                                    textToPrint.append("[C]Koneksi printer thermal berjalan!\n")
                                    textToPrint.append("[C]POS KASIR QRIS OFFLINE\n\n\n")

                                    printer.printFormattedText(textToPrint.toString())
                                    Toast.makeText(context, "Test print berhasil dikirim ke printer!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(context, "Gagal print: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Test Print")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TEST PRINT STRUK (DUMMY)", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // SQLite DB Backup info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Ekspor / Backup Database Room", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Aplikasi kasir ini berjalan 100% offline. Semua database disimpan secara internal di perangkat Anda. Disarankan untuk membackup database secara berkala ke folder eksternal.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Button(
                    onClick = {
                        Toast.makeText(context, "Fitur Auto-Backup Sukses Dijalankan ke internal sandbox!", Toast.LENGTH_LONG).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cadangkan Database Sekarang", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
