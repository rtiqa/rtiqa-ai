with open('./core-domain/src/main/java/com/rtiqa/core/domain/model/EnterpriseModels.kt', 'r') as f:
    content = f.read()

content = content.replace(
'''data class School(
    val id: String,
    val name: String,''',
'''data class School(
    val id: String,
    val orgId: String,
    val name: String,''')

with open('./core-domain/src/main/java/com/rtiqa/core/domain/model/EnterpriseModels.kt', 'w') as f:
    f.write(content)
