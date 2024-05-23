/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;


import de.docware.apps.etk.base.config.db.datatypes.enumtypes.EnumDataType;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.validation.iPartsEditAssemblyListValidationOverlappingEntriesForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.framework.modules.config.db.datatypes.DatatypeHtmlResult;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Dialog zur Anzeige vom Ergebnis der Freigabe(vor)prüfung.
 */
public class iPartsAuthorOrderPreReleaseCheckResults extends SimpleMasterDataSearchResultGrid {

    private static String TABLE = "PRERELEASERESULTTABLE";
    private static String FIELD_NAME = "!!TU-Benennung";
    private static String FIELD_NUMBER = "!!TU-Nummer";
    private static String FIELD_RESULT = "!!Ergebnis";

    private Map<String, iPartsEditAssemblyListValidationOverlappingEntriesForm> validationForms;
    private iPartsEditBaseValidationForm.ValidationResult validationResult;
    private EnumDataType enumTypeForResult;
    private GuiButtonOnPanel openAllModulesButton;
    private GuiButtonOnPanel forceReleaseButton;

    /**
     * Anzeige der Ergebnisse der Freigabevorprüfung
     * <p>
     * Zeigt einen Dialog an, der pro geprüftem Modul die Modulnummer, Benennung und das Ergebnis der Prüfung als Enum ausgibt
     * Doppelklick auf einen Eintrag öffnet das entsprechende {@link iPartsEditAssemblyListValidationOverlappingEntriesForm}.
     * Über das Kontextmenü kann jedes Modul im Edit geöffnet werden, und über einen zusätzlichen Button werden alle
     * fehlerhaften Module im Edit geöffnet
     *
     * @param dataConnector
     * @param parentForm
     * @param validationForms Die zuvor für die Prüfung verwendeten {@link iPartsEditAssemblyListValidationOverlappingEntriesForm}s
     */
    public static void showPrereleaseCheckResults(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                  Map<AssemblyId, iPartsEditAssemblyListValidationOverlappingEntriesForm> validationForms) {
        iPartsAuthorOrderPreReleaseCheckResults form = new iPartsAuthorOrderPreReleaseCheckResults(dataConnector, parentForm,
                                                                                                   null);
        form.setDisplayFields();
        form.setValidationForms(validationForms, dataConnector.getProject());
        form.setTitle("!!Ergebnis der Freigabevorprüfung");
        form.enableCustomButtons();
        form.showModal(parentForm.getRootParentWindow());
    }

    /**
     * Anzeige der Ergebnisse der Freigabeprüfung
     * <p>
     * Dieser Dialog wird angezeigt wenn ein Statusübergang für einen Autoren-Auftrag von "in Arbeit" nach "QA" oder von
     * "QA" nach "freigegeben" stattfinden soll.
     * Hat eines der bearbeiteten Module einen Fehler, dann darf der Statusübergang nicht stattfinden.
     * Der Dialog wird nur angezeigt, wenn es Fehler oder Warnungen gibt.
     * Anzeige wie bei der Freigabevorprüfung. Zusätzlich gibt es hier einen weiteren Button mit dem man den Autoren-Auftrag
     * trotz Warnungen oder Fehlern weiterschieben kann. Dieser Button ist nur aktiv, wenn es ausschließlich Warnungen gibt
     * oder der wenn es Fehler gibt und der Benutzer das spezielle Recht {@see #iPartsRight.IGNORE_ERRORS_IN_AO_RELEASE_CHECKS} hat
     *
     * @param dataConnector
     * @param parentForm
     * @param validationForms     Die zuvor für die Prüfung verwendeten {@link iPartsEditAssemblyListValidationOverlappingEntriesForm}s
     * @param isFinalReleaseCheck <code>true</code> wenn es sich um den Stausübergang nach "freigegeben" handelt
     */
    public static ModalResult showReleaseCheckResults(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                      Map<AssemblyId, iPartsEditAssemblyListValidationOverlappingEntriesForm> validationForms,
                                                      boolean isFinalReleaseCheck) {
        iPartsAuthorOrderPreReleaseCheckResults form = new iPartsAuthorOrderPreReleaseCheckResults(dataConnector, parentForm,
                                                                                                   null);
        form.setDisplayFields();
        form.setValidationForms(validationForms, dataConnector.getProject());
        form.setTitle(isFinalReleaseCheck ? "!!Ergebnis der Freigabeprüfung" : "!!Ergebnis der Freigabevorprüfung");
        form.addForceReleaseButton(isFinalReleaseCheck);
        form.enableCustomButtons();
        return form.showModal(parentForm.getRootParentWindow());
    }

