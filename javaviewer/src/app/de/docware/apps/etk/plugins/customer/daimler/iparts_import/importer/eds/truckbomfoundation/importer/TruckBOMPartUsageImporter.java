/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.importer;

import com.owlike.genson.Genson;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.common.EtkDataTextEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.iPartsDictPrefixAndSuffix;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMMultiLangData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.TruckBOMSingleKEM;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpartusage.TruckBOMPartUsageData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpartusage.TruckBOMPartUsageVersion;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.transferobjects.getpartusage.TruckBOMSinglePartUsage;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.BOMConstructionKitImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSRemarkTextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.truckbomfoundation.helper.TruckBOMFoundationDataCorrectionHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update.BOMConstructionKitContentUpdateImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update.BOMConstructionKitTextUpdateImporter;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.JSONUtils;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.misc.id.IdWithType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Importer für die Baukastenstrukturdaten aus TruckBOM.foundation
 */
public class TruckBOMPartUsageImporter extends AbstractTruckBOMFoundationJSONImporter {

    protected static final String EMPTY_MATURITY_VALUE = "_";
    private static final String DEFAULT_TEXT_TYPE_VALUE = "V";

    public TruckBOMPartUsageImporter(EtkProject project) {
        super(project, TRUCK_BOM_FOUNDATION_PARTS_USAGE_IMPORT_NAME, TABLE_DA_EDS_CONST_KIT + " & " + TABLE_DA_EDS_CONST_PROPS);
    }

    @Override
    protected boolean removeAllExistingData(FilesImporterFileListType importFileType) {
        return false;
    }

    @Override
    protected Map<String, AbstractBOMXMLDataImporter> createSubImporter() {
        Map<String, AbstractBOMXMLDataImporter> importer = new HashMap<>();
        importer.put(TABLE_DA_EDS_CONST_KIT, new BOMConstructionKitContentUpdateImporter(getProject()));
        importer.put(TABLE_DA_EDS_CONST_PROPS, new BOMConstructionKitTextUpdateImporter(getProject()));
        return importer;
    }

