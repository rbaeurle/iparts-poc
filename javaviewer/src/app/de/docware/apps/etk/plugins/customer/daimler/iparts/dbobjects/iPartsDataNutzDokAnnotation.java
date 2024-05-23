/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

import java.util.Calendar;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für die Tabelle {@link #TABLE_DA_NUTZDOK_ANNOTATION}
 */
public class iPartsDataNutzDokAnnotation extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DNA_REF_ID, FIELD_DNA_REF_TYPE, FIELD_DNA_ETS, FIELD_DNA_DATE, FIELD_DNA_LFDNR };

    public iPartsDataNutzDokAnnotation(EtkProject project, iPartsNutzDokAnnotationId id) {
        super(KEYS);
        tableName = TABLE_DA_NUTZDOK_ANNOTATION;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsNutzDokAnnotationId createId(String... idValues) {
        return new iPartsNutzDokAnnotationId(idValues[0], idValues[1], idValues[2], idValues[3], idValues[4]);
    }

    @Override
    public iPartsNutzDokAnnotationId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }
        return (iPartsNutzDokAnnotationId)id;
    }

    public void setDate(Calendar value) {
        setFieldValueAsDate(FIELD_DNA_DATE, value, DBActionOrigin.FROM_EDIT);
    }

    public void setDate(String value) {
        setFieldValue(FIELD_DNA_DATE, value, DBActionOrigin.FROM_EDIT);
    }

    public void setAuthor(String author) {
        setFieldValue(FIELD_DNA_AUTHOR, author, DBActionOrigin.FROM_EDIT);
    }

    public void setText(String annotation) {
        setFieldValue(FIELD_DNA_ANNOTATION, annotation, DBActionOrigin.FROM_EDIT);
    }
}

