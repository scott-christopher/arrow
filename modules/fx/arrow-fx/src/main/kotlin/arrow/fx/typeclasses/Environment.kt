package arrow.fx.typeclasses

import arrow.Kind
import arrow.core.Either


/**
 * Within the environment of [F] we can run effects within [F].
 * It must be used at the edge of the world, and breaks RT in all it's run functions.
 */
interface Environment<F> {

  fun handleAsyncError(e: Throwable): Kind<F, Unit>

  /**
   * [runNonBlocking] allows you to run any [F] to its wrapped value [A].
   *
   * It's called unsafe because it immediately runs the effects wrapped in [F],
   * and thus is **not** referentially transparent.
   *
   * **NOTE** this function is intended for testing, it should never appear in your mainline production code!
   *
   * @return the resulting value
   * @see [runNonBlocking] or [runNonBlockingCancelable] that run the value as [Either].
   */
  fun <A> Kind<F, A>.runBlocking(): A

  /**
   * [runNonBlocking] allows you to run any [F] and receive the values in a callback [cb]
   * and thus **has** the ability to run `NonBlocking` but that depends on the implementation.
   * When the underlying effects/program runs blocking on the callers thread this method will run blocking.
   *
   * To start this on `NonBlocking` use `NonBlocking.shift().followedBy(program).unsafeRunAsync { }`.
   *
   * @param cb the callback that is called with the computations result represented as an [Either].
   * @see [runNonBlockingCancelable] to run in a cancellable manner.
   */
  fun <A> Kind<F, A>.runNonBlocking(cb: (Either<Throwable, A>) -> Unit): Unit

  /**
   * [runNonBlockingCancelable] allows you to run any [F] and receive the values in a callback [cb] while being cancelable.
   * It **has** the ability to run `NonBlocking` but that depends on the implementation, when the underlying
   * effects/program runs blocking on the callers thread this method will run blocking.
   *
   * To start this on `NonBlocking` use `NonBlocking.shift().followedBy(io).unsafeRunAsync { }`.
   *
   * @param cb the callback that is called with the computations result represented as an [Either].
   * @returns [Disposable] or cancel reference that cancels the running [F].
   * @see [runNonBlockingCancelable] to run in a cancellable manner.
   */
  fun <A> Kind<F, A>.runNonBlockingCancelable(cb: (Either<Throwable, A>) -> Unit): Disposable
}
