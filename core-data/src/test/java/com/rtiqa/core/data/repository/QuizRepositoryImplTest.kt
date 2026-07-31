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
import com.rtiqa.core.network.api.LoginRequestDto
import com.rtiqa.core.network.api.NetworkCourseDto
import com.rtiqa.core.network.api.NetworkLessonDto
import com.rtiqa.core.network.api.NetworkSyncPayloadDto
import com.rtiqa.core.network.api.NetworkSyncResponseDto
import com.rtiqa.core.network.api.NetworkUserDto
import com.rtiqa.core.network.api.RegisterRequestDto
import com.rtiqa.core.network.api.RtiqaApiService
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
    override fun getAllPendingSyncItems(): Flow<List<SyncQueueEntity>> = flowOf(items)
    override suspend fun getPendingSyncItemsList(): List<SyncQueueEntity> = items
    override suspend fun insertSyncItem(item: SyncQueueEntity) { items.add(item) }
    override suspend fun deleteSyncItem(id: String) { items.removeAll { it.id == id } }
    override suspend fun clearAll() { items.clear() }
}

class FakeRtiqaApiService : RtiqaApiService {
    override suspend fun login(request: LoginRequestDto): Response<AuthResponseDto> = throw NotImplementedError()
    override suspend fun register(request: RegisterRequestDto): Response<AuthResponseDto> = throw NotImplementedError()
    override suspend fun getUserProfile(): Response<NetworkUserDto> = throw NotImplementedError()
    override suspend fun getCourses(category: String?): Response<List<NetworkCourseDto>> = Response.success(emptyList())
    override suspend fun getCourseLessons(courseId: String): Response<List<NetworkLessonDto>> = Response.success(emptyList())
    override suspend fun syncOfflineData(payload: NetworkSyncPayloadDto): Response<NetworkSyncResponseDto> = throw NotImplementedError()
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
        offlineSyncManager = OfflineSyncManager(
            apiService = FakeRtiqaApiService(),
            courseDao = FakeCourseDao(),
            syncDao = fakeSyncDao
        )
        repository = QuizRepositoryImpl(
            academicDao = fakeDao,
            offlineSyncManager = offlineSyncManager,
            currentUserIdProvider = { "user_123" }
        )
    }

    @Test
    fun getQuizzesForCourse_returnsDefaultWhenDbEmpty() = runTest {
        val quizzes = repository.getQuizzesForCourse("c1").first()
        assertEquals(1, quizzes.size)
        assertEquals("quiz_c1", quizzes[0].id)
        assertEquals(70, quizzes[0].passingScorePercent)
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
    }
}
