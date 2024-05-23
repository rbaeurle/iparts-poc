/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.eds.json;

import de.docware.framework.modules.db.serialization.SerializedDBDataObjectInterface;

/**
 * Created by reimer on 29.11.2017.
 */
public class SparePartSignAndMarketJSONObject implements SerializedDBDataObjectInterface {

    private String market;
    private String sparePartSign;

    public SparePartSignAndMarketJSONObject() {

    }

    public SparePartSignAndMarketJSONObject(String market, String sparePartSign) {
        this.market = market;
        this.sparePartSign = sparePartSign;
    }

    @Override
    public void beforeSaving() {
    }

    @Override
    public void afterSaving() {
    }

    public String getMarket() {
        if (market != null) {
            return market;
        } else {
            return "";
        }
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getSparePartSign() {
        if (sparePartSign != null) {
            return sparePartSign;
        } else {
            return "";
        }
    }

    public void setSparePartSign(String sparePartSign) {
        this.sparePartSign = sparePartSign;
    }
}
