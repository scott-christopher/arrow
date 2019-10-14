package com.example.android.architecture.blueprints.todoapp.util

import android.os.Build
import android.os.Handler
import android.os.Looper
import arrow.effects.typeclasses.suspended.FxSyntax
import java.lang.reflect.Constructor
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

//taken from https://github.com/Kotlin/kotlinx.coroutines/blob/d6a5a399d1724ff56bbb285b25df071dbc98b715/ui/kotlinx-coroutines-android/src/HandlerDispatcher.kt
//and https://github.com/arrow-kt/arrow/blob/master/modules/effects/arrow-effects-io-extensions/src/main/kotlin/arrow/effects/IODispatchers.kt

private val mainHandler = Looper.getMainLooper().asHandler(async = true)

internal fun Looper.asHandler(async: Boolean): Handler {
    // Async support was added in API 16.
    if (!async || Build.VERSION.SDK_INT < 16) {
        return Handler(this)
    }

    if (Build.VERSION.SDK_INT >= 28) {
        return Handler.createAsync(this)
    }

    val constructor: Constructor<Handler>
    try {
        constructor = Handler::class.java.getDeclaredConstructor(Looper::class.java,
              Handler.Callback::class.java, Boolean::class.javaPrimitiveType)
    } catch (ignored: NoSuchMethodException) {
        // Hidden constructor absent. Fall back to non-async constructor.
        return Handler(this)
    }
    return constructor.newInstance(this, null, true)
}

private object AndroidDispatchers {
    val Main: CoroutineContext = HandlerCoroutineContext(mainHandler)

    private class HandlerCoroutineContext(val handler: Handler) : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {
        override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> =
              HandlerContinuation(handler, continuation.context.fold(continuation) { cont, element ->
                  if (element != this@HandlerCoroutineContext && element is ContinuationInterceptor) element.interceptContinuation(cont)
                  else cont
              })
    }

    private class HandlerContinuation<T>(
          val handler: Handler,
          val cont: Continuation<T>
    ) : Continuation<T> {
        override val context: CoroutineContext = cont.context

        override fun resumeWith(result: Result<T>) {
            handler.post { cont.resumeWith(result) }
        }
    }
}

val <F> FxSyntax<F>.Main: CoroutineContext
    get() = AndroidDispatchers.Main
