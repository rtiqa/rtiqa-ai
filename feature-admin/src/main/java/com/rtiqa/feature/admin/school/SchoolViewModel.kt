package com.rtiqa.feature.admin.school

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.data.datastore.RtiqaPreferencesDataStore
import com.rtiqa.core.domain.model.AcademicYear
import com.rtiqa.core.domain.model.Assessment
import com.rtiqa.core.domain.model.AssessmentType
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.EducationStage
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.EnterpriseRole
import com.rtiqa.core.domain.model.GradeLevel
import com.rtiqa.core.domain.model.School
import com.rtiqa.core.domain.model.SchoolClass
import com.rtiqa.core.domain.model.Section
import com.rtiqa.core.domain.model.Subject
import com.rtiqa.core.domain.usecase.DeleteAcademicYearUseCase
import com.rtiqa.core.domain.usecase.DeleteClassUseCase
import com.rtiqa.core.domain.usecase.DeleteGradeLevelUseCase
import com.rtiqa.core.domain.usecase.DeleteSchoolUseCase
import com.rtiqa.core.domain.usecase.DeleteSectionUseCase
import com.rtiqa.core.domain.usecase.DeleteSubjectUseCase
import com.rtiqa.core.domain.usecase.GetAcademicYearsUseCase
import com.rtiqa.core.domain.usecase.GetAssessmentsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetClassesForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetCoursesForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetGradeLevelsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetSchoolsUseCase
import com.rtiqa.core.domain.usecase.GetSectionsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetStudentsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetSubjectsForSchoolUseCase
import com.rtiqa.core.domain.usecase.GetTeachersForSchoolUseCase
import com.rtiqa.core.domain.usecase.SaveAcademicYearUseCase
import com.rtiqa.core.domain.usecase.SaveAssessmentUseCase
import com.rtiqa.core.domain.usecase.SaveClassUseCase
import com.rtiqa.core.domain.usecase.SaveCourseUseCase
import com.rtiqa.core.domain.usecase.SaveEnterpriseMemberUseCase
import com.rtiqa.core.domain.usecase.SaveGradeLevelUseCase
import com.rtiqa.core.domain.usecase.SaveSchoolUseCase
import com.rtiqa.core.domain.usecase.SaveSectionUseCase
import com.rtiqa.core.domain.usecase.SaveSubjectUseCase
import com.rtiqa.core.ui.base.BaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class SchoolViewModel(
    private val getSchoolsUseCase: GetSchoolsUseCase,
    private val saveSchoolUseCase: SaveSchoolUseCase,
    private val deleteSchoolUseCase: DeleteSchoolUseCase,
    private val getStudentsForSchoolUseCase: GetStudentsForSchoolUseCase,
    private val getTeachersForSchoolUseCase: GetTeachersForSchoolUseCase,
    private val getSectionsForSchoolUseCase: GetSectionsForSchoolUseCase,
    private val getSubjectsForSchoolUseCase: GetSubjectsForSchoolUseCase,
    private val getCoursesForSchoolUseCase: GetCoursesForSchoolUseCase,
    private val getAssessmentsForSchoolUseCase: GetAssessmentsForSchoolUseCase?,
    private val saveEnterpriseMemberUseCase: SaveEnterpriseMemberUseCase?,
    private val saveCourseUseCase: SaveCourseUseCase?,
    private val saveAssessmentUseCase: SaveAssessmentUseCase?,
    private val getGradeLevelsForSchoolUseCase: GetGradeLevelsForSchoolUseCase? = null,
    private val saveGradeLevelUseCase: SaveGradeLevelUseCase? = null,
    private val deleteGradeLevelUseCase: DeleteGradeLevelUseCase? = null,
    private val getClassesForSchoolUseCase: GetClassesForSchoolUseCase? = null,
    private val saveClassUseCase: SaveClassUseCase? = null,
    private val deleteClassUseCase: DeleteClassUseCase? = null,
    private val getAcademicYearsUseCase: GetAcademicYearsUseCase? = null,
    private val saveAcademicYearUseCase: SaveAcademicYearUseCase? = null,
    private val deleteAcademicYearUseCase: DeleteAcademicYearUseCase? = null,
    private val saveSectionUseCase: SaveSectionUseCase? = null,
    private val deleteSectionUseCase: DeleteSectionUseCase? = null,
    private val saveSubjectUseCase: SaveSubjectUseCase? = null,
    private val deleteSubjectUseCase: DeleteSubjectUseCase? = null,
    private val preferencesDataStore: RtiqaPreferencesDataStore
) : BaseViewModel<SchoolUiState, SchoolUiAction, SchoolUiEvent>(SchoolUiState()) {

    init {
        seedInitialSchoolsIfEmpty()
        observeData()
    }

    private fun seedInitialSchoolsIfEmpty() {
        viewModelScope.launch {
            getSchoolsUseCase().collect { currentSchools ->
                if (currentSchools.isEmpty()) {
                    val defaultSchool1 = School(
                        id = "school_001",
                        name = "مدرسة المستقبل النموذجية",
                        code = "SCH-001",
                        address = "الرياض - طريق الملك فهد",
                        phone = "+966501234567",
                        studentsCount = 450,
                        teachersCount = 35
                    )
                    val defaultSchool2 = School(
                        id = "school_002",
                        name = "مدرسة التفوق الدولية",
                        code = "SCH-002",
                        address = "جدة - حي الزهراء",
                        phone = "+966507654321",
                        studentsCount = 320,
                        teachersCount = 28
                    )
                    saveSchoolUseCase(defaultSchool1)
                    saveSchoolUseCase(defaultSchool2)

                    // Seed default sample members for School 1
                    saveEnterpriseMemberUseCase?.invoke(
                        EnterpriseMember(
                            id = "std_s1_1",
                            orgId = "org_1",
                            name = "علي أحمد المظفر",
                            email = "ali@school1.edu",
                            role = EnterpriseRole.STUDENT,
                            department = "علوم الحاسب",
                            schoolId = "school_001"
                        )
                    )
                    saveEnterpriseMemberUseCase?.invoke(
                        EnterpriseMember(
                            id = "tch_s1_1",
                            orgId = "org_1",
                            name = "أ.د. عبد الله الشهري",
                            email = "abdullah@school1.edu",
                            role = EnterpriseRole.TEACHER,
                            department = "الرياضيات التطبيقية",
                            schoolId = "school_001"
                        )
                    )

                    // Seed default sample members for School 2
                    saveEnterpriseMemberUseCase?.invoke(
                        EnterpriseMember(
                            id = "std_s2_1",
                            orgId = "org_1",
                            name = "سارة خالد العتيبي",
                            email = "sara@school2.edu",
                            role = EnterpriseRole.STUDENT,
                            department = "الفيزياء الكلاسيكية",
                            schoolId = "school_002"
                        )
                    )
                    saveEnterpriseMemberUseCase?.invoke(
                        EnterpriseMember(
                            id = "tch_s2_1",
                            orgId = "org_1",
                            name = "م. ريم الشمري",
                            email = "reem@school2.edu",
                            role = EnterpriseRole.TEACHER,
                            department = "الكيمياء الحيوية",
                            schoolId = "school_002"
                        )
                    )

                    // Seed courses
                    saveCourseUseCase?.invoke(
                        Course(
                            id = "c_sch1_1",
                            title = "برمجة أندرويد بـ Kotlin (مدرسة المستقبل)",
                            description = "دورة شاملة في البرمجة للمستقبل",
                            category = "تطوير البرمجيات",
                            totalLessons = 10,
                            durationMinutes = 60,
                            schoolId = "school_001"
                        )
                    )
                    saveCourseUseCase?.invoke(
                        Course(
                            id = "c_sch2_1",
                            title = "أساسيات الفيزياء (مدرسة التفوق)",
                            description = "دورة متقدمة في الفيزياء الدولية",
                            category = "العلوم",
                            totalLessons = 8,
                            durationMinutes = 45,
                            schoolId = "school_002"
                        )
                    )

                    // Seed Academic Years
                    saveAcademicYearUseCase?.invoke(
                        AcademicYear(
                            id = "ay_2024_2025",
                            orgId = "org_1",
                            name = "العام الدراسي 2024 - 2025",
                            startDate = "2024-09-01",
                            endDate = "2025-06-30",
                            isCurrent = true
                        )
                    )

                    // Seed Grade Levels
                    saveGradeLevelUseCase?.invoke(
                        GradeLevel(
                            id = "gl_sec_1",
                            schoolId = "school_001",
                            name = "الصف الأول الثانوي",
                            code = "SEC-1",
                            levelSequence = 1,
                            stage = EducationStage.SECONDARY
                        )
                    )

                    // Seed School Classes
                    saveClassUseCase?.invoke(
                        SchoolClass(
                            id = "cls_101",
                            schoolId = "school_001",
                            name = "101 ثانٍ",
                            gradeLevel = "الصف الأول الثانوي",
                            roomNumber = "A-12",
                            capacity = 30,
                            displayOrder = 1
                        )
                    )
                }
            }
        }
    }

    private fun observeData() {
        val schoolsFlow = getSchoolsUseCase()
        val activeSchoolIdFlow = preferencesDataStore.userPreferencesFlow

        combine(schoolsFlow, activeSchoolIdFlow) { schools, userPrefs ->
            val activeId = userPrefs.activeSchoolId
            val activeSchool = schools.find { it.id == activeId } ?: schools.firstOrNull()
            setState {
                copy(
                    schools = schools,
                    activeSchoolId = activeId,
                    activeSchool = activeSchool,
                    isLoading = false
                )
            }
            activeId
        }.flatMapLatest { schoolId ->
            val gradeLevelsFlow = getGradeLevelsForSchoolUseCase?.invoke(schoolId) ?: flowOf(emptyList())
            val classesFlow = getClassesForSchoolUseCase?.invoke(schoolId) ?: flowOf(emptyList())
            val academicYearsFlow = getAcademicYearsUseCase?.invoke("org_1") ?: flowOf(emptyList())
            val assessmentsFlow = getAssessmentsForSchoolUseCase?.invoke(schoolId) ?: flowOf(emptyList())

            combine(
                getStudentsForSchoolUseCase(schoolId),
                getTeachersForSchoolUseCase(schoolId),
                getSectionsForSchoolUseCase(schoolId),
                getSubjectsForSchoolUseCase(schoolId),
                getCoursesForSchoolUseCase(schoolId)
            ) { students, teachers, sections, subjects, courses ->
                Part1Data(students, teachers, sections, subjects, courses)
            }.flatMapLatest { p1 ->
                combine(
                    gradeLevelsFlow,
                    classesFlow,
                    academicYearsFlow,
                    assessmentsFlow
                ) { gradeLevels, schoolClasses, academicYears, assessments ->
                    setState {
                        copy(
                            students = p1.students,
                            teachers = p1.teachers,
                            sections = p1.sections,
                            subjects = p1.subjects,
                            courses = p1.courses,
                            gradeLevels = gradeLevels,
                            schoolClasses = schoolClasses,
                            academicYears = academicYears,
                            assessments = assessments
                        )
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: SchoolUiAction) {
        when (action) {
            is SchoolUiAction.SelectActiveSchool -> {
                viewModelScope.launch {
                    preferencesDataStore.setActiveSchoolId(action.schoolId)
                    sendEvent(SchoolUiEvent.ShowToast("تم اختيار المدرسة النشطة بنجاح"))
                }
            }
            is SchoolUiAction.OpenFormDialog -> {
                setState { copy(editingSchool = action.school, isFormDialogOpen = true) }
            }
            is SchoolUiAction.CloseFormDialog -> {
                setState { copy(editingSchool = null, isFormDialogOpen = false) }
            }
            is SchoolUiAction.SaveSchool -> {
                saveSchool(action)
            }
            is SchoolUiAction.DeleteSchool -> {
                viewModelScope.launch {
                    deleteSchoolUseCase(action.schoolId)
                    sendEvent(SchoolUiEvent.ShowToast("تم حذف المدرسة"))
                }
            }
            is SchoolUiAction.SelectTab -> {
                setState { copy(selectedTab = action.index) }
            }
            is SchoolUiAction.SelectCategoryTab -> {
                setState { copy(activeCategoryTab = action.index) }
            }
            is SchoolUiAction.SaveAcademicYear -> {
                saveAcademicYear(action)
            }
            is SchoolUiAction.DeleteAcademicYear -> {
                viewModelScope.launch {
                    deleteAcademicYearUseCase?.invoke(action.id)
                    sendEvent(SchoolUiEvent.ShowToast("تم حذف العام الدراسي"))
                }
            }
            is SchoolUiAction.SaveGradeLevel -> {
                saveGradeLevel(action)
            }
            is SchoolUiAction.DeleteGradeLevel -> {
                viewModelScope.launch {
                    deleteGradeLevelUseCase?.invoke(action.id)
                    sendEvent(SchoolUiEvent.ShowToast("تم حذف المرحلة الدراسية"))
                }
            }
            is SchoolUiAction.SaveSchoolClass -> {
                saveSchoolClass(action)
            }
            is SchoolUiAction.DeleteSchoolClass -> {
                viewModelScope.launch {
                    deleteClassUseCase?.invoke(action.id)
                    sendEvent(SchoolUiEvent.ShowToast("تم حذف الصف الدراسي"))
                }
            }
            is SchoolUiAction.SaveSection -> {
                saveSection(action)
            }
            is SchoolUiAction.DeleteSection -> {
                viewModelScope.launch {
                    deleteSectionUseCase?.invoke(action.id)
                    sendEvent(SchoolUiEvent.ShowToast("تم حذف الشعبة"))
                }
            }
            is SchoolUiAction.SaveSubject -> {
                saveSubject(action)
            }
            is SchoolUiAction.DeleteSubject -> {
                viewModelScope.launch {
                    deleteSubjectUseCase?.invoke(action.id)
                    sendEvent(SchoolUiEvent.ShowToast("تم حذف المادة الدراسية"))
                }
            }
            is SchoolUiAction.AddStudentToActiveSchool -> {
                addMember(action.name, action.email, action.department, EnterpriseRole.STUDENT)
            }
            is SchoolUiAction.AddTeacherToActiveSchool -> {
                addMember(action.name, action.email, action.department, EnterpriseRole.TEACHER)
            }
            is SchoolUiAction.AddCourseToActiveSchool -> {
                addCourse(action.title, action.category)
            }
            is SchoolUiAction.AddAssessmentToActiveSchool -> {
                addAssessment(action.title, action.passingScore)
            }
        }
    }

    private fun saveSchool(action: SchoolUiAction.SaveSchool) {
        viewModelScope.launch {
            val schoolId = action.id ?: "school_${UUID.randomUUID().toString().take(8)}"
            val school = School(
                id = schoolId,
                name = action.name,
                code = action.code,
                address = action.address,
                phone = action.phone,
                studentsCount = action.studentsCount,
                teachersCount = action.teachersCount
            )
            saveSchoolUseCase(school)
            setState { copy(isFormDialogOpen = false, editingSchool = null) }
            sendEvent(SchoolUiEvent.ShowToast(if (action.id == null) "تمت إضافة المدرسة بنجاح" else "تم تحديث بيانات المدرسة"))
        }
    }

    private fun saveAcademicYear(action: SchoolUiAction.SaveAcademicYear) {
        viewModelScope.launch {
            val ayId = action.id ?: "ay_${UUID.randomUUID().toString().take(6)}"
            val academicYear = AcademicYear(
                id = ayId,
                orgId = "org_1",
                name = action.name,
                startDate = action.startDate,
                endDate = action.endDate,
                isCurrent = action.isCurrent
            )
            saveAcademicYearUseCase?.invoke(academicYear)
            sendEvent(SchoolUiEvent.ShowToast(if (action.id == null) "تمت إضافة العام الدراسي بنجاح" else "تم تحديث العام الدراسي"))
        }
    }

    private fun saveGradeLevel(action: SchoolUiAction.SaveGradeLevel) {
        viewModelScope.launch {
            val glId = action.id ?: "gl_${UUID.randomUUID().toString().take(6)}"
            val gradeLevel = GradeLevel(
                id = glId,
                schoolId = currentState.activeSchoolId,
                name = action.name,
                code = action.code,
                levelSequence = action.sequence,
                stage = action.stage
            )
            saveGradeLevelUseCase?.invoke(gradeLevel)
            sendEvent(SchoolUiEvent.ShowToast(if (action.id == null) "تمت إضافة المرحلة الدراسية بنجاح" else "تم تحديث المرحلة الدراسية"))
        }
    }

    private fun saveSchoolClass(action: SchoolUiAction.SaveSchoolClass) {
        viewModelScope.launch {
            val classId = action.id ?: "cls_${UUID.randomUUID().toString().take(6)}"
            val schoolClass = SchoolClass(
                id = classId,
                schoolId = currentState.activeSchoolId,
                name = action.name,
                gradeLevel = action.gradeLevel,
                roomNumber = action.roomNumber,
                capacity = action.capacity,
                displayOrder = currentState.schoolClasses.size + 1
            )
            saveClassUseCase?.invoke(schoolClass)
            sendEvent(SchoolUiEvent.ShowToast(if (action.id == null) "تمت إضافة الصف الدراسي بنجاح" else "تم تحديث الصف الدراسي"))
        }
    }

    private fun saveSection(action: SchoolUiAction.SaveSection) {
        viewModelScope.launch {
            val secId = action.id ?: "sec_${UUID.randomUUID().toString().take(6)}"
            val section = Section(
                id = secId,
                majorId = currentState.activeSchoolId,
                semesterId = "sem_001",
                branchId = "branch_001",
                name = action.name,
                capacity = action.capacity,
                studentsCount = 0,
                schoolId = currentState.activeSchoolId
            )
            saveSectionUseCase?.invoke(section)
            sendEvent(SchoolUiEvent.ShowToast(if (action.id == null) "تمت إضافة الشعبة بنجاح" else "تم تحديث الشعبة"))
        }
    }

    private fun saveSubject(action: SchoolUiAction.SaveSubject) {
        viewModelScope.launch {
            val subId = action.id ?: "sub_${UUID.randomUUID().toString().take(6)}"
            val subject = Subject(
                id = subId,
                majorId = currentState.activeSchoolId,
                name = action.name,
                code = action.code,
                creditHours = action.creditHours,
                schoolId = currentState.activeSchoolId
            )
            saveSubjectUseCase?.invoke(subject)
            sendEvent(SchoolUiEvent.ShowToast(if (action.id == null) "تمت إضافة المادة الدراسية بنجاح" else "تم تحديث المادة الدراسية"))
        }
    }

    private fun addMember(name: String, email: String, department: String, role: EnterpriseRole) {
        viewModelScope.launch {
            val currentSchoolId = currentState.activeSchoolId
            val member = EnterpriseMember(
                id = "${role.name.lowercase()}_${UUID.randomUUID().toString().take(6)}",
                orgId = "org_1",
                name = name,
                email = email,
                role = role,
                department = department,
                schoolId = currentSchoolId
            )
            saveEnterpriseMemberUseCase?.invoke(member)
            sendEvent(SchoolUiEvent.ShowToast("تمت إضافة ${if (role == EnterpriseRole.STUDENT) "الطالب" else "المعلم"} للمدرسة الحالية"))
        }
    }

    private fun addCourse(title: String, category: String) {
        viewModelScope.launch {
            val currentSchoolId = currentState.activeSchoolId
            val course = Course(
                id = "c_${UUID.randomUUID().toString().take(6)}",
                title = title,
                description = "دورة تدريبية مخصصة للمدرسة الحالية",
                category = category,
                totalLessons = 10,
                durationMinutes = 60,
                schoolId = currentSchoolId
            )
            saveCourseUseCase?.invoke(course)
            sendEvent(SchoolUiEvent.ShowToast("تمت إضافة الدورة للمدرسة الحالية"))
        }
    }

    private fun addAssessment(title: String, passingScore: Int) {
        viewModelScope.launch {
            val currentSchoolId = currentState.activeSchoolId
            val assessment = Assessment(
                id = "asm_${UUID.randomUUID().toString().take(6)}",
                courseId = "c_sch1_1",
                orgId = "org_1",
                title = title,
                type = AssessmentType.QUIZ,
                passingScore = passingScore,
                timeLimitMinutes = 30,
                totalQuestions = 10,
                schoolId = currentSchoolId
            )
            saveAssessmentUseCase?.invoke(assessment)
            sendEvent(SchoolUiEvent.ShowToast("تمت إضافة الاختبار للمدرسة الحالية"))
        }
    }

    private data class Part1Data(
        val students: List<EnterpriseMember>,
        val teachers: List<EnterpriseMember>,
        val sections: List<Section>,
        val subjects: List<Subject>,
        val courses: List<Course>
    )
}
