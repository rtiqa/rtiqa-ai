package com.rtiqa.mobile.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rtiqa.mobile.data.local.dao.CourseDao
import com.rtiqa.mobile.data.local.dao.LessonDao
import com.rtiqa.mobile.data.local.dao.SyncQueueDao
import com.rtiqa.mobile.data.local.dao.UserProfileDao
import com.rtiqa.mobile.data.local.entity.CourseEntity
import com.rtiqa.mobile.data.local.entity.LessonEntity
import com.rtiqa.mobile.data.local.entity.SyncQueueEntity
import com.rtiqa.mobile.data.local.entity.UserProfileEntity
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
                .fallbackToDestructiveMigration(dropAllTables = true)
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
                // Initial courses population below

                val courses = listOf(
                    CourseEntity(
                        id = "c_ai_101",
                        title = "الذكاء الاصطناعي والشبكات العصبيّة",
                        titleAr = "الذكاء الاصطناعي والشبكات العصبيّة",
                        category = "الذكاء الاصطناعي والبيانات",
                        categoryAr = "الذكاء الاصطناعي والبيانات",
                        description = "تعلم بنى التعلم العميق، ونماذج المحولات، والانتشار العكسي، وبناء نماذج الذكاء الاصطناعي العملية.",
                        descriptionAr = "تعلم بنى التعلم العميق، ونماذج المحولات، والانتشار العكسي، وبناء نماذج الذكاء الاصطناعي العملية.",
                        rating = 4.9f,
                        durationMinutes = 240,
                        totalLessons = 6,
                        enrolledCount = 14200,
                        imageResName = "img_course_ai_1785095326156",
                        level = "متوسط",
                        tagsCsv = "ذكاء اصطناعي,تعلم عميق,شبكات عصبية,محولات",
                        progressPercent = 0.35f,
                        isBookmarked = true,
                        isDownloaded = true
                    ),
                    CourseEntity(
                        id = "c_cs_201",
                        title = "كوتلن الحديثة وهندسة أندرويد النظيفة",
                        titleAr = "كوتلن الحديثة وهندسة أندرويد النظيفة",
                        category = "علوم الحاسوب",
                        categoryAr = "علوم الحاسوب",
                        description = "صمّم تطبيقات أندرويد مؤسسية قابلة للتوسع باستخدام Jetpack Compose والطبقات النظيفة وتدفقات Coroutines.",
                        descriptionAr = "صمّم تطبيقات أندرويد مؤسسية قابلة للتوسع باستخدام Jetpack Compose والطبقات النظيفة وتدفقات Coroutines.",
                        rating = 4.95f,
                        durationMinutes = 310,
                        totalLessons = 8,
                        enrolledCount = 18900,
                        imageResName = "img_hero_banner_1785095314710",
                        level = "متقدم",
                        tagsCsv = "كوتلن,أندرويد,كومبوز,هندسة نظيفة",
                        progressPercent = 0.60f,
                        isBookmarked = true,
                        isDownloaded = false
                    ),
                    CourseEntity(
                        id = "c_math_301",
                        title = "الجبر الخطي للحوسبة الكمومية وتعلم الآلة",
                        titleAr = "الجبر الخطي للحوسبة الكمومية وتعلم الآلة",
                        category = "الرياضيات",
                        categoryAr = "الرياضيات",
                        description = "شرح شامل لتفكيك المصفوفات، والمتجهات الذاتية، وفضاءات المتجهات المطبقة في الحوسبة الحديثة.",
                        descriptionAr = "شرح شامل لتفكيك المصفوفات، والمتجهات الذاتية، وفضاءات المتجهات المطبقة في الحوسبة الحديثة.",
                        rating = 4.85f,
                        durationMinutes = 180,
                        totalLessons = 5,
                        enrolledCount = 8500,
                        imageResName = "img_course_ai_1785095326156",
                        level = "متوسط",
                        tagsCsv = "رياضيات,جبر خطي,متجهات,كمومي",
                        progressPercent = 0.10f,
                        isBookmarked = false,
                        isDownloaded = false
                    ),
                    CourseEntity(
                        id = "c_lang_101",
                        title = "اللغة العربية العلمية واللسانيات التعليمية",
                        titleAr = "اللغة العربية العلمية واللسانيات التعليمية",
                        category = "اللغات والترجمة",
                        categoryAr = "اللغات والترجمة",
                        description = "استكشف صياغة المصطلحات العلمية، والترجمة التقنية، وتطبيقات معالجة اللغة العربية بالنظم الذكية.",
                        descriptionAr = "استكشف صياغة المصطلحات العلمية، والترجمة التقنية، وتطبيقات معالجة اللغة العربية بالنظم الذكية.",
                        rating = 4.9f,
                        durationMinutes = 150,
                        totalLessons = 4,
                        enrolledCount = 6100,
                        imageResName = "img_hero_banner_1785095314710",
                        level = "مبتدئ",
                        tagsCsv = "عربية,لسانيات,معالجة اللغة,ترجمة",
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
                        title = "١. مقدمة إلى الإدراك العالي والخلية العصبية الاصطناعية",
                        titleAr = "١. مقدمة إلى الإدراك العالي والخلية العصبية الاصطناعية",
                        durationMinutes = 25,
                        orderIndex = 1,
                        videoUrl = "https://example.com/stream/lesson1.mp4",
                        audioUrl = "https://example.com/audio/lesson1.mp3",
                        contentMarkdown = "تستقبل الخلية العصبية الاصطناعية عدة إشارات مدخلة، وتطبق عليها أوزاناً تشابكية، ثم تجمعها مع المنحاز وتمرر النتيجة عبر دالة تنشيط مثل ReLU أو Sigmoid.\n\n### النقاط الرئيسية:\n- الأوزان تتحكم في تضخيم الإشارة.\n- المنحاز يغير عتبة التنشيط.\n- دوال التنشيط غير الخطي تتيح تعلم النماذج المعقدة.",
                        contentMarkdownAr = "تستقبل الخلية العصبية الاصطناعية عدة إشارات مدخلة، وتطبق عليها أوزاناً تشابكية، ثم تجمعها مع المنحاز وتمرر النتيجة عبر دالة تنشيط.",
                        isCompleted = true,
                        isDownloaded = true,
                        hasQuiz = true,
                        isQuizPassed = true
                    ),
                    LessonEntity(
                        id = "l_ai_2",
                        courseId = "c_ai_101",
                        title = "٢. الانتشار العكسي وخوارزمية الانحدار التدريجي",
                        titleAr = "٢. الانتشار العكسي وخوارزمية الانحدار التدريجي",
                        durationMinutes = 35,
                        orderIndex = 2,
                        videoUrl = "https://example.com/stream/lesson2.mp4",
                        audioUrl = "https://example.com/audio/lesson2.mp3",
                        contentMarkdown = "تحسب خوارزمية الانتشار العكسي المشتقة الجزئية لدالة الخسارة بالنسبة لكل وزن باستخدام قاعدة السلسلة الرياضية.\n\nتساعد خوارزميات التحسين مثل Adam على تسريع التقارب نحو القيم الدنيا للخسارة.",
                        contentMarkdownAr = "تحسب خوارزمية الانتشار العكسي المشتقة الجزئية لدالة الخسارة بالنسبة لكل وزن باستخدام قاعدة السلسلة الرياضية.",
                        isCompleted = true,
                        isDownloaded = true
                    ),
                    LessonEntity(
                        id = "l_ai_3",
                        courseId = "c_ai_101",
                        title = "٣. آليات الانتباه الذاتي في نماذج المحولات",
                        titleAr = "٣. آليات الانتباه الذاتي في نماذج المحولات",
                        durationMinutes = 40,
                        orderIndex = 3,
                        videoUrl = "https://example.com/stream/lesson3.mp4",
                        audioUrl = "https://example.com/audio/lesson3.mp3",
                        contentMarkdown = "تحسب آلية الانتباه الذاتي مصفوفات الاستعلام والمفتاح والقيمة لتحديد العلاقات السياق بين الكلمات بمرونة عالية في نماذج اللغات الضخمة.",
                        contentMarkdownAr = "تحسب آلية الانتباه الذاتي مصفوفات الاستعلام والمفتاح والقيمة لتحديد العلاقات السياق بين الكلمات بمرونة عالية.",
                        isCompleted = false,
                        isDownloaded = true
                    ),
                    LessonEntity(
                        id = "l_cs_1",
                        courseId = "c_cs_201",
                        title = "١. الهندسة النظيفة وتدفق البيانات أحادي الاتجاه",
                        titleAr = "١. الهندسة النظيفة وتدفق البيانات أحادي الاتجاه",
                        durationMinutes = 30,
                        orderIndex = 1,
                        videoUrl = "https://example.com/stream/clean_arch.mp4",
                        audioUrl = "https://example.com/audio/clean_arch.mp3",
                        contentMarkdown = "افصل المنطق الأساسي عن واجهة المستخدم وإطارات البيانات. استخدم StateFlow لإرسال الحالات غير القابلة للتغيير إلى شاشات Jetpack Compose.",
                        contentMarkdownAr = "افصل المنطق الأساسي عن واجهة المستخدم وإطارات البيانات. استخدم StateFlow لإرسال الحالات غير القابلة للتغيير.",
                        isCompleted = true,
                        isDownloaded = false
                    ),
                    LessonEntity(
                        id = "l_cs_2",
                        courseId = "c_cs_201",
                        title = "٢. البرمجة التزامنية المتوازية وتدفقات البيانات",
                        titleAr = "٢. البرمجة التزامنية المتوازية وتدفقات البيانات",
                        durationMinutes = 40,
                        orderIndex = 2,
                        videoUrl = "https://example.com/stream/coroutines.mp4",
                        audioUrl = "https://example.com/audio/coroutines.mp3",
                        contentMarkdown = "إتقان البرمجة غير التزامنية، والمشغل stateIn وتوزيع المهام بين خيوط المعالجة في كوتلن.",
                        contentMarkdownAr = "إتقان البرمجة غير التزامنية، والمشغل stateIn وتوزيع المهام بين خيوط المعالجة في كوتلن.",
                        isCompleted = false,
                        isDownloaded = false
                    ),
                    LessonEntity(
                        id = "l_math_1",
                        courseId = "c_math_301",
                        title = "١. الفضاءات المتجهية والتحويلات المصفوفية",
                        titleAr = "١. الفضاءات المتجهية والتحويلات المصفوفية",
                        durationMinutes = 35,
                        orderIndex = 1,
                        videoUrl = "https://example.com/stream/math1.mp4",
                        audioUrl = "https://example.com/audio/math1.mp3",
                        contentMarkdown = "استكشف الاستقلال الخطي، والمتجهات الأساسية، ورتبة المصفوفات والتحويلات المحددة للأنظمة الكمومية.",
                        contentMarkdownAr = "استكشف الاستقلال الخطي، والمتجهات الأساسية، ورتبة المصفوفات والتحويلات المحددة للأنظمة الكمومية.",
                        isCompleted = false,
                        isDownloaded = false
                    ),
                    LessonEntity(
                        id = "l_math_2",
                        courseId = "c_math_301",
                        title = "٢. القيم والمتجهات الذاتية وتفكيك القيمة المنفردة",
                        titleAr = "٢. القيم والمتجهات الذاتية وتفكيك القيمة المنفردة",
                        durationMinutes = 45,
                        orderIndex = 2,
                        videoUrl = "https://example.com/stream/math2.mp4",
                        audioUrl = "https://example.com/audio/math2.mp3",
                        contentMarkdown = "فهم قطرية المصفوفات، وتحليل المكونات الرئيسية (PCA)، وتفكيك القيم المنفردة في تعلم الآلة.",
                        contentMarkdownAr = "فهم قطرية المصفوفات، وتحليل المكونات الرئيسية (PCA)، وتفكيك القيم المنفردة في تعلم الآلة.",
                        isCompleted = false,
                        isDownloaded = false
                    ),
                    LessonEntity(
                        id = "l_lang_1",
                        courseId = "c_lang_101",
                        title = "١. المصطلحات التقنية وأنظمة معالجة اللغة العربية",
                        titleAr = "١. المصطلحات التقنية وأنظمة معالجة اللغة العربية",
                        durationMinutes = 30,
                        orderIndex = 1,
                        videoUrl = "https://example.com/stream/lang1.mp4",
                        audioUrl = "https://example.com/audio/lang1.mp3",
                        contentMarkdown = "دراسة الاشتقاق الصرفي، والتشكيل، ومحاذاة التضمينات الدلالية لنماذج الذكاء الاصطناعي باللغة العربية.",
                        contentMarkdownAr = "دراسة الاشتقاق الصرفي، والتشكيل، ومحاذاة التضمينات الدلالية لنماذج الذكاء الاصطناعي باللغة العربية.",
                        isCompleted = false,
                        isDownloaded = false
                    )
                )
                database.lessonDao().insertLessons(lessons)
            }
        }
    }
}
