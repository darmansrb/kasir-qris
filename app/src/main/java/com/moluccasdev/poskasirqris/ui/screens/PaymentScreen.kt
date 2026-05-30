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
import androidx.compose.foundation.layout.offset
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

    val totalAmount = orderVM.cartTotal

    androidx.compose.runtime.LaunchedEffect(totalAmount) {
        calcVM.checkoutAmount = totalAmount
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        containerColor = Color(0xFFF4F3EF), // Vintage paper background
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF4F3EF))
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brutalist back button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Brutalist Title Sticker
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFDE4D))
                        .border(2.dp, Color.Black, RoundedCornerShape(2.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "PEMBAYARAN PESANAN",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    ) { innerPadding ->
        if (isLandscape) {
            // Landscape Mode: Side-by-side Row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(start = 16.dp, end = 22.dp, top = 0.dp, bottom = 16.dp)
            ) {
                // Left Column: Show items list for draft
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight()
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Receipt Context Indicator (Brutalist Banner)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8D5FF))
                            .border(2.dp, Color.Black, RoundedCornerShape(2.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DRAFT: ${currentOrderName.uppercase(Locale.getDefault())} (${orderVM.cartItemCount} ITEM)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Purchase items box with brutalist shadow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 8.dp, end = 8.dp) // Room for shadow
                    ) {
                        // Shadow
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 6.dp, y = 6.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        
                        // Content
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White)
                                .border(3.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "RINCIAN BELANJA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
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
                                                text = item.product.name.uppercase(Locale.getDefault()),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Black,
                                                color = Color.Black
                                            )
                                            Text(
                                                text = "${item.qty} X RP ${String.format(Locale.getDefault(), "%,.0f", item.product.price)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.DarkGray,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = "Rp ${String.format(Locale.getDefault(), "%,.0f", item.product.price * item.qty)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                    }
                                    HorizontalDivider(color = Color.Black.copy(alpha = 0.2f), thickness = 1.dp)
                                }
                            }
                        }
                    }
                }

                // Right Column: Active Checkout Workspace
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFDE4D).copy(alpha = 0.25f)) // theme style background
                        .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "METODE PEMBAYARAN",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )

                    // Large Price Display Card with shadow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp, end = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 5.dp, y = 5.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFDE4D), RoundedCornerShape(4.dp))
                                .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TOTAL YANG HARUS DIBAYAR",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rp ${String.format(Locale.getDefault(), "%,.0f", totalAmount)}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Black,
                                fontWeight = FontWeight.Black
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
                            title = "DYNAMIC QRIS",
                            isActive = calcVM.currentPaymentMethod == "QRIS",
                            onClick = {
                                calcVM.checkoutAmount = totalAmount
                                calcVM.selectPaymentMethod("QRIS")
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Dynamic Form
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (calcVM.currentPaymentMethod == "CASH") {
                            CashPaymentPanel(calcVM, totalAmount)
                        } else if (calcVM.currentPaymentMethod == "QRIS") {
                            QrisPaymentPanel(calcVM, totalAmount)
                        }
                    }

                    // Printer Selector
                    val isPrinterActive = remember { prefs.getBoolean("is_printer_active", false) }
                    if (isPrinterActive) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "PRINTER BLUETOOTH TERPILIH:",
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
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                    .clickable { showPrinterDropdown = !showPrinterDropdown }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentPrinterName.uppercase(Locale.getDefault()),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Black,
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
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(4.dp)) {
                                        if (pairedPrinters.isEmpty()) {
                                            Text(
                                                text = "TIDAK ADA PRINTER BLUETOOTH DIPASANG. HUBUNGKAN DULU DI PENGATURAN BLUETOOTH HP.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Red,
                                                fontWeight = FontWeight.Black,
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
                                                            text = (printer.device.name ?: "Unknown Printer").uppercase(Locale.getDefault()),
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Black,
                                                            color = Color.Black
                                                        )
                                                        Text(
                                                            text = printer.device.address,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.DarkGray,
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
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Selesaikan Button with hard shadow
                    val cashAmount = calcVM.cashPaidAmount.toDoubleOrNull() ?: 0.0
                    val isCashValid = calcVM.currentPaymentMethod == "CASH" && cashAmount >= totalAmount
                    val isQrisValid = calcVM.currentPaymentMethod == "QRIS"
                    val canPay = calcVM.currentPaymentMethod.isNotEmpty() && (isCashValid || isQrisValid)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (canPay) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 4.dp, y = 4.dp)
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    if (canPay) Color(0xFF00F5D4) else Color.LightGray.copy(alpha = 0.5f),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    2.5.dp,
                                    if (canPay) Color.Black else Color.Gray,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable(enabled = canPay) {
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
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Selesai",
                                    tint = if (canPay) Color.Black else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SELESAIKAN PEMBAYARAN", 
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (canPay) Color.Black else Color.Gray
                                )
                            }
                        }
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
                    .padding(start = 16.dp, end = 22.dp, top = 0.dp, bottom = 24.dp)
            ) {
                // Receipt Context Indicator (Brutalist Banner)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .background(Color(0xFFE8D5FF))
                        .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Info", tint = Color.Black)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "DRAFT: ${currentOrderName.uppercase(Locale.getDefault())} (${orderVM.cartItemCount} ITEM)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }

                // MUNCULKAN LIST BARANG YANG DIBELI/PESAN UNTUK PEMBAYARAN (PORTRAIT)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .padding(bottom = 8.dp, end = 8.dp) // Shadow
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 6.dp, y = 6.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                            .border(3.dp, Color.Black, RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "RINCIAN BELANJA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
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
                                            text = item.product.name.uppercase(Locale.getDefault()),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                        Text(
                                            text = "${item.qty} X RP ${String.format(Locale.getDefault(), "%,.0f", item.product.price)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.DarkGray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Rp ${String.format(Locale.getDefault(), "%,.0f", item.product.price * item.qty)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                                HorizontalDivider(color = Color.Black.copy(alpha = 0.2f), thickness = 1.dp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Active Checkout Workspace
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFDE4D).copy(alpha = 0.25f))
                        .border(3.dp, Color.Black, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "METODE PEMBAYARAN",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )

                    // Price Display Card with shadow
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp, end = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 5.dp, y = 5.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFDE4D), RoundedCornerShape(4.dp))
                                .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TOTAL YANG HARUS DIBAYAR",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rp ${String.format(Locale.getDefault(), "%,.0f", totalAmount)}",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Checkout Select Mode Buttons
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

                    // Dynamic Form based on chosen payment method
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (calcVM.currentPaymentMethod == "CASH") {
                            CashPaymentPanel(calcVM, totalAmount)
                        } else if (calcVM.currentPaymentMethod == "QRIS") {
                            QrisPaymentPanel(calcVM, totalAmount)
                        }
                    }

                    // Printer Selector - Portrait
                    val isPrinterActive = remember { prefs.getBoolean("is_printer_active", false) }
                    if (isPrinterActive) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "PRINTER BLUETOOTH TERPILIH:",
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
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White)
                                    .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                    .clickable { showPrinterDropdown = !showPrinterDropdown }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentPrinterName.uppercase(Locale.getDefault()),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Black,
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
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(4.dp)) {
                                        if (pairedPrinters.isEmpty()) {
                                            Text(
                                                text = "TIDAK ADA PRINTER BLUETOOTH DIPASANG. HUBUNGKAN DULU DI PENGATURAN BLUETOOTH HP.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Red,
                                                fontWeight = FontWeight.Black,
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
                                                            text = (printer.device.name ?: "Unknown Printer").uppercase(Locale.getDefault()),
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Black,
                                                            color = Color.Black
                                                        )
                                                        Text(
                                                            text = printer.device.address,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.DarkGray,
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
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Selesaikan Button - Portrait
                    val cashAmount = calcVM.cashPaidAmount.toDoubleOrNull() ?: 0.0
                    val isCashValid = calcVM.currentPaymentMethod == "CASH" && cashAmount >= totalAmount
                    val isQrisValid = calcVM.currentPaymentMethod == "QRIS"
                    val canPay = calcVM.currentPaymentMethod.isNotEmpty() && (isCashValid || isQrisValid)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        if (canPay) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 4.dp, y = 4.dp)
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    if (canPay) Color(0xFF00F5D4) else Color.LightGray.copy(alpha = 0.5f),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    2.5.dp,
                                    if (canPay) Color.Black else Color.Gray,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable(enabled = canPay) {
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
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Done,
                                    contentDescription = "Selesai",
                                    tint = if (canPay) Color.Black else Color.Gray,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "SELESAIKAN PEMBAYARAN", 
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Black,
                                    color = if (canPay) Color.Black else Color.Gray
                                )
                            }
                        }
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
            label = { Text("Jumlah Uang Diterima", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Bold) },
            prefix = { Text("Rp ", style = MaterialTheme.typography.bodyMedium, color = Color.Black, fontWeight = FontWeight.Bold) },
            visualTransformation = remember { RupiahVisualTransformation() },
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isCashInsufficient,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                errorBorderColor = Color(0xFFFF595E),
                errorContainerColor = Color.White
            )
        )

        if (isCashInsufficient) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFF595E))
                    .border(2.dp, Color.Black, RoundedCornerShape(2.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = "Jumlah uang kurang! Silakan masukkan nominal yang mencukupi.",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black
                )
            }
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
                        .padding(bottom = 3.dp, end = 3.dp)
                        .clickable { calcVM.cashPaidAmount = amount }
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 3.dp, y = 3.dp)
                            .background(Color.Black, RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Rp ${String.format(Locale.getDefault(), "%,.0f", amount.toDouble())}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Auto change display
        if (calcVM.cashPaidAmount.isNotEmpty() && !isCashInsufficient) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp, end = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(Color.Black, RoundedCornerShape(4.dp))
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF00F5D4), RoundedCornerShape(4.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "KEMBALIAN:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Rp ${String.format(Locale.getDefault(), "%,.0f", cashAmount - totalAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFF595E))
                    .border(2.dp, Color.Black, RoundedCornerShape(2.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "QRIS Master Belum Dikonfigurasi! Silakan tambahkan string QRIS di menu Pengaturan.",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Black
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable { showQrModal = true }
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
                        .background(Color(0xFFE8D5FF), RoundedCornerShape(4.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = "Tampilkan QRIS", tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TAMPILKAN BARCODE QRIS",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Merchant: ${calcVM.merchantName.uppercase(Locale.getDefault())}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                color = Color.Black
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
    val backgroundColors = if (isActive) Color(0xFFFFDE4D) else Color.White

    Box(
        modifier = modifier
            .padding(bottom = 4.dp, end = 4.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(Color.Black, RoundedCornerShape(4.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColors, RoundedCornerShape(4.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                .padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = Color.Black,
                fontWeight = FontWeight.Black
            )
        }
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
        Box(
            modifier = Modifier
                .width(360.dp)
                .padding(bottom = 6.dp, end = 6.dp)
        ) {
            // Shadow
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(Color.Black, RoundedCornerShape(8.dp))
            )
            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(3.dp, Color.Black, RoundedCornerShape(8.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header GPN/QRIS
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFF595E))
                        .border(2.dp, Color.Black, RoundedCornerShape(2.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "QRIS DINAMIS",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = merchant.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // QR Image
                Box(
                    modifier = Modifier
                        .border(3.dp, Color.Black, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                        .background(Color.White)
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "QRIS Dynamic Barcode",
                        modifier = Modifier.size(220.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price display
                Text(
                    text = "TOTAL TAGIHAN",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Rp ${String.format(Locale.getDefault(), "%,.0f", amount)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clickable { onDismiss() }
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
                        Text(
                            text = "OK, SUDAH DI-SCAN",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }
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

