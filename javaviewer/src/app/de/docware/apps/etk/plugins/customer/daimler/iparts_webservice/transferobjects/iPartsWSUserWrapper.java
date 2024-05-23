/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.transferobjects;

import com.owlike.genson.Genson;
import com.owlike.genson.JsonBindingException;
import de.docware.apps.etk.base.webservice.transferobjects.WSRequestTransferObject;
import de.docware.apps.etk.plugins.customer.daimler.iparts_webservice.iPartsWebservicePlugin;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.webservice.restful.RESTfulWebApplicationException;
import de.docware.framework.modules.webservice.restful.WSError;
import de.docware.framework.utils.JSONUtils;

/**
 * Wrapper für das JAVA/JSON Objekt {@link iPartsWSUserInfo}
 */
public class iPartsWSUserWrapper extends WSRequestTransferObject {

    private iPartsWSUserInfo user;

    public iPartsWSUserWrapper() {
    }

    public iPartsWSUserWrapper(iPartsWSUserInfo info) {
        this.user = info;
    }

    public iPartsWSUserInfo getUser() {
        return user;
    }

    public void setUser(iPartsWSUserInfo user) {
        // Feld "user" nur dann überschreiben, wenn es noch null ist oder nicht im Webservice-Token enthalten war
        if ((this.user == null) || !this.user.isFromToken()) {
            this.user = user;
        }
    }

    @Override
    public void checkIfValid(String path) throws RESTfulWebApplicationException {
        checkAttribValid(path, "user", user);
    }

    @Override
    public Object[] createCacheKeyObjectsForResponseCache() {
        return new Object[]{ user };
    }

    @Override
    public void setSecurePayload(String securePayload) {
        if (securePayload != null) {
            try {
                // createGenson(false) reicht, weil Genson hier nur für die Deserialisierung verwendet wird
                Genson genson = JSONUtils.createGenson(false);

                iPartsWSUserInfo userInfoFromToken = genson.deserialize(securePayload, iPartsWSUserInfo.class);
                if (userInfoFromToken != null) {
                    userInfoFromToken.setIsFromToken(true);

                    // einen evtl. vorhandenen Wert für das Feld "user" durch die iPartsWSUserInfo vom Token überschreiben
                    user = userInfoFromToken;
                    String userId = user.getUserId();
                    String language = user.getLang1();
                    if (userId == null) {
                        userId = "";
                    }
                    if (language == null) {
                        language = "";
                    }
                    Logger.log(iPartsWebservicePlugin.LOG_CHANNEL_TOKEN, LogType.DEBUG, "Valid token in request header found with userId \""
                                                                                        + userId + "\" and language \""
                                                                                        + language + "\": " + securePayload);
                    return;
                }
            } catch (JsonBindingException e) {
                throwRequestError(WSError.REQUEST_INVALID_TOKEN, "Token payload in request header is no valid JSON user info: "
                                                                 + e.getMessage(), null);
            }
        }

        // Kein gültiges Token gefunden -> Fallback auf UserInfo aus dem POST-Payload
        String logMessage = "No valid token in request header found.";
        if (getUser() != null) {
            String userId = getUser().getUserId();
            String language = getUser().getLanguage();
            if (userId == null) {
                userId = "";
            }
            if (language == null) {
                language = "";
            }
            logMessage += " Using fallback user info from POST payload with userId \"" + userId + "\" and language \""
                          + language + "\".";
        }
        Logger.log(iPartsWebservicePlugin.LOG_CHANNEL_TOKEN, LogType.INFO, logMessage);

        // kein super-Aufruf, weil wir uns securePayload nicht zusätzlich auch noch als String merken müssen
    }
}
