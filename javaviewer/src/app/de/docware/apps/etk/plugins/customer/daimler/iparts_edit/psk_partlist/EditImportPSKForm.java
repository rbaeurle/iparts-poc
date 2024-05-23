/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.psk_partlist;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.db.EtkDbsHelper;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.mechanic.EtkDataAssembly;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.AssemblyId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsDocumentationType;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsModuleTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsDataReservedPKList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditConstructionToRetailHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditModuleHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.helper.DictMetaTechChangeSetHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.importer.iPartsMainImportHelper;
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
import de.docware.framework.modules.gui.controls.filechooser.filefilter.DWFileFilterEnum;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkDataArray;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Import einer Stücklisten-Datei mit Anlegen der Teilepositionen und ggf MatStamm
 */
public class EditImportPSKForm extends AbstractEditFileImportHelper {

    public static final String PSK_FILE_EXTENSION = ".xlsx";
    public static final String PSK_FILE_DESCRIPTION = "!!PSK-Stückliste (*.xlsx)";
    public static final String COUNTRY_DELIMITER = "|";

    // Namen der Felder (stehen so im Header)
    static final String PSK_POS = "Pos.";
    static final String PSK_PARTNO = "A-Sachnummer";
    static final String PSK_STRUKTURSTUFE = "Strukturstufe";
    static final String PSK_MENGE_ART = "Stück";
    static final String STD_COUNTRIES = "Ländergültigkeit";
    static final String PSK_NAME_DE = "Benennung";
    static final String PSK_NAME_EN = PSK_NAME_DE + "_" + Language.EN.getCode();
    static final String PSK_NAME_FR = PSK_NAME_DE + "_" + Language.FR.getCode();
    static final String PSK_IMAGE_NO_EXTERN = "Zeichnungs-Nummer";
    static final String PSK_MANUFACTURER_CODE_1 = "Herstellercode 1";
    static final String PSK_MANUFACTURER_CODE_2 = "Herstellercode 2";
    static final String PSK_SNR_SUPPLIER_MATNO = "SNR-Lieferant";
    static final String PSK_SNR_MANUFACTURER_MATNO = "SNR-Hersteller";
    static final String PSK_REMARK = "Bemerkung";
    private static final String PSK_SACHNO = "Sach-Nr";
    private static final String PSK_DAG_NAME = "DAG-Benennung";
    private static final String PSK_DAG_SNR = "DAG-SNr";
    private static final String PSK_ACS_SNR = "ACS-SNr";
    private static final String STD_MBSPEC = "MB Spec.";
    private static final String STD_VISCOSITY = "Viscosity";
    private static final String STD_PACKSIZE = "Pack Size";
    private static final String STD_ADDTEXT = "Ergänzungstext";
    private static final String STD_NEUTRALTEXT = "sprachneutraler Text";

    private static final String[] MUST_HEADER_ENTRIES = new String[]{ PSK_POS, PSK_MENGE_ART, PSK_NAME_DE, PSK_IMAGE_NO_EXTERN,
                                                                      PSK_PARTNO, PSK_DAG_NAME, PSK_MANUFACTURER_CODE_1,
                                                                      PSK_MANUFACTURER_CODE_2, PSK_SNR_SUPPLIER_MATNO,
                                                                      PSK_SNR_MANUFACTURER_MATNO, PSK_REMARK };
    private static final String[] OPTIONAL_HEADER_ENTRIES = new String[]{ PSK_STRUKTURSTUFE,
                                                                          STD_COUNTRIES, STD_MBSPEC, STD_VISCOSITY,
                                                                          STD_PACKSIZE, STD_ADDTEXT, STD_NEUTRALTEXT,
                                                                          PSK_NAME_EN, PSK_NAME_FR };
    // Die Reihenfolge von STD_PACKSIZE und STD_VISCOSITY bestimmt auch die Reihenfolge im Kombinierten Text!
    static final String[] NO_PSK_FIELDS = new String[]{ STD_COUNTRIES, STD_MBSPEC, STD_ADDTEXT, STD_PACKSIZE,
                                                        STD_VISCOSITY, STD_NEUTRALTEXT };
    private static final String[] NAVI_CAR_FIELDS = new String[]{ STD_ADDTEXT };

