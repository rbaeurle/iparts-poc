/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsBOMPartHistoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMPartHistory;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;

import java.util.Map;

/**
 * Helper für den Teilestammdaten für Baukasten Import aus der BOM-DB
 */
public class BOMPartHistoryImportHelper extends EDSImportHelper implements iPartsConst {

    private boolean isVakzToEmpty;
    private String revToSourceField;
    private String releaseDateFromSourceField;
    private String releaseDateToSourceField;

    public BOMPartHistoryImportHelper(EtkProject project, Map<String, String> mapping, String tableName, String revToSourceField,
                                      String releaseDateFromSourceField, String releaseDateToSourceField, boolean isVakzToEmpty) {
        super(project, mapping, tableName);
        this.revToSourceField = revToSourceField;
        this.releaseDateFromSourceField = releaseDateFromSourceField;
        this.releaseDateToSourceField = releaseDateToSourceField;
        this.isVakzToEmpty = isVakzToEmpty;
    }

    @Override
    protected String handleValueOfSpecialField(String sourceField, String value) {
        // nur bei VAKZ_BIS = leer wird der Originalwert übernommen bei den folgenden Feldern, ansonsten wird der Wert abgeändert
        if (!isVakzToEmpty) {
            if (sourceField.equals(revToSourceField)) { // Änderungsstand bis
                value = EDS_AS_BIS_UNENDLICH;
            } else if (sourceField.equals(releaseDateToSourceField)) { // Freigabetermin bis
                value = "";
            }
        }
        value = value.trim();
        if (sourceField.equals(releaseDateFromSourceField) || sourceField.equals(releaseDateToSourceField)) {
            iPartsEDSDateTimeHandler dateTimeHandler = new iPartsEDSDateTimeHandler(value);
            value = dateTimeHandler.getBomDbDateValue();
        }
        return value;
    }

    @Override
    protected void importValue(EtkDataObject dataObject, String importFieldName, String dbDestFieldName, String value, Language langDef) {
        // KEM Bis soll nur übernommen werden, wenn "VAKZ Bis" leer ist
        if (dbDestFieldName.equals(FIELD_DBMH_KEM_TO) && !isVakzToEmpty) {
            return;
        }
        super.importValue(dataObject, importFieldName, dbDestFieldName, value, langDef);
    }

    /**
     * Importiert bzw. aktualisiert den Teiletsammdaten für Baukasten Datensatz zu den übergebenen Parameter.
     *
     * @param importer
     * @param importRec
     * @param partNo
     * @param revFrom
     * @param recordNo
     * @param importToDB
     */
    public void importPartHistoryData(AbstractDataImporter importer, Map<String, String> importRec, String partNo,
                                      String revFrom, int recordNo, boolean importToDB) {
        if (StrUtils.isValid(partNo, revFrom)) {
            iPartsBOMPartHistoryId partHistoryId = new iPartsBOMPartHistoryId(partNo, "", revFrom);
            iPartsDataBOMPartHistory partHistory = new iPartsDataBOMPartHistory(getProject(), partHistoryId);
            if (!partHistory.existsInDB()) {
                partHistory.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            fillOverrideCompleteDataForEDSReverse(partHistory, importRec, iPartsEDSLanguageDefs.EDS_DE);
            if (importToDB) {
                importer.saveToDB(partHistory);
            }
        } else {
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Record %1 übersprungen: Teilenummer und " +
                                                                          "Änderungsstand dürfen nicht leer sein! " +
                                                                          "Teilenummer: %2 , Änderungsstand: %3",
                                                                          String.valueOf(recordNo), partNo, revFrom),
                                                 MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        }
    }
}
