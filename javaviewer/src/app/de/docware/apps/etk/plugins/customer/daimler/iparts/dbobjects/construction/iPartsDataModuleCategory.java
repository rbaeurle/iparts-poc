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
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_MODULE_CATEGORY.
 */
public class iPartsDataModuleCategory extends EtkDataObject implements iPartsConst {

    private static final String[] KEYS = new String[]{ FIELD_DMC_MODULE };

    public iPartsDataModuleCategory(EtkProject project, iPartsModuleCategoryId id) {
        super(KEYS);
        tableName = TABLE_DA_MODULE_CATEGORY;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsModuleCategoryId createId(String... idValues) {
        return new iPartsModuleCategoryId(idValues[0]);
    }

    @Override
    public iPartsModuleCategoryId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsModuleCategoryId)id;
    }

    public String getRevisionStateFrom() {
        return getFieldValue(FIELD_DMC_AS_FROM);
    }

    public EtkMultiSprache getDescription() {
        return getFieldValueAsMultiLanguage(FIELD_DMC_DESC);
    }

    public String getPictureName() {
        return getFieldValue(FIELD_DMC_PICTURE);
    }

}
