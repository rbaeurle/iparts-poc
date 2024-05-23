/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml;

import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsMQChannelTypeNames;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQException;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQMessageReceiver;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.dialog.iPartsXMLMixedTable;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.event.AbstractXMLChangeEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLRequestOperation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.event.iPartsXMLEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLRequest;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.modules.gui.session.SessionManager;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sendet und empfängt MQ Nachrichten.
 */
public class iPartsMQMessageManager implements MQMessageReceiver, iPartsConst {

    private static final Map<String, iPartsMQMessageManager> INSTANCES = new HashMap<>();
    private static final int PURGE_MAP_COUNTER_LIMIT = 100;
    private static final int PURGE_MAP_MIN_SIZE = 500;
    private static final String KEY_BROADCAST = "*";
    private static final int BROADCAST_OPTIONS = 4;

    // Maps für die Verknüpfung zwischen Message IDs und AbstractMessageListener. Unterschieden wird zwischen Listener
    // für XML Nachrichten und Listener für Nachrichten, die in ihrer reinen Textform verarbeitet werden.
    // Broadcast ist '*' MessageTyp = iPartsTransferNodeTypes.alias und eine einzelner Messagetyp ist eine GUID. Diese Strings können sich nicht
    // überschneiden, weshalb wir in der HashMap nur einen Key für eigentlich 3 verschiedene Dinge verwenden können
    private final Map<String, List<WeakReference<AbstractMessageListener>>> keyToXMLListenersMap = new HashMap<>();
    private final Map<String, List<WeakReference<AbstractMessageListener>>> keyToTextContentListenersMap = new HashMap<>();
    private int purgeMapsCounter;
    private boolean synchronizedMode;

    public static iPartsMQMessageManager getInstance(String identifier) {
        iPartsMQMessageManager result = INSTANCES.get(identifier);
        if (result == null) {
            result = new iPartsMQMessageManager();
            INSTANCES.put(identifier, result);
        }
        return result;
    }

    private iPartsMQMessageManager() {
    }

    /**
     * Flag, ob alle XML Nachrichten synchronisiert und damit streng hintereinander bearbeitet werden sollen (z.B. für Importe).
     *
     * @return
     */
    public boolean isSynchronizedMode() {
        return synchronizedMode;
    }

    /**
     * Flag, ob alle XML Nachrichten synchronisiert und damit streng hintereinander bearbeitet werden sollen (z.B. für Importe).
     *
     * @param synchronizedMode
     */
    public void setSynchronizedMode(boolean synchronizedMode) {
        this.synchronizedMode = synchronizedMode;
    }


    // ################ Methoden für Nachrichten, die direkt verarbeitet werden (als String) ################

    /**
     * Registriert den {@link AbstractTextContentMessageListener} für die angegebenen Kanal-Typen.
     *
     * @param textContentMessageListener
     * @param channelTypes
     */
    public void addTextContentMessageListenerForChannelTypes(AbstractTextContentMessageListener textContentMessageListener,
                                                             MQChannelType... channelTypes) {
        String[] channelTypeNames = getChannelTypeNames(channelTypes);
        if (channelTypeNames == null) {
            return;
        }
        addTextContentMessageListenerForKeys(textContentMessageListener, channelTypeNames);
    }

    /**
     * Registriert den {@link AbstractTextContentMessageListener} für die angegebenen Schlüssel.
     *
     * @param textContentListener
     * @param keys
     */
    private void addTextContentMessageListenerForKeys(AbstractTextContentMessageListener textContentListener, String... keys) {
        if (checkMessageKeys(textContentListener, keys)) {
            synchronized (keyToTextContentListenersMap) {
                addMessageListenerForKeys(textContentListener, keyToTextContentListenersMap, keys);
            }
        }
    }

    /**
     * Deregistriert den {@link AbstractTextContentMessageListener} für die angegebenen Kanal-Typen. Wenn keine
     * Kanal-Typen angegeben werden, wird der {@link AbstractTextContentMessageListener} komplett deregistriert.
     *
     * @param textContentListener
     * @param channelTypes
     */
    public void removeTextContentMessageListenerForChannelTypes(AbstractTextContentMessageListener textContentListener,
                                                                MQChannelType... channelTypes) {
        if ((channelTypes == null) || (channelTypes.length == 0)) {
            removeTextContentMessageListenerForKeys(textContentListener);
            return;
        }

        removeTextContentMessageListenerForKeys(textContentListener, getChannelTypeNames(channelTypes));
    }

