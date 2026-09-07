import re

with open('core-domain/src/main/java/com/rtiqa/core/domain/di/DomainUseCasesContainer.kt', 'r') as f:
    content = f.read()

content = content.replace("DeleteSemesterUseCase(it)", "com.rtiqa.core.domain.usecase.DeleteSemesterUseCase(it)")
content = content.replace("DeleteDepartmentUseCase(it)", "com.rtiqa.core.domain.usecase.DeleteDepartmentUseCase(it)")
content = content.replace("DeleteMajorUseCase(it)", "com.rtiqa.core.domain.usecase.DeleteMajorUseCase(it)")
content = content.replace("DeleteStudyPlanUseCase(it)", "com.rtiqa.core.domain.usecase.DeleteStudyPlanUseCase(it)")

with open('core-domain/src/main/java/com/rtiqa/core/domain/di/DomainUseCasesContainer.kt', 'w') as f:
    f.write(content)
