import re
content = open('core-data/src/main/java/com/rtiqa/core/data/di/AppDiContainer.kt').read()
content = content.replace(
    '''        OfflineSyncManager(
            apiService = apiService,
            courseDao = database.courseDao(),
            syncDao = database.syncDao(),
            syncMutex = globalSyncMutex,
            sessionStore = restSessionStore
        )''',
    '''        OfflineSyncManager(
            apiService = apiService,
            courseDao = database.courseDao(),
            syncDao = database.syncDao(),
            syncMutex = globalSyncMutex,
            sessionStore = restSessionStore,
            securityManager = coreDiContainer.securityManager
        )'''
)
open('core-data/src/main/java/com/rtiqa/core/data/di/AppDiContainer.kt', 'w').write(content)
