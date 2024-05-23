/*
 * Copyright (c) 2020 Quanos GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsPSKProductVariantId;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataPSKProductVariant extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DPPV_PRODUCT_NO, FIELD_DPPV_VARIANT_ID };

    public iPartsDataPSKProductVariant(EtkProject project, iPartsPSKProductVariantId id) {
        super(KEYS);
        tableName = TABLE_DA_PSK_PRODUCT_VARIANTS;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsPSKProductVariantId createId(String... idValues) {
        return new iPartsPSKProductVariantId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsPSKProductVariantId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsPSKProductVariantId)id;
    }
}
