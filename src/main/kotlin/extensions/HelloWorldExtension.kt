package extensions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.ui.Messages

class HelloWorldExtension : StartupActivity {

    override fun runActivity(project: Project) {
        Messages.showMessageDialog(
            project,
            "world!",
            "Hello",
            Messages.getInformationIcon()
        )
    }
}