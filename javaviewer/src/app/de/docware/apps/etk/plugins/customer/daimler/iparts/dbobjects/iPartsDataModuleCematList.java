/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.*;

/**
 * Liste von {@link iPartsDataModuleCemat}s für EinPAS-Knoten aus CEMaT.
 */
public class iPartsDataModuleCematList extends EtkDataObjectList<iPartsDataModuleCemat> implements iPartsConst {

    public static final int VERSIONS_LIMIT = 3;

    public iPartsDataModuleCematList() {
        setSearchWithoutActiveChangeSets(true);
    }

    /**
     * Liefert eine Map der Versionen zu einer {@link EinPasId} (umgekehrt sortiert)
     * Voraussetzung: jeder EinPAS-Knoten beginnt mit Version 001
     * Ist {@code limitationLength} > 0, so enthält die Map nur {@code limitationLength} Einträge
     * Berücksichtigt werden nur Einträge, bei denen die Teilenummer gleich der {@code currentPartNo} ist,
     * falls {@code currentPartNo} besetzt ist.
     * (Speziell für die WebServices designed)
     *
     * @param list
     * @param currentPartNo
     * @param limitationLength
     * @return
     */
    public static Map<EinPasId, List<String>> buildCematEinPasVersionsMap(List<iPartsDataModuleCemat> list, String currentPartNo,
                                                                          int limitationLength) {
        Map<EinPasId, List<String>> cematEinPasVersionMap = new TreeMap<>();
        if ((list == null) || list.isEmpty()) {
            return cematEinPasVersionMap;
        }
        Set<String> cematVersionsForEinPasId = new TreeSet<>((o1, o2) -> -o1.compareTo(o2)); // Absteigend sortieren
        boolean checkPartNo = StrUtils.isValid(currentPartNo);
        for (iPartsDataModuleCemat dataModuleCemat : list) {
            cematVersionsForEinPasId.clear();
            if (checkPartNo && !dataModuleCemat.getPartNo().equals(currentPartNo)) {
                // Teilenummer passt nicht mehr
                continue;
            }

            String versionSet = dataModuleCemat.getFieldValue(FIELD_DMC_VERSIONS);
            cematVersionsForEinPasId.addAll(StrUtils.toStringList(versionSet, CEMAT_VERSION_DB_DELIMITER, false));

            EinPasId cematEinPasId = dataModuleCemat.getAsId().getEinPasId();
            for (String version : cematVersionsForEinPasId) {
                List<String> cematEinPasVersions = cematEinPasVersionMap.computeIfAbsent(cematEinPasId, einPasId -> new ArrayList<>());
                cematEinPasVersions.add(version);
                if ((limitationLength > 0) && (cematEinPasVersions.size() >= limitationLength)) {
                    break;
                }
            }
        }
        return cematEinPasVersionMap;
    }

    public static String buildEinPasVersionCematString(Map<PartListEntryId, List<iPartsDataModuleCemat>> cematMapForModule,
                                                       EtkDataPartListEntry partListEntry) {
        return buildEinPasVersionCematString(cematMapForModule.get(partListEntry.getAsId()), partListEntry.getPart().getAsId().getMatNr());
    }

    /**
     * Liefert den Wert für das virtuelle Feld der Cemat EinPAS-Knoten einer Teileposition.
     * Voraussetzung: jeder EinPAS-Knoten beginnt mit Version 001
     * Ist {@code limitationLength} > 0, so enthält die Map nur {@code limitationLength} Einträge
     * Berücksichtigt werden nur Einträge, bei denen die Teilenummer gleich der {@code currentPartNo} ist,
     * falls {@code currentPartNo} besetzt ist.
     *
     * @param list
     * @param currentPartNo
     * @return
     */
    public static String buildEinPasVersionCematString(List<iPartsDataModuleCemat> list, String currentPartNo) {
        if ((list == null) || list.isEmpty()) {
            return "";
        }

        Map<EinPasId, List<String>> cematEinPasVersionMap = buildCematEinPasVersionsMap(list, currentPartNo, VERSIONS_LIMIT);
        if (cematEinPasVersionMap.isEmpty()) {
            return "";
        }

        StringBuilder str = new StringBuilder();
        for (Map.Entry<EinPasId, List<String>> resultEntry : cematEinPasVersionMap.entrySet()) {
            if (str.length() > 0) {
                str.append("; ");
            }
            str.append(resultEntry.getKey().toString("/"));
            str.append(" (");
            str.append(StrUtils.stringListToString(resultEntry.getValue(), ", "));
            str.append(")");
        }
        return str.toString();
    }

