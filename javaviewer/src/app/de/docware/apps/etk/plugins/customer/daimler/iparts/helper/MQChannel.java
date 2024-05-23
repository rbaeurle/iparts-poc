/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.util.java1_1.Java1_1_Utils;

import javax.jms.IllegalStateException;
import javax.jms.*;
import java.io.EOFException;
import java.io.InterruptedIOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * MQ Kanal für eine spezielle MQ Kommunikation bestehend aus MQ OUT und MQ IN Queue, z.B. DIALOG Daten Kommunikation
 */
public class MQChannel {

    private MQChannelType channelType;
    private volatile Connection connection;
    private volatile Session session;
    private Destination destinationOut;
    private Destination destinationIn;
    private MessageProducer producerForOutQueue;
    private MessageProducer producerForInQueue;
    private boolean isWithoutConsumer;
    private volatile MessageConsumer consumer;
    private FrameworkThread consumerThread;
    private volatile FrameworkThread connectionWatchDog;
    private Set<MQMessageReceiver> mqMessageReceivers = new LinkedHashSet<MQMessageReceiver>();

    public MQChannel(MQChannelType channelType) {
        this.channelType = channelType;
    }

    /**
     * Initialisiert den MQ Kanal samt Connection, Session, Producer und Consumer
     *
     * @return
     * @throws JMSException
     */
    public synchronized boolean init() throws JMSException {
        boolean initSuccessful = false;
        if (connection == null) {
            return reinitSession(true);
        }
        if (!isReadyForCommunication()) {
            try {
                connection.start();
                if (initSession()) {
                    if (initProducer()) {
                        boolean startConsumer = isConsumerActive();
                        if (!startConsumer || initConsumer()) {
                            isWithoutConsumer = !startConsumer;
                            initWatchDogThread();
                            initSuccessful = true;
                        }
                    }
                }
            } catch (JMSException e) {
                if ((Constants.DEVELOPMENT) && (e.getCause() instanceof InterruptedIOException)) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e.getMessage());
                } else {
                    throw e;
                }
            }
        } else {
            initSuccessful = true;
        }
        return initSuccessful;
    }

    /**
     * @return {@code true}, falls Admin-Option {@link iPartsPlugin#CONFIG_AUTO_IMPORTS_ENABLED} {@code true} ist und
     * wenn der Kanal Importe abarbeiten darf.
     */
    private boolean isConsumerActive() {
        boolean isAutoImportEnabled = iPartsPlugin.getPluginConfig().getConfigValueAsBoolean(iPartsPlugin.CONFIG_AUTO_IMPORTS_ENABLED);
        return getChannelType().isImportAllowed() && isAutoImportEnabled;
    }

    /**
     * Initialisiert die MQ Session
     *
     * @return
     * @throws JMSException
     */
    private boolean initSession() throws JMSException {
        if (connection != null) {
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } else {
            session = null;
        }
        if (session == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + getChannelType().getChannelName() + ": MQ Session could not be created!");
            internalCloseConnection();
            return false;
        }
        return true;
    }

    /**
     * Initialisiert die MQ Producer für die Output und Input Queues
     *
     * @return
     * @throws JMSException
     */
    private boolean initProducer() throws JMSException {
        producerForOutQueue = session.createProducer(destinationOut);
        if (producerForOutQueue == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + getChannelType().getChannelName()
                                                                   + ": MQ Producer for output queue could not be created!");
            return false;
        }
        producerForOutQueue.setDeliveryMode(DeliveryMode.PERSISTENT);
        Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + getChannelType().getChannelName()
                                                               + ": MQ Producer for output queue initialized. DeliveryMode: "
                                                               + producerForOutQueue.getDeliveryMode()
                                                               + "; Destination: " + producerForOutQueue.getDestination());

        producerForInQueue = session.createProducer(destinationIn);
        if (producerForInQueue == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + getChannelType().getChannelName()
                                                                   + ": MQ Producer for input queue could not be created!");
            return false;
        }
        producerForInQueue.setDeliveryMode(DeliveryMode.PERSISTENT);
        Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + getChannelType().getChannelName()
                                                               + ": MQ Producer for input queue initialized. DeliveryMode: "
                                                               + producerForInQueue.getDeliveryMode()
                                                               + "; Destination: " + producerForInQueue.getDestination());
        return true;
    }

    /**
     * Initialisiert den MQ Consumer
     *
     * @return
     * @throws JMSException
     */
    private boolean initConsumer() throws JMSException {
        isWithoutConsumer = false;
        if (session == null) {
            if (!initSession()) {
                consumer = null;
                return false;
            }
        }
        consumer = session.createConsumer(destinationIn);
        if (consumer == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + getChannelType().getChannelName() + ": MQ Consumer could not be created!");
            return false;
        }
        initConsumerThread();
        Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + getChannelType().getChannelName() + ": MQ Consumer initialized");
        return true;
    }

    /**
     * Initialisiert den Consumer Thread, der auf Nachrichten in der IN Queue wartet
     */
    private void initConsumerThread() {
        if (iPartsPlugin.getMqSession() == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ framework session is not initialized");
            return;
        }

        consumerThread = iPartsPlugin.getMqSession().startChildThread(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                // Warten bis mindestens ein MQMessageReceiver vorhanden ist
                while (mqMessageReceivers.isEmpty() || !isReadyToReceive()) {
                    if (Java1_1_Utils.sleep(100) || !AbstractApplication.getApplication().isRunning()) {
                        return;
                    }
                }

                while (!Java1_1_Utils.sleep(1) && isReadyToReceive() && AbstractApplication.getApplication().isRunning()) {
                    if (!iPartsDateTimeHelper.isWithinInterval(channelType.getImportIntervalStart(), channelType.getImportIntervalEnd())) {
                        continue;
                    }
                    try {
                        Message message = consumer.receive(1000);
                        if (AbstractApplication.getApplication().isRunning() && (message != null)) {
                            if (message instanceof TextMessage) { // Wir unterstützen nur TextMessages
                                boolean omitLogMessage = channelType.isOmitLogMessageForEverySingleMessage();
                                if (!omitLogMessage) {
                                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + getChannelType().getChannelName()
                                                                                           + ": MQ Message received: "
                                                                                           + MQHelper.getLogTextFromMessageText(MQHelper.getInstance().messageToString(message, false), true));
                                }

                                // Aktiv-Zustand der DB-Verbindung vom MQ EtkProject überprüfen
                                iPartsPlugin.assertProjectDbIsActive(iPartsPlugin.getMqProject(), "MQ", iPartsPlugin.LOG_CHANNEL_MQ);

                                // Lokale Kopie der Empfänger verwenden, damit durch das synchronized() Empfänger wie z.B.
                                // das MQDemoForm nicht beim Deregistrieren als Empfänger blockiert werden solange eine
                                // Nachricht gerade abgearbeitet wird
                                Set<MQMessageReceiver> mqMessageReceiversLocal;
                                synchronized (mqMessageReceivers) {
                                    mqMessageReceiversLocal = new LinkedHashSet<>(mqMessageReceivers);
                                }

                                for (MQMessageReceiver mqMessageReceiver : mqMessageReceiversLocal) {
                                    try {
                                        if (!omitLogMessage) {
                                            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + getChannelType().getChannelName()
                                                                                                   + ": MQ Message distribution started for "
                                                                                                   + mqMessageReceiver.toString());
                                        }
                                        mqMessageReceiver.messageReceived(message, getChannelType());
                                    } catch (Throwable t) {
                                        if (!Thread.currentThread().isInterrupted()) {
                                            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, t);
                                        } else {
                                            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, t);
                                        }
                                    }
                                }
                            } else {
                                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + getChannelType().getChannelName()
                                                                                       + ": MQ Message of invalid class "
                                                                                       + message.getClass().getSimpleName()
                                                                                       + " and JMS type " + message.getJMSType()
                                                                                       + " received: "
                                                                                       + MQHelper.getLogTextFromMessageText(MQHelper.getInstance().messageToString(message, false), true));
                            }
                        }
                    } catch (Exception e) {
                        if (!Thread.currentThread().isInterrupted()) {
                            if (Constants.DEVELOPMENT && (((e instanceof JMSException) && (e.getCause() instanceof EOFException))
                                                          || (e instanceof IllegalStateException))) {
                                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e.getMessage());
                                return;
                            }

                            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);

                            // ConsumerThread hat eine JMSException geworfen -> Reinitialisierung versuchen
                            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + getChannelType().getChannelName()
                                                                                   + ": MQ Consumer thread stopped because of exception in JMS! Trying to reinitialize...");

                            // Falls der ConsumerThread nicht sowieso beendet wurde, reinitSession() aufrufen
                            if (!consumerThread.wasCanceled()) {
                                reinitSession(true); // Ergebnis ist egal, weil reinitSession() bei Erfolg sowieso einen neuen consumerThread erzeugt
                            }
                            return;
                        }
                    }
                }
            }
        });
        consumerThread.setName("MQ Consumer thread for channel " + getChannelType().getChannelName());
    }

    /**
     * Startet bzw. stoppt den MQ Consumer (jeweils falls notwendig) abhängig von {@link #isConsumerActive()}
     *
     * @return
     */
    public synchronized void startOrStopConsumer() {
        if (isConsumerActive()) {
            isWithoutConsumer = false;
            if (consumer == null) {
                try {
                    if (initConsumer()) {
                        Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.INFO, "MQ Channel " + getChannelType().getChannelName()
                                                                              + ": MQ Consumer started");
                    }
                } catch (JMSException e) {
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, new RuntimeException("MQ Channel "
                                                                                                                        + getChannelType().getChannelName()
                                                                                                                        + ": Error while starting MQ Consumer", e));
                }
            }
        } else {
            isWithoutConsumer = true;
            if (consumer != null) {
                stopConsumer();
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.INFO, "MQ Channel " + getChannelType().getChannelName()
                                                                      + ": MQ Consumer stopped");
            }
        }
    }

    private boolean isReadyToReceive() {
        return (consumer != null) && (session != null) && (connection != null);
    }

    /**
     * Reinitialisiert die Session samt neuer Connection
     *
     * @param logErrors
     * @return
     */
    public synchronized boolean reinitSession(boolean logErrors) {
        try {
            internalCloseConnection(); // alte Verbindung zunächst explizit beenden

            // Wenn die Anwendung gerade herunterfährt keine neue Session mehr starten
            if (!AbstractApplication.getApplication().isRunning()) {
                return false;
            }

            connection = MQHelper.getInstance().getNewConnection();
            if (connection != null) {
                if (init()) {
                    // Als Info loggen, damit die erfolgreiche Reinitialisierung auch ohne Debug geloggt wird
                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.INFO, "MQ Channel " + getChannelType().getChannelName()
                                                                          + ": MQ Connection initialized successfully"
                                                                          + (isWithoutConsumer ? " without MQ Consumer" : " with MQ Consumer"));
                    return true;
                }
            }
            if (logErrors) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + getChannelType().getChannelName()
                                                                       + ": MQ Connection could not be initialized!");
            }
            internalCloseConnection(); // Verbindung funktioniert sowieso nicht -> explizit beenden
            return false;
        } catch (JMSException e) {
            if (logErrors) {
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + getChannelType().getChannelName()
                                                                       + ": MQ Connection could not be initialized!");
            }
            internalCloseConnection(); // Verbindung funktioniert sowieso nicht -> explizit beenden
            return false;
        }
    }

    /**
     * Schließt alle zur MQ Verbindung benötigten Ressourcen inkl. Watch Dog Thread
     */
    public synchronized void closeConnection() {
        FrameworkThread connectionWatchDogThread = getConnectionWatchDogThread();
        if (connectionWatchDogThread != null) {
            connectionWatchDogThread.cancel();
        }
        internalCloseConnection();
    }

    /**
     * Schließt alle zur MQ Verbindung benötigten Ressourcen, lässt den Watch Dog Thread für Reinitialisierungen aber laufen
     */
    private synchronized void internalCloseConnection() {
        if (connection == null) { // Verbindung wurde schon geschlossen
            return;
        }

        stopConsumer();

        if (session != null) {
            try {
                session.close();
            } catch (JMSException e) {
                // Fehler beim Beenden der Verbindung nur als Debug loggen, weil eigentlich uninteressant
                Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, e);
            }
            session = null;
        }

        try {
            connection.close();
        } catch (JMSException e) {
            // Fehler beim Beenden der Verbindung nur als Debug loggen, weil eigentlich uninteressant
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, e);
        }
        connection = null;

        Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.INFO, "MQ Channel " + getChannelType().getChannelName() + ": MQ Connection has been closed");
    }

    /**
     * Stoppt den MQ Consumer
     *
     * @return
     */
    private void stopConsumer() {
        // Beende den noch laufenden ConsumerThread (muss vor dem Consumer geschehen, da der Consumer vielleicht noch
        // in seinem "wait()" Zustand sein könnte); nicht innerhalb vom synchronized, um Deadlocks zu vermeiden
        FrameworkThread consumerThreadLocal = consumerThread;
        if (consumerThreadLocal != null) {
            consumerThreadLocal.cancel();
        }

        synchronized (this) {
            if (consumerThreadLocal == consumerThread) { // Zwischendrin sollte kein anderer ConsumerThread gestartet worden sein
                consumerThread = null;
            }
            if (consumer != null) {
                try {
                    consumer.close();
                } catch (JMSException e) {
                    // Fehler beim Beenden der Verbindung nur als Debug loggen, weil eigentlich uninteressant
                    Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, e);
                }
                consumer = null;
            }
        }
    }

    /**
     * Initialisiert den Watchdog Thread der bei Abbruch der Verbindung die Reconnect Prozedur einleitet.
     * Die Thread Parameter sind über den Adminmodus konfigurierbar
     */
    public synchronized void initWatchDogThread() {
        // Watchdog Thread für die MQ Session. Verbindugnsversuche sowie Wartezeit können via Adminmodus eingestellt werden
        if ((connectionWatchDog == null) || !connectionWatchDog.isRunning()) {
            if (iPartsPlugin.getMqSession() == null) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ framework session is not initialized");
                return;
            }

            connectionWatchDog = iPartsPlugin.getMqSession().startChildThread(new FrameworkRunnable() {
                @Override
                public void run(FrameworkThread thread) {
                    boolean firstReconnect = true;
                    int reconnectCounter = 0;
                    int reconnectMaxAttempts = iPartsPlugin.getPluginConfig().getConfigValueAsInteger(iPartsPlugin.CONFIG_MQ_RECONNECT_ATTEMPTS);
                    while ((reconnectMaxAttempts < 0) || (reconnectCounter < reconnectMaxAttempts)) {
                        // reconnectMaxAttempts und reconnectTimeInSeconds ständig aktualisieren falls sich die Admin-Einstellungen ändern
                        long reconnectTimeInSeconds = iPartsPlugin.getPluginConfig().getConfigValueAsInteger(iPartsPlugin.CONFIG_MQ_RECONNECT_TIME);
                        if (Java1_1_Utils.sleep(Math.max(10, reconnectTimeInSeconds * 1000)) || !AbstractApplication.getApplication().isRunning()) {
                            break;
                        }
                        reconnectMaxAttempts = iPartsPlugin.getPluginConfig().getConfigValueAsInteger(iPartsPlugin.CONFIG_MQ_RECONNECT_ATTEMPTS);

                        if ((connection == null) || (session == null)) { // keine aktive MQ Session
                            if (firstReconnect) {
                                firstReconnect = false;
                                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + getChannelType().getChannelName()
                                                                                       + ": MQ Session is closed! Trying to reinitialize...");
                            }
                            if (reinitSession(false)) {
                                // Wiederholungsversuchszähler bei erfolgreichem Reinit zurücksetzen
                                reconnectCounter = 0;
                                firstReconnect = true;
                            } else {
                                if (reconnectMaxAttempts > 0) {
                                    reconnectCounter++;
                                }
                                if ((reconnectMaxAttempts < 0) || (reconnectCounter < reconnectMaxAttempts)) {
                                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + getChannelType().getChannelName()
                                                                                           + ": Could not reconnect to MQ server because no MQ connection could be established. Trying to reconnect again in "
                                                                                           + reconnectTimeInSeconds + " seconds ("
                                                                                           + ((reconnectMaxAttempts > 0) ? String.valueOf(reconnectMaxAttempts - reconnectCounter) : "unlimited")
                                                                                           + " attempts remaining)...");
                                } else {
                                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + getChannelType().getChannelName()
                                                                                           + ": Could not reconnect to MQ server because no MQ connection could be established. Giving up after "
                                                                                           + reconnectMaxAttempts + " attempts.");
                                }
                            }
                        }
                    }
                    connectionWatchDog = null;
                }
            });
            connectionWatchDog.setName("MQ Watchdog thread for channel " + getChannelType().getChannelName());
        }
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Destination getDestinationOut() {
        return destinationOut;
    }

    public void setDestinationOut(Destination destinationOut) {
        this.destinationOut = destinationOut;
    }

    public Destination getDestinationIn() {
        return destinationIn;
    }

    public void setDestinationIn(Destination destinationIn) {
        this.destinationIn = destinationIn;
    }

    public MQChannelType getChannelType() {
        return channelType;
    }

    public FrameworkThread getConnectionWatchDogThread() {
        return connectionWatchDog;
    }

    public void addMessageReceiver(MQMessageReceiver mqMessageReceiver) {
        synchronized (mqMessageReceivers) {
            mqMessageReceivers.add(mqMessageReceiver);
        }
    }

    public void removeMessageReceiver(MQMessageReceiver mqMessageReceiver) {
        synchronized (mqMessageReceivers) {
            mqMessageReceivers.remove(mqMessageReceiver);
        }
    }

    public MessageProducer getProducerForOutQueue() {
        return producerForOutQueue;
    }

    public MessageProducer getProducerForInQueue() {
        return producerForInQueue;
    }

    /**
     * Gibt zurück, ob der Kanal vollständig initialisiert wurde und bereit für die Kommunikation ist
     *
     * @return
     */
    public synchronized boolean isReadyForCommunication() {
        if (connection != null) {
            if (session != null) {
                if (producerForOutQueue != null) {
                    if (isWithoutConsumer) {
                        return true;
                    } else if ((consumer != null) && (consumerThread != null) && consumerThread.isRunning()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Sendet die übergeben Nachricht an die Output Queue. Falls gewünscht, kann auch eine simulierte Antwort versendet werden.
     *
     * @param messageString
     * @param messageId
     * @param isSimulatedMessageResponse
     * @throws JMSException
     */
    public synchronized void sendMessage(String messageString, String messageId, boolean isSimulatedMessageResponse) throws JMSException {
        if (session == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + getChannelType().getChannelName()
                                                                   + ": MQ message could not be sent because MQ session ist null: "
                                                                   + MQHelper.getLogTextFromMessageText(messageString, true));
            return;
        }
        TextMessage message = session.createTextMessage(messageString);
        // Die ReplyTo-Queue ist die Queue, die wir beim Start als Destination-In-Queue aus der Kontextdatei laden
        if (destinationIn != null) {
            message.setJMSReplyTo(destinationIn);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "MQ Channel " + getChannelType().getChannelName()
                                                                   + ": Destination in queue is null. Could not add it as \"reply to queue\".");
        }
        message.setLongProperty(MQHelper.TIMESTAMP_ALIAS, System.currentTimeMillis());
        message.setJMSCorrelationID(messageId);
        if (isSimulatedMessageResponse) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + getChannelType().getChannelName()
                                                                   + ": Simulated response MQ message sent: "
                                                                   + MQHelper.getLogTextFromMessageText(MQHelper.getInstance().messageToString(message, true), true));
            producerForInQueue.send(message);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ Channel " + getChannelType().getChannelName()
                                                                   + ": MQ Message sent: "
                                                                   + MQHelper.getLogTextFromMessageText(MQHelper.getInstance().messageToString(message, true), true));
            producerForOutQueue.send(message);
        }
    }

    /**
     * Gibt zurück, ob der Receiver in diesem Kanal registriert ist
     *
     * @param mqReceiver
     * @return
     */
    public boolean hasReceiver(MQMessageReceiver mqReceiver) {
        return mqMessageReceivers.contains(mqReceiver);
    }

}