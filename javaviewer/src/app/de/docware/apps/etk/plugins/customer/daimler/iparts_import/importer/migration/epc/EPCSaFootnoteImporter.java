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
 * EPC Importer für SA Fußnotentexte
 * Dateiname: SA_FOOTNOTES_DICTIONARY
 */
public class EPCSaFootnoteImporter extends AbstractEPCFootnoteImporter {

    public EPCSaFootnoteImporter(EtkProject project) {
        super(project, "EPC SA-Footnotes-Dictionary", "!!EPC SA-Fußnoten (SA Footnotes Dictionary)");
    }

    @Override
    protected iPartsDictTextKindId initTextKindIdForImport() {
        return DictTxtKindIdByMADId.getInstance(getProject()).getEPCTxtKindId(DictTextKindEPCTypes.SA_FOOTNOTE);
    }

    @Override
    protected DictTextKindEPCTypes getEPCFootnoteTextKindType() {
        return DictTextKindEPCTypes.SA_FOOTNOTE;
    }

    @Override
    protected EPCFootnoteType getEPCFootnoteType() {
        return EPCFootnoteType.SA;
    }
}
