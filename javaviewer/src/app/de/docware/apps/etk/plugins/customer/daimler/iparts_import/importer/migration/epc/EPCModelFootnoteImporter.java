/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.importer.migration.epc;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.EPCFootnoteType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindEPCTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;

/**
 * EPC Importer für Baumuster Fußnotentexte
 * Dateiname: BM_FOOTNOTES_DICTIONARY
 */
public class EPCModelFootnoteImporter extends AbstractEPCFootnoteImporter {

    public EPCModelFootnoteImporter(EtkProject project) {
        super(project, "EPC BM-Footnotes-Dictionary", "!!EPC Baumuster-Fußnoten (BM Footnotes Dictionary)");
    }

    @Override
    protected iPartsDictTextKindId initTextKindIdForImport() {
        return DictTxtKindIdByMADId.getInstance(getProject()).getEPCTxtKindId(DictTextKindEPCTypes.MODEL_FOOTNOTE);
    }

    @Override
    protected DictTextKindEPCTypes getEPCFootnoteTextKindType() {
        return DictTextKindEPCTypes.MODEL_FOOTNOTE;
    }

    @Override
    protected EPCFootnoteType getEPCFootnoteType() {
        return EPCFootnoteType.MODEL;
    }
}
