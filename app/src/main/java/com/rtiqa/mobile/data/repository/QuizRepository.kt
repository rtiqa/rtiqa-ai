package com.rtiqa.mobile.data.repository

import com.rtiqa.mobile.domain.model.Quiz
import com.rtiqa.mobile.domain.model.QuizQuestion

import com.rtiqa.mobile.domain.model.QuestionType

class QuizRepository {

    fun getSampleQuiz(courseId: String): Quiz {
        return Quiz(
            id = "q_ai_1",
            lessonId = "l_ai_1",
            courseId = courseId,
            title = "Neural Network Fundamentals Assessment",
            titleAr = "تقييم أساسيات الشبكات العصبية",
            timeLimitSeconds = 300,
            passingScorePercent = 70,
            questions = listOf(
                QuizQuestion(
                    id = "qq_1",
                    questionText = "What is the primary role of an activation function in an artificial neural network?",
                    questionTextAr = "ما هو الدور الرئيسي لدالة التنشيط في الخلية العصبية الاصطناعية؟",
                    options = listOf(
                        "To introduce non-linearity into the network output",
                        "To store memory states permanently",
                        "To increase network dataset size",
                        "To convert floating points to integers"
                    ),
                    optionsAr = listOf(
                        "إدخال اللاخطية على مخرجات الشبكة لتمكين تعلم العلاقات المعقدة",
                        "تخزين حالات الذاكرة بشكل دائم",
                        "زيادة حجم مجموعة البيانات",
                        "تحويل الأعداد العشرية إلى أعداد صحيحة"
                    ),
                    correctAnswerIndex = 0,
                    explanation = "Activation functions like ReLU, Sigmoid, or GELU allow neural networks to fit complex non-linear decision boundaries.",
                    explanationAr = "تسمح دوال التنشيط للشبكات العصبية بتمثيل حدود القرار غير الخطية المعقدة.",
                    hint = "Think about why linear regression alone cannot draw curved decision boundaries.",
                    hintAr = "فكر في سبب عدم قدرة الانحدار الخطي وحده على رسم منحنيات معقدة.",
                    xpReward = 50,
                    type = QuestionType.MULTIPLE_CHOICE
                ),
                QuizQuestion(
                    id = "qq_2",
                    questionText = "Gradient descent guarantees finding global minimum for non-convex functions.",
                    questionTextAr = "الانحدار التدريجي يضمن دائماً الوصول للحد الأدنى العام في الدوال غير التحدبية.",
                    options = listOf("True", "False"),
                    optionsAr = listOf("صح", "خطأ"),
                    correctAnswerIndex = 1,
                    explanation = "Gradient descent can get stuck in local minima or saddle points in non-convex optimization.",
                    explanationAr = "خطأ، قد يستقر الانحدار التدريجي في حد أدنى محلي دون الوصول للحد الأدنى العام.",
                    hint = "Think about local minima vs global minima.",
                    hintAr = "تذكر الفارق بين الحد الأدنى المحلي والعام.",
                    xpReward = 50,
                    type = QuestionType.TRUE_FALSE
                ),
                QuizQuestion(
                    id = "qq_3",
                    questionText = "Which algorithm calculates partial derivatives of the loss function using the calculus chain rule?",
                    questionTextAr = "أي خوارزمية تحسب المشتقات الجزئية لدالة الخسارة باستخدام قاعدة السلسلة الرياضية؟",
                    options = listOf(
                        "Backpropagation",
                        "K-Means Clustering",
                        "Breadth-First Search",
                        "Dijkstra Shortest Path"
                    ),
                    optionsAr = listOf(
                        "الانتشار العكسي (Backpropagation)",
                        "تجميع كيو-مينز (K-Means)",
                        "البحث بالعرض أولاً",
                        "خوارزمية ديكسترا"
                    ),
                    correctAnswerIndex = 0,
                    explanation = "Backpropagation efficiently passes error gradients backward from output layer to input layer.",
                    explanationAr = "تمرر خوارزمية الانتشار العكسي تدرج الخطأ من طبقة المخرجات نحو المدخلات.",
                    hint = "It propagates errors backward through layers.",
                    hintAr = "إنها تنتشر بالخطأ عكسياً عبر الطبقات.",
                    xpReward = 50,
                    type = QuestionType.MULTIPLE_CHOICE
                )
            )
        )
    }
}
