package com.moluccasdev.poskasirqris.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moluccasdev.poskasirqris.data.AppRepository
import com.moluccasdev.poskasirqris.data.OrderDetailEntity
import com.moluccasdev.poskasirqris.data.OrderEntity
import com.moluccasdev.poskasirqris.data.ProductEntity
import com.moluccasdev.poskasirqris.data.QrisEntity
import com.moluccasdev.poskasirqris.data.TransactionEntity
import com.moluccasdev.poskasirqris.util.ExportHelper
import com.moluccasdev.poskasirqris.util.QrCodeGenerator
import com.moluccasdev.poskasirqris.util.QrisEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

// Item in the current Order Cart
data class CartItem(
    val product: ProductEntity,
    val qty: Int
)

data class DraftOrderDisplay(
    val order: OrderEntity,
    val totalItems: Int,
    val totalPrice: Double
)

class OrderViewModel(private val repository: AppRepository) : ViewModel() {
    val activeProducts: StateFlow<List<ProductEntity>> = repository.getAllActiveProductsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Mapped flow to include items count and total price for draft list
    val draftOrders: StateFlow<List<DraftOrderDisplay>> = repository.getDraftOrdersFlow()
        .map { list ->
            list.map { order ->
                val details = repository.getOrderDetails(order.id)
                DraftOrderDisplay(
                    order = order,
                    totalItems = details.sumOf { it.qty },
                    totalPrice = details.sumOf { it.subtotal }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var currentCustomerName by mutableStateOf("")
    var currentOrderId by mutableStateOf(0L)
    val cart = mutableStateListOf<CartItem>()

    val cartTotal: Double
        get() = cart.sumOf { it.product.price * it.qty }

    val cartItemCount: Int
        get() = cart.sumOf { it.qty }

    fun addToCart(product: ProductEntity) {
        val index = cart.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val item = cart[index]
            cart[index] = item.copy(qty = item.qty + 1)
        } else {
            cart.add(CartItem(product, 1))
        }
    }

    fun removeFromCart(product: ProductEntity) {
        val index = cart.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val item = cart[index]
            if (item.qty > 1) {
                cart[index] = item.copy(qty = item.qty - 1)
            } else {
                cart.removeAt(index)
            }
        }
    }

    fun updateCartQty(product: ProductEntity, qty: Int) {
        val index = cart.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            if (qty > 0) {
                cart[index] = cart[index].copy(qty = qty)
            } else {
                cart.removeAt(index)
            }
        } else if (qty > 0) {
            cart.add(CartItem(product, qty))
        }
    }

    fun clearCart() {
        cart.clear()
        currentCustomerName = ""
        currentOrderId = 0L
    }

    fun selectDraft(order: OrderEntity) {
        viewModelScope.launch {
            currentOrderId = order.id
            currentCustomerName = order.customerNameOrTable
            cart.clear()
            val details = repository.getOrderDetails(order.id)
            val allProd = repository.getAllProductsFlow().stateIn(viewModelScope).value
            
            for (detail in details) {
                // Try to find product, or create a mock product snapshot if deleted
                val prod = allProd.find { it.id == detail.productId } 
                    ?: ProductEntity(id = detail.productId ?: 0, name = detail.productNameSnapshot, price = detail.priceSnapshot, imagePath = null, isActive = false)
                cart.add(CartItem(prod.copy(price = detail.priceSnapshot, name = detail.productNameSnapshot), detail.qty))
            }
        }
    }

    fun saveAsDraft(clearAfterSave: Boolean = true, onComplete: () -> Unit) {
        if (currentCustomerName.isBlank()) return
        viewModelScope.launch {
            val order = OrderEntity(
                id = currentOrderId,
                customerNameOrTable = currentCustomerName,
                status = "DRAFT"
            )
            val details = cart.map {
                OrderDetailEntity(
                    orderId = currentOrderId,
                    productId = it.product.id,
                    productNameSnapshot = it.product.name,
                    priceSnapshot = it.product.price,
                    qty = it.qty,
                    subtotal = it.product.price * it.qty
                )
            }
            val savedOrderId = repository.saveOrder(order, details)
            if (currentOrderId == 0L) {
                currentOrderId = savedOrderId
            }
            if (clearAfterSave) {
                clearCart()
            }
            onComplete()
        }
    }

