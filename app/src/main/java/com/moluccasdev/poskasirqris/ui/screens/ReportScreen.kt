package com.moluccasdev.poskasirqris.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
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
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
            ) {
                Text(
                    text = "Dashboard Laporan",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Katalog riwayat penjualan kasir lunas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
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
                    .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp)
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FILTER TANGGAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Pilih Tanggal",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { selectCustomDateRange(context, reportVM) }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dateRangeText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { setTodayRange(reportVM) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                            ) {
                                Text("Hari Ini", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = { setWeekRange(reportVM) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                            ) {
                                Text("Minggu", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = { selectCustomDateRange(context, reportVM) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                                modifier = Modifier.weight(1.3f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                            ) {
                                Text("Pilih Tanggal", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // Summary Stats Cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "TOTAL PENDAPATAN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Rp ${String.format(Locale.getDefault(), "%,.0f", totalRevenue)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Count Stats Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("TRANSAKSI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$totalCount kali", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                // Cash vs QRIS Ratio Progress Indicator
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "RASIO TUNAI vs QRIS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tunai (${100 - (qrisPercentage * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall)
                            Text("QRIS (${(qrisPercentage * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        LinearProgressIndicator(
                            progress = { qrisPercentage },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }

                // Export Actions bottom row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { reportVM.exportCSV(context) },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outlineVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ekspor CSV", style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = { reportVM.exportPDF(context) },
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ekspor PDF", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Right Column: Transaction List Workspace
            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEADDFF).copy(alpha = 0.35f))
                    .padding(16.dp)
            ) {
                Text(
                    text = "Riwayat Transaksi",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada data transaksi", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactions) { reportItem ->
                            TransactionRowItem(reportItem = reportItem)
                        }
                    }
                }
            }
        }
        } else {
            // Portrait Mode: Single Scrollable LazyColumn for optimal scrolling compatibility
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

            // Filter Presets Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FILTER TANGGAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Pilih Tanggal",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { selectCustomDateRange(context, reportVM) }
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dateRangeText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { setTodayRange(reportVM) },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                            ) {
                                Text("Hari Ini", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = { setWeekRange(reportVM) },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                            ) {
                                Text("Minggu Ini", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = { selectCustomDateRange(context, reportVM) },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1.3f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                            ) {
                                Text("Pilih Tanggal", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Summary Stats Cards
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "TOTAL PENDAPATAN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rp ${String.format(Locale.getDefault(), "%,.0f", totalRevenue)}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Count Stats Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TRANSAKSI SAKSES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$totalCount kali", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Cash vs QRIS Ratio Progress Indicator
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "RASIO TUNAI vs QRIS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tunai (${100 - (qrisPercentage * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall)
                            Text("QRIS (${(qrisPercentage * 100).toInt()}%)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        LinearProgressIndicator(
                            progress = { qrisPercentage },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                }
            }

            // Export Actions buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { reportVM.exportCSV(context) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outlineVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ekspor CSV", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = { reportVM.exportPDF(context) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ekspor PDF", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Transaction list header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Riwayat Transaksi",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada data transaksi", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                items(transactions) { reportItem ->
                    TransactionRowItem(reportItem = reportItem)
                }
            }
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

private fun setMonthRange(reportVM: ReportViewModel) {
    val c = Calendar.getInstance()
    c.set(Calendar.DAY_OF_MONTH, 1)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    var start = c.timeInMillis
    val end = System.currentTimeMillis()
    
    val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
    if (end - start > thirtyDaysInMillis) {
        val startCal = Calendar.getInstance()
        startCal.timeInMillis = end
        startCal.add(Calendar.DAY_OF_YEAR, -29)
        startCal.set(Calendar.HOUR_OF_DAY, 0)
        startCal.set(Calendar.MINUTE, 0)
        startCal.set(Calendar.SECOND, 0)
        startCal.set(Calendar.MILLISECOND, 0)
        start = startCal.timeInMillis
    }
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
    var isExpanded by remember { mutableStateOf(false) }
    val tx = reportItem.transaction
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Main info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = reportItem.customerName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormatter.format(Date(tx.paymentDate)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Rp ${String.format(Locale.getDefault(), "%,.0f", tx.totalAmount)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = tx.paymentMethod,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (tx.paymentMethod == "QRIS") MaterialTheme.colorScheme.primary else Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand details",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Expandable details block (snap pricing & detail mapping)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "RINCIAN PEMBELIAN:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    reportItem.details.forEach { detail ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${detail.qty}x ${detail.productNameSnapshot}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1.5f)
                            )
                            Text(
                                text = "Rp ${String.format(Locale.getDefault(), "%,.0f", detail.priceSnapshot)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                            Text(
                                text = "Rp ${String.format(Locale.getDefault(), "%,.0f", detail.subtotal)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1.2f),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
        }
    }
}
