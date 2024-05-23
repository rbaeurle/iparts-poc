/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.eds.helper;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataEDSModelContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataEDSModelContentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelElementUsage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModelElementUsageList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.AbstractDataImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.helper.AbstractWbSaaCalculationHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;

/**
 * Helper für die Vorverdichtung der KEM-From und -To Daten für den Bom-DB-Importer
 */
public class EDSWbSaaCalculationHelper extends AbstractWbSaaCalculationHelper implements iPartsConst {

    public EDSWbSaaCalculationHelper(EtkProject project, AbstractDataImporter importer) {
        super(project, iPartsImportDataOrigin.EDS, importer);
    }

    @Override
    protected String getTableName() {
        return edsStructureHelper.getStructureTableName();
    }

    @Override
    protected String getModelField() {
        return edsStructureHelper.getModelNumberField();
    }

    @Override
    protected String getSaaField() {
        return edsStructureHelper.getSubElementField();
    }

    @Override
    protected String getFieldModelReleaseTo() {
        return edsStructureHelper.getReleaseToField();
    }

    @Override
    protected String getFieldModelReleaseFrom() {
        return edsStructureHelper.getReleaseFromField();
    }

    @Override
    protected String getFieldCode() {
        return edsStructureHelper.getCodeField();
    }

    @Override
    protected String getFieldFactories() {
        return edsStructureHelper.getPlantSupplyField();
    }

    @Override
    protected EtkDataObjectList<? extends EtkDataObject> getDataObjectListForSearch() {
        if (edsStructureHelper.isNewStructureActive()) {
            return new iPartsDataModelElementUsageList();
        } else {
            return new iPartsDataEDSModelContentList();
        }
    }

    @Override
    protected EtkDataObject createDataObjectFromAttributes(DBDataObjectAttributes attributes) {
        if (edsStructureHelper.isNewStructureActive()) {
            iPartsDataModelElementUsage dataModel = new iPartsDataModelElementUsage(getProject(), null);
            dataModel.setAttributes(attributes, true, false, DBActionOrigin.FROM_DB);
            return dataModel;
        } else {
            iPartsDataEDSModelContent dataModel = new iPartsDataEDSModelContent(getProject(), null);
            dataModel.setAttributes(attributes, true, false, DBActionOrigin.FROM_DB);
            return dataModel;
        }
    }

    @Override
    protected EtkDisplayFields buildSelectedFieldSet() {
        EtkDisplayFields selectFields = new EtkDisplayFields();
        EtkDisplayField selectField = new EtkDisplayField(getTableName(), getModelField(), false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(getTableName(), edsStructureHelper.getUpperStructureValueField(), false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(getTableName(), edsStructureHelper.getLowerStructureValueField(), false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(getTableName(), edsStructureHelper.getPosField(), false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(getTableName(), edsStructureHelper.getReleaseFromField(), false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(getTableName(), edsStructureHelper.getReleaseToField(), false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(getTableName(), edsStructureHelper.getSubElementField(), false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(getTableName(), edsStructureHelper.getCodeField(), false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(getTableName(), edsStructureHelper.getPlantSupplyField(), false, false);
        selectFields.addFeld(selectField);

        return selectFields;
    }

    @Override
    protected boolean isValidSaaBkNo(DBDataObjectAttributes attributes) {
        String saaBkNo = getSaaBkNo(attributes);
        iPartsNumberHelper helper = new iPartsNumberHelper();
        return helper.isValidSaaOrBk(saaBkNo, false);
    }

    @Override
    protected boolean isMBSConditionValid(DBDataObjectAttributes attributes) {
        return true;
    }
}
