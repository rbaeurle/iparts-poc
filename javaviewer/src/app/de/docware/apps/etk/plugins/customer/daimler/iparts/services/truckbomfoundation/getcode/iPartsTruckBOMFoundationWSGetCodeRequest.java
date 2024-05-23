/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.getcode;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.plugins.customer.daimler.iparts.services.truckbomfoundation.AbstractiPartsTruckBOMFoundationWebserviceRequest;
import de.docware.framework.modules.webservice.restful.RESTfulTransferObjectInterface;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Request für den GetCode Webservice der TruckBOM.foundation
 */
public class iPartsTruckBOMFoundationWSGetCodeRequest extends AbstractiPartsTruckBOMFoundationWebserviceRequest {

    private static final String DEFAULT_RELEASE_FLAG = "releasedOnly";

    private boolean latestVersion;
    private String releaseFlag;
    private List<iPartsTruckBOMFoundationCode> codeRequestParameter;

    public iPartsTruckBOMFoundationWSGetCodeRequest() {
    }

    public boolean isLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(boolean latestVersion) {
        this.latestVersion = latestVersion;
    }

    public String getReleaseFlag() {
        return releaseFlag;
    }

    public void setReleaseFlag(String releaseFlag) {
        this.releaseFlag = releaseFlag;
    }

    public List<iPartsTruckBOMFoundationCode> getCodeRequestParameter() {
        return codeRequestParameter;
    }

    public void setCodeRequestParameter(List<iPartsTruckBOMFoundationCode> codeRequestParameter) {
        this.codeRequestParameter = codeRequestParameter;
    }

    /**
     * Kreiert einen {@link iPartsTruckBOMFoundationWSGetCodeRequest} mit den default Werten
     *
     * @return
     */
    @JsonIgnore
    public static iPartsTruckBOMFoundationWSGetCodeRequest createDefaultRequest() {
        iPartsTruckBOMFoundationWSGetCodeRequest request = new iPartsTruckBOMFoundationWSGetCodeRequest();
        request.setReleaseFlag(DEFAULT_RELEASE_FLAG);
        request.setLatestVersion(true);
        return request;
    }

    /**
     * Befüllt die Anfrage mit den übergebenen Code
     *
     * @param edsCodes
     */
    @JsonIgnore
    public void addCodes(Set<String> edsCodes) {
        List<iPartsTruckBOMFoundationCode> allCodes = edsCodes.stream()
                .map(iPartsTruckBOMFoundationCode::new)
                .collect(Collectors.toList());
        setCodeRequestParameter(allCodes);
    }

    private static class iPartsTruckBOMFoundationCode implements RESTfulTransferObjectInterface {

        private String identifier;

        public iPartsTruckBOMFoundationCode() {

        }

        public iPartsTruckBOMFoundationCode(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }
    }
}
