package com.moluccasdev.poskasirqris.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moluccasdev.poskasirqris.ui.ReportViewModel
import com.moluccasdev.poskasirqris.util.ExportHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReportScreen(reportVM: ReportViewModel) {
    val context = LocalContext.current
    val transactions by reportVM.transactionsList.collectAsState()

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateRangeText = "${dateFormat.format(Date(reportVM.startDate))} - ${dateFormat.format(Date(reportVM.endDate))}"

    // Pagination State
    var currentPage by remember { mutableStateOf(1) }
    var pageSize by remember { mutableStateOf(10) }
    var pageSizeDropdownExpanded by remember { mutableStateOf(false) }

    // Reset page when transaction list changes
    LaunchedEffect(transactions.size, pageSize) {
        currentPage = 1
    }

    // Trigger report loading and reset date range to today on entering
    LaunchedEffect(Unit) {
        val c = Calendar.getInstance()
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        val start = c.timeInMillis
        c.set(Calendar.HOUR_OF_DAY, 23)
        c.set(Calendar.MINUTE, 59)
        c.set(Calendar.SECOND, 59)
        c.set(Calendar.MILLISECOND, 999)
        val end = c.timeInMillis
        reportVM.setDateRange(start, end)
    }

    // Stats calculations
    val totalRevenue = transactions.sumOf { it.transaction.totalAmount }
    val totalCount = transactions.size
    
    val qrisCount = transactions.filter { it.transaction.paymentMethod == "QRIS" }.size
    val cashCount = transactions.filter { it.transaction.paymentMethod == "CASH" }.size
    
    val qrisRevenue = transactions.filter { it.transaction.paymentMethod == "QRIS" }.sumOf { it.transaction.totalAmount }
    val cashRevenue = transactions.filter { it.transaction.paymentMethod == "CASH" }.sumOf { it.transaction.totalAmount }

    val qrisPercentage = if (totalRevenue > 0) (qrisRevenue / totalRevenue).toFloat() else 0f

    // Toast status updates
    LaunchedEffect(reportVM.exportStatusMessage) {
        if (reportVM.exportStatusMessage.isNotEmpty()) {
            Toast.makeText(context, reportVM.exportStatusMessage, Toast.LENGTH_LONG).show()
            reportVM.exportStatusMessage = "" // reset
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        containerColor = Color(0xFFF4F3EF), // Vintage Paper Background
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF4F3EF))
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFDE4D))
                        .border(2.dp, Color.Black, RoundedCornerShape(2.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "DASHBOARD LAPORAN",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Katalog riwayat penjualan kasir lunas",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black,
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
                    .padding(start = 16.dp, end = 22.dp, top = 0.dp, bottom = 16.dp)
            ) {
                // Left Column: Filter and Summary Dashboard (Scrollable)
                Column(
                    modifier = Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // Date Preset Selectors
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, end = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 4.dp, y = 4.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FILTER TANGGAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black
                                )
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Pilih Tanggal",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { selectCustomDateRange(context, reportVM) }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dateRangeText.uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { setTodayRange(reportVM) }
                                        .background(Color(0xFFE8D5FF), RoundedCornerShape(4.dp))
                                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("HARI INI", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { setWeekRange(reportVM) }
                                        .background(Color(0xFFE8D5FF), RoundedCornerShape(4.dp))
                                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("MINGGU", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .clickable { selectCustomDateRange(context, reportVM) }
                                        .background(Color(0xFFFFDE4D), RoundedCornerShape(4.dp))
                                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("PILIH TANGGAL", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    // Summary Stats Cards
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, end = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 4.dp, y = 4.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFDE4D), RoundedCornerShape(4.dp))
                                .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "TOTAL PENDAPATAN",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Rp ${String.format(Locale.getDefault(), "%,.0f", totalRevenue)}",
                                style = MaterialTheme.typography.titleLarge,
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Count Stats Row
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, end = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 4.dp, y = 4.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            Text("TRANSAKSI BEKERJA", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("$totalCount KALI", style = MaterialTheme.typography.titleMedium, color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }

                    // Cash vs QRIS Ratio Line Chart
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, end = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 4.dp, y = 4.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "RASIO TUNAI vs QRIS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("- ", color = Color(0xFF2E7D32), fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                                    Text("Tunai: Rp ${String.format(Locale.getDefault(), "%,.0f", cashRevenue)} (${100 - (qrisPercentage * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("- ", color = Color(0xFFFF595E), fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                                    Text("QRIS: Rp ${String.format(Locale.getDefault(), "%,.0f", qrisRevenue)} (${(qrisPercentage * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Line Chart Canvas
                            RevenueLineChart(transactions = transactions)
                        }
                    }

                    // Export Actions bottom row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { reportVM.exportCSV(context) }
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("EKSPOR CSV", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { reportVM.exportPDF(context) }
                                .background(Color(0xFF00F5D4), RoundedCornerShape(4.dp))
                                .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("EKSPOR PDF", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // Right Column: Transaction List Workspace
                Column(
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                        .background(Color(0xFFE8D5FF).copy(alpha = 0.25f))
                        .border(3.dp, Color.Black, RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    // Header with Pagination Dropdown & Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Riwayat Transaksi".uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )

                        // Pagination Control Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Size Selector Dropdown
                            Box {
                                Box(
                                    modifier = Modifier
                                        .clickable { pageSizeDropdownExpanded = true }
                                        .background(Color.White, RoundedCornerShape(4.dp))
                                        .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "$pageSize", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Black)
                                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                                    }
                                }
                                DropdownMenu(
                                    expanded = pageSizeDropdownExpanded,
                                    onDismissRequest = { pageSizeDropdownExpanded = false }
                                ) {
                                    listOf(10, 25, 50, 100).forEach { size ->
                                        DropdownMenuItem(
                                            text = { Text("$size Transaksi", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) },
                                            onClick = {
                                                pageSize = size
                                                pageSizeDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Prev Button
                            val totalPages = maxOf(1, kotlin.math.ceil(totalCount.toDouble() / pageSize).toInt())
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(if (currentPage > 1) Color.White else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                    .clickable(enabled = currentPage > 1) { currentPage-- },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Prev", modifier = Modifier.size(16.dp), tint = Color.Black)
                            }

                            Text(
                                text = "$currentPage/$totalPages",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )

                            // Next Button
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(if (currentPage < totalPages) Color.White else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                    .clickable(enabled = currentPage < totalPages) { currentPage++ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Next", modifier = Modifier.size(16.dp), tint = Color.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada data transaksi", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        val startIndex = (currentPage - 1) * pageSize
                        val endIndex = minOf(startIndex + pageSize, totalCount)
                        val pagedList = transactions.subList(startIndex, endIndex)

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pagedList) { reportItem ->
                                TransactionRowItem(reportItem = reportItem)
                            }
                        }
                    }
                }
            }
        } else {
            // Portrait Mode: Single Scrollable LazyColumn for optimal scrolling compatibility
            val totalPages = maxOf(1, kotlin.math.ceil(totalCount.toDouble() / pageSize).toInt())
            val startIndex = (currentPage - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, totalCount)
            val pagedList = transactions.subList(startIndex, endIndex)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(start = 16.dp, end = 22.dp, top = 0.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Filter Presets Card
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, end = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 4.dp, y = 4.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FILTER TANGGAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black
                                )
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Pilih Tanggal",
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { selectCustomDateRange(context, reportVM) }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dateRangeText.uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { setTodayRange(reportVM) }
                                        .background(Color(0xFFE8D5FF), RoundedCornerShape(4.dp))
                                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("HARI INI", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { setWeekRange(reportVM) }
                                        .background(Color(0xFFE8D5FF), RoundedCornerShape(4.dp))
                                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("MINGGU", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .clickable { selectCustomDateRange(context, reportVM) }
                                        .background(Color(0xFFFFDE4D), RoundedCornerShape(4.dp))
                                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("PILIH TANGGAL", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }

                // Summary Stats Card
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, end = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 4.dp, y = 4.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFDE4D), RoundedCornerShape(4.dp))
                                .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "TOTAL PENDAPATAN",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rp ${String.format(Locale.getDefault(), "%,.0f", totalRevenue)}",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Count Stats Card
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, end = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 4.dp, y = 4.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(16.dp)
                        ) {
                            Text("TRANSAKSI SELESAI", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$totalCount KALI", style = MaterialTheme.typography.headlineMedium, color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // Cash vs QRIS Ratio Line Chart
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp, end = 4.dp)) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .offset(x = 4.dp, y = 4.dp)
                                .background(Color.Black, RoundedCornerShape(4.dp))
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "RASIO TUNAI vs QRIS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("- ", color = Color(0xFF2E7D32), fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                                    Text("Tunai: Rp ${String.format(Locale.getDefault(), "%,.0f", cashRevenue)} (${100 - (qrisPercentage * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("- ", color = Color(0xFFFF595E), fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                                    Text("QRIS: Rp ${String.format(Locale.getDefault(), "%,.0f", qrisRevenue)} (${(qrisPercentage * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall, color = Color.Black, fontWeight = FontWeight.Black)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Line Chart Canvas
                            RevenueLineChart(transactions = transactions)
                        }
                    }
                }

                // Export Actions buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { reportVM.exportCSV(context) }
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .border(2.2.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("EKSPOR CSV", style = MaterialTheme.typography.labelLarge, color = Color.Black, fontWeight = FontWeight.Black)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { reportVM.exportPDF(context) }
                                .background(Color(0xFF00F5D4), RoundedCornerShape(4.dp))
                                .border(2.2.dp, Color.Black, RoundedCornerShape(4.dp))
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("EKSPOR PDF", style = MaterialTheme.typography.labelLarge, color = Color.Black, fontWeight = FontWeight.Black)
                        }
                    }
                }

                // Transaction list header + Pagination in Portrait
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Riwayat Transaksi".uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.titleMedium, // Dikecilkan dari headlineSmall ke titleMedium
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )

                            // Pagination Controls (Kiri & Kanan saja untuk keterbacaan)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Prev Button
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(if (currentPage > 1) Color.White else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                        .clickable(enabled = currentPage > 1) { currentPage-- },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.KeyboardArrowLeft, contentDescription = "Prev", modifier = Modifier.size(16.dp), tint = Color.Black)
                                }

                                Text(
                                    text = "$currentPage/$totalPages",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )

                                // Next Button
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(if (currentPage < totalPages) Color.White else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                        .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                        .clickable(enabled = currentPage < totalPages) { currentPage++ },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Next", modifier = Modifier.size(16.dp), tint = Color.Black)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))

                        // Pilihan jumlah pagination dipindahkan ke bawah judul secara jelas
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "TAMPILKAN:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                            Box {
                                Box(
                                    modifier = Modifier
                                        .clickable { pageSizeDropdownExpanded = true }
                                        .background(Color.White, RoundedCornerShape(4.dp))
                                        .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "$pageSize TRANSAKSI", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Black)
                                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                                    }
                                }
                                DropdownMenu(
                                    expanded = pageSizeDropdownExpanded,
                                    onDismissRequest = { pageSizeDropdownExpanded = false }
                                ) {
                                    listOf(10, 25, 50, 100).forEach { size ->
                                        DropdownMenuItem(
                                            text = { Text("$size Transaksi", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold) },
                                            onClick = {
                                                pageSize = size
                                                pageSizeDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (transactions.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada data transaksi", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    items(pagedList) { reportItem ->
                        TransactionRowItem(reportItem = reportItem)
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueLineChart(transactions: List<ExportHelper.TransactionReportItem>) {
    // Canvas drawn simple Line Chart representing transactions trend - Tunai (Green), QRIS (Red)
    val cashPoints = remember(transactions) {
        val sorted = transactions.sortedBy { it.transaction.paymentDate }
        var currentSum = 0.0
        sorted.map { tx ->
            if (tx.transaction.paymentMethod == "CASH") {
                currentSum += tx.transaction.totalAmount
            }
            currentSum
        }
    }

    val qrisPoints = remember(transactions) {
        val sorted = transactions.sortedBy { it.transaction.paymentDate }
        var currentSum = 0.0
        sorted.map { tx ->
            if (tx.transaction.paymentMethod == "QRIS") {
                currentSum += tx.transaction.totalAmount
            }
            currentSum
        }
    }

    // Scrollable container for the chart
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        val chartWidth = maxOf(340.dp, (transactions.size * 25).dp)
        Canvas(
            modifier = Modifier
                .width(chartWidth)
                .height(130.dp)
                .background(Color(0xFFF4F3EF), RoundedCornerShape(4.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            val maxAmount = maxOf(
                1.0,
                cashPoints.lastOrNull() ?: 1.0,
                qrisPoints.lastOrNull() ?: 1.0
            )

            // Draw grid baseline
            drawLine(
                color = Color.Black.copy(alpha = 0.2f),
                start = androidx.compose.ui.geometry.Offset(0f, size.height),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height),
                strokeWidth = 2f
            )

            // Draw Tunai Line (Cash) -> Green (Color(0xFF2E7D32) / 0xFF00F5D4)
            if (cashPoints.isNotEmpty()) {
                val path = Path()
                val stepX = size.width / (cashPoints.size - 1).coerceAtLeast(1)
                path.moveTo(0f, size.height - (cashPoints[0] / maxAmount * size.height).toFloat())
                for (i in 1 until cashPoints.size) {
                    path.lineTo(
                        i * stepX,
                        size.height - (cashPoints[i] / maxAmount * size.height).toFloat()
                    )
                }
                drawPath(
                    path = path,
                    color = Color(0xFF2E7D32), // Green
                    style = Stroke(width = 8f)
                )
            }

            // Draw QRIS Line -> Red (Color(0xFFFF595E))
            if (qrisPoints.isNotEmpty()) {
                val path = Path()
                val stepX = size.width / (qrisPoints.size - 1).coerceAtLeast(1)
                path.moveTo(0f, size.height - (qrisPoints[0] / maxAmount * size.height).toFloat())
                for (i in 1 until qrisPoints.size) {
                    path.lineTo(
                        i * stepX,
                        size.height - (qrisPoints[i] / maxAmount * size.height).toFloat()
                    )
                }
                drawPath(
                    path = path,
                    color = Color(0xFFFF595E), // Red
                    style = Stroke(width = 8f)
                )
            }
        }
    }
}

private fun setTodayRange(reportVM: ReportViewModel) {
    val c = Calendar.getInstance()
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    val start = c.timeInMillis
    c.set(Calendar.HOUR_OF_DAY, 23)
    c.set(Calendar.MINUTE, 59)
    c.set(Calendar.SECOND, 59)
    c.set(Calendar.MILLISECOND, 999)
    val end = c.timeInMillis
    reportVM.setDateRange(start, end)
}

private fun setWeekRange(reportVM: ReportViewModel) {
    val c = Calendar.getInstance()
    c.set(Calendar.DAY_OF_WEEK, c.firstDayOfWeek)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    val start = c.timeInMillis
    val end = System.currentTimeMillis()
    reportVM.setDateRange(start, end)
}

private fun selectCustomDateRange(
    context: android.content.Context,
    reportVM: ReportViewModel
) {
    val calendar = Calendar.getInstance()
    
    val startDialog = android.app.DatePickerDialog(
        context,
        { _, startYear, startMonth, startDay ->
            val startCal = Calendar.getInstance()
            startCal.set(startYear, startMonth, startDay, 0, 0, 0)
            startCal.set(Calendar.MILLISECOND, 0)
            val startTime = startCal.timeInMillis

            val endDialog = android.app.DatePickerDialog(
                context,
                { _, endYear, endMonth, endDay ->
                    val endCal = Calendar.getInstance()
                    endCal.set(endYear, endMonth, endDay, 23, 59, 59)
                    endCal.set(Calendar.MILLISECOND, 999)
                    var endTime = endCal.timeInMillis

                    if (endTime < startTime) {
                        Toast.makeText(context, "Tanggal akhir tidak boleh sebelum tanggal mulai", Toast.LENGTH_LONG).show()
                        return@DatePickerDialog
                     }

                     val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
                     if (endTime - startTime > thirtyDaysInMillis) {
                         Toast.makeText(context, "Rentang tanggal maksimal 30 hari. Tanggal akhir disesuaikan otomatis.", Toast.LENGTH_LONG).show()
                         val cappedEndCal = Calendar.getInstance()
                         cappedEndCal.timeInMillis = startTime
                         cappedEndCal.add(Calendar.DAY_OF_YEAR, 29)
                         cappedEndCal.set(Calendar.HOUR_OF_DAY, 23)
                         cappedEndCal.set(Calendar.MINUTE, 59)
                         cappedEndCal.set(Calendar.SECOND, 59)
                         cappedEndCal.set(Calendar.MILLISECOND, 999)
                         endTime = cappedEndCal.timeInMillis
                     }

                     reportVM.setDateRange(startTime, endTime)
                },
                startYear, startMonth, startDay
            )
            endDialog.setTitle("Pilih Tanggal Akhir")
            endDialog.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    startDialog.setTitle("Pilih Tanggal Mulai")
    startDialog.show()
}

@Composable
fun TransactionRowItem(reportItem: ExportHelper.TransactionReportItem) {
    var showDetailDialog by remember { mutableStateOf(false) }
    val tx = reportItem.transaction
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    
    val badgeBg = if (tx.paymentMethod == "QRIS") Color(0xFFFF595E) else Color(0xFF2E7D32)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp, end = 4.dp)
            .clickable { showDetailDialog = true }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(Color.Black, RoundedCornerShape(4.dp))
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(4.dp))
                .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                .padding(16.dp)
        ) {
            // Main info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = reportItem.customerName.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                    Text(
                        text = dateFormatter.format(Date(tx.paymentDate)).uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Rp ${String.format(Locale.getDefault(), "%,.0f", tx.totalAmount)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                        Box(
                            modifier = Modifier
                                .background(badgeBg, RoundedCornerShape(2.dp))
                                .border(1.dp, Color.Black, RoundedCornerShape(2.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tx.paymentMethod,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Lihat detail",
                        tint = Color.Black
                    )
                }
            }
        }
    }

    if (showDetailDialog) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showDetailDialog = false }) {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("pos_settings", android.content.Context.MODE_PRIVATE) }
            val isPrinterActive = prefs.getBoolean("is_printer_active", false)
            val selectedPrinterAddress = prefs.getString("selected_printer_address", "") ?: ""

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
                        .border(3.dp, Color.Black, RoundedCornerShape(8.dp))
                        .padding(20.dp)
                ) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8D5FF))
                            .border(2.dp, Color.Black, RoundedCornerShape(2.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "DETAIL TRANSAKSI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("PELANGGAN: ${reportItem.customerName.uppercase(Locale.getDefault())}", fontWeight = FontWeight.Black, color = Color.Black)
                    Text("TANGGAL: ${dateFormatter.format(Date(tx.paymentDate)).uppercase(Locale.getDefault())}", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 12.sp)
                    Text("METODE BAYAR: ${tx.paymentMethod}", fontWeight = FontWeight.Bold, color = Color.DarkGray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.Black, thickness = 1.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("RINCIAN PEMBELIAN:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = Color.Black)
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        reportItem.details.forEach { detail ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${detail.qty}x ${detail.productNameSnapshot}".uppercase(Locale.getDefault()),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1.5f)
                                )
                                Text(
                                    text = "Rp ${String.format(Locale.getDefault(), "%,.0f", detail.priceSnapshot)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                                Text(
                                    text = "Rp ${String.format(Locale.getDefault(), "%,.0f", detail.subtotal)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.weight(1.2f),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.Black, thickness = 1.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL TRANSAKSI:", fontWeight = FontWeight.Black, color = Color.Black)
                        Text("Rp ${String.format(Locale.getDefault(), "%,.0f", tx.totalAmount)}", fontWeight = FontWeight.Black, color = Color.Black)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isPrinterActive) {
                            Box(
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(44.dp)
                                    .clickable {
                                        printReprintReceipt(
                                            context = context,
                                            reportItem = reportItem,
                                            selectedPrinterAddress = selectedPrinterAddress
                                        )
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
                                        .background(Color(0xFFFFDE4D), RoundedCornerShape(4.dp))
                                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("CETAK STRUK", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = Color.Black)
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable { showDetailDialog = false }
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
                                    .background(Color(0xFF00F5D4), RoundedCornerShape(4.dp))
                                    .border(2.dp, Color.Black, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("TUTUP", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}

fun printReprintReceipt(
    context: android.content.Context,
    reportItem: ExportHelper.TransactionReportItem,
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
        val prefs = context.getSharedPreferences("pos_settings", android.content.Context.MODE_PRIVATE)
        val storeName = prefs.getString("store_name", "POS KASIR QRIS") ?: "POS KASIR QRIS"
        val storeFooter = prefs.getString("store_footer", "Layanan POS Kasir QRIS Offline") ?: "Layanan POS Kasir QRIS Offline"
        val logoUriString = prefs.getString("store_logo_uri", "") ?: ""

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
        val formattedOrderDate = dateFormatter.format(java.util.Date(reportItem.transaction.paymentDate))
        val formattedPrintDate = dateFormatter.format(java.util.Date())

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
                    // Resize to fit printer thermal (max standard width ~200px)
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

        textToPrint.append("[C]* SALINAN STRUK PEMBAYARAN *\n")
        textToPrint.append("[C]================================\n")
        textToPrint.append("[L]Tgl Pesan : $formattedOrderDate\n")
        textToPrint.append("[L]Tgl Cetak : $formattedPrintDate\n")
        textToPrint.append("[L]Pelanggan : ${reportItem.customerName.ifBlank { "Umum" }}\n")
        textToPrint.append("[L]Bayar     : ${reportItem.transaction.paymentMethod}\n")
        textToPrint.append("[C]--------------------------------\n")

        reportItem.details.forEach { detail ->
            val subtotal = detail.priceSnapshot * detail.qty
            textToPrint.append("[L]${detail.productNameSnapshot}\n")
            textToPrint.append("[L]  ${detail.qty} x Rp ${String.format(java.util.Locale.getDefault(), "%,.0f", detail.priceSnapshot)}[R]Rp ${String.format(java.util.Locale.getDefault(), "%,.0f", subtotal)}\n")
        }

        textToPrint.append("[C]--------------------------------\n")
        textToPrint.append("[L]<b>TOTAL[R]Rp ${String.format(java.util.Locale.getDefault(), "%,.0f", reportItem.transaction.totalAmount)}</b>\n")
        textToPrint.append("[C]================================\n")
        textToPrint.append("[C]Terima Kasih atas\n")
        textToPrint.append("[C]Kunjungan Anda!\n")
        
        // Multi-line center footer
        storeFooter.split("\n").forEach { line ->
            if (line.trim().isNotEmpty()) {
                textToPrint.append("[C]${line.trim()}\n")
            }
        }
        textToPrint.append("\n\n\n")

        printer.printFormattedText(textToPrint.toString())
        android.widget.Toast.makeText(context, "Struk salinan berhasil dicetak!", android.widget.Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Error printer: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
    }
}
