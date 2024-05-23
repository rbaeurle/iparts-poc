/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.psk_partlist;

import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.EtkDataPartListEntry;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsLanguage;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictImportTextIdHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.file.DWFile;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Klasse für PSK-Import einer Excel-Datei
 */
class PSKReader extends AbstractPskReader {

    private static final List<Language> VALID_IMPORT_LANGUAGES = new DwList<>();

    static {
        VALID_IMPORT_LANGUAGES.add(Language.DE);
        VALID_IMPORT_LANGUAGES.add(Language.EN);
        VALID_IMPORT_LANGUAGES.add(Language.FR);
    }

    private final EditImportPSKForm editImportPSKForm;
    private List<String> newMatNos;
    private Map<String, EtkMultiSprache> matNameMap;
    private EtkMultiSprache importMultiLang;
    private iPartsNumberHelper numberHelper;
    private boolean isRealPSK;
    private boolean isOilImport;
    private Map<String, String> partMapping;

    public PSKReader(EditImportPSKForm editImportPSKForm) {
        this.editImportPSKForm = editImportPSKForm;
        init();
    }

    private void init() {
        numberHelper = new iPartsNumberHelper();
        isRealPSK = editImportPSKForm.isRealPSK();
        partMapping = new HashMap<>();
        partMapping.put(iPartsConst.FIELD_M_PSK_SUPPLIER_NO, EditImportPSKForm.PSK_MANUFACTURER_CODE_1);
        partMapping.put(iPartsConst.FIELD_M_PSK_MANUFACTURER_NO, EditImportPSKForm.PSK_MANUFACTURER_CODE_2);
        partMapping.put(iPartsConst.FIELD_M_PSK_SUPPLIER_MATNR, EditImportPSKForm.PSK_SNR_SUPPLIER_MATNO);
        partMapping.put(iPartsConst.FIELD_M_PSK_MANUFACTURER_MATNR, EditImportPSKForm.PSK_SNR_MANUFACTURER_MATNO);
        partMapping.put(iPartsConst.FIELD_M_PSK_IMAGE_NO_EXTERN, EditImportPSKForm.PSK_IMAGE_NO_EXTERN);
        partMapping.put(iPartsConst.FIELD_M_PSK_REMARK, EditImportPSKForm.PSK_REMARK);
    }

    @Override
    protected boolean open(DWFile file) {
        newMatNos = new DwList<>();
        matNameMap = new HashMap<>();
        return super.open(file);
    }

    @Override
    protected boolean checkHeader(Map<String, Integer> headerNameToIndex) {
        boolean isHeaderValid = super.checkHeader(headerNameToIndex);
        if (isHeaderValid) {
            this.isOilImport = false;
            for (String recName : EditImportPSKForm.NO_PSK_FIELDS) {
                Integer headerIndex = headerNameToIndex.get(recName);
                if ((headerIndex != null) && (headerIndex != -1)) {
                    this.isOilImport = true;
                    break;
                }
            }
            if (!this.isRealPSK && !this.isOilImport) {
                showWarning("!!Es wird eine PSK-Datei in einen nicht PSK-TU importiert!");
            }
        }
        return isHeaderValid;
    }

    public List<String> getNewMatCount() {
        return newMatNos;
    }

