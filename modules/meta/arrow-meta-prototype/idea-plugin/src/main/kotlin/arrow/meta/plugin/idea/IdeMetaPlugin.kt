package arrow.meta.plugin.idea

import arrow.meta.MetaPlugin
import arrow.meta.dsl.ide.IdeSyntax
import arrow.meta.dsl.ide.editor.inspection.ExtendedReturnsCheck
import arrow.meta.phases.ExtensionPhase
import arrow.meta.plugin.idea.internal.registry.IdeInternalRegistry
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.idea.util.nameIdentifierTextRangeInThis
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.util.ReturnsCheck

class IdeMetaPlugin : MetaPlugin(), IdeInternalRegistry, IdeSyntax {
  override fun intercept(): List<Pair<Name, List<ExtensionPhase>>> {
    return super.intercept() + icon
  }
}

val IdeMetaPlugin.icon: Pair<Name, List<ExtensionPhase>>
  get() = Name.identifier("ImpureLineMarker") to
    meta(
      /*addIcon(
        matchOn = {psi, f ->
          println(DefaultProject.DEFAULT_BUILD_DIR_NAME)
          psi is KtThrowExpression
        }
      )*/
      addApplicableInspection(
        "Fix Me",
        KtNamedFunction::class.java,
        { element -> element.textRange },
        { element -> element.text },
        { element, project, editor ->
          element.addModifier(KtTokens.SUSPEND_KEYWORD)
        },
        { element -> element.name == "foo" }
      )
    )

val IdeMetaPlugin.testIntention: Pair<Name, List<ExtensionPhase>>
  get() = Name.identifier("TestInspection") to
    meta(
      addApplicableInspection(
        defaultFixText = "SomeFix",
        kClass = KtNamedFunction::class.java,
        inspectionText = { f: KtNamedFunction ->
          "This function is odd ${f.name}"
        },
        highlightingRange = { f: KtNamedFunction ->
          f.nameIdentifierTextRangeInThis()
        },
        isApplicable = { f: KtNamedFunction ->
          if (f.nameIdentifier == null || f.hasModifier(KtTokens.SUSPEND_KEYWORD)) false
          else
            f.resolveToDescriptorIfAny()?.run {
              !isSuspend &&
                (ReturnsCheck.ReturnsUnit.check(this) || ExtendedReturnsCheck.ReturnsNothing.check(this) || ExtendedReturnsCheck.ReturnsNullableNothing.check(this))
            } ?: false
        },
        applyTo = { element: KtNamedFunction, _, _ ->
          for (declaration in element.withExpectedActuals()) {
            declaration.addModifier(KtTokens.SUSPEND_KEYWORD)
          }
        }
      )
    )
