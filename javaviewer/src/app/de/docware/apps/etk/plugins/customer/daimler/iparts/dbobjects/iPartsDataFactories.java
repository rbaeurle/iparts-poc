/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für DA_FACTORIES.
 */
public class iPartsDataFactories extends EtkDataObject implements iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DF_LETTER_CODE };

    private iPartsDataFactories(EtkProject project) {
        super(KEYS);
        tableName = TABLE_DA_FACTORIES;
        if (project != null) {
            init(project);
        }
    }

    public iPartsDataFactories(EtkProject project, iPartsFactoriesId id) {
        this(project);
        setId(id, DBActionOrigin.FROM_DB);
    }

    @Override
    public iPartsFactoriesId createId(String... idValues) {
        return new iPartsFactoriesId(idValues[0]);
    }

    @Override
    public iPartsFactoriesId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsFactoriesId)id;
    }

    public String getFactoryNumber() {
        return getFieldValue(FIELD_DF_FACTORY_NO);
    }

    public String getFactoryText(String language) {
        return getFieldValueAsMultiLanguage(FIELD_DF_DESC).getText(language);
    }

    public String getPEMLetterCode() {
        return getFieldValue(FIELD_DF_PEM_LETTER_CODE);
    }

    public iPartsImportDataOrigin getDataSource() {
        return iPartsImportDataOrigin.getTypeFromCode(getFieldValue(FIELD_DF_SOURCE));
    }

    public boolean isValidForFilter() {
        return !getFieldValueAsBoolean(FIELD_DF_FILTER_NOT_REL);
    }
}
