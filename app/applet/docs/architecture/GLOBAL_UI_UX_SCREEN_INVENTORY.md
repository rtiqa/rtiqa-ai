# خريطة واجهات وتجربة المستخدم الشاملة (Global UI/UX Screen Inventory)
# منصة رتقاء التعليمية — RTIQA Educational Platform

تاريخ الإصدار: 2026-09-18  
طبيعة الوثيقة: **خريطة استكشاف وتخطيط واجهات (Discovery & Architecture Prototype Map) — لا يترتب عليها أي تعديل تنفيذي في الكود**.

---

## A. Current Navigation Map (خريطة التنقل الحالية)

يتم إدارة التنقل الفعلي حالياً عبر ملف `app/src/main/java/com/rtiqa/mobile/ui/navigation/RtiqaNavGraph.kt`، وتتفرع المسارات إلى:

1. **Authentication Flow:**
   - `"splash"` ➔ `feature-auth.SplashScreen`
   - `"welcome"` ➔ `feature-auth.WelcomeScreen`
   - `"login"` ➔ `feature-auth.LoginScreen`
   - `"register"` ➔ `feature-auth.RegisterScreen`
   - `"forgot_password"` ➔ `feature-auth.ForgotPasswordScreen`

2. **Main Learner Flow (مع شريط تنقل سفلي BottomBar):**
   - `"home"` ➔ `app.ui.screens.HomeScreen`
   - `"courses"` ➔ `app.ui.screens.CoursesScreen` (يقابله كود غير مربوط في `feature-courses.CoursesListScreen`)
   - `"course_detail/{courseId}"` ➔ `app.ui.screens.CourseDetailScreen`
   - `"lesson_player/{courseId}/{lessonId}"` ➔ `app.ui.screens.LessonPlayerScreen`
   - `LessonRoutes.VIEWER ("lesson_viewer/{lessonId}")` ➔ `feature-lessons.LessonDetailsScreen`
   - `"ai_tutor"` ➔ `app.ui.screens.AiTutorScreen`
   - `"quiz"` ➔ `app.ui.screens.QuizScreen` (يقابله كود غير مربوط في `feature-quiz.CourseQuizzesScreen`)
   - `"downloads"` ➔ `app.ui.screens.DownloadsScreen`
   - `"profile"` ➔ `app.ui.screens.ProfileScreen`
   - `"settings"` ➔ `app.ui.screens.SettingsScreen`
   - `"academic_platform"` ➔ `app.ui.screens.AcademicPlatformScreen`

3. **Institutional / Admin Flow:**
   - `"admin_dashboard"` ➔ `feature-admin.AdminScreen`
   - `"classes_management"` ➔ `feature-admin.ClassesScreen`
   - `"users_management"` ➔ `feature-admin.UsersScreen`
   - `"academic_structure"` ➔ `feature-admin.AcademicStructureScreen`
   - `"schools_management"` ➔ `feature-admin.SchoolsScreen`

---

## B. جرد الشاشات وحالاتها (Screen Inventory)

### 1. تصنيف الحالات:
- **EXISTING:** شاشة موجودة وتعمل في الـ NavGraph.
- **EXISTING_NEEDS_REDESIGN:** شاشة موجودة لكنها تعتمد عناصر بدائية أو أسلوباً أحادياً يحتاج تحديثاً ليتوافق مع معايير M3 العالمية وRDS.
- **DUPLICATE/OVERLAPPING:** شاشات أو وظائف متكررة بين `:app` وموديولات `:feature-*` تحتاج إلى قرار توحيد.
- **FUNCTIONAL_LATER:** شاشات يمكن تصميمها كـ Prototype في البداية وربط منطقها ومصادر بياناتها لاحقاً.
- **MISSING:** شاشات أساسية لتجربة المستخدم غير مبنية حالياً.

---

### 2. جدول الشاشات الكامل