    /**
     * Deregistriert den {@link AbstractTextContentMessageListener} für die angegebenen Schlüssel. Wenn keine Schlüssel
     * angegeben werden, wird der {@link AbstractTextContentMessageListener} für alle Schlüssel deregistriert.
     *
     * @param messageListener
     * @param keys            Kann auch {@code null} oder leer sein, um den {@link AbstractTextContentMessageListener} für alle Schlüssel
     *                        zu deregistrieren.
     */
    private void removeTextContentMessageListenerForKeys(AbstractTextContentMessageListener messageListener, String... keys) {
        synchronized (keyToTextContentListenersMap) {
            removeMessageListenerForKeys(messageListener, keyToTextContentListenersMap, keys);
        }
    }

    /**
     * Wird aufgerufen, wenn eine MQ Message mit dem angegebenen <i>textContent</i> auf dem angegebenen {@link MQChannelType}
     * empfangen wurde und als Text (String) verarbeitet werden soll.
     *
     * @param textContent
     * @param channelType
     * @throws IOException
     * @throws SAXException
     */
    public void textContentMessageReceived(final String textContent, final MQChannelType channelType) throws IOException, SAXException {
        // Benachrichtigung der XMLMessageListener in der Reihenfolge: Broadcast, Kanal-Typ, Nachrichten-Typ, Nachrichten-ID
        String[] keys = new String[2];

        // zuerst Broadcast
        keys[0] = KEY_BROADCAST;

        // dann der Kanal-Typ
        keys[1] = channelType.getChannelName().getTypeName();

        // TextContentMessageListener benachrichtigen
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            List<WeakReference<AbstractMessageListener>> listeners;
            synchronized (keyToTextContentListenersMap) {
                listeners = keyToTextContentListenersMap.get(key);
                if (listeners != null) {
                    listeners = new ArrayList<>(listeners);
                }
            }
            if (listeners != null) {
                for (WeakReference<? extends AbstractMessageListener> listenerRef : listeners) {
                    final AbstractMessageListener listener = listenerRef.get();
                    if (listener != null) {
                        if (listener instanceof AbstractTextContentMessageListener) {
                            AbstractTextContentMessageListener textContentListener = (AbstractTextContentMessageListener)listener;
                            final Session listenerSession = textContentListener.getSession();
                            if ((listenerSession != null) && listenerSession.canHandleGui()) {
                                FrameworkThread frameworkThread = listenerSession.startChildThread(thread -> {
                                    // GUI-Änderungen müssen mit invokeThreadSafe durchgeführt werden
                                    listenerSession.invokeThreadSafe(() -> {
                                        // Auswerten vom Rückgabewert zum Verhindern der weiteren Nachrichten-
                                        // Verteilung hier nicht möglich, weil wir uns in einem separaten Thread
                                        // befinden
                                        try {
                                            textContentListener.messageReceived(textContent, channelType);
                                        } catch (Exception e) {
                                            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);
                                        }
                                    });
                                });

                                // um Parallelität zu vermeiden und Synchronisierung zu erzwingen, muss bei isSynchronizedMode()
                                // auf den Thread gewartet werden
                                if (isSynchronizedMode()) {
                                    frameworkThread.waitFinished();
                                }
                            } else {
                                try {
                                    boolean preventMessageDistribution = textContentListener.messageReceived(textContent, channelType);
                                    if (preventMessageDistribution) {
                                        break;
                                    }
                                } catch (Exception e) {
                                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!channelType.isOmitLogMessageForEverySingleMessage()) {
            logProcessedMQMessage(channelType, textContent);
        }
    }

    // ################ Methoden für Nachrichten, die als XML Datei verarbeitet werden ################

    /**
     * Registriert den {@link AbstractXMLMessageListener} für alle eingehenden Nachrichten (Broadcast).
     *
     * @param xmlListener
     */
    public void addXMLMessageListenerForBroadcast(AbstractXMLMessageListener xmlListener) {
        addXMLMessageListenerForKeys(xmlListener, KEY_BROADCAST);
    }

    /**
     * Registriert den {@link AbstractXMLMessageListener} für die angegebenen Nachrichten-Typen.
     *
     * @param xmlListener
     * @param messageTypes
     */
    public void addXMLMessageListenerForMessageTypes(AbstractXMLMessageListener xmlListener, iPartsTransferNodeTypes... messageTypes) {
        if ((messageTypes == null) || (messageTypes.length == 0)) {
            return;
        }

        String[] messageTypeNames = new String[messageTypes.length];
        int i = 0;
        for (iPartsTransferNodeTypes responseType : messageTypes) {
            messageTypeNames[i] = responseType.getAlias();
            i++;
        }
        addXMLMessageListenerForKeys(xmlListener, messageTypeNames);
    }

    /**
     * Registriert den {@link AbstractXMLMessageListener} für die angegebenen Kanal-Typen.
     *
     * @param xmlListener
     * @param channelTypes
     */
    public void addXMLMessageListenerForChannelTypes(AbstractXMLMessageListener xmlListener, MQChannelType... channelTypes) {
        String[] channelTypeNames = getChannelTypeNames(channelTypes);
        if (channelTypeNames == null) {
            return;
        }
        addXMLMessageListenerForKeys(xmlListener, channelTypeNames);
    }

    /**
     * Registriert den {@link AbstractXMLMessageListener} für die angegebenen Nachrichten-IDs.
     *
     * @param xmlListener
     * @param messageIds
     */
    public void addXMLMessageListener(AbstractXMLMessageListener xmlListener, String... messageIds) {
        addXMLMessageListenerForKeys(xmlListener, messageIds);
    }

    /**
     * Registriert den {@link AbstractXMLMessageListener} für die angegebenen Schlüssel.
     *
     * @param xmlListener
     * @param keys
     */
    private void addXMLMessageListenerForKeys(AbstractXMLMessageListener xmlListener, String... keys) {
        if (checkMessageKeys(xmlListener, keys)) {
            synchronized (keyToXMLListenersMap) {
                addMessageListenerForKeys(xmlListener, keyToXMLListenersMap, keys);
            }
        }
    }

    /**
     * Deregistriert den {@link AbstractXMLMessageListener} für alle eingehenden Nachrichten (Broadcast).
     *
     * @param xmlListener
     */
    public void removeXMLMessageListenerForBroadcast(AbstractXMLMessageListener xmlListener) {
        removeXMLMessageListenerForKeys(xmlListener, KEY_BROADCAST);
    }

    /**
     * Deregistriert den {@link AbstractXMLMessageListener} für die angegebenen Nachrichten-IDs. Wenn keine Nachrichten-IDs
     * angegeben werden, wird der {@link AbstractXMLMessageListener} komplett deregistriert.
     *
     * @param xmlListener
     * @param messageIds  Kann auch {@code null} oder leer sein, um den {@link AbstractXMLMessageListener} für alle Nachrichten-IDs
     *                    zu deregistrieren.
     */
    public void removeXMLMessageListener(AbstractXMLMessageListener xmlListener, String... messageIds) {
        removeXMLMessageListenerForKeys(xmlListener, messageIds);
    }

    /**
     * Deregistriert den {@link AbstractXMLMessageListener} für die angegebenen Nachrichten-Typen. Wenn keine
     * Nachrichten-Typen angegeben werden, wird der {@link AbstractXMLMessageListener} komplett deregistriert.
     *
     * @param xmlListener
     * @param messageTypes
     */
    public void removeXMLMessageListenerForMessageTypes(AbstractXMLMessageListener xmlListener, iPartsTransferNodeTypes... messageTypes) {
        if ((messageTypes == null) || (messageTypes.length == 0)) {
            removeXMLMessageListenerForKeys(xmlListener);
            return;
        }

        String[] messageTypeNames = new String[messageTypes.length];
        int i = 0;
        for (iPartsTransferNodeTypes responseType : messageTypes) {
            messageTypeNames[i] = responseType.getAlias();
            i++;
        }
        removeXMLMessageListenerForKeys(xmlListener, messageTypeNames);
    }


    /**
     * Deregistriert den {@link AbstractXMLMessageListener} für die angegebenen Kanal-Typen. Wenn keine
     * Kanal-Typen angegeben werden, wird der {@link AbstractXMLMessageListener} komplett deregistriert.
     *
     * @param xmlListener
     * @param channelTypes
     */
    public void removeXMLMessageListenerForChannelTypes(AbstractXMLMessageListener xmlListener, MQChannelType... channelTypes) {
        if ((channelTypes == null) || (channelTypes.length == 0)) {
            removeXMLMessageListenerForKeys(xmlListener);
            return;
        }

        removeXMLMessageListenerForKeys(xmlListener, getChannelTypeNames(channelTypes));
    }

    /**
     * Deregistriert den {@link AbstractXMLMessageListener} für die angegebenen Schlüssel. Wenn keine Schlüssel
     * angegeben werden, wird der {@link AbstractXMLMessageListener} für alle Schlüssel deregistriert.
     *
     * @param messageListener
     * @param keys            Kann auch {@code null} oder leer sein, um den {@link AbstractXMLMessageListener} für alle Schlüssel
     *                        zu deregistrieren.
     */
    private void removeXMLMessageListenerForKeys(AbstractMessageListener messageListener, String... keys) {
        synchronized (keyToXMLListenersMap) {
            removeMessageListenerForKeys(messageListener, keyToXMLListenersMap, keys);
        }
    }

    private String[] getXMLMessageReceiverKeys(AbstractMQMessage xmlMQMessage, MQChannelType channelType) {
        if ((xmlMQMessage == null) || (channelType == null)) {
            return null;
        }
        // Benachrichtigung der XMLMessageListener in der Reihenfolge: Broadcast, Kanal-Typ, Nachrichten-Typ, Nachrichten-ID
        String[] keys = new String[BROADCAST_OPTIONS];

        // zuerst Broadcast
        keys[0] = KEY_BROADCAST;

        // dann der Kanal-Typ
        keys[1] = channelType.getChannelName().getTypeName();
        xmlMQMessage.setMQChannelType(channelType);
        String messageId = null;

        // dann der Nachrichten-Typ
        if (xmlMQMessage.isOfType(iPartsXMLMediaMessage.TYPE)) {
            iPartsXMLMediaMessage xmlMediaMessage = (iPartsXMLMediaMessage)xmlMQMessage;
            messageId = xmlMediaMessage.getTypeObject().getiPartsRequestID();
            if (xmlMediaMessage.isResponse()) {
                setReceiverKeyFromResponse(xmlMediaMessage, channelType, keys);
            } else if (xmlMediaMessage.isRequest()) {
                setReceiverKeyFromRequest(xmlMediaMessage, channelType, keys);
            } else if (xmlMediaMessage.isEvent()) {
                setReceiverKeyFromEvent(xmlMediaMessage, channelType, keys);
            }
        } else if (xmlMQMessage.isOfType(iPartsXMLMixedTable.TYPE)) {
            iPartsXMLMixedTable mixedTable = (iPartsXMLMixedTable)xmlMQMessage;
            keys[2] = mixedTable.getNodeType().getAlias();

        }

        // zuletzt die Nachrichten-ID
        keys[3] = messageId;

        return keys;
    }

    /**
     * Versendet eine {@link iPartsXMLMediaMessage} über MQ und registriert optional auch gleich einen {@link AbstractXMLMessageListener}.
     *
     * @param xmlMessage
     * @param xmlListener
     * @return Nachrichten-ID aus der {@link iPartsXMLMediaMessage}
     * @throws MQException
     */
    public String sendXMLMessageWithMQ(MQChannelType channelType, iPartsXMLMediaMessage xmlMessage, AbstractXMLMessageListener xmlListener, boolean isSimulated) throws MQException {
        String messageId = xmlMessage.getTypeObject().getiPartsRequestID();

        if (xmlListener != null) {
            addXMLMessageListener(xmlListener, messageId);
        }

        // Nachricht verschicken (muss nach dem Registrieren des Listeners gemacht werden!)
        MQHelper.getInstance().sendMessageAsXML(channelType, xmlMessage, messageId, isSimulated);

        return messageId;
    }

    /**
     * Versendet eine {@link iPartsXMLMediaMessage} über MQ.
     *
     * @param xmlMessage
     * @return Nachrichten-ID aus der {@link iPartsXMLMediaMessage}
     * @throws MQException
     */
    public String sendXMLMessageWithMQ(MQChannelType channelType, iPartsXMLMediaMessage xmlMessage, boolean isSimulated) throws MQException {
        return sendXMLMessageWithMQ(channelType, xmlMessage, null, isSimulated);
    }

    /**
     * Wird aufgerufen, wenn eine MQ Message mit dem angegebenen <i>xmlContent</i> auf dem angegebenen {@link MQChannelType}
     * empfangen wurde und als XML Datei (XML-Objekte) verarbeitet werden soll.
     *
     * @param xmlContent
     * @param channelType
     * @param notificationOnly       Flag, ob die Nachricht nur als Mitteilung ohne Binärdaten dient und keine Datenbankaktionen
     *                               basierend auf dieser Nachricht stattfinden sollen
     * @param writeDebugFilesEnabled Flag, ob das Abspeichern der XML-Datei sowie weiterer Dateien wie z.B. Zeichnungen
     *                               abhängig vom Inhalt und den Admin-Einstellungen zu Debug-Zwecken erlaubt sein soll
     * @throws IOException
     * @throws SAXException
     */
    public void xmlMessageReceived(final String xmlContent, final MQChannelType channelType, boolean notificationOnly, boolean writeDebugFilesEnabled) throws IOException, SAXException {
        final AbstractMQMessage xmlMQMessage = XMLImportExportHelper.buildMessageFromXmlContent(xmlContent, channelType.getChannelName(),
                                                                                                notificationOnly, writeDebugFilesEnabled);
        if (xmlMQMessage != null) {
            String targetClusterId = getTargetClusterIdFromMessage(xmlMQMessage);
            if (StrUtils.isValid(targetClusterId)) { // ein expliziter Ziel-Cluster-Knoten wurde angegeben
                String clusterId = ApplicationEvents.getClusterId();
                if (!Utils.objectEquals(targetClusterId, clusterId)) { // Weiterleitung zu einem anderen Ziel-Cluster-Knoten
                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "Forwarding received MQ message to cluster node ID \""
                                                                           + targetClusterId + "\" from this cluster node ID \""
                                                                           + clusterId + "\". Content of received message: "
                                                                           + MQHelper.getLogTextFromMessageText(xmlContent, true));
                    ApplicationEvents.fireEventInAllProjectsAndClusters(new iPartsXMLMessageEvent(xmlMQMessage, channelType.getChannelName()),
                                                                        false, false, true, targetClusterId, null);
                    return;
                }
            }

            String[] keys = getXMLMessageReceiverKeys(xmlMQMessage, channelType);
            if (keys != null) {
                // XMLMessageListener benachrichtigen
                for (String key : keys) {
                    if (key == null) {
                        continue;
                    }
                    List<WeakReference<AbstractMessageListener>> xmlListeners;
                    synchronized (keyToXMLListenersMap) {
                        xmlListeners = keyToXMLListenersMap.get(key);
                        if (xmlListeners != null) {
                            xmlListeners = new ArrayList<>(xmlListeners);
                        }
                    }
                    if (xmlListeners != null) {
                        for (WeakReference<? extends AbstractMessageListener> xmlListenerRef : xmlListeners) {
                            AbstractMessageListener abstractXmlListener = xmlListenerRef.get();
                            if (abstractXmlListener instanceof AbstractXMLMessageListener) {
                                AbstractXMLMessageListener xmlListener = (AbstractXMLMessageListener)abstractXmlListener;
                                final Session xmlListenerSession = xmlListener.getSession();
                                if ((xmlListenerSession != null) && xmlListenerSession.canHandleGui()) {
                                    // messageReceived() erfolgt aus keinem Session-Thread -> Session-Kind-Thread erzeugen
                                    FrameworkThread frameworkThread = xmlListenerSession.startChildThread(thread -> {
                                        // GUI-Änderungen müssen mit invokeThreadSafe durchgeführt werden
                                        xmlListenerSession.invokeThreadSafe(() -> {
                                            // Auswerten vom Rückgabewert zum Verhindern der weiteren Nachrichten-
                                            // Verteilung hier nicht möglich, weil wir uns in einem separaten Thread
                                            // befinden
                                            try {
                                                xmlListener.messageReceived(xmlMQMessage);
                                            } catch (Exception e) {
                                                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);
                                            }
                                        });
                                    });

                                    // um Parallelität zu vermeiden und Synchronisierung zu erzwingen, muss bei isSynchronizedMode()
                                    // auf den Thread gewartet werden
                                    if (isSynchronizedMode()) {
                                        frameworkThread.waitFinished();
                                    }
                                } else {
                                    try {
                                        boolean preventMessageDistribution = xmlListener.messageReceived(xmlMQMessage);
                                        if (preventMessageDistribution) {
                                            break;
                                        }
                                    } catch (Exception e) {
                                        Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);
                                    }
                                }

                            }
                        }
                    }
                }
            }
            if (!channelType.isOmitLogMessageForEverySingleMessage()) {
                logProcessedMQMessage(channelType, xmlContent);
            }
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "Building MQ message object from XML message resulted in a null object. Content of received message: "
                                                                   + MQHelper.getLogTextFromMessageText(xmlContent, true));
        }
    }

