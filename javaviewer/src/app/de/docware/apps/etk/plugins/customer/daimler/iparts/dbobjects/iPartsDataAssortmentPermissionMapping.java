/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDataAssortmentPermissionsMappingCache;
import de.docware.framework.modules.db.DBActionOrigin;

public class iPartsDataAssortmentPermissionMapping extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DPPM_BRAND, FIELD_DPPM_ASSORTMENT_CLASS, FIELD_DPPM_AS_PRODUCT_CLASS };

    public iPartsDataAssortmentPermissionMapping(EtkProject project, iPartsAssortmentPermissionMappingId id) {
        super(KEYS);
        tableName = TABLE_DA_AC_PC_PERMISSION_MAPPING;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsAssortmentPermissionMappingId createId(String... idValues) {
        return new iPartsAssortmentPermissionMappingId(idValues[0], idValues[1], idValues[2]);
    }

    @Override
    public iPartsAssortmentPermissionMappingId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsAssortmentPermissionMappingId)id;
    }

    /**
     * Liefert die Marke==Brand: 'SMT', 'MB', MYB', ... und was da sonst noch kommt in der Zukunft
     */
    public String getBrand() {
        return getAsId().getBrand();
    }

    /**
     * Liefer die Sortimentsklasse==AssortmentClass: 'PASSENGER-CAR', 'TRUCK', 'UNIMOG', ...
     */
    public String getAssortmentClass() {
        return getAsId().getAssortmentClass();
    }

    /**
     * Liefert die Aftersales Produktklasse: 'F', 'G', ...
     */
    public String getASProductClass() {
        return getAsId().getAsProductClass();
    }

    /**
     * Liefert das aus Marke + '.' + Sortimentsklasse gebildete Recht.
     * 'SMT.PASSENGER-CAR', 'MB.TRUCK', ...
     */
    public String getPermission() {
        return iPartsDataAssortmentPermissionsMappingCache.getPermission(getAsId().getBrand(), getAsId().getAssortmentClass());
    }
}
