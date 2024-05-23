/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNode;

/**
 * Ein KG/TU-Knoten mit den Bezeichnungen und sonstigen zukünfigen Attributen.
 */
public class KgTuNode extends AbstractiPartsNode<KgTuNodes, KgTuNode, KgTuType> {

    private String seriesType = "";
    private String tuVar = "";

    public KgTuNode(KgTuType type, String number, KgTuNode parent) {
        super(type, number, parent, new KgTuNodes());
    }

    public String getSeriesType() {
        return seriesType;
    }

    public void setSeriesType(String seriesType) {
        this.seriesType = seriesType;
    }

    public String getTuVar() {
        return tuVar;
    }

    public void setTuVar(String tuVar) {
        this.tuVar = tuVar;
    }

    @Override
    public KgTuId getId() {
        if (getType() == KgTuType.TU) {
            return new KgTuId(getParent().getNumber(), getNumber());
        } else {
            return new KgTuId(getNumber(), "");
        }
    }
}
