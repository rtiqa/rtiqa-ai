package com.rtiqa.core.data.datastore

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserPreferencesTest {

    @Test
    fun defaultActiveSchoolId_isNull_andNeverFabricatesSchool001() {
        val defaultPrefs = UserPreferences(
            isDarkTheme = false,
            isOfflineModeEnabled = false,
            activeUserId = null,
            lastSyncTimestamp = 0L
        )

        assertNull(defaultPrefs.activeSchoolId)
        assertNotEquals("school_001", defaultPrefs.activeSchoolId)
    }

    @Test
    fun explicitActiveSchoolId_preservesRealSchoolId() {
        val prefs = UserPreferences(
            isDarkTheme = true,
            isOfflineModeEnabled = false,
            activeUserId = "user_42",
            lastSyncTimestamp = 12345L,
            activeSchoolId = "school_al_amal_007"
        )

        assertEquals("school_al_amal_007", prefs.activeSchoolId)
    }
}
