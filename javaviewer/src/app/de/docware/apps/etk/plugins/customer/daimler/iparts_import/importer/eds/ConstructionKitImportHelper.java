/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * verwaltet die notwendigen Funktionen, die zwischen Tabelle BK und BKV identisch sind, bis auf die Spaltennamen
 */
@Deprecated
public class ConstructionKitImportHelper {

    public static final String IPARTS_DATETIME_UNTIL_ETERNITY = ""; // oder "99999999999999"


    public EtkProject project;

    public String FRG_KZ_AB;
    public String FRG_DAT_AB;
    public String FRG_KZ_BIS;
    public String AS_AB;
    public String AS_BIS;
    public String FRG_DAT_BIS;
    public String KEM_BIS;
    public String SPS;
    public String dbTableName;
    public HashMap<String, String> mapping;

    public boolean isReleasedDataSet(Map<String, String> importRec) {
        return getFRG_KZ_AB(importRec).equals("0");
    }

    public String getFRG_KZ_BIS(Map<String, String> importRec) {
        return importRec.get(FRG_KZ_BIS);
    }

    public String getFRG_KZ_AB(Map<String, String> importRec) {
        return importRec.get(FRG_KZ_AB);
    }

    public DBDataObjectAttributesList getExistingRecord(Map<String, String> importRec, String[] primaryKeys) {
        // Um einen Vorgänger zu finden, wird nach BK_SNR, BK_POS und BK_SNRU sowie
        // BK_AS_BIS="999" und BK_FRG_DAT_BIS="99999999999999" (bzw leer) gesucht
        String[] primaryKeysDBFields = new String[primaryKeys.length + 2];
        String[] primaryValues = new String[primaryKeys.length + 2];
        for (int i = 0; i < primaryKeys.length; i++) {
            primaryValues[i] = importRec.get(primaryKeys[i]);
            primaryKeysDBFields[i] = mapping.get(primaryKeys[i]);
        }
        primaryKeysDBFields[primaryKeys.length] = mapping.get(AS_BIS);
        primaryValues[primaryKeys.length] = EDSImportHelper.EDS_AS_BIS_UNENDLICH;
        primaryKeysDBFields[primaryKeys.length + 1] = mapping.get(FRG_DAT_BIS);
        primaryValues[primaryKeys.length + 1] = IPARTS_DATETIME_UNTIL_ETERNITY;

        return project.getDbLayer().getAttributesList(dbTableName, primaryKeysDBFields, primaryValues);
    }

    public void fillCompleteData(EtkDataObject dataObject, Map<String, String> importRec,
                                 List<String> specialFields) {
        String frgKzBis = getFRG_KZ_BIS(importRec);

        // über alle Mapping Felder
        for (String sourceField : mapping.keySet()) {
            // Name und value in der DB bestimmen
            String destField = mapping.get(sourceField);
            String value = importRec.get(sourceField);
            if (value != null) {
                // Sonderbehandlung für MultiLanguage-Felder
                if (project.getFieldDescription(dbTableName, destField).isMultiLanguage()) {
                    // SprachKennung holen
                    iPartsEDSLanguageDefs langDef = iPartsEDSLanguageDefs.getType(importRec.get(SPS));
                    fillOverrideOneLanguageText(dataObject, langDef, destField, value);
                } else {
                    // Sonderbehandlung für spezielle Import Felder
                    value = handleValueOfSpecialField(sourceField, value, frgKzBis);
                    if (value != null) {
                        // Attributwert im DataObject setzen
                        dataObject.setAttributeValue(destField, value, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
        }
    }

    public void updateData(EtkDataObject dataObject, Map<String, String> importRec, List<String> specialFields) {
        String frgKzBis = getFRG_KZ_BIS(importRec);

        // über alle Mapping Felder
        for (String sourceField : mapping.keySet()) {
            if (specialFields.contains(sourceField)) {
                // Name und value in der DB bestimmen
                String destField = mapping.get(sourceField);
                String value = importRec.get(sourceField);
                if (value != null) {
                    // Sonderbehandlung für MultiLanguage-Felder
                    if (project.getFieldDescription(dbTableName, destField).isMultiLanguage()) {
                        //kann hier nicht vorkommen
                    } else {
                        // Sonderbehandlung für spezielle Import Felder
                        value = handleValueOfSpecialField(sourceField, value, frgKzBis);
                        if (value != null) {
                            // Attributwert im DataObject setzen
                            dataObject.setAttributeValue(destField, value, DBActionOrigin.FROM_EDIT);
                        }
                    }
                }
            }
        }
    }

    public void fillOverrideOneLanguageText(EtkDataObject dataObject, iPartsEDSLanguageDefs langDef, String destField, String value) {
        if ((langDef != iPartsEDSLanguageDefs.EDS_UNKNOWN) && (value != null)) {
            EtkMultiSprache multiSprache = dataObject.getFieldValueAsMultiLanguage(destField);
            multiSprache.setText(langDef.getDbValue().getCode(), value);
            dataObject.setFieldValueAsMultiLanguage(destField, multiSprache, DBActionOrigin.FROM_EDIT);
        }
    }


    /**
     * Wandelt spezielle EDS-Formate in normale iParts-Formate. Kann Null zurückliefern, wenn das Format falsch ist
     *
     * @param sourceField
     * @param value
     * @param frgKzBis
     * @return
     */
    public String handleValueOfSpecialField(String sourceField, String value, String frgKzBis) {
        if (frgKzBis.equals("1")) {
            if (sourceField.equals(AS_BIS)) {
                value = EDSImportHelper.EDS_AS_BIS_UNENDLICH;
            } else if (sourceField.equals(KEM_BIS)) {
                value = "";
            } else if (sourceField.equals(FRG_DAT_BIS)) {
                value = IPARTS_DATETIME_UNTIL_ETERNITY;
            }
        }
        //hier ggf das SAA-DateTime aus der Excel Notation konvertieren
        if (sourceField.equals(FRG_DAT_AB) || sourceField.equals(FRG_DAT_BIS)) {
            iPartsEDSDateTimeHandler dtHandler = new iPartsEDSDateTimeHandler(value);
            value = dtHandler.getDBDateTime();
        }
        // Anderungsstand ist 3-stellig
        if (sourceField.equals(AS_AB) || sourceField.equals(AS_BIS)) {
            value = StrUtils.prefixStringWithCharsUpToLength(value, '0', 3);
        }
        return value;
    }

}
