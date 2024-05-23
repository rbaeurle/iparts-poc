/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.bigdata.handler.multihandler.AbstractMappedHandlerWithRecord;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.helper.MBSImportHelper;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstrakte Klasse für alle Stammdatenimporte aus SAP-MBS
 */
public abstract class AbstractMBSDataHandler extends AbstractMappedHandlerWithRecord implements iPartsConst {

    protected static final int MAX_DB_OBJECTS_CACHE_SIZE = 500;
    // Felder, die für die Logik verwendet werden
    protected static final String UPDATE_SIGN = "UpdateKz";
    protected static final String RELEASE_DATE_TO = "ReleaseDateTo";
    protected static final String RELEASE_DATE_FROM = "ReleaseDateFrom";
    // Felder, die für das DB Objekt verwendet werden
    protected static final String DESCRIPTION = "Description";
    protected static final String MODEL_NUMBER = "ModelNumber";
    protected static final String MODEL_NUMBER_SUFFIX = "ModelNumberSuffix";
    protected static final String PARTS_LIST_NUMBER = "PartsListNumber";
    protected static final String PARTS_LIST_CON_GROUP_NUMBER = "PartsListConGroupNumber";
    protected static final String BASE_LIST_NUMBER = "BaseListNumber";
    protected static final String BASE_LIST_CON_GROUP_NUMBER = "BaseListConGroupNumber";
    protected static final String PART_NUMBER = "PartNumber";
    protected static final String CTT_LIST_NUMBER = "CTTListNumber";
    protected static final String QUANTITY_UNIT = "QuantityUnit";

    private static final Set<String> VALID_UPDATE_SIGNS = new HashSet<>();

    static {
        VALID_UPDATE_SIGNS.add("U");
        VALID_UPDATE_SIGNS.add("I");
    }

    private String tableName;
    private MBSDataImporter importer;
    private Map<String, String> mapping;
    private MBSImportHelper importHelper;
    private MBSDistributionHandler distributionHandler;

    public AbstractMBSDataHandler(EtkProject project, String mainXMLTag, MBSDataImporter importer, String importName,
                                  String tableName) {
        super(project, mainXMLTag, importName);
        this.importer = importer;
        this.tableName = tableName;
        this.mapping = new HashMap<>();
        initMapping(mapping);
        importHelper = new MBSImportHelper(getProject(), getMapping(), getTableName());
    }

    public String getTableName() {
        return tableName;
    }

    protected void saveDataObject(EtkDataObject dataObject) {
        importer.saveToDB(dataObject);
    }

    public boolean hasDistributionHandler() {
        return distributionHandler != null;
    }

    public void setCurrentRecord(Map<String, String> currentRecord) {
        this.currentRecord = currentRecord;
    }

    /**
     * Überprüft, ob es sich um einen INSERT oder UPDATE Datensatz handelt
     *
     * @param keyValue
     * @return
     */
    protected boolean isValidAction(String keyValue) {
        String action = getCurrentRecord().get(UPDATE_SIGN);
        if (VALID_UPDATE_SIGNS.contains(action)) {
            return true;
        }
        writeMessage(TranslationHandler.translate("!!Datensatz mit dem Schlüssel \"%1\" übersprungen, weil %2 = \"%3\"",
                                                  keyValue, UPDATE_SIGN, action), MessageLogType.tmlMessage,
                     MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        return false;
    }

    public Map<String, String> getMapping() {
        return mapping;
    }

    protected MBSDataImporter getImporter() {
        return importer;
    }

    protected MBSImportHelper getImportHelper() {
        return importHelper;
    }

    protected String getDescription() {
        return getCurrentRecord().get(DESCRIPTION);
    }

    protected String getReleaseDateTo() {
        return getImportHelper().getMBSDateTimeValue(getCurrentRecord().get(RELEASE_DATE_TO));
    }

    protected String getReleaseDateFrom() {
        return getImportHelper().getMBSDateTimeValue(getCurrentRecord().get(RELEASE_DATE_FROM));
    }

    public MBSDistributionHandler getDistributionHandler() {
        return distributionHandler;
    }

    public void setDistributionHandler(MBSDistributionHandler distributionHandler) {
        this.distributionHandler = distributionHandler;
    }

    protected abstract void initMapping(Map<String, String> mapping);
}
