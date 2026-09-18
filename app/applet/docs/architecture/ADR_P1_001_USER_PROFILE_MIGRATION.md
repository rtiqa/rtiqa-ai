# خطة معمارية لترحيل ومطابقة الملف الشخصي
# ADR_P1_001: UserProfile Migration & Schema Harmonization Plan

تاريخ الوثيقة: 2026-09-18  
طبيعة الوثيقة: **تخطيط وتوثيق معماري فقط — لا يترتب عليها أي تعديل تنفيذي في الكود حالياً**.

---

## 1. Current State (الوضع الراهن)
يوجد نموذجان منفصلان لـ `UserProfile` داخل المشروع:
- **نموذج App:**
  - الجدول: `user_profile` (مفرد) داخل قاعدة `app.RtiqaDatabase` (الإصدار 1).
  - الكيان: `com.rtiqa.mobile.data.local.entity.UserProfileEntity`.
  - الاستخدام: يغذي مباشرة واجهات التطبيق الرئيسية (`MainViewModel`, `HomeScreen`, `ProfileScreen`, `SettingsScreen`) عبر `com.rtiqa.mobile.data.repository.UserRepository`.
  - الخصائص: يحتوي على حقول واجهة وتفضيلات محلية (`coins`, `level`, `currentGoal`, `language`, `isDarkMode`, `avatarResName`).
- **نموذج Core:**
  - الجدول: `user_profiles` (جمع) داخل قاعدة `core.RtiqaDatabase` (الإصدار 8).
  - الكيان: `com.rtiqa.core.database.entity.DatabaseEntities.UserProfileEntity`.
  - الاستخدام: يغذي موديولات النطاق والمصادقة والملف الشخصي المؤسسي (`core-domain`, `core-data`, `feature-profile`, `feature-home`, `feature-auth`) عبر `AppDiContainer` و `UserRepositoryImpl` و `AuthRepositoryImpl`.
  - الخصائص: يدعم النطاق المؤسسي (`schoolId`, `isAdmin`, `avatarUrl`, `levelXp`).

---

## 2. Field-by-field Mapping Table (جدول مطابقة الحقول)

| Field | App Entity (`user_profile`) | Core Entity (`user_profiles`) | Action / Strategy | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **id** | `String` (default `""`) | `String` (no default) | نسخ مباشر | معرّف المستخدم الموحد (UID). |
| **name** | `String` | `String` | نسخ مباشر | متطابق نوعياً ودلالياً. |
| **email** | `String` | `String` | نسخ مباشر | متطابق نوعياً ودلالياً. |
| **avatar** | `avatarResName: String` | `avatarUrl: String?` | Mapping / تحويل | App يستخدم drawable محلي، وCore يستخدم URL سحابي. يتطلب دعم الحقلين أو وجود آلية تحويل واضحة. |
| **xp / levelXp** | `xp: Int` | `levelXp: Int` | Mapping اسم الحقل | `core.levelXp = app.xp`. كلاهما يمثل نفس القيمة العددية. |
| **streakDays** | `Int` | `Int` | نسخ مباشر | متطابق دلالياً ونوعياً. |
| **coins** | `Int` | *غير موجود* | إضافة لـ Core أو حفظ بديل | موجود في App فقط. يتطلب اتخاذ قرار بدمجه في Core Entity أو معالجته. |
| **level** | `Int` | *غير موجود* | حقل محسوب (Computed) | يحسب في App بالمعادلة `(levelXp / 100).coerceAtLeast(1)`. |
| **currentGoal** | `String` | *غير موجود* | إضافة لـ Core أو حفظ بديل | نص الهدف التعليمي للمستخدم؛ موجود في App فقط. |
| **language** | `String` | *غير موجود* | نقل إلى DataStore أو Core | إعداد لغة الواجهة؛ موجود في App فقط. |
| **isDarkMode** | `Boolean` | *غير موجود* | نقل إلى DataStore أو Core | تفضيل المظهر الداكن؛ موجود في App فقط. |
| **offline sync** | `isOfflineAutoSyncEnabled: Boolean` | `isOfflineModeEnabled: Boolean` | **UNPROVEN** | اختلاف في المسمى والدلالة المحتملة؛ ممنوع افتراض التطابق التام دون حسم دلالي. |
| **isAdmin** | *غير موجود* | `Boolean` (default `false`) | الاحتفاظ به في Core | خاص بالصلاحيات المؤسسية في Core. |
| **schoolId** | *غير موجود* | `String?` (default `null`) | الاحتفاظ به في Core | خاص بالتبعية المدرسية في Core. |

---

## 3. Data Ownership (ملكية البيانات)
- بناءً على **ADR-001** المعتمد، فإن `core.RtiqaDatabase` (وجداولها في `:core-database`) هي **مصدر الحقيقة المستهدف (Target Source of Truth)** للمشروع مستقبلاً.
- أثناء مرحلة الانتقال الحالية، تظل ملكية قراءة وكتابة التفضيلات المحلية في شاشات العميل تابعة لـ App مؤقتاً، بينما تتبع بيانات المصادقة والجلسة والمؤسسة لـ Core، إلى حين اكتمال التوحيد.

