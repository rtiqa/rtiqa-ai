import re

with open('app/src/main/java/com/rtiqa/mobile/ui/navigation/RtiqaNavGraph.kt', 'r') as f:
    content = f.read()

nav_destination = """
            composable("academic_structure") {
                val academicStructureViewModel: com.rtiqa.feature.admin.academic.AcademicStructureViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = com.rtiqa.feature.admin.academic.AcademicStructureViewModelFactory(
                        getAcademicYearsUseCase = appDiContainer.domainUseCasesContainer.getAcademicYearsUseCase!!,
                        saveAcademicYearUseCase = appDiContainer.domainUseCasesContainer.saveAcademicYearUseCase!!,
                        deleteAcademicYearUseCase = appDiContainer.domainUseCasesContainer.deleteAcademicYearUseCase!!,
                        getSemestersUseCase = appDiContainer.domainUseCasesContainer.getSemestersUseCase!!,
                        saveSemesterUseCase = appDiContainer.domainUseCasesContainer.saveSemesterUseCase!!,
                        deleteSemesterUseCase = appDiContainer.domainUseCasesContainer.deleteSemesterUseCase!!,
                        getGradeLevelsForSchoolUseCase = appDiContainer.domainUseCasesContainer.getGradeLevelsForSchoolUseCase!!,
                        saveGradeLevelUseCase = appDiContainer.domainUseCasesContainer.saveGradeLevelUseCase!!,
                        deleteGradeLevelUseCase = appDiContainer.domainUseCasesContainer.deleteGradeLevelUseCase!!,
                        getDepartmentsUseCase = appDiContainer.domainUseCasesContainer.getDepartmentsUseCase!!,
                        saveDepartmentUseCase = appDiContainer.domainUseCasesContainer.saveDepartmentUseCase!!,
                        deleteDepartmentUseCase = appDiContainer.domainUseCasesContainer.deleteDepartmentUseCase!!,
                        getMajorsUseCase = appDiContainer.domainUseCasesContainer.getMajorsUseCase!!,
                        saveMajorUseCase = appDiContainer.domainUseCasesContainer.saveMajorUseCase!!,
                        deleteMajorUseCase = appDiContainer.domainUseCasesContainer.deleteMajorUseCase!!,
                        getSubjectsForSchoolUseCase = appDiContainer.domainUseCasesContainer.getSubjectsForSchoolUseCase!!,
                        saveSubjectUseCase = appDiContainer.domainUseCasesContainer.saveSubjectUseCase!!,
                        deleteSubjectUseCase = appDiContainer.domainUseCasesContainer.deleteSubjectUseCase!!,
                        getSectionsForSchoolUseCase = appDiContainer.domainUseCasesContainer.getSectionsForSchoolUseCase!!,
                        saveSectionUseCase = appDiContainer.domainUseCasesContainer.saveSectionUseCase!!,
                        deleteSectionUseCase = appDiContainer.domainUseCasesContainer.deleteSectionUseCase!!,
                        getStudyPlansUseCase = appDiContainer.domainUseCasesContainer.getStudyPlansUseCase!!,
                        saveStudyPlanUseCase = appDiContainer.domainUseCasesContainer.saveStudyPlanUseCase!!,
                        deleteStudyPlanUseCase = appDiContainer.domainUseCasesContainer.deleteStudyPlanUseCase!!
                    )
                )
                val academicUiState by academicStructureViewModel.uiState.collectAsState()
                com.rtiqa.feature.admin.academic.AcademicStructureScreen(
                    uiState = academicUiState,
                    onAction = { action -> academicStructureViewModel.onAction(action) },
                    onBack = { navController.popBackStack() }
                )
            }
"""

if "composable(\"academic_structure\")" not in content:
    content = content.replace("composable(\"schools_management\") {", nav_destination.strip() + "\n            composable(\"schools_management\") {")

with open('app/src/main/java/com/rtiqa/mobile/ui/navigation/RtiqaNavGraph.kt', 'w') as f:
    f.write(content)
