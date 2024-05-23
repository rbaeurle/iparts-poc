/*
 * Copyright (c) 2020 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary;

import de.docware.apps.etk.base.config.db.EtkDbConst;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDbObjectsLayer;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.sql.TableAndFieldName;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;

/**
 * Erweiterung von {@link EtkDataObject} um iParts-spezifische Methoden und Daten für Tabelle DA_DICT_META.
 */
public class iPartsDataDictMeta extends EtkDataObject implements EtkDbConst, iPartsConst {

    static private final String[] KEYS = new String[]{ FIELD_DA_DICT_META_TXTKIND_ID, FIELD_DA_DICT_META_TEXTID };

    public static final String CHILDREN_NAME_LANGUAGES = "iPartsDataDictMeta.languages";

    protected EtkMultiSprache multiLang;
    protected iPartsDataDictLanguageMetaList languagesList;
    private boolean saveAllUsages;

    public iPartsDataDictMeta(EtkProject project, iPartsDictMetaId id) {
        super(KEYS);
        tableName = TABLE_DA_DICT_META;
        if (project != null) {
            init(project);
        }
        setId(id, DBActionOrigin.FROM_DB);
        multiLang = null;
        saveAllUsages = false;
    }

    public void setSaveAllUsages(boolean value) {
        saveAllUsages = value;
    }

