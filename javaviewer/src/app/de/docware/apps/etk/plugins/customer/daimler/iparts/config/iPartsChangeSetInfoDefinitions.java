/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataChangeSetInfoDefs;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataChangeSetInfoDefsList;
import de.docware.apps.etk.util.CacheHelper;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Cache für die ChangeSetInfoDefinitions (benutzerdefinierter Import)
 */
public class iPartsChangeSetInfoDefinitions {

    public static final String SPECIAL_INFO_DEF_PREFIX = "Part.";

    private static ObjectInstanceStrongLRUList<Object, iPartsChangeSetInfoDefinitions> instances = new ObjectInstanceStrongLRUList<>(iPartsConst.MAX_CACHE_SIZE_STRUCTURE,
                                                                                                                                     iPartsConst.MAX_CACHE_LIFE_TIME_CORE);

    private Map<String, ChangeSetObjectIdInfoDefinitions> changeSetDefinitionsMap;
    private Map<String, ChangeSetObjectIdInfoDefinitions> changeSetDefinitionsForASMap;
    private Map<String, Set<String>> extraAttributeNamesForSerializedObjectMap;

    public static synchronized iPartsChangeSetInfoDefinitions getInstance(EtkProject project) {
        Object hashObject = CacheHelper.getDBCacheIdentifier(project.getEtkDbs(), iPartsChangeSetInfoDefinitions.class,
                                                             "ChangeSetInfoDefinitions", false);
        iPartsChangeSetInfoDefinitions result = instances.get(hashObject);

        if (result == null) {
            // Noch nicht geladen -> lade aus der Datenbank
            result = new iPartsChangeSetInfoDefinitions();
            result.load(project);
            instances.put(hashObject, result);
        }

        return result;
    }

    public static synchronized void warmUpCache(EtkProject project) {
        getInstance(project);
    }

    public static synchronized void clearCache() {
        instances.clear();
    }

    protected iPartsChangeSetInfoDefinitions() {
        changeSetDefinitionsMap = new HashMap<>();
        changeSetDefinitionsForASMap = new HashMap<>();
        extraAttributeNamesForSerializedObjectMap = new HashMap<>();
    }

    public ChangeSetObjectIdInfoDefinitions getChangeSetDefinitions(String objectType) {
        return changeSetDefinitionsMap.get(objectType);
    }

    public ChangeSetObjectIdInfoDefinitions getChangeSetDefinitionsForAS(String objectType) {
        return changeSetDefinitionsForASMap.get(objectType);
    }

    /**
     * Liefert die zusätzlichen Attributnamen, die beim Erzeugen von einem {@link SerializedDBDataObject} für den übergebenen
     * {@code objectType} (aus dem dazugehörigen {@link de.docware.util.misc.id.IdWithType} mit abgespeichert werden sollen,
     * auch wenn sie nicht verändert wurden. Attributnamen aus dem Primärschlüssel sind hier nicht enthalten.
     * Es erfolgt KEINE Überprüfung, ob der Attributname für die Tabelle valide ist!
     *
     * @param objectType
     * @return Die zusätzlichen Attributnamen in Großbuchstaben bzw. {@code null} falls es keine zusätzlichen Attributnamen gibt
     */
    public Set<String> getExtraAttributeNamesForSerializedDBDataObject(String objectType) {
        Set<String> extraFieldNames = extraAttributeNamesForSerializedObjectMap.get(objectType);
        if (extraFieldNames != null) {
            return Collections.unmodifiableSet(extraFieldNames);
        } else {
            return null;
        }
    }

    protected void load(EtkProject project) {
        iPartsDataChangeSetInfoDefsList infoDefList = iPartsDataChangeSetInfoDefsList.loadAllInfoDefs(project);
        for (iPartsDataChangeSetInfoDefs dataChangeSetInfoDefs : infoDefList) {
            // hier noch check der Daten
            addInfoDefinition(project, dataChangeSetInfoDefs);
        }
    }

    private void addInfoDefinition(EtkProject project, iPartsDataChangeSetInfoDefs dataChangeSetInfoDefs) {
        String objectType = dataChangeSetInfoDefs.getAsId().getObjectType();
        boolean asRelevant = dataChangeSetInfoDefs.getAsId().isAsRelevant();
        String tableAndFieldName = dataChangeSetInfoDefs.getAsId().getTableAndFieldName().toUpperCase();
        String tableName = TableAndFieldName.getTableName(tableAndFieldName);
        Map<String, ChangeSetObjectIdInfoDefinitions> changeSetDefsMap = asRelevant ? changeSetDefinitionsForASMap : changeSetDefinitionsMap;
        ChangeSetObjectIdInfoDefinitions changeSetDefs = changeSetDefsMap.get(objectType);
        if (changeSetDefs == null) {
            changeSetDefs = new ChangeSetObjectIdInfoDefinitions(tableName);
            changeSetDefsMap.put(objectType, changeSetDefs);
        }
        boolean isMustField = dataChangeSetInfoDefs.getFieldValueAsBoolean(iPartsConst.FIELD_DCID_MUSTFIELD);
        changeSetDefs.addInfoDefinition(tableAndFieldName, isMustField);

        // Nur Mussfelder zur extraAttributeNamesForSerializedObjectMap hinzufügen
        if (isMustField) {
            String fieldName = TableAndFieldName.getFieldName(tableAndFieldName);
            EtkDatabaseTable table = project.getConfig().getDBDescription().findTable(tableName);
            if ((table == null) || !table.getPrimaryKeyFields().contains(fieldName)) {
                if (objectType.startsWith(SPECIAL_INFO_DEF_PREFIX)) {
                    objectType = StrUtils.stringAfterCharacter(objectType, SPECIAL_INFO_DEF_PREFIX);
                }
                Set<String> fieldNames = extraAttributeNamesForSerializedObjectMap.get(objectType);
                if (fieldNames == null) {
                    fieldNames = new TreeSet<>();
                    extraAttributeNamesForSerializedObjectMap.put(objectType, fieldNames);
                }
                fieldNames.add(fieldName);
            }
        }
    }


    public class ChangeSetObjectIdInfoDefinitions {

        private String tableName;
        private List<String> mustFields;
        private List<String> fields;

        public ChangeSetObjectIdInfoDefinitions(String tableName) {
            this.tableName = tableName;
            mustFields = new DwList<>();
            fields = new DwList<>();
        }

        public String getTableName() {
            return tableName;
        }

        public List<String> getMustFields() {
            return mustFields;
        }

        public List<String> getFields() {
            return fields;
        }

        private void addInfoDefinition(String tableAndFieldName, boolean isMustField) {
            if (tableName.equals(TableAndFieldName.getTableName(tableAndFieldName))) {
                String fieldName = TableAndFieldName.getFieldName(tableAndFieldName);
                if (isMustField) {
                    mustFields.add(fieldName);
                } else {
                    fields.add(fieldName);
                }
            }
        }
    }
}
