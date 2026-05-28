package com.moluccasdev.poskasirqris.ui.screens

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.moluccasdev.poskasirqris.ui.CalculatorViewModel
import com.moluccasdev.poskasirqris.ui.OrderViewModel
import java.util.Locale

@Composable
fun PaymentScreen(
    calcVM: CalculatorViewModel,
    orderVM: OrderViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentOrderName = orderVM.currentCustomerName.ifBlank { "Umum" }
    val orderId = orderVM.currentOrderId

    val prefs = remember { context.getSharedPreferences("pos_settings", android.content.Context.MODE_PRIVATE) }
    var selectedPrinterAddress by remember { mutableStateOf(prefs.getString("selected_printer_address", "") ?: "") }
    var showPrinterDropdown by remember { mutableStateOf(false) }

    val pairedPrinters = remember {
        try {
            com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections().list?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    val bluetoothPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isConnectGranted = permissions[android.Manifest.permission.BLUETOOTH_CONNECT] ?: false
        val isScanGranted = permissions[android.Manifest.permission.BLUETOOTH_SCAN] ?: false
        if (isConnectGranted && isScanGranted) {
            val draftDate = orderVM.draftOrders.value.find { it.order.id == orderId }?.order?.createdAt ?: System.currentTimeMillis()
            printReceipt(context, orderVM, calcVM, draftDate, selectedPrinterAddress)
        } else {
            Toast.makeText(context, "Izin Bluetooth ditolak. Tidak dapat mencetak struk!", Toast.LENGTH_SHORT).show()
        }
    }

    // Prepare checkout total directly from order total computed from database items list
    val totalAmount = orderVM.cartTotal

    androidx.compose.runtime.LaunchedEffect(totalAmount) {
        calcVM.checkoutAmount = totalAmount
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 4.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Detail Pesanan & Pembayaran",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        if (isLandscape) {
            // Landscape Mode: Side-by-side Row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // Left Column: Show items list for draft
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Receipt Context Indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Draft: $currentOrderName (${orderVM.cartItemCount} item)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // MUNCULKAN LIST BARANG YANG DIBELI/PESAN UNTUK PEMBAYARAN
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(orderVM.cart) { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.product.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${item.qty} x Rp ${String.format(Locale.getDefault(), "%,.0f", item.product.price)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Text(
                                        text = "Rp ${String.format(Locale.getDefault(), "%,.0f", item.product.price * item.qty)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                            }
                        }
                    }
                }

                // Right Column: Active Checkout Workspace (Scrollable Column)
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEADDFF).copy(alpha = 0.35f))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Metode Pembayaran",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Large Price To Pay Display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TOTAL YANG HARUS DIBAYAR",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Rp ${String.format(Locale.getDefault(), "%,.0f", totalAmount)}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Checkout Select Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PaymentMethodSelectorCard(
                            title = "TUNAI",
                            isActive = calcVM.currentPaymentMethod == "CASH",
                            onClick = {
                                calcVM.checkoutAmount = totalAmount
                                calcVM.selectPaymentMethod("CASH")
                            },
                            modifier = Modifier.weight(1f)
                        )

                        PaymentMethodSelectorCard(
                            title = "QRIS",
                            isActive = calcVM.currentPaymentMethod == "QRIS",
                            onClick = {
                                calcVM.checkoutAmount = totalAmount
                                calcVM.selectPaymentMethod("QRIS")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Dynamic Form based on chosen payment method
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (calcVM.currentPaymentMethod == "CASH") {
                            CashPaymentPanel(calcVM, totalAmount)
                        } else if (calcVM.currentPaymentMethod == "QRIS") {
                            QrisPaymentPanel(calcVM, totalAmount)
                        }
                    }

                    // Printer Selector (shown only if printer is active in settings)
                    val isPrinterActive = remember { prefs.getBoolean("is_printer_active", false) }
                    if (isPrinterActive) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Printer Bluetooth Terpilih:",
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
                                    .background(MaterialTheme.colorScheme.surface)
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
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Pay & Checkout Action Validation
                    val cashAmount = calcVM.cashPaidAmount.toDoubleOrNull() ?: 0.0
                    val isCashValid = calcVM.currentPaymentMethod == "CASH" && cashAmount >= totalAmount
                    val isQrisValid = calcVM.currentPaymentMethod == "QRIS"

                    Button(
                        onClick = {
                            val draftDate = orderVM.draftOrders.value.find { it.order.id == orderId }?.order?.createdAt ?: System.currentTimeMillis()
                            val prefs = context.getSharedPreferences("pos_settings", android.content.Context.MODE_PRIVATE)
                            val isPrinterActive = prefs.getBoolean("is_printer_active", false)
                            if (isPrinterActive) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    val connectPerm = android.Manifest.permission.BLUETOOTH_CONNECT
                                    val scanPerm = android.Manifest.permission.BLUETOOTH_SCAN
                                    val isConnectGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, connectPerm) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    val isScanGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, scanPerm) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    if (!isConnectGranted || !isScanGranted) {
                                        bluetoothPermissionLauncher.launch(arrayOf(connectPerm, scanPerm))
                                    } else {
                                        printReceipt(context, orderVM, calcVM, draftDate, selectedPrinterAddress)
                                    }
                                } else {
                                    printReceipt(context, orderVM, calcVM, draftDate, selectedPrinterAddress)
                                }
                            }

                            calcVM.checkout(orderId) {
                                Toast.makeText(context, "Pembayaran Berhasil Diselesaikan!", Toast.LENGTH_LONG).show()
                                orderVM.clearCart()
                                calcVM.prepareCheckout(0.0)
                                calcVM.onKeyPress("C")
                                onNavigateBack()
                            }
                        },
                        enabled = calcVM.currentPaymentMethod.isNotEmpty() && (isCashValid || isQrisValid),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Selesai",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SELESAIKAN PEMBAYARAN", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        } else {
            // Portrait Mode: Single Scrollable Column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Receipt Context Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Draft: $currentOrderName (${orderVM.cartItemCount} item)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // MUNCULKAN LIST BARANG YANG DIBELI/PESAN UNTUK PEMBAYARAN (PORTRAIT)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(orderVM.cart) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.product.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${item.qty} x Rp ${String.format(Locale.getDefault(), "%,.0f", item.product.price)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Text(
                                    text = "Rp ${String.format(Locale.getDefault(), "%,.0f", item.product.price * item.qty)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Active Checkout Workspace
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEADDFF).copy(alpha = 0.35f))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Metode Pembayaran",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Price To Pay Display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TOTAL YANG HARUS DIBAYAR",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rp ${String.format(Locale.getDefault(), "%,.0f", totalAmount)}",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Checkout Select Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        PaymentMethodSelectorCard(
                            title = "TUNAI / CASH",
                            isActive = calcVM.currentPaymentMethod == "CASH",
                            onClick = {
                                calcVM.checkoutAmount = totalAmount
                                calcVM.selectPaymentMethod("CASH")
                            },
                            modifier = Modifier.weight(1f)
                        )

                        PaymentMethodSelectorCard(
                            title = "DYN QRIS",
                            isActive = calcVM.currentPaymentMethod == "QRIS",
                            onClick = {
                                calcVM.checkoutAmount = totalAmount
                                calcVM.selectPaymentMethod("QRIS")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Dynamic Form based on chosen payment method (Cash / QRIS dinamis)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (calcVM.currentPaymentMethod == "CASH") {
                            CashPaymentPanel(calcVM, totalAmount)
                        } else if (calcVM.currentPaymentMethod == "QRIS") {
                            QrisPaymentPanel(calcVM, totalAmount)
                        }
                    }

                    // Printer Selector (shown only if printer is active in settings) - Portrait
                    val isPrinterActive = remember { prefs.getBoolean("is_printer_active", false) }
                    if (isPrinterActive) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "Printer Bluetooth Terpilih:",
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
                                    .background(MaterialTheme.colorScheme.surface)
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
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Pay & Checkout Action
                    val cashAmount = calcVM.cashPaidAmount.toDoubleOrNull() ?: 0.0
                    val isCashValid = calcVM.currentPaymentMethod == "CASH" && cashAmount >= totalAmount
                    val isQrisValid = calcVM.currentPaymentMethod == "QRIS"

                    Button(
                        onClick = {
                            val draftDate = orderVM.draftOrders.value.find { it.order.id == orderId }?.order?.createdAt ?: System.currentTimeMillis()
                            val prefs = context.getSharedPreferences("pos_settings", android.content.Context.MODE_PRIVATE)
                            val isPrinterActive = prefs.getBoolean("is_printer_active", false)
                            if (isPrinterActive) {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    val connectPerm = android.Manifest.permission.BLUETOOTH_CONNECT
                                    val scanPerm = android.Manifest.permission.BLUETOOTH_SCAN
                                    val isConnectGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, connectPerm) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    val isScanGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, scanPerm) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    if (!isConnectGranted || !isScanGranted) {
                                        bluetoothPermissionLauncher.launch(arrayOf(connectPerm, scanPerm))
                                    } else {
                                        printReceipt(context, orderVM, calcVM, draftDate, selectedPrinterAddress)
                                    }
                                } else {
                                    printReceipt(context, orderVM, calcVM, draftDate, selectedPrinterAddress)
                                }
                            }

                            calcVM.checkout(orderId) {
                                Toast.makeText(context, "Pembayaran Berhasil Diselesaikan!", Toast.LENGTH_LONG).show()
                                orderVM.clearCart()
                                calcVM.prepareCheckout(0.0)
                                calcVM.onKeyPress("C")
                                onNavigateBack()
                            }
                        },
                        enabled = calcVM.currentPaymentMethod.isNotEmpty() && (isCashValid || isQrisValid),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = "Selesai")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SELESAIKAN PEMBAYARAN", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

class RupiahVisualTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return androidx.compose.ui.text.input.TransformedText(text, androidx.compose.ui.text.input.OffsetMapping.Identity)
        }

        val formatted = try {
            val number = originalText.toLong()
            java.text.NumberFormat.getNumberInstance(java.util.Locale("in", "ID")).format(number)
        } catch (e: Exception) {
            originalText
        }

        val offsetMapping = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                var transformedOffset = 0
                var originalCount = 0
                for (i in 0 until formatted.length) {
                    if (formatted[i].isDigit()) {
                        originalCount++
                    }
                    transformedOffset++
                    if (originalCount == offset) {
                        break
                    }
                }
                return transformedOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val clampedOffset = offset.coerceAtMost(formatted.length)
                var originalOffset = 0
                for (i in 0 until clampedOffset) {
                    if (formatted[i].isDigit()) {
                        originalOffset++
                    }
                }
                return originalOffset
            }
        }

        return androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(formatted), offsetMapping)
    }
}

@Composable
fun CashPaymentPanel(calcVM: CalculatorViewModel, totalAmount: Double) {
    val quickAmounts = listOf("10000", "20000", "50000", "100000")
    val cashAmount = calcVM.cashPaidAmount.toDoubleOrNull() ?: 0.0
    val isCashInsufficient = calcVM.cashPaidAmount.isNotEmpty() && cashAmount < totalAmount

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = calcVM.cashPaidAmount,
            onValueChange = { newVal ->
                val filtered = newVal.filter { it.isDigit() }
                calcVM.cashPaidAmount = filtered
            },
            label = { Text("Jumlah Uang Diterima", style = MaterialTheme.typography.labelSmall) },
            prefix = { Text("Rp ", style = MaterialTheme.typography.bodyMedium) },
            visualTransformation = remember { RupiahVisualTransformation() },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isCashInsufficient,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isCashInsufficient) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = if (isCashInsufficient) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
            )
        )

        if (isCashInsufficient) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Jumlah uang kurang! Silakan masukkan nominal yang mencukupi.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hot keys row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickAmounts.forEach { amount ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                        .clickable { calcVM.cashPaidAmount = amount }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Rp ${String.format(Locale.getDefault(), "%,.0f", amount.toDouble())}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Auto change display
        if (calcVM.cashPaidAmount.isNotEmpty() && !isCashInsufficient) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE2F0D9)) // Soft success green container
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "KEMBALIAN:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rp ${String.format(Locale.getDefault(), "%,.0f", cashAmount - totalAmount)}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QrisPaymentPanel(calcVM: CalculatorViewModel, totalAmount: Double) {
    var showQrModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (calcVM.activeQrisString.isEmpty() && !calcVM.isQrisLoading) {
            Text(
                text = "QRIS Master Belum Dikonfigurasi! Silakan tambahkan string QRIS di menu Pengaturan.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )
        } else {
            Button(
                onClick = { 
                    showQrModal = true 
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Tampilkan QRIS")
                Spacer(modifier = Modifier.width(8.dp))
                Text("TAMPILKAN BARCODE QRIS", style = MaterialTheme.typography.labelLarge)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Merchant: ${calcVM.merchantName}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showQrModal && calcVM.activeQrisBitmap != null) {
        QrisQrCodeModal(
            bitmap = calcVM.activeQrisBitmap!!,
            merchant = calcVM.merchantName,
            amount = totalAmount,
            onDismiss = { showQrModal = false }
        )
    }
}

@Composable
fun PaymentMethodSelectorCard(
    title: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColors = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val backgroundColors = if (isActive) Color(0xFFEADDFF).copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColors)
            .border(if (isActive) 2.dp else 1.dp, borderColors, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun QrisQrCodeModal(
    bitmap: Bitmap,
    merchant: String,
    amount: Double,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .width(360.dp)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header GPN/QRIS
                Text(
                    text = "QRIS DINAMIS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = merchant.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // QR Image
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QRIS Dynamic Barcode",
                    modifier = Modifier
                        .size(240.dp)
                        .border(1.dp, Color.LightGray)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Price display
                Text(
                    text = "TOTAL TAGIHAN",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rp ${String.format(Locale.getDefault(), "%,.0f", amount)}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK, SUDAH DI-SCAN", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

fun printReceipt(
    context: android.content.Context,
    orderVM: OrderViewModel,
    calcVM: CalculatorViewModel,
    orderDate: Long,
    selectedPrinterAddress: String
) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        val connectPerm = android.Manifest.permission.BLUETOOTH_CONNECT
        val scanPerm = android.Manifest.permission.BLUETOOTH_SCAN
        val isConnectGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, connectPerm) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val isScanGranted = androidx.core.content.ContextCompat.checkSelfPermission(context, scanPerm) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!isConnectGranted || !isScanGranted) {
            android.widget.Toast.makeText(context, "Izin Bluetooth dibutuhkan untuk mencetak struk!", android.widget.Toast.LENGTH_LONG).show()
            return
        }
    }

    try {
        val bluetoothPrinters = com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections().list
        val bluetoothConnection = if (selectedPrinterAddress.isNotEmpty() && bluetoothPrinters != null) {
            bluetoothPrinters.find { it.device.address == selectedPrinterAddress }
                ?: com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections.selectFirstPaired()
        } else {
            com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections.selectFirstPaired()
        }

        if (bluetoothConnection == null) {
            android.widget.Toast.makeText(context, "Printer Bluetooth tidak terhubung / terpasang!", android.widget.Toast.LENGTH_LONG).show()
            return
        }

        val printer = com.dantsu.escposprinter.EscPosPrinter(bluetoothConnection, 203, 48f, 32)

        val dateFormatter = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
        val formattedOrderDate = dateFormatter.format(java.util.Date(orderDate))
        val formattedPrintDate = dateFormatter.format(java.util.Date())

        val cashAmount = calcVM.cashPaidAmount.toDoubleOrNull() ?: 0.0
        val totalAmount = orderVM.cartTotal
        val changeAmount = (cashAmount - totalAmount).coerceAtLeast(0.0)

        val textToPrint = StringBuilder()
        textToPrint.append("[C]<b><font size='big'>POS KASIR QRIS</font></b>\n")
        textToPrint.append("[C]Bukti Pembayaran Transaksi\n")
        textToPrint.append("[C]================================\n")
        textToPrint.append("[L]Tgl Pesan : $formattedOrderDate\n")
        textToPrint.append("[L]Tgl Cetak : $formattedPrintDate\n")
        textToPrint.append("[L]Pelanggan : ${orderVM.currentCustomerName.ifBlank { "Umum" }}\n")
        textToPrint.append("[L]Bayar     : ${calcVM.currentPaymentMethod}\n")
        textToPrint.append("[C]--------------------------------\n")

        orderVM.cart.forEach { item ->
            val subtotal = item.product.price * item.qty
            textToPrint.append("[L]${item.product.name}\n")
            textToPrint.append("[L]  ${item.qty} x Rp ${String.format(java.util.Locale.getDefault(), "%,.0f", item.product.price)}[R]Rp ${String.format(java.util.Locale.getDefault(), "%,.0f", subtotal)}\n")
        }

        textToPrint.append("[C]--------------------------------\n")
        textToPrint.append("[L]<b>TOTAL[R]Rp ${String.format(java.util.Locale.getDefault(), "%,.0f", totalAmount)}</b>\n")

        if (calcVM.currentPaymentMethod == "CASH") {
            textToPrint.append("[L]TUNAI[R]Rp ${String.format(java.util.Locale.getDefault(), "%,.0f", cashAmount)}\n")
            textToPrint.append("[L]KEMBALIAN[R]Rp ${String.format(java.util.Locale.getDefault(), "%,.0f", changeAmount)}\n")
        }

        textToPrint.append("[C]================================\n")
        textToPrint.append("[C]Terima Kasih atas\n")
        textToPrint.append("[C]Kunjungan Anda!\n")
        textToPrint.append("[C]Layanan POS Kasir QRIS Offline\n\n\n")

        printer.printFormattedText(textToPrint.toString())
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Error printer: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
    }
}

