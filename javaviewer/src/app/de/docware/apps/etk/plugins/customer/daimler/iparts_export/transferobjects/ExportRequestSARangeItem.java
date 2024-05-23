package de.docware.apps.etk.plugins.customer.daimler.iparts_export.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.util.StrUtils;

public class ExportRequestSARangeItem extends WSRequestTransferObject {

    private String from;
    private String to;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "from", from);
        if (to != null) {
            checkAttribValid(path, "to", to);
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ from, to };
    }

    @JsonIgnore
    @Override
    public String toString() {
        String result = "from: " + from;
        if (StrUtils.isValid(to)) {
            result += ", to: " + to;
        }
        return result;
    }
}
