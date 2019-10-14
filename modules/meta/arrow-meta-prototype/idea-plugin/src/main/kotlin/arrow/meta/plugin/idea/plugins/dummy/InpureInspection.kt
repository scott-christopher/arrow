package arrow.meta.plugin.idea.plugins.dummy

import arrow.meta.dsl.ide.editor.inspection.ExtendedReturnsCheck
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.idea.caches.resolve.resolveToDescriptorIfAny
import org.jetbrains.kotlin.idea.inspections.AbstractApplicabilityBasedInspection
import org.jetbrains.kotlin.idea.util.nameIdentifierTextRangeInThis
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.util.ReturnsCheck
import java.net.URL

class InpureInspection : AbstractApplicabilityBasedInspection<KtNamedFunction>(KtNamedFunction::class.java) {
  override val defaultFixText: String
    get() = "Impure"

  override fun applyTo(f: KtNamedFunction, project: Project, editor: Editor?) =
    f.addModifier(KtTokens.SUSPEND_KEYWORD)

  override fun inspectionText(f: KtNamedFunction): String = "Function should be suspended"

  override fun getDisplayName(): String = defaultFixText

  override fun getGroupDisplayName(): String = defaultFixText

  override fun loadDescription(): String? = "LocalInspections for impure code"

  override fun isApplicable(f: KtNamedFunction): Boolean =
    f.nameIdentifier != null && !f.hasModifier(KtTokens.SUSPEND_KEYWORD) &&
      f.resolveToDescriptorIfAny()?.run {
        !isSuspend && (ReturnsCheck.ReturnsUnit.check(this) || ExtendedReturnsCheck.ReturnsNothing.check(this)
          || ExtendedReturnsCheck.ReturnsNullableNothing.check(this))
      } == true

  override fun isEnabledByDefault(): Boolean = true

  override fun inspectionHighlightType(element: KtNamedFunction): ProblemHighlightType =
    ProblemHighlightType.ERROR

  override fun inspectionHighlightRangeInElement(f: KtNamedFunction): TextRange? =
    f.nameIdentifierTextRangeInThis()
}
