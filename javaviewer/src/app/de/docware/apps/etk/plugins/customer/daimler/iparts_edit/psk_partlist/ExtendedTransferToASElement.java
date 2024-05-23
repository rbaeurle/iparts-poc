/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.psk_partlist;

import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.TransferToASElement;

import java.util.HashMap;
import java.util.Map;

class ExtendedTransferToASElement extends TransferToASElement {

    private final Map<String, String> oilFieldsMap;

    public ExtendedTransferToASElement(AssemblyId assemblyId, KgTuId kgTuId, String hotspot, iPartsProduct product,
                                       EtkDataPartListEntry selectedPartlistEntry) {
        super(assemblyId, kgTuId, hotspot, product, "", null, null, null, selectedPartlistEntry);
        this.oilFieldsMap = new HashMap<>();
    }

    @Override
    public void copyValues(TransferToASElement other) {
        super.copyValues(other);
        if (other instanceof ExtendedTransferToASElement) {
            this.oilFieldsMap.clear();
            this.oilFieldsMap.putAll(((ExtendedTransferToASElement)other).oilFieldsMap);
        }
    }

    public void addOilFieldMapping(String fieldName, String value) {
        oilFieldsMap.put(fieldName, value);
    }

    public Map<String, String> getOilFieldsMap() {
        return oilFieldsMap;
    }
}
