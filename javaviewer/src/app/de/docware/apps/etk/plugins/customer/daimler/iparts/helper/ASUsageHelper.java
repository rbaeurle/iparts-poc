/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.common.EtkDataObjectArray;
import de.docware.apps.etk.base.project.common.EtkDataObjectArrayList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.base.project.mechanic.usage.MechanicUsagePosition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEntrySourceType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSetEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataChangeSetEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsProductModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm.HmMSmId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Klasse um die Suche nach DIALOG-Konstruktions-Stücklisteneinträgen in aktiven ChangeSets und dem Katalog zu vereinfachen
 */
public class ASUsageHelper {

    public static final String UNKNOWN_KG = "00";
    public static final String UNKNOWN_TU = "000";


    private final EtkProject project;
    private Map<String, Map<String, Set<String>>> partListEntryUsageInChangeSetMap; // Key ist die Baureihe als String, Wert ist eine Map von SourceGUIDs auf Set von ChangeSetIds
    private Map<HmMSmId, Set<String>> partListEntryUsageInRetailMap;
    private Map<String, Map<String, Boolean>> seriesNrToMatNrUsedInRetailMap; // Key ist die Baureihe
    private Map<String, Map<String, Boolean>> seriesNrToMatNrUsedInChangeSetMap; // Key ist die Baureihe
    private Map<String, List<PartListEntryId>> partListEntryIdUsageInRetailMap; // Key ist der BCTE-Schlüssel als String
    private Map<String, Map<String, List<PartListEntryId>>> partListEntryIdUsageInChangeSetMap; // Key ist der BCTE-Schlüssel als String
    private Map<PartListEntryId, EtkDataPartListEntry> retailPartListEntryMap;
    private Map<PartListEntryId, EtkDataPartListEntry> retailPartListEntryEqualizeMap; // Stücklisteneinträge mit BCTE-Schlüssel ohne AA
    private Map<String, EtkDataPartListEntry> firstPartListEntriesMap; // Map von BCTE Schlüssel ohne AA auf den ersten AS Treffer in der DB

    private final boolean useModulesEinPas = true;  // EDS: wenn true, dann KG/TU-Bestimmung via DA_MODULES_EINPAS

    public ASUsageHelper(EtkProject project) {
        this.project = project;
        init();
    }

    private void init() {
        partListEntryUsageInChangeSetMap = new HashMap<>();
        partListEntryUsageInRetailMap = new HashMap<>();
        seriesNrToMatNrUsedInRetailMap = new HashMap<>();
        seriesNrToMatNrUsedInChangeSetMap = new HashMap<>();
        partListEntryIdUsageInRetailMap = new HashMap<>();
        partListEntryIdUsageInChangeSetMap = new HashMap<>();
        retailPartListEntryMap = new HashMap<>();
        retailPartListEntryEqualizeMap = new HashMap<>();
    }

    public void clear() {
        init();
    }

    public EtkProject getProject() {
        return project;
    }

    /**
     * Überprüft, ob der {@link iPartsDialogBCTEPrimaryKey} in einem aktiven ChangeSet verwendet wird.
     * Dazu werden alle SOURCE_GUIDs zur Baureihe geladen.
     *
     * @param primaryBCTEKey
     * @return
     */
    public boolean isUsedInActiveChangeSets(iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
        if (primaryBCTEKey == null) {
            return false;
        }
        String seriesNo = primaryBCTEKey.getHmMSmId().getSeries();
        // zuerst Suche in ChangeSetEntries
        Map<String, Set<String>> guidToChangeSetIdsMap = partListEntryUsageInChangeSetMap.get(seriesNo);
        if (guidToChangeSetIdsMap == null) {
            String partialGUID = seriesNo + iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER + "*";
            iPartsDataChangeSetEntryList dataChangeSetEntries = iPartsRevisionsHelper.getActiveChangeSetEntriesContainingSourceGUID(PartListEntryId.TYPE,
                                                                                                                                    partialGUID,
                                                                                                                                    getProject());
            guidToChangeSetIdsMap = new HashMap<>(dataChangeSetEntries.size());
            for (iPartsDataChangeSetEntry dataChangeSetEntry : dataChangeSetEntries) {
                String sourceGUID = dataChangeSetEntry.getSourceGUID();
                Set<String> changeSetIds = guidToChangeSetIdsMap.computeIfAbsent(sourceGUID, k -> new TreeSet<>());
                String changeSetId = dataChangeSetEntry.getAsId().getGUID();
                changeSetIds.add(changeSetId);
                // Hier die PartListEntryIds zum ChangeSet zur Baureihe aufsammeln
                PartListEntryId partListEntryId = dataChangeSetEntry.getIdFromChangeSetEntry(PartListEntryId.class, PartListEntryId.TYPE);
                if ((partListEntryId != null) && partListEntryId.isValidId()) {
                    // ChangeSet zu Positionen, die darin vorkommen
                    Map<String, List<PartListEntryId>> changeSetToPartListEntryIds = partListEntryIdUsageInChangeSetMap.computeIfAbsent(sourceGUID, k -> new HashMap<>());

                    List<PartListEntryId> partListEntryIds = changeSetToPartListEntryIds.computeIfAbsent(changeSetId, k -> new ArrayList<>());
                    partListEntryIds.add(partListEntryId);
                }
            }
            partListEntryUsageInChangeSetMap.put(seriesNo, guidToChangeSetIdsMap);
        }
        return guidToChangeSetIdsMap.containsKey(primaryBCTEKey.createDialogGUID());
    }

