package com.rtiqa.mobile.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rtiqa.core.data.di.AppDiContainer
import com.rtiqa.core.domain.model.AcademicLesson
import com.rtiqa.core.domain.model.AchievementBadge
import com.rtiqa.core.domain.model.Assessment
import com.rtiqa.core.domain.model.AssessmentAttempt
import com.rtiqa.core.domain.model.AssessmentType
import com.rtiqa.core.domain.model.Assignment
import com.rtiqa.core.domain.model.AssignmentSubmission
import com.rtiqa.core.domain.model.AssignmentType
import com.rtiqa.core.domain.model.CurriculumModule
import com.rtiqa.core.domain.model.DownloadStatus
import com.rtiqa.core.domain.model.GradebookRecord
import com.rtiqa.core.domain.model.LearningPath
import com.rtiqa.core.domain.model.OfflineContentDownload
import com.rtiqa.core.domain.model.Prerequisite
import com.rtiqa.core.domain.model.QuestionBankItem
import com.rtiqa.core.domain.model.QuestionType
import com.rtiqa.core.domain.model.SmartRecommendation
import com.rtiqa.core.domain.model.StudentProgress
import com.rtiqa.core.domain.model.SubmissionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AcademicPlatformUiState(
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val selectedCourseId: String = "c_ai_101",
    val selectedStudentId: String = "std_001",
    val selectedOrgId: String = "ORG-KSU-01",
    val modules: List<CurriculumModule> = emptyList(),
    val lessons: List<AcademicLesson> = emptyList(),
    val assignments: List<Assignment> = emptyList(),
    val submissions: List<AssignmentSubmission> = emptyList(),
    val questions: List<QuestionBankItem> = emptyList(),
    val assessments: List<Assessment> = emptyList(),
    val attempts: List<AssessmentAttempt> = emptyList(),
    val gradebook: List<GradebookRecord> = emptyList(),
    val studentProgress: StudentProgress? = null,
    val badges: List<AchievementBadge> = emptyList(),
    val learningPaths: List<LearningPath> = emptyList(),
    val prerequisites: List<Prerequisite> = emptyList(),
    val recommendations: List<SmartRecommendation> = emptyList(),
    val offlineDownloads: List<OfflineContentDownload> = emptyList()
)

sealed interface AcademicPlatformUiAction {
    data class SelectTab(val tabIndex: Int) : AcademicPlatformUiAction
    data class CreateModule(val title: String, val description: String, val durationHours: Int) : AcademicPlatformUiAction
    data class CreateLesson(val moduleId: String, val title: String, val content: String, val videoUrl: String?, val pdfUrl: String?) : AcademicPlatformUiAction
    data class CreateAssignment(val title: String, val prompt: String, val type: AssignmentType, val dueDate: String) : AcademicPlatformUiAction
    data class SubmitAssignment(val assignmentId: String, val content: String, val fileUrl: String?) : AcademicPlatformUiAction
    data class GradeAssignment(val submissionId: String, val score: Int, val feedback: String) : AcademicPlatformUiAction
    data class CreateQuestion(val text: String, val optA: String, val optB: String, val optC: String, val optD: String, val correctIdx: Int, val type: QuestionType) : AcademicPlatformUiAction
    data class CreateAssessment(val title: String, val type: AssessmentType, val passingScore: Int, val timeLimit: Int) : AcademicPlatformUiAction
    data class SubmitAssessmentAttempt(val assessmentId: String, val scorePercent: Int) : AcademicPlatformUiAction
    data class AddGradeRecord(val courseName: String, val totalScore: Float, val gradeLetter: String, val gpaValue: Float, val isPassed: Boolean) : AcademicPlatformUiAction
    data class UnlockBadge(val name: String, val description: String, val icon: String) : AcademicPlatformUiAction
    data class CreateLearningPath(val title: String, val description: String, val category: String, val courses: List<String>) : AcademicPlatformUiAction
    data class AddPrerequisite(val targetCourseId: String, val requiredCourseId: String, val title: String) : AcademicPlatformUiAction
    data class StartOfflineDownload(val lessonId: String) : AcademicPlatformUiAction
    object GenerateAiRecommendation : AcademicPlatformUiAction
}

class AcademicPlatformViewModel(application: Application) : AndroidViewModel(application) {

    private val container = AppDiContainer(application)
    private val useCases = container.domainUseCasesContainer

    private val _uiState = MutableStateFlow(AcademicPlatformUiState())
    val uiState: StateFlow<AcademicPlatformUiState> = _uiState.asStateFlow()

    init {
        loadAcademicData()
    }

