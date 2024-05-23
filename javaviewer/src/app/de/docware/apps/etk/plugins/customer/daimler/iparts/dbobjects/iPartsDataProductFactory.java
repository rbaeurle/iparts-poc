/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductFactoryId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.misc.id.IdWithType;

/**
 * Datenobjekt für die Zuordnung Produkt (=Katalog) zu Werken im After-Sales für DA_PRODUCT_FACTORIES.
 */
public class iPartsDataProductFactory extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DPF_PRODUCT_NO, FIELD_DPF_FACTORY_NO };

    public iPartsDataProductFactory(EtkProject project, iPartsProductFactoryId id) {
        super(KEYS);
        tableName = TABLE_DA_PRODUCT_FACTORIES;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsDataProductFactory cloneMe(EtkProject project) {
        iPartsDataProductFactory clone = new iPartsDataProductFactory(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsProductFactoryId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsProductFactoryId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsProductFactoryId)id;
    }
}
