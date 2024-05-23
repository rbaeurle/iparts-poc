package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.sap_ctt;

public class CTTImportContainer {

    private String aSachNo;
    private String quantity;
    private String matText;
    private String etkz;

    public CTTImportContainer() {

    }

    public void setASachNo(String aSachNo) {
        this.aSachNo = aSachNo;
    }

    public String getASachNo() {
        return aSachNo;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setMatText(String matText) {
        this.matText = matText;
    }

    public String getMatText() {
        return matText;
    }

    public void setEtkZ(String etkz) {
        this.etkz = etkz;
    }

    public String getEtkZ() {
        return etkz;
    }
}
