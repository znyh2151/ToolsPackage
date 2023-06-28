package com.tools.utils

import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

object ThreadManager {
    private var threadPoolExecutor = Executors.newFixedThreadPool(1) as ThreadPoolExecutor

    fun executeTask(runnable: () -> Unit) {
        threadPoolExecutor.execute { runnable.invoke() }
    }
}

