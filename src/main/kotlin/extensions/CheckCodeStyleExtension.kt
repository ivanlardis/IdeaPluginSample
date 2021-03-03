package extensions

import com.intellij.CommonBundle
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diff.DiffBundle
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.psi.codeStyle.CodeStyleSchemes
import com.intellij.ui.awt.RelativePoint
import com.jetbrains.rd.util.string.print

class CheckCodeStyleExtension : StartupActivity {

    override fun runActivity(project: Project) {
        val currentSchemeName = CodeStyleSchemes.getInstance().currentScheme.name
        print("zzz " + currentSchemeName)
        if (currentSchemeName != SBOL_CODE_STYLE_NAME) {
            showStyleWarning(project)
        }
    }

    private fun showStyleWarning(project: Project) {

        val showYesNoDialog = Messages.showYesNoDialog(
            project,
            WARNING_MESSAGE,
            WARNING_TITLE,
            WARNING_YES,
            WARNING_NO,
            AllIcons.General.WarningDialog
        );

        if (showYesNoDialog == Messages.YES) {
            openCodeStyleSetting(project)
        }
    }

    private fun openCodeStyleSetting(project: Project) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, CODE_STYLE_SETTINGS_NAME)
    }

    companion object {
        private const val SBOL_CODE_STYLE_NAME = "SbolCodeStyle"
        private const val CODE_STYLE_SETTINGS_NAME = "Code style"
        private const val WARNING_TITLE = "Нужно использовать SbolCodeStyle"
        private const val WARNING_MESSAGE = "Неправильно настроен CodeStyle"
        private const val WARNING_YES = "Открыть настройки"
        private const val WARNING_NO = "Пропустить"
    }
}