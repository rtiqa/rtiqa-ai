package com.rtiqa.core.data.repository

import com.rtiqa.core.database.dao.AcademicDao
import com.rtiqa.core.database.dao.CourseDao
import com.rtiqa.core.database.dao.SyncDao
import com.rtiqa.core.database.entity.AcademicLessonEntity
import com.rtiqa.core.database.entity.AchievementBadgeEntity
import com.rtiqa.core.database.entity.AssessmentAttemptEntity
import com.rtiqa.core.database.entity.AssessmentEntity
import com.rtiqa.core.database.entity.AssignmentEntity
import com.rtiqa.core.database.entity.AssignmentSubmissionEntity
import com.rtiqa.core.database.entity.CourseEntity
import com.rtiqa.core.database.entity.CurriculumModuleEntity
import com.rtiqa.core.database.entity.GradebookRecordEntity
import com.rtiqa.core.database.entity.LearningPathEntity
import com.rtiqa.core.database.entity.OfflineContentDownloadEntity
import com.rtiqa.core.database.entity.PrerequisiteEntity
import com.rtiqa.core.database.entity.QuestionBankEntity
import com.rtiqa.core.database.entity.SmartRecommendationEntity
import com.rtiqa.core.database.entity.StudentProgressEntity
import com.rtiqa.core.database.entity.SyncQueueEntity
import com.rtiqa.core.data.sync.OfflineSyncManager
import com.rtiqa.core.domain.model.Question
import com.rtiqa.core.domain.model.QuestionType
import com.rtiqa.core.domain.model.Quiz
import com.rtiqa.core.domain.result.RtiqaResult
import com.rtiqa.core.network.api.AuthResponseDto
import com.rtiqa.core.network.api.ClassGradebookDto
import com.rtiqa.core.network.api.LessonCompletionResponseDto
import com.rtiqa.core.network.api.LoginRequestDto
import com.rtiqa.core.network.api.NetworkCourseDto
import com.rtiqa.core.network.api.NetworkLessonDto
import com.rtiqa.core.network.api.NetworkSyncPayloadDto
import com.rtiqa.core.network.api.NetworkSyncResponseDto
import com.rtiqa.core.network.api.NetworkUserDto
import com.rtiqa.core.network.api.RegisterRequestDto
import com.rtiqa.core.network.api.RtiqaApiService
import com.rtiqa.core.network.session.RestSessionStore
import com.rtiqa.core.security.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class FakeAcademicDao : AcademicDao {
    val assessments = mutableListOf<AssessmentEntity>()
    val questions = mutableListOf<QuestionBankEntity>()
    val attempts = mutableListOf<AssessmentAttemptEntity>()

    override fun getModulesForCourse(courseId: String): Flow<List<CurriculumModuleEntity>> = flowOf(emptyList())
    override suspend fun insertModule(module: CurriculumModuleEntity) {}
    override fun getLessonsForModule(moduleId: String): Flow<List<AcademicLessonEntity>> = flowOf(emptyList())
    override suspend fun insertLesson(lesson: AcademicLessonEntity) {}

    override fun getAssignmentsForCourse(courseId: String): Flow<List<AssignmentEntity>> = flowOf(emptyList())
    override suspend fun insertAssignment(assignment: AssignmentEntity) {}
    override fun getSubmissions(assignmentId: String, studentId: String): Flow<List<AssignmentSubmissionEntity>> = flowOf(emptyList())
    override suspend fun insertSubmission(submission: AssignmentSubmissionEntity) {}
    override suspend fun updateSubmissionGrade(submissionId: String, score: Int, feedback: String) {}

    override fun getQuestionsForCourse(courseId: String): Flow<List<QuestionBankEntity>> = flowOf(questions.filter { it.courseId == courseId })
    override suspend fun insertQuestion(question: QuestionBankEntity) { questions.add(question) }
    override suspend fun insertQuestions(questions: List<QuestionBankEntity>) { this.questions.addAll(questions) }

    override fun getAssessmentsForCourse(courseId: String): Flow<List<AssessmentEntity>> = flowOf(assessments.filter { it.courseId == courseId })
    override fun getAssessmentsForSchool(schoolId: String): Flow<List<AssessmentEntity>> = flowOf(assessments.filter { it.schoolId == schoolId })
    override fun getAssessmentById(id: String): Flow<AssessmentEntity?> = flowOf(assessments.find { it.id == id })
    override suspend fun insertAssessment(assessment: AssessmentEntity) { assessments.add(assessment) }

    override fun getAttempts(assessmentId: String, studentId: String): Flow<List<AssessmentAttemptEntity>> =
        flowOf(attempts.filter { it.assessmentId == assessmentId && it.studentId == studentId })
    override suspend fun insertAssessmentAttempt(attempt: AssessmentAttemptEntity) { attempts.add(attempt) }

    override fun getGradebookForStudent(studentId: String, orgId: String): Flow<List<GradebookRecordEntity>> = flowOf(emptyList())
    override suspend fun insertGradebookRecord(record: GradebookRecordEntity) {}

    override fun getStudentProgress(studentId: String, courseId: String): Flow<StudentProgressEntity?> = flowOf(null)
    override suspend fun insertStudentProgress(progress: StudentProgressEntity) {}

    override fun getBadgesForStudent(studentId: String): Flow<List<AchievementBadgeEntity>> = flowOf(emptyList())
    override suspend fun insertBadge(badge: AchievementBadgeEntity) {}

    override fun getLearningPaths(orgId: String): Flow<List<LearningPathEntity>> = flowOf(emptyList())
    override suspend fun insertLearningPath(path: LearningPathEntity) {}

    override fun getPrerequisites(targetCourseId: String): Flow<List<PrerequisiteEntity>> = flowOf(emptyList())
    override suspend fun insertPrerequisite(prerequisite: PrerequisiteEntity) {}

    override fun getRecommendationsForStudent(studentId: String): Flow<List<SmartRecommendationEntity>> = flowOf(emptyList())
    override suspend fun insertRecommendation(recommendation: SmartRecommendationEntity) {}

    override fun getOfflineDownloads(courseId: String): Flow<List<OfflineContentDownloadEntity>> = flowOf(emptyList())
    override suspend fun insertOfflineDownload(download: OfflineContentDownloadEntity) {}
}

