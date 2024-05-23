/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.psk_partlist;

import de.docware.apps.etk.base.config.EtkConfigConst;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.StrUtils;

import java.util.Map;

class NaviCarReader extends AbstractPskReader {

    private final EditImportPSKForm editImportPSKForm;

    public NaviCarReader(EditImportPSKForm editImportPSKForm) {
        this.editImportPSKForm = editImportPSKForm;
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        EtkDataPart dataPart = checkPartFromContainer(importRec, recordNo);
        if (dataPart != null) {
            EtkDataPartListEntry selectedPartlistEntry = buildDummySelectedPartListEntry(importRec);
            if (selectedPartlistEntry != null) {
                String hotspot = handleValueOfSpecialField(EditImportPSKForm.PSK_POS, importRec);

                // iPartsConstructionPrimaryKey wird für PSK nicht benötigt und deshalb hier mit null übergeben
                ExtendedTransferToASElement transferElement = new ExtendedTransferToASElement(editImportPSKForm.getDestAssembly().getAsId(),
                                                                                              editImportPSKForm.getKgTuId(), hotspot, editImportPSKForm.getProduct(),
                                                                                              selectedPartlistEntry);
                setKatFields(transferElement, importRec);
                editImportPSKForm.addToTransferList(transferElement);
            }
        }

    }

    /**
     * bei NaviCar für Ergänzungstext
     *
     * @param transferElement
     * @param importRec
     */
    private void setKatFields(ExtendedTransferToASElement transferElement, Map<String, String> importRec) {
        editImportPSKForm.getPartListStdMapping().forEach((fieldName, stdName) -> {
            if (importRec.get(stdName) != null) {
                String value = handleValueOfSpecialField(stdName, importRec);
                transferElement.addOilFieldMapping(fieldName, value);
            }
        });
    }


    private EtkDataPart checkPartFromContainer(Map<String, String> importRec, int recordNo) {
        EtkDataPart dataPart = getDataPartFromImport(importRec);
        if (dataPart != null) {
            return dataPart;
        }
        // Fehlermeldung
        String aSachNo = importRec.get(EditImportPSKForm.PSK_PARTNO);
        if (StrUtils.isEmpty(aSachNo)) {
            editImportPSKForm.fireMessage("!!In Zeile %1: keine gültige Modulnummer für Produkt vorhanden. Wird ignoriert",
                                          String.valueOf(recordNo));
        } else {
            editImportPSKForm.fireMessage("!!In Zeile %1: keine gültige Modulnummer (%2) für Produkt vorhanden. Wird ignoriert",
                                          String.valueOf(recordNo), aSachNo);
        }
        return null;
    }

    private EtkDataPart getDataPartFromImport(Map<String, String> importRec) {
        String aSachNo = handleValueOfSpecialField(EditImportPSKForm.PSK_PARTNO, importRec);
        if (!StrUtils.isEmpty(aSachNo)) {
            PartId partId = new PartId(aSachNo, "");
            return EtkDataObjectFactory.createDataPart(editImportPSKForm.getProject(), partId);
        }
        return null;
    }

    private EtkDataPartListEntry buildDummySelectedPartListEntry(Map<String, String> importRec) {
        String matNo = handleValueOfSpecialField(EditImportPSKForm.PSK_PARTNO, importRec);
        if (StrUtils.isValid(matNo)) {
            EtkDataPartListEntry dummyPartListEntry
                    = EtkDataObjectFactory.createDataPartListEntry(editImportPSKForm.getProject(), new PartListEntryId("", "", ""));
            dummyPartListEntry.initAttributesWithEmptyValues(DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            // relevante Fields hinzufügen
            DBDataObjectAttributes attributes = dummyPartListEntry.getAttributes();
            attributes.addField(iPartsConst.FIELD_K_MENGE, handleValueOfSpecialField(EditImportPSKForm.PSK_MENGE_ART, importRec),
                                DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            attributes.addField(iPartsConst.FIELD_K_MATNR, matNo, false,
                                DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            // MBS: Strukturstufe wird mit DAIMLER-12456 von Datei gelesen bzw auf 1 gesetzt.
            attributes.addField(iPartsConst.FIELD_K_HIERARCHY, handleValueOfSpecialField(EditImportPSKForm.PSK_STRUKTURSTUFE, importRec),
                                DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            // K_SACH und K_SVER setzen
            attributes.addField(iPartsConst.FIELD_K_SACH, matNo, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
            attributes.addField(iPartsConst.FIELD_K_ART, EtkConfigConst.BAUGRUPPEKENN, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);

            return dummyPartListEntry;
        }
        return null;
    }

    protected String handleValueOfSpecialField(String sourceField, Map<String, String> importRec) {
        return handleValueOfSpecialField(sourceField, importRec.get(sourceField));
    }

    protected String handleValueOfSpecialField(String sourceField, String value) {
        if (value == null) {
            value = "";
        }
        if (sourceField.equals(EditImportPSKForm.PSK_POS)) {
            if (!isAlphaNumeric(value)) {
                value = "";
            }
        } else if (sourceField.equals(EditImportPSKForm.PSK_STRUKTURSTUFE)) {
            if (!StrUtils.isValid(value) || !StrUtils.isInteger(value)) {
                value = "1";
            }
        } else if (sourceField.equals(EditImportPSKForm.PSK_PARTNO)) {
            value = checkPartNumber(value);
        }
        return value;
    }

    private String checkPartNumber(String matNo) {
        // ModulNo muss in Liste zum Produkt sein
        if (editImportPSKForm.containsNaviCarModule(matNo)) {
            return matNo;
        }
        return "";
    }

    @Override
    protected void showMessage(String key, String... placeHolderTexts) {
        editImportPSKForm.fireMessage(key, placeHolderTexts);
    }

    @Override
    protected void showWarning(String key, String... placeHolderTexts) {
        editImportPSKForm.fireWarning(key, placeHolderTexts);
    }

    @Override
    protected void showError(String key, String... placeHolderTexts) {
        editImportPSKForm.fireError(key, placeHolderTexts);
    }

    @Override
    protected void doProgress(int pos, int maxPos) {
        editImportPSKForm.fireProgress(pos, maxPos);
    }

    @Override
    protected void doHideProgress() {
        editImportPSKForm.hideProgress();
    }
}
