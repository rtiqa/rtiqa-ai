#!/bin/bash
sed -i '/private fun seedInitialSchoolsIfEmpty() {/,+1s/viewModelScope.launch {/if(false) viewModelScope.launch {/' /app/applet/feature-admin/src/main/java/com/rtiqa/feature/admin/school/SchoolViewModel.kt
