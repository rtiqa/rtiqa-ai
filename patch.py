import sys
import re

files = [
    'core-network/src/test/java/com/rtiqa/core/network/RestInterceptorsTest.kt',
    'core-data/src/test/java/com/rtiqa/core/data/remote/NodeAuthDataSourceImplTest.kt'
]

for f in files:
    content = open(f).read()
    content = content.replace('var sessionId:', 'var _sessionId:')
    content = content.replace('sessionId = newId', '_sessionId = newId')
    content = content.replace('getSessionId(): String? = sessionId', 'getSessionId(): String? = _sessionId')
    open(f, 'w').write(content)

q_file = 'core-data/src/test/java/com/rtiqa/core/data/repository/QuizRepositoryImplTest.kt'
q_content = open(q_file).read()
q_content = re.sub(
    r'sessionStore = object : com.rtiqa.core.network.session.RestSessionStore \{.*?\}',
    '''sessionStore = object : com.rtiqa.core.network.session.RestSessionStore {
                var _sessionId: String? = "test_session_id"
                override fun saveSession(t: String, o: String?) {}
                override fun getSessionToken() = null
                override fun getActiveOrganizationId() = null
                override fun updateActiveOrganizationId(o: String?) {}
                override fun generateAndSaveSessionId() = "new_id".also { _sessionId = it }
                override fun getSessionId() = _sessionId
                override fun clearSession() { _sessionId = null }
            }''',
    q_content, flags=re.DOTALL
)
open(q_file, 'w').write(q_content)
