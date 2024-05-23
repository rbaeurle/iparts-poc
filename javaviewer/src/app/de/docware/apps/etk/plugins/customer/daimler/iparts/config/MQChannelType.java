/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDateTimeHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Alle in der Anwendung genutzen MQ Kanäle bestehend aus Kanal-Name, MQ OUT und MQ IN Queue.
 */
public class MQChannelType {

    private static LinkedHashMap<iPartsMQChannelTypeNames, MQChannelType> channelTypes = new LinkedHashMap<iPartsMQChannelTypeNames, MQChannelType>();

    private iPartsMQChannelTypeNames channelName;
    private String queueOut;
    private String queueIn;
    private int importIntervalStart = iPartsDateTimeHelper.INVALID_INTERVAL_VALUE;
    private int importIntervalEnd = iPartsDateTimeHelper.INVALID_INTERVAL_VALUE;
    private boolean storeOriginalMessage = true; // Default: Nachrichten werden immer gespeichert
    private boolean isImportAllowed = true;   // Default: Importe über den Channel sind erlaubt
    private boolean omitLogMessageForEverySingleMessage = false; // Default: Log-Meldungen werden ausgegeben

    public MQChannelType(iPartsMQChannelTypeNames channelName, String queueOut, String queueIn, int importIntervalStart, int importIntervalEnd) {
        this.channelName = channelName;
        this.queueOut = queueOut;
        this.queueIn = queueIn;
        this.importIntervalStart = importIntervalStart;
        this.importIntervalEnd = importIntervalEnd;
    }

    public MQChannelType(iPartsMQChannelTypeNames channelName, String queueOut, String queueIn) {
        this(channelName, queueOut, queueIn, iPartsDateTimeHelper.INVALID_INTERVAL_VALUE, iPartsDateTimeHelper.INVALID_INTERVAL_VALUE);
    }

    public String getOutQueue() {
        return queueOut;
    }

    public String getInQueue() {
        return queueIn;
    }

    public iPartsMQChannelTypeNames getChannelName() {
        return channelName;
    }

    public static Collection<MQChannelType> getChannelTypes() {
        return Collections.unmodifiableCollection(channelTypes.values());
    }

    public static MQChannelType getChannelTypeByName(iPartsMQChannelTypeNames channelName) {
        return channelTypes.get(channelName);
    }

    public static void registerChannelType(MQChannelType channelType) {
        channelTypes.put(channelType.getChannelName(), channelType);
    }

    public int getImportIntervalStart() {
        return importIntervalStart;
    }

    public int getImportIntervalEnd() {
        return importIntervalEnd;
    }

    public void setImportIntervalStart(int importIntervalStart) {
        this.importIntervalStart = importIntervalStart;
    }

    public boolean isImportAllowed() {
        return isImportAllowed;
    }

    public void setImportIntervalEnd(int importIntervalEnd) {
        this.importIntervalEnd = importIntervalEnd;
    }

    public boolean isStoreOriginalMessage() {
        return storeOriginalMessage;
    }

    public void setStoreOriginalMessage(boolean storeOriginalMessage) {
        this.storeOriginalMessage = storeOriginalMessage;
    }

    public void setImportAllowed(boolean importAllowed) {
        isImportAllowed = importAllowed;
    }

    public boolean isOmitLogMessageForEverySingleMessage() {
        return omitLogMessageForEverySingleMessage;
    }

    public void setOmitLogMessageForEverySingleMessage(boolean omitLogMessageForEverySingleMessage) {
        this.omitLogMessageForEverySingleMessage = omitLogMessageForEverySingleMessage;
    }
}