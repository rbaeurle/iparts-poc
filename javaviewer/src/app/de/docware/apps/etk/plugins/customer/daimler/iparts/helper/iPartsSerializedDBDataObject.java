/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsCombTextId;
import de.docware.apps.etk.viewer.JavaViewerApplication;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.util.Utils;
import de.docware.util.sql.SQLStringConvert;

/**
 * iParts-spezifisches {@link SerializedDBDataObject}
 */
public class iPartsSerializedDBDataObject extends SerializedDBDataObject implements iPartsConst {

    public iPartsSerializedDBDataObject() {
    }

    public iPartsSerializedDBDataObject(DBDataObject dbDataObject, SerializedDBDataObjectState state, boolean modifiedOnly,
                                        boolean isDeleted, boolean isNew, boolean resetModifiedFlags, boolean serializeVirtualFields) {
        super(dbDataObject, state, modifiedOnly, isDeleted, isNew, resetModifiedFlags, serializeVirtualFields);
    }

    @Override
    public void merge(SerializedDBDataObject otherSerializedDBDataObject, boolean inheritUserAndDateTime, boolean compressAfterMerge) {
        super.merge(otherSerializedDBDataObject, inheritUserAndDateTime, compressAfterMerge);

        if (getState() == SerializedDBDataObjectState.REVERTED) {
            // Verwendung von JavaViewerApplication.getInstance().getProject() anstatt iPartsPlugin.getMqProject() ist
            // wichtig, damit das aktive ChangeSet auch berücksichtigt wird
            if (getType().equals(PartListEntryId.TYPE)) {
                // DAIMLER-15496 Auswertung autom. Submodulverarbeitung um gelöschte Teilepositionen erweitern
                // -> REVERTED SerializedDBDataObjects von Stücklisteneinträgen nicht entfernen, wenn diese automatisch erzeugt wurden
                if (Utils.objectEquals(getAttributeValue(FIELD_K_WAS_AUTO_CREATED, true, JavaViewerApplication.getInstance().getProject()),
                                       SQLStringConvert.booleanToPPString(true))) {
                    setKeepRevertedState(true);
                }
            } else if (getType().equals(iPartsCombTextId.TYPE)) {
                // DAIMLER-15578 Aus der autom. Submodulverarbeitung erzeugte und im gleichen Autorenauftrag gelöschte Ergänzungstexte in der Auswertung ausgeben
                // -> REVERTED SerializedDBDataObjects von Ergänzungstexten nicht entfernen, wenn die dazugehörigen Stücklisteneinträge
                // automatisch erzeugt wurden
                iPartsCombTextId combTextId = new iPartsCombTextId(getPkValues());
                EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(JavaViewerApplication.getInstance().getProject(),
                                                                                                  combTextId.getPartListEntryId());
                if (partListEntry.existsInDB() && partListEntry.getFieldValueAsBoolean(FIELD_K_WAS_AUTO_CREATED)) {
                    setKeepRevertedState(true);
                }
            }
        }
    }
}