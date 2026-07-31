package com.rtiqa.core.domain.usecase

import com.rtiqa.core.domain.model.Question
import com.rtiqa.core.domain.model.QuestionType
import com.rtiqa.core.domain.model.Quiz
import com.rtiqa.core.domain.model.QuizResult
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.domain.repository.QuizRepositoryContract
import com.rtiqa.core.domain.repository.UserRepositoryContract
import com.rtiqa.core.domain.result.RtiqaResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeQuizRepository : QuizRepositoryContract {
    val quizzesMap = mutableMapOf<String, Quiz>()
    val resultsList = mutableListOf<QuizResult>()
    var submittedScore: Int = -1
    var submittedTotal: Int = -1

    override fun getQuizzesForCourse(courseId: String): Flow<List<Quiz>> {
        return flowOf(quizzesMap.values.filter { it.courseId == courseId })
    }

    override fun getQuizForCourse(courseId: String): Flow<Quiz?> {
        return flowOf(quizzesMap.values.firstOrNull { it.courseId == courseId })
    }

    override fun getQuizById(quizId: String): Flow<Quiz?> {
        return flowOf(quizzesMap[quizId])
    }

    override suspend fun submitQuizResult(quizId: String, score: Int, total: Int): RtiqaResult<QuizResult> {
        submittedScore = score
        submittedTotal = total
        val totalCount = if (total <= 0) 1 else total
        val percent = ((score.toFloat() / totalCount) * 100).toInt()
        val isPassed = percent >= 70

        val res = QuizResult(
            id = "res_1",
            quizId = quizId,
            courseId = "c1",
            studentId = "u1",
            score = score,
            totalQuestions = total,
            scorePercent = percent,
            isPassed = isPassed,
            completedAt = System.currentTimeMillis()
        )
        resultsList.add(res)
        return RtiqaResult.Success(res)
    }

    override fun getQuizResultsForUser(quizId: String, userId: String): Flow<List<QuizResult>> {
        return flowOf(resultsList.filter { it.quizId == quizId && it.studentId == userId })
    }

    override suspend fun saveQuiz(quiz: Quiz): RtiqaResult<Unit> {
        quizzesMap[quiz.id] = quiz
        return RtiqaResult.Success(Unit)
    }
}

class FakeUserRepoForQuiz : UserRepositoryContract {
    var addedXp: Int = 0
    override fun getUserProfile(): Flow<UserProfile?> = flowOf(null)
    override suspend fun updateUserProfile(profile: UserProfile): RtiqaResult<Unit> = RtiqaResult.Success(Unit)
    override suspend fun addXp(amount: Int): RtiqaResult<Unit> {
        addedXp += amount
        return RtiqaResult.Success(Unit)
    }
    override suspend fun incrementStreak(): RtiqaResult<Unit> = RtiqaResult.Success(Unit)
}

class QuizUseCasesTest {

    private lateinit var quizRepo: FakeQuizRepository
    private lateinit var userRepo: FakeUserRepoForQuiz
    private lateinit var getQuizzesForCourseUseCase: GetQuizzesForCourseUseCase
    private lateinit var getQuizDetailUseCase: GetQuizDetailUseCase
    private lateinit var evaluateQuizAnswersUseCase: EvaluateQuizAnswersUseCase
    private lateinit var submitQuizResultUseCase: SubmitQuizResultUseCase

    @Before
    fun setUp() {
        quizRepo = FakeQuizRepository()
        userRepo = FakeUserRepoForQuiz()
        getQuizzesForCourseUseCase = GetQuizzesForCourseUseCase(quizRepo)
        getQuizDetailUseCase = GetQuizDetailUseCase(quizRepo)
        evaluateQuizAnswersUseCase = EvaluateQuizAnswersUseCase()
        submitQuizResultUseCase = SubmitQuizResultUseCase(quizRepo, userRepo)

        val sampleQuestions = listOf(
            Question("q1", "Q1 text", listOf("A", "B"), 0, type = QuestionType.MULTIPLE_CHOICE, xpReward = 20),
            Question("q2", "Q2 text", listOf("True", "False"), 1, type = QuestionType.TRUE_FALSE, xpReward = 20)
        )

        quizRepo.quizzesMap["q1"] = Quiz(
            id = "q1",
            courseId = "c1",
            title = "Sample Quiz",
            questions = sampleQuestions,
            passingScorePercent = 70,
            durationMinutes = 5,
            timeLimitSeconds = 300
        )
    }

    @Test
    fun getQuizzesForCourse_returnsListForCourse() = runBlocking {
        val list = getQuizzesForCourseUseCase("c1").first()
        assertEquals(1, list.size)
        assertEquals("Sample Quiz", list[0].title)
    }

    @Test
    fun getQuizDetail_returnsQuizById() = runBlocking {
        val quiz = getQuizDetailUseCase("q1").first()
        assertNotNull(quiz)
        assertEquals("q1", quiz?.id)
        assertEquals(2, quiz?.questions?.size)
    }

    @Test
    fun evaluateQuizAnswers_calculatesCorrectScoreAndPercent() {
        val quiz = quizRepo.quizzesMap["q1"]!!
        val answers = mapOf("q1" to 0, "q2" to 1) // all correct

        val eval = evaluateQuizAnswersUseCase(quiz, answers)
        assertEquals(2, eval.score)
        assertEquals(2, eval.totalQuestions)
        assertEquals(100, eval.scorePercent)
        assertTrue(eval.isPassed)
        assertEquals(50, eval.xpEarned)
    }

    @Test
    fun evaluateQuizAnswers_handlesPartialOrWrongAnswers() {
        val quiz = quizRepo.quizzesMap["q1"]!!
        val answers = mapOf("q1" to 0, "q2" to 0) // q1 right, q2 wrong

        val eval = evaluateQuizAnswersUseCase(quiz, answers)
        assertEquals(1, eval.score)
        assertEquals(2, eval.totalQuestions)
        assertEquals(50, eval.scorePercent)
        assertFalse(eval.isPassed) // < 70%
    }

    @Test
    fun submitQuizResult_rewardsXpWhenPassed() = runBlocking {
        val result = submitQuizResultUseCase("q1", 2, 2)
        assertTrue(result is RtiqaResult.Success)
        val data = (result as RtiqaResult.Success).data
        assertEquals(100, data.scorePercent)
        assertTrue(data.isPassed)
        assertEquals(50, userRepo.addedXp)
    }
}