    /**
     * Setzt den Nachrichten-Schlüssel auf Basis der übergebenen XML Media Antwort
     *
     * @param xmlMediaMessage
     * @param channelType
     * @param keys
     */
    private void setReceiverKeyFromResponse(iPartsXMLMediaMessage xmlMediaMessage, MQChannelType channelType, String[] keys) {
        if ((xmlMediaMessage == null) || (channelType == null) || (keys == null)) {
            return;
        }
        if (xmlMediaMessage.isResponse()) {
            iPartsTransferNodeTypes requestOperation = xmlMediaMessage.getResponse().getRequestOperation();
            if (requestOperation != null) {
                keys[2] = requestOperation.getAlias();
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + channelType.getChannelName()
                                                                       + ": Could not extract request operation type from received response. Message content: "
                                                                       + MQHelper.getLogTextFromMessageText(xmlMediaMessage.getFileContent(), true));
            }
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + channelType.getChannelName()
                                                                   + ": Could not set message key. Received message is not an response!. Message content: "
                                                                   + MQHelper.getLogTextFromMessageText(xmlMediaMessage.getFileContent(), true));
        }
    }

    /**
     * Setzt den Nachrichten-Schlüssel auf Basis des übergebenen XML Media Events
     *
     * @param xmlMediaMessage
     * @param channelType
     * @param keys
     */
    private void setReceiverKeyFromEvent(iPartsXMLMediaMessage xmlMediaMessage, MQChannelType channelType, String[] keys) {
        if ((xmlMediaMessage == null) || (channelType == null) || (keys == null)) {
            return;
        }
        if (xmlMediaMessage.isEvent()) {
            AbstractXMLChangeEvent actualEvent = ((iPartsXMLEvent)xmlMediaMessage.getTypeObject()).getActualEvent();
            if (actualEvent != null) {
                iPartsTransferNodeTypes eventType = actualEvent.getEventType();
                if (eventType != null) {
                    keys[2] = eventType.getAlias();
                } else {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + channelType.getChannelName()
                                                                           + ": Could not extract event type from received event. Message content: "
                                                                           + MQHelper.getLogTextFromMessageText(xmlMediaMessage.getFileContent(), true));
                }
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + channelType.getChannelName()
                                                                       + ": Could not extract event object from received event. Message content: "
                                                                       + MQHelper.getLogTextFromMessageText(xmlMediaMessage.getFileContent(), true));
            }
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + channelType.getChannelName()
                                                                   + ": Could not set message key. Received message is not an event!. Message content: "
                                                                   + MQHelper.getLogTextFromMessageText(xmlMediaMessage.getFileContent(), true));
        }
    }

    /**
     * Setzt den Nachrichten-Schlüssel auf Basis der übergebenen XML Media Anfrage
     *
     * @param xmlMediaMessage
     * @param channelType
     * @param keys
     */
    private void setReceiverKeyFromRequest(iPartsXMLMediaMessage xmlMediaMessage, MQChannelType channelType, String[] keys) {
        if ((xmlMediaMessage == null) || (channelType == null) || (keys == null)) {
            return;
        }
        if (xmlMediaMessage.isRequest()) {
            AbstractXMLRequestOperation operation = ((iPartsXMLRequest)xmlMediaMessage.getTypeObject()).getOperation();
            if (operation != null) {
                iPartsTransferNodeTypes operationType = operation.getOperationType();
                if (operationType != null) {
                    keys[2] = operationType.getAlias();
                } else {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + channelType.getChannelName()
                                                                           + ": Could not extract operation type from received request. Message content: "
                                                                           + MQHelper.getLogTextFromMessageText(xmlMediaMessage.getFileContent(), true));
                }
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + channelType.getChannelName()
                                                                       + ": Could not extract operation from received request. Message content: "
                                                                       + MQHelper.getLogTextFromMessageText(xmlMediaMessage.getFileContent(), true));
            }
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + channelType.getChannelName()
                                                                   + ": Could not set message key. Received message is not a request!. Message content: "
                                                                   + MQHelper.getLogTextFromMessageText(xmlMediaMessage.getFileContent(), true));
        }
    }

    private String getTargetClusterIdFromMessage(AbstractMQMessage xmlMQMessage) {
        if (xmlMQMessage.isOfType(iPartsXMLMediaMessage.TYPE)) {
            iPartsXMLMediaMessage xmlMediaMessage = (iPartsXMLMediaMessage)xmlMQMessage;
            return xmlMediaMessage.getTypeObject().getTargetClusterID();
        } else {
            return null;
        }
    }

    // ################ Generelle Methoden ################

    /**
     * Registriert den {@link AbstractMessageListener} für die angegebenen Schlüssel.
     *
     * @param listener
     * @param keyToListenersMap
     * @param keys
     */
    private void addMessageListenerForKeys(AbstractMessageListener listener, Map<String, List<WeakReference<AbstractMessageListener>>> keyToListenersMap,
                                           String... keys) {
        if ((keys == null) || (keys.length == 0)) {
            return;
        }

        // alle PURGE_MAPS_COUNTER_LIMIT Aufrufe die Maps bereinigen, wenn sie mindestens so viele Einträge haben wie
        // PURGE_MAPS_MIN_SIZE, weil durch das Beenden von Sessions ohne Austragen der XmlListener die Maps ansonsten
        // immer größer werden würden
        if (purgeMapsCounter >= PURGE_MAP_COUNTER_LIMIT) {
            purgeMapsCounter = 0;
            if (keyToListenersMap.size() >= PURGE_MAP_MIN_SIZE) {
                for (String keyToTest : keyToListenersMap.keySet().toArray(new String[keyToListenersMap.size()])) {
                    List<WeakReference<AbstractMessageListener>> xmlListeners = keyToListenersMap.get(keyToTest);
                    if (xmlListeners != null) {
                        for (int i = xmlListeners.size() - 1; i >= 0; i--) {
                            WeakReference<? extends AbstractMessageListener> xmlListenerRefToTest = xmlListeners.get(i);
                            AbstractMessageListener xmlMessageListener = xmlListenerRefToTest.get();
                            if (xmlMessageListener == null) { // xmlMessageListener wurde bereits von der GC aufgeräumt
                                xmlListeners.remove(i);
                            } else { // überprüfen, ob die Session vom xmlMessageListener bereits abgelaufen ist
                                Session xmlListenerSession = xmlMessageListener.getSession();
                                if ((xmlListenerSession != null) && (SessionManager.getInstance().getSessionBySessionId(xmlListenerSession.getId()) == null)) {
                                    xmlListeners.remove(i);
                                }
                            }
                        }
                        if (xmlListeners.isEmpty()) {
                            xmlListeners = null;
                        }
                    }

                    // leeren Eintrag entfernen
                    if (xmlListeners == null) {
                        keyToListenersMap.remove(keyToTest);
                    }
                }
            }
        }
        purgeMapsCounter++;

        // erst nach dem Bereinigen die neuen Einträge hinzufügen
        WeakReference<AbstractMessageListener> xmlListenerRef = new WeakReference<>(listener);
        for (String messageId : keys) {
            List<WeakReference<AbstractMessageListener>> xmlListeners = keyToListenersMap.get(messageId);
            if (xmlListeners == null) {
                xmlListeners = new DwList<>();
                keyToListenersMap.put(messageId, xmlListeners);
            }
            xmlListeners.add(xmlListenerRef);
        }
    }

    private String[] getChannelTypeNames(MQChannelType... channelTypes) {
        if ((channelTypes == null) || (channelTypes.length == 0)) {
            return null;
        }

        String[] channelTypeNames = new String[channelTypes.length];
        int i = 0;
        for (MQChannelType channelType : channelTypes) {
            channelTypeNames[i] = channelType.getChannelName().getTypeName();
            i++;
        }
        return channelTypeNames;
    }

    private boolean checkMessageKeys(AbstractMessageListener xmlListener, String... keys) {
        if ((keys == null) || (keys.length == 0)) {
            return false;
        }

        if (Logger.getLogger().isChannelActive(iPartsPlugin.LOG_CHANNEL_MQ)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "Keys added for MessageListener " + xmlListener.toString()
                                                                   + ": " + StrUtils.stringArrayToString(", ", keys));
        }
        return true;
    }

    /**
     * Deregistriert den {@link AbstractMessageListener} für die angegebenen Schlüssel. Wenn keine Schlüssel
     * angegeben werden, wird der {@link AbstractMessageListener} für alle Schlüssel deregistriert.
     *
     * @param messageListener
     * @param keyToListenersMap
     * @param keys              Kann auch {@code null} oder leer sein, um den {@link AbstractMessageListener} für alle Schlüssel
     *                          zu deregistrieren.
     */
    private void removeMessageListenerForKeys(AbstractMessageListener messageListener, Map<String, List<WeakReference<AbstractMessageListener>>> keyToListenersMap,
                                              String... keys) {
        // ohne Angabe von MessageIds wird der messageListener für alle MessageIds ausgetragen
        if ((keys == null) || (keys.length == 0)) {
            keys = keyToListenersMap.keySet().toArray(new String[keyToListenersMap.size()]);
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "All keys removed for MessageListener " + messageListener.toString());
        } else {
            if (Logger.getLogger().isChannelActive(iPartsPlugin.LOG_CHANNEL_MQ)) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "Keys removed for MessageListener " + messageListener.toString()
                                                                       + ": " + StrUtils.stringArrayToString(", ", keys));
            }
        }

        for (String key : keys) {
            List<WeakReference<AbstractMessageListener>> xmlListeners = keyToListenersMap.get(key);
            if (xmlListeners != null) {
                for (int i = xmlListeners.size() - 1; i >= 0; i--) {
                    WeakReference<AbstractMessageListener> xmlListenerRefToTest = xmlListeners.get(i);
                    if (xmlListenerRefToTest.get() == messageListener) {
                        xmlListeners.remove(i);
                        // break wäre sinnvoll, aber im Worst Case ist ein Listener mehrfach registriert
                    }
                }
                if (xmlListeners.isEmpty()) {
                    xmlListeners = null;
                }
            }

            // leeren Eintrag entfernen
            if (xmlListeners == null) {
                keyToListenersMap.remove(key);
            }
        }
    }

    @Override
    public void messageReceived(Message message, MQChannelType channelType) throws JMSException, IOException, SAXException {
        Object syncObject;
        if (isSynchronizedMode()) {
            syncObject = this;
        } else {
            syncObject = new Object(); // keine Synchronisierung
        }
        synchronized (syncObject) {
            if (message instanceof TextMessage) {
                iPartsMQChannelTypeNames channelName = channelType.getChannelName();
                if (channelName != null) {
                    String content = ((TextMessage)message).getText();
                    if (channelName.isXMLChannel()) {
                        xmlMessageReceived(content, channelType, false, true);
                    } else if (channelName.isTextContentChannel()) {
                        textContentMessageReceived(content, channelType);
                    }
                }
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "Received MQ message must be a TextMessage. Content of received message: "
                                                                       + MQHelper.getLogTextFromMessageText(message.toString(), true));
            }
        }
    }

    private void logProcessedMQMessage(MQChannelType channelType, String xmlContent) {
        Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + channelType.getChannelName()
                                                               + ": MQ Message processed: "
                                                               + MQHelper.getLogTextFromMessageText(xmlContent, true));
    }
}