/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.transferobjects;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import org.apache.commons.codec.binary.Base64;

/**
 * DTO für die Bemerkung eines Arbeitsauftrags aus NutzDok, für KEMs oder SAAs.
 */
public class iPartsWSWorkBasketRemarkItem extends WSRequestTransferObject {

    private String id;
    private String refId;
    private String type;
    private String user;
    private String updateTs;
    private String data;

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "id", id);
        checkAttribValid(path, "refId", refId);
        checkAttribEnumValid(path, "type", type, iPartsWSWorkBasketItem.TYPE.class);
        checkAttribValid(path, "data", data);

        // Base64-Dekodierung versuchen -> bei einer Exception einen Fehler zurückliefern
        try {
            Base64.decodeBase64(data);
        } catch (Exception e) {
            throwRequestError(WSError.REQUEST_PARAMETER_WRONG, "Attribute 'data' has no valid Base64 encoding", path);
        }
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return null;  // Der NutzDok Webservice für Bemerkungen hat keinen JSON-Response-Cache
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getType() {
        return type;
    }

    @JsonIgnore
    public iPartsWSWorkBasketItem.TYPE getTypeAsEnum() {
        return iPartsWSWorkBasketItem.TYPE.valueOf(type);
    }

    public void setType(String type) {
        this.type = type.toUpperCase();
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUpdateTs() {
        return updateTs;
    }

    public void setUpdateTs(String updateTs) {
        this.updateTs = updateTs;
    }

    public String getData() {
        return data;
    }

    @JsonIgnore
    public byte[] getDataAsBLOB() {
        byte[] dataAsBLOB = Base64.decodeBase64(data);
        if ((dataAsBLOB != null) && (dataAsBLOB.length > 0)) {
            return dataAsBLOB;
        } else {
            return null;
        }
    }

    public void setData(String data) {
        this.data = data;
    }
}