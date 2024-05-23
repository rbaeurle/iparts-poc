/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction;

import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.framework.modules.db.DBActionOrigin;

/**
 * Interface um die Gemeinsamkeiten der Werksdaten von Stücklisteneinträgen (DA_FACTORY_DATA)
 * und Varianten bzw. Variantentabellen (DA_COLORTABLE_FACTORY) abzubilden.
 * Hauptsächlich verwendet um beim Kopieren und Einfügen Code Duplikate zu vermeiden
 */
public interface iPartsEtkDataObjectFactoryDataInterface {

    iPartsDataReleaseState getReleaseState();

    String getDataId();

    String getFactory();

    String getFieldValue(String attributeName);

    void setAttributeValue(String attributeName, String value, DBActionOrigin origin);
}