    fun deleteDraft(order: OrderEntity) {
        viewModelScope.launch {
            repository.deleteOrder(order)
            if (currentOrderId == order.id) {
                clearCart()
            }
        }
    }
}

class CalculatorViewModel(private val repository: AppRepository) : ViewModel() {
    var displayExpression by mutableStateOf("")
    var displayResult by mutableStateOf("0")

    var checkoutAmount by mutableStateOf(0.0)
    var currentPaymentMethod by mutableStateOf("") // "CASH", "QRIS"
    
    // Cash payment specific
    var cashPaidAmount by mutableStateOf("")
    val cashChangeAmount: Double
        get() {
            val paid = cashPaidAmount.toDoubleOrNull() ?: 0.0
            return (paid - checkoutAmount).coerceAtLeast(0.0)
        }

    // QRIS payment specific
    var isQrisLoading by mutableStateOf(false)
    var activeQrisString by mutableStateOf("")
    var activeQrisBitmap by mutableStateOf<Bitmap?>(null)
    var merchantName by mutableStateOf("")

    fun onKeyPress(key: String) {
        when (key) {
            "C" -> {
                displayExpression = ""
                displayResult = "0"
            }
            "=" -> {
                evaluateExpression()
            }
            "+", "-", "*", "/" -> {
                if (displayExpression.isNotEmpty()) {
                    val last = displayExpression.last()
                    if (last == '+' || last == '-' || last == '*' || last == '/') {
                        displayExpression = displayExpression.dropLast(1) + key
                    } else {
                        displayExpression += key
                    }
                }
            }
            else -> {
                if (displayExpression == "0") {
                    displayExpression = key
                } else {
                    displayExpression += key
                }
                evaluateQuick()
            }
        }
    }

    private fun evaluateQuick() {
        try {
            val result = evaluate(displayExpression)
            displayResult = String.format(java.util.Locale.US, "%.0f", result)
        } catch (e: Exception) {
            // Ignore error in real-time display
        }
    }

    private fun evaluateExpression() {
        try {
            val result = evaluate(displayExpression)
            displayExpression = String.format(java.util.Locale.US, "%.0f", result)
            displayResult = displayExpression
        } catch (e: Exception) {
            displayResult = "Error"
        }
    }

    private fun evaluate(str: String): Double {
        // A basic mathematical expression evaluator (supporting standard double operators)
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < str.length) str[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm() // addition
                    else if (eat('-'.code)) x -= parseTerm() // subtraction
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) x *= parseFactor() // multiplication
                    else if (eat('/'.code)) x /= parseFactor() // division
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor() // unary plus
                if (eat('-'.code)) return -parseFactor() // unary minus

                var x: Double
                val startPos = this.pos
                if (eat('('.code)) { // parentheses
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
                    while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
                    x = java.lang.Double.parseDouble(str.substring(startPos, this.pos))
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                return x
            }
        }.parse()
    }

    fun prepareCheckout(amount: Double) {
        checkoutAmount = amount
        cashPaidAmount = ""
        activeQrisString = ""
        activeQrisBitmap = null
        currentPaymentMethod = ""
    }

    fun selectPaymentMethod(method: String) {
        currentPaymentMethod = method
        if (method == "QRIS") {
            generateDynamicQris()
        }
    }

    private fun generateDynamicQris() {
        isQrisLoading = true
        viewModelScope.launch {
            val qrisConfig = repository.getDefaultQris()
            if (qrisConfig != null) {
                merchantName = qrisConfig.merchantName
                val dynamicString = QrisEngine.generateDynamicQris(qrisConfig.rawQrisString, checkoutAmount)
                activeQrisString = dynamicString
                activeQrisBitmap = QrCodeGenerator.generateQrCode(dynamicString, 512)
            } else {
                merchantName = "Belum Diatur"
                activeQrisString = ""
                activeQrisBitmap = null
            }
            isQrisLoading = false
        }
    }

    fun checkout(orderId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            if (orderId == 0L) {
                // Dynamic payment without order draft
                onComplete()
            } else {
                repository.completeCheckout(orderId, checkoutAmount, currentPaymentMethod)
                onComplete()
            }
        }
    }
}

