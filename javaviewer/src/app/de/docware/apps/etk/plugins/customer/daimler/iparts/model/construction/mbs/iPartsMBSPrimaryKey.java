/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.mbs;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.iPartsSaaPartsListPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.iPartsConstructionPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsModelId;
import de.docware.framework.modules.db.DBDataObjectAttributes;

/**
 * Einfache Klasse, die alle Felder eines MBS-Konstruktionsdatensatzes enthält.
 * Aus dem {@link de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry} werden die Daten im Konstruktor ermittelt.
 * <p>
 * Achtung: im Retail wird für MBS Einträge folgende K_SOURCE_GUID verwendet: kVari_kLfdNr
 * diese kann nicht auf die Konstruktionswerte zurückgerechnet werden. Daher fehlen auch die Funktionen dafür
 */
public class iPartsMBSPrimaryKey extends iPartsConstructionPrimaryKey {

    // Model
    protected iPartsModelId modelId; // i.A. optional
    // MBS-Knoten
    protected MBSStructureId structureId;           // i.A. optional
    // Obere Sachnummer
    protected String upperNo;
    // Positionsbezeichnung
    protected String pos;
    // Sortierung
    protected String sort;
    // KEM von
    protected String kemFrom;
    // Für die Konstruktionsstückliste muss die laufende Nummer den kompletten Strukturpfad enthalten
    protected String structurePath;

    public iPartsMBSPrimaryKey(String upperNo, String pos, String sort, String kemFrom) {
        this.type = Type.MBS;
        this.upperNo = upperNo;
        this.pos = pos;
        this.sort = sort;
        this.kemFrom = kemFrom;
        this.modelId = null;
        this.structureId = null;
    }

    public iPartsMBSPrimaryKey(DBDataObjectAttributes partlistAttributes, String structurePath) {
        this(partlistAttributes.getField(iPartsConst.FIELD_DPM_SNR).getAsString(),
             partlistAttributes.getField(iPartsConst.FIELD_DPM_POS).getAsString(),
             partlistAttributes.getField(iPartsConst.FIELD_DPM_SORT).getAsString(),
             partlistAttributes.getField(iPartsConst.FIELD_DPM_KEM_FROM).getAsString());
        this.structurePath = structurePath;
    }

    public iPartsModelId getModelId() {
        return modelId;
    }

    public MBSStructureId getStructureId() {
        return structureId;
    }

    public String getUpperNo() {
        return upperNo;
    }

    public String getPos() {
        return pos;
    }

    public String getSort() {
        return sort;
    }

    public String getKemFrom() {
        return kemFrom;
    }

    public String getStructurePath() {
        return structurePath;
    }

    /**
     * Erzeugt eine deterministische GUID auf Basis der Schlüssel aus DA_PARTSLIST_MBS
     *
     * @return
     */
    public String createMBSGUID() {
        return toString(GUID_DELIMITER, false);
    }

    @Override
    public iPartsSaaPartsListPrimaryKey cloneMe() {
        return new iPartsSaaPartsListPrimaryKey(upperNo, pos, sort, kemFrom);
    }

    @Override
    public String toString(String delimiter, boolean spaceForEmptyValue) {
        StringBuilder sb = new StringBuilder();
        // Den Pfad nur einbeziehen, wenn er existiert
        if (structurePath != null) {
            sb.append(getSpaceForEmptyValue(structurePath, spaceForEmptyValue));
            sb.append(K_SOURCE_CONTEXT_DELIMITER);
        }
        sb.append(getSpaceForEmptyValue(upperNo, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(pos, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(sort, spaceForEmptyValue));
        sb.append(delimiter);
        sb.append(getSpaceForEmptyValue(kemFrom, spaceForEmptyValue));
        return sb.toString();
    }

    @Override
    public String createGUID() {
        // wird von der Übernahme in AS verwendet, hier brauchen wir eigentlich eine guid die aus kVari + _ + kLfdNr
        // besteht, aber diese kann nicht aus den Konstruktionswerten berechnet werden, und wird daher in der
        // Übernahme manuell erstellt.
        return "";
    }

    /**
     * Erzeugt eine KlfdNr für die MBS Konstruktionsstückliste auf Basis der Konstruktionsattribute und des
     * Strukturpfades
     *
     * @param partlistAttributes
     * @param structurePath
     * @return
     */
    public static String buildMBSGUIDForPartListSeqNo(DBDataObjectAttributes partlistAttributes, String structurePath) {
        iPartsMBSPrimaryKey mbsPrimaryKey = new iPartsMBSPrimaryKey(partlistAttributes, structurePath);
        return mbsPrimaryKey.createMBSGUID();
    }
}