    /**
     * Überprüft, ob die Materialnummer in einem aktiven ChangeSet verwendet wird von nicht gelöschten Stücklisteneinträgen.
     * Es interessieren nur die Verwendungen in der passenden Baureihe
     *
     * @param seriesNr
     * @param matNr
     * @return
     */
    public boolean isMatNrUsedInActiveChangeSets(String seriesNr, String matNr) {
        // zuerst Suche in ChangeSetEntries
        if (StrUtils.isValid(matNr, seriesNr)) {
            Map<String, Boolean> matNrUsedInChangeSetMap = seriesNrToMatNrUsedInChangeSetMap.get(seriesNr);
            if (matNrUsedInChangeSetMap != null) {
                Boolean isUsed = matNrUsedInChangeSetMap.get(matNr);
                if (isUsed != null) {
                    return isUsed;
                }
            }

            iPartsDataChangeSetEntryList dataChangeSetEntries = iPartsRevisionsHelper.getActiveChangeSetEntriesContainingMatNo(PartListEntryId.TYPE,
                                                                                                                               matNr,
                                                                                                                               getProject());
            VarParam<Boolean> matNrUsed = new VarParam<>(false);
            for (iPartsDataChangeSetEntry dataChangeSetEntry : dataChangeSetEntries) {
                PartListEntryId partListEntryId = dataChangeSetEntry.getIdFromChangeSetEntry(PartListEntryId.class, PartListEntryId.TYPE);
                if ((partListEntryId != null) && partListEntryId.isValidId()) {
                    EtkDataPartListEntry entry = EtkDataObjectFactory.createDataPartListEntry(getProject(), partListEntryId);
                    if (entry instanceof iPartsDataPartListEntry) {
                        iPartsDataPartListEntry partListEntry = (iPartsDataPartListEntry)entry;
                        SerializedDBDataObject serializedDBDataObject = dataChangeSetEntry.getSerializedDBDataObject();
                        SerializedDBDataObjectState state = serializedDBDataObject.getState();

                        // Nur relevante Zustande (gelöscht ist nicht relevant) berücksichtigen
                        if (!state.isMustBeSimulatedAndSaved() || (state == SerializedDBDataObjectState.DELETED)) {
                            continue;
                        }

                        // BCTE-Schlüssel mit Baureihe kann sich nachträglich nicht ändern -> bei MODIFIED muss ChangeSet
                        // nicht simuliert werden, bei anderen Zuständen aber schon, weil der Stücklisteneintrag ansonsten
                        // nicht geladen werden kann
                        EtkRevisionsHelper revisionsHelper = project.getRevisionsHelper();
                        boolean changeSetMustBeActivated = (state != SerializedDBDataObjectState.MODIFIED) && (revisionsHelper != null);

                        Runnable checkSeriesRunnable = () -> {
                            if (partListEntry.existsInDB()) {
                                iPartsSeriesId seriesId = partListEntry.getSeriesId();
                                if ((seriesId != null) && seriesId.getSeriesNumber().equals(seriesNr)) {
                                    matNrUsed.setValue(true);
                                }
                            }
                        };

                        if (changeSetMustBeActivated) {
                            // ChangeSet temporär aktivieren und simulieren für das Bestimmen der Baureihe
                            project.executeWithoutActiveChangeSets(() -> {
                                iPartsRevisionChangeSet changeSet = new iPartsRevisionChangeSet(new iPartsChangeSetId(dataChangeSetEntry.getAsId().getGUID()),
                                                                                                project, true);
                                List<AbstractRevisionChangeSet> changeSets = new DwList<>(1);
                                changeSets.add(changeSet);
                                revisionsHelper.setActiveRevisionChangeSets(changeSets, null, false, project);
                                checkSeriesRunnable.run();
                                // Vorher aktive ChangeSets werden am Ende von executeWithoutActiveChangeSets() automatisch wiederhergestellt
                            }, false);
                        } else {
                            checkSeriesRunnable.run();
                        }

                        if (matNrUsed.getValue()) {
                            break;
                        }
                    }
                }
            }

            matNrUsedInChangeSetMap = seriesNrToMatNrUsedInChangeSetMap.computeIfAbsent(seriesNr, k -> new HashMap<>());
            matNrUsedInChangeSetMap.put(matNr, matNrUsed.getValue());
            return matNrUsed.getValue();
        }

        return false;
    }


    /**
     * Überprüft, ob der BCTE Schlüssel in einem offenen Autorenauftrag existiert, ohne PSK Einträge zu berücksichtigen
     *
     * @param primaryBCTEKey
     * @return
     */
    public boolean isUsedInActiveChangeSetsWithoutPSK(iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
        if (isUsedInActiveChangeSets(primaryBCTEKey)) {
            Map<String, List<PartListEntryId>> changeSetIdToEntries = partListEntryIdUsageInChangeSetMap.get(primaryBCTEKey.createDialogGUID());
            if (changeSetIdToEntries == null) {
                return false;
            }
            return changeSetIdToEntries.values().stream().anyMatch(entries -> !getPartListEntriesByIds(entries, false, retailPartListEntryMap, true).isEmpty());
        }
        return false;
    }

    /**
     * Überprüft, ob der Stücklisteneintrag in einem PSK Modul vorkommt, das in einem offenen Autorenauftrag vorkommt
     *
     * @param primaryBCTEKey
     * @return
     */
    public boolean isUsedInActiveChangeSetsCheckOnlyPSK(iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
        if (isUsedInActiveChangeSets(primaryBCTEKey)) {
            Map<String, List<PartListEntryId>> changeSetIdToEntries = partListEntryIdUsageInChangeSetMap.get(primaryBCTEKey.createDialogGUID());
            if (changeSetIdToEntries == null) {
                return false;
            }
            Map<AssemblyId, Boolean> assemblyIsPSKMap = new HashMap<>();
            return changeSetIdToEntries.values().stream().anyMatch(entries -> {
                // Das Ergebnis ist nicht relevant, sondern nur die Info, ob es ein PSK Modul unter den gefundenen
                // Einträgen gab. omitPSKEntrie muss "true" sein, damit der PSK-Assembly-Check überhaupt gemacht wird!
                getEntriesForIds(entries, false, true, retailPartListEntryMap, assemblyIsPSKMap);
                return assemblyIsPSKMap.values().stream().anyMatch(value -> value);
            });
        }
        return false;
    }

