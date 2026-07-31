package com.rtiqa.core.data.mapper

import com.rtiqa.core.database.entity.CourseEntity
import com.rtiqa.core.database.entity.LessonEntity
import com.rtiqa.core.database.entity.UserProfileEntity
import com.rtiqa.core.domain.model.Course
import com.rtiqa.core.domain.model.Lesson
import com.rtiqa.core.domain.model.UserProfile
import com.rtiqa.core.network.model.CourseDto
import com.rtiqa.core.network.model.LessonDto

fun CourseEntity.toDomain(): Course = Course(
    id = id,
    title = title,
    description = description,
    category = category,
    totalLessons = totalLessons,
    durationMinutes = durationMinutes,
    iconUrl = iconUrl,
    isDownloaded = isDownloaded,
    progressPercent = progressPercent,
    isEnrolled = isEnrolled,
    isBookmarked = isBookmarked
)

fun Course.toEntity(): CourseEntity = CourseEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    totalLessons = totalLessons,
    durationMinutes = durationMinutes,
    iconUrl = iconUrl,
    isDownloaded = isDownloaded,
    progressPercent = progressPercent,
    isEnrolled = isEnrolled,
    isBookmarked = isBookmarked
)

fun CourseDto.toEntity(): CourseEntity = CourseEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    totalLessons = totalLessons,
    durationMinutes = durationMinutes,
    iconUrl = iconUrl,
    isDownloaded = false,
    progressPercent = 0f,
    isEnrolled = false,
    isBookmarked = false
)

fun com.rtiqa.core.network.api.NetworkCourseDto.toEntity(): CourseEntity = CourseEntity(
    id = id,
    title = title,
    description = description,
    category = category,
    totalLessons = totalModules,
    durationMinutes = 30,
    iconUrl = null,
    isDownloaded = false,
    progressPercent = progressPercent,
    isEnrolled = false,
    isBookmarked = false
)

fun LessonEntity.toDomain(): Lesson = Lesson(
    id = id,
    courseId = courseId,
    title = title,
    content = content,
    order = order,
    isCompleted = isCompleted,
    audioUrl = audioUrl
)

fun Lesson.toEntity(): LessonEntity = LessonEntity(
    id = id,
    courseId = courseId,
    title = title,
    content = content,
    order = order,
    isCompleted = isCompleted,
    audioUrl = audioUrl
)

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    id = id,
    name = name,
    email = email,
    avatarUrl = avatarUrl,
    levelXp = levelXp,
    streakDays = streakDays,
    isAdmin = isAdmin,
    isOfflineModeEnabled = isOfflineModeEnabled
)

// Enterprise Mappers
fun com.rtiqa.core.database.entity.OrganizationEntity.toDomain(): com.rtiqa.core.domain.model.Organization = com.rtiqa.core.domain.model.Organization(
    id = id,
    name = name,
    type = com.rtiqa.core.domain.model.OrgType.valueOf(type),
    code = code,
    logoUrl = logoUrl,
    status = com.rtiqa.core.domain.model.OrgStatus.valueOf(status),
    branchesCount = branchesCount,
    studentsCount = studentsCount,
    teachersCount = teachersCount,
    createdAt = createdAt
)

fun com.rtiqa.core.domain.model.Organization.toEntity(): com.rtiqa.core.database.entity.OrganizationEntity = com.rtiqa.core.database.entity.OrganizationEntity(
    id = id,
    name = name,
    type = type.name,
    code = code,
    logoUrl = logoUrl,
    status = status.name,
    branchesCount = branchesCount,
    studentsCount = studentsCount,
    teachersCount = teachersCount,
    createdAt = createdAt
)

fun com.rtiqa.core.database.entity.BranchEntity.toDomain(): com.rtiqa.core.domain.model.Branch = com.rtiqa.core.domain.model.Branch(
    id = id,
    orgId = orgId,
    name = name,
    code = code,
    address = address,
    phone = phone
)

fun com.rtiqa.core.domain.model.Branch.toEntity(): com.rtiqa.core.database.entity.BranchEntity = com.rtiqa.core.database.entity.BranchEntity(
    id = id,
    orgId = orgId,
    name = name,
    code = code,
    address = address,
    phone = phone
)

fun com.rtiqa.core.database.entity.AcademicYearEntity.toDomain(): com.rtiqa.core.domain.model.AcademicYear = com.rtiqa.core.domain.model.AcademicYear(
    id = id,
    orgId = orgId,
    name = name,
    startDate = startDate,
    endDate = endDate,
    isCurrent = isCurrent
)

