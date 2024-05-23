/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;


import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindEPCTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;

/**
 * EPC Importer für SA-KG-Benennungen
 * Dateiname: SA_DICTIONARY
 */
public class EPCSaKgDescriptionImporter extends AbstractEPCDictionaryImporter {

    private final static String SA_DICT_DESCIDX = "DESCIDX";
    private final static String SA_DICT_LANG = "LANG";
    private final static String SA_DICT_TEXT = "TEXT";

    public EPCSaKgDescriptionImporter(EtkProject project) {
        super(project, "EPC SA-Dictionary", "!!EPC SA-KG-Benennung (SA-Dictionary)");
    }

    @Override
    protected iPartsDictTextKindId initTextKindIdForImport() {
        return DictTxtKindIdByMADId.getInstance(getProject()).getEPCTxtKindId(DictTextKindEPCTypes.SA_DICTIONARY);
    }

    @Override
    protected String[] getHeaderNames() {
        return new String[]{ SA_DICT_DESCIDX, SA_DICT_LANG, SA_DICT_TEXT };
    }

    @Override
    protected DictTextKindEPCTypes[] getTextKindTypes() {
        return new DictTextKindEPCTypes[]{ DictTextKindEPCTypes.SA_DICTIONARY };
    }

    @Override
    protected String getTextId(String foreignTextId) {
        return DictHelper.buildEPCSaKgDescTextId(foreignTextId);
    }
}
