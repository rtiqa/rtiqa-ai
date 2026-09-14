import re
content = open('core-data/src/test/java/com/rtiqa/core/data/sync/OfflineSyncManagerIsolationTest.kt').read()
content = content.replace('import org.mockito.kotlin.mock\n', '')
content = content.replace('val apiService = mock<RtiqaApiService>()', 'val apiService = FakeRtiqaApiService()')
content = content.replace('val courseDao = mock<CourseDao>()', 'val courseDao = com.rtiqa.core.data.repository.FakeCourseDao()')
content = content.replace('''        val securityManagerA = object : SecurityManager {
            override fun saveEncryptedString(key: String, value: String) {}
            override fun getEncryptedString(key: String) = if (key == "user_id") "user_A" else null
            override fun removeKey(key: String) {}
            override fun clearAll() {}
        }''', '''        val securityManagerA = object : SecurityManager {
            override fun putEncryptedString(key: String, value: String) {}
            override fun getEncryptedString(key: String, defaultValue: String?): String? = if (key == "user_id") "user_A" else defaultValue
            override fun removeKey(key: String) {}
            override fun clearAll() {}
        }''')
content = content.replace('''        val securityManagerB = object : SecurityManager {
            override fun saveEncryptedString(key: String, value: String) {}
            override fun getEncryptedString(key: String) = if (key == "user_id") "user_B" else null
            override fun removeKey(key: String) {}
            override fun clearAll() {}
        }''', '''        val securityManagerB = object : SecurityManager {
            override fun putEncryptedString(key: String, value: String) {}
            override fun getEncryptedString(key: String, defaultValue: String?): String? = if (key == "user_id") "user_B" else defaultValue
            override fun removeKey(key: String) {}
            override fun clearAll() {}
        }''')

# we need to import FakeRtiqaApiService and FakeCourseDao
content = content.replace('import org.junit.Test', 'import org.junit.Test\nimport com.rtiqa.core.data.repository.FakeRtiqaApiService')

open('core-data/src/test/java/com/rtiqa/core/data/sync/OfflineSyncManagerIsolationTest.kt', 'w').write(content)
