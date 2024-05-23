/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectList;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;

import java.util.List;

/**
 * iParts-spezifische {@link SerializedDBDataObjectList}
 */
public class iPartsSerializedDBDataObjectList extends SerializedDBDataObjectList<iPartsSerializedDBDataObject> {

    public iPartsSerializedDBDataObjectList() {
    }

    public iPartsSerializedDBDataObjectList(List<? extends DBDataObject> list, List<? extends DBDataObject> deletedList,
                                            SerializedDBDataObjectState state, boolean modifiedOnly, boolean isDeleted,
                                            boolean resetModifiedFlags, boolean isCommitted, boolean serializeVirtualFields) {
        super(list, deletedList, state, modifiedOnly, isDeleted, resetModifiedFlags, isCommitted, serializeVirtualFields);
    }
}