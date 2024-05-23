/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_export.endpoints.exportpartslist;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.base.webservice.endpoints.WSAbstractEndpoint;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter.iPartsExportPictureFormat;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.transferobjects.ExportRequestFINItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.transferobjects.ExportRequestModelItem;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.transferobjects.ExportRequestSARangeItem;
import de.docware.framework.modules.gui.misc.http.HttpConstants;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.util.StrUtils;

import java.util.List;
import java.util.Set;

/**
 * Request für den {@link iPartsWSExportPartsListEndpoint}, der alle wichtigen Informationen über den anzustoßenden Export
 * enthält. Diese werden in der Tabelle DA_EXPORT_REQUEST gespeichert.
 */
public class iPartsWSExportPartsListRequest extends WSRequestTransferObject {

    public enum OutputFormat {XML, JSON}

    private String customerId;
    private String externalJobId;
    private List<String> languages;

    private boolean includeSAs;
    private boolean includeAggs;
    private boolean includePictures;
    private boolean includeAdditionalPartInformation;
    private boolean includeEinPAS;
    private boolean includeVisualNav;
    private boolean allProducts;
    private String pictureFormat;

    private String outputFormat; // Format der einzelnen Export-Ergebnisse. Diese sind in der Antwort aber archiviert.

    // Verschiedene Arten von Export-Anfragen, von denen nur eine gesetzt sein darf. Siehe checkIfValid()
    private Set<ExportRequestModelItem> reqModelList;
    private List<ExportRequestFINItem> reqFINList;
    private List<String> reqSAList;
    private List<ExportRequestSARangeItem> reqSARangeList;

    private boolean directDownload;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getExternalJobId() {
        return externalJobId;
    }

