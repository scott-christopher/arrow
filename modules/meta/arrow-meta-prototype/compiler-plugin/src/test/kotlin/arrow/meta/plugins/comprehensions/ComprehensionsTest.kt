package arrow.meta.plugins.comprehensions

import arrow.Kind
import arrow.given
import arrow.meta.plugin.testing.CompilationData
import arrow.meta.plugin.testing.CompilationResult
import arrow.meta.plugin.testing.CompilationStatus
import arrow.meta.plugin.testing.InvocationData
import arrow.meta.plugin.testing.assertCompilation
import arrow.meta.plugin.testing.getFieldFrom
import arrow.meta.plugin.testing.invoke
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
typealias OpOf<A> = arrow.Kind<ComprehensionsTest.ForOp, A>

class ComprehensionsTest {

  companion object {
    const val IO_CLASS_4_TESTS = """
      //import kotlin.reflect.KProperty
      import arrow.given
      import arrow.Kind
      
      //metadebug
  
      /*class IO<A>(val value: A) {
  
        operator fun getValue(value: Any?, property: KProperty<*>): A = TODO()
  
        fun <B> flatMap(f: (A) -> IO<B>): IO<B> =
          f(value)
  
        companion object {
          fun <A> fx(f: IO.Companion.() -> A): IO<A> = TODO()
          fun <A> just(a: A): IO<A> = IO(a)
        }
      }*/
      
      
      
      interface Monad<F> {
          fun <A, B> Kind<F, A>.map(f: (A) -> B): Kind<F, B>
      }

      interface Service<F> {
          fun Kind<F, Int>.addOne(FF: Monad<F> = given): Kind<F, Int> =
            map { it + 1 }
      }
      """
  }

  interface Monad<F> {
    fun <A, B> Kind<F, A>.map(f: (A) -> B): Kind<F, B>
  }

  interface Service<F> {
    fun Kind<F, Int>.addOne(FF: Monad<F> = given): Kind<F, Int> = TODO()
     // map { it + 1 }
  }

  class Op<A>(vararg val el: A)

  class ForOp private constructor() {
    companion object
  }


  @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
  inline fun <A> OpOf<A>.fix(): Op<A> =
    this as Op<A>



  @Test
  fun `simple_case`() {

    val compilationResult: CompilationResult? = assertCompilation(
      CompilationData(
        sourceFileName = "SimpleCase.kt",
        sourceContent = """
          $IO_CLASS_4_TESTS
          
          object : Service<ListK>
          
          /*fun test(): IO<Int> =
            IO.fx {
              val a: Int by IO(1)
              val b: Int by IO(2)
              a + b
            }*/
          """,
        generatedFileContent = null,
        generatedClasses = arrayListOf(
          /*"SimpleCaseKt", "IO", "IO\$Companion", "SimpleCaseKt\$\$test\$lambda-1\$lambda-0\$1", "\$test\$lambda-1\$0"*/),
        compilationStatus = CompilationStatus.OK
      )
    )

    assertThat(compilationResult).isNotNull

    val resultForTest = invoke(
      InvocationData(
        classesDirectory = compilationResult?.classesDirectory,
        className = "SimpleCaseKt",
        methodName = "test"
      )
    )
    assertThat(resultForTest::class.simpleName).isEqualTo("IO")
    assertThat(getFieldFrom(resultForTest, "value")).isEqualTo(3)
  }

 /* @Test
  fun `simple_case_with_type_inference`() {

    val compilationResult: CompilationResult? = assertCompilation(
      CompilationData(
        sourceFileName = "SimpleCase.kt",
        sourceContent = """
          $IO_CLASS_4_TESTS
          
          fun test(): IO<Int> =
            IO.fx {
              val a by IO(1)
              val b by IO(2)
              a + b
            }
          """,
        generatedFileContent = null,
        generatedClasses = arrayListOf(
          "SimpleCaseKt", "IO", "IO\$Companion", "SimpleCaseKt\$\$test\$lambda-1\$lambda-0\$1", "\$test\$lambda-1\$0"),
        compilationStatus = CompilationStatus.OK
      )
    )

    assertThat(compilationResult).isNotNull

    val resultForTest = invoke(
      InvocationData(
        classesDirectory = compilationResult?.classesDirectory,
        className = "SimpleCaseKt",
        methodName = "test"
      )
    )
    assertThat(resultForTest::class.simpleName).isEqualTo("IO")

    val field = getFieldFrom(resultForTest, "value")
    assertThat(field).isEqualTo(3)
    assertThat(field::class).isEqualTo(Int::class)
  }

  @Test
  fun `nested_case_with_type_inference`() {

    val compilationResult: CompilationResult? = assertCompilation(
      CompilationData(
        sourceFileName = "SimpleCase.kt",
        sourceContent = """
          $IO_CLASS_4_TESTS
          
          fun test(): IO<Int> =
            IO.fx {
              val a by IO.fx {
                val a by IO(1)
                val b by IO(2)
                a + b
              }
              val b by IO.fx {
                val a by IO(3)
                val b by IO(4)
                a + b
              }
              a + b
            }
          """,
        generatedFileContent = null,
        generatedClasses = arrayListOf(
          "SimpleCaseKt", "IO", "IO\$Companion", "SimpleCaseKt\$\$test\$lambda-1\$lambda-0\$2",
          "SimpleCaseKt\$\$test\$lambda-5\$lambda-3\$4", "SimpleCaseKt\$\$test\$lambda-5\$lambda-3\$lambda-2\$3",
          "SimpleCaseKt\$\$test\$lambda-5\$lambda-4\$5", "\$test\$lambda-1\$0", "\$test\$lambda-5\$1"),
        compilationStatus = CompilationStatus.OK
      )
    )

    assertThat(compilationResult).isNotNull

    val resultForTest = invoke(
      InvocationData(
        classesDirectory = compilationResult?.classesDirectory,
        className = "SimpleCaseKt",
        methodName = "test"
      )
    )
    assertThat(resultForTest::class.simpleName).isEqualTo("IO")

    val field = getFieldFrom(resultForTest, "value")
    assertThat(field).isEqualTo(10)
    assertThat(field::class).isEqualTo(Int::class)
  }*/
}

interface OpMonad: ComprehensionsTest.Monad<ComprehensionsTest.ForOp> {
  override fun <A, B> Kind<ComprehensionsTest.ForOp, A>.map(f: (A) -> B): Kind<ComprehensionsTest.ForOp, B> =

}
