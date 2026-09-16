#!/bin/bash
# Fix SchoolViewModelTest.kt
sed -i 's/School(id = "school_001", name = "المدرسة النموذجية الأولية",/School(id = "school_001", orgId = "", name = "المدرسة النموذجية الأولية",/g' ./feature-admin/src/test/java/com/rtiqa/feature/admin/school/SchoolViewModelTest.kt
sed -i 's/School(id = "school_002", name = "مدرسة التميز الثانوية",/School(id = "school_002", orgId = "", name = "مدرسة التميز الثانوية",/g' ./feature-admin/src/test/java/com/rtiqa/feature/admin/school/SchoolViewModelTest.kt

# Fix ClassesViewModelTest.kt
sed -i 's/School(id = "school_001", name = "المدرسة النموذجية",/School(id = "school_001", orgId = "", name = "المدرسة النموذجية",/g' ./feature-admin/src/test/java/com/rtiqa/feature/admin/classes/ClassesViewModelTest.kt
