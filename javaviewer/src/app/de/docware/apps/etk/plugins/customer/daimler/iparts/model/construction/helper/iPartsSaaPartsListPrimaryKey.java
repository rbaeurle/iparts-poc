/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper;

import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsBOMConstKitContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataBOMConstKitContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsVirtualNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.iPartsConstructionPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.ops.OpsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.helper.iPartsEdsStructureHelper;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.StrUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Einfache Klasse, die alle Felder eines EDS-Konstruktionsdatensatzes für SAA/BK enthält.
 * Aus dem {@link de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry} werden die Daten im Konstruktor ermittelt.
 */
public class iPartsSaaPartsListPrimaryKey extends iPartsConstructionPrimaryKey {

    public static iPartsSaaPartsListPrimaryKey createFromSaaPartsListGuid(String edsGuid) {
        if (StrUtils.isValid(edsGuid)) {
            String[] tokens = StrUtils.toStringArray(edsGuid, GUID_DELIMITER, true);
            if (tokens.length >= 4) {
                if (tokens.length >= 5) { // Format aus der Konstruktions-Stückliste
                    String saaBkNo = StrUtils.stringUpToCharacter(tokens[0], "&");
                    String kemFrom = StrUtils.stringUpToCharacter(tokens[3], "&");
                    return new iPartsSaaPartsListPrimaryKey(saaBkNo, tokens[1], tokens[2], kemFrom);
                } else { // Format aus der AS-Stückliste
                    return new iPartsSaaPartsListPrimaryKey(tokens[0], tokens[1], tokens[2], tokens[3]);
                }
            }
        }
        return null;
    }

    /**
     * Ermittelt aus einem {@link EtkDataPartListEntry} den Primärschlüssel des EDS-Konstruktionsdatensatzes.
     *
     * @param entry
     */
    public static iPartsSaaPartsListPrimaryKey createFromEDSPartListEntry(EtkDataPartListEntry entry) {
        // beginne mit Konstruktion
        boolean isConstructionPartListEntry = false;
        EtkDataAssembly assembly = entry.getOwnerAssembly();
        if (assembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly iPartsAssembly = (iPartsDataAssembly)assembly;
            List<iPartsVirtualNode> virtualNodesPath = iPartsAssembly.getVirtualNodesPath();
            if (virtualNodesPath != null) {
                if (iPartsEdsStructureHelper.isEdsStructureSaaNode(virtualNodesPath)
                    || iPartsVirtualNode.isCTTSaaConstNode(virtualNodesPath)) {
                    isConstructionPartListEntry = true;
                }
            }
        }
        String saaPartsListGUID;
        if (isConstructionPartListEntry) {
            // Key aus Konstruktion lesen
            saaPartsListGUID = entry.getAsId().getKLfdnr();
        } else {
            // versuche AfterSales
            saaPartsListGUID = entry.getFieldValue(FIELD_K_SOURCE_GUID);
        }
        return createFromSaaPartsListGuid(saaPartsListGUID);
    }

    public static String buildSaaPartsList_GUID(String saaBkNo, String pos, String revFrom, String kemFrom) {
        iPartsSaaPartsListPrimaryKey edsPrimKey = new iPartsSaaPartsListPrimaryKey(saaBkNo, pos, revFrom, kemFrom);
        return edsPrimKey.createSaaPartsListGUID();
    }

    /**
     * Erzeugt eine KlfdNr für die EDS/BCS Konstruktionsstückliste auf Basis der Konstruktionsattribute und des
     * Baukastenpfades
     *
     * @param constKitAttributes
     * @param constKitPath
     * @return
     */
    public static String buildSaaPartsListGUIDForPartListSeqNo(DBDataObjectAttributes constKitAttributes, String constKitPath) {
        iPartsSaaPartsListPrimaryKey edsPrimKey = new iPartsSaaPartsListPrimaryKey(constKitAttributes, constKitPath);
        return edsPrimKey.createSaaPartsListGUID();
    }

    public static String buildSaaPartsList_GUID(iPartsDataBOMConstKitContent dataBOMConstKitContent) {
        iPartsSaaPartsListPrimaryKey edsPrimKey = new iPartsSaaPartsListPrimaryKey(dataBOMConstKitContent);
        return edsPrimKey.createSaaPartsListGUID();
    }

    /**
     * Compare Funktion zur Sortierung bei Übernahme in AS
     * Sortierkriterien: pos, revFrom, kemFrom (aufsteigend)
     *
     * @param o1
     * @param o2
     * @return
     */
    public static int compareForTransferList(iPartsSaaPartsListPrimaryKey o1, iPartsSaaPartsListPrimaryKey o2) {
        if ((o1 == null) && (o2 == null)) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        if (o1.type != o2.type) {
            return -1;
        }

        int result = o1.pos.compareTo(o2.pos);
        if (result == 0) {
            result = o1.revFrom.compareTo(o2.revFrom);
            if (result == 0) {
                result = o1.kemFrom.compareTo(o2.kemFrom);
            }
        }
        return result;
    }


