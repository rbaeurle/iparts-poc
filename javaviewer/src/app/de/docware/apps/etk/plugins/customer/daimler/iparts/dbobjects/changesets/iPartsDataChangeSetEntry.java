/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDbDataObjectAsJSON;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_CHANGE_SET_ENTRY.
 */
public class iPartsDataChangeSetEntry extends EtkDataObject implements EtkDbConst, iPartsConst, AbstractRevisionChangeSet.SerializedDBDataObjectContainer {

    /**
     * Erstellt eine neue Id mit den Werten aus dem übergebenen ChangeSet.
     *
     * @param clazz
     * @param idType
     * @param dataChangeSetEntry
     * @param <T>
     * @return null: falsche clazz oder idType bzgl ChangeSetEntry
     */
    public static <T extends IdWithType> T getIdFromChangeSetEntry(Class<T> clazz, String idType, iPartsDataChangeSetEntry dataChangeSetEntry) {
        if ((dataChangeSetEntry != null) && dataChangeSetEntry.getAsId().getDataObjectType().equals(idType)) {
            String dataObjectId = dataChangeSetEntry.getAsId().getDataObjectId();
            return getIdFromChangeSetEntry(clazz, idType, dataObjectId);
        }
        return null;
    }

    /**
     * Erstellt eine neue ID mit den übergebenen Werten.
     *
     * @param idClass
     * @param idType
     * @param dataObjectId
     * @param <T>
     * @return {@code null} falls falsche {@code idClass} oder {@code idType} bzw. {@link iPartsDataChangeSetEntry}
     */
    public static <T extends IdWithType> T getIdFromChangeSetEntry(Class<T> idClass, String idType, String dataObjectId) {
        if (StrUtils.isValid(dataObjectId)) {
            IdWithType objectId = IdWithType.fromDBString(idType, dataObjectId);
            if (objectId != null) {
                return IdWithType.fromStringArrayWithTypeFromClass(idClass, objectId.toStringArrayWithoutType());
            }
        }
        return null;
    }


    static private final String[] KEYS = new String[]{ FIELD_DCE_GUID, FIELD_DCE_DO_TYPE, FIELD_DCE_DO_ID };

    private SerializedDBDataObject serializedDBDataObject;

    public iPartsDataChangeSetEntry(EtkProject project, iPartsChangeSetEntryId id) {
        super(KEYS);
        tableName = TABLE_DA_CHANGE_SET_ENTRY;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsChangeSetEntryId createId(String... idValues) {
        return new iPartsChangeSetEntryId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsChangeSetEntryId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsChangeSetEntryId)id;
    }

    public String getCurrentData() {
        return getFieldValueAsStringFromZippedBlob(FIELD_DCE_CURRENT_DATA);
    }

    public void setCurrentData(String currentData, DBActionOrigin origin) {
        setFieldValueAsZippedBlobFromString(FIELD_DCE_CURRENT_DATA, currentData, origin);
        serializedDBDataObject = null;
    }

    public String getHistoryData() {
        return getFieldValueAsStringFromZippedBlob(FIELD_DCE_HISTORY_DATA);
    }

    public void setHistoryData(String historyData, DBActionOrigin origin) {
        setFieldValueAsZippedBlobFromString(FIELD_DCE_HISTORY_DATA, historyData, origin);
    }

    public String getSourceGUID() {
        return getFieldValue(FIELD_DCE_DO_SOURCE_GUID);
    }

    @Override
    @JsonIgnore
    public SerializedDBDataObject getSerializedDBDataObject() {
        if (serializedDBDataObject == null) {
            String currentData = getCurrentData();
            if (!currentData.isEmpty()) {
                SerializedDbDataObjectAsJSON serializedDbDataObjectAsJSON = new SerializedDbDataObjectAsJSON(true);
                serializedDBDataObject = serializedDbDataObjectAsJSON.getFromJSON(currentData);
            }
        }
        return serializedDBDataObject;
    }

    @JsonIgnore
    public void setSerializedDBDataObject(SerializedDBDataObject serializedDBDataObject) {
        this.serializedDBDataObject = serializedDBDataObject;
    }

    @JsonIgnore
    public <T extends IdWithType> T getIdFromChangeSetEntry(Class<T> clazz, String idType) {
        return getIdFromChangeSetEntry(clazz, idType, this);
    }

}