    /**
     * eine Zeile aus der PSK-Excel Datei aufbereiten
     *
     * @param importRec
     * @param recordNo
     */
    @Override
    protected void importRecord(Map<String, String> importRec, int recordNo) {
        EtkDataPart dataPart = createPartFromContainer(importRec, recordNo);
        if (dataPart != null) {
            if (!editImportPSKForm.containsDataPart(dataPart.getAsId())) {
                editImportPSKForm.addDataPart(dataPart);
                editImportPSKForm.getDataObjectsToBeSaved().add(dataPart, DBActionOrigin.FROM_EDIT);
            }

            EtkDataPartListEntry selectedPartlistEntry = buildDummySelectedPartListEntry(importRec);

            String hotspot = handleValueOfSpecialField(EditImportPSKForm.PSK_POS, importRec);

            // iPartsConstructionPrimaryKey wird für PSK nicht benötigt und deshalb hier mit null übergeben
            ExtendedTransferToASElement transferElement = new ExtendedTransferToASElement(editImportPSKForm.getDestAssembly().getAsId(),
                                                                                          editImportPSKForm.getKgTuId(), hotspot, editImportPSKForm.getProduct(),
                                                                                          selectedPartlistEntry);
            setStdOilKatFields(transferElement, importRec, recordNo);
            editImportPSKForm.addToTransferList(transferElement);
        }
    }

    private void setStdOilKatFields(ExtendedTransferToASElement transferElement, Map<String, String> importRec,
                                    int recordNo) {
        editImportPSKForm.getPartListStdMapping().forEach((fieldName, stdName) -> {
            if (importRec.get(stdName) != null) {
                if (stdName.equals(EditImportPSKForm.STD_COUNTRIES)) {
                    String value = handleValueOfSpecialField(stdName, importRec);
                    value = buildValidCountryCodes(value, recordNo);
                    transferElement.addOilFieldMapping(fieldName, value);
                } else {
                    String value = handleValueOfSpecialField(stdName, importRec);
                    transferElement.addOilFieldMapping(fieldName, value);
                }
            }
        });
    }

    public String buildValidCountryCodes(String value, int recordNo) {
        if (StrUtils.isEmpty(value)) {
            return value;
        }
        List<String> invalidCountryCodes = new DwList<>();
        List<String> countryCodes = StrUtils.toStringList(value, EditImportPSKForm.COUNTRY_DELIMITER, false, true);
        Iterator<String> iter = countryCodes.iterator();
        while (iter.hasNext()) {
            String countryCode = iter.next();
            if (!iPartsLanguage.isValidDaimlerIsoCountryCode(editImportPSKForm.getProject(), countryCode)) {
                invalidCountryCodes.add(countryCode);
                iter.remove();
            }
        }
        if (!invalidCountryCodes.isEmpty()) {
            showWarning("!!Record %1: Ungültige Länderkürzel \"%2\"! (werden ignoriert)",
                        String.valueOf(recordNo), StrUtils.stringListToString(invalidCountryCodes, iPartsConst.COUNTRY_SPEC_DB_DELIMITER));
        }
        return StrUtils.stringListToString(countryCodes, iPartsConst.COUNTRY_SPEC_DB_DELIMITER);
    }

