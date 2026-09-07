import re

with open('core-domain/src/main/java/com/rtiqa/core/domain/usecase/EnterpriseUseCases.kt', 'r') as f:
    content = f.read()

new_usecases = """
class DeleteSemesterUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(id: String) = repository.deleteSemester(id)
}
class DeleteDepartmentUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(id: String) = repository.deleteDepartment(id)
}
class DeleteMajorUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(id: String) = repository.deleteMajor(id)
}
class DeleteStudyPlanUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(id: String) = repository.deleteStudyPlan(id)
}
"""

if "DeleteSemesterUseCase" not in content:
    content += new_usecases

with open('core-domain/src/main/java/com/rtiqa/core/domain/usecase/EnterpriseUseCases.kt', 'w') as f:
    f.write(content)

with open('core-domain/src/main/java/com/rtiqa/core/domain/di/DomainUseCasesContainer.kt', 'r') as f:
    di_content = f.read()

di_additions = """
    val deleteSemesterUseCase by lazy { enterpriseRepositoryInstance?.let { DeleteSemesterUseCase(it) } }
    val deleteDepartmentUseCase by lazy { enterpriseRepositoryInstance?.let { DeleteDepartmentUseCase(it) } }
    val deleteMajorUseCase by lazy { enterpriseRepositoryInstance?.let { DeleteMajorUseCase(it) } }
    val deleteStudyPlanUseCase by lazy { enterpriseRepositoryInstance?.let { DeleteStudyPlanUseCase(it) } }
"""

if "deleteSemesterUseCase" not in di_content:
    di_content = di_content.replace("val deleteEnterpriseMemberUseCase", di_additions.strip() + "\n    val deleteEnterpriseMemberUseCase")

with open('core-domain/src/main/java/com/rtiqa/core/domain/di/DomainUseCasesContainer.kt', 'w') as f:
    f.write(di_content)
