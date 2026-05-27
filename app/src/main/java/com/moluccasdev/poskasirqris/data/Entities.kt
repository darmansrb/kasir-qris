package com.moluccasdev.poskasirqris.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val price: Double,
    @ColumnInfo(name = "image_path") val imagePath: String?,
    @ColumnInfo(name = "is_active") val isActive: Boolean = true
)

@Entity(tableName = "qris_config")
data class QrisEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "merchant_name") val merchantName: String,
    @ColumnInfo(name = "raw_qris_string") val rawQrisString: String,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "customer_name_or_table") val customerNameOrTable: String,
    val status: String = "DRAFT", // "DRAFT", "PAID", "CANCELLED"
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "order_details",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class OrderDetailEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "order_id") val orderId: Long,
    @ColumnInfo(name = "product_id") val productId: Int?,
    @ColumnInfo(name = "product_name_snapshot") val productNameSnapshot: String,
    @ColumnInfo(name = "price_snapshot") val priceSnapshot: Double,
    val qty: Int,
    val subtotal: Double
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "order_id") val orderId: Long,
    @ColumnInfo(name = "total_amount") val totalAmount: Double,
    @ColumnInfo(name = "payment_method") val paymentMethod: String, // "CASH", "QRIS"
    @ColumnInfo(name = "payment_date") val paymentDate: Long = System.currentTimeMillis()
)
