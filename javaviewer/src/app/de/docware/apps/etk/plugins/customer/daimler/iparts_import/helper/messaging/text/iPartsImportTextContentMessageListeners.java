/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.messaging.text;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.AbstractTextContentMessageListener;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsMQMessageManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin;
import de.docware.framework.modules.gui.session.Session;

/**
 * Sammlung an Listeners für MQ Nachrichten, die Nachrichten in ihrer reinen Textform verarbeiten
 */
public class iPartsImportTextContentMessageListeners implements iPartsConst {

    private static iPartsImportTextContentMessageListeners instance;

    private AbstractTextContentMessageListener handleDIALOGDirectImportListener;

    private iPartsImportTextContentMessageListeners() {
    }

    public static iPartsImportTextContentMessageListeners getInstance() {
        if (instance == null) {
            instance = new iPartsImportTextContentMessageListeners();
        }
        return instance;
    }

    private void registerTextDirectListener(Session session) {
        // MessageListener für DIALOG Import
        handleDIALOGDirectImportListener = new AbstractTextContentMessageListener(session) {

            @Override
            public boolean messageReceived(String textContent, MQChannelType channelType) {
                return iPartsTextToDIALOGDataHelper.getInstance().importData(textContent, channelType);
            }
        };
        iPartsMQMessageManager.getInstance(iPartsImportPlugin.TEXT_MESSAGE_MANAGER_NAME_IMPORT_DATA).addTextContentMessageListenerForChannelTypes(handleDIALOGDirectImportListener,
                                                                                                                                                  iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_DIRECT_IMPORT,
                                                                                                                                                  iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_DIRECT_DELTA_IMPORT);
    }

    public void registerTextMessageListeners(final Session session) {
        registerTextDirectListener(session);
    }

    public void deregisterTextMessageListeners() {
        if (handleDIALOGDirectImportListener != null) {
            iPartsMQMessageManager.getInstance(iPartsImportPlugin.TEXT_MESSAGE_MANAGER_NAME_IMPORT_DATA).removeTextContentMessageListenerForChannelTypes(handleDIALOGDirectImportListener,
                                                                                                                                                         iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_DIRECT_IMPORT,
                                                                                                                                                         iPartsImportPlugin.MQ_CHANNEL_TYPE_DIALOG_DIRECT_DELTA_IMPORT);
            handleDIALOGDirectImportListener = null;
        }
    }

}
