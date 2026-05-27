package com.moluccasdev.poskasirqris.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.moluccasdev.poskasirqris.data.OrderDetailEntity
import com.moluccasdev.poskasirqris.data.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    private val fileDateFormatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Represents a full transaction record inside UI for reporting.
     */
    data class TransactionReportItem(
        val transaction: TransactionEntity,
        val customerName: String,
        val itemCount: Int,
        val details: List<OrderDetailEntity>
    )

    /**
     * Exports transactions to CSV file using standard MediaStore or local downloads.
     */
    fun exportToCsv(context: Context, items: List<TransactionReportItem>): Uri? {
        val fileName = "Laporan_POS_${fileDateFormatter.format(Date())}.csv"
        val csvBuilder = StringBuilder()
        
        // CSV Header
        csvBuilder.append("ID Transaksi,Tanggal,Pelanggan / No Kursi,Metode Pembayaran,Total Transaksi,Jumlah Item\n")
        
        // CSV Rows
        for (item in items) {
            val dateStr = dateFormatter.format(Date(item.transaction.paymentDate))
            val safeName = item.customerName.replace("\"", "\"\"")
            csvBuilder.append("${item.transaction.id},")
            csvBuilder.append("$dateStr,")
            csvBuilder.append("\"$safeName\",")
            csvBuilder.append("${item.transaction.paymentMethod},")
            csvBuilder.append("${item.transaction.totalAmount.toInt()},")
            csvBuilder.append("${item.itemCount}\n")
        }

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/comma-separated-values")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        stream.write(csvBuilder.toString().toByteArray())
                    }
                }
                uri
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { stream ->
                    stream.write(csvBuilder.toString().toByteArray())
                }
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Exports transaction summary to a beautiful PDF document.
     */
    fun exportToPdf(context: Context, items: List<TransactionReportItem>): Uri? {
        val fileName = "Laporan_Keuangan_${fileDateFormatter.format(Date())}.pdf"
        
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4: 595 x 842 pt
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        val titlePaint = Paint().apply {
            color = Color.rgb(79, 55, 138) // Indigo brand color (#4f378a)
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(103, 80, 164) // Indigo tint (#6750A4)
            textSize = 11f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        // Draw Header
        canvas.drawText("Warkop Modern POS", 40f, 60f, titlePaint)
        canvas.drawText("Laporan Keuangan & Riwayat Transaksi", 40f, 80f, Paint().apply {
            textSize = 14f
            isFakeBoldText = true
            color = Color.GRAY
        })
        canvas.drawText("Dicetak pada: ${dateFormatter.format(Date())}", 40f, 100f, subPaint)
        canvas.drawText("Total Transaksi: ${items.size}", 40f, 115f, subPaint)
        
        val totalRevenue = items.sumOf { it.transaction.totalAmount }
        val qrisTotal = items.filter { it.transaction.paymentMethod == "QRIS" }.sumOf { it.transaction.totalAmount }
        val cashTotal = items.filter { it.transaction.paymentMethod == "CASH" }.sumOf { it.transaction.totalAmount }
        
        canvas.drawText("Total Pendapatan: Rp ${String.format(Locale.getDefault(), "%,.0f", totalRevenue)}", 400f, 60f, Paint(headerPaint).apply { textSize = 12f })
        canvas.drawText("Rincian QRIS: Rp ${String.format(Locale.getDefault(), "%,.0f", qrisTotal)}", 400f, 80f, subPaint)
        canvas.drawText("Rincian Tunai: Rp ${String.format(Locale.getDefault(), "%,.0f", cashTotal)}", 400f, 95f, subPaint)

        // Draw Table Frame
        var y = 140f
        canvas.drawLine(40f, y, 555f, y, borderPaint)
        y += 20f
        
        // Draw Table Header
        canvas.drawText("ID", 45f, y - 5, headerPaint)
        canvas.drawText("Tanggal", 90f, y - 5, headerPaint)
        canvas.drawText("Pelanggan/Kursi", 210f, y - 5, headerPaint)
        canvas.drawText("Metode", 370f, y - 5, headerPaint)
        canvas.drawText("Items", 440f, y - 5, headerPaint)
        canvas.drawText("Total (Rp)", 490f, y - 5, headerPaint)
        
        canvas.drawLine(40f, y, 555f, y, borderPaint)
        y += 20f

        // Draw Table Rows
        for (item in items) {
            if (y > 800f) break // A4 page limit bounds (simplified to 1 page for standard sizes, expandable)
            
            val formattedDate = dateFormatter.format(Date(item.transaction.paymentDate))
            canvas.drawText("#${item.transaction.id}", 45f, y - 5, textPaint)
            canvas.drawText(formattedDate, 90f, y - 5, textPaint)
            
            // Limit name width
            val name = if (item.customerName.length > 20) item.customerName.take(17) + "..." else item.customerName
            canvas.drawText(name, 210f, y - 5, textPaint)
            canvas.drawText(item.transaction.paymentMethod, 370f, y - 5, textPaint)
            canvas.drawText("${item.itemCount}", 440f, y - 5, textPaint)
            
            val amountStr = String.format(Locale.getDefault(), "%,.0f", item.transaction.totalAmount)
            canvas.drawText(amountStr, 490f, y - 5, textPaint)

            canvas.drawLine(40f, y, 555f, y, Paint(borderPaint).apply { strokeWidth = 0.5f })
            y += 20f
        }

        // Draw Footer
        canvas.drawText("Terima Kasih telah menggunakan Warkop Modern POS Kasir", 40f, 820f, subPaint)

        pdfDocument.finishPage(page)

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream ->
                        pdfDocument.writeTo(stream)
                    }
                }
                pdfDocument.close()
                uri
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { stream ->
                    pdfDocument.writeTo(stream)
                }
                pdfDocument.close()
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
