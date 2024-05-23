/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.xml;

import de.docware.apps.etk.plugins.customer.daimler.iparts.config.MQChannelType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsColorTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferSMCAttributes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.XMLObjectCreationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.response.*;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImage;
import de.docware.framework.modules.xml.DwXmlFile;
import de.docware.util.StrUtils;
import de.docware.util.cache.ObjectInstanceStrongLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.java1_1.Java1_1_Utils;
import org.apache.commons.codec.binary.Base64;

import java.util.*;

/**
 * Hilfsklasse zum Erzeugen von erwarteten Antworten für {@link iPartsXMLMediaMessage}s für Simulationszwecke.
 */
public class iPartsXMLResponseSimulator {

    public static final String DEFAULT_PICTURE_CONTENT_DIR = "ASPLM_Simulation/PictureContent";
    public static final String DEFAULT_PICTURE_PREVIEW_DIR = "ASPLM_Simulation/PicturePreview";

    protected static final int MAX_ASPLM_PIC_SEARCH_RESULTS = 1000;

    protected static int simulationPicturePreviewIndex;
    protected static final String[] designerNames = new String[]{ "Otto", "Willi", "Hugo" };

    // Nur die letzten 100 XMLMediaVariants 1 Stunde lang merken, da es ansonsten ganz schnell zu einem OutOfMemoryError kommen kann...
    protected static ObjectInstanceStrongLRUList<String, List<iPartsXMLMediaVariant>> variantsForCorrection = new ObjectInstanceStrongLRUList<>(100, 60 * 60);

