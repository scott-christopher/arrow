package arrow.meta.plugins.dummy

import arrow.meta.Meta
import arrow.meta.Plugin
import arrow.meta.invoke
import arrow.meta.quotes.Transform
import arrow.meta.quotes.classOrObject
import org.jetbrains.kotlin.lexer.KtTokens

val Meta.dummy: Plugin
  get() =
    "Dummy" {
      meta(
        crazyFunction { f ->
          print("Processing:  ${f.name}")
          f.apply {
            addModifier(KtTokens.SUSPEND_KEYWORD)
          }
        }
      )
    }
