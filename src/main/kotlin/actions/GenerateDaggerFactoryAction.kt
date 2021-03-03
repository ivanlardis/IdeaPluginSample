package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileFilter
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.util.Query
import utils.DaggerUtils

class GenerateDaggerFactoryAction : AnAction() {

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = requireNotNull(actionEvent.project)

        val directory: VirtualFile = actionEvent.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        if (!directory.isDirectory) {
            return
        }

        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val daggerComponentAnnotation: PsiClass? = DaggerUtils.findDaggerComponentAnnotation(javaPsiFacade, project)

        if (daggerComponentAnnotation != null) {

            val directoryScope = GlobalSearchScope.filesScope(
                project,
                getFilesFromDirectory(directory)
            )

            val daggerComponents: Query<PsiClass> = AnnotatedElementsSearch.searchPsiClasses(
                daggerComponentAnnotation,
                directoryScope
            )

            WriteCommandAction.runWriteCommandAction(project) {
                daggerComponents.forEach(DaggerUtils::generateDaggerFactory)
            }
        }
    }

    private fun getFilesFromDirectory(directory: VirtualFile): List<VirtualFile> =
        mutableListOf<VirtualFile>()
            .apply {
                VfsUtilCore.iterateChildrenRecursively(directory, VirtualFileFilter.ALL, ::add)
            }
}