    /**
     * Einfache Tabelle zur Anzeige der Freigabevorprüfungs-/Freigabeprüfungs-Ergebnisse
     *
     * @param dataConnector
     * @param parentForm
     * @param onEditChangeRecordEvent
     */
    public iPartsAuthorOrderPreReleaseCheckResults(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                   OnEditChangeRecordEvent onEditChangeRecordEvent) {
        super(dataConnector, parentForm, TABLE, onEditChangeRecordEvent);

        enumTypeForResult = new EnumDataType(iPartsPlugin.TABLE_FOR_EVALUATION_RESULTS,
                                             iPartsDataVirtualFieldsDefinition.DA_PICTURE_AND_TU_QUALITY_CHECK);

        setWindowTitle("!!Qualitätsprüfungen");
    }

    @Override
    protected void postCreateGui() {
        super.postCreateGui();
        setEditAllowed(false);
        showToolbar(false);
        showSearchFields(false);
        showSelectCount(false);
        setMultiSelect(false);
        getButtonPanel().setDialogStyle(GuiButtonPanel.DialogStyle.CLOSE);
        openAllModulesButton = getButtonPanel().addCustomButton("!!Alle fehlerhaften TUs bearbeiten", ModalResult.CANCEL);
        openAllModulesButton.setTooltip("!!Öffnet alle TUs mit Fehlern und Warnungen für die Bearbeitung");
        openAllModulesButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                DwList<String> modulesForEdit = new DwList<>();
                for (Map.Entry<String, iPartsEditAssemblyListValidationOverlappingEntriesForm> entry : validationForms.entrySet()) {
                    iPartsEditAssemblyListValidationOverlappingEntriesForm validationForm = entry.getValue();
                    if (validationForm != null) {
                        // Alle Forms aufsammeln, deren Ergebnis nicht OK ist
                        if (validationForm.getTotalValidationResult() != iPartsEditBaseValidationForm.ValidationResult.OK) {
                            modulesForEdit.add(entry.getKey());
                        }
                    }
                }
                openModulesForEdit(modulesForEdit);
            }
        });
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        GuiMenuItem menuEntry = toolbarHelper.createContextMenuEntry(EditToolbarButtonAlias.EDIT_ASSEMBLY, getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                DBDataObjectAttributes selectedAttributes = getSelectedAttributes();
                if (selectedAttributes != null) {
                    DwList<String> modules = new DwList<>();
                    String kVari = selectedAttributes.getFieldValue(FIELD_NUMBER);
                    if (StrUtils.isValid(kVari)) {
                        modules.add(kVari);
                    }
                    openModulesForEdit(modules);
                }
            }
        });
        contextMenu.addChild(menuEntry);

        menuEntry = toolbarHelper.createContextMenuEntry(EditToolbarButtonAlias.EDIT_VALIDATE_ASSEMBLY, getUITranslationHandler(), new EventListener(Event.MENU_ITEM_EVENT) {
            @Override
            public void fire(Event event) {
                doEditOrView(event);
            }
        });
        contextMenu.addChild(menuEntry);
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);
    }


    @Override
    protected void doEditOrView(Event event) {
        DBDataObjectAttributes selectedAttributes = getSelectedAttributes();
        if (selectedAttributes != null) {
            String kVari = selectedAttributes.getFieldValue(FIELD_NUMBER);
            iPartsEditAssemblyListValidationOverlappingEntriesForm form = validationForms.get(kVari);
            if (form != null) {
                // Prüfung auf PSK-Recht
                EtkDataAssembly assembly = form.getConnector().getCurrentAssembly();
                if (assembly instanceof iPartsDataAssembly) {
                    if (!((iPartsDataAssembly)assembly).checkPSKInSession(false, true)) {
                        return;
                    }
                }

                form.setParentForm(this);
                form.updateValidationGUI(false); // Jetzt die GUI aufbauen, da vorher nur die Validierung stattgefunden hat
                form.markFirstErrorOrWarning();
                form.showModal(false);
            }
        }
    }

    @Override
    protected String getVisualValueOfFieldValue(String tableName, String fieldName, DBDataObjectAttribute fieldValue, boolean isMultiLanguage) {
        if (tableName.equals(TABLE) && fieldName.equalsIgnoreCase(FIELD_RESULT)) {
            if (enumTypeForResult != null) {
                DatatypeHtmlResult datatypeHtmlResult = enumTypeForResult.asHtml(getProject(), fieldValue, getProject().getDBLanguage(),
                                                                                 false, true);
                return datatypeHtmlResult.getStringResult();
            }
        }
        return super.getVisualValueOfFieldValue(tableName, fieldName, fieldValue, isMultiLanguage);
    }

    private void enableCustomButtons() {
        openAllModulesButton.setEnabled(isError() || isWarning());
        if (forceReleaseButton != null) {
            // Schließen-Button anpassen
            GuiButton closeButton = getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CLOSE);
            closeButton.setText("!!Abbrechen");
            closeButton.setTooltip("!!Statusübergang nicht durchführen");

            boolean userHasRightToForceRelease = iPartsRight.IGNORE_ERRORS_IN_AO_RELEASE_CHECKS.checkRightInSession();
            forceReleaseButton.setEnabled(isWarning() || (isError() && userHasRightToForceRelease));
            if (isError() && !userHasRightToForceRelease) {
                getButtonPanel().setTooltip(translate("!!Fehlende Berechtigung für \"%1\"", translate(forceReleaseButton.getText())));
            }
        }
    }

    private void addForceReleaseButton(boolean isFinalReleaseCheck) {
        String message;
        if (isFinalReleaseCheck) {
            message = "!!Autoren-Auftrag trotzdem freigeben";
        } else {
            message = "!!Autoren-Auftrag trotzdem weiterleiten";
        }
        forceReleaseButton = getButtonPanel().addCustomButton(message, ModalResult.IGNORE);
    }

    private void setDisplayFields() {
        List<String> viewerLanguages = getConnector().getConfig().getViewerLanguages();
        EtkDisplayFields displayFields = new EtkDisplayFields();

        EtkDisplayField field = new EtkDisplayField(TABLE, FIELD_NUMBER, false, false);
        field.setText(new EtkMultiSprache(FIELD_NUMBER, viewerLanguages));
        field.setDefaultText(false);
        displayFields.addFeld(field);

        field = new EtkDisplayField(TABLE, FIELD_NAME, true, false);
        field.setText(new EtkMultiSprache(FIELD_NAME, viewerLanguages));
        field.setDefaultText(false);
        displayFields.addFeld(field);

        field = new EtkDisplayField(TABLE, FIELD_RESULT, false, false);
        field.setText(new EtkMultiSprache(FIELD_RESULT, viewerLanguages));
        field.setDefaultText(false);
        displayFields.addFeld(field);

        setDisplayResultFields(displayFields);
    }

    private void openModulesForEdit(List<String> moduleNumbers) {
        if ((moduleNumbers != null) && !moduleNumbers.isEmpty()) {
            try {
                GuiWindow.showWaitCursorForRootWindow(true);
                List<AssemblyId> assemblyIds = new DwList<>();
                for (String moduleNumber : moduleNumbers) {
                    assemblyIds.add(new iPartsAssemblyId(moduleNumber, ""));
                }
                EditModuleForm.editOrViewModules(assemblyIds, getConnector().getMainWindow());
                closeWithModalResult(ModalResult.NONE);
            } finally {
                GuiWindow.showWaitCursorForRootWindow(false);
            }
        }
    }

    private void setValidationForms(Map<AssemblyId, iPartsEditAssemblyListValidationOverlappingEntriesForm> validationForms,
                                    EtkProject project) {
        this.validationForms = new LinkedHashMap<>();
        validationResult = iPartsEditBaseValidationForm.ValidationResult.OK;
        for (Map.Entry<AssemblyId, iPartsEditAssemblyListValidationOverlappingEntriesForm> entry : validationForms.entrySet()) {
            DBDataObjectAttributes attributes = new DBDataObjectAttributes();
            AssemblyId assemblyId = entry.getKey();
            attributes.addField(FIELD_NUMBER, assemblyId.getKVari(), true, DBActionOrigin.FROM_DB);

            DBDataObjectAttribute att = new DBDataObjectAttribute(FIELD_NAME, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, true);
            att.setValueAsMultiLanguage(iPartsDataAssembly.getAssemblyText(project, assemblyId), DBActionOrigin.FROM_DB);
            attributes.addField(att, DBActionOrigin.FROM_DB);

            iPartsEditBaseValidationForm.ValidationResult result = entry.getValue().getTotalValidationResult();
            attributes.addField(FIELD_RESULT, result.getDbValue(), true, DBActionOrigin.FROM_DB);
            addAttributesToGrid(attributes);

            this.validationForms.put(assemblyId.getKVari(), entry.getValue());

            if (result == iPartsEditBaseValidationForm.ValidationResult.ERROR) {
                validationResult = iPartsEditBaseValidationForm.ValidationResult.ERROR;
            } else if (result == iPartsEditBaseValidationForm.ValidationResult.WARNING) {
                if (validationResult != iPartsEditBaseValidationForm.ValidationResult.ERROR) {
                    validationResult = iPartsEditBaseValidationForm.ValidationResult.WARNING;
                }
            }
        }
    }

    private boolean isError() {
        return validationResult == iPartsEditBaseValidationForm.ValidationResult.ERROR;
    }

    private boolean isWarning() {
        return validationResult == iPartsEditBaseValidationForm.ValidationResult.WARNING;
    }
}