/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_MODEL_OIL.
 */
public class iPartsDataModelOil extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DMO_MODEL_NO, FIELD_DMO_SPEC_VALIDITY, FIELD_DMO_SPEC_TYPE };

    private EtkMultiSprache multiText;

    public iPartsDataModelOil(EtkProject project, iPartsModelOilId id) {
        super(KEYS);
        tableName = TABLE_DA_MODEL_OIL;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
        multiText = null;
    }

    @Override
    public iPartsModelOilId createId(String... idValues) {
        return new iPartsModelOilId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsModelOilId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsModelOilId)id;
    }

    public String getTextId() {
        return getFieldValue(FIELD_DMO_TEXT_ID);
    }

    public String getSAEClass() {
        return (getFieldValue(FIELD_DMO_SAE_CLASS));
    }

    public void setMultiText(EtkMultiSprache multi) {
        this.multiText = multi;
    }

    public EtkMultiSprache getText() {
        if (multiText == null) {
            if ((getEtkProject() != null) && StrUtils.isValid(getTextId())) {
                multiText = getEtkProject().getDbLayer().getLanguagesTextsByTextId(getTextId());
            } else {
                // samit nicht dauernd geladen wird
                multiText = new EtkMultiSprache();
            }
        }
        return multiText;
    }
}
