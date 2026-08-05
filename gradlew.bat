@rem ==============================================================================
@rem Gradle Wrapper Executable for Windows
@rem RTIQA Open-Source Developer Tooling
@rem ==============================================================================
@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem  Gradle startup script for Windows
@rem ##########################################################################

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set CMD_LINE_ARGS=%*

if exist "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" (
    java -jar "%DIRNAME%gradle\wrapper\gradle-wrapper.jar" %CMD_LINE_ARGS%
) else (
    gradle %CMD_LINE_ARGS%
)
