with open('./core-database/src/main/java/com/rtiqa/core/database/entity/EnterpriseEntities.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''data class SchoolEntity(
    @PrimaryKey val id: String,
    val name: String,''',
'''data class SchoolEntity(
    @PrimaryKey val id: String,
    val orgId: String,
    val name: String,''')

with open('./core-database/src/main/java/com/rtiqa/core/database/entity/EnterpriseEntities.kt', 'w') as f:
    f.write(content)
