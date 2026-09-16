with open('./feature-admin/src/main/java/com/rtiqa/feature/admin/school/SchoolViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('School(id = "school_001", name =', 'School(id = "school_001", orgId = "", name =')
content = content.replace('School(id = "school_002", name =', 'School(id = "school_002", orgId = "", name =')

# For any generic School( that might be missing orgId:
import re
# We need to find School(id = something, name = something)
content = re.sub(r'School\(\s*id\s*=\s*([^,]+),\s*name\s*=', r'School(id = \1, orgId = "", name =', content)

with open('./feature-admin/src/main/java/com/rtiqa/feature/admin/school/SchoolViewModel.kt', 'w') as f:
    f.write(content)
