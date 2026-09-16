#!/bin/bash
# Ah, clearSensitiveData is final! RoomDatabase functions without `open` or `abstract` are final in Kotlin by default.
# I can just add `open` to `clearSensitiveData()` in `RtiqaDatabase.kt`.
sed -i 's/fun clearSensitiveData()/open fun clearSensitiveData()/g' /app/applet/core-database/src/main/java/com/rtiqa/core/database/RtiqaDatabase.kt
