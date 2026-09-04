package dev.questlens

import org.junit.Assert.*
import org.junit.Test

class TapEventGateTest {
    @Test fun deliberateSecondGestureCanClosePanelWithoutLongDelay() {
        val gate = TapEventGate(0)
        assertFalse(gate.accept(100, 0))
        assertTrue(gate.accept(200, 1_000_000_000))
        assertFalse(gate.accept(200, 1_900_000_000))
        assertTrue(gate.accept(300, 1_900_000_000))
    }
    @Test fun acceptsWhenVendorAndAndroidClockOriginsDiffer() {
        val gate = TapEventGate(10_000_000_000_000L)
        assertFalse(gate.accept(2_000_000_000L, 10_000_000_000_000L))
        assertFalse(gate.accept(2_000_000_000L, 10_001_000_000_000L))
        assertTrue(gate.accept(3_000_000_000L, 10_002_000_000_000L))
        assertTrue(gate.accept(7_000_000_000L, 10_006_000_000_000L))
    }

    @Test fun rejectsDuplicatesOutOfOrderAndRapidRepeats() {
        val gate = TapEventGate(10_000_000_000L)
        assertFalse(gate.accept(200_000_000_000L, 10_000_000_000L))
        assertFalse(gate.accept(200_000_000_000L, 12_000_000_000L))
        assertTrue(gate.accept(201_000_000_000L, 12_100_000_000L))
        assertFalse(gate.accept(201_100_000_000L, 12_200_000_000L))
        assertFalse(gate.accept(201_050_000_000L, 12_300_000_000L))
        assertTrue(gate.accept(204_000_000_000L, 15_100_000_000L))
    }

    @Test fun startupAndSensorClockResetNeverLaunch() {
        val gate = TapEventGate(10_000_000_000L)
        assertFalse(gate.accept(200_000_000_000L, 10_000_000_000L))
        assertFalse(gate.accept(200_100_000_000L, 10_100_000_000L))
        assertFalse(gate.accept(1_000_000_000L, 12_000_000_000L))
        assertTrue(gate.accept(5_000_000_000L, 16_000_000_000L))
    }

}
