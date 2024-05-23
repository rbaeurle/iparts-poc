/*
 * Copyright (c) 2019 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

import de.docware.apps.etk.base.relatedinfo.main.forms.RelatedInfoBaseFormIConnector;
import de.docware.apps.etk.base.relatedinfo.main.model.EtkRelatedInfoBaseImpl;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayOption;
import de.docware.apps.etk.base.relatedinfo.main.model.RelatedInfoDisplayState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.relatedinfo.AbstractRelatedInfoPartlistDataForm;

import java.util.EnumSet;

/**
 * Abstrakte Superklasse für die Related Edit von einem Super-Edit für Stücklisteneinträge.
 */
public abstract class AbstractSuperEditRelatedInfo extends EtkRelatedInfoBaseImpl implements iPartsConst {

    public AbstractSuperEditRelatedInfo(String name, String displayText) {
        super(name, displayText, false, true, EnumSet.of(RelatedInfoDisplayOption.WORKBENCH,
                                                         RelatedInfoDisplayOption.DEFVISIBLE,
                                                         RelatedInfoDisplayOption.COMMONENTRY,
                                                         RelatedInfoDisplayOption.NOTINVIEWER));
    }

    @Override
    public boolean hasDisplayDialog() {
        return true;
    }

    @Override
    public RelatedInfoDisplayState getState(RelatedInfoBaseFormIConnector dataConnector) {
        if (dataConnector.getRelatedInfoData().isAssembly()) {
            return RelatedInfoDisplayState.HIDE;
        } else {
            if (!iPartsRelatedInfoSuperEditDataForm.relatedInfoIsVisible(dataConnector.getRelatedInfoData().getAsPartListEntry(dataConnector.getProject()),
                                                                         AbstractRelatedInfoPartlistDataForm.isEditContext(dataConnector, false))) {
                return RelatedInfoDisplayState.HIDE;
            }
        }

        return RelatedInfoDisplayState.ENABLED;
    }
}