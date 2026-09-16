#!/bin/bash
# Add orgId to SchoolEntity
sed -i '/val name: String,/i \    val orgId: String,' ./core-database/src/main/java/com/rtiqa/core/database/entity/EnterpriseEntities.kt

# Add orgId to School domain model
sed -i '/val name: String,/i \    val orgId: String,' ./core-domain/src/main/java/com/rtiqa/core/domain/model/EnterpriseModels.kt

# Update DataMappers for School to Domain
sed -i '/id = id,/a \    orgId = orgId,' ./core-data/src/main/java/com/rtiqa/core/data/mapper/DataMappers.kt

