package com.rtiqa.core.domain.usecase

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
import com.rtiqa.core.domain.model.School
import com.rtiqa.core.domain.repository.EnterpriseRepository
import kotlinx.coroutines.flow.Flow

class GetSchoolsUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(): Flow<List<School>> = repository.getSchools()
}

class GetSchoolByIdUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(id: String): Flow<School?> = repository.getSchoolById(id)
}

class SaveSchoolUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(school: School) = repository.saveSchool(school)
}

class DeleteSchoolUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(id: String) = repository.deleteSchool(id)
}

class GetStudentsForSchoolUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(schoolId: String): Flow<List<EnterpriseMember>> = repository.getStudentsForSchool(schoolId)
}

class GetTeachersForSchoolUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(schoolId: String): Flow<List<EnterpriseMember>> = repository.getTeachersForSchool(schoolId)
}

class GetUsersForSchoolUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(schoolId: String): Flow<List<EnterpriseMember>> = repository.getUsersForSchool(schoolId)
}

class GetSectionsForSchoolUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(schoolId: String): Flow<List<Section>> = repository.getSectionsForSchool(schoolId)
}

class GetSubjectsForSchoolUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(schoolId: String): Flow<List<Subject>> = repository.getSubjectsForSchool(schoolId)
}

class GetOrganizationsUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(): Flow<List<Organization>> = repository.getOrganizations()
}

class SaveOrganizationUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(org: Organization) = repository.saveOrganization(org)
}

class DeleteOrganizationUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(id: String) = repository.deleteOrganization(id)
}

class GetBranchesUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(orgId: String): Flow<List<Branch>> = repository.getBranches(orgId)
}

class SaveBranchUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(branch: Branch) = repository.saveBranch(branch)
}

class GetAcademicYearsUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(orgId: String): Flow<List<AcademicYear>> = repository.getAcademicYears(orgId)
}

class SaveAcademicYearUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(academicYear: AcademicYear) = repository.saveAcademicYear(academicYear)
}

class GetSemestersUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(academicYearId: String): Flow<List<Semester>> = repository.getSemesters(academicYearId)
}

class SaveSemesterUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(semester: Semester) = repository.saveSemester(semester)
}

class GetDepartmentsUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(orgId: String): Flow<List<Department>> = repository.getDepartments(orgId)
}

class SaveDepartmentUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(department: Department) = repository.saveDepartment(department)
}

class GetMajorsUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(departmentId: String): Flow<List<Major>> = repository.getMajors(departmentId)
}

class SaveMajorUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(major: Major) = repository.saveMajor(major)
}

class GetSectionsUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(majorId: String): Flow<List<Section>> = repository.getSections(majorId)
}

class SaveSectionUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(section: Section) = repository.saveSection(section)
}

class GetSubjectsUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(majorId: String): Flow<List<Subject>> = repository.getSubjects(majorId)
}

class SaveSubjectUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(subject: Subject) = repository.saveSubject(subject)
}

class GetStudyPlansUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(majorId: String): Flow<List<StudyPlan>> = repository.getStudyPlans(majorId)
}

class SaveStudyPlanUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(studyPlan: StudyPlan) = repository.saveStudyPlan(studyPlan)
}

class GetEnterpriseMembersUseCase(private val repository: EnterpriseRepository) {
    operator fun invoke(orgId: String): Flow<List<EnterpriseMember>> = repository.getMembers(orgId)
}

class SaveEnterpriseMemberUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(member: EnterpriseMember) = repository.saveMember(member)
}

class DeleteEnterpriseMemberUseCase(private val repository: EnterpriseRepository) {
    suspend operator fun invoke(id: String) = repository.deleteMember(id)
}
