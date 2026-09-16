with open('core-domain/src/main/java/com/rtiqa/core/domain/model/EnterpriseModels.kt', 'r') as f:
    content = f.read()

import re
content = re.sub(r'val createdAt: Long = System\.currentTimeMillis\(\)\),\n\s*val orgId: String = ""\n\)',
                 r'val createdAt: Long = System.currentTimeMillis(),\n    val orgId: String = ""\n)', content)

with open('core-domain/src/main/java/com/rtiqa/core/domain/model/EnterpriseModels.kt', 'w') as f:
    f.write(content)
