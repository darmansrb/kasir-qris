package com.moluccasdev.poskasirqris.util

import java.util.Locale

object QrisEngine {

    /**
     * Parses an EMVCo QR code string into a key-value map.
     */
    fun parseEmvCo(qris: String): LinkedHashMap<String, String> {
        val map = LinkedHashMap<String, String>()
        var index = 0
        val cleanQris = qris.trim()
        while (index < cleanQris.length) {
            if (index + 4 > cleanQris.length) break
            val tag = cleanQris.substring(index, index + 2)
            val lengthStr = cleanQris.substring(index + 2, index + 4)
            val length = lengthStr.toIntOrNull() ?: break
            if (index + 4 + length > cleanQris.length) break
            val value = cleanQris.substring(index + 4, index + 4 + length)
            map[tag] = value
            index += 4 + length
        }
        return map
    }

    /**
     * Extracts the merchant name (Tag 59) from the QRIS template string.
     * Uses robust parsing and recovery.
     */
    fun extractMerchantName(qris: String): String {
        val cleanQris = qris.trim()
        
        // Try standard parsing first
        try {
            val map = parseEmvCo(cleanQris)
            val name = map["59"]
            if (!name.isNullOrBlank()) {
                return name
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Robust fallback scanner: scan sequentially with error recovery (index++) to find Tag 59
        try {
            var index = 0
            while (index < cleanQris.length) {
                if (index + 4 > cleanQris.length) break
                val tag = cleanQris.substring(index, index + 2)
                val lengthStr = cleanQris.substring(index + 2, index + 4)
                val length = lengthStr.toIntOrNull()
                if (length == null || length < 0) {
                    index++
                    continue
                }
                if (index + 4 + length > cleanQris.length) {
                    index++
                    continue
                }
                val value = cleanQris.substring(index + 4, index + 4 + length)
                if (tag == "59") {
                    return value
                }
                index += 4 + length
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return "Unknown Merchant"
    }

    /**
     * Generates a dynamic QRIS string by injecting the transaction amount (Tag 54)
     * using the PHP-equivalent robust string manipulation:
     * 1. Strips the last 4 characters (CRC-16).
     * 2. Replaces "010211" (Static initiation method) with "010212" (Dynamic initiation method).
     * 3. Splices the formatted amount (Tag 54) directly before "5802ID" (Country code tag).
     * 4. Recalculates the CRC-16 checksum and appends it.
     */
    fun generateDynamicQris(staticQris: String, amount: Double): String {
        try {
            val trimmedQris = staticQris.trim()
            if (trimmedQris.length < 4) return staticQris

            // 1. Remove the last 4 CRC characters
            val withoutCrc = trimmedQris.substring(0, trimmedQris.length - 4)

            // 2. Change initiation method from static to dynamic
            val step1 = withoutCrc.replace("010211", "010212")

            // 3. Format amount as integer string
            val amountStr = String.format(Locale.US, "%.0f", amount)
            val tag54 = "54" + String.format(Locale.US, "%02d", amountStr.length) + amountStr

            // 4. Inject Tag 54 directly before "5802ID"
            val fix = if (step1.contains("5802ID")) {
                val parts = step1.split("5802ID", limit = 2)
                parts[0] + tag54 + "5802ID" + parts[1]
            } else {
                // Fallback: append at the end
                step1 + tag54
            }

            // 5. Recalculate CRC
            val crc = calculateCrc16(fix)
            return fix + crc
        } catch (e: Exception) {
            e.printStackTrace()
            return staticQris // Fallback to static if parsing fails
        }
    }

    /**
     * Standard CRC-16 CCITT (0x1021) algorithm matching PHP's implementation.
     */
    fun calculateCrc16(str: String): String {
        var crc = 0xFFFF
        val strlen = str.length
        for (c in 0 until strlen) {
            val charCode = str[c].code
            crc = crc xor (charCode shl 8)
            for (i in 0 until 8) {
                if ((crc and 0x8000) != 0) {
                    crc = (crc shl 1) xor 0x1021
                } else {
                    crc = crc shl 1
                }
            }
        }
        val hex = crc and 0xFFFF
        return String.format(Locale.US, "%04X", hex)
    }
}
