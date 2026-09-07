import re

with open('core-domain/src/main/java/com/rtiqa/core/domain/repository/EnterpriseRepository.kt', 'r') as f:
    content = f.read()

repo_methods = """
    suspend fun deleteSemester(id: String)
    suspend fun deleteDepartment(id: String)
    suspend fun deleteMajor(id: String)
    suspend fun deleteStudyPlan(id: String)
"""

if "deleteSemester(" not in content:
    content = content.replace("suspend fun deleteMember(id: String)\n}", "suspend fun deleteMember(id: String)\n" + repo_methods + "\n}")

with open('core-domain/src/main/java/com/rtiqa/core/domain/repository/EnterpriseRepository.kt', 'w') as f:
    f.write(content)

with open('core-data/src/main/java/com/rtiqa/core/data/repository/EnterpriseRepositoryImpl.kt', 'r') as f:
    content_impl = f.read()

impl_methods = """
    override suspend fun deleteSemester(id: String) {
        enterpriseDao.deleteSemester(id)
    }
    override suspend fun deleteDepartment(id: String) {
        enterpriseDao.deleteDepartment(id)
    }
    override suspend fun deleteMajor(id: String) {
        enterpriseDao.deleteMajor(id)
    }
    override suspend fun deleteStudyPlan(id: String) {
        enterpriseDao.deleteStudyPlan(id)
    }
"""

if "deleteSemester(" not in content_impl:
    content_impl = content_impl.replace("suspend fun deleteMember(id: String) {\n        enterpriseDao.deleteMember(id)\n    }\n}", "suspend fun deleteMember(id: String) {\n        enterpriseDao.deleteMember(id)\n    }\n" + impl_methods + "}")

with open('core-data/src/main/java/com/rtiqa/core/data/repository/EnterpriseRepositoryImpl.kt', 'w') as f:
    f.write(content_impl)
