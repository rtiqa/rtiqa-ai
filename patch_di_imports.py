import re

with open('core-domain/src/main/java/com/rtiqa/core/domain/di/DomainUseCasesContainer.kt', 'r') as f:
    content = f.read()

# Replace all import com.rtiqa.core.domain.usecase.* with a single wildcard
content = re.sub(r'import com\.rtiqa\.core\.domain\.usecase\.[A-Za-z0-9_]+', '', content)
content = content.replace('import com.rtiqa.core.domain.repository.UserRepositoryContract', 'import com.rtiqa.core.domain.repository.UserRepositoryContract\nimport com.rtiqa.core.domain.usecase.*')

with open('core-domain/src/main/java/com/rtiqa/core/domain/di/DomainUseCasesContainer.kt', 'w') as f:
    f.write(content)
