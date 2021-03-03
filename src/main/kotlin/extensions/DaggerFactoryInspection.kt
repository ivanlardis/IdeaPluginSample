package extensions

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import utils.DaggerUtils

class DaggerFactoryInspection : AbstractBaseJavaLocalInspectionTool() {

    override fun checkClass(
        psiClass: PsiClass,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {


        val componentDependencies = DaggerUtils.getDaggerDependencies(psiClass)
        val hasFactoryAnnotation = DaggerUtils.hasFactoryAnnotation(psiClass)

        if (componentDependencies.isNotEmpty() && !hasFactoryAnnotation) {

            val problemsHolder = ProblemsHolder(manager, psiClass.containingFile, isOnTheFly)

            problemsHolder.registerProblem(
                psiClass,
                "Нужно перевести на Dagger Factory",
                ProblemHighlightType.GENERIC_ERROR,
                DaggerFactoryFix(psiClass)
            )

            return problemsHolder.resultsArray

        }
        return ProblemDescriptor.EMPTY_ARRAY
    }
}


class DaggerFactoryFix(private val psiClass: PsiClass) : LocalQuickFixOnPsiElement(psiClass) {
    override fun getFamilyName(): String = "Rewrite to dagger Factory"

    override fun getText(): String = familyName

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {

        val containingFile = psiClass.containingFile

        WriteCommandAction.runWriteCommandAction(project, "GenerateDaggerFactory", null, {
            DaggerUtils.generateDaggerFactory(psiClass)
        }, containingFile)
    }
}