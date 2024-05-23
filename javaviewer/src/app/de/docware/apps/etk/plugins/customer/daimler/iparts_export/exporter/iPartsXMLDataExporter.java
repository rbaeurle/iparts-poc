/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_export.exporter;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.config.partlist.EtkEbenenDaten;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.events.OnExtendFormEvent;
import de.docware.apps.etk.base.forms.events.OnValidateAttributesEvent;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.*;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataImage;
import de.docware.apps.etk.base.project.mechanic.drawing.EtkDataPool;
import de.docware.apps.etk.base.project.mechanic.drawing.ImageVariant;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AbstractDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.AggregateDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.DataCardRetrievalException;
import de.docware.apps.etk.plugins.customer.daimler.iparts.datacard.VehicleDataCard;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.filter.iPartsDataCodeList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteContentList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsPartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.virtualstruct.iPartsSAId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.code.DaimlerCodes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilter;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiDaimlerLangSelectionBox;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.iPartsGuiPSKVariantsSelectTextField;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridModel;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.searchgrids.SelectSearchGridSA;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.replacements.iPartsPRIMUSReplacementsCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.jobs.iPartsJobsManager;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogSeries;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.einpas.EinPasId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuTemplate;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.structure.iPartsCatalogNode;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects.iPartsDataExportContent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects.iPartsDataExportRequest;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects.iPartsExportContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.dbobjects.iPartsExportRequestId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_export.iPartsExportPlugin;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiCheckbox;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.resources.FrameworkImageUtils;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.cache.ObjectInstanceLRUList;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateException;
import de.docware.util.date.DateUtils;
import de.docware.util.file.DWFile;
import de.docware.util.imageconverter.ImageInformation;
import de.docware.util.misc.booleanfunctionparser.BooleanFunction;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLUtils;
import de.docware.util.sql.TableAndFieldName;

import javax.xml.stream.XMLStreamException;
import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Exporter für Stücklistendaten an Dritte.
 */
public class iPartsXMLDataExporter extends AbstractXMLExporter implements iPartsXMLPartListExporterTags {

    private static final String EXPORT_NAME = "PartListData";
    private static final String EXPORT_IMAGES_SUBFOLDER = "images";
    private static final int MAX_SIZE_COLORNAME_CACHE = 1000; // Maximalgrenze für gecachte Farbbenennungen
    private static final Language DEFAULT_EXPORT_LANGUAGE = Language.DE; // Default Export-Sprache
    private static final String LIST_DELIMITER = ", ";

    private final Map<String, EtkDataPart> partsCache = new HashMap<>();
    private final ObjectInstanceLRUList<String, EtkMultiSprache> colorNames = new ObjectInstanceLRUList<>(MAX_SIZE_COLORNAME_CACHE, -1);
    private final Map<String, String> groupNumbers = new HashMap<>();
    private final Map<String, String> allCodesDuringExport = new TreeMap<>(); // Alle Code, die während dem Export vorkommen
    private final Set<SpringMapping> springPartToObjectMapping = new LinkedHashSet<>();
    private final Set<String> existingSaasInAssemblies = new HashSet<>();
    private iPartsDataModel selectedModel;
    private iPartsDataSa selectedSA;
    private iPartsProduct selectedProduct;
    private iPartsProductStructures productStructures;
    private iPartsExportTask exportTask;
    private iPartsFilter filter;
    private iPartsWireHarness wireHarnessCache;
    private int assembliesMaxCount;
    private Map<String, EtkMultiSprache> combTextForKlfdNr;
    private Map<String, EtkDataPart> wireHarnessParts;
    private DWFile imageDirectory;
    private Set<String> validSaasForModel;
    private Set<String> validSAsForModel;
    private Set<String> pskVariants; // PSK Varianten sofern es ein PSK Produkt ist und der Benutzer das PSK Recht hat
    private boolean isModuleSpringRelevant;
    private int nExportedPictures = 0;
    private int nExportedPartListEntries = 0;
    private boolean isWithGUI; // Info, ob es sich um einen Aufruf via Benutzeroberfläche oder Service handelt

    public iPartsXMLDataExporter(EtkProject project) {
        super(project, EXPORT_NAME);
    }

    public void exportData(AbstractJavaViewerForm owner, String exportDataObjectType) {
        switch (exportDataObjectType) {
            case iPartsSaId.TYPE:
            case iPartsModelId.TYPE:
                startExportWithoutChangeSets(() -> startExportWithGUI(owner, exportDataObjectType));
            case iPartsSeriesId.TYPE:
                break;
        }
    }


