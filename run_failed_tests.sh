#!/bin/bash
./gradlew :core-data:testDebugUnitTest --tests "com.rtiqa.core.data.repository.AuthRepositoryImplTest.login_correctCredentials_returnsSuccess" -i | grep -A 20 "FAILED"
