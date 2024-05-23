/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

public class MasterDataProductCharacteristics {

    private boolean isCarAndVanProduct;
    private boolean isTruckAndBusProduct;

    public MasterDataProductCharacteristics(boolean isCarAndVanProduct, boolean isTruckAndBusProduct) {
        this.isCarAndVanProduct = isCarAndVanProduct;
        this.isTruckAndBusProduct = isTruckAndBusProduct;
    }

    public boolean isCarAndVanProduct() {
        return isCarAndVanProduct;
    }

    public boolean isTruckAndBusProduct() {
        return isTruckAndBusProduct;
    }


}
