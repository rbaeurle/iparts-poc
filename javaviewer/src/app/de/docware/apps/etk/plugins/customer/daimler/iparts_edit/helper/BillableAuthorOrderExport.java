/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.db.EtkRevisionsHelper;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.MessageLogOption;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsAuthorOrderStatus;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsDataWorkOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.bst.iPartsWorkOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableContentId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsColorTableFactoryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsFactoryDataId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsFootNoteCatalogueRefId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsCombTextId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.order.iPartsPicOrderModulesId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.replace.iPartsReplacePartId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsDocuRelevant;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogBCTEPrimaryKey;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsDataPicOrderUsageList;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.dbobjects.order.iPartsPicOrderId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsAuthorOrderExportFormatter;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsAuthorOrderExportObject;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.serialization.SerializedDBDataObject;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectAttribute;
import de.docware.framework.modules.db.serialization.SerializedDBDataObjectState;
import de.docware.framework.modules.db.serialization.SerializedEtkMultiSprache;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.MessageLogType;
import de.docware.framework.modules.gui.misc.MimeTypes;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.misc.id.IdWithType;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Helfer zum Exportieren von abrechnungsrelevanten Objektinformationen von Autorenaufträgen
 */
public class BillableAuthorOrderExport implements iPartsConst {

    public static final String FIELD_FOR_PIC_ORDER_TYPE = "PO_IS_CHANGE_ORDER";
    public static final String FIELD_FOR_PIC_ORDER_PRODUCT = "PO_PRODUCT";
    public static final String FIELD_FOR_EDS_PRODUCT = "EDS_PRODUCT";
    public static final String CONTENT_ID_DELIMITER = "%";
    private static final String FILENAME_SUFFIX = "iPartsAuthorOrder";
    private static final String FILENAME_PREFIX_NOT_APPROVED_AO = "Forecast";
    private static final Set<String> FIELDS_TO_SKIP = new HashSet<>();

    static {
        FIELDS_TO_SKIP.add(FIELD_DFD_STATUS);
        FIELDS_TO_SKIP.add(FIELD_DRP_STATUS);
        FIELDS_TO_SKIP.add(FIELD_DCCF_STATUS);
    }

    /**
     * Filtert abrechnungsrelevante Bildaufträge aus. Neuerstellungen haben immer Vorrang vor Änderungsaufträgen. Ungültige
     * Bildaufträge werden nicht ausgegeben. Wird ein Auftrag "ungültig" gesetzt, dann wird der nächst-jüngere Auftrag
     * ausgegeben.
     *
     * @param picOrders
     * @return
     */
    public static iPartsDataPicOrderList filterBillablePicOrders(List<iPartsDataPicOrder> picOrders, boolean forJSONMessage) {
        Map<String, iPartsDataPicOrder> idToPicOrders = new HashMap<>(); // Map mit GUID auf Bildauftrag
        Map<String, iPartsDataPicOrder> newlyCreatedPicOrders = new HashMap<>(); // Map mit GUID auf Neuerstellungen im Changeset
        Map<String, List<String>> idOrdered = new HashMap<>(); // Map mit Vorgänger Bildauftrag auf Nachfolger Bildauftrag und Kopien auf Original
        Map<String, iPartsDataPicOrder> invalidPicOrders = new HashMap<>(); // Map mit allen Bildaufträgen, die auf "ungültig" gesetzt wurden
        for (iPartsDataPicOrder picOrder : picOrders) {
            if (picOrder.existsInDB()) {
                // Zuordnung ID zu Bildauftrag
                idToPicOrders.put(picOrder.getAsId().getOrderGuid(), picOrder);

                // Wurde der Bildauftrag auf "ungültig" gesetzt oder storniert?
                if (picOrder.isInvalid() || picOrder.isCancelled()) {
                    invalidPicOrders.put(picOrder.getAsId().getOrderGuid(), picOrder);
                }
                // Handelt es sich bei dem Bildauftrag um eine Neuerstellung?
                if (picOrder.getOriginalPicOrder().isEmpty() && picOrder.getOriginalOrderForCopy().isEmpty()) {
                    newlyCreatedPicOrders.put(picOrder.getAsId().getOrderGuid(), picOrder);
                }
                // Vorgänger Bildauftrag auf Nachfolger Bildauftrag sammeln
                if (!picOrder.hasFakeOriginalPicOrder() && !picOrder.getOriginalPicOrder().isEmpty()) {
                    List<String> orderIdList = idOrdered.computeIfAbsent(picOrder.getOriginalPicOrder(), k -> new ArrayList<>());
                    orderIdList.add(picOrder.getAsId().getOrderGuid());
                }

                // Die Kopien sammeln
                if (!picOrder.getOriginalOrderForCopy().isEmpty()) {
                    List<String> orderIdList = idOrdered.computeIfAbsent(picOrder.getOriginalOrderForCopy(), k -> new ArrayList<>());
                    orderIdList.add(picOrder.getAsId().getOrderGuid());
                }
            }
        }

        // Die ungültigen Bildaufträge sollen entfernt werden. Vorgänger müssen stehen bleiben, weil diese
        // an BST versorgt werden sollen.
        for (Map.Entry<String, iPartsDataPicOrder> invalidOrder : invalidPicOrders.entrySet()) {
            idToPicOrders.remove(invalidOrder.getKey());
            // Wurde eine Neuerstellung auf "ungültig" gesetzt, dann muss das auch berücksichtigt werden
            newlyCreatedPicOrders.remove(invalidOrder.getKey());
        }

        // Durchlaufe alle Neuerstellungen und entferne alle nachfolgenden Änderungsaufträge, weil bei einer
        // Neuerstellung nur diese abgerechnet werden
        for (Map.Entry<String, iPartsDataPicOrder> newlyCreatedPicOrder : newlyCreatedPicOrders.entrySet()) {
            removeFollowingPicOrders(idToPicOrders.get(newlyCreatedPicOrder.getKey()), idToPicOrders, idOrdered);
        }

        iPartsDataPicOrderList picOrderList = new iPartsDataPicOrderList();
        // Durchlaufe alle übriggebliebenen Bildaufträge und sammle nur die neuesten auf (nur diese werden abgerechnet)
        for (Map.Entry<String, iPartsDataPicOrder> picOrder : idToPicOrders.entrySet()) {
            List<String> nextPicOrders = idOrdered.get(picOrder.getKey());
            if ((nextPicOrders != null) && !nextPicOrders.isEmpty()) {
                boolean con = false;
                for (String nextPicOrder : nextPicOrders) {
                    if (StrUtils.isValid(nextPicOrder) && !invalidPicOrders.containsKey(nextPicOrder)) {
                        // Bildauftrag hat einen gültigen Nachfolger -> nicht abrechnen
                        con = true;
                        break;
                    }
                }
                if (con) {
                    continue;
                }
            }
            iPartsDataPicOrder dataPicOrder = picOrder.getValue();
            // Bei gültigen Bildaufträgen muss die Info dazu, ob es sich um einen neuen oder einen
            // geänderten Bildauftrag handelt. Hierfür wird ein künstliches Attribut angelegt mit
            // dem Hinweis, ob es ein neuer Bildauftrag oder ein Änderungsauftrag ist.
            DBDataObjectAttribute attribute = new DBDataObjectAttribute(FIELD_FOR_PIC_ORDER_TYPE, DBDataObjectAttribute.TYPE.STRING, false);
            // Im JSON an BST soll nur via true/false ausgegeben werden, ob es ein Änderungsauftrag ist. Bei der
            // Abrechnungsfunktion soll der Typ in Textform ausgegeben werden.
            if (forJSONMessage) {
                attribute.setValueAsBoolean(dataPicOrder.isChangeOrCopy(), DBActionOrigin.FROM_DB);
            } else {
                String value = dataPicOrder.isChangeOrCopy() ? "!!Änderungsauftrag" : "!!Neuanlage";
                attribute.setValueAsString(TranslationHandler.translate(value), DBActionOrigin.FROM_DB);
            }
            dataPicOrder.getAttributes().addField(attribute, DBActionOrigin.FROM_DB);

            // Bei gültigen Bildaufträgen muss die Info dazu, in welchem Produkt er liegt
            // Hierfür wird ein künstliches Attribut angelegt mit der Produktnummer
            attribute = new DBDataObjectAttribute(FIELD_FOR_PIC_ORDER_PRODUCT, DBDataObjectAttribute.TYPE.STRING, false);
            // Product bestimmen
            String productNo = "";
            iPartsDataPicOrderUsageList usageList = dataPicOrder.getUsages();
            if (!usageList.isEmpty()) {
                productNo = usageList.get(0).getAsId().getProductNo();
            }
            attribute.setValueAsString(productNo, DBActionOrigin.FROM_DB);
            dataPicOrder.getAttributes().addField(attribute, DBActionOrigin.FROM_DB);

            dataPicOrder.getAttributes().markAsModified();
            picOrderList.add(dataPicOrder, DBActionOrigin.FROM_EDIT);
        }
        return picOrderList;
    }

