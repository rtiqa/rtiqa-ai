import re

with open('core-domain/src/main/java/com/rtiqa/core/domain/model/EnterpriseModels.kt', 'r') as f:
    content = f.read()

# Add orgId to School
content = re.sub(
    r'(data class School\(\s*val id: String,)(.*?)(val createdAt: Long = System\.currentTimeMillis\(\)\s*\))',
    r'\1\2\3,\n    val orgId: String = ""\n)',
    content,
    flags=re.DOTALL
)

with open('core-domain/src/main/java/com/rtiqa/core/domain/model/EnterpriseModels.kt', 'w') as f:
    f.write(content)

with open('core-data/src/main/java/com/rtiqa/core/data/mapper/DataMappers.kt', 'r') as f:
    content = f.read()

# Update toDomain
content = re.sub(
    r'(fun com\.rtiqa\.core\.database\.entity\.SchoolEntity\.toDomain\(\): com\.rtiqa\.core\.domain\.model\.School = com\.rtiqa\.core\.domain\.model\.School\(\s*id = id,)',
    r'\1\n    orgId = orgId,',
    content
)

# Update toEntity
content = re.sub(
    r'(fun com\.rtiqa\.core\.domain\.model\.School\.toEntity\(\): com\.rtiqa\.core\.database\.entity\.SchoolEntity = com\.rtiqa\.core\.database\.entity\.SchoolEntity\(\s*id = id,)',
    r'\1\n    orgId = orgId,',
    content
)

with open('core-data/src/main/java/com/rtiqa/core/data/mapper/DataMappers.kt', 'w') as f:
    f.write(content)
