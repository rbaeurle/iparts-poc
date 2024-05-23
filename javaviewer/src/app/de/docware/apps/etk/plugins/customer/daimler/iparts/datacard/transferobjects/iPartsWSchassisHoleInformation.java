package de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.transferobjects;

import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;

public class iPartsWSchassisHoleInformation implements RESTfulTransferObjectInterface {

    private List<iPartsWSfixingParts> fixingParts;

    public iPartsWSchassisHoleInformation() {
    }

    public List<iPartsWSfixingParts> getFixingParts() {
        return fixingParts;
    }

    public void setFixingParts(List<iPartsWSfixingParts> fixingParts) {
        this.fixingParts = fixingParts;
    }
}