    private EtkDataPart createPartFromContainer(Map<String, String> importRec, int recordNo) {
        EtkDataPart dataPart = getDataPartFromImport(importRec);
        if (dataPart != null) {
            if (!editImportPSKForm.containsDataPart(dataPart.getAsId())) {
                boolean existsInDB = dataPart.existsInDB();
                if (!existsInDB) {
                    String aSachNo = dataPart.getAsId().getMatNr();
                    // Neues PSK-Material anlegen
                    dataPart.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    dataPart.setFieldValue(iPartsConst.FIELD_M_BESTNR, aSachNo, DBActionOrigin.FROM_EDIT);
                    dataPart.setFieldValueAsBoolean(iPartsConst.FIELD_M_BESTFLAG, true, DBActionOrigin.FROM_EDIT);
                    dataPart.setFieldValueAsBoolean(iPartsConst.FIELD_M_PSK_MATERIAL, isRealPSK, DBActionOrigin.FROM_EDIT);

                    if (isASachNo(aSachNo)) {
                        String es1 = getEs1FromPartNo(aSachNo);
                        String es2 = getEs2FromPartNo(aSachNo);
                        if (StrUtils.isValid(es1) || StrUtils.isValid(es2)) {
                            dataPart.setFieldValue(iPartsConst.FIELD_M_BASE_MATNR, getBasePartNo(aSachNo),
                                                   DBActionOrigin.FROM_EDIT);
                            dataPart.setFieldValue(iPartsConst.FIELD_M_AS_ES_1, es1, DBActionOrigin.FROM_EDIT);
                            dataPart.setFieldValue(iPartsConst.FIELD_M_AS_ES_2, es2, DBActionOrigin.FROM_EDIT);
                        }
                    }
                    newMatNos.add(aSachNo);
                }
                // Text setzen
                handleMatText(dataPart, existsInDB, importRec);
                // Source ggf hinzufügen
                String enumValue = editImportPSKForm.getDataOrigin().getOrigin();
                dataPart.addSetOfEnumValueToFieldValue(iPartsConst.FIELD_M_SOURCE, enumValue, DBActionOrigin.FROM_EDIT);
                // relevante Felder setzen
                if (isRealPSK) {
                    setPskMatFields(dataPart, existsInDB, importRec);
                }
            }
            return dataPart;
        }
        // Fehlermeldung
        String aSachNo = importRec.get(EditImportPSKForm.PSK_PARTNO);
        if (StrUtils.isEmpty(aSachNo)) {
            editImportPSKForm.fireMessage("!!In Zeile %1: keine gültige Sachnummer vorhanden. Wird ignoriert",
                                          String.valueOf(recordNo));
        } else {
            if (!isRealPSK) {
                if (!editImportPSKForm.isTruckImport()) {
                    editImportPSKForm.fireWarning("!!In Zeile %1: keine gültige A-/N-Sachnummer \"%2\". Wird ignoriert",
                                                  String.valueOf(recordNo), aSachNo);
                } else {
                    editImportPSKForm.fireWarning("!!In Zeile %1: keine gültige Sachnummer \"%2\". Wird ignoriert",
                                                  String.valueOf(recordNo), aSachNo);
                }
            } else {
                editImportPSKForm.fireMessage("!!In Zeile %1: keine gültige Sachnummer \"%2\". Wird ignoriert",
                                              String.valueOf(recordNo), aSachNo);
            }
        }
        return null;
    }

    private EtkDataPart getDataPartFromImport(Map<String, String> importRec) {
        String aSachNo = handleValueOfSpecialField(EditImportPSKForm.PSK_PARTNO, importRec);
        if (!StrUtils.isEmpty(aSachNo)) {
            PartId partId = new PartId(aSachNo, "");
            return EtkDataObjectFactory.createDataPart(editImportPSKForm.getProject(), partId);
        }
        return null;
    }

    private String getImportPSKName(Language lang) {
        switch (lang) {
            case DE:
                return EditImportPSKForm.PSK_NAME_DE;
            case EN:
                return EditImportPSKForm.PSK_NAME_EN;
            case FR:
                return EditImportPSKForm.PSK_NAME_FR;
            default:
                return EditImportPSKForm.PSK_NAME_DE;
        }
    }

    private EtkMultiSprache setImportMultiLang(Map<String, String> importRec) {
        EtkMultiSprache multiLang = new EtkMultiSprache();
        for (Language lang : VALID_IMPORT_LANGUAGES) {
            if (importRec.get(getImportPSKName(lang)) != null) {
                String importText = handleValueOfSpecialField(getImportPSKName(lang), importRec);
                if (StrUtils.isValid(importText)) {
                    multiLang.setText(lang, importText);
                }
            }
        }
        return multiLang;
    }

    private EtkMultiSprache handleMatText(EtkDataPart dataPart, boolean existsInDB, Map<String, String> importRec) {
        importMultiLang = setImportMultiLang(importRec);
        Set<Language> usedLanguages = importMultiLang.getLanguages();
        if (!usedLanguages.isEmpty()) {
            for (Language lang : usedLanguages) {
                EtkMultiSprache multiLang = handleMatText(dataPart, existsInDB, importMultiLang.getText(lang.getCode()), lang);
                if (multiLang != null) {
                    return multiLang;
                }
            }
        } else {
            return handleMatText(dataPart, existsInDB, "", Language.DE);
        }
        return null;
    }

