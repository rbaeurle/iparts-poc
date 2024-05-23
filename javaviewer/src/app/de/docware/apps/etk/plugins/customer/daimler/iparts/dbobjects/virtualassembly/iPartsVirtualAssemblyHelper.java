/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.db.sqlbuilder.EtkSqlCommonJoins;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.config.db.EtkDatabaseField;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.StrUtils;
import de.docware.util.sql.SQLQuery;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Helper für zusammengezogene Funtkionalitäten in der VirtualAssembly.
 * Aktuell allerdings erst für EDS/MBS Ähnlichkeiten/Gemeinsamkeiten, für zusammengezogene Funktionen.
 */
public class iPartsVirtualAssemblyHelper implements iPartsConst {

    /**
     * Überprüft, ob es sich um ein Material handelt
     * Ist das Attribut nicht gesetzt, so wird davon ausgegangen, dass es reines Material ist
     *
     * @param attributes
     * @return false: FIELD_M_ASSEMBLY ist gesetzt
     */
    public static boolean isRealMaterial(DBDataObjectAttributes attributes) {
        DBDataObjectAttribute attrib = attributes.getField(FIELD_M_ASSEMBLY, false);
        if (attrib != null) {
            return !attrib.getAsBoolean();
        }
        return true;
    }

    /**
     * Setzt in den Attributen die Materialnummer falls sie nicht besetzt ist aus der SUB-SNR.
     * Für EDS die Sub-SNR aus: DA_EDS_CONST_KIT.DCK_SUB_SNR
     * Falls es kein 'echtes' Material ist, wird nicht geändert und die false zurückgegeben
     *
     * @param attributes
     */
    public static boolean checkMatNoSaaPartsList(DBDataObjectAttributes attributes) {
        return checkMatNo(attributes, FIELD_DCK_SUB_SNR);
    }

    /**
     * Setzt in den Attributen die Materialnummer falls sie nicht besetzt ist aus der SUB-SNR.
     * Für MBS die Sub-SNR aus: DA_PARTSLIST_MBS.DPM_SUB_SNR
     * Falls es kein 'echtes' Material ist, wird nicht geändert und die false zurückgegeben
     *
     * @param attributes
     */
    public static boolean checkMatNoMBS(DBDataObjectAttributes attributes) {
        return checkMatNo(attributes, FIELD_DPM_SUB_SNR);
    }

    /**
     * Setzt in den Attributen die Materialnummer aus der SUB-SNR passend zum übergebenen Feldnamen,
     * falls die Materialnummer nicht gesetzt ist und es sich um 'echtes' Material handelt
     *
     * @param attributes      die DBDataObjectAttributes
     * @param fieldNameSubSnr der Feldname der SUB_SNR
     */
    private static boolean checkMatNo(DBDataObjectAttributes attributes, String fieldNameSubSnr) {
        if (!isRealMaterial(attributes)) {
            return false;
        }
        DBDataObjectAttribute attrib = attributes.getField(FIELD_M_MATNR, false);
        if (attrib != null) {
            String matNo = attrib.getAsString();
            if (!StrUtils.isValid(matNo)) {
                // MatNo nicht vorhanden => setze wenigstens die SubSnr ein
                attrib = attributes.getField(fieldNameSubSnr, false);
                if (attrib != null) {
                    matNo = attrib.getAsString();
                    attributes.addField(FIELD_M_MATNR, matNo, DBActionOrigin.FROM_DB);
                    attributes.addField(FIELD_M_BESTNR, matNo, DBActionOrigin.FROM_DB);
                }
            }
        }
        return true;
    }

    /**
     * Liefert alle {@link DBDataObjectAttribute}s für die Felder der übergebenen Tabelle mit leeren Werten inkl. leerer
     * BLOBs zurück.
     *
     * @param tableName
     * @return
     */
    public static DBDataObjectAttributes createEmptyAttributesForTable(EtkProject project, String tableName) {
        DBDataObjectAttributes result = new DBDataObjectAttributes();

        DBDataObject.initAttributesByTable(project, result, tableName, null, DBActionOrigin.FROM_DB);

        // BLOB-Felder alle mit leerem Byte-Array befüllen, damit diese nicht nachgeladen werden
        EtkDatabaseTable tableDef = project.getConfig().getDBDescription().getTable(tableName);
        if (tableDef != null) {
            byte[] emptyBLOB = new byte[0];
            for (String blobFieldName : tableDef.getBlobFields()) {
                result.getField(blobFieldName).setValueAsBlob(emptyBLOB, DBActionOrigin.FROM_DB);
            }
        }

        return result;
    }

