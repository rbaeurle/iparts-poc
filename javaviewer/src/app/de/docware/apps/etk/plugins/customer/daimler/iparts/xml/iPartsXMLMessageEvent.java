/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml;

import com.owlike.genson.annotation.JsonIgnore;
import de.docware.apps.etk.base.project.events.AbstractEtkClusterEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.xml.DwXmlFile;

/**
 * Event zur Verteilung von AS-PLM XML Nachrichten, die über MQ empfangen wurden.
 */
public class iPartsXMLMessageEvent extends AbstractEtkClusterEvent {

    private String xmlContent;
    private AbstractMQMessage xmlMessage;
    private iPartsMQChannelTypeNames channelTypeName;
    private boolean notificationOnly;

    public iPartsXMLMessageEvent() {
    }

    /**
     * Erzeugt einen Event basierend auf einer XML Nachricht als String für den angegebenen <i>channelTypeName</i>.
     *
     * @param xmlContent
     * @param channelTypeName
     * @param notificationOnly Flag, ob diese Nachricht nur als Mitteilung ohne Binärdaten dient und keine Datenbankaktionen
     *                         basierend auf dieser Nachricht stattfinden sollen
     */
    public iPartsXMLMessageEvent(String xmlContent, iPartsMQChannelTypeNames channelTypeName, boolean notificationOnly) {
        this.xmlContent = xmlContent;
        this.channelTypeName = channelTypeName;
        this.notificationOnly = notificationOnly;
    }

    /**
     * Erzeugt einen Event basierend auf einem XML Nachrichtenobjekt für den angegebenen <i>channelTypeName</i>.
     * <br/>Das Flag {@link #notificationOnly} wird aus dem XML Nachrichtenobjekt übernommen.
     *
     * @param xmlMessage
     * @param channelTypeName
     */
    public iPartsXMLMessageEvent(AbstractMQMessage xmlMessage, iPartsMQChannelTypeNames channelTypeName) {
        this.xmlMessage = xmlMessage;
        this.channelTypeName = channelTypeName;
        this.notificationOnly = xmlMessage.isNotificationOnly();
    }

    /**
     * XML Nachricht als String (wird bei Bedarf aus {@link #xmlMessage} erzeugt)
     *
     * @return
     */
    public String getXmlContent() {
        if ((xmlContent == null) && (xmlMessage != null)) {
            DwXmlFile xmlFile = new DwXmlFile(xmlMessage.getAsDwXMLNode(iPartsTransferConst.ASPLM_XML_NAMESPACE_PREFIX));
            xmlContent = xmlFile.getContentAsString();
        }
        return xmlContent;
    }

    /**
     * XML Nachricht als String
     *
     * @param xmlContent
     */
    public void setXmlContent(String xmlContent) {
        this.xmlContent = xmlContent;
    }

    /**
     * XML Nachrichtenobjekt (wird bei Bedarf aus {@link #xmlContent} erzeugt)
     *
     * @return
     */
    @JsonIgnore
    public AbstractMQMessage getXmlMessage() {
        if ((xmlMessage == null) && (xmlContent != null)) {
            try {
                xmlMessage = XMLImportExportHelper.buildMessageFromXmlContent(xmlContent, channelTypeName,
                                                                              notificationOnly, false);
            } catch (Exception e) {
                Logger.getLogger().throwRuntimeException(e);
            }
        }
        return xmlMessage;
    }

    /**
     * XML Nachrichtenobjekt
     *
     * @param xmlMessage
     */
    @JsonIgnore
    public void setXmlMessage(AbstractMQMessage xmlMessage) {
        this.xmlMessage = xmlMessage;
    }

    /**
     * MQ Kanalname für diese Nachricht
     *
     * @return
     */
    public iPartsMQChannelTypeNames getChannelTypeName() {
        return channelTypeName;
    }

    /**
     * MQ Kanalname für diese Nachricht
     *
     * @param channelTypeName
     */
    public void setChannelTypeName(iPartsMQChannelTypeNames channelTypeName) {
        this.channelTypeName = channelTypeName;
    }

    /**
     * Flag, ob diese Nachricht nur als Mitteilung ohne Binärdaten dient und keine Datenbankaktionen basierend auf dieser
     * Nachricht stattfinden sollen.
     *
     * @return
     */
    public boolean isNotificationOnly() {
        return notificationOnly;
    }

    /**
     * Flag, ob diese Nachricht nur als Mitteilung ohne Binärdaten dient und keine Datenbankaktionen basierend auf dieser
     * Nachricht stattfinden sollen.
     *
     * @param notificationOnly
     */
    public void setNotificationOnly(boolean notificationOnly) {
        this.notificationOnly = notificationOnly;
    }
}