class FakeCourseDao : CourseDao {
    override fun getAllCourses(): Flow<List<CourseEntity>> = flowOf(emptyList())
    override fun getCoursesForSchool(schoolId: String): Flow<List<CourseEntity>> = flowOf(emptyList())
    override suspend fun getAllCoursesList(): List<CourseEntity> = emptyList()
    override fun getCourseById(id: String): Flow<CourseEntity?> = flowOf(null)
    override suspend fun insertCourse(course: CourseEntity) {}
    override suspend fun insertCourses(courses: List<CourseEntity>) {}
    override suspend fun deleteCourseById(id: String) {}
    override suspend fun updateEnrollmentStatus(id: String, isEnrolled: Boolean) {}
    override suspend fun updateBookmarkStatus(id: String, isBookmarked: Boolean) {}
    override suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean) {}
    override suspend fun updateCourseProgress(id: String, progressPercent: Float) {}
}

class FakeSyncDao : SyncDao {
    val items = mutableListOf<SyncQueueEntity>()
    override fun getAllPendingSyncItems(userId: String, sessionId: String): Flow<List<SyncQueueEntity>> = kotlinx.coroutines.flow.flowOf(items.filter { it.ownerUserId == userId && it.ownerSessionId == sessionId })
    override suspend fun getPendingSyncItemsList(userId: String, sessionId: String): List<SyncQueueEntity> = items.filter { it.ownerUserId == userId && it.ownerSessionId == sessionId }
    override suspend fun insertSyncItem(item: SyncQueueEntity) { items.add(item) }
    override suspend fun deleteSyncItem(id: String) { items.removeAll { it.id == id } }
    override suspend fun clearAll() { items.clear() }
}

class FakeRtiqaApiService : RtiqaApiService {
    override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> = throw NotImplementedError()
    override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> = throw NotImplementedError()
    override suspend fun getUserProfile(): Response<NetworkUserDto> = throw NotImplementedError()
    override suspend fun getCourses(category: String?): Response<List<NetworkCourseDto>> = Response.success(emptyList())
    override suspend fun getCourse(courseId: String): Response<NetworkCourseDto> = throw NotImplementedError()
    override suspend fun getCourseLessons(courseId: String): Response<List<NetworkLessonDto>> = Response.success(emptyList())
    override suspend fun getLesson(courseId: String, lessonId: String): Response<NetworkLessonDto> = throw NotImplementedError()
    override suspend fun completeLesson(courseId: String, lessonId: String): Response<LessonCompletionResponseDto> =
        Response.success(LessonCompletionResponseDto(true, lessonId, courseId, true, 100.0f, 1, 1))
    override suspend fun updateLessonProgress(courseId: String, lessonId: String, request: com.rtiqa.core.network.api.LessonProgressRequestDto): Response<com.rtiqa.core.network.api.LessonProgressResponseDto> =
        Response.success(LessonCompletionResponseDto(true, lessonId, courseId, true, 100.0f, 1, 1))
    override suspend fun syncOfflineData(payload: NetworkSyncPayloadDto): Response<NetworkSyncResponseDto> = throw NotImplementedError()
    override suspend fun getClassGradebook(classId: String): Response<ClassGradebookDto> =
        Response.success(ClassGradebookDto(classId, emptyList(), emptyList(), emptyList()))
}

class QuizRepositoryImplTest {