    /**
     * Ergänzt alle für den Filter benötigten {@link DBDataObjectAttribute}s der übergebenen Tabelle mit leeren Werten in
     * den übergebenen {@link DBDataObjectAttributes} falls noch nicht vorhanden.
     *
     * @param tableName
     * @return
     */
    public static void addFilterAttributesForTable(EtkProject project, String tableName, DBDataObjectAttributes attributes) {
        Collection<String> filterFields = project.getFilter().getActiveFilterFields(tableName);

        for (String filterField : filterFields) {
            String fieldName = TableAndFieldName.getFieldName(filterField);
            if (!attributes.fieldExists(fieldName)) {
                attributes.addField(fieldName, "", DBActionOrigin.FROM_DB);
            }
        }
    }

    /**
     * Durchläuft alle übergebenen Felder und befüllt die <code>attributesToFill</code> mit den Werten aus
     * <code>attributesFromDB</code> für das jeweilige Feld
     *
     * @param tableFields
     * @param attributesFromDB
     * @param attributesToFill
     */
    public static void addAttributesFromDB(EtkDisplayFields tableFields, DBDataObjectAttributes attributesFromDB, DBDataObjectAttributes attributesToFill) {
        for (EtkDisplayField field : tableFields.getFields()) {
            addAttributeFromDB(field, attributesFromDB, attributesToFill);
        }
    }

