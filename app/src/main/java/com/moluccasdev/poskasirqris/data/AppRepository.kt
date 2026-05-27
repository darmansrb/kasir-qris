package com.moluccasdev.poskasirqris.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface AppRepository {
    // Products
    fun getAllActiveProductsFlow(): Flow<List<ProductEntity>>
    fun getAllProductsFlow(): Flow<List<ProductEntity>>
    suspend fun getProductById(id: Int): ProductEntity?
    suspend fun insertProduct(product: ProductEntity): Long
    suspend fun updateProduct(product: ProductEntity)
    suspend fun deleteProductSoft(product: ProductEntity)

    // QRIS Config
    fun getAllQrisFlow(): Flow<List<QrisEntity>>
    suspend fun getDefaultQris(): QrisEntity?
    suspend fun insertQris(qris: QrisEntity): Long
    suspend fun updateQris(qris: QrisEntity)
    suspend fun setDefaultQris(qrisId: Int)
    suspend fun deleteQris(qris: QrisEntity)

    // Orders
    fun getDraftOrdersFlow(): Flow<List<OrderEntity>>
    suspend fun getOrderById(id: Long): OrderEntity?
    fun getOrderDetailsFlow(orderId: Long): Flow<List<OrderDetailEntity>>
    suspend fun getOrderDetails(orderId: Long): List<OrderDetailEntity>
    suspend fun saveOrder(order: OrderEntity, details: List<OrderDetailEntity>): Long
    suspend fun deleteOrder(order: OrderEntity)

    // Transactions
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>
    fun getTransactionsInRangeFlow(start: Long, end: Long): Flow<List<TransactionEntity>>
    suspend fun completeCheckout(orderId: Long, totalAmount: Double, paymentMethod: String): Long
    suspend fun getTransactionById(id: Long): TransactionEntity?
}

class AppRepositoryImpl(
    private val db: AppDatabase
) : AppRepository {

    private val productDao = db.productDao()
    private val qrisDao = db.qrisDao()
    private val orderDao = db.orderDao()
    private val transactionDao = db.transactionDao()

    // Products
    override fun getAllActiveProductsFlow(): Flow<List<ProductEntity>> = productDao.getAllActiveProductsFlow()
    override fun getAllProductsFlow(): Flow<List<ProductEntity>> = productDao.getAllProductsFlow()
    override suspend fun getProductById(id: Int): ProductEntity? = productDao.getProductById(id)
    override suspend fun insertProduct(product: ProductEntity): Long = productDao.insert(product)
    override suspend fun updateProduct(product: ProductEntity) = productDao.update(product)
    
    override suspend fun deleteProductSoft(product: ProductEntity) {
        // Soft delete to prevent integrity issues in past orders
        productDao.update(product.copy(isActive = false))
    }

    // QRIS
    override fun getAllQrisFlow(): Flow<List<QrisEntity>> = qrisDao.getAllQrisFlow()
    override suspend fun getDefaultQris(): QrisEntity? = qrisDao.getDefaultQris()
    override suspend fun insertQris(qris: QrisEntity): Long = qrisDao.insert(qris)
    override suspend fun updateQris(qris: QrisEntity) = qrisDao.update(qris)
    
    override suspend fun setDefaultQris(qrisId: Int) {
        db.withTransaction {
            qrisDao.clearDefaults()
            val list = qrisDao.getAllQrisFlow().first()
            val target = list.find { it.id == qrisId }
            if (target != null) {
                qrisDao.update(target.copy(isDefault = true))
            }
        }
    }
    
    override suspend fun deleteQris(qris: QrisEntity) = qrisDao.delete(qris)

    // Orders
    override fun getDraftOrdersFlow(): Flow<List<OrderEntity>> = orderDao.getDraftOrdersFlow()
    override suspend fun getOrderById(id: Long): OrderEntity? = orderDao.getOrderById(id)
    override fun getOrderDetailsFlow(orderId: Long): Flow<List<OrderDetailEntity>> = orderDao.getOrderDetailsFlow(orderId)
    override suspend fun getOrderDetails(orderId: Long): List<OrderDetailEntity> = orderDao.getOrderDetails(orderId)

    override suspend fun saveOrder(order: OrderEntity, details: List<OrderDetailEntity>): Long {
        return db.withTransaction {
            val orderId = if (order.id == 0L) {
                orderDao.insertOrder(order)
            } else {
                orderDao.updateOrder(order)
                order.id
            }
            // Delete old details if editing
            orderDao.deleteOrderDetailsByOrderId(orderId)
            // Insert new details with updated orderId
            val updatedDetails = details.map { it.copy(orderId = orderId) }
            orderDao.insertOrderDetails(updatedDetails)
            orderId
        }
    }

    override suspend fun deleteOrder(order: OrderEntity) {
        db.withTransaction {
            orderDao.deleteOrderDetailsByOrderId(order.id)
            orderDao.deleteOrder(order)
        }
    }

    // Transactions
    override fun getAllTransactionsFlow(): Flow<List<TransactionEntity>> = transactionDao.getAllTransactionsFlow()
    
    override fun getTransactionsInRangeFlow(start: Long, end: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsInRangeFlow(start, end)
    }

    override suspend fun completeCheckout(
        orderId: Long,
        totalAmount: Double,
        paymentMethod: String
    ): Long {
        return db.withTransaction {
            // Update order status to PAID
            val order = orderDao.getOrderById(orderId)
            if (order != null) {
                orderDao.updateOrder(order.copy(status = "PAID"))
            }
            
            // Create Transaction record
            val tx = TransactionEntity(
                orderId = orderId,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod
            )
            transactionDao.insertTransaction(tx)
        }
    }

    override suspend fun getTransactionById(id: Long): TransactionEntity? = transactionDao.getTransactionById(id)
}