    private lateinit var fakeDao: FakeAcademicDao
    private lateinit var fakeSyncDao: FakeSyncDao
    private lateinit var offlineSyncManager: OfflineSyncManager
    private lateinit var repository: QuizRepositoryImpl

    @Before
    fun setUp() {
        fakeDao = FakeAcademicDao()
        fakeSyncDao = FakeSyncDao()
        val testSecurityManager = object : SecurityManager {
            override fun putEncryptedString(key: String, value: String) {}
            override fun getEncryptedString(key: String, defaultValue: String?): String? =
                if (key == "user_id") "user_123" else defaultValue
            override fun removeKey(key: String) {}
            override fun clearAll() {}
        }
        offlineSyncManager = OfflineSyncManager(
            apiService = FakeRtiqaApiService(),
            courseDao = FakeCourseDao(),
            syncDao = fakeSyncDao,
            syncMutex = kotlinx.coroutines.sync.Mutex(),
            sessionStore = object : RestSessionStore {
                var _sessionId: String? = "test_session_id"
                override fun saveSession(token: String, organizationId: String?) {}
                override fun getSessionToken() = null
                override fun getActiveOrganizationId() = null
                override fun updateActiveOrganizationId(organizationId: String?) {}
                override fun generateAndSaveSessionId() = "new_id".also { _sessionId = it }
                override fun getSessionId() = _sessionId
                override fun clearSession() { _sessionId = null }
            },
            securityManager = testSecurityManager
        )
        repository = QuizRepositoryImpl(
            academicDao = fakeDao,
            offlineSyncManager = offlineSyncManager,
            currentUserIdProvider = { "user_123" }
        )
    }

    @Test
    fun noAssessment_returnsNoQuiz() = runTest {
        val quizzes = repository.getQuizzesForCourse("c1").first()
        assertTrue(quizzes.isEmpty())
        assertEquals(null, repository.getQuizForCourse("c1").first())
    }

    @Test
    fun missingQuizById_returnsNull() = runTest {
        assertEquals(null, repository.getQuizById("missing").first())
    }

    @Test
    fun noQuestions_doesNotReturnDefaultQuestions() = runTest {
        fakeDao.assessments += AssessmentEntity("a1", "c1", "org", "Real", "QUIZ", 70, 5, 0)
        val quiz = repository.getQuizForCourse("c1").first()
        assertNotNull(quiz)
        assertTrue(quiz!!.questions.isEmpty())
    }

    @Test
    fun noAuthenticatedUser_doesNotSaveQuizAttempt() = runTest {
        val unauthenticated = QuizRepositoryImpl(
            academicDao = fakeDao,
            offlineSyncManager = offlineSyncManager,
            currentUserIdProvider = { null }
        )
        val result = unauthenticated.submitQuizResult("quiz_c1", 1, 1)
        assertTrue(result is RtiqaResult.Error)
        assertTrue(fakeDao.attempts.isEmpty())
        assertTrue(fakeSyncDao.items.isEmpty())
    }

    @Test
    fun saveQuiz_persistsAssessmentAndQuestionsInDao() = runTest {
        val newQuiz = Quiz(
            id = "quiz_save_1",
            courseId = "c2",
            title = "Test Saved Quiz",
            questions = listOf(
                Question("q1", "What is Room?", listOf("Database", "Network"), 0, type = QuestionType.MULTIPLE_CHOICE)
            ),
            passingScorePercent = 80,
            durationMinutes = 10,
            timeLimitSeconds = 600
        )

        val saveResult = repository.saveQuiz(newQuiz)
        assertTrue(saveResult is RtiqaResult.Success)

        val quizzes = repository.getQuizzesForCourse("c2").first()
        assertEquals(1, quizzes.size)
        assertEquals("Test Saved Quiz", quizzes[0].title)
    }

    @Test
    fun submitQuizResult_calculatesPercentagePassesAndEnqueuesOfflineAction() = runTest {
        fakeDao.assessments += AssessmentEntity("quiz_c1", "c1", "org", "Real", "QUIZ", 70, 5, 4)
        val result = repository.submitQuizResult("quiz_c1", 3, 4) // 75% -> passed
        assertTrue(result is RtiqaResult.Success)

        val quizResult = (result as RtiqaResult.Success).data
        assertEquals(75, quizResult.scorePercent)
        assertTrue(quizResult.isPassed)
        assertEquals("user_123", quizResult.studentId)

        val queuedItems = fakeSyncDao.items
        assertEquals(1, queuedItems.size)
        assertEquals("SUBMIT_QUIZ_RESULT", queuedItems[0].actionType)
        assertTrue(queuedItems[0].payloadJson.contains("\"isPassed\":true"))
        assertEquals("user_123", queuedItems[0].ownerUserId)
        assertEquals("test_session_id", queuedItems[0].ownerSessionId)
    }
}
