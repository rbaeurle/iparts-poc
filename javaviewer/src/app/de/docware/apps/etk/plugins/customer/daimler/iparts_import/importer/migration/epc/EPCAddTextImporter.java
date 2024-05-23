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
 * EPC Importer für Ergänzungstexte aus MAD
 * Dateiname: PART_DESCS
 */
public class EPCAddTextImporter extends AbstractEPCDictionaryImporter {

    private final static String PART_DESC_DESCIDX = "DESCIDX";
    private final static String PART_DESC_LANG = "LANG";
    private final static String PART_DESC_TEXT = "DESCRIPTION";

    public EPCAddTextImporter(EtkProject project) {
        super(project, "EPC Part-Desc", "!!EPC Ergänzungstexte MAD (Part-Desc)");
    }

    @Override
    protected iPartsDictTextKindId initTextKindIdForImport() {
        return DictTxtKindIdByMADId.getInstance(getProject()).getEPCTxtKindId(DictTextKindEPCTypes.ADD_TEXT);
    }

    @Override
    protected String[] getHeaderNames() {
        return new String[]{ PART_DESC_DESCIDX, PART_DESC_LANG, PART_DESC_TEXT };
    }

    @Override
    protected DictTextKindEPCTypes[] getTextKindTypes() {
        return new DictTextKindEPCTypes[]{ DictTextKindEPCTypes.ADD_TEXT };
    }

    @Override
    protected String getTextId(String foreignTextId) {
        return DictHelper.buildEPCAddTextTextId(foreignTextId);
    }
}