    /**
     * Entfernt rekursiv alle nachfolgenden Bildaufträge aus der <code>idToPicOrders</code> Map
     *
     * @param picOrder
     * @param idToPicOrders
     * @param idOrdered
     */
    private static void removeFollowingPicOrders(iPartsDataPicOrder picOrder, Map<String, iPartsDataPicOrder> idToPicOrders,
                                                 Map<String, List<String>> idOrdered) {
        List<String> followingGUIDs = idOrdered.remove(picOrder.getAsId().getOrderGuid());
        if (followingGUIDs != null) {
            for (String followingGUID : followingGUIDs) {
                if (StrUtils.isValid(followingGUID)) {
                    iPartsDataPicOrder followingPicOrder = idToPicOrders.remove(followingGUID);
                    if (followingPicOrder != null) {
                        removeFollowingPicOrders(followingPicOrder, idToPicOrders, idOrdered);
                    }
                }
            }
        }
    }

    private final EtkProject project;
    private final iPartsNumberHelper numberHelper = new iPartsNumberHelper();
    private boolean isCancelled;
    private final BillableDataCache billableDataCache;
    private final Map<AssemblyId, Boolean> isEDSAssemblyCache = new HashMap<>();
    private final Set<IdWithType> autoCreatedPartListEntries = new HashSet<>();
    private final Set<IdWithType> wasAutoCreatedPartListEntries = new HashSet<>();

    public BillableAuthorOrderExport(EtkProject project) {
        this.project = project;
        this.billableDataCache = BillableDataCache.getInstance(getProject());
    }

    public EtkProject getProject() {
        return project;
    }

    /**
     * Durchläuft alle übergebenen Autorenaufträge und exportiert die abrechnungsrelevanten Objektinformationen in eine
     * CSV Datei.
     *
     * @param selectedAuthorOrders
     */
    public void exportBillableData(final List<iPartsDataAuthorOrder> selectedAuthorOrders) {
        EtkRevisionsHelper revisionsHelper = getProject().getRevisionsHelper();
        if ((revisionsHelper == null) || (selectedAuthorOrders == null) || selectedAuthorOrders.isEmpty()) {
            return;
        }
        final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Abrechnungsprozess", "Suche abrechnungsrelevante Objektinformationen", null);
        messageLogForm.getButtonPanel().addEventListener(new EventListener(GuiButtonPanel.BUTTON_CANCEL_ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                isCancelled = true;
            }
        });
        messageLogForm.showModal(new FrameworkRunnable() {

            @Override
            public void run(FrameworkThread thread) {

                messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Starte Export von %1 " +
                                                                                        "ausgewählten Autorenaufträgen",
                                                                                        String.valueOf(selectedAuthorOrders.size())),
                                                           MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                List<iPartsAuthorOrderExportObject> exportObjects = new ArrayList<>();
                boolean isForecast = false;
                // Selektierte Aufträge durchgehen
                for (iPartsDataAuthorOrder authorOrder : selectedAuthorOrders) {
                    if (isCancelled) {
                        return;
                    }
                    String authorOrderName = authorOrder.getAuthorOrderName();

                    String bstId = authorOrder.getBstId();
                    // Sobald in der Selektion ein nicht freigegebener Autorenauftrag enthalten ist, erhält die Datei den Präfix "Forecast"
                    if (!isForecast && !iPartsAuthorOrderStatus.isEndState(authorOrder.getStatus())) {
                        isForecast = true;
                    }
                    if (bstId.isEmpty()) {
                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Der Autorenauftrag \"%1\"" +
                                                                                                " wird übersprungen aufgrund fehlender BST-ID",
                                                                                                authorOrderName),
                                                                   MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                        continue;
                    }

                    // Existiert der Bearbeitungsauftrag in der DB?
                    iPartsDataWorkOrder dataWorkOrder = new iPartsDataWorkOrder(getProject(), new iPartsWorkOrderId(bstId));
                    if (!dataWorkOrder.existsInDB()) {
                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Der Autorenauftrag \"%1\"" +
                                                                                                " wird übersprungen aufgrund " +
                                                                                                "fehlenden Bearbeitungsauftrag " +
                                                                                                "in der DB",
                                                                                                authorOrderName),
                                                                   MessageLogType.tmlWarning, MessageLogOption.TIME_STAMP);
                        continue;
                    }

