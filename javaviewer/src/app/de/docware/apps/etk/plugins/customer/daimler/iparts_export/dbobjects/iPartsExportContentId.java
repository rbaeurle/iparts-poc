/*
 * Copyright (c) 2020 Docware GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.util.misc.id.IdWithType;

public class iPartsExportContentId extends IdWithType {

    public static final String TYPE = "DA_iPartsExportContentId";

    protected enum INDEX {JOB_ID, DO_TYPE, DO_ID, PRODUCT_NO}

    /**
     * Der normale Konstruktor
     *
     * @param jobId
     * @param dataObjectId
     * @param productId    kann auch {@code null} sein.
     */
    public iPartsExportContentId(String jobId, IdWithType dataObjectId, iPartsProductId productId) {
        super(TYPE, new String[]{ jobId, dataObjectId.getType(), dataObjectId.toDBString(),
                                  (productId != null) ? productId.getProductNumber() : "" });
    }

    protected iPartsExportContentId(String jobId, String dataObjectType, String dataObjectId, String productNumber) {
        super(TYPE, new String[]{ jobId, dataObjectType, dataObjectId, productNumber });
    }

    /**
     * Eine ungültige ID erzeugen
     */
    public iPartsExportContentId() {
        this("", "", "", "");
    }

    public String getJobId() {
        return id[INDEX.JOB_ID.ordinal()];
    }

    public String getDataObjectType() {
        return id[INDEX.DO_TYPE.ordinal()];
    }

    public <C extends IdWithType> C getDataObjectId(Class<C> dataObjectClass) {
        IdWithType genericDataObjectId = IdWithType.fromDBString(getDataObjectType(), id[INDEX.DO_ID.ordinal()]);
        return IdWithType.fromStringArrayWithTypeFromClass(dataObjectClass, genericDataObjectId.toStringArrayWithoutType());
    }

    public String getProductNumber() {
        return id[INDEX.PRODUCT_NO.ordinal()];
    }

}