    // Model
    iPartsModelId modelId; // i.A. optional
    // OPS-Knoten
    OpsId opsId;           // i.A. optional
    // SAA/BK Nummer
    protected String saaBkNo;
    // Positionsbezeichnung
    protected String pos;
    // Änderungsstand von
    protected String revFrom;
    // KEM von
    protected String kemFrom;
    // Für die Konstruktionsstückliste muss die laufende Nummer den kompletten Baukastenpfad enthalten
    protected String constKitPath;

    public iPartsSaaPartsListPrimaryKey(String saaBkNo, String pos, String revFrom, String kemFrom) {
        this.type = Type.SAA_PARTSLIST;
        this.saaBkNo = saaBkNo;
        this.pos = pos;
        this.revFrom = revFrom;
        this.kemFrom = kemFrom;
        this.modelId = null;
        this.opsId = null;
    }

    public iPartsSaaPartsListPrimaryKey(DBDataObjectAttributes constKitAttributes, String constKitPath) {
        this(constKitAttributes.getField(FIELD_DCK_SNR).getAsString(),
             constKitAttributes.getField(FIELD_DCK_PARTPOS).getAsString(),
             constKitAttributes.getField(FIELD_DCK_REVFROM).getAsString(),
             constKitAttributes.getField(FIELD_DCK_KEMFROM).getAsString());
        this.constKitPath = constKitPath;
    }

    public iPartsSaaPartsListPrimaryKey(iPartsBOMConstKitContentId constKitId, String kemFrom) {
        this(constKitId.getConstKitNo(), constKitId.getPartPos(), constKitId.getRevFrom(), kemFrom);
    }

    public iPartsSaaPartsListPrimaryKey(iPartsDataBOMConstKitContent dataBOMConstKitContent) {
        this(dataBOMConstKitContent.getAsId(), dataBOMConstKitContent.getFieldValue(FIELD_DCK_KEMFROM));
    }

    public iPartsSaaPartsListPrimaryKey cloneMe() {
        return new iPartsSaaPartsListPrimaryKey(saaBkNo, pos, revFrom, kemFrom);
    }

    public iPartsModelId getModelId() {
        return modelId;
    }

    public void setModelId(iPartsModelId modelId) {
        this.modelId = modelId;
    }

    public OpsId getOpsId() {
        return opsId;
    }

    public void setOpsId(OpsId opsId) {
        this.opsId = opsId;
    }

    public String getSaaBkNo() {
        return saaBkNo;
    }

    public String getPos() {
        return pos;
    }

    public String getRevFrom() {
        return revFrom;
    }

    public String getKemFrom() {
        return kemFrom;
    }

    public iPartsBOMConstKitContentId getConstKitId() {
        return new iPartsBOMConstKitContentId(saaBkNo, pos, revFrom);
    }

    @Override
    public String createGUID() {
        return createSaaPartsListGUID();
    }

    /**
     * Erzeugt eine deterministische GUID als Referenz in das Feld {@code EDS_CONST_KIT.DCK_GUID} einer EDS Konstruktionsstückliste.
     * Dabei werden den (optionalen) Ids modelId und OpsId voran gestellt
     * Mit der Extended-GUID ist ein direkter Sprung aus dem Retail in die EDS-Konstruktion möglich
     *
     * @return
     */
    public String createEDSExtendedGUID() {
        StringBuilder str = new StringBuilder();
        if (getModelId() != null) {
            str.append(getModelId().getModelNumber());
        }
        str.append(GUID_DELIMITER);
        if (getOpsId() != null) {
            str.append(getOpsId().getGroup());
            str.append(GUID_DELIMITER);
            str.append(getOpsId().getScope());
        } else {
            str.append(GUID_DELIMITER);
        }
        str.append(GUID_DELIMITER);
        str.append(createSaaPartsListGUID());
        return str.toString();
    }

    /**
     * Erzeugt eine deterministische GUID als Referenz in das Feld {@code EDS_CONST_KIT.DCK_GUID} einer EDS Konstruktionsstückliste.
     *
     * @return
     */
    public String createSaaPartsListGUID() {
        return toString(GUID_DELIMITER, false);
    }

    @Override
    public String toString() {
        return toString("/", false);
    }

    public String toString(String delimiter, boolean spaceForEmptyValue) {
        StringBuilder sb = new StringBuilder();
        // Den Pfad nur einbeziehen, wenn er existiert
        if (constKitPath != null) {
            sb.append(getSpaceForEmptyValue(constKitPath, spaceForEmptyValue));
            sb.append(K_SOURCE_CONTEXT_DELIMITER);
        }
        sb.append(getSpaceForEmptyValue(saaBkNo, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(pos, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(revFrom, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(kemFrom, spaceForEmptyValue));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }

        iPartsSaaPartsListPrimaryKey o2 = (iPartsSaaPartsListPrimaryKey)o;

        if (!Arrays.equals(toArray(), o2.toArray())) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(toArray());
    }

    private String[] toArray() {
        return new String[]{ saaBkNo,
                             pos,
                             revFrom,
                             kemFrom };
    }

}

