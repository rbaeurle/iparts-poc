/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.sap_ctt;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartList;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataAssembly;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPAS;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataModuleEinPASList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditConstructionToRetailHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.construction.helper.EdsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.kgtu.KgTuId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.AbstractEditFileImportHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.transfer_to_as.TransferToASElement;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.filechooser.GuiFileChooserTextfield;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Pseudo-Formular, das die Anzeige des Upload-Dialogs und MessageLogForm für die Bearbeitung verbindet
 */
public class EditImportCTTForm extends AbstractEditFileImportHelper {

    private CTTFileAnalyzer fileAnalyzer;
    private List<CTTImportContainerList> totalList;
    private boolean saveToDB = true;

    /**
     * Erzeugt eine Instanz von EditImportImageForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     */
    public EditImportCTTForm(AbstractJavaViewerFormIConnector dataConnector) {
        super(dataConnector);
    }

    /**
     * Vorbereitung für Import
     *
     * @return
     */
    @Override
    protected boolean prepareFileImport() {
        fileAnalyzer = new CTTFileAnalyzer(getProject());
        fileAnalyzer.setMessageLogForm(messageLogForm);
        totalList = new DwList<>();
        return true;
    }

    /**
     * eine (von mehreren) Dateien lesen und analysieren
     *
     * @param file
     */
    @Override
    protected void handleFile(DWFile file) {
        CTTImportContainerList importList = new CTTImportContainerList();
        // DAIMLER-10779: Analyse der Datei
        if (fileAnalyzer.analyzeCTTFile(file, importList)) {
            if (fileAnalyzer.hasWarningsOrErrors()) {
                hasErrorsOrWarnings = true;
            }
            if (!importList.isEmpty()) {
                totalList.add(importList);
            }
        } else {
            // Fehlerhandling
            hasErrors = true;
            hasErrorsOrWarnings = true;
        }
    }

    /**
     * Abfrage ob, nach Lesen der Datei(en), der Import fortgesetzt werden soll
     *
     * @return
     */
    @Override
    protected boolean isImportAllowed() {
        boolean doImport = true;
        if (hasErrorsOrWarnings) {
            if (!totalList.isEmpty()) {
                if (MessageDialog.showYesNo("!!Es sind Fehler aufgetreten. Sollen die restlichen Teilepositionen importiert werden?", "!!CTT-Import") != ModalResult.YES) {
                    doImport = false;
                }
            } else {
                doImport = false;
            }
        }
        return doImport;
    }

    /**
     * Import vornehmen
     */
    @Override
    protected void doImport() {
        dataObjectsToBeSaved = new GenericEtkDataObjectList<>();
        iPartsDataAssembly destAssembly = null;
        EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();
        if (currentAssembly instanceof iPartsDataAssembly) {
            destAssembly = (iPartsDataAssembly)currentAssembly;
        }
        if (destAssembly != null) {
            if (destAssembly.isSAAssembly()) {
                fireMessage("!!Importiere in SA-TU...");
            } else {
                fireMessage("!!Importiere...");
            }
            getProject().startPseudoTransactionForActiveChangeSet(true);
            try {

                // DAIMLER-10781: Materialstamm anlegen bei Bedarf
                handleMissingMatNos(totalList);

                // DAIMLER-10782: Teilepositionen anlegen und einsortieren
                createAndHandlePartListEntries(totalList, destAssembly);
            } finally {
                getProject().stopPseudoTransactionForActiveChangeSet();
            }
        } else {
            // sollte nie vorkommen
            fireError("!!Ungültiger TU!");
        }
    }

