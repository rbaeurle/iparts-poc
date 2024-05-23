/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_ES1.
 */
public class iPartsDataES1 extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DES_ES1, FIELD_DES_FNID };

    public static final String TYPE_SECOND_PART = "02";
    public static final String TYPE_REMAN_PART = "01";
    public static final String TYPE_ENGINE_BASIC = "10";
    public static final String TYPE_ENGINE_LONG_BLOCK_PLUS = "11";
    public static final String TYPE_ENGINE_LONG_BLOCK = "12";
    public static final String TYPE_ENGINE_SHORT_BLOCK = "13";

    private iPartsDataES1(EtkProject project) {
        super(KEYS);
        tableName = TABLE_DA_ES1;
        if (project != null) {
            init(project);
        }
    }

    public iPartsDataES1(EtkProject project, iPartsES1Id id) {
        this(project);
        setId(id, DBActionOrigin.FROM_DB);
    }

    public iPartsDataES1() {
        this(null);
    }

    @Override
    public iPartsES1Id createId(String... idValues) {
        return new iPartsES1Id(idValues[0], idValues[1]);
    }

    @Override
    public iPartsES1Id getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsES1Id)id;
    }

    @Override
    public DBDataObject cloneMe(EtkProject project) {
        iPartsDataES1 clone = new iPartsDataES1(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    public String getES1Type() {
        return getFieldValue(FIELD_DES_TYPE);
    }

    public String getFootNote() {
        return getFieldValue(FIELD_DES_FNID);
    }

    public boolean isSecondPart() {
        return getES1Type().equals(TYPE_SECOND_PART);
    }

    public boolean isRemanPart() {
        return getES1Type().equals(TYPE_REMAN_PART);
    }
}
