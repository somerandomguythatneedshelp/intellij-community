package org.jetbrains.kotlin.tools.projectWizard.wizard.ui.secondStep

import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.popup.PopupFactoryImpl
import com.intellij.util.ui.JBUI
import org.jetbrains.kotlin.tools.projectWizard.core.Context
import org.jetbrains.kotlin.tools.projectWizard.plugins.kotlin.KotlinPlugin
import org.jetbrains.kotlin.tools.projectWizard.plugins.kotlin.withAllSubModules
import org.jetbrains.kotlin.tools.projectWizard.settings.buildsystem.*
import org.jetbrains.kotlin.tools.projectWizard.wizard.ui.*
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import java.awt.Dimension
import javax.swing.Icon
import javax.swing.JPanel

class ModuleDependenciesComponent(
    context: Context
) : TitledComponent(context) {
    override val title: String = "Module dependencies"
    private val dependenciesList = ModuleDependenciesList()
    override val needCentering: Boolean = false
    override val maximumWidth: Int = 500

    private val toolbarDecorator: ToolbarDecorator = ToolbarDecorator.createDecorator(dependenciesList).apply {
        setToolbarPosition(ActionToolbarPosition.BOTTOM)
        setAddAction { button ->
            AddModulesPopUp.create(
                possibleDependencies(),
                dependenciesList::addDependency
            ).show(button.preferredPopupPoint!!)
        }
        setAddActionName("Add module dependency")
        setAddActionUpdater { e ->
            e.presentation.apply {
                isEnabled = possibleDependencies().isNotEmpty()
            }
            e.presentation.isEnabled
        }
        setRemoveAction {
            dependenciesList.removeSelected()
        }
        setRemoveActionName("Remove module dependency")
        setMoveDownAction(null)
        setMoveUpAction(null)
    }

    var module: Module? = null
        set(value) {
            field = value
            dependenciesList.module = value
        }

    private fun possibleDependencies(): List<Module> =
        read { KotlinPlugin::modules.settingValue }.withAllSubModules().toMutableList().apply {
            module?.let(::remove)
            removeAll(
                module
                    ?.dependencies
                    ?.filterIsInstance<ModuleReference.ByModule>()
                    ?.map(ModuleReference.ByModule::module)
                    .orEmpty()
            )
        }.filter { to ->
            ModuleDependencyType.isDependencyPossible(module!!, to)
        }

    override fun shouldBeShow(): Boolean = module?.let {
        it.dependencies.isEmpty() && possibleDependencies().isEmpty()
    } != true

    override val component: JPanel = toolbarDecorator.createPanelWithPopupHandler(dependenciesList).apply {
        preferredSize = Dimension(preferredSize.width, 200)
        addBorder(JBUI.Borders.empty(0, 3))
    }
}

private class AddModulesPopUp(
    modules: List<Module>,
    private val onChosenCallBack: (Module) -> Unit
) : BaseListPopupStep<Module>(null, modules) {
    override fun getIconFor(value: Module?): Icon? = value?.icon
    override fun getTextFor(value: Module): String = value.fullTextHtml


    override fun onChosen(selectedValue: Module?, finalChoice: Boolean): PopupStep<*>? {
        if (selectedValue != null) {
            onChosenCallBack(selectedValue)
        }
        return PopupStep.FINAL_CHOICE
    }

    companion object {
        fun create(modules: List<Module>, onChosen: (Module) -> Unit) =
            PopupFactoryImpl.getInstance().createListPopup(AddModulesPopUp(modules, onChosen))
    }
}

private class ModuleDependenciesList : AbstractSingleSelectableListWithIcon<Module>() {
    init {
        setEmptyText("There is no module dependencies")
    }

    override fun ColoredListCellRenderer<Module>.render(value: Module) {
        renderModule(value)
    }

    var module: Module? = null
        set(value) {
            field = value
            updateValues(
                value?.dependencies?.mapNotNull { it.safeAs<ModuleReference.ByModule>()?.module } ?: return
            )
            updateUI()
        }


    fun addDependency(dependency: Module) {
        model.addElement(dependency)
        module?.let { it.dependencies += ModuleReference.ByModule(dependency) }
    }

    fun removeSelected() {
        val index = selectedIndex
        model.removeElementAt(selectedIndex)
        module?.dependencies?.removeAt(index)
        if (model.size() > 0) {
            selectedIndex = index.coerceAtMost(model.size - 1)
        }
    }
}

private fun ColoredListCellRenderer<Module>.renderModule(module: Module) {
    append(module.path.asString())
    append(" ")
    module.greyText?.let {
        append(it, SimpleTextAttributes.GRAYED_ATTRIBUTES)
    }
    icon = module.icon
}