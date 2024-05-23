/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.framework.modules.config.db.DBConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.util.StrUtils;
import de.docware.util.misc.id.IdWithType;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_REPLACE_PART.
 */
public class iPartsDataReplacePart extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DRP_VARI, FIELD_DRP_VER, FIELD_DRP_LFDNR, FIELD_DRP_SEQNO };

    public iPartsDataReplacePart(EtkProject project, iPartsReplacePartId id) {
        super(KEYS);
        tableName = TABLE_DA_REPLACE_PART;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsReplacePartId createId(String... idValues) {
        return new iPartsReplacePartId(idValues[0], idValues[1], idValues[2], idValues[3]);
    }

    @Override
    public iPartsReplacePartId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsReplacePartId)id;
    }

    @Override
    public iPartsDataReplacePart cloneMe(EtkProject project) {
        iPartsDataReplacePart clone = new iPartsDataReplacePart(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    public iPartsReplacement.Source getSource() {
        return iPartsReplacement.Source.getFromDBValue(getFieldValue(FIELD_DRP_SOURCE));
    }

    public iPartsDataReleaseState getStatus() {
        String status = getFieldValue(iPartsConst.FIELD_DRP_STATUS);
        return status.isEmpty() ? iPartsDataReleaseState.RELEASED : iPartsDataReleaseState.getTypeByDBValue(status);
    }

    /**
     * Prüft ob diese Ersetzung zwischen den selben Stücklisteneinträgen besteht, wie die übergebene Ersetzung.
     *
     * @param otherDataReplacePart
     * @return
     */
    public boolean isSameReplacement(iPartsDataReplacePart otherDataReplacePart) {
        if (!getFieldValue(FIELD_DRP_VARI).equals(otherDataReplacePart.getFieldValue(FIELD_DRP_VARI))) {
            return false;
        }
        if (!getFieldValue(FIELD_DRP_VER).equals(otherDataReplacePart.getFieldValue(FIELD_DRP_VER))) {
            return false;
        }
        if (!getFieldValue(FIELD_DRP_LFDNR).equals(otherDataReplacePart.getFieldValue(FIELD_DRP_LFDNR))) {
            return false;
        }
        if (!getFieldValue(FIELD_DRP_REPLACE_LFDNR).equals(otherDataReplacePart.getFieldValue(FIELD_DRP_REPLACE_LFDNR))) {
            return false;
        }
        return true;
    }

    /**
     * Prüft, ob diese Ersetzung zwischen den selben Stücklisteneinträgen bzgl. der echten Vorgänger und Nachfolger aus
     * der Konstruktion mit den übergebenen GUIDs (in DIALOG sind das die BCTE-Schlüssel) besteht.
     *
     * @param predecessorGUID
     * @param successorGUID
     * @return
     */
    public boolean isSameReplacementInConstruction(String predecessorGUID, String successorGUID) {
        if (!getFieldValue(FIELD_DRP_SOURCE_GUID).equals(predecessorGUID)) {
            return false;
        }
        if (!getFieldValue(FIELD_DRP_REPLACE_SOURCE_GUID).equals(successorGUID)) {
            return false;
        }
        return true;
    }

    /**
     * Prüft ob diese Ersetzung ein Duplikat zur übergebenen Ersetzung ist. Das ist sie wenn sie in allen Feldern
     * übereinstimmt, bis auf den Status und die Sequenznummer.
     *
     * @param otherDataReplacePart
     * @return
     */
    public boolean isDuplicateOf(iPartsDataReplacePart otherDataReplacePart) {
        for (DBDataObjectAttribute otherDataAttribute : otherDataReplacePart.getAttributes().getFields()) {
            if (otherDataAttribute.getName().equals(iPartsConst.FIELD_DRP_SEQNO)
                || otherDataAttribute.getName().equals(iPartsConst.FIELD_DRP_STATUS)
                || otherDataAttribute.getName().equals(DBConst.FIELD_STAMP)) {
                continue;
            }
            DBDataObjectAttribute attribute = getAttribute(otherDataAttribute.getName());
            if ((attribute != null) && !otherDataAttribute.equalContent(attribute)) {
                return false;
            }
        }
        return true;
    }

    public PartListEntryId getPredecessorPartListEntryId() {
        return getAsId().getPredecessorPartListEntryId();
    }

    public PartListEntryId getSuccessorPartListEntryId() {
        String successorVari = getAsId().getReplaceVari();
        String successorVer = getAsId().getReplaceVer();
        String successorLfdNr = getFieldValue(FIELD_DRP_REPLACE_LFDNR);
        if (StrUtils.isValid(successorVari, successorLfdNr)) {
            return new PartListEntryId(successorVari, successorVer, successorLfdNr);
        }
        return null;
    }

    /**
     * Liefert einen Schlüssel für Vergleiche zwischen Ersetzungen zurück basierend auf allen relevanten Dateninhalten dieser
     * Ersetzung.
     *
     * @param includePredecessorAndSuccessorPLEIds Sollen die IDs der Stücklisteneinträge von Vorgänger und Nachfolger im
     *                                             Schlüssel enthalten sein?
     * @param includeRFMEFlags                     Sollen die RFME-Flags im Schlüssel enthalten sein?
     * @param includeSourceAndState                Sollen die Quelle und der Status im Schüssel enthalten sein?
     * @return
     * @see iPartsReplacement#getCompareKey(boolean, boolean, boolean) Der Rückgabewert ist kompatibel zu dieser Methode.
     */
    public String getCompareKey(boolean includePredecessorAndSuccessorPLEIds, boolean includeRFMEFlags, boolean includeSourceAndState) {
        StringBuilder sb = new StringBuilder();

        if (includePredecessorAndSuccessorPLEIds) {
            sb.append(getPredecessorPartListEntryId().toDBString());
            sb.append(IdWithType.DB_ID_DELIMITER);
            PartListEntryId successorPartListEntryId = getSuccessorPartListEntryId();
            if (successorPartListEntryId != null) {
                sb.append(successorPartListEntryId.toDBString());
                sb.append(IdWithType.DB_ID_DELIMITER);
            }
        }

        sb.append(getFieldValue(iPartsConst.FIELD_DRP_REPLACE_MATNR));

        if (includeRFMEFlags) {
            sb.append(IdWithType.DB_ID_DELIMITER);
            sb.append(getFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEA));
            sb.append(IdWithType.DB_ID_DELIMITER);
            sb.append(getFieldValue(iPartsConst.FIELD_DRP_REPLACE_RFMEN));
        }

        if (includeSourceAndState) {
            sb.append(IdWithType.DB_ID_DELIMITER);
            sb.append(getSource().getDbValue());
            sb.append(IdWithType.DB_ID_DELIMITER);
            sb.append(getStatus().getDbValue());
        }

        String predecessorSourceGUID = getFieldValue(iPartsConst.FIELD_DRP_SOURCE_GUID);
        String successorSourceGUID = getFieldValue(iPartsConst.FIELD_DRP_REPLACE_SOURCE_GUID);
        if (StrUtils.isValid(predecessorSourceGUID, successorSourceGUID)) {
            sb.append(IdWithType.DB_ID_DELIMITER);
            sb.append(predecessorSourceGUID);
            sb.append(IdWithType.DB_ID_DELIMITER);
            sb.append(successorSourceGUID);
        }

        return sb.toString();
    }
}
