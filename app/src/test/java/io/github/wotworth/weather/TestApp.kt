package io.github.wotworth.weather

import androidx.work.testing.WorkManagerTestInitHelper

/** Tests: a test WorkManager (nothing runs unless a test drives it) and an in-memory key. */
class TestApp : App() {
    override fun onCreate() {
        WorkManagerTestInitHelper.initializeTestWorkManager(this)
        testSecret = MemorySecret()
        super.onCreate()
    }
}
