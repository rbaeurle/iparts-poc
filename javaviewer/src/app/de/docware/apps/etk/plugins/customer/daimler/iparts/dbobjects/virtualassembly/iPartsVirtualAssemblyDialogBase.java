/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualassembly;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.project.mechanic.usage.MechanicUsagePosition;
import de.docware.apps.etk.base.search.model.PartsSearchSqlSelect;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.VirtualMaterialType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEntrySourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsSearchVirtualDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.search.iPartsVirtualMaterialSearchDataset;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsAssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsNodeType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditConstructionToRetailHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsPartlistTextHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsReplacementHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.textkinds.iPartsDialogPartlistTextkind;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsColorTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactories;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactoryData;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacementConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.HierarchicalIDWithType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.mapping.MappingHmMSmToEinPas;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.config.db.VirtualFieldDefinition;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.collections.weak.WeakKeysMap;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sort.SortStringCache;
import de.docware.util.sql.SQLStringConvert;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

import static de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition.VIRTFIELD_SPACER;

/**
 * Basisklasse für die virtuelle Sicht der DIALOG-Daten
 */
public abstract class iPartsVirtualAssemblyDialogBase extends iPartsVirtualAssemblyEinPasBase {

    public static final String NO_ACTIVE_AUTHOR_ORDER_NAME = "-";

    private static final String[] DIALOG_SORT_FIELDS = new String[]{ FIELD_DD_SERIES_NO, FIELD_DD_HM, FIELD_DD_M, FIELD_DD_SM,
                                                                     FIELD_DD_POSE, FIELD_DD_POSV, FIELD_DD_WW, FIELD_DD_ETZ,
                                                                     FIELD_DD_AA, FIELD_DD_SDATA };

    public static final EtkDisplayFields NEEDED_DIALOG_DISPLAY_FIELDS = new EtkDisplayFields();