---

## 4. Migration Strategy (استراتيجية الترحيل)
1. **المرحلة التمهيدية (Schema Harmonization):**
   - تعديل كيان `core.UserProfileEntity` ليتضمن الحقول المطلوبة لواجهات المستخدم أو اعتماد طبقة تفضيلات منفصلة (مثل DataStore) للخصائص غير السحابية (`isDarkMode`, `language`).
   - ترقية إصدار `core.RtiqaDatabase` عبر كتابة Migration رسمي يضيف الأعمدة الجديدة لجدول `user_profiles`.
2. **مرحلة ترحيل البيانات المحلية (Local Data Transfer):**
   - كتابة آلية لمرة واحدة (One-time migration helper) تقرأ السجل الموجود في `app.user_profile` وتقوم بترحيله/دمجه داخل `core.user_profiles`.
3. **مرحلة إعادة توجيه الـ ViewModels:**
   - توجيه `MainViewModel` للاعتماد على `core-domain` UseCases و `core-data.UserRepositoryImpl`.
4. **مرحلة التحقق والتعطيل:**
   - التأكد من تطابق كامل بيانات الواجهة من مصدر الحقيقة الموحد، ثم عزل وإيقاف مسار App القديم.

---

## 5. Compatibility Requirements (متطلبات التوافق)
- **عدم فقدان البيانات:** الحفاظ على عدد النقاط (`xp`) وسلسلة الأيام (`streakDays`) للمستخدم الحالي أثناء الترحيل.
- **استمرارية الواجهة:** عدم حدوث وميض أو انهيار في شاشات `HomeScreen` أو `ProfileScreen` أو `SettingsScreen` أثناء قراءة الحقول بعد التحويل.
- **التوافق مع السحابة:** الحفاظ على تكامل الـ DTOs الخاصة بـ Firestore أو Ktor دون التأثير على الحقول المؤسسية (`schoolId`, `isAdmin`).

---

## 6. Risks (المخاطر)
- **Room Migration Crash:** خطر فشل ترقية قاعدة البيانات `core.RtiqaDatabase` في حال وجود خطأ في تعريفات الأعمدة الجديدة أو الـ Nullability.
- **تضارب دلالة وضع عدم الاتصال [UNPROVEN]:** خطر تفعيل أو تعطيل وضع الأوفلاين بشكل خاطئ بسبب الخلط بين `isOfflineAutoSyncEnabled` و `isOfflineModeEnabled`.
- **فقدان تفضيلات المظهر واللغة:** في حال إهمال حقول `isDarkMode` أو `language` دون نقلها لمكان تخزين بديل وموثوق.

---

## 7. Preconditions Before Code Changes (الشروط المسبقة قبل التعديل)
1. حسم مصير تفضيلات المستخدم (هل تدمج في `user_profiles` أم تنقل إلى `DataStore`).
2. حسم الدلالة الدقيقة لحقل `isOfflineAutoSyncEnabled` مقابل `isOfflineModeEnabled`.
3. إعداد اختبارات Unit و Migration مسبقة للتأكد من نجاح الترحيل دون فقدان بيانات.

---

## 8. Exact Files / Classes Expected to Change (الملفات المتوقع تعديلها لاحقاً)
- `core-database/src/main/java/com/rtiqa/core/database/entity/DatabaseEntities.kt`
- `core-database/src/main/java/com/rtiqa/core/database/RtiqaDatabase.kt`
- `core-domain/src/main/java/com/rtiqa/core/domain/model/UserProfile.kt`
- `core-data/src/main/java/com/rtiqa/core/data/mapper/Mappers.kt`
- `core-data/src/main/java/com/rtiqa/core/data/repository/UserRepositoryImpl.kt`
- `app/src/main/java/com/rtiqa/mobile/ui/viewmodel/MainViewModel.kt`
- `app/src/main/java/com/rtiqa/mobile/data/repository/UserRepository.kt` (إلغاء أو إعادة توجيه)

---

## 9. What Must NOT Be Deleted Yet (ما يُمنع حذفه حالياً)
- **يُمنع حذف `app.RtiqaDatabase` نهائياً.**
- **يُمنع حذف `com.rtiqa.mobile.data.local.entity.UserProfileEntity`.**
- **يُمنع حذف `com.rtiqa.mobile.data.local.dao.UserProfileDao`.**
- **يُمنع حذف `com.rtiqa.mobile.data.repository.UserRepository`.**
- تظل هذه المكونات قائمة وقيد الاستخدام حتى إتمام المراحل واختبارها بنجاح.

---

## 10. Verification Plan (خطة التحقق بعد التنفيذ مستقبلاً)
- **Room Migration Test:** اختبار ترقية قاعدة بيانات Core آلياً عبر اختبارات وحدة Room للتأكد من سلامة الأعمدة.
- **Data Integrity Check:** التأكد من قراءة البيانات المرحلة (`name`, `email`, `xp`, `streakDays`) وظهورها كما هي في شاشة الملف الشخصي.
- **UI & Settings Check:** التأكد من استمرار عمل تعديل المظهر واللغة والهدف دون أي أخطاء وقت التشغيل.
