/*
 * Copyright (c) 2016 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.util.misc.id.IdWithType;

/**
 * Mapping der Sortimentsklassen (=Assortment Classes) auf die Aftersales Produktklassen
 *
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für [TABLE_DA_AC_PC_MAPPING].
 *
 * Sortimentsklasse  [DAPM_ASSORTMENT_CLASS]
 * Aftersales Produktklasse                    [DAPM_AS_PRODUCT_CLASS]
 */
public class iPartsDataAssortmentClassesMapping extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DAPM_ASSORTMENT_CLASS };

    /**
     * Einfach nur der Konstruktor
     *
     * @param project
     * @param id
     */
    public iPartsDataAssortmentClassesMapping(EtkProject project, iPartsAssortmentClassMappingId id) {
        super(KEYS);
        tableName = TABLE_DA_AC_PC_MAPPING;

        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    /**
     * Erzeugt aus der Sortimentsklasse eine eindeutige ID
     *
     * @param idValues Die Sortimentsklasse.
     * @return
     */
    @Override
    public IdWithType createId(String... idValues) {
        return new iPartsAssortmentClassMappingId(idValues[0]);
    }

    @Override
    public iPartsAssortmentClassMappingId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsAssortmentClassMappingId)id;
    }

    /**
     * Holt die Aftersales-Produktklasse zur eigenen Sortimentsklasse aus dem Objekt.
     */
    public String getASProductClass() {
        return getFieldValue(FIELD_DAPM_AS_PRODUCT_CLASS);
    }
}
