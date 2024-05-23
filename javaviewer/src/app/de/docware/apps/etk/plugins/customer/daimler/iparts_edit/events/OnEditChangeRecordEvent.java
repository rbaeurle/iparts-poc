/*
 * Copyright (c) 2015 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events;

import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;

import java.util.List;

/**
 * Interface für die Edit-Events bei den Stammdaten: Create, Modify, Delete
 */
public interface OnEditChangeRecordEvent {

    List<EtkDataObject> dataObjectList = new DwList<>();

    /**
     * Mit den Angaben aus tableName, Id und Attributen einen neuen Record anlegen
     *
     * @param dataConnector
     * @param tableName
     * @param id
     * @param attributes
     * @return true: Record wurde kreiert
     */
    public boolean onEditCreateRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes);

    /**
     * Mit den Angaben aus tableName, Id und Attributen einen bestehenden Record modifizieren
     *
     * @param dataConnector
     * @param tableName
     * @param id
     * @param attributes
     * @return true: Record wurde gespeichert
     */
    public boolean onEditModifyRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, IdWithType id, DBDataObjectAttributes attributes);

    /**
     * Überprüfen, ob die übergebenen Records (attributeList) gelöscht werden sollen oder können (Verwendung)
     * Aus attributeList können Records, die nicht gelöscht werden sollen gelöscht werden
     *
     * @param dataConnector
     * @param tableName
     * @param attributeList
     * @return true: die in attributeList verbliebenen Records werden gelöscht
     */
    public boolean onEditAskForDelete(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList);

    /**
     * Löschen der Records aus attributeList
     *
     * @param dataConnector
     * @param tableName
     * @param attributeList
     * @return true: Records wurden gelöscht
     */
    public boolean onEditDeleteRecordEvent(AbstractJavaViewerFormIConnector dataConnector, String tableName, DBDataObjectAttributesList attributeList);
}
