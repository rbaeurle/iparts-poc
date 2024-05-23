/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.TransmissionIdentKeys;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactories;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADDateTimeHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADFixedPointHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsMADLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.dialog.iPartsCatalogImportWorker;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.util.StrUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Hilfsklasse für das Umsetzen der ImportRecord Data auf die Attribute-Data via Mapping
 * Erweiterung für Migration von MAD
 */
public class MADImportHelper extends iPartsMainImportHelper {

    protected static final String MAD_NULL_VALUE = "(null)";
    private static final Set<String> SPECIAL_AGG_TYPES = new HashSet<String>(Arrays.asList(
            new String[]{ TransmissionIdentKeys.TRANSMISSION_MECHANICAL, TransmissionIdentKeys.TRANSMISSION_AUTOMATED }
    ));

    public MADImportHelper(EtkProject project, Map<String, String> mapping, String tableName) {
        super(project, mapping, tableName);
    }

    /**
     * Die Import-Werte komplett in einen Datensatz übertragen (DIALOG spezifisch)
     * Das Mapping besitzt dabei den folgenden Aufbau:
     * Key = DB-FieldName, Value = Import-Spaltenname
     *
     * @param dataObject
     * @param importRec
     * @param langDef    DIALOG spezifisch
     */
    public void fillOverrideCompleteDataForMADReverse(EtkDataObject dataObject, Map<String, String> importRec, iPartsMADLanguageDefs langDef) {
        Language language = (langDef != null) ? langDef.getDbValue() : null;
        fillOverrideCompleteDataReverse(dataObject, importRec, language);
    }

    public String getMADDateTimeValue(String value) {
        iPartsMADDateTimeHandler dtHandler = new iPartsMADDateTimeHandler(value);
        value = dtHandler.getDBDateTime();
        if (value == null) {
            value = "";
        }
        return value;
    }

    public String getMADFixedPointValue(String value) {
        iPartsMADFixedPointHandler fpHandler = new iPartsMADFixedPointHandler(4, 3);
        return fpHandler.handleFixedPointNumber(value);
    }

    /**
     * Konvertiert den MAD Mengenwert in das gewünschte Mengenformat in der DB. Aus MAD kommen Werte wie z.B. "0001000"
     * oder 0001010. Diese werden zu 1 und 1.01 konvertiert.
     *
     * @param value
     * @return
     */
    public String convertToMADFixedPointQuantityValue(String value) {
        return checkQuantityFormat(getMADFixedPointValue(value));
    }

    /**
     * Liefert das Werk zur PEM bzw. ggf. mehrere Werke für AS-PEMs.
     * Für die AS-PEMs wurde beim vorhergehenden Werkseinsatzdatenimport (PODW) und Farbtabellenimport (FTTE, FTAB) ein eintsprechender Cache aufgebaut.
     *
     * @param pem
     * @param br                  Baureihe, nur relevant für AS-PEMs
     * @param aa                  AA, nur relevant für AS-PEMs
     * @param catalogImportWorker
     * @return
     */
    public Set<String> getFactoriesForPem(String pem, String br, String aa, iPartsCatalogImportWorker catalogImportWorker) {
        Set<String> factories = null;
        if (isASPem(pem)) {
            if ((catalogImportWorker != null) && StrUtils.isValid(br)) {
                factories = getFactoriesForASPemFromCache(catalogImportWorker.getAsPemToFactoriesMap(), pem, br, aa);
            }

            // Wenn im ersten Versuch mit AA nichts gefunden wurde, nochmal ohne AA suchen. Damit auch Cache Einträge
            // die vom FTTE und FTAB Importer angelegt wurden gefunden werden können
            if ((factories == null) && (catalogImportWorker != null) && StrUtils.isValid(aa)) {
                factories = getFactoriesForASPemFromCache(catalogImportWorker.getAsPemToFactoriesMap(), pem, br, "");
            }

            // Wenn dann immer noch nichts gefunden wurde setze Werk "006" (sollte nicht mehr vorkommen)
            if (factories == null) {
                factories = new HashSet<>();
                factories.add("006");
            }
        } else {
            // Der "normale" Weg, um an das Werk zu einer PEM zu kommen
            String factory = iPartsFactories.getInstance(getProject()).getFactoryNumberForPEMAndDataSource(pem, iPartsImportDataOrigin.MAD);
            if (factory != null) {
                factories = new HashSet<>();
                factories.add(factory);
            }
        }
        return factories;
    }

    /**
     * Sonderbehandlung für die Aggregate-Kennzeichen(AK) "GM" und "GA".
     *
     * @param currentAggType
     * @return
     */
    public boolean checkIFSpecialAggType(String currentAggType) {
        return SPECIAL_AGG_TYPES.contains(currentAggType);
    }

    /**
     * Sonderbehandlung für die Aggregate-Kennzeichen(AK) "GM" und "GA". Diese AK kommen bei DIALOG nicht vor, sondern nur bei MAD.
     * In DIALOG würden Produkte mit diesen Werten das AK "G" erhalten. Da wir bei einem DIALOG Import diese Zusatzinformation aber
     * nicht verlieren möchetn, wird geprüft, ob das aktuelle AK eines der Sonder-AKs und das neue AK ein "G" ist. Sollte
     * das der Fall sein, dann wird "true" zurückgegeben (true == ja, es handelt sich um den Sonderfall).
     *
     * @param currentAggType
     * @param newAggType
     * @return
     */
    public boolean checkIfSpecialAggTypeCase(String currentAggType, String newAggType) {
        if (checkIFSpecialAggType(currentAggType) && newAggType.toUpperCase().equals("G")) {
            return true;
        }
        return false;
    }

    /**
     * Hilfsmethode um {@link iPartsDialogBCTEPrimaryKey} Objekte mit den übergebenen Werten zu erzeugen
     *
     * @param importer
     * @param recordNo
     * @param series
     * @param hmmsmKey
     * @param posv
     * @param ww
     * @param etz
     * @param aa
     * @param sda
     * @return
     */
    public iPartsDialogBCTEPrimaryKey getPartListPrimaryBCTEKey(AbstractDataImporter importer, int recordNo,
                                                                String series, String hmmsmKey, String posv, String ww,
                                                                String etz, String aa, String sda) {
        if (StrUtils.isEmpty(hmmsmKey) || (hmmsmKey.length() != 10)) {
            importer.getMessageLog().fireMessage(importer.translateForLog("!!Record %1 fehlerhaft (ungültiger HM/M/SM Strukturschlüssel: %2)",
                                                                          String.valueOf(recordNo), hmmsmKey),
                                                 MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
            return null;
        }
        String hm = hmmsmKey.substring(0, 2);
        String m = hmmsmKey.substring(2, 4);
        String sm = hmmsmKey.substring(4, 6);
        String pose = hmmsmKey.substring(6, 10);
        HmMSmId hmMSmId = new HmMSmId(series, hm, m, sm);

        return getPartListPrimaryBCTEKey(importer, recordNo, hmMSmId, pose, posv, ww, etz, aa, sda);


    }
}