    private EtkMultiSprache handleMatText(EtkDataPart dataPart, boolean existsInDB, String matText, Language lang) {
        // Text setzen
        // M_TEXTNR immer setzen für die Importer
        EtkMultiSprache multiLang;
        if (!existsInDB) {
            // MAT existiert nicht => Suche in Lexikon nach Benennung, ggf anlegen
            multiLang = searchOrCreateDictionaryText(dataPart, matText, lang);
        } else {
            // MAT existiert
            if (StrUtils.isValid(matText)) {
                // Part existiert in DB - erst mal die Benennung aus DB holen
                EtkMultiSprache dbMultiLang = dataPart.getFieldValueAsMultiLanguageIncomplete(iPartsConst.FIELD_M_TEXTNR, true);
                if (StrUtils.isEmpty(dbMultiLang.getTextId())) {
                    // Text aus der DB besitzt keine TextId => Suche in Lexikon + Neuanlage
                    multiLang = searchOrCreateDictionaryText(dataPart, matText, lang);
                } else {
                    // Text aus DB besitzt TextId => Benennung aus DB gewinnt
                    // Warnung ausgeben, falls die Texte unterschiedlich sind
                    if (!dbMultiLang.getText(lang.getCode()).equals(matText)) {
                        editImportPSKForm.fireWarning("!!Material \"%1\" existiert bereits. Benennung \"%2\" ist unterschiedlich!",
                                                      dataPart.getAsId().getMatNr(), dbMultiLang.getText(lang.getCode()));
                    }
                    multiLang = dbMultiLang.cloneMe();
                }
            } else {
                // es bleibt alles wie es ist
                multiLang = dataPart.getFieldValueAsMultiLanguageIncomplete(iPartsConst.FIELD_M_TEXTNR, true);
            }
        }
        if (multiLang != null) {
            // wenn Material keine Benennung hat und jetzt kommt eine Benennung => übernehmen
            if (StrUtils.isValid(matText) && StrUtils.isEmpty(multiLang.getText(lang.getCode()))) {
                multiLang.setText(lang, matText);
                editImportPSKForm.setMatTextHasChanged(true);
            }
            dataPart.setFieldValueAsMultiLanguage(iPartsConst.FIELD_M_TEXTNR, multiLang, DBActionOrigin.FROM_EDIT);
        }
        return multiLang;
    }

    /**
     * Suche nach {@param matText} im Lexikon, falls valid
     * Wurde matText gefunden, Übernahme der multiLanguage aus dataDictMeta
     * Existiert noch kein Lexikon-Eintrag, dann diesen anlegen
     *
     * @param dataPart
     * @param matText
     * @return
     */
    private EtkMultiSprache searchOrCreateDictionaryText(EtkDataPart dataPart, String matText, Language lang) {
        EtkMultiSprache multiLang = null;
        if (StrUtils.isValid(matText)) {
            // gültiger Import-Text => gab es den schon mal?
            multiLang = matNameMap.get(matText);
            if (multiLang == null) {
                // hier Lexikonsuche
                multiLang = new EtkMultiSprache();
                multiLang.setText(lang, matText);

                DictImportTextIdHelper importHelper = new DictImportTextIdHelper(editImportPSKForm.getProject());
                // bei PSK: Suche nur mit Fremdquelle PSK
                String foreignSource;
                if (isRealPSK) {
                    foreignSource = DictHelper.getPSKForeignSource();
                } else {
                    foreignSource = DictHelper.getMADForeignSource();
                }
                iPartsDataDictMeta dataDictMeta = importHelper.handleDictTextIdWithoutSave(DictTextKindTypes.MAT_NAME, multiLang,
                                                                                           foreignSource, true,
                                                                                           TableAndFieldName.make(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR),
                                                                                           lang);
                if (importHelper.hasWarnings()) {
                    // multiLang so lassen wie es ist
                    //Fehler beim Dictionary Eintrag
                    for (String str : importHelper.getWarnings()) {
                        // Anzeige im LogFenster wurde ausgeschaltet, da sonst zu viele Ausgaben
                        editImportPSKForm.fireWarning("!!Teilenummer %1 bei Lexikonablage: \"%2\"", dataPart.getAsId().getMatNr(), str);
                    }
                } else {
                    if (dataDictMeta == null) {
                        editImportPSKForm.fireWarning("!!Fehler bei Lexikonablage bei Teilenummer \"%1\"", dataPart.getAsId().getMatNr());
                        return null;
                    } else {
                        // dataDictMeta in genericList speichern
                        editImportPSKForm.addDictMetaData(dataDictMeta, null, importMultiLang);
                        multiLang = dataDictMeta.getMultiLang();
                        editImportPSKForm.setMatTextHasChanged(true);
                        matNameMap.put(matText, multiLang);
                        importMultiLang = null;
                    }
                }
            }
        }
        return multiLang;
    }

