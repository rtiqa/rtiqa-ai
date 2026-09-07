import re

with open('core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt', 'r') as f:
    content = f.read()

# Add missing delete queries
deletes = """
    @Query("DELETE FROM semesters WHERE id = :id")
    suspend fun deleteSemester(id: String)

    @Query("DELETE FROM departments WHERE id = :id")
    suspend fun deleteDepartment(id: String)

    @Query("DELETE FROM majors WHERE id = :id")
    suspend fun deleteMajor(id: String)

    @Query("DELETE FROM study_plans WHERE id = :id")
    suspend fun deleteStudyPlan(id: String)
"""

if "deleteSemester(" not in content:
    content = content.replace("suspend fun deleteMember(id: String)\n}", "suspend fun deleteMember(id: String)\n" + deletes + "\n}")

with open('core-database/src/main/java/com/rtiqa/core/database/dao/EnterpriseDao.kt', 'w') as f:
    f.write(content)
