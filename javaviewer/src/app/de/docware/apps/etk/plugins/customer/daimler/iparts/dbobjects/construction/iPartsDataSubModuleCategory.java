/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.utils.EtkMultiSprache;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_SUB_MODULE_CATEGORY.
 */
public class iPartsDataSubModuleCategory extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DSMC_SUB_MODULE };

    public iPartsDataSubModuleCategory(EtkProject project, iPartsSubModuleCategoryId id) {
        super(KEYS);
        tableName = TABLE_DA_SUB_MODULE_CATEGORY;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsSubModuleCategoryId createId(String... idValues) {
        return new iPartsSubModuleCategoryId(idValues[0]);
    }

    @Override
    public iPartsSubModuleCategoryId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsSubModuleCategoryId)id;
    }

    public EtkMultiSprache getDescription() {
        return getFieldValueAsMultiLanguage(FIELD_DSMC_DESC);
    }

    public String getPictureName() {
        return getFieldValue(FIELD_DSMC_PICTURE);
    }

}
