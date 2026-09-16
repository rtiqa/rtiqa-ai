import re

with open('core-database/src/main/java/com/rtiqa/core/database/entity/EnterpriseEntities.kt', 'r') as f:
    content = f.read()

# Replace all occurrences of duplicated orgId
content = re.sub(r'val orgId:\s*String,\s*val orgId:\s*String,', r'val orgId: String,', content)

# Check OrganizationEntity. If it has orgId, remove it.
# It should be:
# data class OrganizationEntity(
#    @PrimaryKey val id: String,
#    val name: String,
content = re.sub(
    r'(data class OrganizationEntity\(\s*@PrimaryKey val id: String,)\s*val orgId: String,',
    r'\1',
    content
)

# Also fix any other places where sed wrongly inserted orgId if it didn't belong.
# Wait, my sed was: 's/@PrimaryKey val id: String,/@PrimaryKey val id: String,\n    val orgId: String,/g'
# I'll just remove `val orgId: String,` from classes that shouldn't have it.
# Let's check which models have orgId in EnterpriseModels.kt to know which entities should have it.

with open('core-database/src/main/java/com/rtiqa/core/database/entity/EnterpriseEntities.kt', 'w') as f:
    f.write(content)
