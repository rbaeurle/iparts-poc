package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects;

import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;

import java.util.Collection;

/**
 * Helper-Klasse für die Gleichteile
 */
public class iPartsEqualPartsHelper {

    public static boolean SHOW_EQUAL_PARTS = false;

    private boolean isTruckAndBus;
    private boolean isCarAndVan;
    private boolean isOriginalMatNumber;

    public iPartsEqualPartsHelper() {
        this.isTruckAndBus = false;
        this.isCarAndVan = false;
        this.isOriginalMatNumber = true;
    }

    public String getEqualPartNumber(EtkDataPartListEntry partListEntry) {
        return getEqualPartNumber(partListEntry.getPart());
    }

    public String getEqualPartNumber(EtkDataPart part) {
        if (isShowEqualParts() && part.existsInDB()) {
            String equalPartFieldName = null;
            if (isCarAndVan) {
                equalPartFieldName = iPartsConst.FIELD_M_MATNR_MBAG;
            } else if (isTruckAndBus) {
                equalPartFieldName = iPartsConst.FIELD_M_MATNR_DTAG;
            }
            if (equalPartFieldName != null) {
                String equalPart = getEqualPartValue(part, equalPartFieldName);
                if (!equalPart.isEmpty()) {
                    return equalPart;
                }
            }
        }
        return part.getAsId().getMatNr();
    }

    public void setProducts(Collection<iPartsProduct> products) {
        this.isTruckAndBus = false;
        this.isCarAndVan = false;

        if (SHOW_EQUAL_PARTS && (products != null)) {
            for (iPartsProduct product : products) {
                this.isTruckAndBus |= product.isTruckAndBusProduct();
                this.isCarAndVan |= product.isCarAndVanProduct();
                if (isTruckAndBus && isCarAndVan) {
                    break;
                }
            }
        }

        this.isOriginalMatNumber = isTruckAndBus == isCarAndVan; // beide false oder beide true
    }

    public boolean isShowEqualParts() {
        return !isOriginalMatNumber;
    }

    private String getEqualPartValue(EtkDataPart part, String fieldName) {
        return part.getFieldValue(fieldName);
    }

    public boolean isTruckAndBus() {
        return isTruckAndBus;
    }

    public boolean isCarAndVan() {
        return isCarAndVan;
    }
}