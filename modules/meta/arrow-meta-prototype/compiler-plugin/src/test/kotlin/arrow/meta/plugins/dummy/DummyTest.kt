package arrow.meta.plugins.dummy

import arrow.meta.plugin.testing.Check
import arrow.meta.plugin.testing.CompilationData
import arrow.meta.plugin.testing.CompilationStatus
import arrow.meta.plugin.testing.assertThis
import org.junit.Test

class DummyTest {
  companion object {
    val DUMMY_CODE = """fun a(): Int = 4
fun b(c: String): Int = a()
fun d(e: Int, f: String): String = f + e.toString
val k = { a: Int, b: Int -> a + b * a }""".trimIndent()

  }

  @Test
  fun `dummyTest`(): Unit =
    assertThis(
      CompilationData(
        sourceCode = DUMMY_CODE,
        compilationStatus = CompilationStatus.OK,
        checks = listOf(Check.GeneratedSourceCode(
          DUMMY_CODE
        )),
        sourceFilename = "DummyTest.kt"
      )
    )
}
