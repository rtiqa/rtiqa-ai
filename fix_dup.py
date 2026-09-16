with open('core-database/src/main/java/com/rtiqa/core/database/entity/EnterpriseEntities.kt', 'r') as f:
    content = f.read()
import re
content = re.sub(r'val orgId: String,\s*val orgId: String,', r'val orgId: String,', content)
with open('core-database/src/main/java/com/rtiqa/core/database/entity/EnterpriseEntities.kt', 'w') as f:
    f.write(content)
