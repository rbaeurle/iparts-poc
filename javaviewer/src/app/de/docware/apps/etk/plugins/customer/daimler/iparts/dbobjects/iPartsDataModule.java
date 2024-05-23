/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsSpecType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsModuleId;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.StrUtils;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_MODULE.
 */
public class iPartsDataModule extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DM_MODULE_NO };

    public static final boolean DM_VARIANTS_VISIBLE_DEFAULT = true;

    public iPartsDataModule(EtkProject project, iPartsModuleId id) {
        super(KEYS);
        tableName = TABLE_DA_MODULE;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDataModule cloneMe(EtkProject project) {
        iPartsDataModule clone = new iPartsDataModule(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public iPartsModuleId createId(String... idValues) {
        return new iPartsModuleId(idValues[0]);
    }

    @Override
    public iPartsModuleId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsModuleId)id;
    }

    public iPartsDocumentationType getDocumentationType() {
        return iPartsDocumentationType.getFromDBValue(getFieldValue(FIELD_DM_DOCUTYPE));
    }

    public boolean isVariantsVisible() {
        return getFieldValueAsBoolean(FIELD_DM_VARIANTS_VISIBLE);
    }

    public void initWithDefaultValues() {
        // Erst alle Attribute leer anlegen ...
        initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        // ... dann die nötigen Felder mit Default-Werten initialisieren.
        setFieldValueAsBoolean(FIELD_DM_VARIANTS_VISIBLE, DM_VARIANTS_VISIBLE_DEFAULT, DBActionOrigin.FROM_EDIT);
    }

    public boolean isShowColorTablefootnotes() {
        return getFieldValueAsBoolean(FIELD_DM_USE_COLOR_TABLEFN);
    }

    public boolean isSpringFilterRelevant() {
        return getFieldValueAsBoolean(FIELD_DM_SPRING_FILTER);
    }

    public DCAggregateTypes getAggTypeForSpecialZBFilter() {
        String aggType = getFieldValue(FIELD_DM_ZB_PART_NO_AGG_TYPE);
        if (StrUtils.isEmpty(aggType)) {
            return DCAggregateTypes.UNKNOWN;
        }
        DCAggregateTypes selectedAggType = DCAggregateTypes.getDCAggregateTypeByAggregateType(aggType);
        if ((selectedAggType != DCAggregateTypes.ENGINE) && (selectedAggType != DCAggregateTypes.HIGH_VOLTAGE_BATTERY)) {
            return DCAggregateTypes.UNKNOWN;
        }
        return selectedAggType;
    }

    public boolean isZBNumberFilterRelevant() {
        return getAggTypeForSpecialZBFilter() != DCAggregateTypes.UNKNOWN;
    }

    public String getDbDocuType() {
        return getFieldValue(FIELD_DM_DOCUTYPE);
    }

    public iPartsDocumentationType getDocuType() {
        return iPartsDocumentationType.getFromDBValue(getDbDocuType());
    }

    public void setDbDocuType(String dbValue, DBActionOrigin origin) {
        setFieldValue(FIELD_DM_DOCUTYPE, dbValue, origin);
    }

    public void setDocuType(iPartsDocumentationType docuType, DBActionOrigin origin) {
        setDbDocuType(docuType.getDBValue(), origin);
    }

    public iPartsSpecType getSpecType() {
        return iPartsSpecType.getFromDBValue(getFieldValue(FIELD_DM_SPEC));
    }

    public boolean skipEmptyHotspotCheck() {
        return getFieldValueAsBoolean(FIELD_DM_POS_PIC_CHECK_INACTIVE);
    }

    // DAIMLER-15137
    public boolean skipHotspotWithoutImageCheck() {
        return getFieldValueAsBoolean(FIELD_DM_HOTSPOT_PIC_CHECK_INACTIVE);
    }

    // DAIMLER-15089
    public void setSourceModule(AssemblyId assemblyId, DBActionOrigin origin) {
        setFieldValue(FIELD_DM_SOURCE_TU, assemblyId.getKVari(), origin);
    }

    public String getSourceModuleDbValue() {
        return getFieldValue(FIELD_DM_SOURCE_TU);
    }

    public AssemblyId getSourceModuleId() {
        String sourceModuleValue = getSourceModuleDbValue();
        if (StrUtils.isValid(sourceModuleValue)) {
            return new AssemblyId(sourceModuleValue, "");
        }
        return null;
    }

    public String getSpecialTUDbValue() {
        return getFieldValue(FIELD_DM_SPECIAL_TU);
    }

    public String getSpecialTU() {
        if (getEtkProject() != null) {
            return getEtkProject().getVisObject().asText(tableName, FIELD_DM_SPECIAL_TU, getSpecialTUDbValue(), Language.DE.getCode());
        }
        return getSpecialTUDbValue();
    }
}