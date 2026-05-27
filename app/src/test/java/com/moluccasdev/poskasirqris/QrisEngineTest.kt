package com.moluccasdev.poskasirqris

import com.moluccasdev.poskasirqris.util.QrisEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QrisEngineTest {

    private val sampleQris = "00020101021126680016ID123456789012345011893600000000001234502150000000001234560303UMI51440014ID12345678901202150000000001234560303UMI5204581153033605802ID5913Warkop Modern6007Bandung61054011562090703030036304FFFF"


    @Test
    fun testParseEmvCo() {
        val map = QrisEngine.parseEmvCo(sampleQris)
        assertNotNull(map)
        assertEquals("01", map["00"])
        assertEquals("11", map["01"])
        assertEquals("Warkop Modern", map["59"])
        assertEquals("Bandung", map["60"])
    }

    @Test
    fun testExtractMerchantName() {
        val merchant = QrisEngine.extractMerchantName(sampleQris)
        assertEquals("Warkop Modern", merchant)
    }

    @Test
    fun testGenerateDynamicQris() {
        val amount = 15000.0
        val dynamicQris = QrisEngine.generateDynamicQris(sampleQris, amount)
        
        // Parse the dynamic QRIS to verify values
        val map = QrisEngine.parseEmvCo(dynamicQris)
        
        // 1. Point of Initiation Method should be dynamic ("12")
        assertEquals("12", map["01"])
        
        // 2. Transaction Amount should be "15000"
        assertEquals("15000", map["54"])
        
        // 3. CRC tag "63" should be present and length is 4 characters
        val crc = map["63"]
        assertNotNull(crc)
        assertEquals(4, crc?.length)
        
        // 4. Checksum should match recalculated string up to tag 6304
        val baseString = dynamicQris.dropLast(4)
        val computedCrc = QrisEngine.calculateCrc16(baseString)
        assertEquals(computedCrc, crc)
    }

    @Test
    fun testCrc16Ccitt() {
        // Standard test vector for CRC-16 CCITT
        // "123456789" -> 29B1 (in standard CCITT false / standard CCITT)
        val data = "123456789"
        val crc = QrisEngine.calculateCrc16(data)
        assertEquals("29B1", crc)
    }
}
