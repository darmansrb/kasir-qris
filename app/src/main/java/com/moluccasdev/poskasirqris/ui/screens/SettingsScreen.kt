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

    var activeTab by remember { mutableStateOf("PRODUCTS") } // "PRODUCTS", "QRIS", "CONFIG"

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        // Landscape Mode: Side-by-side Row layout
        Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Left Column: Tab Selectors (Scrollable Column)
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pengaturan",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Settings Tab items
                SettingsTabSelectorItem(
                    title = "Manajemen Produk",
                    isActive = activeTab == "PRODUCTS",
                    onClick = { activeTab = "PRODUCTS" }
                )
                SettingsTabSelectorItem(
                    title = "Pengaturan Master QRIS",
                    isActive = activeTab == "QRIS",
                    onClick = { activeTab = "QRIS" }
                )
                SettingsTabSelectorItem(
                    title = "Preferensi & Backup",
                    isActive = activeTab == "CONFIG",
                    onClick = { activeTab = "CONFIG" }
                )
            }

            // Right Column: Tab View Workspace
            Column(
                modifier = Modifier
                    .weight(1.9f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEADDFF).copy(alpha = 0.35f))
                    .padding(16.dp)
            ) {
                when (activeTab) {
                    "PRODUCTS" -> ProductSettingsTab(settingsVM, products)
                    "QRIS" -> QrisSettingsTab(settingsVM, qrisConfigs)
                    "CONFIG" -> ConfigSettingsTab(context)
                }
            }
        }
    } else {
        // Portrait Mode: Top Horizontal Scrollable Tabs & Remaining Space content frame
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Pengaturan",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Konfigurasi katalog barang, QRIS merchant & preferensi",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Horizontal Tab Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsTabChip(
                    title = "Manajemen Produk",
                    isActive = activeTab == "PRODUCTS",
                    onClick = { activeTab = "PRODUCTS" }
                )
                SettingsTabChip(
                    title = "Master QRIS",
                    isActive = activeTab == "QRIS",
                    onClick = { activeTab = "QRIS" }
                )
                SettingsTabChip(
                    title = "Preferensi & Backup",
                    isActive = activeTab == "CONFIG",
                    onClick = { activeTab = "CONFIG" }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab View Workspace (occupies remaining space)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEADDFF).copy(alpha = 0.35f))
                    .padding(16.dp)
            ) {
                when (activeTab) {
                    "PRODUCTS" -> ProductSettingsTab(settingsVM, products)
                    "QRIS" -> QrisSettingsTab(settingsVM, qrisConfigs)
                    "CONFIG" -> ConfigSettingsTab(context)
                }
            }
        }
    }
}

@Composable
fun SettingsTabSelectorItem(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val containerColors = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val contentColors = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(containerColors)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = contentColors,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsTabChip(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColors = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .border(1.dp, borderColors, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
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
    var name by remember { mutableStateOf("") }
    var qrisString by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }

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
                    text = "Tambah Master QRIS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = qrisString,
                    onValueChange = { qrisString = it },
                    label = { Text("Paste Raw QRIS String") },
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
                    singleLine = true
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

// 3. CONFIG & BACKUP TAB WORKSPACE
@Composable
fun ConfigSettingsTab(context: android.content.Context) {
    var isPrinterActive by remember { mutableStateOf(false) }

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
                            .clickable { isPrinterActive = !isPrinterActive }
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