    public void setExternalJobId(String externalJobId) {
        this.externalJobId = externalJobId;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public boolean isIncludeSAs() {
        return includeSAs;
    }

    public void setIncludeSAs(boolean includeSAs) {
        this.includeSAs = includeSAs;
    }

    public boolean isIncludeAggs() {
        return includeAggs;
    }

    public void setIncludeAggs(boolean includeAggs) {
        this.includeAggs = includeAggs;
    }

    public boolean isIncludePictures() {
        return includePictures;
    }

    public void setIncludePictures(boolean includePictures) {
        this.includePictures = includePictures;
    }

    public boolean isIncludeAdditionalPartInformation() {
        return includeAdditionalPartInformation;
    }

    public void setIncludeAdditionalPartInformation(boolean includeAdditionalPartInformation) {
        this.includeAdditionalPartInformation = includeAdditionalPartInformation;
    }

    public boolean isIncludeEinPAS() {
        return includeEinPAS;
    }

    public void setIncludeEinPAS(boolean includeEinPAS) {
        this.includeEinPAS = includeEinPAS;
    }

    public boolean isIncludeVisualNav() {
        return includeVisualNav;
    }

    public void setIncludeVisualNav(boolean includeVisualNav) {
        this.includeVisualNav = includeVisualNav;
    }

    public boolean isAllProducts() {
        return allProducts;
    }

    public void setAllProducts(boolean allProducts) {
        this.allProducts = allProducts;
    }

    public String getPictureFormat() {
        return pictureFormat;
    }

    public void setPictureFormat(String pictureFormat) {
        this.pictureFormat = pictureFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    @JsonIgnore
    public OutputFormat getOutputFormatAsEnum() {
        if (StrUtils.isValid(outputFormat)) {
            try {
                return OutputFormat.valueOf(outputFormat.toUpperCase());
            } catch (Exception e) {
                // Kann eigentlich gar nicht passieren, weil outputFormat bereits in checkIfValid() geprüft wird
            }
        }

        return OutputFormat.XML; // Standardwert ist XML
    }

    @JsonIgnore
    public iPartsExportPictureFormat getPictureFormatAsEnum() {
        if (StrUtils.isValid(pictureFormat)) {
            return iPartsExportPictureFormat.getEnumFromWSValue(pictureFormat);
        } else {
            return iPartsExportPictureFormat.PNG_AND_SVG; // pictureFormat ist optional -> ohne Angabe beide Bildformate wie bisher
        }
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public Set<ExportRequestModelItem> getReqModelList() {
        return reqModelList;
    }

    public void setReqModelList(Set<ExportRequestModelItem> reqModelList) {
        this.reqModelList = reqModelList;
    }

    public List<ExportRequestFINItem> getReqFINList() {
        return reqFINList;
    }

    public void setReqFINList(List<ExportRequestFINItem> reqFINList) {
        this.reqFINList = reqFINList;
    }

    public List<String> getReqSAList() {
        return reqSAList;
    }

    public void setReqSAList(List<String> reqSAList) {
        this.reqSAList = reqSAList;
    }

    public List<ExportRequestSARangeItem> getReqSARangeList() {
        return reqSARangeList;
    }

    public void setReqSARangeList(List<ExportRequestSARangeItem> reqSARangeList) {
        this.reqSARangeList = reqSARangeList;
    }

    public boolean isDirectDownload() {
        return directDownload;
    }

    public void setDirectDownload(boolean directDownload) {
        this.directDownload = directDownload;
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "customerId", customerId);
        checkStringAttribListValid(path, "languages", languages);

        if (includeSAs) {
            WSAbstractEndpoint.throwError(HttpConstants.HTTP_STATUS_NOT_IMPLEMENTED, WSError.FUNCTION_NOT_IMPLEMENTED,
                                          "Including SAs is not yet supported", path);
        }

        if (StrUtils.isValid(outputFormat)) {
            checkAttribEnumValid(path, "outputFormat", outputFormat.toUpperCase(), OutputFormat.class);
            if (getOutputFormatAsEnum() != OutputFormat.XML) {
                WSAbstractEndpoint.throwError(HttpConstants.HTTP_STATUS_NOT_IMPLEMENTED, WSError.FUNCTION_NOT_IMPLEMENTED,
                                              "Currently only XML is supported as export output format", path);
            }
        }

        int numberOfDifferentRequestLists = getNumberOfNotEmptyRequestLists();
        if (numberOfDifferentRequestLists == 0) {
            throwRequestError(WSError.REQUEST_PARAMETER_WRONG, "All request lists are empty", path);
        } else if (numberOfDifferentRequestLists > 1) {
            throwRequestError(WSError.REQUEST_PARAMETER_WRONG, "More than one of the exclusive request lists is not empty", path);
        }

        if ((reqModelList != null) && !reqModelList.isEmpty()) {
            checkAttribListValid(path, "reqModelList", reqModelList);
        }
        if ((reqFINList != null) && !reqFINList.isEmpty()) {
            // checkAttribListValid(path, "reqFINList", reqFINList);
            WSAbstractEndpoint.throwError(HttpConstants.HTTP_STATUS_NOT_IMPLEMENTED, WSError.FUNCTION_NOT_IMPLEMENTED,
                                          "FIN export is not yet supported", path);
        }
        if ((reqSAList != null) && !reqSAList.isEmpty()) {
            checkStringAttribListValid(path, "reqSAList", reqSAList);
        }
        if ((reqSARangeList != null) && !reqSARangeList.isEmpty()) {
            checkAttribListValid(path, "reqSARangeList", reqSARangeList);
        }

        if (StrUtils.isValid(pictureFormat)) {
            checkAttribEnumValid(path, "pictureFormat", pictureFormat.toUpperCase(), iPartsExportPictureFormat.class);
        }
    }

    private int getNumberOfNotEmptyRequestLists() {
        int count = 0;
        if ((reqModelList != null) && !reqModelList.isEmpty()) {
            count++;
        }
        if ((reqFINList != null) && !reqFINList.isEmpty()) {
            count++;
        }
        if ((reqSAList != null) && !reqSAList.isEmpty()) {
            count++;
        }
        if ((reqSARangeList != null) && !reqSARangeList.isEmpty()) {
            count++;
        }
        return count;
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return null;
    }
}
