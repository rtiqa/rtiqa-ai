import re
content = open('core-data/src/main/java/com/rtiqa/core/data/worker/OfflineSyncWorker.kt').read()
content = content.replace('syncDao.getPendingSyncItemsList()', 'syncDao.getPendingSyncItemsList(userId, startSessionId)')
open('core-data/src/main/java/com/rtiqa/core/data/worker/OfflineSyncWorker.kt', 'w').write(content)