    private final boolean saveToDB = true; // nur zum Testen
    //    private Map<String, String> partListMapping;
    private Map<String, String> partListStdMapping;
    private iPartsDataAssembly destAssembly;
    private iPartsProduct product;
    private KgTuId kgTuId;
    private List<TransferToASElement> transferList;
    private iPartsImportDataOrigin dataOrigin;
    private Map<String, EtkMultiSprache> addTextMap;
    private boolean isRealPSK;
    private boolean isTruckImport;
    private boolean isNaviCarImport;
    private boolean matTextHasChanged;
    private DictMetaTechChangeSetHelper dictSearchHelper;
    private Set<String> naviCarModuleNoSet;

    /**
     * Erzeugt eine Instanz von EditImportImageForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     */
    public EditImportPSKForm(AbstractJavaViewerFormIConnector dataConnector, boolean isRealPSK, boolean isTruck,
                             iPartsImportDataOrigin dataOrigin) {
        super(dataConnector);
        initMapping();
        initImporter(isRealPSK, isTruck, dataOrigin);
    }

    private void initImporter(boolean isRealPSK, boolean isTruck, iPartsImportDataOrigin dataOrigin) {
        this.newDataParts = new HashMap<>();
        this.transferList = new DwList<>();
        this.dataObjectsToBeSaved = new GenericEtkDataObjectList();
        this.dictSearchHelper = new DictMetaTechChangeSetHelper(getProject(),
                                                                DictTextKindTypes.MAT_NAME,
                                                                Language.DE);
        this.isRealPSK = isRealPSK;
        this.isTruckImport = isTruck;
        this.isNaviCarImport = false;
        this.matTextHasChanged = false;
        this.dataOrigin = dataOrigin;
        this.addTextMap = new HashMap<>();
        if (isRealPSK) {
            importFileChooser.setApproveButtonText("!!PSK-/Excel-Datei auswählen");
            messageLogForm.setTitle("!!PSK-/Excel-Datei importieren");
        } else {
            if (EditModuleHelper.isCarPerspectiveAssembly(getConnector().getCurrentAssembly())) {
                importFileChooser.setApproveButtonText("!!NaviCar Excel-Datei auswählen");
                messageLogForm.setTitle("!!NaviCar Excel-Datei importieren");
            } else {
                importFileChooser.setApproveButtonText("!!Excel-Datei auswählen");
                messageLogForm.setTitle("!!Excel-Datei importieren");
            }
        }
    }

    private void initMapping() {
        partListStdMapping = new HashMap<>();
        partListStdMapping.put(iPartsConst.FIELD_K_COUNTRY_VALIDITY, STD_COUNTRIES);
        partListStdMapping.put(iPartsConst.FIELD_K_SPEC_VALIDITY, STD_MBSPEC);
        partListStdMapping.put("DUMMY_1", STD_VISCOSITY);  // wird im CombText integriert
        partListStdMapping.put("DUMMY_2", STD_PACKSIZE);   // wird im CombText integriert
        partListStdMapping.put("DUMMY_3", STD_ADDTEXT);   // wird im CombText integriert
        partListStdMapping.put("DUMMY_4", STD_NEUTRALTEXT);   // wird im CombText integriert
    }

    public boolean isMatTextChanged() {
        return matTextHasChanged;
    }

    public void setMatTextHasChanged(boolean matTextHasChanged) {
        this.matTextHasChanged = matTextHasChanged;
    }

