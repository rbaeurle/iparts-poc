package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.usage.form.ShowUsageForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsUserSettingsHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EtkEditFieldHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.iPartsMatrixEditFields;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.GuiSeparator;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Formular für die Anzeige der Stammdaten (Tabelle MAT).
 */
public class MasterDataMatForm extends SimpleMasterDataSearchFilterGrid {

    private static final List<String> UNFORMAT_FIELDS_FOR_SEARCH = new DwList<>();

    static {
        UNFORMAT_FIELDS_FOR_SEARCH.add(TableAndFieldName.make(TABLE_MAT, FIELD_M_MATNR));
        UNFORMAT_FIELDS_FOR_SEARCH.add(TableAndFieldName.make(TABLE_MAT, FIELD_M_BASE_MATNR));
        UNFORMAT_FIELDS_FOR_SEARCH.add(TableAndFieldName.make(TABLE_MAT, FIELD_M_MATNR_MBAG));
        UNFORMAT_FIELDS_FOR_SEARCH.add(TableAndFieldName.make(TABLE_MAT, FIELD_M_MATNR_DTAG));
    }

    /**
     * Anzeige der Baureihen Tabelle (DA_Series)
     *
     * @param owner
     */
    public static void showMatMasterData(AbstractJavaViewerForm owner) {
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        MasterDataMatForm dlg = new MasterDataMatForm(activeForm.getConnector(), activeForm);

        EtkProject project = activeForm.getConnector().getProject();
        EtkConfig config = project.getConfig();
        // Suchfelder definieren
        EtkDisplayFields searchFields = new EtkDisplayFields();
        searchFields.load(config, iPartsEditConfigConst.iPARTS_EDIT_MASTER_MAT_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_SEARCHFIELDS,
                          config.getCurrentDatabaseLanguage());
        if (searchFields.size() == 0) {
            // Suchfelder definieren
            searchFields.addFeld(createSearchField(project, TABLE_MAT, FIELD_M_MATNR, false, false));
            searchFields.addFeld(createSearchField(project, TABLE_MAT, FIELD_M_BASE_MATNR, false, false));
        }

        // Anzeigefelder definieren
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(config, iPartsEditConfigConst.iPARTS_EDIT_MASTER_MAT_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            EtkDisplayField displayField = addDisplayField(TABLE_MAT, FIELD_M_MATNR, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            displayField = addDisplayField(TABLE_MAT, FIELD_M_BASE_MATNR, false, false, null, project, displayFields);
            displayField.setColumnFilterEnabled(true);
            addDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false, null, project, displayFields);
            addDisplayField(TABLE_MAT, FIELD_M_CONST_DESC, true, false, null, project, displayFields);
        }

        // Editfelder fürs Editieren festlegen
        EtkEditFields editFields = new EtkEditFields();
        editFields.load(config, iPartsEditConfigConst.iPARTS_EDIT_MASTER_MAT_KEY + iPartsEditConfigConst.REL_EDIT_MASTER_EDITFIELDS);
        // wenn nichts konfiguriert, dann werden alle Felder angezeigt

        // Sortierung
        LinkedHashMap<String, Boolean> sortFields = new LinkedHashMap<>();
        sortFields.put(FIELD_M_MATNR, false);
        dlg.setSortFields(sortFields);

        dlg.setDisplayResultFields(displayFields);
        dlg.setSearchFields(searchFields);
        dlg.setEditFields(editFields);
        dlg.setEditAllowed(false);
        dlg.setNewAllowed(false);
        dlg.setModifyAllowed(false);
        dlg.setDeleteAllowed(false);
        dlg.setWindowTitle("!!Teilestamm");
        dlg.setTitlePrefix("!!Teilestamm");
        dlg.setWindowName("MatMasterData");
        dlg.setMaxResults(J2EEHandler.isJ2EE() ? MAX_SELECT_SEARCH_RESULTS_SIZE : -1);
        dlg.showNonModal();
    }

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     */
    public MasterDataMatForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm, TABLE_MAT, null);
        doResizeWindow(SCREEN_SIZES.MAXIMIZE);
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);

        GuiSeparator separator = new GuiSeparator();
        separator.setName("menuSeparator");
        contextMenu.addChild(separator);
        ToolbarButtonMenuHelper.ToolbarMenuHolder holder = toolbarHelper.addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_USAGE,
                                                                                                       EditToolbarButtonAlias.EDIT_USAGE.getTooltip(),
                                                                                                       getUITranslationHandler(),
                                                                                                       new MenuRunnable() {
                                                                                                           @Override
                                                                                                           public void run(Event event) {
                                                                                                               doShowUsage();
                                                                                                           }
                                                                                                       });
        contextMenu.addChild(holder.menuItem);

        // hier weitere ContextMenus
    }

    @Override
    protected List<String> getSearchValues() {
        List<String> searchValues = new DwList<>();
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        EtkProject project = getProject();
        for (int lfdNr = 0; lfdNr < searchFields.size(); lfdNr++) {
            EditControlFactory ctrl = editControls.getControlByFeldIndex(lfdNr).getEditControl();
            String value = ctrl.getText();
            if (UNFORMAT_FIELDS_FOR_SEARCH.contains(ctrl.getTableFieldName())) {
                // Sonderbehandlung für Mat-Nummern
                value = numberHelper.unformatASachNoForDB(project, value);
            }
            searchValues.add(value);
        }
        return searchValues;
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();

        boolean singleSelectionEnabled = getTable().getSelectedRows().size() == 1;
        toolbarHelper.enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_USAGE, getTable().getContextMenu(), singleSelectionEnabled);
    }

    protected void doShowUsage() {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            IdWithType id = buildIdFromAttributes(attributes);
            if (id == null) {
                return;
            }
            PartId partId = new PartId(id.toStringArrayWithoutType());
            // Aufruf von Usage
            ShowUsageForm.showMatUsage(getConnector(), this, partId, getProject().getRootNodes().getRootAssemblyId());
        }
    }

    @Override
    protected void doEditOrView(Event event) {
        boolean isMatrix = iPartsUserSettingsHelper.isMatrixEdit(getProject());
        if (!isMatrix) {
            if (editFields.getFields().isEmpty()) {
                EtkEditFieldHelper.getEditFields(getProject(), TABLE_MAT, editFields, false);
            }
            super.doEditOrView(event);
            return;
        }
        endSearch();
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if (attributes != null) {
            IdWithType id = buildIdFromAttributes(attributes);
            if (id == null) {
                return;
            }
            EditMatrixUserControls editMatrixControls;
            if (editFields.getFields().isEmpty()) {
                editMatrixControls = new EditMatrixUserControls(getConnector(), this, TABLE_MAT, id);
            } else {
                // einfaches 3spaltiges Layout
                iPartsMatrixEditFields externalMatrixEditFields = new iPartsMatrixEditFields();
                EtkEditFieldHelper.convertEditFieldsToMatrixEditFields(editFields, externalMatrixEditFields, 3);
                editMatrixControls = new EditMatrixUserControls(getConnector(), this, TABLE_MAT, id,
                                                                null, externalMatrixEditFields,
                                                                EditMatrixUserControls.MATRIX_LAYOUT.DEFAULT, "");
            }
            editMatrixControls.setReadOnly(true);
            editMatrixControls.setTitle(TranslationHandler.translate("!!%1 für \"%2\"", TranslationHandler.translate(titleForView),
                                                                     iPartsNumberHelper.formatPartNo(getProject(), id.getValue(1))));
            editMatrixControls.setWindowName(editControlsWindowName);
            editMatrixControls.showModal();
        }
    }
}
