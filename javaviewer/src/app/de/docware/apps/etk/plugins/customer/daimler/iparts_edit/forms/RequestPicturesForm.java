/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImageList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReference;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataPicReferenceList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.ImportExportLogHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQException;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.MQHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.XMLImportExportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsASPLMItemId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.config.iPartsTransferNodeTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsMQMessageManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.iPartsXMLResponseSimulator;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.XMLObjectCreationHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.iPartsXMLMediaMessage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLGetMediaContents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.xml.model.media.request.iPartsXMLRequestor;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderPicture;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderPicturesList;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.table.GuiTableHeader;
import de.docware.framework.modules.gui.controls.table.GuiTableRow;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.StrUtils;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Dialog zum Nachfordern von Bildtafeln via MQ (nur für den Regelprozess)
 */
public class RequestPicturesForm extends AbstractJavaViewerForm implements iPartsConst {

    enum RequestDataType {
        PRODUCT("!!ausgewähltes Produkt", "!!ausgewählte Produkte"),
        SA("!!ausgewählte SA", "!!ausgewählte SAs"),
        UNKNOWN("", "");

        private final String singleText;
        private final String pluralText;

        RequestDataType(String singleText, String pluralText) {
            this.singleText = singleText;
            this.pluralText = pluralText;
        }

        public String getSingleText() {
            return singleText;
        }

        public String getPluralText() {
            return pluralText;
        }
    }

    /**
     * Zeigt den Dialog für das Nachfordern von Bildtafeln via MQ mit den Optionen für alle bzw. nur fehlerhafte und fehlende
     * Bildtafeln für die übergebenen Produkte.
     *
     * @param dataConnector
     * @param parentForm
     * @param productIds
     * @return
     */
    public static boolean showRequestOptionsForProducts(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                        List<iPartsProductId> productIds) {
        RequestPicturesForm requestPicturesForm = new RequestPicturesForm(dataConnector, parentForm, RequestDataType.PRODUCT);
        requestPicturesForm.initProductData(productIds);
        return requestPicturesForm.showModal() == ModalResult.OK;
    }

    public static boolean showRequestOptionsForSAs(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                   List<iPartsSaId> saIds) {
        RequestPicturesForm requestPicturesForm = new RequestPicturesForm(dataConnector, parentForm, RequestDataType.SA);
        requestPicturesForm.initSaData(saIds);
        return requestPicturesForm.showModal() == ModalResult.OK;
    }

    private final RequestDataType currentRequestType;
    private List<iPartsProductId> productIds;
    private List<iPartsSaId> saIds;
    private Map<String, Set<PicReferenceData>> foundImages;

