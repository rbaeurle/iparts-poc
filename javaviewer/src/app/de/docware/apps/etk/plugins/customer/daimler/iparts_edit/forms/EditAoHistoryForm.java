/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsVirtualUserGroup;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAOHistory;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAOHistoryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDataAOHistoryHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectFilterGrid;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;

import java.util.List;

/**
 * Dialog zur Darstellung der Autoren-Auftrags Historie
 */
public class EditAoHistoryForm extends AbstractJavaViewerForm implements iPartsConst {

    private static final String ACTION_FIELD_SUFFIX_FROM = "_FROM";
    private static final String ACTION_FIELD_SUFFIX_TO = "_TO";

    public static void showAOHistory(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, iPartsDataAuthorOrder dataAuthorOrder, boolean decending) {
        EditAoHistoryForm dlg = new EditAoHistoryForm(dataConnector, parentForm);
        dlg.setTitle("!!Autoren-Auftrags-Historie");
        dlg.setDecending(decending);
        dlg.init(dataAuthorOrder);
        dlg.showModal();
    }

    private EditDataObjectFilterGrid grid;
    private boolean decending;

    /**
     * Erzeugt eine Instanz von EditAoHistoryForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public EditAoHistoryForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.decending = false;
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        grid = new EditDataObjectFilterGrid(getConnector(), this) {
            @Override
            protected String getVisualValueOfField(String tableName, String fieldName, EtkDataObject objectForTable) {
                if (fieldName.startsWith(FIELD_DAH_ACTION)) {
                    // Zusammenbau der drei Felder aus dem Action-Field
                    String action = objectForTable.getFieldValue(FIELD_DAH_ACTION);
                    List<String> actionList = iPartsDataAOHistoryHelper.getSplittedActionString(action);
                    if (actionList.size() == 3) {
                        String value;
                        if (fieldName.equals(FIELD_DAH_ACTION)) {
                            iPartsDataAOHistoryHelper.ACTION_KEYS actionKey = iPartsDataAOHistoryHelper.getActionKey(actionList.get(0));
                            switch (actionKey) {
                                case UNKNOWN:
                                    value = "";
                                    break;
                                default:
                                    value = TranslationHandler.translate(actionKey.getDescription());
                                    break;
                            }
                            return value;
                        } else if (fieldName.endsWith(ACTION_FIELD_SUFFIX_FROM) || fieldName.endsWith(ACTION_FIELD_SUFFIX_TO)) {
                            value = actionList.get(1);
                            if (fieldName.endsWith(ACTION_FIELD_SUFFIX_TO)) {
                                value = actionList.get(2);
                            }
                            if (iPartsVirtualUserGroup.isVirtualUserGroupId(value)) {
                                value = iPartsVirtualUserGroup.getVirtualUserGroupName(value);
                            }
                            iPartsDataAOHistoryHelper.ACTION_KEYS actionKey = iPartsDataAOHistoryHelper.getActionKey(actionList.get(0));
                            switch (actionKey) {
                                case STATUS:
                                    value = getVisObject().asHtml(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_STATUS, value,
                                                                  getProject().getDBLanguage()).getStringResult();
                                    break;
                                case USER:
                                    value = getVisObject().asHtml(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_CURRENT_USER_ID, value,
                                                                  getProject().getDBLanguage()).getStringResult();
                                    break;
                                case MSG:
                                    break;
                                default:
                                    value = "";
                                    break;
                            }
                            return value;
                        }
                    }
                    return "";
                } else {
                    return super.getVisualValueOfField(tableName, fieldName, objectForTable);
                }
            }
        };

        grid.setDisplayFields(buildDisplayFields());
        grid.getGui().setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER));
        mainWindow.panelMain.addChild(grid.getGui());
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        close();
        return modalResult;
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public boolean isDecending() {
        return decending;
    }

    public void setDecending(boolean decending) {
        this.decending = decending;
    }

    /**
     * Initialisierung mit {@link iPartsDataAuthorOrder}
     *
     * @param dataAuthorOrder
     */
    public void init(iPartsDataAuthorOrder dataAuthorOrder) {
        fillGrid(dataAuthorOrder);
    }


    private GuiTable getTable() {
        return grid.getTable();
    }

    /**
     * Grid füllen
     *
     * @param dataAuthorOrder
     */
    private void fillGrid(iPartsDataAuthorOrder dataAuthorOrder) {
        grid.clearGrid();
        iPartsDataAOHistoryList dataAOHistoryList = iPartsDataAOHistoryList.loadAOHistoryList(getProject(), dataAuthorOrder.getAsId().getAuthorGuid(), isDecending());

        for (iPartsDataAOHistory dataAOHistory : dataAOHistoryList) {
            grid.addObjectToGrid(dataAOHistory);
        }
        grid.showNoResultsLabel(dataAOHistoryList.isEmpty());
    }

    /**
     * Display-Fields bestimmen
     *
     * @return
     */
    private EtkDisplayFields buildDisplayFields() {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_AO_HISTORY, FIELD_DAH_CHANGE_USER_ID, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_AO_HISTORY, FIELD_DAH_CHANGE_DATE, false, false);
        displayFields.addFeld(displayField);
        // da Action-Field wird in 3 Felder aufgesplittet
        displayField = new EtkDisplayField(TABLE_DA_AO_HISTORY, FIELD_DAH_ACTION, false, false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_AO_HISTORY, FIELD_DAH_ACTION + ACTION_FIELD_SUFFIX_FROM, false, false);
        EtkMultiSprache text = new EtkMultiSprache("!!von", getProject().getConfig().getDatabaseLanguages());
        displayField.setText(text);
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);
        displayField = new EtkDisplayField(TABLE_DA_AO_HISTORY, FIELD_DAH_ACTION + ACTION_FIELD_SUFFIX_TO, false, false);
        text = new EtkMultiSprache("!!nach", getProject().getConfig().getDatabaseLanguages());
        displayField.setText(text);
        displayField.setDefaultText(false);
        displayFields.addFeld(displayField);

        displayFields.loadStandards(getConfig());
        return displayFields;
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            buttonpanel.setDialogStyle(de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel.DialogStyle.CLOSE);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}