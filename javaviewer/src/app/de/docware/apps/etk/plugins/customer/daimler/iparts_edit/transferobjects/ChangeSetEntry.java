/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects;

import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSetEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectHistory;
import de.docware.framework.modules.db.serialization.SerializedDbDataObjectAsJSON;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

/**
 * DTO für einen ChangeSet-Eintrag {@link iPartsDataChangeSetEntry} im Webservice zur Versorgung nach BST
 */
public class ChangeSetEntry extends WSRequestTransferObject {

    private String dataObjectType;
    private String dataObjectId;
    private String dataObjectOldId;
    private String dataObjectSourceGUID;
    private SerializedDBDataObject mergedData;

    public ChangeSetEntry() {
    }

    public ChangeSetEntry(iPartsDataChangeSetEntry dataChangeSetEntry) {
        dataObjectType = dataChangeSetEntry.getAsId().getDataObjectType();
        dataObjectId = dataChangeSetEntry.getAsId().getDataObjectId();
        String oldId = dataChangeSetEntry.getFieldValue(iPartsConst.FIELD_DCE_DO_ID_OLD);
        if (!oldId.isEmpty()) {
            dataObjectOldId = oldId;
        }
        String sourceGUID = dataChangeSetEntry.getSourceGUID();
        if (!sourceGUID.isEmpty()) {
            dataObjectSourceGUID = sourceGUID;
        }

        // SerializedDBDataObject aus JSON erzeugen
        SerializedDbDataObjectAsJSON serializedDbDataObjectAsJSON = new SerializedDbDataObjectAsJSON(true);
        String jsonString = dataChangeSetEntry.getHistoryData();
        if (!jsonString.isEmpty()) {
            SerializedDBDataObjectHistory historyData = serializedDbDataObjectAsJSON.getHistoryFromJSON(jsonString);
            mergedData = historyData.mergeSerializedDBDataObject(false, iPartsRevisionChangeSet.SYSTEM_USER_IDS);
            if (mergedData != null) {
                mergedData.inheritUserAndDateTime(null, null, true); // Personenbezogene Daten entfernen
            }
        }
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "dataObjectType", dataObjectType);
        checkAttribValid(path, "dataObjectId", dataObjectId);
        checkAttribPresent(path, "mergedData", mergedData);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ dataObjectType, dataObjectId };
    }

    public String getDataObjectType() {
        return dataObjectType;
    }

    public void setDataObjectType(String dataObjectType) {
        this.dataObjectType = dataObjectType;
    }

    public String getDataObjectId() {
        return dataObjectId;
    }

    public void setDataObjectId(String dataObjectId) {
        this.dataObjectId = dataObjectId;
    }

    public String getDataObjectOldId() {
        return dataObjectOldId;
    }

    public void setDataObjectOldId(String dataObjectOldId) {
        this.dataObjectOldId = dataObjectOldId;
    }

    public String getDataObjectSourceGUID() {
        return dataObjectSourceGUID;
    }

    public void setDataObjectSourceGUID(String dataObjectSourceGUID) {
        this.dataObjectSourceGUID = dataObjectSourceGUID;
    }

    public SerializedDBDataObject getMergedData() {
        return mergedData;
    }

    public void setMergedData(SerializedDBDataObject mergedData) {
        this.mergedData = mergedData;
    }
}