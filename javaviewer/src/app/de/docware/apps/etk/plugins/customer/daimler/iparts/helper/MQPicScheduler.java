/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.helper;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.base.project.events.DataChangedEvent;
import de.docware.apps.etk.base.project.mechanic.drawing.*;
import de.docware.apps.etk.base.project.mechanic.ids.PoolEntryId;
import de.docware.apps.etk.base.project.mechanic.ids.PoolId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsPicReferenceState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReference;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReferenceList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsPicReferenceId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.AbstractGenericImporter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.AbstractXMLMessageListener;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferSMCAttributes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsMQMessageManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsXMLResponseSimulator;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.AbstractMQMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.XMLObjectCreationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.request.AbstractXMLObjectWithMCAttributes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.abstractclasses.response.AbstractXMLResponseOperation;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLGetMediaContents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLGetMediaPreview;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLRequestor;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLSearchMediaContainers;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.*;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.app.AbstractApplication;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.logger.LogChannels;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.J2EEHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.java1_1.Java1_1_Utils;

import java.util.*;

/**
 * Scheduler, der die Such- und Bildanfragen für DASTI Bildreferenzen triggert.
 */
public class MQPicScheduler {

    private static boolean SIMULATE_ERRORS = false;

    private static MQPicScheduler instance;

    private EtkProject mqProject;
    private Session mqSession;
    private FrameworkThread workerThread;
    private AbstractXMLMessageListener xmlSearchMediaListener;
    private AbstractXMLMessageListener xmlMediaContentsListener;
    private iPartsXMLRequestor requestor;

    private boolean doHandlePicRefs = false;
    private boolean simulateAnswers = true;

    public static MQPicScheduler getInstance() {
        if (instance == null) {
            instance = new MQPicScheduler();
        }
        return instance;
    }

    private MQPicScheduler() {
        requestor = new iPartsXMLRequestor(iPartsConst.AS_PLM_USER_ID);
    }