| Screen | Role | Current Status | Existing Route / File | Purpose | Dependencies | Prototype Candidate | Notes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Splash** | All | EXISTING | `splash`<br>`feature-auth/SplashScreen.kt` | التحقق من الجلسة وتوجيه المستخدم | AuthState, SessionStore | نعم | بسيطة وتعمل بشكل سليم. |
| **Welcome / Onboarding** | All | EXISTING_NEEDS_REDESIGN | `welcome`<br>`feature-auth/WelcomeScreen.kt` | بوابة البداية والتعريف بالمنصة | لا يوجد | نعم | تحتاج توسيع لتشمل شرائح تعريفية متعددة (Multi-step Onboarding). |
| **Login** | All | EXISTING | `login`<br>`feature-auth/LoginScreen.kt` | تسجيل الدخول بالبريد وكلمة المرور | AuthRepository | نعم | تعمل ومكتملة كواجهة. |
| **Register** | Learner / Teacher | EXISTING | `register`<br>`feature-auth/RegisterScreen.kt` | إنشاء حساب جديد | AuthRepository | نعم | تعمل كواجهة ولكن الباك إند غير مكتمل. |
| **Forgot Password** | All | EXISTING | `forgot_password`<br>`feature-auth/ForgotPasswordScreen.kt` | استعادة كلمة المرور | AuthRepository | نعم | واجهة نموذجية. |
| **Learner Home / Dashboard** | Student / Learner | EXISTING_NEEDS_REDESIGN | `home`<br>`app/ui/screens/HomeScreen.kt` | لوحة تحكم الطالب اليومية والتقدم والمهام | MainViewModel, UserProfile | نعم | تحتاج إعادة هيكلة لاستيعاب سياق المدرسة وتوصيات الذكاء الاصطناعي. |
| **Courses Catalog / Explorer** | Student / Learner | DUPLICATE/OVERLAPPING | `courses`<br>`app/CoursesScreen.kt`<br>vs `feature-courses/CoursesListScreen.kt` | استعراض والبحث في الكورسات المتاحة | CourseRepository | نعم | يوجد نسختان في المشروع؛ تحتاج توحيد التصميم فوق RDS. |
| **Course Detail** | Student / Learner | DUPLICATE/OVERLAPPING | `course_detail/{courseId}`<br>`app/CourseDetailScreen.kt`<br>vs `feature-courses/CourseDetailScreen.kt` | استعراض فهرس الكورس ووحداته ودروسه | CourseRepository | نعم | يوجد نسختان في المشروع؛ تحتاج دمج واجهات الموديول والدروس. |
| **Lesson Player / Viewer** | Student | DUPLICATE/OVERLAPPING | `lesson_player` vs `lesson_viewer`<br>`app/LessonPlayerScreen.kt`<br>vs `feature-lessons/LessonDetailsScreen.kt` | مشغل المحتوى التعليمي والتنقل بين الدروس | AcademicDao / ContentRepository | نعم | تداخل وظيفي بين مشغلين؛ تحتاج توحيد واجهة العرض والمحتوى. |
| **Quiz / Assessment Play** | Student | DUPLICATE/OVERLAPPING | `quiz`<br>`app/QuizScreen.kt`<br>vs `feature-quiz/CourseQuizzesScreen.kt` | خوض الاختبارات القصيرة وتقييم الإجابات | QuizViewModel, AcademicDao | نعم | نسخة `app` تعمل على مسار ثابت، ونسخة `feature-quiz` تدعم بنك الأسئلة. |
| **AI Tutor Chat / Mentor** | Student / Learner | EXISTING_NEEDS_REDESIGN | `ai_tutor`<br>`app/AiTutorScreen.kt` | محادثة المساعد الذكي التفاعلي والإرشادي | AiTutorViewModel, GeminiRepository | نعم | واجهة محادثة بسيطة تحتاج دعم السياق التعليمي والمقترحات السريعة. |
| **Offline Downloads** | All | EXISTING | `downloads`<br>`app/DownloadsScreen.kt` | إدارة وتصفح المحتوى والدروس المحملة محلياً | AcademicDao, WorkManager | نعم | تحتاج تحسين مؤشرات حجم التخزين وحالة التحميل. |
| **Profile** | All | EXISTING_NEEDS_REDESIGN | `profile`<br>`app/ProfileScreen.kt`<br>vs `feature-profile/ProfileViewModel.kt` | عرض إحصائيات المستخدم والشهادات والرتب | UserRepository | نعم | تفتقر حالياً لإمكانية رفع أو اختيار الصورة الشخصية وإدارة بيانات الحساب. |
| **Settings** | All | EXISTING | `settings`<br>`app/SettingsScreen.kt`<br>vs `feature-settings/SettingsViewModel.kt` | التحكم باللغة، المظهر، والمزامنة | SettingsViewModel | نعم | تعمل بشكل جيد وتحتاج إضافة خيارات الخصوصية والإشعارات. |
| **Academic Hub / Multi-tenant** | All | EXISTING | `academic_platform`<br>`app/AcademicPlatformScreen.kt` | بوابة التبديل بين الكورسات العامة والتعليم المؤسسي | AcademicPlatformViewModel | نعم | واجهة عرض موحدة لدعم المدارس والمؤسسات. |
| **Admin Dashboard** | School/Admin | EXISTING | `admin_dashboard`<br>`feature-admin/AdminScreen.kt` | لوحة المؤشرات الإحصائية لإدارة المؤسسة | EnterpriseDao, AcademicDao | نعم | مبنية باستخدام RDS Card ومؤشرات المدارس والطلاب. |
| **Classes Management** | School/Admin | EXISTING | `classes_management`<br>`feature-admin/ClassesScreen.kt` | إدارة الفصول الدراسية وتوزيع الطلاب | ClassesViewModel, EnterpriseDao | نعم | تدعم الإضافة والعرض والربط. |
| **Users Management** | School/Admin | EXISTING | `users_management`<br>`feature-admin/UsersScreen.kt` | إدارة المعلمين والطلاب وحساباتهم | UserManagementViewModel | نعم | تدعم البحث وإضافة المستخدمين وتعيين الأدوار. |
| **Academic Structure** | School/Admin | EXISTING | `academic_structure`<br>`feature-admin/AcademicStructureScreen.kt` | إدارة التخصصات، الأقسام، والسنوات الدراسية | EnterpriseDao | نعم | واجهة شجرية لإدارة الهيكل الأكاديمي. |
| **Schools Management** | Platform Admin | EXISTING | `schools_management`<br>`feature-admin/SchoolsScreen.kt` | إدارة المدارس والمؤسسات التابعة للمنصة | SchoolsViewModel, EnterpriseDao | نعم | مخصصة لمدير المنصة العام (Super Admin). |
| **Role Selection / Onboarding** | All | MISSING | *غير موجود* | تحديد نوع الحساب (طالب مستقل، طالب مدرسة، معلم) | UserProfile | **نعم (مرشح)** | شاشة لازمة لتخصيص تجربة البداية. |
| **Notifications Center** | All | MISSING | *غير موجود* | استعراض إشعارات المهام، التقييمات، وتحديثات المزامنة | NotificationRepository | **نعم (مرشح)** | ضرورية لإشعار الطلاب والأساتذة. |
| **Global Search & Filter** | Student / Teacher | MISSING | *غير موجود* | بحث شامل في الدروس، الكورسات، والأسئلة | SearchRepository | **نعم (مرشح)** | ترفع من سهولة الوصول للمحتوى. |
| **Achievements & Badges Hub** | Student | MISSING | *غير موجود* | معرض الأوسمة، الشهادات، وسجل الإنجازات المفصل | AchievementDao | **نعم (مرشح)** | الجداول موجودة في Core (`AchievementBadgeEntity`) وتنتظر واجهة عرض. |
| **Teacher Dashboard** | Teacher | FUNCTIONAL_LATER | *غير موجود* | لوحة تحكم المعلم لمتابعة الفصول والواجبات | EnterpriseDao, AcademicDao | **نعم (مرشح)** | تجربة محورية لدور المعلم داخل المدارس. |
| **Assignments & Submissions** | Student / Teacher | FUNCTIONAL_LATER | *غير موجود* | تسليم الواجبات وتصحيحها ورصد الدرجات | AcademicDao (`AssignmentEntity`) | **نعم (مرشح)** | النماذج والجداول مهيأة في قاعدة البيانات. |
| **Student Gradebook / Reports** | Student / Parent | FUNCTIONAL_LATER | *غير موجود* | كشف الدرجات التفصيلي والتقارير الأكاديمية | AcademicDao (`GradebookRecordEntity`) | **نعم (مرشح)** | الجداول مهيأة في قاعدة البيانات. |

