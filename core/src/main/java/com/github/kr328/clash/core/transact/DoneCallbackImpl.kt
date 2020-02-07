package com.github.kr328.clash.core.transact

import bridge.DoneCallback
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.CompletableFuture

class DoneCallbackImpl : DoneCallback, CompletableDeferred<Unit> by CompletableDeferred() {
    override fun doneWithError(e: Exception?) {
        completeExceptionally(e ?: return done())
    }

    override fun done() {
        complete(Unit)
    }
}