    private void setPskMatFields(EtkDataPart dataPart, boolean existsInDB, Map<String, String> importRec) {
        if (!existsInDB) {
            partMapping.forEach((fieldName, pskName) -> dataPart.setFieldValue(fieldName,
                                                                               handleValueOfSpecialField(pskName, importRec),
                                                                               DBActionOrigin.FROM_EDIT));
        } else {
            partMapping.forEach((fieldName, pskName) -> {
                String currentValue = handleValueOfSpecialField(pskName, importRec);
                if (StrUtils.isValid(currentValue)) {
                    if (!currentValue.equals(dataPart.getFieldValue(fieldName))) {
                        dataPart.setFieldValue(fieldName, currentValue, DBActionOrigin.FROM_EDIT);
                    }
                }
            });
        }
    }

    protected String handleValueOfSpecialField(String sourceField, Map<String, String> importRec) {
        return handleValueOfSpecialField(sourceField, importRec.get(sourceField));
    }

    protected String handleValueOfSpecialField(String sourceField, String value) {
        if (value == null) {
            value = "";
        }
        if (sourceField.equals(EditImportPSKForm.PSK_POS)) {
            if (!isAlphaNumeric(value)) {
                value = "";
            }
        } else if (sourceField.equals(EditImportPSKForm.PSK_STRUKTURSTUFE)) {
            if (!StrUtils.isValid(value) || !StrUtils.isInteger(value)) {
                value = "1";
            }
        } else if (sourceField.equals(EditImportPSKForm.PSK_PARTNO)) {
            value = convertPartNumber(value);
        }
        return value;
    }

    private String convertPartNumber(String value) {
        boolean doCheck = !isRealPSK || isOilImport;
        if (doCheck) {
            // kein PSK-TU und kein Excel-Import bei PSK-TU => check Sachnummer
            if (StrUtils.isValid(value)) {
                if (hasValidSyntax(value)) {
                    if (!editImportPSKForm.isTruckImport()) {
                        // bei Import in PKW-TU sind nur A-/N-Sachnummern erlaubt
                        if (isASachNo(value)) {
                            if (numberHelper.isValidASachNoWithES(editImportPSKForm.getProject(), value)) {
                                // gültige A-Sachnummer
                                value = value.toUpperCase();
                                String basePartNo = getBasePartNo(value);
                                String es1 = getEs1FromPartNo(value);
                                String es2 = getEs2FromPartNo(value);
                                return numberHelper.getPRIMUSPartNoWithEs1AndEs2(basePartNo, es1, es2);
                            } else {
                                // ungültige A-SachNo
                                value = "";
                            }
                        } else if (numberHelper.isValidNSachNo(editImportPSKForm.getProject(), value)) {
                            return numberHelper.unformatASachNoForDB(editImportPSKForm.getProject(), value);
                        } else {
                            // ungültige SachNo
                            value = "";
                        }
                    }
                } else {
                    // ungültige SachNo
                    value = "";
                }
            }
        }
        return value;
    }

