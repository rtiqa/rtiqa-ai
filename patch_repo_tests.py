import re

impl_methods = """
            override suspend fun deleteSemester(id: String) {}
            override suspend fun deleteDepartment(id: String) {}
            override suspend fun deleteMajor(id: String) {}
            override suspend fun deleteStudyPlan(id: String) {}
"""

with open('feature-admin/src/test/java/com/rtiqa/feature/admin/classes/ClassesViewModelTest.kt', 'r') as f:
    content = f.read()

content = content.replace("override suspend fun deleteMember(id: String) {}", "override suspend fun deleteMember(id: String) {}\n" + impl_methods)

with open('feature-admin/src/test/java/com/rtiqa/feature/admin/classes/ClassesViewModelTest.kt', 'w') as f:
    f.write(content)

with open('feature-admin/src/test/java/com/rtiqa/feature/admin/school/SchoolViewModelTest.kt', 'r') as f:
    content = f.read()

content = content.replace("override suspend fun deleteMember(id: String) {}", "override suspend fun deleteMember(id: String) {}\n" + impl_methods)

with open('feature-admin/src/test/java/com/rtiqa/feature/admin/school/SchoolViewModelTest.kt', 'w') as f:
    f.write(content)

