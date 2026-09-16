import re

with open('./core-domain/src/main/java/com/rtiqa/core/domain/model/EnterpriseModels.kt', 'r') as f:
    content = f.read()

# We only want to keep `val orgId: String,` in School, Organization (wait, Organization shouldn't have orgId, it has id).
# Let's just remove ALL `val orgId: String,` that are immediately followed by `val name: String,` (the ones added by my sed command).

lines = content.split('\n')
new_lines = []
for i in range(len(lines)):
    if 'val orgId: String,' in lines[i]:
        # check if next line has name
        if i+1 < len(lines) and 'val name: String,' in lines[i+1]:
            # This is the one we added, EXCEPT for School where we WANT to add it.
            # But wait, how do we identify School? We can just remove ALL of them, and then explicitly add to School.
            pass
        else:
            new_lines.append(lines[i])
    else:
        new_lines.append(lines[i])

with open('./core-domain/src/main/java/com/rtiqa/core/domain/model/EnterpriseModels.kt', 'w') as f:
    f.write('\n'.join(new_lines))