---

## C. تجارب المستخدم حسب الأدوار (User Roles & Experiences)

1. **Student / Learner (الطالب في مدرسة):**
   - التركيز: الفصول المرتبطة، الواجبات، تقييمات الدروس، كشف الدرجات، والمزامنة السحابية عند الاتصال.
2. **Independent Learner (المتعلم المستقل):**
   - التركيز: استكشاف الكورسات الحرة، التلعيب (XP والعملات والسلسلة اليومية)، المحادثة مع المعلم الذكي (AI Tutor)، والتحميل للمشاهدة بدون إنترنت.
3. **Teacher (المعلم):**
   - التركيز: متابعة تقدم طلاب الفصل، مراجعة التسليمات، إضافة التقييمات، والتواصل مع الطلاب.
4. **School / Institutional Admin (مدير المدرسة/المؤسسة):**
   - التركيز: هيكل الفصول، إدارة حسابات المعلمين والطلاب، تقارير الحضور والإنجاز المؤسسي.
5. **Platform Administration (إدارة المنصة المركزية):**
   - التركيز: إدارة المدارس والمؤسسات، الاشتراكات، مؤشرات الأداء الكلية، والتحكم في إعدادات النظام.

---

## D. مجالات المنتج العالمية (Global Product Areas)

- **Onboarding & Auth:** البداية التعريفية، المصادقة، وتحديد المسار/الدور.
- **Learning Core:** الكورسات، الوحدات، مشغل الدروس، والمحتوى التفاعلي.
- **Assessment & Practice:** الاختبارات، التقييمات التكوينية، بنك الأسئلة، والواجبات.
- **Gamification & Engagement:** السلسلة اليومية (Streak)، الأوسمة، رصيد النقاط، ولوحة الشرف.
- **AI Intelligence:** المعلم التفاعلي الذكي (AI Tutor)، والتحليلات والتوصيات الموجهة.
- **Offline Infrastructure:** مركز التنزيلات، مؤشرات حالة الاتصال، وطابور المزامنة المرئي.
- **Institutional Workflows:** إدارة الفصول، الطلاب، والمناهج المدرسية.
- **Personal & System:** الملف الشخصي، التفضيلات، الأمان، وإدارة الجلسات.