    /**
     * Befüllt die <code>attributesToFill</code> mit den Werten aus <code>attributesFromDB</code> für das übergebene Feld
     *
     * @param field
     * @param attributesFromDB
     * @param attributesToFill
     */
    public static void addAttributeFromDB(EtkDisplayField field, DBDataObjectAttributes attributesFromDB, DBDataObjectAttributes attributesToFill) {
        String name = field.getKey().getFieldName();
        DBDataObjectAttribute originalField = attributesFromDB.getField(name, false);
        if (originalField == null) {
            attributesToFill.addField(name, "", DBActionOrigin.FROM_DB);
        } else {
            DBDataObjectAttribute newAttribute = new DBDataObjectAttribute(originalField);
            attributesToFill.addField(newAttribute, DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Fügt der übergebenen Liste mit select-Fields die Alias-Felder für die Sprach-Spalten aus der Sprachtabelle inkl.
     * Rückfallsprachen hinzu.
     *
     * @param databaseTable
     * @param selectFields
     * @param hasLongTexts
     * @param startFieldIndex
     * @return Endindex basierend auf dem {@code startFieldIndex} und der Anzahl der mehrspachigen Felder
     */
    public static int addMultiLangAliasToSelectFields(EtkProject project, EtkDatabaseTable databaseTable, Collection<String> selectFields,
                                                      boolean hasLongTexts, int startFieldIndex) {
        int langFieldIndex = startFieldIndex;
        // Da wir pro Sprache einen Join auf die SPRACHE Tabelle machen, müssen die SPRACHE Felder so oft angelegt werden
        // wie es eingetsellte Sprachen gibt
        for (int i = 0; i < project.getDataBaseFallbackLanguagesWithMainLanguage().size(); i++) {
            for (String multiLangField : databaseTable.getMultiLangFields()) {
                if (selectFields.contains(TableAndFieldName.make(databaseTable.getName(), multiLangField).toLowerCase())) {
                    langFieldIndex++;
                    selectFields.add(TableAndFieldName.make("s_" + langFieldIndex, FIELD_S_BENENN).toLowerCase());
                    // Das Memofeld FIELD_S_BENENN_LANG gibt's erst ab DB Version 6.2
                    if (hasLongTexts) {
                        selectFields.add(TableAndFieldName.make("s_" + langFieldIndex, FIELD_S_BENENN_LANG).toLowerCase());
                    }
                }
            }
        }
        return langFieldIndex;
    }

    /**
     * Fügt der übergebenenen {@link SQLQuery} die Joins auf die Sprachtabelle inkl. Rückfallsprachen pro Mutisprachfeld
     * in der übergebenenen Tabelle hinzu.
     *
     * @param query
     * @param databaseTable
     * @param selectFields
     * @param startFieldIndex
     * @param languages
     * @return Endindex basierend auf dem {@code startFieldIndex} und der Anzahl der mehrspachigen Felder
     */
    public static int addLanguageJoinsToQuery(SQLQuery query, EtkDatabaseTable databaseTable, Collection<String> selectFields,
                                              int startFieldIndex, List<String> languages) {
        int langFieldIndex = startFieldIndex;
        // Ein Join pro übergebener Sprache
        for (String dbLanguage : languages) {
            for (String multiLangField : databaseTable.getMultiLangFields()) {
                if (selectFields.contains(TableAndFieldName.make(databaseTable.getName(), multiLangField).toLowerCase())) {
                    langFieldIndex++;
                    EtkSqlCommonJoins.addLanguageJoinToQuery(query, databaseTable.getName(), multiLangField, "s_" + langFieldIndex,
                                                             multiLangField, dbLanguage);
                }
            }
        }

        return langFieldIndex;
    }

    /**
     * Fügt unbedingt benötigte Material-Felder aus der Konfiguration und den statischen {@link EtkDisplayFields} hinzu
     *
     * @param selectFields
     * @return
     */
    private static Set<String> addNeededMatFields(EtkProject project, AssemblyId assemblyId, Set<String> selectFields) {
        // Alle in der DWK konfigurierten Felder (auch unsichtbare) aus der Tabelle MAT zu den matFieldNames hinzufügen
        EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
        EtkEbenenDaten partsListType = project.getConfig().getPartsDescription().getEbene(assembly.getEbeneName());
        Set<String> matFieldNames = addSelectFieldsFromPartsListType(project, TABLE_MAT, partsListType, selectFields);
        // Unbedingt benötigte Felder hinzufügen falls nicht schon vorhanden (M_MARKET_ETKZ darf in NEEDED_DISPLAY_FIELDS
        // nicht enthalten sein, weil es ein BLOB ist)
        for (EtkDisplayField neededDisplayField : iPartsVirtualAssembly.NEEDED_DISPLAY_FIELDS.getFields()) {
            if (selectFields.add(neededDisplayField.getKey().getName().toLowerCase())) {
                if (neededDisplayField.getKey().getTableName().equals(TABLE_MAT)) {
                    matFieldNames.add(neededDisplayField.getKey().getFieldName().toLowerCase());
                }
            }
        }
        return matFieldNames;
    }

    /**
     * Liefert alle unbedingt benötigten Material-Felder aus der Konfiguration und den statischen {@link EtkDisplayFields}
     * zurück zzgl. der optional übergebenen {@code additionalMatFieldNames}.
     *
     * @param additionalMatFieldNames
     * @return
     */
    public static EtkDisplayFields getNeededMatSelectFields(EtkProject project, AssemblyId assemblyId, String... additionalMatFieldNames) {
        Set<String> matFieldNames = addNeededMatFields(project, assemblyId, new HashSet<>());
        if (additionalMatFieldNames != null) {
            matFieldNames.addAll(Arrays.asList(additionalMatFieldNames));
        }

        EtkDisplayFields matFields = new EtkDisplayFields();
        EtkDatabaseTable tableMat = project.getConfig().getDBDescription().getTable(TABLE_MAT);
        if (tableMat != null) {
            for (String matFieldName : matFieldNames) {
                EtkDatabaseField dbField = tableMat.getField(matFieldName);
                if (dbField != null) {
                    matFields.addFeld(new EtkDisplayField(TABLE_MAT, matFieldName.toUpperCase(), dbField.isMultiLanguage(), dbField.isArray()));
                }
            }
        }
        return matFields;
    }

    /**
     * Fügt der übergebenen Liste {@code selectFields} alle in der DWK konfigurierten Felder (auch unsichtbare) aus der
     * gewünschten Tabelle hinzu mit Ausnahme der Blob-Felder.
     *
     * @param tableName
     * @param partsListType
     * @param selectFields
     * @return
     */
    private static Set<String> addSelectFieldsFromPartsListType(EtkProject project, String tableName, EtkEbenenDaten partsListType, Set<String> selectFields) {
        Set<String> selectedFieldNames = new LinkedHashSet<>();
        for (EtkDisplayField field : partsListType.getFields()) {
            if (field.getKey().getTableName().equals(tableName) && (field.getEtkDatabaseFieldType(project.getConfig()) != EtkFieldType.feBlob)) {
                selectFields.add(field.getKey().getName().toLowerCase());
                selectedFieldNames.add(field.getKey().getFieldName().toLowerCase());
            }
        }
        return selectedFieldNames;
    }

}
