import re

with open('core-database/src/main/java/com/rtiqa/core/database/entity/EnterpriseEntities.kt', 'r') as f:
    content = f.read()

def remove_orgid(entity_name, text):
    pattern = r'(data class ' + entity_name + r'\(\s*@PrimaryKey val id: String,)\s*val orgId: String,'
    return re.sub(pattern, r'\1', text)

entities_to_clean = [
    'SemesterEntity',
    'MajorEntity',
    'SectionEntity',
    'SubjectEntity',
    'StudyPlanEntity'
]

for ent in entities_to_clean:
    content = remove_orgid(ent, content)

with open('core-database/src/main/java/com/rtiqa/core/database/entity/EnterpriseEntities.kt', 'w') as f:
    f.write(content)