    /**
     * Zeigt die Auswahldialoge, zum Auswählen, was exportiert werden soll an, und startet den Export, falls der User
     * nicht vorher abbricht. Die Export-Datei wird in einem temporären Verzeichnis angelegt und nach erfolgreichem
     * Export zum Download angeboten.
     *
     * @param owner
     * @param exportDataObjectType
     */
    private void startExportWithGUI(AbstractJavaViewerForm owner, String exportDataObjectType) {
        isWithGUI = true;
        setLogFile();

        final List<MessageEventData> messageEventDatasForLogForm = new ArrayList<>();
        OnExtendExportDataElement extend = new OnExtendExportDataElement(getProject(), DEFAULT_EXPORT_LANGUAGE.getCode());

        final iPartsDataExportRequest exportRequest = iPartsDataExportRequest.createExportRequestWithUnusedGUID(getProject());
        iPartsExportRequestId exportRequestId = exportRequest.getAsId();
        iPartsExportContentId exportContentId = null;
        if (exportDataObjectType.equals(iPartsModelId.TYPE)) {
            if (!showModelAndProductSelectionDialog(owner, messageEventDatasForLogForm, extend)) {
                cancelledByUser("");
                return;
            }
            exportContentId = new iPartsExportContentId(exportRequestId.getJobId(), selectedModel.getAsId(), selectedProduct.getAsId());
        } else if (exportDataObjectType.equals(iPartsSaId.TYPE)) {
            if (!showSASelectionDialog(owner, messageEventDatasForLogForm, extend)) {
                cancelledByUser("");
                return;
            }
            exportContentId = new iPartsExportContentId(exportRequestId.getJobId(), selectedSA.getAsId(), null);
        }

        // Im Gegensatz zum einem vom Webservice getriggerten Export, gibt es beim manuell getriggerten noch keinen
        // Eintrag in DA_EXPORT_CONTENT.  Dieser wird angelegt sobald der Export anläuft.
        iPartsDataExportContent exportContent = new iPartsDataExportContent(getProject(), exportContentId);
        exportContent.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        exportTask = new iPartsExportTask();
        exportTask.init(exportContent, extend.getPictureExportType(), extend.getSelectedLanguages(), "",
                        extend.isExportAdditionalPartData(), extend.isExportEinPASData(), false);
        // Psk Inhalte ausgeben - Aufruf via GUI:
        // - ausgewähltes Produkt ist valide und ein PSK Produkt
        // - Benutzer hat das PSK Recht
        exportTask.setExportPSKData(isValidPSKProductSelected() && iPartsRight.checkPSKInSession());
        // PSK Varianten aus dem Auswahldialog extrahieren
        handleSelectedPSKVariants(extend, messageEventDatasForLogForm);


        // Gleiches gilt für den Eintrag in DA_EXPORT_REQUEST. Deswegen wird dieser jetzt angelegt.
        exportTask.fillNewExportRequest(exportRequest);
        exportRequest.setFieldValue(FIELD_DER_STATE, iPartsExportState.IN_PROCESS.getDbValue(), DBActionOrigin.FROM_EDIT);
        exportRequest.saveToDB();

        final EtkMessageLogForm logForm = initMessageLogForm("!!Stücklistendaten Export", getExportTitle());
        logForm.showModal(thread -> {
            // Im Fall vom manuellen Export, wird der finale Status direkt auf COMPLETED gesetzt, da aktuell nur einzelne
            // Exporte getriggert werden können, und damit sobald der eine fertig ist, auch der Gesamte Job fertig ist.
            exportContent.setFieldValue(iPartsConst.FIELD_DEC_STATE, iPartsExportState.IN_PROCESS.getDbValue(), DBActionOrigin.FROM_EDIT);
            exportContent.saveToDB();

            runExport(messageEventDatasForLogForm);

            if (isFailed()) {
                exportContent.setFieldValue(iPartsConst.FIELD_DEC_STATE, iPartsExportState.ERROR.getDbValue(), DBActionOrigin.FROM_EDIT);
                exportContent.setFieldValue(iPartsConst.FIELD_DEC_ERROR_TEXT, getTranslatedErrorMessage(), DBActionOrigin.FROM_EDIT);
                exportContent.saveToDB();

                exportRequest.setFieldValue(FIELD_DER_STATE, iPartsExportState.ERROR.getDbValue(), DBActionOrigin.FROM_EDIT);
                exportRequest.setFieldValueAsDateTime(FIELD_DER_COMPLETION_DATE, Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
                exportRequest.setFieldValue(iPartsConst.FIELD_DER_ERROR_TEXT, getTranslatedErrorMessage(), DBActionOrigin.FROM_EDIT);
                exportRequest.saveToDB();

                iPartsJobsManager.getInstance().jobError(getLogFile());
                return;
            }

            if (isCancelled()) {
                // Export wurde von Hand abgebrochen, also doch nicht in der Datenbank festhalten
                exportContent.deleteFromDB(true);
                exportRequest.deleteFromDB(true);

                iPartsJobsManager.getInstance().jobCancelled(getLogFile(), true);
            } else {
                exportContent.setFieldValue(iPartsConst.FIELD_DEC_STATE, iPartsExportState.COMPLETED.getDbValue(), DBActionOrigin.FROM_EDIT);
                exportContent.setFieldValue(iPartsConst.FIELD_DEC_ARCHIVE_SIZE, Long.toString(getExportFile().length()), DBActionOrigin.FROM_EDIT);
                exportContent.setFieldValueAsInteger(iPartsConst.FIELD_DEC_NUMBER_PICTURES, nExportedPictures, DBActionOrigin.FROM_EDIT);
                exportContent.setFieldValueAsInteger(iPartsConst.FIELD_DEC_NUMBER_PARTLIST_ITEMS, nExportedPartListEntries, DBActionOrigin.FROM_EDIT);
                exportContent.saveToDB();

                exportRequest.setFieldValue(FIELD_DER_STATE, iPartsExportState.COMPLETED.getDbValue(), DBActionOrigin.FROM_EDIT);
                exportRequest.setFieldValueAsDateTime(FIELD_DER_COMPLETION_DATE, Calendar.getInstance(), DBActionOrigin.FROM_EDIT);
                exportRequest.saveToDB();

                // Erzeugte XML Datei herunterladen
                downloadExportFile();

                // Letzte Informationen ins Log schreiben
                finishExportAndLogFile();
            }
        });

        // Temp-Verzeichnis löschen
        deleteExportFileDirectory();
    }

    /**
     * Verarbeitet die vom Benutzer ausgewählten PSK Varianten
     *
     * @param extend
     * @param messageEventDatasForLogForm
     */
    private void handleSelectedPSKVariants(OnExtendExportDataElement extend, List<MessageEventData> messageEventDatasForLogForm) {
        if (isPSKSpecialCase()) {
            // Die PSK Varianten zwischenspeichern
            pskVariants = extend.getProductVariants();
            // Log, ob und welche Varianten ausgewählt wurden
            if (pskVariants.isEmpty()) {
                handleMessageEventData(messageEventDatasForLogForm, translateForLog("!!Für das Produkt \"%1\" " +
                                                                                    "wurden keine PSK Varianten ausgewählt",
                                                                                    selectedProduct.getAsId().getProductNumber()),
                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            } else {
                handleMessageEventData(messageEventDatasForLogForm, translateForLog("!!Für das Produkt \"%1\" " +
                                                                                    "wurden folgende PSK Varianten ausgewählt: \"%2\"",
                                                                                    selectedProduct.getAsId().getProductNumber(),
                                                                                    StrUtils.stringListToString(pskVariants,
                                                                                                                LIST_DELIMITER)),
                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            }
        }
    }

    public void startExportWithoutGUI(iPartsExportTask exportTask) {
        isWithGUI = false;
        // Log-File anlegen. Da ohne GUI exportiert wird, werden alle Meldungen während des Exports direkt in die Datei geloggt.
        setLogFile();

        // Im Fall vom über den iPartsExportScheduler getriggerten Export, wird der finale Status nur auf
        // EXPORTED gesetzt, da erst nachdem alle Exporte durch sind, der Scheduler alle Tasks auf COMPLETED setzt.
        iPartsDataExportContent exportContent = exportTask.getExportContent();
        exportContent.setFieldValue(iPartsConst.FIELD_DEC_STATE, iPartsExportState.IN_PROCESS.getDbValue(), DBActionOrigin.FROM_EDIT);
        exportContent.saveToDB();

        this.exportTask = exportTask;
        VarParam<String> translatedErrorMessage = new VarParam<>();
        if (exportContent.getAsId().getDataObjectType().equals(iPartsModelId.TYPE)) {
            if (!setModelAndProductFromExportTask(exportTask, translatedErrorMessage)) {
                stopExport(translatedErrorMessage.getValue());
            }
        } else if (exportContent.getAsId().getDataObjectType().equals(iPartsSaId.TYPE)) {
            if (!setSaFromExportTask(exportTask, translatedErrorMessage)) {
                stopExport(translatedErrorMessage.getValue());
            }
        }

        runExport(null);

        saveExportFile(exportTask, exportContent);

        if (isFailed()) {
            exportContent.setFieldValue(iPartsConst.FIELD_DEC_STATE, iPartsExportState.ERROR.getDbValue(), DBActionOrigin.FROM_EDIT);
            exportContent.setFieldValue(iPartsConst.FIELD_DEC_ERROR_TEXT, getTranslatedErrorMessage(), DBActionOrigin.FROM_EDIT);
            exportContent.saveToDB();

            iPartsJobsManager.getInstance().jobError(getLogFile());
        } else {
            exportContent.setFieldValue(iPartsConst.FIELD_DEC_STATE, iPartsExportState.EXPORTED.getDbValue(), DBActionOrigin.FROM_EDIT);
            exportContent.setFieldValue(iPartsConst.FIELD_DEC_ARCHIVE_SIZE, Long.toString(getExportFile().length()), DBActionOrigin.FROM_EDIT);
            exportContent.setFieldValue(iPartsConst.FIELD_DEC_ARCHIVE_FILE, getExportFile().getName(), DBActionOrigin.FROM_EDIT);
            exportContent.setFieldValueAsInteger(iPartsConst.FIELD_DEC_NUMBER_PICTURES, nExportedPictures, DBActionOrigin.FROM_EDIT);
            exportContent.setFieldValueAsInteger(iPartsConst.FIELD_DEC_NUMBER_PARTLIST_ITEMS, nExportedPartListEntries, DBActionOrigin.FROM_EDIT);
            exportContent.saveToDB();

            // Letzte Informationen ins Log schreiben
            finishExportAndLogFile();
        }

        // Temp-Verzeichnis löschen
        deleteExportFileDirectory();
    }

    private void runExport(List<MessageEventData> messageEventDataForLog) {
        if (!isRunning()) {
            return;
        }
        initFilter();
        // Vorher erzeugte Meldungen verarbeiten
        if (messageEventDataForLog != null) {
            for (MessageEventData message : messageEventDataForLog) {
                fireMessage(message);
            }
        }
        checkRequiredData();
        createExportFile();
        exportXMLFile();
    }

    private boolean setModelAndProductFromExportTask(iPartsExportTask exportTask, VarParam<String> translatedErrorMessage) {
        iPartsModelId modelId = exportTask.getExportContent().getAsId().getDataObjectId(iPartsModelId.class);
        iPartsDataModel model = getModelIfValid(modelId, translatedErrorMessage);
        if (model == null) {
            return false;
        }

        Set<String> productsForModel = getProductsForModel(modelId, translatedErrorMessage);
        if ((productsForModel == null) || productsForModel.isEmpty()) {
            return false;
        }

        String productForExport = getProductForExport(exportTask, model, productsForModel, translatedErrorMessage);
        if (productForExport == null) {
            return false;
        }

        iPartsProduct product = getProductIfValid(productForExport, translatedErrorMessage);
        if (product == null) {
            return false;
        }

        if (!acquireExportInformationFromModel(model.getAsId(), product.getAsId(), null, translatedErrorMessage)) {
            return false;
        }

        selectedModel = model;
        selectedProduct = product;

        return true;
    }


    private String getProductForExport(iPartsExportTask exportTask, iPartsDataModel model, Set<String> productsForModel,
                                       VarParam<String> translatedErrorMessage) {
        String productNumber = exportTask.getExportContent().getAsId().getProductNumber();
        if (StrUtils.isValid(productNumber)) {
            for (String productForModel : productsForModel) {
                if (productForModel.equals(productNumber)) {
                    return productNumber;
                }
            }
            translatedErrorMessage.setValue(translateForLog("!!Baumuster \"%1\" ist dem angeforderten Produkt \"%2\" nicht zugeordnet!",
                                                            model.getAsId().getModelNumber(), productNumber));
            return null;
        } else {
            if (productsForModel.size() == 1) {
                return productsForModel.iterator().next();
            } else {
                translatedErrorMessage.setValue(translateForLog("!!Baumuster \"%1\" ist mehreren Produkten zugeordnet, aber keines wurde angegeben!",
                                                                model.getAsId().getModelNumber()));
                return null;
            }
        }
    }

    private boolean setSaFromExportTask(iPartsExportTask exportTask, VarParam<String> translatedErrorMessage) {
        iPartsSaId saId = exportTask.getExportContent().getAsId().getDataObjectId(iPartsSaId.class);
        iPartsDataSa sa = getSaIfValid(saId, translatedErrorMessage);
        if (sa == null) {
            return false;
        }
        if (!acquireExportInformationForSA(sa.getAsId(), null, translatedErrorMessage)) {
            return false;
        }

        selectedSA = sa;

        return true;
    }

    /**
     * Liefert abhängig vom Export-Typ den Dateinamen für das Exportergebnis
     *
     * @return
     */
    @Override
    protected String getExportFileName() {
        switch (exportTask.getExportContent().getAsId().getDataObjectType()) {
            case iPartsModelId.TYPE:
                return EXPORT_NAME + "_" + selectedProduct.getAsId().getProductNumber() + "_" +
                       selectedModel.getAsId().getModelNumber() + "_"
                       + DateUtils.getCurrentDateFormatted(DATEFORMAT_EXPORT_FILE)
                       + "." + MimeTypes.EXTENSION_XML;
            case iPartsSaId.TYPE:
                return EXPORT_NAME + "_" + StrUtils.replaceSubstring(selectedSA.getAsId().getSaNumber(), " ", "_") + "_"
                       + DateUtils.getCurrentDateFormatted(DATEFORMAT_EXPORT_FILE)
                       + "." + MimeTypes.EXTENSION_XML;
        }
        return "";
    }

    /**
     * Liefert abhängig von Export-Typ den Titel für das Export-Fenster
     *
     * @return
     */
    @Override
    protected String getExportTitle() {
        switch (exportTask.getExportContent().getAsId().getDataObjectType()) {
            case iPartsModelId.TYPE:
                return translateForLog("!!Exportiere Stücklisten zu Baumuster \"%1\"" +
                                       " und Produkt \"%2\"", selectedModel.getAsId().getModelNumber(),
                                       selectedProduct.getAsId().getProductNumber());
            case iPartsSaId.TYPE:
                return translateForLog("!!Exportiere Stücklisten zur freien SA \"%1\"", selectedSA.getAsId().getSaNumber());
        }
        return "";
    }

    /**
     * Verschiebt die Export-Datei in das Verzeichnis des Customers des Export-Tasks und darin in das Unterverzeichnis
     * mit dem Namen der Job-Id des Tasks.
     *
     * @param exportTask
     * @param exportContent
     * @return
     */
    private void saveExportFile(iPartsExportTask exportTask, iPartsDataExportContent exportContent) {
        if (!isRunning()) {
            return;
        }
        DWFile exportJobDirForCustomer = DWFile.get(iPartsExportPlugin.getDirForExport(),
                                                    DWFile.convertToValidFileName(exportTask.getCustomerId()));
        if (!exportJobDirForCustomer.mkDirsWithRepeat()) {
            stopExport(translateForLog("!!Fehler beim Anlegen vom Export-Verzeichnis \"%1\".",
                                       exportJobDirForCustomer.getAbsolutePath()));
            return;
        }
        DWFile exportJobDir = DWFile.get(exportJobDirForCustomer, exportContent.getAsId().getJobId());
        if (!exportJobDir.mkDirsWithRepeat()) {
            stopExport(translateForLog("!!Fehler beim Anlegen vom Export-Verzeichnis \"%1\".",
                                       exportJobDir.getAbsolutePath()));
            return;
        }
        if (!moveExportFileToExportDirectory(exportJobDir)) {
            return;
        }
    }

    /**
     * Startet mit dem Schreiben der Informationen in die XML Datei.
     *
     * @throws XMLStreamException
     */
    private void exportXMLFile() {
        // XML Writer erzeugen. Der OutputStream muss lokal gehalten werden, weil laut API die close()
        // Methode vom XMLStreamWriter den darunterliegenden Stream NICHT schließt.
        try {
            if (!initWriterForFile(true)) {
                return;
            }
            // Initialisierung des Writers ist fertig -> Beginne mit dem Schreiben der Informationen
            getXmlWriter().writeStartDocument("UTF-8", "1.0");
            getXmlWriter().writeDTD(DOCTYPE_TEXT);
            // Die obersten zwei Elemente anlegen: <SCHNITSTELLE_PRODUKTION> und <PRODUKTDATEN>
            Map<String, String> attributes = new LinkedHashMap<>();
            attributes.put(ATTRIBUTE_IF_IDENT, DateUtils.getCurrentDateFormatted(EXPORT_HEADER_TIMESTAMP));
            setOptionalAttribute(ATTRIBUTE_EMPFAENGER, "", attributes);
            setOptionalAttribute(ATTRIBUTE_FTP_ADRESSE, "", attributes);
            attributes.put(ATTRIBUTE_USER, FrameworkUtils.getUserName());
            startElement(ELEMENT_SCHNITTSTELLE_PRODUKTDATEN, attributes);
            startElement(ELEMENT_PRODUKTDATEN);
            // Header und Nutzdaten schreiben
            exportHeader();
            exportPayload();
            closeRemainingElements();
            getXmlWriter().writeEndDocument();
        } catch (Exception e) {
            stopExport("!!Fehler beim Schreiben der Export-Datei \"" + getExportFile().getName() + "\"");
            Logger.getLogger().handleRuntimeException(e);
        } finally {
            try {
                closeXMLWriterAndOutputStreamAfterExport();
            } catch (Exception e) {
                Logger.getLogger().handleRuntimeException(e);
            }
        }
    }

    /**
     * Überprüft, ob für den aktuellen Exporttyp die notwendigen Daten vorhanden sind
     */
    private void checkRequiredData() {
        String cancelMessage = "";
        switch (exportTask.getExportContent().getAsId().getDataObjectType()) {
            case iPartsModelId.TYPE:
                if (selectedModel == null) {
                    cancelMessage = translateForLog("!!Fehler beim Stücklisten-Export. Baumuster wurde nicht gefunden.");
                }
                break;
            case iPartsSaId.TYPE:
                if (selectedSA == null) {
                    cancelMessage = translateForLog("!!Fehler beim Stücklisten-Export. Freie SA wurde nicht gefunden.");
                }
                break;
            case iPartsSeriesId.TYPE: {
                break;
            }
        }

        if (StrUtils.isValid(cancelMessage)) {
            stopExport(cancelMessage);
        }
    }

    /**
     * Exportiert die Header-Informationen. Hierbei handelt es sich um den <FRAGE> Teil der XML Datei
     *
     * @throws XMLStreamException
     */
    private void exportHeader() throws XMLStreamException {
        if (!isRunning()) {
            return;
        }
        fireMessageWithTimeStamp(translateForLog("!!Exportiere Header-Informationen \"%1\"", ELEMENT_FRAGE));
        Map<String, String> attibutes = new LinkedHashMap<>();
        // <FRAGE>
        startElement(ELEMENT_FRAGE);
        switch (exportTask.getExportContent().getAsId().getDataObjectType()) {
            case iPartsModelId.TYPE:
                attibutes.put(ATTRIBUTE_SEL_FORMAT_ID, "BAUMUSTER");
                // <SEL_FORMAT>
                writeEnclosedElement(ELEMENT_SEL_FORMAT, "", attibutes, false);
                exportModelHeader();
                break;
            case iPartsSaId.TYPE:
                attibutes.put(ATTRIBUTE_SEL_FORMAT_ID, "SA_INHALT");
                // <SEL_FORMAT>
                writeEnclosedElement(ELEMENT_SEL_FORMAT, "", attibutes, false);
                exportSaHeader();
                break;
            case iPartsSeriesId.TYPE:
                break;
        }
        endElement(ELEMENT_FRAGE);

        fireMessageToLogFileWithTimeStamp(translateForLog("!!Exportieren der Header-Informationen \"%1\" erfolgreich", ELEMENT_FRAGE));
    }

    /**
     * SA-Export spezifische Angaben im Header
     *
     * @throws XMLStreamException
     */
    private void exportSaHeader() throws XMLStreamException {
        Map<String, String> attibutes = new LinkedHashMap<>();
        // <SEL_SA_INHALT>
        startElement(ELEMENT_SEL_SA_INHALT);
        attibutes.put(ATTRIBUTE_SEL_SA_ID, selectedSA.getAsId().getSaNumber());
        // <SEL_SA_IDENT>
        writeEnclosedElement(ELEMENT_SEL_SA_IDENT, "", attibutes, false);
        endElement(ELEMENT_SEL_SA_INHALT);
    }

    /**
     * Baumuster-Export spezifische Angaben im Header
     *
     * @throws XMLStreamException
     */
    private void exportModelHeader() throws XMLStreamException {
        Map<String, String> attibutes = new LinkedHashMap<>();
        // <SEL_BAUMUSTER>
        startElement(ELEMENT_SEL_BAUMUSTER);
//        attibutes.put(ATTRIBUTE_SEL_BAUMUSTER_ID, getFormatedModel());
        attibutes.put(ATTRIBUTE_SEL_BAUMUSTER_ID, selectedModel.getAsId().getModelNumber());
        attibutes.put(ATTRIBUTE_SEL_EINSATZ, selectedProduct.getAsId().getProductNumber());
        // <SEL_BAUMUSTER_IDENT>
        writeEnclosedElement(ELEMENT_SEL_BAUMUSTER_IDENT, "", attibutes, false);
        endElement(ELEMENT_SEL_BAUMUSTER);
    }

    /**
     * Exportiert den eigentlichen Inhalt der XML (Nutzdaten). Hierbei handelt es sich um den <Antwort> Teil der XML Datei
     *
     * @throws XMLStreamException
     */
    private void exportPayload() throws XMLStreamException {
        if (!isRunning()) {
            return;
        }
        fireMessageWithTimeStamp(translateForLog("!!Exportiere Nutzdaten-Informationen \"%1\"", ELEMENT_ANTWORT));
        startElement(ELEMENT_ANTWORT);
        switch (exportTask.getExportContent().getAsId().getDataObjectType()) {
            case iPartsModelId.TYPE:
                exportModelData();
                break;
            case iPartsSaId.TYPE:
                exportSaData();
                break;
            case iPartsSeriesId.TYPE:
                break;
        }
        endElement(ELEMENT_ANTWORT);
        fireMessageToLogFileWithTimeStamp(translateForLog("!!Exportieren der Nutzdaten-Informationen \"%1\" erfolgreich", ELEMENT_ANTWORT));
    }

    /**
     * Exportiert die SA-Informationen
     *
     * @throws XMLStreamException
     */
    private void exportSaData() throws XMLStreamException {
        if (!isRunning()) {
            return;
        }
        fireMessageWithTimeStamp(translateForLog("!!Exportiere SA-Informationen \"%1\"", ELEMENT_SA_INHALT));
        iPartsDataSAModulesList saModulesList = iPartsDataSAModulesList.loadDataForSA(getProject(), new iPartsSAId(selectedSA.getAsId().getSaNumber()));
        if (saModulesList.isEmpty()) {
            fireMessageWithTimeStamp(translateForLog("!!Zur freien SA \"%1\" sind keine Stücklisten vorhanden", selectedSA.getAsId().getSaNumber()));
            warningCount++;
            return;
        } else {
            List<iPartsDataAssembly> assemblies = new ArrayList<>();
            Map<iPartsDocumentationType, Integer> docTypes = new HashMap<>();
            for (iPartsDataSAModules saModule : saModulesList) {
                EtkDataAssembly dataAssembly = EtkDataObjectFactory.createDataAssembly(getProject(), new AssemblyId(saModule.getFieldValue(FIELD_DSM_MODULE_NO), ""));
                if (dataAssembly.existsInDB() && (dataAssembly instanceof iPartsDataAssembly)) {
                    iPartsDataAssembly iPartsDataAssembly = (iPartsDataAssembly)dataAssembly;
                    assemblies.add(iPartsDataAssembly);
                    iPartsDocumentationType docType = iPartsDataAssembly.getModuleMetaData().getDocumentationType();
                    Integer count = docTypes.get(docType);
                    if (count == null) {
                        docTypes.put(docType, 1);
                    } else {
                        docTypes.put(docType, count + 1);
                    }
                }
            }
            boolean multipleDocTypes = docTypes.size() > 1;
            Map<String, String> attributes = new LinkedHashMap<>();
            String documentationType = "";
            int docTypeAmount = 0;
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<iPartsDocumentationType, Integer> docTypesEntry : docTypes.entrySet()) {
                if (docTypesEntry.getValue() > docTypeAmount) {
                    docTypeAmount = docTypesEntry.getValue();
                    documentationType = docTypesEntry.getKey().getExportValue();
                }
                if (multipleDocTypes) {
                    builder.append(" ");
                    builder.append(translateForLog("!!%1 (%2 Stücklisten)",
                                                   docTypesEntry.getKey().getExportValue(),
                                                   String.valueOf(docTypesEntry.getValue())));
                }
            }
            if (multipleDocTypes) {
                fireMessageWithTimeStamp(translateForLog("!!Zur freien SA \"%1\" existieren Stücklisten mit" +
                                                         " unterschiedlichen Dokumentationsmethoden:", selectedSA.getAsId().getSaNumber()) + builder);
                fireMessageWithTimeStamp(translateForLog("!!Es wird der Wert für die Dokumethode \"%1\" exportiert", documentationType));
                warningCount++;
            }
            // <SA_INHALT>
            setOptionalAttribute(ATTRIBUTE_DOKUMETHODE, documentationType, attributes);
            startElement(ELEMENT_SA_INHALT, attributes);
            attributes.clear();
            iPartsDataSaaList saaList = iPartsDataSaaList.loadAllSaasForSa(getProject(), selectedSA.getAsId().getSaNumber());

            // Bestimmung aller real existierenden SAA-Gültigkeiten in den Modulen der freien SA. Die eigentlich sinnvolle
            // Methode exportPartListData() darf hier noch nicht aufgerufen werden, weil dann die Reihenfolge der XML-Elemente
            // nicht mehr stimmen würde: erst müssen nämlich die SA_GRUNDDATEN kommen und DANACH erst die TUs -> relevanten
            // Code zur Ermittlung der SAA-Gültigkeiten aus exportPartListData() hier extrahieren
            for (iPartsDataAssembly assembly : assemblies) {
                assembly.clearFilteredPartLists();
                for (EtkDataImage image : assembly.getUnfilteredImages()) {
                    if (checkFilter(image)) {
                        writeValidity(ELEMENT_KOMPONENTEN_ID, image, FIELD_I_SAA_CONSTKIT_VALIDITY, null, existingSaasInAssemblies, false);
                    }
                }

                for (EtkDataPartListEntry partlistEntry : assembly.getPartListUnfiltered(getFieldsForPartListLoad())) {
                    if (!isRunning()) {
                        return;
                    }
                    if (partlistEntry instanceof iPartsDataPartListEntry) {
                        iPartsDataPartListEntry iPartsDataPartListEntry = (iPartsDataPartListEntry)partlistEntry;

                        // Das virtuelle Feld für die um AS-/Zubehör-Code reduzierte Code-Regel muss basierend auf dem lokalen
                        // Filter für den Export neu berechnet werden -> auch das darauf basierende virtuelle Feld RETAIL_CODES_WITH_EVENTS
                        // entfernen, damit es neu berechnet wird
                        iPartsDataPartListEntry.calculateRetailCodesReducedAndFiltered(filter);
                        iPartsDataPartListEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_WITH_EVENTS);

                        if (checkFilter(iPartsDataPartListEntry)) {
                            EtkDataPart part = iPartsDataPartListEntry.getPart();
                            if (part != null) {
                                writeValidity(ELEMENT_KOMPONENTEN_ID, iPartsDataPartListEntry, FIELD_K_SA_VALIDITY, null,
                                              existingSaasInAssemblies, false);
                            }
                        }
                    }
                }
            }

            List<ExportSaa> saasForExport = new ArrayList<>();
            for (iPartsDataSaa saaData : saaList) {
                if (existingSaasInAssemblies.contains(saaData.getAsId().getSaaNumber())) {
                    saasForExport.add(new ExportSaa(saaData));
                }
            }
            exportSaMainData(selectedSA.getAsId().getSaNumber(), saasForExport);

            int assemblyCount = 0;
            for (iPartsDataAssembly assembly : assemblies) {
                assemblyCount = exportPartListData(assembly, assemblyCount, selectedSA.getAsId().getSaNumber(),
                                                   selectedSA.getFieldValueAsMultiLanguage(FIELD_DS_DESC), "", getFootnoteTextsForModuleSearchTerm(assembly.getAsId().getKVari()));
            }
            endElement(ELEMENT_SA_INHALT);
            fireMessageToLogFileWithTimeStamp(translateForLog("!!Exportiere SA-Informationen \"%1\" erfolgreich", ELEMENT_SA_INHALT));
        }

    }

    private String getLangText(EtkMultiSprache multi, String lang) {
        return multi.getTextByNearestLanguage(lang, exportTask.getFallbackLanguages());
    }

    private String getLangText(EtkMultiSprache multi, Language language) {
        return getLangText(multi, language.getCode());
    }

    /**
     * Exportiert die Baumusterinformationen
     *
     * @throws XMLStreamException
     */
    private void exportModelData() throws XMLStreamException {
        if (!isRunning()) {
            return;
        }

        Map<String, String> attributes = new LinkedHashMap<>();
        // Baumuster-Export
        fireMessageWithTimeStamp(translateForLog("!!Exportiere Baumuster-Informationen \"%1\"", ELEMENT_BAUMUSTER));
        iPartsModelId selectedModelId = selectedModel.getAsId();
        iPartsDataProductModels productModel = iPartsProductModels.getInstance(getProject()).getProductModelsByModelAndProduct(getProject(),
                                                                                                                               selectedModelId.getModelNumber(),
                                                                                                                               selectedProduct.getAsId().getProductNumber());
        String steering = "";
        String validFrom = "";
        String validTo = "";
        if (productModel != null) {
            steering = productModel.getFieldValue(FIELD_DPM_STEERING);
            validFrom = getISODate(iPartsProductModelHelper.getValidFromValue(getProject(), productModel, selectedModelId));
            validTo = getISODate(iPartsProductModelHelper.getValidToValue(getProject(), productModel, selectedModelId));
        } else {
            fireWarning(translateForLog("!!Folgende produktspezifische Elemente können nicht befüllt werden, " +
                                        "da die Baumuster-Produkt-Zugehörigkeit in der DB nicht existiert:" +
                                        " \"%1\", \"%2\", \"%3\"", ATTRIBUTE_LENKUNG, ATTRIBUTE_MARKTEINFUEHRUNGSTERMIN_AB,
                                        ATTRIBUTE_MARKTEINFUEHRUNGSTERMIN_BIS));
            warningCount++;
        }

        // <BAUMUSTER>
        setOptionalAttribute(ATTRIBUTE_BAUMUSTERART, selectedModel.getModelType(), attributes);
        setOptionalAttribute(ATTRIBUTE_LENKUNG, steering, attributes);
        setOptionalAttribute(ATTRIBUTE_MARKTEINFUEHRUNGSTERMIN_AB, validFrom, attributes);
        setOptionalAttribute(ATTRIBUTE_MARKTEINFUEHRUNGSTERMIN_BIS, validTo, attributes);
        setOptionalAttribute(ATTRIBUTE_PS, selectedModel.getFieldValue(FIELD_DM_HORSEPOWER), attributes);
        setOptionalAttribute(ATTRIBUTE_KW, selectedModel.getFieldValue(FIELD_DM_KILOWATTS), attributes);
        setOptionalAttribute(ATTRIBUTE_DOKUMETHODE, selectedProduct.getDocumentationType().getExportValue(), attributes);
        startElement(ELEMENT_BAUMUSTER, attributes);
        attributes.clear();
        // <BAUMUSTER_IDENT>
        attributes.put(ATTRIBUTE_BAUMUSTER_ID, selectedModelId.getModelNumber());
        attributes.put(ATTRIBUTE_EINSATZ, selectedProduct.getAsId().getProductNumber());
        // Ausgewählte PSK Varianten nur ausgeben, wenn es sich um den speziellen Fall handelt
        if (isPSKSpecialCase()) {
            //<SELECTED_PSK_VARIANTS>
            attributes.put(ATTRIBUTE_SELECTED_PSK_VARIANTS, (pskVariants == null) ? "" : StrUtils.stringListToString(pskVariants,
                                                                                                                     LIST_DELIMITER));
        }
        writeEmptyElement(ELEMENT_BAUMUSTER_IDENT, attributes, false);
        attributes.clear();
        // <GUELTIGKEIT> (Produkt-spezifische Gültigkeiten)
        setOptionalAttribute(ATTRIBUTE_BEMERKUNG, selectedProduct.getProductRemarkWithCommentFallback(getProject()).getText(getProject().getDBLanguage()), attributes);
        setOptionalAttribute(ATTRIBUTE_DATUM_DOKUMENTATION, getISODate(selectedProduct.getDatasetDateString()), attributes);
        setOptionalAttribute(ATTRIBUTE_MODELJAHRCODE, selectedProduct.getCodeForAutoSelect(), attributes);
        for (int i = 0; i < selectedProduct.getIdentsForAutoSelect().size(); i++) {
            iPartsIdentRange range = selectedProduct.getIdentsForAutoSelect().get(i);
            int attributeNumber = i + 1;
            setOptionalAttribute(ATTRIBUTE_ENDNUMMER_AB_BASE + attributeNumber, range.getFromIdent(), attributes);
            setOptionalAttribute(ATTRIBUTE_ENDNUMMER_BIS_BASE + attributeNumber, range.getToIdent(), attributes);
            if (attributeNumber >= MAX_COUNT_APS_ENDNUMBER) {
                break;
            }
        }
        setOptionalAttribute(ATTRIBUTE_NUR_GUELTIG_IN_LAENDERN, StrUtils.stringListToString(selectedProduct.getValidCountries(),
                                                                                            LIST_DELIMITER),
                             attributes);
        setOptionalAttribute(ATTRIBUTE_UNGUELTIGE_LAENDER, StrUtils.stringListToString(selectedProduct.getInvalidCountries(),
                                                                                       LIST_DELIMITER),
                             attributes);

        writeEnclosedElement(ELEMENT_GUELTIGKEIT, "", attributes, true);
        attributes.clear();
        // <AS_PRODUKTKLASSE>
        for (String productClass : selectedProduct.getAsProductClasses()) {
            writeEnclosedElement(ELEMENT_AS_PRODUKTKLASSE, productClass, null, true);
        }
        // <TEXTE> Baumusterbenennung
        EtkMultiSprache description = selectedModel.getFieldValueAsMultiLanguage(FIELD_DM_NAME);
        EtkMultiSprache salesDescription = selectedModel.getFieldValueAsMultiLanguage(FIELD_DM_SALES_TITLE);
        if (productModel != null) {
            EtkMultiSprache additionalTexts = iPartsProductModelHelper.getModelAddText(getProject(), productModel);
            // DAIMLER-7977
            for (Language language : exportTask.getSelectedLanguages()) {
                String additionalText = getLangText(additionalTexts, language);
                String descriptionText = getLangText(description, language);
                String salesDescriptionText = getSalesDescriptionText(salesDescription, language);
                //Existiert keine Benennung, keine Verkaufsbezeichnung und kein Zusatztext -> Überspringen
                if (StrUtils.isEmpty(additionalText) && StrUtils.isEmpty(descriptionText) && StrUtils.isEmpty(salesDescriptionText)) {
                    attributes.clear();
                    continue;
                }
                attributes.put(ATTRIBUTE_SPRACH_ID, language.getCode());
                setOptionalAttribute(ATTRIBUTE_BENENNUNG, descriptionText, attributes);
                setOptionalAttribute(ATTRIBUTE_VERKAUFSBEZEICHNUNG, salesDescriptionText, attributes);
                // <ZUSATZTEXTE> für Baumuster, falls vorhanden
                if (StrUtils.isValid(additionalText)) {
                    startElement(ELEMENT_TEXTE, attributes);
                    attributes.clear();
                    attributes.put(ATTRIBUTE_ZUSATZTEXT, additionalText);
                    writeEnclosedElement(ELEMENT_ZUSATZTEXTE, "", attributes, true);
                    endElement(ELEMENT_TEXTE);
                } else {
                    writeEnclosedElement(ELEMENT_TEXTE, "", attributes, true);
                }
                attributes.clear();
            }
        }
        // <AGGREGATE_BAUMUSTER_ID> D-Baumuster zu C-Baumuster
        iPartsModel modelFromCache = iPartsModel.getInstance(getProject(), selectedModelId);
        if (modelFromCache.isVehicleModel()) {
            iPartsDataModelsAggsList modelsAggsList = iPartsDataModelsAggsList.loadDataModelsAggsListSortedForModel(getProject(), selectedModelId.getModelNumber(), false);
            if (!modelsAggsList.isEmpty()) {
                for (iPartsDataModelsAggs aggModel : modelsAggsList) {
                    iPartsModel aggModelFromCache = iPartsModel.getInstance(getProject(), new iPartsModelId(aggModel.getAsId().getAggregateModelNumber()));
                    if (aggModelFromCache.existsInDB() && aggModelFromCache.isModelVisible()) {
                        writeEnclosedElement(ELEMENT_AGGREGATE_BAUMUSTER_ID, aggModelFromCache.getModelId().getModelNumber(), null, false);
                    }
                }
            }
        }
        exportKGTUStructureFromProduct();
        exportCodeInformation();
        exportValidSaasAndBKsForModel();
        exportSpringMapping();
        exportWireHarnessData();

        endElement(ELEMENT_BAUMUSTER);
        fireMessageToLogFileWithTimeStamp(translateForLog("!!Exportiere Baumuster-Informationen \"%1\" erfolgreich", ELEMENT_BAUMUSTER));
    }

    /**
     * Liefert die Verkaufsbezeichnung für die übergebene Sprache. Sollte für die Sprache (samt Fallbacksprachen) kein
     * Text existieren, dann wird der deutsche Text geliefert (weil die Verkaufsbezeichnung normalerweise nur für
     * "deutsch" angelegt wird).
     *
     * @param salesDescription
     * @param language
     * @return
     */
    private String getSalesDescriptionText(EtkMultiSprache salesDescription, Language language) {
        if ((salesDescription != null) && !salesDescription.isEmpty() && !salesDescription.allStringsAreEmpty()) {
            String text = getLangText(salesDescription, language);
            if (StrUtils.isEmpty(text)) {
                // Eigentlich wird die Verkaufsbezeichnung nur in Deutsch angelegt -> Suche nach deutschen Text
                text = getLangText(salesDescription, Language.DE);
            }
            return text;
        }
        return "";
    }

    /**
     * Exportiert die Leitungssatz-Baukasten, die während dem normalen Export aufgesammelt wurden
     */
    private void exportWireHarnessData() throws XMLStreamException {
        if ((wireHarnessParts != null) && !wireHarnessParts.isEmpty()) {
            fireMessageWithTimeStamp(translateForLog("!!Exportiere %1 Leitungssatz-Baukasten", String.valueOf(wireHarnessParts.size())));
            // <WIRE_HARNESS_DATEN>
            startElement(ELEMENT_WIRE_HARNESS_DATEN);
            Map<String, String> attributes = new HashMap<>();
            for (EtkDataPart wireHarnessPart : wireHarnessParts.values()) {
                if (wireHarnessPart == null) {
                    continue;
                }
                String partNumber = wireHarnessPart.getAsId().getMatNr();
                attributes.put(ATTRIBUTE_TEIL_ID, partNumber);
                // Einzelteile des Leitungssatz-BK bestimmen
                List<iPartsDataWireHarness> components = iPartsFilterHelper.getFilteredWireHarnessComponent(getProject(), partNumber,
                                                                                                            iPartsXMLExportLoadingFields.getWiringHarnessDisplayFields());
                EtkMultiSprache multiLang = wireHarnessPart.getFieldValueAsMultiLanguage(FIELD_M_TEXTNR);
                // Sollte ein Leitungssatz-BK keine Einzelteile und keine Benennung haben, dann reicht ein leeres XML Element
                if (components.isEmpty() && multiLang.allStringsAreEmpty()) {
                    writeEmptyElement(ELEMENT_WIRE_HARNESS, attributes, false);
                    continue;
                }
                // <WIRE_HARNESS>
                startElement(ELEMENT_WIRE_HARNESS, attributes);
                writeMultiLangEnclosedText(multiLang, translateForLog("!!Der Leitungssatz-Baukasten \"%1\" " +
                                                                      "hat keine Benennung",
                                                                      partNumber));
                // <WIRE_HARNESS_CONTENT>
                startElement(ELEMENT_WIRE_HARNESS_CONTENT);
                for (iPartsDataWireHarness component : components) {
                    exportSingleWireHarnessComponent(component, partNumber);
                }
                endElement(ELEMENT_WIRE_HARNESS_CONTENT);
                endElement(ELEMENT_WIRE_HARNESS);

                attributes.clear();
            }
            endElement(ELEMENT_WIRE_HARNESS_DATEN);
        }
    }

    /**
     * Exportiert ein Einzelteil eines Leitungssatz-Baukasten
     *
     * @param component
     * @param partNumber
     * @throws XMLStreamException
     */
    private void exportSingleWireHarnessComponent(iPartsDataWireHarness component, String partNumber) throws XMLStreamException {
        Map<String, String> attributes = new HashMap<>();
        String componentPartNumber = component.getAsId().getSubSnr();
        attributes.put(ATTRIBUTE_TEIL_ID, componentPartNumber);
        setOptionalAttribute(ATTRIBUTE_ES1, component.getFieldValue(FIELD_M_AS_ES_1), attributes);
        setOptionalAttribute(ATTRIBUTE_ES2, component.getFieldValue(FIELD_M_AS_ES_2), attributes);
        setOptionalAttribute(ATTRIBUTE_REFERENCE_NUMBER, component.getFieldValue(FIELD_DWH_REF), attributes);
        setOptionalAttribute(ATTRIBUTE_CONNECTOR_NUMBER, component.getFieldValue(FIELD_DWH_CONNECTOR_NO), attributes);
        setOptionalAttribute(ATTRIBUTE_PART_NUMBER_TYPE, component.getFieldValue(FIELD_DWH_SNR_TYPE), attributes);
        // <WIRE_HARNESS_COMPONENT>
        startElement(ELEMENT_WIRE_HARNESS_COMPONENT, attributes);
        EtkMultiSprache desc = getProject().getDbLayer().loadMultiLanguageByTextNr(TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR),
                                                                                   component.getAttribute(FIELD_M_TEXTNR).getMultiLanguageTextNr());
        // <TEXTE> - Benennung
        writeMultiLangEnclosedText(desc, translateForLog("!!Das Einzelteil \"%1\" aus dem Leitungssatz-Baukasten \"%2\" " +
                                                         "hat keine Benennung",
                                                         componentPartNumber, partNumber));
        // kombinierter Text
        writeCombinedText(null, component);
        desc = getProject().getDbLayer().loadMultiLanguageByTextNr(TableAndFieldName.make(TABLE_DA_WIRE_HARNESS, FIELD_DWH_CONTACT_ADD_TEXT),
                                                                   component.getAttribute(FIELD_DWH_CONTACT_ADD_TEXT).getMultiLanguageTextNr());
        // <ADDITIONAL_TEXT_DATEN>
        writeMultiLangWithSpecificEnclosedElements(ELEMENT_ADDITIONAL_TEXT_DATEN, ELEMENT_ADDITIONAL_TEXT, desc,
                                                   translateForLog("!!Das Einzelteil \"%1\" aus dem Leitungssatz-Baukasten \"%2\" " +
                                                                   "hat keinen Verwendungstext",
                                                                   componentPartNumber, partNumber));
        // <ADDITIONAL_PART_DATEN>
        loadAndWriteAdditionalPartInformation(componentPartNumber, component);

        endElement(ELEMENT_WIRE_HARNESS_COMPONENT);
    }

