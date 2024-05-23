/*
 * Copyright (c) 2022 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.xml.DwXmlFile;
import de.docware.util.StrUtils;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;

/**
 * Hilfsklasse für die Kommunikation über MQ.
 */
public class MQHelper {

    private static MQHelper instance;
    public static final String TIMESTAMP_ALIAS = "timestamp";
    public static final String PREVENT_TRANSMISSION_TO_ASPLM_WARNING_MESSAGE = "!!In AS-PLM werden Wartungen durchgeführt. Bitte versuchen Sie es später erneut.";

    private final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
    private String connectionFactoryJNDI = "";
    private ConnectionFactory connectionFactory;
    private HashMap<MQChannelType, MQChannel> channels = new HashMap<>();

    /**
     * Erzeugt für den übergebenen Nachrichtentext eine abgekürzte Nachricht, die in Logs verwendet werden kann und entfernt
     * dabei optional auch alle Zeilenvorschübe.
     *
     * @param messageText
     * @param removeNewLines
     * @return
     */
    public static String getLogTextFromMessageText(String messageText, boolean removeNewLines) {
        String logText = StrUtils.makeAbbreviation(messageText, iPartsConst.MAX_MQ_MESSAGE_LOG_CHAR_COUNT);
        if (removeNewLines) {
            return logText.replace("\r\n", "").replace("\n", "");
        } else {
            return logText;
        }
    }

    public static MQHelper getInstance() {
        if (instance == null) {
            instance = new MQHelper();
        }
        return instance;
    }

    /**
     * Initialisiert die ConnectionFactory und Destinations
     *
     * @return
     * @throws javax.jms.JMSException
     */
    private boolean initConnectionFactoryAndChannels() {
        try {
            Context ctx = (Context)new InitialContext().lookup("java:comp/env");
            if (ctx == null) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "Context 'java:comp/env' not found!");
                return false;
            }

