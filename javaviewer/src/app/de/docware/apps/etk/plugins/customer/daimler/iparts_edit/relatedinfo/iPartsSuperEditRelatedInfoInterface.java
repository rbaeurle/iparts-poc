/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.relatedinfo;

/**
 * Interface für RelatedInfos, die auch in Super-Editoren (siehe {@link iPartsRelatedInfoSuperEditDataForm}) verwendet
 * werden können sollen.
 */
public interface iPartsSuperEditRelatedInfoInterface {

    /**
     * Berechnet die optimale Höhe für diese RelatedInfo. Dabei sollten auch interne Divider-Positionen usw. optimnal gesetzt werden.
     *
     * @return
     */
    int calculateOptimalHeight();
}