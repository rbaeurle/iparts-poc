/*
 * Copyright (c) 2024 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper;

import de.docware.apps.etk.base.db.AbstractRevisionChangeSet;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsFactoryDataTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataFactories;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataReleaseState;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.FactoryDataHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.iPartsPlugin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.dialog.iPartsDialogId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.helper.iPartsFactories;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.misc.logger.LogType;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.sql.SQLStringConvert;

import java.util.*;
import java.util.stream.Collectors;

public class CopyAndPasteData implements iPartsConst {

    private static final String SESSION_KEY_COPY_PASTE_FACTORY_DATA = "sessionKeyCopyPasteFactoryData";

    // Relevante Felder beim Kopieren der Werkseinsatzdaten am Teil und den Varianten
    private enum MAPPING_KEYS {
        FACTORY, PEMA, PEMTA, PEMB, PEMTB, STCA, STCB, STATUS, FN_ID
    }

    private static Map<MAPPING_KEYS, String> KEY_MAPPING_COLORTABLE = new HashMap<>();
    private static Map<MAPPING_KEYS, String> KEY_MAPPING_PART = new HashMap<>();

    static {
        KEY_MAPPING_COLORTABLE.put(MAPPING_KEYS.FACTORY, FIELD_DCCF_FACTORY);
        KEY_MAPPING_COLORTABLE.put(MAPPING_KEYS.PEMA, FIELD_DCCF_PEMA);
        KEY_MAPPING_COLORTABLE.put(MAPPING_KEYS.PEMTA, FIELD_DCCF_PEMTA);
        KEY_MAPPING_COLORTABLE.put(MAPPING_KEYS.PEMB, FIELD_DCCF_PEMB);
        KEY_MAPPING_COLORTABLE.put(MAPPING_KEYS.PEMTB, FIELD_DCCF_PEMTB);
        KEY_MAPPING_COLORTABLE.put(MAPPING_KEYS.STCA, FIELD_DCCF_STCA);
        KEY_MAPPING_COLORTABLE.put(MAPPING_KEYS.STCB, FIELD_DCCF_STCB);
        KEY_MAPPING_COLORTABLE.put(MAPPING_KEYS.STATUS, FIELD_DCCF_STATUS);
        //KEY_MAPPING_COLORTABLE.put(KEYS.FN_ID, FIELD_DFD_FN_ID); //gibts bei Varianten nicht

        KEY_MAPPING_PART.put(MAPPING_KEYS.FACTORY, FIELD_DFD_FACTORY);
        KEY_MAPPING_PART.put(MAPPING_KEYS.PEMA, FIELD_DFD_PEMA);
        KEY_MAPPING_PART.put(MAPPING_KEYS.PEMTA, FIELD_DFD_PEMTA);
        KEY_MAPPING_PART.put(MAPPING_KEYS.PEMB, FIELD_DFD_PEMB);
        KEY_MAPPING_PART.put(MAPPING_KEYS.PEMTB, FIELD_DFD_PEMTB);
        KEY_MAPPING_PART.put(MAPPING_KEYS.STCA, FIELD_DFD_STCA);
        KEY_MAPPING_PART.put(MAPPING_KEYS.STCB, FIELD_DFD_STCB);
        KEY_MAPPING_PART.put(MAPPING_KEYS.STATUS, FIELD_DFD_STATUS);
        KEY_MAPPING_PART.put(MAPPING_KEYS.FN_ID, FIELD_DFD_FN_ID);
    }

    private static final String IS_DIALOG = "isDialog";
    private static final String IS_SOURCE_LINKED = "isSourceLinked";
    private static final List<String> warningMessageUnlink = new ArrayList<>();

    private static String guidOfLinkSource = "";

    public CopyAndPasteData() {
    }

    /**
     * Speichert die Daten in der Session
     *
     * @param filteredData   zu kopierende Werksdaten
     * @param isDialog       Kommt der Datensatz von Dialog
     * @param isLinkedSource ist Quelle bereits ein Ziel einer anderen Kopplung
     * @param guidToLink     guid der Quelle
     */
    public static void copyFactoryData(List<? extends iPartsEtkDataObjectFactoryDataInterface> filteredData, boolean isDialog,
                                       boolean isLinkedSource, String guidToLink, boolean isColorTable) {
        Session session = Session.get();
        if (session != null) {
            List<Map<String, String>> dataToCopyList = createCopyDataList(filteredData, isDialog, isLinkedSource, guidToLink, isColorTable);

            session.setAttribute(SESSION_KEY_COPY_PASTE_FACTORY_DATA, dataToCopyList);
        }
    }

    public static void copyFactoryDataOfPart(iPartsDataFactoryDataList filteredData, boolean isDialog, boolean isLinkedSource, String guidToLink) {
        copyFactoryDataOfPart(filteredData.getAsList(), isDialog, isLinkedSource, guidToLink);
    }

    public static void copyFactoryDataOfPart(List<? extends iPartsEtkDataObjectFactoryDataInterface> filteredData, boolean isDialog,
                                             boolean isLinkedSource, String guidToLink) {
        copyFactoryData(filteredData, isDialog, isLinkedSource, guidToLink, false);
    }

    public static void copyFactoryDataOfColortable(List<? extends iPartsEtkDataObjectFactoryDataInterface> filteredData) {
        copyFactoryData(filteredData, true, false, "", true);
    }

    /**
     * Erzeugt die Datenstruktur für die zu kopierenden Daten, damit die nachfolgende Logik die Daten verarbeiten kann
     *
     * @param filteredData zu kopierende Werksdaten
     * @param isDialog     Kommt der Datensatz von Dialog
     * @return Datenstruktur für die zu kopierenden Daten
     */
    public static List<Map<String, String>> createCopyDataList(List<? extends iPartsEtkDataObjectFactoryDataInterface> filteredData,
                                                               boolean isDialog, boolean isLinked, String guidToLink, boolean isColorTable) {
        List<Map<String, String>> dataToCopyList = new ArrayList<>();
        guidOfLinkSource = guidToLink;
        Map<MAPPING_KEYS, String> keyMapping;
        if (isColorTable) {
            keyMapping = KEY_MAPPING_COLORTABLE;
        } else {
            keyMapping = KEY_MAPPING_PART;
        }
        for (iPartsEtkDataObjectFactoryDataInterface factoryData : filteredData) {
            Map<String, String> temp = new HashMap<>();
            temp.put(IS_DIALOG, Boolean.toString(isDialog));
            temp.put(IS_SOURCE_LINKED, Boolean.toString(isLinked));
            for (Map.Entry<MAPPING_KEYS, String> entry : keyMapping.entrySet()) {
                temp.put(entry.getKey().name(), factoryData.getFieldValue(entry.getValue()));
            }
            dataToCopyList.add(temp);
        }
        return dataToCopyList;
    }

    /**
     * Liefert die einzufügenden Daten ungefiltert zurück.
     *
     * @return
     */
    private static List<Map<String, String>> getToPasteData() {
        Session session = Session.get();
        if ((session != null) && session.hasAttribute(CopyAndPasteData.SESSION_KEY_COPY_PASTE_FACTORY_DATA)) {
            return (List<Map<String, String>>)(session.getAttribute(SESSION_KEY_COPY_PASTE_FACTORY_DATA));
        }
        return null;
    }

    /**
     * Einfügefunktion -> Holt die Daten aus der Session und verarbeitet sie weiter
     *
     * @param partListEntry                            Ziel
     * @param allFactoryDataForPartlistEntryUnfiltered Komplette original Werksdaten am Stücklisteneintrag um Sequenznummern für ELDAS zu bestimmen
     * @param project                                  EtkProject
     * @return hat es geklappt
     */
    public static iPartsDataFactoryDataList pasteFactoryDataOfPart(EtkDataPartListEntry partListEntry, iPartsDataFactoryDataList allFactoryDataForPartlistEntryUnfiltered,
                                                                   EtkProject project) {

        //GUID vom Stücklisteneintrag
        String guid = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
        List<Map<String, String>> toPasteData = getToPasteData();
        if (toPasteData != null) {

            // Einfügen der Werkseinsatzdaten am Stücklisteneintrag
            return pasteFactoryDataOfPart(toPasteData, partListEntry, allFactoryDataForPartlistEntryUnfiltered, guid, project,
                                          false, true);
        }
        return null;
    }

    /**
     * Gleichzeitiges Einfügen der Werkseinsatzdaten und Koppeln mit dem Stücklisteneintrag
     *
     * @param partListEntry                            Ziel
     * @param allFactoryDataForPartlistEntryUnfiltered Komplette original Werksdaten am Stücklisteneintrag um Sequenznummern für ELDAS zu bestimmen
     * @param project                                  EtkProject
     * @param changesWithChangeset
     * @param changedTargetDataEntryList
     * @param showMessageDialogs                       Sollen {@link MessageDialog}e angezeigt werden mit Infos zum Einfügen/Koppeln?
     * @return hat es geklappt
     */
    public static boolean pasteAndLink(EtkDataPartListEntry partListEntry, iPartsDataFactoryDataList allFactoryDataForPartlistEntryUnfiltered, EtkProject project,
                                       iPartsDataFactoryDataList changesWithChangeset, EtkDataObjectList changedTargetDataEntryList,
                                       boolean showMessageDialogs) {

        //GUID vom Stücklisteneintrag
        String guidOfLinkDestination = partListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
        List<Map<String, String>> toPasteData = getToPasteData();

        if (toPasteData != null) {

            boolean withLink = false;
            if (StrUtils.isValid(guidOfLinkSource, guidOfLinkDestination)) {
                // Quell-Stücklisteneintrag Guid am Ziel speichern
                iPartsDialogId constructionIdOfDestination = new iPartsDialogId(guidOfLinkDestination);
                iPartsDataDialogData constructionDataOfDestination = new iPartsDataDialogData(project, constructionIdOfDestination);
                if (constructionDataOfDestination.existsInDB()) {

                    constructionDataOfDestination.setFieldValue(FIELD_DD_LINKED_FACTORY_DATA_GUID, guidOfLinkSource, DBActionOrigin.FROM_EDIT);

                    changedTargetDataEntryList.add(constructionDataOfDestination, DBActionOrigin.FROM_EDIT);
                    withLink = true;
                } else {
                    addWarningMessageUnlink("!!Kopierte Werkseinsatzdaten konnten nicht gekoppelt werden. Es existiert kein Konstruktionsstücklisteneintrag!");
                }
            } else {
                addWarningMessageUnlink("!!Kopierte Werkseinsatzdaten konnten nicht gekoppelt werden. Es existiert kein Konstruktionsstücklisteneintrag!");
            }
            // kann nicht gekoppelt werden, werden trotzdem die Werkseinsatzdaten eingefügt
            iPartsDataFactoryDataList afterPasteFactoryData = pasteFactoryDataOfPart(toPasteData, partListEntry,
                                                                                     allFactoryDataForPartlistEntryUnfiltered,
                                                                                     guidOfLinkDestination, project, withLink,
                                                                                     showMessageDialogs);
            if (afterPasteFactoryData != null) {
                changesWithChangeset.addAll(afterPasteFactoryData, DBActionOrigin.FROM_EDIT);
                return true;
            }
        }
        return false;
    }

    /**
     * Einfügen der kopierten Werkseinsatzdaten am Stücklisteneintrag
     *
     * @param toPasteDataList                          zu kopierende Daten
     * @param partListEntry                            Ziel-Stücklisteneintrag
     * @param allFactoryDataForPartlistEntryUnfiltered Komplette original Werksdaten am Stücklisteneintrag um Sequenznummern für ELDAS zu bestimmen
     * @param guid                                     guid des Ziels
     * @param project                                  EtkProject
     * @param withLink                                 Mit Koppeln?
     * @param showMessageDialogs                       Sollen {@link MessageDialog}e angezeigt werden mit Infos zum Einfügen/Koppeln?
     * @return Liste der eingefügten Werksdaten
     */
    private static iPartsDataFactoryDataList pasteFactoryDataOfPart(List<Map<String, String>> toPasteDataList,
                                                                    EtkDataPartListEntry partListEntry,
                                                                    iPartsDataFactoryDataList allFactoryDataForPartlistEntryUnfiltered,
                                                                    String guid, EtkProject project, boolean withLink,
                                                                    boolean showMessageDialogs) {

        // Stücklistentyp der Zielstückliste
        iPartsDataAssembly ownerAssembly = (iPartsDataAssembly)partListEntry.getOwnerAssembly();
        boolean targetEntryIsDialog = ownerAssembly.getDocumentationType().isPKWDocumentationType();

        // Ergebnis
        iPartsDataFactoryDataList newFactoryDataList = new iPartsDataFactoryDataList();

        // Kopie der kompletten original Werksdaten am Stücklisteneintrag um Sequenznummern für ELDAS zu bestimmen
        iPartsDataFactoryDataList allFactoryDataForPartlistEntryUnfilteredCopy = null;
        if (!targetEntryIsDialog) {
            if (allFactoryDataForPartlistEntryUnfiltered == null) {
                return newFactoryDataList;
            }
            allFactoryDataForPartlistEntryUnfilteredCopy = new iPartsDataFactoryDataList();
            allFactoryDataForPartlistEntryUnfilteredCopy.addAll(allFactoryDataForPartlistEntryUnfiltered, DBActionOrigin.FROM_DB);
        }

        // Aggregate Typ von der Stückliste
        String aggregateType = ownerAssembly.getAggregateTypeOfProductFromModuleUsage();

        // Aktuelles Datum für Adat
        String aDat = SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance());

        // Gültige Werke zum Produkt
        Set<String> validFactoriesForProduct = null;
        iPartsProductId productId = ownerAssembly.getProductIdFromModuleUsage();
        if (productId != null) {
            iPartsProduct product = iPartsProduct.getInstance(project, productId);
            validFactoriesForProduct = product.getProductFactories(project);
        }

        // Prototypenwerke für die Filterung
        Set<String> prototypeFactories = new HashSet<>();
        for (iPartsDataFactories dataFactory : iPartsFactories.getInstance(project).getDataFactories()) {
            if (!dataFactory.isValidForFilter()) {
                prototypeFactories.add(dataFactory.getFactoryNumber());
            }
        }

        // Kopierte Daten vor dem Einfügen filtern:
        // Prototypenwerke werden immer ausgefiltert
        List<Map<String, String>> toPasteDataFiltered = toPasteDataList.stream()
                .filter((toPasteData) -> !prototypeFactories.contains(toPasteData.get(MAPPING_KEYS.FACTORY.name())))
                .collect(Collectors.toList());

        int originalPasteDataSize = toPasteDataFiltered.size();

        // Beim normalen Einfügen werden zusätzlich noch ungültige Werke über das Produkt ausgefiltert (bei freien SAs gibt
        // es keine gültigen Werke)
        if (!withLink && (validFactoriesForProduct != null)) {
            Set<String> finalValidFactoriesForProduct = validFactoriesForProduct;
            toPasteDataFiltered = toPasteDataFiltered.stream()
                    .filter((toPasteData) -> {
                        // Leere Werksnummer kann z.B. bei Truck-Werksdaten vorkommen
                        String factoryNumber = toPasteData.get(MAPPING_KEYS.FACTORY.name());
                        return StrUtils.isEmpty(factoryNumber) || finalValidFactoriesForProduct.contains(factoryNumber);
                    })
                    .collect(Collectors.toList());
        }

        // Nur kopieren, falls Datensätze nach Gültigkeitsprüfung existieren
        if (Utils.isValid(toPasteDataFiltered)) {
            for (Map<String, String> toPasteData : toPasteDataFiltered) {
                // Nur bei gleicher Quelle kopieren
                if (!toPasteData.containsKey(IS_DIALOG)) {
                    continue;
                }
                iPartsDataFactoryData newFactoryData = null;
                boolean pasteDataIsDialog = StrUtils.toBoolean(toPasteData.get(IS_DIALOG));
                // Entweder sind beide Dialog oder beide Eldas
                if (pasteDataIsDialog && targetEntryIsDialog) {
                    // Werkseinsatzdaten sollen als AS-Werksdaten eingefügt werden
                    iPartsFactoryDataId pastedFactoryDataId = new iPartsFactoryDataId(guid, toPasteData.get(MAPPING_KEYS.FACTORY.name()), "",
                                                                                      aDat, iPartsFactoryDataTypes.FACTORY_DATA_AS.getDbValue(),
                                                                                      "");
                    newFactoryData = new iPartsDataFactoryData(project, pastedFactoryDataId);
                    newFactoryData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

                    // Attribute, die über den Stücklisteneintrag gesetzt werden
                    newFactoryData.setFieldsFromBCTEKEYAndPartListEntry(partListEntry);


                } else if (!pasteDataIsDialog && !targetEntryIsDialog) {
                    iPartsFactoryDataId pastedFactoryDataId = iPartsDataFactoryData.
                            getFactoryDataIDForNonDIALOGFromPartListEntry(partListEntry,
                                                                          allFactoryDataForPartlistEntryUnfilteredCopy);
                    if (pastedFactoryDataId != null) {
                        // aDat mit eintragen
                        pastedFactoryDataId = new iPartsFactoryDataId(pastedFactoryDataId.getGuid(),
                                                                      aDat, pastedFactoryDataId.getSeqNo());
                        newFactoryData = new iPartsDataFactoryData(project, pastedFactoryDataId);
                        newFactoryData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    }
                }

                if (newFactoryData != null) {
                    // Attribute, die von den kopierten Werkseinsatzdaten übernommen werden
                    setFieldsToSelection(toPasteData, newFactoryData, KEY_MAPPING_PART);

                    // Aggregate Typ vom Stücklisteneintrag übernehmen
                    if (StrUtils.isValid(aggregateType)) {
                        newFactoryData.getAttributes().addField(iPartsDataVirtualFieldsDefinition.DFD_AGGREGATE_TYPE, aggregateType, true, DBActionOrigin.FROM_DB);
                    }
                    // Alle kopierten Werkseinsatzdaten haben Quelle iParts
                    newFactoryData.setAttributeValue(FIELD_DFD_SOURCE, iPartsImportDataOrigin.IPARTS.getOrigin(), DBActionOrigin.FROM_EDIT);

                    if (withLink) {
                        newFactoryData.setFieldValueAsBoolean(FIELD_DFD_LINKED, true, DBActionOrigin.FROM_EDIT);
                        if (!validFactoriesForProduct.contains(newFactoryData.getFactory())) {
                            // beim Koppeln bekommen Werksdaten von Werken, die zum Produkt nicht gültig sind, den Status RELEASED
                            newFactoryData.setFieldValue(FIELD_DFD_STATUS, iPartsDataReleaseState.RELEASED.getDbValue(), DBActionOrigin.FROM_EDIT);
                        } else {
                            // Alle Werksdaten zu gültigen Werken bekommen den Status NEU
                            newFactoryData.setFieldValue(FIELD_DFD_STATUS, iPartsDataReleaseState.NEW.getDbValue(), DBActionOrigin.FROM_EDIT);
                        }
                    }

                    newFactoryDataList.add(newFactoryData, DBActionOrigin.FROM_EDIT);
                    if (allFactoryDataForPartlistEntryUnfilteredCopy != null) {
                        // Liste für ELDAS aktualisieren um die passende Sequenznummer zu finden
                        allFactoryDataForPartlistEntryUnfilteredCopy.add(newFactoryData, DBActionOrigin.FROM_EDIT);
                    }
                }
            }

            if (newFactoryDataList.isEmpty()) {
                if (showMessageDialogs) {
                    MessageDialog.show("!!Kopierte Werksdaten konnten nicht eingefügt werden. Der Stücklistentyp ist verschieden!",
                                       "!!Kopieren", MessageDialogIcon.INFORMATION, MessageDialogButtons.OK);
                }
                return null;
            }

        } else {
            if (showMessageDialogs) {
                MessageDialog.show("!!Keine Daten kopiert. Kopierte Werksdaten wurden nach Prüfung auf gültige Werke vollständig ausgefiltert.",
                                   "!!Kopieren", MessageDialogIcon.INFORMATION, MessageDialogButtons.OK);
            }
            return null;
        }

        // Hinweis auf ausgefilterte Werkseinsatzdaten
        if ((toPasteDataFiltered.size() != originalPasteDataSize) && showMessageDialogs) {
            MessageDialog.show("!!Kopierte Werksdaten wurden nach Prüfung auf gültige Werke teilweise ausgefiltert.",
                               "!!Kopieren", MessageDialogIcon.INFORMATION, MessageDialogButtons.OK);
        }

        return newFactoryDataList;
    }

    public static iPartsDataColorTableFactoryList pasteFactoryDataOfColortableToPart(EtkProject project,
                                                                                     iPartsColorTableToPartId colorTablePartId) {
        List<Map<String, String>> toPasteData = CopyAndPasteData.getToPasteData();
        if (toPasteData != null) {
            // Aktuelles Datum für Adat
            String aDat = SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance());

            iPartsColorTableFactoryId pastedColortableFactoryId = new iPartsColorTableFactoryId(colorTablePartId.getColorTableId(),
                                                                                                colorTablePartId.getPosition(),
                                                                                                "",
                                                                                                aDat,
                                                                                                iPartsFactoryDataTypes.COLORTABLE_PART_AS.getDbValue(),
                                                                                                colorTablePartId.getSDATA());
            return pasteFactoryDataOfColortable(toPasteData, project, pastedColortableFactoryId);
        }
        return null;
    }

    public static iPartsDataColorTableFactoryList pasteFactoryDataOfColortableContent(EtkProject project,
                                                                                      iPartsColorTableContentId colorTableContentId) {
        List<Map<String, String>> toPasteData = CopyAndPasteData.getToPasteData();
        if (toPasteData != null) {
            // Aktuelles Datum für Adat
            String aDat = SQLStringConvert.calendarToPPDateTimeString(Calendar.getInstance());

            iPartsColorTableFactoryId pastedColortableFactoryId = new iPartsColorTableFactoryId(colorTableContentId.getColorTableId(),
                                                                                                colorTableContentId.getPosition(),
                                                                                                "",
                                                                                                aDat,
                                                                                                iPartsFactoryDataTypes.COLORTABLE_CONTENT_AS.getDbValue(),
                                                                                                colorTableContentId.getSDATA());
            return pasteFactoryDataOfColortable(toPasteData, project, pastedColortableFactoryId);
        }
        return null;
    }

    private static iPartsDataColorTableFactoryList pasteFactoryDataOfColortable(List<Map<String, String>> toPasteDataList,
                                                                                EtkProject project,
                                                                                iPartsColorTableFactoryId colorTableFactoryId) {
        // Ergebnis
        iPartsDataColorTableFactoryList newFactoryDataList = new iPartsDataColorTableFactoryList();

        boolean wasNonDialogData = false;

        for (Map<String, String> toPasteData : toPasteDataList) {
            // nur DIALOG Werksdaten bei Farbtabellen einfügen
            if (!StrUtils.toBoolean(toPasteData.get(IS_DIALOG))) {
                wasNonDialogData = true;
                continue;
            }

            iPartsDataColorTableFactory newColortableFactoryData = new iPartsDataColorTableFactory(project, colorTableFactoryId);
            newColortableFactoryData.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);

            //Attribute, die von den kopierten Werkseinsatzdaten übernommen werden
            setFieldsToSelection(toPasteData, newColortableFactoryData, KEY_MAPPING_COLORTABLE);

            // Alle kopierten Werkseinsatzdaten haben Quelle iParts
            newColortableFactoryData.setAttributeValue(FIELD_DCCF_SOURCE, iPartsImportDataOrigin.IPARTS.getOrigin(), DBActionOrigin.FROM_EDIT);

            // PK update, weil im Mapping unter anderem die Factory gesetzt wird
            newColortableFactoryData.updateIdFromPrimaryKeys();
            newColortableFactoryData.updateOldId();

            newFactoryDataList.add(newColortableFactoryData, DBActionOrigin.FROM_EDIT);
        }
        if (newFactoryDataList.isEmpty()) {
            if (wasNonDialogData) {
                MessageDialog.show("!!Es dürfen nur Daten mit Quelle DIALOG eingefügt werden.", "!!Kopieren");
            } else {
                MessageDialog.show("!!Kopierte Werksdaten konnten nicht eingefügt werden.", "!!Kopieren");
            }
            return null;
        }

        return newFactoryDataList;
    }

    /**
     * Kopiert alle relevanten Werksdaten für Nicht-DIALOG-Stücklisteneinträge.
     *
     * @param targetPartListEntry Ziel-Stücklisteneintrag
     * @param factoryDataList
     * @param project             EtkProject
     * @return Liefert nur kopierte Werksdaten zurück, die aufgrund einer neuen GUID auch wirklich notwendig sind.
     */
    public static iPartsDataFactoryDataList copyNonDIALOGFactoryDataOfPartListEntry(EtkDataPartListEntry targetPartListEntry,
                                                                                    iPartsDataFactoryDataList factoryDataList,
                                                                                    EtkProject project) {
        String targetSourceGUID = targetPartListEntry.getFieldValue(FIELD_K_SOURCE_GUID);
        iPartsDataFactoryDataList copiedFactoryDataList = new iPartsDataFactoryDataList();
        for (iPartsDataFactoryData dataFactoryData : factoryDataList) {
            if (!dataFactoryData.getAsId().getGuid().equals(targetSourceGUID)) {
                iPartsDataFactoryData copiedDataFactoryData = new iPartsDataFactoryData(project, dataFactoryData.getAsId());
                copiedDataFactoryData.initAttributesWithDefaultValues(DBActionOrigin.FROM_EDIT);
                copiedDataFactoryData.assignAttributesValues(project, dataFactoryData.getAttributes(), false, DBActionOrigin.FROM_EDIT);

                // DFD_GUID muss als einziges geändert werden
                copiedDataFactoryData.setFieldValue(FIELD_DFD_GUID, targetSourceGUID, DBActionOrigin.FROM_EDIT);
                copiedDataFactoryData.updateOldId();

                copiedFactoryDataList.add(copiedDataFactoryData, DBActionOrigin.FROM_EDIT);
            }
        }
        return copiedFactoryDataList;
    }

    /**
     * Beim Entkoppeln und bei Kopplung einer Teilepos mit schon vorhandener Kopplung muss die alte Kopplung aufgelöst werden
     * Eintrag in DA_DIALOG löschen
     *
     * @param guidOfLinkDestination               Guid des Ziel-Stücklisteneintrags
     * @param project                             EtkProject
     * @param changedDataObjectsWithoutChangeSets Datensätze, die ohne Changeset gespeichert werden
     * @return hat es geklappt
     */
    public static boolean unlink(String guidOfLinkDestination, EtkProject project, EtkDataObjectList changedDataObjectsWithoutChangeSets) {
        if (StrUtils.isValid(guidOfLinkDestination)) {
            iPartsDialogId constructionIdOfDestination = new iPartsDialogId(guidOfLinkDestination);
            iPartsDataDialogData dialogDataLinkDestination = new iPartsDataDialogData(project, constructionIdOfDestination);
            if (dialogDataLinkDestination.existsInDB()) {
                dialogDataLinkDestination.setFieldValue(FIELD_DD_LINKED_FACTORY_DATA_GUID, "", DBActionOrigin.FROM_EDIT);
                changedDataObjectsWithoutChangeSets.add(dialogDataLinkDestination, DBActionOrigin.FROM_EDIT);
            }
        }

        return !changedDataObjectsWithoutChangeSets.isEmpty();
    }

    /**
     * Mit Hilfe der GUID der vorherigen Kopplung können die betroffenen Werkseinsatzdaten geladen werden
     *
     * @param changedDataEntryList
     * @param dataInTopGrid
     * @return
     */
    public static boolean overwriteLinkFlag(iPartsDataFactoryDataList changedDataEntryList, List<iPartsDataFactoryData> dataInTopGrid) {
        if (!dataInTopGrid.isEmpty()) {
            iPartsDataFactoryDataList factoryDataListInGrid = new iPartsDataFactoryDataList();
            factoryDataListInGrid.addAll(dataInTopGrid, DBActionOrigin.FROM_EDIT);
            overwriteLinkFlag(changedDataEntryList, factoryDataListInGrid);
        }
        return !changedDataEntryList.isEmpty();
    }

    /**
     * Falls die Kopplung nur überschrieben wird, reicht das ändern der vorher gekoppelten Werkseinsatzdaten
     * DA_DIALOG wird mit der neuen Kopplung überschrieben
     *
     * @param changedDataEntryList
     * @param factoryDataList
     */
    private static void overwriteLinkFlag(iPartsDataFactoryDataList changedDataEntryList, iPartsDataFactoryDataList factoryDataList) {
        // Wenn in dieser Liste schon gekoppelte Werkseinsatzdaten sind, diese Entkoppeln
        for (iPartsDataFactoryData factoryData : factoryDataList) {
            if (factoryData.getFieldValueAsBoolean(FIELD_DFD_LINKED)) {
                factoryData.setFieldValueAsBoolean(FIELD_DFD_LINKED, false, DBActionOrigin.FROM_EDIT);
                changedDataEntryList.add(factoryData, DBActionOrigin.FROM_EDIT);
            }
        }
    }


    /**
     * Fügt die in KEY definierten Felder aus dem kopierten Datensatz zu den übergebenen Werkseinsatzdaten hinzu
     *
     * @param pastedData
     * @param newFactoryData
     * @param keys
     */
    public static void setFieldsToSelection(Map<String, String> pastedData, iPartsEtkDataObjectFactoryDataInterface newFactoryData, Map<MAPPING_KEYS, String> keys) {
        for (Map.Entry<MAPPING_KEYS, String> entry : keys.entrySet()) {
            String value = pastedData.get(entry.getKey().name());
            if (value == null) {
                value = "";
            }
            newFactoryData.setAttributeValue(entry.getValue(), value, DBActionOrigin.FROM_EDIT);
        }
    }

    /**
     * Speichert die Änderungen, die durch das Koppeln entstanden sind
     *
     * @param changedDataWithoutChangeSet    Änderungen in DA-DIALOG
     * @param changedDataInChangeSetToCommit Der Datensatz der Committed wird, der durch das Koppeln entstanden ist
     * @param afterOverwrite                 Der Datensatz der Committed wird, der durch das Entkoppeln entstanden ist
     * @param abstractRevisionChangeSet      Changeset in dem gespeichert wird
     * @param statusFieldName                Name des Status-Feldes
     * @param seriesNoFieldName              Name des Baureihen-Feldes
     * @return
     */
    public static boolean saveChangesForLinkingRunnable(EtkProject project,
                                                        final EtkDataObjectList changedDataWithoutChangeSet,
                                                        final EtkDataObjectList changedDataInChangeSetToCommit,
                                                        final iPartsDataFactoryDataList afterOverwrite,
                                                        AbstractRevisionChangeSet abstractRevisionChangeSet,
                                                        String statusFieldName,
                                                        String seriesNoFieldName) {
        boolean fireDataChangedEvent = false;
        project.executeWithoutActiveChangeSets(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!changedDataWithoutChangeSet.isEmpty()) {
                        changedDataWithoutChangeSet.saveToDB(project);
                    }

                    if (!changedDataInChangeSetToCommit.isEmpty()) {
                        abstractRevisionChangeSet.addDataObjectListCommitted(changedDataInChangeSetToCommit);
                    }
                    if (!afterOverwrite.isEmpty()) {
                        abstractRevisionChangeSet.addDataObjectListCommitted(afterOverwrite);
                    }

                    if (!afterOverwrite.isEmpty()) {
                        for (iPartsDataFactoryData factoryData : afterOverwrite) {
                            iPartsDataFactoryData factoryDataInDB = new iPartsDataFactoryData(project, factoryData.getAsId());
                            if (factoryDataInDB.existsInDB()) {
                                factoryDataInDB.setFieldValueAsBoolean(FIELD_DFD_LINKED, false, DBActionOrigin.FROM_EDIT);
                                changedDataInChangeSetToCommit.add(factoryDataInDB, DBActionOrigin.FROM_EDIT);
                            }
                        }
                    }

                    if (!changedDataInChangeSetToCommit.isEmpty()) {
                        if (changedDataInChangeSetToCommit.saveToDB(project)) {
                            // Wo Status neu ist muss ein Dialog_Changes Eintrag in die Datenbank geschrieben werden
                            iPartsDataDIALOGChangeList dialogChangesToSave = createDialogChangesForNew(project,
                                                                                                       changedDataInChangeSetToCommit,
                                                                                                       statusFieldName,
                                                                                                       seriesNoFieldName);
                            dialogChangesToSave.saveToDB(project);
                        }
                    }
                } catch (Exception e) {
                    Session session = Session.get();
                    if ((session != null) && session.canHandleGui()) {
                        Logger.logExceptionWithoutThrowing(iPartsPlugin.LOG_CHANNEL_DEBUG, LogType.ERROR, e);
                        GuiWindow.showWaitCursorForCurrentWindow(false);
                        MessageDialog.showError("!!Fehler beim Speichern.");
                    } else {
                        throw e;
                    }
                }
            }

            private iPartsDataDIALOGChangeList createDialogChangesForNew(EtkProject project,
                                                                         EtkDataObjectList<? extends EtkDataObject> changedDataInChangeSetToCommit,
                                                                         String statusFieldName,
                                                                         String seriesNoFieldName) {
                iPartsDataDIALOGChangeList dialogChangesToSave = new iPartsDataDIALOGChangeList();
                for (Object changedData : changedDataInChangeSetToCommit) {
                    if (changedData instanceof iPartsDataFactoryData) {
                        iPartsDataFactoryData factoryData = (iPartsDataFactoryData)changedData;
                        if (factoryData.getFieldValue(statusFieldName).equals(iPartsDataReleaseState.NEW.getDbValue())) {
                            iPartsDataDIALOGChange dialogChangeOfNew = FactoryDataHelper.getFactoryDataDIALOGChanges(project, factoryData, seriesNoFieldName);
                            if (dialogChangeOfNew != null) {
                                if (!dialogChangeOfNew.existsInDB()) {
                                    dialogChangeOfNew.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                                    dialogChangeOfNew.setFieldValue(FIELD_DDC_SERIES_NO, factoryData.getFieldValue(seriesNoFieldName),
                                                                    DBActionOrigin.FROM_EDIT);
                                    dialogChangeOfNew.setFieldValue(FIELD_DDC_BCTE, factoryData.getFieldValue(FIELD_DFD_GUID),
                                                                    DBActionOrigin.FROM_EDIT);
                                    dialogChangeOfNew.setFieldValue(FIELD_DDC_MATNR, "", DBActionOrigin.FROM_EDIT);
                                    dialogChangeOfNew.setFieldValue(FIELD_DDC_KATALOG_ID, "", DBActionOrigin.FROM_EDIT);
                                    dialogChangesToSave.add(dialogChangeOfNew, DBActionOrigin.FROM_EDIT);
                                }
                            }
                        }
                    }
                }
                return dialogChangesToSave;
            }
        }, fireDataChangedEvent);
        return fireDataChangedEvent;
    }


    /**
     * Prüft, ob sich etwas im Zwischenspeicher befindet
     *
     * @return
     */
    public static boolean isCopyCacheFilled() {
        return getToPasteData() != null;
    }

    public static List<Map<String, String>> getCopyCacheData() {
        return getToPasteData();
    }

    public static void setCopyCacheData(List<Map<String, String>> copyCacheData) {
        Session session = Session.get();
        if (session != null) {
            session.setAttribute(SESSION_KEY_COPY_PASTE_FACTORY_DATA, copyCacheData);
        }
    }

    /**
     * War die Quellteileposition schon gekoppelt?
     *
     * @return
     */
    public static boolean isSourceAlreadyATarget() {
        List<Map<String, String>> sessionData = getToPasteData();
        if (sessionData != null) {
            for (Map<String, String> data : sessionData) {
                if (data.containsKey(IS_SOURCE_LINKED)) {
                    if (StrUtils.toBoolean(data.get(IS_SOURCE_LINKED))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Ist der Werksdatensatz schon gekoppelt
     *
     * @param dataObject Werksdatensatz
     * @return Ist der Werksdatensatz schon gekoppelt
     */
    public static boolean isFactoryDataAlreadyLinked(EtkDataObject dataObject) {
        if (dataObject instanceof iPartsDataFactoryData) {
            return dataObject.getFieldValueAsBoolean(FIELD_DFD_LINKED);
        }
        return false;
    }

    /**
     * Ist die Ziel-Guid schon in DA_DIALOG unter FIELD_DD_LINKED_FACTORY_DATA_GUID eingetragen und
     * die Teilepos somit schon eine Quelle
     *
     * @param project
     * @param targetGUID
     * @return
     */
    public static boolean isTargetAlreadyASource(EtkProject project, String targetGUID) {
        iPartsDataDialogDataList dataDialogData = iPartsDataDialogDataList.loadBCTEKeyForLinkedFactoryDataGuid(project, targetGUID);
        return !dataDialogData.isEmpty();
    }

    /***
     * Leert den Zwischenspeicher
     */
    public static void clearCopyCache() {
        Session session = Session.get();
        if ((session != null) && session.hasAttribute(CopyAndPasteData.SESSION_KEY_COPY_PASTE_FACTORY_DATA)) {
            session.removeAttribute(CopyAndPasteData.SESSION_KEY_COPY_PASTE_FACTORY_DATA);

        }
    }

    /**
     * Die Quelle darf sich sich nicht selber koppeln
     *
     * @param guidOfLinkTarget
     * @return
     */
    public static boolean isSourceTheTarget(String guidOfLinkTarget) {
        return guidOfLinkSource.equals(guidOfLinkTarget);
    }

    public static List<String> getWarningMessageUnlink() {
        return warningMessageUnlink;
    }

    public static void addWarningMessageUnlink(String key, String... placeHolderTexts) {
        warningMessageUnlink.add(TranslationHandler.translate(key, placeHolderTexts));
    }
}
