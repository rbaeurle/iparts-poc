/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.mbs.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSStructure;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataMBSStructureList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.mbs.MBSStructureId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.AbstractWbSaaCalculationHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.util.StrUtils;

/**
 * Helper für die Vorverdichtung der KEM-From und -To Daten für den MBS-Importer
 */
public class MBSWbSaaCalculationHelper extends AbstractWbSaaCalculationHelper implements iPartsConst {

    public MBSWbSaaCalculationHelper(EtkProject project, AbstractDataImporter importer) {
        super(project, iPartsImportDataOrigin.SAP_MBS, importer);
    }

    @Override
    protected void addToMap(SaaAVContainer saaAvContainer) {
        if (saaAvContainer.isModified()) {
            // Texte ausschliessen
            if (StrUtils.isValid(saaAvContainer.getStoredSaaBkNo())) {
                if (isValidSaaBkNo(saaAvContainer.getStoredSaaBkNo()) && isMBSConditionValid(saaAvContainer.getStoredSaaBkNo())) {
                    // Untere SachNo sollte eine Saa oder Baukasten sein
                    MBSStructureId test = new MBSStructureId(saaAvContainer.getStoredModelNo(), "");
                    // Obere SachNo sollte ein BM sein
                    if (!(test.isBasePartlistId() || test.isSaaId() || test.isFreeSaId())) {
                        addToMap(saaAvContainer.getStoredModelNo(), saaAvContainer.getStoredSaaBkNo(), saaAvContainer);
                    }
                }
            }
        }
    }

    @Override
    protected String getTableName() {
        return TABLE_DA_STRUCTURE_MBS;
    }

    @Override
    protected String getModelField() {
        return FIELD_DSM_SNR;
    }

    @Override
    protected String getSaaField() {
        return FIELD_DSM_SUB_SNR;
    }

    @Override
    protected String getFieldModelReleaseTo() {
        return FIELD_DSM_RELEASE_TO;
    }

    @Override
    protected String getFieldModelReleaseFrom() {
        return FIELD_DSM_RELEASE_FROM;
    }

    @Override
    protected String getFieldCode() {
        return FIELD_DSM_CODE;
    }

    @Override
    protected String getFieldFactories() {
        return null;
    }

    @Override
    protected EtkDataObjectList<? extends EtkDataObject> getDataObjectListForSearch() {
        return new iPartsDataMBSStructureList();
    }

    @Override
    protected EtkDataObject createDataObjectFromAttributes(DBDataObjectAttributes attributes) {
        iPartsDataMBSStructure dataStructure = new iPartsDataMBSStructure(getProject(), null);
        dataStructure.setAttributes(attributes, true, false, DBActionOrigin.FROM_DB);
        return dataStructure;
    }

    @Override
    protected EtkDisplayFields buildSelectedFieldSet() {
        EtkDisplayFields selectFields = new EtkDisplayFields();
        EtkDisplayField selectField = new EtkDisplayField(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SNR_SUFFIX, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_POS, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SORT, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_KEM_FROM, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_RELEASE_FROM, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_RELEASE_TO, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_SUB_SNR, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_STRUCTURE_MBS, FIELD_DSM_CODE, false, false);
        selectFields.addFeld(selectField);
        return selectFields;
    }

    @Override
    protected boolean isValidSaaBkNo(DBDataObjectAttributes attributes) {
        return isValidSaaBkNo(getSaaBkNo(attributes));
    }

    private boolean isValidSaaBkNo(String saaBkNo) {
        iPartsNumberHelper helper = new iPartsNumberHelper();
        return helper.isValidSaaOrBk(saaBkNo, false);
    }

    @Override
    protected boolean isMBSConditionValid(DBDataObjectAttributes attributes) {
        // Grundstücklisten nicht berücksichtigen
        return isMBSConditionValid(getSaaBkNo(attributes));
    }

    private boolean isMBSConditionValid(String saaBkValue) {
        if (StrUtils.isValid(saaBkValue)) {
            MBSStructureId testId = new MBSStructureId(saaBkValue, "");
            if (testId.isBasePartlistId()) {
                return false;
            }
        } else {
            // Texte (SUB_SNR leer) nicht berücksichtigen
            return false;
        }
        return true;
    }

    /**
     * DAIMLER-11363: Gültigkeitsdatum Ab = Bis bedeutet, das die entsprechenden SAAs bereits in SAP gelöscht wurden bzw. nie gebaut wurden
     * => ignorieren
     *
     * @param attributes
     * @return
     */
    @Override
    protected boolean isMBSEqualStartEndDate(DBDataObjectAttributes attributes) {
        String minReleaseFrom = getKemFromDate(attributes);
        String maxReleaseTo = getKemToDate(attributes);
        if (StrUtils.isValid(minReleaseFrom, maxReleaseTo) && minReleaseFrom.equals(maxReleaseTo)) {
            return true;
        }
        return false;
    }
}
