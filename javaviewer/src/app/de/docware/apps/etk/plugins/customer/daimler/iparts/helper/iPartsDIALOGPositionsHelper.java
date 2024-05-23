/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.util.collections.dwlist.DwList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hilfsklasse zum Bestimmen von DIALOG-Positionen und Untermengen davon (z.B alle Positionvarianten zu einer Position)
 * zu einer DIALOG Konstruktionsstückliste.
 */
public class iPartsDIALOGPositionsHelper {

    private Map<iPartsDialogBCTEPrimaryKey, List<EtkDataPartListEntry>> positionBCTEKeyToPartListEntriesMap;

    public iPartsDIALOGPositionsHelper(DBDataObjectList<EtkDataPartListEntry> destPartList) {
        initPositionVariants(destPartList);
    }

    /**
     * Liefert alle Positionsvarianten der DIALOG-Position des Stücklisteneintrags ohne Einschränkung
     *
     * @param partListEntry
     * @param removeOwnEntry
     * @return
     */
    public List<EtkDataPartListEntry> getAllPositionVariants(EtkDataPartListEntry partListEntry, boolean removeOwnEntry) {
        return getPositionPartListEntries(partListEntry, removeOwnEntry, false, false, false, false, false);
    }

    /**
     * Liefert alle Positionsvarianten der DIALOG-Position des Stücklisteneintrags mit Einschränkung auf dessen AA
     *
     * @param partListEntry
     * @param removeOwnEntry
     * @return
     */
    public List<EtkDataPartListEntry> getPositionVariantsWithAACheck(EtkDataPartListEntry partListEntry, boolean removeOwnEntry) {
        return getPositionPartListEntries(partListEntry, removeOwnEntry, false, false, false, true, false);
    }

    /**
     * Liefert alle Positionsvarianten der DIALOG-Position des Stücklisteneintrags mit Einschränkung auf dessen AA und PV
     *
     * @param partListEntry
     * @param removeOwnEntry
     * @return
     */
    public List<EtkDataPartListEntry> getPositionVariantsWithPVAndAACheck(EtkDataPartListEntry partListEntry, boolean removeOwnEntry) {
        return getPositionPartListEntries(partListEntry, removeOwnEntry, true, false, false, true, false);
    }

    /**
     * Liefert alle KEM-Stände des übergebenen Stücklisteneintrags
     *
     * @param partListEntry
     * @param removeOwnEntry
     * @return
     */
    public List<EtkDataPartListEntry> getPartListEntriesForAllKEMs(EtkDataPartListEntry partListEntry, boolean removeOwnEntry) {
        return getPositionPartListEntries(partListEntry, removeOwnEntry, true, true, true, true, false);
    }

    /**
     * Erzeugt die Positionsvarianten Gruppen
     *
     * @param destPartList
     */
    private void initPositionVariants(DBDataObjectList<EtkDataPartListEntry> destPartList) {
        if (positionBCTEKeyToPartListEntriesMap == null) {
            positionBCTEKeyToPartListEntriesMap = new HashMap<>();
            if (destPartList != null) {
                for (EtkDataPartListEntry partListEntry : destPartList) {
                    iPartsDialogBCTEPrimaryKey positionBCTEKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
                    if (positionBCTEKey == null) {
                        continue;
                    }
                    positionBCTEKey = positionBCTEKey.getPositionBCTEPrimaryKey();
                    List<EtkDataPartListEntry> posVariantenList = positionBCTEKeyToPartListEntriesMap.get(positionBCTEKey);
                    if (posVariantenList == null) {
                        posVariantenList = new DwList<>();
                        positionBCTEKeyToPartListEntriesMap.put(positionBCTEKey, posVariantenList);
                    }
                    posVariantenList.add(partListEntry);
                }
            }
        }
    }

