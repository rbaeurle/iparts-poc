/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleEinPASId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.db.etkrecord.EtkRecords;
import de.docware.util.StrUtils;

import java.util.List;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_MODULES_EINPAS.
 */
public class iPartsDataModuleEinPAS extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DME_PRODUCT_NO, FIELD_DME_MODULE_NO, FIELD_DME_LFDNR };

    public iPartsDataModuleEinPAS(EtkProject project, iPartsModuleEinPASId id) {
        super(KEYS);
        tableName = TABLE_DA_MODULES_EINPAS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsModuleEinPASId createId(String... idValues) {
        return new iPartsModuleEinPASId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsModuleEinPASId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsModuleEinPASId)id;
    }

    @Override
    public void initAttributesWithEmptyValues(DBActionOrigin origin) {
        super.initAttributesWithEmptyValues(origin);
        if (getAsId().getSerialNumber().isEmpty()) {
            setAttributeValue(FIELD_DME_LFDNR, createSerialNumberForEinPAS(), origin);
            updateOldId();
        }
    }

    /**
     * neue DME_LFDNR berechnen
     *
     * @return
     */
    private String createSerialNumberForEinPAS() {
        String[] whereFields = new String[]{ FIELD_DME_PRODUCT_NO, FIELD_DME_MODULE_NO };
        String[] whereValues = new String[]{ getFieldValue(FIELD_DME_PRODUCT_NO), getFieldValue(FIELD_DME_MODULE_NO) };

        if (project instanceof EtkProject) {
            return ((EtkProject)project).getEtkDbs().getNextLfdNr(tableName, FIELD_DME_LFDNR, whereFields, whereValues);
        } else {
            String[] fields = new String[]{ FIELD_DME_LFDNR };
            int serialNo = 1;
            EtkRecords records = project.getDB().getRecords(tableName, fields, whereFields, whereValues);
            if (!records.isEmpty()) {
                records.sortBetterSort(fields);
                EtkRecord rec = records.get(records.size() - 1);
                String lastNo = rec.getField(FIELD_DME_LFDNR).getAsString();
                if (!lastNo.isEmpty()) {
                    if (StrUtils.isInteger(lastNo)) {
                        int lastNum = Integer.valueOf(lastNo);
                        serialNo = lastNum + 1;
                    }
                }
            }
            return StrUtils.prefixStringWithCharsUpToLength(serialNo + "", '0', 5);
        }
    }

    @Override
    public boolean saveToDB(boolean checkIfPKExistsInDB, PrimaryKeyExistsInDB forcePKExistsInDB) {
        return saveToDB(checkIfPKExistsInDB, forcePKExistsInDB, null);
    }

    private boolean saveToDB(boolean checkIfPKExistsInDB, PrimaryKeyExistsInDB forcePKExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        if (isRevisionChangeSetActiveForEdit()) {
            EtkRevisionsHelper revisionsHelper = getRevisionsHelper();
            revisionsHelper.addDataObjectToActiveChangeSetForEdit(this);
            return true;
        } else {
            // speichern
            if (techChangeSet != null) {
                techChangeSet.addDataObject(this, false, false, false);
            }
            return super.saveToDB(checkIfPKExistsInDB, forcePKExistsInDB);
        }
    }

    /**
     * Erzeugung einer Verortung mit {@link de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPas}
     * Voraussetzung: ID ist gesetzt und es ist {@link #initAttributesWithEmptyValues(DBActionOrigin)} gelaufen
     *
     * @param einPasId
     * @param checkIfExistsInDB Flag, ob sicherheitshalber überprüft werden soll, ob ein Datensatz mit der Verortung bereits
     *                          in der Datenbank existiert
     * @param techChangeSet     Optionales technisches {@link iPartsRevisionChangeSet} zum zusätzlichen Abspeichern
     *                          der Daten neben dem Speichern direkt in der DB, wobei ein aktives Edit-{@link iPartsRevisionChangeSet}
     *                          Vorrang hat
     * @return
     */
    public boolean create_iPartsVerortung(EinPasId einPasId, boolean checkIfExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        boolean result = (einPasId != null) && einPasId.isValidId();
        if (result) {
            setAttributeValue(iPartsConst.FIELD_DME_EINPAS_HG, einPasId.getHg(), DBActionOrigin.FROM_EDIT);
            setAttributeValue(iPartsConst.FIELD_DME_EINPAS_G, einPasId.getG(), DBActionOrigin.FROM_EDIT);
            setAttributeValue(iPartsConst.FIELD_DME_EINPAS_TU, einPasId.getTu(), DBActionOrigin.FROM_EDIT);
            // speichern
            saveToDB(checkIfExistsInDB, PrimaryKeyExistsInDB.CHECK, techChangeSet);
        }
        return result;
    }


    /**
     * Erzeugung einer Verortung mit {@link de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuTemplate}
     * Voraussetzung: ID ist gesetzt und es ist {@link #initAttributesWithEmptyValues(DBActionOrigin)} gelaufen
     *
     * @param kgTuId
     * @param checkIfExistsInDB Flag, ob sicherheitshalber überprüft werden soll, ob ein Datensatz mit der Verortung bereits
     *                          in der Datenbank existiert
     * @param techChangeSet     Optionales technisches {@link iPartsRevisionChangeSet} zum zusätzlichen Abspeichern
     *                          der Daten neben dem Speichern direkt in der DB, wobei ein aktives Edit-{@link iPartsRevisionChangeSet}
     *                          Vorrang hat
     * @return
     */
    public boolean create_iPartsVerortung(KgTuId kgTuId, boolean checkIfExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        boolean result = (kgTuId != null) && kgTuId.isValidId();
        if (result) {
            setAttributeValue(iPartsConst.FIELD_DME_SOURCE_KG, kgTuId.getKg(), DBActionOrigin.FROM_EDIT);
            setAttributeValue(iPartsConst.FIELD_DME_SOURCE_TU, kgTuId.getTu(), DBActionOrigin.FROM_EDIT);
            // speichern
            saveToDB(checkIfExistsInDB, PrimaryKeyExistsInDB.CHECK, techChangeSet);
        }
        return result;
    }


    /**
     * Erzeugung einer Liste von Verortungen
     * Voraussetzung: ID ist gesetzt und es ist {@link #initAttributesWithEmptyValues(DBActionOrigin)} gelaufen
     *
     * @param einPasIdList
     * @param checkIfExistsInDB Flag, ob sicherheitshalber überprüft werden soll, ob ein Datensatz mit der Verortung bereits
     *                          in der Datenbank existiert
     * @param techChangeSet     Optionales technisches {@link iPartsRevisionChangeSet} zum zusätzlichen Abspeichern
     *                          der Daten neben dem Speichern direkt in der DB, wobei ein aktives Edit-{@link iPartsRevisionChangeSet}
     *                          Vorrang hat
     * @return
     */
    public boolean create_iPartsVerortung(List<EinPasId> einPasIdList, boolean checkIfExistsInDB, iPartsRevisionChangeSet techChangeSet) {
        boolean result = true;
        for (EinPasId einPasId : einPasIdList) {
            if (!create_iPartsVerortung(einPasId, checkIfExistsInDB, techChangeSet)) {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Löscht die Verortung ohne dass diese vorher geladen sein muss.
     * Voraussetzung: ID ist gesetzt und es ist {@link #initAttributesWithEmptyValues(DBActionOrigin)} gelaufen
     */
    public void delete_iPartsVerortung() {
        deleteFromDB(true);
    }
}