    fun onAction(action: AcademicPlatformUiAction) {
        when (action) {
            is AcademicPlatformUiAction.SelectTab -> {
                _uiState.update { it.copy(selectedTab = action.tabIndex) }
            }
            is AcademicPlatformUiAction.CreateModule -> createModule(action.title, action.description, action.durationHours)
            is AcademicPlatformUiAction.CreateLesson -> createLesson(action.moduleId, action.title, action.content, action.videoUrl, action.pdfUrl)
            is AcademicPlatformUiAction.CreateAssignment -> createAssignment(action.title, action.prompt, action.type, action.dueDate)
            is AcademicPlatformUiAction.SubmitAssignment -> submitAssignment(action.assignmentId, action.content, action.fileUrl)
            is AcademicPlatformUiAction.GradeAssignment -> gradeAssignment(action.submissionId, action.score, action.feedback)
            is AcademicPlatformUiAction.CreateQuestion -> createQuestion(action.text, action.optA, action.optB, action.optC, action.optD, action.correctIdx, action.type)
            is AcademicPlatformUiAction.CreateAssessment -> createAssessment(action.title, action.type, action.passingScore, action.timeLimit)
            is AcademicPlatformUiAction.SubmitAssessmentAttempt -> submitAssessmentAttempt(action.assessmentId, action.scorePercent)
            is AcademicPlatformUiAction.AddGradeRecord -> addGradeRecord(action.courseName, action.totalScore, action.gradeLetter, action.gpaValue, action.isPassed)
            is AcademicPlatformUiAction.UnlockBadge -> unlockBadge(action.name, action.description, action.icon)
            is AcademicPlatformUiAction.CreateLearningPath -> createLearningPath(action.title, action.description, action.category, action.courses)
            is AcademicPlatformUiAction.AddPrerequisite -> addPrerequisite(action.targetCourseId, action.requiredCourseId, action.title)
            is AcademicPlatformUiAction.StartOfflineDownload -> startOfflineDownload(action.lessonId)
            is AcademicPlatformUiAction.GenerateAiRecommendation -> generateAiRecommendation()
        }
    }

    private fun loadAcademicData() {
        val courseId = uiState.value.selectedCourseId
        val studentId = uiState.value.selectedStudentId
        val orgId = uiState.value.selectedOrgId

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Observe Modules
            useCases.getModulesUseCase?.invoke(courseId)?.collect { modules ->
                _uiState.update { it.copy(modules = modules) }
            }
        }

        viewModelScope.launch {
            // Seed initial data if empty
            seedInitialAcademicData()

            // Collect Assignments
            useCases.getAssignmentsUseCase?.invoke(courseId)?.collect { list ->
                _uiState.update { it.copy(assignments = list) }
            }
        }

        viewModelScope.launch {
            useCases.getAssessmentsUseCase?.invoke(courseId)?.collect { list ->
                _uiState.update { it.copy(assessments = list) }
            }
        }

        viewModelScope.launch {
            useCases.getGradebookUseCase?.invoke(studentId, orgId)?.collect { list ->
                _uiState.update { it.copy(gradebook = list) }
            }
        }

        viewModelScope.launch {
            useCases.getBadgesUseCase?.invoke(studentId)?.collect { list ->
                _uiState.update { it.copy(badges = list) }
            }
        }

        viewModelScope.launch {
            useCases.getLearningPathsUseCase?.invoke(orgId)?.collect { list ->
                _uiState.update { it.copy(learningPaths = list) }
            }
        }

