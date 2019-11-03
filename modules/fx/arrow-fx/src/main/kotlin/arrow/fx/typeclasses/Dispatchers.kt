package arrow.fx.typeclasses

import kotlin.coroutines.CoroutineContext

interface Dispatchers<F> {
  @Deprecated("Use computation instead, or io for blocking effects", ReplaceWith("computation()"))
  fun default(): CoroutineContext = computation()

  fun blocking(): CoroutineContext

  fun computation(): CoroutineContext
}
