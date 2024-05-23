/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.update;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMXMLDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.BOMPartHistoryImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Importer für den Teilestammdaten für Baukästen aus der BOM-DB (T43RTEIL) - Änderungsdienst
 */
public class BOMPartHistoryUpdateImporter extends AbstractBOMXMLDataImporter {

    // Hier haben wir den Fall, dass zwei unterschiedliche Importer den gleichen Datensatztyp verarbeiten
    public static final String IMPORT_TABLENAME = BOMPartMasterDataUpdateImporter.IMPORT_TABLENAME;

    private static final String TEIL_PART_NUMBER = "PartNumber"; // SNR - Länge: 13
    private static final String TEIL_RELEASE_FROM = "ReleaseDateFrom"; // FRG_DAT_AB
    private static final String TEIL_RELEASE_TO = "ReleaseDateTo"; // FRG_DAT_BIS
    private static final String TEIL_ECO_FROM = "EcoFrom"; // KEM_AB
    private static final String TEIL_ECO_TO = "EcoTo"; // KEM_BIS
    private static final String TEIL_AS_AB = "VersionFrom"; // AS_AB
    private static final String TEIL_AS_BIS = "VersionTo"; // AS_BIS
    private static final String TEIL_VAKZ_AB = "StatusFrom"; // VAKZ_AB
    private static final String TEIL_VAKZ_BIS = "StatusTo"; // VAKZ_BIS

    public BOMPartHistoryUpdateImporter(EtkProject project) {
        super(project, "!!BOM Teilestammdaten für Baukasten (TEIL)", TABLE_DA_BOM_MAT_HISTORY, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_DA_BOM_MAT_HISTORY, BOM_PART_MASTERDATA_HISTORY_UPDATE, false, false, false,
                                            new String[]{ MimeTypes.EXTENSION_XML }));
    }

    @Override
    protected void initXMLMapping(HashMap<String, String> mapping, Set<String> allXMLElementsToConsider, Set<String> allXMLElements) {
        mapping.put(FIELD_DBMH_REV_TO, TEIL_AS_BIS);
        mapping.put(FIELD_DBMH_KEM_FROM, TEIL_ECO_FROM);
        mapping.put(FIELD_DBMH_KEM_TO, TEIL_ECO_TO);
        mapping.put(FIELD_DBMH_RELEASE_FROM, TEIL_RELEASE_FROM);
        mapping.put(FIELD_DBMH_RELEASE_TO, TEIL_RELEASE_TO);

        allXMLElementsToConsider.addAll(mapping.values());
        allXMLElementsToConsider.add(TEIL_VAKZ_AB);
        allXMLElementsToConsider.add(TEIL_VAKZ_BIS);

        allXMLElements.addAll(allXMLElementsToConsider);
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ TEIL_PART_NUMBER, TEIL_AS_AB };
    }

    @Override
    protected String[] getMustHaveData() {
        return getMustExist();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        String vakzToValue = EDSImportHelper.getTrimmedValueFromRecord(importRec, TEIL_VAKZ_BIS);
        boolean isVakzValueEmpty = StrUtils.isEmpty(vakzToValue);
        BOMPartHistoryImportHelper importHelper = new BOMPartHistoryImportHelper(getProject(), getMapping(), getDestinationTable(),
                                                                                 TEIL_AS_BIS, TEIL_RELEASE_FROM, TEIL_RELEASE_TO, isVakzValueEmpty);
        importHelper.prepareXMLImportRec(importRec, getAllXMLElementsToConsider(), getAllXMLElements());
        if (!importHelper.isValidRecord(importRec, TEIL_VAKZ_AB)) {
            getMessageLog().fireMessage(translateForLog("!!Record %1 wird übersprungen: \"%2\" enthält \"%3\" und ist nicht leer.",
                                                        String.valueOf(recordNo), TEIL_VAKZ_AB, importHelper.handleValueOfSpecialField(TEIL_VAKZ_AB, importRec)),
                                        MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            reduceRecordCount();
            return;
        }

        String partNo = importHelper.handleValueOfSpecialField(TEIL_PART_NUMBER, importRec);
        String revFrom = importHelper.handleValueOfSpecialField(TEIL_AS_AB, importRec);
        importHelper.importPartHistoryData(this, importRec, partNo, revFrom, recordNo, importToDB);
    }
}