    /**
     * Überprüft, ob der {@link iPartsDialogBCTEPrimaryKey} im Katalog verwendet wird.
     * Dazu werden alle Source_GUIDs zur {@link HmMSmId} geladen.
     *
     * @param primaryBCTEKey
     * @return
     */
    public boolean isUsedInASPartList(iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
        // jetzt Suche in Katalog
        HmMSmId hmMSmId = primaryBCTEKey.getHmMSmId();
        Set<String> guidSet = partListEntryUsageInRetailMap.get(hmMSmId);
        if (guidSet == null) {
            String[] fields = new String[]{ iPartsConst.FIELD_K_VARI, iPartsConst.FIELD_K_VER, iPartsConst.FIELD_K_LFDNR,
                                            iPartsConst.FIELD_K_SOURCE_GUID };
            String partialGUID = hmMSmId.toString(iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER) +
                                 iPartsDialogBCTEPrimaryKey.DIALOG_GUID_DELIMITER + "*";
            DBDataObjectAttributesList attributesList = EditConstructionToRetailHelper.getRetailSourceGuidAttributeList(iPartsEntrySourceType.DIALOG,
                                                                                                                        partialGUID,
                                                                                                                        hmMSmId.getDIALOGSourceContext(),
                                                                                                                        fields,
                                                                                                                        getProject());
            guidSet = new HashSet<>(attributesList.size());
            for (DBDataObjectAttributes attribute : attributesList) {
                String sourceGUID = attribute.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID);
                guidSet.add(sourceGUID);
                List<PartListEntryId> partEntryIdList = partListEntryIdUsageInRetailMap.get(sourceGUID);
                if (partEntryIdList == null) {
                    partEntryIdList = new DwList<>();
                    partListEntryIdUsageInRetailMap.put(sourceGUID, partEntryIdList);
                }
                partEntryIdList.add(new PartListEntryId(attribute.getFieldValue(iPartsConst.FIELD_K_VARI),
                                                        attribute.getFieldValue(iPartsConst.FIELD_K_VER),
                                                        attribute.getFieldValue(iPartsConst.FIELD_K_LFDNR)));
            }
            partListEntryUsageInRetailMap.put(hmMSmId, guidSet);
        }
        return guidSet.contains(primaryBCTEKey.createDialogGUID());
    }

    /**
     * Überprüft, ob die Materialnummer im Katalog verwendet wird.
     * Es interessieren nur die Verwendung in der passenden Baureihe
     *
     * @param seriesNr
     * @param matNr
     * @return
     */
    public boolean isMatNrUsedInAsPartList(String seriesNr, String matNr) {
        // jetzt Suche in Katalog
        if (StrUtils.isValid(matNr, seriesNr)) {
            Map<String, Boolean> matNrUsedInRetailMap = seriesNrToMatNrUsedInRetailMap.get(seriesNr);
            if (matNrUsedInRetailMap != null) {
                Boolean isUsed = matNrUsedInRetailMap.get(matNr);
                if (isUsed != null) {
                    return isUsed;
                }
            }
            String[] fields = new String[]{ iPartsConst.FIELD_K_VARI, iPartsConst.FIELD_K_VER, iPartsConst.FIELD_K_LFDNR,
                                            iPartsConst.FIELD_K_SOURCE_GUID };
            String seriesNrWithWildCard = seriesNr.concat("*");
            DBDataObjectAttributesList attributesList = EditConstructionToRetailHelper.getRetailMatNrAtrributeListFilteredWithSeriesNo(seriesNrWithWildCard, matNr, fields,
                                                                                                                                       true, project);
            matNrUsedInRetailMap = seriesNrToMatNrUsedInRetailMap.computeIfAbsent(seriesNr, k -> new HashMap<>());
            matNrUsedInRetailMap.put(matNr, !attributesList.isEmpty());
            return !attributesList.isEmpty();
        }
        return false;
    }

    /**
     * Überprüft, ob der {@link iPartsDialogBCTEPrimaryKey} in einem aktiven ChangeSet oder im Katalog verwendet wird.
     *
     * @param primaryBCTEKey
     * @return
     */
    public boolean isUsedInAS(iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
        // zuerst Suche in ChangeSetEntries
        if (!isUsedInActiveChangeSets(primaryBCTEKey)) {
            // jetzt Suche in Katalog
            return isUsedInASPartList(primaryBCTEKey);
        }
        return true;
    }

    /**
     * Überprüft, ob die Materialnummer in einem aktiven ChangeSet oder im katalog verwendet wird abh von der Bauhreihe
     *
     * @param seriesNr
     * @param matNr
     * @return
     */
    public boolean isMatNrUsedInAS(String seriesNr, String matNr) {
        // zuerst Suche in ChangeSetEntries
        if (!isMatNrUsedInActiveChangeSets(seriesNr, matNr)) {
            // jetzt Suche in Katalog
            return isMatNrUsedInAsPartList(seriesNr, matNr);
        }
        return true;
    }

    /**
     * Überprüft, ob der übergeben BCTE Schlüssel nur in Stücklisten innerhalb PSK Produkte vorkommt
     *
     * @param bctePrimaryKey
     * @param checkOpenAuthorOrders
     * @return
     */
    public boolean checkIfOnlyPSKProducts(iPartsDialogBCTEPrimaryKey bctePrimaryKey, boolean checkOpenAuthorOrders) {
        // Erst überprüfen, ob die Positionen zum BCTE Schlüssel in der DB zu PSK Produkten gehören
        List<EtkDataPartListEntry> partListEntries = getPartListEntriesUsedInAS(bctePrimaryKey, false);
        if (partListEntries == null) {
            partListEntries = new DwList<>();
        }
        Map<AssemblyId, Boolean> assemblyIsPSKMap = new HashMap<>();
        for (EtkDataPartListEntry entry : partListEntries) {
            if (!isPSKAssembly(getProject(), entry.getOwnerAssemblyId(), assemblyIsPSKMap)) {
                return false;
            }
        }

        // Jetzt überprüfen, ob die Positionen zum BCTE Schlüssel in offenen Autorenaufträgen zu PSK Produkten gehören (sofern gewünscht)
        boolean entryIdsInChangeSetsFound = false;
        if (checkOpenAuthorOrders) {
            // Erst alle ChangeSets holen in denen der BCTE Schlüssel an eine Position existiert
            Set<String> changeSetIds = getChangeSetIdsForPartListEntriesUsedInActiveChangeSets(bctePrimaryKey);

            if ((changeSetIds != null) && !changeSetIds.isEmpty()) {
                for (String changeSetId : changeSetIds) {
                    // Jetzt alle Positionen holen, die den BCTE Schlüssel haben und im ChangeSet existieren
                    List<PartListEntryId> entryIdsInChangeSets = getPartListEntryIdsUsedInGivenChangeSet(bctePrimaryKey, changeSetId);
                    if (entryIdsInChangeSets == null) {
                        continue;
                    } else if (!entryIdsInChangeSets.isEmpty()) {
                        entryIdsInChangeSetsFound = true;
                    }
                    for (PartListEntryId partListEntryId : entryIdsInChangeSets) {
                        // Im ChangeSet via Like-Suche nach einer Produkt zu Modul Verknüpfung suchen.
                        AssemblyId assemblyId = partListEntryId.getOwnerAssemblyId();
                        // Wurde die Position zum BCTE im Changeset mit einem anderen Produkt (Stückliste in Produkt)
                        // verknüpft und das Modul existiert schon am Produkt, dann kann hier der "einfache" Check
                        // gemacht werden
                        if (isPSKAssembly(getProject(), assemblyId, assemblyIsPSKMap)) {
                            continue;
                        }
                        iPartsDataChangeSetEntryList loadedChangeSetEntries
                                = iPartsDataChangeSetEntryList.loadChangeSetEntriesWithLikeForChangeSetAndDataObjectIdWithType(getProject(),
                                                                                                                               new iPartsChangeSetId(changeSetId),
                                                                                                                               new iPartsProductModulesId("*", assemblyId.getKVari()));
                        // Wurden neue Verknüpfungen gefunden, wird das Produkt extrahiert und auf seine PSK Eigenschaft
                        // hin geprüft
                        for (iPartsDataChangeSetEntry foundEntry : loadedChangeSetEntries) {
                            IdWithType id = foundEntry.getAsId().getDataObjectIdWithType();
                            if (id != null) {
                                iPartsProductModulesId productModulesId = IdWithType.fromStringArrayWithTypeFromClass(iPartsProductModulesId.class,
                                                                                                                      id.toStringArrayWithoutType());
                                if (!isPSKProduct(new iPartsProductId(productModulesId.getProductNumber()))) {
                                    // Ist ein Produkt vorhanden, dass kein PSK Produkt ist, dann ist der komplett Check "false"
                                    // (Es gibt also nicht-PSK Produkte, die Positionen zum BCTE Schlüssel enthalten)
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        return !partListEntries.isEmpty() || entryIdsInChangeSetsFound;
    }

    public static boolean isPSKAssembly(EtkProject projectForCheck, AssemblyId assemblyId, Map<AssemblyId, Boolean> assemblyIsPSKMap) {
        return assemblyIsPSKMap.computeIfAbsent(assemblyId, id -> {
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(projectForCheck, id);
            return iPartsPSKHelper.isPSKAssembly(assembly);
        });
    }

    public boolean isPSKProduct(iPartsProductId productId) {
        if (productId != null) {
            iPartsProduct productObject = iPartsProduct.getInstance(getProject(), productId);
            return productObject.isPSK();
        }
        return false;
    }

    /**
     * Liefert die Liste der {@link PartListEntryId}s, die zum {@link iPartsDialogBCTEPrimaryKey} adressiert im AfterSales
     * sind.
     *
     * @param primaryBCTEKey
     * @return
     */
    public List<PartListEntryId> getPartListEntryIdsUsedInAS(iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
        if (isUsedInASPartList(primaryBCTEKey)) {
            return partListEntryIdUsageInRetailMap.get(primaryBCTEKey.createDialogGUID());
        }
        return null;
    }

    /**
     * Liefert die {@link PartListEntryId}s für den BCTE Schlüssel innerhalb des übergebenen ChangeSets
     *
     * @param primaryBCTEKey
     * @param changeSetId
     * @return
     */
    public List<PartListEntryId> getPartListEntryIdsUsedInGivenChangeSet(iPartsDialogBCTEPrimaryKey primaryBCTEKey,
                                                                         String changeSetId) {
        if (isUsedInActiveChangeSets(primaryBCTEKey)) {
            Map<String, List<PartListEntryId>> changeSetToIds = partListEntryIdUsageInChangeSetMap.get(primaryBCTEKey.createDialogGUID());
            if (changeSetToIds != null) {
                return changeSetToIds.get(changeSetId);
            }
        }
        return null;
    }


    /**
     * Liefert die Liste der {@link EtkDataPartListEntry}s, die zum {@link iPartsDialogBCTEPrimaryKey} adressiert im AfterSales
     * sind.
     * Ist {@code withLoad == true}, dann werden die {@link EtkDataPartListEntry}s aus der DB geladen.
     *
     * @param primaryBCTEKey
     * @param withLoad
     * @param omitPSKEntries {@code true}, falls Stücklisteneinträge aus PSK-Modulen nicht geliefert werden sollen
     * @return {@code null} falls keine {@link EtkDataPartListEntry}s zum {@link iPartsDialogBCTEPrimaryKey} adressiert
     * sind im AfterSales
     */
    private List<EtkDataPartListEntry> retrievePartListEntriesUsedInAS(iPartsDialogBCTEPrimaryKey primaryBCTEKey, boolean withLoad,
                                                                       boolean omitPSKEntries) {
        if (primaryBCTEKey != null) {
            List<PartListEntryId> idList = getPartListEntryIdsUsedInAS(primaryBCTEKey);
            if (idList != null) {
                return getPartListEntriesByIds(idList, withLoad, retailPartListEntryMap, omitPSKEntries);
            }
        }
        return null;
    }

    /**
     * Liefert alle {@link EtkDataPartListEntry}s zum übergebenen {@link iPartsDialogBCTEPrimaryKey}, die im Retail
     * vorkommen. Aktive Autorenaufträge werden NICHT berücksichtigt!
     *
     * @param primaryBCTEKey
     * @param withLoad
     * @return
     */
    public List<EtkDataPartListEntry> getPartListEntriesUsedInAS(iPartsDialogBCTEPrimaryKey primaryBCTEKey, boolean withLoad) {
        return retrievePartListEntriesUsedInAS(primaryBCTEKey, withLoad, false);
    }

    /**
     * Liefert alle {@link EtkDataPartListEntry}s zum übergebenen {@link iPartsDialogBCTEPrimaryKey}, die im Retail
     * vorkommen und nicht zu PSK Stücklisten gehören. Aktive Autorenaufträge werden NICHT berücksichtigt!
     *
     * @param primaryBCTEKey
     * @param withLoad
     * @return
     */
    public List<EtkDataPartListEntry> getPartListEntriesUsedInASWithoutPSKProducts(iPartsDialogBCTEPrimaryKey primaryBCTEKey,
                                                                                   boolean withLoad) {
        return retrievePartListEntriesUsedInAS(primaryBCTEKey, withLoad, true);
    }

    /**
     * Erzeugt zu den übergebenen {@link PartListEntryId}s die dazugehörigen {@link EtkDataPartListEntry}s und liefert
     * diese zurück.
     *
     * @param partListEntryIds
     * @param withLoad
     * @param partListEntryMap
     * @param omitPSKEntries   {@code true}, falls Stücklisteneinträge aus PSK-Modulen nicht geliefert werden sollen
     * @return
     */
    private List<EtkDataPartListEntry> getPartListEntriesByIds(List<PartListEntryId> partListEntryIds, boolean withLoad,
                                                               Map<PartListEntryId, EtkDataPartListEntry> partListEntryMap,
                                                               boolean omitPSKEntries) {
        Map<AssemblyId, Boolean> assemblyIsPSKMap = new HashMap<>();
        return getEntriesForIds(partListEntryIds, withLoad, omitPSKEntries,
                                partListEntryMap, assemblyIsPSKMap);
    }

    /**
     * Liefert die Stücklistenpositionen zu den übergebenen {@link PartListEntryId}s
     *
     * @param partListEntryIds
     * @param withLoad
     * @param omitPSKEntries
     * @param partListEntryMap
     * @param assemblyIsPSKMap
     * @return
     */
    private List<EtkDataPartListEntry> getEntriesForIds(List<PartListEntryId> partListEntryIds, boolean withLoad,
                                                        boolean omitPSKEntries,
                                                        Map<PartListEntryId, EtkDataPartListEntry> partListEntryMap,
                                                        Map<AssemblyId, Boolean> assemblyIsPSKMap) {
        List<EtkDataPartListEntry> partListEntries = new ArrayList<>();
        for (PartListEntryId partListEntryId : partListEntryIds) {
            EtkDataPartListEntry retailPartListEntry = getEntryForId(partListEntryId, partListEntryMap, withLoad);

            // Falls PSK-Module nicht berücksichtigt werden sollen, hier weiterloopen bei einem PSK-Modul
            if (omitPSKEntries && isPSKAssembly(getProject(), retailPartListEntry.getOwnerAssemblyId(), assemblyIsPSKMap)) {
                continue;
            }
            partListEntries.add(retailPartListEntry);
        }
        return partListEntries;
    }

    /**
     * Liefert zur übergebenen {@link PartListEntryId} das erzeugte {@link EtkDataPartListEntry}
     *
     * @param partListEntryId
     * @param partListEntryMap
     * @param withLoad
     * @return
     */
    public EtkDataPartListEntry getEntryForId(PartListEntryId partListEntryId,
                                              Map<PartListEntryId, EtkDataPartListEntry> partListEntryMap,
                                              boolean withLoad) {
        EtkDataPartListEntry retailPartListEntry = partListEntryMap.get(partListEntryId);
        if (retailPartListEntry == null) {
            retailPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(), partListEntryId);
            partListEntryMap.put(partListEntryId, retailPartListEntry);
        }
        if (withLoad && !retailPartListEntry.isLoaded()) {
            retailPartListEntry.loadFromDB(partListEntryId);
        }
        return retailPartListEntry;
    }

    /**
     * Liefert die Liste aller ChangeSet-IDs von nicht freigegebenen Autoren-Aufträgen, die den übergebenen {@link iPartsDialogBCTEPrimaryKey}
     * enthalten.
     *
     * @param primaryBCTEKey
     * @return {@code null} falls es keine nicht freigegebenen Autoren-Aufträge zum {@link iPartsDialogBCTEPrimaryKey} gibt
     */
    public Set<String> getChangeSetIdsForPartListEntriesUsedInActiveChangeSets(iPartsDialogBCTEPrimaryKey primaryBCTEKey) {
        if (primaryBCTEKey != null) {
            if (isUsedInActiveChangeSets(primaryBCTEKey)) {
                // Map für Baureihe aus dem primaryBCTEKey ist nun auf jeden Fall vorhanden
                return partListEntryUsageInChangeSetMap.get(primaryBCTEKey.getHmMSmId().getSeries()).get(primaryBCTEKey.createDialogGUID());
            }
        }
        return null;
    }

    /**
     * Liefert die Liste der {@link EtkDataPartListEntry}s, die zum {@link iPartsDialogBCTEPrimaryKey} ohne Berücksichtigung
     * von der Ausführungsart adressiert im AfterSales sind (z.B. zum Vereinheitlichen von Daten zwischen diesen Stücklisteneinträgen).
     * Ist {@code withLoad == true}, dann werden die {@link EtkDataPartListEntry}s aus der DB geladen.
     *
     * @param primaryBCTEKey
     * @param withLoad
     * @param omitPSKEntries {@code true}, falls Stücklisteneinträge aus PSK-Modulen nicht geliefert werden sollen
     * @return
     */
    public List<EtkDataPartListEntry> retrievePartListEntriesUsedInAsForEqualize(iPartsDialogBCTEPrimaryKey primaryBCTEKey,
                                                                                 boolean withLoad, boolean omitPSKEntries) {
        PartListEntryReferenceKeyByAA refKey = new PartListEntryReferenceKeyByAA(primaryBCTEKey);
        if ((primaryBCTEKey != null) && (refKey.isValid())) {
            String sourceGuid = refKey.getBcteKeyWithoutAA().createDialogGUID();
            List<PartListEntryId> idList = partListEntryIdUsageInRetailMap.get(sourceGuid);
            if (idList == null) {
                String[] fields = new String[]{ iPartsConst.FIELD_K_VARI, iPartsConst.FIELD_K_VER, iPartsConst.FIELD_K_LFDNR };
                idList = new DwList<>();
                DBDataObjectAttributesList attributesList = EditConstructionToRetailHelper.getRetailSourceGuidAttributeList(iPartsEntrySourceType.DIALOG,
                                                                                                                            refKey.getSearchSQLBcteKeyWithoutAA(),
                                                                                                                            refKey.getBcteKeyWithoutAA().getHmMSmId().getDIALOGSourceContext(),
                                                                                                                            fields,
                                                                                                                            getProject());

                for (DBDataObjectAttributes attribute : attributesList) {
                    PartListEntryId entryId = new PartListEntryId(attribute.getFieldValue(iPartsConst.FIELD_K_VARI),
                                                                  attribute.getFieldValue(iPartsConst.FIELD_K_VER),
                                                                  attribute.getFieldValue(iPartsConst.FIELD_K_LFDNR));
                    idList.add(entryId);
                }
                partListEntryIdUsageInRetailMap.put(sourceGuid, idList);
            }
            return getPartListEntriesByIds(idList, withLoad, retailPartListEntryEqualizeMap, omitPSKEntries);
        }
        return new DwList<>();
    }

    /**
     * Liefert alle {@link EtkDataPartListEntry}s zum {@link iPartsDialogBCTEPrimaryKey} (ohne AA) der übergebenen
     * Position, die im Retail vorkommen und nicht zu PSK Stücklisten gehören.
     * <p>
     * Aktive Autorenaufträge werden NICHT berücksichtigt!
     *
     * @param partListEntry
     * @param withLoad
     * @return
     */
    public List<EtkDataPartListEntry> getPartListEntriesUsedInAsForEqualizeWithoutPSKEntries(EtkDataPartListEntry partListEntry,
                                                                                             boolean withLoad) {
        return retrievePartListEntriesUsedInAsForEqualize(iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry),
                                                          withLoad, true);
    }

    /**
     * Liefert die erste AS Stücklistenposition zum übergebenen BCTE Schlüssel mit einer Wildcard als AA
     *
     * @param referenceKeyByAA
     * @return
     */
    public EtkDataPartListEntry getFirstASPartListEntryForBCTEWithOutAA(PartListEntryReferenceKeyByAA referenceKeyByAA) {
        if (firstPartListEntriesMap == null) {
            firstPartListEntriesMap = new HashMap<>();
        }
        EtkDataPartListEntry partListEntry = firstPartListEntriesMap.get(referenceKeyByAA.getSearchSQLBcteKeyWithoutAA());
        // Zum BCTE Schlüssel gibt es noch keinen Treffer im Cache. Wenn der BCTE Schlüssel nicht als Key vorkommt,
        // dann wurde noch nicht nach dem BCTE Schlüssel gesucht
        // -> Suche in DB
        if ((partListEntry == null) && !firstPartListEntriesMap.containsKey(referenceKeyByAA.getSearchSQLBcteKeyWithoutAA())) {
            partListEntry = findFirstReferencePartListEntryForReferenceKey(getProject(), referenceKeyByAA);
            firstPartListEntriesMap.put(referenceKeyByAA.getSearchSQLBcteKeyWithoutAA(), partListEntry);
        }
        return partListEntry;
    }

    /**
     * Sucht den {@link EtkDataPartListEntry} Datensatz zum übergebenen <code>searchBCTEKey</code> in der KATALOG Tabelle.
     * Zuerst wird nach dem Datenstaz gesucht, der alle BCTE Werte hat wie im übergebenen <code>searchBCTEKey</code>. Falls
     * keine Datensatz mit identischen BCTE Schlüssel gefunden wird, wird nach einem Datensatz gesucht, der alle BCTE
     * Attribute gleich hat außer AA.
     *
     * @param searchBCTEKey
     * @return
     */
    public static EtkDataPartListEntry findFirstReferencePartListEntryForReferenceKey(EtkProject project, PartListEntryReferenceKeyByAA searchBCTEKey) {
        if (StrUtils.isValid(iPartsConst.TABLE_KATALOG, searchBCTEKey.getSearchSQLBcteKeyWithoutAA())) {
            iPartsDialogBCTEPrimaryKey bcteKeyWithoutAA = searchBCTEKey.getBcteKeyWithoutAA();
            if (bcteKeyWithoutAA == null) {
                return null;
            }
            String sourceContext = bcteKeyWithoutAA.getHmMSmId().getDIALOGSourceContext();
            String searchTerm = searchBCTEKey.getSearchSQLBcteKeyWithoutAA();

            final VarParam<EtkDataPartListEntry> firstEntry = new VarParam<>(null);
            // Alle Treffer durchgehen. Wird ein Datensatz gefunden mit identischen BCTE-Schlüssel, dann wird dieser zurückgeliefert.
            // Ansonsten wird nach Datensätze mich gleichen BCTE-Attributen und willkürlicher AA gesucht.

            EtkDataPartListEntryList partListEntryList = new EtkDataPartListEntryList();
            partListEntryList.searchSortAndFillWithJoin(project, project.getDBLanguage(), null, new String[]{ TableAndFieldName.make(iPartsConst.TABLE_KATALOG,
                                                                                                                                     iPartsConst.FIELD_K_SOURCE_TYPE),
                                                                                                              TableAndFieldName.make(iPartsConst.TABLE_KATALOG,
                                                                                                                                     iPartsConst.FIELD_K_SOURCE_CONTEXT),
                                                                                                              TableAndFieldName.make(iPartsConst.TABLE_KATALOG,
                                                                                                                                     iPartsConst.FIELD_K_SOURCE_GUID) },
                                                        new String[]{ iPartsEntrySourceType.DIALOG.getDbValue(), sourceContext, searchTerm },
                                                        false, null, false, null, false, true, false,
                                                        new EtkDataObjectList.FoundAttributesCallback() {
                        private boolean firstEntryWithAAFound;

                        @Override
                        public boolean foundAttributes(DBDataObjectAttributes attributes) {
                            if (firstEntryWithAAFound) {
                                // Suche komplett abbrechen geht nicht so einfach -> zumindest aber den Callback schnell
                                // beenden sobald wir einen optimalen Treffer mit AA haben
                                return false;
                            }

                            String aaFromDB = attributes.getFieldValue(iPartsConst.FIELD_K_AA);
                            if (aaFromDB.equals(searchBCTEKey.getOriginalAA())) {
                                firstEntry.setValue(EtkDataObjectFactory.createDataPartListEntry(project, attributes));
                                // Wir haben einen Datensatz gefunden, der einen identischen BCTE Schlüssel hat
                                firstEntryWithAAFound = true;
                            } else if (firstEntry.getValue() == null) {
                                firstEntry.setValue(EtkDataObjectFactory.createDataPartListEntry(project, attributes));
                            }

                            return false;
                        }
                    }, false);

            return firstEntry.getValue();
        }
        return null;
    }

    /**
     * Verwendung im EDS Retail
     * Sucht die {@param saaBkNo} in DWARRAY und die Stücklisten, die diese ArrayId verwenden.
     * Ist die {@param matNo} angegeben, so wird in Stücklisteneinträgen auch die Mat-Nummer verglichen
     * Liefert die Liste der Treffer als {@link PartListEntryId}s, sortiert nach K_VARI
     * Siehe DAIMLER-9406; (DAIMLER-9494: KG/TU-Vorschlag Prüfung 1)
     *
     * @param saaBkNo
     * @param matNo
     * @return
     */
    public Set<PartListEntryId> getEDSUsageSaaMatNoInASPartList(String saaBkNo, String matNo) {
        Set<PartListEntryId> result = new TreeSet<>();
        if (!StrUtils.isValid(saaBkNo)) {
            return result;
        }
        EtkDataObjectArrayList list = getPartEntriesFromSaaValidity(saaBkNo, matNo);

        for (EtkDataObjectArray dataObjectArray : list) {
            result.add(new PartListEntryId(dataObjectArray.getFieldValue(iPartsConst.FIELD_K_VARI),
                                           dataObjectArray.getFieldValue(iPartsConst.FIELD_K_VER),
                                           dataObjectArray.getFieldValue(iPartsConst.FIELD_K_LFDNR)));
        }
        return result;
    }

    /**
     * Verwendung im EDS Retail
     * Sucht die {@param saaBkNo} in DWARRAY und die Stücklisten, die diese ArrayId verwenden.
     * Ist die {@param matNo} angegeben, so wird in Stücklisteneinträgen auch die Mat-Nummer verglichen
     * Liefert die Liste der Treffer als Map, sortiert nach Product, KG/TU und Stücklisteneinträgen
     * Siehe DAIMLER-9406; (DAIMLER-9494: KG/TU-Vorschlag Prüfung 1)
     *
     * @param saaBkNo
     * @param matNo
     * @return
     */
    public Map<iPartsProductId, List<EDSUsageContainer>> getEDSUsageBySaaMatNoInASPartList(String saaBkNo, String matNo) {
        Set<PartListEntryId> asPartListEntryIdList = getEDSUsageSaaMatNoInASPartList(saaBkNo, matNo);
        return calcEDSUsageProductKgTu(asPartListEntryIdList);
    }

    /**
     * Verwendung im EDS Retail
     * Sucht die {@param saaBkNo} in DWARRAY und die Stücklisten, die diese ArrayId verwenden.
     * Ist die {@param productNo} angegeben, so wird überprüft, ob die Stücklisteneinträge im selben Produkt sind
     * Liefert die Liste der Treffer als {@link PartListEntryId}s, sortiert nach K_VARI
     * Siehe DAIMLER-9406; (DAIMLER-9495: KG/TU-Vorschlag Prüfung 2)
     *
     * @param saaBkNo
     * @param productNo
     * @return
     */
    public Set<PartListEntryId> getEDSUsageSaaProductInASPartList(String saaBkNo, String productNo) {
        List<iPartsProductId> productIdList = null;
        if (StrUtils.isValid(productNo)) {
            productIdList = new DwList<>();
            productIdList.add(new iPartsProductId(productNo));
        }
        return getEDSUsageSaaProductInASPartList(saaBkNo, productIdList);
    }

    /**
     * Verwendung im EDS Retail
     * Sucht die {@param saaBkNo} in DWARRAY und die Stücklisten, die diese ArrayId verwenden.
     * Ist die {@param productIdList} angegeben, so wird überprüft, ob die Stücklisteneinträge in einem
     * der übergebenen Produkte verwendet sind
     * Liefert die Liste der Treffer als {@link PartListEntryId}s, sortiert nach K_VARI
     * Siehe DAIMLER-9406; (DAIMLER-9495: KG/TU-Vorschlag Prüfung 2)
     *
     * @param saaBkNo
     * @param productIdList
     * @return
     */
    public Set<PartListEntryId> getEDSUsageSaaProductInASPartList(String saaBkNo, List<iPartsProductId> productIdList) {
        Set<PartListEntryId> result = new TreeSet<>();
        if (!StrUtils.isValid(saaBkNo)) {
            return result;
        }
        EtkDataObjectArrayList list = getPartEntriesFromSaaValidity(saaBkNo, null);

        Map<AssemblyId, Boolean> handledModules = new HashMap<>();
        boolean isSearchProductNoValid = (productIdList != null) && !productIdList.isEmpty();
        for (EtkDataObjectArray dataObjectArray : list) {
            PartListEntryId partListEntryId = new PartListEntryId(dataObjectArray.getFieldValue(iPartsConst.FIELD_K_VARI),
                                                                  dataObjectArray.getFieldValue(iPartsConst.FIELD_K_VER),
                                                                  dataObjectArray.getFieldValue(iPartsConst.FIELD_K_LFDNR));
            AssemblyId ownerAssemblyId = partListEntryId.getOwnerAssemblyId();
            if (isSearchProductNoValid) {
                if (!handledModules.containsKey(ownerAssemblyId)) {
                    boolean isRightProduct = false;
                    iPartsProductId productId = null;
                    if (useModulesEinPas) {
                        EDSUsageElem helper = getProductIdAndKgTuFromAssemblyId(ownerAssemblyId, false);
                        if (helper != null) {
                            productId = helper.getProductId();
                        }
                    } else {
                        productId = getProductIdFromAssemblyId(ownerAssemblyId);
                    }
                    if ((productId != null) && productIdList.contains(productId)) {
                        result.add(partListEntryId);
                        isRightProduct = true;
                    }
                    handledModules.put(ownerAssemblyId, isRightProduct);
                } else {
                    if (handledModules.get(ownerAssemblyId)) {
                        result.add(partListEntryId);
                    }
                }
            } else {
                result.add(partListEntryId);
            }
        }
        return result;
    }

    /**
     * Verwendung im EDS Retail
     * Sucht die {@param saaBkNo} in DWARRAY und die Stücklisten, die diese ArrayId verwenden.
     * Ist die {@param productNo} angegeben, so wird überprüft, ob die Stücklisteneinträge im selben Produkt sind
     * Liefert die Liste der Treffer als Map, sortiert nach Product, KG/TU und Stücklisteneinträgen
     * Siehe DAIMLER-9406; (DAIMLER-9495: KG/TU-Vorschlag Prüfung 2)
     *
     * @param saaBkNo
     * @param productNo
     * @return
     */
    public Map<iPartsProductId, List<EDSUsageContainer>> getEDSUsageBySaaProductInASPartList(String saaBkNo, String productNo) {
        Set<PartListEntryId> asPartListEntryIdList = getEDSUsageSaaProductInASPartList(saaBkNo, productNo);
        return calcEDSUsageProductKgTu(asPartListEntryIdList);
    }

    /**
     * Verwendung im EDS Retail
     * Sucht die {@param saaBkNo} in DWARRAY und die Stücklisten, die diese ArrayId verwenden.
     * Ist die {@param productIdList} angegeben, so wird überprüft, ob die Stücklisteneinträge in einem
     * der übergebenen Produkte verwendet sind
     * Liefert die Liste der Treffer als Map, sortiert nach Product, KG/TU und Stücklisteneinträgen
     * Siehe DAIMLER-9406; (DAIMLER-9495: KG/TU-Vorschlag Prüfung 2)
     *
     * @param saaBkNo
     * @param productIdList
     * @return
     */
    public Map<iPartsProductId, List<EDSUsageContainer>> getEDSUsageBySaaProductInASPartList(String saaBkNo, List<iPartsProductId> productIdList) {
        Set<PartListEntryId> asPartListEntryIdList = getEDSUsageSaaProductInASPartList(saaBkNo, productIdList);
        return calcEDSUsageProductKgTu(asPartListEntryIdList);
    }

    /**
     * Verwendung im EDS Retail
     * Überprüft die {@param saaBkNo}, ob es sich um eine SA-Nummer handelt und bildet diese gegebenenfalls.
     * Sucht Wildcard-mäßig (saNo + "*") in DWARRAY und die Stücklisten, die diese ArrayId verwenden.
     * Liefert die Liste der Treffer als {@link PartListEntryId}s, sortiert nach K_VARI
     * Siehe DAIMLER-9406; (DAIMLER-9496: KG/TU-Vorschlag Prüfung 3)
     *
     * @param saaBkNo
     * @return
     */
    public Set<PartListEntryId> getEDSUsageSaASPartList(String saaBkNo, boolean useSA) {
        Set<PartListEntryId> result = new TreeSet<>();
        if (!StrUtils.isValid(saaBkNo)) {
            return result;
        }
        String searchValue = saaBkNo;
        if (useSA) {
            String saNo = iPartsNumberHelper.convertSAAtoSANumber(saaBkNo);
            if (StrUtils.isValid(saNo)) {
                searchValue = saNo + "*";
            } else {
                return result;
            }
        } else {
            iPartsNumberHelper numberHelper = new iPartsNumberHelper();
            if (!numberHelper.isValidSaaOrBk(saaBkNo, false)) {
                return result;
            }
        }
        EtkDataObjectArrayList list = getPartEntriesFromSaaValidity(searchValue, null);

        for (EtkDataObjectArray dataObjectArray : list) {
            result.add(new PartListEntryId(dataObjectArray.getFieldValue(iPartsConst.FIELD_K_VARI),
                                           dataObjectArray.getFieldValue(iPartsConst.FIELD_K_VER),
                                           dataObjectArray.getFieldValue(iPartsConst.FIELD_K_LFDNR)));
        }
        return result;

    }

    /**
     * Verwendung im EDS Retail
     * Überprüft die {@param saaBkNo}, ob es sich um eine SA-Nummer handelt und bildet diese gegebenenfalls.
     * Sucht Wildcard-mäßig (saNo + "*") in DWARRAY und die Stücklisten, die diese ArrayId verwenden.
     * Liefert die Liste der Treffer als Map, sortiert nach Product, KG/TU und Stücklisteneinträgen
     * Siehe DAIMLER-9406; (DAIMLER-9496: KG/TU-Vorschlag Prüfung 3)
     *
     * @param saaBkNo
     * @return
     */
    public Map<iPartsProductId, List<EDSUsageContainer>> getEDSUsageBySaASPartList(String saaBkNo) {
        Set<PartListEntryId> asPartListEntryIdList = getEDSUsageSaASPartList(saaBkNo, true);
        return calcEDSUsageProductKgTu(asPartListEntryIdList);
    }

    /**
     * Verwendung im EDS Retail
     * Überprüft die {@param saaBkNo}, ob es sich um eine SAA-Nummer handelt und such mit dieser über DWARRAY die
     * Stücklisten, die diese ArrayId verwenden.
     * Liefert die Liste der Treffer als Map, sortiert nach Product, KG/TU und Stücklisteneinträgen.
     * Im Gegensatz zu {@link #getEDSUsageBySaASPartList(String)} wird hier die SAA im ganzen benutzt. Es findet keine
     * Umwandlung zur SA statt.
     *
     * @param saaBkNo
     * @return
     */
    public Map<iPartsProductId, List<EDSUsageContainer>> getEDSUsageBySaaASPartList(String saaBkNo) {
        Set<PartListEntryId> asPartListEntryIdList = getEDSUsageSaASPartList(saaBkNo, false);
        return calcEDSUsageProductKgTu(asPartListEntryIdList);
    }

    private EtkDataObjectArrayList getPartEntriesFromSaaValidity(String searchSaaBkValue, String searchMatNo) {
        EtkDataObjectArrayList list = new EtkDataObjectArrayList();
        list.setSearchWithoutActiveChangeSets(false);
        list.clear(DBActionOrigin.FROM_DB);
        if (!StrUtils.isValid(searchSaaBkValue)) {
            return list;
        }

        EtkDisplayFields selectFields = new EtkDisplayFields();
        EtkDisplayField selectField = new EtkDisplayField(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_VARI, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_VER, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_LFDNR, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_SOURCE_GUID, false, false);
        selectFields.addFeld(selectField);
        String[] whereTableAndFields;
        String[] whereValues;
        whereTableAndFields = new String[]{ TableAndFieldName.make(iPartsConst.TABLE_DWARRAY, iPartsConst.FIELD_DWA_FELD),
                                            TableAndFieldName.make(iPartsConst.TABLE_DWARRAY, iPartsConst.FIELD_DWA_TOKEN) };
        whereValues = new String[]{ TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_SA_VALIDITY),
                                    searchSaaBkValue };
        if (StrUtils.isValid(searchMatNo)) {
            whereTableAndFields = StrUtils.mergeArrays(whereTableAndFields, TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_MATNR));
            whereValues = StrUtils.mergeArrays(whereValues, searchMatNo);
        }

        list.searchSortAndFillWithJoin(getProject(), getProject().getDBLanguage(),
                                       selectFields,
                                       whereTableAndFields,
                                       whereValues,
                                       false,
                                       new String[]{ TableAndFieldName.make(iPartsConst.TABLE_KATALOG, iPartsConst.FIELD_K_VARI) },
                                       false, true,
                                       null,
                                       new EtkDataObjectList.JoinData(iPartsConst.TABLE_KATALOG,
                                                                      new String[]{ iPartsConst.FIELD_DWA_ARRAYID },
                                                                      new String[]{ iPartsConst.FIELD_K_SA_VALIDITY },
                                                                      false, false));
        return list;
    }


    private Map<iPartsProductId, List<EDSUsageContainer>> calcEDSUsageProductKgTu(Set<PartListEntryId> asPartListEntryIdList) {
        Map<iPartsProductId, List<EDSUsageContainer>> resultMap = new HashMap<>();
        Map<AssemblyId, EDSUsageElem> handledModules = new HashMap<>();
        for (PartListEntryId partListEntryId : asPartListEntryIdList) {
            AssemblyId ownerAssemblyId = partListEntryId.getOwnerAssemblyId();
            EDSUsageElem usageElem = handledModules.get(ownerAssemblyId);
            if (usageElem == null) {
                if (useModulesEinPas) {
                    EDSUsageElem helper = getProductIdAndKgTuFromAssemblyId(ownerAssemblyId, true);
                    if (helper != null) {
                        usageElem = new EDSUsageElem(helper.getProductId(), helper.getKgTuId(), partListEntryId);
                        handledModules.put(ownerAssemblyId, usageElem);
                    }
                } else {
                    List<MechanicUsagePosition> usageList = new ArrayList<>();
                    iPartsProductId productId = getProductIdAndUsageListFromAssemblyId(ownerAssemblyId, usageList);
                    if (productId != null) {
                        for (MechanicUsagePosition usagePosition : usageList) {
                            AssemblyId parentAssemblyId = usagePosition.getParentAssemblyId();
                            if (iPartsVirtualNode.isVirtualId(parentAssemblyId)) {
                                List<iPartsVirtualNode> virtualNodes = iPartsVirtualNode.parseVirtualIds(usagePosition.getParentAssemblyId());
                                if (iPartsVirtualNode.isProductKgTuNode(virtualNodes)) {
                                    // Produkt überprüfen
                                    iPartsProductId virtualProductId = (iPartsProductId)virtualNodes.get(0).getId();
                                    if (productId.getProductNumber().equals(virtualProductId.getProductNumber())) {
                                        // KG/TU bestimmen
                                        KgTuId kgTuId = (KgTuId)virtualNodes.get(1).getId();
                                        usageElem = new EDSUsageElem(productId, kgTuId, partListEntryId);
                                        handledModules.put(ownerAssemblyId, usageElem);
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                usageElem.addPartListEntryId(partListEntryId);
            }
        }
        // Ergebnisse umsortieren
        for (EDSUsageElem usageElem : handledModules.values()) {
            List<EDSUsageContainer> usageContainerList = resultMap.get(usageElem.getProductId());
            if (usageContainerList == null) {
                usageContainerList = new DwList<>();
                resultMap.put(usageElem.getProductId(), usageContainerList);
            }
            if (usageContainerList.isEmpty()) {
                usageContainerList.add(new EDSUsageContainer(usageElem.getKgTuId(), usageElem.getPartListEntryIdList()));
            } else {
                EDSUsageContainer currentUsageContainer = null;
                for (EDSUsageContainer usageContainer : usageContainerList) {
                    if (usageContainer.getKgTuId().equals(usageElem.getKgTuId())) {
                        currentUsageContainer = usageContainer;
                        break;
                    }
                }
                if (currentUsageContainer == null) {
                    usageContainerList.add(new EDSUsageContainer(usageElem.getKgTuId(), usageElem.getPartListEntryIdList()));
                } else {
                    currentUsageContainer.addPartListEntryIds(usageElem.getPartListEntryIdList());
                }
            }
        }
        return resultMap;
    }

    private iPartsProductId getProductIdFromAssemblyId(AssemblyId assemblyId) {
        return getProductIdAndUsageListFromAssemblyId(assemblyId, null);
    }

    private iPartsProductId getProductIdAndUsageListFromAssemblyId(AssemblyId assemblyId, List<MechanicUsagePosition> usageList) {
        EtkDataAssembly etkAssembly = EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId);
        if (etkAssembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly assembly = (iPartsDataAssembly)etkAssembly;
            iPartsProductId productId = assembly.getProductIdFromModuleUsage();
            if (usageList != null) {
                List<MechanicUsagePosition> currentUsageList = assembly.getMechanicUsage(false, false);
                usageList.addAll(currentUsageList);
            }
            return productId;
        }
        return null;
    }

    private EDSUsageElem getProductIdAndKgTuFromAssemblyId(AssemblyId assemblyId, boolean withKgTu) {
        EtkDataAssembly etkAssembly = EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId);
        if (etkAssembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly assembly = (iPartsDataAssembly)etkAssembly;
            if (assembly.isSAAssembly()) {
                iPartsModuleId moduleId = new iPartsModuleId(assembly.getAsId().getKVari());
                iPartsDataSAModulesList saModules = iPartsDataSAModulesList.loadDataForModule(getProject(), moduleId);
                for (iPartsDataSAModules saModule : saModules) {
                    iPartsSAId saId = new iPartsSAId(saModule.getAsId().getSaNumber());
                    Map<iPartsProductId, Set<String>> productIdsToKGsMap = iPartsSA.getInstance(project, saId).getProductIdsToKGsMap(project);
                    for (Map.Entry<iPartsProductId, Set<String>> productIdToKGsEntry : productIdsToKGsMap.entrySet()) {
                        // todo? es wird nur der erste Eintrag genommen, vielleicht sollten hier alle Einträge des SA-TU rein?
                        iPartsProductId productId = productIdToKGsEntry.getKey();
                        if (productId.isValidId()) {
                            KgTuId kgTuId = null;
                            if (withKgTu) {
                                String kg = productIdToKGsEntry.getValue().iterator().next();
                                if (StrUtils.isValid(kg)) {
                                    kgTuId = new KgTuId(kg, UNKNOWN_TU);
                                } else {
                                    kgTuId = new KgTuId(UNKNOWN_KG, UNKNOWN_TU);
                                }
                            }
                            return new EDSUsageElem(productId, kgTuId);
                        }
                    }
                }
            } else {
                iPartsProductId productId = assembly.getProductIdFromModuleUsage();
                if (productId != null) {
                    if (withKgTu) {
                        iPartsDataModuleEinPASList moduleEinPASList = iPartsDataModuleEinPASList.loadForProductAndModule(getProject(),
                                                                                                                         productId,
                                                                                                                         assemblyId);
                        if (!moduleEinPASList.isEmpty()) {
                            iPartsDataModuleEinPAS dataModuleEinPAS = moduleEinPASList.get(0);
                            return new EDSUsageElem(productId,
                                                    new KgTuId(dataModuleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_KG),
                                                               dataModuleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_TU)));
                        }
                    } else {
                        return new EDSUsageElem(productId, null);
                    }
                }
            }
        }
        return null;
    }

    public static class EDSUsageContainer {

        private KgTuId kgTuId;
        private List<PartListEntryId> partListEntryIdList;

        public EDSUsageContainer(KgTuId kgTuId) {
            this.kgTuId = kgTuId;
            this.partListEntryIdList = new DwList<>();
        }

        public EDSUsageContainer(KgTuId kgTuId, PartListEntryId partListEntryId) {
            this(kgTuId);
            addPartListEntryId(partListEntryId);
        }

        public EDSUsageContainer(KgTuId kgTuId, List<PartListEntryId> partListEntryIdList) {
            this(kgTuId);
            addPartListEntryIds(partListEntryIdList);
        }

        public void addPartListEntryIds(List<PartListEntryId> partListEntryIdList) {
            this.partListEntryIdList.addAll(partListEntryIdList);
        }

        public void addPartListEntryId(PartListEntryId partListEntryId) {
            partListEntryIdList.add(partListEntryId);
        }

        public KgTuId getKgTuId() {
            return kgTuId;
        }

        public List<PartListEntryId> getPartListEntryIdList() {
            return partListEntryIdList;
        }

        public AssemblyId getFirstAssemblyId() {
            if (!getPartListEntryIdList().isEmpty()) {
                return getPartListEntryIdList().get(0).getOwnerAssemblyId();
            }
            return null;
        }

        public String getFirstModuleNumber() {
            AssemblyId assemblyId = getFirstAssemblyId();
            if (assemblyId != null) {
                return assemblyId.getKVari();
            }
            return "";
        }
    }

    private static class EDSUsageElem {

        private iPartsProductId productId;
        private KgTuId kgTuId;
        private List<PartListEntryId> partListEntryIdList;

        public EDSUsageElem(iPartsProductId productId, KgTuId kgTuId) {
            this.productId = productId;
            this.kgTuId = kgTuId;
            this.partListEntryIdList = new DwList<>();
        }

        public EDSUsageElem(iPartsProductId productId, KgTuId kgTuId, PartListEntryId partListEntryId) {
            this(productId, kgTuId);
            this.partListEntryIdList.add(partListEntryId);
        }

        public void addPartListEntryId(PartListEntryId partListEntryId) {
            partListEntryIdList.add(partListEntryId);
        }

        public iPartsProductId getProductId() {
            return productId;
        }

        public KgTuId getKgTuId() {
            return kgTuId;
        }

        public List<PartListEntryId> getPartListEntryIdList() {
            return partListEntryIdList;
        }
    }
}
