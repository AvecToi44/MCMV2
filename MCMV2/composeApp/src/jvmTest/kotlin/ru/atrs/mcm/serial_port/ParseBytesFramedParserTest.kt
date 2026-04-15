package ru.atrs.mcm.serial_port

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParseBytesFramedParserTest {
    @Test
    fun crcVectorsMatchSpec() {
        val crc1 = crc8Telemetry(byteArrayOf(0x01, 0x00) + ByteArray(24))
        val crc2 = crc8Telemetry(byteArrayOf(0x02, 0x05) + ByteArray(24) { it.toByte() })
        val crc3 = crc8Telemetry(byteArrayOf(0x10, 0xFF.toByte()) + ByteArray(24))

        assertEquals(0xD0, crc1.toInt() and 0xFF)
        assertEquals(0x29, crc2.toInt() and 0xFF)
        assertEquals(0x3D, crc3.toInt() and 0xFF)
    }

    @Test
    fun parsesValidPressureAndCurrentFrames() {
        resetFramedTelemetryParserState()
        val pressurePayload = ByteArray(24) { it.toByte() }
        val currentPayload = ByteArray(24) { (it + 10).toByte() }

        val frames = consumeFramedTelemetryBytes(
            buildFrame(type = 0x01, seq = 0x00, payload = pressurePayload) +
                buildFrame(type = 0x02, seq = 0x01, payload = currentPayload)
        )

        assertEquals(2, frames.size)
        assertEquals(0x01, frames[0].type.toInt() and 0xFF)
        assertEquals(0x02, frames[1].type.toInt() and 0xFF)
        assertContentEquals(pressurePayload, frames[0].payload)
        assertContentEquals(currentPayload, frames[1].payload)
        assertEquals(2L, rxFramesOk)
        assertEquals(0L, rxCrcFail)
        assertEquals(0L, rxSeqDropCount)
    }

    @Test
    fun parsesFrameSplitAcrossChunks() {
        resetFramedTelemetryParserState()
        val frame = buildFrame(type = 0x01, seq = 0x10, payload = ByteArray(24) { 0x55.toByte() })

        val first = consumeFramedTelemetryBytes(frame.copyOfRange(0, 11))
        val second = consumeFramedTelemetryBytes(frame.copyOfRange(11, frame.size))

        assertTrue(first.isEmpty())
        assertEquals(1, second.size)
        assertEquals(0x10, second[0].seq)
        assertEquals(1L, rxFramesOk)
    }

    @Test
    fun resyncsAfterGarbageBeforeSof() {
        resetFramedTelemetryParserState()
        val frame = buildFrame(type = 0x01, seq = 0x22, payload = ByteArray(24) { 0x11.toByte() })
        val chunk = byteArrayOf(0x44, 0x55, 0x66) + frame

        val frames = consumeFramedTelemetryBytes(chunk)

        assertEquals(1, frames.size)
        assertEquals(1L, rxResyncCount)
        assertEquals(0L, rxCrcFail)
    }

    @Test
    fun recoversAfterCrcFailure() {
        resetFramedTelemetryParserState()
        val badFrame = buildFrame(type = 0x01, seq = 0x30, payload = ByteArray(24) { it.toByte() }).apply {
            this[28] = (this[28].toInt() xor 0x01).toByte()
        }
        val goodFrame = buildFrame(type = 0x02, seq = 0x31, payload = ByteArray(24) { (it + 1).toByte() })

        val frames = consumeFramedTelemetryBytes(badFrame + goodFrame)

        assertEquals(1, frames.size)
        assertEquals(0x02, frames[0].type.toInt() and 0xFF)
        assertEquals(1L, rxCrcFail)
        assertTrue(rxResyncCount >= 1L)
        assertEquals(1L, rxFramesOk)
    }

    @Test
    fun incrementsSeqDropCounterOnGap() {
        resetFramedTelemetryParserState()
        val frame0 = buildFrame(type = 0x01, seq = 0x00, payload = ByteArray(24))
        val frame2 = buildFrame(type = 0x01, seq = 0x02, payload = ByteArray(24))

        consumeFramedTelemetryBytes(frame0 + frame2)

        assertEquals(1L, rxSeqDropCount)
        assertEquals(2, lastSeq)
    }

    private fun buildFrame(type: Int, seq: Int, payload: ByteArray): ByteArray {
        require(payload.size == 24)
        val frame = ByteArray(29)
        frame[0] = 0xA5.toByte()
        frame[1] = 0x5A
        frame[2] = type.toByte()
        frame[3] = seq.toByte()
        payload.copyInto(frame, destinationOffset = 4)

        val crcInput = byteArrayOf(type.toByte(), seq.toByte()) + payload
        frame[28] = crc8Telemetry(crcInput)
        return frame
    }
}