            connectionFactory = (ConnectionFactory)ctx.lookup(connectionFactoryJNDI);
            if (connectionFactory == null) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Connection factory '" + connectionFactoryJNDI + "' not found in context!");
                return false;
            }

            // Einzelne MQ Kanäle initialisieren
            MQChannel channel = null;
            for (MQChannelType channelType : MQChannelType.getChannelTypes()) {
                try {
                    channel = new MQChannel(channelType);
                    // Queues für in und out Kommunikation setzen
                    channel.setDestinationOut((Destination)ctx.lookup(channelType.getOutQueue()));
                    channel.setDestinationIn((Destination)ctx.lookup(channelType.getInQueue()));
                    if (!channel.init()) {
                        // Falls beim init() etwas schief läuft, obwohl JNDI funktioniert hat -> starte den WatchDog Thread
                        // vom Channel, damit die Reconnect Prozedur eingeleitet wird
                        Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "Could not initialize MQ Channel: " + channelType.getChannelName()
                                                                               + "; OutQueue: " + channelType.getOutQueue()
                                                                               + "; InQueue: " + channelType.getInQueue() + " -> starting channel watchdog thread");
                        channel.initWatchDogThread();
                    }
                    channels.put(channelType, channel);
                } catch (NamingException ne) {
                    // JNDI LookUp hat nicht funktioniert -> späterer Reconnect gar nicht möglich, da Ressourcen nicht da sind
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, ne);
                    continue;
                } catch (Exception e) {
                    // Exception beim init() -> Reconnect möglich
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);
                    channel.initWatchDogThread();
                    channels.put(channelType, channel);
                    continue;
                }
            }
        } catch (Exception e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);
            return false;
        }
        // Falls kein Channel initialisiert wurde (aufgrund von JNDI Fehler)
        if (channels.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Initialisiert die ConnectionFactory und die MQ Channels
     *
     * @return
     */
    public boolean initConnectionAndActors(String connectionFactoryJNDI) {
        if (connectionFactoryJNDI.isEmpty()) { // MQ Einstellungen ungültig
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ connection settings invalid: JMS ConnectionFactory: '" + connectionFactoryJNDI);
            return false;
        }

        this.connectionFactoryJNDI = connectionFactoryJNDI;

        try {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "Initializing MQ ConnectionFactory");
            boolean initSuccessful = false;
            if (initConnectionFactoryAndChannels()) {
                initSuccessful = true;
            }

            // MQ verwendet intern anscheinend Bouncy Castle (BC); diese SecurityProvider muss aber unbedingt wieder entfernt
            // werden, weil anonsten später sämtliche Verschlüsselungen fehlschlagen
            try {
                Security.removeProvider("BC");
            } catch (Throwable t) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "Removing security provider Bouncy Castle failed.");
            }

            return initSuccessful;
        } catch (Throwable e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);
            return false;
        }
    }

    /**
     * Schließt die Verbindungen und alle dazugehörigen Ressourcen inkl. Watch Dog Threads aller Channels
     */
    public void closeConnection() {
        for (MQChannel channel : channels.values()) {
            channel.closeConnection();
        }
    }

    /**
     * Weist dem übergebenen {@link MQChannel} den übergebenen {@link MQMessageReceiver} zu.
     *
     * @param channelType
     * @param mqMessageReceiver
     */
    public void addMQMessageReceiver(MQChannelType channelType, MQMessageReceiver mqMessageReceiver) {
        MQChannel channel = channels.get(channelType);
        if (channel != null) {
            channel.addMessageReceiver(mqMessageReceiver);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + channelType.getChannelName() + " is not initialized for adding an MQ receiver!");
        }
    }

    /**
     * Entfernt die Zuweisung {@link MQMessageReceiver} zu {@link MQChannel}.
     *
     * @param channelType
     * @param mqMessageReceiver
     */
    public void removeMQMessageReceiver(MQChannelType channelType, MQMessageReceiver mqMessageReceiver) {
        MQChannel channel = channels.get(channelType);
        if (channel != null) {
            channel.removeMessageReceiver(mqMessageReceiver);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + channelType.getChannelName() + " is not initialized for removing an MQ receiver!");
        }
    }

    /**
     * Erstellt den gewünschten Message String für die übergebene {@link Message} inkl. Logausgaben.
     *
     * @param message
     * @param input
     * @return
     * @throws JMSException
     */
    public String messageToString(Message message, boolean input) throws JMSException {
        String messageText = "";
        if (message == null) {
            return messageText;
        }
        if (message instanceof TextMessage) {
            messageText = ((TextMessage)message).getText();
        } else {
            messageText = message.toString();
        }
        messageText = getLogTextFromMessageText(messageText, false);

        long timestamp;
        try {
            timestamp = message.getLongProperty(MQHelper.TIMESTAMP_ALIAS);
        } catch (NumberFormatException e) {
            timestamp = 0;
        }
        GregorianCalendar calender = new GregorianCalendar();
        if (timestamp > 0) {
            calender.setTimeInMillis(timestamp);
        }

        StringBuilder builder = new StringBuilder();
        builder.append(messageText);
        builder.append("\n----------\nSent: " + ((timestamp > 0) ? TIMESTAMP_FORMAT.format(calender.getTime()) : "unknown"));
        if (!input) {
            calender.setTimeInMillis(System.currentTimeMillis());
            builder.append("; \nReceived: " + TIMESTAMP_FORMAT.format(calender.getTime()));
            calender.setTimeInMillis(message.getJMSTimestamp());
            builder.append("; \nHanded off: " + TIMESTAMP_FORMAT.format(calender.getTime()));
        }
        if (message.getJMSDestination() != null) {
            builder.append("; \nDestination: " + message.getJMSDestination());
        }
        if (message.getJMSReplyTo() != null) {
            builder.append("; \nReplyQueue: " + message.getJMSReplyTo());
        }
        if (message.getJMSMessageID() != null) {
            builder.append("; \nJMSMessageID: " + message.getJMSMessageID());
        }

        return builder.toString();
    }

    /**
     * Schickt den Inhalt einer XML Datei als {@link javax.jms.TextMessage} mit der angegebenen Nachrichten-ID.
     *
     * @param xmlFile
     * @param messageId
     * @param retryAfterException        Flag, ob bei einer Exception während des Versendens ein {@link MQChannel#reinitSession(boolean)}
     *                                   aufgerufen und bei Erfolg das Versenden erneut versucht werden soll
     * @param isSimulatedMessageResponse Flag, ob es sich um eine simulierte Nachrichten-Antwort handelt, die nicht über die
     *                                   Out-Queue sondern über die In-Queue versendet werden muss.  @throws MQException
     */
    public void sendXML(MQChannelType channelType, DwXmlFile xmlFile, String messageId, boolean retryAfterException,
                        boolean isSimulatedMessageResponse) throws MQException {
        if ((xmlFile != null) && (xmlFile.getRootElement() != null)) {
            String messageString = xmlFile.getContentAsString();
            MQChannel channel = channels.get(channelType);
            try {
                boolean channelInitialized = true;
                if (channel == null) {
                    // Channel ist nicht bereit für die Kommunikation
                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + channelType.getChannelName()
                                                                           + " is not initialized for sending XML!");
                    channelInitialized = false;
                } else if (!channel.isReadyForCommunication()) {
                    // Channel ist nicht bereit für die Kommunikation
                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + channelType.getChannelName()
                                                                           + " is not ready to communicate for sending XML! Trying to reinitialize...");
                    if (!channel.reinitSession(true)) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + channelType.getChannelName()
                                                                               + ": MQ message content could not be sent because no active MQ session exists: "
                                                                               + getLogTextFromMessageText(messageString, true));
                        channelInitialized = false;
                    }
                }
                if (!channelInitialized) {
                    throw new MQException(TranslationHandler.translate("!!Senden von MQ Messages nicht möglich, da keine aktive MQ Session für den MQ Kanal \"%1\" existiert.",
                                                                       channelType.getChannelName().getTypeName()));
                }
                channel.sendMessage(messageString, messageId, isSimulatedMessageResponse);
            } catch (MQException e) {
                throw e;
            } catch (Exception e) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);

                if (retryAfterException) {
                    // Fehler beim Versenden (z.B. Session wurde zwischenzeitlich beendet) -> Reinitialisierung versuchen
                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + channelType.getChannelName()
                                                                           + ": MQ exception while sending MQ message! Trying to reinitialize MQ session and send MQ message again...");
                }
                if (retryAfterException && (channel != null) && channel.reinitSession(true)) {
                    sendXML(channelType, xmlFile, messageId, false, isSimulatedMessageResponse); // XML Nachricht erneut versenden aber ohne erneutes ReinitSession
                } else {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + channelType.getChannelName()
                                                                           + ": MQ message content could not be sent because of MQ exception: "
                                                                           + getLogTextFromMessageText(messageString, true));
                    throw new MQException(TranslationHandler.translate("!!Fehler beim Versenden der MQ Message für für den MQ Kanal \"%1\".",
                                                                       channelType.getChannelName().getTypeName()));
                }
            }
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ message content could not be sent because XML file is null or empty.");
            throw new MQException(TranslationHandler.translate("!!Senden einer MQ Message mit leerem XML-Inhalt nicht möglich."));
        }
    }

    /**
     * Schickt den Inhalt eine {@link iPartsXMLMediaMessage} Objekts als {@link javax.jms.TextMessage}
     *
     * @param channelType
     * @param xmlMessage
     * @param messageId
     * @param isSimulated
     * @throws MQException
     */
    public void sendMessageAsXML(MQChannelType channelType, iPartsXMLMediaMessage xmlMessage, String messageId, boolean isSimulated) throws MQException {
        DwXmlFile xmlFile = XMLImportExportHelper.writeXMLFileFromMessageObject(xmlMessage, channelType,
                                                                                "iPartsOut", iPartsTransferConst.DEFAULT_XML_COMMENT, false);

        // Nachricht nur verschicken, wenn keine Simulation stattfindet
        if (!isSimulated) {
            sendXML(channelType, xmlFile, messageId, true, false);
        }
    }

    /**
     * Gibt eine neue MQ Connection zurück
     *
     * @return - MQ Connection, generiert mit ConnectionFactory
     */
    public Connection getNewConnection() {
        try {
            if (connectionFactory != null) {
                return connectionFactory.createConnection();
            }
        } catch (JMSException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);
        }
        return null;
    }

    /**
     * Fügt den übergebenen Receiver jedem aktiven Channel hinzu
     *
     * @param mqReceiver
     */
    public void addMQMessageReceiverToAllChannels(MQMessageReceiver mqReceiver) {
        for (MQChannel channel : channels.values()) {
            channel.addMessageReceiver(mqReceiver);
        }
    }

    /**
     * Entfernt den übergebenen Receiver aus jedem aktiven Channel (sofern der Channel den Receiver registriert hat)
     *
     * @param mqReceiver
     */
    public void removeMQMessageReceiverFromAllChannels(MQMessageReceiver mqReceiver) {
        for (MQChannel channel : channels.values()) {
            if (channel.hasReceiver(mqReceiver)) {
                channel.removeMessageReceiver(mqReceiver);
            }
        }
    }

    public MQChannel getChannel(MQChannelType channelType) {
        return channels.get(channelType);
    }

    /**
     * Reinitialisiert alle MQ Channel
     *
     * @param errorLogs
     */
    public boolean reinitAllChannels(boolean errorLogs) {
        boolean result = true;
        for (MQChannel channel : channels.values()) {
            if (!channel.reinitSession(errorLogs)) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Startet bzw. stoppt die MQ Consumer von allen MQ Channels (jeweils falls notwendig) abhängig von der Admin-Option
     * {@link iPartsPlugin#CONFIG_AUTO_IMPORTS_ENABLED}.
     */
    public void startOrStopAllConsumers() {
        for (MQChannel channel : channels.values()) {
            channel.startOrStopConsumer();
        }
    }

    /**
     * Reinitialisiert einen speziellen MQ Channel
     *
     * @param channelType
     * @param errorLogs
     */
    public boolean reinitChannel(MQChannelType channelType, boolean errorLogs) {
        MQChannel channel = channels.get(channelType);
        if (channel != null) {
            return channel.reinitSession(errorLogs);
        } else {
            return false;
        }
    }

    public boolean isChannelInitialized(MQChannelType type) {
        return channels.get(type) != null;
    }

    public Collection<MQChannel> getChannels() {
        return Collections.unmodifiableCollection(channels.values());
    }

    public boolean hasWorkingChannels() {
        return !channels.isEmpty();
    }

    /**
     * Überprüft, ob die Kommunikation mit AS-PLM via Adminoption deaktiviert wurde. Falls ja, wird eine Meldung
     * ausgegeben.
     *
     * @return
     */
    public static boolean checkTransmissionToASPLMConfigWithMessage() {
        if (isPreventTransmissionToASPLM()) {
            MessageDialog.showWarning(PREVENT_TRANSMISSION_TO_ASPLM_WARNING_MESSAGE);
            return false;
        }
        return true;
    }


    // Hilfsmethoden zum Abfragen der MQ Konfiguration in der Admin-Oberfläche:

    /**
     * Hilfsmethode um zu überprüfen, ob der Status "RELEASED" als "SUPPLIED" interpretiert werden soll.
     *
     * @return
     */
    public boolean isUseMQReleasedStateAsSuppliedState() {
        return iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_USE_RELEASED_STATE_AS_SUPPLIED_STATE);
    }

    public static boolean isDerivedSVGsMQRequest() {
        return iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_PIC_ORDER_REQUEST_DERIVED_PICS);
    }

    public static String getProjectAssignmentPrefixForPSK() {
        return iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_PIC_ORDER_PROJECT_ASSIGNMENT_PSK_PRODUCT);
    }

    public static String getProjectAssignmentPrefixForSpecialProduct() {
        return iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_PIC_ORDER_PROJECT_ASSIGNMENT_SPECIAL_PRODUCT);
    }

    public static boolean isPreventTransmissionToASPLM() {
        return iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_PREVENT_TRANSMISSION_TO_ASPLM);
    }

    public static boolean isCopyOfPicOrdersAllowed() {
        return iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_PIC_ORDER_ALLOW_PICORDER_COPIES);
    }
}
