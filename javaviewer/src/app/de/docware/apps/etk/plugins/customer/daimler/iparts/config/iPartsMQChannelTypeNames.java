/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.config;

import java.util.EnumSet;

/**
 * Enum mit allen MQChannels
 */
public enum iPartsMQChannelTypeNames {
    DIALOG_IMPORT("DIALOG Import"),
    DIALOG_DELTA_IMPORT("DIALOG Delta Import"),
    DIALOG_DIRECT_IMPORT("DIALOG Direct Import"),
    DIALOG_DIRECT_DELTA_IMPORT("DIALOG Direct Delta Import"),
    EDS_IMPORT("EDS Import"),
    PRIMUS_IMPORT("PRIMUS Import"),
    SRM_IMPORT("SRM Import"),
    MEDIA("Media"),
    TEST("Test"),
    UNKNOWN("UNKNOWN");

    private static final EnumSet<iPartsMQChannelTypeNames> DIALOG_XML_CHANNELS = EnumSet.of(DIALOG_DELTA_IMPORT, DIALOG_IMPORT);
    private static final EnumSet<iPartsMQChannelTypeNames> XML_CHANNELS = EnumSet.of(DIALOG_IMPORT, DIALOG_DELTA_IMPORT, EDS_IMPORT, PRIMUS_IMPORT, SRM_IMPORT, MEDIA);
    private static final EnumSet<iPartsMQChannelTypeNames> TEXT_CONTENT_CHANNELS = EnumSet.of(DIALOG_DIRECT_IMPORT, DIALOG_DIRECT_DELTA_IMPORT);
    private static final EnumSet<iPartsMQChannelTypeNames> DELTA_CHANNELS = EnumSet.of(DIALOG_DELTA_IMPORT, DIALOG_DIRECT_DELTA_IMPORT);
    private static final EnumSet<iPartsMQChannelTypeNames> INITIAL_CHANNELS = EnumSet.of(DIALOG_IMPORT, DIALOG_DIRECT_IMPORT);

    private String typeName;

    iPartsMQChannelTypeNames(String type) {
        this.typeName = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public static iPartsMQChannelTypeNames getType(String typeName) {
        for (iPartsMQChannelTypeNames type : values()) {
            if (type.getTypeName().equals(typeName)) {
                return type;
            }
        }
        return UNKNOWN;
    }


    @Override
    public String toString() {
        return getTypeName();
    }

    public static boolean isChannelValidatable(iPartsMQChannelTypeNames channelTypeName) {
        return channelTypeName != TEST;
    }

    public boolean isDialogXMLChannel() {
        return DIALOG_XML_CHANNELS.contains(this);
    }

    public boolean isXMLChannel() {
        return XML_CHANNELS.contains(this);
    }

    public boolean isTextContentChannel() {
        return TEXT_CONTENT_CHANNELS.contains(this);
    }

    public boolean isDeltaChannel() {
        return DELTA_CHANNELS.contains(this);
    }

    public boolean isInitialChannel() {
        return INITIAL_CHANNELS.contains(this);
    }
}
