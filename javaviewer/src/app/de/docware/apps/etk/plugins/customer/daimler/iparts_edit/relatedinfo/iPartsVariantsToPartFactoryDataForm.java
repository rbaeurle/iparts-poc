/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserControlsForColorTableAndContentFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.CopyAndPasteData;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Formular für die Werkseinsatzdaten für eine Variantentabelle am Teil.
 */
public class iPartsVariantsToPartFactoryDataForm extends AbstractVariantFactoryDataForm implements iPartsConst {

    public static final String CONFIG_KEY_VARIANT_TABLE_FACTORY_DATA = "Plugin/iPartsEdit/VariantsToPartFactoryData";
    public static final String CONFIG_KEY_VARIANT_TABLE_FACTORY_DATA_AS = "Plugin/iPartsEdit/VariantsToPartFactoryDataAfterSales";

    private iPartsColorTableToPartId colorTableToPartId;

    public iPartsVariantsToPartFactoryDataForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                               EtkDataPartListEntry partListEntry, iPartsColorTableToPartId colorTableToPartId,
                                               iPartsSeriesId filterSeriesId, boolean enableEditMode, boolean retailFilter) {
        super(dataConnector, parentForm, CONFIG_KEY_VARIANT_TABLE_FACTORY_DATA_AS + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
              CONFIG_KEY_VARIANT_TABLE_FACTORY_DATA + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
              partListEntry, filterSeriesId, enableEditMode, retailFilter);
        String factoryDataText = TranslationHandler.translate("!!Werkseinsatzdaten zu %1", colorTableToPartId.getColorTableId());
        iPartsDataColorTableToPart colorTableToPart = new iPartsDataColorTableToPart(getProject(), colorTableToPartId);
        String formattedPartNo = iPartsNumberHelper.formatPartNo(getProject(), colorTableToPart.getFieldValue(FIELD_DCTP_PART), getProject().getDBLanguage());
        setWindowTitle(factoryDataText, factoryDataText + ' ' + TranslationHandler.translate("!!für Sachnummer \"%1\"", formattedPartNo));
        this.colorTableToPartId = colorTableToPartId;
        setReadOnly(isRetailFilter() || !(editMode && isEditContext(dataConnector, true)));
        createCopyPasteToolbarbuttons();
        dataToGrid();
    }

    @Override
    protected void createAndAddDataObjectsToGrid(boolean top) {
        iPartsDataColorTableFactoryList factoryDataList;

        if (top) {
            if (isRetailFilter()) { // Anstatt Werkseinsatzdaten After-Sales die Daten verdichtet aus den Datenstrukturen vom Stücklisteneintrag anzeigen
                iPartsDataColorTableFactoryList colorToPartFactoryDataListForRetail = ColorTableHelper.getColorToPartFactoryDataListForRetail(getPartListEntry(), colorTableToPartId,
                                                                                                                                              getFilterSeriesId(), getProject(), responseDataForPemRetailFilter);
                addDataObjectListToGrid(top, colorToPartFactoryDataListForRetail);
                return;
            } else {
                factoryDataList = iPartsDataColorTableFactoryList.loadColorTableFactoryListForColorTableToPartIdForAS(getProject(),
                                                                                                                      colorTableToPartId);
                // ungefilterte Daten zum Kopieren merken
                listOfUnfilteredFactoryData.addAll(factoryDataList.getAsList());
            }
        } else {
            // Werkseinsatzdaten Konstruktion
            factoryDataList = iPartsDataColorTableFactoryList.loadColorTableFactoryListForColorTableToPartId(getProject(),
                                                                                                             colorTableToPartId);
            listOfUnfilteredFactoryData.addAll(factoryDataList.getAsList());
        }
        // Werksdaten filtern
        filterFactoryData(factoryDataList);
        addVirtualFieldsForResponseData(factoryDataList);
        iPartsDataColorTableFactoryList result = new iPartsDataColorTableFactoryList();
        result.addAll(factoryDataList, DBActionOrigin.FROM_DB);
        addDataObjectListToGrid(top, result);

        enableButtonsAndMenu();
    }

    @Override
    protected void doNew(boolean top) {
        iPartsDataColorTableFactory result = EditUserControlsForColorTableAndContentFactoryData.showCreateColorTableToPartFactoryData(
                getConnector(), this, colorTableToPartId);

        if (result != null) {
            // handelt eventuelle Statusänderungen ab inkl. dem speichern des aktuellen Eintrags im Changeset
            statusChangedForDataObject(result, top);
        }
    }

    @Override
    protected void doEdit(boolean top) {
        iPartsDataColorTableFactory selectedColorTableFactory = getColorTableFactoryFromSelection(top);
        if (selectedColorTableFactory != null) {
            iPartsDataColorTableFactory result = EditUserControlsForColorTableAndContentFactoryData.showCreateColorTableToPartFactoryData(
                    getConnector(), this, colorTableToPartId,
                    selectedColorTableFactory.getAsId());
            if (result != null) {
                // bei Statusänderungen diese Abhandeln und den aktuellen Eintrag im Changeset speichern
                if (selectedColorTableFactory.getReleaseState() != result.getReleaseState()) {
                    statusChangedForDataObject(result, top);
                } else {
                    saveDataObjectWithUpdate(result);
                }
            }
        }
    }


    @Override
    public List<iPartsDataDIALOGChange> getDataDIALOGChanges(EtkDataObject dataObject) {
        List<iPartsDataDIALOGChange> result = new ArrayList<>(1);
        if (isRelatedInfoEditContext() && (dataObject instanceof iPartsDataColorTableFactory)) {
            iPartsDataColorTableFactory factoryData = (iPartsDataColorTableFactory)dataObject;
            iPartsColorTableDataId colorTableDataId = new iPartsColorTableDataId(factoryData.getAsId().getTableId());
            iPartsDataColorTableData colorTableData = new iPartsDataColorTableData(getProject(), colorTableDataId);
            String series;
            // Normalerweise kann die Baureihe aus der Tabelle DA_COLORTABLE_DATA ausgelesen werden. Falls es zu der
            // Variantentabelle keinen Eintrag in DA_COLORTABLE_DATA gibt wird die Baureihe aus der QFT Nummer gelesen,
            // damit sichergestellt wird, dass die passenden Einträge in DA_DIALOG_CHANGES gefunden werden.
            if (colorTableData.existsInDB()) {
                series = colorTableData.getValidSeries();
            } else {
                series = ColorTableHelper.extractSeriesNumberFromTableId(factoryData.getAsId().getTableId());
            }

            iPartsDialogChangesId dialogChangesId = new iPartsDialogChangesId(iPartsDataDIALOGChange.ChangeType.COLORTABLE_FACTORY_DATA,
                                                                              factoryData.getAsId(), series, "",
                                                                              getPartListEntry().getPart().getAsId().getMatNr(), "");
            result.add(new iPartsDataDIALOGChange(getProject(), dialogChangesId));
        }
        return result;
    }

    @Override
    protected void pasteFactoryData() {
        iPartsDataColorTableFactoryList results = CopyAndPasteData.pasteFactoryDataOfColortableToPart(getProject(), colorTableToPartId);
        if (results != null) {
            // bei Statusänderungen diese Abhandeln und den aktuellen Eintrag im Changeset speichern
            // Werkseinsatzdaten werden als AS-Werkseinsatzdaten eingefügt
            statusChangedForDataObjects(results, true);
        }
    }
}