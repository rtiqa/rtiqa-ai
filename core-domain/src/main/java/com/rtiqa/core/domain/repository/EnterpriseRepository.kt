package com.rtiqa.core.domain.repository

import com.rtiqa.core.domain.model.AcademicYear
import com.rtiqa.core.domain.model.Branch
import com.rtiqa.core.domain.model.Department
import com.rtiqa.core.domain.model.EnterpriseMember
import com.rtiqa.core.domain.model.Major
import com.rtiqa.core.domain.model.Organization
import com.rtiqa.core.domain.model.Section
import com.rtiqa.core.domain.model.Semester
import com.rtiqa.core.domain.model.StudyPlan
import com.rtiqa.core.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface EnterpriseRepository {
    fun getOrganizations(): Flow<List<Organization>>
    fun getOrganizationById(id: String): Flow<Organization?>
    suspend fun saveOrganization(org: Organization)
    suspend fun deleteOrganization(id: String)

    fun getBranches(orgId: String): Flow<List<Branch>>
    suspend fun saveBranch(branch: Branch)
    suspend fun deleteBranch(id: String)

    fun getAcademicYears(orgId: String): Flow<List<AcademicYear>>
    suspend fun saveAcademicYear(academicYear: AcademicYear)

    fun getSemesters(academicYearId: String): Flow<List<Semester>>
    suspend fun saveSemester(semester: Semester)

    fun getDepartments(orgId: String): Flow<List<Department>>
    suspend fun saveDepartment(department: Department)

    fun getMajors(departmentId: String): Flow<List<Major>>
    suspend fun saveMajor(major: Major)

    fun getSections(majorId: String): Flow<List<Section>>
    suspend fun saveSection(section: Section)

    fun getSubjects(majorId: String): Flow<List<Subject>>
    suspend fun saveSubject(subject: Subject)

    fun getStudyPlans(majorId: String): Flow<List<StudyPlan>>
    suspend fun saveStudyPlan(studyPlan: StudyPlan)

    fun getMembers(orgId: String): Flow<List<EnterpriseMember>>
    suspend fun saveMember(member: EnterpriseMember)
    suspend fun deleteMember(id: String)
}