class ReportViewModel(private val repository: AppRepository) : ViewModel() {
    var startDate by mutableStateOf(getStartOfDay())
    var endDate by mutableStateOf(getEndOfDay())
    
    private val _transactionsList = MutableStateFlow<List<ExportHelper.TransactionReportItem>>(emptyList())
    val transactionsList = _transactionsList.asStateFlow()

    var exportUri by mutableStateOf<Uri?>(null)
    var exportStatusMessage by mutableStateOf("")

    fun fetchReports() {
        viewModelScope.launch {
            repository.getTransactionsInRangeFlow(startDate, endDate).collect { txs ->
                val reports = txs.map { tx ->
                    val order = repository.getOrderById(tx.orderId)
                    val details = repository.getOrderDetails(tx.orderId)
                    ExportHelper.TransactionReportItem(
                        transaction = tx,
                        customerName = order?.customerNameOrTable ?: "Pesanan Kilat",
                        itemCount = details.sumOf { it.qty },
                        details = details
                    )
                }
                _transactionsList.value = reports
            }
        }
    }

    fun setDateRange(start: Long, end: Long) {
        startDate = start
        endDate = end
        fetchReports()
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    fun exportCSV(context: Context) {
        viewModelScope.launch {
            val uri = ExportHelper.exportToCsv(context, _transactionsList.value)
            exportUri = uri
            exportStatusMessage = if (uri != null) "CSV Berhasil diekspor ke folder Downloads!" else "Gagal mengekspor CSV."
        }
    }

    fun exportPDF(context: Context) {
        viewModelScope.launch {
            val uri = ExportHelper.exportToPdf(context, _transactionsList.value)
            exportUri = uri
            exportStatusMessage = if (uri != null) "PDF Berhasil diekspor ke folder Downloads!" else "Gagal mengekspor PDF."
        }
    }
}

class SettingsViewModel(private val repository: AppRepository) : ViewModel() {
    val allProducts: StateFlow<List<ProductEntity>> = repository.getAllProductsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val qrisList: StateFlow<List<QrisEntity>> = repository.getAllQrisFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveProduct(id: Int, name: String, price: Double, imagePath: String?) {
        viewModelScope.launch {
            val product = ProductEntity(
                id = id,
                name = name,
                price = price,
                imagePath = imagePath,
                isActive = true
            )
            if (id == 0) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
            }
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProductSoft(product)
        }
    }

    fun saveQris(id: Int, merchantNameInput: String, rawQris: String, isDefault: Boolean) {
        viewModelScope.launch {
            // Auto detect merchant name from string if blank
            val finalName = if (merchantNameInput.isBlank()) {
                QrisEngine.extractMerchantName(rawQris)
            } else {
                merchantNameInput
            }
            val qris = QrisEntity(
                id = id,
                merchantName = finalName,
                rawQrisString = rawQris,
                isDefault = isDefault
            )
            
            val newId = if (id == 0) {
                repository.insertQris(qris)
            } else {
                repository.updateQris(qris)
                id.toLong()
            }
            
            if (isDefault) {
                repository.setDefaultQris(newId.toInt())
            }
        }
    }

    fun setDefaultQris(qrisId: Int) {
        viewModelScope.launch {
            repository.setDefaultQris(qrisId)
        }
    }

    fun deleteQris(qris: QrisEntity) {
        viewModelScope.launch {
            repository.deleteQris(qris)
        }
    }

    fun copyImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val resolver = context.contentResolver
            val inputStream = resolver.openInputStream(sourceUri) ?: return null
            val fileName = "prod_img_${System.currentTimeMillis()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)
            
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// ViewModel Factory to create ViewModels with correct repositories
class ViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(CalculatorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalculatorViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(ReportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportViewModel(repository) as T
        }
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
