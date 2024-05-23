/*
 * Copyright (c) 2016 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media;

import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractMediaOrderRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLObjectWithMCAttributes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLRequestOperation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLGetMediaPreview;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLRequestor;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLSearchMediaContainers;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.util.StrUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hilft beim Erstellen von {@link iPartsXMLMediaMessage} Objekten um XML Nachrichten zu verschicken
 */
public class XMLObjectCreationHelper {

    private static XMLObjectCreationHelper instance;

    private XMLObjectCreationHelper() {
    }

    public static XMLObjectCreationHelper getInstance() {
        if (instance == null) {
            instance = new XMLObjectCreationHelper();
        }
        return instance;
    }

    /**
     * Baut für einen Bildauftrag eine Default {@link iPartsXMLMediaMessage} damit die XML Element Objekte nicht manuell erstellt werden müssen
     *
     * @param createMediaOrderObject - das Objekt mit den eigentlichen Bildauftrags Daten
     * @param requestor              - Benutzer der den Auftrag erstellt und losschickt
     * @param GUID                   - iParts GUID für den Bildauftrag
     * @return
     */
    public iPartsXMLMediaMessage createDefaultPicOrderXMLMessage(AbstractMediaOrderRequest createMediaOrderObject,
                                                                 iPartsXMLRequestor requestor, String GUID) {
        if (createMediaOrderObject != null) {
            return makeMessage(createMediaOrderObject, requestor, GUID, false);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "CreateMediaOrder object must not be null!");
        }
        return null;
    }

    /**
     * Baut für die Bildrecherche eine Default {@link iPartsXMLMediaMessage} damit die XML Element Objekte nicht manuell erstellt werden müssen
     *
     * @param smcObject               - das Objekt mit den Angaben zur Suche
     * @param requestor               - Benutzer
     * @param GUID                    - iParts GUID
     * @param answerToThisClusterOnly Flag, ob die Antwort nur an diesen Cluster-Knoten geschickt werden soll
     * @return
     */
    public iPartsXMLMediaMessage createDefaultPicSearchXMLMessage(iPartsXMLSearchMediaContainers smcObject, iPartsXMLRequestor requestor,
                                                                  String GUID, boolean answerToThisClusterOnly) {
        if (smcObject != null) {
            return makeMessage(smcObject, requestor, GUID, answerToThisClusterOnly);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "SearchMediaContainers object must not be null!");
        }
        return null;
    }

    public iPartsXMLMediaMessage createDefaultGetPicMediaXMLMessage(AbstractXMLObjectWithMCAttributes getMediaObject,
                                                                    iPartsXMLRequestor requestor, String GUID,
                                                                    iPartsTransferNodeTypes nodeType) {
        if (getMediaObject != null) {
            return makeMessage(getMediaObject, requestor, GUID, false);
        } else {
            String logMessage;
            if (nodeType != null) {
                logMessage = nodeType.getAlias() + " object must not be null!";
            } else {
                logMessage = "Error while creating default GetPicMediaXMLMessage!";
            }
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, logMessage);
        }
        return null;
    }

    /**
     * Überprüft, ob die in der Admin-Option eingegebenen Dateitypen für die GetMediaContents Operation gültig sind.
     *
     * @param fileTypes
     * @return
     */
    public boolean isFileTypesInputValidForGetMediaGontents(List<String> fileTypes) {
        // Wurden keine Dateitypen angegeben, dann werden alle Dateitypen angefragt
        if ((fileTypes == null) || fileTypes.isEmpty()) {
            return true;
        }

        boolean result = true;
        for (String fileType : fileTypes) {
            // Logeinträge für nicht valide Media-Typen
            if (!iPartsTransferConst.MediaFileTypes.isValidFileExtension(fileType)) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Entered filetype \"" + fileType + "\" is not " +
                                                                           "valid for a GetMediaContents operation!");
                result = false;
            }
        }
        return result;
    }

    /**
     * Liefert alle Dateitypen für die GetMediaContents Anfrage in einem Set zurück. Sollte die Admin-Option leer sein
     * (alle möglichen Dateitypen werden angefragt), dann liefert die Methode "null" zurück.
     *
     * @return
     */
    public Set<String> getFileTypesForGetMediaContents() {
        // Alle eingegebenen Dateitypen
        String validFileTypes = iPartsPlugin.getPluginConfig().getConfigValueAsString(iPartsPlugin.CONFIG_PIC_REF_VALID_FILES).trim();
        // wenn nichts eingebenen wurde -> alle Dateitypen anfragen
        if (!StrUtils.isEmpty(validFileTypes)) {
            List<String> enteredFileTypesList = StrUtils.toStringList(validFileTypes, ";", false, true);
            return new HashSet<String>(enteredFileTypesList);
        }
        return null;
    }

    private iPartsXMLMediaMessage makeMessage(AbstractXMLRequestOperation actualOperation, iPartsXMLRequestor requestor, String GUID,
                                              boolean answerToThisClusterOnly) {
        // Den eigentlichen Request
        iPartsXMLRequest request = new iPartsXMLRequest(GUID, answerToThisClusterOnly, iPartsTransferConst.PARTICIPANT_IPARTS,
                                                        iPartsTransferConst.PARTICIPANT_ASPLM);
        request.setRequestor(requestor);
        request.setOperation(actualOperation);
        // Und die Nachricht
        iPartsXMLMediaMessage message = new iPartsXMLMediaMessage(true);
        message.setTypeObject(request);
        return message;
    }

    /**
     * Baut für einen Vorschaubildauftrag eine Default {@link iPartsXMLMediaMessage} damit die XML Element Objekte nicht manuell erstellt werden müssen
     *
     * @param gmp       - das Objekt mit den eigentlichen Vorschauauftrags Daten
     * @param requestor - Benutzer der den Auftrag erstellt und losschickt
     * @param GUID      - iParts GUID für den Bildauftrag
     * @return
     */
    public iPartsXMLMediaMessage createDefaultPicPreviewXMLMessage(iPartsXMLGetMediaPreview gmp, iPartsXMLRequestor requestor, String GUID) {
        if (gmp != null) {
            return makeMessage(gmp, requestor, GUID, true);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "GetMediaPreview object must not be null!");
        }
        return null;
    }
}
