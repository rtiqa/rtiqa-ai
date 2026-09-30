package com.rtiqa.core.domain

import com.rtiqa.core.domain.model.AcademicContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AcademicContextTest {

    @Test
    fun defaultContext_hasNoFabricatedOrganizationId() {
        val context = AcademicContext()
        assertNull("Default currentOrgId must be null when unselected", context.currentOrgId)
        assertNotEquals("org_001", context.currentOrgId)
    }

    @Test
    fun defaultContext_hasNoFabricatedSchoolId() {
        val context = AcademicContext()
        assertNull("Default currentSchoolId must be null when unselected", context.currentSchoolId)
        assertNotEquals("school_001", context.currentSchoolId)
    }

    @Test
    fun explicitRealOrganizationId_isPreserved() {
        val context = AcademicContext(currentOrgId = "org_real_777")
        assertEquals("org_real_777", context.currentOrgId)
    }

    @Test
    fun explicitRealSchoolId_isPreserved() {
        val context = AcademicContext(currentSchoolId = "sch_real_888")
        assertEquals("sch_real_888", context.currentSchoolId)
    }
}
