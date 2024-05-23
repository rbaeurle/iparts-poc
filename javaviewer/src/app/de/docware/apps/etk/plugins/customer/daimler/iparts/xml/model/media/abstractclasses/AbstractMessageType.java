/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractXMLObject;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;

/**
 * Abstrakte Klasse für die gemeinsamen Methoden und Attribute von
 * {@link de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLRequest} und
 * {@link de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLResponse} und
 * {@link de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event.iPartsXMLEvent}
 */
public abstract class AbstractMessageType extends AbstractXMLObject {

    public static final String REQUEST_ID_CLUSTER_DELIMITER = "@cluster@";

    protected String fromParticipant;
    protected String toParticipant;
    protected String iPartsRequestID;
    protected String targetClusterID;
    protected String when;
    protected iPartsTransferNodeTypes messageType;

    public iPartsTransferNodeTypes getMessageType() {
        return messageType;
    }

    public String getFromParticipant() {
        return fromParticipant;
    }

    public String getToParticipant() {
        return toParticipant;
    }

    public String getiPartsRequestID() {
        return iPartsRequestID;
    }

    public String getTargetClusterID() {
        return targetClusterID;
    }

    /**
     * Liefert die Request ID für das XML zurück, die sich aus der reinen {@link #iPartsRequestID} und {@link #targetClusterID}
     * getrennt durch {@link #REQUEST_ID_CLUSTER_DELIMITER} zusammensetzt.
     *
     * @return
     */
    public String getXmlRequestID() {
        if (targetClusterID != null) {
            return iPartsRequestID + REQUEST_ID_CLUSTER_DELIMITER + targetClusterID;
        } else {
            return iPartsRequestID;
        }
    }

    /**
     * Setzt die reine {@link #iPartsRequestID} und {@link #targetClusterID} über die angegebene Request ID vom XML (getrennt
     * durch {@link #REQUEST_ID_CLUSTER_DELIMITER}).
     *
     * @param xmlRequestID
     */
    public void setXmlRequestID(String xmlRequestID) {
        if (xmlRequestID == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Response does not contain an iParts RequestId!");
            return;
        }
        int delimiterIndex = xmlRequestID.lastIndexOf(REQUEST_ID_CLUSTER_DELIMITER);
        if (delimiterIndex >= 0) {
            iPartsRequestID = xmlRequestID.substring(0, delimiterIndex);
            targetClusterID = xmlRequestID.substring(delimiterIndex + REQUEST_ID_CLUSTER_DELIMITER.length());
        } else {
            iPartsRequestID = xmlRequestID;
            targetClusterID = null;
        }
    }

    public String getWhen() {
        return when;
    }

    public boolean isEvent() {
        return (messageType == iPartsTransferNodeTypes.EVENT);
    }

    public boolean isRequest() {
        return (messageType == iPartsTransferNodeTypes.REQUEST);
    }

    public boolean isResponse() {
        return (messageType == iPartsTransferNodeTypes.RESPONSE);
    }

    /**
     * Wird aufgerufen, wenn diese Nachricht nur als Mitteilung ohne Binärdaten dienen soll und keine Datenbankaktionen
     * basierend auf dieser Nachricht stattfinden sollen.
     */
    public void convertToNotificationOnly() {
    }
}
