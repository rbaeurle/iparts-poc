/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.inplaceditor;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ColorTableHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.iPartsGuiCodeTextField;

import java.util.List;

/**
 * Implementierung den Inplace Editors für die Aftersales Codebedingung bei Varianten mit Validierung über
 * Baureihe, Datum ab und Produktgruppe
 */
public class iPartsGuiCodeTextFieldForColorsInplaceEditor extends iPartsGuiCodeTextFieldForPartlistInplaceEditor {

    private String partlistEntrySeriesNumber;
    private AbstractJavaViewerFormIConnector connector;
    private iPartsDocumentationType documentationType;

    public iPartsGuiCodeTextFieldForColorsInplaceEditor() {
        super();
    }

    public void init(String seriesNoFromPartlistEntry, iPartsDocumentationType documentationType, RelatedInfoBaseFormIConnector connector) {
        this.partlistEntrySeriesNumber = seriesNoFromPartlistEntry;
        this.documentationType = documentationType;
        this.connector = connector;
    }

    @Override
    public boolean updateEditor(Object cellContent, List<EtkDataObject> dataObjects, int width) {
        iPartsGuiCodeTextField codeTextfield = getCodeTextfield();
        if ((codeTextfield != null) && (dataObjects != null) && !dataObjects.isEmpty()) {
            String cellText = getDBCellContentText(dataObjects, cellContent);
            String sdata = "";
            String colorTableContentProductGroup = "";
            String seriesNumber = "";

            for (EtkDataObject dataObject : dataObjects) {
                if (dataObject.getTableName().equals(iPartsConst.TABLE_DA_COLORTABLE_CONTENT)) {
                    String fieldValue = dataObject.getFieldValue(iPartsConst.FIELD_DCTC_SDATA);
                    if (!fieldValue.isEmpty()) {
                        sdata = fieldValue;
                    }
                    // Produktgruppe aus der selektierten Zeile und nicht vom Stücklisteneintrag verwenden
                    fieldValue = dataObject.getFieldValue(iPartsConst.FIELD_DCTC_PGRP);
                    if (!fieldValue.isEmpty()) {
                        colorTableContentProductGroup = fieldValue;
                    }
                } else if (dataObject.getTableName().equals(iPartsConst.TABLE_DA_COLORTABLE_DATA)) {
                    seriesNumber = ColorTableHelper.getSeriesFromColorTableOrPartListEntry(dataObject, this.partlistEntrySeriesNumber).getSeriesNumber();

                }
            }

            codeTextfield.init(connector.getProject(), documentationType, seriesNumber, colorTableContentProductGroup, sdata, "", iPartsGuiCodeTextField.CODE_TEST_TYPE.PRODUCTGRP_ONLY);
            lastValidatedInput = null;
            codeTextfield.setText(cellText);
        }
        return true;
    }

    @Override
    public String getEditResult(EtkDataObject dataObject) {
        iPartsGuiCodeTextField codeTextfield = getCodeTextfield();
        if (codeTextfield != null) {
            String text = codeTextfield.getTextCaseMode();
            if (text.isEmpty()) {
                return text;
            }
            // nur den ";" anhängen wenn die Codebedingung nicht leer ist, damit der Benutzer den Code auch explizit leer
            // lassen kann, um damit die Konstruktions-Codebedingung gültig zu machen
            return DaimlerCodes.beautifyCodeString(text);
        }
        return null;
    }
}
