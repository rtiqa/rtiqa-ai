package com.example.data.repository

import com.example.domain.model.Quiz
import com.example.domain.model.QuizQuestion

class QuizRepository {

    fun getSampleQuiz(courseId: String): Quiz {
        return Quiz(
            id = "q_ai_1",
            lessonId = "l_ai_1",
            courseId = courseId,
            title = "Neural Network Fundamentals Assessment",
            titleAr = "تقييم أساسيات الشبكات العصبية",
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
                    xpReward = 50
                ),
                QuizQuestion(
                    id = "qq_2",
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
                    xpReward = 50
                ),
                QuizQuestion(
                    id = "qq_3",
                    questionText = "In Transformer architectures, what does Self-Attention calculate for each word token?",
                    questionTextAr = "في نماذج المحولات، ماذا تحسب آلية الانتباه الذاتي لكل رمز كلمة؟",
                    options = listOf(
                        "Query (Q), Key (K), and Value (V) representations",
                        "RGB Pixel Histogram",
                        "Frequency Spectrogram",
                        "Network IP Address"
                    ),
                    optionsAr = listOf(
                        "تمثيلات الاستعلام (Q) والمفتاح (K) والقيمة (V)",
                        "المدرج التكراري للبكسلات",
                        "مخطط التردد الصوتي",
                        "عنوان شبكة الإنترنت"
                    ),
                    correctAnswerIndex = 0,
                    explanation = "Self-attention project token embeddings into Q, K, V space to compute weighted dynamic contexts.",
                    explanationAr = "إسقاط التضمينات في فضاء الاستعلام والمفتاح والقيمة لحساب العلاقات السياقية.",
                    hint = "Three fundamental matrices in Attention Is All You Need paper.",
                    hintAr = "ثلاث مصفوفات أساسية في الورقة العلمية للمحولات.",
                    xpReward = 50
                )
            )
        )
    }
}
