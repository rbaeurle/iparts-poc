package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokKEMId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsNutzDokSAAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.truckworkbasket.iPartsWorkBasketInternalTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditSelectDataObjectsForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.truckworkbasket.iPartsDataNutzDokSAA;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.Comparator;
import java.util.List;

/**
 * Formular für den ET-Sichtenabgleich der Enumwerte
 */
public class WorkbasketEtsExtensionForm extends EditSelectDataObjectsForm {

    private static final String UNCONFIRMED_PREFIX = "U_";

    public static EtkDataObject showEtsExtensionForm(AbstractJavaViewerForm parentForm, iPartsWorkBasketInternalTextId wbIntTextId,
                                                     EtkDataObject dataObject) {
        String tableName;
        if (wbIntTextId.isSaaOrBk()) {
            tableName = TABLE_DA_NUTZDOK_SAA;
        } else {
            tableName = TABLE_DA_NUTZDOK_KEM;
        }
        WorkbasketEtsExtensionForm dlg = new WorkbasketEtsExtensionForm(parentForm.getConnector(), parentForm, wbIntTextId,
                                                                        dataObject, tableName);
        String title = "!!ET-Sichten abgleichen für %1";
        String subTitle = "!!SAA: \"%1\"";
        String value = wbIntTextId.getSaaBkKemValue();
        if (wbIntTextId.isKEM()) {
            subTitle = "!!KEM: \"%1\"";
        } else {
            value = iPartsNumberHelper.formatPartNo(parentForm.getConnector().getProject(), value);
        }
        title = TranslationHandler.translate(title, TranslationHandler.translate(subTitle, value));
        dlg.setTitle(title);
        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getModifiedObject();
        }
        return null;
    }

    private iPartsWorkBasketInternalTextId wbIntTextId;
    private EtkDataObject sourceDataObject;
    private List<String> sourceEtsList;
    private List<String> sourceUnconfirmedEtsList;

    /**
     * Erzeugt eine Instanz von EditSelectDataObjectsForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     */
    public WorkbasketEtsExtensionForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                      iPartsWorkBasketInternalTextId wbIntTextId,
                                      EtkDataObject dataObject, String tableName) {
        super(dataConnector, parentForm, tableName, "", "");
        this.wbIntTextId = wbIntTextId;
        this.sourceDataObject = dataObject;
        searchTable = getTableName();
        setName("SelectEtsExtensionForm");
        setAvailableEntriesTitle("!!Nicht bestätigte ET-Sichten:");
        setSelectedEntriesTitle("!!Bestätigte ET-Sichten:");
        availableEntriesGrid.setNoResultsLabelText("!!Keine unbestätigten ET-Sichten vorhanden");
        selectedEntriesGrid.setNoResultsLabelText("!!Keine ET-Sichten vorhanden");
        setWithDeleteEntry(true);
        setNoDoubles(true);
        setMoveEntriesVisible(false);
        availableEntriesGrid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);

        initGrids();
    }

    private void initGrids() {
        sourceEtsList = sourceDataObject.getFieldValueAsSetOfEnum(getEtsFieldName());
        sourceUnconfirmedEtsList = sourceDataObject.getFieldValueAsSetOfEnum(getEtsUnconfirmedFieldName());
        List<EtkDataObject> etsObjectList = new DwList<>(sourceEtsList.size());
        List<EtkDataObject> etsUnconfirmedObjectList = new DwList<>(sourceUnconfirmedEtsList.size());
        // Dummy-DataObjects für jeweils einen Enumwert erzeugen
        for (String elem : sourceEtsList) {
            if (wbIntTextId.isSaaOrBk()) {
                etsObjectList.add(createSaaObject(elem, getEtsFieldName(), elem));
            } else {
                etsObjectList.add(createKemObject(elem, getEtsFieldName(), elem));
            }
        }
        // Dummy-DataObjects für jeweils einen Enumwert erzeugen
        for (String elem : sourceUnconfirmedEtsList) {
            if (wbIntTextId.isSaaOrBk()) {
                etsUnconfirmedObjectList.add(createSaaObject(makeUnconfirmedIdValue(elem), getEtsUnconfirmedFieldName(), elem));
            } else {
                etsUnconfirmedObjectList.add(createKemObject(makeUnconfirmedIdValue(elem), getEtsUnconfirmedFieldName(), elem));
            }
        }
        // die Grids füllen
        fillAvailableEntries(etsUnconfirmedObjectList);
        fillSelectedEntries(etsObjectList);
        doEnableButtons();
        doEnableOKButton();
        // Formulargröße anpassen
        setAutoSize();
    }

    public void setAutoSize() {
        setSize(calcWidth(), calcHeight());
    }

    /**
     * Das endgültige Ergebnis bestimmen
     *
     * @return
     */
    public EtkDataObject getModifiedObject() {
        List<String> etList = new DwList<>();
        List<String> unconfirmedEtList = new DwList<>();
        // Enumwerte der selektierten Liste zusammenbauen
        for (EtkDataObject dataObject : getCompleteSelectedList()) {
            etList.add(getEtsValueFromDummyObject(dataObject));
        }
        // Enumwerte der noch nicht bestätigten Liste zusammenbauen
        for (EtkDataObject dataObject : getCompleteAvailableList()) {
            unconfirmedEtList.add(getEtsValueFromDummyObject(dataObject));
        }
        // erstmal die Werte löschen
        sourceDataObject.setFieldValue(getEtsFieldName(), "", DBActionOrigin.FROM_EDIT);
        sourceDataObject.setFieldValue(getEtsUnconfirmedFieldName(), "", DBActionOrigin.FROM_EDIT);
        // die Werte im jeweiligen DataObject setzen
        if (wbIntTextId.isSaaOrBk()) {
            iPartsDataNutzDokSAA saaObject = (iPartsDataNutzDokSAA)sourceDataObject;
            saaObject.setFieldValueAsSetOfEnum(getEtsFieldName(), etList, DBActionOrigin.FROM_EDIT);
            saaObject.setFieldValueAsSetOfEnum(getEtsUnconfirmedFieldName(), unconfirmedEtList, DBActionOrigin.FROM_EDIT);
        } else {
            iPartsDataNutzDokKEM kemObject = (iPartsDataNutzDokKEM)sourceDataObject;
            kemObject.setFieldValueAsSetOfEnum(getEtsFieldName(), etList, DBActionOrigin.FROM_EDIT);
            kemObject.setFieldValueAsSetOfEnum(getEtsUnconfirmedFieldName(), unconfirmedEtList, DBActionOrigin.FROM_EDIT);
        }

        return sourceDataObject;
    }

    @Override
    protected EtkDisplayFields buildDefaultDisplayFields(boolean forSelectedEntries) {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        String fieldName;
        // hier der Umweg über tableName, da wbIntTextId noch nicht gesetzt ist
        if (forSelectedEntries) {
            if (searchTable.equals(TABLE_DA_NUTZDOK_SAA)) {
                fieldName = FIELD_DNS_ETS;
            } else {
                fieldName = FIELD_DNK_ETS;
            }
        } else {
            if (searchTable.equals(TABLE_DA_NUTZDOK_SAA)) {
                fieldName = FIELD_DNS_ETS_UNCONFIRMED;
            } else {
                fieldName = FIELD_DNK_ETS_UNCONFIRMED;
            }
        }
        displayFields.addFeld(new EtkDisplayField(searchTable, fieldName, false, false));
        displayFields.loadStandards(getConfig());
        return displayFields;
    }

    /**
     * Abgeleitet, da bereits bestätigte Enumwerte nicht verschoben werden dürfen
     * und die nach oben/unten ToolbarButtons nicht angezeigt werden
     */
    @Override
    protected void doEnableButtons() {
        int availableEntriesSelectionCount = availableEntriesGrid.getTable().getSelectedRows().size();
        int selectedEntriesSelectionCount = selectedEntriesGrid.getTable().getSelectedRows().size();

        boolean moveRight = false;
        boolean moveLeft = false;
        boolean moveLeftAll = false;
        if (availableEntriesSelectionCount > 0) {
            List<IdWithType> currentIdList = getCompleteSelectedIdList();
            List<EtkDataObject> currentList = getSelectedList(availableEntriesGrid);
            boolean somethingNew = false;
            for (EtkDataObject dataObject : currentList) {
                if (!currentIdList.contains(dataObject.getAsId())) {
                    moveRight = true;
                    somethingNew = true;
                    break;
                }
            }
            for (IdWithType id : currentIdList) {
                if (isUnconfirmedId(id)) {
                    moveLeft = true;
                    break;
                }
            }
            if (!somethingNew) {
                availableEntriesSelectionCount = 0;
            }
        } else {
            if (selectedEntriesSelectionCount > 0) {
                List<EtkDataObject> currentList = getSelectedList(selectedEntriesGrid);
                moveLeft = true;
                for (EtkDataObject dataObject : currentList) {
                    if (!isUnconfirmedDataObject(dataObject)) {
                        moveLeft = false;
                        break;
                    }
                }
            }
        }

        for (EtkDataObject dataObject : getCompleteSelectedList()) {
            if (isUnconfirmedDataObject(dataObject)) {
                moveLeftAll = true;
                break;
            }
        }

        enableToolbarButtonVisible(iPartsToolbarButtonAlias.IMG_RIGHT, (availableEntriesSelectionCount > 0) && moveRight);
        enableToolbarButtonVisible(iPartsToolbarButtonAlias.IMG_LEFT, (selectedEntriesSelectionCount > 0) && moveLeft);
        enableToolbarButtonVisible(iPartsToolbarButtonAlias.IMG_RIGHT_ALL, availableEntriesGrid.getTable().getRowCount() > 0);
        enableToolbarButtonVisible(iPartsToolbarButtonAlias.IMG_LEFT_ALL, (selectedEntriesGrid.getTable().getRowCount() > 0) && moveLeftAll);

        availableEntriesGrid.showNoResultsLabel(availableEntriesGrid.getTable().getRowCount() <= 0);
        selectedEntriesGrid.showNoResultsLabel(selectedEntriesGrid.getTable().getRowCount() <= 0);
    }

    @Override
    protected void doRemoveEntries(List<EtkDataObject> selectedList) {
        // bereits bestätigte Enumwerte dürfen nicht gelöscht werden
        selectedList.removeIf(dataObject -> !isUnconfirmedDataObject(dataObject));
        super.doRemoveEntries(selectedList);
    }

    @Override
    protected boolean areEntriesChanged() {
        return selectedEntriesGrid.getTable().getRowCount() > sourceEtsList.size();
    }

    @Override
    protected void sortSelectedObjects(List<EtkDataObject> selectedObjects) {
        // Elemente nach Id sortieren, aber vorher den Dummy-Prefix entfernen
        Comparator<EtkDataObject> sortComparator = new Comparator<EtkDataObject>() {
            @Override
            public int compare(EtkDataObject o1, EtkDataObject o2) {
                return normalizeId(o1.getAsId()).compareTo(normalizeId(o2.getAsId()));
            }
        };
        selectedObjects.sort(sortComparator);
    }

    private String getTableName() {
        if (wbIntTextId.isSaaOrBk()) {
            return TABLE_DA_NUTZDOK_SAA;
        } else {
            return TABLE_DA_NUTZDOK_KEM;
        }
    }

    private String getEtsFieldName() {
        if (wbIntTextId.isSaaOrBk()) {
            return FIELD_DNS_ETS;
        } else {
            return FIELD_DNK_ETS;
        }
    }

    private String getEtsUnconfirmedFieldName() {
        if (wbIntTextId.isSaaOrBk()) {
            return FIELD_DNS_ETS_UNCONFIRMED;
        } else {
            return FIELD_DNK_ETS_UNCONFIRMED;
        }
    }

    private String makeUnconfirmedIdValue(String value) {
        return UNCONFIRMED_PREFIX + value;
    }

    private String normalizeId(IdWithType id) {
        if (isUnconfirmedId(id)) {
            return StrUtils.removeFirstCharacterIfCharacterIs(id.getValue(1), UNCONFIRMED_PREFIX);
        }
        return id.getValue(1);
    }

    private boolean isUnconfirmedId(IdWithType id) {
        return id.getValue(1).startsWith(UNCONFIRMED_PREFIX);
    }

    private boolean isUnconfirmedDataObject(EtkDataObject dataObject) {
        return isUnconfirmedId(dataObject.getAsId());
    }

    /**
     * SAA-Dummy DataObject erzeugen
     *
     * @param idValue
     * @param fieldName
     * @param enumValue
     * @return
     */
    private EtkDataObject createSaaObject(String idValue, String fieldName, String enumValue) {
        iPartsNutzDokSAAId id = new iPartsNutzDokSAAId(idValue);
        iPartsDataNutzDokSAA saaObject = new iPartsDataNutzDokSAA(getProject(), id);
        saaObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        List<String> helpList = new DwList<>();
        helpList.add(enumValue);
        saaObject.setFieldValueAsSetOfEnum(fieldName, helpList, DBActionOrigin.FROM_DB);
        return saaObject;
    }

    /**
     * KEM-Dummy DataObject erzeugen
     *
     * @param idValue
     * @param fieldName
     * @param enumValue
     * @return
     */
    private EtkDataObject createKemObject(String idValue, String fieldName, String enumValue) {
        iPartsNutzDokKEMId id = new iPartsNutzDokKEMId(idValue);
        iPartsDataNutzDokKEM kemObject = new iPartsDataNutzDokKEM(getProject(), id);
        kemObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        List<String> helpList = new DwList<>();
        helpList.add(enumValue);
        kemObject.setFieldValueAsSetOfEnum(fieldName, helpList, DBActionOrigin.FROM_DB);
        return kemObject;
    }

    /**
     * Den 'richtigen' Enumwert aus dem Dummy-Object holen
     *
     * @param dataObject
     * @return
     */
    private String getEtsValueFromDummyObject(EtkDataObject dataObject) {
        List<String> enumList;
        if (isUnconfirmedDataObject(dataObject)) {
            enumList = dataObject.getFieldValueAsSetOfEnum(getEtsUnconfirmedFieldName());
        } else {
            enumList = dataObject.getFieldValueAsSetOfEnum(getEtsFieldName());
        }
        if (enumList.isEmpty()) {
            return "";
        } else {
            return enumList.get(0);
        }
    }

    /**
     * hier kann getVisualValueOfField des selected-Grids überlagert werden
     *
     * @param tableName
     * @param fieldName
     * @param objectForTable
     * @return null: normale Ausgabe, sonst Anzeige des übergebenen Wertes
     */
    @Override
    protected String getVisualValueOfFieldSelected(String tableName, String fieldName, EtkDataObject objectForTable) {
        if (fieldName.equals(getEtsFieldName())) {
            boolean isUnconfirmed = isUnconfirmedDataObject(objectForTable);
            if (isUnconfirmed) {
                fieldName = getEtsUnconfirmedFieldName();
            }
            String value = getVisObject().asHtml(tableName, fieldName, objectForTable.getAttributeForVisObject(fieldName),
                                                 getProject().getDBLanguage(), true).getStringResult();
            return value;
        }
        return null;
    }

    /**
     * Bereits bestätigte Enumwerte Bold und farbig darstellen
     *
     * @param tableName
     * @param fieldName
     * @param objectForTable
     * @param value
     * @return
     */
    @Override
    protected AbstractGuiControl createCellContentSelected(String tableName, String fieldName, EtkDataObject objectForTable, String value) {
        if (fieldName.equals(getEtsFieldName())) {
            boolean isUnconfirmed = isUnconfirmedDataObject(objectForTable);
            if (!isUnconfirmed) {
                GuiLabel label = new GuiLabel(value);
                label.setFontStyle(DWFontStyle.BOLD);
                label.setForegroundColor(iPartsEditPlugin.clPlugin_iPartsEdit_SecurityNoticeLabelForegroundColor.getColor());
                return label;
            }
        }
        return null;
    }
}
