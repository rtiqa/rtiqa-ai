package com.rtiqa.core.network.session
import com.rtiqa.core.security.SecurityManager
interface RestSessionStore {
    fun saveSession(token: String, organizationId: String?)
    fun getSessionToken(): String?
    fun getActiveOrganizationId(): String?
    fun updateActiveOrganizationId(organizationId: String?)
    fun clearSession()
}
class RestSessionStoreImpl(private val securityManager: SecurityManager) : RestSessionStore {
    companion object {
        private const val KEY_AUTH_TOKEN = "rtiqa_rest_auth_token"
        private const val KEY_ACTIVE_ORG_ID = "rtiqa_rest_org_id"
    }
    override fun saveSession(token: String, organizationId: String?) {
        securityManager.putEncryptedString(KEY_AUTH_TOKEN, token)
        if (organizationId != null) securityManager.putEncryptedString(KEY_ACTIVE_ORG_ID, organizationId)
        else securityManager.removeKey(KEY_ACTIVE_ORG_ID)
    }
    override fun getSessionToken(): String? {
        val token = securityManager.getEncryptedString(KEY_AUTH_TOKEN)
        return if (token.isNullOrBlank()) null else token
    }
    override fun getActiveOrganizationId(): String? {
        val orgId = securityManager.getEncryptedString(KEY_ACTIVE_ORG_ID)
        return if (orgId.isNullOrBlank()) null else orgId
    }
    override fun updateActiveOrganizationId(organizationId: String?) {
        if (organizationId != null) securityManager.putEncryptedString(KEY_ACTIVE_ORG_ID, organizationId)
        else securityManager.removeKey(KEY_ACTIVE_ORG_ID)
    }
    override fun clearSession() {
        securityManager.removeKey(KEY_AUTH_TOKEN)
        securityManager.removeKey(KEY_ACTIVE_ORG_ID)
    }
}