    static {
        // Benötigte Felder aus der MAT-Tabelle
        NEEDED_DIALOG_DISPLAY_FIELDS.addFelder(NEEDED_DISPLAY_FIELDS);
        NEEDED_DIALOG_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_ETKZ), false, false));
        NEEDED_DIALOG_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_VERKSNR), false, false));
        NEEDED_DIALOG_DISPLAY_FIELDS.addFeld(new EtkDisplayField(TableAndFieldName.make(EtkDbConst.TABLE_MAT, iPartsConst.FIELD_M_LAYOUT_FLAG), false, false));
    }

    public static void getMechanicUsageForDialogPartLists(PartId partId, boolean filtered, EtkProject project, List<MechanicUsagePosition> result) {
        DBDataObjectAttributesList dialogAttributesList = project.getDbLayer().getAttributesList(TABLE_DA_DIALOG,
                                                                                                 new String[]{ FIELD_DD_SERIES_NO, FIELD_DD_HM, FIELD_DD_M, FIELD_DD_SM, FIELD_DD_QUANTITY },
                                                                                                 new String[]{ FIELD_DD_PARTNO },
                                                                                                 new String[]{ partId.getMatNr() });
        for (DBDataObjectAttributes dialogAttributes : dialogAttributesList) {
            // HM/M/SM
            iPartsSeriesId seriesId = new iPartsSeriesId(dialogAttributes.getField(FIELD_DD_SERIES_NO).getAsString());
            iPartsVirtualNode virtualRootNode = new iPartsVirtualNode(iPartsNodeType.DIALOG_HMMSM, seriesId);
            HmMSmId hmMSmId = new HmMSmId(seriesId.getSeriesNumber(),
                                          dialogAttributes.getField(FIELD_DD_HM).getAsString(),
                                          dialogAttributes.getField(FIELD_DD_M).getAsString(),
                                          dialogAttributes.getField(FIELD_DD_SM).getAsString());
            iPartsVirtualNode virtualParentNode = new iPartsVirtualNode(iPartsNodeType.HMMSM, hmMSmId);
            EtkDataPartListEntry parentAssembly = iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(null,
                                                                                                                      filtered,
                                                                                                                      project,
                                                                                                                      virtualRootNode,
                                                                                                                      virtualParentNode);
            String quantity = dialogAttributes.getField(FIELD_DD_QUANTITY).getAsString();
            if (parentAssembly != null) {
                result.add(MechanicUsagePosition.createAsPartsEntry(parentAssembly.getAsId().getOwnerAssemblyId(), partId, parentAssembly.getAsId(), quantity));
            }

            // HM/M/SM nach EinPAS
            MappingHmMSmToEinPas mapping = MappingHmMSmToEinPas.getInstance(project, seriesId);
            List<EinPasId> einPasIdList = mapping.get(hmMSmId);

            if ((einPasIdList != null) && !einPasIdList.isEmpty()) {
                virtualRootNode = new iPartsVirtualNode(iPartsNodeType.DIALOG_EINPAS, seriesId);
                for (EinPasId einPasId : einPasIdList) {
                    virtualParentNode = new iPartsVirtualNode(iPartsNodeType.EINPAS, einPasId);
                    parentAssembly = iPartsDataAssembly.addVirtualParentNodesPathToParentAssemblyEntries(null,
                                                                                                         filtered,
                                                                                                         project,
                                                                                                         virtualRootNode,
                                                                                                         virtualParentNode);
                    if (parentAssembly != null) {
                        result.add(MechanicUsagePosition.createAsPartsEntry(parentAssembly.getAsId().getOwnerAssemblyId(), partId, parentAssembly.getAsId(), quantity));
                    }
                }
            }
        }
    }

    /**
     * Sucht alle Stücklisteneinträge mit den entsprechenden Suchwerten in den DIALOG-Stücklisten der Konstruktion, wobei optional
     * eine <i>optionalRootAssemblyId</i> angegeben werden kann, die bei enthaltener Baureihe die Suche erheblich beschleunigt.
     *
     * @param optionalRootAssemblyId
     * @param isSearchValuesDisjunction
     * @param selectFields
     * @param selectValues
     * @param whereFields
     * @param whereValues
     * @param andOrSearch
     * @param project
     * @param multiLanguageCache
     * @param wildCardSettings
     * @return
     */
    public static iPartsSearchVirtualDataset searchPartListEntriesForDialogPartLists(final AssemblyId optionalRootAssemblyId,
                                                                                     final boolean isSearchValuesDisjunction,
                                                                                     final EtkDisplayFields selectFields,
                                                                                     final List<String> selectValues,
                                                                                     final EtkDisplayFields whereFields,
                                                                                     final List<String> whereValues,
                                                                                     final boolean andOrSearch, EtkProject project,
                                                                                     WeakKeysMap<String, String> multiLanguageCache,
                                                                                     WildCardSettings wildCardSettings) {
        return new iPartsVirtualMaterialSearchDataset(TABLE_DA_DIALOG, FIELD_DD_PARTNO, optionalRootAssemblyId, isSearchValuesDisjunction,
                                                      selectFields, selectValues, whereFields, whereValues, andOrSearch,
                                                      project, multiLanguageCache, wildCardSettings) {
            protected int fieldIndexGuid;
            protected int fieldIndexSeriesNo;
            protected int fieldIndexHm;
            protected int fieldIndexM;
            protected int fieldIndexSm;
            protected int fieldIndexPartNumber;

            @Override
            public DBDataSetCancelable createDBDataSet() throws CanceledException {
                // Bestimmung der Baureihennummer anhand einer virtuellen optionalRootAssemblyId
                String seriesNumber = iPartsVirtualNode.getSeriesNumberFromAssemblyId(optionalRootAssemblyId);

                // Baureihe als zusätzliche Bedingung angeben
                if (seriesNumber != null) {
                    whereFields = new EtkDisplayFields(whereFields);
                    whereFields.addFeld(new EtkDisplayField(TABLE_DA_DIALOG, FIELD_DD_SERIES_NO, false, false));
                    whereValues = new ArrayList<>(whereValues);
                    whereValues.add(seriesNumber);
                }
                return super.createDBDataSet();
            }

            @Override
            protected void addNeededJoins(PartsSearchSqlSelect partsSearchSqlSelect, List<String> doNotJoinList) {
                //keine weiteren Joins nötig
            }

            @Override
            protected void addAdditionalSelectFields(EtkDisplayFields selectFieldsWithoutKatalog, List<String> selectValuesWithoutKatalog) {
                // dafür die DIALOG-relevanten Felder in selectFieldsWithoutKatalog hinzufügen
                fieldIndexGuid = selectFieldsWithoutKatalog.size();
                selectFieldsWithoutKatalog.addFeld(new EtkDisplayField(TABLE_DA_DIALOG, FIELD_DD_GUID, false, false));
                selectValuesWithoutKatalog.add("");
                fieldIndexSeriesNo = selectFieldsWithoutKatalog.size();
                selectFieldsWithoutKatalog.addFeld(new EtkDisplayField(TABLE_DA_DIALOG, FIELD_DD_SERIES_NO, false, false));
                selectValuesWithoutKatalog.add("");
                fieldIndexHm = selectFieldsWithoutKatalog.size();
                selectFieldsWithoutKatalog.addFeld(new EtkDisplayField(TABLE_DA_DIALOG, FIELD_DD_HM, false, false));
                selectValuesWithoutKatalog.add("");
                fieldIndexM = selectFieldsWithoutKatalog.size();
                selectFieldsWithoutKatalog.addFeld(new EtkDisplayField(TABLE_DA_DIALOG, FIELD_DD_M, false, false));
                selectValuesWithoutKatalog.add("");
                fieldIndexSm = selectFieldsWithoutKatalog.size();
                selectFieldsWithoutKatalog.addFeld(new EtkDisplayField(TABLE_DA_DIALOG, FIELD_DD_SM, false, false));
                selectValuesWithoutKatalog.add("");
                fieldIndexPartNumber = selectFieldsWithoutKatalog.size();
                selectFieldsWithoutKatalog.addFeld(new EtkDisplayField(TABLE_DA_DIALOG, FIELD_DD_PARTNO, false, false));
                selectValuesWithoutKatalog.add("");
            }

            @Override
            protected String getPartNumber(List<String> values) {
                return values.get(fieldIndexPartNumber);
            }

            @Override
            protected HierarchicalIDWithType createParentId(List<String> values) {
                return new HmMSmId(values.get(fieldIndexSeriesNo), values.get(fieldIndexHm), values.get(fieldIndexM), values.get(fieldIndexSm));
            }

            @Override
            protected List<EtkDataPartListEntry> searchResultPartListEntries(List<String> values, String partNumber, HierarchicalIDWithType parentId) {
                List<EtkDataPartListEntry> resultPartListEntries = new ArrayList<>();
                iPartsSeriesId seriesId = new iPartsSeriesId(values.get(fieldIndexSeriesNo));
                HmMSmId hmMSmId = (HmMSmId)parentId;

                // HM/M/SM-Treffer nur dann hinzufügen, wenn nicht innerhalb von DIALOG_EINPAS gesucht wird
                if ((virtualRootNode == null) || (virtualRootNode.getType() != iPartsNodeType.DIALOG_EINPAS)) {
                    iPartsVirtualNode virtualRootNode = new iPartsVirtualNode(iPartsNodeType.DIALOG_HMMSM, seriesId);
                    iPartsVirtualNode structureNode = new iPartsVirtualNode(iPartsNodeType.HMMSM, hmMSmId);
                    AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(virtualRootNode, structureNode), "");
                    createAndAddSearchResultPartListEntry(assemblyId, values.get(fieldIndexGuid), partNumber, resultPartListEntries);
                }

                // EinPAS-Treffer nur dann hinzufügen, wenn nicht innerhalb von DIALOG_HMMSM gesucht wird und das
                // EinPAS Mapping aktiv
                Boolean showEinPASMapping = Session.get().getAttribute(iPartsPlugin.SESSION_KEY_SHOW_EINPAS_MAPPING, false);
                if (showEinPASMapping && ((virtualRootNode == null) || (virtualRootNode.getType() != iPartsNodeType.DIALOG_HMMSM))) {
                    List<EinPasId> mappingToEinPAS = MappingHmMSmToEinPas.getInstance(project, seriesId).get(hmMSmId);
                    if ((mappingToEinPAS != null) && !mappingToEinPAS.isEmpty()) {
                        iPartsVirtualNode virtualRootNode = new iPartsVirtualNode(iPartsNodeType.DIALOG_EINPAS, seriesId);
                        for (EinPasId einPasId : mappingToEinPAS) {
                            iPartsVirtualNode structureNode = new iPartsVirtualNode(iPartsNodeType.EINPAS, einPasId);
                            AssemblyId assemblyId = new iPartsAssemblyId(iPartsVirtualNode.getVirtualIdString(virtualRootNode, structureNode), "");
                            createAndAddSearchResultPartListEntry(assemblyId, values.get(fieldIndexGuid), partNumber, resultPartListEntries);
                        }
                    }
                }

                return resultPartListEntries;
            }
        };
    }

    /**
     * Überprüft, ob der übergebene Berechnungzeitpunkt für einen HM/M/SM-Knoten anhand der maximalen Gültigkeitsdauer aus
     * dem Admin-Modus noch gültig ist.
     *
     * @param calculationDateString
     * @return
     */
    public static boolean isHmMSmNodeCalculationValid(String calculationDateString) {
        Calendar calculationDate = SQLStringConvert.ppDateTimeStringToCalendar(calculationDateString);
        if (calculationDate == null) {
            return false;
        }

        long maxValidityInMillis = 60 * 1000 * iPartsPlugin.getPluginConfig().getConfigValueAsInteger(iPartsPlugin.CONFIG_MAX_VALIDITY_REPORT_CONST_NODE_CALCULATIONS);
        return Calendar.getInstance().getTimeInMillis() - calculationDate.getTimeInMillis() <= maxValidityInMillis;
    }

    public iPartsVirtualAssemblyDialogBase(EtkProject project, List<iPartsVirtualNode> virtualIds, iPartsAssemblyId assemblyId) {
        super(project, virtualIds, assemblyId);
    }

    protected class DialogPartsListResult {

        DBDataObjectAttributes dialogAttributes = new DBDataObjectAttributes();
        DBDataObjectAttributes matAttributes = new DBDataObjectAttributes();
    }

    protected String[] getDialogSortFields() {
        return DIALOG_SORT_FIELDS;
    }

    private void sortDialogResults(List<DialogPartsListResult> result) {
        final String[] compareFields = getDialogSortFields();
        final SortStringCache cache = new SortStringCache();

        Collections.sort(result, new Comparator<DialogPartsListResult>() {
            @Override
            public int compare(DialogPartsListResult o1, DialogPartsListResult o2) {
                for (String fieldName : compareFields) {
                    String s1 = o1.dialogAttributes.getField(fieldName).getAsString();
                    String s2 = o2.dialogAttributes.getField(fieldName).getAsString();

                    s1 = cache.getSortString(s1, true);
                    s2 = cache.getSortString(s2, true);
                    int result = s1.compareTo(s2);
                    if (result != 0) {
                        return result;
                    }
                }
                return 0;
            }
        });
    }


    private class FactoryDataForPartList {

        String factoryId;
        String dateFrom;
        String dateTo;
        iPartsFactoryDataTypes type;

        String getValueForFieldName(String fieldName) {
            if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_ID)) {
                return factoryId;
            }

            if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_FIRST_USE)) {
                return dateFrom;
            }

            if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_FIRST_USE_TO)) {
                return dateTo;
            }

            return null;
        }

        public iPartsFactoryDataTypes getType() {
            return type;
        }
    }

    /**
     * @param completeStructure
     * @param rootNode
     * @param subNodeId
     * @param subAssembliesOnly
     * @param fields            Felder die benötigt werden; Felder die in nachgelagerten Verarbeitungen benötigt werden, würden normal nachgeladen wenn sie nicht
     *                          in fields enthalten sind. Für virtuelle Felder ist das aber (zumindest bisher) nicht möglich. Daher muss man hier im Hinterkopf haben,
     *                          welche Felder nachgelagert benötigt werden und diese dann bestimmen auch wenn sie nicht in fields stehen. Im Zweifel sollte man auch
     *                          einfach virtuelle Felder, deren Bestimmung nichts kostet, mitbestimmen.
     * @return
     */
    protected DBDataObjectList<EtkDataPartListEntry> loadVirtualDialogHmMSm(iPartsCatalogNode completeStructure, iPartsVirtualNode rootNode,
                                                                            HmMSmId subNodeId, boolean subAssembliesOnly, EtkDisplayFields fields) {
        iPartsCatalogNode nodeParent;
        if (subNodeId != null) {
            nodeParent = completeStructure.getNode(subNodeId);
        } else {
            // Kein subknoten -> nehme den obersten
            nodeParent = completeStructure;
        }

        if (nodeParent == null) {
            // Stückliste nicht mehr da, evtl. von jemandem gelöscht worden
            return new EtkDataPartListEntryList();
        }


        //Jetzt die Childs an diese Baugruppe anfügen
        DBDataObjectList<EtkDataPartListEntry> result = new EtkDataPartListEntryList();

        int lfdNumber = 0;
        if ((nodeParent.getId() instanceof HmMSmId) && ((HmMSmId)nodeParent.getId()).isSmNode()) {
            HmMSmId parentId = (HmMSmId)nodeParent.getId();

            // Wir sind auf dem untersten Knoten, zeige dieses Submodul als Stückliste an
            List<DialogPartsListResult> records = getDialogPartsList(parentId, subAssembliesOnly);
            if (records.isEmpty()) { // Tritt explizit bei SM-Knoten mit subAssembliesOnly = true auf
                return result;
            }

            // Sammeln aller unveränderten Werkseinsatzdaten und gruppieren nach BCTE Schlüssel und Werk
            iPartsDataFactoryDataList factoryDataList = iPartsDataFactoryDataList.loadFactoryDataForSubModule(getEtkProject(), parentId, true);

            // Erster Einsatz im Werk -> Werkseinsatzdaten (berechnete Felder für die Stückliste)
            Map<iPartsDialogBCTEPrimaryKey, FactoryDataForPartList> firstUseInProduction = null;
            Map<String, iPartsDataGenInstallLocation> genInstallLocationMap = null;
            if (fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_FIRST_USE, false)
                || fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_FIRST_USE_TO, false)
                || fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_ID, false)) {  // virt. Feld nur hinzufügen wenn benötigt

                // Lade zu diesem Submodul alle Werkseinsatzdaten, ermittele zu jeder Dialogvariante den aktuellen Datensatz pro Werk (getListWithoutHistoryData)
                // und über alle Werke den frühesten PEM-Einsatztermin. Dieser Datensatz wird in einer MAP zum Primärschlüssel der Dialogdaten gespeichert
                List<iPartsDataFactoryData> filteredList = factoryDataList.getListWithoutHistoryData(true);

                firstUseInProduction = new HashMap<>();

                iPartsFactories factories = iPartsFactories.getInstance(getEtkProject());

                for (iPartsDataFactoryData factoryData : filteredList) {

                    String testFactoryId = factoryData.getFieldValue(FIELD_DFD_FACTORY);
                    // Nicht AS-relevante Werke sollen bei der Ermittlung des frühesten Einsatztermins in Konstruktionssicht ignoriert werden
                    if (factories.isValidForFilter(testFactoryId)) {

                        iPartsDialogBCTEPrimaryKey dialogPrimaryKey = factoryData.getAsId().getBCTEPrimaryKey();
                        if (dialogPrimaryKey != null) {
                            FactoryDataForPartList data = firstUseInProduction.get(dialogPrimaryKey);
                            String testDateFrom = factoryData.getFieldValue(FIELD_DFD_PEMTA);
                            iPartsFactoryDataTypes type = iPartsFactoryDataTypes.getTypeByDBValue(factoryData.getFieldValue(FIELD_DFD_DATA_ID));

                            if ((data == null) || (data.dateFrom == null) || checkEarliestDateAndLowestFactoryNumber(data, testDateFrom, testFactoryId)) {
                                // Noch kein vorhandenes Datum gefunden oder das gefundene ist kleiner bzw. die Werksnummer ist kleiner
                                data = new FactoryDataForPartList();
                                data.dateFrom = testDateFrom;
                                data.dateTo = factoryData.getFieldValue(FIELD_DFD_PEMTB);
                                data.factoryId = testFactoryId;
                                data.type = type;
                                firstUseInProduction.put(dialogPrimaryKey, data);
                            }
                        }
                    }
                }
            }
            if (fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_GENVO, false) ||
                fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_SPLITSIGN, false) ||
                fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.K_GENVO_TEXT, false)) {
                if (genInstallLocationMap == null) {
                    genInstallLocationMap = iPartsDataGenInstallLocationList.loadAllReleasedDataForHmMSmIdAsMap(getEtkProject(), parentId);
                }
            }

            // Wenn keine Records vorhanden sind, muss nicht extra nach Werksdaten und Farbtabellen gesucht werden
            if (!records.isEmpty()) {
                iPartsVirtualCalcFieldDocuRel.clearBadCodes();
                // Werkseinsatzdaten für Konstruktion laden
                Map<iPartsDialogBCTEPrimaryKey, iPartsFactoryData> factoryDataWithHistory = loadFactoryDataForConstruction(factoryDataList);
                // Interne Texte für Konstruktion laden
                Map<iPartsDialogBCTEPrimaryKey, List<iPartsDataInternalText>> internalTextMap = loadInteralTextForConstruction(parentId);
                // Variantentabellen für Konstruktion laden
                Map<PartId, iPartsColorTable> colorTableDataWithHistory = loadColorTableDataForConstruction(parentId);
                // BCTX Stücklistentexte für die aktuelle Stückliste laden
                Map<String, Map<IdWithType, iPartsPartlistTextHelper.PartListTexts>> partListTexts
                        = iPartsPartlistTextHelper.getAllTextsForAllTextkinds(getEtkProject(), parentId);

                // BCTG Generic Parts nur laden, wenn die Spalten in der Anzeige der Stückliste konfiguriert sind
                Map<iPartsDialogBCTEPrimaryKey, List<iPartsDataGenericPart>> genericPartMap = null;
                if (fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_BCTG_GENERIC_PARTNO, false)
                    || fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_BCTG_SOLUTION, false)
                    || fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_BCTG_VARIANTNO, false)) {
                    iPartsDataGenericPartList genericPartList = iPartsDataGenericPartList.loadGenericPartDataForHmMSmId(getEtkProject(),
                                                                                                                        parentId);

                    // Map von BCTE-Schlüssel ohne SDATA auf Liste von iPartsDataGenericParts aufbauen
                    genericPartMap = new HashMap<>();
                    for (iPartsDataGenericPart dataGenericPart : genericPartList) {
                        iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(dataGenericPart.getAsId().getGuid());
                        if (bcteKey != null) {
                            iPartsDialogBCTEPrimaryKey bcteKeyWithoutSDatA = bcteKey.getPositionBCTEPrimaryKeyWithoutSDA();
                            List<iPartsDataGenericPart> genericPartListForKey = genericPartMap.computeIfAbsent(bcteKeyWithoutSDatA,
                                                                                                               key -> new ArrayList<>());
                            genericPartListForKey.add(dataGenericPart);
                        }
                    }
                }

                String currentPosition = null;
                List<EtkMultiSprache> cachedPosVariantTextsForEntry = new DwList<>();
                for (DialogPartsListResult rec : records) {
                    // neueste Positionsnummer und Positionsvariante
                    String position = rec.dialogAttributes.getFieldValue(FIELD_DD_POSE);
                    String positionVariant = rec.dialogAttributes.getFieldValue(FIELD_DD_POSV);
                    // Falls Positionstexte existieren, müssen diese vor der Position auftauchen
                    if (!partListTexts.isEmpty()) {
                        boolean otherPosition = checkReplaceCurrentValue(currentPosition, position);
                        // Falls sich die Positionsnummer geändert hat, nach Positionstexten (BCTX_PV = blank oder POS Tabelle) suchen
                        if (otherPosition) {
                            currentPosition = position;
                            lfdNumber = addPartlistTextEntry(result, lfdNumber, partListTexts, position, positionVariant,
                                                             true, cachedPosVariantTextsForEntry);
                        }
                    }

                    // Hier wird die eigentliche Stücklistenposition gebaut
                    lfdNumber++;
                    EtkDataPartListEntry newEntry = createDialogEntry(lfdNumber, rec, null, firstUseInProduction,
                                                                      factoryDataWithHistory, internalTextMap, colorTableDataWithHistory,
                                                                      genericPartMap, genInstallLocationMap);
                    if (newEntry != null) {
                        result.add(newEntry, DBActionOrigin.FROM_DB);
                    }

                    // Falls Positionsvariantentexte existieren, müssen diese nach der Stücklistenposition auftauchen
                    // Hier wird nicht mehr überprüft, ob sich die Position oder die Positionsvariante geändert haben,
                    // weil jede Varianten samt SDATA und SDATB separat geprüft werden muss (Gültigkeitscheck)
                    if (!partListTexts.isEmpty()) {
                        lfdNumber = addPartlistTextEntryWithValidityChecks(result, rec, lfdNumber, partListTexts, position,
                                                                           positionVariant, false, cachedPosVariantTextsForEntry);
                    }
                    addPartlistTextForSingleEntry(newEntry, cachedPosVariantTextsForEntry);
                    // Weil bei jeder Positionsvariante geprüft werden muss, ob ein Text existiert, werden hier die Texte
                    // zur Variante gelöscht.
                    cachedPosVariantTextsForEntry.clear();
                }
                loadAllReplacementMats(parentId, result);
            }
        } else {
            // Wir sind noch in der HM oder M Ebene -> Zeige die Unterstrukturknoten an
            for (iPartsCatalogNode nodeChild : nodeParent.getChildren()) {
                if (nodeChild.getId() instanceof HmMSmId) {
                    HmMSmId hmMSmId = (HmMSmId)nodeChild.getId();
                    lfdNumber++;
                    // Die Untergeordneten Strukturknoten anzeigen
                    EtkDataPartListEntry entry = createVirtualNode(lfdNumber, rootNode, new iPartsVirtualNode(iPartsNodeType.HMMSM, hmMSmId));
                    if (entry != null) {
                        DBDataObjectAttributes attributes = entry.getAttributes();
                        fillHmMSmAttributes(attributes, hmMSmId, true);
                        result.add(entry, DBActionOrigin.FROM_DB);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Lädt zum übergebenen HM/M/SM-Knoten die erste Autorenauftragsbenennung zu offenene Stücklistenpositionen, die in
     * den jeweiligen Autorenaufträgen übernommen wurden.
     *
     * @param parentId
     * @return Map von DIALOG-GUID auf die erste Autorenauftragsbenennung
     */
    private Map<String, AOInfoForBCTEKey> loadAuthorOrderNames(HmMSmId parentId) {
        String activeChangeSetGuid = getEtkProject().getActiveChangeSetGuidAsDbValue();
        final Map<String, AOInfoForBCTEKey> result = new HashMap<>();

        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean createDataObjects() {
                return false;
            }

            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String guid = attributes.getFieldValue(FIELD_DD_GUID);

                // Der erste Treffer genügt, danach nur noch ";..."
                String aoName = attributes.getFieldValue(FIELD_DAO_NAME);
                if (StrUtils.isValid(guid, aoName)) {
                    String changeSetGuid = attributes.getFieldValue(FIELD_DCE_GUID);
                    AOInfoForBCTEKey existingAOInfo = result.get(guid);
                    if (existingAOInfo == null) {
                        existingAOInfo = new AOInfoForBCTEKey();
                        result.put(guid, existingAOInfo);
                    }
                    boolean textExists = existingAOInfo.isTextValid();
                    boolean isActiveChangeSet = changeSetGuid.equals(activeChangeSetGuid);
                    existingAOInfo.setAOAffiliation(isActiveChangeSet);
                    if (textExists && !isActiveChangeSet) { // Beim aktiven ChangeSet müssen alle Treffer geprüft werden
                        existingAOInfo.extendExistingText();
                    } else {
                        // Beim aktiven ChangeSet prüfen, ob der Status != gelöscht ist (bei anderen ChangeSets wäre dies
                        // für die Performance ziemlich ungünstig)
                        if (isActiveChangeSet) {
                            // Passende ChangeSetEntries in dem aktiven ChangeSet für die ID des Stücklisteneintrags suchen
                            String[] plePkValues = IdWithType.fromDBString(PartListEntryId.TYPE, attributes.getFieldValue(FIELD_DCE_DO_ID)).toStringArrayWithoutType();
                            Collection<SerializedDBDataObject> serializedDBDataObjects = getEtkProject().getRevisionsHelper().getSerializedObjectsByPKValuesAndState(TABLE_KATALOG,
                                                                                                                                                                     plePkValues, null);
                            if (serializedDBDataObjects != null) {
                                boolean stateIsDeleted = true;
                                for (SerializedDBDataObject serializedDBDataObject : serializedDBDataObjects) {
                                    if (serializedDBDataObject.getType().equals(PartListEntryId.TYPE) && (serializedDBDataObject.getState() != SerializedDBDataObjectState.DELETED)) {
                                        stateIsDeleted = false;
                                        break;
                                    }
                                }
                                if (stateIsDeleted) {
                                    return false;
                                }
                            }
                        }
                        if (textExists) {
                            existingAOInfo.extendExistingText();
                        } else {
                            // Den Autorenauftrag des aktuell aktiven ChangeSet nicht anzeigen
                            if (!isActiveChangeSet) {
                                existingAOInfo.setText(aoName);
                            }
                        }
                    }
                }
                return false;
            }
        };

        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_DIALOG, FIELD_DD_GUID, false, false)); // BCTE Schlüssel
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_NAME, false, false)); // Autorenauftrag
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_GUID, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_DO_ID, false, false));

        iPartsDataDialogDataList dataDialogDataList = new iPartsDataDialogDataList();
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_SERIES_NO),
                                                     TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_HM),
                                                     TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_M),
                                                     TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_SM),
                                                     TableAndFieldName.make(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_STATUS) };
        String[] whereValues = new String[]{ parentId.getSeries(), parentId.getHm(), parentId.getM(), parentId.getSm(),
                                             iPartsDataDialogDataList.getNotWhereValue(iPartsAuthorOrderStatus.APPROVED.getDBValue()) };
        String[] sortFields = new String[]{ TableAndFieldName.make(TABLE_DA_AUTHOR_ORDER, FIELD_DAO_NAME), TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_GUID) };
        // Hoin DA_DIALOG auf DA_CHANGE_SET_ENTRY auf DA_AUTHOR_ORDER
        dataDialogDataList.searchSortAndFillWithJoin(getEtkProject(), getEtkProject().getDBLanguage(), selectFields,
                                                     whereTableAndFields, whereValues, false,
                                                     sortFields, true, null, false,
                                                     false, true, foundAttributesCallback, false,
                                                     new EtkDataObjectList.JoinData(TABLE_DA_CHANGE_SET_ENTRY,
                                                                                    new String[]{ FIELD_DD_GUID },
                                                                                    new String[]{ FIELD_DCE_DO_SOURCE_GUID },
                                                                                    false, false),
                                                     new EtkDataObjectList.JoinData(TABLE_DA_AUTHOR_ORDER,
                                                                                    new String[]{ TableAndFieldName.make(TABLE_DA_CHANGE_SET_ENTRY, FIELD_DCE_GUID) },
                                                                                    new String[]{ FIELD_DAO_CHANGE_SET_ID },
                                                                                    false, false));

        return result;
    }

    /**
     * Hilfsklasse um die Zugehörigkeit eines Autorenauftrags zu setzen
     */
    private static class AOInfoForBCTEKey {

        private String text;
        private AOAffiliationForDIALOGEntry aoAffiliation = AOAffiliationForDIALOGEntry.NONE;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public boolean isTextValid() {
            return text != null;
        }

        public void extendExistingText() {
            if (isTextValid() && !text.endsWith("; ...")) {
                text += "; ...";
            }
        }

        public void setAOAffiliation(boolean isActiveChangeSet) {
            switch (aoAffiliation) {
                case NONE:
                    if (isActiveChangeSet) {
                        aoAffiliation = AOAffiliationForDIALOGEntry.ONLY_IN_ACTIVE_ORDER;
                    } else {
                        aoAffiliation = AOAffiliationForDIALOGEntry.IN_OTHER_AUTHOR_ORDER;
                    }
                    break;
                case ONLY_IN_ACTIVE_ORDER:
                    if (!isActiveChangeSet) {
                        aoAffiliation = AOAffiliationForDIALOGEntry.IN_ACTIVE_AND_OTHER_AUTHOR_ORDER;
                    }
                    break;
                case IN_OTHER_AUTHOR_ORDER:
                    if (isActiveChangeSet) {
                        aoAffiliation = AOAffiliationForDIALOGEntry.IN_ACTIVE_AND_OTHER_AUTHOR_ORDER;
                    }
                    break;
            }
        }

        public AOAffiliationForDIALOGEntry getAoAffiliation() {
            return aoAffiliation;
        }
    }

    /**
     * Befüllt die für den HM/M/SM-Knoten notwendigen Attribute (optonal auch mit Laden der Berechnungen für die Auswertung
     * für Teilepositionen).
     *
     * @param attributes
     * @param hmMSmId
     * @param loadReportCalculations
     */
    protected void fillHmMSmAttributes(DBDataObjectAttributes attributes, HmMSmId hmMSmId, boolean loadReportCalculations) {
        attributes.addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SERIES_NO, hmMSmId.getSeries(), true, DBActionOrigin.FROM_DB);
        attributes.addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_HM, hmMSmId.getHm(), true, DBActionOrigin.FROM_DB);
        attributes.addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_M, hmMSmId.getM(), true, DBActionOrigin.FROM_DB);
        attributes.addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SM, hmMSmId.getSm(), true, DBActionOrigin.FROM_DB);

        // Werte als (leere) Strings initialisieren und auslesen, damit sie den Attributen auch als leere Strings hinzugefügt
        // werden können
        String openEntriesString = "";
        String changedEntriesString = "";
        String calculationDateString = "";

        // Ist der HM/M/SM-Knoten ausgeblendet oder nicht berechnungsrelevant?
        boolean isHiddenOrNoCalc = false;
        HmMSmNode hmMSmNode = HmMSm.getInstance(getEtkProject(), new iPartsSeriesId(hmMSmId.getSeries())).getNode(hmMSmId);
        if (hmMSmNode != null) {
            if (hmMSmNode.isHiddenRecursively()) {
                isHiddenOrNoCalc = true;
                openEntriesString = TranslationHandler.translate("!!ausgeblendet");
                changedEntriesString = openEntriesString;
            } else if (hmMSmNode.isNoCalcRecursively()) {
                isHiddenOrNoCalc = true;
                openEntriesString = TranslationHandler.translate("!!nicht berechnen");
                changedEntriesString = openEntriesString;
            }
        }

        if (loadReportCalculations) {
            // Überprüfen, ob gerade eine Berechnung für diese Baureihe stattfindet
            String lockReportConstNodeCalculationDate = null;
            iPartsReportConstNodeId lockReportConstNodeId = new iPartsReportConstNodeId(hmMSmId.getSeries(), "", "");
            iPartsDataReportConstNode lockDataReportConstNode = new iPartsDataReportConstNode(getEtkProject(),
                                                                                              lockReportConstNodeId);
            if (lockDataReportConstNode.existsInDB()) {
                lockReportConstNodeCalculationDate = lockDataReportConstNode.getFieldValue(iPartsConst.FIELD_DRCN_CALCULATION_DATE);
                if (isHmMSmNodeCalculationValid(lockReportConstNodeCalculationDate)) {
                    if (!isHiddenOrNoCalc) {
                        openEntriesString = TranslationHandler.translate("!!Berechnung läuft...");
                        changedEntriesString = openEntriesString;
                    }
                } else { // Berechnung ist nicht mehr gültig (vermutlich abgestürzt) -> Datensatz entfernen
                    lockReportConstNodeCalculationDate = null;
                    lockDataReportConstNode.deleteFromDB();
                }
            }

            // Virtuelle Felder für die Auswertung von DIALOG-Stücklisten laden bzw. initial leer besetzen
            iPartsDataReportConstNode dataReportConstNode = new iPartsDataReportConstNode(getEtkProject(), new iPartsReportConstNodeId(hmMSmId.getSeries(),
                                                                                                                                       hmMSmId.toDBString(),
                                                                                                                                       getProjectForReportForConstructionNodesCalculations()));

            if (dataReportConstNode.existsInDB()) {
                calculationDateString = dataReportConstNode.getFieldValue(iPartsConst.FIELD_DRCN_CALCULATION_DATE);

                // Gültigkeit der Berechnung überprüfen
                if (!calculationDateString.isEmpty()) {
                    if (!isHiddenOrNoCalc && isHmMSmNodeCalculationValid(calculationDateString)) {
                        // Falls gerade eine Berechnung für diese Baureihe stattfindet, die erst nach dem Berechnungszeitpunkt
                        // gestartet wurde, dann sind die berechneten Werte nicht mehr gültig
                        if ((lockReportConstNodeCalculationDate != null) && (lockReportConstNodeCalculationDate.compareTo(calculationDateString) > 0)) {
                            calculationDateString = "";
                        } else {
                            // Berechnungen sind noch gültig
                            openEntriesString = dataReportConstNode.getFieldValue(iPartsConst.FIELD_DRCN_OPEN_ENTRIES);
                            changedEntriesString = dataReportConstNode.getFieldValue(iPartsConst.FIELD_DRCN_CHANGED_ENTRIES);
                        }
                    } else {
                        // Berechnungen sind nicht mehr gültig -> calculationDateString leeren und Daten in der DB löschen
                        calculationDateString = "";

                        // Ungültigen Datensatz nur dann aus der DB löschen, wenn nicht gerade eine Berechnung für diese
                        // Baureihe stattfindet; ansonsten könnte es aufgrund von Race Conditions zum Löschen von gerade
                        // berechneten Daten kommen
                        if (lockReportConstNodeCalculationDate == null) {
                            dataReportConstNode.deleteFromDB();
                        }
                    }
                }
            }
        }

        attributes.addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_OPEN_ENTRIES, openEntriesString,
                            true, DBActionOrigin.FROM_DB);
        attributes.addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CHANGED_ENTRIES, changedEntriesString,
                            true, DBActionOrigin.FROM_DB);
        attributes.addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATION_DATE, calculationDateString,
                            true, DBActionOrigin.FROM_DB);
    }

    @Override
    public synchronized void afterLoadPartlist(boolean subAssembliesOnly, EtkDisplayFields fields, DBDataObjectList<EtkDataPartListEntry> partlist,
                                               boolean loadAdditionalData) {
        super.afterLoadPartlist(subAssembliesOnly, fields, partlist, loadAdditionalData);

        // Baureihe bzw. HM/M/SM-Knoten bestimmen
        HmMSmId hmMSmId = null;
        iPartsSeriesId seriesId = iPartsVirtualNode.getSeriesIdForAssemblyId(getAsId());
        EtkProject etkProject = getEtkProject();
        if (seriesId == null) {
            HmMSmNode hmMSmNode = iPartsVirtualNode.getHmMSmNodeForAssemblyId(getAsId(), etkProject);
            if (hmMSmNode != null) {
                hmMSmId = hmMSmNode.getId();
            }
        }

        if (!subAssembliesOnly && loadAdditionalData) {
            if (seriesId == null) {
                if (hmMSmId == null) { // Weder Baureihe noch HM/M/SM-Knoten gefunden für diese Stückliste
                    return;
                } else if (hmMSmId.isSmNode()) { // SM-Knoten sind die DIALOG-Konstruktions-Stücklisten
                    // DIALOG-Änderungen für die gesamte Stückliste laden für teilweisen BCTE-Schlüssel und Materialnummern
                    // und in Maps ablegen mit dem BCTE-Schlüssel bzw. der Materialnummern als Schlüssel

                    // Liste der DIALOG-Änderungen und Map für den BCTE-Schlüssel
                    iPartsDataDIALOGChangeList dataDIALOGChangeListBCTEKey = new iPartsDataDIALOGChangeList();
                    String partialBcteKey = hmMSmId.toString(iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER)
                                            + iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER + "*";
                    seriesId = new iPartsSeriesId(hmMSmId.getSeries());
                    dataDIALOGChangeListBCTEKey.loadDIALOGChangesForPartialBCTEKeyWithSeries(partialBcteKey, seriesId, getEtkProject());
                    Map<String, iPartsDataDIALOGChangeList> dataDIALOGChangeListBCTEKeyMap = new HashMap<>();
                    for (iPartsDataDIALOGChange dataDIALOGChange : dataDIALOGChangeListBCTEKey) {
                        String bcteKey = dataDIALOGChange.getFieldValue(FIELD_DDC_BCTE);
                        iPartsDataDIALOGChangeList dataDIALOGChangeListForBCTEKey = dataDIALOGChangeListBCTEKeyMap.get(bcteKey);
                        if (dataDIALOGChangeListForBCTEKey == null) {
                            dataDIALOGChangeListForBCTEKey = new iPartsDataDIALOGChangeList();
                            dataDIALOGChangeListBCTEKeyMap.put(bcteKey, dataDIALOGChangeListForBCTEKey);
                        }
                        dataDIALOGChangeListForBCTEKey.add(dataDIALOGChange, DBActionOrigin.FROM_DB);
                    }

                    // Liste der DIALOG-Änderungen und Map für die Materialnummer
                    iPartsDataDIALOGChangeList dataDIALOGChangeListMat = new iPartsDataDIALOGChangeList();
                    dataDIALOGChangeListMat.loadDIALOGChangesForMatWithSeries(seriesId, getEtkProject());
                    Map<String, iPartsDataDIALOGChangeList> dataDIALOGChangeListMatMap = new HashMap<>();
                    for (iPartsDataDIALOGChange dataDIALOGChange : dataDIALOGChangeListMat) {
                        String matNr = dataDIALOGChange.getFieldValue(FIELD_DDC_MATNR);
                        iPartsDataDIALOGChangeList dataDIALOGChangeListForMat = dataDIALOGChangeListMatMap.get(matNr);
                        if (dataDIALOGChangeListForMat == null) {
                            dataDIALOGChangeListForMat = new iPartsDataDIALOGChangeList();
                            dataDIALOGChangeListMatMap.put(matNr, dataDIALOGChangeListForMat);
                        }
                        dataDIALOGChangeListForMat.add(dataDIALOGChange, DBActionOrigin.FROM_DB);
                    }

                    // BCTE Schlüssel auf Autorenauftrag-Benennung, falls notwendig
                    Map<String, AOInfoForBCTEKey> dialogGUIDToAuthorOrderName = null;
                    if (fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DD_AUTHOR_ORDER, false)) {
                        dialogGUIDToAuthorOrderName = loadAuthorOrderNames(hmMSmId);
                    }

                    // Retail-Stücklisteneinträge für Source-Context BR&HM&M&SM bestimmen
                    String sourceContext = EditConstructionToRetailHelper.createSourceContext(iPartsEntrySourceType.DIALOG, hmMSmId);
                    Set<String> retailDialogGUIDs = EditConstructionToRetailHelper.getRetailSourceGUIDs(iPartsEntrySourceType.DIALOG,
                                                                                                        sourceContext, null,
                                                                                                        false, getEtkProject());

                    // DIALOG-Änderungen über den BCTE-Schlüssel und die Materialnummer an den Stücklisteneinträgen setzen
                    boolean calculateFailLocation = fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_FAIL_LOCATION, false)
                                                    || fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DA_FAIL_LOCATION, false);

                    // Berechnung vom Feld "Ohne Verwendung"
                    boolean calculateWithoutUsage = etkProject.getEtkDbs().isRevisionChangeSetActive()
                                                    && fields.contains(TABLE_KATALOG, iPartsDataVirtualFieldsDefinition.DIALOG_DD_WITHOUT_USAGE, false);
                    Set<String> deletedDialogGUIDsInRetail = null;
                    if (calculateWithoutUsage) {
                        deletedDialogGUIDsInRetail = getDeletedDialogGUIDsInRetailForActiveChangeSet(hmMSmId, etkProject);
                    }

                    for (EtkDataPartListEntry partListEntry : partlist) {
                        if (partListEntry instanceof iPartsDataPartListEntry) {
                            String dialogGUID = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID);
                            iPartsDataDIALOGChangeList dataDIALOGChangeListForBCTEKey = dataDIALOGChangeListBCTEKeyMap.get(dialogGUID);
                            iPartsDataDIALOGChangeList dataDIALOGChangeListForMat = dataDIALOGChangeListMatMap.get(partListEntry.getFieldValue(FIELD_K_MATNR));
                            iPartsDataPartListEntry iPartsPartListEntry = (iPartsDataPartListEntry)partListEntry;
                            iPartsPartListEntry.setDIALOGChangesAttributes(dataDIALOGChangeListForBCTEKey,
                                                                           dataDIALOGChangeListForMat);

                            // Im Retail übernommen?
                            boolean assigned = false;
                            String assignedValue;
                            if (retailDialogGUIDs.contains(dialogGUID)) {
                                assigned = true;
                                assignedValue = RETAIL_ASSIGNED;
                            } else {
                                assignedValue = RETAIL_NOT_ASSIGNED;
                            }
                            iPartsPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE,
                                                                         assignedValue, true, DBActionOrigin.FROM_DB);

                            // Berechnete virtuelle Felder müssen auch gelöscht werden, damit sie neu berechnet werden,
                            // da diese DIALOG_DD_RETAIL_USE verwenden
                            iPartsPartListEntry.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_DOCU_RELEVANT,
                                                                            false, DBActionOrigin.FROM_DB);
                            iPartsPartListEntry.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_AS_RELEVANT,
                                                                            false, DBActionOrigin.FROM_DB);
                            iPartsPartListEntry.getAttributes().deleteField(iPartsDataVirtualFieldsDefinition.DD_CALCULATED_BUSINESS_CASE,
                                                                            false, DBActionOrigin.FROM_DB);

                            // Autorenaufträge zu übernommenen Konstruktionsstücklisten hinzufügen
                            if ((dialogGUIDToAuthorOrderName != null) && !dialogGUIDToAuthorOrderName.isEmpty()) {
                                AOInfoForBCTEKey authorOrderInfo = dialogGUIDToAuthorOrderName.get(dialogGUID);
                                if (authorOrderInfo != null) {
                                    String authorOrderName = authorOrderInfo.getText();
                                    if (StrUtils.isValid(authorOrderName)) {
                                        // Virtuelles Feld wurde bereits in addAdditionalVirtualFields() mit leerem Autoren-Auftrag hinzugefügt
                                        partListEntry.setFieldValue(iPartsDataVirtualFieldsDefinition.DD_AUTHOR_ORDER, authorOrderName,
                                                                    DBActionOrigin.FROM_DB);
                                    }
                                    AOAffiliationForDIALOGEntry aoAffiliation = authorOrderInfo.getAoAffiliation();
                                    if (aoAffiliation != AOAffiliationForDIALOGEntry.NONE) {
                                        // Virtuelles Feld wurde bereits in addAdditionalVirtualFields() mit leerem Autoren-Auftrag hinzugefügt
                                        partListEntry.setFieldValue(iPartsDataVirtualFieldsDefinition.DD_AUTHOR_ORDER_AFFILIATION, aoAffiliation.getTextValue(),
                                                                    DBActionOrigin.FROM_DB);
                                    }
                                }
                            }

                            // Fehlerorte berechnen.
                            if (calculateFailLocation) {
                                iPartsPartListEntry.calculateOriginalFailLocation();

                                // Berechnete Fehlerorte auch als vererbte Fehlerorte setzen (Vererbung gibt es bei
                                // DIALOG-Konstruktions-Stücklisten nicht)
                                iPartsPartListEntry.calculateInheritedFailLocation(false, null);
                            }

                            // Ohne Verwendung
                            boolean withoutUsage = false;
                            if (calculateWithoutUsage) {
                                // Wenn ein DIALOG-Stücklisteneintrag keine Retail-Verwendung mehr hat und im aktiven ChangeSet
                                // ein Retail-Stücklisteneintrag mit passender DIALOG-GUID gelöscht wurde, dann ist der
                                // DIALOG-Stücklisteneintrag "ohne Verwendung"
                                if (!assigned && deletedDialogGUIDsInRetail.contains(dialogGUID)) {
                                    withoutUsage = true;
                                }
                            }
                            iPartsPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_WITHOUT_USAGE,
                                                                         SQLStringConvert.booleanToPPString(withoutUsage),
                                                                         true, DBActionOrigin.FROM_DB);
                        }
                    }

                    return; // Hier gibt es keine Berechnungen für die Auswertung von Teilepositionen -> rausspringen
                }
            }

            // Überprüfen der Berechnungen nicht für SM-Knonten machen, weil diese ja direkt die Konstruktions-Stückliste darstellen
            if ((hmMSmId != null) && hmMSmId.isSmNode()) {
                return;
            }

            // Überprüfen, ob gerade eine Berechnung für diese Baureihe stattfindet
            String lockReportConstNodeCalculationDate = null;
            if ((seriesId != null) || (hmMSmId != null)) {
                String seriesNumber = (seriesId != null) ? seriesId.getSeriesNumber() : hmMSmId.getSeries();
                iPartsReportConstNodeId lockReportConstNodeId = new iPartsReportConstNodeId(seriesNumber, "", "");
                iPartsDataReportConstNode lockDataReportConstNode = new iPartsDataReportConstNode(getEtkProject(), lockReportConstNodeId);
                if (lockDataReportConstNode.existsInDB()) {
                    lockReportConstNodeCalculationDate = lockDataReportConstNode.getFieldValue(iPartsConst.FIELD_DRCN_CALCULATION_DATE);
                }
            }

            // Bei allen Stücklisteneinträgen den Berechnungszeitpunkt überprüfen
            boolean removeAssemblyFromCache = false;
            EtkProject projectForReportForConstructionNodesCalculations = getProjectForReportForConstructionNodesCalculations();
            for (EtkDataPartListEntry partListEntry : partlist) {
                String openAndChangedEntriesString = "";
                boolean isHiddenOrNoCalc = false;
                HmMSmNode subHmMSmNode = iPartsVirtualNode.getHmMSmNodeForAssemblyId(partListEntry.getDestinationAssemblyId(), etkProject);
                // Ist der HM/M/SM-Knoten ausgeblendet oder nicht berechnungsrelevant?
                if (subHmMSmNode != null) {
                    if (subHmMSmNode.isHiddenRecursively()) {
                        isHiddenOrNoCalc = true;
                        openAndChangedEntriesString = TranslationHandler.translate("!!ausgeblendet");
                    } else if (subHmMSmNode.isNoCalcRecursively()) {
                        isHiddenOrNoCalc = true;
                        openAndChangedEntriesString = TranslationHandler.translate("!!nicht berechnen");
                    }
                }

                String calculationDateString = partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATION_DATE);

                // Gültigkeit der Berechnung für die Auswertung von Teilepositionen überprüfen
                boolean calculationDateIsValid = false;
                if (!calculationDateString.isEmpty()) {
                    if (isHiddenOrNoCalc || !isHmMSmNodeCalculationValid(calculationDateString)) {
                        // Berechnungen sind nicht mehr gültig -> veraltete Daten aus der DB löschen
                        // Ungültigen Datensatz nur dann aus der DB löschen, wenn nicht gerade eine Berechnung für diese
                        // Baureihe stattfindet; ansonsten könnte es aufgrund von Race Conditions zum Löschen von gerade
                        // berechneten Daten kommen
                        if (lockReportConstNodeCalculationDate == null) {
                            removeAssemblyFromCache = true;
                            if (subHmMSmNode != null) {
                                HmMSmId subHmMSmId = subHmMSmNode.getId();
                                iPartsDataReportConstNode dataReportConstNode = new iPartsDataReportConstNode(etkProject, new iPartsReportConstNodeId(subHmMSmId.getSeries(),
                                                                                                                                                      subHmMSmId.toDBString(),
                                                                                                                                                      projectForReportForConstructionNodesCalculations));
                                dataReportConstNode.deleteFromDB(true);
                            }
                        }
                    } else {
                        // Falls gerade eine Berechnung für diese Baureihe stattfindet, die erst nach dem Berechnungszeitpunkt
                        // gestartet wurde, dann sind die berechneten Werte nicht mehr gültig
                        calculationDateIsValid = !((lockReportConstNodeCalculationDate != null) && (lockReportConstNodeCalculationDate.compareTo(calculationDateString) > 0));
                    }
                }

                if (!calculationDateIsValid) {
                    // Virtuelle Feldwerte anpassen
                    if (!isHiddenOrNoCalc && (lockReportConstNodeCalculationDate != null)) {
                        openAndChangedEntriesString = TranslationHandler.translate("!!Berechnung läuft...");
                    }
                    partListEntry.setFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_OPEN_ENTRIES, openAndChangedEntriesString,
                                                DBActionOrigin.FROM_DB);
                    partListEntry.setFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CHANGED_ENTRIES, openAndChangedEntriesString,
                                                DBActionOrigin.FROM_DB);
                    partListEntry.setFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_CALCULATION_DATE, "", DBActionOrigin.FROM_DB);
                }
            }

            // Die Stückliste muss aus dem Cache entfernt werden, weil mindestens ein Berechnungszeitpunkt eines Stücklisteneintrags
            // nicht mehr gültig ist
            if (removeAssemblyFromCache) {
                EtkDataAssembly.removeDataAssemblyFromCache(etkProject, getAsId());
            }
        }
    }

    public static Set<String> getDeletedDialogGUIDsInRetailForActiveChangeSet(HmMSmId hmMSmId, EtkProject etkProject) {
        Set<String> deletedDialogGUIDsInRetail = new HashSet<>();

        // Gelöschte Stücklisteneinträge suchen und deren DIALOG-GUIDs (sofern zu dieser DIALOG-Stückliste
        // passend) merken
        String dialogGUIDPrefix = hmMSmId.toString(iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER);
        Collection<SerializedDBDataObject> serializedPLEs = etkProject.getRevisionsHelper().getMergedSerializedObjectsForTable(TABLE_KATALOG);
        if (serializedPLEs != null) {
            for (SerializedDBDataObject serializedPLE : serializedPLEs) {
                if (serializedPLE.getState() == SerializedDBDataObjectState.DELETED) {
                    String deletedDialogGUID = serializedPLE.getAttributeValue(FIELD_K_SOURCE_GUID, true, etkProject);
                    if (deletedDialogGUID.startsWith(dialogGUIDPrefix)) {
                        deletedDialogGUIDsInRetail.add(deletedDialogGUID);
                    }
                }
            }
        }
        return deletedDialogGUIDsInRetail;
    }

    /**
     * Lädt alle Ersetzungen am Teilestamm für die aktuelle Konstruktionsstückliste
     *
     * @param hmMSmId
     * @param partListEntries
     */
    public void loadAllReplacementMats(HmMSmId hmMSmId, DBDataObjectList<EtkDataPartListEntry> partListEntries) {
        Map<String, List<iPartsDataPartListEntry>> partNrToPartListEntryMap = iPartsReplacementHelper.createPartNoToPartlistEntryMap(partListEntries);

        iPartsDataReplaceConstMatList dataReplacementsVTNV = iPartsDataReplaceConstMatList.loadAllReplacementsForHmMSm(getEtkProject(), hmMSmId);
        iPartsDataReplaceConstPartList dataReplacementsTS7 = iPartsDataReplaceConstPartList.loadAllReplacementsForHmMSm(getEtkProject(), hmMSmId);

        for (iPartsDataReplaceConstMat dataReplacement : dataReplacementsVTNV) {
            String predecessorPartNr = dataReplacement.getFieldValue(FIELD_DRCM_PRE_PART_NO);
            List<iPartsDataPartListEntry> predecessors = partNrToPartListEntryMap.get(predecessorPartNr);
            if ((predecessors != null) && !predecessors.isEmpty()) {
                // Es muss mindestens einen passenden Nachfolger geben wegen iPartsDataReplaceConstMatList.loadAllReplacementsForHmMSm()
                String successorPartNr = dataReplacement.getFieldValue(FIELD_DRCM_PART_NO);
                List<iPartsDataPartListEntry> successors = partNrToPartListEntryMap.get(successorPartNr);
                addReplacementsToPartListEntries(dataReplacement, true, predecessors, successors);
            }
        }

        for (iPartsDataReplaceConstPart dataReplacement : dataReplacementsTS7) {
            String predecessorPartNr = dataReplacement.getFieldValue(FIELD_DRCP_PRE_MATNR);
            List<iPartsDataPartListEntry> predecessors = partNrToPartListEntryMap.get(predecessorPartNr);
            if ((predecessors != null) && !predecessors.isEmpty()) {
                // Es muss mindestens einen passenden Nachfolger geben wegen iPartsDataReplaceConstPartList.loadAllReplacementsForHmMSm()
                String successorPartNr = dataReplacement.getFieldValue(FIELD_DRCP_PART_NO);
                List<iPartsDataPartListEntry> successors = partNrToPartListEntryMap.get(successorPartNr);
                addReplacementsToPartListEntries(dataReplacement, false, predecessors, successors);
            }
        }
    }

    private void addReplacementsToPartListEntries(EtkDataObject dataReplacement, boolean isReplaceConstMat, Collection<iPartsDataPartListEntry> potentialPredecessors,
                                                  Collection<iPartsDataPartListEntry> potentialSuccessors) {
        List<iPartsReplacementConst> constReplacementsForPotentialPLEs = iPartsReplacementHelper.getConstReplacementsForPotentialPLEs(dataReplacement,
                                                                                                                                      isReplaceConstMat,
                                                                                                                                      potentialPredecessors,
                                                                                                                                      potentialSuccessors);
        for (iPartsReplacementConst replacement : constReplacementsForPotentialPLEs) {
            replacement.predecessorEntry.addSuccessorConst(replacement);
            replacement.successorEntry.addPredecessorConst(replacement);
        }
    }

    /**
     * Hängt die verknüpften Stücklistentexte an die erzeugte Stücklistenposition an
     *
     * @param partListEntry
     * @param cachedPosVariantTextsForEntry
     */
    private void addPartlistTextForSingleEntry(EtkDataPartListEntry partListEntry, List<EtkMultiSprache> cachedPosVariantTextsForEntry) {
        if (partListEntry == null) {
            return;
        }
        EtkMultiSprache result = new EtkMultiSprache();
        // Wenn Texte vorhanden sind, baue einen Text mit allen vorhandenen Texten
        if ((cachedPosVariantTextsForEntry != null) && !cachedPosVariantTextsForEntry.isEmpty()) {
            for (EtkMultiSprache multiLangText : cachedPosVariantTextsForEntry) {
                Map<String, String> allLanguagesAndText = multiLangText.getLanguagesAndTexts();
                for (Map.Entry<String, String> entry : allLanguagesAndText.entrySet()) {
                    if (StrUtils.isValid(result.getText(entry.getKey()))) {
                        String currentText = result.getText(entry.getKey());
                        String textFromPartlist = entry.getValue();
                        String newText = currentText + "\n" + textFromPartlist;
                        result.setText(entry.getKey(), newText);
                    } else {
                        result.setText(entry.getKey(), entry.getValue());

                    }
                }
            }
        }
        partListEntry.getAttributes().getField(iPartsDataVirtualFieldsDefinition.DD_PARTLIST_TEXT).setValueAsMultiLanguage(result, DBActionOrigin.FROM_DB);
    }

    /**
     * Check, ob ein neuer Wert gleich einem aktuellen Wert ist
     *
     * @param currentValue
     * @param newValue
     * @return
     */
    private boolean checkReplaceCurrentValue(String currentValue, String newValue) {
        return (StrUtils.isEmpty(currentValue)) && StrUtils.isValid(newValue) ||
               (StrUtils.isValid(currentValue, newValue) && !currentValue.equals(newValue));
    }


    private int addPartlistTextEntry(DBDataObjectList<EtkDataPartListEntry> result, int lfdNumber,
                                     Map<String, Map<IdWithType, iPartsPartlistTextHelper.PartListTexts>> partListTexts,
                                     String position, String positionVariant, boolean isPositionText,
                                     List<EtkMultiSprache> cachedPosVariantTextsForEntry) {
        return addPartlistTextEntryWithValidityChecks(result, null, lfdNumber, partListTexts, position, positionVariant,
                                                      isPositionText, cachedPosVariantTextsForEntry);

    }

    /**
     * Fügt der übergebenen Stücklistenliste eine Stücklistenposition mit einem Stücklistentext hinzu. Als Ergebnis wird
     * die aktuelle laufende Nummer zurückgeliefert.
     *
     * @param result
     * @param rec
     * @param lfdNumber
     * @param partListTexts
     * @param position
     * @param positionVariant
     * @param isPositionText  @return
     */
    private int addPartlistTextEntryWithValidityChecks(DBDataObjectList<EtkDataPartListEntry> result, DialogPartsListResult rec, int lfdNumber,
                                                       Map<String, Map<IdWithType, iPartsPartlistTextHelper.PartListTexts>> partListTexts,
                                                       String position, String positionVariant, boolean isPositionText,
                                                       List<EtkMultiSprache> cachedPosVariantTextsForEntry) {
        // Schlüssel auf POSE und POSV
        String key = iPartsPartlistTextHelper.createPosAndPVKey(position, isPositionText ? "" : positionVariant);
        // Texte zur Position und/oder Positionsvariante
        Map<IdWithType, iPartsPartlistTextHelper.PartListTexts> partListTextsWithId = partListTexts.get(key);
        if (partListTextsWithId != null) {
            for (iPartsPartlistTextHelper.PartListTexts textsForPartList : partListTextsWithId.values()) {
                if ((textsForPartList != null) && !textsForPartList.getTexts().isEmpty()) {
                    // Positions-und Positionsvariantentexte werden ja nach ihrem "abstrakten" Schlüssel gruppiert
                    // Alle Texte innerhalb dieser Gruppierung haben bereits die gleichen Werte für:
                    // BR, HM, M, SM, POSE, POSV, WW und ETZ
                    // Deshalb kann hier die Positionsvariantenfilterung, die sich auf WW und ETZ bezieht, auf der
                    // kompletten Gruppe angewandt werden. Positionstexte sind von dieser Filterung ausgenommen.
                    if (isPositionText || checkPartListTextGroupValidity(rec, textsForPartList)) {
                        // Durchlaufe jeden einzelnen Text
                        for (iPartsPartlistTextHelper.PartListText partListText : textsForPartList.getTexts().values()) {
                            boolean creatText = isPositionText || checkSinglePartListTextValidity(rec, partListText);
                            if (creatText) {
                                lfdNumber++;
                                result.add(createDialogPartlistTextEntry(lfdNumber, partListText, position, positionVariant,
                                                                         textsForPartList.getWW(), textsForPartList.getETZ(),
                                                                         isPositionText), DBActionOrigin.FROM_DB);
                                if (!isPositionText && (cachedPosVariantTextsForEntry != null)) {
                                    EtkMultiSprache textWithPrefix = partListText.getTextWithPrefix();
                                    if (!textWithPrefix.isEmpty()) {
                                        cachedPosVariantTextsForEntry.add(textWithPrefix);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return lfdNumber;
    }

    /**
     * Überprüft, ob die übergebene Text-Gruppe zu den übergebenen Daten der Stücklistenposition passt. Eine Text-Gruppe
     * besteht aus Texten, die die gleichen Werte für BR, HM, M, SM, POSE, POSV, WW und ETZ besitzen.
     * Regeln zum Filtern:
     * 1. BR, HM, M, SM, POSE, POSV, WW, ETZ von Teileposition und Text müssen übereinstimmen
     * - BR, HM, M, SM stimmen schon überein, weil beim Bestimmen der Texte die DB mit diesen Werten abgefragt wird.
     * - POSE und POSV stimmen schon überein, weil die Texte in den Schritten davor schon nach POSE und POSV sortiet wurden
     * -> Hier wird also explizit nur nach WW und ETZ gefiltert
     *
     * @param rec
     * @param partListTexts
     * @return
     */
    private boolean checkPartListTextGroupValidity(DialogPartsListResult rec, iPartsPartlistTextHelper.PartListTexts partListTexts) {
        if ((rec != null) && (partListTexts != null)) {
            return iPartsPartlistTextHelper.checkSameETZAndWWValues(partListTexts.getETZ(), partListTexts.getWW(),
                                                                    rec.dialogAttributes.getFieldValue(FIELD_DD_ETZ),
                                                                    rec.dialogAttributes.getFieldValue(FIELD_DD_WW));
        }
        return false;
    }

    /**
     * Überprüft, ob der übergebene Text zu den übergebenen Daten der Stücklistenposition passt.
     * Regeln:
     * 1. Die Ausführungsart der Teileposition muss in der AA-Matrix des Stücklistentextes enthalten sein
     * 2. SDATA von Text < SDATB von Stücklistenposition und SDATB von Stücklistenposition <= SDATB von Text
     *
     * @param rec
     * @param partListText
     * @return
     */
    private boolean checkSinglePartListTextValidity(DialogPartsListResult rec, iPartsPartlistTextHelper.PartListText partListText) {
        if ((rec != null) && (partListText != null)) {
            // Check, Ausführungsart der Stücklistenposition passt zur Ausführungsart des Stücklistentextes
            boolean result = partListText.hasAAValue(rec.dialogAttributes.getFieldValue(FIELD_DD_AA));
            // Check, ob der Stücklistentext zu den Datumsangaben der Stücklistenposition passt
            if (result) {
                result = iPartsPartlistTextHelper.checkPOSVAndEntryDateValues(rec.dialogAttributes.getFieldValue(FIELD_DD_SDATB),
                                                                              partListText.getSdata(), partListText.getSdatb());
            }
            return result;
        }
        return false;
    }

    /**
     * Erzeugt eine Stücklistenposition mit einem Stücklistentext
     *
     * @param lfdNumber
     * @param positionText
     * @param position
     * @param poistionVariant
     * @param ww
     * @param etz
     * @param isPosText       @return
     */
    private EtkDataPartListEntry createDialogPartlistTextEntry(int lfdNumber, iPartsPartlistTextHelper.PartListText positionText,
                                                               String position, String poistionVariant, String ww, String etz, boolean isPosText) {
        // Katalogattribute erzeugen und alle Felder mit Leer auffüllen
        DBDataObjectAttributes katAttributes = iPartsVirtualAssemblyHelper.createEmptyAttributesForTable(getEtkProject(), TABLE_KATALOG);

        katAttributes.addField(FIELD_K_VARI, getAsId().getKVari(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VER, getAsId().getKVer(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SACH, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VER, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MATNR, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MVER, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_LFDNR, positionText.getOriginalTextId().toString("|"), DBActionOrigin.FROM_DB); // Die ID des Textes als laufende Nummer
        katAttributes.addField(FIELD_K_SEQNR, EtkDbsHelper.formatLfdNr(lfdNumber), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_BESTFLAG, SQLStringConvert.booleanToPPString(false), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VIRTUAL_MAT_TYPE, isPosText ? VirtualMaterialType.TEXT_HEADING.getDbValue() : VirtualMaterialType.TEXT_SUB_HEADING.getDbValue(), DBActionOrigin.FROM_DB);

        for (VirtualFieldDefinition mapping : iPartsDataVirtualFieldsDefinition.getMapping(TABLE_DA_DIALOG, TABLE_KATALOG)) {
            String fieldName = mapping.getVirtualFieldName();
            String value = "";
            // virtuelle Felder DIALOG_DD_POSE und DIALOG_DD_POSV werden übergeben und müssen fest gesetzt werden
            // Außerdem müssen WW, ETZ, Reifegrad, SDATA und SDATB gesetzt werden
            // Todo: Sobald wir einen KEM-Stamm haben müssen hier abhängig von SDATA und SDATB die zugehörigen KEMs gesetzt werden
            // Todo: (1:1 Beziehung zwischen KEM und KEM-DATUM)
            if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSE)) {
                value = position;
            } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_POSV)) {
                value = isPosText ? "" : poistionVariant;
            } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_WW)) {
                value = ww;
            } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_ETZ)) {
                value = etz;
            } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RFG)) {
                value = positionText.getMaturity();
            } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATA)) {
                value = positionText.getSdata();
            } else if (fieldName.equals(iPartsDataVirtualFieldsDefinition.DIALOG_DD_SDATB)) {
                value = positionText.getSdatb();
            }
            if (value == null) {
                value = "";
            }
            katAttributes.addField(fieldName, value, true, DBActionOrigin.FROM_DB);
        }

        // Bisher werden zu den Stücklistentexten keine Infos aus DA_DIALOG_ADD_DATA benötigt. Daher werden die virtuellen
        // Felder alle leer angelegt. Sollte sich das ändern, muss hier die Unterscheidung und Bestimmung der Werte
        // geschehen (siehe virtuelle Felder DA_DIALOG)
        for (VirtualFieldDefinition mapping : iPartsDataVirtualFieldsDefinition.getMapping(TABLE_DA_DIALOG_ADD_DATA, TABLE_KATALOG)) {
            String fieldName = mapping.getVirtualFieldName();
            if (fieldName != null) {
                katAttributes.addField(fieldName, "", true, DBActionOrigin.FROM_DB);
            }
        }

        // Restliche rein virtuelle DIALOG-Felder leer besetzen, damit diese nicht unnötig nachträglich berechnet oder gar
        // nachgeladen werden müssen, was bei Stücklistentexten sowieso zu keinem Ergebnis führen würde
        String virtualDIALOGFieldPrefix = VirtualFieldsUtils.VIRTUAL_FIELD_MARKER_PREFIX + iPartsDataVirtualFieldsDefinition.DIALOG
                                          + VIRTFIELD_SPACER;
        for (VirtualFieldDefinition mapping : iPartsDataVirtualFieldsDefinition.getMapping(null, TABLE_KATALOG)) {
            String fieldName = mapping.getVirtualFieldName();
            if ((fieldName != null) && fieldName.startsWith(virtualDIALOGFieldPrefix)) {
                katAttributes.addField(fieldName, "", true, DBActionOrigin.FROM_DB);
            }
        }

        addAditionalVirtualFields(katAttributes);

        EtkDataPartListEntry newPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getEtkProject(), katAttributes);
        newPartListEntry.setFieldValue(iPartsDataVirtualFieldsDefinition.DD_PARTLIST_TEXTKIND, positionText.getTextKind().getTxtKindShort(), DBActionOrigin.FROM_DB);
        // Den eigentlichen Text setzen
        EtkDataPart partForPartListEntry = newPartListEntry.getPart();
        partForPartListEntry.setAttributes(iPartsVirtualAssemblyHelper.createEmptyAttributesForTable(getEtkProject(), TABLE_MAT), DBActionOrigin.FROM_DB);

        // M_TEXTNR muss mit einem leeren EtkMultiSprache befüllt werden, damit dieses Feld nicht sinnlos aus der DB nachgeladen wird
        partForPartListEntry.setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, new EtkMultiSprache(), DBActionOrigin.FROM_DB);

        partForPartListEntry.setFieldValueAsMultiLanguage(FIELD_M_CONST_DESC, positionText.getTextWithPrefix(), DBActionOrigin.FROM_DB);
        addAASetOfEnumColumn(newPartListEntry, positionText.getAAMatrixAsString());

        if (newPartListEntry instanceof iPartsDataPartListEntry) {
            ((iPartsDataPartListEntry)newPartListEntry).setDataLoadedFlagsForVirtualPLE(true);
        }
        return newPartListEntry;
    }

    private void addAASetOfEnumColumn(EtkDataPartListEntry newPartListEntry, String aaMatrixAsString) {
        if (StrUtils.isValid(aaMatrixAsString)) {
            List<String> aaValues = StrUtils.toStringList(aaMatrixAsString, " ", false);
            if (!aaValues.isEmpty()) {
                newPartListEntry.setFieldValueAsSetOfEnum(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA_SOE, aaValues, DBActionOrigin.FROM_DB);
            }
        }
    }

    /**
     * Lädt alle internen Texte für das Submodul.
     *
     * @param parentId
     * @return
     */
    private Map<iPartsDialogBCTEPrimaryKey, List<iPartsDataInternalText>> loadInteralTextForConstruction(HmMSmId parentId) {
        return iPartsDataInternalTextList.loadInternalTextsForAssembly(getEtkProject(), parentId);
    }

    /**
     * Lädt alle Farbtabellen für das Submodul und weißt sie den vorkommenden Materialien zu.
     *
     * @param parentId
     * @return
     */
    private Map<PartId, iPartsColorTable> loadColorTableDataForConstruction(final HmMSmId parentId) {

        final Map<PartId, iPartsColorTable> result = new HashMap<>();

        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                String matNr = attributes.getFieldValue(FIELD_DCTP_PART);
                String colorTableId = attributes.getFieldValue(FIELD_DCTP_TABLE_ID);
                String pos = attributes.getFieldValue(FIELD_DCTP_POS);
                String sdata = attributes.getFieldValue(FIELD_DCTP_SDATA);

                PartId partId = new PartId(matNr, "");
                iPartsColorTable colorTableDataForPartListEntry = result.get(partId);
                if (colorTableDataForPartListEntry == null) {
                    colorTableDataForPartListEntry = new iPartsColorTable();
                    result.put(partId, colorTableDataForPartListEntry);
                }

                iPartsColorTable.ColorTable colorTable = colorTableDataForPartListEntry.getColorTable(colorTableId);
                if (colorTable == null) {
                    colorTable = new iPartsColorTable.ColorTable();
                    colorTable.colorTableId = new iPartsColorTableDataId(colorTableId);
                    colorTableDataForPartListEntry.addColorTable(colorTable);
                }

                iPartsColorTable.ColorTableToPart colorTableToPart = new iPartsColorTable.ColorTableToPart();
                colorTableToPart.colorTableId = new iPartsColorTableToPartId(colorTableId, pos, sdata);
                colorTable.addColorTableToPart(colorTableToPart);
                return false;
            }
        };


        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_DIALOG, FIELD_DD_AA, false, false)); // Farbvariantentabelle

        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_TABLE_ID, false, false)); // Farbvariantentabelle
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_POS, false, false)); // notwendig für die ID
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_SDATA, false, false)); // notwendig für die ID
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_COLORTABLE_PART, FIELD_DCTP_PART, false, false)); // notwendig für die ID

        iPartsDataColorTableToPartList colorTablesForModule = new iPartsDataColorTableToPartList();
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_SERIES_NO),
                                                     TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_HM),
                                                     TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_M),
                                                     TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_SM) };
        String[] whereValues = new String[]{ parentId.getSeries(), parentId.getHm(), parentId.getM(), parentId.getSm() };
        colorTablesForModule.searchSortAndFillWithJoin(getEtkProject(), getEtkProject().getDBLanguage(), selectFields,
                                                       whereTableAndFields, whereValues, false,
                                                       null, // sortFields in Kombination im FoundAttributesCallback sind wirkungslos
                                                       false, foundAttributesCallback,
                                                       new EtkDataObjectList.JoinData(TABLE_DA_DIALOG,
                                                                                      new String[]{ FIELD_DCTP_PART },
                                                                                      new String[]{ FIELD_DD_PARTNO },
                                                                                      false, false));

        return result;
    }

    /**
     * Lädt alle Werkseinsatzdaten zu allen Stücklistenpositionen im aktuellen Submodul und liefert eine Map
     * mit der Verknüpfung BCTE Schlüssel zu Werkseinsatzdaten
     *
     * @param factoryDataList
     * @return
     */
    public static Map<iPartsDialogBCTEPrimaryKey, iPartsFactoryData> loadFactoryDataForConstruction(iPartsDataFactoryDataList factoryDataList) {
        Map<iPartsDialogBCTEPrimaryKey, iPartsFactoryData> factoryDataWithHistory = new HashMap<>();
        // Werkseinsatzdaten durchgehen und nach BCTE Schlüssel und Werk sortieren
        for (iPartsDataFactoryData factoryData : factoryDataList) {
            // eindeutigen BCTE Schlüssel bestimmen
            iPartsDialogBCTEPrimaryKey dialogPrimaryKey = factoryData.getAsId().getBCTEPrimaryKey();
            if (dialogPrimaryKey == null) {
                continue;
            }
            iPartsFactoryData factoryDataForDialogDataset = factoryDataWithHistory.get(dialogPrimaryKey);
            // Gibt es zu dem Schlüssel schon Werksdaten? Falls nein, anlegen.
            if (factoryDataForDialogDataset == null) {
                factoryDataForDialogDataset = new iPartsFactoryData();
                factoryDataWithHistory.put(dialogPrimaryKey, factoryDataForDialogDataset);
            }
            // Unterscheidung nach Werk
            String factory = factoryData.getFieldValue(FIELD_DFD_FACTORY);
            List<iPartsFactoryData.DataForFactory> dataForFactories = factoryDataForDialogDataset.getDataForFactory(factory);
            if (dataForFactories == null) {
                dataForFactories = new DwList<>();
                factoryDataForDialogDataset.setDataForFactory(factory, dataForFactories);
            }
            // Die eigentlichen Werksdaten füllen
            iPartsFactoryData.DataForFactory data = new iPartsFactoryData.DataForFactory();
            data.dateFrom = iPartsFactoryData.getFactoryDateFromDateString(factoryData.getFieldValue(FIELD_DFD_PEMTA),
                                                                           TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMTA));
            data.dateTo = iPartsFactoryData.getFactoryDateFromDateString(factoryData.getFieldValue(FIELD_DFD_PEMTB),
                                                                         TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_PEMTB));
            data.factoryDataId = factoryData.getAsId();
            data.seriesNumber = factoryData.getFieldValue(FIELD_DFD_SERIES_NO);
            data.adat = iPartsFactoryData.getFactoryDateFromDateString(factoryData.getAsId().getAdat(),
                                                                       TableAndFieldName.make(TABLE_DA_FACTORY_DATA, FIELD_DFD_ADAT));
            data.pemFrom = factoryData.getFieldValue(FIELD_DFD_PEMA);
            data.pemTo = factoryData.getFieldValue(FIELD_DFD_PEMB);
            data.releaseState = factoryData.getReleaseState();
            data.stCodeFrom = factoryData.getFieldValue(FIELD_DFD_STCA);
            data.stCodeTo = factoryData.getFieldValue(FIELD_DFD_STCB);

            dataForFactories.add(data);
        }
        return factoryDataWithHistory;
    }

    /**
     * Überprüft, ob das neue Datum vor dem aktuellen Datum liegt bzw. bei identischem Datum, ob die neue Werksnummer kleiner
     * ist als die bisherige.
     *
     * @param data
     * @param testDateFrom
     * @param testFactoryId
     * @return
     */
    private boolean checkEarliestDateAndLowestFactoryNumber(FactoryDataForPartList data, String testDateFrom, String testFactoryId) {
        int dateCompare = data.dateFrom.compareTo(testDateFrom);
        return (dateCompare > 0) || ((dateCompare == 0) && (data.factoryId.compareTo(testFactoryId) > 0));
    }

    private List<DialogPartsListResult> getDialogPartsList(HmMSmId hmMSmId, boolean subAssembliesOnly) {
        List<DialogPartsListResult> result = new ArrayList<>();

        if (subAssembliesOnly) {
            // es sind nur die Unterbaugruppen gesucht, per Definition gibt es unterhalb eines Submoduls keine Unterbaugruppen mehr
            // -> ist immer leer
            return result;

        } else {
            // Alle Felder aus DA_DIALOG (außer STAMP) zu den selectFields hinzufügen
            iPartsDataDialogDataList dialogDataList = new iPartsDataDialogDataList();
            EtkDisplayFields selectFields = getEtkProject().getAllDisplayFieldsForTable(TABLE_DA_DIALOG);
            int stampIndex = selectFields.getIndexOfFeld(TABLE_DA_DIALOG, DBConst.FIELD_STAMP, false);
            if (stampIndex >= 0) {
                selectFields.removeField(stampIndex);
            }

            // Alle in der DWK konfigurierten Felder (auch unsichtbare) aus der Tabelle MAT zu den selectFields hinzufügen
            Set<String> matFieldNames = new HashSet<>();
            String partsListType = getPartsListType();
            EtkEbenenDaten ebene = getEtkProject().getConfig().getPartsDescription().getEbene(partsListType);
            for (EtkDisplayField field : ebene.getFields()) {
                if (field.getKey().getTableName().equals(TABLE_MAT)) {
                    matFieldNames.add(field.getKey().getFieldName());
                    selectFields.addFeld(field);
                }
            }

            // Unbedingt benötigte Felder hinzufügen falls nicht schon vorhanden
            for (EtkDisplayField neededDisplayField : NEEDED_DIALOG_DISPLAY_FIELDS.getFields()) {
                if (selectFields.addFeldIfNotExists(neededDisplayField) && neededDisplayField.getKey().getTableName().equals(TABLE_MAT)) {
                    matFieldNames.add(neededDisplayField.getKey().getFieldName());
                }
            }

            Map<String, iPartsDataDialogAddData> additionalData = getAdditionalData(hmMSmId); // AS Zusatzdaten aus DA_DIALOG_ADD_DATA

            EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
                @Override
                public boolean foundAttributes(DBDataObjectAttributes attributes) {
                    DialogPartsListResult newRow = new DialogPartsListResult();

                    // Attribute aufsplitten in die Attribute für DA_DIALOG und MAT
                    for (DBDataObjectAttribute attribute : attributes.values()) {
                        if (matFieldNames.contains(attribute.getName())) {
                            newRow.matAttributes.addField(attribute, DBActionOrigin.FROM_DB);
                        } else {
                            newRow.dialogAttributes.addField(attribute, DBActionOrigin.FROM_DB);
                        }
                    }

                    // DA_DIALOG Einträge können AS Zusatzdaten haben (DA_DIALOG_ADD_DATA), d.h. wenn Daten vorhanden sind,
                    // dann müssen diese den Attributen hinzugefügt werden
                    String guid = newRow.dialogAttributes.getFieldValue(FIELD_DD_GUID);

                    // Zusatzdaten sind AA unabhängig
                    iPartsDialogBCTEPrimaryKey bcteKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(guid);
                    bcteKey.aa = "";
                    guid = bcteKey.createDialogGUID();


                    // DAD_EVENT_FROM und DAD_EVENT_TO
                    String dadEventFrom = "";
                    String dadEventTo = "";
                    String dadInternalTextFromDIALOG = "";
                    if (additionalData != null) {
                        iPartsDataDialogAddData addData = additionalData.get(guid);
                        if (addData != null) {
                            dadEventFrom = addData.getFieldValue(FIELD_DAD_EVENT_FROM);
                            dadEventTo = addData.getFieldValue(FIELD_DAD_EVENT_TO);
                            dadInternalTextFromDIALOG = addData.getFieldValue(FIELD_DAD_INTERNAL_TEXT);
                        }
                    }
                    newRow.dialogAttributes.addField(FIELD_DAD_EVENT_FROM, dadEventFrom, DBActionOrigin.FROM_DB);
                    newRow.dialogAttributes.addField(FIELD_DAD_EVENT_TO, dadEventTo, DBActionOrigin.FROM_DB);
                    newRow.dialogAttributes.addField(FIELD_DAD_INTERNAL_TEXT, dadInternalTextFromDIALOG, DBActionOrigin.FROM_DB);

                    result.add(newRow);
                    return false;
                }
            };

            // Verwende !true für die where-Bedingung von MAT.M_ASSEMBLY, damit bei fehlendem Material im LeftOuter-Join
            // der Datensatz trotzdem durchkommt (dort ist das Feld nämlich dann leer und nicht 0, was bei false erwartet
            // werden würde)
            dialogDataList.searchSortAndFillWithJoin(getEtkProject(), getEtkProject().getDBLanguage(), selectFields,
                                                     new String[]{ TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_SERIES_NO),
                                                                   TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_HM),
                                                                   TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_M),
                                                                   TableAndFieldName.make(TABLE_DA_DIALOG, FIELD_DD_SM),
                                                                   TableAndFieldName.make(TABLE_MAT, FIELD_M_ASSEMBLY) },
                                                     new String[]{ hmMSmId.getSeries(), hmMSmId.getHm(), hmMSmId.getM(),
                                                                   hmMSmId.getSm(), SQLStringConvert.booleanToPPString(false) },
                                                     false, null, false, foundAttributesCallback,
                                                     new EtkDataObjectList.JoinData(TABLE_MAT,
                                                                                    new String[]{ FIELD_DD_PARTNO },
                                                                                    new String[]{ FIELD_M_MATNR },
                                                                                    true, false));

            sortDialogResults(result);
            return result;
        }
    }

    /**
     * Liefert die AS-Zusatzdaten zum übergebenen DIALOG HMMSM Knoten
     *
     * @param hmMSmId
     * @return
     */
    private Map<String, iPartsDataDialogAddData> getAdditionalData(HmMSmId hmMSmId) {
        iPartsDataDialogAddDataList newestDataForHmMSm = iPartsDataDialogAddDataList.loadNewestDataForHmMSmNode(getEtkProject(), hmMSmId);
        if (!newestDataForHmMSm.isEmpty()) {
            Map<String, iPartsDataDialogAddData> result = new HashMap<>();
            for (iPartsDataDialogAddData addData : newestDataForHmMSm) {
                result.put(addData.getAsId().getGUID(), addData);
            }
            return result;
        }
        return null;
    }

    /**
     * @param lfdNumber
     * @param dialogPartsListResult
     */
    private EtkDataPartListEntry createDialogEntry(int lfdNumber, DialogPartsListResult dialogPartsListResult) {
        return createDialogEntry(lfdNumber, dialogPartsListResult, null, null, null, null, null, null, null);
    }

    public EtkDataPartListEntry createDialogEntry(DBDataObjectAttributes dialogAttributes, DBDataObjectAttributes matAttributes) {
        DialogPartsListResult dialogPartsListResult = new DialogPartsListResult();
        dialogPartsListResult.dialogAttributes = dialogAttributes;
        dialogPartsListResult.matAttributes = matAttributes;
        return createDialogEntry(0, dialogPartsListResult);
    }


    /**
     * @param lfdNumber
     * @param dialogPartsListResult
     * @param retailDialogGUIDs         GUIDs der Retail-Quellen für aktuellen Kontext
     * @param firstUseInProduction
     * @param factoryDataWithHistory
     * @param internalTextMap
     * @param colorTableDataWithHistory
     * @param genericPartMap
     * @param genInstallLocationMap
     * @return
     */
    private EtkDataPartListEntry createDialogEntry(int lfdNumber, DialogPartsListResult dialogPartsListResult, Set<String> retailDialogGUIDs,
                                                   Map<iPartsDialogBCTEPrimaryKey, FactoryDataForPartList> firstUseInProduction,
                                                   Map<iPartsDialogBCTEPrimaryKey, iPartsFactoryData> factoryDataWithHistory,
                                                   Map<iPartsDialogBCTEPrimaryKey, List<iPartsDataInternalText>> internalTextMap,
                                                   Map<PartId, iPartsColorTable> colorTableDataWithHistory,
                                                   Map<iPartsDialogBCTEPrimaryKey, List<iPartsDataGenericPart>> genericPartMap,
                                                   Map<String, iPartsDataGenInstallLocation> genInstallLocationMap) {
        // Attribute erzeugen und alle Felder mit Leer auffüllen
        DBDataObjectAttributes katAttributes = iPartsVirtualAssemblyHelper.createEmptyAttributesForTable(getEtkProject(), TABLE_KATALOG);

        String matNo = dialogPartsListResult.dialogAttributes.getField(FIELD_DD_PARTNO).getAsString();

        katAttributes.addField(FIELD_K_VARI, getAsId().getKVari(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VER, getAsId().getKVer(), DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SACH, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_VER, "", DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MATNR, matNo, DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_MVER, "", DBActionOrigin.FROM_DB);
        String dialogGUID = dialogPartsListResult.dialogAttributes.getField(FIELD_DD_GUID).getAsString();
        katAttributes.addField(FIELD_K_LFDNR, dialogGUID, DBActionOrigin.FROM_DB);
        katAttributes.addField(FIELD_K_SEQNR, EtkDbsHelper.formatLfdNr(lfdNumber), DBActionOrigin.FROM_DB);

        // virtuelle Felder setzen
        for (VirtualFieldDefinition mapping : iPartsDataVirtualFieldsDefinition.getMapping(TABLE_DA_DIALOG, TABLE_KATALOG)) {
            String fieldName = mapping.getVirtualFieldName();
            String value;
            DBDataObjectAttribute attrib = dialogPartsListResult.dialogAttributes.getField(mapping.getSourceFieldName(), false);
            if (attrib != null) {
                value = attrib.getAsString();
            } else {
                value = null;
            }
            if (value != null) { // Feld hat nur einen Wert, wenn es benötigt wird
                katAttributes.addField(fieldName, value, true, DBActionOrigin.FROM_DB);
            }
        }

        // Schleife über das Mapping der virtuellen Felder aus [DA_DIALOG_ADD_DATA]
        for (VirtualFieldDefinition mapping : iPartsDataVirtualFieldsDefinition.getMapping(iPartsConst.TABLE_DA_DIALOG_ADD_DATA, TABLE_KATALOG)) {
            String fieldName = mapping.getVirtualFieldName();
            DBDataObjectAttribute attrib = dialogPartsListResult.dialogAttributes.getField(mapping.getSourceFieldName(), false);
            if (attrib != null) {
                katAttributes.addField(fieldName, attrib.getAsString(), true, DBActionOrigin.FROM_DB);
            }
        }

        addAditionalVirtualFields(katAttributes);

        EtkDataPartListEntry newPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getEtkProject(), katAttributes);

        EtkDataPart partForPartListEntry = newPartListEntry.getPart();
        partForPartListEntry.setAttributes(dialogPartsListResult.matAttributes, DBActionOrigin.FROM_DB);
        iPartsVirtualAssemblyHelper.addFilterAttributesForTable(getEtkProject(), TABLE_MAT, partForPartListEntry.getAttributes());

        // jetzt ist der Entry schon lebensfähig, hier die speziell berechneten Felder ermitteln
        // virtuelles Feld DIALOG_DD_RETAIL_USE nur hinzufügen wenn benötigt (dann ist retailDialogGUIDs != null)
        if (retailDialogGUIDs != null) {
            String value;
            if (retailDialogGUIDs.contains(dialogGUID)) {
                value = RETAIL_ASSIGNED;
            } else {
                value = RETAIL_NOT_ASSIGNED;
            }
            newPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_RETAILUSE, value, true,
                                                      DBActionOrigin.FROM_DB);
        }

        // finde zu diesem Entry das von der übergeordeten Funktion ermittelte früheste Einsatzdatum
        if (firstUseInProduction != null) {
            boolean factoryDataForPartListFound = false;
            iPartsDialogBCTEPrimaryKey dialogKeyOfEntry = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(newPartListEntry);
            if (dialogKeyOfEntry != null) {
                FactoryDataForPartList data = firstUseInProduction.get(dialogKeyOfEntry);
                if (data != null) {
                    factoryDataForPartListFound = true;
                    setVirtualFactoryDataFields(newPartListEntry, data.getValueForFieldName(iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_FIRST_USE),
                                                data.getValueForFieldName(iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_FIRST_USE_TO),
                                                data.getValueForFieldName(iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_ID));
                }
            }

            if (!factoryDataForPartListFound) {
                // Entweder es existieren keine Werkseinsatzdaten, oder kein Inhalt für ein Feld
                // In beiden Fällen wird das Feld mit "" gefüllt, damit gekennzeichnet wird, dass der Inhalt schon ermittelt wurde
                setVirtualFactoryDataFields(newPartListEntry, "", "", "");
            }
        }

        if (newPartListEntry instanceof iPartsDataPartListEntry) {
            // Setzen der unbearbeiteten Werkseinsatzdaten
            iPartsDialogBCTEPrimaryKey dialogKeyOfEntry = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(newPartListEntry);
            iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)newPartListEntry;
            if (dialogKeyOfEntry != null) {
                if ((factoryDataWithHistory != null) && factoryDataWithHistory.containsKey(dialogKeyOfEntry)) {
                    partListEntry.setFactoryDataForConstruction(factoryDataWithHistory.get(dialogKeyOfEntry));
                } else {
                    // Damit das "factoryDataForConstructionLoaded" Flag auf true steht und der Datensatz nicht manuell nachgeladen werden muss -> StackOverFlow
                    partListEntry.setFactoryDataForConstruction(new iPartsFactoryData());
                }
            }
            if (colorTableDataWithHistory != null) {
                iPartsColorTable colorTableData = colorTableDataWithHistory.get(new PartId(matNo, ""));
                partListEntry.setColorTableForConstruction(colorTableData); // Falls "null" trotzdem setzen, da loaded-Flag auf "true" gesetzt wird
            }
            addAASetOfEnumColumn(newPartListEntry, dialogPartsListResult.dialogAttributes.getField(FIELD_DD_AA).getAsString());
            if (internalTextMap != null) {
                List<iPartsDataInternalText> internalTexts = internalTextMap.get(dialogKeyOfEntry);
                String currentInternalText = null;
                if ((internalTexts != null) && !internalTexts.isEmpty()) {
                    currentInternalText = internalTexts.get(0).getText();
                }
                partListEntry.setCurrentInternalText(currentInternalText);
            }

            // GenericPart
            if (genericPartMap != null) {
                String bctgGenericPartNo = "";
                String bctgSolutuion = "";
                String bctgVariantNo = "";
                if (dialogKeyOfEntry != null) {
                    // Mit dem BCTE-Schlüssel ohne SDATA in der Map nachsehen und die gefundenen iPartsDataGenericParts
                    // filtern und sortieren
                    iPartsDialogBCTEPrimaryKey bcteKeyWithoutSDatA = dialogKeyOfEntry.getPositionBCTEPrimaryKeyWithoutSDA();
                    List<iPartsDataGenericPart> genericPartList = genericPartMap.get(bcteKeyWithoutSDatA);
                    if (genericPartList != null) {
                        List<iPartsDataGenericPart> validGenericParts =
                                iPartsDataGenericPartList.getSortedValidGenericPartData(genericPartList, partListEntry.getSDATA(),
                                                                                        partListEntry.getSDATB());
                        if ((validGenericParts != null) && !validGenericParts.isEmpty()) {
                            iPartsDataGenericPart selectedGenericPart = validGenericParts.get(0);
                            bctgGenericPartNo = selectedGenericPart.getFieldValue(FIELD_DGP_GENERIC_PARTNO);
                            bctgSolutuion = selectedGenericPart.getFieldValue(FIELD_DGP_SOLUTION);
                            bctgVariantNo = selectedGenericPart.getFieldValue(FIELD_DGP_VARIANTNO);
                        }
                    }
                }

                // Die virtuellen Felder immer (im Zweifelsfall leer) hinzufügen
                partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_BCTG_GENERIC_PARTNO,
                                                       bctgGenericPartNo, true, DBActionOrigin.FROM_DB);
                partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_BCTG_SOLUTION,
                                                       bctgSolutuion, true, DBActionOrigin.FROM_DB);
                partListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_BCTG_VARIANTNO,
                                                       bctgVariantNo, true, DBActionOrigin.FROM_DB);
            }

            // Generic Installation Location
            if (genInstallLocationMap != null) {
                DBDataObjectAttributes attributes = iPartsDataGenInstallLocationList.getGenInstallLocationAttributesForBcteKey(genInstallLocationMap,
                                                                                                                               dialogKeyOfEntry);
                partListEntry.getAttributes().addFields(attributes, DBActionOrigin.FROM_DB);
            }
        }

        if (newPartListEntry instanceof iPartsDataPartListEntry) {
            // Aufruf mit false, weil bei DIALOG-Konstruktions-Stücklisten auch Werkseinsatzdaten und Farbvarianten angezeigt
            // werden sollen und diese nachgeladen werden müssen, wenn sie nicht bei der Erzeugung durch Übergabe von
            // factoryDataWithHistory und colorTableDataWithHistory gesetzt wurden
            ((iPartsDataPartListEntry)newPartListEntry).setDataLoadedFlagsForVirtualPLE(false);
        }
        return newPartListEntry;
    }

    private void setVirtualFactoryDataFields(EtkDataPartListEntry newPartListEntry, String factoryFirstUse, String factoryFirstUseTo,
                                             String factoryId) {
        newPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_FIRST_USE, factoryFirstUse,
                                                  true, DBActionOrigin.FROM_DB);
        newPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_FIRST_USE_TO, factoryFirstUseTo,
                                                  true, DBActionOrigin.FROM_DB);
        newPartListEntry.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DIALOG_DD_FACTORY_ID, factoryId,
                                                  true, DBActionOrigin.FROM_DB);
    }

    /**
     * Fügt zusätzliche virtuelle Felder zur Konstruktionsstücklisten hinzu, z.B. Felder, für die es kein DA_DIALOG
     * Mapping gibt
     *
     * @param attributes
     */
    private void addAditionalVirtualFields(DBDataObjectAttributes attributes) {
        if (!attributes.fieldExists(iPartsDataVirtualFieldsDefinition.DD_PARTLIST_TEXT)) {
            DBDataObjectAttribute objectAttribute = new DBDataObjectAttribute(iPartsDataVirtualFieldsDefinition.DD_PARTLIST_TEXT, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, true);
            attributes.addField(objectAttribute, DBActionOrigin.FROM_DB);
        }
        if (!attributes.fieldExists(iPartsDataVirtualFieldsDefinition.DD_PARTLIST_TEXTKIND)) {
            attributes.addField(iPartsDataVirtualFieldsDefinition.DD_PARTLIST_TEXTKIND, iPartsDialogPartlistTextkind.NOT_A_TEXT.getTxtKindShort(), true, DBActionOrigin.FROM_DB);
        }
        if (!attributes.fieldExists(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA_SOE)) {
            DBDataObjectAttribute objectAttribute = new DBDataObjectAttribute(iPartsDataVirtualFieldsDefinition.DIALOG_DD_AA_SOE, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, true);
            attributes.addField(objectAttribute, DBActionOrigin.FROM_DB);
        }
        if (!attributes.fieldExists(iPartsDataVirtualFieldsDefinition.DD_AUTHOR_ORDER)) {
            attributes.addField(iPartsDataVirtualFieldsDefinition.DD_AUTHOR_ORDER, NO_ACTIVE_AUTHOR_ORDER_NAME, true, DBActionOrigin.FROM_DB);
        }
        if (!attributes.fieldExists(iPartsDataVirtualFieldsDefinition.DD_AUTHOR_ORDER_AFFILIATION)) {
            attributes.addField(iPartsDataVirtualFieldsDefinition.DD_AUTHOR_ORDER_AFFILIATION, AOAffiliationForDIALOGEntry.NONE.getTextValue(), true, DBActionOrigin.FROM_DB);
        }
    }

    protected DBDataObjectList<EtkDataPartListEntry> loadVirtualDialogEinPas(iPartsCatalogNode completeStructure, iPartsVirtualNode rootNode,
                                                                             EinPasId subNodeId, boolean subAssembliesOnly, EtkDisplayFields fields) {
        iPartsCatalogNode nodeParent;
        if (subNodeId != null) {
            nodeParent = completeStructure.getNode(subNodeId);
        } else {
            // Kein subknoten -> nehme den obersten
            nodeParent = completeStructure;
        }

        if (nodeParent == null) {
            // Stückliste nicht mehr da, evtl. von jemandem gelöscht worden
            return new EtkDataPartListEntryList();
        }


        //Jetzt die Childs an diese Baugruppe anfügen
        DBDataObjectList<EtkDataPartListEntry> result = new EtkDataPartListEntryList();
        int lfdNumber = 0;
        if ((nodeParent.getId() instanceof EinPasId) && ((EinPasId)nodeParent.getId()).isTuNode()) {
            EinPasId parentId = (EinPasId)nodeParent.getId();

            // Wir sind auf dem untersten Knoten, zeige alle Submodule dieses EinPAS-Knotens an
            iPartsSeriesId seriesId = (iPartsSeriesId)rootNode.getId();
            List<DialogPartsListResult> records = getDialogPartsListForEinPas(seriesId, parentId, subAssembliesOnly);

            for (DialogPartsListResult rec : records) {
                lfdNumber++;
                EtkDataPartListEntry newEntry = createDialogEntry(lfdNumber, rec);
                if (newEntry != null) {
                    result.add(newEntry, DBActionOrigin.FROM_DB);
                }
            }
        } else {
            for (iPartsCatalogNode nodeChild : nodeParent.getChildren()) {
                lfdNumber++;
                if (nodeChild.getId() instanceof AssemblyId) {
                    // Hier wird eine ganz normale Baugruppe als Child angezeigt
                    AssemblyId childAssemblyId = (AssemblyId)nodeChild.getId();
                    EtkDataPartListEntry newEntry = createAssemblyChildNode(lfdNumber, childAssemblyId);
                    if (newEntry != null) {
                        result.add(newEntry, DBActionOrigin.FROM_DB);
                    }
                } else if (nodeChild.getId() instanceof EinPasId) {
                    // Hier wird die EinPAS angezeigt
                    EinPasId childId = (EinPasId)nodeChild.getId();
                    EtkDataPartListEntry newEntry = createVirtualNode(lfdNumber, rootNode, new iPartsVirtualNode(iPartsNodeType.EINPAS, childId));
                    if (newEntry != null) {
                        result.add(newEntry, DBActionOrigin.FROM_DB);
                    }
                } else if (nodeChild.getId() instanceof HmMSmId) {
                    // Hier wird die HM/M/SM angezeigt
                    HmMSmId childId = (HmMSmId)nodeChild.getId();
                    EtkDataPartListEntry newEntry = createVirtualNode(lfdNumber, rootNode, new iPartsVirtualNode(iPartsNodeType.HMMSM, childId));
                    if (newEntry != null) {
                        result.add(newEntry, DBActionOrigin.FROM_DB);
                    }
                }
            }
        }

        return result;
    }

    private List<DialogPartsListResult> getDialogPartsListForEinPas(iPartsSeriesId seriesId, EinPasId parentId, boolean subAssembliesOnly) {
        List<DialogPartsListResult> result = new ArrayList<>();
        MappingHmMSmToEinPas mapping = MappingHmMSmToEinPas.getInstance(getEtkProject(), seriesId);
        List<HmMSmId> hmMSmIds = mapping.get(parentId);

        if (hmMSmIds != null) {
            for (HmMSmId hmMSmId : hmMSmIds) {
                List<DialogPartsListResult> records = getDialogPartsList(hmMSmId, subAssembliesOnly);
                result.addAll(records);
            }
        }
        return result;
    }

    @Override
    public String getOrderNumber() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();

        if (lastVirtualNode.getId() instanceof HmMSmId) { // HM/M/SM
            HmMSmId hmMSmId = (HmMSmId)lastVirtualNode.getId();
            if (hmMSmId.isHmNode()) {
                return hmMSmId.getHm();
            } else if (hmMSmId.isMNode()) {
                return hmMSmId.getM();
            } else if (hmMSmId.isSmNode()) {
                return hmMSmId.getSm();
            }
        }

        return super.getOrderNumber();
    }

    @Override
    public String getPartsListType() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof HmMSmId) { // HM/M/SM
            HmMSmId hmMSmId = (HmMSmId)lastNodeId;
            if (hmMSmId.isHmNode()) {
                return PARTS_LIST_TYPE_DIALOG_HM;
            } else if (hmMSmId.isMNode()) {
                return PARTS_LIST_TYPE_DIALOG_M;
            } else if (hmMSmId.isSmNode()) {
                return PARTS_LIST_TYPE_DIALOG_SM;
            }
        }

        return super.getPartsListType();
    }

    @Override
    public EtkMultiSprache getTexts() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof HmMSmId) { // HM/M/SM
            HmMSmId hmMSmId = (HmMSmId)lastNodeId;
            HmMSm hmMSm = HmMSm.getInstance(getEtkProject(), new iPartsSeriesId(hmMSmId.getSeries()));

            HmMSmNode node = hmMSm.getNode(hmMSmId);

            if (node != null) {
                EtkMultiSprache title = node.getTitle();

                // Ist der HM/M/SM-Knoten ausgeblendet?
                if (node.isHidden()) {
                    return createTitleWithSuffix(title, "!!<%1> (ausgeblendet)");
                } else if (node.isNoCalc()) {
                    return createTitleWithSuffix(title, "!!<%1> (nicht berechnen)");
                } else if (node.isChangeDocuRelOmittedPart()) {
                    return createTitleWithSuffix(title, "!!<%1> (Sonderberechnung Wegfall-SNR)");
                } else {
                    return title;
                }
            } else {
                return null;
            }
        }

        return super.getTexts();
    }

    /**
     * Erzeugt eine Kopie des übergebenen {@link EtkMultiSprache} mit dem übergebenen Suffix
     *
     * @param title
     * @param textKey
     * @return
     */
    private EtkMultiSprache createTitleWithSuffix(EtkMultiSprache title, String textKey) {
        EtkMultiSprache titleWithSuffix = title.cloneMe();
        for (Map.Entry<String, String> languagesAndTexts : title.getLanguagesAndTexts().entrySet()) {
            String language = languagesAndTexts.getKey();
            titleWithSuffix.setText(language, TranslationHandler.translateForLanguage(textKey, language,
                                                                                      languagesAndTexts.getValue()));
        }
        return titleWithSuffix;
    }

    @Override
    public String getPictureName() {
        iPartsVirtualNode lastVirtualNode = getLastVirtualNode();
        IdWithType lastNodeId = lastVirtualNode.getId();

        if (lastNodeId instanceof HmMSmId) { // HM/M/SM
            HmMSmId hmMSmId = (HmMSmId)lastNodeId;
            HmMSmNode node = HmMSm.getInstance(getEtkProject(), new iPartsSeriesId(hmMSmId.getSeries())).getNode(hmMSmId);
            if (node != null) {
                return node.getPictureName();
            }
            return "";
        }

        return super.getPictureName();
    }

    @Override
    public void getParentAssemblyEntries(boolean filtered, List<EtkDataPartListEntry> result) {
        if (getParentAssemblyEntriesForParentId(HmMSmId.class, iPartsNodeType.HMMSM, filtered, result)) {
            return;
        }

        super.getParentAssemblyEntries(filtered, result);
    }

    /**
     * Liefert das {@link EtkProject} für die Berechnung von offenen Ständen in Konstruktions-Stücklisten zurück.
     *
     * @return
     */
    private EtkProject getProjectForReportForConstructionNodesCalculations() {
        // ChangeSet bei der Berechnung berücksichtigen oder nicht?
        if (iPartsConst.USE_CHANGE_SET_FOR_REPORT_FOR_CONSTRUCTION_NODES_CALCULATIONS) {
            return getEtkProject();
        } else {
            return iPartsPlugin.getMqProject();
        }
    }
}
