/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.filter.forms.common.UserDataSessionManager;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.SimpleDoubleListSelectForm;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.forms.toolbar.ToolbarButtonAlias;
import de.docware.apps.etk.base.project.events.AssemblyListFormConfigurationChangedEvent;
import de.docware.apps.etk.viewer.usersettings.DisplayFieldSettings;
import de.docware.apps.etk.viewer.usersettings.MultiDisplayFieldSetting;
import de.docware.apps.etk.viewer.usersettings.MultiDisplayFieldSettings;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.misc.DWOrientation;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.controls.toolbar.ToolButtonStyle;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.inputdialog.InputDialog;
import de.docware.framework.modules.gui.dialogs.inputdialog.InputDialogValidator;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.resources.FrameworkConstantImage;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;

import java.util.LinkedList;
import java.util.List;

/**
 * Erweitertes Formular zum Bearbeiten von {@link EtkDisplayField}s mit Namensvergabe bei Persistenter Konfiguration (z.B. Konfiguration der sichtbaren Stücklistenfelder)
 */
public class iPartsSimpleDoubleListSelectForm extends SimpleDoubleListSelectForm {

    public static boolean showDoubleListForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                             String ebeneAndSourceName, String extraConfigKeyForDisplayFields, List<EtkDisplayField> localDisplayFieldList,
                                             boolean arefixedColumnsSupported, int localFixedColumnsCount) {
        iPartsSimpleDoubleListSelectForm dlg = new iPartsSimpleDoubleListSelectForm(dataConnector, parentForm);
        dlg.setTitle("!!Benutzerdefinierte Konfiguration");
        dlg.setEbeneAndSourceName(ebeneAndSourceName, extraConfigKeyForDisplayFields);
        dlg.setLocalDisplayFields(localDisplayFieldList, arefixedColumnsSupported, localFixedColumnsCount);
        dlg.load();
        ModalResult result = dlg.showModal();
        return result != ModalResult.CANCEL;
    }

    protected GuiPanel multiConfigPanel;
    protected ToolbarButtonMenuHelper toolbarHelperMulti;
    protected RComboBox<MultiDisplayFieldSetting> comboBoxMulti;
    protected String originalActiveMultiConfigName;
    private MultiDisplayFieldSettings currentMultiDisplayFieldSettings;

    /**
     * Erzeugt eine Instanz von SimpleDoubleListSelectForm.
     * Den $$internalCreateGui$$() Aufruf nicht �ndern!
     *
     * @param dataConnector
     * @param parentForm
     */
    public iPartsSimpleDoubleListSelectForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        createMultiConfigPanel();
    }

    private void createMultiConfigPanel() {
        multiConfigPanel = new GuiPanel();
        multiConfigPanel.setLayout(new LayoutBorder());
        multiConfigPanel.setPadding(8, 8, 0, 0);
        GuiToolbar toolbar = new GuiToolbar(ToolButtonStyle.SMALL);
        toolbar.setName("toolbar");
        toolbar.registerTranslationHandler(getUITranslationHandler());
        toolbar.setBackgroundImage(new FrameworkConstantImage("imgDesignToolbarBigBackground"));
        toolbar.setButtonOrientation(DWOrientation.HORIZONTAL);
        toolbar.setPaddingTop(0);
        toolbar.setPaddingBottom(0);
        ConstraintsBorder toolbarForChangeConstraints = new ConstraintsBorder();
        toolbarForChangeConstraints.setPosition(ConstraintsBorder.POSITION_CENTER);
        toolbar.setConstraints(toolbarForChangeConstraints);
        multiConfigPanel.addChildBorderWest(toolbar);
        toolbarHelperMulti = new ToolbarButtonMenuHelper(getConnector(), toolbar);
        toolbarHelperMulti.addToolbarButton(ToolbarButtonAlias.EDIT_CONFIG_NEW, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doNewMultiName();
            }
        });
        toolbarHelperMulti.addToolbarButton(ToolbarButtonAlias.EDIT_CONFIG_WORK, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doEditMultiName();
            }
        });
        toolbarHelperMulti.addToolbarButton(ToolbarButtonAlias.EDIT_CONFIG_DELETE, new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doDeleteMultiName();
            }
        });
        comboBoxMulti = new RComboBox<>();
        comboBoxMulti.setConstraints(new ConstraintsBorder());
        toolbar.setName("comboboxmulti");
        comboBoxMulti.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                changeMultiSetting(comboBoxMulti.getSelectedItem(), true);
            }
        });
        multiConfigPanel.addChildBorderCenter(comboBoxMulti);
        multiConfigPanel.setMaximumHeight(26);
        getPanelForExtraElements().addChildBorderCenter(multiConfigPanel);
        getPanelForExtraElements().setMinimumHeight(34);

        doEnableMultiButtons();
    }

    private void changeMultiSetting(String name, boolean saveData) {
        MultiDisplayFieldSettings multiDisplayFieldSettings = getMultiDisplayFieldSettings();
        MultiDisplayFieldSetting multiDisplayFieldSetting = multiDisplayFieldSettings.setActiveDisplayFieldSetting(getEbeneAndSourceName(), name);
        if (multiDisplayFieldSetting != null) {
            // Anzeige aktualisieren
            DisplayFieldSettings displayFieldSettings = multiDisplayFieldSetting.getDisplayFieldSetting();
            // Anzeige anpassen
            fillItems(displayFieldSettings);
            if (saveData) {
                // Speichern und Synchronisation mit DisplayFieldSettings
                save(false);
            }
        }
        doEnableMultiButtons();
        doEnableButtons();
    }

    @Override
    protected boolean doubleClickEnabled() {
        if (!isUserSettings()) {
            return true;
        }

        boolean hasElements = comboBoxMulti.getItemCount() > 0;
        boolean isSelected = comboBoxMulti.getSelectedItem() != null;
        return hasElements && isSelected;
    }

    private MultiDisplayFieldSettings getMultiDisplayFieldSettings() {
        if (currentMultiDisplayFieldSettings == null) {
            currentMultiDisplayFieldSettings = getProject().getUserSettings().getMultiDisplayFieldSettings();
            // zur Sicherheit
            if (currentMultiDisplayFieldSettings == null) {
                currentMultiDisplayFieldSettings = new MultiDisplayFieldSettings(getProject());
            }
        }
        return currentMultiDisplayFieldSettings;
    }

    @Override
    public void setEbeneAndSourceName(String ebeneAndSourceName, String extraConfigKeyForDisplayFields) {
        super.setEbeneAndSourceName(ebeneAndSourceName, extraConfigKeyForDisplayFields);
        MultiDisplayFieldSettings multiDisplayFieldSettings = getMultiDisplayFieldSettings();
        originalActiveMultiConfigName = multiDisplayFieldSettings.getActiveDisplayFieldSettingName(getEbeneAndSourceName());
    }

    private InputDialogValidator createInputValidator(List<String> usedNames) {
        return new InputDialogValidator() {
            @Override
            public String validateInput(String input) {
                String searchString = input.trim();
                if (searchString.isEmpty()) {
                    return "!!Der Name darf nicht leer sein.";
                }
                if (usedNames.contains(searchString)) {
                    return "!!Dieser Name wird bereits verwendet.";
                }
                return "";
            }
        };
    }

    private void doNewMultiName() {
        MultiDisplayFieldSettings multiDisplayFieldSettings = getMultiDisplayFieldSettings();
        List<String> usedNames = multiDisplayFieldSettings.getUsedMultiConfigNames(getEbeneAndSourceName());

        String multiName = InputDialog.show("!!Neue Konfiguration", "!!Neuer Konfigurationsname", "", false,
                                            createInputValidator(usedNames));
        if (StrUtils.isValid(multiName)) {
            // Darstellung resetten
            resetButton.doClick();

            multiDisplayFieldSettings.createAndAddMultiDisplayFieldSetting(getEbeneAndSourceName(), multiName, getFixedColumnsCount(), true);
            // ComboBox Inhalt anpassen
            fillMultiName(getEbeneAndSourceName(), multiDisplayFieldSettings);
            // Speichern und Synchronisation mit DisplayFieldSettings
            save(false);

            doEnableButtons();
        }
    }

    private void doEditMultiName() {
        MultiDisplayFieldSettings multiDisplayFieldSettings = getMultiDisplayFieldSettings();
        List<String> usedNames = multiDisplayFieldSettings.getUsedMultiConfigNames(getEbeneAndSourceName());
        String selectedItem = comboBoxMulti.getSelectedItem();
        if (StrUtils.isValid(selectedItem)) {
            String multiName = InputDialog.show("!!Konfigurationsname ändern", "!!Neuer Konfigurationsname", selectedItem, false,
                                                createInputValidator(usedNames));
            if (StrUtils.isValid(multiName) && !multiName.equals(selectedItem)) {
                // Namen einer MultiConfig ändern
                if (multiDisplayFieldSettings.changeMultiConfigName(getEbeneAndSourceName(), selectedItem, multiName)) {
                    // ComboBox Inhalt anpassen
                    fillMultiName(getEbeneAndSourceName(), multiDisplayFieldSettings);
                } else {
                    MessageDialog.showWarning("!!Fehler bei der Änderungen des Konfigurationsnamens.");
                }
            }
        }
    }

    private void doDeleteMultiName() {
        String selectedItem = comboBoxMulti.getSelectedItem();
        if (StrUtils.isValid(selectedItem)) {
            String message = TranslationHandler.translate("!!Konfiguration \"%1\" wirklich löschen?", selectedItem);
            if (ModalResult.YES == MessageDialog.showYesNo(message, "!!Konfiguration löschen")) {
                // Löschen einer MultiConfig
                MultiDisplayFieldSettings multiDisplayFieldSettings = getMultiDisplayFieldSettings();
                if (multiDisplayFieldSettings.removeMultiDisplayFieldSetting(getEbeneAndSourceName(), selectedItem)) {
                    // nach dem Löschen: die erste MultiKonfig wird aktiv (falls sie existiert)
                    if (!multiDisplayFieldSettings.isEmpty()) {
                        LinkedList<MultiDisplayFieldSetting> multiDisplayFieldSettingList = multiDisplayFieldSettings.get(getEbeneAndSourceName());
                        if (Utils.isValid(multiDisplayFieldSettingList)) {
                            String name = multiDisplayFieldSettingList.get(0).getMultiDisplayFieldName();
                            changeMultiSetting(name, false);
                        } else {
                            // war der letzte Eintrag
                            resetButton.doClick();
                        }
                    }

                    // ComboBox Inhalt anpassen
                    fillMultiName(getEbeneAndSourceName(), multiDisplayFieldSettings);
                    // Speichern und Synchronisation mit DisplayFieldSettings
                    save(false);
                    doEnableButtons();
                }
            }
        }
    }

    private void doEnableMultiButtons() {
        boolean hasElements = comboBoxMulti.getItemCount() > 0;
        boolean isSelected = comboBoxMulti.getSelectedItem() != null;
        if (hasElements) {
            toolbarHelperMulti.enableToolbarButton(ToolbarButtonAlias.EDIT_CONFIG_NEW, true);
            toolbarHelperMulti.enableToolbarButton(ToolbarButtonAlias.EDIT_CONFIG_WORK, isSelected);
            toolbarHelperMulti.enableToolbarButton(ToolbarButtonAlias.EDIT_CONFIG_DELETE, isSelected);
        } else {
            toolbarHelperMulti.enableToolbarButton(ToolbarButtonAlias.EDIT_CONFIG_NEW, true);
            toolbarHelperMulti.enableToolbarButton(ToolbarButtonAlias.EDIT_CONFIG_WORK, false);
            toolbarHelperMulti.enableToolbarButton(ToolbarButtonAlias.EDIT_CONFIG_DELETE, false);
        }
    }

    private void fillMultiName(String key, MultiDisplayFieldSettings multiDisplayFieldSettings) {
        comboBoxMulti.switchOffEventListeners();
        comboBoxMulti.removeAllItems();
        LinkedList<MultiDisplayFieldSetting> settingList = multiDisplayFieldSettings.get(key);
        if (Utils.isValid(settingList)) {
            for (MultiDisplayFieldSetting multiDisplayFieldSetting : settingList) {
                if (StrUtils.isValid(multiDisplayFieldSetting.getMultiDisplayFieldName())) {
                    comboBoxMulti.addItem(multiDisplayFieldSetting, multiDisplayFieldSetting.getMultiDisplayFieldName());
                }
            }
            comboBoxMulti.setSelectedItem(multiDisplayFieldSettings.getActiveDisplayFieldSettingName(key));
        }
        comboBoxMulti.switchOnEventListeners();
        doEnableMultiButtons();
    }

    @Override
    protected void doEnableButtons() {
        super.doEnableButtons();
        if (isUserSettings()) {
            if (comboBoxMulti.getSelectedIndex() == -1) {
                disableAllMoveEntries();
                resetButton.setEnabled(false);
                setCurrentConfigButton.setEnabled(false);
                fixedColumnsSpinner.setEnabled(false);
            } else {
                setCurrentConfigButton.setEnabled(true);
                fixedColumnsSpinner.setEnabled(true);
                if (!comboBoxMulti.getSelectedItem().equals(originalActiveMultiConfigName)) {
                    getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).setEnabled(true);
                }
            }
        }
    }

    @Override
    public boolean load() {
        multiConfigPanel.setVisible(isUserSettings());
        boolean result = super.load();
        if (result && isUserSettings()) {
            fillMultiName(getEbeneAndSourceName(), getMultiDisplayFieldSettings());
            doEnableButtons();
        }
        return result;
    }

    private void fillItems(DisplayFieldSettings displayFieldSettings) {
        // Anzeige anpassen
        fillItems(displayFieldSettings.getUserSettingDisplayFields(getEbeneAndSourceName(), extraConfigKeyForDisplayFields),
                  displayFieldSettings.getUserSettingFixedColumnsCount(getEbeneAndSourceName()));
    }

    @Override
    protected void save(boolean saveSessionData) {
        String ebeneAndSourceName = getEbeneAndSourceName();
        int fixedColumnsCount = 0;
        if (J2EEHandler.isJ2EE()) { // Fixierte Spalten funktionieren nur unter J2EE
            fixedColumnsCount = fixedColumnsSpinner.getValue();
        }
        if (saveSessionData) {
            UserDataSessionManager.saveDisplayFieldsInSession(ebeneAndSourceName, getSelectedItems(), fixedColumnsCount);
        } else {
            String name = comboBoxMulti.getSelectedItem();
            if (StrUtils.isValid(name)) {
                // Muss vorher gemacht werden, da DisplayFieldSettings().saveData() die UserSettings speichert
                getMultiDisplayFieldSettings().saveData(ebeneAndSourceName, name, getSelectedItems(), fixedColumnsCount);
                getProject().getUserSettings().getDisplayFieldSettings().saveData(ebeneAndSourceName, getSelectedItems(), fixedColumnsCount);
            } else {
                getProject().getUserSettings().getDisplayFieldSettings().saveData(ebeneAndSourceName, new DwList<>(), 0);
            }
        }
        getProject().fireProjectEvent(AssemblyListFormConfigurationChangedEvent.createTableConfigurationChangedEvent(parentForm,
                                                                                                                     ebeneAndSourceName));
        wasSaved = true;
    }
}
