/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.config.iPartsEDSLanguageDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSSaaMasterDataImportHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Importer für die SAA Stammdaten aus BCS (Änderungsdienst)
 */
public class BCSMasterDataSaaUpdateImporter extends AbstractBOMXMLDataImporter {

    public static final String IMPORT_TABLENAME = "getT43RSAA";

    public static final String SAA_PARTLIST_NUMBER = "PartsListNumber"; // SNR
    private static final String SAA_PARTLIST_LANG_DATA = "PartsListLangData"; //
    private static final String SAA_PARTLIST_LANG_DATA_ATTRIBUTE = "language";
    public static final String SAA_AS_AB = "VersionFrom"; // AS_AB
    private static final String SAA_AS_BIS = "VersionTo"; // AS_BIS
    public static final String SAA_DESCRIPTION = "Description"; // BEN
    private static final String SAA_EXTENDED_DESCRIPTION = "ExtendedDescription"; // Erweiterte Benennung (ERW_BEN)
    public static final String SAA_REMARK = "Remark"; // Bemerkung 352 Zeichen (BEM)
    private static final String SAA_RELEASE_FROM = "ReleaseDateFrom"; // FRG_DAT_AB
    private static final String SAA_RELEASE_TO = "ReleaseDateTo"; // FRG_DAT_BIS
    private static final String SAA_ECO_TO = "EcoTo"; // KEM_BIS
    private static final String SAA_VAKZ_AB = "StatusFrom"; // VAKZ_AB
    private static final String SAA_VAKZ_BIS = "StatusTo"; // VAKZ_BIS

    private static final Map<String, String> EXTERNAL_TEXT_MAPPING = new HashMap<>();

    static {
        EXTERNAL_TEXT_MAPPING.put(SAA_DESCRIPTION, FIELD_DS_CONST_DESC);
        EXTERNAL_TEXT_MAPPING.put(SAA_REMARK, FIELD_DS_REMARK);
    }

    private Map<String, iPartsDataSaa> saaMasterDataMap;

    public BCSMasterDataSaaUpdateImporter(EtkProject project) {
        super(project, "!!BCS SAA-Stammdaten (T43RSAA)", TABLE_DA_SAA, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_SAA, BCS_SAA_MASTERDATA_UPDATE, false, false, false, new String[]{ MimeTypes.EXTENSION_XML }));
    }

    @Override
    protected void initXMLMapping(HashMap<String, String> mapping, Set<String> allXMLElementsToConsider, Set<String> allXMLElements) {
        mapping.put(FIELD_DS_REV_FROM, SAA_AS_AB);
        mapping.put(FIELD_DS_REMARK, SAA_REMARK);
        mapping.put(FIELD_DS_DESC_EXTENDED, SAA_EXTENDED_DESCRIPTION);
        mapping.put(FIELD_DS_CONST_DESC, SAA_DESCRIPTION);

        allXMLElementsToConsider.addAll(mapping.values());
        allXMLElementsToConsider.add(SAA_PARTLIST_LANG_DATA);
        allXMLElementsToConsider.add(SAA_VAKZ_AB);
        allXMLElementsToConsider.add(SAA_VAKZ_BIS);

        allXMLElements.addAll(allXMLElementsToConsider);
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ SAA_PARTLIST_NUMBER, SAA_AS_AB };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void preImportTask() {
        saaMasterDataMap = new HashMap<>();
        super.preImportTask();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        String vakzToValue = EDSImportHelper.getTrimmedValueFromRecord(importRec, SAA_VAKZ_BIS);
        // OB VAKZ_BIS leer ist oder nicht wirkt sich auf einzelene Werte aus. Daher wird hier diese Information direkt an
        // den Helper weitergegeben.
        boolean isVakzToValueEmpty = StrUtils.isEmpty(vakzToValue);
        EDSSaaMasterDataImportHelper importHelper = new EDSSaaMasterDataImportHelper(getProject(), getMapping(), getDestinationTable(),
                                                                                     isVakzToValueEmpty, SAA_AS_BIS, SAA_ECO_TO,
                                                                                     SAA_RELEASE_FROM, SAA_RELEASE_TO);
        importHelper.prepareXMLImportRec(importRec, getAllXMLElementsToConsider(), getAllXMLElements());
        // Nur Datensätze übernehmen bei denen VAKZ_AB leer ist (d.h. alle freigegebenen historischen Stände werden übernommen)
        if (!importHelper.isValidRecord(importRec, SAA_VAKZ_AB)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wird übersprungen: \"%2\" enthält \"%3\" und ist nicht leer.",
                                                        String.valueOf(recordNo), SAA_VAKZ_AB, importHelper.handleValueOfSpecialField(SAA_VAKZ_AB, importRec)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }
        String saaNumber = importHelper.handleValueOfSpecialField(SAA_PARTLIST_NUMBER, importRec);

        iPartsSaaId saaId = new iPartsSaaId(saaNumber);
        iPartsDataSaa dataSaa = saaMasterDataMap.get(saaNumber);
        if (dataSaa == null) {
            iPartsDataSaa saaInDB = new iPartsDataSaa(getProject(), saaId);
            if (saaInDB.existsInDB()) {
                saaMasterDataMap.put(saaNumber, saaInDB);
                dataSaa = saaInDB;
            }
        }
        boolean notInCache = dataSaa == null;
        boolean hasHigherVersion = !notInCache && importHelper.hasHigherVersion(importRec, dataSaa.getFieldValue(FIELD_DS_REV_FROM), SAA_VAKZ_AB, SAA_AS_AB);
        boolean hasHigherOrEqualsVersion = !notInCache && importHelper.hasHigherOrEqualsVersion(importRec, dataSaa.getFieldValue(FIELD_DS_REV_FROM), SAA_VAKZ_AB, SAA_AS_AB);
        boolean fillWithNewData;
        if (notInCache || hasHigherVersion) {
            if (dataSaa == null) {
                dataSaa = new iPartsDataSaa(getProject(), saaId);
                // Mit leeren Attributen initiieren, wenn
                // - nicht im Cache
                // - ImportRec hat einen höheren Änderungsstand als in der DB
                dataSaa.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            }
            saaMasterDataMap.put(saaNumber, dataSaa);
            fillWithNewData = true;
        } else {
            fillWithNewData = hasHigherOrEqualsVersion;
        }

        if (fillWithNewData) {
            importHelper.fillOverrideCompleteDataForEDSReverse(dataSaa, importRec, iPartsEDSLanguageDefs.EDS_DE);
        }
        // Sofern vorhanden, die von außen gesetzten Texte verwenden
        setExternalTexts(dataSaa, EXTERNAL_TEXT_MAPPING);

        iPartsDataSa dataSa = importHelper.addSaIfNotExists(dataSaa, iPartsImportDataOrigin.EDS);
        if (importToDB && (dataSa != null)) {
            saveToDB(dataSa);
        }
    }

    @Override
    protected void postImportTask() {
        if (!isCancelled()) {
            if (importToDB) {
                for (iPartsDataSaa dataSaa : saaMasterDataMap.values()) {
                    // Die Quelle setzen werden (DAIMLER-9895)
                    dataSaa.setFieldValue(FIELD_DS_SOURCE, iPartsImportDataOrigin.EDS.getOrigin(), DBActionOrigin.FROM_EDIT);
                    saveToDB(dataSaa);
                }
            }
        }
        saaMasterDataMap.clear();
        super.postImportTask();
    }
}
