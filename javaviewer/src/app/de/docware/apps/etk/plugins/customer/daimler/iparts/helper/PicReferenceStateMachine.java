/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPicReferenceState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReference;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLResponseOperation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLResGetMediaContents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLResGetMediaPreview;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLResSearchMediaContainers;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.iPartsXMLSuccess;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;

/**
 * Statemachine für die möglichen Übergänge für Bildreferenzen in der DB
 */
public class PicReferenceStateMachine {

    private static PicReferenceStateMachine instance;

    public static PicReferenceStateMachine getInstance() {
        if (instance == null) {
            instance = new PicReferenceStateMachine();
        }
        return instance;
    }

    private PicReferenceStateMachine() {
    }

    /**
     * Gibt den nächsten Bildreferenz-Zustand zurück.
     *
     * @param picReference
     * @param mediaMessage
     * @return
     */
    public iPartsPicReferenceState getNextStateWithMediaMessage(iPartsDataPicReference picReference, iPartsXMLMediaMessage mediaMessage) {
        iPartsPicReferenceState currentState = picReference.getStatus();
        iPartsXMLSuccess success = null;
        iPartsXMLResSearchMediaContainers rsmc = null;
        iPartsXMLResGetMediaContents rgmc = null;
        iPartsXMLResGetMediaPreview rgmp = null;
        if (mediaMessage != null) {
            success = mediaMessage.getResponse().getSuccess();
            AbstractXMLResponseOperation resultOperation = mediaMessage.getResponse().getResult();
            if (resultOperation != null) {
                if (resultOperation.getResultType() == iPartsTransferNodeTypes.RES_SEARCH_MEDIA_CONTAINERS) {
                    rsmc = (iPartsXMLResSearchMediaContainers)resultOperation;
                } else if (resultOperation.getResultType() == iPartsTransferNodeTypes.RES_GET_MEDIA_CONTENTS) {
                    rgmc = (iPartsXMLResGetMediaContents)resultOperation;
                } else if (resultOperation.getResultType() == iPartsTransferNodeTypes.RES_GET_MEDIA_PREVIEW) {
                    rgmp = (iPartsXMLResGetMediaPreview)resultOperation;
                }
            }
        }
        switch (currentState) {
            case NEW:
                return iPartsPicReferenceState.SEARCH_SENT;
            case SEARCH_SENT:
                if ((success != null) && (rsmc != null)) {
                    if (success.isErrorFree()) {
                        if (!rsmc.hasResultsDelivered() || (rsmc.getMContainers() == null) || rsmc.getMContainers().isEmpty()) {
                            return iPartsPicReferenceState.NOT_FOUND;
                        } else {
                            return iPartsPicReferenceState.SEARCH_RECEIVED;
                        }
                    }
                }
                return iPartsPicReferenceState.SEARCH_ERROR;
            case SEARCH_RECEIVED:
                return iPartsPicReferenceState.MEDIA_REQUESTED;
            case MEDIA_REQUESTED:
                if ((success != null) && (rgmc != null)) {
                    if (success.isErrorFree() && (rgmc.getmContainer() != null)) {
                        return iPartsPicReferenceState.MEDIA_RECEIVED;
                    }
                }
                return iPartsPicReferenceState.MEDIA_ERROR;
            case MEDIA_RECEIVED:
                return iPartsPicReferenceState.PREVIEW_REQUESTED;
            case PREVIEW_REQUESTED:
                if ((success != null) && (rgmp != null)) {
                    if (success.isErrorFree() && (rgmp.getBinaryFile() != null)) {
                        return iPartsPicReferenceState.PREVIEW_RECEIVED;
                    }
                }
                return iPartsPicReferenceState.PREVIEW_ERROR;
            case PREVIEW_RECEIVED:
                return iPartsPicReferenceState.DONE;
            case DONE:
                return iPartsPicReferenceState.DONE;
        }
        return currentState;
    }

    /**
     * Gibt den nächsten Bildreferenz-Zustand ohne MediaMessage zurück.
     *
     * @param picReference
     * @return
     */
    public iPartsPicReferenceState getNextStateWithoutMediaMessage(iPartsDataPicReference picReference) {
        if (!iPartsPicReferenceState.isStateValidForTransitionWithoutMediaMessage(picReference)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Transition from status \"" + picReference.getStatus() +
                                                                       "\" is not valid without MQ MediaMessage.");
            return picReference.getStatus();
        }
        return getNextStateWithMediaMessage(picReference, null);
    }

}