    @Override
    public boolean saveToDB(boolean checkIfPKExistsInDB, PrimaryKeyExistsInDB forcePKExistsInDB) {
        boolean wasNew = isNew();
        boolean saved = false;
        getEtkProject().getDB().startBatchStatement();
        try {
            saved = super.saveToDB(checkIfPKExistsInDB, forcePKExistsInDB);
            if ((multiLang != null) && !multiLang.allStringsAreEmpty()) {
                EtkDbObjectsLayer dbLayer = getEtkProject().getDbLayer();
                if (!wasNew) {
                    saved |= getEtkProject().getDbLayer().updateLanguageTextsWithTextId(getTextId(), multiLang, null);
                } else {
                    boolean usagesAreSaved = false;
                    if (saveAllUsages) {
                        iPartsDataDictTextKindUsageList usages = DictTxtKindIdByMADId.getInstance(getEtkProject()).getUsagesByTextKindId(new iPartsDictTextKindId(getAsId().getTextKindId()),
                                                                                                                                         getEtkProject());
                        if (usages != null) {
                            usagesAreSaved = true;
                            for (iPartsDataDictTextKindUsage textKindUsage : usages) {
                                String tableDotFieldName = textKindUsage.getFeld();
                                if (StrUtils.isEmpty(tableDotFieldName)) {
                                    tableDotFieldName = TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR);
                                }
                                dbLayer.saveLanguageEntry(tableDotFieldName, multiLang.getTextId(), "", multiLang, null);
                            }
                        }
                    }
                    if (!usagesAreSaved) {
                        // Die erste echte Verwendung der TextId für ein Feld aus DA_DICT_TXTKIND_USAGE laden, um keinen unnötigen
                        // Eintrag für MAT.M_TEXTNR zu erzeugen (das ist nur der Fallback, wenn es keine echte Verwendung gibt)
                        String tableDotFieldName = DictTxtKindIdByMADId.getInstance(getEtkProject()).findTableAndFieldNameByTextKindId(new iPartsDictTextKindId(getAsId().getTextKindId()),
                                                                                                                                       getEtkProject());
                        if ((tableDotFieldName == null) || tableDotFieldName.isEmpty()) {
                            tableDotFieldName = TableAndFieldName.make(TABLE_MAT, FIELD_M_TEXTNR);
                        }
                        dbLayer.saveLanguageEntry(tableDotFieldName, multiLang.getTextId(), "", multiLang, null);
                    }
                    saved = true;
                }
            }
        } finally {
            getEtkProject().getDB().endBatchStatement();
        }
        return saved;
    }

    @Override
    public void deleteFromDB(boolean forceDelete) {
        if (forceDelete || !isNew()) { // ein neuer Datensatz muss keine Kindelemente aus der DB laden
            if (iPartsDataDictMetaList.isMetaSpracheUsedInDictMeta(getEtkProject(), getAsId()) /*!usedList.isEmpty()*/) {
                // es gibt noch weitere Verwendungen der DA_DICT_SPRACHE-Elemente => die Einträge nicht löschen
                resetLanguages();
            } else {
                getLanguages();
            }
        }
        super.deleteFromDB(forceDelete);
    }

    @Override
    public void setChildren(String childrenName, DBDataObjectList<? extends DBDataObject> children) {
        super.setChildren(childrenName, children);
        if (childrenName.equals(CHILDREN_NAME_LANGUAGES)) {
            languagesList = (iPartsDataDictLanguageMetaList)children;
        }
    }

    @Override
    public iPartsDataDictMeta cloneMe(EtkProject project) {
        iPartsDataDictMeta clone = new iPartsDataDictMeta(project, getAsId());
        clone.assignRecursively(project, this, DBActionOrigin.FROM_DB);
        return clone;
    }

    @Override
    public iPartsDictMetaId createId(String... idValues) {
        return new iPartsDictMetaId(idValues[0], idValues[1]);
    }

    @Override
    public iPartsDictMetaId getAsId() {
        if (id == null) {
            setEmptyId(DBActionOrigin.FROM_DB);
        }

        return (iPartsDictMetaId)id;
    }

    @Override
    public void clear(DBActionOrigin origin) {
        super.clear(origin);
        setChildren(CHILDREN_NAME_LANGUAGES, null);
        multiLang = null;
    }

    protected synchronized void loadLanguages() {
        if (languagesList != null) {
            return;
        }

        setChildren(CHILDREN_NAME_LANGUAGES, iPartsDataDictLanguageMetaList.loadLanguageMetaList(getEtkProject(), getAsId().getTextId()));
    }

    public void prepareLanguagesForDelete() {
        loadLanguages();
        // es gibt noch weitere Verwendungen der DA_DICT_SPRACHE-Elemente => die Einträge nicht löschen
        if (iPartsDataDictMetaList.isMetaSpracheUsedInDictMeta(getEtkProject(), getAsId()) /*!usedList.isEmpty()*/) {
            resetLanguages();
        }
    }

    private void resetLanguages() {
        languagesList = null;
        setChildren(CHILDREN_NAME_LANGUAGES, languagesList);
    }

    public iPartsDataDictLanguageMetaList getLanguages() {
        loadLanguages();
        return languagesList;
    }

    public synchronized iPartsDataDictLanguageMeta addLanguage(String language, DBActionOrigin origin) {
        iPartsDataDictLanguageMeta dataDictLanguageMeta = getLanguages().findLanguage(language);
        if (dataDictLanguageMeta == null) {
            iPartsDictLanguageMetaId langMetaId = new iPartsDictLanguageMetaId(getAsId().getTextId(), language);
            dataDictLanguageMeta = new iPartsDataDictLanguageMeta(getEtkProject(), langMetaId);
            if (!dataDictLanguageMeta.loadFromDB(langMetaId)) {
                dataDictLanguageMeta.initAttributesWithEmptyValues(origin);
                updateDictLanguageMeta(dataDictLanguageMeta, true, origin);
            }
            getLanguages().add(dataDictLanguageMeta, origin);
        }
        return dataDictLanguageMeta;
    }

    public synchronized void addLanguageFromDB(iPartsDataDictLanguageMeta language) {
        if (languagesList == null) {
            setChildren(CHILDREN_NAME_LANGUAGES, new iPartsDataDictLanguageMetaList());
        }
        if (getLanguages().findLanguage(language.getAsId().getLanguage()) == null) {
            getLanguages().add(language, DBActionOrigin.FROM_DB);
        }
    }

    public void updateDictLanguageMeta(iPartsDataDictLanguageMeta dataDictLanguageMeta, boolean isForCreate, DBActionOrigin origin) {
        if (isForCreate) {
            dataDictLanguageMeta.setActCreationDate(origin);
        } else {
            dataDictLanguageMeta.setActChangeDate(origin);
        }
        dataDictLanguageMeta.setState(getState(), origin);
    }

    public void updateDictLanguageMeta(iPartsDataDictLanguageMeta dataDictLanguageMeta) {
        updateDictLanguageMeta(dataDictLanguageMeta, false, DBActionOrigin.FROM_EDIT);
    }

    public EtkMultiSprache getMultiLang() {
        if (multiLang == null) {
            if (isNew()) {
                multiLang = new EtkMultiSprache();
            } else {
                multiLang = getEtkProject().getDbLayer().getLanguagesTextsByTextId(getTextId());
                if (multiLang == null) {
                    multiLang = new EtkMultiSprache();
                }
            }
        }
        return multiLang.cloneMe();
    }

    public void clearMultiLang() {
        multiLang = null;
    }

    public void setNewMultiLang(EtkMultiSprache newMultiLang) {
        setNewMultiLang(newMultiLang, false);
    }

    public void setNewMultiLangForNeutText(EtkMultiSprache newMultiLang) {
        setNewMultiLang(newMultiLang, true);
    }

    protected void setNewMultiLang(EtkMultiSprache newMultiLang, boolean isNeutText) {
        checkDictLanguagesForMultiLang(newMultiLang);
        //multiLang übernehmen
        //leere Texte bei neuem MultiLang entfernen
        newMultiLang.removeLanguagesWithEmptyTexts();
        //die besetzten Sprachen des 'alten' MultiLangs holen
        Set<String> oldSprachen = getMultiLang().getSprachen();
        //erstmal übernehmen
        multiLang.assign(newMultiLang);
        Set<String> newLanguages = newMultiLang.getSprachen();

        if (!isNeutText) {
            //gelöschte Texte, die vorher definiert waren übernehmen
            for (String language : oldSprachen) {
                if (!newLanguages.contains(language)) {
                    multiLang.setText(language, "");
                }
            }
        } else {
            for (String language : oldSprachen) {
                if (!newLanguages.contains(language)) {
                    iPartsDataDictLanguageMeta dataDictLanguageMeta = languagesList.findLanguage(language);
                    if (dataDictLanguageMeta != null) {
                        languagesList.delete(dataDictLanguageMeta, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
        }
    }

    /**
     * Check, ob die {@link iPartsDataDictLanguageMeta} Objekte zur übergebenen <code>multiLang</code> innerhalb dieses
     * {@link iPartsDataDictMeta} Objekts passen
     *
     * @param multiLang
     */
    private void checkDictLanguagesForMultiLang(EtkMultiSprache multiLang) {
        if (isNew()) {
            //ist Neu => für alle nicht leeren Texte ein neues {@link iPartsDataDictLanguageMeta) Element anlegen
            for (Map.Entry<String, String> languageText : multiLang.getLanguagesAndTexts().entrySet()) {
                if (!languageText.getValue().isEmpty()) {
                    addLanguage(languageText.getKey(), DBActionOrigin.FROM_EDIT);
                }
            }
        } else {
            //neue Texte bei {@link iPartsDataDictLanguageMeta) hinzufügen, modifizieren oder löschen
            for (Map.Entry<String, String> languageText : multiLang.getLanguagesAndTexts().entrySet()) {
                String language = languageText.getKey();
                if (languageText.getValue().isEmpty()) {
                    //leerer Text => falls {@link iPartsDataDictLanguageMeta) vorhanden -> löschen
                    iPartsDataDictLanguageMeta dataDictLanguageMeta = getLanguages().findLanguage(language);
                    if (dataDictLanguageMeta != null) {
                        getLanguages().delete(dataDictLanguageMeta, DBActionOrigin.FROM_EDIT);
                    }
                } else {
                    //besetzter Text
                    iPartsDataDictLanguageMeta actDictLanguageMeta = getLanguages().findLanguage(language);
                    if (actDictLanguageMeta == null) {
                        //in {@link iPartsDataDictLanguageMetaList) nicht vorhanden => hinzufügen
                        addLanguage(language, DBActionOrigin.FROM_EDIT);
                    } else {
                        //vorhanden: falls modifiziert => {@link iPartsDataDictLanguageMeta) updaten
                        if (!languageText.getValue().equals(getMultiLang().getText(language))) {
                            updateDictLanguageMeta(actDictLanguageMeta);
                        }
                    }
                }
            }
        }
    }

    /**
     * Setzt ein {@link EtkMultiSprache} Objekt mit schon vorgeladenenen Texten. Dadurch wird das zusätzliche Laden
     * in {@link #setNewMultiLang(EtkMultiSprache)} umgangen. Zusätzlich werden die {@link iPartsDataDictLanguageMeta}
     * Objekte synchronisiert.
     *
     * @param multiLang
     */
    public void setNewMultiLangFromDB(EtkMultiSprache multiLang) {
        clearMultiLang();
        this.multiLang = multiLang;
        checkDictLanguagesForMultiLang(multiLang);
    }

    // Convenience Method
    public String getTextId() {
        return getAsId().getTextId();
    }

    public String getForeignId() {
        return getFieldValue(FIELD_DA_DICT_META_FOREIGNID);
    }

    public void setForeignId(String foreignId, DBActionOrigin origin) {
        setFieldValue(FIELD_DA_DICT_META_FOREIGNID, foreignId, origin);
    }

    public String getSource() {
        return getFieldValue(FIELD_DA_DICT_META_SOURCE);
    }

    public void setSource(String source, DBActionOrigin origin) {
        setFieldValue(FIELD_DA_DICT_META_SOURCE, source, origin);
    }

    public String getState() {
        return getFieldValue(FIELD_DA_DICT_META_STATE);
    }

    public void setState(String state, DBActionOrigin origin) {
        setFieldValue(FIELD_DA_DICT_META_STATE, state, origin);
    }

    public String getCreationDate() {
        return getFieldValue(FIELD_DA_DICT_META_CREATE);
    }

    public void setActCreationDate(DBActionOrigin origin) {
        setFieldValueAsDateTime(FIELD_DA_DICT_META_CREATE, Calendar.getInstance(), origin);
    }

    public String getChangeDate() {
        return getFieldValue(FIELD_DA_DICT_META_CHANGE);
    }

    public void setActChangeDate(DBActionOrigin origin) {
        setFieldValueAsDateTime(FIELD_DA_DICT_META_CHANGE, Calendar.getInstance(), origin);
    }

    public String getUserId() {
        return getFieldValue(FIELD_DA_DICT_META_USERID);
    }

    public void setUserId(String userId, DBActionOrigin origin) {
        setFieldValue(FIELD_DA_DICT_META_USERID, userId, origin);
    }

    public void setELDASId(String eldasId, DBActionOrigin origin) {
        setFieldValue(FIELD_DA_DICT_META_ELDASID, eldasId, origin);
    }

    public void setDIALOGId(String dialogId, DBActionOrigin origin) {
        setFieldValue(FIELD_DA_DICT_META_DIALOGID, dialogId, origin);
    }

    public String getELDASId() {
        return getFieldValue(FIELD_DA_DICT_META_ELDASID);
    }

    public String getDIALOGId() {
        return getFieldValue(FIELD_DA_DICT_META_DIALOGID);
    }

    /**
     * Prüft, ob der Text für mind. eine Sprache im Übersetzungsumfang vorhanden ist (= JobId vorhanden)
     *
     * @return
     */
    public boolean isInTranslationWorkflow() {
        for (iPartsDataDictLanguageMeta language : getLanguages()) {
            if (language.getTranslationState() == iPartsDictSpracheTransState.IN_TRANSLATION_WORKFLOW) {
                return true;
            }
        }
        return false;
    }

    public void setTranslationStateForAllExistingLanguages(iPartsDictSpracheTransState translationState, DBActionOrigin origin) {
        for (iPartsDataDictLanguageMeta language : getLanguages()) {
            language.setTranslationState(translationState, origin);
        }
    }

    public void setTranslationStateForLanguage(iPartsDictSpracheTransState translationState, Language language, DBActionOrigin origin) {
        for (iPartsDataDictLanguageMeta existingLang : getLanguages()) {
            if (existingLang.getAsId().getLanguage().equals(language.getCode())) {
                existingLang.setTranslationState(translationState, origin);
                return;
            }
        }
    }
}
