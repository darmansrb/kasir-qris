package com.moluccasdev.poskasirqris.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        QrisEntity::class,
        OrderEntity::class,
        OrderDetailEntity::class,
        TransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun qrisDao(): QrisDao
    abstract fun orderDao(): OrderDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pos_kasir_qris_db"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDb(database)
                }
            }
        }

        suspend fun populateDb(db: AppDatabase) {
            val productDao = db.productDao()
            val qrisDao = db.qrisDao()

            // 1. Tambah Master QRIS Default
            val defaultQris = QrisEntity(
                merchantName = "Warkop Modern",
                rawQrisString = "00020101021126680016ID123456789012345011893600000000001234502150000000001234560303UMI51440014ID12345678901202150000000001234560303UMI5204581153033605802ID5913Warkop Modern6007Bandung61054011562090703030036304FFFF",

                isDefault = true
            )
            qrisDao.insert(defaultQris)

            // 2. Tambah Master Produk Bawaan
            val products = listOf(
                ProductEntity(name = "Indomie Goreng Tante", price = 15000.0, imagePath = null),
                ProductEntity(name = "Indomie Rebus Becek", price = 15000.0, imagePath = null),
                ProductEntity(name = "Nasi Goreng Spesial", price = 20000.0, imagePath = null),
                ProductEntity(name = "Ayam Geprek Sambal Korek", price = 22000.0, imagePath = null),
                ProductEntity(name = "Roti Bakar Keju Susu", price = 12000.0, imagePath = null),
                ProductEntity(name = "Es Teh Manis Jumbo", price = 5000.0, imagePath = null),
                ProductEntity(name = "Es Jeruk Peras Segar", price = 8000.0, imagePath = null),
                ProductEntity(name = "Kopi Susu Gula Aren", price = 15000.0, imagePath = null)
            )
            for (product in products) {
                productDao.insert(product)
            }
        }
    }
}
