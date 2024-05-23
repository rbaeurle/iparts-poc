package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class iPartsAcceptETKZChangesForm extends SimpleMasterDataSearchResultGrid {

    private static String TABLE = "ETKChanges";

    private enum ShowFields {
        FIELD_PART_NO(TABLE_MAT, FIELD_M_BESTNR, ""),
        FIELD_ETKZ_OLD(TABLE_MAT, FIELD_M_ETKZ_OLD, "!!ET-KZ alt"),
        FIELD_ETKZ_NEW(TABLE_MAT, FIELD_M_ETKZ, "!!ET-KZ neu"),
        FIELD_HOTSPOT(TABLE_KATALOG, FIELD_K_POS, ""),
        FIELD_LFDNR(TABLE_KATALOG, FIELD_K_LFDNR, ""),
        FIELD_BCTE(TABLE_KATALOG, FIELD_K_SOURCE_GUID, ""),
        FIELD_ETK_OLD(TABLE_KATALOG, FIELD_K_ETKZ, "!!ETK alt"),
        FIELD_ETK_NEW(TABLE_KATALOG, FIELD_DD_ETKZ, "!!ETK neu");

        private String tableName;
        private String fieldName;
        private String text;

        ShowFields(String tableName, String fieldName, String text) {
            this.tableName = tableName;
            this.fieldName = fieldName;
            this.text = text;
        }

        public boolean hasText() {
            return StrUtils.isValid(text);
        }
    }

    // für ETKZ Darstellung
    private static final EnumSet<ShowFields> ETKZ_FIELDS = EnumSet.of(ShowFields.FIELD_PART_NO, ShowFields.FIELD_ETKZ_OLD,
                                                                      ShowFields.FIELD_ETKZ_NEW);

    // für ETK Darstellung
    private static final EnumSet<ShowFields> ETK_FIELDS = EnumSet.of(ShowFields.FIELD_HOTSPOT, ShowFields.FIELD_PART_NO,
                                                                     ShowFields.FIELD_LFDNR, ShowFields.FIELD_BCTE,
                                                                     ShowFields.FIELD_ETK_OLD, ShowFields.FIELD_ETK_NEW);


    /**
     * ETKZ Änderungen (am Material) müssen bestätigt werden
     *
     * @param dataConnector
     * @param parentForm
     * @param selectedPartListEntries
     * @return
     */
    public static List<EtkDataPartListEntry> showAcceptETKZChanges(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, List<EtkDataPartListEntry> selectedPartListEntries) {
        String title = "!!Bestätigen der ET-KZ-Änderung";
        if (selectedPartListEntries.size() > 1) {
            title = "!!Bestätigen der ET-KZ-Änderungen";
        }
        return showAcceptETKAndETKZChanges(dataConnector, parentForm, selectedPartListEntries,
                                           ETKZ_FIELDS, "!!Quittierung von ET-KZ-Änderungen", title);

//        iPartsAcceptETKZChangesForm form = new iPartsAcceptETKZChangesForm(dataConnector, parentForm, null, "!!Quittierung von ET-KZ-Änderungen");
//
//        form.setDisplayFields(ETKZ_FIELDS);
//        form.fillGrid(selectedPartListEntries, ETKZ_FIELDS);
////        form.fillGridETKZ(selectedPartListEntries);
//        if (selectedPartListEntries.size() > 1) {
//            form.setTitle("!!Bestätigen der ET-KZ-Änderungen");
//        } else {
//            form.setTitle("!!Bestätigen der ET-KZ-Änderung");
//        }
//        form.getTable().selectAllRows();
//        ModalResult result = form.showModal(parentForm.getRootParentWindow());
//        List<EtkDataPartListEntry> acceptedETKZChanges = new ArrayList<>();
//        if (result == ModalResult.OK) {
//            form.mapAcceptedETKZToPartListEntry(acceptedETKZChanges, selectedPartListEntries);
//        }
//        return acceptedETKZChanges;
    }

    /**
     * ETK Änderungen (An der Teileposition) müssen bestätigt werden
     *
     * @param dataConnector
     * @param parentForm
     * @param selectedPartListEntries
     * @return
     */
    public static List<EtkDataPartListEntry> showAcceptETKChanges(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, List<EtkDataPartListEntry> selectedPartListEntries) {
        String title = "!!Bestätigen der ETK-Änderung";
        if (selectedPartListEntries.size() > 1) {
            title = "!!Bestätigen der ETK-Änderungen";
        }
        return showAcceptETKAndETKZChanges(dataConnector, parentForm, selectedPartListEntries,
                                           ETK_FIELDS, "!!Quittierung von ETK-Änderungen", title);


//        iPartsAcceptETKZChangesForm form = new iPartsAcceptETKZChangesForm(dataConnector, parentForm, null, "!!Quittierung von ETK-Änderungen");
//
//        form.setDisplayFields(ETK_FIELDS);
//        form.fillGrid(selectedPartListEntries, ETK_FIELDS);
//
////        form.fillGridETK(selectedPartListEntries);
//        if (selectedPartListEntries.size() > 1) {
//            form.setTitle("!!Bestätigen der ETK-Änderungen");
//        } else {
//            form.setTitle("!!Bestätigen der ETK-Änderung");
//        }
//        form.getTable().selectAllRows();
//
//        ModalResult result = form.showModal(parentForm.getRootParentWindow());
//        List<EtkDataPartListEntry> acceptedETKChanges = new ArrayList<>();
//        if (result == ModalResult.OK) {
//            form.mapAcceptedETKZToPartListEntry(acceptedETKChanges, selectedPartListEntries);
//        }
//        return acceptedETKChanges;
    }

    private static List<EtkDataPartListEntry> showAcceptETKAndETKZChanges(AbstractJavaViewerFormIConnector dataConnector,
                                                                          AbstractJavaViewerForm parentForm,
                                                                          List<EtkDataPartListEntry> selectedPartListEntries,
                                                                          EnumSet<ShowFields> showFields,
                                                                          String windowTitle, String title) {
        iPartsAcceptETKZChangesForm form = new iPartsAcceptETKZChangesForm(dataConnector, parentForm, null, windowTitle);

        form.setDisplayFields(showFields);
        form.fillGrid(selectedPartListEntries, showFields);
        form.setTitle(title);
        form.getTable().selectAllRows();

        ModalResult result = form.showModal(parentForm.getRootParentWindow());
        List<EtkDataPartListEntry> acceptedETKChanges = new ArrayList<>();
        if (result == ModalResult.OK) {
            form.mapAcceptedETKZToPartListEntry(acceptedETKChanges, selectedPartListEntries);
        }
        return acceptedETKChanges;
    }

    /**
     * Nur die in der Form selektierten Änderungen sollen behandelt werden. Zu diesen Änderungen müssen die Stücklisteneinträge
     * gefunden werden
     *
     * @param acceptedETKZChanges
     * @param selectedPartListEntries
     */
    private void mapAcceptedETKZToPartListEntry(List<EtkDataPartListEntry> acceptedETKZChanges, List<EtkDataPartListEntry> selectedPartListEntries) {
        GuiTable table = this.getTable();
        List<GuiTableRow> selectedRows = table.getSelectedRows();
        // Den PartListEntry heraussuchen, dessen ETK/ETKZ Änderung akzeptiert wurde
        // gleiche Reihenfolge in Liste der selektierten Stücklisteneinträge, wie in den Anzeige der Änderungen
        for (GuiTableRow selectedRow : selectedRows) {
            int rowIndex = table.getRowIndex(selectedRow);
            acceptedETKZChanges.add(selectedPartListEntries.get(rowIndex));
        }
    }

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param onEditChangeRecordEvent
     */
    public iPartsAcceptETKZChangesForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, OnEditChangeRecordEvent onEditChangeRecordEvent, String windowTitle) {
        super(dataConnector, parentForm, TABLE, onEditChangeRecordEvent);

        setWindowTitle(windowTitle);
    }

    protected void postCreateGui() {
        super.postCreateGui();
        setEditAllowed(false);
        showToolbar(false);
        showSearchFields(false);
        showSelectCount(false);
        setOnDblClickEvent(null);
        setLabelNotFoundText("!!Es liegen keine Ergebnisse vor.");
        getButtonPanel().setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.OK);
        getButtonPanel().setDialogStyle(GuiButtonPanel.DialogStyle.DIALOG);
    }

    private void setDisplayFields(EnumSet<ShowFields> showFields) {
        // Möglichst bereits konfugurierte DisplayFields nehmen
        EtkEbenenDaten proposalEbene = null;
        if (getConnector() instanceof EditModuleFormConnector) {
            EtkDataAssembly assembly = ((EditModuleFormConnector)getConnector()).getCurrentAssembly();
            if (assembly != null) {
                proposalEbene = assembly.getEbene();
            }
        }
        List<String> viewerLanguages = getConnector().getConfig().getViewerLanguages();
        EtkDisplayFields displayFields = new EtkDisplayFields();
        for (ShowFields showField : showFields) {
            EtkDisplayField field = getLocalDisplayField(showField.tableName, showField.fieldName, proposalEbene);
            if (showField.hasText()) {
                field.setText(new EtkMultiSprache(showField.text, viewerLanguages));
                field.setDefaultText(false);
            }
            displayFields.addFeld(field);
        }
        setDisplayResultFields(displayFields);
    }

    private EtkDisplayField getLocalDisplayField(String tableName, String fieldName, EtkEbenenDaten proposalEbene) {
        EtkDisplayField field = null;
        if (proposalEbene != null) {
            // möglichst bereits konfigurierte DisplayFields nehmen
            EtkDisplayField proposalField = proposalEbene.getFeldByName(tableName, fieldName);
            if (proposalField != null) {
                field = proposalField.cloneMe();
                field.setVisible(true);
            }
        }
        if (field == null) {
            // nicht konfiguriert oder vorhanden => neu erzeugen
            field = createDisplayField(getProject(), tableName, fieldName, false, false);
        }
        return field;
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();
        boolean isGridSelected = !getTable().getSelectedRows().isEmpty();
        getButtonPanel().setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isGridSelected);
    }

    @Override
    protected void doEditOrView(Event event) {
        // Weder Edit noch Anzeige sind hier möglich
    }

    /**
     * Tabelle füllen für beide Änderungen
     *
     * @param partListEntriesToAdapt
     * @param showFields
     */
    private void fillGrid(List<EtkDataPartListEntry> partListEntriesToAdapt, EnumSet<ShowFields> showFields) {
        for (EtkDataPartListEntry partListEntry : partListEntriesToAdapt) {
            DBDataObjectAttributes attributes = new DBDataObjectAttributes();
            boolean oldLogLoadFieldIfNeeded = partListEntry.isLogLoadFieldIfNeeded();
            boolean oldLogLoadFieldIfNeededPart = partListEntry.getPart().isLogLoadFieldIfNeeded();
            try {
                partListEntry.setLogLoadFieldIfNeeded(false);
                partListEntry.getPart().setLogLoadFieldIfNeeded(false);
                for (ShowFields showField : showFields) {
                    String value = "";
                    switch (showField) {
                        case FIELD_PART_NO:
                            value = partListEntry.getPart().getAsId().getMatNr();
                            break;
                        case FIELD_ETKZ_OLD:
                            value = partListEntry.getPart().getFieldValue(showField.fieldName);
                            break;
                        case FIELD_ETKZ_NEW:
                            value = partListEntry.getPart().getFieldValue(showField.fieldName);
                            break;
                        case FIELD_ETK_NEW:
                            // suche den zugehörigen Eintrag aus der Konstruktion, da dieser den neuen ETK-Wert beinhaltet
                            iPartsDataDialogData dialogData = new iPartsDataDialogData(getProject(), new iPartsDialogId(partListEntry.getFieldValue(FIELD_K_SOURCE_GUID)));
                            if (dialogData.existsInDB()) {
                                value = dialogData.getFieldValue(FIELD_DD_ETKZ);
                            }
                            break;
                        default:
                            value = partListEntry.getFieldValue(showField.fieldName);
                            break;
                    }
                    attributes.addField(showField.fieldName, value, DBActionOrigin.FROM_DB);
                }
            } finally {
                partListEntry.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
                partListEntry.getPart().setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeededPart);
            }
            addAttributesToGrid(attributes);
        }
        showNoResultsLabel(getTable().getRowCount() == 0, false);
    }

    /**
     * Tabelle füllen für die ETKZ Änderungen
     *
     * @param partListEntriesToAdapt
     */
    private void fillGridETKZ(List<EtkDataPartListEntry> partListEntriesToAdapt) {
        for (EtkDataPartListEntry partListEntry : partListEntriesToAdapt) {
            DBDataObjectAttributes attributes = new DBDataObjectAttributes();

            EtkDataPart matData = partListEntry.getPart();
            String partNo = matData.getFieldValue(FIELD_M_MATNR);
            attributes.addField(ShowFields.FIELD_PART_NO.fieldName, partNo, true, DBActionOrigin.FROM_DB);
//            attributes.addField(FIELD_M_MATNR, partNo, true, DBActionOrigin.FROM_DB);

            boolean oldLogLoadFieldIfNeeded = partListEntry.isLogLoadFieldIfNeeded();
            String etk_old = "";
            try {
                partListEntry.setLogLoadFieldIfNeeded(false);
                etk_old = matData.getFieldValue(FIELD_M_ETKZ_OLD);
            } finally {
                partListEntry.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
            }

//            attributes.addField(FIELD_ETKZ_OLD, etk_old, true, DBActionOrigin.FROM_DB);
            attributes.addField(ShowFields.FIELD_ETKZ_OLD.fieldName, etk_old, true, DBActionOrigin.FROM_DB);
            String etkz = matData.getFieldValue(FIELD_M_ETKZ);
//            attributes.addField(FIELD_ETKZ_NEW, etkz, true, DBActionOrigin.FROM_DB);
            attributes.addField(ShowFields.FIELD_ETKZ_NEW.fieldName, etkz, true, DBActionOrigin.FROM_DB);
            addAttributesToGrid(attributes);
        }
        showNoResultsLabel(getTable().getRowCount() == 0, false);
    }

    /**
     * Tabelle füllen für die ETK-Änderungen
     *
     * @param partListEntriesToAdapt
     */
    private void fillGridETK(List<EtkDataPartListEntry> partListEntriesToAdapt) {
        for (EtkDataPartListEntry partListEntry : partListEntriesToAdapt) {
            DBDataObjectAttributes attributes = new DBDataObjectAttributes();

            String hotspot = partListEntry.getFieldValue(FIELD_K_POS);
            attributes.addField(ShowFields.FIELD_HOTSPOT.fieldName, hotspot, DBActionOrigin.FROM_DB);

            EtkDataPart matData = partListEntry.getPart();
            String partNo = matData.getFieldValue(iPartsConst.FIELD_M_MATNR);
            attributes.addField(ShowFields.FIELD_PART_NO.fieldName, partNo, DBActionOrigin.FROM_DB);

            String lfdnr = partListEntry.getFieldValue(FIELD_K_LFDNR);
            attributes.addField(ShowFields.FIELD_LFDNR.fieldName, lfdnr, DBActionOrigin.FROM_DB);

            String bcte = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
            attributes.addField(ShowFields.FIELD_BCTE.fieldName, bcte, DBActionOrigin.FROM_DB);

            // suche den zugehörigen Eintrag aus der Konstruktion, da dieser den neuen ETK-Wert beinhaltet
            iPartsDataDialogData dialogData = new iPartsDataDialogData(getProject(), new iPartsDialogId(partListEntry.getFieldValue(FIELD_K_SOURCE_GUID)));
            if (dialogData.existsInDB()) {
                String etkz = dialogData.getFieldValue(FIELD_DD_ETKZ);
                attributes.addField(ShowFields.FIELD_ETK_NEW.fieldName, etkz, DBActionOrigin.FROM_DB);
            } else {
                attributes.addField(ShowFields.FIELD_ETK_NEW.fieldName, "", DBActionOrigin.FROM_DB);
            }

            String etkz_old = partListEntry.getFieldValue(FIELD_K_ETKZ);
            attributes.addField(ShowFields.FIELD_ETK_OLD.fieldName, etkz_old, DBActionOrigin.FROM_DB);

            addAttributesToGrid(attributes);
        }
    }
}
