package com.rtiqa.core.data.repository

import com.rtiqa.core.data.mapper.toDomain
import com.rtiqa.core.data.mapper.toEntity
import com.rtiqa.core.database.dao.EnterpriseDao
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
import com.rtiqa.core.domain.repository.EnterpriseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.rtiqa.core.domain.model.School

class EnterpriseRepositoryImpl(
    private val enterpriseDao: EnterpriseDao
) : EnterpriseRepository {

    override fun getSchools(): Flow<List<School>> =
        enterpriseDao.getAllSchools().map { list -> list.map { it.toDomain() } }

    override fun getSchoolById(id: String): Flow<School?> =
        enterpriseDao.getSchoolById(id).map { it?.toDomain() }

    override suspend fun saveSchool(school: School) {
        enterpriseDao.insertSchool(school.toEntity())
    }

    override suspend fun deleteSchool(id: String) {
        enterpriseDao.deleteSchool(id)
    }

    override fun getStudentsForSchool(schoolId: String): Flow<List<EnterpriseMember>> =
        enterpriseDao.getStudentsForSchool(schoolId).map { list -> list.map { it.toDomain() } }

    override fun getTeachersForSchool(schoolId: String): Flow<List<EnterpriseMember>> =
        enterpriseDao.getTeachersForSchool(schoolId).map { list -> list.map { it.toDomain() } }

    override fun getUsersForSchool(schoolId: String): Flow<List<EnterpriseMember>> =
        enterpriseDao.getUsersForSchool(schoolId).map { list -> list.map { it.toDomain() } }

    override fun getSectionsForSchool(schoolId: String): Flow<List<Section>> =
        enterpriseDao.getSectionsForSchool(schoolId).map { list -> list.map { it.toDomain() } }

    override fun getSubjectsForSchool(schoolId: String): Flow<List<Subject>> =
        enterpriseDao.getSubjectsForSchool(schoolId).map { list -> list.map { it.toDomain() } }

    override fun getOrganizations(): Flow<List<Organization>> =
        enterpriseDao.getAllOrganizations().map { list -> list.map { it.toDomain() } }

    override fun getOrganizationById(id: String): Flow<Organization?> =
        enterpriseDao.getOrganizationById(id).map { it?.toDomain() }

    override suspend fun saveOrganization(org: Organization) {
        enterpriseDao.insertOrganization(org.toEntity())
    }

    override suspend fun deleteOrganization(id: String) {
        enterpriseDao.deleteOrganization(id)
    }

    override fun getBranches(orgId: String): Flow<List<Branch>> =
        enterpriseDao.getBranchesForOrg(orgId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveBranch(branch: Branch) {
        enterpriseDao.insertBranch(branch.toEntity())
    }

    override suspend fun deleteBranch(id: String) {
        enterpriseDao.deleteBranch(id)
    }

    override fun getAcademicYears(orgId: String): Flow<List<AcademicYear>> =
        enterpriseDao.getAcademicYearsForOrg(orgId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveAcademicYear(academicYear: AcademicYear) {
        enterpriseDao.insertAcademicYear(academicYear.toEntity())
    }

    override fun getSemesters(academicYearId: String): Flow<List<Semester>> =
        enterpriseDao.getSemestersForYear(academicYearId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveSemester(semester: Semester) {
        enterpriseDao.insertSemester(semester.toEntity())
    }

    override fun getDepartments(orgId: String): Flow<List<Department>> =
        enterpriseDao.getDepartmentsForOrg(orgId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveDepartment(department: Department) {
        enterpriseDao.insertDepartment(department.toEntity())
    }

    override fun getMajors(departmentId: String): Flow<List<Major>> =
        enterpriseDao.getMajorsForDepartment(departmentId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveMajor(major: Major) {
        enterpriseDao.insertMajor(major.toEntity())
    }

    override fun getSections(majorId: String): Flow<List<Section>> =
        enterpriseDao.getSectionsForMajor(majorId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveSection(section: Section) {
        enterpriseDao.insertSection(section.toEntity())
    }

    override fun getSubjects(majorId: String): Flow<List<Subject>> =
        enterpriseDao.getSubjectsForMajor(majorId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveSubject(subject: Subject) {
        enterpriseDao.insertSubject(subject.toEntity())
    }

    override fun getStudyPlans(majorId: String): Flow<List<StudyPlan>> =
        enterpriseDao.getStudyPlansForMajor(majorId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveStudyPlan(studyPlan: StudyPlan) {
        enterpriseDao.insertStudyPlan(studyPlan.toEntity())
    }

    override fun getMembers(orgId: String): Flow<List<EnterpriseMember>> =
        enterpriseDao.getMembersForOrg(orgId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveMember(member: EnterpriseMember) {
        enterpriseDao.insertMember(member.toEntity())
    }

    override suspend fun deleteMember(id: String) {
        enterpriseDao.deleteMember(id)
    }
}
