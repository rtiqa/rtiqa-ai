import re

with open('app/src/main/res/values/strings.xml', 'r') as f:
    content = f.read()

new_strings = """
    <!-- Academic Structure -->
    <string name="academic_structure">الهيكل الأكاديمي</string>
    <string name="academic_structure_desc">إدارة الأعوام، الفصول، الأقسام، والمواد</string>
    <string name="academic_years">الأعوام الدراسية</string>
    <string name="semesters">الفصول الدراسية</string>
    <string name="grade_levels">المراحل الدراسية</string>
    <string name="departments">الأقسام</string>
    <string name="majors">التخصصات</string>
    <string name="subjects">المواد الدراسية</string>
    <string name="sections">الشعب</string>
    <string name="study_plans">الخطط الدراسية</string>
    
    <string name="empty_academic_years">لا توجد أعوام دراسية</string>
    <string name="empty_semesters">لا توجد فصول دراسية</string>
    <string name="empty_grade_levels">لا توجد مراحل دراسية</string>
    <string name="empty_departments">لا توجد أقسام</string>
    <string name="empty_majors">لا توجد تخصصات</string>
    <string name="empty_subjects">لا توجد مواد دراسية</string>
    <string name="empty_sections">لا توجد شعب دراسية</string>
    <string name="empty_study_plans">لا توجد خطط دراسية</string>
    
    <string name="select_year_first">الرجاء تحديد عام دراسي أولاً</string>
    <string name="select_dept_first">الرجاء تحديد قسم أولاً</string>
    <string name="select_major_first">الرجاء تحديد تخصص أولاً</string>
    
    <string name="order_prefix">الترتيب: </string>
    <string name="code_prefix">الرمز: </string>
    <string name="hours_prefix">الساعات: </string>
    <string name="capacity_prefix">السعة: </string>
"""

if "academic_structure" not in content:
    content = content.replace("</resources>", new_strings + "</resources>")

with open('app/src/main/res/values/strings.xml', 'w') as f:
    f.write(content)

with open('app/src/main/res/values-en/strings.xml', 'r') as f:
    content_en = f.read()

new_strings_en = """
    <!-- Academic Structure -->
    <string name="academic_structure">Academic Structure</string>
    <string name="academic_structure_desc">Manage years, terms, departments, and subjects</string>
    <string name="academic_years">Academic Years</string>
    <string name="semesters">Semesters</string>
    <string name="grade_levels">Grade Levels</string>
    <string name="departments">Departments</string>
    <string name="majors">Majors</string>
    <string name="subjects">Subjects</string>
    <string name="sections">Sections</string>
    <string name="study_plans">Study Plans</string>
    
    <string name="empty_academic_years">No academic years</string>
    <string name="empty_semesters">No semesters</string>
    <string name="empty_grade_levels">No grade levels</string>
    <string name="empty_departments">No departments</string>
    <string name="empty_majors">No majors</string>
    <string name="empty_subjects">No subjects</string>
    <string name="empty_sections">No sections</string>
    <string name="empty_study_plans">No study plans</string>
    
    <string name="select_year_first">Please select an academic year first</string>
    <string name="select_dept_first">Please select a department first</string>
    <string name="select_major_first">Please select a major first</string>
    
    <string name="order_prefix">Order: </string>
    <string name="code_prefix">Code: </string>
    <string name="hours_prefix">Hours: </string>
    <string name="capacity_prefix">Capacity: </string>
"""
if "academic_structure" not in content_en:
    content_en = content_en.replace("</resources>", new_strings_en + "</resources>")

with open('app/src/main/res/values-en/strings.xml', 'w') as f:
    f.write(content_en)