fun com.rtiqa.core.domain.model.AcademicYear.toEntity(): com.rtiqa.core.database.entity.AcademicYearEntity = com.rtiqa.core.database.entity.AcademicYearEntity(
    id = id,
    orgId = orgId,
    name = name,
    startDate = startDate,
    endDate = endDate,
    isCurrent = isCurrent
)

fun com.rtiqa.core.database.entity.SemesterEntity.toDomain(): com.rtiqa.core.domain.model.Semester = com.rtiqa.core.domain.model.Semester(
    id = id,
    academicYearId = academicYearId,
    name = name,
    order = order,
    isActive = isActive
)

fun com.rtiqa.core.domain.model.Semester.toEntity(): com.rtiqa.core.database.entity.SemesterEntity = com.rtiqa.core.database.entity.SemesterEntity(
    id = id,
    academicYearId = academicYearId,
    name = name,
    order = order,
    isActive = isActive
)

fun com.rtiqa.core.database.entity.DepartmentEntity.toDomain(): com.rtiqa.core.domain.model.Department = com.rtiqa.core.domain.model.Department(
    id = id,
    orgId = orgId,
    name = name,
    code = code,
    headName = headName
)

fun com.rtiqa.core.domain.model.Department.toEntity(): com.rtiqa.core.database.entity.DepartmentEntity = com.rtiqa.core.database.entity.DepartmentEntity(
    id = id,
    orgId = orgId,
    name = name,
    code = code,
    headName = headName
)

fun com.rtiqa.core.database.entity.MajorEntity.toDomain(): com.rtiqa.core.domain.model.Major = com.rtiqa.core.domain.model.Major(
    id = id,
    departmentId = departmentId,
    name = name,
    code = code,
    degreeType = degreeType
)

fun com.rtiqa.core.domain.model.Major.toEntity(): com.rtiqa.core.database.entity.MajorEntity = com.rtiqa.core.database.entity.MajorEntity(
    id = id,
    departmentId = departmentId,
    name = name,
    code = code,
    degreeType = degreeType
)

fun com.rtiqa.core.database.entity.SectionEntity.toDomain(): com.rtiqa.core.domain.model.Section = com.rtiqa.core.domain.model.Section(
    id = id,
    majorId = majorId,
    semesterId = semesterId,
    branchId = branchId,
    name = name,
    capacity = capacity,
    studentsCount = studentsCount
)

fun com.rtiqa.core.domain.model.Section.toEntity(): com.rtiqa.core.database.entity.SectionEntity = com.rtiqa.core.database.entity.SectionEntity(
    id = id,
    majorId = majorId,
    semesterId = semesterId,
    branchId = branchId,
    name = name,
    capacity = capacity,
    studentsCount = studentsCount
)

fun com.rtiqa.core.database.entity.SubjectEntity.toDomain(): com.rtiqa.core.domain.model.Subject = com.rtiqa.core.domain.model.Subject(
    id = id,
    majorId = majorId,
    code = code,
    name = name,
    creditHours = creditHours
)

fun com.rtiqa.core.domain.model.Subject.toEntity(): com.rtiqa.core.database.entity.SubjectEntity = com.rtiqa.core.database.entity.SubjectEntity(
    id = id,
    majorId = majorId,
    code = code,
    name = name,
    creditHours = creditHours
)

fun com.rtiqa.core.database.entity.StudyPlanEntity.toDomain(): com.rtiqa.core.domain.model.StudyPlan = com.rtiqa.core.domain.model.StudyPlan(
    id = id,
    majorId = majorId,
    name = name,
    totalCredits = totalCredits,
    version = version
)

fun com.rtiqa.core.domain.model.StudyPlan.toEntity(): com.rtiqa.core.database.entity.StudyPlanEntity = com.rtiqa.core.database.entity.StudyPlanEntity(
    id = id,
    majorId = majorId,
    name = name,
    totalCredits = totalCredits,
    version = version
)

fun com.rtiqa.core.database.entity.EnterpriseMemberEntity.toDomain(): com.rtiqa.core.domain.model.EnterpriseMember = com.rtiqa.core.domain.model.EnterpriseMember(
    id = id,
    orgId = orgId,
    name = name,
    email = email,
    role = com.rtiqa.core.domain.model.EnterpriseRole.valueOf(role),
    department = department,
    status = com.rtiqa.core.domain.model.MemberStatus.valueOf(status),
    phone = phone
)

