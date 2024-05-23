/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.initial;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.FilesImporterFileListType;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.AbstractBOMDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.BOMSparePartSignsImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper.EDSImportHelper;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;

import java.util.Map;

/**
 * Importer für die Teilestammdaten Ersatzteilkennzeichnung aus der BOM-DB
 */
public class BOMSparePartSignsImporter extends AbstractBOMDataImporter {

    public static final String IMPORT_TABLENAME = "T43RTEID";

    private static final String TEID_SNR = "TEID_SNR";
    private static final String TEID_ET_KENNER = "TEID_ET_KENNER";

    private boolean importToDB = true;
    private boolean doBufferSave = true;
    private BOMSparePartSignsImportHelper helper; // Der Importer hat kein Mapping, somit reicht eine Helper Instanz für dne kompletten Import


    public BOMSparePartSignsImporter(EtkProject project) {
        super(project, "!!BOM Ersatzteilkeinnzeichnung (TEID)", TABLE_MAT, IMPORT_TABLENAME,
              new FilesImporterFileListType(TABLE_MAT, BOM_SPARE_PART_SIGNS, true, false, false, new String[]{ MimeTypes.EXTENSION_TXT, MimeTypes.EXTENSION_ALL_FILES }));
    }

    @Override
    protected String[] getMustExist() {
        return new String[]{ TEID_SNR, TEID_ET_KENNER };
    }

    @Override
    protected String[] getMustHaveData() {
        return new String[]{ TEID_SNR };
    }

    @Override
    protected void preImportTask() {
        progressMessageType = ProgressMessageType.READING;
        helper = new BOMSparePartSignsImportHelper(getProject(), getDestinationTable());
        super.preImportTask();
    }

    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        String partNo = EDSImportHelper.getTrimmedValueFromRecord(importRec, TEID_SNR);
        String signsAndMarkets = EDSImportHelper.getTrimmedValueFromRecord(importRec, TEID_ET_KENNER);
        if (importToDB && (helper != null) && StrUtils.isValid(partNo)) {
            helper.handleSparePartsImport(this, partNo, signsAndMarkets);
        }
    }

    @Override
    protected boolean importFile(FilesImporterFileListType importFileType, DWFile importFile) {
        if (importFileType.getFileListType().equals(getDestinationTable())) {
            return importMasterData(prepareImporterKeyValue(importFile, getDestinationTable(), '|', true, null, '\0'));
        }
        return false;
    }
}
