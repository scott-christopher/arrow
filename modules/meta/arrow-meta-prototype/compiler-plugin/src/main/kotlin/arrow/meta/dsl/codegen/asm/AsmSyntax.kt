package arrow.meta.dsl.codegen.asm

import arrow.meta.internal.Noop
import arrow.meta.phases.CompilerContext
import arrow.meta.phases.codegen.asm.ClassBuilder
import arrow.meta.phases.codegen.asm.Codegen
import org.jetbrains.kotlin.codegen.ExpressionCodegen
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.ImplementationBodyCodegen
import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunctionType
import org.jetbrains.kotlin.psi.KtModifierList
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitor
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.KtVisitor
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

/**
 * currently based on [org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension]
 */
interface AsmSyntax {
  fun codegen(
    applyFunction: CompilerContext.(receiver: StackValue, resolvedCall: ResolvedCall<*>, c: ExpressionCodegenExtension.Context) -> StackValue? = Noop.nullable4(),
    applyProperty: CompilerContext.(receiver: StackValue, resolvedCall: ResolvedCall<*>, c: ExpressionCodegenExtension.Context) -> StackValue? = Noop.nullable4(),
    generateClassSyntheticParts: CompilerContext.(codegen: ImplementationBodyCodegen) -> Unit = Noop.effect2
  ): Codegen =
    object : Codegen {
      override fun CompilerContext.applyFunction(
        receiver: StackValue,
        resolvedCall: ResolvedCall<*>,
        c: ExpressionCodegenExtension.Context
      ): StackValue? =
        applyFunction(receiver, resolvedCall, c)

      override fun CompilerContext.applyProperty(
        receiver: StackValue,
        resolvedCall: ResolvedCall<*>,
        c: ExpressionCodegenExtension.Context
      ): StackValue? =
        applyProperty(receiver, resolvedCall, c)

      override fun CompilerContext.generateClassSyntheticParts(codegen: ImplementationBodyCodegen): Unit =
        generateClassSyntheticParts(codegen)
    }

  // either with ClassBuilder or
  fun classBuilder(
    interceptClassBuilder: CompilerContext.(interceptedFactory: ClassBuilderFactory, bindingContext: BindingContext, diagnostics: DiagnosticSink) -> ClassBuilderFactory
  ): ClassBuilder =
    object : ClassBuilder {
      override fun CompilerContext.interceptClassBuilder(interceptedFactory: ClassBuilderFactory, bindingContext: BindingContext, diagnostics: DiagnosticSink): ClassBuilderFactory =
        interceptClassBuilder(interceptedFactory, bindingContext, diagnostics)
    }

  fun crazyFunction(
    f: CompilerContext.(f: KtNamedFunction) -> KtNamedFunction
  ): Codegen =
    codegen(
      applyFunction = { receiver, resolvedCall, c ->
        resolvedCall.call.callElement.accept(object : KtVisitor<StackValue, StackValue>() {
          override fun visitNamedFunction(function: KtNamedFunction, data: StackValue?): StackValue {
            print("visit function ${function}, $data")
            return super.visitNamedFunction(f(this@codegen, function), data)
          }
          override fun visitModifierList(list: KtModifierList, data: StackValue?): StackValue {
            println("visit modifier $list, $data")
            return super.visitModifierList(list, data)
          }
          override fun visitUserType(type: KtUserType, data: StackValue?): StackValue {
            println("visit UserType: $type, $data")
            return super.visitUserType(type, data)
          }
        }, receiver)

        /*files.forEach { file ->
          val g = file.accept(
            //
            //return  data)
            //}c.codegen.visitNamedFunction(, receiver)
            //super.visitNamedFunction(f(this@codegen, function))
          }, receiver)
        }
        receiver*/
      }
      /*generateClassSyntheticParts = { codegen ->
        files.map { file ->
          file.accept(object : KtVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) =
              codegen.functionCodegen.
            //super.visitNamedFunction(f(this@codegen, function))
          })
        }
      }*/
    )

  /*fun a() = {iclass: ImplementationBodyCodegen, classb: org.jetbrains.kotlin.codegen.ClassBuilder ->
    iclass
  }

  val d = {a: FirTransformer ->

  }
*/
}


object Stra1 : KtTreeVisitor<KtNamedFunction>() {
  override fun visitUserType(type: KtUserType, data: KtNamedFunction?): Void {
    print("visit ${type}, $data")
    return super.visitUserType(type, data)
  }

  override fun visitFunctionType(type: KtFunctionType, data: KtNamedFunction?): Void {
    print("visit ${type}, $data")
    return super.visitFunctionType(type, data)
  }

  override fun visitNamedFunction(function: KtNamedFunction, data: KtNamedFunction?): Void {

    return super.visitNamedFunction(function, data)
  }

  override fun visitExpression(expression: KtExpression, data: KtNamedFunction?): Void {
    print("visit ${expression}, $data")
    return super.visitExpression(expression, data)
  }
}

/*
*   val kClass: (typeName: (String) -> String) -> Remapper
  get() =
  { f ->
    object : Remapper() {
      override fun map(typeName: String?): String =
        typeName?.let { f(it) } ?: super.map(typeName)

      override fun mapType(type: String?): String {
        Type.getObjectType()

        return super.mapType(type)
      }
    }
  }

  fun element(prev: ClassVisitor, map: Remapper): ClassRemapper =
    object : ClassRemapper(prev, map) {
      override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        return super.visitMethod(access, name, desc, signature, exceptions)
      }

    }

  fun a() =
    object : ClassVisitor(ASM_VERSION) {
      override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
        return super.visitMethod(access, name, desc, signature, exceptions)
      }
    }
*/