    /**
     * Methode zum Laden und Exportieren von Additional Part Information.
     * Lädt PRIMUS-Daten aus der DB und holt Custom Properties aus dem Cache,
     * kumuliert diese in einer Liste und übergibt sie an eine Methode, die ein
     * XML-Element aus diesen Daten erzeugt.
     *
     * @param matNr
     * @param dataObject
     * @throws XMLStreamException
     */
    private void loadAndWriteAdditionalPartInformation(String matNr, EtkDataObject dataObject) throws XMLStreamException {
        // <ADDITIONAL_PART_DATEN>
        // Diese sollen nur exportiert werden, wenn explizit angefordert (GUI Checkbox oder Request Parameter)
        if (exportTask.isExportAdditionalPartData()) {
            // Additional Part Information & Custom Properties aufsammeln
            iPartsCustomProperty iPartsCustomPropertyCache = iPartsCustomProperty.getInstance(getProject());
            List<iPartsCustomProperty.CustomProperty> additionalPartInformationList = new DwList<>();
            // Custom Properties einsammeln
            Collection<iPartsCustomProperty.CustomProperty> customProperties
                    = iPartsCustomPropertyCache.getCustomProperties(matNr);
            if ((customProperties != null) && (!customProperties.isEmpty())) {
                additionalPartInformationList.addAll(customProperties);
            }

            // Höhe, Breite, Länge, Gewicht und Volumen aus der Material-Tabelle als Custom Property sammeln und hinzufügen
            iPartsAdditionalPartInformationHelper.addAdditionalPartInformationAsCustomProperties(getProject(), dataObject,
                                                                                                 additionalPartInformationList);
            // Additional Part Information ausgeben
            writeAdditionalPartInformationData(additionalPartInformationList);
        }
    }

    /**
     * Schreibt die zusätzlichen Attribute einer Teilenummer ins XML
     *
     * @param additionalPartInformationList
     * @throws XMLStreamException
     */
    private void writeAdditionalPartInformationData(List<iPartsCustomProperty.CustomProperty> additionalPartInformationList) throws XMLStreamException {
        // Gesammelte Custom Properties und PRIMUS Daten gemeinsam, in einem Parent-Element dem XMl Export hinzufügen
        if (!additionalPartInformationList.isEmpty()) {
            // <ADDITIONAL_PART_INFORMATIONS>
            startElement(ELEMENT_ADDITIONAL_PART_DATA);
            for (iPartsCustomProperty.CustomProperty customProperty : additionalPartInformationList) {
                Map<String, String> customPropertyAttributes = new HashMap<>();
                setOptionalAttribute(ATTRIBUTE_API_TYPE, customProperty.getType(), customPropertyAttributes);
                // <ADDITIONAL_PART_INFORMATION>
                startElement(ELEMENT_ADDITIONAL_PART_INFORMATION, customPropertyAttributes);
                Map<String, EtkMultiSprache> multiLanguageValues = new HashMap<>();
                multiLanguageValues.put(ATTRIBUTE_API_DESCRIPTION, customProperty.getDescription());
                multiLanguageValues.put(ATTRIBUTE_API_VALUE, customProperty.getValueMultiLang());
                writeMultipleMultiLangValuesInEnclosedElement(multiLanguageValues, ELEMENT_ADDITIONAL_PART_VALUES, true);

                endElement(ELEMENT_ADDITIONAL_PART_INFORMATION);
            }
            endElement(ELEMENT_ADDITIONAL_PART_DATA);
        }
    }

    /**
     * Erzeugt XML-Elemente für die ausgewählten Sprachen aus einer übergebenen Map mit Attribut-Namen und {@link EtkMultiSprache}-Objekt.
     * Der Name des Knotens kann frei gewählt werden. Die Werte werden als Attribut an das Element einer Sprache angefügt.
     *
     * @param multiLangValues
     * @param multiLangXMLElementName
     * @param checkNeutralText
     * @throws XMLStreamException
     */
    private void writeMultipleMultiLangValuesInEnclosedElement(Map<String, EtkMultiSprache> multiLangValues, String multiLangXMLElementName,
                                                               boolean checkNeutralText) throws XMLStreamException {
        if ((multiLangValues != null) && (!multiLangValues.isEmpty())) {
            for (Language lang : exportTask.getSelectedLanguages()) {
                Map<String, String> multiLangValueAttributes = new HashMap<>();
                multiLangValueAttributes.put(ATTRIBUTE_SPRACH_ID, lang.getCode());
                for (Map.Entry<String, EtkMultiSprache> multiLangValue : multiLangValues.entrySet()) {
                    String multiLangValueText = getLangText(multiLangValue.getValue(), lang);
                    if (StrUtils.isEmpty(multiLangValueText) && checkNeutralText) {
                        multiLangValueText = getLangText(multiLangValue.getValue(), "");
                    }
                    setOptionalAttribute(multiLangValue.getKey(), multiLangValueText, multiLangValueAttributes);
                }
                writeEnclosedElement(multiLangXMLElementName, "", multiLangValueAttributes, true);
            }
        }
    }

    /**
     * Exportiert das Federmapping
     *
     * @throws XMLStreamException
     */
    private void exportSpringMapping() throws XMLStreamException {
        if (!springPartToObjectMapping.isEmpty()) {
            fireMessageWithTimeStamp(translateForLog("!!Exportiere %1 Federmapping-Paare", String.valueOf(springPartToObjectMapping.size())));
            // <BM_SPRING_MAPPING>
            startElement(ELEMENT_BM_SPRING_MAPPING);
            for (SpringMapping springMappingObject : springPartToObjectMapping) {
                // <SPRING_TABLE>
                startElement(ELEMENT_SPRING_TABLE);
                String springPartNumber = springMappingObject.getSpringPartNumber();
                EtkMultiSprache springPartDesc = springMappingObject.getSpringPartText();
                // <SPRING_PART>
                writeSpringMappingElement(springPartNumber, ELEMENT_SPRING_PART, springPartDesc);
                // <SPARE_PART>
                writeSpringMappingElement(springMappingObject.getSparePartNumber(), ELEMENT_SPARE_PART, null);
                endElement(ELEMENT_SPRING_TABLE);
            }
            endElement(ELEMENT_BM_SPRING_MAPPING);
        }
    }

    /**
     * Schreibt das übergebene Element für das Federmapping
     *
     * @param partNumber
     * @param xmlElement
     * @param partDesc
     * @throws XMLStreamException
     */
    private void writeSpringMappingElement(String partNumber, String xmlElement, EtkMultiSprache partDesc) throws XMLStreamException {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(ATTRIBUTE_TEIL_ID, partNumber);
        if ((partDesc == null) || partDesc.allStringsAreEmpty()) {
            writeEnclosedElement(xmlElement, "", attributes, false);
        } else {
            startElement(xmlElement, attributes);
            writeMultiLangEnclosedText(partDesc, translateForLog("!!Das Federmapping Element \"%1\" zur Teilenummer \"%2\" " +
                                                                 "hat keine Benennung",
                                                                 xmlElement, partNumber));
            endElement(xmlElement);
        }
    }

    /**
     * Exportiert die gültigen SAAs (samt SAs) und Baukästen zum Baumuster
     *
     * @throws XMLStreamException
     */
    private void exportValidSaasAndBKsForModel() throws XMLStreamException {
        exportValidSaasForModel();
        exportValidBKsForModel();
    }

