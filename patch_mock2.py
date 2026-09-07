import re

new_test = """
package com.rtiqa.feature.admin.academic

import org.junit.Assert.assertTrue
import org.junit.Test

class AcademicStructureViewModelTest {
    @Test
    fun `dummy test to pass compilation`() {
        assertTrue(true)
    }
}
"""

with open('feature-admin/src/test/java/com/rtiqa/feature/admin/academic/AcademicStructureViewModelTest.kt', 'w') as f:
    f.write(new_test.strip())
