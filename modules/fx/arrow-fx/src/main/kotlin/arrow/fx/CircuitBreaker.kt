package arrow.fx

import arrow.Kind
import arrow.core.NonEmptyList
import arrow.core.nel
import arrow.fx.typeclasses.Concurrent
import arrow.fx.typeclasses.ExitCase

private fun Boolean.toInt(): Int = if (this) 1 else 0
private fun Boolean.toFloat(): Float = if (this) 1f else 0f

class CircuitBreaker<F, A> private constructor(private val ref: Ref<F, CircuitState>, private val CF: Concurrent<F>) : Concurrent<F> by CF {

  operator fun invoke(effect: Kind<F, A>): Kind<F, A> = CF.fx.concurrent {
    val state = !ref.get()
    if (state.isOpen()) {
      !ref.update { it.append(false) }
      !raiseError<A>(OpenCircuit)
    } else {
      val x = effect.guaranteeCase { result ->
        when (result) {
          is ExitCase.Completed -> ref.update { it.append(false) }
          is ExitCase.Canceled -> unit()
          is ExitCase.Error -> ref.update { it.append(true) }
        }
      }
      !x
    }
  }

  companion object {
    fun <F, A> moving(windowSize: Int, errorThreshold: Float, CF: Concurrent<F>): Kind<F, CircuitBreaker<F, A>> = CF.run {
      if (windowSize < 1) {
        raiseError(IllegalArgumentException("windowSize must be greater than 0"))
      } else {
        Ref<CircuitState>(Moving.Empty(windowSize, errorThreshold)).map {
          CircuitBreaker<F, A>(it, this)
        }
      }
    }

    fun <F, A> exponential(weight: Float, errorThreshold: Float, CF: Concurrent<F>): Kind<F, CircuitBreaker<F, A>> = CF.run {
      Ref<CircuitState>(Exponential.Empty(weight, errorThreshold)).map {
        CircuitBreaker<F, A>(it, this)
      }
    }
  }

  interface CircuitState {
    fun isOpen(): Boolean
    fun append(error: Boolean): CircuitState
  }

  private sealed class Exponential : CircuitState {
    data class NonEmpty(val weight: Float, val errorThreshold: Float, val errorRate: Float) : Exponential() {
      override fun isOpen(): Boolean = errorRate > errorThreshold

      override fun append(error: Boolean): CircuitState =
        this.copy(errorRate = weight * error.toFloat() + (1 - weight) * errorRate)
    }

    data class Empty(val weight: Float, val errorThreshold: Float) : Exponential() {
      override fun isOpen(): Boolean = false

      override fun append(error: Boolean): CircuitState = NonEmpty(weight, errorThreshold, error.toFloat())
    }
  }

  private sealed class Moving : CircuitState {

    data class Full(val errorThreshold: Float, val errorCount: Int, val errors: NonEmptyList<Boolean>) : Moving() {
      override fun isOpen(): Boolean = errorCount.toFloat() / errors.size > errorThreshold

      override fun append(error: Boolean): CircuitState =
        this.copy(errorCount = errorCount + error.toInt() - errors.head.toInt(), errors = NonEmptyList(error, errors.tail))
    }

    data class Partial(val windowSize: Int, val errorThreshold: Float, val errorCount: Int, val errors: NonEmptyList<Boolean>) : Moving() {
      override fun isOpen(): Boolean = errorCount.toFloat() / errors.size > errorThreshold

      override fun append(error: Boolean): CircuitState {
        val newCount = error.toInt() + errorCount
        return if (errors.size < windowSize) this.copy(errorCount = newCount, errors = error.nel() + errors)
        else Full(errorThreshold, newCount, error.nel() + errors)
      }
    }

    data class Empty(val windowSize: Int, val errorThreshold: Float) : Moving() {
      override fun isOpen(): Boolean = false

      override fun append(error: Boolean): CircuitState = Partial(windowSize, errorThreshold, error.toInt(), NonEmptyList(error, emptyList()))
    }
  }

  object OpenCircuit : Throwable()
}