fun com.rtiqa.core.domain.model.EnterpriseMember.toEntity(): com.rtiqa.core.database.entity.EnterpriseMemberEntity = com.rtiqa.core.database.entity.EnterpriseMemberEntity(
    id = id,
    orgId = orgId,
    name = name,
    email = email,
    role = role.name,
    department = department,
    status = status.name,
    phone = phone
)

// Academic Platform Mappers
fun com.rtiqa.core.database.entity.CurriculumModuleEntity.toDomain(): com.rtiqa.core.domain.model.CurriculumModule =
    com.rtiqa.core.domain.model.CurriculumModule(id, courseId, orgId, title, description, orderIndex, durationHours)

fun com.rtiqa.core.domain.model.CurriculumModule.toEntity(): com.rtiqa.core.database.entity.CurriculumModuleEntity =
    com.rtiqa.core.database.entity.CurriculumModuleEntity(id, courseId, orgId, title, description, orderIndex, durationHours)

fun com.rtiqa.core.database.entity.AcademicLessonEntity.toDomain(): com.rtiqa.core.domain.model.AcademicLesson =
    com.rtiqa.core.domain.model.AcademicLesson(id, moduleId, courseId, title, content, videoUrl, pdfAttachmentUrl, durationMinutes, orderIndex, isCompleted)

fun com.rtiqa.core.domain.model.AcademicLesson.toEntity(): com.rtiqa.core.database.entity.AcademicLessonEntity =
    com.rtiqa.core.database.entity.AcademicLessonEntity(id, moduleId, courseId, title, content, videoUrl, pdfAttachmentUrl, durationMinutes, orderIndex, isCompleted)

fun com.rtiqa.core.database.entity.AssignmentEntity.toDomain(): com.rtiqa.core.domain.model.Assignment =
    com.rtiqa.core.domain.model.Assignment(id, lessonId, courseId, title, prompt, maxScore, dueDate, com.rtiqa.core.domain.model.AssignmentType.valueOf(type))

fun com.rtiqa.core.domain.model.Assignment.toEntity(): com.rtiqa.core.database.entity.AssignmentEntity =
    com.rtiqa.core.database.entity.AssignmentEntity(id, lessonId, courseId, title, prompt, maxScore, dueDate, type.name)

fun com.rtiqa.core.database.entity.AssignmentSubmissionEntity.toDomain(): com.rtiqa.core.domain.model.AssignmentSubmission =
    com.rtiqa.core.domain.model.AssignmentSubmission(id, assignmentId, studentId, submissionContent, fileAttachmentUrl, com.rtiqa.core.domain.model.SubmissionStatus.valueOf(status), score, feedback, submittedAt)

fun com.rtiqa.core.domain.model.AssignmentSubmission.toEntity(): com.rtiqa.core.database.entity.AssignmentSubmissionEntity =
    com.rtiqa.core.database.entity.AssignmentSubmissionEntity(id, assignmentId, studentId, submissionContent, fileAttachmentUrl, status.name, score, feedback, submittedAt)

fun com.rtiqa.core.database.entity.QuestionBankEntity.toDomain(): com.rtiqa.core.domain.model.QuestionBankItem =
    com.rtiqa.core.domain.model.QuestionBankItem(id, courseId, orgId, questionText, optionA, optionB, optionC, optionD, correctAnswerIndex, explanation, difficultyLevel, com.rtiqa.core.domain.model.QuestionType.valueOf(questionType))

fun com.rtiqa.core.domain.model.QuestionBankItem.toEntity(): com.rtiqa.core.database.entity.QuestionBankEntity =
    com.rtiqa.core.database.entity.QuestionBankEntity(id, courseId, orgId, questionText, optionA, optionB, optionC, optionD, correctAnswerIndex, explanation, difficultyLevel, questionType.name)

fun com.rtiqa.core.database.entity.AssessmentEntity.toDomain(): com.rtiqa.core.domain.model.Assessment =
    com.rtiqa.core.domain.model.Assessment(id, courseId, orgId, title, com.rtiqa.core.domain.model.AssessmentType.valueOf(type), passingScore, timeLimitMinutes, totalQuestions)

fun com.rtiqa.core.domain.model.Assessment.toEntity(): com.rtiqa.core.database.entity.AssessmentEntity =
    com.rtiqa.core.database.entity.AssessmentEntity(id, courseId, orgId, title, type.name, passingScore, timeLimitMinutes, totalQuestions)

fun com.rtiqa.core.database.entity.AssessmentAttemptEntity.toDomain(): com.rtiqa.core.domain.model.AssessmentAttempt =
    com.rtiqa.core.domain.model.AssessmentAttempt(id, assessmentId, studentId, scorePercent, isPassed, autoGradedFeedback, completedAt)