    /**
     * Schreibt den Inhalt der {@link de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage} in eine XML-Datei und/oder versendet sie nach einer Wartezeit
     * als {@link javax.jms.TextMessage} über die MQ IN-Queue als Simulation einer Antwort.
     *
     * @param xmlMessage
     * @param writeXmlFile Flag, ob die {@link de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage} in eine XML-Datei geschrieben werden soll
     * @param sendDelay    Wartezeit in Millisekunden nach der die {@link de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage} automatisch über die MQ IN-Queue als Simulation
     *                     einer Antwort versendet werden soll; bei Werten {@code < 0} wird keine Nachricht über MQ versendet
     * @return Nachrichten-ID aus der {@link de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage} bzw. {@code null} falls der Inhalt weder in eine XML-Datei
     * geschrieben noch eine Antwort als Simulation versendet wurde.
     * @throws de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQException
     */
    public static String writeAndSendSimulatedMessageResponseFromXML(final MQChannelType channelType, iPartsXMLMediaMessage xmlMessage, boolean writeXmlFile,
                                                                     final int sendDelay) {
        if (!writeXmlFile && (sendDelay < 0)) {
            return null;
        }

        final DwXmlFile xmlFile = XMLImportExportHelper.writeXMLFileFromMessageObject(xmlMessage, channelType,
                                                                                      "iPartsInExpectedResponse",
                                                                                      iPartsTransferConst.DEFAULT_XML_COMMENT,
                                                                                      writeXmlFile, true);
        final String messageId = xmlMessage.getTypeObject().getiPartsRequestID();
        if (sendDelay >= 0) {
            if (iPartsPlugin.getMqSession() != null) {
                iPartsPlugin.getMqSession().startChildThread(thread -> {
                    if (!Java1_1_Utils.sleep(sendDelay)) {
                        try {
                            MQHelper.getInstance().sendXML(channelType, xmlFile, messageId, true, true);
                        } catch (Throwable mqE) {
                            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, mqE);
                        }
                    }
                });
            } else {
                Logger.log(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, "Simulated response XML message could not be sent because session for MQ communication is null.");
                Logger.getLogger().throwRuntimeException("Simulated response XML message could not be sent because session for MQ communication is null.");
            }
        }
        return messageId;
    }

    /**
     * Erzeugt die erwartete Antwort für die übergebene Bildrecherche-XML-Nachricht.
     *
     * @param xmlMessage Bildrecherche
     */
    public static iPartsXMLMediaMessage createPicSearchResponse(iPartsXMLMediaMessage xmlMessage, boolean forPicReferences, boolean forceError) {
        Random r = new Random();
        boolean error = forceError;
        // History mit abgesendeter Nachricht füllen
        iPartsXMLHistory history = new iPartsXMLHistory();
        history.setRequest((iPartsXMLRequest)xmlMessage.getTypeObject());
        // Neuen Response bauen
        iPartsXMLResponse response = new iPartsXMLResponse(xmlMessage.getTypeObject().getiPartsRequestID(), xmlMessage.getTypeObject().getTargetClusterID(),
                                                           iPartsTransferNodeTypes.SEARCH_MEDIA_CONTAINERS,
                                                           xmlMessage.getTypeObject().getToParticipant(), xmlMessage.getTypeObject().getFromParticipant());
        iPartsXMLSearchMediaContainers smc = ((iPartsXMLSearchMediaContainers)xmlMessage.getRequest().getOperation());
        // Such- und Resultkriterien aus der Anfrage holen
        Set<iPartsTransferSMCAttributes> resultAtts = smc.getResultAttributes();
        Collection<iPartsXMLSearchCriterion> searchAtts = smc.getSearchCriteria().values();
        String picRefNumber = "";
        if (!forceError) {
            // Wenn als Suchtext "error" eingegeben wird -> Fehler als Antwort
            for (iPartsXMLSearchCriterion sC : searchAtts) {
                String value = sC.getAttributeValue();
                if (value.toLowerCase().contains("error")) {
                    error = true;
                    break;
                }
                if (forPicReferences && (sC.getAttributeName() == iPartsTransferSMCAttributes.SMC_ALTERNATE_ID)) {
                    picRefNumber = value;
                }
            }
        }
        iPartsXMLSuccess success;
        if (error) {
            success = createErrorMessage("Fehler bei der Bildsuche!", "Error while searching for image!");
        } else {
            success = new iPartsXMLSuccess(true);
            int resultsShown;
            int resultsFound = 1;
            if (!forPicReferences) {
                resultsFound = r.nextInt(MAX_ASPLM_PIC_SEARCH_RESULTS);
            }

            // Falls eine Obergrenze für die Suchanfrage gesetzt wurde
            int searchRestriction = smc.getMaxResultFromIParts();
            if ((searchRestriction > 0) && (searchRestriction < resultsFound)) {
                resultsShown = searchRestriction;
            } else {
                resultsShown = resultsFound;
            }

            // iPartsXMLResSearchMediaContainer nur bei Erfolg hinzufügen
            iPartsXMLResSearchMediaContainers rsmc = new iPartsXMLResSearchMediaContainers(resultsShown);
            if (resultsShown < resultsFound) {
                rsmc.setNumResultsFound(resultsFound);
            }
            iPartsXMLMediaContainer mContainer;

            // ResSearchMediaContainers mit den angefragten Kriterien + Ergebnissen füllen
            for (int i = 0; i < resultsShown; i++) {
                String randomMCId = "MC" + r.nextInt(1000) + "." + r.nextInt(1000) + "." + r.nextInt(1000) + "." + r.nextInt(1000);
                mContainer = new iPartsXMLMediaContainer(randomMCId, String.valueOf(r.nextInt(1000)));
                for (iPartsTransferSMCAttributes att : resultAtts) {
                    iPartsXMLSearchCriterion sC = smc.getSearchCriterionByAttributeName(att);
                    // Jedes Suchkriterium hat einen Suchstring der angepasst werden muss
                    if (sC != null) {
                        String resultValue;
                        if (forPicReferences) {
                            resultValue = picRefNumber;
                        } else {
                            resultValue = createPicSearchResultValue(sC.getAttributeValue(), i);
                        }
                        mContainer.addAttrElement(att.getAsASPLMValue(), resultValue);
                    } else {
                        mContainer.addAttrElement(att.getAsASPLMValue(), att.getAsASPLMValue() + "_" + i);
                    }
                }
                rsmc.addMediaContainer(mContainer);
            }
            response.setResult(rsmc);
        }

        response.setSuccess(success);
        iPartsXMLMediaMessage messageResponse = new iPartsXMLMediaMessage(true);
        messageResponse.setTypeObject(response);
        messageResponse.setHistory(history);
        return messageResponse;
    }

    /**
     * Baut einen Ergebnisstring aus eigentlicher Anfrage + laufende Nummer
     *
     * @param attributeValue
     * @param sNumber
     * @return
     */
    protected static String createPicSearchResultValue(String attributeValue, int sNumber) {
        String intValue = String.valueOf(sNumber);
        String suffix = "_";
        while ((suffix.length() + intValue.length()) < 5) {
            suffix = suffix + "0";
        }
        suffix = suffix + intValue;
        if (attributeValue.endsWith("*")) {
            return ("Value_" + attributeValue.substring(0, attributeValue.length() - 1)) + suffix;
        }
        return "Value_" + attributeValue + suffix;
    }

    public static iPartsXMLMediaMessage createPicContentResponse(iPartsXMLMediaMessage xmlMessage, boolean forceError,
                                                                 boolean forceWarning, boolean isBttTemplate) {
        return createPicContentResponse(xmlMessage, null, null, forceError, forceWarning, isBttTemplate);

    }

    /**
     * Erzeugt einen simulierten Response zu einem GetMediaContents Request
     *
     * @param xmlMessage
     * @param forceError
     * @return
     */
    public static iPartsXMLMediaMessage createPicContentResponse(iPartsXMLMediaMessage xmlMessage, String varItemId, String varItemRevId, boolean forceError,
                                                                 boolean forceWarning, boolean isBttTemplate) {
        Random r = new Random();
        iPartsXMLHistory history = new iPartsXMLHistory();
        history.setRequest((iPartsXMLRequest)xmlMessage.getTypeObject());
        iPartsXMLResponse response = new iPartsXMLResponse(xmlMessage.getTypeObject().getiPartsRequestID(), xmlMessage.getTypeObject().getTargetClusterID(),
                                                           iPartsTransferNodeTypes.GET_MEDIA_CONTENTS,
                                                           xmlMessage.getTypeObject().getToParticipant(), xmlMessage.getTypeObject().getFromParticipant());

        iPartsXMLSuccess success;
        if (forceError) {
            success = createErrorMessage("Das Bild mag ich nicht!", "I don't like this image!");
        } else {
            success = new iPartsXMLSuccess(true);
            iPartsXMLGetMediaContents gmp = (iPartsXMLGetMediaContents)xmlMessage.getRequest().getOperation();
            // iPartsXMLResGetMediaContents nur bei Erfolg hinzufügen
            iPartsXMLResGetMediaContents rgmc = new iPartsXMLResGetMediaContents();
            iPartsXMLMediaContainer mc = new iPartsXMLMediaContainer(gmp.getMcItemId(), gmp.getMcItemRevId());
            String mcKey = makeMCKey(gmp.getMcItemId(), gmp.getMcItemRevId());
            // Wenn zu korrigierende Bilder für diesen Auftrag vorhanden sind und es sich nicht um eine DASTI Bildanfrage handelt
            // -> PV Item- und RevIds anpassen (Revision hochzählen)
            List<iPartsXMLMediaVariant> variantsFromCorrection = variantsForCorrection.get(mcKey);
            List<iPartsXMLMediaVariant> variantsForResponse = new ArrayList<>();
            if ((variantsFromCorrection == null) || variantsFromCorrection.isEmpty()) {
                // AS-PLM erzeugt pro Antwort genau ein Bild. Hier wird per default "COLOR" als Typ gewählt
                iPartsColorTypes color = iPartsColorTypes.COLOR;
                iPartsXMLMediaVariant mv = new iPartsXMLMediaVariant();
                String variantId;
                String variantRevId;
                // Möchte man als Antwort eine spezielle VariantenId, muss diese unbedingt gesetzt werden
                if (StrUtils.isValid(varItemId, varItemRevId)) {
                    variantId = varItemId;
                    variantRevId = varItemRevId;
                } else {
                    variantId = "PV" + r.nextInt(1000) + "." + r.nextInt(1000) + "." + r.nextInt(1000) + "." + r.nextInt(1000);
                    variantRevId = String.valueOf(r.nextInt(1000));
                }
                mv.setItemId(variantId);
                mv.setPictureLanguage(""); // standardmäßig ist keine Sprache gesetzt bei Zeichnungen
                mv.setItemRevId(variantRevId);
                mv.setColorType(color);
                if (isBttTemplate) {
                    mv.setAutomationLevel(Integer.toString(r.nextInt(10)));
                    mv.setIsTemplate(r.nextBoolean());
                }
                variantsForResponse.add(mv);
            } else {
                for (iPartsXMLMediaVariant variant : variantsFromCorrection) {
                    variant.setItemRevId(increaseRevValue(variant.getItemRevId()));
                    if (isBttTemplate) {
                        variant.setAutomationLevel(Integer.toString(r.nextInt(10)));
                        variant.setIsTemplate(r.nextBoolean());
                    }
                    variantsForResponse.add(variant);
                }
            }
            fillVariantsWithContent(variantsForResponse, forceWarning, success);
            mc.addMediaVariants(variantsForResponse);
            rgmc.setmContainer(mc);
            response.setResult(rgmc);
        }

        response.setSuccess(success);
        iPartsXMLMediaMessage messageResponse = new iPartsXMLMediaMessage(true);
        messageResponse.setTypeObject(response);
        messageResponse.setHistory(history);
        return messageResponse;
    }

    /**
     * Füllt die übergebenen {@link iPartsXMLMediaVariant}s entweder mit Fehlertexten oder eigentlichen Media-Dateien
     *
     * @param mediaVariants
     * @param forceWarning
     * @param success
     */
    protected static void fillVariantsWithContent(List<iPartsXMLMediaVariant> mediaVariants, boolean forceWarning, iPartsXMLSuccess success) {

        if (forceWarning) {
            // WarningText für Deutsch
            iPartsXMLWarningText warningText = new iPartsXMLWarningText("Alle Bilder sind zu groß!");
            warningText.setTextID("4711");
            warningText.setLanguage("de");
            iPartsXMLWarning warning = new iPartsXMLWarning(warningText);

            // ErrorText für Englisch
            warningText = new iPartsXMLWarningText("All pictures are too large!");
            warningText.setTextID("4711");
            warningText.setLanguage("en");
            warning.addWarningText(warningText);
            success.addWarning(warning);
        } else {
            fillVariantsWithMedia(mediaVariants);
        }
    }

    /**
     * Füllt die übergebenen {@link iPartsXMLMediaVariant}s mit Bild- und Hotspotdateien
     *
     * @param mediaVariants
     */
    protected static synchronized void fillVariantsWithMedia(List<iPartsXMLMediaVariant> mediaVariants) {
        DWFile dir;
        try {
            dir = de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin.getPluginConfig().getConfigValueAsDWFile(de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin.CONFIG_SIM_PIC_CONTENT_DIR);
        } catch (NoClassDefFoundError e) {
            // iPartsEdit Plug-in nicht vorhanden
            dir = DWFile.get(DEFAULT_PICTURE_CONTENT_DIR);
        }
        if (!dir.isDirectory()) {
            MessageDialog.showError("Ordner mit neuen Bildern für die AS-PLM Simulation existiert nicht: " + dir.getAbsolutePath());
            return;
        }

        Set<String> fileTypeForGetMediaContents = XMLObjectCreationHelper.getInstance().getFileTypesForGetMediaContents();
        List<DWFile> files = dir.listDWFiles();
        Map<String, List<DWFile>> validFiles = new LinkedHashMap<>(); // LinkedHashMap mit allen validen Bilddateien mit gleichem Dateinamen (speziell PNG und SVG)
        Map<String, DWFile> validSenFiles = new LinkedHashMap<>(); // LinkedHashMap mit PNG Dateiname auf SEN-Datei
        for (DWFile file : files) {
            if (iPartsTransferConst.MediaFileTypes.isValidFileExtension(file.getExtension())) {
                if (iPartsTransferConst.MediaFileTypes.isHotspotFile(file.getExtension())) {
                    validSenFiles.put(file.extractFileName(false), file);
                } else if (fileTypeForGetMediaContents.contains(file.getExtension().toLowerCase())) {
                    List<DWFile> filesWithSameFileName = validFiles.computeIfAbsent(file.extractFileName(false), fileName -> new DwList<>());
                    filesWithSameFileName.add(file);
                }
            }
        }

        // SEN-Dateien ohne dazugehörige Bilddatei entfernen
        Iterator<String> senFilesIterator = validSenFiles.keySet().iterator();
        while (senFilesIterator.hasNext()) {
            String fileName = senFilesIterator.next();
            if (!validFiles.containsKey(fileName)) {
                senFilesIterator.remove();
            }
        }

        if (validFiles.isEmpty()) {
            MessageDialog.showError("Ordner mit neuen Bildern für die AS-PLM Simulation ist leer: " + dir.getAbsolutePath());
            return;
        }

        int fileIndex = 0;
        Random r = new Random();
        Calendar c = GregorianCalendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -3);
        c.add(Calendar.HOUR_OF_DAY, -3);
        List<List<DWFile>> validFilesList = new ArrayList<>(validFiles.values());
        for (iPartsXMLMediaVariant variant : mediaVariants) {
            // allgemeine Informationen
            variant.setLastModified(c.getTime());
            String designer = designerNames[r.nextInt(3)];

            // Bilddatei
            List<DWFile> pictureFiles;
            DWFile senFile = null;
            // Wenn nur eine Variante zurückgeschickt wird, dann sollte die Antwort ein Bild mit Hotspots enthalten
            // (sofern die Simulationsdateien Hotspots haben)
            if (mediaVariants.size() > 1) {
                if (fileIndex >= validFilesList.size()) {
                    fileIndex = 0;
                }
                pictureFiles = validFilesList.get(fileIndex);
                fileIndex++;
                senFile = validSenFiles.get(pictureFiles.get(0).extractFileName(false));
            } else {
                if (!validSenFiles.isEmpty()) {
                    senFile = validSenFiles.values().iterator().next();
                    pictureFiles = validFiles.get(senFile.extractFileName(false));
                } else {
                    pictureFiles = validFilesList.get(fileIndex);
                }
            }

            for (DWFile pictureFile : pictureFiles) {
                FrameworkImage image = FrameworkImage.getFromFile(pictureFile);

                String fileType;
                if (image == null) {
                    image = FrameworkImage.getInvalid16x16Image();
                    fileType = MimeTypes.EXTENSION_PNG;
                } else {
                    fileType = DWFile.extractExtension(image.getPath(), false);
                }

                iPartsXMLMedia mediaPicture = new iPartsXMLMedia();
                mediaPicture.setDesigner(designer);
                mediaPicture.setLastModified(c.getTime());
                if (!image.getPath().toLowerCase().contains("error")) {
                    String imageAsString = XMLImportExportHelper.convertFrameworkImageToBase64String(image);
                    iPartsXMLMediaBinaryFile binaryFile = new iPartsXMLMediaBinaryFile(fileType, imageAsString);
                    binaryFile.setImage(image);
                    mediaPicture.addBinaryFile(binaryFile);
                }
                variant.addMedia(mediaPicture);
            }

            // Falls zur Bilddatei eine SEN-Datei existiert -> Füge die SEN-Datei als iPartsXMLMedia Element hinzu
            if (senFile != null) {
                iPartsXMLMedia mediaHotspot = new iPartsXMLMedia();
                mediaHotspot.setDesigner(designer);
                mediaHotspot.setLastModified(c.getTime());

                String fileType = DWFile.extractExtension(senFile.getPath(), false);
                String senFileAsString = Base64.encodeBase64String(senFile.readByteArray());

                iPartsXMLMediaBinaryFile binaryFile = new iPartsXMLMediaBinaryFile(fileType, senFileAsString);
                mediaHotspot.addBinaryFile(binaryFile);
                variant.addMedia(mediaHotspot);
            }
        }
    }

    /**
     * Erzeugt die erwartete Antwort für die übergebene Stornierungsanfrage-XML-Nachricht.
     *
     * @param xmlMessage
     * @param simError
     * @return
     */
    public static iPartsXMLMediaMessage createAbortMediaOrderResponse(iPartsXMLMediaMessage xmlMessage, boolean simError) {
        // History aus eigentlicher Anfrage bauen
        iPartsXMLHistory history = new iPartsXMLHistory();
        history.setRequest(xmlMessage.getRequest());
        // Response aus Anfrage erstellen
        iPartsXMLResponse response = new iPartsXMLResponse(xmlMessage.getTypeObject().getiPartsRequestID(), xmlMessage.getTypeObject().getTargetClusterID(),
                                                           iPartsTransferNodeTypes.ABORT_MEDIA_ORDER,
                                                           xmlMessage.getTypeObject().getToParticipant(), xmlMessage.getTypeObject().getFromParticipant());
        iPartsXMLSuccess success;
        if (simError) {
            success = createErrorMessage("Stornierung nicht möglich", "Could not abort Media Order");
        } else {
            success = new iPartsXMLSuccess(true);
        }
        response.setSuccess(success);
        iPartsXMLMediaMessage messageResponse = new iPartsXMLMediaMessage(true);
        messageResponse.setTypeObject(response);
        messageResponse.setHistory(history);
        return messageResponse;


    }

    /**
     * Erzeugt ein {@link iPartsXMLSuccess} Objekt mit der übergebenen Fehlermeldung
     *
     * @param germanText
     * @param englishText
     * @return
     */
    private static iPartsXMLSuccess createErrorMessage(String germanText, String englishText) {
        iPartsXMLSuccess success = new iPartsXMLSuccess(false);
        success.setErrorCode(23);

        // ErrorText für Deutsch
        iPartsXMLErrorText errorText = new iPartsXMLErrorText(germanText);
        errorText.setTextID("0815");
        errorText.setLanguage("de");
        success.addError(errorText);

        // ErrorText für Englisch
        errorText = new iPartsXMLErrorText(englishText);
        errorText.setTextID("0815");
        errorText.setLanguage("en");
        success.addError(errorText);
        return success;
    }


    /**
     * Erzeugt die erwartete Antwort für die übergebene Bildvorschau-XML-Nachricht.
     *
     * @param xmlMessage Bildvorschau
     * @param forceError Flag, um unabhängig von den Dateinamen der Simulationsbilder eine Antwort mit Fehler zu erzeugen
     */
    public static iPartsXMLMediaMessage createPicPreviewResponse(iPartsXMLMediaMessage xmlMessage, boolean forceError) {
        // History aus eigentlicher Anfrage bauen
        iPartsXMLHistory history = new iPartsXMLHistory();
        history.setRequest(xmlMessage.getRequest());
        // Response aus Anfrage erstellen
        iPartsXMLResponse response = new iPartsXMLResponse(xmlMessage.getTypeObject().getiPartsRequestID(), xmlMessage.getTypeObject().getTargetClusterID(),
                                                           iPartsTransferNodeTypes.GET_MEDIA_PREVIEW,
                                                           xmlMessage.getTypeObject().getToParticipant(), xmlMessage.getTypeObject().getFromParticipant());

        FrameworkImage previewImage = getSimulationPicturePreview();

        // Wenn im Bilddateinamen "error" enthalten ist -> Fehler als Antwort
        forceError |= previewImage.getPath().toLowerCase().contains("error");

        iPartsXMLSuccess success;
        if (forceError) {
            success = createErrorMessage("Das Vorschaubild mag ich nicht!", "I don't like this preview image!");
        } else {
            success = new iPartsXMLSuccess(true);
            iPartsXMLGetMediaPreview gmp = (iPartsXMLGetMediaPreview)xmlMessage.getRequest().getOperation();
            // iPartsXMLResGetMediaPreview nur bei Erfolg hinzufügen
            iPartsXMLResGetMediaPreview rgmp = new iPartsXMLResGetMediaPreview(gmp.getMcItemId(), gmp.getMcItemRevId());
            String fileType = DWFile.extractExtension(previewImage.getPath(), false);
            String imageAsString = XMLImportExportHelper.convertFrameworkImageToBase64String(previewImage);
            iPartsXMLMediaBinaryFile binaryFile = new iPartsXMLMediaBinaryFile(fileType, imageAsString);
            binaryFile.setImage(previewImage);
            rgmp.setBinaryFile(binaryFile);
            response.setResult(rgmp);
        }

        response.setSuccess(success);
        iPartsXMLMediaMessage messageResponse = new iPartsXMLMediaMessage(true);
        messageResponse.setTypeObject(response);
        messageResponse.setHistory(history);
        return messageResponse;
    }

    /**
     * Initialisiert die Vorschaubilder für die Simulation
     */
    public static synchronized FrameworkImage getSimulationPicturePreview() {
        DWFile dir;
        try {
            dir = de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin.getPluginConfig().getConfigValueAsDWFile(de.docware.apps.etk.plugins.customer.daimler.iparts_edit.iPartsEditPlugin.CONFIG_SIM_PIC_PREVIEW_DIR);
        } catch (NoClassDefFoundError e) {
            // iPartsEdit Plug-in nicht vorhanden
            dir = DWFile.get(DEFAULT_PICTURE_PREVIEW_DIR);
        }
        if (!dir.isDirectory()) {
            Logger.getLogger().throwRuntimeException("Location for simulated preview images must be an existing directory. Current directory: "
                                                     + dir.getAbsolutePath());
        }

        List<DWFile> files = dir.listDWFiles();
        List<DWFile> validFiles = new ArrayList<>();
        for (DWFile file : files) {
            if (iPartsTransferConst.MediaFileTypes.isValidPreviewExtension(file.getExtension())) {
                validFiles.add(file);
            }
        }

        if (validFiles.isEmpty()) {
            Logger.getLogger().throwRuntimeException("Directory for simulated preview images must contain at least one valid image file. Current directory: "
                                                     + dir.getAbsolutePath());
        }
        if (simulationPicturePreviewIndex >= validFiles.size()) {
            simulationPicturePreviewIndex = 0;
        }

        DWFile simulationPicturePreviewFile = validFiles.get(simulationPicturePreviewIndex);
        simulationPicturePreviewIndex++;
        return FrameworkImage.getFromFile(simulationPicturePreviewFile);
    }

    protected static String makeMCKey(String mcItemId, String mcItemRevId) {
        return mcItemId + "||" + mcItemRevId;
    }

    protected static String increaseRevValue(String itemRevId) {
        int mcRevInt = StrUtils.strToIntDef(itemRevId, -1) + 1;
        return String.valueOf(mcRevInt);
    }

    /**
     * Fügt von außen MediaVarianten zu bestimmten MediaContainer hinzu
     *
     * @param mcItemId
     * @param mcItemRevId
     * @param mediaVariants
     */
    public static void addVariants(String mcItemId, String mcItemRevId, List<iPartsXMLMediaVariant> mediaVariants) {
        String mcKey = makeMCKey(mcItemId, mcItemRevId);
        variantsForCorrection.put(mcKey, mediaVariants);
    }
}