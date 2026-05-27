package com.moluccasdev.poskasirqris.util

import java.util.Locale

object QrisEngine {

    /**
     * Parses an EMVCo QR code string into a key-value map.
     */
    fun parseEmvCo(qris: String): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        var index = 0
        while (index < qris.length) {
            if (index + 4 > qris.length) break
            val tag = qris.substring(index, index + 2)
            val lengthStr = qris.substring(index + 2, index + 4)
            val length = lengthStr.toIntOrNull() ?: break
            if (index + 4 + length > qris.length) break
            val value = qris.substring(index + 4, index + 4 + length)
            map[tag] = value
            index += 4 + length
        }
        return map
    }

    /**
     * Extracts the merchant name (Tag 59) from the QRIS template string.
     */
    fun extractMerchantName(qris: String): String {
        return try {
            val map = parseEmvCo(qris.trim())
            map["59"] ?: "Unknown Merchant"
        } catch (e: Exception) {
            "Warkop Modern"
        }
    }

    /**
     * Generates a dynamic QRIS string by injecting the transaction amount (Tag 54)
     * and re-calculating the CRC-16 CCITT checksum (Tag 63).
     */
    fun generateDynamicQris(staticQris: String, amount: Double): String {
        try {
            val trimmedQris = staticQris.trim()
            val map = parseEmvCo(trimmedQris)
            
            // Remove CRC tag (Tag 63) from the map since we will recalculate it
            map.remove("63")
            
            // Set initiation method (Tag 01) to "12" (Dynamic QR)
            map["01"] = "12"
            
            // Format amount as integer or float (with max 2 decimal places if needed, but in Indonesia IDR is integer)
            val amountStr = String.format(Locale.US, "%.0f", amount)
            map["54"] = amountStr

            // Build standard EMVCo string
            val builder = StringBuilder()
            
            // Sort tags to ensure compliance (EMVCo prefers ascending, but keeping original with inserts is also okay)
            // Sorting is highly recommended for standard-compliance!
            val sortedKeys = map.keys.sorted()
            for (key in sortedKeys) {
                val value = map[key] ?: continue
                builder.append(key)
                builder.append(String.format(Locale.US, "%02d", value.length))
                builder.append(value)
            }
            
            // Append Tag 63 with length 04 (value will be calculated CRC)
            builder.append("6304")
            
            val baseString = builder.toString()
            val crc = calculateCrc16(baseString)
            return baseString + crc
        } catch (e: Exception) {
            e.printStackTrace()
            return staticQris // Fallback to static if parsing fails
        }
    }

    /**
     * Standard CRC-16 CCITT (0x1021) algorithm.
     */
    fun calculateCrc16(data: String): String {
        var crc = 0xFFFF
        val polynomial = 0x1021
        val bytes = data.toByteArray(Charsets.ISO_8859_1)
        
        for (b in bytes) {
            for (i in 0 until 8) {
                val bit = (b.toInt() ushr (7 - i) and 1) == 1
                val c15 = (crc ushr 15 and 1) == 1
                crc = crc shl 1
                if (c15 xor bit) {
                    crc = crc xor polynomial
                }
            }
        }
        crc = crc and 0xFFFF
        return String.format(Locale.US, "%04X", crc)
    }
}
