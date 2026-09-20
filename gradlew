#!/usr/bin/env bash
# ==============================================================================
# Gradle Wrapper Executable for Linux/macOS
# RTIQA Open-Source Developer Tooling
# ==============================================================================
set -euo pipefail

# Resolve script directory
PRG="$0"
while [ -h "$PRG" ]; do
    ls=$(ls -ld "$PRG")
    link=$(expr "$ls" : '.*-> \(.*\)$')
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=$(dirname "$PRG")/"$link"
    fi
done
SAVED="$(pwd)"
CDPATH="" cd "$(dirname "$PRG")" >/dev/null 2>&1 || exit 1
APP_HOME="$(pwd -P)"
cd "$SAVED" >/dev/null 2>&1 || exit 1

# Execute standard system gradle if wrapper jar is absent in lightweight build environments
if [ -f "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
    JAVACMD="java"
    if [ -n "${JAVA_HOME:-}" ]; then
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    exec "$JAVACMD" -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
elif command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
else
    echo "ERROR: Neither gradle command nor wrapper jar found." >&2
    exit 1
fi
