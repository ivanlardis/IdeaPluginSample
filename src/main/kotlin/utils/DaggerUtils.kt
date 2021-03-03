package utils

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiArrayInitializerMemberValue
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiType
import com.intellij.psi.impl.source.tree.java.PsiClassObjectAccessExpressionImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiUtil

object DaggerUtils {

    private const val DAGGER_COMPONENT = "dagger.Component"
    private const val DAGGER_COMPONENT_DEPENDENCIES = "dependencies"
    private const val DAGGER_COMPONENT_FACTORY = "dagger.Component.Factory"

    fun generateDaggerFactory(daggerModuleClass: PsiClass) {

        if (hasFactoryAnnotation(daggerModuleClass)) {
            return
        }

        val dependencies = getDaggerDependencies(daggerModuleClass)

        if (dependencies.isEmpty()) {
            return
        }

        val factoryInterface = generateFactoryInterface(daggerModuleClass, dependencies)
        daggerModuleClass.add(factoryInterface)
    }

    private fun generateFactoryInterface(
        daggerModuleClass: PsiClass,
        dependencies: List<PsiType>
    ): PsiClass {
        val psiElementFactory = JavaPsiFacade.getElementFactory(daggerModuleClass.project)

        val factoryInterface = psiElementFactory.createInterface("Factory")
        PsiUtil.setModifierProperty(factoryInterface, PsiModifier.PUBLIC, false)
        factoryInterface.modifierList?.addAnnotation(DAGGER_COMPONENT_FACTORY)

        val factoryCreateMethod = generateFactoryCreateMethod(daggerModuleClass, dependencies)
        val psiCreateMethod = psiElementFactory.createMethodFromText(factoryCreateMethod, factoryInterface)

        factoryInterface.add(psiCreateMethod)
        return factoryInterface
    }

    private fun generateFactoryCreateMethod(
        daggerModuleClass: PsiClass,
        dependencies: List<PsiType>
    ): String {
        val parameters = dependencies.map { it.presentableText + " " + it.presentableText.decapitalize() }

        return """
            ${daggerModuleClass.name} create(
            ${parameters.joinToString(separator = ",\n")}
            );
            """.trim()
    }

    fun getDaggerDependencies(daggerModuleClass: PsiClass): List<PsiType> {
        val annotation: PsiAnnotation? = daggerModuleClass.getAnnotation(DAGGER_COMPONENT)
        return when (val attributeValue =
            annotation?.findAttributeValue(DAGGER_COMPONENT_DEPENDENCIES)) {
            is PsiArrayInitializerMemberValue -> attributeValue.initializers.mapNotNull {
                (it as? PsiClassObjectAccessExpressionImpl)?.operand?.type
            }
            is PsiClassObjectAccessExpressionImpl -> listOf(attributeValue.operand.type)
            else -> listOf()
        }.distinct().sortedBy { it.presentableText }
    }

    fun findDaggerComponentAnnotation(
        javaPsiFacade: JavaPsiFacade,
        project: Project
    ) = javaPsiFacade.findClass(
        DAGGER_COMPONENT,
        GlobalSearchScope.allScope(project)
    )

    fun hasFactoryAnnotation(daggerModuleClass: PsiClass) =
        daggerModuleClass.allInnerClasses.any { it.hasAnnotation(DAGGER_COMPONENT_FACTORY) }
}

