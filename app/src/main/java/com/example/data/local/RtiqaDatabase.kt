package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.CourseDao
import com.example.data.local.dao.LessonDao
import com.example.data.local.dao.SyncQueueDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.CourseEntity
import com.example.data.local.entity.LessonEntity
import com.example.data.local.entity.SyncQueueEntity
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CourseEntity::class,
        LessonEntity::class,
        UserProfileEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RtiqaDatabase : RoomDatabase() {

    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun syncQueueDao(): SyncQueueDao

    companion object {
        @Volatile
        private var INSTANCE: RtiqaDatabase? = null

        fun getDatabase(context: Context): RtiqaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RtiqaDatabase::class.java,
                    "rtiqa_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: RtiqaDatabase) {
                // Populate default User Profile
                database.userProfileDao().saveUserProfile(
                    UserProfileEntity(
                        id = "user_001",
                        name = "Tariq Al-Mansoor",
                        email = "learner@rtiqa.edu",
                        avatarResName = "img_ai_tutor_avatar_1785095337393",
                        xp = 2450,
                        coins = 380,
                        level = 5,
                        streakDays = 12,
                        currentGoal = "Master AI Neural Networks in 30 Days",
                        language = "en",
                        isOfflineAutoSyncEnabled = true,
                        isDarkMode = true
                    )
                )

                // Populate Initial Courses
                val courses = listOf(
                    CourseEntity(
                        id = "c_ai_101",
                        title = "Artificial Intelligence & Neural Networks",
                        titleAr = "الذكاء الاصطناعي والشبكات العصبيّة",
                        category = "AI & Data Science",
                        categoryAr = "الذكاء الاصطناعي والبيانات",
                        description = "Master deep learning architectures, transformers, backpropagation, and real-world AI modeling with hands-on Kotlin and Python implementations.",
                        descriptionAr = "تعلم بنى التعلم العميق، ونماذج المحولات، والانتشار العكسي، وبناء نماذج الذكاء الاصطناعي العملية.",
                        rating = 4.9f,
                        durationMinutes = 240,
                        totalLessons = 6,
                        enrolledCount = 14200,
                        imageResName = "img_course_ai_1785095326156",
                        level = "Intermediate",
                        tagsCsv = "AI,Deep Learning,Neural Networks,Transformers",
                        progressPercent = 0.35f,
                        isBookmarked = true,
                        isDownloaded = true
                    ),
                    CourseEntity(
                        id = "c_cs_201",
                        title = "Modern Kotlin & Clean Android Architecture",
                        titleAr = "كوتلن الحديثة وهندسة أندرويد النظيفة",
                        category = "Computer Science",
                        categoryAr = "علوم الحاسوب",
                        description = "Architect scalable enterprise Android applications with Jetpack Compose, Clean Architecture, Coroutines, Flow, and Room local persistence.",
                        descriptionAr = "صمّم تطبيقات أندرويد مؤسسية قابلة للتوسع باستخدام Jetpack Compose والطبقات النظيفة وتدفقات Coroutines.",
                        rating = 4.95f,
                        durationMinutes = 310,
                        totalLessons = 8,
                        enrolledCount = 18900,
                        imageResName = "img_hero_banner_1785095314710",
                        level = "Advanced",
                        tagsCsv = "Kotlin,Android,Compose,Clean Architecture",
                        progressPercent = 0.60f,
                        isBookmarked = true,
                        isDownloaded = false
                    ),
                    CourseEntity(
                        id = "c_math_301",
                        title = "Linear Algebra for Quantum & Machine Learning",
                        titleAr = "الجبر الخطي للحوسبة الكمومية وتعلم الآلة",
                        category = "Mathematics",
                        categoryAr = "الرياضيات",
                        description = "Comprehensive breakdown of matrix decomposition, eigenvectors, vector spaces, and probability theory applied to modern computing.",
                        descriptionAr = "شرح شامل لتفكيك المصفوفات، والمتجهات الذاتية، وفضاءات المتجهات المطبقة في الحوسبة الحديثة.",
                        rating = 4.85f,
                        durationMinutes = 180,
                        totalLessons = 5,
                        enrolledCount = 8500,
                        imageResName = "img_course_ai_1785095326156",
                        level = "Intermediate",
                        tagsCsv = "Math,Linear Algebra,Vectors,Quantum",
                        progressPercent = 0.10f,
                        isBookmarked = false,
                        isDownloaded = false
                    ),
                    CourseEntity(
                        id = "c_lang_101",
                        title = "Scientific Arabic & Educational Linguistics",
                        titleAr = "اللغة العربية العلمية واللسانيات التعليمية",
                        category = "Languages",
                        categoryAr = "اللغات والترجمة",
                        description = "Explore scientific terminology creation, technical Arabic translation, and natural language processing techniques for Arabic AI.",
                        descriptionAr = "استكشف صياغة المصطلحات العلمية، والترجمة التقنية، وتطبيقات معالجة اللغة العربية بالنظم الذكية.",
                        rating = 4.9f,
                        durationMinutes = 150,
                        totalLessons = 4,
                        enrolledCount = 6100,
                        imageResName = "img_hero_banner_1785095314710",
                        level = "Beginner",
                        tagsCsv = "Arabic,Linguistics,NLP,Translation",
                        progressPercent = 0.0f,
                        isBookmarked = false,
                        isDownloaded = false
                    )
                )
                database.courseDao().insertCourses(courses)

                // Populate Initial Lessons
                val lessons = listOf(
                    LessonEntity(
                        id = "l_ai_1",
                        courseId = "c_ai_101",
                        title = "1. Introduction to Perceptrons & Neural Nodes",
                        titleAr = "١. مقدمة إلى الإدراك العالي والخلية العصبية الاصطناعية",
                        durationMinutes = 25,
                        orderIndex = 1,
                        videoUrl = "https://example.com/stream/lesson1.mp4",
                        audioUrl = "https://example.com/audio/lesson1.mp3",
                        contentMarkdown = "An artificial neuron (perceptron) receives multiple input signals, applies synaptic weights, sums them with a bias, and passes the result through an activation function like ReLU or Sigmoid.\n\n```kotlin\nfun perceptron(inputs: DoubleArray, weights: DoubleArray, bias: Double): Double {\n    var sum = bias\n    for (i in inputs.indices) {\n        sum += inputs[i] * weights[i]\n    }\n    return if (sum > 0) 1.0 else 0.0\n}\n```\n\n### Core Key Takeaways:\n- Weights control signal amplification.\n- Bias shifts the activation threshold.\n- Non-linear activation functions allow learning non-linear surfaces.",
                        contentMarkdownAr = "تستقبل الخلية العصبية الاصطناعية عدة إشارات مدخلة، وتطبق عليها أوزاناً تشابكية، ثم تجمعها مع المنحاز وتمرر النتيجة عبر دالة تنشيط.",
                        isCompleted = true,
                        isDownloaded = true
                    ),
                    LessonEntity(
                        id = "l_ai_2",
                        courseId = "c_ai_101",
                        title = "2. Backpropagation & Gradient Descent Algorithm",
                        titleAr = "٢. الانتشار العكسي وخوارزمية الانحدار التدريجي",
                        durationMinutes = 35,
                        orderIndex = 2,
                        videoUrl = "https://example.com/stream/lesson2.mp4",
                        audioUrl = "https://example.com/audio/lesson2.mp3",
                        contentMarkdown = "Backpropagation calculates the partial derivative of the loss function with respect to each weight using the mathematical chain rule.\n\n### Chain Rule Equation:\n`∂L/∂w = (∂L/∂y) * (∂y/∂z) * (∂z/∂w)`\n\nOptimizers like **Adam** and **SGD with Momentum** accelerate convergence towards global loss minimums.",
                        contentMarkdownAr = "تحسب خوارزمية الانتشار العكسي المشتقة الجزئية لدالة الخسارة بالنسبة لكل وزن باستخدام قاعدة السلسلة الرياضية.",
                        isCompleted = true,
                        isDownloaded = true
                    ),
                    LessonEntity(
                        id = "l_ai_3",
                        courseId = "c_ai_101",
                        title = "3. Transformer Self-Attention Mechanisms",
                        titleAr = "٣. آليات الانتباه الذاتي في نماذج المحولات",
                        durationMinutes = 40,
                        orderIndex = 3,
                        videoUrl = "https://example.com/stream/lesson3.mp4",
                        audioUrl = "https://example.com/audio/lesson3.mp3",
                        contentMarkdown = "Self-attention computes Query (Q), Key (K), and Value (V) matrix representations to dynamic model token-to-token contextual relationships.\n\n`Attention(Q, K, V) = softmax( (Q * K^T) / sqrt(d_k) ) * V`",
                        contentMarkdownAr = "تحسب آلية الانتباه الذاتي مصفوفات الاستعلام والمفتاح والقيمة لتحديد العلاقات السياق بين الكلمات بمرونة عالية.",
                        isCompleted = false,
                        isDownloaded = true
                    ),
                    LessonEntity(
                        id = "l_cs_1",
                        courseId = "c_cs_201",
                        title = "1. Clean Architecture & Unidirectional Data Flow",
                        titleAr = "١. الهندسة النظيفة وتدفق البيانات أحادي الاتجاه",
                        durationMinutes = 30,
                        orderIndex = 1,
                        videoUrl = "https://example.com/stream/clean_arch.mp4",
                        audioUrl = "https://example.com/audio/clean_arch.mp3",
                        contentMarkdown = "Decouple domain logic from UI and data frameworks. Use standard StateFlow for immutable state emission to Compose screens.",
                        contentMarkdownAr = "افصل المنطق الأساسي عن واجهة المستخدم وإطارات البيانات. استخدم StateFlow لإرسال الحالات غير القابلة للتغيير.",
                        isCompleted = true,
                        isDownloaded = false
                    )
                )
                database.lessonDao().insertLessons(lessons)
            }
        }
    }
}