    /**
     * Verarbeitet eine von AS-PLM geschickte Antwort mit Bildinhalten (ResGetMediaContent oder ResGetMediaPreview)
     *
     * @param mediaMessage
     */
    private void handleMediaMessage(iPartsXMLMediaMessage mediaMessage) {
        AbstractXMLResponseOperation resultOperation = mediaMessage.getResponse().getResult();
        if (resultOperation == null) {
            checkResponseSuccess(mediaMessage, mqProject);
            return;
        }

        // Überprüfen, ob es sich um den gewünschten Antworttyp handelt
        if (resultOperation.getResultType() == iPartsTransferNodeTypes.RES_GET_MEDIA_CONTENTS) {
            checkRequestSimulation(mediaMessage); // Check, ob Varianten zum Container für die Simulation gespeichert wrden sollen
            handleMediaContent(mediaMessage, resultOperation);
        } else if (resultOperation.getResultType() == iPartsTransferNodeTypes.RES_GET_MEDIA_PREVIEW) {
            handleMediaPreview(mediaMessage, resultOperation);
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Invalid response operation. Valid expected responses" +
                                                                       "are GetMediaContent or GetMediaPreview. Received Operation: "
                                                                       + resultOperation.getResultType());
            return;
        }
    }

    /**
     * Verarbeitet eine von AS-PLM geschickte Antwort mit Bildinhalten (ResGetMediaContent)
     *
     * @param mediaMessage
     * @param resultOperation
     */
    private void handleMediaContent(iPartsXMLMediaMessage mediaMessage, AbstractXMLResponseOperation resultOperation) {
        iPartsXMLResGetMediaContents rgmc = (iPartsXMLResGetMediaContents)resultOperation;
        iPartsXMLMediaContainer mContainer = rgmc.getmContainer();

        // Aktuell liegen die Datensätze mit ihren MC Ids in der Datenbank. Die Antwort hat die Verknüpfung zwischen MC IDs und Varianten IDs
        // -> Hole alle Datensätze, die die mitgeschickte MC ID haben.
        iPartsDataPicReferenceList list = iPartsDataPicReferenceList.loadPicRefsWithMcIdsAndEmptyVarIds(mqProject, mContainer);
        // Beim Nachfordern von Zeichnungen besitzt der Datensatz noch seine Varianten IDs, d.h. hier kann auch direkt via GUID nach dem Datensatz gesucht werden
        if (list.isEmpty()) {
            String messageId = XMLImportExportHelper.removeMediaContentPrefix(mediaMessage.getResponse().getXmlRequestID());
            list = iPartsDataPicReferenceList.loadPicReferencesWithMessageGUID(mqProject, messageId);
        }
        List<iPartsDataPicReference> dataPicReferences = null;
        // Hole die beste Variante aus der AS-PLM Antwort
        iPartsXMLMediaVariant bestVariant = findBestVariant(mContainer);
        if (bestVariant == null) {
            return;
        }
        if (list.isEmpty()) {
            // Check, ob ein "fertiger" Datensatz existiert
            iPartsDataPicReferenceList refsWithMcIds = iPartsDataPicReferenceList.loadPicRefsWithBothIds(mqProject, bestVariant, mContainer);
            if (refsWithMcIds.isEmpty()) {
                // kein Datensatz für die MC IDs gefunden -> Kann eigentlich nicht passieren, da wir die Datensätze vor dem Abschicken abspeichern.
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Received GetMedia response does not match sent picture references. MC ID: " +
                                                                           mContainer.getMcItemId() + ", MC REV ID: " + mContainer.getMcItemRevId() + ", VAR ID: " +
                                                                           bestVariant.getItemId() + ", VAR REV ID: " + bestVariant.getItemRevId());
            }
            return;
        } else if (list.size() == 1) {
            // Es gibt genau einen Datensatz mit der von AS-PLM erhaltenen MC ID -> Idealfall
            dataPicReferences = list.getAsList();
        } else if (list.size() > 1) {
            // Bei mehr als einem Datensatz werden alle Referenzen mit gleicher MC ID und DASTI ID zu einem Datensatz zusammengefügt
            dataPicReferences = adjustPicReferenceDatasets(list, mqProject);
        }

        iPartsXMLSuccess success = mediaMessage.getResponse().getSuccess();
        for (iPartsDataPicReference dataPicReference : dataPicReferences) {
            // Verarbeite alle Datensätze, die die empfangenen MC IDs enthalten und noch keine Varianten IDs haben
            // (Status anpassen und Varianten IDs setzen)
            if (!dataPicReference.isFinishedWithPicture()) {
                iPartsPicReferenceState nextState = PicReferenceStateMachine.getInstance().getNextStateWithMediaMessage(dataPicReference, mediaMessage);
                String oldVarId = dataPicReference.getVarId();
                // Ist die bestehende Varianten ID ungleich der neuen muss die Referenz angepasst werden, bevor der neue Datensatz gespeichert wird
                if (StrUtils.isValid(oldVarId) && !oldVarId.equals(bestVariant.getItemId())) {
                    updateImageTableReference(dataPicReference, bestVariant, oldVarId, mqProject);
                }
                setFieldValues(nextState, dataPicReference, success, bestVariant.getItemId(), bestVariant.getItemRevId());
            }
            // Status "Media empfangen"
            handleDBObjectOperation(dataPicReference, false, mqProject);
        }

        list = iPartsDataPicReferenceList.loadPicRefsWithBothIds(mqProject, bestVariant, mContainer);
        removeInvalidDatasets(list);
        if (list.size() > 1) {
            // Sollte nicht passieren, außer verschiedene DASTI Bildnummern referenzieren auf die gleichen Varianten IDs
            dataPicReferences = adjustPicReferenceDatasets(list, mqProject);
        } else if (list.size() == 1) {
            // Idealfall, Bild und Referenz sind in der DB gespeichert (aus einer vorherigen Operation)
            dataPicReferences = list.getAsList();
        } else { // Kann eigentlich nicht passieren (keine passenden Einträge in der DB gefunden)
            return;
        }

        if (findAndStoreBestPicture(bestVariant, mqProject)) {
            if (dataPicReferences.isEmpty()) {
                // Kann eigentlich nur passieren, wenn adjustPicReferenceDatasets(list) ein leeres Ergebnis zurückgeliefert hat
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Storing picture data was successful but related reference list is empty"
                                                                           + "MC ID: " + mContainer.getMcItemId()
                                                                           + ", MC REV ID: " + mContainer.getMcItemRevId()
                                                                           + ", VAR ID: " + bestVariant.getItemId()
                                                                           + ", VAR REV ID: " + bestVariant.getItemRevId());
            }
            for (iPartsDataPicReference dataPicReference : dataPicReferences) {
                // Hole alle DB Bildreferenzen und update sie + Import neuer Bilder
                updateImageTableReference(dataPicReference, bestVariant, null, mqProject);

                // GetMediaPreview Anfragen losschicken
                if (iPartsPicReferenceState.canSendGetMediaPreviews(dataPicReference.getStatus())) {
                    if (sendMediaRequest(dataPicReference, iPartsTransferNodeTypes.GET_MEDIA_PREVIEW, mqProject)) {
                        // Status: "GetMediaPreview Anfrage losgeschickt"
                        iPartsPicReferenceState nextState = PicReferenceStateMachine.getInstance().getNextStateWithoutMediaMessage(dataPicReference);
                        setFieldValues(nextState, dataPicReference);
                        handleDBObjectOperation(dataPicReference, false, mqProject);
                    }
                }
            }
        } else {
            iPartsXMLSuccess error = new iPartsXMLSuccess(false);
            error.addError(new iPartsXMLErrorText("No valid XML media binary file found"));
            for (iPartsDataPicReference dataPicReference : dataPicReferences) {
                // Status auf MEDIA_ERROR setzen
                setFieldValues(iPartsPicReferenceState.MEDIA_ERROR, dataPicReference, error, null, null);
                handleDBObjectOperation(dataPicReference, false, mqProject);
            }
        }
    }

    /**
     * Aktualisiert die Verknüpfung Modul zu Zeichnung. Es kann vorkommen, dass eine Referenz noch auf einer alten
     * Varianten ID basiert. Hierfür wird anhand von <i>oldVarId</i> der Referenzdatensatz gefunden und mit der neuen
     * Varainten ID aus <i>variant</i> aktualisiert.
     *
     * @param dataPicReference
     * @param variant
     * @param oldId
     * @param project
     */
    private void updateImageTableReference(iPartsDataPicReference dataPicReference, iPartsXMLMediaVariant variant, String oldId,
                                           EtkProject project) {
        updateImageTableReference(dataPicReference, variant.getItemId(), variant.getItemRevId(), oldId, project, false);
    }

    private void updateImageTableReference(iPartsDataPicReference dataPicReference, String newId, String newRevId, String oldId,
                                           EtkProject project, boolean onlyOldId) {
        // Hole alle DB Bildreferenzen und update sie + Import neuer Bilder
        EtkDataImageList imageList = getImageList(dataPicReference, oldId, project, onlyOldId);
        updateImageInDB(imageList, dataPicReference, newId, newRevId, project);
    }

    /**
     * Verarbeitet eine von AS-PLM geschickte Antwort mit Vorschaubildinhalten (ResGetMediaPreview)
     *
     * @param mediaMessage
     * @param resultOperation
     */
    private void handleMediaPreview(iPartsXMLMediaMessage mediaMessage, AbstractXMLResponseOperation resultOperation) {
        String messageId = XMLImportExportHelper.removeMediaContentPrefix(mediaMessage.getResponse().getXmlRequestID());
        iPartsXMLResGetMediaPreview rgmp = (iPartsXMLResGetMediaPreview)resultOperation;

        // Hole die Bildreferenz via GUID
        iPartsDataPicReferenceList list = iPartsDataPicReferenceList.loadPicReferencesWithMessageGUID(mqProject, messageId);
        if (list.isEmpty()) {
            // Fehler: Vor dem Abschicken angelegter Eintrag wird nicht gefunden
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Could not find matching GetMediaPreview request dataset. iPartsRequestID: "
                                                                       + messageId);
            return;
        }
        if (list.size() > 1) { // Kann durch die GUID eigentlich gar nicht mehr passieren
            // Fehler: Zwei Einträge losgeschickt -> keine Zuweisung möglich
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "handleMediaPreview(): Only one search request per picture reference number is allowed. iPartsRequestID: "
                                                                       + messageId);
            return;
        }

        iPartsDataPicReference dataPicReference = list.get(0);
        iPartsXMLSuccess success = mediaMessage.getResponse().getSuccess();
        iPartsPicReferenceState nextState;

        // Status anpassen
        if (!dataPicReference.isFinishedWithPicture()) {
            nextState = PicReferenceStateMachine.getInstance().getNextStateWithMediaMessage(dataPicReference, mediaMessage);
            setFieldValues(nextState, dataPicReference);
            // Status "Preview empfangen oder Preview Error"
            handleDBObjectOperation(dataPicReference, false, mqProject);
        }

        // Falls errors, das Vorschaubild nicht speichern
        if ((success != null) && !success.isErrorFree()) {
            return;
        }

        // Vorschaubild speichern
        if (XMLImportExportHelper.importPreviewBinaryFile(mqProject, rgmp.getBinaryFile(), dataPicReference.getVarId(),
                                                          dataPicReference.getVarRevId())) {
            nextState = PicReferenceStateMachine.getInstance().getNextStateWithoutMediaMessage(dataPicReference);
            // Status auf PREVIEW_RECEIVED setzen
            setFieldValues(nextState, dataPicReference);
        } else {
            iPartsXMLSuccess error = new iPartsXMLSuccess(false);
            error.addError(new iPartsXMLErrorText("No valid XML media binary preview file found"));
            // Status auf PREVIEW_ERROR setzen
            setFieldValues(iPartsPicReferenceState.PREVIEW_ERROR, dataPicReference, error, null, null);
        }
        handleDBObjectOperation(dataPicReference, false, mqProject);
    }

    /**
     * Entfernt Datensätze, die AS-PLM Fehler enthalten
     *
     * @param list
     */
    private void removeInvalidDatasets(iPartsDataPicReferenceList list) {
        Iterator<iPartsDataPicReference> iterator = list.iterator();
        while (iterator.hasNext()) {
            iPartsDataPicReference dataPicReference = iterator.next();
            if (iPartsPicReferenceState.hasError(dataPicReference.getStatus())) {
                iterator.remove();
            }
        }
    }

    public List<iPartsDataPicReference> adjustPicReferenceDatasets(iPartsDataPicReferenceList list, EtkProject project) {
        return adjustPicReferenceDatasets(list, true, project);
    }

    /**
     * Führt Datensätze mit der gleichen Bildreferenznummer zusammen. Das neueste Datum wird als "Hauptdatum" (Schlüsselwert)
     * gesetzt. Ältere Angaben werden in einer kommaseparierten Liste gehalten.
     *
     * @param list
     * @param withDBAction gibt an, ob die Original-Datensätze nach dem Zusammenführen gelöscht und der Hauptdatensatz
     *                     gespeicher werden soll
     * @param project
     * @return
     */
    public List<iPartsDataPicReference> adjustPicReferenceDatasets(iPartsDataPicReferenceList list, boolean withDBAction,
                                                                   EtkProject project) {
        HashMap<String, ReferenceToCompare> mainDatasets = new HashMap<>();
        for (iPartsDataPicReference picReference : list) {
            String refId = picReference.getAsId().getPicReferenceNumber();
            ReferenceToCompare toCompare = mainDatasets.get(refId);
            if (toCompare == null) {
                mainDatasets.put(refId, new ReferenceToCompare(picReference));
            } else {
                if (iPartsPicReferenceState.hasFinalState(picReference)) {
                    toCompare.getReference().setStatus(iPartsPicReferenceState.DONE);
                }
                toCompare.addDates(picReference.getPreviousDates());
                toCompare.addDate(picReference.getPicRefDate());
                toCompare.calcDates(picReference);
                if (withDBAction) {
                    handleDBObjectOperation(picReference, true, project);
                }
            }
        }
        List<iPartsDataPicReference> result = new ArrayList<>();
        for (ReferenceToCompare referenceWrapper : mainDatasets.values()) {
            if (withDBAction) {
                referenceWrapper.fillAndStoreReference(project);
            } else {
                referenceWrapper.fillReference();
            }
            result.add(referenceWrapper.getReference());
        }
        return result;
    }

    /**
     * Findet die "beste" Variante in einem übergebenen MediaContainer ("beste" bezieht sich hier auf den Colortype und
     * die vorgegebene Reihenfolge in der Klasse {@link iPartsXMLMediaContainer})
     *
     * @param mediaContainer
     * @return
     */
    private iPartsXMLMediaVariant findBestVariant(iPartsXMLMediaContainer mediaContainer) {
        List<iPartsXMLMediaVariant> mediaVariants = mediaContainer.getMediaVariants();
        if ((mediaVariants == null) || mediaVariants.isEmpty()) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Result of GetMedia response does not contain any pictures.");
            return null;
        }
        iPartsXMLMediaVariant bestVariant;
        if (mediaVariants.size() == 1) {
            bestVariant = mediaVariants.get(0);
        } else {
            bestVariant = mediaContainer.findPreferredVariantByFileType();
        }
        return bestVariant;
    }

    /**
     * Sucht das "beste" Bild in einer {@link iPartsXMLMediaVariant} und speichert es in der DB ("beste" bezieht sich
     * hier auf die Dateiendung und die vorgegebene Reihenfolge in der Klasse {@link iPartsXMLMediaVariant})
     *
     * @param variant
     * @param project
     * @return {@code true} falls ein gültiges Bild in der {@link iPartsXMLMediaVariant} gefunden wurde
     */
    private boolean findAndStoreBestPicture(iPartsXMLMediaVariant variant, EtkProject project) {
        if (variant.hasPNGImageFile() || variant.hasSVGImageFile()) {
            XMLImportExportHelper.importBinaryFile(project, variant);
            return true;
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "No valid XML media binary file found for media variant. VAR ID: " +
                                                                       variant.getItemId() + ", VAR REV ID: " + variant.getItemRevId());
            return false;
        }
    }

    /**
     * Verarbeitet die Ergebnisse zu den einzelnen Suchanfragen
     *
     * @param mediaMessage
     */
    private void handleSearchResult(iPartsXMLMediaMessage mediaMessage) {
        String messageId = XMLImportExportHelper.removeMediaContentPrefix(mediaMessage.getResponse().getXmlRequestID());
        AbstractXMLResponseOperation resultOperation = mediaMessage.getResponse().getResult();
        if (resultOperation == null) {
            checkResponseSuccess(mediaMessage, mqProject);
            return;
        }
        // MediaContainer bestimmen
        iPartsXMLMediaContainer mContainer = getMediaContainerFromSearch(resultOperation, messageId);
        if (mContainer == null) {
            return;
        }
        // Die Original Id bestimmen
        String picRefId = getRefIdFromMediaContainer(mContainer, messageId);
        if (StrUtils.isEmpty(picRefId)) {
            return;
        }

        // Da die Zuordnung nicht über das Datum möglich ist, darf bei mehreren Datensätzen mit der gleichen DASTI ID immer
        // nur eine Suchanfrage pro DASTI ID unterwergs sein. Andernfalls wäre eine spätere Zuordnung nicht möglich
        // Hole die Bildreferenz via GUID
        iPartsDataPicReferenceList list = iPartsDataPicReferenceList.loadPicReferencesWithMessageGUID(mqProject, messageId);

        if (list.size() > 1) { // Kann durch die GUID eigentlich gar nicht mehr passieren
            // Fehler: Zwei Einträge losgeschickt -> keine Zuweisung möglich
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Only one search request per picture reference number is allowed. iPartsRequestID: "
                                                                       + messageId + ", picture reference number: " + picRefId);
            return;
        }
        if (list.isEmpty()) {
            // Fehler: Vor dem Abschicken angelegter Eintrag wird nicht gefunden
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Could not find matching search request dataset. iPartsRequestID: "
                                                                       + messageId + ", picture reference number: " + picRefId);
            return;
        }
        iPartsDataPicReference dataPicReference = list.get(0);

        // Status auf "Empfangen" setzen und speichern
        iPartsPicReferenceState nextState = PicReferenceStateMachine.getInstance().getNextStateWithMediaMessage(dataPicReference, mediaMessage);
        setFieldValues(nextState, dataPicReference, mediaMessage.getResponse().getSuccess(), mContainer.getMcItemId(), mContainer.getMcItemRevId());
        handleDBObjectOperation(dataPicReference, false, mqProject);
        if (iPartsPicReferenceState.hasError(nextState)) {
            return;
        }

        // GetMediaContents Anfragen losschicken
        if (iPartsPicReferenceState.canSendGetMediaContents(nextState)) {
            if (sendMediaRequest(dataPicReference, iPartsTransferNodeTypes.GET_MEDIA_CONTENTS, mqProject)) {
                // Status: "GetMedia Anfrage losgeschickt"
                nextState = PicReferenceStateMachine.getInstance().getNextStateWithoutMediaMessage(dataPicReference);
                setFieldValues(nextState, dataPicReference);
                handleDBObjectOperation(dataPicReference, false, mqProject);
            }
        }
    }

    /**
     * Liefert die Original ID mit der nach der MC Nummer gesucht wurde
     *
     * @param mContainer
     * @param messageId
     * @return
     */
    public String getRefIdFromMediaContainer(iPartsXMLMediaContainer mContainer, String messageId) {
        String picRefId = StrUtils.removeWhitespace(mContainer.getAttValueForAttName(iPartsTransferSMCAttributes.SMC_ALTERNATE_ID.getAsASPLMValue()));
        if (StrUtils.isEmpty(picRefId)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Search response does not contain reference number. iPartsRequestID: "
                                                                       + messageId);
            return null;
        }
        return picRefId;
    }

    /**
     * Bestimmt den MediaContainer den die Suchanfrage geliefert hat
     *
     * @param resultOperation
     * @param messageId
     * @return
     */
    public iPartsXMLMediaContainer getMediaContainerFromSearch(AbstractXMLResponseOperation resultOperation, String messageId) {
        // Überprüfen, ob es sich um den gewünschten Antworttyp handelt
        iPartsXMLResSearchMediaContainers rsmc;
        if ((resultOperation != null) && (resultOperation.getResultType() == iPartsTransferNodeTypes.RES_SEARCH_MEDIA_CONTAINERS)) {
            rsmc = (iPartsXMLResSearchMediaContainers)resultOperation;
            if (rsmc.hasResultsDelivered()) {
                if (rsmc.getMContainers().size() > 1) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Search request for a picture reference should not " +
                                                                               "deliver more than one result. iPartsRequestID: " + messageId +
                                                                               "; Results delivered: " + rsmc.getNumResultsDelivered());
                }
                return rsmc.getMContainers().get(0);
            }
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Invalid response operation. Picture reference search " +
                                                                       "request expects a search response. Received Operation: "
                                                                       + ((resultOperation != null) ? resultOperation.getResultType() : "null"));

        }
        return null;
    }

    /**
     * Überprüft, ob das Success Element der übergebene MQ Message fehlerfrei ist. Falls nicht, wird der AS-PLM Fehlertext
     * via Log-Meldung ausgegeben und in der DB gespeichert.
     *
     * @param message
     * @param project
     */
    private void checkResponseSuccess(iPartsXMLMediaMessage message, EtkProject project) {
        iPartsXMLSuccess success = message.getResponse().getSuccess();
        if (success.isErrorFree()) {
            if (message.getResponse().getResult() == null) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Result operation must not be null!");
            }
            return;
        }

        Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Received response contains errors. Request operation type: "
                                                                   + message.getResponse().getRequestOperation().getAlias() + "; error text: "
                                                                   + success.getErrorText());
        String messageId = message.getResponse().getXmlRequestID();
        String messageGUID = XMLImportExportHelper.removeMediaContentPrefix(messageId);
        iPartsDataPicReferenceList list = iPartsDataPicReferenceList.loadPicReferencesWithMessageGUID(project, messageGUID);
        if (list.size() != 1) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Could not find corresponding picture reference dataset. Message ID: "
                                                                       + messageGUID);
            return;
        }
        iPartsDataPicReference reference = list.get(0);
        iPartsPicReferenceState nextState = PicReferenceStateMachine.getInstance().getNextStateWithMediaMessage(reference, message);
        setFieldValues(nextState, reference, success, reference.getVarId(), reference.getVarRevId());
        handleDBObjectOperation(reference, false, project);
    }

    private void setFieldValues(iPartsPicReferenceState nextState, iPartsDataPicReference dataPicReference) {
        setFieldValues(nextState, dataPicReference, null, null, null);
    }

    /**
     * Sendet eine GetMedia Anfrage (Vorschaubild oder eigentliches Bild)
     *
     * @param dataPicReference
     * @param nodeType
     * @param project
     */
    private synchronized boolean sendMediaRequest(iPartsDataPicReference dataPicReference, iPartsTransferNodeTypes nodeType,
                                                  EtkProject project) {
        if (dataPicReference == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Could not send " + nodeType.getAlias() + " requests for picture reference. Picture reference is null.");
            return false;
        }
        String messageGUID = dataPicReference.getMessageGUID();
        if (StrUtils.isEmpty(messageGUID)) {
            // Falls unter irgendwelchen Umständen die ID nicht gesetzt wurde -> neue Nachrichten ID erzeugen
            messageGUID = StrUtils.makeGUID();
            dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_GUID, messageGUID, DBActionOrigin.FROM_EDIT);
            handleDBObjectOperation(dataPicReference, false, project);
            String previousOperationlogInfo = (nodeType == iPartsTransferNodeTypes.GET_MEDIA_CONTENTS) ? "search" : "media";
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Message ID from picture reference " + previousOperationlogInfo + " request does not exist. Replacement ID was created: "
                                                                       + messageGUID);
        }

        AbstractXMLObjectWithMCAttributes operation;
        iPartsTransferNodeTypes simulatedAnswerNodeType;
        switch (nodeType) {
            case GET_MEDIA_CONTENTS:
                operation = new iPartsXMLGetMediaContents(dataPicReference.getMcItemId(), dataPicReference.getMcItemRevId());
                simulatedAnswerNodeType = iPartsTransferNodeTypes.RES_GET_MEDIA_CONTENTS;
                break;
            case GET_MEDIA_PREVIEW:
                operation = new iPartsXMLGetMediaPreview(dataPicReference.getMcItemId(), dataPicReference.getMcItemRevId());
                simulatedAnswerNodeType = iPartsTransferNodeTypes.RES_GET_MEDIA_PREVIEW;
                break;
            default:
                return false;
        }
        String messageId = XMLImportExportHelper.makePicReferenceGUIDForMediaContent(messageGUID);
        iPartsXMLMediaMessage xmlMessage = XMLObjectCreationHelper.getInstance().createDefaultGetPicMediaXMLMessage(operation, requestor, messageId, nodeType);
        return sendMediaMessage(xmlMessage, simulatedAnswerNodeType, getSimDelay(), false);
    }

    /**
     * Setzt abhängig vom neuen Status, die neuen Datenbankwerte.
     *
     * @param newState
     * @param dataPicReference
     * @param success
     * @param itemId
     * @param itemRevId
     */
    private void setFieldValues(iPartsPicReferenceState newState, iPartsDataPicReference dataPicReference, iPartsXMLSuccess success, String itemId, String itemRevId) {
        switch (newState) {
            case SEARCH_RECEIVED:
                dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_MC_ID, itemId, DBActionOrigin.FROM_EDIT);
                dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_MC_REV_ID, itemRevId, DBActionOrigin.FROM_EDIT);
                break;
            case MEDIA_RECEIVED:
                dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_VAR_ID, itemId, DBActionOrigin.FROM_EDIT);
                dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_VAR_REV_ID, itemRevId, DBActionOrigin.FROM_EDIT);
                break;
            case MEDIA_ERROR:
            case SEARCH_ERROR:
            case PREVIEW_ERROR:
                if (success != null) {
                    dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_ERROR_CODE, String.valueOf(success.getErrorCode()), DBActionOrigin.FROM_EDIT);
                    dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_ERROR_TEXT, success.getErrorText(),
                                                   DBActionOrigin.FROM_EDIT);
                }
                break;
        }
        dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_STATUS, newState.getDbValue(), DBActionOrigin.FROM_EDIT);
        dataPicReference.setFieldValueAsDateTime(iPartsConst.FIELD_DPR_LAST_MODIFIED, GregorianCalendar.getInstance(), DBActionOrigin.FROM_EDIT);
    }

    /**
     * Passt bestehende Bildreferenzen mit DASTI Bildnummer an die MediaContainer Varianten IDs an
     *
     * @param imageList
     * @param dataPicReference
     * @param newId
     * @param newRevId
     * @param project
     */
    private void updateImageInDB(EtkDataImageList imageList, iPartsDataPicReference dataPicReference,
                                 String newId, String newRevId, EtkProject project) {
        if ((imageList != null) && !imageList.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (EtkDataImage image : imageList) {
                if (builder.length() != 0) {
                    builder.append("; ");
                }
                builder.append("Pic reference \"" + image.getAsId().toString("|") + "\": ");
                builder.append("Values (before/after): " + image.getFieldValue(EtkDbConst.FIELD_I_IMAGES) + "/" + newId + ", "
                               + image.getFieldValue(EtkDbConst.FIELD_I_PVER) + "/" + newRevId + ", "
                               + image.getFieldValue(iPartsConst.FIELD_I_IMAGEDATE) + "/" + dataPicReference.getAsId().getPicReferenceDate());
                // Setze die neuen Werte
                image.setFieldValue(EtkDbConst.FIELD_I_IMAGES, newId, DBActionOrigin.FROM_EDIT);
                image.setFieldValue(EtkDbConst.FIELD_I_PVER, newRevId, DBActionOrigin.FROM_EDIT);
                image.setFieldValue(iPartsConst.FIELD_I_IMAGEDATE, dataPicReference.getAsId().getPicReferenceDate(), DBActionOrigin.FROM_EDIT);
            }
            handleDBObjectList(imageList, false, project);
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, builder.toString());
        } else {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, "Could not fetch pic reference from DB with given reference number."
                                                                       + " Reference number: " + dataPicReference.getAsId().getPicReferenceNumber()
                                                                       + " MC ID: " + dataPicReference.getMcItemId()
                                                                       + ", MC REV ID: " + dataPicReference.getMcItemRevId()
                                                                       + ", VAR ID: " + newId
                                                                       + ", VAR REV ID: " + newRevId);
        }
    }

    /**
     * Lädt die DB Bildreferenzen zu der DASTi Bildnummer. Diese werden später durch die neue Varianten IDs ausgetauscht.
     *
     * @param dataPicReference
     * @param oldVarId
     * @param project
     * @return
     */
    private EtkDataImageList getImageList(iPartsDataPicReference dataPicReference, String oldVarId, EtkProject project, boolean onlyOldId) {
        EtkDataImageList imageList = EtkDataObjectFactory.createDataImageList();
        if (!onlyOldId) {
            imageList.loadImagesForImageNumber(project, dataPicReference.getAsId().getPicReferenceNumber());
        }
        // Beim Nachfordern der Zeichnungen kann es vorkommen, dass die Verknüpfung schon mit der Variantennummer angelegt wurde
        if (imageList.isEmpty()) {
            // Gibt es eine explizite alte Variantennummer?
            if (StrUtils.isValid(oldVarId)) {
                imageList.loadImagesForImageNumber(project, oldVarId);
            }
            if (!onlyOldId) {
                if (imageList.isEmpty()) {
                    imageList.loadImagesForImageNumber(project, dataPicReference.getVarId());
                }
            }
        }
        return imageList;
    }

    /**
     * Speichert oder löscht {@link EtkDataObject} Objekte
     *
     * @param dataObject
     * @param delete
     * @param project
     */
    private void handleDBObjectOperation(EtkDataObject dataObject, boolean delete, EtkProject project) {
        if (dataObject == null) {
            return;
        }
        try {
            project.getDbLayer().startTransaction();
            if (delete) {
                dataObject.deleteFromDB();
            } else {
                dataObject.saveToDB();
            }
            project.getDbLayer().commit();
        } catch (Exception e) {
            project.getDbLayer().rollback();
            Logger.getLogger().throwRuntimeException(e);
        }
    }

    /**
     * Speichert {@link EtkDataObjectList} Objekte
     *
     * @param objectList
     * @param delete
     * @param project
     */
    private void handleDBObjectList(EtkDataObjectList objectList, boolean delete, EtkProject project) {
        if ((objectList == null) || objectList.isEmpty()) {
            return;
        }
        try {
            project.getDbLayer().startTransaction();
            project.getDbLayer().startBatchStatement();
            if (delete) {
                objectList.deleteFromDB(project);
            } else {
                String imageListLogMessage = "ImageList modified before/after save: " + objectList.isModifiedWithChildren();
                objectList.saveToDB(project);
                Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.DEBUG, imageListLogMessage + "/" + objectList.isModifiedWithChildren());
            }
            project.getDbLayer().endBatchStatement();
            project.getDbLayer().commit();
        } catch (Exception e) {
            project.getDbLayer().cancelBatchStatement();
            project.getDbLayer().rollback();
            Logger.getLogger().throwRuntimeException(e);
        }

    }

    /**
     * Aktiviert den Scheduler
     *
     * @return {@code true} falls der Scheduler gestartet wurde
     */
    public boolean activatePicWorker() {
        if (!doHandlePicRefs) {
            doHandlePicRefs = true;
        }
        if ((workerThread == null) || workerThread.isFinished()) {
            initWorkerThread();
            return true;
        }

        return false;
    }

    /**
     * Initialisiert den Worker Thread, der Suchanfragen Richtung AS-PLM erstellt und verschickt
     */
    private void initWorkerThread() {
        if ((mqProject == null) || (mqSession == null)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "EtkProject or session for MQPicScheduler.initWorkerThread() is null. The method registerMQListener() must be called first.");
            return;
        }

        workerThread = mqSession.startChildThread(thread -> {
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "Starting MQ search picture reference scheduler thread...");
            while (!Java1_1_Utils.sleep(1000) && doHandlePicRefs && AbstractApplication.getApplication().isRunning()) {
                // Aktiv-Zustand der DB-Verbindung vom (MQ) EtkProject überprüfen
                iPartsPlugin.assertProjectDbIsActive(mqProject, "MQPicScheduler", iPartsPlugin.LOG_CHANNEL_MQ);

                iPartsDataPicReferenceList list = iPartsDataPicReferenceList.loadNewBatchForProcessing(mqProject);
                doHandlePicRefs = !list.isEmpty();
                if (doHandlePicRefs) {
                    sendSearchMessage(list, mqProject);
                }
            }
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ search picture reference scheduler thread finished");
        });
        workerThread.setName("MQ picture references worker");
    }

    /**
     * Erzeugt aus jedem {@link iPartsDataPicReference} Objekt eine MQ Suchanfrage und verschickt sie
     *
     * @param picReferenceList
     * @param project
     * @return
     */
    private synchronized boolean sendSearchMessage(iPartsDataPicReferenceList picReferenceList, EtkProject project) {
        if (picReferenceList == null) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_XML_MQ, LogType.ERROR, "Could not send search requests for picture references. Picture references list is null.");
            return false;
        }
        HashSet<String> refIdsSentRigthNow = new HashSet<>();
        // Die Nachricht ist die gleiche, nur das Suchkriterium sowie die Request ID ändern sich
        iPartsXMLSearchMediaContainers container = new iPartsXMLSearchMediaContainers();
        container.setMaxResultFromIParts(10);
        iPartsXMLMediaMessage xmlMessage = XMLObjectCreationHelper.getInstance().createDefaultPicSearchXMLMessage(container, requestor,
                                                                                                                  "",
                                                                                                                  false);
        for (iPartsDataPicReference picReference : picReferenceList) {
            if (Thread.currentThread().isInterrupted()) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "Sending MQ picture reference search messages cancelled...");
                return false;
            }

            String picRefId = picReference.getAsId().getPicReferenceNumber();
            if (!iPartsPicReferenceState.canSendSearch(picReference.getStatus()) || refIdsSentRigthNow.contains(picRefId)) {
                continue;
            }

            iPartsDataPicReferenceList sameImageNumbers = iPartsDataPicReferenceList.loadPicRefForState(project, picRefId,
                                                                                                        iPartsPicReferenceState.SEARCH_SENT);
            if (!sameImageNumbers.isEmpty()) {
                continue;
            }

            String messageGUID = picReference.getMessageGUID();
            if (StrUtils.isEmpty(messageGUID)) {
                messageGUID = StrUtils.makeGUID();
            }
            String messageID = XMLImportExportHelper.makePicReferenceGUIDForMediaContent(messageGUID);
            xmlMessage.getRequest().setXmlRequestID(messageID);
            iPartsPicReferenceState nextState = PicReferenceStateMachine.getInstance().getNextStateWithoutMediaMessage(picReference);
            setFieldValues(nextState, picReference);
            container.removeSearchCriterion(iPartsTransferSMCAttributes.SMC_ALTERNATE_ID);
            container.addSearchCriterion(iPartsTransferSMCAttributes.SMC_ALTERNATE_ID, picRefId);
            picReference.setFieldValue(iPartsConst.FIELD_DPR_GUID, messageGUID, DBActionOrigin.FROM_EDIT);
            if (!sendMediaMessage(xmlMessage, iPartsTransferNodeTypes.RES_SEARCH_MEDIA_CONTAINERS, getSimDelay(), false)) {
                return false;
            }
            refIdsSentRigthNow.add(picRefId);
            handleDBObjectOperation(picReference, false, project);
        }
        return true;
    }

    /**
     * Versendet die angegebene XML MQ Message mit otionaler simulierter Antwort.
     *
     * @param xmlMessage
     * @param simulatedAnswerNodeType
     * @return
     */
    public synchronized boolean sendMediaMessage(iPartsXMLMediaMessage xmlMessage, iPartsTransferNodeTypes simulatedAnswerNodeType, int simDelay, boolean doError) {
        try {
            iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).sendXMLMessageWithMQ(iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA, xmlMessage,
                                                                                                                 (simDelay >= 0) && simulateAnswers);
            simulateMQAnswer(xmlMessage, simulatedAnswerNodeType, simDelay, doError);
        } catch (MQException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);
            return false;
        }
        return true;
    }

    /**
     * Simuliert eine MQ Antwort
     *
     * @param xmlMessage
     * @param simDelay
     */
    private void simulateMQAnswer(iPartsXMLMediaMessage xmlMessage, iPartsTransferNodeTypes type, int simDelay, boolean doError) {
        // Erwartete AS-PLM-Antwort zu Simulationszwecken erzeugen und optional versenden
        if ((xmlMessage != null) && (simDelay >= 0)) {
            iPartsXMLMediaMessage expectedResponeXmlMessage = null;
            boolean writeXML = false;
            int tempSimDelay = -1;
            Random r = new Random();
            boolean simError = doError || ((r.nextInt(3) > 1) && SIMULATE_ERRORS); // 1:3 Wahrscheinlichkeit für einen Fehler oder wenn explizit gewünscht
            switch (type) {
                case RES_SEARCH_MEDIA_CONTAINERS:
                    expectedResponeXmlMessage = iPartsXMLResponseSimulator.createPicSearchResponse(xmlMessage, true, simError);
                    break;
                case RES_GET_MEDIA_CONTENTS:
                    expectedResponeXmlMessage = iPartsXMLResponseSimulator.createPicContentResponse(xmlMessage, simError, false, false);
                    break;
                case RES_GET_MEDIA_PREVIEW:
                    expectedResponeXmlMessage = iPartsXMLResponseSimulator.createPicPreviewResponse(xmlMessage, simError);
                    break;
                case RES_ABORT_MEDIA_ORDER:
                    expectedResponeXmlMessage = iPartsXMLResponseSimulator.createAbortMediaOrderResponse(xmlMessage, simError);
                    break;
            }
            if (expectedResponeXmlMessage != null) {
                tempSimDelay = simDelay;
                writeXML = tempSimDelay >= 0;
            }
            if (!simulateAnswers) { // Simulation hart ausschalten
                tempSimDelay = -1;
            }
            if (!writeXML && (tempSimDelay < 0)) { // kein simuliertes Antwort-XML notwendig
                return;
            }
            if (expectedResponeXmlMessage != null) {
                iPartsXMLResponseSimulator.writeAndSendSimulatedMessageResponseFromXML(iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA,
                                                                                       expectedResponeXmlMessage,
                                                                                       writeXML,
                                                                                       tempSimDelay);
            }
        }
    }

    /**
     * Registriert die Listener für die Such- sowie Bildanfrage
     *
     * @param project
     * @param session
     */
    public void registerMQListener(EtkProject project, Session session) {
        this.mqProject = project;
        this.mqSession = session;
        xmlSearchMediaListener = new AbstractXMLMessageListener(session) {
            @Override
            public boolean messageReceived(AbstractMQMessage xmlMQMessage) {
                if (xmlMQMessage.isOfType(iPartsXMLMediaMessage.TYPE)) {
                    iPartsXMLMediaMessage mediaMessage = (iPartsXMLMediaMessage)xmlMQMessage;
                    if (mediaMessage.isNotificationOnly()) {
                        return false; // keine DB-Aktionen
                    }

                    // Nur PicReference-Antworten bearbeiten
                    if (!mediaMessage.isResponse()) {
                        Logger.getLogger().throwRuntimeException("Message type must be an iPartsXMLResponse! Type is: "
                                                                 + mediaMessage.getClass().getName());
                        return true; // keine weitere Verarbeitung sinnvoll
                    }
                    String iPartsRequestId = mediaMessage.getResponse().getiPartsRequestID();
                    if (XMLImportExportHelper.isMediaContentFromPicReference(iPartsRequestId)) {
                        handleSearchResult(mediaMessage);
                        return true; // keine weitere Verarbeitung notwendig
                    }
                }
                return false;
            }
        };
        iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).addXMLMessageListenerForMessageTypes(xmlSearchMediaListener,
                                                                                                                             iPartsTransferNodeTypes.SEARCH_MEDIA_CONTAINERS);

        xmlMediaContentsListener = new AbstractXMLMessageListener(session) {
            @Override
            public boolean messageReceived(AbstractMQMessage xmlMQMessage) {
                if (xmlMQMessage.isOfType(iPartsXMLMediaMessage.TYPE)) {
                    iPartsXMLMediaMessage mediaMessage = (iPartsXMLMediaMessage)xmlMQMessage;
                    if (mediaMessage.isNotificationOnly()) {
                        return false; // keine DB-Aktionen
                    }

                    // Nur PicReference-Antworten bearbeiten
                    String iPartsRequestId = mediaMessage.getResponse().getiPartsRequestID();
                    if (XMLImportExportHelper.isMediaContentFromPicReference(iPartsRequestId)) {
                        handleMediaMessage(mediaMessage);
                        return true; // keine weitere Verarbeitung notwendig
                    }
                }
                return false;
            }
        };
        iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).addXMLMessageListenerForMessageTypes(xmlMediaContentsListener,
                                                                                                                             iPartsTransferNodeTypes.GET_MEDIA_CONTENTS,
                                                                                                                             iPartsTransferNodeTypes.GET_MEDIA_PREVIEW);
    }

    /**
     * Startet den MQ Workflow indem geprüft wird, ob die Referenz schon auf ein Bild in der DB referenziert. Falls nicht,
     * werden in DA_PIC_REFERENCE Datensätze angelegt, die andeuten, dass diese Referenzen bei AS-PLM angefragt werden sollen.
     * Es wird für das Suchen und Zusammenlegen der Bildreferenzen ein eigener Thread mit eigener Transaktion verwendet und
     * auf das Beenden des Threads gewartet, damit der Aufruf auch innerhalb von anderen Transaktionen problemlos stattfinden
     * kann und die Daten dennoch auf jeden Fall sofort in die Datenbank geschrieben werden.
     *
     * @param picReferenceIds
     * @param project
     * @param messageLog
     */
    public void startRetrievingImages(final Set<iPartsPicReferenceId> picReferenceIds, final EtkProject project, EtkMessageLog messageLog) {
        // Aktiv-Zustand der DB-Verbindung vom (MQ) EtkProject überprüfen
        iPartsPlugin.assertProjectDbIsActive(project, "MQPicScheduler", iPartsPlugin.LOG_CHANNEL_MQ);

        if (iPartsPlugin.isImportPluginActive()) {
            // Ist das Holen der Bilder von ASPLM abgeschaltet, sollen die Bilder aus der Verzeichnisstruktur geholt werden.
            de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.images.DASTiPictureHelper helper = new de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.images.DASTiPictureHelper(project);
            if (helper.isDastiDirStructurePictureImportEnabled()) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "Retrieving picture references from directory "
                                                                       + helper.getConfiguredPictureImportRootDir());

                // Erst die Bildreferenz(en) in die Datenbank eintragen.
                List<iPartsDataPicReference> storedReferences = importPicReferences(picReferenceIds, project, true);
                if (storedReferences == null) {
                    return;
                }
                retrieveImagesFromFileSystem(project, storedReferences, messageLog, false);

            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "Retrieving picture references via MQ is " +
                                                                       (isMQWorkflowEnabled() ? "enabled" : "disabled"));

                // Das Speichern in einem eigenen Thread für die (MQ) Session erledigen, damit vor dem Aufruf von activatePicWorker()
                // die Transaktion auch auf jeden Fall durchgeführt wurde
                FrameworkThread saveThread = Session.startChildThreadInSession(thread -> {
                    importPicReferences(picReferenceIds, project, false);

                    if (J2EEHandler.isJ2EE() && !MQHelper.isPreventTransmissionToASPLM() && isMQWorkflowEnabled()) {
                        MQPicScheduler.getInstance().activatePicWorker();
                    }
                });
                saveThread.setName("Picture references startRetrievingImages thread");
                saveThread.waitFinished(); // Durch das Warten einen synchronen Aufruf trotz Speichern-Thread bewirken
            }
        }
    }

    /**
     * Abfragen der Bilder über das Dateisystem (DASTi-Import)
     *
     * @param project
     * @param references
     * @param messageLog
     * @param importOnlyNewerOnes Sollen nur Bilder mit neuerem Referenzdatum importiert werden?
     */
    private void retrieveImagesFromFileSystem(EtkProject project, List<iPartsDataPicReference> references, EtkMessageLog messageLog,
                                              boolean importOnlyNewerOnes) {
        if (!iPartsPlugin.isImportPluginActive()) {
            return;
        }

        de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.images.DASTiPictureHelper helper = new de.docware.apps.etk.plugins.customer.daimler.iparts_import.helper.images.DASTiPictureHelper(project);
        if (!helper.getConfiguredPictureImportRootDir().isEmpty()) { // isDastiDirStructurePictureImportEnabled() wertet auch die Admin-Einstellung der Mediensuche aus
            // Suche Bilder im DASTi-Verzeichnis und importiere sie. Referenzen zu denen Bilder gefunden wurden
            // erhalten den Status "DONE". Referenzen zu denen keine Bilder gefunden wurden haben weiterhin den
            // Status "NEW".
            iPartsDataPicReferenceList handledReferences = helper.handleReferencesFromMigration(references, messageLog, importOnlyNewerOnes);
            // Wurden Bilder zu bestehenden Referenzen im Verzeichnis nicht gefunden (obwohl die Bildnummer im
            // eigentlichen Import vorkam). Dann soll laut DAIMLER die Referenz zum alten Bild aufgebaut werden.
            // Zusätzlich erhält die Referenz den Status "DONE", weil das Bild damit wieder fertig ist.
            restoreReferenceIfNotInFileSystem(project, handledReferences, messageLog);
            handleDBObjectList(handledReferences, false, project);
        }
    }

    /**
     * Überprüft, ob zuvor bestehende Referenzen wieder hergestellt werden sollen. Falls ja, wird der jeweilige Eintrag
     * IMAGES angepasst.
     * 1. Szenario:
     * Referenz, die via AS-PLM Anfrage erzeugt wurde liegt in der DB. Über den Verzeichnisimporter kommt eine Referenz
     * mit gleicher Bildnummer aber jüngeren Datum. Wenn das Bild im erzeugten DASTi Verzeichnis gefunden wurde und
     * in DA_PIC_REFERENCE schon ein Eintrag mit Varianten IDs existierte, dann wird dieser Eintrag geleert und das
     * dazugehörige Bild mit der alten VariantenID aus der DB gelöscht. Die Referenz von Varianten ID zur DASTi Nummer
     * wurde ja schon zovor angelegt.
     * <p>
     * 2. Szenario:
     * Referenz, die via AS-PLM Anfrage erzeugt wurde liegt in der DB. Über den Verzeichnisimporter kommt eine Referenz
     * mit gleicher Bildnummer aber jüngeren Datum. Wenn das Bild im erzeugten DASTi Verzeichnis NICHT gefunden wurde und
     * in DA_PIC_REFERENCE schon ein Eintrag mit Varianten IDs existierte, dann wird die zuvor erzeugte IMAGES Referenz
     * wieder auf die Varianten ID umgebogen. Das sorgt dafür, dass bei fehlendem Bild nach dem Verzeichnisimport das
     * vorherige Bild angezeigt wird.
     *
     * @param project
     * @param handledReferences
     * @param messageLog
     */
    private void restoreReferenceIfNotInFileSystem(EtkProject project, iPartsDataPicReferenceList handledReferences, EtkMessageLog messageLog) {
        EtkDataObjectList objectsToDelete = EtkDataObjectFactory.createDataImageList();
        for (iPartsDataPicReference picReference : handledReferences) {
            String varIdInDB = picReference.getVarId();
            String varRevIdInDB = picReference.getVarRevId();
            boolean varIdValid = StrUtils.isValid(varIdInDB, varRevIdInDB);
            // Wenn keine Varianten IDs exitiert haben, dann brauchen wir die Referenz in IMAGES nicht umzubiegen, weil
            // ja vorher schon auf die DASTi Bildnummer und nicht auf eine Varianten ID refrenziert wurde.
            if (varIdValid) {
                if (picReference.getStatus() == iPartsPicReferenceState.NOT_FOUND) {
                    picReference.setStatus(iPartsPicReferenceState.DONE);
                    updateImageTableReference(picReference, varIdInDB, varRevIdInDB, null, project, false);
                    writeMessage(messageLog, TranslationHandler.translateForLanguage("!!Bildreferenz \"%1\" mit Datum \"%2\" wurde beim Verzeichnisimport nicht " +
                                                                                     "gefunden und wird wieder mit der Zeichnung aus AS-PLM verknüpft (%3 - %4).",
                                                                                     iPartsConst.LOG_FILES_LANGUAGE,
                                                                                     picReference.getAsId().getPicReferenceNumber(),
                                                                                     picReference.getAsId().getPicReferenceDate(),
                                                                                     varIdInDB, varRevIdInDB));
                } else if (picReference.getStatus() == iPartsPicReferenceState.DONE) {
                    // Lösche bestehende IDs (MC und Varianten IDs) weil wir ja via Verzeichnis importiert haben.
                    // DONE ist der Hinweis darauf, dass Bilder im Verzeichnis gefunden und importiert wurden
                    cleanPicReferenceFromFileSystemImport(picReference);
                    updateImageTableReference(picReference, picReference.getAsId().getPicReferenceNumber(), "", varIdInDB,
                                              project, true);
                    PoolEntryId poolEntryId = new PoolEntryId(varIdInDB, varRevIdInDB);
                    PoolId poolId = new PoolId(varIdInDB, varRevIdInDB, "", "");
                    addPicturesToDeleteList(project, objectsToDelete, poolEntryId, poolId);
                    writeMessage(messageLog, TranslationHandler.translateForLanguage("!!Bildreferenz \"%1\" mit Datum \"%2\" war zuvor " +
                                                                                     "über eine Zeichnung aus AS-PLM verknüpft. " +
                                                                                     "Die Varianten ID \"%3 - %4\" am Referenzdatensatz wird entfernt.",
                                                                                     iPartsConst.LOG_FILES_LANGUAGE,
                                                                                     picReference.getAsId().getPicReferenceNumber(),
                                                                                     picReference.getAsId().getPicReferenceDate(),
                                                                                     varIdInDB, varRevIdInDB));
                }
            }
        }
        handleDBObjectList(objectsToDelete, true, project);
    }

    /**
     * Schreibt die Bildreferenzen direkt in die Datenbank
     *
     * @param picReferenceIds
     * @param project
     */
    private List<iPartsDataPicReference> importPicReferences(final Set<iPartsPicReferenceId> picReferenceIds, final EtkProject project, boolean importFromFileSystem) {
        List<iPartsDataPicReference> result = new ArrayList<>();
        try {
            project.getDbLayer().startTransaction();
            final iPartsDataPicReferenceList list = new iPartsDataPicReferenceList();
            boolean startWorker = false;
            EtkDataObjectList objectsToDelete = EtkDataObjectFactory.createDataImageList();
            for (iPartsPicReferenceId picRefId : picReferenceIds) {
                if (Thread.currentThread().isInterrupted()) {
                    Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "Retrieving MQ picture references cancelled...");
                    project.getDbLayer().rollback();
                    return null;
                }

                iPartsDataPicReference dataPicReference = new iPartsDataPicReference(project, picRefId);
                if (!dataPicReference.existsInDB()) {
                    // Der genaue Datensatz existiert nicht in der DB. Überprüfe, ob ein ähnlicher Datensatz in der DB
                    // existiert (gleiche Bildreferenz, anderes Datum)
                    iPartsDataPicReferenceList sameRefIdsInDB = iPartsDataPicReferenceList.loadPicReferencesWithoutDate(project, picRefId);
                    dataPicReference.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    dataPicReference.setStatus(iPartsPicReferenceState.NEW);
                    if (sameRefIdsInDB.isEmpty()) {
                        // Kein ähnlicher vorhanden -> mache normal weiter
                        putPicReferenceInSearchList(dataPicReference, list);
                    } else {
                        // es existiert ein ähnlicher Datensatz in der DB -> Aktualisiere die Referenz zum Modul mit der
                        // Varianten ID falls eine existiert und merge beide Datensätze
                        iPartsDataPicReference picReferenceInDB = sameRefIdsInDB.get(0);
                        sameRefIdsInDB.add(dataPicReference, DBActionOrigin.FROM_EDIT);
                        iPartsDataPicReference mergedPicReference = adjustPicReferenceDatasets(sameRefIdsInDB, true, project).get(0);
                        String varIdInDB = picReferenceInDB.getVarId();
                        String varRevIdInDB = picReferenceInDB.getVarRevId();
                        boolean varIdValid = StrUtils.isValid(varIdInDB, varRevIdInDB);
                        if ((mergedPicReference.getStatus() == iPartsPicReferenceState.DONE) && varIdValid) {
                            // Der zusammengesetzte Datensatz hat als Status "DONE". D.h. der fertige Datensatz hatte das größte Datum
                            // -> Die Verknüpfung vom neuen Datensatz in IMAGES muss auf die Varianten IDs vom bestehenden
                            // Datensatz verweisen
                            updateImageTableReference(dataPicReference, varIdInDB, varRevIdInDB, null, project, false);
                        } else if (mergedPicReference.getStatus() == iPartsPicReferenceState.NEW) {
                            // Nach dem Zusammenlegen von Referenzen mit der gleichen DASTi Nummer hat der erzeugte Datensatz
                            // den Status "NEU". D.h. einer der neuen Datensätze hatte ein DASTi Datum das jünger ist
                            // als das Datum vom bestehenden Datensatz -> Starte den MQWorker (es gibt ja anscheinend
                            // ein neueres Bild)
                            startWorker = true;
                            PoolEntryId poolEntryId = null;
                            PoolId poolId = null;
                            if (varIdValid) {
                                // Wenn der bestehende Datensatz schon ein fertiges Bild hatte, dann muss man die Referenz in
                                // der DB umbiegen...
                                updateImageTableReference(mergedPicReference, mergedPicReference.getAsId().getPicReferenceNumber(),
                                                          "", varIdInDB, project, true);
                            }
                            // Wenn der Import über das Verzeichnis getriggert wurde, dann kann man hier noch nicht
                            // entscheiden, ob die vorherigen Bilder gelöscht werden sollen. Das wird erst entschieden,
                            // wenn geprüft wurde, ob die Bilder in den DASTi Verzeichnissen existieren
                            if (!importFromFileSystem) {
                                if (varIdValid) {
                                    // Wenn es ein Import über AS-PLM ist, dann wird hier das Bild gelöscht, die Referenz
                                    // geleert, weil wir durch die Anfrage ja neue Bilder bekommen (bei Fehlern hätten wir halt keine)
                                    cleanPicReferenceFromFileSystemImport(mergedPicReference);
                                    poolEntryId = new PoolEntryId(varIdInDB, varRevIdInDB);
                                    poolId = new PoolId(varIdInDB, varRevIdInDB, "", "");
                                } else {
                                    // Die bestehende Referenz in der DB hatte keine VariantenIDs, d.h. sie wurde zuvor über
                                    // den Verzeichnisimporter angelegt. Lösche das bestehende Bild, weil wir über AS-PLM
                                    // ein neues Bild bekommen
                                    poolEntryId = new PoolEntryId(mergedPicReference.getAsId().getPicReferenceNumber(), "");
                                    poolId = new PoolId(mergedPicReference.getAsId().getPicReferenceNumber(), "", "", "");
                                }
                            }
                            if ((poolEntryId != null) && (poolId != null)) {
                                addPicturesToDeleteList(project, objectsToDelete, poolEntryId, poolId);
                            }
                        }
                        putPicReferenceInSearchList(mergedPicReference, list);
                    }
                } else if (iPartsPicReferenceState.isNotFound(dataPicReference.getStatus())
                           || iPartsPicReferenceState.isSendError(dataPicReference.getStatus())) {
                    // nicht gefundene Anfragen und Sendungsfehler sollen nochmals verschickt werden
                    putPicReferenceInSearchList(dataPicReference, list);
                } else {
                    // Falls die übergebenen PictureReferenceIds schon in der DB existieren, ihr Status aber auf "NEW" steht,
                    // dann soll der ActiveWorker trotzdem starten
                    if (!startWorker && iPartsPicReferenceState.isNew(dataPicReference.getStatus())) {
                        startWorker = true;
                    }
                }
                if (objectsToDelete.size() > AbstractGenericImporter.MAX_ENTRIES_FOR_BUFFERED_SAVE_AND_COMMIT) {
                    handleDBObjectList(objectsToDelete, true, project);
                    objectsToDelete.clear(DBActionOrigin.FROM_EDIT);
                }
            }
            handleDBObjectList(objectsToDelete, true, project);
            objectsToDelete.clear(DBActionOrigin.FROM_EDIT);

            if (list.isEmpty() && !startWorker) {
                project.getDbLayer().commit();
                return result;
            }

            // Es kann passieren, dass mehrere gleiche Referenzen in einem Import vorkommen. Diese müssen zu einem Datensatz
            // zusammengeführt werden. Dadurch erspart man sich mehrere Anfragen für die gleiche Bildnummer und das spätere
            // Zusammenführen in handleMediaContent()
            result = adjustPicReferenceDatasets(list, true, project);

            if (Thread.currentThread().isInterrupted()) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "Retrieving MQ picture references cancelled...");
                project.getDbLayer().rollback();
                return null;
            }

            project.getDbLayer().commit();
        } catch (Exception e) {
            project.getDbLayer().rollback();

            // Die Exception nicht erneut werfen wie sonst bei rollback() üblich, da wir uns hier explizit in einem
            // eigenen Thread befinden und die Transaktion damit hier auf jeden Fall zu Ende ist. Außerdem soll bei
            // einem fehler der aufrufende Importer hiervon nicht betroffen sein.
            // Logausgabe reicht, da ein Fehlerdialog in der GUI-losen MQ Session sowieso nicht möglich wäre
            Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "Error while saving new picture references in MQPicScheduler.startRetrievingImages()");
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);
        }
        return result;
    }

    private void addPicturesToDeleteList(EtkProject project, EtkDataObjectList objectsToDelete, PoolEntryId poolEntryId, PoolId poolId) {
        // Das aktuelle Bild löschen (es kommt ja ein neues, dass das alte ersetzen soll)
        EtkDataPoolEntry poolentry = EtkDataObjectFactory.createDataPoolEntry();
        poolentry.init(project);
        if (poolentry.loadFromDB(poolEntryId)) {
            objectsToDelete.add(poolentry, DBActionOrigin.FROM_DB);
        }
        EtkDataPool poolObject = EtkDataObjectFactory.createDataPool();
        poolObject.init(project);
        if (poolObject.loadFromDB(poolId)) {
            objectsToDelete.add(poolObject, DBActionOrigin.FROM_DB);
        }
        EtkDataHotspotList hotspotList = EtkDataObjectFactory.createDataHotspotList();
        hotspotList.loadHotspotsFromDB(project, poolEntryId.getPEImages(), poolEntryId.getPEVer(), "", EtkDataImage.IMAGE_USAGE_2D,
                                       DBActionOrigin.FROM_DB);
        objectsToDelete.addAll(hotspotList, DBActionOrigin.FROM_DB);

    }

    private void cleanPicReferenceFromFileSystemImport(iPartsDataPicReference dataPicReference) {
        dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_ERROR_CODE, "", DBActionOrigin.FROM_EDIT);
        dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_ERROR_TEXT, "", DBActionOrigin.FROM_EDIT);
        dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_MC_ID, "", DBActionOrigin.FROM_EDIT);
        dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_MC_REV_ID, "", DBActionOrigin.FROM_EDIT);
        dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_VAR_ID, "", DBActionOrigin.FROM_EDIT);
        dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_VAR_REV_ID, "", DBActionOrigin.FROM_EDIT);

    }

    private boolean isMQWorkflowEnabled() {
        if (iPartsPlugin.isImportPluginActive()) {
            ImageImportSource configuredImportSource =
                    de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.getSelectedImageImportSource();
            return configuredImportSource.equals(ImageImportSource.ASPLM);
        }
        return true;
    }

    public String createStartRetrievingImagesLogMessage(int imagesCount, AbstractGenericImporter importer) {
        ImageImportSource configuredImportSource = ImageImportSource.NONE;
        if (iPartsPlugin.isImportPluginActive()) {
            configuredImportSource =
                    de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.getSelectedImageImportSource();
        }

        if (configuredImportSource != ImageImportSource.NONE) {
            return importer.translateForLog("!!Starte Synchronisierung der %1 importierten Bildreferenzen (%2)...",
                                            String.valueOf(imagesCount), importer.translateForLog(configuredImportSource.getDescription()));
        } else {
            return importer.translateForLog("!!Keine Synchronisierung der %1 importierten Bildreferenzen, da die Mediensuche deaktiviert ist",
                                            String.valueOf(imagesCount));
        }
    }

    private void putPicReferenceInSearchList(iPartsDataPicReference dataPicReference, iPartsDataPicReferenceList list) {
        dataPicReference.setFieldValueAsDateTime(iPartsConst.FIELD_DPR_LAST_MODIFIED, GregorianCalendar.getInstance(), DBActionOrigin.FROM_EDIT);
        dataPicReference.setFieldValue(iPartsConst.FIELD_DPR_STATUS, iPartsPicReferenceState.NEW.getDbValue(), DBActionOrigin.FROM_EDIT);
        list.add(dataPicReference, DBActionOrigin.FROM_EDIT);
    }

    /**
     * Deregistriert die Listener für die Such- sowie Bildanfrage
     */
    public void deregisterMQListener() {
        if (xmlSearchMediaListener != null) {
            iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).removeXMLMessageListenerForMessageTypes(xmlSearchMediaListener,
                                                                                                                                    iPartsTransferNodeTypes.SEARCH_MEDIA_CONTAINERS);
            xmlSearchMediaListener = null;
        }
        if (xmlMediaContentsListener != null) {
            iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).removeXMLMessageListenerForMessageTypes(xmlMediaContentsListener,
                                                                                                                                    iPartsTransferNodeTypes.GET_MEDIA_CONTENTS,
                                                                                                                                    iPartsTransferNodeTypes.GET_MEDIA_PREVIEW);
            xmlMediaContentsListener = null;
        }
    }

    public void stopWorkerThread() {
        if (workerThread != null) {
            workerThread.cancel();
            workerThread = null;
        }
    }


    private class ReferenceToCompare {

        private iPartsDataPicReference actualReference;
        private Set<Calendar> allDates = new HashSet<>();
        private long highestDoneDate;
        private long highestDateOtherThanDone;
        private iPartsPicReferenceState highestStateForNonDoneDate;

        public ReferenceToCompare(iPartsDataPicReference picReference) {
            this.actualReference = picReference;
            addDates(picReference.getPreviousDates());
            addDate(picReference.getPicRefDate());
            calcDates(picReference);

        }

        /**
         * Berechnet das höchste "DONE" und nicht-"DONE" Datum, der einzelnen Referenzen aus denen später die endgültige
         * Referenz erzeugt wird.
         *
         * @param picReference
         */
        public void calcDates(iPartsDataPicReference picReference) {
            long dateAsLong = StrUtils.strToLongDef(picReference.getAsId().getPicReferenceDate(), -1);
            if (iPartsPicReferenceState.hasFinalState(picReference)) {
                highestDoneDate = getYoungerDate(highestDoneDate, dateAsLong);
            } else {
                highestDateOtherThanDone = getYoungerDate(highestDateOtherThanDone, dateAsLong);
                if (highestDateOtherThanDone == dateAsLong) {
                    highestStateForNonDoneDate = picReference.getStatus();
                }
            }
        }

        private long getYoungerDate(long... datesAsLong) {
            long result = -1;
            if (datesAsLong != null) {
                for (long dateAsLong : datesAsLong) {
                    if (dateAsLong > result) {
                        result = dateAsLong;
                    }
                }
            }
            return result;
        }


        public void addDates(List<Calendar> allDates) {
            if ((allDates != null) && !allDates.isEmpty()) {
                this.allDates.addAll(allDates);
            }
        }

        public void addDate(Calendar latestDate) {
            if (latestDate != null) {
                allDates.add(latestDate);
            }
        }

        /**
         * Befülle das aktuelle {@link iPartsDataPicReference} Objekt mit allen Datumsangaben und aktualisiere den
         * Datensatz in der DB.
         */
        public void fillAndStoreReference(EtkProject project) {
            fillReference();
            // Sofort in der DB speichern (mehrere Datensätze wurden zu einem zusammengefügt)
            handleDBObjectOperation(actualReference, false, project);
        }

        /**
         * Befülle das aktuelle {@link iPartsDataPicReference} Objekt mit allen Datumsangaben
         */
        public void fillReference() {
            if ((allDates == null) || allDates.isEmpty()) {
                return;
            }
            List<Calendar> allDatesList = new ArrayList<>(allDates);
            Collections.sort(allDatesList);

            // Neuestes Datum wird das "Hauptdatum" und aktualisiert automatisch den Primärschlüssel
            actualReference.setRefDate(allDatesList.remove(allDatesList.size() - 1));
            actualReference.setPreviousDates(new TreeSet<>(allDatesList));
            // Wenn ein Bestandteil dieser zusammengesetzten Referenz ein jüngeres Datum hatte als die jüngste "DONE"
            // Referenz, dann übernehme für den zusammnegesetzten Datensatz den dazugehörigen Status
            if (highestDateOtherThanDone > highestDoneDate) {
                actualReference.setStatus(highestStateForNonDoneDate);
            }
        }

        public iPartsDataPicReference getReference() {
            return actualReference;
        }
    }

    /**
     * Fordert die übergebenen Zeichnungen über das voreingestellte DASTi-RFTS/x-Verzeichnis nach.
     *
     * @param picReferencesForRequest
     */
    public void requestNewPicturesViaFileSystem(iPartsDataPicReferenceList picReferencesForRequest) {
        requestNewPictures(picReferencesForRequest, true, iPartsPlugin.LOG_CHANNEL_DEBUG);
    }

    /**
     * Fordert die übergebenen Zeichnungen über das übergebene {@link EtkProject} in der übergebenen {@link Session} über
     * MQ oder das voreingestellte DASTi-RFTS/x-Verzeichnis nach.
     *
     * @param picReferencesForRequest
     * @param fromFileSystem
     * @param logChannel
     */
    private void requestNewPictures(final iPartsDataPicReferenceList picReferencesForRequest, final boolean fromFileSystem,
                                    final LogChannels logChannel) {
        if ((mqProject == null) || (mqSession == null)) {
            Logger.log(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, "EtkProject or session for MQPicScheduler.requestNewPictures()" +
                                                                      " is null. The method registerMQListener() must be called first.");
            return;
        }

        FrameworkThread requestThread = mqSession.startChildThread(thread -> {
            Logger.log(logChannel, LogType.DEBUG, "Starting request picture references thread...");
            String importSourceText = DWFile.convertToValidFileName(fromFileSystem ? TranslationHandler.translateForLanguage("!!DASTi-RFTSx-Verzeichnis",
                                                                                                                             iPartsConst.LOG_FILES_LANGUAGE) : "MQ");
            String logFileTitle = TranslationHandler.translateForLanguage("!!Zeichnungen nachfordern via %1", iPartsConst.LOG_FILES_LANGUAGE,
                                                                          importSourceText);
            EtkMessageLog messageLog = new EtkMessageLog();
            DWFile logFile = iPartsJobsManager.getInstance().addDefaultLogFileToMessageLog(messageLog, logFileTitle,
                                                                                           iPartsPlugin.LOG_CHANNEL_MQ);
            try {
                writeMessage(messageLog, TranslationHandler.translateForLanguage("!!Nachfordern von %1 Zeichnungen via %2:",
                                                                                 iPartsConst.LOG_FILES_LANGUAGE,
                                                                                 String.valueOf(picReferencesForRequest.size()),
                                                                                 importSourceText));

                // Aktiv-Zustand der DB-Verbindung vom (MQ) EtkProject überprüfen
                iPartsPlugin.assertProjectDbIsActive(mqProject, "RequestNewPictures", logChannel);

                if (fromFileSystem && iPartsPlugin.isImportPluginActive()) {
                    retrieveImagesFromFileSystem(mqProject, picReferencesForRequest.getAsList(), messageLog, true);
                } else {
                    retrieveImagesViaMQ(picReferencesForRequest, messageLog);
                }
            } catch (Exception e) {
                finishJobLog(logFile, false);
                Logger.log(logChannel, LogType.ERROR, "Request picture references thread cancelled with errors");
                Logger.getLogger().throwRuntimeException(e);
            }
            finishJobLog(logFile, true);
            Logger.log(logChannel, LogType.DEBUG, "Request picture references thread finished");

            // Aktualisieren, wenn das Nachfordern über das Datei-System fertig ist. Bei MQ kann man das nicht machen,
            // da hier die Kommunikation mit AS-PLM asynchron ist
            if (fromFileSystem) {
                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new DataChangedEvent(null));
            }
        });
        requestThread.setName("Picture references request thread");
    }

    /**
     * Abfragen der Bilder via MQ (AS-PLM)
     *
     * @param picReferencesForRequest
     * @param messageLog
     */
    private void retrieveImagesViaMQ(iPartsDataPicReferenceList picReferencesForRequest, EtkMessageLog messageLog) {
        boolean activateMQWorker = false;
        for (iPartsDataPicReference picReference : picReferencesForRequest) {
            if (Thread.currentThread().isInterrupted()) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "MQ request picture references thread cancelled...");
                return;
            }

            String mcItemId = picReference.getMcItemId();
            String mcItemRevId = picReference.getMcItemRevId();
            boolean hasMcItemIds = StrUtils.isValid(mcItemId) && StrUtils.isValid(mcItemRevId);
            logRequestMessage(picReference, messageLog, hasMcItemIds);
            cleanPicReference(picReference, hasMcItemIds, mqProject);
            if (hasMcItemIds) {
                sendMediaRequest(picReference, iPartsTransferNodeTypes.GET_MEDIA_CONTENTS, mqProject);
            } else if (!activateMQWorker) {
                String picRefId = picReference.getAsId().getPicReferenceNumber();
                if (StrUtils.isValid(picRefId)) {
                    activateMQWorker = true;
                }
            }
        }

        if (activateMQWorker) {
            if (activatePicWorker()) {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.DEBUG, "Activating main MQ search picture reference scheduler for requested new pictures");
            }
        }
    }

    /**
     * Schließt die Jobs-Log Datei abhängig vom übergebenen Parameter <i>success</i> ab.
     *
     * @param logFile
     * @param success
     */
    private void finishJobLog(DWFile logFile, boolean success) {
        if (logFile != null) {
            if (success) {
                iPartsJobsManager.getInstance().jobProcessed(logFile);
            } else {
                iPartsJobsManager.getInstance().jobError(logFile);
            }
        }
    }

    /**
     * Überprüft, ob für die Simulation die Varianten zur übergebenen {@link iPartsXMLMediaMessage} gespeichert werden sollen.
     *
     * @param mediaMessage
     */
    private void checkRequestSimulation(iPartsXMLMediaMessage mediaMessage) {
        if (getSimDelay() >= 0) {
            if (mediaMessage.getResponse().getRequestOperation() == iPartsTransferNodeTypes.GET_MEDIA_CONTENTS) {
                iPartsXMLMediaContainer mContainer = ((iPartsXMLResGetMediaContents)mediaMessage.getResponse().getResult()).getmContainer();
                iPartsXMLResponseSimulator.addVariants(mContainer.getMcItemId(), mContainer.getMcItemRevId(), mContainer.getMediaVariants());
            }
        }
    }

    /**
     * Schreibt die übergebene Nachricht in den übergebenen {@link EtkMessageLog}
     *
     * @param messageLog
     * @param text
     */
    private void writeMessage(EtkMessageLog messageLog, String text) {
        if (messageLog != null) {
            messageLog.fireMessage(text, MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
        }
    }

    /**
     * Logt für das übergebene {@link iPartsDataPicReference} Objekt alle objektspezifischen Informationen
     *
     * @param picReference
     * @param messageLog
     * @param hasMcItemIds
     */
    private void logRequestMessage(iPartsDataPicReference picReference, EtkMessageLog messageLog, boolean hasMcItemIds) {
        if (messageLog == null) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(TranslationHandler.translateForLanguage("!!Bildreferenznummer \"%1\";", iPartsConst.LOG_FILES_LANGUAGE,
                                                               picReference.getAsId().toString("|")));
        builder.append(" ");
        iPartsPicReferenceState previousState = picReference.getStatus();
        iPartsPicReferenceState newState;
        if (hasMcItemIds) {
            builder.append(TranslationHandler.translateForLanguage("!!Mediencontainer ID \"%1 %2\";", iPartsConst.LOG_FILES_LANGUAGE,
                                                                   picReference.getMcItemId(), picReference.getMcItemRevId()));
            builder.append(" ");
            newState = iPartsPicReferenceState.MEDIA_REQUESTED;
        } else {
            newState = iPartsPicReferenceState.NEW;
        }
        builder.append(TranslationHandler.translateForLanguage("!!alter Status \"%1\"; neuer Status \"%2\"", iPartsConst.LOG_FILES_LANGUAGE,
                                                               previousState.getDbValue(), newState.getDbValue()));
        writeMessage(messageLog, builder.toString());
    }

    /**
     * Setzt einen Zeichnungsreferenzdatensatz zurück, damit er nochmals verschickt werden kann.
     *
     * @param picReference
     * @param hasMcItemIds
     * @param project
     */
    private void cleanPicReference(iPartsDataPicReference picReference, boolean hasMcItemIds, EtkProject project) {
        picReference.setFieldValue(iPartsConst.FIELD_DPR_ERROR_CODE, "", DBActionOrigin.FROM_EDIT);
        picReference.setFieldValue(iPartsConst.FIELD_DPR_ERROR_TEXT, "", DBActionOrigin.FROM_EDIT);
        if (hasMcItemIds) {
            setFieldValues(iPartsPicReferenceState.MEDIA_REQUESTED, picReference, null, picReference.getMcItemId(), picReference.getMcItemRevId());
        } else {
            setFieldValues(iPartsPicReferenceState.NEW, picReference);
        }
        handleDBObjectOperation(picReference, false, project);
    }

    private int getSimDelay() {
        if (iPartsPlugin.isImportPluginActive()) {
            return de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.getPluginConfig()
                           .getConfigValueAsInteger(de.docware.apps.etk.plugins.customer.daimler.iparts_import.iPartsImportPlugin.CONFIG_SIM_AUTO_RESPONSE_DELAY_PIC_REF) * 1000;
        }
        return -1;
    }

}