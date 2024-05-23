/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.ExtendedDataTypeLoadType;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEntrySourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataDialogData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataReplaceConstMat;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNote;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsReplaceConstMatId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataIncludePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePart;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsDataReplacePartList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.timeslice.iPartsFilterTimeSliceHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsRFMEA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsRFMEN;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacementConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.booleanfunctionparser.BooleanFunctionSyntaxException;
import de.docware.util.misc.id.IdWithType;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * Hilfsklasse für das Übernehmen von Stücklisteneinträgen aus der Konstruktion in den Retail (AS-Stückliste/Modul) für iParts.
 * Sowohl manuelle Übernahme als auch Migrationsimporter.
 */
public class EditConstructionToRetailHelper implements EtkDbConst, iPartsConst {

    private static final Map<String, String> DIALOG_TO_KATALOG_FIELDS_MAPPING = new HashMap<>();
    private static final Map<String, String> EDS_TO_KATALOG_FIELDS_MAPPING = new HashMap<>();
    private static final Map<String, String> MBS_TO_KATALOG_FIELD_MAPPING = new HashMap<>();
    private static final Map<String, String> MIGRATION_DIALOG_TO_KATALOG_FIELDS_MAPPING = new HashMap<>();
    private static boolean DO_URL_ENCODING_FOR_K_SOURCE_CONTEXT = true;

    static {
        // DIALOG
        DIALOG_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA, FIELD_K_AA);
        DIALOG_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES, FIELD_K_CODES);
        DIALOG_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_ETKZ, FIELD_K_ETKZ);
        DIALOG_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_QUANTITY, FIELD_K_MENGE);
        DIALOG_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_HIERARCHY, FIELD_K_HIERARCHY);
        DIALOG_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA, FIELD_K_DATEFROM);
        DIALOG_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATB, FIELD_K_DATETO);
        DIALOG_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_STEERING, FIELD_K_STEERING);

        // DIALOG_DD_WW kann bei DIALOG nicht 1:1 in K_WW übernommen werden sondern es wird Code analog zum POSD-Importer benötigt (siehe DAIMLER-2710)
