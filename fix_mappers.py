error_lines = [14, 30, 46, 62, 78, 90, 102, 142, 156, 171, 181, 191, 201, 210, 219, 229, 238, 246, 255, 264, 276, 288, 298, 308, 317, 327, 340]
# the error lines are 1-indexed.
with open('./core-data/src/main/java/com/rtiqa/core/data/mapper/DataMappers.kt', 'r') as f:
    lines = f.read().split('\n')

new_lines = []
for i, line in enumerate(lines):
    if (i + 1) in error_lines:
        if 'orgId = orgId' in line or 'orgId' in line:
            continue # drop line
    new_lines.append(line)

with open('./core-data/src/main/java/com/rtiqa/core/data/mapper/DataMappers.kt', 'w') as f:
    f.write('\n'.join(new_lines))
