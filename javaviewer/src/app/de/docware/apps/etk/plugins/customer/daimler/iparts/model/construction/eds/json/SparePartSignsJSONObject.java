/*
 * Copyright (c) 2017 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.eds.json;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectInterface;
import de.docware.util.StrUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by reimer on 29.11.2017.
 */
public class SparePartSignsJSONObject implements SerializedDBDataObjectInterface {

    private List<SparePartSignAndMarketJSONObject> sparePartSignsAndMarket = new ArrayList<SparePartSignAndMarketJSONObject>();

    public SparePartSignsJSONObject() {

    }

    public SparePartSignsJSONObject(String sparePartSignsStringFromImporter) {
        fillFromString(sparePartSignsStringFromImporter);
    }

    @Override
    public void beforeSaving() {
    }

    @Override
    public void afterSaving() {
    }

    @JsonIgnore
    private void fillFromString(String sparePartSignsStringFromImporter) {
        if (StrUtils.isValid(sparePartSignsStringFromImporter)) {
            List<String> sparePartSignsAndMarkets = StrUtils.splitStringIntoSubstrings(sparePartSignsStringFromImporter, 3);
            for (String sparePartSignForMarket : sparePartSignsAndMarkets) {
                if (sparePartSignForMarket.length() == 3) {
                    String market = StrUtils.copySubString(sparePartSignForMarket, 0, 2);
                    String sparePartSign = StrUtils.copySubString(sparePartSignForMarket, 2, 1);
                    sparePartSignsAndMarket.add(new SparePartSignAndMarketJSONObject(market, sparePartSign));
                }
            }
        }
    }

    public List<SparePartSignAndMarketJSONObject> getSparePartSignsAndMarket() {
        return sparePartSignsAndMarket;
    }

    public void setSparePartSignsAndMarket(List<SparePartSignAndMarketJSONObject> sparePartSignsAndMarket) {
        this.sparePartSignsAndMarket = sparePartSignsAndMarket;
    }
}
