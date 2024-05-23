/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.iPartsMaterialImportHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;
import de.docware.util.sql.SQLStringConvert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Helper für den Teilestammdatenimport aus der BOM-DB
 */
public class BOMPartMasterDataImportHelper extends EDSImportHelper implements iPartsConst {

    private static final String NOT_VALID_SOURCEFIELD = "NOT_VALID_SOURCEFIELD";

    private AbstractDataImporter importer;
    private String securitySignSourceField;
    private String certSourceField;
    private String vedocSourceField;
    private String theftRelInfoSourceField;
    private String releaseFromSourceField;
    private String releaseToSourceField;
    private Set<String> madFields;
    private boolean trimValues;

    public BOMPartMasterDataImportHelper(AbstractDataImporter importer, EtkProject project, Map<String, String> mapping,
                                         String tableName, String securitySignSourceField, String certSourceField,
                                         String vedocSourceField, String theftRelInfoSourceField, String releaseFromSourceField,
                                         String releaseToSourceField) {
        super(project, mapping, tableName);
        this.importer = importer;
        this.securitySignSourceField = StrUtils.isValid(securitySignSourceField) ? securitySignSourceField : NOT_VALID_SOURCEFIELD;
        this.certSourceField = StrUtils.isValid(certSourceField) ? certSourceField : NOT_VALID_SOURCEFIELD;
        this.vedocSourceField = StrUtils.isValid(vedocSourceField) ? vedocSourceField : NOT_VALID_SOURCEFIELD;
        this.theftRelInfoSourceField = StrUtils.isValid(theftRelInfoSourceField) ? theftRelInfoSourceField : NOT_VALID_SOURCEFIELD;
        this.releaseFromSourceField = StrUtils.isValid(releaseFromSourceField) ? releaseFromSourceField : NOT_VALID_SOURCEFIELD;
        this.releaseToSourceField = StrUtils.isValid(releaseToSourceField) ? releaseToSourceField : NOT_VALID_SOURCEFIELD;

        // MAD Felder, die nicht überschrieben werden dürfen
        madFields = new HashSet<String>();
        madFields.add(FIELD_M_QUANTUNIT); // DIALOG, MAD, PRIMUS
    }

    public static void handlePartMasterDataPostImport(AbstractBOMDataImporter importer, HashMap<iPartsPartId, PartWithVersion> partsWithNewestVersion) {
        int count = 0;
        for (BOMPartMasterDataImportHelper.PartWithVersion dataPartWithVersion : partsWithNewestVersion.values()) {
            count++;
            importer.saveToDB(dataPartWithVersion.getPart());
            importer.getMessageLog().fireProgress(count, partsWithNewestVersion.size(), "", true, true);
        }
    }

    /**
     * Check, ob das Material im ImportRecord einen neueren Änderungsstand besitzt als das bestehende Material. Falls ja,
     * wird das neue Teil in der übergebenen Map samt Änderungsstand abgelegt.
     *
     * @param dataPart
     * @param importRec
     * @param versionFromSourceField
     * @param partsWithNewestVersion
     */
    public void checkAndFillNewerPartData(iPartsDataPart dataPart, Map<String, String> importRec, String versionFromSourceField,
                                          HashMap<iPartsPartId, PartWithVersion> partsWithNewestVersion) {
        // Hat der neue Datensatz alle Prüfungen bestanden, dann wird er samt Änderungsstand gespeichert
        // Existiert zu einer Materialnummer schon ein Datensatz, dann wird der Änderungsstand verglichen. Der höhere
        // Änderungsstand wird übernommen
        if (dataPart != null) {
            int version = StrUtils.strToIntDef(handleValueOfSpecialField(versionFromSourceField, importRec), -1);
            PartWithVersion partWithVersion = partsWithNewestVersion.get(dataPart.getAsId());
            if ((partWithVersion == null) || (version >= partWithVersion.getVersion())) {
                partWithVersion = new PartWithVersion(dataPart, version);
                partsWithNewestVersion.put(dataPart.getAsId(), partWithVersion);
            }
        }
    }

    /**
     * Befüllt das übergebene Material mit den Werten aus dem Mapping. Zusätzlich werden bestimmte BOM-DB default Werte
     * gesetzt.
     *
     * @param dataPart
     * @param importRec
     * @param releaseDate
     */
    public void fillPartMasterData(EDSImportHelper helper, iPartsDataPart dataPart, Map<String, String> importRec, String releaseDate) {
        if (dataPart == null) {
            return;
        }
        // Felder mit Hilfe des Mappings überschreiben
        fillOverrideCompleteDataForEDSReverse(dataPart, importRec, iPartsEDSLanguageDefs.EDS_DE);
        // Das Zeichnungsdatum kommt aus der BOM-DB ohne Jahrhundert-Angabe. Das soll hier korrigiert werden
        helper.handleBOMImageDate(dataPart, FIELD_M_IMAGEDATE);
        // Setze das "Diebstahl relevant" Flag abhängig von der "Diebstahl relevant Information"
        setTheftRelFlagForDataPart(dataPart);
        // LAST_MODIFIED setzen für zukünftige DIALOG/BOM-DB Importe
        if (isNewerVersion()) {
            dataPart.setFieldValue(FIELD_M_LAST_MODIFIED, releaseDate, DBActionOrigin.FROM_EDIT);
        }
        // Quelle setzen
        dataPart.addSetOfEnumValueToFieldValue(FIELD_M_SOURCE, iPartsImportDataOrigin.EDS.getOrigin(), DBActionOrigin.FROM_EDIT);
        // Zur Sicherheit: Bestellnummer immer setzen
        dataPart.setFieldValue(FIELD_M_BESTNR, dataPart.getAsId().getMatNr(), DBActionOrigin.FROM_EDIT);
    }