    /**
     * Liefert eine Map der Versionen mit EinpasId (umgekehrt sortiert)
     * Ist limitationLength > 0, so enthält die Map nur limitationLength Einträge
     * Berücksichtigt werden nur Einträge, bei denen die PartNo gleich der currentPartNo ist,
     * falls currentPartNo besetzt ist.
     * (Speziell für die WebServices designed)
     *
     * @param list
     * @param currentPartNo
     * @param limitationLength
     * @return
     */
    public static Map<String, EinPasId> buildCematVersionEinPasMap(List<iPartsDataModuleCemat> list, String currentPartNo,
                                                                   int limitationLength) {
        Map<String, EinPasId> cematVersionEinPasMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return -o1.compareTo(o2);
            }
        });
        if ((list == null) || list.isEmpty()) {
            return cematVersionEinPasMap;
        }
        boolean checkPartNo = StrUtils.isValid(currentPartNo);
        for (iPartsDataModuleCemat dataModuleCemat : list) {
            if (checkPartNo && !dataModuleCemat.getPartNo().equals(currentPartNo)) {
                continue;
            }
            EinPasId cematEinPasId = dataModuleCemat.getAsId().getEinPasId();
            String versionSet = dataModuleCemat.getFieldValue(FIELD_DMC_VERSIONS);
            List<String> versionList = StrUtils.toStringList(versionSet, CEMAT_VERSION_DB_DELIMITER, true);
            for (String version : versionList) {
                cematVersionEinPasMap.put(version, cematEinPasId);
            }
        }
        if (limitationLength <= 0) {
            return cematVersionEinPasMap;
        }
        Map<String, EinPasId> resultCematVersionEinPasMap = new LinkedHashMap<>();
        int lfdNr = 0;
        for (Map.Entry<String, EinPasId> entry : cematVersionEinPasMap.entrySet()) {
            resultCematVersionEinPasMap.put(entry.getKey(), entry.getValue());
            lfdNr++;
            if (lfdNr >= limitationLength) {
                break;
            }
        }
        return resultCematVersionEinPasMap;
    }

    /**
     * Liefert den Wert für das virtuelle Feld einer TeilePosition
     * Ist limitationLength > 0, so enthält die Map nur limitationLength Einträge
     * Berücksichtigt werden nur Einträge, bei denen die PartNo gleich der currentPartNo ist,
     * falls currentPartNo besetzt ist.
     *
     * @param cematMapForModule
     * @param partListEntry
     * @return
     */
    public static String buildEinPasCematString(Map<PartListEntryId, List<iPartsDataModuleCemat>> cematMapForModule, EtkDataPartListEntry partListEntry) {
        return buildEinPasCematString(cematMapForModule.get(partListEntry.getAsId()), partListEntry.getPart().getAsId().getMatNr());
    }

    /**
     * Liefert den Wert für das virtuelle Feld einer TeilePosition
     * Ist limitationLength > 0, so enthält die Map nur limitationLength Einträge
     * Berücksichtigt werden nur Einträge, bei denen die PartNo gleich der currentPartNo ist,
     * falls currentPartNo besetzt ist.
     *
     * @param list
     * @param currentPartNo
     * @return
     */
    public static String buildEinPasCematString(List<iPartsDataModuleCemat> list, String currentPartNo) {
        if ((list == null) || list.isEmpty()) {
            return "";
        }
        Map<String, EinPasId> cematVersionEinPasMap = buildCematVersionEinPasMap(list, currentPartNo, 3);
        if (cematVersionEinPasMap.isEmpty()) {
            return "";
        }
        Map<EinPasId, List<String>> resultMap = new LinkedHashMap<>();
        for (Map.Entry<String, EinPasId> entry : cematVersionEinPasMap.entrySet()) {
            resultMap.putIfAbsent(entry.getValue(), new DwList<>());
            resultMap.get(entry.getValue()).add(entry.getKey());
        }

        StringBuilder str = new StringBuilder();
        for (Map.Entry<EinPasId, List<String>> resultEntry : resultMap.entrySet()) {
            if (str.length() > 0) {
                str.append("; ");
            }
            str.append(resultEntry.getKey().toString("/"));
            str.append(" (");
            str.append(StrUtils.stringListToString(resultEntry.getValue(), ", "));
            str.append(")");
        }
        return str.toString();
    }

    /**
     * Lädt alle Einträge aus Cemat zu einem Module (assemblyId),
     * Liefert eine Map geordnet nach TeilePositionen (PartListEntryId)
     *
     * @param project
     * @param assemblyId
     * @return
     */
    public static Map<PartListEntryId, List<iPartsDataModuleCemat>> loadCematMapForModule(EtkProject project, AssemblyId assemblyId) {
        iPartsDataModuleCematList list = loadCematModuleForModule(project, assemblyId);
        Map<PartListEntryId, List<iPartsDataModuleCemat>> resultMap = new HashMap<>();
        for (iPartsDataModuleCemat dataModuleCemat : list) {
            PartListEntryId partListEntryId = dataModuleCemat.getAsId().getPartListEntryId();
            resultMap.putIfAbsent(partListEntryId, new DwList<>());
            resultMap.get(partListEntryId).add(dataModuleCemat);
        }
        return resultMap;
    }

    /**
     * Lädt alle Einträge aus Cemat zu einem Module (assemblyId)
     *
     * @param project
     * @param assemblyId
     * @return
     */
    public static iPartsDataModuleCematList loadCematModuleForModule(EtkProject project, AssemblyId assemblyId) {
        iPartsDataModuleCematList list = new iPartsDataModuleCematList();
        list.loadCematModuleForModuleFromDB(project, assemblyId.getKVari(), DBActionOrigin.FROM_DB);
        return list;
    }

    /**
     * Lädt alle Einträge aus Cemat zu einem Produkt.
     *
     * @param project
     * @param productId
     * @return
     */
    public static iPartsDataModuleCematList loadCematModuleForProduct(EtkProject project, iPartsProductId productId) {
        iPartsDataModuleCematList list = new iPartsDataModuleCematList();
        list.loadCematModuleForProductFromDB(project, productId, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadCematModuleForProductFromDB(EtkProject project, iPartsProductId productId, DBActionOrigin origin) {
        clear(origin);
        String modulePattern = productId.getProductNumber() + "_*";
        searchWithWildCardsSortAndFill(project, new String[]{ FIELD_DMC_MODULE_NO }, new String[]{ modulePattern }, null,
                                       LoadType.COMPLETE, DBActionOrigin.FROM_DB);
    }

    public static iPartsDataModuleCematList loadAllCematModule(EtkProject project) {
        iPartsDataModuleCematList list = new iPartsDataModuleCematList();
        list.loadAllCematModuleFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadCematModuleForModuleFromDB(EtkProject project, String moduleNo, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_MODULE_CEMAT, new String[]{ FIELD_DMC_MODULE_NO }, new String[]{ moduleNo }, LoadType.COMPLETE, origin);
    }

    /**
     * Lädt eine Liste aller {@link iPartsDataModuleCemat}s aus der DB.
     *
     * @param project
     * @param origin
     */
    private void loadAllCematModuleFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_MODULE_CEMAT, null, null, LoadType.COMPLETE, origin);
    }

/*
    String FIELD_DMC_MODULE_NO = "DMC_MODULE_NO";                          // Modulnummer, KATALOG.K_VARI
    String FIELD_DMC_LFDNR = "DMC_LFDNR";                                  // Laufende Nummer, KATALOG.LFDNR
    String FIELD_DMC_PARTNO = "DMC_PARTNO";                                // Teilenummer
    String FIELD_DMC_EINPAS_HG = "DMC_EINPAS_HG";                          // Hauptgruppe, EINPAS-HG
    String FIELD_DMC_EINPAS_G = "DMC_EINPAS_G";                            // Gruppe, EINPAS-G
    String FIELD_DMC_EINPAS_TU = "DMC_EINPAS_TU";                          // Technischer Umfang, EINPAS-TU
    String FIELD_DMC_VERSIONS = "DMC_VERSIONS";                            // Versionen
 */

    @Override
    protected iPartsDataModuleCemat getNewDataObject(EtkProject project) {
        return new iPartsDataModuleCemat(project, null);
    }
}
