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
 * EPC Importer für Teilebennung ET
 * Dateiname: PART_NOUNS
 */
public class EPCPartDescriptionImporter extends AbstractEPCDictionaryImporter {

    private final static String PART_NOUNS_NOUNIDX = "NOUNIDX";
    private final static String PART_NOUNS_LANG = "LANG";
    private final static String PART_NOUNS_TEXT = "NOUN";

    public EPCPartDescriptionImporter(EtkProject project) {
        super(project, "EPC Part-Nouns", "!!EPC Teilebenennungen ET (Part-Nouns)");
    }

    @Override
    protected iPartsDictTextKindId initTextKindIdForImport() {
        return DictTxtKindIdByMADId.getInstance(getProject()).getEPCTxtKindId(DictTextKindEPCTypes.PART_DESCRIPTION);
    }

    @Override
    protected String[] getHeaderNames() {
        return new String[]{ PART_NOUNS_NOUNIDX, PART_NOUNS_LANG, PART_NOUNS_TEXT };
    }

    @Override
    protected DictTextKindEPCTypes[] getTextKindTypes() {
        return new DictTextKindEPCTypes[]{ DictTextKindEPCTypes.PART_DESCRIPTION };
    }

    @Override
    protected String getTextId(String foreignTextId) {
        return DictHelper.buildEPCPartDescTextId(foreignTextId);
    }
}
