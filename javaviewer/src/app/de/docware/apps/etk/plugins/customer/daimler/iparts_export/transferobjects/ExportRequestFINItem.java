package de.docware.apps.etk.plugins.customer.daimler.iparts_export.transferobjects;

import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;

public class ExportRequestFINItem extends WSRequestTransferObject {

    private String fin;
    private String product;

    public String getFin() {
        return fin;
    }

    public void setFin(String fin) {
        this.fin = fin;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "fin", fin);
        if (product != null) {
            checkAttribValid(path, "product", product);
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ fin, product };
    }
}
