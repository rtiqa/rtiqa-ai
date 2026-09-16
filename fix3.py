with open('core-domain/src/main/java/com/rtiqa/core/domain/model/EnterpriseModels.kt', 'r') as f:
    lines = f.readlines()

new_lines = lines[:2] + [
    "data class School(\n",
    "    val id: String,\n",
    "    val name: String,\n",
    "    val code: String,\n",
    "    val address: String = \"\",\n",
    "    val phone: String = \"\",\n",
    "    val logoUrl: String? = null,\n",
    "    val studentsCount: Int = 0,\n",
    "    val teachersCount: Int = 0,\n",
    "    val createdAt: Long = System.currentTimeMillis(),\n",
    "    val orgId: String = \"\"\n",
    ")\n"
] + lines[15:]

with open('core-domain/src/main/java/com/rtiqa/core/domain/model/EnterpriseModels.kt', 'w') as f:
    f.writelines(new_lines)
