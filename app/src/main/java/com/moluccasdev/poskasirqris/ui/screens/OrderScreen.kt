package com.moluccasdev.poskasirqris.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material.icons.filled.Edit
import com.moluccasdev.poskasirqris.data.OrderEntity
import com.moluccasdev.poskasirqris.data.ProductEntity
import com.moluccasdev.poskasirqris.ui.CalculatorViewModel
import com.moluccasdev.poskasirqris.ui.CartItem
import com.moluccasdev.poskasirqris.ui.DraftOrderDisplay
import com.moluccasdev.poskasirqris.ui.OrderViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 1. MAIN ORDER SCREEN: Active draft orders list with search filter and a FAB
 */
@Composable
fun OrderScreen(
    orderVM: OrderViewModel,
    onAddOrderClick: () -> Unit,
    onEditOrderClick: () -> Unit,
    onSelectOrderForPayment: () -> Unit
) {
    val drafts by orderVM.draftOrders.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var orderToDelete by remember { mutableStateOf<OrderEntity?>(null) }
    
    val filteredDrafts = drafts.filter {
        it.order.customerNameOrTable.contains(searchQuery, ignoreCase = true)
    }

    if (showDeleteDialog && orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { 
                Text(
                    text = "KONFIRMASI HAPUS", 
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                ) 
            },
            text = { 
                Text(
                    text = "APAKAH ANDA YAKIN INGIN MENGHAPUS PESANAN '${orderToDelete?.customerNameOrTable?.uppercase(Locale.getDefault())}'? TINDAKAN INI TIDAK DAPAT DIBATALKAN.",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                ) 
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFF595E), RoundedCornerShape(4.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                        .clickable {
                            orderToDelete?.let { orderVM.deleteDraft(it) }
                            showDeleteDialog = false
                            orderToDelete = null
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("HAPUS PESANAN", color = Color.Black, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .background(Color.White, RoundedCornerShape(4.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                        .clickable { showDeleteDialog = false }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("BATAL", color = Color.Black, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                }
            },
            shape = RoundedCornerShape(4.dp),
            containerColor = Color.White,
            modifier = Modifier.border(3.dp, Color.Black, RoundedCornerShape(4.dp))
        )
    }

    Scaffold(
        containerColor = Color(0xFFF4F3EF), // Neo-Brutalism vintage gallery paper background
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF4F3EF))
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
            ) {
                // Neo-Brutalism header sticker block
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFDE4D))
                        .border(3.dp, Color.Black, RoundedCornerShape(2.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "PESANAN BERJALAN",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Daftar draft transaksi aktif kasir belum lunas",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        floatingActionButton = {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .offset(x = (-4).dp, y = (-4).dp)
            ) {
                // Shadow layer
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(Color.Black, RoundedCornerShape(4.dp))
                )
                // FAB Button
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color(0xFF00F5D4), RoundedCornerShape(4.dp))
                        .border(3.dp, Color.Black, RoundedCornerShape(4.dp))
                        .clickable {
                            orderVM.clearCart() // Start with a completely fresh cart
                            onAddOrderClick()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Pesanan Baru",
                        tint = Color.Black,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 16.dp, end = 22.dp, top = 0.dp, bottom = 8.dp) // extra right padding to fit row item shadows
        ) {

            // Search filter with Neo-Brutalism look
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, end = 6.dp) // Room for shadow
            ) {
                // Shadow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 5.dp, y = 5.dp)
                        .background(Color.Black, RoundedCornerShape(4.dp))
                )
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("CARI PESANAN NAMA / KURSI...", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", tint = Color.Black) },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(3.dp, Color.Black, RoundedCornerShape(4.dp)),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
            }

            // Drafts list grid/column
            if (filteredDrafts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "BELUM ADA PESANAN BERJALAN.\nTEKAN TOMBOL + UNTUK MEMBUAT BARU." else "TIDAK ADA PESANAN COCOK DENGAN PENCARIAN",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Room for FAB
                ) {
                    items(filteredDrafts) { draft ->
                        DraftOrderRowItem(
                            draft = draft,
                            onSelect = {
                                orderVM.selectDraft(draft.order)
                                onSelectOrderForPayment()
                            },
                            onEdit = {
                                orderVM.selectDraft(draft.order)
                                onEditOrderClick()
                            },
                            onDelete = { 
                                orderToDelete = draft.order
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DraftOrderRowItem(
    draft: DraftOrderDisplay,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, end = 8.dp) // Leave room for shadow
    ) {
        // Shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 6.dp, y = 6.dp)
                .background(Color.Black, RoundedCornerShape(4.dp))
        )
        
        // Content layer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .border(3.dp, Color.Black, RoundedCornerShape(4.dp))
                .clickable { onSelect() }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = draft.order.customerNameOrTable.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Brutalist badge for total items
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFE8D5FF), RoundedCornerShape(2.dp))
                            .border(1.5.dp, Color.Black, RoundedCornerShape(2.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${draft.totalItems} ITEM",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }
                    
                    // Brutalist price highlight
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFD166), RoundedCornerShape(2.dp))
                            .border(1.5.dp, Color.Black, RoundedCornerShape(2.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Rp ${String.format(Locale.getDefault(), "%,.0f", draft.totalPrice)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "DIBUAT: ${dateFormatter.format(java.util.Date(draft.order.createdAt)).uppercase(Locale.getDefault())}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Square Brutalist Button for Edit
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF4EA8DE), RoundedCornerShape(4.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                        .clickable { onEdit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Pesanan",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                // Square Brutalist Button for Delete
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFFF595E), RoundedCornerShape(4.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Pesanan",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


/**
 * 2. NEW CATALOG & CHECKOUT SCREEN: Neo-Brutalism style full screen workspace
 */
@Composable
fun CreateOrderScreen(
    orderVM: OrderViewModel,
    calcVM: CalculatorViewModel,
    onNavigateToPayment: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val products by orderVM.activeProducts.collectAsState()
    
    var catalogSearch by remember { mutableStateOf("") }
    val filteredProducts = products.filter {
        it.name.contains(catalogSearch, ignoreCase = true)
    }

    var isCartExpanded by remember { mutableStateOf(false) }

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
                        .clickable {
                            orderVM.clearCart()
                            onNavigateBack()
                        },
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
                        text = if (orderVM.currentOrderId == 0L) "BUAT PESANAN BARU" else "EDIT DETAIL PESANAN",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF4F3EF))
        ) {

            // Product search bar with drop shadow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp, end = 4.dp) // Room for shadow
            ) {
                // Shadow
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(Color.Black, RoundedCornerShape(4.dp))
                )
                
                OutlinedTextField(
                    value = catalogSearch,
                    onValueChange = { catalogSearch = it },
                    placeholder = { Text("CARI BARANG KATALOG...", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari", tint = Color.Black) },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp)),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
            }

            // Catalog Grid Workspace
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 4.dp)
            ) {
                if (filteredProducts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("BARANG TIDAK DITEMUKAN", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, color = Color.Black)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(if (isLandscape) 4 else 2),
                        contentPadding = PaddingValues(bottom = 16.dp, end = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredProducts) { product ->
                            ProductCard(
                                product = product,
                                onProductClick = { orderVM.addToCart(product) }
                            )
                        }
                    }
                }
            }

            // STICKY BOTTOM DRAWER (Vibrant Lavender, Scrollable & Expandable)
            val panelHeight = if (isCartExpanded) {
                if (isLandscape) 220.dp else 400.dp
            } else {
                if (isLandscape) 115.dp else 165.dp
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(panelHeight)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color(0xFFE8D5FF)) // Brutalist Lavender background
                    .border(3.dp, Color.Black, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header Drawer: Customer input, Grand total, and Expand toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.1f)) {
                        OutlinedTextField(
                            value = orderVM.currentCustomerName,
                            onValueChange = { orderVM.currentCustomerName = it },
                            placeholder = { Text("NAMA / NO. MEJA...", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.Gray) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .border(2.dp, Color.Black, RoundedCornerShape(4.dp)),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(0.9f)
                    ) {
                        // Total price highlight badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFD166), RoundedCornerShape(2.dp))
                                .border(1.5.dp, Color.Black, RoundedCornerShape(2.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Rp ${String.format(Locale.getDefault(), "%,.0f", orderVM.cartTotal)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))

                        // Cart expand toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isCartExpanded = !isCartExpanded }
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isCartExpanded) "TUTUP DETAIL" else "LIHAT DETAIL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "(${orderVM.cartItemCount} ITEM)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isCartExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = "Detail",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Scrollable cart items list when drawer is expanded
                if (isCartExpanded) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(orderVM.cart) { item ->
                            CartItemRow(
                                item = item,
                                onAdd = { orderVM.addToCart(item.product) },
                                onRemove = { orderVM.removeFromCart(item.product) },
                                onQtyChange = { newQty -> orderVM.updateCartQty(item.product, newQty) }
                            )
                            HorizontalDivider(color = Color.Black.copy(alpha = 0.2f), thickness = 1.dp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Bottom Action buttons: Simpan & Bayar in Neo-Brutalism layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Simpan Draft Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        val isEnabled = orderVM.currentCustomerName.isNotBlank() && orderVM.cart.isNotEmpty()
                        
                        if (isEnabled) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 3.dp, y = 3.dp)
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    if (isEnabled) Color(0xFFFF9F1C) else Color.LightGray.copy(alpha = 0.5f),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    2.dp,
                                    if (isEnabled) Color.Black else Color.Gray,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable(enabled = isEnabled) {
                                    orderVM.saveAsDraft {
                                        Toast.makeText(context, "Pesanan disimpan sebagai draft!", Toast.LENGTH_SHORT).show()
                                        onNavigateBack()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "SIMPAN DRAFT", 
                                style = MaterialTheme.typography.labelLarge, 
                                fontWeight = FontWeight.Black,
                                color = if (isEnabled) Color.Black else Color.Gray
                            )
                        }
                    }

                    // Bayar Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        val isEnabled = orderVM.cart.isNotEmpty() && orderVM.currentCustomerName.isNotBlank()
                        
                        if (isEnabled) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .offset(x = 3.dp, y = 3.dp)
                                    .background(Color.Black, RoundedCornerShape(4.dp))
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    if (isEnabled) Color(0xFF00F5D4) else Color.LightGray.copy(alpha = 0.5f),
                                    RoundedCornerShape(4.dp)
                                )
                                .border(
                                    2.dp,
                                    if (isEnabled) Color.Black else Color.Gray,
                                    RoundedCornerShape(4.dp)
                                )
                                .clickable(enabled = isEnabled) {
                                    orderVM.saveAsDraft(clearAfterSave = false) {
                                        calcVM.prepareCheckout(orderVM.cartTotal)
                                        onNavigateToPayment()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "BAYAR", 
                                style = MaterialTheme.typography.labelLarge, 
                                fontWeight = FontWeight.Black,
                                color = if (isEnabled) Color.Black else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    onProductClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp, end = 6.dp) // Room for shadow
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 5.dp, y = 5.dp)
                .background(Color.Black, RoundedCornerShape(4.dp))
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White)
                .border(2.5.dp, Color.Black, RoundedCornerShape(4.dp))
                .clickable { onProductClick() }
        ) {
            val imageRequest = ImageRequest.Builder(LocalContext.current)
                .data(product.imagePath?.let { File(it) })
                .crossfade(true)
                .build()

            if (product.imagePath != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp)
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(105.dp)
                        .background(Color(0xFFE8D5FF)) // Pastel purple placeholder
                        .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = product.name.take(2).uppercase(Locale.getDefault()),
                        color = Color.Black,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            androidx.compose.material3.HorizontalDivider(color = Color.Black, thickness = 2.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = product.name.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                // Brutalist yellow price tag
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .background(Color(0xFFFFD166), RoundedCornerShape(2.dp))
                        .border(1.dp, Color.Black, RoundedCornerShape(2.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Rp ${String.format(Locale.getDefault(), "%,.0f", product.price)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onQtyChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1.1f)) {
            Text(
                text = item.product.name.uppercase(Locale.getDefault()),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black
            )
            Text(
                text = "Rp ${String.format(Locale.getDefault(), "%,.0f", item.product.price)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Black.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Square Minus/Delete Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFFFF595E), RoundedCornerShape(4.dp))
                    .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp))
                    .clickable { onRemove() }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Kurang",
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
            }

            var textState by remember(item.qty) { mutableStateOf(item.qty.toString()) }

            // Brutalist quantity text area
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(28.dp)
                    .background(Color.White, RoundedCornerShape(2.dp))
                    .border(1.5.dp, Color.Black, RoundedCornerShape(2.dp))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = textState,
                    onValueChange = { newVal ->
                        val filtered = newVal.filter { it.isDigit() }
                        textState = filtered
                        val parsed = filtered.toIntOrNull() ?: 0
                        onQtyChange(parsed)
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    ),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Square Add Button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .background(Color(0xFF00F5D4), RoundedCornerShape(4.dp))
                    .border(1.5.dp, Color.Black, RoundedCornerShape(4.dp))
                    .clickable { onAdd() }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah",
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