---

## E. متطلبات تجربة المستخدم العالمية (Global UX Requirements)

1. **RTL / LTR:** دعم كامل للغة العربية والإنجليزية عبر معايير التخطيط المرن في Compose دون تثبيت محاذاة قسرية.
2. **Accessibility (إمكانية الوصول):** أحجام أهداف لمس لا تقل عن 48dp، تباين ألوان يتوافق مع WCAG 2.1 AA، وتوفير `contentDescription` شامل لجميع الأيقونات.
3. **Theming:** دعم كامل لنمطي Light و Dark باستخدام نظام Material 3 ومكونات RDS الموحدة.
4. **Phone / Tablet Responsiveness:** تكيف الشاشات بين الهواتف والأجهزة اللوحية (NavigationRail على الأجهزة الكبيرة بدلاً من BottomBar).
5. **States Handling:** التزام كافة الشاشات بعرض الحالات الأربع: (Loading عبر `RdsLoadingState`, Empty عبر `RdsEmptyState`, Error عبر `RdsErrorState`, و Success Content).
6. **Connectivity States:** ظهور شريط التنبيه `OfflineModeBanner` عند فقد الاتصال دون حجب إمكانية التصفح للمحتوى المحمل.

---

## F. أولوية الشاشات للنموذج الأولي (Screen Priority)

### 1. CORE PRODUCT (الأساس التشغيلي والتعليمي)
- Login / Register
- Learner Home Dashboard
- Courses Catalog & Detail
- Unified Lesson Viewer
- Quiz & Assessment Play
- AI Tutor Screen
- Profile & Settings

### 2. SUPPORTING (الداعمة للمنظومة والربط المؤسسي)
- Multi-step Welcome & Role Selection
- Admin & School Dashboard
- Classes & Users Management
- Downloads & Offline Hub
- Notifications Center

### 3. ADVANCED / FUTURE (المتقدمة والتوسعية)
- Assignments & Student Submissions
- Gradebook & Academic Reports
- Detailed Badges & Achievements Hub
- Global Search & Filter
