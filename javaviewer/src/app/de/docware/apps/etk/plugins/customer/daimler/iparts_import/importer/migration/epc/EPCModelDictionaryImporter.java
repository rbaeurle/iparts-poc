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
 * EPC Importer für Baumusterbennungen (enthält auch KG-Benennung, TU-Benennung und Baukasten-Benennung)
 * Dateiname: BM_DICTIONARY
 */
public class EPCModelDictionaryImporter extends AbstractEPCDictionaryImporter {

    private final static String BM_DICT_DESCIDX = "DESCIDX";
    private final static String BM_DICT_LANG = "LANG";
    private final static String BM_DICT_TEXT = "TEXT";

    public EPCModelDictionaryImporter(EtkProject project) {
        super(project, "EPC BM-Dictionary", "!!EPC Baumusterbenennungen (BM-Dictionary)");
    }

    @Override
    protected iPartsDictTextKindId initTextKindIdForImport() {
        return DictTxtKindIdByMADId.getInstance(getProject()).getEPCTxtKindId(DictTextKindEPCTypes.MODEL_DICTIONARY);
    }

    @Override
    protected String[] getHeaderNames() {
        return new String[]{ BM_DICT_DESCIDX, BM_DICT_LANG, BM_DICT_TEXT };
    }

    @Override
    protected DictTextKindEPCTypes[] getTextKindTypes() {
        return new DictTextKindEPCTypes[]{ DictTextKindEPCTypes.MODEL_DICTIONARY };
    }

    @Override
    protected String getTextId(String foreignTextId) {
        return DictHelper.buildEPCModelDescTextId(foreignTextId);
    }

}
