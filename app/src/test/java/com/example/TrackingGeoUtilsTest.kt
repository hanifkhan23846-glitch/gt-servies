package com.example

import com.example.ui.components.buildGoogleMapsDirectionsUrl
import com.example.ui.components.calculateDistanceKm
import com.example.ui.components.estimateTravelMinutes
import com.example.ui.components.formatDistanceLabel
import com.example.ui.components.formatDistanceWithEta
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackingGeoUtilsTest {

    @Test
    fun calculateDistanceKm_samePoint_isZero() {
        val km = calculateDistanceKm(29.3956, 71.6836, 29.3956, 71.6836)
        assertEquals(0.0, km, 0.0001)
    }

    @Test
    fun calculateDistanceKm_bahawalpurPoints_returnsExpectedDistanceAndEta() {
        // Model Town A (~29.3956, 71.6836) to Satellite Town (~29.4150, 71.7100) ~ 3.3 km
        val km = calculateDistanceKm(29.3956, 71.6836, 29.4150, 71.7100)
        assertTrue("Expected ~3.3 km, got $km", km in 2.8..3.8)
        val label = formatDistanceLabel(km)
        assertTrue(label.endsWith("km"))
        val eta = estimateTravelMinutes(km)
        assertTrue("Expected ETA >= 5 min, got $eta", eta >= 5)
        assertTrue(formatDistanceWithEta(km).contains("min"))
    }

    @Test
    fun formatDistanceLabel_shortDistance_usesMeters() {
        assertEquals("450 m", formatDistanceLabel(0.45))
    }

    @Test
    fun buildGoogleMapsDirectionsUrl_formatsWithAndWithoutOrigin() {
        val withOrigin = buildGoogleMapsDirectionsUrl(29.39, 71.68, 29.41, 71.71)
        assertTrue(withOrigin.contains("origin=29.39,71.68"))
        assertTrue(withOrigin.contains("destination=29.41,71.71"))
        assertTrue(withOrigin.contains("travelmode=driving"))

        val withoutOrigin = buildGoogleMapsDirectionsUrl(null, null, 29.41, 71.71)
        assertTrue(!withoutOrigin.contains("origin="))
        assertTrue(withoutOrigin.contains("destination=29.41,71.71"))
    }
}
