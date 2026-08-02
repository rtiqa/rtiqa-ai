package com.rtiqa.core.domain.model

data class SchoolClass(
    val id: String,
    val schoolId: String,
    val name: String,
    val gradeLevel: String = "",
    val sectionName: String = "",
    val capacity: Int = 30,
    val studentsCount: Int = 0,
    val roomNumber: String = "",
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
