package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.sap_ctt;

import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CTTImportContainerList {

    private String saaValidity;
    private List<CTTImportContainer> importList;

    public CTTImportContainerList() {
        importList = new DwList<>();
    }

    public void clear() {
        saaValidity = "";
        importList.clear();
    }

    public boolean isEmpty() {
        return importList.isEmpty();
    }

    public List<CTTImportContainer> getImportList() {
        return importList;
    }

    public void setSaaValidity(String saaValidity) {
        this.saaValidity = saaValidity;
    }

    public String getSaaValidity() {
        return saaValidity;
    }

    public boolean add(CTTImportContainer container) {
        return importList.add(container);
    }

    public Set<String> getMatNoSet() {
        Set<String> result = new HashSet<>();
        importList.forEach((container) -> {
            String aSachNo = container.getASachNo();
            if (!StrUtils.isEmpty(aSachNo)) {
                result.add(aSachNo);
            }
        });
        return result;
    }
}
