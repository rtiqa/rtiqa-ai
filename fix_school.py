import re

with open('core-domain/src/main/java/com/rtiqa/core/domain/model/EnterpriseModels.kt', 'r') as f:
    content = f.read()

content = content.replace("val createdAt: Long = System.currentTimeMillis()),\n    val orgId: String = \"\"\n)", "val createdAt: Long = System.currentTimeMillis(),\n    val orgId: String = \"\"\n)")

with open('core-domain/src/main/java/com/rtiqa/core/domain/model/EnterpriseModels.kt', 'w') as f:
    f.write(content)
