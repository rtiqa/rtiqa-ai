package com.rtiqa.core.data.mapper

import com.rtiqa.core.database.entity.SchoolClassEntity
import com.rtiqa.core.domain.model.SchoolClass

fun SchoolClassEntity.toDomain(): SchoolClass = SchoolClass(
    id = id,
    schoolId = schoolId,
    name = name,
    gradeLevel = gradeLevel,
    sectionName = sectionName,
    capacity = capacity,
    studentsCount = studentsCount,
    roomNumber = roomNumber,
    displayOrder = displayOrder,
    createdAt = createdAt
)

fun SchoolClass.toEntity(): SchoolClassEntity = SchoolClassEntity(
    id = id,
    schoolId = schoolId,
    name = name,
    gradeLevel = gradeLevel,
    sectionName = sectionName,
    capacity = capacity,
    studentsCount = studentsCount,
    roomNumber = roomNumber,
    displayOrder = displayOrder,
    createdAt = createdAt
)
