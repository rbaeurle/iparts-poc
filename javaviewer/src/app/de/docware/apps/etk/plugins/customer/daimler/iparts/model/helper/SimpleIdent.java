/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DCAggregateTypes;

/**
 * Minimale Implementierung eines Aggregate-Idents
 * Wird übergangsweise verwendet, um Nullpointer bei noch nicht definierten Aggregate-Typen zu verhindern.
 */
public class SimpleIdent extends AggregateIdent {

    public static final String TYPE = "DA_SimpleIdent";

    private DatacardIdentOrderTypes identOrderType;

    public SimpleIdent(EtkProject project, String aggregateIdent, DCAggregateTypes aggType, DatacardIdentOrderTypes identOrderType) {
        super(project, TYPE, aggregateIdent, aggType);
        this.identOrderType = identOrderType;
    }

    @Override
    public DatacardIdentOrderTypes getIdentType() {
        return identOrderType;
    }
}
