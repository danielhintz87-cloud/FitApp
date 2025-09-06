package com.example.fitapp.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoveNetOutputParserTest {
    @Test
    fun parseMoveNetOutput_filtersLowConfidence() {
        val output = Array(1) { Array(1) { Array(17) { FloatArray(3) } } }
        for (i in 0 until 17) {
            output[0][0][i][0] = 0.5f // y
            output[0][0][i][1] = 0.5f // x
            output[0][0][i][2] = if (i % 2 == 0) 0.9f else 0.1f // score
        }
        val keypoints = parseMoveNetOutput(output, 0.3f)
        assertEquals(9, keypoints.size)
        assertTrue(keypoints.all { it.confidence >= 0.3f })
    }
}
