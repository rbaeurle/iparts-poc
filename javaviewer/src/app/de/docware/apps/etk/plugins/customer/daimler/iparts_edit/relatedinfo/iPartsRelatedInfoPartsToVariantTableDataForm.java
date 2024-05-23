/*
 * Copyright (c) 2019 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.components.DataObjectGrid;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.relatedinfo.main.forms.AbstractRelatedInfoMainForm;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.IEtkRelatedInfo;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableToPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableToPartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDIALOGChangeHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

import java.util.ArrayList;
import java.util.List;

public class iPartsRelatedInfoPartsToVariantTableDataForm extends iPartsRelatedInfoVariantsToPartDataForm {

    private static final String CONFIG_KEY_PARTS_TO_VARIANT_TABLE_DATA_DISPLAYFIELDS = "Plugin/iPartsEdit/PartsToVariantTable";

    private final iPartsDataColorTableToPart colorTableToPart;

    protected iPartsRelatedInfoPartsToVariantTableDataForm(RelatedInfoBaseFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                           AbstractRelatedInfoMainForm relatedInfoMainForm,
                                                           IEtkRelatedInfo relatedInfo, iPartsDataColorTableToPart colorTableToPart,
                                                           boolean isRetailFilter, boolean isShowHistory) {
        super(dataConnector, parentForm, relatedInfoMainForm, relatedInfo,
              CONFIG_KEY_PARTS_TO_VARIANT_TABLE_DATA_DISPLAYFIELDS + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
              "!!Teile zu Variantentabelle:",
              CONFIG_KEY_COLOR_VARIANTS_DATA + iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS,
              "!!Varianten zu Variantentabelle:",
              true);
        getCheckboxShowHistory().switchOffEventListeners();
        getCheckboxRetailFilter().switchOffEventListeners();
        setCheckboxRetailFilterVisible(false);
        setCheckboxShowHistoryVisible(false);
        setRetailFilter(isRetailFilter);
        setShowHistory(isShowHistory);
        getCheckboxShowHistory().switchOnEventListeners();
        getCheckboxRetailFilter().switchOnEventListeners();
        this.colorTableToPart = colorTableToPart;
        String colorTableText = TranslationHandler.translate("!!Teile zu Variantentabelle \"%1\"", colorTableToPart.getAsId().getColorTableId());
        setWindowTitle(colorTableText, colorTableText);
    }

    @Override
    protected List<EtkDisplayField> createVariantTablesDataDefaultDisplayFields() {
        List<EtkDisplayField> defaultDisplayFields = new ArrayList<EtkDisplayField>();
        EtkDisplayField displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_PART, iPartsConst.FIELD_DCTP_PART, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_PART, iPartsConst.FIELD_DCTP_SDATA, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_PART, iPartsConst.FIELD_DCTP_SDATB, false, false);
        defaultDisplayFields.add(displayField);

        displayField = new EtkDisplayField(iPartsConst.TABLE_DA_COLORTABLE_PART, iPartsConst.FIELD_DCTP_STATUS, false, false);
        defaultDisplayFields.add(displayField);

        return defaultDisplayFields;
    }

    @Override
    protected DataObjectGrid createGrid(boolean top) {
        DataObjectGrid grid = super.createGrid(top);
        if (top) {
            // Teile zu Variantentabelle soll nicht aufrufbar sein, da wir bereits in dieser Form sind
            partsToVariantTableMenuItem.setVisible(false);
        }
        return grid;
    }

    @Override
    protected void variantTablesDataToGrid() {
        iPartsColorTableDataId colorTableDataId = new iPartsColorTableDataId(colorTableToPart.getAsId());
        iPartsDataColorTableToPartList colorTableToPartList =
                iPartsDataColorTableToPartList.loadColorTableToPartListForColortableId(getProject(), colorTableDataId);

        List<iPartsDataColorTableToPart> colorTableToPartListFiltered = colorTableToPartList.getAsList();
        if (!isShowHistory()) {
            ColorTableHelper.removeColorTableToPartHistoryDataFromList(colorTableToPartListFiltered);
        }

        iPartsDataColorTableData tableData = new iPartsDataColorTableData(getProject(), colorTableDataId);
        if (!tableData.existsInDB()) {
            tableData.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        }

        for (iPartsDataColorTableToPart colorTableToPart : colorTableToPartListFiltered) {
            // Setze die Info, ob Werkseinsatzdaten-Änderungen vorhanden sind
            iPartsDIALOGChangeHelper.checkSingleVariantToPartFactoryData(getProject(), colorTableToPart);

            PartId partId = new PartId(colorTableToPart.getPartNumber(), "");
            EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(getProject(), partId);
            if (!dataPart.existsInDB()) {
                dataPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }
            addDataObjectToGrid(true, tableData, colorTableToPart, dataPart);
        }
    }

    @Override
    protected void doTableSelectionChanged(boolean top) {
        // Bei Änderung der Selektion ändern sich die Daten weder im oberen noch im unteren Grid,
        // da die Variantentabelle immer die gleiche ist. Bearbeiten Buttons müssen
        // z.B bei erster Selektion trotzdem aktiviert werden.
        enableButtonsAndMenu();
    }
}
