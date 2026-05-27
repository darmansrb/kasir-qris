package com.moluccasdev.poskasirqris.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActiveProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)
}

@Dao
interface QrisDao {
    @Query("SELECT * FROM qris_config ORDER BY id DESC")
    fun getAllQrisFlow(): Flow<List<QrisEntity>>

    @Query("SELECT * FROM qris_config WHERE is_default = 1 LIMIT 1")
    suspend fun getDefaultQris(): QrisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(qris: QrisEntity): Long

    @Update
    suspend fun update(qris: QrisEntity)

    @Query("UPDATE qris_config SET is_default = 0")
    suspend fun clearDefaults()

    @Delete
    suspend fun delete(qris: QrisEntity)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders WHERE status = 'DRAFT' ORDER BY created_at DESC")
    fun getDraftOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Long): OrderEntity?

    @Query("SELECT * FROM order_details WHERE order_id = :orderId")
    fun getOrderDetailsFlow(orderId: Long): Flow<List<OrderDetailEntity>>

    @Query("SELECT * FROM order_details WHERE order_id = :orderId")
    suspend fun getOrderDetails(orderId: Long): List<OrderDetailEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderDetail(detail: OrderDetailEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderDetails(details: List<OrderDetailEntity>)

    @Query("DELETE FROM order_details WHERE order_id = :orderId")
    suspend fun deleteOrderDetailsByOrderId(orderId: Long)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY payment_date DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE payment_date BETWEEN :start AND :end ORDER BY payment_date DESC")
    fun getTransactionsInRangeFlow(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?
}
