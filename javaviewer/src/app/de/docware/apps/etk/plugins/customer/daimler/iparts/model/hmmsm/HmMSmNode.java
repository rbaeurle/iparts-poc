/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.model.hmmsm;

import de.docware.apps.etk.plugins.customer.daimler.iparts.model.AbstractiPartsNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;

/**
 * Ein HM/M/SM-Knoten mit den Bezeichnungen und sonstigen zukünfigen Attributen.
 */
public class HmMSmNode extends AbstractiPartsNode<HmMSmNodes, HmMSmNode, HmMSmType> {

    private iPartsSeriesId seriesId;
    private volatile boolean hidden;
    private volatile boolean noCalc; // bei true ist es nicht berechnungsrelevant
    private volatile boolean changeDocuRelOmittedPart; // bei true werden Wegfallsachnummern mit dem Status "ANR" auf "Offen" gesetzt

    public HmMSmNode(HmMSmType type, String number, HmMSmNode parent) {
        super(type, number, parent, new HmMSmNodes());
    }

    @Override
    public HmMSmId getId() {
        iPartsSeriesId sId = seriesId;
        if (getType() == HmMSmType.SM) {
            sId = ((HmMSmNode)getParent().getParent()).getSeriesId();
            return new HmMSmId(sId.getSeriesNumber(), getParent().getParent().getNumber(), getParent().getNumber(), getNumber());
        } else if (getType() == HmMSmType.M) {
            sId = ((HmMSmNode)getParent()).getSeriesId();
            return new HmMSmId(sId.getSeriesNumber(), getParent().getNumber(), getNumber(), "");
        } else {
            return new HmMSmId(sId.getSeriesNumber(), getNumber(), "", "");
        }
    }

    public iPartsSeriesId getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(iPartsSeriesId seriesId) {
        this.seriesId = seriesId;
    }

    /**
     * Überprüft, ob dieser HM/M/SM-Knoten ausgeblendet ist inkl. Berücksichtigung der Vater-Knoten.
     *
     * @return
     */
    public boolean isHiddenRecursively() {
        if (isHidden()) {
            return true;
        } else {
            if (getParent() instanceof HmMSmNode) {
                return ((HmMSmNode)getParent()).isHiddenRecursively();
            }
        }
        return false;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * Überprüft, ob dieser HM/M/SM-Knoten nicht berechnungsrelevant ist inkl. Berücksichtigung der Vater-Knoten.
     *
     * @return
     */
    public boolean isNoCalcRecursively() {
        if (isNoCalc()) {
            return true;
        } else {
            if (getParent() instanceof HmMSmNode) {
                return ((HmMSmNode)getParent()).isNoCalcRecursively();
            }
        }
        return false;
    }

    public boolean isNoCalc() {
        return noCalc;
    }

    public void setNoCalc(boolean noCalc) {
        this.noCalc = noCalc;
    }

    /**
     * Überprüft, ob bei diesem HM/M/SM-Knoten die Sonderberechnung für Wegfallsachnummern durchgeführt werden soll
     * inkl. Berücksichtigung der Vater-Knoten.
     *
     * @return
     */
    public boolean isChangeDocuRelOmittedPartRecursively() {
        if (isChangeDocuRelOmittedPart()) {
            return true;
        } else {
            if (getParent() instanceof HmMSmNode) {
                return ((HmMSmNode)getParent()).isChangeDocuRelOmittedPartRecursively();
            }
        }
        return false;
    }

    public boolean isChangeDocuRelOmittedPart() {
        return changeDocuRelOmittedPart;
    }

    public void setChangeDocuRelOmittedPart(boolean changeDocuRelOmittedPart) {
        this.changeDocuRelOmittedPart = changeDocuRelOmittedPart;
    }
}