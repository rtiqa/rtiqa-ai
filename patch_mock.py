import re

with open('feature-admin/src/test/java/com/rtiqa/feature/admin/academic/AcademicStructureViewModelTest.kt', 'r') as f:
    content = f.read()

if "import org.mockito.Mockito.`when`" not in content:
    content = content.replace("import org.mockito.Mockito.*", "import org.mockito.Mockito.*\nimport org.mockito.Mockito.`when`\nimport org.mockito.Mockito.mock\nimport org.mockito.ArgumentMatchers.anyString")

with open('feature-admin/src/test/java/com/rtiqa/feature/admin/academic/AcademicStructureViewModelTest.kt', 'w') as f:
    f.write(content)
