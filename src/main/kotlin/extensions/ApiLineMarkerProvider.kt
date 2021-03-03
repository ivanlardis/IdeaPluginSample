package extensions

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiArrayInitializerMemberValue
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.intellij.psi.impl.source.tree.java.PsiClassObjectAccessExpressionImpl
import com.intellij.psi.search.searches.ClassInheritorsSearch
import org.jetbrains.annotations.NotNull

class ApiLineMarkerProvider : RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        apiClass: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (apiClass is PsiClass) {

            val daggerComponent: PsiClass? = getInheritorDaggerClass(apiClass)
            if (daggerComponent != null) {

                val daggerProvideMethods: List<PsiMethod> = getDaggerProvideMethods(daggerComponent)

                apiClass.methods.forEach { apiMethod ->

                    val provideMethod = daggerProvideMethods.find { provideMethod ->
                        provideMethod.returnType == apiMethod.returnType
                    }

                    if (provideMethod != null) {
                        addLineMarker(result, provideMethod, apiMethod)
                    }
                }
            }
        }
    }

    private fun getInheritorDaggerClass(element: PsiClass) = ClassInheritorsSearch
        .search(element)
        .find { it.hasAnnotation(DAGGER_COMPONENT) }

    /**
    @Component(
    modules = {
    TestModule.class
    }
    ,
    dependencies = TestComponentDependencies.class
    )
    public interface TestCoreComponent
     */
    private fun getDaggerProvideMethods(daggerComponentClass: PsiClass): List<PsiMethod> {

        val daggerComponentAnnotation: PsiAnnotation? = daggerComponentClass.getAnnotation(DAGGER_COMPONENT)
        val daggerModulesAnnotation = daggerComponentAnnotation?.findAttributeValue(DAGGER_COMPONENT_MODULES)

        val daggerModules = when (daggerModulesAnnotation) {
            is PsiArrayInitializerMemberValue -> daggerModulesAnnotation.initializers.mapNotNull {
                (it as? PsiClassObjectAccessExpressionImpl)?.operand?.type
            }
            is PsiClassObjectAccessExpressionImpl -> listOf(daggerModulesAnnotation.operand.type)
            else -> listOf()
        }

        return daggerModules
            .filterIsInstance(PsiClassReferenceType::class.java)
            .mapNotNull(PsiClassReferenceType::resolve)
            .flatMap { it.methods.toList() }
    }

    private fun addLineMarker(
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
        provideMethod: PsiMethod?,
        apiMethod: PsiMethod
    ) {
        val builder = NavigationGutterIconBuilder.create(AllIcons.Gutter.ReadAccess)
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setTarget(provideMethod)
            .setTooltipText("Navigate to provide")

        result.add(builder.createLineMarkerInfo(apiMethod.nameIdentifier!!))
    }

    companion object {
        private const val DAGGER_COMPONENT = "dagger.Component"
        private const val DAGGER_COMPONENT_MODULES = "modules"
    }
}