    /**
     * Vorbereitung für Import
     *
     * @return
     */
    @Override
    protected boolean prepareFileImport() {
        destAssembly = null;
        EtkDataAssembly currentAssembly = getConnector().getCurrentAssembly();
        if (currentAssembly instanceof iPartsDataAssembly) {
            destAssembly = (iPartsDataAssembly)currentAssembly;
        }
        if (destAssembly != null) {
            if (destAssembly.isSAAssembly()) {
                fireMessage("!!Importiere in SA-TU...");
                return true;
            }
            iPartsProductId productId = destAssembly.getProductIdFromModuleUsage();
            if (productId != null) {
                product = iPartsProduct.getInstance(getProject(), productId);
                // Produktstruktur über Produkt-Cache bestimmen
                iPartsConst.PRODUCT_STRUCTURING_TYPE productType = product.getProductStructuringType();
                iPartsDataModuleEinPASList moduleEinPASList
                        = iPartsDataModuleEinPASList.loadForProductAndModule(getProject(), productId,
                                                                             destAssembly.getAsId());
                if (!moduleEinPASList.isEmpty()) {
                    iPartsDataModuleEinPAS moduleEinPAS = moduleEinPASList.get(0);
                    if (productType == iPartsConst.PRODUCT_STRUCTURING_TYPE.KG_TU) {
                        // Wird sind in einem KGTU Modul
                        String kg = moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_KG);
                        String tu = moduleEinPAS.getFieldValue(iPartsConst.FIELD_DME_SOURCE_TU);
                        kgTuId = new KgTuId(kg, tu);
                        isNaviCarImport = EditModuleHelper.isCarPerspectiveAssembly(destAssembly);
                        if (isNaviCarImport && EditModuleHelper.isCarPerspectiveKgTuId(kgTuId)) {
                            // Load AssemblyNumbers-Set vom Produkt
                            naviCarModuleNoSet = getAllModulesForProduct(productId);
                            if (naviCarModuleNoSet.isEmpty()) {
                                fireError("!!Das Produkt enthält keine TUs!");
                                return false;
                            }
                        } else {
                            if (isNaviCarImport) {
                                fireError("!!Der TU ist kein NaviCar-TU!");
                                return false;
                            }
                        }

                        fireMessage("!!Importiere...");
                        return true;
                    } else {
                        fireError("!!Das Produkt ist kein KG/TU!");
                    }
                } else {
                    fireError("!!Der TU ist nicht verortet!");
                }
            } else {
                fireError("!!Dem TU ist kein Produkt zugeordnet!");
            }
        }
        fireError("!!Ungültiger TU.");
        return false;
    }

    private Set<String> getAllModulesForProduct(iPartsProductId productId) {
        EtkDisplayFields selectedFields = new EtkDisplayFields();
        selectedFields.addFeldIfNotExists(new EtkDisplayField(iPartsConst.TABLE_DA_MODULE, iPartsConst.FIELD_DM_DOCUTYPE, false, false));
        selectedFields.addFeldIfNotExists(new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_MATNR, false, false));
        selectedFields.addFeldIfNotExists(new EtkDisplayField(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_VER, false, false));

        HashMap<iPartsProductId, Boolean> productValidityMap = new HashMap<>();
        Set<String> moduleNoSet = new HashSet<>();
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                filterAndAddModulesForRightsInSession(attributes, productValidityMap, moduleNoSet);
                return false;
            }
        };

        iPartsDataModuleList.searchAllModulesForAllProducts(getProject(), selectedFields, productId.getProductNumber(), false,
                                                            foundAttributesCallback);
        return moduleNoSet;
    }

    protected void filterAndAddModulesForRightsInSession(DBDataObjectAttributes attributes, HashMap<iPartsProductId, Boolean> productValidityMap,
                                                         Set<String> moduleNoSet) {
        iPartsDocumentationType documentationType = iPartsDocumentationType.getFromDBValue(attributes.getFieldValue(iPartsConst.FIELD_DM_DOCUTYPE));
        String moduleNumber = attributes.getFieldValue(iPartsConst.FIELD_DM_MODULE_NO);
        String productNumber = attributes.getFieldValue(iPartsConst.FIELD_DPM_PRODUCT_NO);
        iPartsProductId productId = new iPartsProductId(productNumber);
        boolean valid = iPartsFilterHelper.isModuleVisibleForUserSession(getProject(), documentationType, false, false,
                                                                         false, moduleNumber, productId,
                                                                         productValidityMap);
        if (valid) {
            // NaviCar selbst nicht aufnehmen
            if (!EditModuleHelper.isCarPerspectiveAssemblyShort(new AssemblyId(moduleNumber, ""))) {
                moduleNoSet.add(moduleNumber);
            }
        }
    }

    /**
     * Datei lesen und analysieren
     *
     * @param file
     */
    @Override
    protected void handleFile(DWFile file) {
        if (!isNaviCarImport) {
            PSKReader reader = new PSKReader(this);
            try {
                // Datei komplett lesen und relevante Daten herausholen
                if (!reader.readFile(file, MUST_HEADER_ENTRIES, OPTIONAL_HEADER_ENTRIES)) {
                    // Fehler beim Öffnen oder Lesen
                    hasErrorsOrWarnings = true;
                } else {
                    List<String> newMatList = reader.getNewMatCount();
                    if (!newMatList.isEmpty()) {
                        String msg = "!!Es werden %1 Materialien neu angelegt.";
                        if (newMatList.size() == 1) {
                            msg = "!!Es wird %1 neues Material angelegt.";
                        }
                        fireMessage(msg, String.valueOf(newMatList.size()));
                        if (newMatList.size() <= iPartsMainImportHelper.MAX_ELEMS_FOR_SHOW) {
                            fireMessage(iPartsMainImportHelper.buildPartNumberList(getProject(), newMatList));
                        } else {
                            fireMessage(iPartsMainImportHelper.buildNumberListForLogFile(newMatList).toString());
                        }
                    }
                }
            } catch (Exception e) {
                fireError("!!Es ist ein Fehler aufgetreten", e.getMessage());
            }
        } else {
            NaviCarReader reader = new NaviCarReader(this);
            try {
                // Datei komplett lesen und relevante Daten herausholen
                if (!reader.readFile(file, MUST_HEADER_ENTRIES, NAVI_CAR_FIELDS)) {
                    // Fehler beim Öffnen oder Lesen
                    hasErrorsOrWarnings = true;
                }
            } catch (Exception e) {
                fireError("!!Es ist ein Fehler aufgetreten", e.getMessage());
            }
        }
    }

    /**
     * Abfrage ob, nach Lesen der Datei, der Import fortgesetzt werden soll
     *
     * @return
     */
    @Override
    protected boolean isImportAllowed() {
        if (/*hasErrorsOrWarnings ||*/ hasErrors) {
            return false;
        }
        if (transferList.isEmpty()) {
            fireWarning("!!Keine Elemente zum Erzeugen von Teilepositionen gefunden!");
            return false;
        }
        return true;
    }

    /**
     * Import vornehmen
     */
    @Override
    protected void doImport() {
        getProject().startPseudoTransactionForActiveChangeSet(true);
        try {

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
//            DBDataObjectList<EtkDataPartListEntry> destPartList = EditModuleHelper.getDestPartList(destAssembly, assemblyIsNew);
            DBDataObjectList<EtkDataPartListEntry> destPartList = getTuPartListEntries();
            // bei vorhandenen Modulen des DocuTyp vom Modul verwenden
            iPartsDocumentationType documentationType = destAssembly.getDocumentationType();
            iPartsModuleTypes moduleType = documentationType.getModuleType(false);
            // sourceContext ist für EDS Stücklisten leer
            String sourceContext = "";
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
                currentPos++;
                fireProgress(currentPos, maxPos);

                // DAIMLER-9238: laufende Nummer hochzählen und reservieren
                destLfdNr.setValue(iPartsDataReservedPKList.getAndReserveNextKLfdNr(getProject(), destAssemblyId,
                                                                                    destLfdNr.getValue()));
                // Den neuen Stücklisteneintrag erzeugen
                PartListEntryId destPartListEntryId
                        = new PartListEntryId(destAssemblyId.getKVari(), destAssemblyId.getKVer(),
                                              EtkDbsHelper.formatLfdNr(destLfdNr.getValue()));
                EtkDataPartListEntry destPartListEntry
                        = EditConstructionToRetailHelper.createRetailPartListEntry(sourceContext,
                                                                                   rowContent.getSelectedPartlistEntry(),
                                                                                   destPartListEntryId,
                                                                                   moduleType, true,
                                                                                   getProject(), logMessages);
                replaceKatalogFields(destPartListEntry, moduleType, rowContent.getSelectedPartlistEntry());

                // Einsortieren nur nach Hotspot (sourceGUID wird absichtlich leer übergeben)
                EditModuleHelper.setHotSpotAndNextSequenceNumberELDAS(destPartListEntry, rowContent.getHotspot(),
                                                                      destPartList, finalSeqNr.getValue(),
                                                                      sourcePartListEntriesToTransferFiltered);

                // SAA Gültigkeit und Baumustergültigkeit bereits durch createRetailPartListEntry() gesetzt

                // Bei MBS eine K_SOURCE_GUID analog der aus der ELDAS Migration verwenden
                rowContent.setSourceGUID(EditConstructionToRetailHelper.createNonDIALOGSourceGUID(destPartListEntry.getAsId()));
                // SourceGUID des zu übernehmenden Eintrags bestimmen
                String sourceGUID = rowContent.getSourceGUIDForAttribute();
                destPartListEntry.setFieldValue(iPartsConst.FIELD_K_SOURCE_GUID, sourceGUID, DBActionOrigin.FROM_EDIT);

                // Array-ID in SAA_VALIDITY richtig setzen
                EtkDataArray saaValidity = destPartListEntry.getFieldValueAsArray(iPartsConst.FIELD_K_SA_VALIDITY);
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
                handleCombText(rowContent, destPartListEntry);
                EditModuleHelper.finishPartListEntryCreation(destPartListSourceGUIDMap, sourceGUID, destPartListEntry,
                                                             sourcePartListEntriesToTransferFiltered, finalSeqNr);
                if (maxPos < 100) {
                    fireMessage("!!Teileposition (%1 %2) erzeugt.", destPartListEntryId.getKVari(),
                                destPartListEntryId.getKLfdnr());
                }
            }
            // Jetzt die neuen Einträge zur Stückliste hinzufügen
            for (EtkDataPartListEntry partListEntry : sourcePartListEntriesToTransferFiltered) {
                destPartList.add(partListEntry, DBActionOrigin.FROM_EDIT);
            }
            if (saveToDB) {
                EditModuleHelper.finishModuleModificationEDS(dataObjectsToBeSaved, destPartList, assemblyIsNew,
                                                             destAssembly, null, null, true);
            } else {
                dataObjectsToBeSaved.clear(DBActionOrigin.FROM_DB);
                fireWarning("!!Speichern unterdrückt!");
            }
        } catch (Exception e) {
            fireError("!!Es ist ein Fehler aufgetreten: %1", e.getMessage());
            hasErrorsOrWarnings = true;
        } finally {
            getProject().stopPseudoTransactionForActiveChangeSet();
        }
        saveDictMetaData();
    }

    private void saveDictMetaData() {
        if (saveToDB && dictSearchHelper.getTotalDictMetaSavedCounter() > 0) {
            fireMessage("!!Speichere %1 neue/modifizierte Lexikoneinträge in einem technischen Änderungsset",
                        String.valueOf(dictSearchHelper.getTotalDictMetaSavedCounter()));
        }
        dictSearchHelper.saveCreatedDataDictMetaList(saveToDB);
    }

    private void addDictMetaData(iPartsDataDictMeta dataDictMeta, String tableDotFieldName) {
        if ((dataDictMeta != null) && (dataDictMeta.isNew() || dataDictMeta.isModifiedWithChildren())) {
            dictSearchHelper.updateCreatedDataDictMetaListWithoutSave(dataDictMeta, tableDotFieldName);
        }
    }

    public void addDictMetaData(iPartsDataDictMeta dataDictMeta, String tableDotFieldName, EtkMultiSprache importMultiLang) {
        if ((dataDictMeta != null) && (dataDictMeta.isNew() || dataDictMeta.isModifiedWithChildren())) {
            boolean isChanged = false;
            if ((importMultiLang != null) && !importMultiLang.isEmpty()) {
                EtkMultiSprache currentMultiLang = dataDictMeta.getMultiLang();
                for (Language lang : importMultiLang.getLanguages()) {
                    if (!currentMultiLang.containsLanguage(lang, true) && importMultiLang.containsLanguage(lang, true)) {
                        currentMultiLang.setText(lang, importMultiLang.getText(lang.getCode()));
                        isChanged = true;

                    }
                }
                if (isChanged) {
                    dataDictMeta.setNewMultiLang(currentMultiLang);
                }
            }
            dictSearchHelper.updateCreatedDataDictMetaListWithoutSave(dataDictMeta, tableDotFieldName, true);
        }
    }

    private void replaceKatalogFields(EtkDataPartListEntry destPartListEntry, iPartsModuleTypes moduleType,
                                      EtkDataPartListEntry sourcePartListEntry) {
        // einige Felder wurden von createRetailPartListEntry überschrieben, wenn TU kein PSK ist
        EditConstructionToRetailHelper.SourcePartListEntryRef sourcePartListEntryRef =
                new EditConstructionToRetailHelper.SourcePartListEntryRef(moduleType, sourcePartListEntry, true);
        for (Map.Entry<String, String> sourceToDestFieldMapping : sourcePartListEntryRef.getSourceToDestFieldsMapping().entrySet()) {
            String kFieldName = sourceToDestFieldMapping.getValue();
            destPartListEntry.setFieldValue(kFieldName, sourcePartListEntry.getFieldValue(kFieldName), DBActionOrigin.FROM_EDIT);
        }
    }

    private void handleCombText(TransferToASElement rowContent, EtkDataPartListEntry destPartListEntry) {
        if (rowContent instanceof ExtendedTransferToASElement) {
            ExtendedTransferToASElement transferElem = (ExtendedTransferToASElement)rowContent;
            DictImportTextIdHelper importHelper = new DictImportTextIdHelper(getProject());
            Map<String, EtkMultiSprache> multiList = new HashMap<>();
            // wegen der Reihenfolge
            partListStdMapping.forEach((fieldName, stdName) -> {
                String value = transferElem.getOilFieldsMap().get(fieldName);
                if (StrUtils.isEmpty(value)) {
                    value = "";
                }
                if (stdName.equals(STD_ADDTEXT)) {
                    if (StrUtils.isValid(value)) {
                        EtkMultiSprache addMultiText = handleAddText(importHelper, value);
                        if (addMultiText != null) {
                            multiList.put(stdName, addMultiText);
                        }
                    }
                } else if (stdName.equals(STD_PACKSIZE) || stdName.equals(STD_VISCOSITY) || stdName.equals(STD_NEUTRALTEXT)) {
                    if (StrUtils.isValid(value)) {
                        EtkMultiSprache neutralText = handleNeutralText(importHelper, value);
                        if (neutralText != null) {
                            multiList.put(stdName, neutralText);
                        }
                    }
                } else {
                    destPartListEntry.setFieldValue(fieldName, value, DBActionOrigin.FROM_EDIT);
                }
            });
            if (!multiList.isEmpty()) {
                int localCurrentCombinedTextSeqNo = 1;
                // über die Reihenfolge in NO_PSK_FIELDS wird die Reihenfolge der NeutralTexte im CombText festgelegt
                for (String stdName : NO_PSK_FIELDS) {
                    if (stdName.equals(STD_ADDTEXT) || stdName.equals(STD_PACKSIZE)
                        || stdName.equals(STD_VISCOSITY) || stdName.equals(STD_NEUTRALTEXT)) {
                        EtkMultiSprache multiText = multiList.get(stdName);
                        if (multiText != null) {
                            iPartsDataCombText dataCombText = new iPartsDataCombText(getProject(), destPartListEntry.getAsId(),
                                                                                     multiText, null,
                                                                                     localCurrentCombinedTextSeqNo);
                            dataObjectsToBeSaved.add(dataCombText, DBActionOrigin.FROM_EDIT);
                            localCurrentCombinedTextSeqNo++;
                        }
                    }
                }
            }
        }
    }

    private EtkMultiSprache handleNeutralText(DictImportTextIdHelper importHelper, String value) {
        EtkMultiSprache neutralText = null;
        if (StrUtils.isValid(value)) {
            // Lexikonsuche
            neutralText = new EtkMultiSprache();
            neutralText.setText(Language.DE, value);
            VarParam<iPartsDataDictMeta> foundDictMeta = new VarParam<>(null);
            boolean dictSuccessful = importHelper.handleNeutralTextIdForCombText(neutralText, dataOrigin, true,
                                                                                 false, foundDictMeta);
            if (!dictSuccessful || importHelper.hasWarnings()) {
                //Fehler beim Dictionary Eintrag
                for (String str : importHelper.getWarnings()) {
                    fireWarning("!!Benennung wegen \"%1\" übersprungen", str);
                }
                neutralText = null;
            } else {
                // und speichern mit anderem tableAndFieldName
                addDictMetaData(foundDictMeta.getValue(), TableAndFieldName.make(iPartsConst.TABLE_DA_COMB_TEXT, iPartsConst.FIELD_DCT_DICT_TEXT));
            }
        }
        return neutralText;
    }

    private EtkMultiSprache handleAddText(DictImportTextIdHelper importHelper, String value) {
        EtkMultiSprache addMultiText = null;
        if (StrUtils.isValid(value)) {
            addMultiText = addTextMap.get(value);
            if (addMultiText != null) {
                return addMultiText;
            }
            // Lexikonsuche
            addMultiText = new EtkMultiSprache();
            addMultiText.setText(Language.DE, value);
            VarParam<iPartsDataDictMeta> foundDictMeta = new VarParam<>(null);
            boolean dictSuccessful = importHelper.handleTextIdForCombText(addMultiText, dataOrigin, true,
                                                                          false, foundDictMeta);
            if (!dictSuccessful || importHelper.hasWarnings()) {
                //Fehler beim Dictionary Eintrag
                for (String str : importHelper.getWarnings()) {
                    fireWarning("!!Benennung wegen \"%1\" übersprungen", str);
                }
                addMultiText = null;
            } else {
                addTextMap.put(value, addMultiText);
                addDictMetaData(foundDictMeta.getValue(), TableAndFieldName.make(iPartsConst.TABLE_DA_COMB_TEXT, iPartsConst.FIELD_DCT_DICT_TEXT));
            }
        }
        return addMultiText;


    }

    private DBDataObjectList<EtkDataPartListEntry> getTuPartListEntries() {
        return EditModuleHelper.getDestPartList(destAssembly, false);
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
        label.setText("!!PSK Datei");
        importFileChooser.setTooltip(label);
        importFileChooser.setMultiSelectionMode(false);
        importFileChooser.setApproveButtonText("!!PSK-Datei auswählen");
        List<String[]> fileFilters = new ArrayList<>();
        fileFilters.add(new String[]{ TranslationHandler.translate(DWFileFilterEnum.XLSFILES.getDescription()),
                                      DWFileFilterEnum.XLSFILES.getExtensions() });
        fileFilters.add(new String[]{ TranslationHandler.translate(DWFileFilterEnum.ALLFILES.getDescription()),
                                      DWFileFilterEnum.ALLFILES.getExtensions() });
        importFileChooser.setChoosableFileFilters(fileFilters);
        importFileChooser.setActiveFileFilter(DWFileFilterEnum.XLSFILES.getDescription());
        return importFileChooser;
    }

    /**
     * Erzeugte MessageLogForm anpassen
     *
     * @return
     */
    @Override
    protected EtkMessageLogForm createMessageLogForm() {
        EtkMessageLogForm msgLogForm = super.createMessageLogForm();
        msgLogForm.setTitle("!!PSK Datei importieren");
        return msgLogForm;
    }

    public iPartsDataAssembly getDestAssembly() {
        return destAssembly;
    }

    public iPartsProduct getProduct() {
        return product;
    }

    public KgTuId getKgTuId() {
        return kgTuId;
    }

    public Map<String, String> getPartListStdMapping() {
        return partListStdMapping;
    }

    public boolean isRealPSK() {
        return isRealPSK;
    }

    public boolean isTruckImport() {
        return isTruckImport;
    }

    public iPartsImportDataOrigin getDataOrigin() {
        return dataOrigin;
    }

    public void addToTransferList(TransferToASElement transferElement) {
        if (transferElement != null) {
            transferList.add(transferElement);
        }
    }

    public boolean containsNaviCarModule(String moduleNo) {
        return naviCarModuleNoSet.contains(moduleNo);
    }
}