    @Override
    protected boolean importJSONResponse(String response) {
        String fileName = getImportName(getProject().getDBLanguage());
        Genson genson = JSONUtils.createGenson(true);
        try {
            TruckBOMPartUsageData truckBOMPartUsageData = deserializeFromString(genson, response, fileName, TruckBOMPartUsageData.class);
            if (truckBOMPartUsageData == null) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine gültigen Werte.", fileName);
                return false;
            }
            if (!checkObjectCount(truckBOMPartUsageData)) {
                return true;
            }
            // Alle Baukastenstruktur-Stammdaten
            List<TruckBOMSinglePartUsage> singlePartUsageList = truckBOMPartUsageData.getPartUsage();
            if ((singlePartUsageList == null) || singlePartUsageList.isEmpty()) {
                // Fehlermeldung
                fireWarningLF("!!Die Importdatei \"%1\" enthält keine Baukastenstrukturdaten.", fileName);
                return false;
            }

            // Verknüpfung Baukastenstruktur zu KEM ab Daten
            Map<String, TruckBOMSingleKEM> idsToKemFromData = checkKEMFromData(truckBOMPartUsageData, fileName);
            if (idsToKemFromData == null) {
                return false;
            }
            // Verknüpfung Baukastenstruktur zu KEM bis Daten
            Map<String, TruckBOMSingleKEM> idsToKemToData = checkKEMToData(truckBOMPartUsageData, fileName);
            // SubImporter starten
            PartUsageValueReader partUsageValueReader = new PartUsageValueReader(getSavedJSONFile(), singlePartUsageList,
                                                                                 idsToKemFromData, idsToKemToData,
                                                                                 checkDistributionTasksData(truckBOMPartUsageData,
                                                                                                            fileName));
            if (startSubImporter(TABLE_DA_EDS_CONST_KIT, partUsageValueReader)) {
                // Nach dem Import die Daten aktualisieren
                updateConstKitData(singlePartUsageList);
            } else {
                logSkipImport(TABLE_DA_EDS_CONST_KIT);
            }

            PartUsageTextValueReader partUsageTextValueReader = new PartUsageTextValueReader(getSavedJSONFile(),
                                                                                             singlePartUsageList,
                                                                                             idsToKemFromData,
                                                                                             idsToKemToData,
                                                                                             getSubImporters().get(TABLE_DA_EDS_CONST_PROPS));
            if (startSubImporter(TABLE_DA_EDS_CONST_PROPS, partUsageTextValueReader)) {
                // Nach dem Import die Daten aktualisieren
                updateConstPropsData(singlePartUsageList);
            } else {
                logSkipImport(TABLE_DA_EDS_CONST_PROPS);
            }

            importRemarks(singlePartUsageList);
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsImportPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
            fireErrorLF("!!Fehler beim Importieren der Baukastenstrukturdaten aus TruckBOM.foundation");
            return false;
        }
        return true;
    }

    /**
     * Importiert die Bemerkungen für die SAA oder den Baukasten
     *
     * @param singlePartUsageList
     */
    private void importRemarks(List<TruckBOMSinglePartUsage> singlePartUsageList) {
        // Helper, um zu unterscheiden, ob es eine SAA oder ein Baukasten ist
        iPartsNumberHelper numberHelper = new iPartsNumberHelper();
        // Cache mit Sprache auf Text auf EtkMultiSprache Objekt
        Map<String, Map<String, EtkMultiSprache>> textCache = new HashMap<>();
        // Alle partUsage Elemente durchlaufen
        singlePartUsageList.forEach(singlePartUsage -> {
            // Position 6-stellig bestimmen, weil die Position anstelle der alten Bemerkungskennziffer in den Schlüssel
            // eingetragen wird
            String position = BOMConstructionKitImportHelper.convertIntoEDSBCSPosValue(singlePartUsage.getPosition());
            if (StrUtils.isEmpty(position)) {
                return;
            }
            // Die Bemerkung stehen an den Versionen (Änderungsstand)
            List<TruckBOMPartUsageVersion> singlePartUsageVersions = singlePartUsage.getPartUsageVersion();
            if (singlePartUsageVersions != null) {
                // Änderungsstände durchlaufen
                singlePartUsageVersions.stream()
                        // Nur die Versionen berücksichtigen, die eine Bemerkung haben
                        .filter(singlePartUsageVersion -> (singlePartUsageVersion.getRemark() != null)
                                                          && !singlePartUsageVersion.getRemark().isEmpty())
                        .forEach(singlePartUsageVersion -> {
                            // SAA oder Baukasten bestimmen
                            String saaOrConstKit = singlePartUsage.getPartUsageParentElementIdentifier();
                            if (numberHelper.isValidSaaOrBk(saaOrConstKit, true)) {
                                // Änderungsstand
                                String version = singlePartUsageVersion.getVersion();
                                // Text aus der Importdatei bestimmen
                                EtkMultiSprache text = singlePartUsageVersion.getRemarkAsMultiLangObject();
                                if (text.allStringsAreEmpty()) {
                                    return;
                                }
                                EtkDataObject dataObject;
                                // Unterscheidung SAA <-> Baukasten
                                if (numberHelper.isValidSaa(saaOrConstKit, true)) {
                                    // SAA
                                    dataObject = createSaaDataObject(saaOrConstKit, version, position, text, textCache);
                                } else {
                                    // Baukasten
                                    dataObject = createConstKitDataObject(saaOrConstKit, version, position, text, textCache);
                                }
                                if (dataObject != null) {
                                    saveToDB(dataObject);
                                }
                            }
                        });
            }
        });
    }

    /**
     * Erzeugt ein {@link iPartsDataMaterialRemark} für den Baukasten inkl. Bestimmung des mehrsprachigen Texts via
     * Cache oder DB Suche
     *
     * @param saaOrConstKit
     * @param version
     * @param position
     * @param text
     * @param textCache
     * @return
     */
    private EtkDataObject createConstKitDataObject(String saaOrConstKit, String version, String position, EtkMultiSprache text,
                                                   Map<String, Map<String, EtkMultiSprache>> textCache) {
        // Für den Baukasten und den Änderungsstand alle Texte aus der DB bestimmen
        iPartsDataMaterialRemarkList remarksList = iPartsDataMaterialRemarkList.loadAllRemarksForMaterialAndRevisionFromDB(getProject(), saaOrConstKit, version);
        if (!remarksList.isEmpty()) {
            // Check, ob alle Bemerkungen aus dem MQ Import stammen. Falls ja, nicht importieren
            if (hasRemarksFromQueueImport(remarksList, FIELD_DEMR_REMARK_NO)) {
                return null;
            }
        }
        // DataObject aus den geladenen DB Objekten bestimmen. Falls der Datensatz neu ist, initialisieren
        iPartsMaterialRemarksId id = new iPartsMaterialRemarksId(saaOrConstKit, version, position);
        EtkDataObject dataObject = getDataObjectForId(remarksList, id);
        if (dataObject == null) {
            dataObject = new iPartsDataMaterialRemark(getProject(), id);
            dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        // Den Text aus dem Import in der DB oder im Cache suchen
        EtkMultiSprache textForObject = checkTextObject(text, textCache, TABLE_DA_EDS_MAT_REMARKS, FIELD_DEMR_TEXT);
        dataObject.setFieldValueAsMultiLanguage(FIELD_DEMR_TEXT, textForObject, DBActionOrigin.FROM_EDIT);
        return dataObject;
    }

    /**
     * Erzeugt ein {@link iPartsDataSaaRemarks} für die SAA inkl. Bestimmung des mehrsprachigen Texts via
     * Cache oder DB Suche
     *
     * @param saaOrConstKit
     * @param version
     * @param position
     * @param text
     * @param textCache
     * @return
     */
    private EtkDataObject createSaaDataObject(String saaOrConstKit, String version, String position, EtkMultiSprache text,
                                              Map<String, Map<String, EtkMultiSprache>> textCache) {
        // Für die SAA und den Änderungsstand alle Texte aus der DB bestimmen
        iPartsDataSaaRemarksList remarksList = iPartsDataSaaRemarksList.loadAllRemarksForSAARevision(getProject(),
                                                                                                     saaOrConstKit,
                                                                                                     version);
        if (!remarksList.isEmpty()) {
            // Check, ob alle Bemerkungen aus dem MQ Import stammen. Falls ja, nicht importieren
            if (hasRemarksFromQueueImport(remarksList, FIELD_DESR_REMARK_NO)) {
                return null;
            }
        }
        // DataObject aus den geladenen DB Objekten bestimmen. Falls der Datensatz neu ist, initialisieren
        iPartsSaaRemarksId id = new iPartsSaaRemarksId(saaOrConstKit, version, position);
        EtkDataObject dataObject = getDataObjectForId(remarksList, id);
        if (dataObject == null) {
            dataObject = new iPartsDataSaaRemarks(getProject(), id);
            dataObject.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        }
        // Den Text aus dem Import in der DB oder im Cache suchen
        EtkMultiSprache textForObject = checkTextObject(text, textCache, TABLE_DA_EDS_SAA_REMARKS, FIELD_DESR_TEXT);
        dataObject.setFieldValueAsMultiLanguage(FIELD_DESR_TEXT, textForObject, DBActionOrigin.FROM_EDIT);
        return dataObject;
    }

    /**
     * Überprüft, ob der Text aus dem Import schon im Cache liegt. Falls nicht, wird in der DB gesucht. Falls er dort
     * ebenfalls nicht vorkommt, wird er mit einer neuen Text-Id angelegt.
     *
     * @param textFromImport
     * @param textCache
     * @param tableName
     * @param textField
     * @return
     */
    private EtkMultiSprache checkTextObject(EtkMultiSprache textFromImport, Map<String, Map<String, EtkMultiSprache>> textCache,
                                            String tableName, String textField) {
        EtkMultiSprache multiLang = null;
        // Check, ob der Text schon einmal vorkam (Cache während dem Import). Dafür wird für jede Sprache ein Eintrag
        // im Cache gesucht
        for (Map.Entry<String, String> entry : textFromImport.getLanguagesAndTexts().entrySet()) {
            String lang = entry.getKey();
            String text = entry.getValue();
            Map<String, EtkMultiSprache> textToObject = textCache.get(lang);
            if (textToObject == null) {
                continue;
            }
            EtkMultiSprache textObject = textToObject.get(text);
            if (textObject == null) {
                continue;
            }
            multiLang = textObject;
            break;
        }
        boolean addToCache = false;
        // Suche nach dem Text in der DB, falls kein Treffer im Cache
        if (multiLang == null) {
            for (Map.Entry<String, String> entry : textFromImport.getLanguagesAndTexts().entrySet()) {
                EtkDataTextEntryList textIds = EDSRemarkTextHelper.searchRemarkInDB(getProject(), entry.getValue(), tableName,
                                                                                    textField, Language.findLanguage(entry.getKey()));
                if (!textIds.isEmpty()) {
                    // Text gefunden -> Treffer aus DB nutzen
                    multiLang = getProject().getDbLayer().getLanguagesTextsByTextId(textIds.get(0).getAsId().getTextNr());
                    // Aus DB geladen und noch nicht im Cache -> Zum Cache hinzufügen
                    addToCache = true;
                    break;
                }
            }
        }
        if (multiLang == null) {
            // Neuer Text -> Text-Id anlegen
            multiLang = textFromImport;
            multiLang.setTextId(DictHelper.buildTextId(iPartsDictPrefixAndSuffix.EDS_BCS_REMARKS.getPrefixValue(),
                                                       StrUtils.makeGUID()));
            // Neues mehrsprachiges Objekt, das nicht im Cache liegt und nicht in der DB existiert -> Zum Cache hinzufügen
            addToCache = true;
        } else if (!multiLang.equals(textFromImport)) {
            // Mögliche neue Texte zum Cache Objekt hinzufügen
            String textId = multiLang.getTextId();
            // Cache Objekt die Texte aus dem Import hinzufügen
            multiLang.assignData(textFromImport);
            // Text-id wieder setzen, da sie beim Zusammenlegen der Texte geleert wurde
            multiLang.setTextId(textId);
            // DB Objekt oder Cache-Objekt, dass angepasst wurde -> Zum Cache hinzufügen
            addToCache = true;
        }
        if (addToCache) {
            // Neues, aus der DB geladenes oder angepasstest Objekt im Cache ablegen
            addToTextCache(multiLang, textCache);
        }
        return multiLang;
    }

    /**
     * Fügt den übergebenen Text dem Cache hinzu
     *
     * @param multiLang
     * @param textCache
     */
    private void addToTextCache(EtkMultiSprache multiLang, Map<String, Map<String, EtkMultiSprache>> textCache) {
        multiLang.getLanguagesAndTexts().forEach((lang, text) -> {
            if (StrUtils.isValid(text)) {
                Map<String, EtkMultiSprache> textToObject = textCache.computeIfAbsent(lang, k -> new HashMap<>());
                textToObject.put(text, multiLang);
            }
        });
    }

    /**
     * Liefert aus der übergebenen Liste das Objekt zurück, zu dem die übergeben Id passt
     *
     * @param remarksList
     * @param id
     * @return
     */
    private EtkDataObject getDataObjectForId(EtkDataObjectList<? extends EtkDataObject> remarksList, IdWithType id) {
        for (EtkDataObject dataObject : remarksList) {
            if (dataObject.getAsId().equals(id)) {
                return dataObject;
            }
        }
        return null;
    }

    /**
     * Überprüft, ob alle Bemerkungen aus dem alten MQ Import stammen
     *
     * @param remarksList
     * @param remarkNoFieldName
     * @return
     */
    private boolean hasRemarksFromQueueImport(EtkDataObjectList<? extends EtkDataObject> remarksList, String remarkNoFieldName) {
        if ((remarksList == null) || remarksList.isEmpty() || StrUtils.isEmpty(remarkNoFieldName)) {
            return false;
        }
        for (EtkDataObject remarkData : remarksList) {
            String remarkNo = remarkData.getFieldValue(remarkNoFieldName);
            if (StrUtils.isValid(remarkNo) && (remarkNo.length() == 6)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Aktualisiert die Verknüpfung zwischen bestehenden und importierten Daten (KEM bis, AS bis und Datum bis) für
     * die Tabelle DA_EDS_CONST_PROPS
     *
     * @param singlePartUsageList
     */
    private void updateConstPropsData(List<TruckBOMSinglePartUsage> singlePartUsageList) {
        TruckBOMFoundationDataCorrectionHelper dataTransferHelper = new TruckBOMFoundationDataCorrectionHelper(this,
                                                                                                               FIELD_DCP_REVFROM,
                                                                                                               FIELD_DCP_REVTO,
                                                                                                               EDSImportHelper.EDS_AS_BIS_UNENDLICH);
        dataTransferHelper.addFields(FIELD_DCP_KEMFROM, FIELD_DCP_KEMTO);
        dataTransferHelper.addFields(FIELD_DCP_RELEASE_FROM, FIELD_DCP_RELEASE_TO);

        singlePartUsageList.forEach(singlePartUsageObject -> {
            String upperPartNumber = singlePartUsageObject.getPartUsageParentElementIdentifier();
            String position = BOMConstructionKitImportHelper.convertIntoEDSBCSPosValue(singlePartUsageObject.getPosition());
            // Falls keine Struktur existiert, brauchen wir nicht zu korrigieren
            if (!StrUtils.isValid(upperPartNumber, position)) {
                return;
            }
            // Alle Stände zu einer Struktur laden (sortiert nach AS ab)
            iPartsDataBOMConstKitTextList allDataForUpperNumberAndPosition
                    = iPartsDataBOMConstKitTextList.loadAllDataForUpperNumberAndPositionAndTextType(getProject(),
                                                                                                    upperPartNumber,
                                                                                                    DEFAULT_TEXT_TYPE_VALUE,
                                                                                                    position);
            dataTransferHelper.correctDBDataRevisionChain(allDataForUpperNumberAndPosition);
        });
    }

    /**
     * Aktualisiert die Verknüpfung zwischen bestehenden und importierten Daten (KEM bis, AS bis und Datum bis) für
     * die Tabelle DA_EDS_CONST_KIT
     *
     * @param singlePartUsageList
     */
    private void updateConstKitData(List<TruckBOMSinglePartUsage> singlePartUsageList) {
        TruckBOMFoundationDataCorrectionHelper dataTransferHelper = new TruckBOMFoundationDataCorrectionHelper(this,
                                                                                                               FIELD_DCK_REVFROM,
                                                                                                               FIELD_DCK_REVTO,
                                                                                                               EDSImportHelper.EDS_AS_BIS_UNENDLICH);
        dataTransferHelper.addFields(FIELD_DCK_KEMFROM, FIELD_DCK_KEMTO);
        dataTransferHelper.addFields(FIELD_DCK_RELEASE_FROM, FIELD_DCK_RELEASE_TO);

        singlePartUsageList.forEach(singlePartUsageObject -> {
            String upperPartNumber = singlePartUsageObject.getPartUsageParentElementIdentifier();
            String position = BOMConstructionKitImportHelper.convertIntoEDSBCSPosValue(singlePartUsageObject.getPosition());
            // Falls keine Struktur existiert, brauchen wir nicht zu korrigieren
            if (!StrUtils.isValid(upperPartNumber, position)) {
                return;
            }
            // Alle Stände zu einer Struktur laden (sortiert nach AS ab)
            iPartsDataBOMConstKitContentList allDataForUpperNumberAndPosition
                    = iPartsDataBOMConstKitContentList.loadAllDataForUpperNumberAndPosition(getProject(),
                                                                                            upperPartNumber,
                                                                                            position);
            dataTransferHelper.correctDBDataRevisionChain(allDataForUpperNumberAndPosition);
        });
    }

    /**
     * Konvertiert ein {@link TruckBOMSinglePartUsage} Objekt in einen ImportRecord (Map) für den
     * {@link BOMConstructionKitContentUpdateImporter} und den {@link BOMConstructionKitTextUpdateImporter}
     *
     * @param upperPartNumber
     * @param position
     * @param singlePartUsageVersion
     * @param kemFromForParts
     * @param kemToForParts
     * @return
     */
    private RecordData convertPartUsageToImportRecord(String upperPartNumber, String position, TruckBOMPartUsageVersion singlePartUsageVersion,
                                                      Map<String, TruckBOMSingleKEM> kemFromForParts,
                                                      Map<String, TruckBOMSingleKEM> kemToForParts) {
        RecordData recordData = new RecordData();
        // Obere Sachnummer und Position setzen
        addValueIfExists(BOMConstructionKitContentUpdateImporter.BK_PARTS_LIST, upperPartNumber, recordData);
        addValueIfExists(BOMConstructionKitContentUpdateImporter.BK_POSITION, position, recordData);
        // Die "einfachen" String basierten Daten befüllen
        addPlainData(recordData, singlePartUsageVersion);
        // KEM ab Daten setzen
        addKEMData(recordData, singlePartUsageVersion.getId(), kemFromForParts, BOMConstructionKitContentUpdateImporter.BK_ECO_FROM,
                   BOMConstructionKitContentUpdateImporter.BK_VAKZ_AB, BOMConstructionKitContentUpdateImporter.BK_RELEASE_FROM);
        // KEM bis Daten setzen
        addKEMData(recordData, singlePartUsageVersion.getId(), kemToForParts, BOMConstructionKitContentUpdateImporter.BK_ECO_TO,
                   BOMConstructionKitContentUpdateImporter.BK_VAKZ_BIS, BOMConstructionKitContentUpdateImporter.BK_RELEASE_TO);

        return recordData;
    }

    /**
     * Setzt den Verwendungsstellentext, der für den {@link BOMConstructionKitTextUpdateImporter} benötigt wird
     *
     * @param recordData
     * @param newestPartUsage
     */
    private void addPartUsageTexts(RecordData recordData, TruckBOMPartUsageVersion newestPartUsage) {
        // Standard-Wert "V" setzen
        addValueIfExists(BOMConstructionKitTextUpdateImporter.BKV_TEXT_TYPE, DEFAULT_TEXT_TYPE_VALUE, recordData);
        // Der Text soll nur verarbeitet werden, wenn er auch existiert. Durch EDS_UNKNOWN importiert der Sub-Importer den
        // Text nicht direkt, sondern erst über die gesetzte Description
        addLangDataMetaInfo(recordData, BOMConstructionKitTextUpdateImporter.BKV_TEXT_LANG_DATA,
                            BOMConstructionKitTextUpdateImporter.BKV_TEXT_LANG_ATTRIBUTE, iPartsEDSLanguageDefs.EDS_UNKNOWN);
        // Damit es beim Sub-Importer keine Exception gibt, muss ein Wert für das Textelement existieren
        recordData.put(BOMConstructionKitTextUpdateImporter.BKV_TEXT_DATA, "");
        // Verwendungsstellentext (aus BKV) setzen
        recordData.setDescription(TruckBOMMultiLangData.convertTextsToMultiLang(newestPartUsage.getPointOfUsageInformation()));
    }

    /**
     * Setzt die "einfachen" String basierten Daten
     *
     * @param recordData
     * @param newestPartUsage
     */
    private void addPlainData(RecordData recordData, TruckBOMPartUsageVersion newestPartUsage) {
        addValueIfExists(BOMConstructionKitContentUpdateImporter.BK_AS_AB, newestPartUsage.getVersion(), recordData);
        addValueIfExists(BOMConstructionKitContentUpdateImporter.BK_ITEM, newestPartUsage.getPartUsageChildElementIdentifier(), recordData);
        addValueIfExists(BOMConstructionKitContentUpdateImporter.BK_ALTERNATIVE_FLAG, newestPartUsage.getAlternativeIdentifier(), recordData);
        addValueIfExists(BOMConstructionKitContentUpdateImporter.BK_TBF_ALTERNATIVE_COMBINATION_IDENTIFIER, newestPartUsage.getAlternativeCombinationIdentifier(), recordData);
        addValueIfExists(BOMConstructionKitContentUpdateImporter.BK_QUANTITY, newestPartUsage.getQuantity(), recordData);
        addValueIfExists(BOMConstructionKitContentUpdateImporter.BK_STEERING_TYPE, newestPartUsage.getSteeringType(), recordData);
        // Beim Reifegrad wird von "_" auf leer gemappt
        String maturityLevel = newestPartUsage.getMaturityLevel();
        if ((maturityLevel != null) && maturityLevel.equals(EMPTY_MATURITY_VALUE)) {
            maturityLevel = StrUtils.replaceSubstring(maturityLevel, EMPTY_MATURITY_VALUE, "");
        }
        addValueIfExists(BOMConstructionKitContentUpdateImporter.BK_MATURITY_LEVEL, maturityLevel, recordData);
        addValueIfExists(BOMConstructionKitContentUpdateImporter.BK_ACQUISITION_TYPE, newestPartUsage.getAcquisitionType(), recordData);
        handleOptionalBooleanValue(BOMConstructionKitContentUpdateImporter.BK_PIPE_PARTS_LIST_FLAG, newestPartUsage.getPipePartUsage(), recordData);
    }

    /**
     * Erzeugt pro {@link TruckBOMPartUsageVersion} zum übergebenen {@link TruckBOMSinglePartUsage} ein
     * {@link RecordData} Objekt
     *
     * @param upperPartNumber
     * @param position
     * @param allSinglePartUsageVersions
     * @param kemFromForParts
     * @param kemToForParts
     * @return
     */
    private Map<String, RecordData> createRecordDataForSinglePartUsage(String upperPartNumber, String position,
                                                                       List<TruckBOMPartUsageVersion> allSinglePartUsageVersions,
                                                                       Map<String, TruckBOMSingleKEM> kemFromForParts,
                                                                       Map<String, TruckBOMSingleKEM> kemToForParts) {
        Map<String, RecordData> createdRecords = new LinkedHashMap<>();
        allSinglePartUsageVersions.sort(Comparator.comparing(TruckBOMPartUsageVersion::getVersion));
        allSinglePartUsageVersions.forEach(singlePartUsageVersion -> createdRecords.put(singlePartUsageVersion.getId(),
                                                                                        convertPartUsageToImportRecord(upperPartNumber,
                                                                                                                       position,
                                                                                                                       singlePartUsageVersion,
                                                                                                                       kemFromForParts,
                                                                                                                       kemToForParts)));
        return createdRecords;
    }

    /**
     * KeyValue-Reader für den Sub-Importer, der die Texte zur Baukastenstruktur importiert (getT43RBKV)
     */
    private class PartUsageTextValueReader extends AbstractTruckBOMKeyValueJSONReaderWithKEMData {

        private final List<TruckBOMSinglePartUsage> singlePartUsageList;
        private final AbstractBOMXMLDataImporter importer;

        public PartUsageTextValueReader(DWFile savedJSONFile, List<TruckBOMSinglePartUsage> singlePartUsageList,
                                        Map<String, TruckBOMSingleKEM> kemFromForParts, Map<String, TruckBOMSingleKEM> kemToForParts,
                                        AbstractBOMXMLDataImporter importer) {
            this(savedJSONFile, singlePartUsageList, kemFromForParts, kemToForParts, importer, TABLE_DA_EDS_CONST_PROPS);
        }

        protected PartUsageTextValueReader(DWFile savedJSONFile, List<TruckBOMSinglePartUsage> singlePartUsageList,
                                           Map<String, TruckBOMSingleKEM> kemFromForParts, Map<String, TruckBOMSingleKEM> kemToForParts,
                                           AbstractBOMXMLDataImporter importer, String importTable) {
            super(savedJSONFile, kemFromForParts, kemToForParts, singlePartUsageList.size(), importTable);
            this.singlePartUsageList = singlePartUsageList;
            this.importer = importer;
        }

        @Override
        protected List<RecordData> getNextRecordData() {
            if (!isCancelled()) {
                // Den nächsten Datensatz bestimmen und den Zähler um eins erhöhen
                TruckBOMSinglePartUsage singlePartUsage = getSinglePartUsageList().get(getImportRecCount());
                if (singlePartUsage.hasVersions()) {
                    // Position und obere Sachnummer bestimmen. Bei Texten ist die Position = Position der Stücklistenposition -1
                    String position = getPositionForText(singlePartUsage);
                    String upperPartNumber = singlePartUsage.getPartUsageParentElementIdentifier();
                    if (isValidPartUsageData(upperPartNumber, position)) {
                        // Nur die Versionen mit Text-Objekten filtern
                        List<TruckBOMPartUsageVersion> allVersionsWithText
                                = singlePartUsage.getPartUsageVersion().stream()
                                .filter(singleVersion -> (singleVersion.getPointOfUsageInformation() != null)
                                                         && !singleVersion.getPointOfUsageInformation().isEmpty())
                                .collect(Collectors.toList());
                        Map<String, RecordData> createdRecords = createRecordDataForSinglePartUsage(upperPartNumber, position,
                                                                                                    allVersionsWithText,
                                                                                                    getKemFromForParts(),
                                                                                                    getKemToForParts());
                        // T43RBKV spezifische Daten setzen
                        if (!createdRecords.isEmpty()) {
                            // Verwendungsstellentext setzen
                            singlePartUsage.getPartUsageVersion().forEach(singlePartUsageVersion -> {
                                RecordData recordData = createdRecords.get(singlePartUsageVersion.getId());
                                if (recordData != null) {
                                    addPartUsageTexts(recordData, singlePartUsageVersion);
                                }
                            });
                            return new ArrayList<>(createdRecords.values());
                        }
                    }
                }
            }
            return null;
        }

        /**
         * Liefert den Positions-Wert für Textpositionen.
         * <p>
         * Position Text = Position Stücklistenposition - 1
         *
         * @param singlePartUsage
         * @return
         */
        private String getPositionForText(TruckBOMSinglePartUsage singlePartUsage) {
            String position = singlePartUsage.getPosition();
            if (StrUtils.isValid(position)) {
                int textPosition = StrUtils.strToIntDef(position, -1);
                if (textPosition > 0) {
                    position = String.valueOf(textPosition - 1);
                }
            }

            return position;
        }

        @Override
        protected void postProcessRecordData(RecordData record) {
            // Verwendungsstellentext setzen
            Map<String, EtkMultiSprache> textsForRecord = new HashMap<>();
            addTextObjectIfNotNull(BOMConstructionKitTextUpdateImporter.BKV_TEXT_DATA, record.getDescription(), textsForRecord);
            importer.setTextsForMultiLangFields(textsForRecord);
        }

        @Override
        protected String getOriginalTableName() {
            return BOMConstructionKitTextUpdateImporter.IMPORT_TABLENAME;
        }

        protected List<TruckBOMSinglePartUsage> getSinglePartUsageList() {
            return singlePartUsageList;
        }
    }

    /**
     * KeyValue-Reader für den Baukastenstruktur Sub-Importer (getT43RBK)
     */
    private class PartUsageValueReader extends PartUsageTextValueReader {

        private final Map<String, Set<String>> assocIdToDistributionTask;

        public PartUsageValueReader(DWFile savedJSONFile, List<TruckBOMSinglePartUsage> singlePartUsageList,
                                    Map<String, TruckBOMSingleKEM> kemFromForParts, Map<String, TruckBOMSingleKEM> kemToForParts,
                                    Map<String, Set<String>> assocIdToDistributionTask) {
            super(savedJSONFile, singlePartUsageList, kemFromForParts, kemToForParts, null, TABLE_DA_EDS_CONST_KIT);
            this.assocIdToDistributionTask = assocIdToDistributionTask;
        }

        @Override
        protected void postProcessRecordData(RecordData record) {
            // Beim einfachen PartUsageImport sollen keine Post-Processing Operationen durchgeführt werden
        }

        @Override
        protected List<RecordData> getNextRecordData() {
            if (!isCancelled()) {
                // Den nächsten Datensatz bestimmen und den Zähler um eins erhöhen
                TruckBOMSinglePartUsage singlePartUsage = getSinglePartUsageList().get(getImportRecCount());
                if (singlePartUsage.hasVersions()) {
                    String position = singlePartUsage.getPosition();
                    String upperPartNumber = singlePartUsage.getPartUsageParentElementIdentifier();
                    if (isValidPartUsageData(upperPartNumber, position)) {
                        List<TruckBOMPartUsageVersion> allVersions = singlePartUsage.getPartUsageVersion();
                        Map<String, RecordData> createdRecords = createRecordDataForSinglePartUsage(upperPartNumber, position,
                                                                                                    allVersions,
                                                                                                    getKemFromForParts(),
                                                                                                    getKemToForParts());
                        // T43RBK spezifische Daten setzen
                        if (!createdRecords.isEmpty()) {
                            // Werksverteiler Daten setzen
                            createdRecords.forEach((usageId, createdRecord) -> {
                                addPlantSupplyIfExists(createdRecord, assocIdToDistributionTask, usageId, BOMConstructionKitContentUpdateImporter.BK_PLANTSUPPLIES,
                                                       BOMConstructionKitContentUpdateImporter.BK_PLANTSUPPLY);
                            });
                            return new ArrayList<>(createdRecords.values());
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected String getOriginalTableName() {
            return BOMConstructionKitContentUpdateImporter.IMPORT_TABLENAME;
        }
    }

    private boolean isValidPartUsageData(String upperPartNumber, String position) {
        // Falls keine Struktur existiert, brauchen wir nicht importieren
        return StrUtils.isValid(upperPartNumber, position);
    }
}
