package com.rtiqa.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_classes")
data class SchoolClassEntity(
    @PrimaryKey val id: String,
    val schoolId: String,
    val name: String,
    val gradeLevel: String,
    val sectionName: String,
    val capacity: Int,
    val studentsCount: Int,
    val roomNumber: String,
    val displayOrder: Int,
    val createdAt: Long = System.currentTimeMillis()
)
