#!/bin/bash
sed -i 's/val schoolId: String/val schoolId: String? = null/g' /app/applet/core-database/src/main/java/com/rtiqa/core/database/entity/DatabaseEntities.kt