    /**
     * Exportiert alle gültigen BKs zum Baumuster
     *
     * @throws XMLStreamException
     */
    private void exportValidBKsForModel() throws XMLStreamException {
        iPartsDataSAAModelsList bkModelsList = new iPartsDataSAAModelsList();
        EtkDataObjectList.JoinData joinData = new EtkDataObjectList.JoinData(TABLE_MAT,
                                                                             new String[]{ TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO) },
                                                                             new String[]{ FIELD_M_MATNR }, true, false);
        EtkDisplayFields selectFields = new EtkDisplayFields();
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_MATNR, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_1, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_AS_ES_2, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_BASE_MATNR, false, false));
        selectFields.addFeld(new EtkDisplayField(TABLE_MAT, FIELD_M_TEXTNR, true, false));
        // Nur für den PSK Spezialfall müssen weitere MAT Attribute geladen werden
        addPSKSelectFieldsForBKs(selectFields);

        bkModelsList.searchSortAndFillWithMultiLangValueForAllLanguagesAndJoin(getProject(), selectFields, TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR),
                                                                               new String[]{ FIELD_DA_ESM_MODEL_NO, FIELD_DA_ESM_SAA_NO },
                                                                               new String[]{ selectedModel.getAsId().getModelNumber(), "A*" },
                                                                               false,
                                                                               new String[]{ FIELD_M_MATNR },
                                                                               false, true,
                                                                               joinData);
        if (!bkModelsList.isEmpty()) {
            fireMessageWithTimeStamp(translateForLog("!!Exportiere %1 BK-Gültigkeiten zum Baumuster", String.valueOf(bkModelsList.size())));
            // <BAUMUSTER_BK_DATEN> Start der BK-Gültigkeiten zum Baumuster
            startElement(ELEMENT_BAUMUSTER_BK_DATEN);
            for (iPartsDataSAAModels bkModelData : bkModelsList) {
                EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(getProject(), new iPartsPartId(bkModelData.getAsId().getSAANumber(), ""));
                dataPart.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
                dataPart.setFieldValue(FIELD_M_BASE_MATNR, bkModelData.getFieldValue(FIELD_M_BASE_MATNR), DBActionOrigin.FROM_DB);
                dataPart.setFieldValue(FIELD_M_AS_ES_1, bkModelData.getFieldValue(FIELD_M_AS_ES_1), DBActionOrigin.FROM_DB);
                dataPart.setFieldValue(FIELD_M_AS_ES_2, bkModelData.getFieldValue(FIELD_M_AS_ES_2), DBActionOrigin.FROM_DB);
                EtkMultiSprache partDesc = bkModelData.getFieldValueAsMultiLanguage(FIELD_M_TEXTNR);
                dataPart.setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, partDesc, DBActionOrigin.FROM_DB);
                addPSKFieldValuesForBKDataPart(bkModelData, dataPart);
                writePartElement(dataPart, null, null, true, ELEMENT_BAUMUSTER_BK, ATTRIBUTE_BK_IDENT);
            }
            endElement(ELEMENT_BAUMUSTER_BK_DATEN);
        }
    }

    /**
     * Fügt bei dem {@link EtkDataPart} aus der BM zu Baukasten Abfrage die optionalen PSK Werte hinzu
     *
     * @param bkModelData
     * @param dataPart
     */
    private void addPSKFieldValuesForBKDataPart(iPartsDataSAAModels bkModelData, EtkDataPart dataPart) {
        if (isPSKSpecialCase()) {
            for (String pskField : iPartsXMLExportLoadingFields.getPskMatFieldsForExport().getAsFieldNamesArray()) {
                dataPart.setFieldValue(pskField, bkModelData.getFieldValue(pskField), DBActionOrigin.FROM_DB);
            }
        }
    }

    /**
     * Fügt bei der Baukasten DB Abfrage die optionalen PSK Materialfelder hinzu
     *
     * @param selectFields
     */
    private void addPSKSelectFieldsForBKs(EtkDisplayFields selectFields) {
        if (isPSKSpecialCase()) {
            for (String pskField : iPartsXMLExportLoadingFields.getPskMatFieldsForExport().getAsFieldNamesArray()) {
                selectFields.addFeld(new EtkDisplayField(TABLE_MAT, pskField, false, false));
            }
        }
    }

    /**
     * Exportiert alle gültigen SAAs zum Baumuster (samt SAs)
     *
     * @throws XMLStreamException
     */
    private void exportValidSaasForModel() throws XMLStreamException {
        if (existingSaasInAssemblies.isEmpty()) { // Ohne SAAs in den Modulen gibt es nichts zu tun
            return;
        }

        // Laden aller SAAs samt Benennungen und Infos
        iPartsDataSAAModelsList saaModelsList = new iPartsDataSAAModelsList();
        EtkDataObjectList.JoinData joinData = new EtkDataObjectList.JoinData(TABLE_DA_SAA, new String[]{ TableAndFieldName.make(TABLE_DA_EDS_SAA_MODELS, FIELD_DA_ESM_SAA_NO) },
                                                                             new String[]{ FIELD_DS_SAA }, true, false);
        EtkDisplayFields selectFields = new EtkDisplayFields();
        EtkDisplayField selectField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_SAA, false, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_DESC, true, false);
        selectFields.addFeld(selectField);
        selectField = new EtkDisplayField(TABLE_DA_SAA, FIELD_DS_CONNECTED_SAS, false, false);
        selectFields.addFeld(selectField);

        saaModelsList.searchSortAndFillWithMultiLangValueForAllLanguagesAndJoin(getProject(), selectFields, TableAndFieldName.make(TABLE_DA_SAA, FIELD_DS_DESC),
                                                                                new String[]{ FIELD_DA_ESM_MODEL_NO, FIELD_DA_ESM_SAA_NO },
                                                                                new String[]{ selectedModel.getAsId().getModelNumber(), "Z*" },
                                                                                false,
                                                                                new String[]{ FIELD_DS_SAA },
                                                                                false, true,
                                                                                joinData);

        // Gefundene SAAs und deren Benennungen filtern mit den existierenden SAAs aus den Modulen
        List<iPartsDataSAAModels> filteredSaaModelsList = new ArrayList<>(existingSaasInAssemblies.size());
        for (iPartsDataSAAModels saaModelData : saaModelsList) {
            String saaNumber = saaModelData.getAsId().getSAANumber();
            if (existingSaasInAssemblies.contains(saaNumber)) {
                filteredSaaModelsList.add(saaModelData);
            }
        }

        if (!filteredSaaModelsList.isEmpty()) {
            fireMessageWithTimeStamp(translateForLog("!!Exportiere %1 SAA-Gültigkeiten zum Baumuster", String.valueOf(filteredSaaModelsList.size())));
            // Eine Hilfsmap aufbauen mit SA-> Map mit SAA-Nummer auf SAA DatenObjekt
            Map<String, Map<String, ExportSaa>> saToSaaMap = new HashMap<>();
            for (iPartsDataSAAModels saaModelData : filteredSaaModelsList) {
                if (!isRunning()) {
                    return;
                }
                String saString = StrUtils.cutIfLongerThan(saaModelData.getAsId().getSAANumber(), 7);
                if (StrUtils.isValid(saString) && (saString.length() == 7)) {
                    Map<String, ExportSaa> saaMap = saToSaaMap.computeIfAbsent(saString, k -> new HashMap<>());
                    String saaString = saaModelData.getAsId().getSAANumber();
                    if (!saaMap.containsKey(saaString)) {
                        EtkMultiSprache saaDesc = saaModelData.getFieldValueAsMultiLanguage(FIELD_DS_DESC);
                        String connectedSasString = saaModelData.getFieldValue(FIELD_DS_CONNECTED_SAS);
                        ExportSaa saaForExport = new ExportSaa(saaString, saaDesc, connectedSasString);
                        saaMap.put(saaString, saaForExport);
                    }
                }
            }
            // <BAUMUSTER_SA_SAA_DATEN> Start der SAA-Gültigkeiten zum Baumuster
            startElement(ELEMENT_BAUMUSTER_SA_SAA_DATEN);
            for (Map.Entry<String, Map<String, ExportSaa>> saToSaaEntry : saToSaaMap.entrySet()) {
                exportSaMainData(saToSaaEntry.getKey(), saToSaaEntry.getValue().values());
            }
            endElement(ELEMENT_BAUMUSTER_SA_SAA_DATEN);
        }
    }

    /**
     * Exportiert die Stammdaten einer SA (samt Unter-SAAs)
     *
     * @param saNumber
     * @param saaCollection
     * @throws XMLStreamException
     */
    private void exportSaMainData(String saNumber, Collection<ExportSaa> saaCollection) throws XMLStreamException {
        Map<String, String> attributes = new HashMap<>();
        // <SA_GRUNDDATEN> Grunddaten zur SA
        startElement(ELEMENT_SA_GRUNDDATEN);
        attributes.put(ATTRIBUTE_SA_IDENT, saNumber);
        // <SA> SA-Nummer
        writeEmptyElement(ELEMENT_SA, attributes, false);
        iPartsSA saCache = iPartsSA.getInstance(getProject(), new iPartsSAId(saNumber));
        EtkMultiSprache saDescription = saCache.getTitle(getProject());
        writeMultiLangTagAndEnclosedText(ELEMENT_SA_BENENNUNG, saDescription,
                                         translateForLog("!!Die vorhandene SA %1 enthält keine Benennung",
                                                         saNumber));
        // <SA_CODE> Code an SAs
        writeEnclosedElement(ELEMENT_CODE, saCache.getCodes(getProject()), null, true);
        exportSaasForSA(saaCollection, saCache);
        endElement(ELEMENT_SA_GRUNDDATEN);
    }

    /**
     * Exportiert die gültigen SAAs zum Baumuster
     *
     * @param saasForExport
     * @param saCache
     * @throws XMLStreamException
     */
    private void exportSaasForSA(Collection<ExportSaa> saasForExport, iPartsSA saCache) throws XMLStreamException {
        if (saasForExport == null) {
            return;
        }
        Map<String, String> attributes = new HashMap<>();
        for (ExportSaa saaForExport : saasForExport) {
            // <SAA_GRUNDDATEN> Grunddaten zur SAA
            startElement(ELEMENT_SAA_GRUNDDATEN);
            attributes.put(ATTRIBUTE_SAA_IDENT, saaForExport.getSaaNumber());
            // <SA_SAA> SAA-Nummer
            writeEmptyElement(ELEMENT_SA_SAA, attributes, false);
            attributes.clear();
            EtkMultiSprache saaDesc = saaForExport.getSaaDesc();
            // <SAA_BENENNUNG> SAA-Benennung
            writeMultiLangTagAndEnclosedText(ELEMENT_SAA_BENENNUNG, saaDesc,
                                             translateForLog("!!Die vorhandene SAA %1 enthält keine Benennung",
                                                             saaForExport.getSaaNumber()));

            writeConnectedSAs(saaForExport);

            // Hole die Map mit SAA auf Fussnoten aus dem SA Cache
            Map<String, List<iPartsFootNote>> footnoteMap = saCache.getSaaFootNotesMap(getProject());
            if (footnoteMap != null) {
                // Existieren zur aktuellen SAA Fussnoten?
                List<iPartsFootNote> foornotesForSaa = footnoteMap.get(saaForExport.getSaaNumber());
                if (foornotesForSaa != null) {
                    Set<String> footnoteIdsForSA = new HashSet<>();
                    for (iPartsFootNote footnote : foornotesForSaa) {
                        footnoteIdsForSA.add(footnote.getFootNoteId().getFootNoteId());
                    }
                    // Map für die Standardfunktion zum Exportieren von Fussnoten
                    Map<String, List<iPartsDataFootNoteContent>> footnotesWithLinesMap = createFootnoteMap(footnoteIdsForSA);
                    if (!footnotesWithLinesMap.isEmpty()) {
                        // <FUSSNOTEN_DATEN> Fussnoten zu SAAs
                        startElement(ELEMENT_FUSSNOTE_DATEN);
                        for (iPartsFootNote footnote : foornotesForSaa) {
                            writeEnclosedFootnote(footnote.getFootNoteId().getFootNoteId(), footnotesWithLinesMap);
                        }
                        endElement(ELEMENT_FUSSNOTE_DATEN);
                    }
                }
            }
            endElement(ELEMENT_SAA_GRUNDDATEN);
        }
    }

    /**
     * Exportiert am Ende des Exports alle Code, die während dem Export aufgesammelt wurden
     *
     * @throws XMLStreamException
     */
    private void exportCodeInformation() throws XMLStreamException {
        if (!isRunning()) {
            return;
        }
        if (getMessageLog() != null) {
            getMessageLog().hideProgress();
        }

        // bm-bildende Code und Ausführungsart müssen ebenfalls mit aufgenommen werden
        addCodeString(selectedModel.getCodes());
        addCodeString(selectedModel.getAusfuehrungsart());
        if (allCodesDuringExport.isEmpty()) {
            return;
        }

        fireMessageWithTimeStamp(translateForLog("!!Exportiere Code-Informationen zu %1 Code", String.valueOf(allCodesDuringExport.size())));

        Map<String, Map<iPartsImportDataOrigin, List<iPartsDataCode>>> codeMapWithSeriesFromDB = new HashMap<>();
        Map<String, Map<iPartsImportDataOrigin, List<iPartsDataCode>>> codeMapWithoutSeriesFromDB = new HashMap<>();

        // baureihenspezifische Daten laden (MAD und DIALOG)
        fillCodeListWithSeriesData(codeMapWithSeriesFromDB);

        // baureihenunspezifische Daten laden (MAD und DIALOG)
        fillCodeListEmptySeriesData(codeMapWithoutSeriesFromDB);

        // Zähler für Code, die nicht gefunden werden konnten
        int noDataCount = 0;
        if (!codeMapWithSeriesFromDB.isEmpty() || !codeMapWithoutSeriesFromDB.isEmpty()) {
            startElement(ELEMENT_BM_CODE_DESCRIPTION);
            int dataWithoutSeries = 0;
            int count = 0;
            for (String code : allCodesDuringExport.values()) {
                count++;
                fireProgress(count, allCodesDuringExport.size(), "", true, true);
                boolean usedDataWithoutSeries = false;

                // Erst in der baureihenspezifischen Map suchen (MAD und DIALOG)
                Map<iPartsImportDataOrigin, List<iPartsDataCode>> codeDataMap = codeMapWithSeriesFromDB.get(code);
                if (codeDataMap == null) {
                    // In den baureihenspezifischen Daten wurde nichts gefunden -> Suche in Daten ohne Baureihenbezug
                    codeDataMap = codeMapWithoutSeriesFromDB.get(code);
                    usedDataWithoutSeries = true;
                }

                // Wenn in beiden Maps nichts gefunden wurde -> Keine Informationen zu Code in der DB
                if (codeDataMap == null) {
                    noDataCount++;
                    fireMessageToLogFileWithTimeStamp(translateForLog("!!Für Code \"%1\" konnten keine Daten gefunden werden (weder baureihen-spezifisch noch baureihen-unspezifisch).",
                                                                      code));
                    continue;
                }

                // Erst schauen, ob MAD Daten vorhanden sind
                List<iPartsDataCode> codesForOrigin = codeDataMap.get(iPartsImportDataOrigin.MAD);
                boolean foundValidCode = writeCodeElements(code, codesForOrigin);
                if (!foundValidCode) {
                    // MAD Daten sind keine vorhanden -> Suche nach DIALOG Daten
                    codesForOrigin = codeDataMap.get(iPartsImportDataOrigin.DIALOG);
                    foundValidCode = writeCodeElements(code, codesForOrigin);
                }

                // In MAD und DIALOG Daten wurde kein valider Datensatz gefunden (valide = mit existierender Benennung)
                if (!foundValidCode) {
                    noDataCount++;
                    fireMessageToLogFileWithTimeStamp(translateForLog("!!Für Code \"%1\" konnten keine Daten gefunden werden (weder baureihen-spezifisch noch baureihen-unspezifisch).",
                                                                      code));
                } else if (usedDataWithoutSeries) {
                    dataWithoutSeries++;
                }

            }
            endElement(ELEMENT_BM_CODE_DESCRIPTION);
            int exporterdCode = allCodesDuringExport.size() - noDataCount;
            fireMessageWithTimeStamp(translateForLog("!!%1 Code exportiert: %2 baureihen-spezifische, %3 baureihen-unspezifische",
                                                     String.valueOf(exporterdCode), String.valueOf(exporterdCode - dataWithoutSeries),
                                                     String.valueOf(dataWithoutSeries)));
        } else {
            // Es konnten keine Informationen zu den aufgesammelten Code in der DB gefunden werden
            noDataCount = allCodesDuringExport.size();
            for (String code : allCodesDuringExport.values()) {
                fireMessageToLogFileWithTimeStamp(translateForLog("!!Zu Code \"%1\", Baureihe \"%2\" und Produktgruppe \"%3\" konnten " +
                                                                  "keine Daten gefunden werden.",
                                                                  code, ((selectedProduct.getReferencedSeries() != null) ? selectedProduct.getReferencedSeries().getSeriesNumber() : ""),
                                                                  selectedProduct.getProductGroup()));
            }
        }

        if (noDataCount > 0) {
            fireMessageWithTimeStamp(translateForLog("!!Für %1 Code konnten keine Daten gefunden werden. Diese wurden daher nicht verarbeitet.",
                                                     String.valueOf(noDataCount)));
        }

    }

    /**
     * Schreibt den übergebenen Code als CODE-Elemente in die Export-Datei
     *
     * @param code
     * @param codesForOrigin
     * @return
     * @throws XMLStreamException
     */
    private boolean writeCodeElements(String code, List<iPartsDataCode> codesForOrigin) throws XMLStreamException {
        boolean result = false;
        if ((codesForOrigin != null) && !codesForOrigin.isEmpty()) {
            for (iPartsDataCode codeData : codesForOrigin) {
                if (codeData.getFieldValueAsMultiLanguage(FIELD_DC_DESC).allStringsAreEmpty()) {
                    continue;
                }
                Map<String, String> attributes = new LinkedHashMap<>();
                attributes.put(ATTRIBUTE_CODE_ID, code);
                attributes.put(ATTRIBUTE_DATUM_GUELTIG_AB, getISODate(codeData.getAsId().getSdata()));
                setOptionalAttribute(ATTRIBUTE_DATUM_GUELTIG_BIS, getISODate(codeData.getFieldValue(FIELD_DC_SDATB)), attributes);
                startElement(ELEMENT_CODE_DESCRIPTION, attributes);
                // <PRODUKTGRUPPE>
                writeEnclosedElement(ELEMENT_PRODUKTGRUPPE, codeData.getAsId().getProductGroup(), null, true);
                // TEXTE
                writeMultiLangEnclosedText(codeData.getFieldValueAsMultiLanguage(FIELD_DC_DESC),
                                           translateForLog("!!Code %1 hat keine Benennung", code));
                endElement(ELEMENT_CODE_DESCRIPTION);
                result = true;
            }
        }

        if (!result) {
            fireMessageToLogFileWithTimeStamp(translateForLog("!!Code %1 hat keine Benennung und wird nicht exportiert.", code));
        }
        return result;
    }

    /**
     * Befüllt die übergebenen Code-zu-CodeData-Map mit den Daten aus der DB zur aktuellen Baureihe. Hierbei werden MAD
     * und DIALOG Daten verarbeitet.
     *
     * @param codeMapToFill
     */
    private void fillCodeListWithSeriesData(Map<String, Map<iPartsImportDataOrigin, List<iPartsDataCode>>> codeMapToFill) {
        if (selectedProduct.getReferencedSeries() != null) {
            fireMessageWithTimeStamp(translateForLog("!!Lade Code mit Baureihenbezug zur Baureihe \"%1\"...", selectedProduct.getReferencedSeries().getSeriesNumber()));
            fillCodeListWithData(codeMapToFill, selectedProduct.getReferencedSeries().getSeriesNumber());
        }
    }

    /**
     * Befüllt die übergebenen Code-zu-CodeData-Map mit den Daten aus der DB zur leeren Baureihe. Hierbei werden MAD und
     * DIALOG Daten verarbeitet.
     *
     * @param codeMapToFill
     */
    private void fillCodeListEmptySeriesData(Map<String, Map<iPartsImportDataOrigin, List<iPartsDataCode>>> codeMapToFill) {
        fireMessageWithTimeStamp(translateForLog("!!Lade Code ohne Baureihenbezug..."));
        fillCodeListWithData(codeMapToFill, "");
    }

    /**
     * Befüllt die übergebenen Code-zu-CodeData-Map mit den Daten aus der DB zur aktuellen Produktgruppe, Quelle und Baureihe.
     * Jede Code-Benennung muss in allen verfügbaren Sprachen geladen werden.
     * <p>
     * Hierbei werden MAD, DIALOG und ggf. PROVAL Daten verarbeitet.
     *
     * @param codeMapToFill
     */
    private void fillCodeListWithData(Map<String, Map<iPartsImportDataOrigin, List<iPartsDataCode>>> codeMapToFill,
                                      String series) {
        iPartsDataCodeList codeList = iPartsDataCodeList.loadCodesForSeriesAndProductGroupAllLanguages(getProject(), series, selectedProduct.getProductGroup(),
                                                                                                       iPartsImportDataOrigin.MAD, true);

        // Alle MAD Daten zur übergebenen Baureihe aufbereiten
        fillMapWithCodes(codeList, codeMapToFill, iPartsImportDataOrigin.MAD);

        codeList = iPartsDataCodeList.loadCodesForSeriesAndProductGroupAllLanguages(getProject(), series, selectedProduct.getProductGroup(),
                                                                                    iPartsImportDataOrigin.DIALOG, false);
        // Alle DIALOG Daten zur übergebenen Baureihe aufbereiten
        fillMapWithCodes(codeList, codeMapToFill, iPartsImportDataOrigin.DIALOG);
        // Falls PROVAL CODE Benennungen angezeigt werden sollen, müssen diese geladen werden. Bei den schon vorhandenen Coden
        // wird die Benennung geändert falls nötig
        if (iPartsDataCodeList.SHOW_PROVAL_CODE_DESC) {
            codeList = iPartsDataCodeList.loadCodeForSeriesAndSourceAllLanguages(getProject(), series, iPartsImportDataOrigin.PROVAL);
            setPROVALCodeDesc(codeList, codeMapToFill);
        }
    }

    /**
     * Die Code Bennungen der Code aus der Map wird mit der passenden Code Benennung der Code aus der Liste überschrieben
     *
     * @param codeList
     * @param codeMapToChange
     */
    private void setPROVALCodeDesc(iPartsDataCodeList codeList, Map<String, Map<iPartsImportDataOrigin, List<iPartsDataCode>>> codeMapToChange) {
        if (codeMapToChange == null) {
            return;
        }

        for (iPartsDataCode provalCode : codeList) {
            String codeId = provalCode.getAsId().getCodeId();
            Map<iPartsImportDataOrigin, List<iPartsDataCode>> originGroupedCodeMap = codeMapToChange.get(codeId);
            if (originGroupedCodeMap != null) {
                for (List<iPartsDataCode> dataCodeList : originGroupedCodeMap.values()) {
                    for (iPartsDataCode dataCode : dataCodeList) {
                        EtkMultiSprache provalDesc = provalCode.getFieldValueAsMultiLanguage(iPartsConst.FIELD_DC_DESC);
                        dataCode.setFieldValueAsMultiLanguage(iPartsConst.FIELD_DC_DESC, provalDesc, DBActionOrigin.FROM_DB);
                    }
                }
            }
        }
    }

    /**
     * Befüllt die übergebene Map mit den Code aus der übergebenen <code>codeList</code>. Sortiert werden die Code
     * intern nach ihrer Quelle.
     *
     * @param codeList
     * @param codeMapToFill
     * @param dataOrigin
     */
    private void fillMapWithCodes(iPartsDataCodeList codeList, Map<String, Map<iPartsImportDataOrigin, List<iPartsDataCode>>> codeMapToFill,
                                  iPartsImportDataOrigin dataOrigin) {
        if (codeMapToFill != null) {
            if (!codeList.isEmpty()) {
                for (iPartsDataCode codeData : codeList) {
                    String code = codeData.getAsId().getCodeId();

                    // Code zeigt auf eine Map mit <Quelle zu Liste mit DBObjects>
                    Map<iPartsImportDataOrigin, List<iPartsDataCode>> codeDataMap = codeMapToFill.computeIfAbsent(code, k -> new HashMap<>());

                    // Quelle zeigt auf eine Liste mit DBObjects
                    List<iPartsDataCode> codeForOrigin = codeDataMap.computeIfAbsent(dataOrigin, k -> new ArrayList<>());
                    codeForOrigin.add(codeData);
                }
            }
        }
    }

    /**
     * Exportiert die KGTU Struktur des gefundenen Produkts
     *
     * @throws XMLStreamException
     */
    private void exportKGTUStructureFromProduct() throws XMLStreamException {
        if (!isRunning()) {
            return;
        }
        if (selectedProduct == null) {
            stopExport(translateForLog("!!Produkt konnte für den Export nicht bestimmt werden."));
            errorCount++;
            return;
        }
        fireMessageWithTimeStamp(translateForLog("!!Exportiere KGTU-Struktur-Informationen \"%1\" und \"%2\"", ELEMENT_KG, ELEMENT_TU));

        iPartsCatalogNode kgtuStructure;
        if (exportTask.isExportVisualNav() && selectedProduct.containsVisibleCarPerspectiveTU(getProject(), false)) {
            kgtuStructure = productStructures.getCompleteKgTuStructure(getProject(), false);
        } else {
            kgtuStructure = productStructures.getKgTuStructureWithoutCarPerspective(getProject(), false);
        }

        int assemblyCount = 0;
        for (iPartsCatalogNode kgNode : kgtuStructure.getChildren()) {
            if (!isRunning()) {
                return;
            }

            if (kgNode.isKgTuId()) {
                String searchTermForFootnotes = SQLUtils.escapeSQLWildcardExpressions(selectedProduct.getAsId().getProductNumber() + "_" + kgNode.getId().getValue(1) + "_*", getProject().getDB().getDatabaseType(MAIN));
                Map<String, List<iPartsDataFootNoteContent>> footnotesForKG = getFootnoteTextsForModuleSearchTerm(searchTermForFootnotes);
                startKGTUElement(kgNode);
                Set<String> freeSAsInKG = new HashSet<>();
                for (iPartsCatalogNode kgChildNode : kgNode.getChildren()) {
                    if (!isRunning()) {
                        return;
                    }
                    // Check, ob es sich auch um einen TU Knoten handelt
                    if (kgChildNode.isKgTuId()) {
                        String tuNumber = kgChildNode.getId().getValue(2);
                        fireMessageToLogFileWithTimeStamp(translateForLog("!!Exportiere Informationen zu TU \"%1\" (Element \"%2\", Attribut \"%3\")",
                                                                          tuNumber, ELEMENT_TU, ATTRIBUTE_TU_ID));
                        // Laufe alle Stücklisten durch und exportiere jede einzelne
                        for (iPartsCatalogNode assemblyNode : kgChildNode.getChildren()) {
                            if (!isRunning()) {
                                return;
                            }
                            if (assemblyNode.isAssemblyId()) {
                                AssemblyId assemblyId = (AssemblyId)assemblyNode.getId();
                                EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId);
                                KgTuNode kgTuNode = getKGTUNodeWithFallback((KgTuId)kgChildNode.getId());
                                EtkMultiSprache tuText = null;
                                if (kgTuNode != null) {
                                    tuText = kgTuNode.getTitle();
                                }
                                assemblyCount = exportPartListData(assembly, assemblyCount, tuNumber, tuText, kgChildNode.getId().toString(), footnotesForKG);
                            }
                        }
                    } else if (kgChildNode.isKgSaId()) {
                        String saValue = kgChildNode.getId().getValue(2);
                        if (StrUtils.isValid(saValue) && ((filter != null)) && ((validSAsForModel == null) || validSAsForModel.contains(saValue))) {
                            freeSAsInKG.add(saValue);
                        }
                    }
                }
                exportFreeSAsForModel(freeSAsInKG);
                endElement(ELEMENT_KG); // Schließe den KG Knoten, sofern er geöffnet wurde (es exitiert mind. ein TU Knoten unter der KG
            }
        }
    }

    /**
     * Exportiert die Stücklistenpositionen samt Unterstrukturen der übergebenen Stückliste
     *
     * @param assembly
     * @param assemblyCount
     * @param tuNumber
     * @param tuText
     * @param tuNavText
     * @param footnotes
     * @return
     * @throws XMLStreamException
     */
    private int exportPartListData(EtkDataAssembly assembly, int assemblyCount, String tuNumber, EtkMultiSprache tuText,
                                   String tuNavText, Map<String, List<iPartsDataFootNoteContent>> footnotes) throws XMLStreamException {
        Map<String, String> attributes = new LinkedHashMap<>();
        // <TU>
        attributes.put(ATTRIBUTE_TU_ID, tuNumber);
        attributes.put(ATTRIBUTE_TU_MODULNUMMER, assembly.getAsId().getKVari());
        startElement(ELEMENT_TU, attributes);
        attributes.clear();

        // <BILDTAFEL>
        for (EtkDataImage image : assembly.getUnfilteredImages()) {
            if (checkFilter(image)) {
                exportImageReference(image);

                // Bilddateien exportieren
                if (exportImageFile(assembly.getAsId(), image)) {
                    nExportedPictures++;
                }
            }
        }
        exportTUText(tuText, tuNavText);
        exportPartList(assembly, footnotes);
        assemblyCount++;
        fireProgress(assemblyCount, assembliesMaxCount, "", true, true);
        endElement(ELEMENT_TU); // Schließe den TU Knoten
        return assemblyCount;
    }

    private void exportImageReference(EtkDataImage image) throws XMLStreamException {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put(ATTRIBUTE_BILD_ID, image.getImagePoolNo());
        setOptionalAttribute(ATTRIBUTE_BILD_REV_ID, image.getImagePoolVer(), attributes);

        // Den Typ (=Navigationsperspektive) setzen
        if (exportTask.isExportVisualNav()) {
            String naviPerspective = image.getFieldValue(iPartsConst.FIELD_I_NAVIGATION_PERSPECTIVE);
            setOptionalAttribute(ATTRIBUTE_TYPE, naviPerspective, attributes);
        }

        boolean hasSAAValidity = !image.getFieldValueAsArray(FIELD_I_SAA_CONSTKIT_VALIDITY).isEmpty();
        String code = getCodeFromImage(image);
        boolean hasCode = !DaimlerCodes.isEmptyCodeString(code);
        boolean hasPSKVariants = isPSKSpecialCase() && !image.getFieldValueAsArray(FIELD_I_PSK_VARIANT_VALIDITY).isEmpty();
        if (!hasSAAValidity && !hasCode && !hasPSKVariants) {
            writeEnclosedElement(ELEMENT_BILDTAFEL, "", attributes, true);
        } else {
            startElement(ELEMENT_BILDTAFEL, "", attributes);
            // <KOMPONENTEN_ID>
            writeValidity(ELEMENT_KOMPONENTEN_ID, image, FIELD_I_SAA_CONSTKIT_VALIDITY, validSaasForModel, existingSaasInAssemblies, true);
            // <CODEBEDINGUNG>
            if (hasCode) {
                addCodeString(code);
                writeEnclosedElement(ELEMENT_CODEBEDINGUNG, code, null, true);
            }
            // PSK Varianten am Bild ausgeben, wenn es sich um den speziellen Fall handelt
            if (isPSKSpecialCase()) {
                // <PSK_VARIANTS>
                writeValidity(ELEMENT_PSK_VARIANT, image, FIELD_I_PSK_VARIANT_VALIDITY, null, null, true);
            }
            endElement(ELEMENT_BILDTAFEL);
        }
    }

    /**
     * Liefert die Code für die übergebene Bildtafel mit einer Anreicherung der Ereignis-Code
     *
     * @param image
     * @return
     */
    private String getCodeFromImage(EtkDataImage image) {
        String code = image.getFieldValue(FIELD_I_CODES);
        // Überprüfen, ob die Zeichnungsreferenz Ereignisse hat. Falls ja, Code anhängen.
        // Beim Export von freien SAs wird das Produkt nicht bestimmt, und ist hier null
        // In diesem Fall macht die Prüfung ob die Baureihe Event gesteuert ist keinen Sinn -> immer die Code ohne Ereignisse ausgeben
        if ((selectedProduct != null) && (selectedProduct.getReferencedSeries() != null)) {
            iPartsDialogSeries series = iPartsDialogSeries.getInstance(getProject(), selectedProduct.getReferencedSeries());
            if (series.isEventTriggered()) {
                String eventFromId = image.getFieldValue(FIELD_I_EVENT_FROM);
                String eventToId = image.getFieldValue(FIELD_I_EVENT_TO);
                code = DaimlerCodes.addEventsCodes(code, series.getEvent(eventFromId), series.getEvent(eventToId), getProject());
            }
        }
        return code;
    }

    /**
     * Exportiert die übergebene Zeichnung zur übergebenen Stückliste
     *
     * @param assemblyId
     * @param image
     * @return
     */
    private boolean exportImageFile(AssemblyId assemblyId, EtkDataImage image) {
        iPartsExportPictureFormat exportFormat = exportTask.getPictureFormat();
        if (exportFormat == iPartsExportPictureFormat.NONE) {
            return false;
        }
        String imageUsage;
        // Check, ob der Benutzer die SVG Zeichnung haben möchte
        if ((exportFormat == iPartsExportPictureFormat.PREFER_SVG) || (exportFormat == iPartsExportPictureFormat.PNG_AND_SVG)) {
            // Laut Benutzer soll die SVG Zeichnung ausgegeben werden
            imageUsage = EtkDataImage.IMAGE_USAGE_SVG;
            // Check, ob es ein Produkt gibt (SAs haben kein Produkt) und falls ja, ob SVGs exportiert werden dürfen
            if (selectedProduct != null) {
                if (!selectedProduct.isUseSVGs()) {
                    // Auch wenn der Benutzer die SVG Zeichnung haben möchte, wenn das Produkt es nicht erlaubt, wird
                    // nur PNG ausgegeben
                    imageUsage = EtkDataImage.IMAGE_USAGE_2D_FILLED; // Explizit nur 2D ohne SVG
                }
            }
        } else {
            // PNG wurde explizit ausgewählt (SVG wird nicht benötigt)
            imageUsage = EtkDataImage.IMAGE_USAGE_2D_FILLED;
        }

        // Das Bild zur Usage bestimmen
        EtkDataPool imageObject = image.getBestImageVariant(getProject().getDBLanguage(), imageUsage);
        // Existiert das Bild nicht in der DB (nur die Referenz) dann kann auch keins exportiert werden
        if (imageObject == null) {
            fireMessageToLogFileWithTimeStamp(translateForLog("!!Zur Bildnummer \"%1\" existiert kein Bild in der DB", image.getImagePoolNo()));
            return false;
        }

        // Unterverzeichnis für Bilder anlegen, falls es noch nicht existiert
        if (!checkMainPicFolderExists()) {
            return false;
        }

        // Modulverzeichnis für Bilder anlegen
        DWFile assemblyImageFolder = createAssemblyImageFolder(assemblyId);
        if (assemblyImageFolder == null) {
            return false;
        }

        // Bild exportieren
        boolean result = exportFoundImage(imageObject, image, assemblyImageFolder);
        // Check, ob beide Formate exportiert werden sollen und ob das exportierte Bild eine SVG Zeichnung war. Falls ja,
        // muss noch die PNG Zeichnung exportiert werden
        if ((exportFormat == iPartsExportPictureFormat.PNG_AND_SVG) && (ImageVariant.usageToImageVariant(imageObject.getAsId().getPUsage()) == ImageVariant.ivSVG)) {
            imageObject = image.getBestImageVariant(getProject().getDBLanguage(), EtkDataImage.IMAGE_USAGE_2D_FILLED);
            result |= exportFoundImage(imageObject, image, assemblyImageFolder);
        }
        return result;
    }

    /**
     * Erzeugt das Bildverzeichnis für die Bildtafeln des übergebenen Moduls
     *
     * @param assemblyId
     * @return
     */
    private DWFile createAssemblyImageFolder(AssemblyId assemblyId) {
        String assembly = assemblyId.getKVari();
        DWFile assemblyImageFolder = imageDirectory.getChild(assembly);
        if (!assemblyImageFolder.exists(1000)) {
            if (!assemblyImageFolder.mkDirsWithRepeat()) {
                // Wenn es ein Problem gab, dann rausspringen und beim nächsten versuchen
                fireWarningToLogFile(translateForLog("!!Das Bildverzeichnis für die Stückliste \"%1\" konnte nicht angelegt werden.", assemblyId.getKVari()));
                return null;
            }
        }
        return assemblyImageFolder;
    }

    /**
     * Erzeugt das Unterverzeichnis für die Zeichnungen im Export
     *
     * @return
     */
    private boolean checkMainPicFolderExists() {
        if (imageDirectory == null) {
            imageDirectory = getExportFile().getParentDWFile().getChild(EXPORT_IMAGES_SUBFOLDER);
            if (!imageDirectory.mkDirsWithRepeat()) {
                // Wenn es ein Problem gab, dann rausspringen und beim nächsten versuchen
                fireWarningToLogFile(translateForLog("!!Das Unterverzeichnis für die Bilder konnte nicht angelegt werden."));
                imageDirectory = null;
                return false;
            }
        }
        return true;
    }

    /**
     * Exportiert die übergebene Bildtafel
     *
     * @param imageObject
     * @param image
     * @param assemblyImageFolder
     * @return
     */
    private boolean exportFoundImage(EtkDataPool imageObject, EtkDataImage image, DWFile assemblyImageFolder) {
        String extension = imageObject.getFieldValue(FIELD_P_IMGTYPE).toLowerCase();
        byte[] imageBytes = imageObject.getImgBytes();
        if (StrUtils.isEmpty(extension)) {
            ImageInformation imageInfo = FrameworkImageUtils.getImageInformation(imageBytes);
            extension = imageInfo.getDefaultExtension();
        }
        String filename = image.getImagePoolNo();
        if (!extension.equals(ImageInformation.EXTENSION_UNKNOWN)) {
            filename += "." + extension;
        } else {
            fireMessageToLogFileWithTimeStamp(translateForLog("!!Für das Bild mit der Bildnummer \"%1\" konnte keine geeignete " +
                                                              "Dateiendung bestimmt werden. Bild wird nicht exportiert.", filename));
            return false;
        }

        DWFile imageFile = assemblyImageFolder.getChild(filename);
        if (imageFile.saveByteArray(imageBytes)) {
            ImageVariant imageVariant = ImageVariant.usageToImageVariant(imageObject.getAsId().getPUsage());
            if (imageVariant != ImageVariant.ivSVG) {
                // SEN-Datei schreiben, wenn es keine SVG Datei ist
                DWFile senFile = imageFile.getParentDWFile().getChild(imageFile.extractFileName(false) + "." + MimeTypes.EXTENSION_SEN);
                imageObject.getHotspotDataObjectList().writeHotspotToSenFile(senFile, getMessageLog());
            }
        } else {
            fireWarningToLogFile(translateForLog("!!Fehler beim Anlegen der Bilddatei mit der Bildnummer \"%1\". " +
                                                 "Die dazugehörige SEN Datei wird übersprungen!", filename));
            return false;
        }
        return true;
    }

    /**
     * Filtert das übergebene {@link EtkDataObject} Objekt abhängig vom Exporttyp
     *
     * @param dataObject
     * @return
     */
    private boolean checkFilter(EtkDataObject dataObject) {
        if (iPartsModelId.TYPE.equals(exportTask.getExportContent().getAsId().getDataObjectType())) {
            return filter.checkFilter(dataObject);
        }
        return true;
    }

    /**
     * Exportiert die TU Benennung
     *
     * @param tuText
     * @param tuNavText
     * @throws XMLStreamException
     */
    private void exportTUText(EtkMultiSprache tuText, String tuNavText) throws XMLStreamException {
        switch (exportTask.getExportContent().getAsId().getDataObjectType()) {
            case iPartsModelId.TYPE:
                writeKgTuNodeTexts(tuText, translateForLog("!!Der vorhandene KG/TU-Knoten %1 enthält keine Benennung",
                                                           tuNavText));
                break;
            case iPartsSaId.TYPE:
                writeKgTuNodeTexts(tuText, translateForLog("!!Benennung für SA Stückliste konnte nicht ausgegeben werden"));
                break;
        }
    }

    /**
     * Exportiert die während einem Baumuster-Export aufgesammelten freien SAs
     *
     * @param freeSAsInKG
     * @throws XMLStreamException
     */
    private void exportFreeSAsForModel(Set<String> freeSAsInKG) throws XMLStreamException {
        if ((freeSAsInKG == null) || freeSAsInKG.isEmpty()) {
            return;
        }
        startElement(ELEMENT_BAUMUSTER_KG_SA);
        Map<String, String> attributes = new LinkedHashMap<>();
        for (String saValue : freeSAsInKG) {
            if (!isRunning()) {
                return;
            }
            attributes.put(ATTRIBUTE_SA_IDENT, saValue);
            writeEmptyElement(ELEMENT_KG_SA, attributes, false);
            attributes.clear();
        }
        endElement(ELEMENT_BAUMUSTER_KG_SA);
    }

    /**
     * Schreibt die KG- bzw. TU-Benennungen in die XML
     *
     * @param kgTuNode
     * @param catalogNode
     * @throws XMLStreamException
     */
    private void writeKgTuNodeTexts(KgTuNode kgTuNode, iPartsCatalogNode catalogNode) throws XMLStreamException {
        if (kgTuNode != null) {
            EtkMultiSprache desc = kgTuNode.getTitle();
            // <TEXTE> für TU oder KG
            String errorMessage = translateForLog("!!Der vorhandene KG/TU-Knoten %1 enthält keine Benennung",
                                                  catalogNode.getId().toString());
            writeKgTuNodeTexts(desc, errorMessage);
        } else {
            fireWarningToLogFile(translateForLog("!!Der KG/TU-Knoten %1 enthält keine Benennung, da weder eine " +
                                                 "produktspezifische noch allgemeine Benennung existiert.",
                                                 catalogNode.getId().toString()));
            warningCount++;
        }
    }

    private void writeKgTuNodeTexts(EtkMultiSprache desc, String errorMessage) throws XMLStreamException {
        if (desc != null) {
            // <TEXTE> für TU oder KG
            writeMultiLangEnclosedText(desc, errorMessage);
        }
    }

    /**
     * Lädt alle Fußnotentexte zum übergebenen Suchstring. Hierbei wird in der Tabelle "DA_FN_KATALOG_REF" nach allen
     * Fußnoten-IDs für die KG im aktuellen Produkt gesucht. Mit den gefundenen Fußnoten-IDs werden die dazugehörigen
     * Texte aus der Tabelle DA_FN_CONTENT geladen.
     *
     * @param moduleSearchTermForFootnotes
     * @return
     */
    private Map<String, List<iPartsDataFootNoteContent>> getFootnoteTextsForModuleSearchTerm(String moduleSearchTermForFootnotes) {
        // 1. Bestimme alle Fußnoten-IDs für die KG im aktuellen Produkt
        Set<String> allFootnoteIds = new HashSet<>();
        boolean likeQuery = StrUtils.stringEndsWith(moduleSearchTermForFootnotes, '*', false);
        String[] selectFields = new String[]{ FIELD_DFNK_FNID };
        String[] whereFields = new String[]{ FIELD_DFNK_MODULE };
        String[] whereValues = new String[]{ moduleSearchTermForFootnotes };
        DBDataObjectAttributesList allIdsFromDB = getProject().getDbLayer().getAttributesList(TABLE_DA_FN_KATALOG_REF,
                                                                                              selectFields, whereFields,
                                                                                              whereValues, ExtendedDataTypeLoadType.NONE,
                                                                                              likeQuery, true);
        for (DBDataObjectAttributes idFromDB : allIdsFromDB) {
            allFootnoteIds.add(idFromDB.getFieldValue(FIELD_DFNK_FNID));
        }

        // 2. Lade alle Fußnoten für alle DB-Sprachen zu den gefundenen IDs
        return createFootnoteMap(allFootnoteIds);
    }

    /**
     * Erzeugt eine Map mit Fußnoten-Id auf alle Fußnotentexte zur Fußnoten-Id
     *
     * @param allFootnoteIds
     * @return
     */
    private Map<String, List<iPartsDataFootNoteContent>> createFootnoteMap(Set<String> allFootnoteIds) {
        Map<String, List<iPartsDataFootNoteContent>> result = new HashMap<>();
        if (!allFootnoteIds.isEmpty()) {
            iPartsDataFootNoteContentList allFootnotes = iPartsDataFootNoteContentList.loadFootNoteContentsForIds(getProject(),
                                                                                                                  allFootnoteIds);
            // Key: Fußnoten-ID ohne Zeilenangabe
            // Value: Liste aller Fußnotentexte zur Fußnoten-ID
            for (iPartsDataFootNoteContent footnoteContent : allFootnotes) {
                String footnoteId = footnoteContent.getAsId().getFootNoteId();
                List<iPartsDataFootNoteContent> footnotesForId = result.get(footnoteId);
                if (footnotesForId == null) {
                    footnotesForId = new DwList<>();
                    result.put(footnoteId, footnotesForId);
                }
                footnotesForId.add(footnoteContent);
            }
        }
        return result;
    }

    /**
     * Startet das XML Element für die Konstruktions-Gruppe. Ob ein KG Element geöffnet wird, hängt davon ab, ob die KG
     * mind. einen TU Knoten besitzt.
     *
     * @param kgNode
     * @throws XMLStreamException
     */
    private void startKGTUElement(iPartsCatalogNode kgNode) throws XMLStreamException {
        if (kgNode.isKgTuId()) {
            // <KG>
            startElement(ELEMENT_KG);
            // <KG_ID>
            String kgNumber = kgNode.getId().getValue(1);
            fireMessageToLogFileWithTimeStamp(translateForLog("!!Exportiere Informationen zu KG \"%1\" (Element \"%2\")", kgNumber, ELEMENT_KG_ID));
            writeEnclosedElement(ELEMENT_KG_ID, kgNumber, null, false);
            KgTuNode kgTuNode = getKGTUNodeWithFallback((KgTuId)kgNode.getId());
            writeKgTuNodeTexts(kgTuNode, kgNode);
        }
    }

    /**
     * Liefert zu einer {@link KgTuId} den passenden KGTU Knoten. Erst wird nach einem produktspezifische KGTU Knoten
     * gesucht und falls keiner existiert, wird in der Liste der produktspezifschen KGTU Templates nach einem allgemeinen Knoten gesucht.
     *
     * @param kgTuId
     * @return
     */
    public KgTuNode getKGTUNodeWithFallback(KgTuId kgTuId) {
        KgTuNode kgTuNode = productStructures.getKgTuNode(getProject(), kgTuId);
        if (kgTuNode == null) {
            Map<String, KgTuTemplate> kgTuTemplateMap = KgTuTemplate.getInstance(selectedProduct.getAsId(), getProject());
            for (KgTuTemplate kgTuTemplate : kgTuTemplateMap.values()) {
                kgTuNode = kgTuTemplate.getNode(kgTuId);
                if (kgTuNode != null) {
                    return kgTuNode;
                }
            }
        }
        return kgTuNode;
    }

    /**
     * Schreibt die Verbidnungs-SA in die Export-Datei
     *
     * @param saaForExport
     * @throws XMLStreamException
     */
    private void writeConnectedSAs(ExportSaa saaForExport) throws XMLStreamException {
        List<String> connectedSas = saaForExport.getConnectedSasAsList();
        if (!connectedSas.isEmpty()) {
            Map<String, String> attributes = new HashMap<>();
            // <VERBINDUNG_SAS> Gruppierung Verbindungs-SAs
            startElement(ELEMENT_VERBINDUNGS_SAS);
            for (String connectedSa : connectedSas) {
                attributes.put(ATTRIBUTE_SA_IDENT, connectedSa);
                // <VERBINDUNG_SA> Verbindungs-SAs
                writeEmptyElement(ELEMENT_VERBINDUNGS_SA, attributes, false);
                attributes.clear();
            }
            endElement(ELEMENT_VERBINDUNGS_SAS);
        }
    }

    /**
     * Schreibt Gültigkeiten, falls vorhanden.
     *
     * @param xmlElement              Das Element für die XML Datei
     * @param iPartsDataPartListEntry Stücklistenposition mit den Gültigkeiten
     * @param fieldName               Feldname für die Gültigkeiten
     * @param validValues             Set mit validen Gültigkeiten, falls man beim Export Gültigkeiten filtern möchte
     * @param existingValues          Set mit Gültigkeiten, das mit den real existierenden Gültigkeiten gefüllt wird
     * @param writeXMLElement         Flag, ob das XML-Element geschrieben werden soll
     * @throws XMLStreamException
     */
    private void writeValidity(String xmlElement, EtkDataObject iPartsDataPartListEntry, String fieldName,
                               Set<String> validValues, Set<String> existingValues, boolean writeXMLElement) throws XMLStreamException {
        EtkDataArray validity = iPartsDataPartListEntry.getFieldValueAsArray(fieldName);
        if (!validity.isEmpty()) {
            for (String validitySingleValue : validity.getArrayAsStringList()) {
                if (validValues != null) {
                    if (!validValues.contains(validitySingleValue)) {
                        continue;
                    }
                }
                if (existingValues != null) {
                    existingValues.add(validitySingleValue);
                }

                if (writeXMLElement) {
                    writeEnclosedElement(xmlElement, validitySingleValue, null, true);
                }
            }
        }
    }

    /**
     * Schreibt mehrere Text-XML-Elemente in das übergebene, umschließende XML Element. Pro Sprache wird ein Element erzeugt.
     *
     * @param enclosingElement
     * @param multiLangText
     * @param logText
     * @throws XMLStreamException
     */
    private void writeMultiLangTagAndEnclosedText(String enclosingElement, EtkMultiSprache multiLangText, String logText) throws XMLStreamException {
        writeMultiLangWithSpecificEnclosedElements(enclosingElement, ELEMENT_TEXTE, multiLangText, logText);
    }

    /**
     * Schreibt den übergebenen Text in das übergebene <code>textElement</code> - XML Element und packt diese Texte in
     * das übergebene, umschließende <code>enclosingElement</code> - XML Element.
     *
     * @param enclosingElement
     * @param textElement
     * @param multiLangText
     * @param logText
     * @throws XMLStreamException
     */
    private void writeMultiLangWithSpecificEnclosedElements(String enclosingElement, String textElement,
                                                            EtkMultiSprache multiLangText, String logText) throws XMLStreamException {
        if ((multiLangText != null) && !multiLangText.allStringsAreEmpty()) {
            startElement(enclosingElement);
            writeMultiLangWithSpecificElement(multiLangText, textElement, logText);
            endElement(enclosingElement);
        }
    }

    /**
     * Schreibt mehrere Text-XML-Elemente. Pro Sprache wird ein Element erzeugt.
     *
     * @param multiLangText
     * @param logText
     * @throws XMLStreamException
     */
    private void writeMultiLangEnclosedText(EtkMultiSprache multiLangText, String logText) throws XMLStreamException {
        writeMultiLangWithSpecificElement(multiLangText, ELEMENT_TEXTE, logText);
    }

    /**
     * Schreibt mehrere Texte in das übergebene <code>xmlElementForText</code>-XML-Elemente. Pro Sprache wird ein Element erzeugt.
     *
     * @param multiLangText
     * @param xmlElementForText
     * @param logText
     * @throws XMLStreamException
     */
    private void writeMultiLangWithSpecificElement(EtkMultiSprache multiLangText, String xmlElementForText,
                                                   String logText) throws XMLStreamException {
        if (multiLangText != null) {
            Map<String, String> attributes = new LinkedHashMap<>();
            // DAIMLER-7977
            for (Language lang : exportTask.getSelectedLanguages()) {
                attributes.put(ATTRIBUTE_SPRACH_ID, lang.getCode());
                setOptionalAttribute(ATTRIBUTE_BENENNUNG, getLangText(multiLangText, lang), attributes);
                writeEnclosedElement(xmlElementForText, "", attributes, true);
                attributes.clear();
            }
        } else {
            if (StrUtils.isValid(logText)) {
                fireMessage(logText, MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP, MessageLogOption.WRITE_ONLY_IN_LOG_FILE);
                warningCount++;
            }
        }
    }

    /**
     * Exportiert die Stücklisten des übergebenen Moduls
     *
     * @param assembly
     * @param footnotesForKG
     * @throws XMLStreamException
     */
    private void exportPartList(EtkDataAssembly assembly, Map<String, List<iPartsDataFootNoteContent>> footnotesForKG) throws XMLStreamException {
        if (!isRunning()) {
            return;
        }
        Map<String, String> attributes = new LinkedHashMap<>();
        // <TEILEPOSITION>
        fireMessageWithTimeStamp(translateForLog("!!Exportiere Stückliste \"%1\"", assembly.getAsId().getKVari()));
        if (assembly instanceof iPartsDataAssembly) {
            iPartsDataAssembly iPartsDataAssembly = (iPartsDataAssembly)assembly;
            iPartsDataAssembly.clearFilteredPartLists();
            initCombTextMap(iPartsDataAssembly.getAsId());
            int partListEntryCounter = 0;
            isModuleSpringRelevant = iPartsDataAssembly.getModuleMetaData().isSpringFilterRelevant();
            boolean isCarNavigationModule = exportTask.isExportVisualNav() && EditModuleHelper.isCarPerspectiveAssembly(iPartsDataAssembly);
            iPartsHashHelper hashHelper = iPartsHashHelper.getInstance();

            // Stückliste zunächst vollständig filtern und dann exportieren (u.a. damit gemappte Gleichteile-Teilenummern
            // überall korrekt gesetzt sind)
            List<iPartsDataPartListEntry> filteredPartList = new ArrayList<>();
            for (EtkDataPartListEntry partListEntry : iPartsDataAssembly.getPartListUnfiltered(getFieldsForPartListLoad())) {
                if (!isRunning()) {
                    return;
                }
                if (partListEntry instanceof iPartsDataPartListEntry) {
                    iPartsDataPartListEntry iPartsDataPartListEntry = (iPartsDataPartListEntry)partListEntry;

                    // Das virtuelle Feld für die um AS-/Zubehör-Code reduzierte Code-Regel muss basierend auf dem lokalen
                    // Filter für den Export neu berechnet werden -> auch das darauf basierende virtuelle Feld RETAIL_CODES_WITH_EVENTS
                    // entfernen, damit es neu berechnet wird
                    iPartsDataPartListEntry.calculateRetailCodesReducedAndFiltered(filter);
                    iPartsDataPartListEntry.getAttributes().remove(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_WITH_EVENTS);

                    if (checkFilter(iPartsDataPartListEntry)) {
                        filteredPartList.add(iPartsDataPartListEntry);
                    }
                }
            }

            iPartsPRIMUSReplacementsCache primusReplacementsCache = iPartsPRIMUSReplacementsCache.getInstance(getProject());
            boolean isPKWDocumentationType = iPartsDataAssembly.getDocumentationType().isPKWDocumentationType();
            Map<iPartsProductId, iPartsProductStructures> productStructures = isCarNavigationModule ? new HashMap<>() : null;
            Set<String> datacardModelNumbers = isCarNavigationModule && (filter != null) ? filter.getCurrentDataCard().getFilterModelNumbers(getProject()) : null;
            // Map mit allen EinPAS Informationen, sofern die EinPAS Infos ausgegeben werden sollen
            Map<PartListEntryId, List<iPartsDataModuleCemat>> einPasDataMap = exportTask.isExportEinPASData() ? iPartsDataModuleCematList.loadCematMapForModule(getProject(), assembly.getAsId()) : null;
            for (iPartsDataPartListEntry iPartsPartListEntry : filteredPartList) {
                if (!isRunning()) {
                    return;
                }
                iPartsDialogBCTEPrimaryKey bctePrimaryKey = iPartsDialogBCTEPrimaryKey.createFromDialogGuid(iPartsPartListEntry.getFieldValue(iPartsConst.FIELD_K_SOURCE_GUID));
                String groupNumber = hashHelper.getHashValueForBCTEPrimaryKey(bctePrimaryKey, groupNumbers);
                EtkDataPart part = iPartsPartListEntry.getPart();
                if (part != null) {
                    checkWireHarnessPart(iPartsPartListEntry);
                    setOptionalAttribute(ATTRIBUTE_BILD_ID, iPartsPartListEntry.getFieldValue(FIELD_K_POS), attributes);
                    setOptionalAttribute(ATTRIBUTE_MENGE, iPartsPartListEntry.getFieldValue(FIELD_K_MENGE), attributes);
                    setOptionalAttribute(ATTRIBUTE_LENKUNG, iPartsPartListEntry.getFieldValue(FIELD_K_STEERING), attributes);
                    setOptionalAttribute(ATTRIBUTE_GETRIEBE, iPartsPartListEntry.getFieldValue(FIELD_K_GEARBOX_TYPE), attributes);
                    setOptionalAttribute(ATTRIBUTE_STRUKTURSTUFE, iPartsPartListEntry.getFieldValue(FIELD_K_HIERARCHY), attributes);
                    if (isPKWDocumentationType) {
                        setOptionalAttribute(ATTRIBUTE_GENERIC_LOCATION, iPartsPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DIALOG_DD_GENVO),
                                             attributes);
                    }
                    attributes.put(ATTRIBUTE_LAUFENDENUMMER, iPartsPartListEntry.getAsId().getKLfdnr());

                    // Attribute des Ziel-TUs in einem Fahrzeug-Navigations-Modul setzen
                    List<EtkDataImage> subAssemblyImages = null;
                    if (isCarNavigationModule) {
                        subAssemblyImages = addCarNavigationModuleAttributes(iPartsPartListEntry, productStructures, datacardModelNumbers,
                                                                             attributes);
                    }

                    startElement(ELEMENT_TEILEPOSITION, attributes);
                    attributes.clear();

                    // Zeichnungen des Ziel-TUs in einem Fahrzeug-Navigations-Modul
                    if (Utils.isValid(subAssemblyImages)) {
                        for (EtkDataImage image : subAssemblyImages) {
                            if (checkFilter(image)) {
                                exportImageReference(image);
                            }
                        }
                    }

                    // <CODEBEDINGUNG>
                    String code = iPartsPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.RETAIL_CODES_WITH_EVENTS);
                    addCodeString(code);
                    writeEnclosedElement(ELEMENT_CODEBEDINGUNG, code, null, true);
                    // <PRODUKTGRUPPE>
                    writeEnclosedElement(ELEMENT_PRODUKTGRUPPE, iPartsPartListEntry.getFieldValue(FIELD_K_PRODUCT_GRP), null, true);
                    // <KOMPONENTEN_ID>
                    writeValidity(ELEMENT_KOMPONENTEN_ID, iPartsPartListEntry, FIELD_K_SA_VALIDITY, validSaasForModel,
                                  existingSaasInAssemblies, true);
                    // PSK Varianten an der Teileposition ausgeben, wenn es sich um den speziellen Fall handelt
                    if (isPSKSpecialCase()) {
                        // <PSK_VARIANTS>
                        writeValidity(ELEMENT_PSK_VARIANT, iPartsPartListEntry, FIELD_K_PSK_VARIANT_VALIDITY, null, null, true);
                    }
                    // <AUSFUEHRUNGSART>
                    writeEnclosedElement(ELEMENT_AUSFUEHRUNGSART, iPartsPartListEntry.getFieldValue(FIELD_K_AA), null, true);
                    // <TEILEPOSITIONS_ID>
                    partListEntryCounter++;
                    writeEnclosedElement(ELEMENT_TEILEPOSITIONS_ID, String.valueOf(partListEntryCounter), null, false);
                    // <WAHLWEISE_DATEN>
                    Collection<EtkDataPartListEntry> wwParts = iPartsWWPartsHelper.getWWPartsForExport(iPartsPartListEntry, filter);
                    if (!wwParts.isEmpty()) {
                        startElement(ELEMENT_WAHLWEISE_DATEN);
                        for (EtkDataPartListEntry wwPartListEntry : wwParts) {
                            if (wwPartListEntry instanceof iPartsDataPartListEntry) {
                                writeEnclosedPartElement((iPartsDataPartListEntry)wwPartListEntry, false);
                            }
                        }
                        endElement(ELEMENT_WAHLWEISE_DATEN);
                    }
                    // <REPLACE_DATEN>
                    if (iPartsPartListEntry.hasSuccessors(filter)) {
                        startElement(ELEMENT_REPLACE_DATEN);
                        for (iPartsReplacement successor : iPartsPartListEntry.getSuccessors(filter)) {
                            if (StrUtils.isValid(successor.successorPartNumber)) {
                                iPartsDataPartListEntry successorPartListEntry = (iPartsDataPartListEntry)successor.successorEntry;
                                // <REPLACE_TEIL>
                                startElement(ELEMENT_REPLACE_TEIL);
                                // <TEIL>
                                // Im AS gibt es den Fall, dass ein Nachfolger nicht in der Stückliste existiert
                                // und wir dadurch nur die Teilenummer haben und nicht den Stücklisteneintrag.
                                // In diesem Fall wir ein temporäres EtkDataPart Objekt angelegt und übergeben.
                                if (successorPartListEntry != null) {
                                    writeEnclosedPartElement(successorPartListEntry, false);
                                } else {
                                    // Alle Daten zur  gemappten Teilenummer ausgaben (ist laut Daimler OK und erspart
                                    // das nachträgliche Setzen der gemappten Teilenummer inkl. ES1 und ES2 für das
                                    // Originalteil aus dem Cache)
                                    EtkDataPart replaceDataPart = EtkDataObjectFactory.createDataPart(getProject(), new iPartsPartId(successor.successorMappedPartNumber, ""));
                                    writeEnclosedPartElement(replaceDataPart);
                                }
                                if (successor.hasIncludeParts(getProject())) {
                                    // <MITLIEFER_DATEN>
                                    startElement(ELEMENT_MITLIEFER_DATEN);
                                    for (iPartsReplacement.IncludePart includePart : successor.getIncludeParts(getProject())) {
                                        startElement(ELEMENT_MITLIEFER_TEIL);
                                        EtkDataPart supplementPart = EtkDataObjectFactory.createDataPart(getProject(), new iPartsPartId(includePart.partNumber, ""));
                                        // <TEIL>
                                        writeEnclosedPartElement(supplementPart);
                                        // <MITLIEFERMENGE>
                                        writeEnclosedElement(ELEMENT_MITLIEFERMENGE, includePart.quantity, null, false);
                                        endElement(ELEMENT_MITLIEFER_TEIL);
                                    }
                                    endElement(ELEMENT_MITLIEFER_DATEN);
                                }

                                // <ALTERNATIVTEIL_DATEN> bei PRIMUS-Ersetzungen
                                if ((successor.source == iPartsReplacement.Source.PRIMUS) && iPartsPlugin.isShowAlternativePartsForPRIMUS()) {
                                    Set<EtkDataPart> alternativePartsList = primusReplacementsCache.getAlternativeParts(successor.successorPartNumber,
                                                                                                                        filter, getProject());
                                    if (alternativePartsList != null) {
                                        startElement(ELEMENT_ALTERNATIVE_PART_DATA);
                                        for (EtkDataPart alternativePart : alternativePartsList) {
                                            writeEnclosedPartElement(alternativePart);
                                        }
                                        endElement(ELEMENT_ALTERNATIVE_PART_DATA);
                                    }
                                }

                                endElement(ELEMENT_REPLACE_TEIL);
                            }
                        }
                        endElement(ELEMENT_REPLACE_DATEN);
                    }
                    // <TEIL>
                    writeEnclosedPartElement(iPartsPartListEntry, true);

                    Set<EtkDataPart> alternativeParts = iPartsPartListEntry.getAlternativeParts(false, false); // Ohne Alternativteile vom Typ 02
                    // <ALTERNATIVTEIL_DATEN>
                    if ((alternativeParts != null) && !alternativeParts.isEmpty()) {
                        startElement(ELEMENT_ALTERNATIVE_PART_DATA);
                        for (EtkDataPart alternativePart : alternativeParts) {
                            writeEnclosedPartElement(alternativePart);
                        }
                        endElement(ELEMENT_ALTERNATIVE_PART_DATA);
                    }
                    // <FUSSNOTE_DATEN>
                    if (iPartsPartListEntry.hasFootNotes()) {
                        Collection<iPartsFootNote> retailFootnotes = iPartsPartListEntry.getFootNotesForRetail();
                        // Check, ob Retail-Fußnoten vorhanden sind. Falls ja, ausgeben
                        if (!retailFootnotes.isEmpty()) {
                            startElement(ELEMENT_FUSSNOTE_DATEN);
                            for (iPartsFootNote footnote : retailFootnotes) {
                                writeEnclosedFootnote(footnote.getFootNoteId().getFootNoteId(), footnotesForKG);
                            }
                            endElement(ELEMENT_FUSSNOTE_DATEN);
                        }
                    }
                    // <KOMBITEXT_DATEN>
                    EtkMultiSprache combinedRetailText = combTextForKlfdNr.get(iPartsPartListEntry.getAsId().getKLfdnr());
                    writeCombinedText(combinedRetailText, part);

                    // <PLANT_INFORMATION_DATEN>
                    // sind Werkseinsatzdaten vorhanden?
                    if (iPartsPartListEntry.getFactoryDataValidity() == iPartsFactoryData.ValidityType.VALID) {
                        iPartsFactoryData retailFactoryData = iPartsPartListEntry.getFactoryDataForRetail();
                        iPartsFactoryModel factoryModel = iPartsFactoryModel.getInstance(getProject());
                        boolean started = false;
                        for (Map.Entry<String, List<iPartsFactoryData.DataForFactory>> factoryEntry : retailFactoryData.getFactoryDataMap().entrySet()) {
                            for (iPartsFactoryData.DataForFactory dataForFactory : factoryEntry.getValue()) {
                                addCodeString(dataForFactory.stCodeFrom);
                                addCodeString(dataForFactory.stCodeTo);

                                // Werkskennbuchstaben zu Werksnummer ermitteln aus Cache
                                String factorySigns = factoryModel.getFactorySignsStringForFactoryNumberAndSeries(factoryEntry.getKey(),
                                                                                                                  new iPartsSeriesId(dataForFactory.seriesNumber),
                                                                                                                  getAggregateType());
                                if ((dataForFactory.eldasFootNoteId != null) && !dataForFactory.eldasFootNoteId.trim().isEmpty()) {
                                    // Es handelt sich um eine ELDAS Fußnote, also wird nur der Text gesetzt
                                    if (!started) {
                                        startElement(ELEMENT_PLANT_INFORMATION_DATEN);
                                        started = true;
                                    }
                                    attributes.put(ATTRIBUTE_TYPE, PlantInformationType.UNDEFINED.getDbValue());

                                    // <PLANT_INFORMATION>
                                    startElement(ELEMENT_PLANT_INFORMATION, attributes);
                                    attributes.clear();
                                    writeEnclosedFootnote(dataForFactory.eldasFootNoteId, footnotesForKG);
                                    endElement(ELEMENT_PLANT_INFORMATION);
                                } else {
                                    // Normale Rückmeldedaten werden in From und Upto Daten aufgeteilt
                                    if (retailFactoryData.isEvalPemFrom() && dataForFactory.hasPEMFrom()) { // PEM ab relevant?
                                        started |= writePlantInformation(factorySigns, dataForFactory.identsFrom,
                                                                         PlantInformationType.FROM, dataForFactory.dateFrom,
                                                                         dataForFactory.stCodeFrom, started);


                                    }

                                    if (retailFactoryData.isEvalPemTo() && dataForFactory.hasPEMTo()) { // PEM bis relevant?
                                        started |= writePlantInformation(factorySigns, dataForFactory.identsTo,
                                                                         PlantInformationType.UPTO, dataForFactory.dateTo,
                                                                         dataForFactory.stCodeTo, started);
                                    }
                                }
                            }
                        }
                        if (started) {
                            endElement(ELEMENT_PLANT_INFORMATION_DATEN);
                        }
                    }

                    // <GROUPNUMBER>
                    writeEnclosedElement(ELEMENT_GROUPNUMBER, groupNumber, null, true);

                    // <ADDITIONAL_PART_DATEN>
                    loadAndWriteAdditionalPartInformation(iPartsPartListEntry.getPart().getAsId().getMatNr(), iPartsPartListEntry.getPart());

                    // <EINPAS_DATA>
                    writeEinPASData(iPartsPartListEntry, einPasDataMap);
                    // TEILEPOSITION schließen
                    endElement(ELEMENT_TEILEPOSITION);
                }
            }
            nExportedPartListEntries += partListEntryCounter;
            // Cache wird für jede Stückliste neu aufgebaut
            partsCache.clear();
        }
    }

    private List<EtkDataImage> addCarNavigationModuleAttributes(iPartsDataPartListEntry partListEntry, Map<iPartsProductId, iPartsProductStructures> productStructures,
                                                                Set<String> datacardModelNumbers, Map<String, String> attributes) {
        List<EtkDataImage> subAssemblyImages = null;
        AssemblyId assemblyId = partListEntry.getDestinationAssemblyId();
        if (assemblyId.isValidId()) {
            EtkDataAssembly subAssembly = EtkDataObjectFactory.createDataAssembly(getProject(), assemblyId);

            subAssemblyImages = subAssembly.getUnfilteredImages().getAsList();

            if (subAssembly instanceof iPartsDataAssembly) {
                EditModuleHelper.CarPerspectiveSubModuleData data = EditModuleHelper.getCarPerspectiveSubModuleData(getProject(),
                                                                                                                    (iPartsDataAssembly)subAssembly,
                                                                                                                    productStructures,
                                                                                                                    datacardModelNumbers);
                setOptionalAttribute(ATTRIBUTE_MODEL_ID, data.modelNumber, attributes);
                setOptionalAttribute(ATTRIBUTE_PRODUCT_ID, data.productNumber, attributes);
                setOptionalAttribute(ATTRIBUTE_CG, data.cg, attributes);
                setOptionalAttribute(ATTRIBUTE_CSG, data.csg, attributes);
            }
        }

        return subAssemblyImages;
    }

    /**
     * Schreibt die zur Stücklistenposition verknüpften EinPAS Informationen ins XML. Analog zum WebService werden nur
     * die letzten drei Versionen eines EinPAS Knotens geliefert
     *
     * @param partListEntry
     * @param einPasDataMap
     * @throws XMLStreamException
     */
    private void writeEinPASData(iPartsDataPartListEntry partListEntry, Map<PartListEntryId, List<iPartsDataModuleCemat>> einPasDataMap) throws XMLStreamException {
        if (exportTask.isExportEinPASData() && (einPasDataMap != null) && !einPasDataMap.isEmpty()) {
            String matNr = partListEntry.getPart().getAsId().getMatNr();
            List<iPartsDataModuleCemat> einPasDataForModuleList = einPasDataMap.get(partListEntry.getAsId());
            if ((einPasDataForModuleList != null) && (!einPasDataForModuleList.isEmpty())) {
                Map<EinPasId, List<String>> einPasVersionMap = iPartsDataModuleCematList.buildCematEinPasVersionsMap(einPasDataForModuleList, matNr,
                                                                                                                     iPartsDataModuleCematList.VERSIONS_LIMIT);
                if (!einPasVersionMap.isEmpty()) {
                    Map<String, String> attributes = new LinkedHashMap<>();
                    // <EINPAS_DATA>
                    startElement(ELEMENT_EINPAS_DATA);
                    for (Map.Entry<EinPasId, List<String>> idAndVersions : einPasVersionMap.entrySet()) {
                        EinPasId einPasId = idAndVersions.getKey();
                        for (String version : idAndVersions.getValue()) {
                            attributes.put(ATTRIBUTE_EINPAS_MAIN_GROUP, einPasId.getHg());
                            attributes.put(ATTRIBUTE_EINPAS_GROUP, einPasId.getG());
                            attributes.put(ATTRIBUTE_EINPAS_TECHNICAL_SCOPE, einPasId.getTu());
                            attributes.put(ATTRIBUTE_EINPAS_VERSION, version);
                            // <EINPAS_NODE>
                            writeEmptyElement(ELEMENT_EINPAS_NODE, attributes, true);
                            attributes.clear();
                        }
                    }
                    endElement(ELEMENT_EINPAS_DATA);
                }
            }
        }
    }

    /**
     * Schreibt den übergebenen kombinierten Text in die dazugehörigen XML-Elemente
     *
     * @param combinedRetailText
     * @param objectWithNeutralText
     * @throws XMLStreamException
     */
    private void writeCombinedText(EtkMultiSprache combinedRetailText, EtkDataObject objectWithNeutralText) throws XMLStreamException {
        boolean hasMultiLangText = (combinedRetailText != null) && !combinedRetailText.allStringsAreEmpty();
        // Der sprachneutrale Text vom Materialstamm
        EtkMultiSprache neutralTextOnPart = objectWithNeutralText.getFieldValueAsMultiLanguage(FIELD_M_ADDTEXT);
        boolean hasNeutralText = (neutralTextOnPart != null) && !neutralTextOnPart.allStringsAreEmpty();
        if (hasMultiLangText || hasNeutralText) {
            // <KOMBITEXT_DATEN>
            startElement(ELEMENT_KOMBITEXT_DATEN);
            Map<String, String> attributes = new LinkedHashMap<>();
            // DAIMLER-7977
            for (Language lang : exportTask.getSelectedLanguages()) {
                String language = lang.getCode();
                // Wenn kein mehrsprachiger Text existiert, dann wird nur der Text vom Materialstamm ausgegeben
                String multiLangText = hasMultiLangText ? getLangText(combinedRetailText, language) : "";
                String neutralText = hasNeutralText ? getLangText(neutralTextOnPart, language) : "";

                // Falls es für die Sprache keinen Text gibt, dann wird auch kein Tag erzeugt
                if (StrUtils.isEmpty(multiLangText, neutralText)) {
                    continue;
                }

                String combText = makeCombText(multiLangText, neutralText);
                attributes.put(ATTRIBUTE_SPRACH_ID, language);
                attributes.put(ATTRIBUTE_KOMBI_TEXT, combText);
                // <KOMBITEXT>
                writeEnclosedElement(ELEMENT_KOMBITEXT, "", attributes, false);
                attributes.clear();
            }
            endElement(ELEMENT_KOMBITEXT_DATEN);
        }
    }

    /**
     * Überprüft, ob ess sich bei der Stücklistenposition um ein Leitungssatz-Baukasten handelt. Falls ja, wird er für
     * den späteren Export in <code>wireHarnessParts</code> abgelegt.
     *
     * @param iPartsDataPartListEntry
     */
    private void checkWireHarnessPart(iPartsDataPartListEntry iPartsDataPartListEntry) {
        if ((wireHarnessParts != null) && isExportWireHarnessData()) {
            EtkDataPart dataPart = iPartsDataPartListEntry.getPart();
            // Nur Leitungssatz-BK ausgeben, die sonstige-KZ = "LA" haben
            if (iPartsWireHarnessHelper.hasValidAdditionalWireHarnessFlag(iPartsDataPartListEntry)) {
                // Falls es die vorgegebene Dummy-Sachnummer ist, check, ob eine Original-Teilenummer existiert
                if (iPartsWireHarnessHelper.isWireHarnessDummyPart(dataPart)
                    && iPartsDataPartListEntry.attributeExists(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_MAT_NR)) {
                    // Die echte Leitungssatz-BK Teilenummer bestimmen und am EtkDataPart Objekt setzen, damit die Info beim
                    // Schreiben der Teilenummer ins XML gefunden wird
                    String originalPartNr = iPartsDataPartListEntry.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_MAT_NR);
                    dataPart.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_MAT_NR, originalPartNr, true, DBActionOrigin.FROM_DB);
                    // Ist die Original-Teilenummer vorhanden, brauchen wir das Objekt nicht aus der DB zu laden
                    if (!wireHarnessParts.containsKey(originalPartNr)) {
                        // Hier jetzt das Original-Teil laden um an alle Infos zu kommen
                        EtkDataPart originalPart = EtkDataObjectFactory.createDataPart(getProject(), new iPartsPartId(originalPartNr, ""));
                        if (originalPart.existsInDB()) {
                            // Hier den Text vollständig laden, weil die Teilenummer nur im Leitungssatz-BK Cache liegt
                            // und nicht normal verarbeitet wird (der Text wird somit später in writeEnclosedPartElement()
                            // nicht bestimmt)
                            originalPart.loadMultiLanguageFromDB(dataPart.getAttribute(FIELD_M_TEXTNR));
                            addWireHarnessToCache(originalPart);
                        } else {
                            wireHarnessParts.put(originalPartNr, null);
                        }
                    }

                } else if (wireHarnessCache.isWireHarness(dataPart.getAsId())) {
                    addWireHarnessToCache(dataPart);
                }
            }
        }
    }

    private boolean isExportWireHarnessData() {
        return (selectedProduct != null) && selectedProduct.isWireHarnessDataVisible();
    }

    private void addWireHarnessToCache(EtkDataPart dataPart) {
        String partNumber = dataPart.getAsId().getMatNr();
        if (!wireHarnessParts.containsKey(partNumber)) {
            wireHarnessParts.put(partNumber, dataPart);
        }
    }

    /**
     * Liefert die Felder, die beim Laden der Stückliste mitgeladen werden sollen
     *
     * @return
     */
    private EtkEbenenDaten getFieldsForPartListLoad() {
        EtkEbenenDaten fieldsForLoad;
        if (isPSKSpecialCase()) {
            fieldsForLoad = iPartsXMLExportLoadingFields.getAllFieldsForExport();
        } else {
            fieldsForLoad = iPartsXMLExportLoadingFields.getPartListTypeForExport();
        }
        if (exportTask.isExportAdditionalPartData()) {
            fieldsForLoad.addFelder(iPartsXMLExportLoadingFields.getAdditionalPartInformationFields());
        }
        return fieldsForLoad;
    }

    /**
     * Liefert die Aggregateart abhängig vom Export-Typ
     *
     * @return
     */
    private String getAggregateType() {
        if (iPartsModelId.TYPE.equals(exportTask.getExportContent().getAsId().getDataObjectType())) {
            return selectedProduct.getAggregateType();
        }
        return "";
    }

    /**
     * Fügt zur Sammlung aller Code eines Imports den übergebenen Code hinzu
     *
     * @param codeString
     */
    private void addCodeString(String codeString) {
        // Unnötiges LinkedHashSet durch Aufruf von DaimlerCodes.getCodeSet() vermeiden
        if (DaimlerCodes.isEmptyCodeString(codeString)) {
            return;
        }

        BooleanFunction functionParser = DaimlerCodes.getFunctionParser(codeString);
        Set<String> codeSet = functionParser.getVariableNames();
        if (!codeSet.isEmpty()) {
            for (String singleCode : codeSet) {
                String sortKey = Utils.toSortString(singleCode);
                if (!allCodesDuringExport.containsKey(sortKey)) {
                    allCodesDuringExport.put(sortKey, singleCode);
                }
            }
        }
    }


    /**
     * Schreibt die Einsatzdaten in die XML. Einsatzdaten kommen entweder in Abhängigkeit von Idents oder ganz ohne Idents
     *
     * @param factorySigns
     * @param idents
     * @param plantInformationType
     * @param dateInMillis
     * @param stCode
     * @param plantInfoElementStarted
     * @return
     * @throws XMLStreamException
     */
    private boolean writePlantInformation(String factorySigns, Map<iPartsFactoryData.IdentWithModelNumber, Set<String>> idents,
                                          PlantInformationType plantInformationType, long dateInMillis, String stCode,
                                          boolean plantInfoElementStarted) throws XMLStreamException {
        // Wenn keine validen Daten existieren, dann schreibe auch nichts raus
        if ((plantInformationType == null) || ((dateInMillis <= 0) && (StrUtils.isEmpty(factorySigns))
                                               && ((idents == null) || idents.isEmpty()))) {
            return false;
        }

        // Es ist mind. ein Ident vorhanden
        if ((idents != null) && !idents.isEmpty()) {
            // Das Elemente kann erst hier erzeugt werden, weil erst ab hier klar ist, dass wir Einsatzdaten haben, die
            if (!plantInfoElementStarted) {
                startElement(ELEMENT_PLANT_INFORMATION_DATEN);
            }
            for (Map.Entry<iPartsFactoryData.IdentWithModelNumber, Set<String>> identWithSpikes : idents.entrySet()) {
                iPartsFactoryData.IdentWithModelNumber ident = identWithSpikes.getKey();
                writeSinglePlantInformation(plantInformationType, factorySigns, ident.ident,
                                            identWithSpikes.getValue(), dateInMillis, stCode, ident.eldasWMI);
            }
            return true;
        } else if ((dateInMillis > 0) || (StrUtils.isValid(factorySigns) && (dateInMillis == 0))) {
            // Einsatzdaten ohne idents (aber auch nur, wenn valide Daten existieren)
            if (!plantInfoElementStarted) {
                startElement(ELEMENT_PLANT_INFORMATION_DATEN);
            }
            writeSinglePlantInformation(plantInformationType, factorySigns, "", null, dateInMillis, stCode, "");
            return true;
        }
        return false;
    }

    /**
     * Schreibt einzelne Einsatzdaten-Elemente in die XML.
     *
     * @param plantInformationType
     * @param factorySigns
     * @param ident
     * @param spikes
     * @param dateInMillis
     * @param stCode
     * @param eldasWMI
     * @throws XMLStreamException
     */
    private void writeSinglePlantInformation(PlantInformationType plantInformationType, String factorySigns,
                                             String ident, Set<String> spikes, long dateInMillis,
                                             String stCode, String eldasWMI) throws XMLStreamException {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put(ATTRIBUTE_TYPE, plantInformationType.getDbValue()); // Typ: "from", "upto", "undefined" (Pflichtattribut)
        setOptionalAttribute(ATTRIBUTE_PLANT, factorySigns, attributes); // Werkskennbuchstaben
        setOptionalAttribute(ATTRIBUTE_WMI, eldasWMI, attributes); // Weltherstellercode
        setOptionalAttribute(ATTRIBUTE_HAUPTIDENT, ident, attributes); // Hauptident
        setOptionalAttribute(ATTRIBUTE_EINSATZTERMIN, convertLongDateToString(dateInMillis), attributes); // Datum (ab bzw. bis)
        if (iPartsModelYearCode.isModelYearCode(stCode)) {
            setOptionalAttribute(ATTRIBUTE_STEUERCODE, stCode, attributes); // Steuercode (ab bzw. bis)
        }
        if ((spikes == null) || spikes.isEmpty()) {
            writeEnclosedElement(ELEMENT_PLANT_INFORMATION, "", attributes, false);
        } else {
            // <PLANT_INFORMATION>
            startElement(ELEMENT_PLANT_INFORMATION, attributes);
            attributes.clear();
            writeSpikesElements(spikes);
            endElement(ELEMENT_PLANT_INFORMATION);
        }
    }

    /**
     * Konvertiert das Long-Datum in das gewünschte Daimler Format: yyyy-MM-dd
     *
     * @param dateInMillis
     * @return
     */
    private String convertLongDateToString(long dateInMillis) {
        if (dateInMillis > 0) {
            String dateAsString = String.valueOf(dateInMillis);
            dateAsString = StrUtils.cutIfLongerThan(dateAsString, 8);
            try {
                dateAsString = DateUtils.toISO_yyyyMMdd(dateAsString);
                return dateAsString;
            } catch (DateException e) {
                fireWarningToLogFile(translateForLog("!!Datumsangabe \"%1\" konnte nicht konvertiert werden!", dateAsString));
            }
        }

        return "";
    }


    /**
     * Schreibt die Ausreißer-Idents in die XML
     *
     * @param spikes
     * @throws XMLStreamException
     */
    private void writeSpikesElements(Set<String> spikes) throws XMLStreamException {
        if ((spikes != null) && !spikes.isEmpty()) {
            Map<String, String> attributes = new LinkedHashMap<>();
            for (String spike : spikes) {
                attributes.put(ATTRIBUTE_AUSREISSER_IDENT_ID, spike);
                writeEmptyElement(ELEMENT_AUSREISSER_IDENT, attributes, true);
                attributes.clear();
            }
        }
    }

    /**
     * Schreibt ein Fußnoten-Element in die XML Datei.
     *
     * @param footNoteId
     * @param footnotesForKG - die für die KG geladenen Fußnotentexte
     * @throws XMLStreamException
     */
    private void writeEnclosedFootnote(String footNoteId, Map<String, List<iPartsDataFootNoteContent>> footnotesForKG) throws XMLStreamException {
        if ((footnotesForKG == null) || footnotesForKG.isEmpty()) {
            return;
        }
        List<iPartsDataFootNoteContent> footnotesForId = footnotesForKG.get(footNoteId);
        if (footnotesForId == null) {
            iPartsDataFootNoteContentList footnotesFromDB = iPartsDataFootNoteContentList.loadFootNoteWithAllLanguages(getProject(), footNoteId);
            if (footnotesFromDB.isEmpty()) {
                return;
            }
            footnotesForId = footnotesFromDB.getAsList();
        }
        Map<String, String> attributes = new LinkedHashMap<>();

        // <FUSSNOTE>
        attributes.put(ATTRIBUTE_FUSSNOTEN_ID, footNoteId);
        startElement(ELEMENT_FUSSNOTE, attributes);
        attributes.clear();
        for (iPartsDataFootNoteContent footnotePerLine : footnotesForId) {
            String lineNo = footnotePerLine.getAsId().getFootNoteLineNo();
            String neutralText = footnotePerLine.getNeutralText();

            // <FUSSNOTENTEXTE>
            // DAIMLER-7977
            for (Language language : exportTask.getSelectedLanguages()) {
//            for (Language language : footnotePerLine.getMultiText().getLanguages()) {
                attributes.put(ATTRIBUTE_FUSSNOTENPOSITIONS_ID, lineNo);
                attributes.put(ATTRIBUTE_SPRACH_ID, language.getCode());
                String text = footnotePerLine.getText(language.getCode(), exportTask.getFallbackLanguages());
                if (StrUtils.isValid(neutralText)) {
                    text = text + " " + neutralText;
                }
                attributes.put(ATTRIBUTE_FUSSNOTENTEXT, text);
                writeEnclosedElement(ELEMENT_FUSSNOTENTEXTE, "", attributes, false);
                attributes.clear();
            }
        }
        endElement(ELEMENT_FUSSNOTE);
    }

    /**
     * Erzeugt einen kombinierten Text aus den übergbenen Texten.
     *
     * @param text
     * @param neutralText
     * @return
     */
    private String makeCombText(String text, String neutralText) {
        if (StrUtils.isEmpty(text, neutralText)) {
            return "";
        }
        String result = "";
        if (StrUtils.isValid(text)) {
            result = text;
        }
        if (StrUtils.isValid(neutralText)) {
            if (result.isEmpty()) {
                result = neutralText;
            } else {
                if (!StrUtils.stringEndsWith(result, neutralText, true)) {
                    result = result + "; " + neutralText;
                }
            }
        }
        return result;
    }

    /**
     * Liefert eine Map mit allen kombinierten Texten zur übergebenen {@link AssemblyId}. Als Schlüssel wird die KlfdNr
     * der jeweiligen Stücklistenposition verwendet.
     *
     * @param assemblyId
     * @return
     */
    private void initCombTextMap(AssemblyId assemblyId) {
        combTextForKlfdNr = new HashMap<>();
        iPartsDataCombTextList combTextList = iPartsDataCombTextList.loadForModuleAndAllLanguages(getProject(), assemblyId);
        StringBuilder stringBuilder = new StringBuilder();
        for (iPartsDataCombText combText : combTextList) {
            String klfdnr = combText.getAsId().getModuleSeqNo();
            EtkMultiSprache multiSpracheFromCache = combTextForKlfdNr.get(klfdnr);
            EtkMultiSprache multiSpracheFromObject = combText.getFieldValueAsMultiLanguage(FIELD_DCT_DICT_TEXT);
            if (multiSpracheFromCache == null) {
                multiSpracheFromCache = multiSpracheFromObject.cloneMe();
                combTextForKlfdNr.put(klfdnr, multiSpracheFromCache);
            } else {
                // Hier kommt man nur rein, wenn der kombinierte Text aus mehreren Bausteinen besteht
                for (Map.Entry<String, String> textForLanguage : multiSpracheFromObject.getLanguagesAndTexts().entrySet()) {
                    stringBuilder.append(multiSpracheFromCache.getText(textForLanguage.getKey()));
                    stringBuilder.append(" ");
                    stringBuilder.append(multiSpracheFromObject.getText(textForLanguage.getKey()));
                    multiSpracheFromCache.setText(textForLanguage.getKey(), stringBuilder.toString());
                    stringBuilder.setLength(0);
                }
            }
        }
    }

    /**
     * Liefert die gefilterten Farbtabellendaten (samt Unterstrukturen) zur übergebenen Stücklistenposition
     *
     * @param partListEntry
     * @return
     */
    private iPartsColorTable getColorTableForRetailFiltered(iPartsDataPartListEntry partListEntry) {
        if (iPartsModelId.TYPE.equals(exportTask.getExportContent().getAsId().getDataObjectType())) {
            return filter.getColorTableForRetailFiltered(partListEntry.getColorTableForRetailWithoutFilter(), partListEntry);
        }
        return null;
    }

    private void writeEnclosedPartElement(EtkDataPart originalPart) throws XMLStreamException {
        writeEnclosedPartElement(originalPart, null, null, VirtualMaterialType.NONE);
    }

    private void writeEnclosedPartElement(iPartsDataPartListEntry partListEntry, boolean withColorTableInfo) throws XMLStreamException {
        EtkDataPart part = partListEntry.getPart();
        iPartsColorTable colorTableForRetail = withColorTableInfo ? getColorTableForRetailFiltered(partListEntry) : null;
        VirtualMaterialType virtualMaterialType = VirtualMaterialType.getFromDbValue(partListEntry.getFieldValue(FIELD_K_VIRTUAL_MAT_TYPE));
        writeEnclosedPartElement(part, colorTableForRetail, partListEntry.getAsId(), virtualMaterialType);
    }

    /**
     * Schreibt ein Teil/Material samt Benennungen und Farbinformationen
     *
     * @param originalPart
     * @param colorTableForRetail
     * @param partListEntryId
     * @throws XMLStreamException
     */
    private void writeEnclosedPartElement(EtkDataPart originalPart, iPartsColorTable colorTableForRetail,
                                          PartListEntryId partListEntryId, VirtualMaterialType virtualMaterialType) throws XMLStreamException {
        if (originalPart != null) {
            if (!originalPart.existsInDB()) {
                originalPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
            }
            String cacheKey = originalPart.getAsId().getMatNr() + IdWithType.DB_ID_DELIMITER + originalPart.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR);
            EtkDataPart cachedPart = partsCache.get(cacheKey);
            if (cachedPart == null) {
                cachedPart = originalPart.cloneMe(getProject());
                if ((virtualMaterialType == VirtualMaterialType.TEXT_SUB_HEADING) || (virtualMaterialType == VirtualMaterialType.TEXT_HEADING)) {
                    // Stücklistenüberschriften in ELDAS sind virtuelle Teilebenennungen. Diese sollen ebenfalls ausgegeben
                    // werden. Diese Überschriften sind eigentlich kombinierte Texte, die als Teilebenennung angezeigt werden
                    // -> hole den kombinierten Text und gebe diesen als Teilenebennung aus (in allen Sprachen)
                    if (combTextForKlfdNr != null) {
                        EtkMultiSprache partDesc = combTextForKlfdNr.get(partListEntryId.getKLfdnr());
                        if (partDesc != null) {
                            cachedPart.setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, partDesc, DBActionOrigin.FROM_DB);
                        }
                    }
                } else {
                    // Fehlende Attribute nachladen
                    String[] missingAttributes = getMissingAttributesForPartReload();
                    if (cachedPart.loadMissingAttributesFromDB(missingAttributes, false, false, false)) {
                        // Die Benennung muss über diesen Weg bestimmt werden, selbst wenn das Teil schon einen Text
                        // als Multilang-Objekt besitzt. Der Text am Teil wird generell für die aktuelle DB Sprache geladen.
                        // Hier brauchen wir den Text aber in jeder Sprache.
                        EtkMultiSprache partDesc = getProject().getDbLayer().loadMultiLanguageByTextNr(TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR),
                                                                                                       cachedPart.getAttribute(FIELD_M_TEXTNR).getMultiLanguageTextNr());
                        cachedPart.setFieldValueAsMultiLanguage(FIELD_M_TEXTNR, partDesc, DBActionOrigin.FROM_DB);
                        // Nach dem Laden des Textes prüfen, ob das Teil ein Leitungssatz-BK ist. Falls ja, das Objekt
                        // austauschen, da wir später den Text brauchen und so nicht neuladen müssen.
                        String materialNummer = cachedPart.getAsId().getMatNr();
                        if ((wireHarnessParts != null) && wireHarnessParts.containsKey(materialNummer)) {
                            wireHarnessParts.put(materialNummer, cachedPart);
                        }
                    }
                    if (!originalPart.getAsId().getMatNr().isEmpty()) {
                        partsCache.put(cacheKey, cachedPart);
                    }
                }
            }
            boolean existInDB = cachedPart.existsInDB() && !cachedPart.isNew();
            writePartElement(cachedPart, colorTableForRetail, partListEntryId, existInDB, ELEMENT_TEIL, ATTRIBUTE_TEIL_ID);
            if (!existInDB) {
                fireWarningToLogFile(translateForLog("!!Zur Teilenummer \"%1\" existiert kein Teilestamm in der Datenbank.", cachedPart.getAsId().getMatNr()));
                warningCount++;
            }
        }
    }

    /**
     * Liefert die fehlenden Teilenummer Attribute für das Nachladen einer Teilenummer
     *
     * @return
     */
    private String[] getMissingAttributesForPartReload() {
        String[] missingAttributes = new String[]{ FIELD_M_TEXTNR, FIELD_M_AS_ES_1, FIELD_M_AS_ES_2,
                                                   FIELD_M_BASE_MATNR };
        if (isPSKSpecialCase()) {
            missingAttributes = StrUtils.mergeArrays(missingAttributes, iPartsXMLExportLoadingFields.getPskMatFieldsForExport().getAsFieldNamesArray());
        }
        return missingAttributes;
    }

    /**
     * Exportiert das übergebene Teil (samt Unterstrukturen)
     *
     * @param colorTableForRetail
     * @param partListEntryId
     * @param existInDB
     * @param elementName
     * @param numberAttributeName
     * @throws XMLStreamException
     */
    private void writePartElement(EtkDataPart dataPart, iPartsColorTable colorTableForRetail, PartListEntryId partListEntryId,
                                  boolean existInDB, String elementName, String numberAttributeName) throws XMLStreamException {
        String originalPartNumber = dataPart.getAsId().getMatNr();
        originalPartNumber = iPartsNumberHelper.isPseudoPart(originalPartNumber) ? "" : originalPartNumber;

        String partNumber = dataPart.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_MAPPED_MATNR);
        // Die Basis-Materialnummer verwenden, sofern vorhanden
        String basePartNumber = dataPart.getFieldValue(FIELD_M_BASE_MATNR);
        if (!basePartNumber.isEmpty()) {
            partNumber = basePartNumber;
        }
        EtkMultiSprache partDesc = dataPart.getFieldValueAsMultiLanguage(FIELD_M_TEXTNR);
        // Es können Pseudo-Teile vorkommen, die nur eine Teilebenennung und keine Teilenummer haben. Hier darf die
        // Teilenummer nicht ausgegebene werden
        String exportPartNumber = iPartsNumberHelper.isPseudoPart(partNumber) ? "" : partNumber;
        if (isModuleSpringRelevant) {
            // Das Federmapping geht immer auf die Original-Teilenummer (laut Daimler gibt es dort keine ES1/ES2)
            Set<String> springLegsForSpringPartNumber = iPartsSpringMapping.getInstance(getProject()).getSpringLegsForSpringPartNumber(originalPartNumber);
            if (springLegsForSpringPartNumber != null) {
                for (String springLeg : springLegsForSpringPartNumber) {
                    SpringMapping springMappingObject = new SpringMapping(originalPartNumber, partDesc, springLeg);
                    springPartToObjectMapping.add(springMappingObject);
                }
            }
        }
        Map<String, String> attributes = new LinkedHashMap<>();
        // Ab DAIMLER-9716 werden QSL Sachnummer ohne SL ausgegeben
        exportPartNumber = iPartsNumberHelper.handleQSLPartNo(exportPartNumber);
        attributes.put(numberAttributeName, exportPartNumber);
        // Wurde das Teil als Teil mit einer "Original-Teilenummer" markiert, soll hier die Original-Teilenummer auch
        // ausgegeben werden
        if (dataPart.attributeExists(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_MAT_NR)) {
            attributes.put(ATTRIBUTE_ORIGINAL_TEIL_ID, dataPart.getFieldValue(iPartsDataVirtualFieldsDefinition.DA_ORIGINAL_MAT_NR));
        }
        setOptionalAttribute(ATTRIBUTE_ES1, dataPart.getFieldValue(FIELD_M_AS_ES_1), attributes);

        // ES2 soll nicht gesetzt werden, wenn existent, aber die Admin-Option nicht gesetzt ist.
        if (iPartsPlugin.isWebservicesXMLExportSeparateES12Keys()) {
            setOptionalAttribute(ATTRIBUTE_ES2, dataPart.getFieldValue(FIELD_M_AS_ES_2), attributes); // wird nicht angezeigt, falls ES2 leer/null ist
            setOptionalAttribute(ATTRIBUTE_TEIL_ID_FORMATIERT,
                                 iPartsNumberHelper.formatPartNo(getProject(), exportPartNumber, getProject().getDBLanguage()),
                                 attributes);
        }

        // PSK Felder hinzufügen, falls möglich
        addPSKPartFields(dataPart, attributes);

        boolean hasColorTables = colorTableForRetail != null;
        boolean hasOnlyAttributes = StrUtils.isValid(exportPartNumber) && !hasColorTables && ((partDesc == null) || partDesc.allStringsAreEmpty());
        boolean doesNotExist = !existInDB && !hasColorTables;
        if (doesNotExist || hasOnlyAttributes) {
            writeEnclosedElement(elementName, null, attributes, false);
        } else {
            startElement(elementName, attributes);
            attributes.clear();
            if (existInDB) {
                // <TEXTE>
                if (partDesc != null) {
                    writeMultiLangEnclosedText(partDesc, (partListEntryId != null) ?
                                                         translateForLog("!!Das vorhandene Teil \"%1\" zum Stücklisteneintrag %2 enthält keine Benennung",
                                                                         originalPartNumber, partListEntryId.toString()) : "");
                }
            }
            // <FARBE>
            if (hasColorTables) {
                iPartsFactoryModel factoryModel = iPartsFactoryModel.getInstance(getProject());
                for (Map.Entry<String, iPartsColorTable.ColorTable> colorTableEntry : colorTableForRetail.getColorTablesMap().entrySet()) {
                    iPartsColorTable.ColorTable colorTable = colorTableEntry.getValue();
                    iPartsSeriesId seriesId = new iPartsSeriesId(ColorTableHelper.extractSeriesNumberFromTableId(colorTable.colorTableId.getColorTableId()));
                    for (iPartsColorTable.ColorTableContent colorTableContent : colorTable.colorTableContents) {
                        String colorNumber = colorTableContent.colorNumber;
                        attributes.put(ATTRIBUTE_ES2, colorNumber);
                        setOptionalAttribute(ATTRIBUTE_FARB_KZ, colorTable.colorSign, attributes);
                        startElement(ELEMENT_FARBE, attributes);

                        // <FARBPOSITIONS_ID>
                        writeEnclosedElement(ELEMENT_FARBPOSITIONS_ID, colorTableContent.colorTableContentId.getPosition(), null, false);

                        // <TEXTE> Farbbenennung
                        EtkMultiSprache currentName = colorTableContent.colorName;
                        if (currentName != null) {
                            String textId = currentName.getTextId();

                            // Suche nach der Benennung im Farbbenennungs-Cache
                            EtkMultiSprache colorName = colorNames.get(textId);
                            if (colorName == null) {
                                if (StrUtils.isValid(textId)) {
                                    // Wenn die TextId existiert und die Farbbenennung noch nicht im Cache liegt,
                                    // dann lade die Farbbenennung in allen Sprachen und lege sie in den Cache
                                    colorName = getProject().getDbLayer().getLanguagesTextsByTextId(textId, TableAndFieldName.make(TABLE_DA_COLOR_NUMBER, FIELD_DCN_DESC));
                                }

                                // Falls das Laden der Benennung nicht erfolgreich war, dann nimm die Benennung, die
                                // an den Retaildaten hängt
                                if (colorName == null) {
                                    colorName = currentName;
                                }
                                colorNames.put(textId, colorName);
                            }

                            writeMultiLangEnclosedText(colorName, translateForLog("!!Die Farbe \"%1\" zur Farbtabelle \"%2\" " +
                                                                                  "und Teilenummer \"%3\" enthält keine Benennung",
                                                                                  colorNumber, colorTable.colorTableId.getColorTableId(),
                                                                                  originalPartNumber));
                        }

                        // <CODEBEDINGUNG>
                        String colorCode = ColorTableHelper.getColorTableCode(getProject(), colorTableContent);
                        if (!DaimlerCodes.isEmptyCodeString(colorCode)) { // Optional
                            addCodeString(colorCode);
                            writeEnclosedElement(ELEMENT_CODEBEDINGUNG, colorCode, null, true);
                        }

                        // <PRODUKTGRUPPE>
                        writeEnclosedElement(ELEMENT_PRODUKTGRUPPE, colorTableContent.productGroup, null, true);

                        // <PLANT_INFORMATION_DATEN>
                        boolean started = false;
                        iPartsColorFactoryDataForRetail factoryData = colorTableContent.getFactoryData();
                        if (factoryData != null) {
                            for (Map.Entry<String, List<iPartsColorFactoryDataForRetail.DataForFactory>> factoryEntry : factoryData.getFactoryDataMap().entrySet()) {
                                // Werkskennbuchstabe ermitteln aus Cache
                                String factorySigns = factoryModel.getFactorySignsStringForFactoryNumberAndSeries(factoryEntry.getKey(),
                                                                                                                  seriesId,
                                                                                                                  selectedProduct.getAggregateType());
                                for (iPartsColorFactoryDataForRetail.DataForFactory factoryContent : factoryEntry.getValue()) {
                                    if (factoryContent.hasPEMFrom()) {
                                        started |= writePlantInformation(factorySigns, factoryContent.identsFrom,
                                                                         PlantInformationType.FROM, factoryContent.dateFrom,
                                                                         factoryContent.stCodeFrom, started);
                                    }
                                    if (factoryContent.hasPEMTo()) {
                                        started |= writePlantInformation(factorySigns, factoryContent.identsTo,
                                                                         PlantInformationType.UPTO, factoryContent.dateTo,
                                                                         factoryContent.stCodeTo, started);
                                    }
                                }
                            }
                        }
                        if (started) {
                            endElement(ELEMENT_PLANT_INFORMATION_DATEN);
                        }

                        endElement(ELEMENT_FARBE);
                        attributes.clear();

                    }
                }
            }
            endElement(elementName);
        }
    }

    /**
     * Setzt die PSK Attribute sofern es sich um einen validen PSK Fall handelt
     *
     * @param dataPart
     * @param attributes
     */
    private void addPSKPartFields(EtkDataPart dataPart, Map<String, String> attributes) {
        if (isPSKSpecialCase()) {
            // Nur ausgeben, wenn es ein echte PSK Material ist
            if (dataPart.getFieldValueAsBoolean(FIELD_M_PSK_MATERIAL)) {
                setOptionalAttribute(ATTRIBUTE_PSK_PART, Boolean.toString(true), attributes);
            }
            setOptionalAttribute(ATTRIBUTE_PSK_SUPPLIER_NO, dataPart.getFieldValue(FIELD_M_PSK_SUPPLIER_NO), attributes);
            setOptionalAttribute(ATTRIBUTE_PSK_MANUFACTURER_NO, dataPart.getFieldValue(FIELD_M_PSK_MANUFACTURER_NO), attributes);
            setOptionalAttribute(ATTRIBUTE_PSK_SUPPLIER_PART_NO, dataPart.getFieldValue(FIELD_M_PSK_SUPPLIER_MATNR), attributes);
            setOptionalAttribute(ATTRIBUTE_PSK_MANUFACTURER_PART_NO, dataPart.getFieldValue(FIELD_M_PSK_MANUFACTURER_MATNR), attributes);
            setOptionalAttribute(ATTRIBUTE_PSK_IMAGE_NO_EXTERN, dataPart.getFieldValue(FIELD_M_PSK_IMAGE_NO_EXTERN), attributes);
            setOptionalAttribute(ATTRIBUTE_PSK_PSK_REMARK, dataPart.getFieldValue(FIELD_M_PSK_REMARK), attributes);
            setOptionalAttribute(ATTRIBUTE_PSK_NATO_NO, dataPart.getFieldValue(FIELD_M_NATO_NO), attributes);
        }
    }

    /**
     * Handelt es sich um einen expliziten PSK Export Fall.
     *
     * @return
     */
    private boolean isPSKSpecialCase() {
        return exportTask.isExportPSKData();
    }

    /**
     * Liefert zurück, ob das ausgewählte Produkt valide und ein PSK Produkt ist
     *
     * @return
     */
    private boolean isValidPSKProductSelected() {
        return (selectedProduct != null) && selectedProduct.isPSK();
    }

    /**
     * Initialisiert den Filter für den jeweiligen Export-Typ
     */
    private void initFilter() {
        if (filter == null) {
            switch (exportTask.getExportContent().getAsId().getDataObjectType()) {
                case iPartsModelId.TYPE:
                    initFilterForModel();
                    break;
                case iPartsSeriesId.TYPE:
                case iPartsSaId.TYPE:
                    break;
            }
        }
        // Leitungssatz-Baukasten nur exportieren, wenn die Konfig und der Filter aktiv sind
        if ((filter != null) && iPartsWireHarnessHelper.isWireHarnessFilterConfigActive() && isExportWireHarnessData()) {
            wireHarnessParts = new HashMap<>();
            wireHarnessCache = iPartsWireHarness.getInstance(getProject());
        }
    }

    /**
     * Initialisiert den Filter für den Baumusterexport
     */
    private void initFilterForModel() {
        filter = new iPartsFilter();
        filter.setModelFilterActive(true);
        filter.setFilterImagesWithOnlyFINFlag(false); // Beim Export sollen Bilder mit dem FIN Kenner trotzdem ausgegeben werden
        filter.getSwitchboardState().setMainSwitchActive(true);
        AbstractDataCard dataCard = null;
        try {
            if (!selectedModel.getAsId().isAggregateModel()) {
                dataCard = VehicleDataCard.getVehicleDataCard(selectedModel.getAsId().getModelNumber(), false, false,
                                                              true, null, getProject(), false);

                // Bei gewünschter Fahrzeug-Navigation müssen alle Aggregate-Datenkarten für die Filterung hinzugefügt werden
                if ((dataCard != null) && exportTask.isExportVisualNav()) {
                    filter.setAggModelsFilterActive(true);
                    for (String aggregateModelNumber : ((VehicleDataCard)dataCard).getAggregateModelNumbers().getAllCheckedValues()) {
                        AbstractDataCard aggregateDataCard = AggregateDataCard.getAggregateDataCard(null, aggregateModelNumber, false,
                                                                                                    true, null, getProject());
                        if (aggregateDataCard != null) {
                            ((VehicleDataCard)dataCard).addActiveAggregate((AggregateDataCard)aggregateDataCard);
                        }
                    }
                }
            } else if (selectedModel.getAsId().isAggregateModel()) {
                dataCard = AggregateDataCard.getAggregateDataCard(null, selectedModel.getAsId().getModelNumber(), false,
                                                                  true, null, getProject());
            }
            // PSK Varianten hinzufügen
            addPSKVariantsToDatacard(dataCard);
        } catch (DataCardRetrievalException e) {
            Logger.getLogger().handleRuntimeException(e);
        }
        if (dataCard == null) {
            filter = null;
            stopExport(translateForLog("!!Baumusterfilter konnte mit dem Baumuster \"%1\" nicht " +
                                       "initialisiert werden. Export wird abgebrochen",
                                       ((selectedModel != null) ? selectedModel.getAsId().getModelNumber() : ""))
            );
            errorCount++;
            return;
        }
        filter.setCurrentDataCard(dataCard, getProject());
    }

    /**
     * Fügt der erzeugten BM Datenkarte die ausgewählten PSK Varianten hinzu
     *
     * @param dataCard
     */
    private void addPSKVariantsToDatacard(AbstractDataCard dataCard) {
        if ((dataCard != null) && (pskVariants != null) && isPSKSpecialCase()) {
            filter.setPSKVariantsFilterActive(true);
            dataCard.setPskVariants(pskVariants);
        }
    }

    /**
     * Liefert zu einem Date-String bzw DateTime-String den ISO Date-String
     *
     * @param dateValue
     * @return
     */
    private String getISODate(String dateValue) {
        if (StrUtils.isValid(dateValue)) {
            String tempDateValue = dateValue;
            if (dateValue.length() > 8) {
                // Falls es sich um ein DateTime handelt (z.B. aus DA_PRODUCT_MODELS) -> Ersten 8 Zeichen verwenden
                tempDateValue = StrUtils.copySubString(dateValue, 0, 8);
            }
            try {
                return DateUtils.toISO_yyyyMMdd(tempDateValue);
            } catch (DateException e) {
                Logger.getLogger().handleRuntimeException(e);
            }
        }
        return "";
    }

    /**
     * Führt alle nötigen Handlungen nach einem Export durch
     */
    @Override
    protected void finishExportAndLogFile() {
        super.finishExportAndLogFile();
        groupNumbers.clear();
        partsCache.clear();
        colorNames.clear();
        allCodesDuringExport.clear();
        combTextForKlfdNr = null;
        validSaasForModel = null;
        validSAsForModel = null;
        existingSaasInAssemblies.clear();
        springPartToObjectMapping.clear();
    }

    /**
     * Zeigt den Such-Dialog für Baumuster
     *
     * @param owner
     * @param onExtendEvent
     * @return
     */
    private String showModelSelection(AbstractJavaViewerForm owner, OnExtendExportDataElement onExtendEvent) {
        SelectSearchGridModel selectSearchGridModel = new SelectSearchGridModel(owner);
        selectSearchGridModel.setOnExtendFormEvent(onExtendEvent);
        return selectSearchGridModel.showGridSelectionDialog("");
    }

    /**
     * Zeigt den Such-Dialog für Produkte
     *
     * @param owner
     * @param products
     * @param onExtendEvent
     * @return
     */
    private String showProductSelection(AbstractJavaViewerForm owner, final Set<String> products,
                                        OnExtendExportDataElement onExtendEvent) {
        OnValidateAttributesEvent onValidateAttributesEvent = attributes -> {
            // Im Auswahl-Dialog sollen nur die verknüpften Produkte angezeigt werden
            String foundProduct = attributes.getFieldValue(FIELD_DP_PRODUCT_NO);
            return products.contains(foundProduct);
        };
        SelectSearchGridProduct selectSearchGridProduct = new SelectSearchGridProduct(owner);
        selectSearchGridProduct.setOnValidateAttributesEvent(onValidateAttributesEvent);
        selectSearchGridProduct.setOnExtendFormEvent(onExtendEvent);
        int maxResults = SelectSearchGridProduct.getMaxSelectResultSize(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE, false);
        selectSearchGridProduct.setMaxResults(maxResults);
        selectSearchGridProduct.setAutoSelectSingleSearchResult(true);
        return selectSearchGridProduct.showGridSelectionDialog("*");
    }

    private boolean showModelAndProductSelectionDialog(AbstractJavaViewerForm owner,
                                                       List<MessageEventData> messageEventDataForLogForm,
                                                       OnExtendExportDataElement extend) {

        String modelNumber = showModelSelection(owner, extend);
        iPartsModelId modelId = new iPartsModelId(modelNumber);

        VarParam<String> translatedErrorMessage = new VarParam<>();
        iPartsDataModel model = getModelIfValid(modelId, translatedErrorMessage);
        if (model == null) {
            handleMessageEventData(messageEventDataForLogForm, translatedErrorMessage.getValue(),
                                   MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            return false;
        }

        String productNumber;
        Set<String> productsForModel = getProductsForModel(modelId, translatedErrorMessage);
        if ((productsForModel == null) || productsForModel.isEmpty()) {
            handleMessageEventData(messageEventDataForLogForm, translatedErrorMessage.getValue(), MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
            ModalResult result = MessageDialog.showYesNo(translatedErrorMessage.getValue() + "\n\n" +
                                                         translateForLog("!!Möchten Sie ein anderes Baumuster auswählen?"),
                                                         translateForLog("!!Export Stücklistendaten"));
            if (result == ModalResult.YES) {
                return showModelAndProductSelectionDialog(owner, messageEventDataForLogForm, extend);
            } else {
                return false;
            }
        } else if (productsForModel.size() == 1) {
            productNumber = productsForModel.iterator().next();
            // Wir haben hier nur ein Produkt zum BM. Handelt es sich um ein PSK Produkt und hat der Benutzer das PSK
            // Recht, dann müssen die Varianten ausgewählt werden. isWithGUI muss eigentlich nicht geprüft werden, da
            // wir hier nur reinkommen, wenn wir den BM/Produkt Dialog anzeigen
            if (isWithGUI && iPartsRight.checkPSKInSession()) {
                iPartsProduct product = getProductIfValid(productNumber, null);
                if ((product != null) && product.isPSK()) {
                    // Zur Auswahl der PSK Varianten den Produktdialog anzeigen
                    productNumber = showProductSelection(owner, productsForModel, extend);
                }
            }
        } else {
            // Ist das Baumuster in mehr als einem Produkt vorhanden -> Manuelle Auswahl durch Benutzer
            String productsForLog = StrUtils.stringListToString(productsForModel, LIST_DELIMITER);
            if (StrUtils.isValid(productsForLog)) {
                handleMessageEventData(messageEventDataForLogForm, translateForLog("!!Baumuster \"%1\" ist in den Produkten \"%2\" enthalten.",
                                                                                   model.getAsId().getModelNumber(), productsForLog),
                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            }
            productNumber = showProductSelection(owner, productsForModel, extend);
        }

        iPartsProduct product = getProductIfValid(productNumber, translatedErrorMessage);
        if (product == null) {
            handleMessageEventData(messageEventDataForLogForm, translatedErrorMessage.getValue(),
                                   MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
            return false;
        }

        if (!acquireExportInformationFromModel(model.getAsId(), product.getAsId(), messageEventDataForLogForm, translatedErrorMessage)) {
            handleMessageEventData(messageEventDataForLogForm, translatedErrorMessage.getValue(),
                                   MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
            return false;
        }

        selectedModel = model;
        selectedProduct = product;

        return true;
    }

    private iPartsDataModel getModelIfValid(iPartsModelId modelId, VarParam<String> translatedErrorMessage) {
        iPartsDataModel model = new iPartsDataModel(getProject(), modelId);
        if (!modelId.isValidId() || !model.existsInDB()) {
            translatedErrorMessage.setValue(translateForLog("!!Baumuster \"%1\" existiert nicht in der Datenbank!",
                                                            modelId.getModelNumber()));
            return null;
        }
        return model;
    }

    private Set<String> getProductsForModel(iPartsModelId modelId, VarParam<String> translatedErrorMessage) {
        // Hole alle Produkte mit denen das Baumuster verknüpft ist
        Set<String> products = iPartsProductModels.getInstance(getProject()).getProductNumbersByModel(modelId.getModelNumber());
        if ((products == null) || products.isEmpty()) {
            translatedErrorMessage.setValue(translateForLog("!!Baumuster \"%1\" ist keinem Produkt zugeordnet!",
                                                            modelId.getModelNumber()));
            return null;
        }
        return products;
    }

    private iPartsProduct getProductIfValid(String productNo, VarParam<String> translatedErrorMessage) {
        iPartsProduct product = null;
        if (StrUtils.isValid(productNo)) {
            iPartsProductId productId = new iPartsProductId(productNo);
            product = iPartsProduct.getInstance(getProject(), productId);
        }
        if ((product == null) && (translatedErrorMessage != null)) {
            translatedErrorMessage.setValue(translateForLog("!!Produkt \"%1\" existiert nicht!", productNo));
            return null;
        }
        return product;
    }

    /**
     * Sammelt alle Informationen für den Export der Stücklisten zu einem Baumuster - Produkt Paar
     *
     * @param modelId
     * @param productId
     * @param messageEventDatas
     * @return
     */
    private boolean acquireExportInformationFromModel(iPartsModelId modelId, iPartsProductId productId,
                                                      List<MessageEventData> messageEventDatas,
                                                      VarParam<String> translatedErrorMessage) {
        // Hole alle Module zum ausgewählten Produkt
        productStructures = iPartsProductStructures.getInstance(getProject(), productId);
        Set<AssemblyId> moduleIds = productStructures.getModuleIds(getProject());
        if (!moduleIds.isEmpty()) {
            // Setze die validen SAAs zum Baumuster
            validSaasForModel = iPartsModel.getInstance(getProject(), modelId).getSaas(getProject());
            // Setze die validen SAs zum Baumuster
            if ((validSaasForModel != null) && validSaasForModel.isEmpty()) {
                validSAsForModel = iPartsFilter.retrieveSasFromSaas(validSaasForModel);
            }
            assembliesMaxCount = moduleIds.size();
            handleMessageEventData(messageEventDatas, translateForLog("!!Baumuster \"%1\" für Export ausgewählt",
                                                                      modelId.getModelNumber()),
                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

            handleMessageEventData(messageEventDatas, translateForLog("Manuelle Auswahl durch Benutzer: %1.",
                                                                      productId.getProductNumber()),
                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

            handleMessageEventData(messageEventDatas, translateForLog("!!Ausgewähltes Produkt \"%1\" enthält %2 Stückliste(n)",
                                                                      productId.getProductNumber(), String.valueOf(assembliesMaxCount)),
                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            return true;
        } else {
            translatedErrorMessage.setValue(translateForLog("!!Keine Stücklisten für die Kombination \"Produkt %1\" und \"Baumuster %2\"" +
                                                            " vorhanden.", productId.getProductNumber(), modelId.getModelNumber()));
            MessageDialog.show(translatedErrorMessage.getValue(),
                               translateForLog("!!Export Stücklistendaten"));
            return false;
        }
    }

    /**
     * Zeigt den Such-Dialog für SAs
     *
     * @param owner
     * @param onExtendEvent
     * @return
     */
    private String showSASelection(AbstractJavaViewerForm owner, OnExtendExportDataElement onExtendEvent) {
        SelectSearchGridSA selectSearchGridSA = new SelectSearchGridSA(owner);
        selectSearchGridSA.setOnExtendFormEvent(onExtendEvent);
        return selectSearchGridSA.showGridSelectionDialog("");
    }

    private iPartsDataSa getSaIfValid(iPartsSaId saId, VarParam<String> translatedErrorMessage) {
        iPartsDataSa dataSa = new iPartsDataSa(getProject(), saId);
        if (!saId.isValidId() || !dataSa.existsInDB()) {
            translatedErrorMessage.setValue(translateForLog("!!SA \"%1\" existiert nicht!", saId.getSaNumber()));
            return null;
        }
        return dataSa;
    }

    private boolean showSASelectionDialog(AbstractJavaViewerForm owner, List<MessageEventData> messageEventDataForLogForm,
                                          OnExtendExportDataElement extend) {

        String saNumber = showSASelection(owner, extend);
        iPartsSaId saId = new iPartsSaId(saNumber);

        VarParam<String> translatedErrorMessage = new VarParam<>();
        iPartsDataSa sa = getSaIfValid(saId, translatedErrorMessage);
        if (sa == null) {
            handleMessageEventData(messageEventDataForLogForm, translatedErrorMessage.getValue(),
                                   MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
            return false;
        }

        if (!acquireExportInformationForSA(sa.getAsId(), messageEventDataForLogForm, translatedErrorMessage)) {
            handleMessageEventData(messageEventDataForLogForm, translatedErrorMessage.getValue(),
                                   MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
            ModalResult result = MessageDialog.showYesNo(translatedErrorMessage + "\n\n" +
                                                         translateForLog("Möchten Sie eine andere SA exportieren?"),
                                                         translateForLog("!!Export Stücklistendaten"));
            if (result == ModalResult.YES) {
                return showSASelectionDialog(owner, messageEventDataForLogForm, extend);
            } else {
                return false;
            }
        }

        selectedSA = sa;

        return true;
    }

    /**
     * Sammelt alle Informationen für den Export der Stücklisten zu einer freie SA
     *
     * @param saId
     * @param messageEventDatas
     * @return
     */
    private boolean acquireExportInformationForSA(iPartsSaId saId, List<MessageEventData> messageEventDatas,
                                                  VarParam<String> translatedErrorMessage) {
        String saNumber = saId.getSaNumber();
        handleMessageEventData(messageEventDatas, translateForLog("!!Freie SA \"%1\" für Export ausgewählt", saNumber),
                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
        iPartsDataSAModulesList saModulesList = iPartsDataSAModulesList.loadDataForSA(getProject(), new iPartsSAId(saNumber));
        if (saModulesList.isEmpty()) {
            translatedErrorMessage.setValue(translateForLog("!!Zur freien SA \"%1\" sind keine Stücklisten vorhanden", saNumber));
            return false;
        } else {
            assembliesMaxCount = saModulesList.size();
            handleMessageEventData(messageEventDatas, translateForLog("!!Ausgewählte freie SA \"%1\" enthält %2 Stückliste(n)", saNumber,
                                                                      String.valueOf(assembliesMaxCount)), MessageLogType.tmlMessage,
                                   MessageLogOption.TIME_STAMP);
            return true;
        }
    }

    private static class ExportSaa {

        private final String saaNumber;
        private final EtkMultiSprache saaDesc;
        private final String connectedSasString;

        public ExportSaa(String saaNumber, EtkMultiSprache saaDesc, String connectedSasString) {
            this.saaNumber = saaNumber;
            this.saaDesc = saaDesc;
            this.connectedSasString = connectedSasString;
        }

        public ExportSaa(iPartsDataSaa saaData) {
            this(saaData.getAsId().getSaaNumber(), saaData.getFieldValueAsMultiLanguage(FIELD_DS_DESC),
                 saaData.getFieldValue(FIELD_DS_CONNECTED_SAS));
        }

        public String getSaaNumber() {
            return saaNumber;
        }

        public EtkMultiSprache getSaaDesc() {
            return saaDesc;
        }

        public String getConnectedSasString() {
            return connectedSasString;
        }

        public List<String> getConnectedSasAsList() {
            return StrUtils.toStringListContainingDelimiterAndBlanks(connectedSasString, EDS_CONNECTED_SAS_DELIMITER, false);
        }
    }

    private static class SpringMapping {

        private final String springPartNumber;
        private final EtkMultiSprache springPartText;
        private final String sparePartNumber;

        public SpringMapping(String springPartNumber, EtkMultiSprache springPartText, String sparePartNumber) {
            this.springPartNumber = springPartNumber;
            this.springPartText = springPartText;
            this.sparePartNumber = sparePartNumber;
        }

        public String getSpringPartNumber() {
            return springPartNumber;
        }

        public EtkMultiSprache getSpringPartText() {
            return springPartText;
        }

        public String getSparePartNumber() {
            return sparePartNumber;
        }

    }

    /**
     * Hilfklasse für den OnExtendFormEvent
     * baut das Sprachen-Selection-Element und die CheckBox für BildExport auf
     */
    private static class OnExtendExportDataElement implements OnExtendFormEvent {

        private final EtkProject project;
        private final String initialLanguages;
        private GuiPanel panel;
        private iPartsGuiDaimlerLangSelectionBox langSelectionBox;
        private RComboBox<iPartsExportPictureFormat> pictureExportType;
        private GuiLabel productVariantsLabel;
        private iPartsGuiPSKVariantsSelectTextField productVariants;
        private GuiCheckbox exportAdditionalPartDataCheckbox;
        private boolean exportAdditionalPartData;
        private GuiCheckbox exportEinPASDataCheckbox;
        private boolean exportEinPASData;

        public OnExtendExportDataElement(EtkProject project, String initialLanguages) {
            this.project = project;
            this.initialLanguages = initialLanguages;
            panel = null;
        }

        @Override
        public AbstractGuiControl getExtensionElement(AbstractJavaViewerForm parentForm) {
            if ((panel == null) && (project != null)) {
                panel = new GuiPanel();
                panel.setLayout(new LayoutGridBag());
                panel.setPadding(4);

                GuiLabel label = new GuiLabel("!!Export Konfiguration:");
                label.setFontStyle(DWFontStyle.BOLD);
                int gridY = 0;
                ConstraintsGridBag constraints = createLabelConstraints(gridY);
                constraints.setGridwidth(2);
                constraints.setAnchor(ConstraintsGridBag.ANCHOR_WEST);
                label.setConstraints(constraints);
                panel.addChild(label);

                gridY++;
                label = new GuiLabel("!!Sprachen:");
                label.setConstraints(createLabelConstraints(gridY));
                label.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
                panel.addChild(label);
                langSelectionBox = new iPartsGuiDaimlerLangSelectionBox();
                langSelectionBox.init(project, initialLanguages);
                langSelectionBox.setMaximumWidth(langSelectionBox.getPreferredWidth());
                constraints = createValueConstraints(gridY);
                constraints.setInsetsRight(4);
                langSelectionBox.setConstraints(constraints);
                // Änderungen an den Buttons via OnChangeEvent an das Panel weitergeben
                langSelectionBox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                    @Override
                    public void fire(Event event) {
                        panel.fireEvent(event);
                    }
                });
                panel.addChild(langSelectionBox);

                gridY++;
                label = new GuiLabel("!!Export mit Bildern:");
                label.setConstraints(createLabelConstraints(gridY));
                label.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
                panel.addChild(label);
                pictureExportType = new RComboBox<>();
                constraints = createValueConstraints(gridY);
                constraints.setFill(ConstraintsGridBag.FILL_NONE);
                constraints.setAnchor(ConstraintsGridBag.ANCHOR_WEST);
                pictureExportType.setConstraints(constraints);
                setPicFormatValues(iPartsExportPictureFormat.ALL_FORMATS);
                panel.addChild(pictureExportType);

                gridY++;
                label = new GuiLabel("!!Optionale Export-Informationen:");
                label.setFontStyle(DWFontStyle.BOLD);
                constraints = createLabelConstraints(gridY);
                constraints.setGridwidth(2);
                constraints.setAnchor(ConstraintsGridBag.ANCHOR_WEST);
                label.setConstraints(constraints);
                panel.addChild(label);

                // Export der Materialeigenschaften (PRIMUS und Custom Properties)
                gridY++;
                label = new GuiLabel("!!Materialeigenschaften:");
                label.setConstraints(createLabelConstraints(gridY));
                label.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
                panel.addChild(label);
                exportAdditionalPartDataCheckbox = new GuiCheckbox();
                exportAdditionalPartDataCheckbox.setConstraints(createValueConstraints(gridY));
                exportAdditionalPartDataCheckbox.setSelected(false);
                exportAdditionalPartDataCheckbox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                    @Override
                    public void fire(Event event) {
                        setExportAdditionalPartData(exportAdditionalPartDataCheckbox.isSelected());
                    }
                });
                panel.addChild(exportAdditionalPartDataCheckbox);
                // Export der EinPAS Daten (CeMat)
                gridY++;
                label = new GuiLabel("!!EinPAS Knoten zu Teilenummern:");
                label.setConstraints(createLabelConstraints(gridY));
                label.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
                panel.addChild(label);
                exportEinPASDataCheckbox = new GuiCheckbox();
                exportEinPASDataCheckbox.setConstraints(createValueConstraints(gridY));
                exportEinPASDataCheckbox.setSelected(false);
                exportEinPASDataCheckbox.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
                    @Override
                    public void fire(Event event) {
                        setExportEinPASData(exportEinPASDataCheckbox.isSelected());
                    }
                });
                panel.addChild(exportEinPASDataCheckbox);

                // Textfield für die PSK Variantenauswahl anlegen
                if (iPartsRight.checkPSKInSession()) {
                    gridY++;
                    productVariantsLabel = new GuiLabel("!!PSK Varianten:");
                    productVariantsLabel.setConstraints(createLabelConstraints(gridY));
                    productVariantsLabel.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
                    productVariantsLabel.setVisible(false);
                    panel.addChild(productVariantsLabel);
                    productVariants = new iPartsGuiPSKVariantsSelectTextField(project);
                    productVariants.setConstraints(createValueConstraints(gridY));
                    productVariants.setVisible(false);
                    productVariants.init(parentForm);
                    panel.addChild(productVariants);
                }

//                // TODO DAIMLER-6685 wieder einkommentieren
//                if (exportTask.getExportType() == iPartsExportTask.ExportType.MODEL) {
//                    gridY++;
//                    label = new GuiLabel("!!Export mit freien SAs:");
//                    label.setConstraints(createLabelConstraints(gridY));
//                    label.setHorizontalAlignment(GuiLabel.HorizontalAlignment.RIGHT);
//                    panel.addChild(label);
//                    withSAs = new GuiCheckbox();
//                    withSAs.setConstraints(createValueConstraints(gridY));
//                    withSAs.setSelected(DEFAULT_EXPORT_WITH_SAS);
//                    panel.addChild(withSAs);
//                }
            }
            return panel;
        }

        @Override
        public boolean checkOkButtonResult(boolean enabled) {
            if (enabled) {
                enabled = !langSelectionBox.getSelectedLanguages().isEmpty();
            }
            return enabled;
        }

        @Override
        public void selectionChanged(DBDataObjectAttributesList newSelection) {
            if (newSelection != null) {
                boolean isSingleSelection = newSelection.size() == 1;
                if (isSingleSelection) {
                    DBDataObjectAttributes attributes = newSelection.get(0);
                    boolean isProductSelection = attributes.fieldExists(FIELD_DP_PRODUCT_NO);
                    // Die Selektion hat sich geändert
                    // -> PSK Felder anzeigen und füllen (sofern vorhanden)
                    checkSingleSelectionPSKVariantValues(attributes, isProductSelection);
                    // -> gültige Bildformate anzeigen
                    checkSingleSelectionPicFormatValues(attributes, isProductSelection);
                }
            }
        }


        /**
         * Überprüft, ob die gültigen Bildformate gesetzt sind
         *
         * @param attributes
         * @param isProductSelection
         */
        private void checkSingleSelectionPicFormatValues(DBDataObjectAttributes attributes, boolean isProductSelection) {
            if (isProductSelection) {
                iPartsProductId productId = new iPartsProductId(attributes.getFieldValue(FIELD_DP_PRODUCT_NO));
                setPicFormatsForProduct(productId);
            } else if (attributes.fieldExists(FIELD_DM_MODEL_NO)) {
                // Wir sind in der Baumusterauswahl. Wenn das BM nur ein Produkt hat, können wir hier schon prüfen, ob
                // das Produkt das Exportieren von SVGs erlaubt
                String modelNumber = attributes.getFieldValue(FIELD_DM_MODEL_NO);
                Set<String> products = iPartsProductModels.getInstance(project).getProductNumbersByModel(modelNumber);
                if (products.size() == 1) {
                    iPartsProductId productId = new iPartsProductId(products.iterator().next());
                    setPicFormatsForProduct(productId);
                } else {
                    setPicFormatValues(iPartsExportPictureFormat.ALL_FORMATS);
                }
            }
        }

        /**
         * Setzt die Bildformate in Abhängigkeit der Produkt-Eigenschaft
         *
         * @param productId
         */
        private void setPicFormatsForProduct(iPartsProductId productId) {
            iPartsProduct product = iPartsProduct.getInstance(project, productId);
            // Prüfen, ob das Produkt SVGs exportieren darf
            Collection<iPartsExportPictureFormat> values;
            if (product.isUseSVGs()) {
                values = iPartsExportPictureFormat.ALL_FORMATS;
            } else {
                values = iPartsExportPictureFormat.NON_SVG_FORMATS;
            }
            setPicFormatValues(values);
        }

        private void setPicFormatValues(Collection<iPartsExportPictureFormat> values) {
            // Ist der aktuelle Inhalt schon passend, muss nichts angepasst werden
            if ((values.size() != pictureExportType.getItemCount()) || !values.containsAll(pictureExportType.getUserObjects())) {
                Session.invokeThreadSafeInSession(() -> {
                    iPartsExportPictureFormat selectedPictureExportFormat = pictureExportType.getSelectedUserObject();
                    if (!values.contains(selectedPictureExportFormat)) {
                        selectedPictureExportFormat = null;
                    }
                    pictureExportType.removeAllItems();
                    for (iPartsExportPictureFormat value : values) {
                        pictureExportType.addItem(value, TranslationHandler.translate(value.getDisplayValue()));
                    }
                    pictureExportType.setSelectedUserObject((selectedPictureExportFormat != null) ? selectedPictureExportFormat
                                                                                                  : iPartsExportPictureFormat.PNG);
                });
            }
        }

        /**
         * Überprüft, ob die PSK Varianten gesetzt sind (sofern welche vorhanden sind)
         *
         * @param attributes
         * @param isProductSelection
         */
        private void checkSingleSelectionPSKVariantValues(DBDataObjectAttributes attributes, boolean isProductSelection) {
            if (iPartsRight.checkPSKInSession()) {
                // Benutzer hat das Recht
                if (isProductSelection) {
                    // Wir haben den Auswahldialog für Produkte
                    String product = attributes.getFieldValue(FIELD_DP_PRODUCT_NO);
                    iPartsProductId productId = new iPartsProductId(product);
                    if (StrUtils.isValid(product) && iPartsProduct.getInstance(project, productId).isPSK()) {
                        // Produkt ist gültig und PSK -> Varianten eingeben
                        Session.invokeThreadSafeInSession(() -> {
                            productVariants.setProductId(productId);
                            resetVariantsTextfield(true);
                        });
                        return;
                    }
                }
                // Wir sind entweder nicht im Produktdialog oder das Produkt ist nicht gültig oder das Produkt ist kein
                // PSK Produkt -> Felder verstecken
                resetVariantsTextfield(false);
            }
        }

        /**
         * Setzt das Textfield für die PSK Varianten zurück
         *
         * @param showTextField
         */
        private void resetVariantsTextfield(boolean showTextField) {
            productVariants.setText("");
            productVariants.setArray(null);
            productVariants.setVisible(showTextField);
            productVariantsLabel.setVisible(showTextField);
        }

        protected ConstraintsGridBag createLabelConstraints(int gridy) {
            return new ConstraintsGridBag(0, gridy, 1, 1, 0.0, 0.0,
                                          ConstraintsGridBag.ANCHOR_EAST, ConstraintsGridBag.FILL_NONE,
                                          4, 8, 4, 4);
        }

        protected ConstraintsGridBag createValueConstraints(int gridy) {
            return new ConstraintsGridBag(1, gridy, 1, 1, 100.0, 0.0,
                                          ConstraintsGridBag.ANCHOR_CENTER, ConstraintsGridBag.FILL_HORIZONTAL,
                                          4, 4, 4, 8);
        }

        public iPartsExportPictureFormat getPictureExportType() {
            return pictureExportType.getSelectedUserObject();
        }

        public List<Language> getSelectedLanguages() {
            return langSelectionBox.getSelectedLanguages();
        }

        public Set<String> getProductVariants() {
            return new HashSet<>(productVariants.getArray().getArrayAsStringList());
        }

        public boolean isExportAdditionalPartData() {
            return exportAdditionalPartData;
        }

        public void setExportAdditionalPartData(boolean exportAdditionalPartData) {
            this.exportAdditionalPartData = exportAdditionalPartData;
        }

        public boolean isExportEinPASData() {
            return exportEinPASData;
        }

        public void setExportEinPASData(boolean exportEinPASData) {
            this.exportEinPASData = exportEinPASData;
        }
    }
}