    public boolean hasValidSyntax(String value) {
        // DAIMLER-15282: spezielle Prüfung für Truck (beginnt mit Großbuchstabe, alle anderen Zeichen sind Digits)
        if (value.length() > 1) {
            char firstChar = value.charAt(0);
            if (Character.isLetter(firstChar) && Character.isUpperCase(firstChar) && StrUtils.isDigit(value.substring(1))) {
                return true;
            }
        }
        return false;
    }

    public boolean isASachNo(String number) {
        return number.startsWith("A") || number.startsWith("a");
    }

    public String getBasePartNo(String partNo) {
        return numberHelper.getBasePartNoFromDialogInputPartNo(partNo);
    }

    public String getEs1FromPartNo(String partNo) {
        return numberHelper.getES1FromDialogInputPartNo(partNo);
    }

    public String getEs2FromPartNo(String partNo) {
        return numberHelper.getES2FromDialogInputPartNo(partNo);
    }

    private EtkDataPartListEntry buildDummySelectedPartListEntry(Map<String, String> importRec) {
        String matNo = handleValueOfSpecialField(EditImportPSKForm.PSK_PARTNO, importRec);
        EtkDataPartListEntry dummyPartListEntry
                = EtkDataObjectFactory.createDataPartListEntry(editImportPSKForm.getProject(), new PartListEntryId("", "", ""));
        dummyPartListEntry.initAttributesWithEmptyValues(DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        // relevante Fields hinzufügen
        DBDataObjectAttributes attributes = dummyPartListEntry.getAttributes();
        attributes.addField(iPartsConst.FIELD_K_MENGE, handleValueOfSpecialField(EditImportPSKForm.PSK_MENGE_ART, importRec),
                            DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        attributes.addField(iPartsConst.FIELD_K_MATNR, matNo, false,
                            DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        // MBS: Strukturstufe wird mit DAIMLER-12456 von Datei gelesen bzw auf 1 gesetzt.
        attributes.addField(iPartsConst.FIELD_K_HIERARCHY, handleValueOfSpecialField(EditImportPSKForm.PSK_STRUKTURSTUFE, importRec),
                            DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);

        boolean partIsNew = false;
        PartId partId = new PartId(matNo, "");
        EtkDataPart dataPart = editImportPSKForm.getNewDataParts().get(partId);
        if (dataPart != null) {
            partIsNew = true;
        } else {
            dataPart = dummyPartListEntry.getPart();
            if (!dataPart.existsInDB()) {
                dataPart = null;
            }
        }
        if (dataPart != null) {
            if (!partIsNew && dataPart.isModified()) { // Neue Materialien werden sowieso schon gespeichert
                editImportPSKForm.getDataObjectsToBeSaved().add(dataPart, DBActionOrigin.FROM_EDIT);
            }
        }
        return dummyPartListEntry;
    }

    @Override
    protected void showMessage(String key, String... placeHolderTexts) {
        editImportPSKForm.fireMessage(key, placeHolderTexts);
    }

    @Override
    protected void showWarning(String key, String... placeHolderTexts) {
        editImportPSKForm.fireWarning(key, placeHolderTexts);
    }

    @Override
    protected void showError(String key, String... placeHolderTexts) {
        editImportPSKForm.fireError(key, placeHolderTexts);
    }

    @Override
    protected void doProgress(int pos, int maxPos) {
        editImportPSKForm.fireProgress(pos, maxPos);
    }

    @Override
    protected void doHideProgress() {
        editImportPSKForm.hideProgress();
    }
}
