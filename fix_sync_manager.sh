#!/bin/bash
sed -i '/val syncItem = SyncQueueEntity(/i \
                var finalPayload = payloadJson\
                val orgId = sessionStore.getActiveOrganizationId()\
                if (!orgId.isNullOrBlank()) {\
                    try {\
                        val json = org.json.JSONObject(payloadJson)\
                        json.put("orgId", orgId)\
                        finalPayload = json.toString()\
                    } catch (e: Exception) {\
                        RtiqaLog.w(tag, "Could not embed orgId into payloadJson", e)\
                    }\
                }' /app/applet/core-data/src/main/java/com/rtiqa/core/data/sync/OfflineSyncManager.kt
sed -i 's/payloadJson = payloadJson/payloadJson = finalPayload/' /app/applet/core-data/src/main/java/com/rtiqa/core/data/sync/OfflineSyncManager.kt