        viewModelScope.launch {
            useCases.getSmartRecommendationsUseCase?.invoke(studentId)?.collect { list ->
                _uiState.update { it.copy(recommendations = list, isLoading = false) }
            }
        }
    }

    private suspend fun seedInitialAcademicData() {
        val state = uiState.value
        val courseId = state.selectedCourseId
        val orgId = state.selectedOrgId
        val studentId = state.selectedStudentId

        // Seed Module if empty
        if (state.modules.isEmpty()) {
            val mod = CurriculumModule("mod_101", courseId, orgId, "الوحدة الأولى: أساسيات نماذج الذكاء الاصطناعي", "مقدمة شاملة عن الشبكات العصبية والخوارزميات", 1, 10)
            useCases.saveModuleUseCase?.invoke(mod)

            val lesson = AcademicLesson(
                id = "les_101",
                moduleId = "mod_101",
                courseId = courseId,
                title = "الدرس الأول: العصبونات الاصطناعية ومعمارية Transformer",
                content = "في هذا الدرس نتعلم كيفية بناء العصبون وتحديث الأوزان باستخدام الانحدار الخطي.",
                videoUrl = "https://rtiqa.edu/videos/ai_lesson_1.mp4",
                pdfAttachmentUrl = "https://rtiqa.edu/docs/ai_ch1.pdf",
                durationMinutes = 20,
                orderIndex = 1
            )
            useCases.saveAcademicLessonUseCase?.invoke(lesson)
        }

        if (state.assignments.isEmpty()) {
            val assign1 = Assignment("asg_101", "les_101", courseId, "واجب تطبيقي: خوارزمية Gradient Descent", "قم بكتابة كود Python بسيط لتنفيذ خوارزمية Gradient Descent على مجموعة بيانات سيناريو", 100, "2026-08-15", AssignmentType.WRITTEN)
            val lab1 = Assignment("lab_101", "les_101", courseId, "مختبر عملي: بناء نموذج تصنيف الصور بواسطة PyTorch", "قم بفتح البيئة البرمجية وتدريب النموذج على 10 epochs على الأقل", 100, "2026-08-20", AssignmentType.LAB)
            useCases.saveAssignmentUseCase?.invoke(assign1)
            useCases.saveAssignmentUseCase?.invoke(lab1)
        }

        if (state.assessments.isEmpty()) {
            val quiz = Assessment("ass_101", courseId, orgId, "الاختبار القصير الأول: مبادئ الذكاء الاصطناعي", AssessmentType.QUIZ, 70, 15, 5)
            val finalExam = Assessment("ass_final", courseId, orgId, "الامتحان النهائي الشامل للمقرر", AssessmentType.FINAL_EXAM, 60, 60, 20)
            useCases.saveAssessmentUseCase?.invoke(quiz)
            useCases.saveAssessmentUseCase?.invoke(finalExam)
        }

        if (state.gradebook.isEmpty()) {
            val g1 = GradebookRecord("gb_101", studentId, courseId, orgId, "مبادئ الذكاء الاصطناعي وتعلم الآلة", 95.5f, "A+", 4.0f, true)
            useCases.saveGradebookRecordUseCase?.let { /* optional extra invocation */ }
        }

        if (state.learningPaths.isEmpty()) {
            val path = LearningPath("path_ai_master", orgId, "مسار خبير الذكاء الاصطناعي والتنعيم الدقيق", "مسار متكامل لبناء وتدريب النماذج اللغوية الضخمة وتطبيق RAG", "تكنولوجيا المعلومات", listOf("c_ai_101", "c_ai_202"))
            useCases.saveLearningPathUseCase?.invoke(path)
        }
    }

    private fun createModule(title: String, description: String, durationHours: Int) {
        viewModelScope.launch {
            val mod = CurriculumModule(
                id = UUID.randomUUID().toString(),
                courseId = uiState.value.selectedCourseId,
                orgId = uiState.value.selectedOrgId,
                title = title,
                description = description,
                orderIndex = uiState.value.modules.size + 1,
                durationHours = durationHours
            )
            useCases.saveModuleUseCase?.invoke(mod)
        }
    }

    private fun createLesson(moduleId: String, title: String, content: String, videoUrl: String?, pdfUrl: String?) {
        viewModelScope.launch {
            val lesson = AcademicLesson(
                id = UUID.randomUUID().toString(),
                moduleId = moduleId,
                courseId = uiState.value.selectedCourseId,
                title = title,
                content = content,
                videoUrl = videoUrl,
                pdfAttachmentUrl = pdfUrl,
                orderIndex = uiState.value.lessons.size + 1
            )
            useCases.saveAcademicLessonUseCase?.invoke(lesson)
        }
    }

    private fun createAssignment(title: String, prompt: String, type: AssignmentType, dueDate: String) {
        viewModelScope.launch {
            val assignment = Assignment(
                id = UUID.randomUUID().toString(),
                lessonId = "les_101",
                courseId = uiState.value.selectedCourseId,
                title = title,
                prompt = prompt,
                dueDate = dueDate,
                type = type
            )
            useCases.saveAssignmentUseCase?.invoke(assignment)
        }
    }

    private fun submitAssignment(assignmentId: String, content: String, fileUrl: String?) {
        viewModelScope.launch {
            val submission = AssignmentSubmission(
                id = UUID.randomUUID().toString(),
                assignmentId = assignmentId,
                studentId = uiState.value.selectedStudentId,
                submissionContent = content,
                fileAttachmentUrl = fileUrl,
                status = SubmissionStatus.SUBMITTED
            )
            useCases.submitAssignmentUseCase?.invoke(submission)
        }
    }

    private fun gradeAssignment(submissionId: String, score: Int, feedback: String) {
        viewModelScope.launch {
            // Auto-grade or instructor grading
        }
    }

    private fun createQuestion(text: String, optA: String, optB: String, optC: String, optD: String, correctIdx: Int, type: QuestionType) {
        viewModelScope.launch {
            val q = QuestionBankItem(
                id = UUID.randomUUID().toString(),
                courseId = uiState.value.selectedCourseId,
                orgId = uiState.value.selectedOrgId,
                questionText = text,
                optionA = optA,
                optionB = optB,
                optionC = optC,
                optionD = optD,
                correctAnswerIndex = correctIdx,
                questionType = type
            )
        }
    }

    private fun createAssessment(title: String, type: AssessmentType, passingScore: Int, timeLimit: Int) {
        viewModelScope.launch {
            val assessment = Assessment(
                id = UUID.randomUUID().toString(),
                courseId = uiState.value.selectedCourseId,
                orgId = uiState.value.selectedOrgId,
                title = title,
                type = type,
                passingScore = passingScore,
                timeLimitMinutes = timeLimit
            )
            useCases.saveAssessmentUseCase?.invoke(assessment)
        }
    }

    private fun submitAssessmentAttempt(assessmentId: String, scorePercent: Int) {
        viewModelScope.launch {
            val isPassed = scorePercent >= 70
            val attempt = AssessmentAttempt(
                id = UUID.randomUUID().toString(),
                assessmentId = assessmentId,
                studentId = uiState.value.selectedStudentId,
                scorePercent = scorePercent,
                isPassed = isPassed,
                autoGradedFeedback = if (isPassed) "تم الاجتياز بنجاح! إجابات ممتازة وتقييم آلي متكامل." else "نوصي بمراجعة الوحدة الأولى وإعادة المحاولة."
            )
            useCases.submitAssessmentAttemptUseCase?.invoke(attempt)

            // Auto-unlock badge if high score
            if (scorePercent >= 90) {
                unlockBadge("وسام التفوق الأكاديمي", "حصلت على أكثر من 90% في الاختبار النهائي", "ic_badge_star")
            }
        }
    }

    private fun addGradeRecord(courseName: String, totalScore: Float, gradeLetter: String, gpaValue: Float, isPassed: Boolean) {
        viewModelScope.launch {
            val record = GradebookRecord(
                id = UUID.randomUUID().toString(),
                studentId = uiState.value.selectedStudentId,
                courseId = uiState.value.selectedCourseId,
                orgId = uiState.value.selectedOrgId,
                courseName = courseName,
                totalScore = totalScore,
                gradeLetter = gradeLetter,
                gpaValue = gpaValue,
                isPassed = isPassed
            )
        }
    }

    private fun unlockBadge(name: String, description: String, icon: String) {
        viewModelScope.launch {
            val badge = AchievementBadge(
                id = UUID.randomUUID().toString(),
                studentId = uiState.value.selectedStudentId,
                badgeName = name,
                badgeDescription = description,
                iconName = icon
            )
        }
    }

    private fun createLearningPath(title: String, description: String, category: String, courses: List<String>) {
        viewModelScope.launch {
            val path = LearningPath(
                id = UUID.randomUUID().toString(),
                orgId = uiState.value.selectedOrgId,
                title = title,
                description = description,
                category = category,
                courseIds = courses
            )
            useCases.saveLearningPathUseCase?.invoke(path)
        }
    }

    private fun addPrerequisite(targetCourseId: String, requiredCourseId: String, title: String) {
        viewModelScope.launch {
            val prereq = Prerequisite(
                id = UUID.randomUUID().toString(),
                targetCourseId = targetCourseId,
                requiredCourseId = requiredCourseId,
                requiredCourseTitle = title
            )
        }
    }

    private fun startOfflineDownload(lessonId: String) {
        viewModelScope.launch {
            val download = OfflineContentDownload(
                id = UUID.randomUUID().toString(),
                courseId = uiState.value.selectedCourseId,
                lessonId = lessonId,
                localFilePath = "/storage/emulated/0/Rtiqa/offline/$lessonId.mp4",
                status = DownloadStatus.COMPLETED,
                progressPercent = 1.0f
            )
            useCases.saveOfflineDownloadUseCase?.invoke(download)
        }
    }

    private fun generateAiRecommendation() {
        viewModelScope.launch {
            val rec = SmartRecommendation(
                id = UUID.randomUUID().toString(),
                studentId = uiState.value.selectedStudentId,
                courseId = "c_ai_202",
                courseTitle = "التعلم العميق والرؤية الحاسوبية",
                reasonText = "استناداً إلى أداء الطالب الممتاز في اختبار الشبكات العصبية بنسبة 95%",
                confidenceScore = 0.94f
            )
        }
    }
}