//        DIALOG_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_WW, FIELD_K_WW); // DIALOG_DD_WW muss vor der Übernahme schon leer gesetzt werden bei WW_ART == ZZ oder Menge == 0

        DIALOG_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_PRODUCT_GRP, FIELD_K_PRODUCT_GRP);
        DIALOG_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_ETZ, FIELD_K_ETZ);

        // Für die KEM-Ketten zur Berechnung der virtuellen Ersetzungen auf Vorgänger- und Nachfolgerständen
        DIALOG_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MIN_KEM_DATE_FROM, FIELD_K_MIN_KEM_DATE_FROM);
        DIALOG_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MAX_KEM_DATE_TO, FIELD_K_MAX_KEM_DATE_TO);
    }

    static {
        // MIGRATION - DIALOG
        MIGRATION_DIALOG_TO_KATALOG_FIELDS_MAPPING.put(FIELD_DD_AA, FIELD_K_AA);
        MIGRATION_DIALOG_TO_KATALOG_FIELDS_MAPPING.put(FIELD_DD_CODES, FIELD_K_CODES);
        MIGRATION_DIALOG_TO_KATALOG_FIELDS_MAPPING.put(FIELD_DD_ETKZ, FIELD_K_ETKZ);
        MIGRATION_DIALOG_TO_KATALOG_FIELDS_MAPPING.put(FIELD_DD_QUANTITY, FIELD_K_MENGE);
        MIGRATION_DIALOG_TO_KATALOG_FIELDS_MAPPING.put(FIELD_DD_HIERARCHY, FIELD_K_HIERARCHY);
        MIGRATION_DIALOG_TO_KATALOG_FIELDS_MAPPING.put(FIELD_DD_SDATA, FIELD_K_DATEFROM);
        MIGRATION_DIALOG_TO_KATALOG_FIELDS_MAPPING.put(FIELD_DD_SDATB, FIELD_K_DATETO);
        MIGRATION_DIALOG_TO_KATALOG_FIELDS_MAPPING.put(FIELD_DD_STEERING, FIELD_K_STEERING);
        MIGRATION_DIALOG_TO_KATALOG_FIELDS_MAPPING.put(FIELD_DD_WW, FIELD_K_WW); // DIALOG_DD_WW muss vom Importer vorher schon leer gesetzt werden bei WW_ART == ZZ oder Menge == 0
        MIGRATION_DIALOG_TO_KATALOG_FIELDS_MAPPING.put(FIELD_DD_PRODUCT_GRP, FIELD_K_PRODUCT_GRP);
        MIGRATION_DIALOG_TO_KATALOG_FIELDS_MAPPING.put(FIELD_DD_ETZ, FIELD_K_ETZ);
    }

    static {
        EDS_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.EDS_QUANTITY, FIELD_K_MENGE);
        EDS_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.EDS_LEVEL, FIELD_K_HIERARCHY);
        EDS_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.EDS_RELEASE_FROM, FIELD_K_DATEFROM);
        EDS_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.EDS_RELEASE_TO, FIELD_K_DATETO);
        EDS_TO_KATALOG_FIELDS_MAPPING.put(iPartsDataVirtualFieldsDefinition.EDS_STEERING, FIELD_K_STEERING);
    }

    static {
        MBS_TO_KATALOG_FIELD_MAPPING.put(iPartsDataVirtualFieldsDefinition.MBS_QUANTITY, FIELD_K_MENGE);
        MBS_TO_KATALOG_FIELD_MAPPING.put(iPartsDataVirtualFieldsDefinition.MBS_RELEASE_FROM, FIELD_K_DATEFROM);
        MBS_TO_KATALOG_FIELD_MAPPING.put(iPartsDataVirtualFieldsDefinition.MBS_RELEASE_TO, FIELD_K_DATETO);
    }

    /**
     * Erzeugt den Quell-Modul-Kontext zum Abspeichern in der Datenbank für den angegebenen <i>moduleType</i> mit den
     * übergebenen Parametern.
     * Codiert ist das getrennt mit & Zeichen und die Inhalte sind UrlEncoded
     *
     * @param moduleType
     * @param sourceId   DIALOG: BR/HM/S/SM; EDS: SAA
     * @return
     */
    public static String createSourceContext(iPartsEntrySourceType moduleType, IdWithType sourceId) {
        if (moduleType != iPartsEntrySourceType.NONE) {
            return sourceId.toString(iPartsConst.K_SOURCE_CONTEXT_DELIMITER, DO_URL_ENCODING_FOR_K_SOURCE_CONTEXT);
        }
        Logger.getLogger().throwRuntimeException("Unsupported module type to create a source context: " + moduleType.name());
        return null;
    }

    /**
     * Liefert alle Bestandteile des übergeben SourceContext
     *
     * @param sourceContext
     * @return
     */
    public static List<String> getSourceContextElements(String sourceContext) {
        if (StrUtils.isEmpty(sourceContext)) {
            return null;
        }
        List<String> elements = StrUtils.toStringList(sourceContext, K_SOURCE_CONTEXT_DELIMITER, false, true);
        List<String> result = new ArrayList<>();
        try {
            // URL Decoding durchführen, wenn beim Erzeugen encoded wurde
            for (String element : elements) {
                if (DO_URL_ENCODING_FOR_K_SOURCE_CONTEXT) {
                    result.add(URLDecoder.decode(element, "UTF-8"));
                } else {
                    result.add(element);
                }
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            Logger.getLogger().handleRuntimeException(e);
            return null;
        }
    }

    /**
     * Liefert die Baureihen-ID vom übergeben DIALOG Quell-Modul-Kontext zurück.
     *
     * @param dialogSourceContext
     * @return
     */
    public static iPartsSeriesId getSeriesIdFromDIALOGSourceContext(String dialogSourceContext) {
        String[] split = dialogSourceContext.split(K_SOURCE_CONTEXT_DELIMITER);
        if (split.length == 4) {
            return new iPartsSeriesId(split[0]);
        }
        return null;
    }

    /**
     * Stücklisteneintrag bei (manueller Übernahme) aus Konstruktion -> AS anlegen
     * Erzeugt einen neuen {@link EtkDataPartListEntry} für den angegebenen <i>moduleType</i> mit den übergebenen Quelldaten
     * und <i>destPartListEntryId</i>.
     *
     * @param sourceContext
     * @param sourcePartListEntry
     * @param destPartListEntryId
     * @param moduleType
     * @param isMBS
     * @param project
     * @param logMessages         für Ausgabe in Konsole des Importers bzw. MessageDialog bei manueller Übernahme
     * @return
     */
    public static EtkDataPartListEntry createRetailPartListEntry(String sourceContext, EtkDataPartListEntry sourcePartListEntry,
                                                                 PartListEntryId destPartListEntryId, iPartsModuleTypes moduleType,
                                                                 boolean isMBS, EtkProject project, List<String> logMessages) {
        EtkDataPartListEntry destPartListEntry = EtkDataObjectFactory.createDataPartListEntry(project, destPartListEntryId);

        // virtuelle Attribute werden prinzipiell nicht abgespeichert und machen daher hier auch keine Probleme
        destPartListEntry.assignAttributes(project, sourcePartListEntry.getAttributes(), false, DBActionOrigin.FROM_EDIT);

        // DAIMLER-4825: DIALOG Coderegel, Menge, Strukturstufe mit Stand zum Zeitpunkt der Übernahme in die AS-StüLi als
        // Spezialfelder aus der Konstruktion übernehmen (DIALOG_TO_KATALOG_FIELDS_MAPPING) kann hierfür nicht verwendet werden,
        // weil die entsprechenden DIALOG_DD-Felder bereits im Mapping enthalten sind für die normalen AS-Katalog-Felder)
        if (iPartsModuleTypes.isDialogRetailType(moduleType)) {
            destPartListEntry.setAttributeValue(iPartsConst.FIELD_K_CODES_CONST, sourcePartListEntry.getAttribute(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES).getAsString(), DBActionOrigin.FROM_EDIT);
            destPartListEntry.setAttributeValue(iPartsConst.FIELD_K_MENGE_CONST, sourcePartListEntry.getAttribute(iPartsDataVirtualFieldsDefinition.DIALOG_DD_QUANTITY).getAsString(), DBActionOrigin.FROM_EDIT);
            destPartListEntry.setAttributeValue(iPartsConst.FIELD_K_HIERARCHY_CONST, sourcePartListEntry.getAttribute(iPartsDataVirtualFieldsDefinition.DIALOG_DD_HIERARCHY).getAsString(), DBActionOrigin.FROM_EDIT);

            // Übernahme der Ereignisse
            // In erster Linie die DIALOG Konstruktions-AS-Events aus DA_DIALOG_ADD_DATA übernehmen (VBRT), wenn gesetzt ...
            String eventFrom = sourcePartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DAD_EVENT_FROM);
            String eventTo = sourcePartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DAD_EVENT_TO);
            if (StrUtils.isEmpty(eventFrom, eventTo)) {
                // ... ansonsten die DIALOG Konstruktions-Events aus DA_DIALOG (BRTE) übernehmen
                eventFrom = sourcePartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_EVENT_FROM);
                eventTo = sourcePartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_EVENT_TO);
            }
            // Die Event-Konstruktionsfelder setzen
            destPartListEntry.setAttributeValue(iPartsConst.FIELD_K_EVENT_FROM_CONST, eventFrom, DBActionOrigin.FROM_EDIT);
            destPartListEntry.setAttributeValue(iPartsConst.FIELD_K_EVENT_TO_CONST, eventTo, DBActionOrigin.FROM_EDIT);
            // Die Event-Felder bei der Übernahme gleich den Konstruktionsfeldern setzen.
            destPartListEntry.setAttributeValue(iPartsConst.FIELD_K_EVENT_FROM, eventFrom, DBActionOrigin.FROM_EDIT);
            destPartListEntry.setAttributeValue(iPartsConst.FIELD_K_EVENT_TO, eventTo, DBActionOrigin.FROM_EDIT);
        }

        // Ein Klon vom Material des Quell-Stücklisteneintrags im Ziel-Stücklisteneintrag setzen
        destPartListEntry.setAggregatedDataObject(EtkDataPartListEntry.AGGREGATE_NAME_PART, sourcePartListEntry.getPart().cloneMe(project));

        // ID muss nochmal gesetzt werden, weil diese durch die Attribute vom sourcePartListEntry wieder überschrieben wurde
        // sicherheitshalber auch noch das Löschen der alten ID unterbinden
        destPartListEntry.setId(destPartListEntryId, DBActionOrigin.FROM_EDIT);
        destPartListEntry.setDeleteOldId(false);

        SourcePartListEntryRef sourcePartListEntryRef = new SourcePartListEntryRef(moduleType, sourcePartListEntry, isMBS);
        // Quell-Daten in Standard-Katalogfeldern abspeichern
        initCodeReductionFlags(destPartListEntry);  // Felder mit false vorbesetzen
        for (Map.Entry<String, String> sourceToDestFieldMapping : sourcePartListEntryRef.getSourceToDestFieldsMapping().entrySet()) {
            String destFieldValue = getModifiedSourceFieldValue(sourceToDestFieldMapping.getValue(), sourcePartListEntry.getFieldValue(sourceToDestFieldMapping.getKey()),
                                                                destPartListEntry, logMessages);
            destPartListEntry.setFieldValue(sourceToDestFieldMapping.getValue(), destFieldValue, DBActionOrigin.FROM_EDIT);
        }

        // Daten vom Quell-Modul setzen
        assignDataFromSourceModule(sourceContext, sourcePartListEntryRef, destPartListEntry, moduleType);

        return destPartListEntry;
    }

    /**
     * Stücklisteneintrag bei Migrationsimport anlegen
     *
     * @param sourceContext
     * @param sourceDataDialog
     * @param destPartListEntryId
     * @param part
     * @param moduleType
     * @param project
     * @param logMessages         für Ausgabe in Konsole des Importers bzw. MessageDialog bei manueller Übernahme
     * @return
     */
    public static EtkDataPartListEntry createMigrationRetailPartListEntry(String sourceContext, iPartsDataDialogData sourceDataDialog,
                                                                          PartListEntryId destPartListEntryId, EtkDataPart part, iPartsModuleTypes moduleType,
                                                                          EtkProject project, List<String> logMessages) {
        EtkDataPartListEntry destPartListEntry = EtkDataObjectFactory.createDataPartListEntry(project, destPartListEntryId);

        // virtuelle Attribute werden prinzipiell nicht abgespeichert und machen daher hier auch keine Probleme
        destPartListEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

        // Ein Klon vom Material des Quell-Stücklisteneintrags im Ziel-Stücklisteneintrag setzen
        destPartListEntry.setAggregatedDataObject(EtkDataPartListEntry.AGGREGATE_NAME_PART, part.cloneMe(project));

        // ID muss nochmal gesetzt werden, weil diese durch initAttributesWithEmptyValues() wieder überschrieben wurde
        // sicherheitshalber auch noch das Löschen der alten ID unterbinden
        destPartListEntry.setId(destPartListEntryId, DBActionOrigin.FROM_EDIT);
        destPartListEntry.setDeleteOldId(false);

        SourcePartListEntryRef sourcePartListEntryRef = new SourcePartListEntryRef(moduleType, sourceDataDialog);

        // Quell-Daten in Standard-Katalogfeldern abspeichern
        initCodeReductionFlags(destPartListEntry);  // Felder mit false vorbesetzen
        for (Map.Entry<String, String> sourceToDestFieldMapping : sourcePartListEntryRef.getSourceToDestFieldsMapping().entrySet()) {
            String destFieldValue = getModifiedSourceFieldValue(sourceToDestFieldMapping.getValue(), sourceDataDialog.getFieldValue(sourceToDestFieldMapping.getKey()),
                                                                destPartListEntry, logMessages);
            destPartListEntry.setFieldValue(sourceToDestFieldMapping.getValue(), destFieldValue, DBActionOrigin.FROM_EDIT);
        }

        // Daten vom Quell-Modul setzen
        assignDataFromSourceModule(sourceContext, sourcePartListEntryRef, destPartListEntry, moduleType);

        return destPartListEntry;

    }

    /**
     * Initialisiert die Codestring-Reduktions Flags mit "false"
     *
     * @param partListEntry
     */
    public static void initCodeReductionFlags(EtkDataPartListEntry partListEntry) {
        EditModuleHelper.setCodeReductionFlags(partListEntry, null);
    }

    /**
     * Feldwert bei der Übernahme ggf. modifizieren und falls notwendig andere Felder vom {@code destPartlistEntry} setzen.
     *
     * @param fieldName
     * @param fieldValue
     * @param destPartListEntry
     * @param logMessages       für Ausgabe in Konsole des Importers bzw. MessageDialog bei manueller Übernahme
     * @return
     */
    private static String getModifiedSourceFieldValue(String fieldName, String fieldValue, EtkDataPartListEntry destPartListEntry,
                                                      List<String> logMessages) {
        if (fieldName.equals(FIELD_K_CODES)) {
            EditModuleHelper.updateCodeFieldsForPartListEntry(fieldValue, destPartListEntry, false, logMessages);

            // Original-Code in das Feld K_CODES schreiben (also den Original-Code zurückliefern)
            return fieldValue;
        }
        return fieldValue;
    }

    private static void assignDataFromSourceModule(String sourceContext, SourcePartListEntryRef sourcePartListEntryRef, EtkDataPartListEntry destPartListEntry,
                                                   iPartsModuleTypes moduleType) {
        destPartListEntry.setFieldValue(iPartsConst.FIELD_K_SOURCE_TYPE, moduleType.getSourceType().getDbValue(), DBActionOrigin.FROM_EDIT);
        destPartListEntry.setFieldValue(iPartsConst.FIELD_K_SOURCE_CONTEXT, sourceContext, DBActionOrigin.FROM_EDIT);
        destPartListEntry.setFieldValue(iPartsConst.FIELD_K_SOURCE_REF1, sourcePartListEntryRef.getSourceRef1(), DBActionOrigin.FROM_EDIT);
        destPartListEntry.setFieldValue(iPartsConst.FIELD_K_SOURCE_REF2, sourcePartListEntryRef.getSourceRef2(), DBActionOrigin.FROM_EDIT);
        destPartListEntry.setFieldValue(iPartsConst.FIELD_K_SOURCE_GUID, sourcePartListEntryRef.getSourceGUID(), DBActionOrigin.FROM_EDIT);
    }

    /**
     * Liefert alle {@link EtkDataPartListEntry}s aus den AS-Stücklisten/Modulen, die als Quelle den angegebenen <i>sourceModuleType</i>
     * und <i>sourceContext</i> besitzen und sich im optionalen Modul mit der angegeben <i>destAssemblyId</i> befinden.
     *
     * @param sourceModuleType
     * @param sourceContext
     * @param destAssemblyId   Optionale {@link AssemblyId} zur Einschränkung der Ergebnisse; sonst null
     * @param project
     * @return
     */
    public static List<EtkDataPartListEntry> getRetailPartListEntries(iPartsEntrySourceType sourceModuleType, String sourceContext,
                                                                      AssemblyId destAssemblyId, EtkProject project) {
        String[] whereFields;
        String[] whereValues;
        if (destAssemblyId != null) {
            whereFields = new String[]{ FIELD_K_VARI, FIELD_K_VER, FIELD_K_SOURCE_TYPE, FIELD_K_SOURCE_CONTEXT };
            whereValues = new String[]{ destAssemblyId.getKVari(), destAssemblyId.getKVer(), sourceModuleType.getDbValue(), sourceContext };
        } else {
            whereFields = new String[]{ FIELD_K_SOURCE_TYPE, FIELD_K_SOURCE_CONTEXT };
            whereValues = new String[]{ sourceModuleType.getDbValue(), sourceContext };
        }

        DBDataObjectAttributesList retailPLEsAttributesList = project.getDbLayer().getAttributesList(TABLE_KATALOG, whereFields, whereValues);

        List<EtkDataPartListEntry> retailPartListEntries = new ArrayList<>(retailPLEsAttributesList.size());
        for (DBDataObjectAttributes attributes : retailPLEsAttributesList) {
            EtkDataPartListEntry retailPartListEntry = EtkDataObjectFactory.createDataPartListEntry(project, attributes);
            retailPartListEntries.add(retailPartListEntry);
        }

        return retailPartListEntries;
    }

    /**
     * Liefert alle Quell-GUIDs {@link #FIELD_K_SOURCE_GUID} aus den AS-Stücklisten/Modulen, die als Quelle den angegebenen
     * <i>sourceModuleType</i> und <i>sourceContext</i> besitzen und sich im optionalen Modul mit der angegeben <i>destAssemblyId</i>
     * befinden.
     *
     * @param sourceModuleType
     * @param sourceContext
     * @param destAssemblyId   Optionale {@link AssemblyId} zur Einschränkung der Ergebnisse; sonst null
     * @param project
     * @return
     */
    public static Set<String> getRetailSourceGUIDs(iPartsEntrySourceType sourceModuleType, String sourceContext,
                                                   AssemblyId destAssemblyId, boolean loadForEDS, EtkProject project) {
        // DAIMLER-10563 TODO Anpassung Logik für "In Verwendung" bei EDS
        String sourceField = loadForEDS ? FIELD_K_SOURCE_REF1 : FIELD_K_SOURCE_GUID;
        String[] selectFields = new String[]{ sourceField };

        String[] whereFields;
        String[] whereValues;
        if (destAssemblyId != null) {
            whereFields = new String[]{ FIELD_K_VARI, FIELD_K_VER, FIELD_K_SOURCE_TYPE, FIELD_K_SOURCE_CONTEXT };
            whereValues = new String[]{ destAssemblyId.getKVari(), destAssemblyId.getKVer(), sourceModuleType.getDbValue(), sourceContext };
        } else {
            whereFields = new String[]{ FIELD_K_SOURCE_TYPE, FIELD_K_SOURCE_CONTEXT };
            whereValues = new String[]{ sourceModuleType.getDbValue(), sourceContext };
        }

        DBDataObjectAttributesList retailSourceGUIDsList = project.getDbLayer().getAttributesList(TABLE_KATALOG, selectFields,
                                                                                                  whereFields, whereValues,
                                                                                                  ExtendedDataTypeLoadType.NONE,
                                                                                                  false, true);

        Set<String> sourceGUIDs = new HashSet<>();
        for (DBDataObjectAttributes dataObjectAttributes : retailSourceGUIDsList) {
            String sourceGUID = dataObjectAttributes.getFieldValue(sourceField);
            sourceGUIDs.add(sourceGUID);
        }
        return sourceGUIDs;
    }

    /**
     * Liefert alle {@link EtkDataPartListEntry}s aus den AS-Stücklisten/Modulen, die als Quelle den angegebenen <i>sourceModuleType</i>
     * und <i>sourceGuid</i> besitzen und sich im optionalen Modul mit der angegeben <i>destAssemblyId</i> befinden.
     *
     * @param sourceModuleType
     * @param sourceGuid
     * @param destAssemblyId   Optionale {@link AssemblyId} zur Einschränkung der Ergebnisse; sonst null
     * @param project
     * @return
     */
    public static List<EtkDataPartListEntry> getRetailSourceGuidPartListEntries(iPartsEntrySourceType sourceModuleType, String sourceGuid,
                                                                                AssemblyId destAssemblyId, EtkProject project) {
        String[] whereFields;
        String[] whereValues;
        if (destAssemblyId != null) {
            whereFields = new String[]{ FIELD_K_VARI, FIELD_K_VER, FIELD_K_SOURCE_TYPE, FIELD_K_SOURCE_GUID };
            whereValues = new String[]{ destAssemblyId.getKVari(), destAssemblyId.getKVer(), sourceModuleType.getDbValue(), sourceGuid };
        } else {
            whereFields = new String[]{ FIELD_K_SOURCE_TYPE, FIELD_K_SOURCE_GUID };
            whereValues = new String[]{ sourceModuleType.getDbValue(), sourceGuid };
        }

        DBDataObjectAttributesList retailPLEsAttributesList = project.getDbLayer().getAttributesList(TABLE_KATALOG, whereFields, whereValues);

        List<EtkDataPartListEntry> retailPartListEntries = new ArrayList<>(retailPLEsAttributesList.size());
        for (DBDataObjectAttributes attributes : retailPLEsAttributesList) {
            EtkDataPartListEntry retailPartListEntry = EtkDataObjectFactory.createDataPartListEntry(project, attributes);
            retailPartListEntries.add(retailPartListEntry);
        }

        return retailPartListEntries;
    }

    /**
     * Sucht mit LIKE im KATALOG nach der {@code partialSourceGuid} und liefert die in {@code fields} definierten Attribute
     * ({@code null} = alle).
     *
     * @param sourceModuleType
     * @param partialSourceGuid     Teileweise SourceGuid mit Wildcards
     * @param optionalSourceContext Optionaler SourceContext zur schnelleren Einschränkung in der SQL-Query
     * @param fields                Elemente der DBDataObjectAttributes; null = alle
     * @param project
     * @return
     */
    public static DBDataObjectAttributesList getRetailSourceGuidAttributeList(iPartsEntrySourceType sourceModuleType,
                                                                              String partialSourceGuid,
                                                                              String optionalSourceContext,
                                                                              String[] fields,
                                                                              EtkProject project) {
        return getRetailSourceGuidAttributeList(sourceModuleType, partialSourceGuid, optionalSourceContext, fields, true, project);
    }

    /**
     * Sucht mit LIKE im KATALOG nach der {@code partialSourceGuid} und liefert die in {@code fields} definierten Attribute
     * ({@code null} = alle).
     *
     * @param sourceModuleType
     * @param partialSourceGuid     Teileweise SourceGuid mit Wildcards
     * @param optionalSourceContext Optionaler SourceContext zur schnelleren Einschränkung in der SQL-Query
     * @param fields                Elemente der DBDataObjectAttributes; null = alle
     * @param distinct              Suche distinct ja/nein
     * @param project
     * @return
     */
    public static DBDataObjectAttributesList getRetailSourceGuidAttributeList(iPartsEntrySourceType sourceModuleType,
                                                                              String partialSourceGuid,
                                                                              String optionalSourceContext,
                                                                              String[] fields, boolean distinct,
                                                                              EtkProject project) {
        // DAIMLER-10563 TODO Anpassung Logik für "In Verwendung" bei EDS
        String[] whereFields;
        String[] whereValues;
        String fieldForPartialGuid = (sourceModuleType == iPartsEntrySourceType.EDS) ? FIELD_K_SOURCE_REF1 : FIELD_K_SOURCE_GUID;
        if (StrUtils.isValid(optionalSourceContext)) {
            whereFields = new String[]{ FIELD_K_SOURCE_TYPE, FIELD_K_SOURCE_CONTEXT, fieldForPartialGuid };
            whereValues = new String[]{ sourceModuleType.getDbValue(), optionalSourceContext, partialSourceGuid };
        } else {
            whereFields = new String[]{ FIELD_K_SOURCE_TYPE, fieldForPartialGuid };
            whereValues = new String[]{ sourceModuleType.getDbValue(), partialSourceGuid };
        }

        return project.getDbLayer().getAttributesList(TABLE_KATALOG, fields,
                                                      whereFields, whereValues,
                                                      ExtendedDataTypeLoadType.MARK,
                                                      StrUtils.stringContainsWildcards(partialSourceGuid), distinct);
    }

    public static DBDataObjectAttributesList getRetailMatNrAtrributeListFilteredWithSeriesNo(String seriesNo, String matNr, String[] fields, boolean distinct,
                                                                                             EtkProject project) {
        String[] whereFields = new String[]{ FIELD_K_SOURCE_GUID, FIELD_K_MATNR };
        String[] whereValues = new String[]{ seriesNo, matNr };
        return project.getDbLayer().getAttributesList(TABLE_KATALOG, fields,
                                                      whereFields, whereValues,
                                                      ExtendedDataTypeLoadType.MARK,
                                                      StrUtils.stringContainsWildcards(seriesNo), distinct);
    }

    // Gemeinsamen Schlüssel berechnen, den alle Stücklisteneinträge für einen identischen Wahlweise-Wert haben müssen
    private static String calculateWWKey(EtkDataPartListEntry partListEntry, String ww, boolean fromMigration) {
        StringBuilder str = new StringBuilder();
        if (fromMigration) { // hotspot nur bei Migration berücksichtigen; nicht bei Übernahme aus Konstruktion
            str.append(partListEntry.getFieldValue(FIELD_K_POS));  // Bild-Hotspot
            str.append(K_SOURCE_CONTEXT_DELIMITER);
        }
        str.append(partListEntry.getFieldValue(FIELD_K_SOURCE_CONTEXT)); // Baureihe, HM, M, SM
        str.append(K_SOURCE_CONTEXT_DELIMITER);
        str.append(partListEntry.getFieldValue(FIELD_K_SOURCE_REF1)); // DIALOG-Position
        str.append(K_SOURCE_CONTEXT_DELIMITER);
        str.append(partListEntry.getFieldValue(FIELD_K_SOURCE_REF2)); // DIALOG-Positionsvariante
        str.append(K_SOURCE_CONTEXT_DELIMITER);
        str.append(partListEntry.getFieldValue(FIELD_K_AA)); // Ausführungsart
        str.append(K_SOURCE_CONTEXT_DELIMITER);
        if (!StrUtils.isEmpty(ww)) {
            str.append(ww.charAt(0));  // 1. Zeichen des WW-Kenners
        }
        return str.toString();
    }

    private static void wwCheckAmount(Map<String, Collection<EtkDataPartListEntry>> wwCheckMap) {
        // Regeln für Menge anwenden
        Iterator<String> iterWWGUIDs = wwCheckMap.keySet().iterator();
        while (iterWWGUIDs.hasNext()) {
            String wwGUID = iterWWGUIDs.next();
            Collection<EtkDataPartListEntry> wwPartListEntries = wwCheckMap.get(wwGUID);

            // wir suchen im WW-Set nach einer Menge <> 0
            String trueQuantity = null;
            for (EtkDataPartListEntry partListEntry : wwPartListEntries) {
                String quantity = partListEntry.getFieldValue(FIELD_K_MENGE);
                if (StrUtils.isFloat(quantity) && !quantity.equals("0")) {
                    trueQuantity = quantity;
                    break; // Erster Mengenwert wird genommen
                }
            }

            if (trueQuantity != null) {
                // Stüli-Einträge mit Menge 0 erhalten die Menge eines beliebigen anderen Stüli-Eintrags des WW-Sets und werden aus dem WW-Set entfernt
                Iterator<EtkDataPartListEntry> iter = wwPartListEntries.iterator();
                while (iter.hasNext()) {
                    EtkDataPartListEntry partListEntry = iter.next();
                    String quantity = partListEntry.getFieldValue(FIELD_K_MENGE);
                    if (quantity.equals("0")) {
                        partListEntry.setFieldValue(FIELD_K_MENGE, trueQuantity, DBActionOrigin.FROM_EDIT);
                        partListEntry.setFieldValue(FIELD_K_WW, "", DBActionOrigin.FROM_EDIT);
                        iter.remove();
                    }
                }
            } else {
                // es ist kein WW-Set -> WW-Set entfernen und WW-Kennzeichen aus Stüli-Einträgen löschen
                for (EtkDataPartListEntry partListEntry : wwPartListEntries) {
                    partListEntry.setFieldValue(FIELD_K_WW, "", DBActionOrigin.FROM_EDIT);
                }
                wwPartListEntries.clear();
                iterWWGUIDs.remove();
            }
        }
    }

    private static void wwCheckDistinctWWKeyAndPartnumber(Map<String, Collection<EtkDataPartListEntry>> wwCheckMap, boolean fromImport) {
        Iterator<String> iterWWGUIDs = wwCheckMap.keySet().iterator();
        while (iterWWGUIDs.hasNext()) {
            String wwGUID = iterWWGUIDs.next();
            Collection<EtkDataPartListEntry> wwPartListEntries = wwCheckMap.get(wwGUID);

            Set<String> distinctWWValues = new HashSet<>();
            Set<String> distinctPartNumbers = new HashSet<>();
            boolean distinctWWValuesAndPartNumbersFound = false;
            for (EtkDataPartListEntry partListEntry : wwPartListEntries) {
                String ww = "";
                if (fromImport) {
                    ww = partListEntry.getFieldValue(FIELD_K_WW);
                } else {
                    iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(partListEntry.getFieldValue(FIELD_K_SOURCE_GUID));
                    if (bcteKey != null) {
                        ww = bcteKey.ww;
                    }
                }
                distinctWWValues.add(ww);
                distinctPartNumbers.add(partListEntry.getFieldValue(FIELD_K_MATNR));

                // Sobald mindestens 2 unterschiedliche WW-Werte und Teilenummern gefunden wurden, kann die Prüfung abgebrochen werden
                if ((distinctWWValues.size() >= 2) && (distinctPartNumbers.size() >= 2)) {
                    distinctWWValuesAndPartNumbersFound = true;
                    break;
                }
            }

            // Sind nur noch Stücklisteneinträge mit identischem WW-Wert oder identischer Teilenummer im WW-Set, dann
            // handelt es sich nicht um ein echtes WW-Set und es wird entfernt
            if (!distinctWWValuesAndPartNumbersFound) {
                for (EtkDataPartListEntry partListEntry : wwPartListEntries) {
                    partListEntry.setFieldValue(FIELD_K_WW, "", DBActionOrigin.FROM_EDIT);
                }
                wwPartListEntries.clear();
                iterWWGUIDs.remove();
            }
        }
    }

    /**
     * Konvertiert alle zusammengehörenden Wahlweise-Werte für die übergebenen DIALOG-Stücklisteneinträge in GUIDs
     * (aufsteigende Zahlen beginnend bei 1). Es müssen alle Stücklisteneinträge eines Moduls übergeben werden, da es
     * ansonsten zu doppelten Wahlweise-Werten kommen könnte, wenn andere Stücklisteneinträge bereits Wahlweise-Werte haben.
     *
     * @param partListEntries
     */
    public static void convertWWValuesForDIALOGPartListEntries(DBDataObjectList<EtkDataPartListEntry> partListEntries) {
        // WW-Sets aufbauen (Zuordnung WW-GUIDs ->> Stücklisteneinträge )
        Map<String, String> wwMap = new HashMap<>();
        Map<String, Collection<EtkDataPartListEntry>> wwCheckMap = new HashMap<>();
        for (EtkDataPartListEntry partListEntry : partListEntries) {
            // DAIMLER-6809: Dieser Stücklisteneintrag soll nur im Baumuster-Filter verwendet werden -> keine Wahlweise-Sets
            // damit aufbauen
            if (partListEntry.getFieldValueAsBoolean(FIELD_K_ONLY_MODEL_FILTER)) {
                continue;
            }

            String ww = partListEntry.getFieldValue(FIELD_K_WW);
            if (!ww.isEmpty()) {

                // Positionen mit 1-stelligen WW-Kennzeichen sind nicht WW
                if (ww.trim().length() == 1) {
                    partListEntry.setFieldValue(FIELD_K_WW, "", DBActionOrigin.FROM_EDIT);
                    continue;
                }

                // Gemeinsamen Schlüssel berechnen, den alle Stücklisteneinträge für einen identischen Wahlweise-Wert haben müssen
                String wwKey = calculateWWKey(partListEntry, ww, true);

                // Wahlweise-GUID für wwKey bestimmen
                String wwGUID = wwMap.get(wwKey);
                if (wwGUID == null) {
                    wwGUID = String.valueOf(wwMap.size() + 1);
                    wwMap.put(wwKey, wwGUID);
                }

                // WW-Set ggf. erzeugen und um Stüli-Eintrag erweitern
                Collection<EtkDataPartListEntry> wwPartListEntries = wwCheckMap.get(wwGUID);
                if (wwPartListEntries == null) {
                    wwPartListEntries = new DwList<>();
                    wwCheckMap.put(wwGUID, wwPartListEntries);
                }
                wwPartListEntries.add(partListEntry);
            }
        }

        // Regeln für Menge anwenden; wwCheckMap wird ggf. modifiziert
        wwCheckAmount(wwCheckMap);

        // die Teile eines WW-Sets müssen sich in der 2. Stelle des WW-Kenners unterscheiden und unterschiedl. Teilenummern haben
        wwCheckDistinctWWKeyAndPartnumber(wwCheckMap, true);

        // Jetzt GUID des WW-Sets als WW-Kenner in Teilepositionen einsetzen
        for (Map.Entry<String, Collection<EtkDataPartListEntry>> wwEntry : wwCheckMap.entrySet()) {
            Collection<EtkDataPartListEntry> wwPartListEntries = wwEntry.getValue();
            for (EtkDataPartListEntry partListEntry : wwPartListEntries) {
                partListEntry.setFieldValue(FIELD_K_WW, wwEntry.getKey(), DBActionOrigin.FROM_EDIT);
            }
        }
    }

    public static iPartsDataReplacePart createAndAddRetailReplacement(EtkProject project, iPartsDataReplacePartList replacementsRetail,
                                                                      iPartsDataIncludePartList includePartsRetail, EtkDataPartListEntry predecessorRetail,
                                                                      EtkDataPartListEntry successorRetail, iPartsReplacementConst replacementConst) {
        // Keine Ersetzung auf sich selbst erzeugen
        if (predecessorRetail.getAsId().getKLfdnr().equals(successorRetail.getAsId().getKLfdnr())) {
            return null;
        }

        if (replacementConst.isSourceTS7()) {
            // DAIMLER-7551: Eventuell existiert eine vom Primärschlüssel her gleiche VTNV Ersetzung mit anderem Vorgänger
            // In dem Fall wird die TS7 Ersetzung zwar separat angezeigt, weil der Vorgänger ein anderer ist.
            // Aufgrund des gleichen Schlüssels soll sie aber trotzdem vom VTNV Pendant unterdrückt und nicht übernommen werden.
            iPartsReplaceConstMatId replacementVTNVId = new iPartsReplaceConstMatId(replacementConst.getSuccessorPartNo(),
                                                                                    replacementConst.sDatA);
            iPartsDataReplaceConstMat replacementVTNV = new iPartsDataReplaceConstMat(project, replacementVTNVId);
            if (replacementVTNV.existsInDB()) {
                return null;
            }
        }

        // Ersetzungs- und Mitlieferteile-Datenobjekte erstellen, die am Ende in der DB gespeichert werden.
        iPartsDataReplacePart newRetailDataReplacement = EditConstructionToRetailHelper.createRetailReplacement(project,
                                                                                                                predecessorRetail,
                                                                                                                successorRetail,
                                                                                                                replacementConst);
        iPartsDataReplacePart existingDataReplacement = EditConstructionToRetailHelper.addNewRetailReplacementReturnExisting(replacementsRetail,
                                                                                                                             newRetailDataReplacement);

        // Mitlieferteile hinzufügen bzw. aktualisieren
        EditConstructionToRetailHelper.updateRetailIncludeParts(project, predecessorRetail, successorRetail, includePartsRetail,
                                                                replacementConst, (existingDataReplacement != null)
                                                                                  ? existingDataReplacement
                                                                                  : newRetailDataReplacement);

        if (existingDataReplacement == null) {
            return newRetailDataReplacement;
        } else {
            return null;
        }
    }

    /**
     * Konvertiert eine Konstruktions-Ersetzung ({@link iPartsReplacementConst}) am Teilestamm in ein Retail-Ersetzungs-Datenobjekt ({@link iPartsDataReplacePart}).
     *
     * @param project
     * @param predecessorEntryRetail Der Vorgänger in der AS-Stückliste (der aus dem Vorgänger aus der Konstruktion entstanden ist)
     * @param successorEntryRetail   Der Nachfolger in der AS-Stückliste (der aus dem Nachfolger aus der Konstruktion entstanden ist)
     * @param replacementConst       Die Ersetzung aus der Konstruktion, aus der die Retail-Ersetzung mit Mitlieferteilen erzeugt wird
     * @return Die neu erzeugte Retail-Ersetzung.
     */
    public static iPartsDataReplacePart createRetailReplacement(EtkProject project, EtkDataPartListEntry predecessorEntryRetail,
                                                                EtkDataPartListEntry successorEntryRetail, iPartsReplacementConst replacementConst) {
        iPartsReplacement replacementRetail = new iPartsReplacement();
        replacementRetail.predecessorEntry = predecessorEntryRetail;
        replacementRetail.successorEntry = successorEntryRetail;
        replacementRetail.setSuccessorPartNumber(successorEntryRetail.getPart().getMatAttributes().getFieldValue(FIELD_M_MATNR));

        // DAIMLER-7476: Bei Sperrvermerk = "S" sollen die RFMEA Flags "   W  0" und die RFMEN Flags "      0" sein
        // DAIMLER-7475: Genauso, wenn die Ersetzung aus TS7 kam
        if (replacementConst.lockFlag.equals(iPartsReplacementConst.LOCK_FLAG_S) || replacementConst.isSourceTS7()) {
            replacementRetail.rfmeaFlags = iPartsRFMEA.getByEnumToken(project, iPartsRFMEA.ENUM_TOKEN_W0);
            replacementRetail.rfmenFlags = iPartsRFMEN.getByEnumToken(project, iPartsRFMEN.ENUM_TOKEN_0);
        } else {
            replacementRetail.rfmeaFlags = replacementConst.preRFMEFlags;
            replacementRetail.rfmenFlags = replacementConst.RFMEFlags;
        }
        // Setzen der Quelle. Konstruktionsersetzungen haben entweder TS7 oder VTNV als Quelle. Dies auch der AS-Ersetzung bekannt machen
        if (replacementConst.isSourceTS7()) {
            replacementRetail.source = iPartsReplacement.Source.TS7;
        }
        if (replacementConst.isSourceVTNV()) {
            replacementRetail.source = iPartsReplacement.Source.VTNV;
        }

        // DIALOG-GUIDs von Vorgänger und Nachfolger setzen
        iPartsDataReplacePart dataReplacePart = replacementRetail.getAsDataReplacePart(project, false);
        dataReplacePart.setFieldValue(FIELD_DRP_SOURCE_GUID, replacementConst.predecessorEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID),
                                      DBActionOrigin.FROM_EDIT);
        dataReplacePart.setFieldValue(FIELD_DRP_REPLACE_SOURCE_GUID, replacementConst.successorEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID),
                                      DBActionOrigin.FROM_EDIT);
        return dataReplacePart;
    }

    /**
     * @param replacementsRetail Liste, die alle bereits existierenden Retail-Ersetzungs-Datenobjekte ({@link iPartsDataReplacePart}) enthält.
     *                           Wird auch benötigt um die nächste freie Seqnzenznummer der neuen Ersetzung zu bestimmen.
     * @param newReplacePart     Die neue Retail-Ersetzung die hinzugefügt werden soll.
     * @return <b>Achtung!</b> Existierendes {@link iPartsDataReplacePart}, falls die neueste Version der Ersetzung (Vorgänger
     * und Nachfolger sind gleich) aus der Liste bereits mit gleichem Dateninhalt existiert; {@code null}, falls die Ersetzung
     * neu angelegt wurde.
     */
    private static iPartsDataReplacePart addNewRetailReplacementReturnExisting(iPartsDataReplacePartList replacementsRetail,
                                                                               iPartsDataReplacePart newReplacePart) {
        iPartsDataReplacePart newestExistingSameReplacement = null;
        int maxSeqNoOfExistingSameReplacement = -1;
        for (iPartsDataReplacePart existingReplacePart : replacementsRetail) {
            if (existingReplacePart.isSameReplacement(newReplacePart)) {
                int existingSeqNo = StrUtils.strToIntDef(existingReplacePart.getAsId().getSeqNo(), -1);
                if (existingSeqNo > maxSeqNoOfExistingSameReplacement) {
                    maxSeqNoOfExistingSameReplacement = existingSeqNo;
                    newestExistingSameReplacement = existingReplacePart;
                }
            }
        }
        if ((newestExistingSameReplacement != null) && newestExistingSameReplacement.isDuplicateOf(newReplacePart)) {
            return newestExistingSameReplacement;
        }

        // Nächste freie Sequenznummer bestimmen
        String replacementSeqNo = iPartsReplacementHelper.getNextReplacementSeqNo(newReplacePart.getEtkProject(),
                                                                                  newReplacePart.getAsId().getPredecessorPartListEntryId(),
                                                                                  replacementsRetail);
        newReplacePart.setFieldValue(FIELD_DRP_SEQNO, replacementSeqNo, DBActionOrigin.FROM_EDIT);
        newReplacePart.updateOldId(); // DRP_SEQNO ist Teil vom Primärschlüssel!

        if (!newReplacePart.existsInDB()) {
            newReplacePart.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        }
        replacementsRetail.add(newReplacePart, DBActionOrigin.FROM_EDIT);
        return null;
    }


    /**
     * Konvertiert alle zur Konstruktions-Ersetzung zugehörigen Mitlieferteile in Retail-Mitlieferteile, falls es welche gibt.
     * Die neu erzeugten {@link iPartsDataIncludePart}s werden in die übergebene Liste eingefügt.
     *
     * @param project
     * @param predecessorEntry
     * @param successorEntry
     * @param includePartsRetail
     * @param replacementConst
     * @param dataReplacementRetail
     */
    private static void updateRetailIncludeParts(EtkProject project, EtkDataPartListEntry predecessorEntry,
                                                 EtkDataPartListEntry successorEntry, iPartsDataIncludePartList includePartsRetail,
                                                 iPartsReplacementConst replacementConst, iPartsDataReplacePart dataReplacementRetail) {
        // Bereits existierende Mitlieferteile bestimmen
        iPartsReplacement existingReplacementRetail = new iPartsReplacement(dataReplacementRetail, predecessorEntry, successorEntry);
        Collection<iPartsReplacement.IncludePart> existingIncludeParts = existingReplacementRetail.getIncludeParts(project);
        iPartsDataIncludePartList existingDataIncludePartList = null;
        if (existingIncludeParts != null) {
            existingDataIncludePartList = existingReplacementRetail.getIncludePartsAsDataIncludePartList(project, true,
                                                                                                         DBActionOrigin.FROM_DB);
        }

        // Retail-Mitlieferteile erzeugen falls Mitlieferteile vorhanden sind und zur Liste hinzufügen
        Collection<iPartsReplacementConst.IncludePartConstMat> includePartsConstMat = replacementConst.getIncludeParts(project);
        if ((includePartsConstMat != null) && !includePartsConstMat.isEmpty()) {
            iPartsReplacement replacementRetail = new iPartsReplacement(dataReplacementRetail, predecessorEntry, successorEntry);
            for (iPartsReplacementConst.IncludePartConstMat includePartConstMat : includePartsConstMat) {
                iPartsReplacement.IncludePart includePartRetail = new iPartsReplacement.IncludePart();
                includePartRetail.partNumber = includePartConstMat.partNumber;
                includePartRetail.quantity = includePartConstMat.quantity;
                replacementRetail.addIncludePart(includePartRetail);
            }

            // Mitlieferteile zunächst löschen und danach hinzufügen falls sie nicht identisch sind
            boolean includePartsAreDifferent = false;
            iPartsDataIncludePartList newDataIncludePartList = replacementRetail.getIncludePartsAsDataIncludePartList(project, false,
                                                                                                                      DBActionOrigin.FROM_EDIT);
            if ((existingDataIncludePartList != null) && !existingDataIncludePartList.isTheSame(newDataIncludePartList, false)) {
                includePartsRetail.deleteAll(existingDataIncludePartList, true, true, DBActionOrigin.FROM_EDIT);
                includePartsAreDifferent = true;
            }

            if (includePartsAreDifferent || (existingDataIncludePartList == null)) {
                includePartsRetail.addAll(newDataIncludePartList, DBActionOrigin.FROM_EDIT);
            }
        } else if (existingDataIncludePartList != null) { // Existierende Mitlieferteile löschen, weil es keine mehr gibt
            includePartsRetail.deleteAll(existingDataIncludePartList, true, true, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Erzeugt Wahlweise Sets aus den neuen Einträgen die aus der Konstruktion übernommen werden.
     * Dabei bleiben nach Möglichkeit vorhandene Wahlweise Sets erhalten, im Konfliktfall (falls manuell bearbeitet wurde)
     * werden die Wahlweise Sets allerdings neu aufgebaut, wodurch die Bearbeitung verloren geht.
     * (siehe Kommentar in https://jira.docware.de/jira/browse/DAIMLER-4823)
     * Es werden nur die Wahlweise Sets ggf. modifiziert in denen die neuen Einträge (partListEntriesFromConstruction)
     * enthalten sind.
     *
     * @param destinationAssembly             Ziel-Modul aus AS
     * @param destinationPartList             Ziel Stückliste aus AS
     * @param partListEntriesFromConstruction zu übernehmende Stücklisteneinträge aus Konstruktion
     */
    public static void addWWPartsFromConstruction(iPartsDataAssembly destinationAssembly, DBDataObjectList<EtkDataPartListEntry> destinationPartList,
                                                  List<EtkDataPartListEntry> partListEntriesFromConstruction) {
        if (partListEntriesFromConstruction.isEmpty()) {
            return;
        }
        List<EtkDataPartListEntry> workList = new ArrayList<>(partListEntriesFromConstruction.size());
        for (EtkDataPartListEntry partListEntryFromConstruction : partListEntriesFromConstruction) {
            iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntryFromConstruction);
            if (bcteKey != null) {
                String ww = bcteKey.ww;
                String ddGes = partListEntryFromConstruction.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GES);
                if (!StrUtils.isEmpty(ww)) {
                    // Positionen mit 1-stelligen WW-Kennzeichen oder "ZZ" sind nicht WW
                    if (!((ww.trim().length() == 1) || iPartsWWPartsHelper.isZZ(ddGes))) {
                        workList.add(partListEntryFromConstruction);
                    }
                }
            }
        }

        Map<String, Collection<EtkDataPartListEntry>> wwCandidates = new TreeMap<>();
        // Gehe die bisherige Stückliste durch und suche nach potentiellen WW Kandidaten
        for (EtkDataPartListEntry partListEntry : workList) {
            iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(partListEntry.getFieldValue(FIELD_K_SOURCE_GUID));//new iPartsDialogBCTEPrimaryKey(partListEntry);
            if (bcteKey != null) {
                String wwKey = calculateWWKey(partListEntry, bcteKey.ww, false);
                for (EtkDataPartListEntry destPartlistEntry : destinationPartList) {
                    iPartsDialogBCTEPrimaryKey destKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(destPartlistEntry.getFieldValue(FIELD_K_SOURCE_GUID));
                    if (destKey != null) {
                        String destWWKey = calculateWWKey(destPartlistEntry, destKey.ww, false);
                        // WW Kenner muss übereinstimmen, dann werden beide als potentiell WW Kandidaten aufgenommen
                        if (wwKey.equals(destWWKey)) {
                            Collection<EtkDataPartListEntry> wwCandidateSet = wwCandidates.computeIfAbsent(wwKey, k -> new HashSet<>());
                            wwCandidateSet.add(destPartlistEntry);
                            wwCandidateSet.add(partListEntry);
                        }
                    }
                }
            }
        }

        // Logik aus Importer übernommen:
        // Mengen prüfen und sicherstellen dass unterschiedliche WW Kenner und Materialnummnern in der Liste enthalten sein
        wwCheckAmount(wwCandidates);
        wwCheckDistinctWWKeyAndPartnumber(wwCandidates, false);

        // Für die Kandidaten die Zeitscheiben prüfen
        for (Map.Entry<String, Collection<EtkDataPartListEntry>> wwCandidate : wwCandidates.entrySet()) {
            List<iPartsFilterTimeSliceHelper.TimeSliceContent> timeSlices = new DwList<>();
            Collection<EtkDataPartListEntry> wwParts = wwCandidate.getValue();

            for (EtkDataPartListEntry wwPart : wwParts) {
                // Für jeden Eintrag den Konstruktions-Datensatz ermitteln und dort das KEM ab/bis Datum nehmen
                iPartsDataDialogData dialogData = new iPartsDataDialogData(wwPart.getEtkProject(), new iPartsDialogId(wwPart.getFieldValue(FIELD_K_SOURCE_GUID)));
                String sdata = dialogData.getFieldValue(FIELD_DD_SDATA);
                String sdatb = dialogData.getFieldValue(FIELD_DD_SDATB);
                timeSlices.add(new iPartsFilterTimeSliceHelper.TimeSliceContent(StrUtils.strToLongDef(sdata, 0), StrUtils.strToLongDef(sdatb, 0), wwPart));
            }
            List<List<EtkDataPartListEntry>> mergeResult = iPartsFilterTimeSliceHelper.mergeAnyOverlappingTimeSlices(timeSlices);

            // hier dürfen nur noch die WW Sets weiterverarbeitet werden in denen die neu zu übernehmenen Stücklisten
            // Einträge enthalten sind, sonst würden bereits existierende WW Beziehungen zerstört werden
            List<List<EtkDataPartListEntry>> cleanedMergeResult = new DwList<>();
            for (List<EtkDataPartListEntry> mergedWWset : mergeResult) {
                if (mergedWWset.size() > 1) { // Wahlweise Sets mit nur einem Element machen keinen Sinn
                    for (EtkDataPartListEntry wwEntry : mergedWWset) {
                        if (workList.contains(wwEntry)) {
                            cleanedMergeResult.add(mergedWWset);
                            break;
                        }
                    }
                }
            }

            destinationAssembly.createWWSetsForPartList(destinationPartList); // Sicherstellen, dass die wwMap aktuell ist in destinationAssembly
            String wwGUID = destinationAssembly.getNextUnusedWWGUID();
            // bereits verwendete WW-Kenner in den bearbeiteten Sets aufsammeln
            for (List<EtkDataPartListEntry> mergedWWset : cleanedMergeResult) {
                Set<String> existingWWGUIDs = new TreeSet<>();
                for (EtkDataPartListEntry wwContent : mergedWWset) {
                    String ww = wwContent.getFieldValue(FIELD_K_WW);
                    if (!ww.isEmpty()) {
                        existingWWGUIDs.add(ww);
                    }
                }
                // wenn es genau einen WW-Kenner gibt, dann diesen verwenden
                if (existingWWGUIDs.size() == 1) {
                    wwGUID = existingWWGUIDs.iterator().next();
                }
                // Wenn es in einem WW-Set mehrere verschiedene WW-Kenner gibt (z.B. durch Bearbeitung) dann wird ein
                // neuer WW-Kenner verwendet. Dadurch werden bereits vorhandene Bearbeitungen wieder zurück gesetzt.
                // Das ist aktuell so gewünscht. Wird aber später wahrscheinlich durch Abfragen an den Benutzer
                // erweitert. (siehe Kommentar in https://jira.docware.de/jira/browse/DAIMLER-4823)
                for (EtkDataPartListEntry wwContent : mergedWWset) {
                    wwContent.setFieldValue(FIELD_K_WW, wwGUID, DBActionOrigin.FROM_EDIT);
                }
                wwGUID = Integer.toString(Integer.parseInt(wwGUID) + 1);
            }
        }
    }

    /**
     * Überprüft, ob der übergebene BCTE Schlüssel an einem Stücklisteneintarg im After-Sales hängt. Zuerst wird in der
     * DB geschaut und falls es dort keine Treffer gibt, dann in den Changesets. Falls ein AS-Datensatz gefunden wird,
     * ist das der Hinweis darauf, dass der Stücklisteneintrag aus der Konstruktion
     *
     * @param project
     * @param bcteKey
     * @return
     */
    public static boolean checkIfUsedInAS(EtkProject project, String bcteKey) {
        List<EtkDataPartListEntry> retailPartListEntries = EditConstructionToRetailHelper.getRetailSourceGuidPartListEntries(iPartsEntrySourceType.DIALOG, bcteKey, null, project);
        boolean existsInAS = !retailPartListEntries.isEmpty();
        // Keine Verwendung in freigegebenen Retail-Stücklisten -> Verwendung in nicht freigegebenen ChangeSets suchen
        if (!existsInAS) {
            Set<iPartsChangeSetId> changeSetIds = iPartsRevisionsHelper.searchActiveChangeSetsContainingSourceGUID(PartListEntryId.TYPE,
                                                                                                                   bcteKey, project);
            existsInAS = !changeSetIds.isEmpty();
        }
        return existsInAS;
    }

    /**
     * Ermittelt zum übergebenen Retail Stücklisteneintrag über den BCTE Schlüssel den entsprechenden Konstruktions Stücklisteneintrag
     *
     * @param partListEntry
     * @return Die Konstruktions PartlistEntryId
     */
    public static PartListEntryId getVirtualConstructionPartlistEntryIdFromRetailPartlistEntry(EtkDataPartListEntry partListEntry) {
        String sourceGuid = partListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
        if (StrUtils.isValid(sourceGuid)) {
            iPartsEntrySourceType sourceType = iPartsEntrySourceType.getFromDbValue(partListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_TYPE));
            if (sourceType == iPartsEntrySourceType.DIALOG) {
                iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(sourceGuid);
                if (bcteKey != null) {
                    String virtualIdString = iPartsVirtualNode.getVirtualIdString(bcteKey.getHmMSmId());
                    return new PartListEntryId(virtualIdString, "", bcteKey.createDialogGUID());
                }
            }
        }
        return null;
    }

    /**
     * Ermittelt zum übergebenen Retail Stücklisteneintrag über BCTE Schlüssel die entsprechende Konstruktions AssemblyId
     *
     * @param partListEntry
     * @return Die Konstruktions AssemblyId
     */
    public static AssemblyId getVirtualConstructionAssemblyIdFromRetailPartlistEntry(EtkDataPartListEntry partListEntry) {
        PartListEntryId partListEntryId = getVirtualConstructionPartlistEntryIdFromRetailPartlistEntry(partListEntry);
        if (partListEntryId != null) {
            return partListEntryId.getOwnerAssemblyId();
        }
        return null;
    }

    /**
     * Berechnet das minimale KEM-Datum ab sowie das maximale KEM-Datum bis für den DIALOG-Stücklisteneintrag mit dem übergebenen
     * BCTE-Schlüssel über den {@link iPartsDIALOGPositionsHelper} und legt die Ergebnisse in den virtuellen Feldern
     * {@link iPartsDataVirtualFieldsDefinition#DIALOG_DD_CALCULATED_MIN_KEM_DATE_FROM} sowie {@link iPartsDataVirtualFieldsDefinition#DIALOG_DD_CALCULATED_MAX_KEM_DATE_TO}
     * ab für den späteren Zugriff inkl. Cache-Funktion an allen relevanten DIALOG-Positionsvarianten.
     *
     * @param bctePrimaryKey
     * @param dialogPositionsHelper
     * @return DIALOG-Stücklisteneintrag für den übergebenen BCTE-Schlüssel bzw. {@code null} falls dieser nicht
     * gefunden werden konnte
     */
    public static EtkDataPartListEntry calculateMinMaxKEMDates(iPartsDialogBCTEPrimaryKey bctePrimaryKey, iPartsDIALOGPositionsHelper dialogPositionsHelper) {
        EtkDataPartListEntry constructionPLE = dialogPositionsHelper.getPositionVariantByBCTEKey(bctePrimaryKey);
        if (constructionPLE != null) {
            calculateMinMaxKEMDates(constructionPLE, dialogPositionsHelper);
            return constructionPLE;
        } else {
            return null;
        }
    }

    /**
     * Berechnet das minimale KEM-Datum ab sowie das maximale KEM-Datum bis für den übergebenen DIALOG-Stücklisteneintrag
     * über den {@link iPartsDIALOGPositionsHelper} und legt die Ergebnisse in den virtuellen Feldern
     * {@link iPartsDataVirtualFieldsDefinition#DIALOG_DD_CALCULATED_MIN_KEM_DATE_FROM} sowie {@link iPartsDataVirtualFieldsDefinition#DIALOG_DD_CALCULATED_MAX_KEM_DATE_TO}
     * ab für den späteren Zugriff inkl. Cache-Funktion an allen relevanten DIALOG-Positionsvarianten.
     *
     * @param constructionPLE
     * @param dialogPositionsHelper
     */
    public static void calculateMinMaxKEMDates(EtkDataPartListEntry constructionPLE, iPartsDIALOGPositionsHelper dialogPositionsHelper) {
        DBDataObjectAttribute minKemDateFromAttribute = constructionPLE.getAttributes().getField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MIN_KEM_DATE_FROM, false);
        if (minKemDateFromAttribute != null) {
            return; // Virtuelle Felder wurden bereits berechnet
        }

        calculateMinMaxKEMDatesWithoutCache(constructionPLE, dialogPositionsHelper);
    }

    /**
     * Berechnet das minimale KEM-Datum ab sowie das maximale KEM-Datum bis für den übergebenen DIALOG-Stücklisteneintrag
     * über den {@link iPartsDIALOGPositionsHelper} und legt die Ergebnisse in den virtuellen Feldern
     * {@link iPartsDataVirtualFieldsDefinition#DIALOG_DD_CALCULATED_MIN_KEM_DATE_FROM} sowie {@link iPartsDataVirtualFieldsDefinition#DIALOG_DD_CALCULATED_MAX_KEM_DATE_TO}
     * ab für den späteren Zugriff, allerdings ohne Verwendung des Caches, so dass der Rückgabewert immer alle zur KEM-Kette
     * gehörenden Konstruktions-{@link EtkDataPartListEntry}s enthält.
     *
     * @param constructionPLE
     * @param dialogPositionsHelper
     * @return Set mit allen {@link EtkDataPartListEntry}s aus der Konstruktion (inkl. {@code constructionPLE}, die zur
     * KEM-Kette gehören
     */
    public static Set<EtkDataPartListEntry> calculateMinMaxKEMDatesWithoutCache(EtkDataPartListEntry constructionPLE, iPartsDIALOGPositionsHelper dialogPositionsHelper) {
        // Set mit allen relevanten Stücklisteneinträgen der DIALOG-Position
        Set<EtkDataPartListEntry> partListEntriesForKemChain = new HashSet<>();
        partListEntriesForKemChain.add(constructionPLE);

        List<EtkDataPartListEntry> partListEntriesForAllKEMs = dialogPositionsHelper.getPartListEntriesForAllKEMs(constructionPLE, true);
        if ((partListEntriesForAllKEMs == null) || partListEntriesForAllKEMs.isEmpty()) { // Es gibt keine Vorgänger- oder Nachfolgerstände
            constructionPLE.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MIN_KEM_DATE_FROM,
                                                     constructionPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA),
                                                     true, DBActionOrigin.FROM_DB);
            constructionPLE.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MAX_KEM_DATE_TO,
                                                     constructionPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATB),
                                                     true, DBActionOrigin.FROM_DB);
            return partListEntriesForKemChain;
        }

        // KEM-Datum ab/bis für alle relevanten KEM-Stände aufsammeln
        Map<String, EtkDataPartListEntry> kemDateFromMap = new HashMap<>();
        Map<String, EtkDataPartListEntry> kemDateToMap = new HashMap<>();
        for (EtkDataPartListEntry positionVariant : partListEntriesForAllKEMs) {
            if (isRelatedPositionVariantForReplacement(constructionPLE, positionVariant)) {
                kemDateFromMap.put(positionVariant.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA), positionVariant);
                kemDateToMap.put(positionVariant.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATB), positionVariant);
            }
        }

        // KEM-Kette für die Vorgänger aufbauen. Die Einträge in der gerade erstellten Map könnten nicht zusammenhängend sein,
        // weil z.B der Vorgänger die gleiche Coderegel hat, wie ein vorheriger Stand, aber es einen Stand dazwischen gibt,
        // der eine andere hat. Damit wird hier die Kette trotzdem nur bis zu dem Eintrag mit der anderen Coderegel durchlaufen.
        // Es reicht also NICHT für alle Positionsvarianten in den beiden Maps nur das maximale und minimale Datum zu ermitteln.
        String minKemDateFrom;
        EtkDataPartListEntry currentMinKemFromPLE = constructionPLE;
        do {
            minKemDateFrom = currentMinKemFromPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA);
            currentMinKemFromPLE = kemDateToMap.get(minKemDateFrom);
            if ((currentMinKemFromPLE != null) && !partListEntriesForKemChain.add(currentMinKemFromPLE)) {
                break; // Abbruch wegen Endlosschleife (SDATA = SDATB)
            }

        } while (currentMinKemFromPLE != null);

        // Nach gleichem Vorgehen die KEM-Kette für die Nachfolger aufbauen
        String maxKemDateTo;
        EtkDataPartListEntry currentMaxKemToPLE = constructionPLE;
        do {
            maxKemDateTo = currentMaxKemToPLE.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATB);
            if (!maxKemDateTo.isEmpty()) {
                currentMaxKemToPLE = kemDateFromMap.get(maxKemDateTo);
                if ((currentMaxKemToPLE != null) && !partListEntriesForKemChain.add(currentMaxKemToPLE)) {
                    break; // Abbruch wegen Endlosschleife (SDATA = SDATB)
                }
            } else {
                break; // Unendliches SDATB -> es gibt keinen Nachfolgerstand
            }
        } while (currentMaxKemToPLE != null);

        // Minimales KEM-Datum ab und maximales KEM-Datum bis an allen Stücklisteneinträgen der KEM-Kette setzen
        for (EtkDataPartListEntry partListEntry : partListEntriesForKemChain) {
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MIN_KEM_DATE_FROM,
                                                   minKemDateFrom, true, DBActionOrigin.FROM_DB);
            partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATED_MAX_KEM_DATE_TO,
                                                   maxKemDateTo, true, DBActionOrigin.FROM_DB);
        }

        return partListEntriesForKemChain;
    }

    private static boolean isRelatedPositionVariantForReplacement(EtkDataPartListEntry partListEntry, EtkDataPartListEntry positionVariant) {
        String partListEntryMatNr = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_PARTNO);
        String partListEntryEventFrom = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_EVENT_FROM);
        String partListEntryEventTo = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_EVENT_TO);
        String partListEntryHierarchy = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_HIERARCHY);

        String positionVariantMatNr = positionVariant.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_PARTNO);
        String positionVariantEventFrom = positionVariant.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_EVENT_FROM);
        String positionVariantEventTo = positionVariant.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_EVENT_TO);
        String positionVariantHierarchy = positionVariant.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_HIERARCHY);

        // Codes als letztes prüfen, da am aufwändigsten
        return partListEntryMatNr.equals(positionVariantMatNr)
               && partListEntryEventFrom.equals(positionVariantEventFrom)
               && partListEntryEventTo.equals(positionVariantEventTo)
               && partListEntryHierarchy.equals(positionVariantHierarchy)
               && getPartListEntryConstructionCodes(partListEntry).equals(getPartListEntryConstructionCodes(positionVariant));
    }

    private static String getPartListEntryConstructionCodes(EtkDataPartListEntry partListEntry) {
        try {
            String partListEntryCodes = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CODES);

            // Klonen der DNF ist für getStringRepresentation() nicht notwendig
            return DaimlerCodes.getDnfCodeOriginal(partListEntryCodes).getStringRepresentation();
        } catch (BooleanFunctionSyntaxException e) {
            Logger.getLogger().throwRuntimeException(e);
            return ""; // egal, weil durch RuntimeException sowieso unerreichbar -> nur für die Code-Analyse
        }
    }

    /**
     * Erzeugt aus der übergebenen {@link PartListEntryId} für NICHT-DIALOG-Stücklisteneinträge eine Quell-GUID für das
     * Feld {@link #FIELD_K_SOURCE_GUID}.
     *
     * @param partListEntryId
     * @return
     */
    public static String createNonDIALOGSourceGUID(PartListEntryId partListEntryId) {
        return partListEntryId.getKVari() + iPartsDataFootNote.FOOTNOTE_ID_DELIMITER + partListEntryId.getKLfdnr();
    }


    /**
     * Datenklasse für die Referenz zu einem Quell-Stücklisteneintrag (Konstruktionsstückliste).
     */
    public static class SourcePartListEntryRef {

        private String sourceRef1;
        private String sourceRef2;
        private String sourceGUID;
        private Map<String, String> sourceToDestFieldsMapping;

        public SourcePartListEntryRef(iPartsModuleTypes moduleType, EtkDataPartListEntry sourcePartListEntry, boolean isMBS) {
            if (iPartsModuleTypes.isDialogRetailType(moduleType)) {
                sourceRef1 = sourcePartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSE);
                sourceRef2 = sourcePartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSV);
                sourceGUID = sourcePartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
                sourceToDestFieldsMapping = DIALOG_TO_KATALOG_FIELDS_MAPPING;
                return;
            } else if (iPartsModuleTypes.isEdsRetailType(moduleType)) {
                sourceRef2 = "";
                sourceGUID = ""; // DAIMLER-10948: Sowohl bei EDS als auch MBS eine K_SOURCE_GUID analog der aus der ELDAS Migration verwenden
                if (isMBS) {
                    sourceRef1 = "";
                    sourceToDestFieldsMapping = MBS_TO_KATALOG_FIELD_MAPPING;
                    return;
                } else {
                    sourceRef1 = sourcePartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.EDS_SAAGUID);
                    sourceToDestFieldsMapping = EDS_TO_KATALOG_FIELDS_MAPPING;
                    return;
                }
            } else if (iPartsModuleTypes.isPskType(moduleType)) {
                sourceRef1 = "";
                sourceRef2 = "";
                sourceGUID = ""; // DAIMLER-10948: Sowohl bei EDS als auch MBS eine K_SOURCE_GUID analog der aus der ELDAS Migration verwenden
                sourceToDestFieldsMapping = new HashMap<>();
                return;
            }

            Logger.getLogger().throwRuntimeException("Unsupported module type to create a source part list entry context: " + moduleType.name());
        }

        public SourcePartListEntryRef(iPartsModuleTypes moduleType, iPartsDataDialogData sourceDataDialog) {
            if (iPartsModuleTypes.isDialogRetailType(moduleType)) {
                sourceRef1 = sourceDataDialog.getFieldValue(FIELD_DD_POSE);
                sourceRef2 = sourceDataDialog.getFieldValue(FIELD_DD_POSV);
                sourceGUID = sourceDataDialog.getFieldValue(FIELD_DD_GUID);
                sourceToDestFieldsMapping = MIGRATION_DIALOG_TO_KATALOG_FIELDS_MAPPING;
                return;
            }

            Logger.getLogger().throwRuntimeException("Unsupported module type to create a source part list entry context from iPartsDataDialogData: "
                                                     + moduleType.name());
        }

        public String getSourceRef1() {
            return sourceRef1;
        }

        public String getSourceRef2() {
            return sourceRef2;
        }

        public String getSourceGUID() {
            return sourceGUID;
        }

        public Map<String, String> getSourceToDestFieldsMapping() {
            return sourceToDestFieldsMapping;
        }
    }
}