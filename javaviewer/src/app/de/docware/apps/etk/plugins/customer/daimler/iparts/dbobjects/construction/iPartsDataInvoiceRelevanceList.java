/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Liste mit {@link iPartsDataInvoiceRelevance} Objekten
 */
public class iPartsDataInvoiceRelevanceList extends EtkDataObjectList<iPartsDataInvoiceRelevance> implements iPartsConst {

    public iPartsDataInvoiceRelevanceList() {
        setSearchWithoutActiveChangeSets(true);
    }

    public static iPartsDataInvoiceRelevanceList loadAllInvoiceRelevances(EtkProject project) {
        iPartsDataInvoiceRelevanceList list = new iPartsDataInvoiceRelevanceList();
        list.loadAllInvoiceRelevancesFromDB(project, DBActionOrigin.FROM_DB);
        return list;
    }

    private void loadAllInvoiceRelevancesFromDB(EtkProject project, DBActionOrigin origin) {
        clear(origin);
        searchAndFill(project, TABLE_DA_INVOICE_RELEVANCE, null, null, LoadType.COMPLETE, origin);
    }

    @Override
    protected iPartsDataInvoiceRelevance getNewDataObject(EtkProject project) {
        return new iPartsDataInvoiceRelevance(project, null);
    }
}
