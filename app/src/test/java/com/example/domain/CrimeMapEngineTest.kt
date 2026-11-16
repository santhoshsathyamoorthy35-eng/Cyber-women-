package com.example.domain

import com.example.data.model.CrimeMapFilters
import com.example.data.model.ShieldRiskLevel
import com.example.data.remote.MeenaAiRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrimeMapEngineTest {

    @Test
    fun `generateSeededIncidents produces deterministic 4200 records`() {
        val now = 1_760_000_000_000L
        val a = CrimeMapEngine.generateSeededIncidents(count = 4200, referenceNowMs = now)
        val b = CrimeMapEngine.generateSeededIncidents(count = 4200, referenceNowMs = now)
        assertEquals(4200, a.size)
        assertEquals(a.first().lat, b.first().lat, 1e-6)
        assertEquals(a.last().crime, b.last().crime)
        assertTrue(a.all { it.isSynthetic })
        assertTrue(a.all { it.lat in CrimeMapEngine.BOUNDS.latMin..CrimeMapEngine.BOUNDS.latMax })
        assertTrue(a.all { it.lon in CrimeMapEngine.BOUNDS.lonMin..CrimeMapEngine.BOUNDS.lonMax })
    }

    @Test
    fun `detectHotspots and predictRiskGrid produce ranked spatial output`() {
        val now = 1_760_000_000_000L
        val incidents = CrimeMapEngine.generateSeededIncidents(count = 4200, referenceNowMs = now)
        val filtered = CrimeMapEngine.applyFilters(incidents, CrimeMapFilters(daysWindow = 365), nowMs = now)
        val hotspots = CrimeMapEngine.detectHotspots(filtered, nowMs = now)
        val riskGrid = CrimeMapEngine.predictRiskGrid(filtered, nowMs = now)

        assertTrue("Expected clustered hotspots", hotspots.isNotEmpty())
        assertTrue("Hotspots should be sorted descending by risk", hotspots.zipWithNext().all { (x, y) -> x.risk >= y.risk })
        assertTrue("Expected predicted risk grid cells", riskGrid.isNotEmpty())
    }

    @Test
    fun `computeShieldTimeVulnerableZones handles valid window and outside-coverage fallback`() {
        val now = 1_760_000_000_000L
        val incidents = CrimeMapEngine.generateSeededIncidents(count = 4200, referenceNowMs = now)
        val zones = CrimeMapEngine.computeShieldTimeVulnerableZones(incidents, currentHour = 20, currentDayName = "Friday")

        assertEquals(CrimeMapEngine.HOTSPOTS.size, zones.size)
        assertTrue(zones.first().riskScore > 0)

        val outside = CrimeMapEngine.evaluatePointVulnerability(incidents, lat = 13.08, lon = 80.27, currentHour = 20, currentDayName = "Friday")
        assertEquals(ShieldRiskLevel.INSUFFICIENT_DATA, outside.riskLevel)
    }

    @Test
    fun `optimizePatrols builds bounded non-empty routes`() {
        val now = 1_760_000_000_000L
        val incidents = CrimeMapEngine.generateSeededIncidents(count = 4200, referenceNowMs = now)
        val routes = CrimeMapEngine.optimizePatrols(incidents, nUnits = 5, horizonMinutes = 60, priority = "risk")

        assertEquals(5, routes.size)
        assertTrue(routes.all { it.path.isNotEmpty() && it.timeUsedMinutes <= 60 })
    }

    @Test
    fun `meena danger intent detector catches distress queries`() {
        assertTrue(MeenaAiRepository.detectImmediateDangerIntent("Someone is following me home, help me!"))
        assertFalse(MeenaAiRepository.detectImmediateDangerIntent("What are women's legal rights under PoSH Act?"))
    }
}
