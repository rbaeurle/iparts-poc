/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper;

import de.docware.apps.etk.base.project.EtkProject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Map zum festlegen der Aggregate Idents mit zugehörigen Beschreibungen
 */
public class DatacardIdentOrderMap {

    private Map<DatacardIdentOrderTypes, DatacardIdentOrderElem> datacardIdentOrderElemMap;

    public DatacardIdentOrderMap(EtkProject project) {
        datacardIdentOrderElemMap = new LinkedHashMap<DatacardIdentOrderTypes, DatacardIdentOrderElem>();
        for (DatacardIdentOrderTypes dcIdentOrderType : DatacardIdentOrderTypes.values()) {
            datacardIdentOrderElemMap.put(dcIdentOrderType, new DatacardIdentOrderElem(project, dcIdentOrderType));
        }
    }

    public Map<DatacardIdentOrderTypes, DatacardIdentOrderElem> getDatacardIdentOrderElemMap() {
        return datacardIdentOrderElemMap;
    }

    public DatacardIdentOrderElem getDcItemElem(DatacardIdentOrderTypes dcIdentOrderType) {
        return datacardIdentOrderElemMap.get(dcIdentOrderType);
    }

    public void setDcItemElem(DatacardIdentOrderElem identOrderElem) {
        datacardIdentOrderElemMap.put(identOrderElem.getType(), identOrderElem);
    }
}