fun com.rtiqa.core.domain.model.AssessmentAttempt.toEntity(): com.rtiqa.core.database.entity.AssessmentAttemptEntity =
    com.rtiqa.core.database.entity.AssessmentAttemptEntity(id, assessmentId, studentId, scorePercent, isPassed, autoGradedFeedback, completedAt)

fun com.rtiqa.core.database.entity.GradebookRecordEntity.toDomain(): com.rtiqa.core.domain.model.GradebookRecord =
    com.rtiqa.core.domain.model.GradebookRecord(id, studentId, courseId, orgId, courseName, totalScore, gradeLetter, gpaValue, isPassed)

fun com.rtiqa.core.domain.model.GradebookRecord.toEntity(): com.rtiqa.core.database.entity.GradebookRecordEntity =
    com.rtiqa.core.database.entity.GradebookRecordEntity(id, studentId, courseId, orgId, courseName, totalScore, gradeLetter, gpaValue, isPassed)

fun com.rtiqa.core.database.entity.StudentProgressEntity.toDomain(): com.rtiqa.core.domain.model.StudentProgress =
    com.rtiqa.core.domain.model.StudentProgress(id, studentId, courseId, completedLessonsCount, totalLessonsCount, progressPercent, lastAccessedAt)

fun com.rtiqa.core.domain.model.StudentProgress.toEntity(): com.rtiqa.core.database.entity.StudentProgressEntity =
    com.rtiqa.core.database.entity.StudentProgressEntity(id, studentId, courseId, completedLessonsCount, totalLessonsCount, progressPercent, lastAccessedAt)

fun com.rtiqa.core.database.entity.AchievementBadgeEntity.toDomain(): com.rtiqa.core.domain.model.AchievementBadge =
    com.rtiqa.core.domain.model.AchievementBadge(id, studentId, badgeName, badgeDescription, iconName, unlockedAt)

fun com.rtiqa.core.domain.model.AchievementBadge.toEntity(): com.rtiqa.core.database.entity.AchievementBadgeEntity =
    com.rtiqa.core.database.entity.AchievementBadgeEntity(id, studentId, badgeName, badgeDescription, iconName, unlockedAt)

fun com.rtiqa.core.database.entity.LearningPathEntity.toDomain(): com.rtiqa.core.domain.model.LearningPath =
    com.rtiqa.core.domain.model.LearningPath(id, orgId, title, description, category, courseIdsJson.split(",").filter { it.isNotBlank() })

fun com.rtiqa.core.domain.model.LearningPath.toEntity(): com.rtiqa.core.database.entity.LearningPathEntity =
    com.rtiqa.core.database.entity.LearningPathEntity(id, orgId, title, description, category, courseIds.joinToString(","))

fun com.rtiqa.core.database.entity.PrerequisiteEntity.toDomain(): com.rtiqa.core.domain.model.Prerequisite =
    com.rtiqa.core.domain.model.Prerequisite(id, targetCourseId, requiredCourseId, requiredCourseTitle)

fun com.rtiqa.core.domain.model.Prerequisite.toEntity(): com.rtiqa.core.database.entity.PrerequisiteEntity =
    com.rtiqa.core.database.entity.PrerequisiteEntity(id, targetCourseId, requiredCourseId, requiredCourseTitle)

fun com.rtiqa.core.database.entity.SmartRecommendationEntity.toDomain(): com.rtiqa.core.domain.model.SmartRecommendation =
    com.rtiqa.core.domain.model.SmartRecommendation(id, studentId, courseId, courseTitle, reasonText, confidenceScore)

fun com.rtiqa.core.domain.model.SmartRecommendation.toEntity(): com.rtiqa.core.database.entity.SmartRecommendationEntity =
    com.rtiqa.core.database.entity.SmartRecommendationEntity(id, studentId, courseId, courseTitle, reasonText, confidenceScore)

fun com.rtiqa.core.database.entity.OfflineContentDownloadEntity.toDomain(): com.rtiqa.core.domain.model.OfflineContentDownload =
    com.rtiqa.core.domain.model.OfflineContentDownload(id, courseId, lessonId, localFilePath, com.rtiqa.core.domain.model.DownloadStatus.valueOf(status), progressPercent)

fun com.rtiqa.core.domain.model.OfflineContentDownload.toEntity(): com.rtiqa.core.database.entity.OfflineContentDownloadEntity =
    com.rtiqa.core.database.entity.OfflineContentDownloadEntity(id, courseId, lessonId, localFilePath, status.name, progressPercent)
