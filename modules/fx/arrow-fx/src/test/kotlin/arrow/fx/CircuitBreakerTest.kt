package arrow.fx

import arrow.core.left
import arrow.fx.extensions.concurrent
import arrow.fx.extensions.fx
import arrow.fx.extensions.io.applicative.applicative
import arrow.fx.extensions.io.dispatchers.dispatchers
import arrow.test.UnitSpec
import arrow.test.generators.nonEmptyList
import io.kotlintest.properties.Gen
import io.kotlintest.properties.forAll
import io.kotlintest.shouldBe

class CircuitBreakerTest : UnitSpec() {
  init {
    "successes remain closed".config(enabled = false) {
      forAll(Gen.nonEmptyList(Gen.int())) { xs ->
        IO.fx {
          val cb = !CircuitBreaker.exponential<ForIO, Int>(0.7f, 0.1f, IO.concurrent(IO.dispatchers()))
          !xs.traverse(IO.applicative()) { i -> cb(just(i)) }
        }.unsafeRunSync() == xs
      }
    }

    "failures remain open" {
      IO.fx {
        val cb = !CircuitBreaker.exponential<ForIO, Int>(0.7f, 0.1f, IO.concurrent(IO.dispatchers()))
        !cb(raiseError(Exception())).attempt()
        !cb(just(1))
      }.attempt().unsafeRunSync() shouldBe CircuitBreaker.OpenCircuit.left()
    }
  }
}