    private void createAndHandlePartListEntries(List<CTTImportContainerList> totalList, iPartsDataAssembly destAssembly) {
        List<TransferToASElement> transferList = createTransferList(totalList, destAssembly);
        if (!transferList.isEmpty()) {
            if (transferList.size() == 1) {
                fireMessage("!!Bilde Teileposition...");
            } else {
                fireMessage("!!Bilde Teilepositionen (%1)...", String.valueOf(transferList.size()));
            }
            int maxPos = transferList.size();
            int currentPos = 0;
            boolean assemblyIsNew = false;
            List<String> logMessages = new DwList<>();
            AssemblyId destAssemblyId = destAssembly.getAsId();
            DBDataObjectList<EtkDataPartListEntry> destPartList = EditModuleHelper.getDestPartList(destAssembly, assemblyIsNew);
            // bei vorhandenen Modulen des DocuTyp vom Modul verwenden
            iPartsDocumentationType documentationType = destAssembly.getDocumentationType();
            iPartsModuleTypes moduleType = documentationType.getModuleType(false);
            // die Ziel-Stückliste für schnelleren Zugriff als Map mit der sourceGUID als Schlüssel ablegen
            // wird unter anderem verwendet um zu prüfen ob der Eintrag schon übernommen wurde
            HashMap<String, List<EtkDataPartListEntry>> destPartListSourceGUIDMap = new HashMap<>();
            VarParam<String> finalSeqNr = new VarParam<>("");
            VarParam<Integer> destLfdNr = new VarParam<>(0);
            // Startwert für die laufende Nummer und Sequenznummer auf Basis der höchsten existierenden laufenden
            // Nummer bzw. Sequenznummer bestimmt
            // maximaler Hotspot wird für EDS nicht benötigt, bleibt im Zweifelsfall leer
            EditModuleHelper.preprocessDestPartListEntries(destPartList, destLfdNr, finalSeqNr, null,
                                                           destPartListSourceGUIDMap, null, moduleType,
                                                           "", null);
            List<EtkDataPartListEntry> sourcePartListEntriesToTransferFiltered = new ArrayList<>();

            for (TransferToASElement rowContent : transferList) {
                // sourceContext ist für CTT-Stücklisten die SAA (wie bei EDS)
                String sourceContext = "";
                EtkDataArray saaValidity = rowContent.getSelectedPartlistEntry().getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);
                if ((saaValidity != null) && !saaValidity.isEmpty()) {
                    String saa = saaValidity.getArrayAsStringList().get(0); // CTT-Stücklisteneinträge haben nur eine SAA-Gültigkeit
                    sourceContext = EditConstructionToRetailHelper.createSourceContext(iPartsEntrySourceType.EDS, new EdsSaaId(saa));
                }

                currentPos++;
                fireProgress(currentPos, maxPos);

                // DAIMLER-9238: laufende Nummer hochzählen und reservieren
                destLfdNr.setValue(iPartsDataReservedPKList.getAndReserveNextKLfdNr(getProject(), destAssemblyId, destLfdNr.getValue()));
                // Den neuen Stücklisteneintrag erzeugen
                PartListEntryId destPartListEntryId = new PartListEntryId(destAssemblyId.getKVari(), destAssemblyId.getKVer(),
                                                                          EtkDbsHelper.formatLfdNr(destLfdNr.getValue()));
                EtkDataPartListEntry destPartListEntry = EditConstructionToRetailHelper.createRetailPartListEntry(sourceContext,
                                                                                                                  rowContent.getSelectedPartlistEntry(),
                                                                                                                  destPartListEntryId,
                                                                                                                  moduleType, true,
                                                                                                                  getProject(), logMessages);
                // Einsortieren nur nach Hotspot (sourceGUID wird absichtlich leer übergeben)
                EditModuleHelper.setHotSpotAndNextSequenceNumberELDAS(destPartListEntry, rowContent.getHotspot(),
                                                                      destPartList, finalSeqNr.getValue(), sourcePartListEntriesToTransferFiltered);

                // SAA Gültigkeit und Baumustergültigkeit bereits durch createRetailPartListEntry() gesetzt
                // MBS: Strukturstufe immer auf 1 setzen
                destPartListEntry.setFieldValue(iPartsConst.FIELD_K_HIERARCHY, "1", DBActionOrigin.FROM_EDIT);

                // Bei MBS eine K_SOURCE_GUID analog der aus der ELDAS Migration verwenden
                rowContent.setSourceGUID(EditConstructionToRetailHelper.createNonDIALOGSourceGUID(destPartListEntry.getAsId()));
                // SourceGUID des zu übernehmenden Eintrags bestimmen
                String sourceGUID = rowContent.getSourceGUIDForAttribute();
                destPartListEntry.setFieldValue(iPartsConst.FIELD_K_SOURCE_GUID, sourceGUID, DBActionOrigin.FROM_EDIT);

                // Array-ID in SAA_VALIDITY richtig setzen
                saaValidity = destPartListEntry.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);
                if ((saaValidity != null) && !saaValidity.isEmpty()) {
                    String arrayId = getProject().getDbLayer().getNewArrayNo(TableAndFieldName.make(iPartsConst.TABLE_KATALOG,
                                                                                                    iPartsConst.FIELD_K_SA_VALIDITY),
                                                                             destPartListEntry.getAsId().toString("|"),
                                                                             false);
                    saaValidity.setArrayId(arrayId);
                    destPartListEntry.setIdForArray(iPartsConst.FIELD_K_SA_VALIDITY, arrayId, DBActionOrigin.FROM_EDIT);
                    destPartListEntry.setFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY, saaValidity, DBActionOrigin.FROM_EDIT);
                } else {
                    destPartListEntry.setIdForArray(iPartsConst.FIELD_K_SA_VALIDITY, "", DBActionOrigin.FROM_EDIT);
                }
                destPartListEntry.setIdForArray(iPartsConst.FIELD_K_MODEL_VALIDITY, "", DBActionOrigin.FROM_EDIT);
                EditModuleHelper.finishPartListEntryCreation(destPartListSourceGUIDMap, sourceGUID, destPartListEntry,
                                                             sourcePartListEntriesToTransferFiltered, finalSeqNr);
                fireMessage("!!Teileposition (%1 %2) erzeugt.", destPartListEntryId.getKVari(), destPartListEntryId.getKLfdnr());
            }
            // Jetzt die neuen Einträge zur Stückliste hinzufügen
            for (EtkDataPartListEntry partListEntry : sourcePartListEntriesToTransferFiltered) {
                destPartList.add(partListEntry, DBActionOrigin.FROM_EDIT);
            }
            if (saveToDB) {
                EditModuleHelper.finishModuleModificationEDS(dataObjectsToBeSaved, destPartList, assemblyIsNew,
                                                             destAssembly, null, null, true);
            }
        } else {
            fireWarning("!!Keine Elemente zum Erzeugen von Teilepositionen gefunden!");
        }
    }

    private void handleMissingMatNos(List<CTTImportContainerList> totalList) {
        newDataParts = new HashMap<>();
        Set<String> usedMatNoSet = new HashSet<>();
        totalList.forEach((containerList) -> {
            usedMatNoSet.addAll(containerList.getMatNoSet());
        });
        if (usedMatNoSet.isEmpty()) {
            return;
        }
        List<String> missingMatNos = getMissingMatNos(usedMatNoSet);
        if (missingMatNos.isEmpty()) {
            return;
        }
        List<CTTImportContainer> missingMatNoContainers = findContainerByMatNo(totalList, missingMatNos);
        if (!missingMatNoContainers.isEmpty()) {
            String msg = "!!Es werden %1 Materialien neu angelegt.";
            if (missingMatNoContainers.size() == 1) {
                msg = "!!Es wird %1 neues Material angelegt.";
            }
            fireMessage(msg, String.valueOf(missingMatNoContainers.size()));
            fireMessage(buildPartNumberListFromContainer(missingMatNoContainers));
            // Mat anlegen
            for (CTTImportContainer container : missingMatNoContainers) {
                EtkDataPart dataPart = createPartFromContainer(container);
                if (dataPart != null) {
                    newDataParts.put(dataPart.getAsId(), dataPart);
                    dataObjectsToBeSaved.add(dataPart, DBActionOrigin.FROM_EDIT);
                }
            }
        }
    }

    private String buildPartNumberListFromContainer(List<CTTImportContainer> missingMatNoContainers) {
        if (!missingMatNoContainers.isEmpty()) {
            List<String> modelNoList = new DwList<>();
            missingMatNoContainers.forEach((container) -> {
                modelNoList.add(container.getASachNo());
            });
            return iPartsMainImportHelper.buildPartNumberList(getProject(), modelNoList);
        }
        return "";
    }


    private EtkDataPart createPartFromContainer(CTTImportContainer container) {
        String aSachNo = container.getASachNo();
        if (!StrUtils.isEmpty(aSachNo)) {
            EtkDataPart dataPart = EtkDataObjectFactory.createDataPart(getProject(), aSachNo, "");
            dataPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            dataPart.setFieldValue(iPartsConst.FIELD_M_BESTNR, aSachNo, DBActionOrigin.FROM_EDIT);
            dataPart.setFieldValueAsBoolean(iPartsConst.FIELD_M_BESTFLAG, true, DBActionOrigin.FROM_EDIT);
            // Source ist SAP_CTT
            dataPart.addSetOfEnumValueToFieldValue(iPartsConst.FIELD_M_SOURCE, iPartsImportDataOrigin.SAP_CTT.getOrigin(), DBActionOrigin.FROM_EDIT);
            // Text setzen
            // M_TEXTNR immer setzen für die Importer
            EtkMultiSprache multiLang = new EtkMultiSprache();
            dataPart.setFieldValueAsMultiLanguage(iPartsConst.FIELD_M_TEXTNR, multiLang, DBActionOrigin.FROM_EDIT);
            String matText = container.getMatText();
            if (!StrUtils.isEmpty(matText)) {
                multiLang = new EtkMultiSprache();
                multiLang.setText(Language.DE, matText);
                dataPart.setFieldValueAsMultiLanguage(iPartsConst.FIELD_M_CONST_DESC, multiLang, DBActionOrigin.FROM_EDIT);
            }

            return dataPart;
        }
        return null;
    }

    private List<CTTImportContainer> findContainerByMatNo(List<CTTImportContainerList> totalList, List<String> matNos) {
        List<CTTImportContainer> result = new DwList<>();
        List<String> helper = new DwList<>(matNos);
        for (CTTImportContainerList containerList : totalList) {
            for (CTTImportContainer container : containerList.getImportList()) {
                if (helper.contains(container.getASachNo())) {
                    result.add(container);
                    helper.remove(container.getASachNo());
                    if (helper.isEmpty()) {
                        break;
                    }
                }
            }
            if (helper.isEmpty()) {
                break;
            }
        }
        return result;
    }

    private List<String> getMissingMatNos(Set<String> usedMatNoSet) {
        EtkDataPartList list = new EtkDataPartList();
        list.setSearchWithoutActiveChangeSets(false);
        String[] whereTableAndFields = new String[usedMatNoSet.size()];
        String[] whereValues = new String[usedMatNoSet.size()];
        int index = 0;
        String tableAndFieldName = TableAndFieldName.make(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR);
        for (String matNo : usedMatNoSet) {
            whereTableAndFields[index] = tableAndFieldName;
            whereValues[index] = matNo;
            index++;
        }
        String[] sortFields = new String[]{ iPartsConst.FIELD_M_MATNR };
        EtkDisplayFields selectedFields = new EtkDisplayFields();
        EtkDisplayField selectField = new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR, false, false);
        selectedFields.addFeld(selectField);
        List<String> missingMatNos = new DwList<>(usedMatNoSet);

        list.searchSortAndFillWithJoin(getProject(), null, selectedFields, whereTableAndFields, whereValues,
                                       true, sortFields, false, new EtkDataObjectList.FoundAttributesCallback() {

                    @Override
                    public boolean foundAttributes(DBDataObjectAttributes attributes) {
                        String matNo = attributes.getFieldValue(iPartsConst.FIELD_M_MATNR);
                        missingMatNos.remove(matNo);
                        return false;
                    }
                });

        return missingMatNos;
    }

    private List<TransferToASElement> createTransferList(List<CTTImportContainerList> totalList, iPartsDataAssembly destAssembly) {
        iPartsProduct product = null;
        KgTuId kgTuId = null;
        if (destAssembly.isSAAssembly()) {
            return createTransferListForSA(totalList, destAssembly);
        }
        List<TransferToASElement> transferList = new DwList<>();
        iPartsProductId productId = destAssembly.getProductIdFromModuleUsage();
        if (productId != null) {
            product = iPartsProduct.getInstance(getProject(), productId);
            // Produktstruktur über Produkt-Cache bestimmen
            iPartsConst.PRODUCT_STRUCTURING_TYPE productType = product.getProductStructuringType();
            iPartsDataModuleEinPASList moduleEinPASList = iPartsDataModuleEinPASList.loadForProductAndModule(getProject(), productId, destAssembly.getAsId());
            if (!moduleEinPASList.isEmpty()) {
                iPartsDataModuleEinPAS moduleEinPAS = moduleEinPASList.get(0);
                if (productType == iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU) {
                    // Wird sind in einem KGTU Modul
                    String kg = moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_KG);
                    String tu = moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_TU);
                    kgTuId = new KgTuId(kg, tu);
                }
            } else {
                fireError("!!Der TU ist nicht verortet!");
            }
        } else {
            fireError("!!Dem TU ist kein Produkt zugeordnet!");
        }
        if (kgTuId != null) {
            for (CTTImportContainerList containerList : totalList) {
                for (CTTImportContainer container : containerList.getImportList()) {
                    EtkDataPartListEntry selectedPartlistEntry = buildDummySelectedPartListEntry(container);
                    if (!StrUtils.isEmpty(containerList.getSaaValidity())) {
                        // saaNo in SAA_VALIDITY hinzufügen
                        EtkDataArray saaValidity = new EtkDataArray();
                        saaValidity.add(containerList.getSaaValidity());
                        selectedPartlistEntry.setFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY, saaValidity, DBActionOrigin.FROM_DB);
                    }

                    // iPartsConstructionPrimaryKey wird für MBS nicht benötigt und deshalb hier mit null übergeben
                    TransferToASElement transferElement = new TransferToASElement(destAssembly.getAsId(), kgTuId, "", product,
                                                                                  "", null, null, null,
                                                                                  selectedPartlistEntry);
                    transferList.add(transferElement);
                }
            }
        }
        return transferList;
    }

    private List<TransferToASElement> createTransferListForSA(List<CTTImportContainerList> totalList, iPartsDataAssembly destAssembly) {
        List<TransferToASElement> transferList = new DwList<>();
        DBDataObjectList<EtkDataPartListEntry> partListUnfiltered = destAssembly.getPartListUnfiltered(null, false, false);
        Map<String, List<EtkDataPartListEntry>> existingEntries = new HashMap<>();
        for (EtkDataPartListEntry partListEntry : partListUnfiltered) {
            String matNr = partListEntry.getPart().getAsId().getMatNr();
            existingEntries.putIfAbsent(matNr, new DwList<>());
            existingEntries.get(matNr).add(partListEntry);
        }
        for (CTTImportContainerList containerList : totalList) {
            String saaNumber = containerList.getSaaValidity();
            for (CTTImportContainer container : containerList.getImportList()) {
                EtkDataPartListEntry referenceEntry = null;
                if (!StrUtils.isEmpty(saaNumber)) {
                    String matNr = container.getASachNo();
                    List<EtkDataPartListEntry> sameMatNoList = existingEntries.get(matNr);
                    if (sameMatNoList != null) {
                        for (EtkDataPartListEntry similarEntry : sameMatNoList) {
                            EtkDataArray similarSaValidity = similarEntry.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);
                            if (similarSaValidity.containsValue(saaNumber)) {
                                // Eintrag mit gleicher Materialnummer und passender SAA Gültigkeit gefunden
                                referenceEntry = similarEntry;
                                break;
                            }
                        }
                    }
                }
                EtkDataPartListEntry selectedPartlistEntry = buildDummySelectedPartListEntry(container);
                if (!StrUtils.isEmpty(saaNumber)) {
                    // saaNo in SAA_VALIDITY hinzufügen
                    EtkDataArray saaValidity = new EtkDataArray();
                    saaValidity.add(saaNumber);
                    selectedPartlistEntry.setFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY, saaValidity, DBActionOrigin.FROM_DB);
                }

                TransferToASElement transferElement = new TransferToASElement();
                transferElement.setAssemblyId(destAssembly.getAsId());
                transferElement.setSaModuleNumber(destAssembly.getAsId().getKVari());
                transferElement.setSelectedPartlistEntry(selectedPartlistEntry);
                String hotspot = "";
                if (referenceEntry != null) {
                    hotspot = referenceEntry.getFieldValue(iPartsConst.FIELD_K_POS);
                }
                transferElement.setHotspot(hotspot);
                transferList.add(transferElement);
            }
        }
        return transferList;
    }

    private EtkDataPartListEntry buildDummySelectedPartListEntry(CTTImportContainer container) {
        EtkDataPartListEntry dummyPartListEntry = EtkDataObjectFactory.createDataPartListEntry(getProject(), new PartListEntryId("", "", ""));
        dummyPartListEntry.initAttributesWithEmptyValues(DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        // relevante virtuelle Fields hinzufügen
        DBDataObjectAttributes attributes = dummyPartListEntry.getAttributes();
        attributes.addField(iPartsDataVirtualFieldsDefinition.MBS_QUANTITY, container.getQuantity(), true, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        attributes.addField(iPartsConst.FIELD_K_MATNR, container.getASachNo(), false, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);

        // M_ETKZ_CTT am Material hinzufügen
        String etkZ = container.getEtkZ();
        if (!StrUtils.isEmpty(etkZ)) {
            boolean partIsNew = false;
            PartId partId = new PartId(container.getASachNo(), "");
            EtkDataPart dataPart = newDataParts.get(partId);
            if (dataPart != null) {
                partIsNew = true;
            } else {
                dataPart = dummyPartListEntry.getPart();
                if (!dataPart.existsInDB()) {
                    dataPart = null;
                }
            }
            if (dataPart != null) {
                dataPart.getAttributes().addField(iPartsConst.FIELD_M_ETKZ_CTT, etkZ, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                if (!partIsNew && dataPart.isModified()) { // Neue Materialien werden sowieso schon gespeichert
                    dataObjectsToBeSaved.add(dataPart, DBActionOrigin.FROM_EDIT);
                }
            }
        }
        return dummyPartListEntry;

    }

    /**
     * Erzeugte MessageLogForm anpassen
     *
     * @return
     */
    @Override
    protected EtkMessageLogForm createMessageLogForm() {
        EtkMessageLogForm msgLogForm = super.createMessageLogForm();
        msgLogForm.setTitle("!!SAP.CTT Dateien importieren");
        return msgLogForm;
    }

    /**
     * FileChooser erzeugen und setzen
     *
     * @return
     */
    @Override
    protected GuiFileChooserTextfield createImportFileChooser() {
        GuiFileChooserTextfield importFileChooser = super.createImportFileChooser();
        GuiLabel label = new GuiLabel();
        label.setName("importFileChooserLabel");
        label.setText("!!SAP.CTT Datei");
        importFileChooser.setTooltip(label);
        importFileChooser.setMultiSelectionMode(true);
        importFileChooser.setApproveButtonText("!!SAP.CTT-Datei auswählen");
        return importFileChooser;
    }
}
