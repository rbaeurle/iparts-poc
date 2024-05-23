package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.AbstractJavaViewerMainFormContainer;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditModuleForm;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.VarParam;

import java.util.List;
import java.util.Set;

/**
 * Helper für das Laden von Modulen im Editor
 */
public class iPartsLoadEditModuleHelper {

    /**
     * Lädt die übergebenen Module {@param selectedModulList} im Editor und wechselt am Ende zur EditModuleForm
     * Falls {@param loadReadOnly} true ist, werden die Module readOnly geöffnet
     *
     * @param dataConnector
     * @param selectedModulList
     * @param loadReadOnly
     * @param progressForm
     */
    public static void doLoadModules(AbstractJavaViewerFormIConnector dataConnector,
                                     Set<String> selectedModulList,
                                     boolean loadReadOnly,
                                     EtkMessageLogForm progressForm) {
        doLoadModules(dataConnector, selectedModulList, null, loadReadOnly, true, progressForm);
    }

    /**
     * Lädt die übergebenen Module {@param selectedModulList} im Editor
     * Falls {@param loadReadOnly} true ist, werden die Module readOnly geöffnet
     * Falls {@param changeFocusToEdit} true ist, wird am Ende zur EditModuleForm gewechselt
     *
     * @param dataConnector
     * @param selectedModulList
     * @param loadReadOnly
     * @param changeFocusToEdit
     * @param progressForm
     */
    public static void doLoadModules(AbstractJavaViewerFormIConnector dataConnector,
                                     Set<String> selectedModulList,
                                     boolean loadReadOnly,
                                     boolean changeFocusToEdit,
                                     EtkMessageLogForm progressForm) {
        doLoadModules(dataConnector, selectedModulList, null, loadReadOnly, changeFocusToEdit, progressForm);
    }

    /**
     * Lädt die übergebenen Module {@param selectedModulList} im Editor
     * Falls {@param loadReadOnly} true ist, werden die Module readOnly geöffnet
     * Falls {@param changeFocusToEdit} true ist, wird am Ende zur EditModuleForm gewechselt
     * Falls {@param kemNo} gesetzt ist, wird sie an den Editor weitergegeben
     *
     * @param dataConnector
     * @param selectedModulList
     * @param additionalTextForHeader
     * @param loadReadOnly
     * @param changeFocusToEdit
     * @param progressForm
     */
    public static void doLoadModules(AbstractJavaViewerFormIConnector dataConnector,
                                     Set<String> selectedModulList, String additionalTextForHeader,
                                     boolean loadReadOnly,
                                     boolean changeFocusToEdit,
                                     EtkMessageLogForm progressForm) {
        List<AbstractJavaViewerMainFormContainer> editModuleForms = dataConnector.getMainWindow().getFormsFromClass(EditModuleForm.class);
        if (!editModuleForms.isEmpty()) {
            final EditModuleForm editModuleForm = (EditModuleForm)editModuleForms.get(0);
            Session.invokeThreadSafeInSession(() -> GuiWindow.showWaitCursorForRootWindow(true));
            try {
                int count = 1;
                int total = selectedModulList.size();
                final VarParam<Boolean> oneModuleLoaded = new VarParam<>(false);
                for (final String module : selectedModulList) {
                    if (total > 1) {
                        if (progressForm != null) {
                            progressForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Lade Modul \"%1\" (%2 von %3)",
                                                                                                  module,
                                                                                                  String.valueOf(count), String.valueOf(total)));
                        }
                    }

                    Runnable loadRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (editModuleForm.loadModule(module, null, loadReadOnly, true, additionalTextForHeader)) {
                                oneModuleLoaded.setValue(true);
                            }
                        }
                    };
                    if (J2EEHandler.isJ2EE()) {
                        Session.invokeThreadSafeInSession(loadRunnable);
                    } else {
                        loadRunnable.run();
                    }

                    count++;
                }

                // Wenn alle (aber mindestens eines) Module geladen sind und es soll der Focus umgestellt werden =>
                // am Ende zur EditModuleForm wechseln
                if (oneModuleLoaded.getValue() && changeFocusToEdit) {
                    Session.invokeThreadSafeInSession(() -> dataConnector.getMainWindow().displayForm(editModuleForm));
                }
            } finally {
                Session.invokeThreadSafeInSession(() -> GuiWindow.showWaitCursorForRootWindow(false));
            }
        }
    }
}
