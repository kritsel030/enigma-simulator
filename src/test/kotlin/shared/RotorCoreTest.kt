package shared

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RotorCoreTest {

    @Test
    fun constructor_0() {
        val rotor = RotorCore(RotorType.I, 0)
        assertEquals(0, rotor.startOrientation)
        assertEquals(rotor.currentOrientation, rotor.startOrientation)
    }

    @Test
    fun constructor_5() {
        val rotor = RotorCore(RotorType.I, 5)
        assertEquals(5, rotor.startOrientation)
        assertEquals(rotor.currentOrientation, rotor.startOrientation)
    }

    @Test
    fun rotateToStartOffset() {
        val rotor = RotorCore(RotorType.I, 5)
        rotor.rotateToStartOffset(8)
        assertEquals(8, rotor.startOrientation)
        assertEquals(rotor.currentOrientation, rotor.startOrientation)

    }

    @Test
    fun encryptRightToLeft() {
        val rotor = RotorCore(RotorType.I, 0)
        val outputContactOffset = rotor.encryptRightToLeft(0)
        assertEquals(4, outputContactOffset)
    }

    @Test
    fun encryptLeftToRight() {
        val rotor = RotorCore(RotorType.I, 0)
        val outputContactOffset = rotor.encryptLeftToRight(4)
        assertEquals(0, outputContactOffset)
    }

    @Test
    fun stepRotor() {
        val rotor = RotorCore(RotorType.I, 5)
        for (i in 1 .. 3) {
            rotor.stepRotor()
        }
        assertEquals(5, rotor.startOrientation)
        assertEquals(8, rotor.currentOrientation)
    }

    @Test
    fun reset() {
        val rotor = RotorCore(RotorType.I, 5)
        for (i in 1 .. 3) {
            rotor.stepRotor()
        }
        rotor.reset()
        assertEquals(5, rotor.startOrientation)
        assertEquals(rotor.startOrientation, rotor.currentOrientation)
    }
}