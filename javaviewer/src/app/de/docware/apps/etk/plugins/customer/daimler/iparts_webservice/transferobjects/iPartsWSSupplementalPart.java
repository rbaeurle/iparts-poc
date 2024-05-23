/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsReplacement;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.helper.iPartsWSPartsBaseCache;

/**
 * SupplementalPart Data Transfer Object für die iParts Webservices
 * Wird verwendet in {@link iPartsWSReplacementPart}
 */
public class iPartsWSSupplementalPart extends iPartsWSPartBase {

    private String quantity;

    /**
     * Leerer Konstruktor (notwendig für die die automatische Erzeugung aus einem JSON-String)
     */
    public iPartsWSSupplementalPart() {
    }

    public iPartsWSSupplementalPart(EtkProject project, iPartsReplacement.IncludePart includePart, boolean withExtendedDescription) {
        // Dem Mitlieferteil kann kein Eindeutiger Stücklisten Eintrag zugeordnet werden, d.h. PartContext, ES1, ES2
        // können nicht belegt werden
        assign(iPartsWSPartsBaseCache.getPartBaseFromCache(project, includePart.partNumber, withExtendedDescription));

        this.quantity = includePart.quantity;
    }

    // Getter and Setter
    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

}