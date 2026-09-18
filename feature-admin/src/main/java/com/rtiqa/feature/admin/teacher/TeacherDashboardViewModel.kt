package com.rtiqa.feature.admin.teacher

import androidx.lifecycle.viewModelScope
import com.rtiqa.core.ui.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TeacherDashboardViewModel : BaseViewModel<TeacherDashboardUiState, TeacherDashboardUiAction, TeacherDashboardUiEvent>(
    TeacherDashboardUiState()
) {

    init {
        loadDashboardData()
    }

    override fun onAction(action: TeacherDashboardUiAction) {
        when (action) {
            is TeacherDashboardUiAction.SelectTab -> {
                setState { copy(selectedTab = action.tabIndex) }
            }
            is TeacherDashboardUiAction.SearchQueryChanged -> {
                setState { copy(searchQuery = action.query) }
            }
            is TeacherDashboardUiAction.Refresh -> {
                loadDashboardData()
            }
            is TeacherDashboardUiAction.ClassClicked -> {
                sendEvent(TeacherDashboardUiEvent.NavigateToClassDetail(action.classId))
            }
            is TeacherDashboardUiAction.GradeSubmissionClicked -> {
                // In prototype: remove or mark as handled and notify
                val updatedList = currentState.pendingSubmissions.filterNot { it.id == action.submissionId }
                setState { copy(pendingSubmissions = updatedList) }
                sendEvent(TeacherDashboardUiEvent.ShowToast("تم فتح تصحيح الواجب بنجاح"))
            }
            is TeacherDashboardUiAction.OpenQuickNoticeDialog -> {
                setState {
                    copy(
                        showQuickNoticeDialog = true,
                        selectedClassForNotice = action.classItem
                    )
                }
            }
            is TeacherDashboardUiAction.CloseQuickNoticeDialog -> {
                setState {
                    copy(
                        showQuickNoticeDialog = false,
                        selectedClassForNotice = null
                    )
                }
            }
            is TeacherDashboardUiAction.SendQuickNotice -> {
                setState {
                    copy(
                        showQuickNoticeDialog = false,
                        selectedClassForNotice = null
                    )
                }
                sendEvent(TeacherDashboardUiEvent.ShowToast("تم إرسال التعميم إلى الطلاب بنجاح 📢"))
            }
        }
    }

    private fun loadDashboardData() {
        setState { copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            // Simulated realistic fetch with high quality prototype data
            delay(250)

            val mockMetrics = listOf(
                TeacherMetric(
                    title = "الفصول والشعب",
                    value = "5 فصول",
                    subtext = "142 طالبًا نشطًا",
                    iconType = "classes"
                ),
                TeacherMetric(
                    title = "واجبات بانتظار التصحيح",
                    value = "12 تسليم",
                    subtext = "3 مهام تحتاج تقييم",
                    iconType = "pending"
                ),
                TeacherMetric(
                    title = "نسبة الحضور الأسبوعي",
                    value = "96.4%",
                    subtext = "+2.1% عن الأسبوع الماضي",
                    iconType = "attendance"
                ),
                TeacherMetric(
                    title = "متوسط أداء الطلاب",
                    value = "88 / 100",
                    subtext = "مستوى متميز (A-)",
                    iconType = "performance"
                )
            )

            val mockClasses = listOf(
                TeacherClassItem(
                    id = "cls_101",
                    name = "مقدمة في الذكاء الاصطناعي",
                    subject = "علم البيانات والذكاء الاصطناعي",
                    gradeLevel = "الصف الثالث الثانوي",
                    section = "شعبة أ (متقدم)",
                    studentCount = 32,
                    scheduleTime = "الأحد والثلاثاء 08:30 ص",
                    room = "معمل الابتكار 1",
                    attendanceRate = 0.98f
                ),
                TeacherClassItem(
                    id = "cls_102",
                    name = "برمجة تطبيقات الأجهزة المحمولة",
                    subject = "علوم الحاسب وتقنية المعلومات",
                    gradeLevel = "الصف الثاني الثانوي",
                    section = "شعبة ب",
                    studentCount = 28,
                    scheduleTime = "الإثنين والأربعاء 10:15 ص",
                    room = "معمل الحاسب 3",
                    attendanceRate = 0.94f
                ),
                TeacherClassItem(
                    id = "cls_103",
                    name = "أساسيات الأمن السيبراني والشبكات",
                    subject = "الأمن الرقمي",
                    gradeLevel = "الصف الثالث الثانوي",
                    section = "شعبة ج",
                    studentCount = 30,
                    scheduleTime = "الأحد والخميس 11:45 ص",
                    room = "القاعة الرقمية 2",
                    attendanceRate = 0.95f
                ),
                TeacherClassItem(
                    id = "cls_104",
                    name = "التفكير الحسابي والخوارزميات",
                    subject = "المهارات الرقمية",
                    gradeLevel = "الصف الأول الثانوي",
                    section = "شعبة د",
                    studentCount = 27,
                    scheduleTime = "الثلاثاء 01:00 م",
                    room = "معمل الحاسب 1",
                    attendanceRate = 0.96f
                ),
                TeacherClassItem(
                    id = "cls_105",
                    name = "مشروع التخرج والابتكار البرمجي",
                    subject = "المشاريع التطبيقية",
                    gradeLevel = "الصف الثالث الثانوي",
                    section = "المسار التخصصي",
                    studentCount = 25,
                    scheduleTime = "الخميس 09:00 ص",
                    room = "حاضنة الابتكار المدرسي",
                    attendanceRate = 0.99f
                )
            )

            val mockPendingSubmissions = listOf(
                PendingSubmissionItem(
                    id = "sub_01",
                    assignmentTitle = "تطبيق نموذج التصنيف العصبي (KNN/Perceptron)",
                    studentName = "فيصل عبد الرحمن السالم",
                    className = "مقدمة في الذكاء الاصطناعي - شعبة أ",
                    submittedTimeAgo = "منذ 25 دقيقة",
                    type = "مختبر برمجي",
                    maxScore = 100
                ),
                PendingSubmissionItem(
                    id = "sub_02",
                    assignmentTitle = "واجهة Compose لتطبيق المهام مع State Flow",
                    studentName = "سارة خالد الشمري",
                    className = "برمجة تطبيقات الأجهزة المحمولة - شعبة ب",
                    submittedTimeAgo = "منذ ساعتين",
                    type = "مشروع عملي",
                    maxScore = 50
                ),
                PendingSubmissionItem(
                    id = "sub_03",
                    assignmentTitle = "تحليل حزم الشبكة وتحديد الثغرات بواسطة Wireshark",
                    studentName = "عمر سلطان العتيبي",
                    className = "أساسيات الأمن السيبراني - شعبة ج",
                    submittedTimeAgo = "منذ 4 ساعات",
                    type = "واجب تخصصي",
                    maxScore = 100
                ),
                PendingSubmissionItem(
                    id = "sub_04",
                    assignmentTitle = "خوارزمية البحث الثنائي ومصفوفات التعقيد Big-O",
                    studentName = "نورة محمد الغامدي",
                    className = "التفكير الحسابي والخوارزميات - شعبة د",
                    submittedTimeAgo = "منذ 5 ساعات",
                    type = "واجب منزلي",
                    maxScore = 30
                ),
                PendingSubmissionItem(
                    id = "sub_05",
                    assignmentTitle = "تقرير تقييم نموذج التعلم العميق على بيانات الصور",
                    studentName = "يوسف إبراهيم الدوسري",
                    className = "مقدمة في الذكاء الاصطناعي - شعبة أ",
                    submittedTimeAgo = "منذ يوم واحد",
                    type = "مشروع نهائي",
                    maxScore = 100
                )
            )

            val mockUpcomingSchedule = listOf(
                UpcomingScheduleItem(
                    id = "sch_1",
                    title = "حصة تفاعلية: شبكات الأعصاب التلافيفية (CNN)",
                    className = "مقدمة في الذكاء الاصطناعي (شعبة أ)",
                    time = "الآن • 08:30 ص - 09:15 ص",
                    location = "معمل الابتكار 1",
                    isLiveNow = true
                ),
                UpcomingScheduleItem(
                    id = "sch_2",
                    title = "مراجعة عملية وتصحيح المختبر الجماعي",
                    className = "برمجة تطبيقات الأجهزة المحمولة (شعبة ب)",
                    time = "اليوم • 10:15 ص - 11:00 ص",
                    location = "معمل الحاسب 3",
                    isLiveNow = false
                ),
                UpcomingScheduleItem(
                    id = "sch_3",
                    title = "ورشة فحص المنافذ والتشفير المتماثل",
                    className = "أساسيات الأمن السيبراني (شعبة ج)",
                    time = "اليوم • 11:45 ص - 12:30 م",
                    location = "القاعة الرقمية 2",
                    isLiveNow = false
                )
            )

            setState {
                copy(
                    isLoading = false,
                    metrics = mockMetrics,
                    classes = mockClasses,
                    pendingSubmissions = mockPendingSubmissions,
                    upcomingSchedule = mockUpcomingSchedule
                )
            }
        }
    }
}