    /**
     * Liefert alle Stücklisteneinträge der DIALOG-Position des Stücklisteneintrags. Wenn removeOwnEntry {@code true}
     * ist und keine anderen Stücklisteneinträge zur Position vorhanden sind wird eine leere Liste geliefert.
     *
     * @param partListEntry
     * @param removeOwnEntry {@code true} wenn der Stücklisteneintrag selbst nicht enthalten sein soll
     * @param checkPosV      Schränkt die Stücklisteneinträge der DIALOG-Position auf die Positionsvariante
     *                       des übergebenen Stücklisteneintrags ein
     * @param checkWW        Schränkt auf das WW des übergebenen Stücklisteneintrags ein
     * @param checkET        Schränkt auf das ET des übergebenen Stücklisteneintrags ein
     * @param checkAA        Schränkt auf das AA des übergebenen Stücklisteneintrags ein
     * @param checkSDATA     Schränkt auf das SDATA des übergebenen Stücklisteneintrags ein
     * @return
     */
    private List<EtkDataPartListEntry> getPositionPartListEntries(EtkDataPartListEntry partListEntry, boolean removeOwnEntry,
                                                                  boolean checkPosV, boolean checkWW, boolean checkET,
                                                                  boolean checkAA, boolean checkSDATA) {
        if ((partListEntry == null) || (positionBCTEKeyToPartListEntriesMap == null)) {
            return null;
        }
        List<EtkDataPartListEntry> result = new DwList<>();
        iPartsDialogBCTEPrimaryKey fullBCTEKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(partListEntry);
        if (fullBCTEKey == null) {
            return null;
        }
        iPartsDialogBCTEPrimaryKey positionBCTEKey = fullBCTEKey.getPositionBCTEPrimaryKey();
        List<EtkDataPartListEntry> posVariantsList = positionBCTEKeyToPartListEntriesMap.get(positionBCTEKey);
        if ((posVariantsList != null) && !posVariantsList.isEmpty()) {
            for (EtkDataPartListEntry positionVariant : posVariantsList) {
                if (removeOwnEntry && positionVariant.getAsId().equals(partListEntry.getAsId())) {
                    continue;
                }
                iPartsDialogBCTEPrimaryKey positionVariantBCTEKey = iPartsDialogBCTEPrimaryKey.createFromDIALOGPartListEntry(positionVariant);
                if (positionVariantBCTEKey == null) {
                    continue;
                }
                if (compare(fullBCTEKey, positionVariantBCTEKey, checkPosV, checkWW, checkET, checkAA, checkSDATA)) {
                    result.add(positionVariant);
                }
            }
        }
        return result;
    }

    /**
     * Liefert den DIALOG-Stücklisteneintrag für den übergebenen BCTE-Schlüssel zurück
     *
     * @param bctePrimaryKey
     * @return {@code null} falls kein DIALOG-Stücklisteneintrag für den übergebenen BCTE-Schlüssel gefunden werden konnte
     */
    public EtkDataPartListEntry getPositionVariantByBCTEKey(iPartsDialogBCTEPrimaryKey bctePrimaryKey) {
        if ((bctePrimaryKey != null) && (positionBCTEKeyToPartListEntriesMap != null)) {
            List<EtkDataPartListEntry> posVariantsList = positionBCTEKeyToPartListEntriesMap.get(bctePrimaryKey.getPositionBCTEPrimaryKey());
            if (posVariantsList != null) {
                String dialogGUID = bctePrimaryKey.createDialogGUID();
                for (EtkDataPartListEntry partListEntry : posVariantsList) {
                    if (partListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GUID).equals(dialogGUID)) {
                        return partListEntry;
                    }
                }
            }
        }

        return null;
    }

    public boolean compare(iPartsDialogBCTEPrimaryKey compareKey1, iPartsDialogBCTEPrimaryKey compareKey2,
                           boolean checkPosV, boolean checkWW, boolean checkET,
                           boolean checkAA, boolean checkSDATA) {
        boolean samePosition = compareKey1.getHmMSmId().equals(compareKey2.getHmMSmId()) &&
                               compareKey1.getPosE().equals(compareKey2.getPosE());
        if (!samePosition) {
            return false;
        }
        if (checkPosV && !compareKey1.posV.equals(compareKey2.posV)) {
            return false;
        }
        if (checkWW && !compareKey1.ww.equals(compareKey2.ww)) {
            return false;
        }
        if (checkET && !compareKey1.et.equals(compareKey2.et)) {
            return false;
        }
        if (checkAA && !compareKey1.aa.equals(compareKey2.aa)) {
            return false;
        }
        if (checkSDATA && !compareKey1.sData.equals(compareKey2.sData)) {
            return false;
        }
        return true;
    }


}
