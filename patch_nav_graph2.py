import re

with open('app/src/main/java/com/rtiqa/mobile/ui/navigation/RtiqaNavGraph.kt', 'r') as f:
    content = f.read()

if "onNavigateToAcademicStructure =" not in content:
    content = content.replace("onNavigateToAcademicPlatform = { navController.navigate(\"academic_platform\") },", "onNavigateToAcademicPlatform = { navController.navigate(\"academic_platform\") },\n                    onNavigateToAcademicStructure = { navController.navigate(\"academic_structure\") },")

with open('app/src/main/java/com/rtiqa/mobile/ui/navigation/RtiqaNavGraph.kt', 'w') as f:
    f.write(content)