    public iPartsDataPart getDataPart(iPartsPartId partId) {
        EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), partId);
        if (part instanceof iPartsDataPart) {
            return (iPartsDataPart)part;
        } else {
            return null;
        }
    }

    public void setTrimValues(boolean trimValues) {
        this.trimValues = trimValues;
    }

    public boolean isTrimValues() {
        return trimValues;
    }

    @Override
    protected String handleValueOfSpecialField(String sourceField, String value) {
        if (trimValues && (value != null)) {
            value = value.trim();
        }
        if (sourceField.equals(securitySignSourceField) || sourceField.equals(certSourceField) || sourceField.equals(vedocSourceField)) {
            if ((importer != null) && StrUtils.isValid(value) && !value.equals(BOM_VALUE_FALSE) && !value.equals(BOM_VALUE_TRUE)) {
                importer.getMessageLog().fireMessage(importer.translateForLog("!!Boolean Wert \"%1\" für das Element \"%2\" ist nicht " +
                                                                              "gültig und wird auf false gesetzt.", value, sourceField),
                                                     MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                value = SQLStringConvert.booleanToPPString(false);
            }
        } else if (sourceField.equals(theftRelInfoSourceField)) {
            if ((value == null) || value.trim().isEmpty()) { // Auf jeden Fall trimmen
                value = THEFT_REL_FLAG_VALUE_FALSE;
            }
        } else if (sourceField.equals(releaseFromSourceField) || sourceField.equals(releaseToSourceField)) {
            iPartsEDSDateTimeHandler dateTimeHandler = new iPartsEDSDateTimeHandler(value);
            value = dateTimeHandler.getBomDbDateValue();
        }
        return value;
    }

    @Override
    protected void importValue(EtkDataObject dataObject, String importFieldName, String dbDestFieldName, String value, Language langDef) {
        // Materialfelder, die vom MAD Importer befüllt wurde, dürfen nicht überschrieben werden.
        if (dataObject.containsFieldValueSetOfEnumValue(FIELD_M_SOURCE, iPartsImportDataOrigin.MAD.getOrigin()) && madFields.contains(dbDestFieldName)) {
            return;
        }

        // Ist der zu importierende Datensatz älter als der Datensatz in der DB, dann sollen nur die Felder importiert
        // werden, die nicht vom DIALOG Importer (und somit dem neueren Datensatz) importiert wurden
        if (skipFieldIfOlderVersion(dbDestFieldName)) {
            return;
        }

        super.importValue(dataObject, importFieldName, dbDestFieldName, value, langDef);
    }

    /**
     * Liefert zurück, ob das übergebene Feld übersprungen werden soll, wenn der neue Datensatz älter ist als der aktuelle
     *
     * @param dbDestFieldName
     * @return
     */
    public boolean skipFieldIfOlderVersion(String dbDestFieldName) {
        return !isNewerVersion() && iPartsMaterialImportHelper.isAffectedMaterialField(iPartsImportDataOrigin.DIALOG, dbDestFieldName);
    }

    /**
     * Überprüft, ob die gehaltenen Teileinformationen in der DB gespeichert werden sollen. Damit die Teile nicht bis zum Ende
     * des Importvorgangs in der Map gehalten werden (und der Speicher dadurch voll läuft), müssen diese ab einer bestimmten
     * Map-Größe gespeichert werden.
     *
     * @param importer
     * @param partId
     * @param partsWithNewestVersion
     * @param maxEntries
     * @param importToDB
     * @return
     */
    public boolean checkStoreData(AbstractDataImporter importer, iPartsPartId partId,
                                  HashMap<iPartsPartId, PartWithVersion> partsWithNewestVersion, int maxEntries, boolean importToDB) {
        if (!partsWithNewestVersion.containsKey(partId) && partsWithNewestVersion.size() >= maxEntries) {
            if (importToDB) {
                for (PartWithVersion part : partsWithNewestVersion.values()) {
                    importer.saveToDB(part.getPart());
                }
            }
            return true;
        }
        return false;
    }

    public static class PartWithVersion {

        private iPartsDataPart part;
        private int version;

        public PartWithVersion(iPartsDataPart part, int version) {
            this.part = part;
            this.version = version;
        }

        public iPartsDataPart getPart() {
            return part;
        }

        public int getVersion() {
            return version;
        }
    }
}