                    autoCreatedPartListEntries.clear();
                    wasAutoCreatedPartListEntries.clear();
                    iPartsRevisionChangeSet revisionChangeSet = new iPartsRevisionChangeSet(authorOrder.getChangeSetId(), getProject());
                    revisionsHelper.executeWithoutActiveChangeSets(() -> {
                        // ChangeSet temporär aktivieren (am Ende von executeWithoutActiveChangeSets() wird ein evtl.
                        // anderes bereits aktives ChangeSet wieder aktiviert)
                        List<iPartsRevisionChangeSet> changeSets = new ArrayList<>(1);
                        changeSets.add(revisionChangeSet);
                        revisionsHelper.setActiveRevisionChangeSets(changeSets, revisionChangeSet, false, project);

                        Map<IdWithType, SerializedDBDataObject> mergedObjects = revisionChangeSet.getMergedSerializedDataObjects(false, true, iPartsRevisionChangeSet.SYSTEM_USER_IDS);
                        ExportContainer container = new ExportContainer();
                        // Alle PartListEntryIds sammeln, die noch ein "Auto-Created"-Flag haben
                        buildAutoCreatedPartListEntryLists(mergedObjects);

                        // Alle serialisierten Objekte durchlaufen und nach abrechnungsrelevanten Objekten suchen
                        for (Map.Entry<IdWithType, SerializedDBDataObject> objectEntry : mergedObjects.entrySet()) {
                            if (isCancelled) {
                                return;
                            }
                            IdWithType id = objectEntry.getKey();
                            SerializedDBDataObject serializedDBDataObject = objectEntry.getValue();

                            if (billableDataCache.isIdTypeBillable(id)) {
                                switch (id.getType()) {
                                    case PartListEntryId.TYPE:
                                        handlePartListEntry(serializedDBDataObject, container);
                                        break;
                                    case iPartsCombTextId.TYPE:
                                        handleCombText(id, serializedDBDataObject, container, mergedObjects);
                                        break;
                                    case iPartsFootNoteCatalogueRefId.TYPE:
                                        handleFootNote(id, serializedDBDataObject, container, mergedObjects);
                                        break;
                                    case iPartsReplacePartId.TYPE:
                                        handleReplacement(id, serializedDBDataObject, container, mergedObjects);
                                        break;
                                    case iPartsDialogId.TYPE:
                                        handleConstructionData(id, serializedDBDataObject, container);
                                        break;
                                    case iPartsPicOrderModulesId.TYPE:
                                        handlePicOrders(id, container);
                                        break;
                                    case iPartsFactoryDataId.TYPE:
                                        handlePartListEntryFactoryData(id, serializedDBDataObject, container);
                                        break;
                                    case iPartsColorTableContentId.TYPE:
                                        handleColorTableContent(id, serializedDBDataObject, container);
                                        break;
                                    case iPartsColorTableFactoryId.TYPE:
                                        handleColorTableFactoryData(id, serializedDBDataObject, container);
                                        break;
                                }
                            }
                        }

                        // Restliche kombinierten Texte verarbeiten
                        processRemainingCombTexts(container, mergedObjects);

                        // Nachdem alle Objekte aufgenommen wurden, bestimmen wir die Objekte, die rein durch eine
                        // Statusänderung exportiert werden
                        Set<String> objectsIdsWithStatusChange = container.checkDataWithStatusChanges();

                        // Export Objekt für Exporter erzeugen
                        iPartsAuthorOrderExportObject authorOrderExportObject = new iPartsAuthorOrderExportObject(project, authorOrder, dataWorkOrder);
                        // Statusänderungen durchgeben
                        authorOrderExportObject.setStatusChangedData(objectsIdsWithStatusChange);
                        if (container.hasBCTEKeys()) {
                            authorOrderExportObject.setBcteKeyToAutoCreatedMap(container.getBcteKeyToAutoCreatedMap());
                        }
                        if (container.hasBCTEAutoStateKeys()) {
                            authorOrderExportObject.setBcteKeyForAutoStateMap(container.getBcteKeyForAutoStateMap());
                        }
                        if (container.hasPartListEntryIdsForExport()) {
                            authorOrderExportObject.addPartListEntryIds(container.getPartListEntryIdsForExport());
                        }

                        // Aufgesammelte Bildaufträge filtern und anhängen
                        processCollectedPicOrders(authorOrderExportObject, container);
                        // Aufgesammelte Varianten zu Variantentabellen anhängen
                        if (container.hasColorTableContents()) {
                            authorOrderExportObject.addColorTableContents(container.getColorTableContentIds());
                        }
                        if (authorOrderExportObject.isEmpty()) {
                            messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Der Autorenauftrag \"%1\"" +
                                                                                                    " enthält keine abrechnungsrelevante" +
                                                                                                    " Objektinformationen und " +
                                                                                                    "wird nicht exportiert",
                                                                                                    authorOrderName),
                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                        } else {
                            String prefixText = TranslationHandler.translate("!!Der Autorenauftrag \"%1\" enthält folgende abrechnungsrelevante Objekte:",
                                                                             authorOrderName);
                            messageLogForm.getMessageLog().fireMessage(prefixText + buildAdditionalText(authorOrderExportObject),
                                                                       MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                            exportObjects.add(authorOrderExportObject);
                        }
                    }, false, getProject());
                }
                // hier die Daten an den Exporter übergeben
                if (!exportObjects.isEmpty()) {
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Exportiere alle aufgesammelten Objektinformationen..."),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);

                    // Für nicht freigegebene Autorenaufträge bekommt der Dateinname einen Prefix
                    if (isForecast) {
                        createAndSaveCSV(exportObjects, messageLogForm, getFileNameWithPrefix());
                    } else {
                        createAndSaveCSV(exportObjects, messageLogForm, getFileName());
                    }

                } else {
                    messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Keine abrechnungsrelevanten Objektinformationen gefunden"),
                                                               MessageLogType.tmlMessage, MessageLogOption.TIME_STAMP);
                }
                if (isCancelled) {
                    messageLogForm.getMessageLog().fireMessageWithSeparators(TranslationHandler.translate("!!Export abgebrochen"),
                                                                             MessageLogOption.TIME_STAMP);
                } else {
                    messageLogForm.getMessageLog().fireMessageWithSeparators(TranslationHandler.translate("!!Export beendet"),
                                                                             MessageLogOption.TIME_STAMP);
                }
            }

            private void buildAutoCreatedPartListEntryLists(Map<IdWithType, SerializedDBDataObject> mergedObjects) {
                // Alle PartListEntryIds sammeln, die noch ein "Auto-Created"-Flag haben
                for (Map.Entry<IdWithType, SerializedDBDataObject> objectEntry : mergedObjects.entrySet()) {
                    SerializedDBDataObject serializedDBDataObject = objectEntry.getValue();
                    if (objectEntry.getKey().getType().equals(PartListEntryId.TYPE) && (serializedDBDataObject.getState() != SerializedDBDataObjectState.DELETED)) {
                        PartListEntryId partListEntryId = new PartListEntryId(serializedDBDataObject.getPkValues());
                        EtkDataPartListEntry partListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(), partListEntryId);
                        if (partListEntry.existsInDB()) {
                            boolean isAutoCreated = partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_AUTO_CREATED);
                            boolean wasAutoCreated = partListEntry.getFieldValueAsBoolean(iPartsConst.FIELD_K_WAS_AUTO_CREATED);
                            if (wasAutoCreated) {
                                if (isAutoCreated) {
                                    autoCreatedPartListEntries.add(partListEntryId);
                                } else {
                                    wasAutoCreatedPartListEntries.add(partListEntryId);
                                }
                            } else {
                                // Altdaten berücksichtigen (bevor es das Feld FIELD_K_WAS_AUTO_CREATED gab)
                                if (isAutoCreated) {
                                    autoCreatedPartListEntries.add(partListEntryId);
                                }
                            }
                        }
                    }
                }

            }

            private String buildAdditionalText(iPartsAuthorOrderExportObject authorOrderExportObject) {
                StringBuilder str = new StringBuilder();

                if (authorOrderExportObject.hasBCTEKeys()) {
                    str.append("\n- ");
                    String key = "!!%1 BCTE-Schlüssel";
                    str.append(TranslationHandler.translate(key, String.valueOf(authorOrderExportObject.getBcteKeyToAutoCreatedMap().size())));
                }
                if (authorOrderExportObject.hasPartListEntryIds()) {
                    str.append("\n- ");
                    String key = "!!%1 TRUCK-Schlüssel";
                    str.append(TranslationHandler.translate(key, String.valueOf(authorOrderExportObject.getPartListEntryIds().size())));
                }
                if (authorOrderExportObject.hasPicOrders()) {
                    str.append("\n- ");
                    String key = "!!%1 Bildaufträge";
                    if (authorOrderExportObject.getPicOrders().size() == 1) {
                        key = "!!%1 Bildauftrag";
                    }
                    str.append(TranslationHandler.translate(key, String.valueOf(authorOrderExportObject.getPicOrders().size())));
                }
                if (authorOrderExportObject.hasColorTableContentIds()) {
                    str.append("\n- ");
                    String key = "!!%1 Varianten zu Variantentabellen";
                    if (authorOrderExportObject.getColorTableContentIds().size() == 1) {
                        key = "!!%1 Variante zu Variantentabellen";
                    }
                    str.append(TranslationHandler.translate(key, String.valueOf(authorOrderExportObject.getColorTableContentIds().size())));
                }
                return str.toString();
            }
        });
    }

    /**
     * Verarbeitet abrechnungsrelevante Werksdaten zu Varainteninhalten und Teil zu Variantentabelle Beziehungen
     *
     * @param id
     * @param serializedDBDataObject
     * @param container
     */
    private void handleColorTableFactoryData(IdWithType id, SerializedDBDataObject serializedDBDataObject, ExportContainer container) {
        if ((id == null) || !id.getType().equals(iPartsColorTableFactoryId.TYPE)) {
            return;
        }
        iPartsColorTableFactoryId colorTableFactoryId = new iPartsColorTableFactoryId(id.toStringArrayWithoutType());
        iPartsColorTableContentId colorTableContentId = new iPartsColorTableContentId(colorTableFactoryId.getTableId(), colorTableFactoryId.getPos(), colorTableFactoryId.getSdata());
        if (container.containsColorTableContentId(colorTableContentId)) {
            return;
        }
        // Neue oder gelöschte Varianten zu Variantentabellen müssen direkt aufgenommen werde.
        if (isSerializedStateNewOrDeleted(serializedDBDataObject)) {
            container.addColorTableContentId(colorTableContentId);
            return;
        }

        // Bei einem "modifizierten" Eintrag wird geprüft, ob die Quelle "IPARTS" ist und ob es geänderte Attribute gibt
        Collection<SerializedDBDataObjectAttribute> attributes = serializedDBDataObject.getAttributes();
        if ((attributes != null) && isSerializedStateModified(serializedDBDataObject)) {
            if (isCreatedInIParts(serializedDBDataObject, FIELD_DCCF_SOURCE)) {
                for (SerializedDBDataObjectAttribute attribute : attributes) {
                    if (billableDataCache.isAttributeBillable(id, attribute.getName()) && attributeChanged(attribute)) {
                        container.addColorTableContentId(colorTableContentId);
                        // nach der ersten relevanten Änderung kann man aufhören
                        return;
                    }
                }
            }
            // Relevante Statusänderungen unabhängig von der Quelle prüfen
            // Gab es eine Änderung am Status, dann muss das separat festgehalten werden.
            // Bedingungen:
            // - es geht nur um das Status-Feld
            // - es muss ein Attribut sein, dass konfiguriert wurde (DB Tabelle)
            // - es muss sich geändert haben
            // - die Farb-Werksdaten dürfen nur vom 9er Typ sein (VX9 und WX9 bzw X9P)
            // - es muss einen Wechsel von "neu" auf "freigegeben" oder "nicht relevant" gegeben haben
            Optional<SerializedDBDataObjectAttribute> validStatusAttribute
                    = attributes
                    .stream()
                    .filter(attribute -> {
                        if (attribute.getName().equals(FIELD_DCCF_STATUS) && billableDataCache.isAttributeBillable(id, attribute.getName())
                            && attributeChanged(attribute, false)) {
                            // Bei Farben sind nur die Statusänderungen an Werksdaten zu VX9 und WX9 (Farbtabelleninhalt) relevant
                            iPartsFactoryDataTypes dataType = iPartsFactoryDataTypes.getTypeByDBValue(colorTableFactoryId.getDataId());
                            if ((dataType == iPartsFactoryDataTypes.COLORTABLE_CONTENT) || (dataType == iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS)) {
                                return hasValidStatusChange(attribute);
                            }
                        }
                        return false;
                    }).findFirst();
            // Falls der Statuswert verändert wurde, hier die ContentId merken
            validStatusAttribute.ifPresent(statusAttribute -> container.addContentIdWithStatusChange(colorTableContentId));

        }
    }

    /**
     * Berechnet den Statusübergang des übergebenen {@link SerializedDBDataObjectAttribute} und liefert zurück, ob es ein
     * gültiger Übergang war: "neu" zu "freigegeben" oder "nicht relevant"
     *
     * @param attribute
     * @return
     */
    private boolean hasValidStatusChange(SerializedDBDataObjectAttribute attribute) {
        // Es sollen nur die Übergänge von "neu" zu "freigegeben" oder "nicht relevant" dokumentiert werden
        iPartsDataReleaseState previousState = iPartsDataReleaseState.getTypeByDBValue(attribute.getOldValue());
        iPartsDataReleaseState newState = iPartsDataReleaseState.getTypeByDBValue(attribute.getValue());
        return (previousState == iPartsDataReleaseState.NEW)
               && ((newState == iPartsDataReleaseState.RELEASED)
                   || (newState == iPartsDataReleaseState.NOT_RELEVANT));
    }

    /**
     * Verarbeitet abrechnungsrelevante Varianten zu Variantentabellen Datensätze
     *
     * @param id
     * @param serializedDBDataObject
     * @param container
     */
    private void handleColorTableContent(IdWithType id, SerializedDBDataObject serializedDBDataObject, ExportContainer container) {
        if ((id == null) || !id.getType().equals(iPartsColorTableContentId.TYPE)) {
            return;
        }
        // Neue oder gelöschte Varianten zu Variantentabellen müssen direkt aufgenommen werde.
        iPartsColorTableContentId colorTableContentId = new iPartsColorTableContentId(id.toStringArrayWithoutType());
        if (isSerializedStateNewOrDeleted(serializedDBDataObject)) {
            container.addColorTableContentId(colorTableContentId);
            return;
        }
        // Bei modifizierten Datensätzen müssen abhängig von der Quelle bestimmte Felder geprüft werden. Hat sich eines
        // dieser Felder geändert, muss die Tabellennummer, die Position und das KEM-ab Datum übernommen werden (alle
        // Attribute sind in der ID enthalten)
        Collection<SerializedDBDataObjectAttribute> attributes = serializedDBDataObject.getAttributes();
        if ((attributes != null) && isSerializedStateModified(serializedDBDataObject)) {
            for (SerializedDBDataObjectAttribute attribute : attributes) {
                if (billableDataCache.isAttributeBillable(id, attribute.getName()) && attributeChanged(attribute)) {
                    container.addColorTableContentId(colorTableContentId);
                    return;
                }
            }
        }
    }

    /**
     * Verarbeitet abrechnungsrelevante Ersetzungen und übergibt die dazugehörigen {@link iPartsDialogBCTEPrimaryKey} Objekte
     *
     * @param id
     * @param serializedDBDataObject
     * @param container
     * @param mergedObjects
     */
    private void handleReplacement(IdWithType id, SerializedDBDataObject serializedDBDataObject,
                                   ExportContainer container, Map<IdWithType, SerializedDBDataObject> mergedObjects) {
        if ((id == null) || !id.getType().equals(iPartsReplacePartId.TYPE)) {
            return;
        }
        // Neue oder gelöschte Ersetzungen müssen direkt aufgenommen werden falls Quelle iParts.
        if ((isSerializedStateNewOrDeleted(serializedDBDataObject) || isSerializedStateReplaced(serializedDBDataObject))
            && isCreatedInIParts(serializedDBDataObject, FIELD_DRP_SOURCE)) {
            addReplaceUsedPartListEntryAndIdsOrBCTEKeysForExport(serializedDBDataObject, id, container, mergedObjects);
            return;
        }
        // Bei geänderten Ersetzungen wird geprüft, ob sich die Attribute verändert haben (Hauptsächlich RFMEA und RFMEN).
        // Ist das der Fall, wird der BCTE Schlüssel bzw. die PartListEntryId des Nachfolgers aufgenommen
        Collection<SerializedDBDataObjectAttribute> attributes = serializedDBDataObject.getAttributes();
        if ((attributes != null) && isSerializedStateModified(serializedDBDataObject)) {
            if (isCreatedInIParts(serializedDBDataObject, FIELD_DRP_SOURCE)) {
                for (SerializedDBDataObjectAttribute attribute : attributes) {
                    // Check, ob richtige Felder und ob sich der Inhalt geändert hat
                    if (billableDataCache.isAttributeBillable(id, attribute.getName()) && attributeChanged(attribute)) {
                        addReplaceUsedPartListEntryAndIdsOrBCTEKeysForExport(serializedDBDataObject, id, container, mergedObjects);
                        return;
                    }
                }
            }


            // Relevante Statusänderungen unabhängig von der Quelle prüfen
            // Gab es eine Änderung am Status, dann muss das separat festgehalten werden.
            // Bedingungen:
            // - es geht nur um das Status-Feld
            // - es muss ein Attribut sein, dass konfiguriert wurde (DB Tabelle)
            // - es muss sich geändert haben
            // - Es muss einen Wechsel von "neu" auf "freigegeben" oder "nicht relevant" gegeben haben
            Optional<SerializedDBDataObjectAttribute> validStatusAttribute
                    = attributes
                    .stream()
                    .filter(attribute -> attribute.getName().equals(FIELD_DRP_STATUS)
                                         && billableDataCache.isAttributeBillable(id, attribute.getName())
                                         && attributeChanged(attribute, false)
                                         && hasValidStatusChange(attribute))
                    .findFirst();
            // Wenn das Statusfeld alle Bedingungen erfüllt, muss der BCTE Schlüssel des Nachfolgers ausgegeben werden
            validStatusAttribute.ifPresent(attribute -> {
                PartListEntryId successorEntryId = getSuccessorPartListEntryId(serializedDBDataObject, id);
                if (successorEntryId != null) {
                    iPartsDialogBCTEPrimaryKey bcteKey = getBCTEKeyFromMergedObjects(mergedObjects, successorEntryId);
                    if (bcteKey != null) {
                        container.addBCTEKeyWithStatusChange(bcteKey);
                    }
                }
            });


        }
    }

    /**
     * Bestimmt den BCTE Schlüssel bzw. die PartListEntryId des Nachfolgers der übergebenen Ersetzung und nimmt diesen in dem Set
     * für den Export auf. Zusätzlich wird die Stücklistenposition durch das Aufnehmen in <code>usedPartListEntryIds</code>
     * als "verarbeitet" markiert.
     *
     * @param serializedDBDataObject
     * @param id
     * @param container
     * @param mergedObjects
     */
    private void addReplaceUsedPartListEntryAndIdsOrBCTEKeysForExport(SerializedDBDataObject serializedDBDataObject, IdWithType id,
                                                                      ExportContainer container, Map<IdWithType, SerializedDBDataObject> mergedObjects) {
        PartListEntryId partListEntryId = getSuccessorPartListEntryId(serializedDBDataObject, id);
        if ((partListEntryId != null) && !container.containsUsedPartListEntryId(partListEntryId)) {
            addUsedPartListEntryAndIdsOrBCTEKeysForExport(partListEntryId, container, mergedObjects, true);
        }
    }

    /**
     * Liefert zum Ersetzungsobjekt {@link SerializedDBDataObject} die {@link PartListEntryId des Nachfolgers}
     *
     * @param replacementSerializedDBDataObject
     * @param id
     * @return
     */
    private PartListEntryId getSuccessorPartListEntryId(SerializedDBDataObject replacementSerializedDBDataObject, IdWithType id) {
        String replaceKlfdNr = replacementSerializedDBDataObject.getAttributeValue(FIELD_DRP_REPLACE_LFDNR, true, getProject());
        if (StrUtils.isValid(replaceKlfdNr)) {
            // Stücklisteneintrag ID aus der Ersetzungs ID generieren
            return new PartListEntryId(id.getValue(1), id.getValue(2), replaceKlfdNr);
        }
        return null;
    }

    /**
     * Verarbeitet abrechnungsrelevante Werksdaten zu Stücklistenpositionen
     *
     * @param id
     * @param serializedDBDataObject
     * @param container
     */
    private void handlePartListEntryFactoryData(IdWithType id, SerializedDBDataObject serializedDBDataObject,
                                                ExportContainer container) {
        if ((id == null) || !id.getType().equals(iPartsFactoryDataId.TYPE)) {
            return;
        }

        boolean isEDS = false;
        PartListEntryId partListEntryId = null;

        // BCTE Schlüssel bestimmen
        iPartsDialogBCTEPrimaryKey bctePrimaryKey = getBcteKey(id.getValue(1));
        if (bctePrimaryKey == null) { // Kein gültiger BCTE-Schlüssel -> EDS-GUID annehmen
            // Die GUID in iPartsFactoryDataId für EDS ist folgendermaßen aufgebaut: K_Vari + _ + K_Lfdnr
            // Für die partListEntryId diese GUID wieder aufdröseln
            String edsGuid = id.getValue(1);
            partListEntryId = numberHelper.getPartListEntryIdFromEDSGuid(edsGuid);
            if (partListEntryId != null) {
                isEDS = isEDSAssembly(partListEntryId);
            }
        }
        if (((bctePrimaryKey != null) && container.isNewOrAutoCreatedBCTEKey(bctePrimaryKey)) ||
            (isEDS && !container.containsPartListEntryIdForExport(partListEntryId))) {
            // "neu" oder "gelöscht" führt sofort dazu, dass der BCTE Schlüssel bzw die PartListEntryId aufgenommen wird
            if (isSerializedStateNewOrDeleted(serializedDBDataObject)) {
                if (isEDS) {
                    container.addPartListEntryIdForExport(partListEntryId);
                } else {
                    container.addOrUpdateBCTEKeyAsNotAutoCreated(bctePrimaryKey);
                }
                return;
            }
            // Bei einem "modifizierten" Eintrag wird geprüft, ob die Quelle "IPARTS" ist und ob es geänderte Attribute gibt
            Collection<SerializedDBDataObjectAttribute> attributes = serializedDBDataObject.getAttributes();
            if ((attributes != null) && isSerializedStateModified(serializedDBDataObject)) {
                if (isCreatedInIParts(serializedDBDataObject, FIELD_DFD_SOURCE)) {
                    for (SerializedDBDataObjectAttribute attribute : attributes) {
                        if (billableDataCache.isAttributeBillable(id, attribute.getName()) && attributeChanged(attribute)) {
                            if (isEDS) {
                                container.addPartListEntryIdForExport(partListEntryId);
                            } else {
                                container.addOrUpdateBCTEKeyAsNotAutoCreated(bctePrimaryKey);
                            }
                            // nach der ersten relevanten Änderung kann man aufhören
                            return;
                        }
                    }
                }

                // Relevante Statusänderungen unabhängig von der Quelle prüfen
                // Gab es eine Änderung am Status, dann muss das separat festgehalten werden.
                Optional<SerializedDBDataObjectAttribute> validStatusAttribute
                        = attributes
                        .stream()
                        .filter(attribute -> attribute.getName().equals(FIELD_DFD_STATUS)
                                             && billableDataCache.isAttributeBillable(id, attribute.getName())
                                             && attributeChanged(attribute, false)
                                             && hasValidStatusChange(attribute)).findFirst();
                validStatusAttribute.ifPresent(attribute -> container.addBCTEKeyWithStatusChange(bctePrimaryKey));


            }
        }
    }

    private boolean isCreatedInIParts(SerializedDBDataObject serializedDBDataObject, String sourceField) {
        return iPartsImportDataOrigin.getTypeFromCode(serializedDBDataObject.getAttributeValue(sourceField, true, getProject())) == iPartsImportDataOrigin.IPARTS;
    }

    /**
     * Lädt die Bildaufträge zur übergebenen {@link iPartsPicOrderModulesId} und sammelt sie auf.
     *
     * @param id
     * @param container
     */
    private void handlePicOrders(IdWithType id, ExportContainer container) {
        if ((id == null) || !id.isValidId() || !id.getType().equals(iPartsPicOrderModulesId.TYPE)) {
            return;
        }
        iPartsDataPicOrder picOrder = new iPartsDataPicOrder(getProject(), new iPartsPicOrderId(id.getValue(1)));
        if (picOrder.existsInDB()) {
            container.addPicOrder(picOrder);
        }
    }

    /**
     * Verarbeitet abrechnungsrelevante Konstruktionspositionen und übergibt die dazugehörigen {@link iPartsDialogBCTEPrimaryKey} Objekte
     *
     * @param id
     * @param serializedDBDataObject
     * @param container
     */
    private void handleConstructionData(IdWithType id, SerializedDBDataObject serializedDBDataObject, ExportContainer container) {
        if ((id == null) || !id.getType().equals(iPartsDialogId.TYPE)) {
            return;
        }
        // BCTE Schlüssel bestimmen
        iPartsDialogBCTEPrimaryKey bctePrimaryKey = getBcteKey(id.getValue(1));
        if ((bctePrimaryKey == null) || !container.isNewOrAutoCreatedBCTEKey(bctePrimaryKey)) {
            return;
        }
        // Es sollen nur modifizierte Datensätze berücksichtigt werden
        Collection<SerializedDBDataObjectAttribute> attributes = serializedDBDataObject.getAttributes();
        if ((attributes != null) && isSerializedStateCommitted(serializedDBDataObject)) {
            for (SerializedDBDataObjectAttribute attribute : attributes) {
                if (attribute.getName().equals(FIELD_DD_DOCU_RELEVANT) && attributeChanged(attribute)) {
                    iPartsDocuRelevant docuRelevantValue = iPartsDocuRelevant.getFromDBValue(attribute.getValue());
                    if (docuRelevantValue == iPartsDocuRelevant.DOCU_RELEVANT_NO) {
                        container.addOrUpdateBCTEKeyAsNotAutoCreated(bctePrimaryKey);
                    }
                    // Das Doku-Relevanz Feld kann nur einmal vorkommen
                    return;
                }
            }
        }
    }

    private PartListEntryId handleSerializedDataObject(IdWithType id, SerializedDBDataObject serializedDBDataObject, ExportContainer container,
                                                       Map<IdWithType, SerializedDBDataObject> mergedObjects, String idType) {
        if ((id == null) || !id.getType().equals(idType)) {
            return null;
        }
        PartListEntryId partListEntryId = new PartListEntryId(id.getValue(1), id.getValue(2), id.getValue(3));

        // Hat die Position schon zur Verarbeitung eines BCTE Schlüssels geführt, dann braucht man sie nicht
        // mehr berücksichtigen.
        if (container.containsUsedPartListEntryId(partListEntryId)) {
            return null;
        }

        // Wurde eine Fußnote angelegt, geändert oder gelöscht wird ein neues Fußnotenobjekt angelegt. Somit
        // muss hier nicht zusätzlich auf "MODIFIED" geprüft werden.
        if (isSerializedStateNewOrDeleted(serializedDBDataObject) || isSerializedStateReplaced(serializedDBDataObject)) {
            addUsedPartListEntryAndIdsOrBCTEKeysForExport(partListEntryId, container, mergedObjects, true);
            return null;
        }

        return partListEntryId;
    }

    /**
     * Verarbeitet abrechnungsrelevante Fußnoten und übergibt die dazugehörigen {@link iPartsDialogBCTEPrimaryKey} Objekte bzw. {@link PartListEntryId}s.
     *
     * @param id
     * @param serializedDBDataObject
     * @param container
     * @param mergedObjects
     */
    private void handleFootNote(IdWithType id, SerializedDBDataObject serializedDBDataObject, ExportContainer container,
                                Map<IdWithType, SerializedDBDataObject> mergedObjects) {
        handleSerializedDataObject(id, serializedDBDataObject, container, mergedObjects, iPartsFootNoteCatalogueRefId.TYPE);
    }

    /**
     * Verarbeitet abrechnungsrelevante kombinierte Texte und übergibt die dazugehörigen {@link iPartsDialogBCTEPrimaryKey} Objekte bzw. PartListEntryIds
     *
     * @param id
     * @param serializedDBDataObject
     * @param container
     * @param mergedObjects
     */
    private void handleCombText(IdWithType id, SerializedDBDataObject serializedDBDataObject,
                                ExportContainer container, Map<IdWithType, SerializedDBDataObject> mergedObjects) {
        PartListEntryId partListEntryId = handleSerializedDataObject(id, serializedDBDataObject, container, mergedObjects,
                                                                     iPartsCombTextId.TYPE);
        if (partListEntryId == null) {
            return;
        }

        // Bei modifizierten kombinierten Texten kann erst am Ende geprüft werden, ob sie abrechnungsrelevant
        // sind
        if (isSerializedStateModified(serializedDBDataObject)) {
            Set<SerializedDBDataObject> combTexts = container.getCombTextForPartListEntryId(partListEntryId);
            if (combTexts == null) {
                combTexts = new HashSet<>();
                container.addCombTextForPartListEntryId(partListEntryId, combTexts);
            }
            // kombinierten Texte sammeln, um später zu prüfen, ob "nur" die Reihenfolge geändert wurde
            combTexts.add(serializedDBDataObject);
        }
    }

    /**
     * Verarbeitet abrechnungsrelevante Stücklistenpositionen basierend auf vorgeschriebenen AS Attributen
     *
     * @param serializedDBDataObject
     * @param container
     */
    private void handlePartListEntry(SerializedDBDataObject serializedDBDataObject, ExportContainer container) {
        if (serializedDBDataObject == null) {
            return;
        }
        PartListEntryId partListEntryId = new PartListEntryId(serializedDBDataObject.getPkValues());

        // Hat die Position schon zur Verarbeitung geführt, dann braucht man sie nicht
        // mehr berücksichtigen. Das AutoCreated-Flag wurde für diese Position ebenfalls schon berücksichtigt.
        if (container.containsUsedPartListEntryId(partListEntryId)) {
            return;
        }
        Collection<SerializedDBDataObjectAttribute> attributes = serializedDBDataObject.getAttributes();
        if (attributes == null) {
            return;
        }

        // BCTE-Schlüssel bzw. EDS-Schlüssel bestimmen
        iPartsDialogBCTEPrimaryKey bctePrimaryKey = getBcteKey(serializedDBDataObject);

        boolean isEDS = false;
        if (bctePrimaryKey == null) {
            isEDS = isEDSAssembly(partListEntryId);
        }

        // Für geänderte Attribute in EDS wird sich die PartListEntryId gemerkt
        if (((bctePrimaryKey != null) && container.isNewOrAutoCreatedBCTEKey(bctePrimaryKey)) ||
            (isEDS && !container.containsPartListEntryIdForExport(partListEntryId))) {
            // Wurde eine Position angelegt oder gelöscht, dann muss der BCTE Schlüssel bzw die PartListEntryId ausgegeben werden
            if (isSerializedStateNewOrDeleted(serializedDBDataObject) || isSerializedStateReplaced(serializedDBDataObject)) {
                if (!isEDS) {
                    container.addOrUpdateBCTEKey(bctePrimaryKey, partListEntryId);
                } else {
                    container.addPartListEntryIdForExport(partListEntryId);
                }
                addUsedPartListEntry(partListEntryId, container);
            } else if (isSerializedStateModified(serializedDBDataObject)) {
                // Wurde die Position verändert, müssen folgende AS Attribute geprüft werden:
                // Hotspot, Strukturstufe AS, Coderegel AS, PEMab Flag, PEMbis Flag, Unterdrückt, Ereignis-ID ab, Ereignis-ID
                for (SerializedDBDataObjectAttribute attribute : attributes) {
                    if (billableDataCache.isAttributeBillable(partListEntryId, attribute.getName()) && !attribute.isNotModified()) {
                        if (attributeChanged(attribute)) {
                            // Hat sich ein Attribut wirklich geändert, dann müssen die anderen Attribute
                            // nicht mehr geprüft werden -> hier aussteigen
                            if (!isEDS) {
                                container.addOrUpdateBCTEKey(bctePrimaryKey, partListEntryId);
                            } else {
                                container.addPartListEntryIdForExport(partListEntryId);
                            }
                            addUsedPartListEntry(partListEntryId, container);
                            // nach der ersten relevanten Änderung kann man aufhören
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Verarbeitet die kombinierten Texte, die nach der Verarbeitung der serialisierten Objekte übrig geblieben sind
     *
     * @param container
     * @param mergedObjects
     */
    private void processRemainingCombTexts(ExportContainer container, Map<IdWithType, SerializedDBDataObject> mergedObjects) {
        // Kombinierte Texte zu Stücklistenpositionen, die sonst nicht verändert wurden. Das Ändern der Reihenfolge bei
        // den kombinierten Texte unterscheidet sich bezüglich des Status nicht vom Ersetzen eines Textes. Somit wird
        // hier geprüft, ob alle "alten" Werte auch "neue" Werte sind. Ist das der Fall, hat eine Reihenfolgenänderung
        // stattgefunden und keine Ersetzung.
        if (!container.getCombTextsForPartListEntry().isEmpty()) {
            for (Map.Entry<PartListEntryId, Set<SerializedDBDataObject>> partListEntryIdEntry : container.getCombTextsForPartListEntry().entrySet()) {
                Set<String> oldValues = new HashSet<>();
                Set<String> newValues = new HashSet<>();
                for (SerializedDBDataObject serializedCombText : partListEntryIdEntry.getValue()) {
                    if (isCancelled) {
                        return;
                    }
                    SerializedDBDataObjectAttribute attribute = serializedCombText.getAttribute(FIELD_DCT_DICT_TEXT);
                    if ((attribute != null) && !attribute.isNotModified()) {
                        SerializedEtkMultiSprache oldMultiLang = attribute.getOldMultiLanguage();
                        SerializedEtkMultiSprache newMultiLang = attribute.getMultiLanguage();
                        if ((oldMultiLang != null) && (newMultiLang != null)) {
                            oldValues.add(oldMultiLang.getTextId());
                            newValues.add(newMultiLang.getTextId());
                        }
                    }
                }
                if (oldValues.isEmpty()) {
                    continue;
                }
                oldValues.removeAll(newValues);
                if (!oldValues.isEmpty()) {
                    addUsedPartListEntryAndIdsOrBCTEKeysForExport(partListEntryIdEntry.getKey(), container, mergedObjects, false);
                }
            }
        }
    }

    /**
     * Filter die aufgesammelten Bildaufträge und hängt sie ans {@link iPartsAuthorOrderExportObject}
     *
     * @param authorOrderExportObject
     * @param container
     */
    private void processCollectedPicOrders(iPartsAuthorOrderExportObject authorOrderExportObject, ExportContainer container) {
        if (!container.getPicOrders().isEmpty()) {
            iPartsDataPicOrderList validPicOrders = filterBillablePicOrders(container.getPicOrders(), false);
            if ((validPicOrders != null) && !validPicOrders.isEmpty()) {
                authorOrderExportObject.addPicOrders(validPicOrders.getAsList());
            }
        }
    }

    private boolean isSerializedStateNewOrDeleted(SerializedDBDataObject serializedDBDataObject) {
        return hasSerializedObjectTargetState(serializedDBDataObject, SerializedDBDataObjectState.NEW, SerializedDBDataObjectState.DELETED);
    }

    private boolean isSerializedStateModified(SerializedDBDataObject serializedDBDataObject) {
        return hasSerializedObjectTargetState(serializedDBDataObject, SerializedDBDataObjectState.MODIFIED);
    }

    private boolean isSerializedStateCommitted(SerializedDBDataObject serializedDBDataObject) {
        return hasSerializedObjectTargetState(serializedDBDataObject, SerializedDBDataObjectState.COMMITTED);
    }

    private boolean isSerializedStateReplaced(SerializedDBDataObject serializedDBDataObject) {
        return hasSerializedObjectTargetState(serializedDBDataObject, SerializedDBDataObjectState.REPLACED);
    }

    private boolean hasSerializedObjectTargetState(SerializedDBDataObject serializedDBDataObject, SerializedDBDataObjectState... states) {
        if ((serializedDBDataObject == null) || (states == null) || (states.length < 1)) {
            return false;
        }
        SerializedDBDataObjectState currentState = serializedDBDataObject.getState();
        for (SerializedDBDataObjectState targetState : states) {
            if (currentState == targetState) {
                return true;
            }
        }
        return false;
    }

    private iPartsDialogBCTEPrimaryKey getBcteKey(SerializedDBDataObject serializedDBDataObject) {
        if ((serializedDBDataObject != null) && serializedDBDataObject.getTableName().equals(TABLE_KATALOG)) {
            String sourceGUID = serializedDBDataObject.getAttributeValue(FIELD_K_SOURCE_GUID, true, getProject());
            return getBcteKey(sourceGUID);
        }
        return null;
    }

    private iPartsDialogBCTEPrimaryKey getBcteKey(String guid) {
        if (StrUtils.isValid(guid)) {
            // BCTE Schlüssel bestimmen
            return iPartsDialogBCTEPrimaryKey.createFromDialogGuid(guid);
        }
        return null;
    }

    /**
     * Gibt zurück, ob das Attribut - abhängig vom Typ - sich geändert hat
     *
     * @param attribute
     * @return
     */
    private boolean attributeChanged(SerializedDBDataObjectAttribute attribute, boolean checkFieldsToSkip) {
        if (attribute.isNotModified()) {
            return false;
        }
        if (checkFieldsToSkip && isFieldToSkip(attribute.getName())) {
            return false;
        }
        return attribute.isValueModified();
    }

    private boolean attributeChanged(SerializedDBDataObjectAttribute attribute) {
        return attributeChanged(attribute, true);
    }


    private boolean isFieldToSkip(String name) {
        return FIELDS_TO_SKIP.contains(name);
    }

    /**
     * Bestimmt anhand der übergebenen <code>partListentryId</code> das zugehörige {@link SerializedDBDataObject}
     * und fügt den BCTE Schlüssel des gefundenen Objekts der Sammlung aller BCTE Schlüssel hinzu.
     * Falls es sich um ein EDS-Modul handelt, wird die {@link PartListEntryId} der Sammlung aller IDs hinzugefügt.
     *
     * @param partListEntryId
     * @param container
     * @param mergedObjects
     */
    private void addUsedPartListEntryAndIdsOrBCTEKeysForExport(PartListEntryId partListEntryId, ExportContainer container,
                                                               Map<IdWithType, SerializedDBDataObject> mergedObjects,
                                                               boolean checkPartListEntryId) {
        iPartsDialogBCTEPrimaryKey bctePrimaryKey = getBCTEKeyFromMergedObjects(mergedObjects, partListEntryId);
        if (bctePrimaryKey != null) {
            container.addOrUpdateBCTEKey(bctePrimaryKey, partListEntryId);
            if (checkPartListEntryId) {
                addUsedPartListEntry(partListEntryId, container);
            }
        } else if (isEDSAssembly(partListEntryId)) {
            container.addPartListEntryIdForExport(partListEntryId);
            if (checkPartListEntryId) {
                addUsedPartListEntry(partListEntryId, container);
            }
        }
    }

    /**
     * Findet zur übergebenen {@link PartListEntryId} das passende {@link SerializedDBDataObject} und bestimmt den BCTE
     * Schlüssel zum gefundenen Objekt
     *
     * @param mergedObjects
     * @param partListEntryId
     * @return
     */
    private iPartsDialogBCTEPrimaryKey getBCTEKeyFromMergedObjects(Map<IdWithType, SerializedDBDataObject> mergedObjects,
                                                                   PartListEntryId partListEntryId) {
        SerializedDBDataObject serializedPartListEntry = mergedObjects.get(partListEntryId);
        return getBcteKey(serializedPartListEntry);
    }

    /**
     * Anhand der {@link PartListEntryId} bestimmen, ob es sich um ein EDS-Modul handelt
     *
     * @param partListEntryId
     * @return
     */
    public boolean isEDSAssembly(PartListEntryId partListEntryId) {
        return isEDSAssemblyCache.computeIfAbsent(partListEntryId.getOwnerAssemblyId(), assemblyId -> {
            EtkDataAssembly assembly = EtkDataObjectFactory.createDataAssembly(project, assemblyId);
            if (assembly instanceof iPartsDataAssembly) {
                iPartsDocumentationType documentationType = ((iPartsDataAssembly)assembly).getDocumentationType();
                return documentationType.isTruckDocumentationType();
            }
            return false;
        });
    }

    /**
     * Markiert den übergebenen Stücklisteneintrag als "verarbeitet" durch die Aufnahme der ID in <code>usedPartListEntryIds</code>.
     * Kombinierte Text Objekte zum Stücklisteneintrag werden entfernt, weil eine weitere Analyse der
     * kombinierten Texte durch das Markieren nicht mehr nötig ist.
     *
     * @param partListEntryId
     * @param container
     */
    private void addUsedPartListEntry(PartListEntryId partListEntryId, ExportContainer container) {
        container.addUsedPartListEntryId(partListEntryId);
        if (container.getCombTextsForPartListEntry() != null) {
            container.removeCombTextForPartListEntry(partListEntryId);
        }
    }


    private void createAndSaveCSV(List<iPartsAuthorOrderExportObject> exportObjects, EtkMessageLogForm messageLogForm, String fileName) {
        CsvZipExportWriter exportWriter = new CsvZipExportWriter();
        try {
            if (!exportObjects.isEmpty()) {
                if (exportWriter.open(fileName)) {
                    iPartsAuthorOrderExportFormatter formatter = new iPartsAuthorOrderExportFormatter(getProject());
                    exportWriter.writeHeader(formatter.getHeader());
                    for (iPartsAuthorOrderExportObject authorOrderExportObject : exportObjects) {
                        List<List<String>> exportList = formatter.exportOneExportObject(authorOrderExportObject);
                        // Schreiben in CSV-Zip-Stream
                        for (List<String> lineList : exportList) {
                            if (isCancelled) {
                                return;
                            }
                            exportWriter.writeToZipStream(lineList);
                        }
                    }
                    exportWriter.closeOutputStreams();
                    exportWriter.downloadExportFile();
                } else {
                    isCancelled = true;
                    if (messageLogForm != null) {
                        messageLogForm.getMessageLog().fireMessage(TranslationHandler.translate("!!Die Datei \"%1\"" +
                                                                                                " kann nicht erstellt werden",
                                                                                                fileName + "." + MimeTypes.EXTENSION_ZIP),
                                                                   MessageLogType.tmlError, MessageLogOption.TIME_STAMP);
                    }
                }
            }
        } finally {
            exportWriter.closeOutputStreams();
            exportWriter.clearAfterDownload();
        }
    }

    private String getFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.simpleDateFormatyyyyMMdd + "_" + "HHmm");
        String filePrefix = sdf.format(Calendar.getInstance().getTime());

        return filePrefix + "_" + FILENAME_SUFFIX;
    }

    private String getFileNameWithPrefix() {
        return FILENAME_PREFIX_NOT_APPROVED_AO + "_" + getFileName();
    }

    private class ExportContainer {

        private final Map<iPartsDialogBCTEPrimaryKey, Boolean> bcteKeyToAutoCreatedMap;
        private final Map<iPartsDialogBCTEPrimaryKey, iPartsAuthorOrderExportFormatter.AUTO_STATE> bcteKeyForAutoStateMap;
        private final Set<PartListEntryId> partListEntryIdsForExport;
        private final Set<PartListEntryId> usedPartListEntryIds;
        private final Map<PartListEntryId, Set<SerializedDBDataObject>> combTextsForPartListEntry;
        private final List<iPartsDataPicOrder> picOrders;
        private final Set<iPartsColorTableContentId> colorTableContentIds;
        private final Set<iPartsColorTableContentId> contentIdsWithStatusChange;
        private final Set<iPartsDialogBCTEPrimaryKey> bcteKeyWithStatusChange;

        public ExportContainer() {
            partListEntryIdsForExport = new HashSet<>();
            usedPartListEntryIds = new HashSet<>();
            combTextsForPartListEntry = new HashMap<>();
            picOrders = new DwList<>();
            colorTableContentIds = new HashSet<>();
            bcteKeyToAutoCreatedMap = new HashMap<>();
            bcteKeyForAutoStateMap = new HashMap<>();
            contentIdsWithStatusChange = new HashSet<>();
            bcteKeyWithStatusChange = new HashSet<>();

        }

        public void addPartListEntryIdForExport(PartListEntryId partListEntryId) {
            if ((partListEntryId != null) && !partListEntryId.isEmpty()) {
                partListEntryIdsForExport.add(partListEntryId);
            }
        }

        /**
         * BCTE-Schlüssel als nicht automatisch erzeugt hinzufügen bzw. in der Map aktualisieren.
         *
         * @param bcteKey
         */
        private void addOrUpdateBCTEKeyAsNotAutoCreated(iPartsDialogBCTEPrimaryKey bcteKey) {
            if (bcteKey != null) {
                bcteKeyToAutoCreatedMap.put(bcteKey, false);
            }
        }

        /**
         * BCTE-Schlüssel hinzufügen bzw. in der Map aktualisieren, wobei Datensätze zu einem BCTE-Schlüssel solange als
         * automatisch erzeugt gelten bis ein Datensatz kommt, der es nicht ist.
         *
         * @param bcteKey
         * @param partListEntryId
         */
        private void addOrUpdateBCTEKey(iPartsDialogBCTEPrimaryKey bcteKey, PartListEntryId partListEntryId) {
            if (bcteKey != null) {
                boolean partListEntryIsAutoCreated = autoCreatedPartListEntries.contains(partListEntryId);
                bcteKeyToAutoCreatedMap.put(bcteKey, isNewOrAutoCreatedBCTEKey(bcteKey) && partListEntryIsAutoCreated);
                addOrUpdateBCTEKeyForAutoState(bcteKey, partListEntryId);
            }
        }

        private void addOrUpdateBCTEKeyForAutoState(iPartsDialogBCTEPrimaryKey bcteKey, PartListEntryId partListEntryId) {
            if (bcteKey != null) {
                iPartsAuthorOrderExportFormatter.AUTO_STATE autoState = iPartsAuthorOrderExportFormatter.AUTO_STATE.MANUAL;
                if (autoCreatedPartListEntries.contains(partListEntryId)) {
                    autoState = iPartsAuthorOrderExportFormatter.AUTO_STATE.AUTO_NOT_MODIFIED;
                } else if (wasAutoCreatedPartListEntries.contains(partListEntryId)) {
                    autoState = iPartsAuthorOrderExportFormatter.AUTO_STATE.AUTO_MODIFIED;
                }

                // Den neuen AutoState nur setzen, falls noch keiner gesetzt wurde, der neue Zustand MANUAL ist (gewinnt immer)
                // oder der bisherige Zustand AUTO_NOT_MODIFIED, da sowohl MANUAL als auch AUTO_MODIFIED diesen überschreiben.
                iPartsAuthorOrderExportFormatter.AUTO_STATE oldAutoState = bcteKeyForAutoStateMap.get(bcteKey);
                if ((oldAutoState == null) || (autoState == iPartsAuthorOrderExportFormatter.AUTO_STATE.MANUAL)
                    || (oldAutoState == iPartsAuthorOrderExportFormatter.AUTO_STATE.AUTO_NOT_MODIFIED)) {
                    bcteKeyForAutoStateMap.put(bcteKey, autoState);
                }
            }
        }

        /**
         * Sind alle Datensätze zum BCTE-Schlüssel automatisch erzeugt bzw. wurde der BCTE-Schlüssel noch gar nicht behandelt?
         *
         * @param bcteKey
         * @return
         */
        private boolean isNewOrAutoCreatedBCTEKey(iPartsDialogBCTEPrimaryKey bcteKey) {
            Boolean autoCreatedBCTEKey = bcteKeyToAutoCreatedMap.get(bcteKey);
            return (autoCreatedBCTEKey == null) || autoCreatedBCTEKey;
        }

        public void addUsedPartListEntryId(PartListEntryId partListEntryId) {
            if (partListEntryId != null) {
                usedPartListEntryIds.add(partListEntryId);
            }
        }

        public boolean containsUsedPartListEntryId(PartListEntryId partListEntryId) {
            return usedPartListEntryIds.contains(partListEntryId);
        }

        public boolean containsPartListEntryIdForExport(PartListEntryId partListEntryId) {
            return partListEntryIdsForExport.contains(partListEntryId);
        }

        public Set<iPartsDialogBCTEPrimaryKey> getBcteKeysForExport() {
            return bcteKeyToAutoCreatedMap.keySet();
        }

        public Set<PartListEntryId> getPartListEntryIdsForExport() {
            return partListEntryIdsForExport;
        }

        public Set<PartListEntryId> getUsedPartListEntryIds() {
            return usedPartListEntryIds;
        }

        public Map<PartListEntryId, Set<SerializedDBDataObject>> getCombTextsForPartListEntry() {
            return combTextsForPartListEntry;
        }

        public List<iPartsDataPicOrder> getPicOrders() {
            return picOrders;
        }

        public void removeCombTextForPartListEntry(PartListEntryId entryId) {
            if (entryId != null) {
                getCombTextsForPartListEntry().remove(entryId);
            }
        }

        public void addPicOrder(iPartsDataPicOrder picOrder) {
            if (picOrder != null) {
                picOrders.add(picOrder);
            }
        }

        public Set<SerializedDBDataObject> getCombTextForPartListEntryId(PartListEntryId entryId) {
            if (entryId != null) {
                return getCombTextsForPartListEntry().get(entryId);
            }
            return null;
        }

        public void addCombTextForPartListEntryId(PartListEntryId entryId, Set<SerializedDBDataObject> combTexts) {
            if ((entryId != null) && (combTexts != null)) {
                getCombTextsForPartListEntry().put(entryId, combTexts);
            }
        }

        public void addColorTableContentId(iPartsColorTableContentId colorTableContentId) {
            if (colorTableContentId != null) {
                colorTableContentIds.add(colorTableContentId);
            }
        }

        public Set<iPartsColorTableContentId> getColorTableContentIds() {
            return colorTableContentIds;
        }

        public boolean hasBCTEKeys() {
            return (bcteKeyToAutoCreatedMap != null) && !bcteKeyToAutoCreatedMap.isEmpty();
        }

        public boolean hasBCTEAutoStateKeys() {
            return (bcteKeyForAutoStateMap != null) && !bcteKeyForAutoStateMap.isEmpty();
        }

        public boolean hasPartListEntryIdsForExport() {
            return (partListEntryIdsForExport != null) && !partListEntryIdsForExport.isEmpty();
        }

        public boolean hasColorTableContents() {
            return (colorTableContentIds != null) && !colorTableContentIds.isEmpty();
        }

        public boolean containsColorTableContentId(iPartsColorTableContentId colorTableContentId) {
            return colorTableContentIds.contains(colorTableContentId);
        }

        public Map<iPartsDialogBCTEPrimaryKey, Boolean> getBcteKeyToAutoCreatedMap() {
            return bcteKeyToAutoCreatedMap;
        }

        public Map<iPartsDialogBCTEPrimaryKey, iPartsAuthorOrderExportFormatter.AUTO_STATE> getBcteKeyForAutoStateMap() {
            return bcteKeyForAutoStateMap;
        }

        /**
         * Überprüft, ob die Objekte, die eine Statusänderung enthielten, zusätzlich von anderen Änderungen betroffen sind.
         * Liefert als Ergebnis die IDs (in String-Form) der Objekte, die nur eine Statusänderung hatten
         *
         * @return
         */
        public Set<String> checkDataWithStatusChanges() {
            Set<String> result = new HashSet<>();
            // Check, ob BCTE Schlüssel von anderen Änderungen betroffen sind
            if (!bcteKeyWithStatusChange.isEmpty() && hasBCTEKeys()) {
                // Wenn ein BCTE Schlüssel automatisch erzeugt wurde und bei den Werksdaten nur der Status geändert wurde,
                // dann müssen beide Tags auf "nein" gesetzt werden. Die Statusänderung wird auf "nein" gesetzt, weil
                // der BCTE Datensatz aus bcteKeyWithStatusChange entfernt wird und das "automatisch erzeugt" wird auf
                // "nein" gesetzt durch den Rückgabewert im "replaceAll" Aufruf
                bcteKeyToAutoCreatedMap.replaceAll((key, value) -> {
                    if (bcteKeyWithStatusChange.remove(key)) {
                        return false;
                    }
                    return value;
                });
            }
            // Hier die BCTE Schlüssel anlegen, die nur eine reine Statusänderung haben
            bcteKeyWithStatusChange.forEach(bctePrimaryKey -> {
                bcteKeyToAutoCreatedMap.put(bctePrimaryKey, false);
                result.add(bctePrimaryKey.toString());
            });

            // Check, ob Farbinhalt-IDs von anderen Änderungen betroffen sind
            if (!contentIdsWithStatusChange.isEmpty() && hasColorTableContents()) {
                contentIdsWithStatusChange.removeAll(getColorTableContentIds());
            }
            // Hier die iPartsColorTableContentId anlegen, die nur eine reine Statusänderung haben
            contentIdsWithStatusChange.forEach(colorTableContentId -> {
                addColorTableContentId(colorTableContentId);
                result.add(colorTableContentId.toString(CONTENT_ID_DELIMITER));
            });

            return result;
        }

        public void addBCTEKeyWithStatusChange(iPartsDialogBCTEPrimaryKey bctePrimaryKey) {
            bcteKeyWithStatusChange.add(bctePrimaryKey);
        }

        public void addContentIdWithStatusChange(iPartsColorTableContentId colorTableContentId) {
            contentIdsWithStatusChange.add(colorTableContentId);
        }


    }
}
