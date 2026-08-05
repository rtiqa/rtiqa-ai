package com.rtiqa.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rtiqa.core.database.entity.AcademicYearEntity
import com.rtiqa.core.database.entity.BranchEntity
import com.rtiqa.core.database.entity.DepartmentEntity
import com.rtiqa.core.database.entity.EnterpriseMemberEntity
import com.rtiqa.core.database.entity.MajorEntity
import com.rtiqa.core.database.entity.OrganizationEntity
import com.rtiqa.core.database.entity.SectionEntity
import com.rtiqa.core.database.entity.SemesterEntity
import com.rtiqa.core.database.entity.StudyPlanEntity
import com.rtiqa.core.database.entity.SubjectEntity
import com.rtiqa.core.database.entity.SchoolEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnterpriseDao {
    @Query("SELECT * FROM schools ORDER BY createdAt DESC")
    fun getAllSchools(): Flow<List<SchoolEntity>>

    @Query("SELECT * FROM schools WHERE id = :id LIMIT 1")
    fun getSchoolById(id: String): Flow<SchoolEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchool(school: SchoolEntity)

    @Query("DELETE FROM schools WHERE id = :id")
    suspend fun deleteSchool(id: String)

    @Query("SELECT * FROM enterprise_members WHERE schoolId = :schoolId AND role = 'STUDENT'")
    fun getStudentsForSchool(schoolId: String): Flow<List<EnterpriseMemberEntity>>

    @Query("SELECT * FROM enterprise_members WHERE schoolId = :schoolId AND role = 'TEACHER'")
    fun getTeachersForSchool(schoolId: String): Flow<List<EnterpriseMemberEntity>>

    @Query("SELECT * FROM enterprise_members WHERE schoolId = :schoolId")
    fun getUsersForSchool(schoolId: String): Flow<List<EnterpriseMemberEntity>>

    @Query("SELECT * FROM sections WHERE schoolId = :schoolId")
    fun getSectionsForSchool(schoolId: String): Flow<List<SectionEntity>>

    @Query("SELECT * FROM subjects WHERE schoolId = :schoolId")
    fun getSubjectsForSchool(schoolId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM organizations ORDER BY createdAt DESC")
    fun getAllOrganizations(): Flow<List<OrganizationEntity>>

    @Query("SELECT * FROM organizations WHERE id = :id LIMIT 1")
    fun getOrganizationById(id: String): Flow<OrganizationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrganization(org: OrganizationEntity)

    @Query("DELETE FROM organizations WHERE id = :id")
    suspend fun deleteOrganization(id: String)

    @Query("SELECT * FROM branches WHERE orgId = :orgId")
    fun getBranchesForOrg(orgId: String): Flow<List<BranchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBranch(branch: BranchEntity)

    @Query("DELETE FROM branches WHERE id = :id")
    suspend fun deleteBranch(id: String)

    @Query("SELECT * FROM academic_years WHERE orgId = :orgId")
    fun getAcademicYearsForOrg(orgId: String): Flow<List<AcademicYearEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAcademicYear(year: AcademicYearEntity)

    @Query("DELETE FROM academic_years WHERE id = :id")
    suspend fun deleteAcademicYear(id: String)

    @Query("SELECT * FROM semesters WHERE academicYearId = :yearId ORDER BY `order` ASC")
    fun getSemestersForYear(yearId: String): Flow<List<SemesterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSemester(semester: SemesterEntity)

    @Query("SELECT * FROM departments WHERE orgId = :orgId")
    fun getDepartmentsForOrg(orgId: String): Flow<List<DepartmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartment(department: DepartmentEntity)

    @Query("SELECT * FROM majors WHERE departmentId = :deptId")
    fun getMajorsForDepartment(deptId: String): Flow<List<MajorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMajor(major: MajorEntity)

    @Query("SELECT * FROM sections WHERE majorId = :majorId")
    fun getSectionsForMajor(majorId: String): Flow<List<SectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionEntity)

    @Query("DELETE FROM sections WHERE id = :id")
    suspend fun deleteSection(id: String)

    @Query("SELECT * FROM subjects WHERE majorId = :majorId")
    fun getSubjectsForMajor(majorId: String): Flow<List<SubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :id")
    suspend fun deleteSubject(id: String)

    @Query("SELECT * FROM study_plans WHERE majorId = :majorId")
    fun getStudyPlansForMajor(majorId: String): Flow<List<StudyPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyPlan(studyPlan: StudyPlanEntity)

    @Query("SELECT * FROM enterprise_members WHERE orgId = :orgId")
    fun getMembersForOrg(orgId: String): Flow<List<EnterpriseMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: EnterpriseMemberEntity)

    @Query("DELETE FROM enterprise_members WHERE id = :id")
    suspend fun deleteMember(id: String)
}