    /**
     * Erzeugt eine Instanz von RequestPicturesForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     */
    public RequestPicturesForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                               RequestDataType requestDataType) {
        super(dataConnector, parentForm);
        this.currentRequestType = requestDataType;
        $$internalCreateGui$$(null);
        postCreateGui();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        foundImages = new TreeMap<>();
        GuiTableHeader header = new GuiTableHeader();
        header.addChild(TranslationHandler.translate("!!Produkt"));
        header.addChild(TranslationHandler.translate("!!Bildtafeln"));
        mainWindow.tableProductInfos.setHeader(header);
        mainWindow.buttonPanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).setEnabled(MQHelper.checkTransmissionToASPLMConfigWithMessage());
    }

    /**
     * Initialisiert die Anzeige für die übergebenen Produkte
     *
     * @param productIds
     */
    private void initProductData(List<iPartsProductId> productIds) {
        if (productIds != null) {
            this.productIds = productIds;
            fillPictures();
        }
    }

    /**
     * Initialisiert die Anzeige für die übergebenen SAs
     *
     * @param saIds
     */
    private void initSaData(List<iPartsSaId> saIds) {
        if (saIds != null) {
            this.saIds = saIds;
            fillPictures();
        }
    }

    public ModalResult showModal() {
        mainWindow.pack();
        return mainWindow.showModal();
    }

    /**
     * Fordert die ausgewählten Zeichnungen nach
     *
     * @param event
     */
    private void requestNewPictures(Event event) {
        // Da die Anfrage auch nach einer geschlossenen Session weiter verschickt werden sollen, muss hier das MQ Project
        // und die MQ Session genutzt werden
        iPartsPlugin.getMqSession().startChildThread(thread -> {
            EtkProject mqProject = iPartsPlugin.getMqProject();
            // Log Datei für das Abschicken aller Anfragen
            DWFile logFile = iPartsJobsManager.getInstance().exportJobRunning("PRODUCT_PICTURES_REQUEST");
            iPartsXMLRequestor requestor = new iPartsXMLRequestor();
            // Ein LogHelper, damit wir die Standardmethoden nicht nachprogrammieren müssen
            ImportExportLogHelper logHelper = new ImportExportLogHelper(logFile);
            // Anzahl Produkte
            int size = foundImages.keySet().size();
            // Nachfordern via SA oder Produkt
            String type = (size > 1) ? currentRequestType.getPluralText() : currentRequestType.getSingleText();
            type = logHelper.translateForLog(type);
            logHelper.addLogMsgWithTranslation("!!Starte AS-PLM Bildtafel Anfrage für %1 %2",
                                               String.valueOf(size), type);
            logHelper.fireLineSeparator();
            foundImages.forEach((key, value) -> {
                if (thread.wasCanceled()) {
                    return;
                }
                int picCount = 0;
                int maxSize = value.size();
                if (maxSize == 0) {
                    logHelper.addLogMsgWithTranslation("!!Für %1 wurden keine Bildtafeln gefunden. %1 wird " +
                                                       "übersprungen!", key);
                } else {
                    logHelper.addLogMsgWithTranslation("!!Frage %1 Bildtafeln für \"%2\" an...",
                                                       String.valueOf(maxSize), key);
                    int lastLogFilePercentage = 0;
                    for (PicReferenceData picture : value) {
                        if (thread.wasCanceled()) {
                            return;
                        }
                        picCount++;
                        // Nur Bildtafeln betrachten, die auch wirklich AS-PLM Bildtafeln sind
                        if (XMLImportExportHelper.isASPLMPictureNumber(picture.getPictureId())) {
                            // Für die GetMediaContents Anfrage benötigen wir die MC Nummer und Revision des Bildes
                            iPartsASPLMItemId mcInfo = null;
                            iPartsDataPicOrderPicturesList picturesList = loadPicOrdersForPictureNumber(mqProject, picture);
                            // Wurde kein Bildauftrag für die PV Nummer gefunden, muss geprüft werden, ob die PV vielleicht
                            // über die Migration oder eine Nachforderung hereingekommen ist (DA_PIC_REFERENCE)
                            if (picturesList.isEmpty()) {
                                iPartsDataPicReferenceList picReferenceList = loadPicReferencesForPictureNumber(mqProject, picture);
                                if (picReferenceList.isEmpty()) {
                                    // Wir haben die PV Nummer weder in den Bildaufträgen noch in den Bildreferenzen gefunden
                                    // -> Keine Möglichkeit irgendwie an eine MC Nummer zu kommen
                                    logHelper.addLogMsgWithTranslation("!!Zur Bildtafel \"%1\" und Revision \"%2\" " +
                                                                       "konnte keine Media-Container Nummer gefunden werden",
                                                                       picture.getPictureId(), picture.getPictureRevId());
                                } else {
                                    // Wir haben einen Treffer in den Bildreferenzen. MC Nummer und Revision bestimmen
                                    iPartsDataPicReference foundMcReference = picReferenceList.getLast();
                                    mcInfo = new iPartsASPLMItemId(foundMcReference.getFieldValue(FIELD_DPR_MC_ID),
                                                                   foundMcReference.getFieldValue(FIELD_DPR_MC_REV_ID));
                                }
                            } else {
                                // Wir haben Bildaufträge zu der PV Nummer gefunden. Nimm den neuesten Bildauftrag
                                iPartsDataPicOrderPicture foundMcReference = picturesList.getLast();
                                mcInfo = new iPartsASPLMItemId(foundMcReference.getFieldValue(FIELD_DA_PO_ORDER_ID_EXTERN),
                                                               foundMcReference.getFieldValue(FIELD_DA_PO_ORDER_REVISION_EXTERN));
                            }
                            // Nur wenn wir eine MC Nummer und eine Revision haben, können wir eine Anfrage abschicken
                            requestNewPictureForMCData(mcInfo, picture.getPictureId(), picture.getPictureRevId(), requestor);
                        }
                        lastLogFilePercentage = printProgress(logHelper, picCount, maxSize, lastLogFilePercentage);
                    }
                    logHelper.addLogMsgWithTranslation("!!Bildtafel-Anfrage für \"%1\" beendet", key);
                }
            });
            logHelper.addNewLine();
            logHelper.fireLineSeparator();
            logHelper.addNewLine();
            // Abschließende Log-Einträge schreiben
            finishSendingRequests(thread, logHelper, logFile, size, type);
        });
        close();

    }

    /**
     * Fordert das Bild für die übergebene MC Nummer nach
     *
     * @param mcInfo
     * @param pictureId
     * @param pictureRevId
     * @param requestor
     */
    public static void requestNewPictureForMCData(iPartsASPLMItemId mcInfo, String pictureId, String pictureRevId,
                                                  iPartsXMLRequestor requestor) {
        if ((mcInfo != null) && StrUtils.isValid(mcInfo.getMcItemId(), mcInfo.getMcItemRevId())) {
            iPartsXMLMediaMessage xmlMediaMessage = createGetMediaContentsMessage(mcInfo, requestor);
            sendGetMediaContentMessage(xmlMediaMessage, pictureId, pictureRevId);
        }
    }

    /**
     * Erzeugt eine {@link iPartsXMLMediaMessage} mit der GetMediaContents Operation aus den übergebenen Parameter
     *
     * @param mcInfo
     * @param requestor
     * @return
     */
    private static iPartsXMLMediaMessage createGetMediaContentsMessage(iPartsASPLMItemId mcInfo, iPartsXMLRequestor requestor) {
        iPartsXMLGetMediaContents operation = new iPartsXMLGetMediaContents(mcInfo.getMcItemId(), mcInfo.getMcItemRevId());
        String messageId = XMLImportExportHelper.makePicRequestGUIDForMediaContent(StrUtils.makeGUID());
        return XMLObjectCreationHelper.getInstance().createDefaultGetPicMediaXMLMessage(operation, requestor, messageId, iPartsTransferNodeTypes.GET_MEDIA_CONTENTS);
    }

    /**
     * Schließ das Anfragen der Bilder bei AS-PLM ab. Finale Log-Einträge werden geschrieben und die Log-Datei verschoben
     *
     * @param thread
     * @param logHelper
     * @param logFile
     * @param size
     * @param type
     */
    private void finishSendingRequests(FrameworkThread thread, ImportExportLogHelper logHelper, DWFile logFile, int size, String type) {
        if (thread.wasCanceled()) {
            logHelper.addLogWarningWithTranslation("!!AS-PLM Bildtafel Anfrage wurde nicht komplett abgearbeitet. " +
                                                   "Bitte Logdatei der Anwendung prüfen!");
            logHelper.addNewLine();
            logHelper.fireLineSeparator();
            iPartsJobsManager.getInstance().jobError(logFile);
        } else {
            logHelper.addLogMsgWithTranslation("!!AS-PLM Bildtafel Anfrage für %1 %2 erfolgreich beendet",
                                               String.valueOf(size), type);
            logHelper.addNewLine();
            logHelper.fireLineSeparator();
            iPartsJobsManager.getInstance().jobProcessed(logFile);
        }
    }

    /**
     * Lädt alle Bildreferenzen zu übergebenen PV Bildtafel
     *
     * @param project
     * @param picture
     * @return
     */
    private iPartsDataPicReferenceList loadPicReferencesForPictureNumber(EtkProject project, PicReferenceData picture) {
        iPartsDataPicReferenceList picReferenceList = new iPartsDataPicReferenceList();
        picReferenceList.searchSortAndFill(project, TABLE_DA_PIC_REFERENCE,
                                           new String[]{ FIELD_DPR_MC_ID, FIELD_DPR_MC_REV_ID },
                                           new String[]{ FIELD_DPR_VAR_ID, FIELD_DPR_VAR_REV_ID },
                                           new String[]{ picture.getPictureId(), picture.getPictureRevId() },
                                           null, null,
                                           new String[]{ FIELD_DPR_MC_ID, FIELD_DPR_MC_REV_ID },
                                           DBDataObjectList.LoadType.ONLY_IDS, true,
                                           DBActionOrigin.FROM_DB);
        return picReferenceList;
    }

    /**
     * Lädt alle Bildaufträge zur übergebenen PV Bildtafel
     *
     * @param project
     * @param picture
     * @return
     */
    private iPartsDataPicOrderPicturesList loadPicOrdersForPictureNumber(EtkProject project, PicReferenceData picture) {

        iPartsDataPicOrderPicturesList picturesList = new iPartsDataPicOrderPicturesList();
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_PICORDER, FIELD_DA_PO_ORDER_ID_EXTERN,
                                                 false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_DA_PICORDER, FIELD_DA_PO_ORDER_REVISION_EXTERN,
                                                 false, false));
        picturesList.searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields,
                                               new String[]{ TableAndFieldName.make(TABLE_DA_PICORDER_PICTURES,
                                                                                    FIELD_DA_POP_PIC_ITEMID),
                                                             TableAndFieldName.make(TABLE_DA_PICORDER_PICTURES,
                                                                                    FIELD_DA_POP_PIC_ITEMREVID) },
                                               new String[]{ picture.getPictureId(), picture.getPictureRevId() },
                                               false,
                                               new String[]{ TableAndFieldName.make(TABLE_DA_PICORDER,
                                                                                    FIELD_DA_PO_ORDER_ID_EXTERN),
                                                             TableAndFieldName.make(TABLE_DA_PICORDER,
                                                                                    FIELD_DA_PO_ORDER_REVISION_EXTERN) },
                                               false, false,
                                               true, null,
                                               new EtkDataObjectList.JoinData(TABLE_DA_PICORDER,
                                                                              new String[]{ FIELD_DA_POP_ORDER_GUID },
                                                                              new String[]{ FIELD_DA_PO_ORDER_GUID },
                                                                              false, false));
        return picturesList;
    }

    /**
     * Versendet die übergebene {@link iPartsXMLMediaMessage} Nachricht
     *
     * @param xmlMessage
     * @param pictureId
     * @param pictureRevId
     */
    private static void sendGetMediaContentMessage(iPartsXMLMediaMessage xmlMessage, String pictureId, String pictureRevId) {
        if (xmlMessage == null) {
            return;
        }
        int simDelay = iPartsPlugin.getSimAutoResponseDelayForSession(iPartsPlugin.SIM_AUTO_RESPONSE_DELAY) * 1000;
        boolean doSimulation = (simDelay >= 0);

        try {
            iPartsMQMessageManager.getInstance(iPartsPlugin.XML_MESSAGE_MANAGER_NAME_MEDIA).sendXMLMessageWithMQ(iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA, xmlMessage,
                                                                                                                 doSimulation);

        } catch (MQException e) {
            Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_MQ, LogType.ERROR, e);
            return;
        }
        if (doSimulation) {
            // Antwort erzeugen und schicken
            iPartsXMLMediaMessage expectedResponseXmlMessage
                    = iPartsXMLResponseSimulator.createPicContentResponse(xmlMessage, pictureId, pictureRevId,
                                                                          false, false, false);
            iPartsXMLResponseSimulator.writeAndSendSimulatedMessageResponseFromXML(iPartsPlugin.MQ_CHANNEL_TYPE_MEDIA,
                                                                                   expectedResponseXmlMessage,
                                                                                   true,
                                                                                   simDelay);
        }

    }


    /**
     * Schreibt den aktuellen Zustand der Nachforderung in die Log-Datei
     *
     * @param logHelper
     * @param picCount
     * @param maxSize
     * @param lastLogFilePercentage
     * @return
     */
    private int printProgress(ImportExportLogHelper logHelper, int picCount, int maxSize, int lastLogFilePercentage) {
        int percentage = (picCount * 100) / maxSize;
        if (((percentage % 25) == 0) && (percentage != lastLogFilePercentage)) { // nur alle 25% loggen
            lastLogFilePercentage = percentage;
            logHelper.addLogMsgWithTranslation("!!%1/%2 Bildtafeln angefragt", String.valueOf(picCount), String.valueOf(maxSize));
        }
        return lastLogFilePercentage;
    }


    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    /**
     * Befüllt die entsprechende {@link iPartsDataPicReferenceList} mit den Referenzen zu den Bildtafeln, die nachgefordert
     * werden sollen, und aktualisiert die GUI.
     */
    private void fillPictures() {
        // Suche der Bildtafelreferenzen in einem Thread durchführen, da dies für mehrere Produkte auch mal länger
        // dauern kann
        EtkMessageLogForm logForm = new EtkMessageLogForm("!!Bildtafeln nachfordern", "!!Suche Bildtafeln...", null, true);
        logForm.disableButtons(true);
        logForm.showModal(thread -> {
            int allPictures = 0;
            switch (currentRequestType) {
                case PRODUCT:
                    allPictures = fillPicturesFromProducts();
                    break;
                case SA:
                    allPictures = fillPicturesFromSas();
                    break;
            }
            setRequestInfos(allPictures);
        });
    }

    /**
     * Setzt die Infos zu den ausgewählten Produkten/SAs inkl. der Anzahl aller betroffenen Bildtafeln
     *
     * @param allPicturesSize
     */
    private void setRequestInfos(int allPicturesSize) {
        Dimension screenSize = FrameworkUtils.getScreenSize();
        double finalPreferredHeight = Math.min(mainWindow.tableProductInfos.getPreferredFrameHeight(), screenSize.getHeight() * 0.6);
        Session.invokeThreadSafeInSession(() -> {
            foundImages.forEach((key, value) -> {
                GuiTableRow row = new GuiTableRow();
                row.addChild(key);
                row.addChild(String.valueOf(value.size()));
                mainWindow.tableProductInfos.addRow(row);
            });
            // Die Höhe in Abhängigkeit der Anzahl der ausgewählten Produkte setzen
            mainWindow.panelInfo.setMinimumHeight((int)(mainWindow.panelInfo.getPreferredFrameHeight() + finalPreferredHeight));
            mainWindow.panelInfo.setMinimumWidth((mainWindow.panelInfo.getPreferredFrameWidth() + mainWindow.tableProductInfos.getPreferredFrameWidth()));
            mainWindow.scrollpaneProductInfos.setMinimumHeight((int)(mainWindow.scrollpaneProductInfos.getPreferredHeight() + finalPreferredHeight));
            mainWindow.scrollpaneProductInfos.setMinimumWidth(mainWindow.scrollpaneProductInfos.getPreferredFrameWidth() + mainWindow.tableProductInfos.getPreferredFrameWidth());
            mainWindow.pack();

            // Gesamtanzahl Bildtafeln setzen und den OK Button (de-)aktivieren
            mainWindow.labelAllResult.setText(String.valueOf(allPicturesSize));
            mainWindow.buttonPanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).setEnabled(allPicturesSize > 0);
        });
    }

    /**
     * Durchläuft alle SAs und bestimmt die Bilder zu allen SAs
     *
     * @return
     */
    private int fillPicturesFromSas() {
        int allPictures = 0;
        for (iPartsSaId saId : saIds) {
            Set<PicReferenceData> pictures = getPicturesForSa(saId, getProject());
            allPictures += pictures.size();
            foundImages.put(saId.getSaNumber(), pictures);
        }
        return allPictures;
    }

    /**
     * Durchläuft alle Produkte und bestimmt die Bilder zu allen Produkten
     *
     * @return
     */
    private int fillPicturesFromProducts() {
        int allPictures = 0;
        for (iPartsProductId productId : productIds) {
            Set<PicReferenceData> pictures = getPicturesForProduct(productId, getProject());
            allPictures += pictures.size();
            foundImages.put(productId.getProductNumber(), pictures);
        }
        return allPictures;
    }

    /**
     * Liefert alle PV Bildtafeln zu dem übergebenen Produkt
     *
     * @param productId
     * @param project
     * @return
     */
    public Set<PicReferenceData> getPicturesForProduct(iPartsProductId productId, EtkProject project) {
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_PRODUCT_MODULES,
                                                                            FIELD_DPM_PRODUCT_NO) };
        String[] whereValues = new String[]{ productId.getProductNumber() };
        return getPicReferences(project, whereTableAndFields, whereValues,
                                TABLE_DA_PRODUCT_MODULES,
                                FIELD_DPM_MODULE_NO);
    }

    /**
     * Liefert alle PV Bildtafeln zu der übergebenen SA
     *
     * @param saId
     * @param project
     * @return
     */
    public Set<PicReferenceData> getPicturesForSa(iPartsSaId saId, EtkProject project) {
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_SA_MODULES,
                                                                            FIELD_DSM_SA_NO) };
        String[] whereValues = new String[]{ saId.getSaNumber() };
        return getPicReferences(project, whereTableAndFields, whereValues,
                                TABLE_DA_SA_MODULES,
                                FIELD_DSM_MODULE_NO);
    }

    /**
     * Sucht in der Datenbank nach Zeichnungsreferenzen zu den übergebenen Parameter
     *
     * @param project
     * @return
     */
    private Set<PicReferenceData> getPicReferences(EtkProject project, String[] whereTableAndFields, String[] whereValues,
                                                   String joinTableName, String joinFieldName) {
        Set<PicReferenceData> result = new HashSet<>();
        EtkDataImageList imageList = EtkDataObjectFactory.createDataImageList();
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(EtkDbConst.TABLE_IMAGES, EtkDbConst.FIELD_I_IMAGES, false, false));
        selectFields.addFeld(new EtkDisplayField(EtkDbConst.TABLE_IMAGES, EtkDbConst.FIELD_I_PVER, false, false));
        String[] completeWhereTableAndFields = StrUtils.mergeArrays(whereTableAndFields, TableAndFieldName.make(TABLE_IMAGES, FIELD_I_IMAGES));
        String[] completeWhereValues = StrUtils.mergeArrays(whereValues, "PV*");
        imageList.searchSortAndFillWithJoin(project, project.getDBLanguage(), selectFields,
                                            completeWhereTableAndFields, completeWhereValues,
                                            false, null, false,
                                            true, null,
                                            new EtkDataObjectList.JoinData(joinTableName,
                                                                           new String[]{ EtkDbConst.FIELD_I_TIFFNAME },
                                                                           new String[]{ joinFieldName },
                                                                           false,
                                                                           false));

        for (EtkDataImage dataImage : imageList) {
            result.add(new PicReferenceData(dataImage));
        }
        return result;
    }

    /**
     * Klasse zum Verwalten der PV Bildtafeln zu einem Produkt / einer SA
     */
    private static class PicReferenceData {

        private final EtkDataImage image;

        public PicReferenceData(EtkDataImage dataImage) {
            this.image = dataImage;
        }

        public String getPictureId() {
            return image.getFieldValue(EtkDbConst.FIELD_I_IMAGES);
        }

        public String getPictureRevId() {
            return image.getFieldValue(EtkDbConst.FIELD_I_PVER);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if ((o == null) || (getClass() != o.getClass())) {
                return false;
            }
            PicReferenceData that = (PicReferenceData)o;
            return Objects.equals(getPictureId(), that.getPictureId()) && Objects.equals(getPictureRevId(), that.getPictureRevId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getPictureId(), getPictureRevId());
        }
    }


    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle titleMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelInfo;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiScrollPane scrollpaneProductInfos;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.table.GuiTable tableProductInfos;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelAll;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelAllResult;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonPanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            titleMain = new de.docware.framework.modules.gui.controls.GuiTitle();
            titleMain.setName("titleMain");
            titleMain.__internal_setGenerationDpi(96);
            titleMain.registerTranslationHandler(translationHandler);
            titleMain.setScaleForResolution(true);
            titleMain.setMinimumWidth(10);
            titleMain.setMinimumHeight(50);
            titleMain.setTitle("!!Zeichnungen nachfordern");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleMainConstraints.setPosition("north");
            titleMain.setConstraints(titleMainConstraints);
            this.addChild(titleMain);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMain.setLayout(panelMainLayout);
            panelInfo = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelInfo.setName("panelInfo");
            panelInfo.__internal_setGenerationDpi(96);
            panelInfo.registerTranslationHandler(translationHandler);
            panelInfo.setScaleForResolution(true);
            panelInfo.setMinimumWidth(10);
            panelInfo.setMinimumHeight(10);
            panelInfo.setTitle("!!Informationen");
            de.docware.framework.modules.gui.layout.LayoutGridBag panelInfoLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelInfo.setLayout(panelInfoLayout);
            scrollpaneProductInfos = new de.docware.framework.modules.gui.controls.GuiScrollPane();
            scrollpaneProductInfos.setName("scrollpaneProductInfos");
            scrollpaneProductInfos.__internal_setGenerationDpi(96);
            scrollpaneProductInfos.registerTranslationHandler(translationHandler);
            scrollpaneProductInfos.setScaleForResolution(true);
            scrollpaneProductInfos.setMinimumWidth(100);
            scrollpaneProductInfos.setMinimumHeight(100);
            tableProductInfos = new de.docware.framework.modules.gui.controls.table.GuiTable();
            tableProductInfos.setName("tableProductInfos");
            tableProductInfos.__internal_setGenerationDpi(96);
            tableProductInfos.registerTranslationHandler(translationHandler);
            tableProductInfos.setScaleForResolution(true);
            tableProductInfos.setMinimumWidth(100);
            tableProductInfos.setMinimumHeight(100);
            tableProductInfos.setSelectionMode(de.docware.framework.modules.gui.controls.table.TableSelectionMode.SELECTION_MODE_NONE);
            tableProductInfos.setHtmlTablePageSplitMode(de.docware.framework.modules.gui.controls.table.HtmlTablePageSplitMode.NO_SPLIT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder tableProductInfosConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            tableProductInfos.setConstraints(tableProductInfosConstraints);
            scrollpaneProductInfos.addChild(tableProductInfos);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag scrollpaneProductInfosConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 2, 1, 100.0, 0.0, "c", "h", 4, 8, 4, 8);
            scrollpaneProductInfos.setConstraints(scrollpaneProductInfosConstraints);
            panelInfo.addChild(scrollpaneProductInfos);
            labelAll = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelAll.setName("labelAll");
            labelAll.__internal_setGenerationDpi(96);
            labelAll.registerTranslationHandler(translationHandler);
            labelAll.setScaleForResolution(true);
            labelAll.setMinimumWidth(10);
            labelAll.setMinimumHeight(10);
            labelAll.setText("!!Gesamtanzahl Bildtafeln für Anfrage:");
            labelAll.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelAllConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "w", "n", 4, 8, 8, 4);
            labelAll.setConstraints(labelAllConstraints);
            panelInfo.addChild(labelAll);
            labelAllResult = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelAllResult.setName("labelAllResult");
            labelAllResult.__internal_setGenerationDpi(96);
            labelAllResult.registerTranslationHandler(translationHandler);
            labelAllResult.setScaleForResolution(true);
            labelAllResult.setMinimumWidth(10);
            labelAllResult.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelAllResultConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 0.0, 0.0, "w", "h", 4, 4, 8, 8);
            labelAllResult.setConstraints(labelAllResultConstraints);
            panelInfo.addChild(labelAllResult);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag panelInfoConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "w", "h", 8, 8, 8, 8);
            panelInfo.setConstraints(panelInfoConstraints);
            panelMain.addChild(panelInfo);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonPanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonPanel.setName("buttonPanel");
            buttonPanel.__internal_setGenerationDpi(96);
            buttonPanel.registerTranslationHandler(translationHandler);
            buttonPanel.setScaleForResolution(true);
            buttonPanel.setMinimumWidth(10);
            buttonPanel.setMinimumHeight(10);
            buttonPanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    requestNewPictures(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonPanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonPanelConstraints.setPosition("south");
            buttonPanel.setConstraints(buttonPanelConstraints);
            this.addChild(buttonPanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}