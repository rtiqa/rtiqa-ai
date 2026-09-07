import re

with open('feature-admin/src/main/java/com/rtiqa/feature/admin/AdminScreen.kt', 'r') as f:
    content = f.read()

# Add callback parameter
if "onNavigateToAcademicStructure: () -> Unit = {}," not in content:
    content = content.replace("onNavigateToAcademicPlatform: () -> Unit = {},", "onNavigateToAcademicPlatform: () -> Unit = {},\n    onNavigateToAcademicStructure: () -> Unit = {},")

# Add AdminCard
card_code = """
            AdminCard(
                title = "الهيكل الأكاديمي",
                description = "إدارة الأعوام، الفصول، الأقسام، والمواد",
                icon = Icons.Default.Business,
                onClick = onNavigateToAcademicStructure
            )
"""
if "الهيكل الأكاديمي" not in content:
    content = content.replace("AdminCard(\n                title = \"إدارة المدارس\"", card_code.strip() + "\n            AdminCard(\n                title = \"إدارة المدارس\"")

with open('feature-admin/src/main/java/com/rtiqa/feature/admin/AdminScreen.kt', 'w') as f:
    f.write(content)
