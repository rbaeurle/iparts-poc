/*
 * Copyright (c) 2020 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.factoryDataAutomation;

import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableFactoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataColorTableFactory;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.ImportFactoryDataAutomationHelper;

/**
 * Klasse für die Prüfungen der automatischen Werksfreigabe bei Farben
 */
public class ColorTableFactoryDataAutomationChecker extends AbstractAutomationChecker {

    public ColorTableFactoryDataAutomationChecker(ImportFactoryDataAutomationHelper importFactoryDataAutomationHelper) {
        super(importFactoryDataAutomationHelper);
    }

    @Override
    protected String makeKey(EtkDataObject newFactoryData) {
        if (newFactoryData instanceof iPartsDataColorTableFactory) {
            iPartsColorTableFactoryId id = (iPartsColorTableFactoryId)newFactoryData.getAsId();
            iPartsColorTableFactoryId idWithoutAdat = new iPartsColorTableFactoryId(id.getTableId(), id.getPos(),
                                                                                    id.getFactory(), "",
                                                                                    id.getDataId(), id.getSdata());
            return idWithoutAdat.toDBString();
        }
        return "";
    }
}
