/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary;

import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.common.EtkDataTextEntry;
import de.docware.apps.etk.base.project.common.EtkDataTextEntryList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsTermIdHandler;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsUserAdminDb;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMeta;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDataDictMetaList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictMetaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.iPartsDictTextKindId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.helper.DictTextSearchHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObject;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.VarParam;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper-Klasse für das Handling mit Lexikon Einträgen
 */
public class DictImportTextIdHelper implements iPartsConst {

    private static final boolean LOG_DICT_DIFFS_BETWEEN_IMPORT_AND_DB = false;

    private EtkProject project;
    private List<String> warnings;
    private List<String> infos;
    private DictImportTextCache dictImportTextCache;
    private DictTextSearchHelper dictTextSearchHelper;

    public DictImportTextIdHelper(EtkProject project) {
        this.project = project;
        this.warnings = new DwList<>();
        this.infos = new DwList<>();
    }

    public EtkProject getProject() {
        return project;
    }

    public boolean hasWarnings() {
        return (warnings.size() > 0);
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean hasInfos() {
        return (infos.size() > 0);
    }

    public List<String> getInfos() {
        return infos;
    }

    /**
     * Verarbeiten von sprachneutralen Texten für das übergebene Feld. Falls der übergebene Text in der DB (bzw. dem Cache) existiert,
     * wird die Text-ID in dem übergebenen EtkMultiSprache Objekt gespeichert. Falls der Text noch nicht existiert, wird ein neuer
     * "sprachneutraler Text" in der DB (und dem Cache) abgelegt. Die neu erzeugte Text-ID wird dem übergebenen EtkMultiSprache Objekt
     * zugewiesen.
     *
     * @param multiLang
     * @param tableDotFieldName
     * @return
     */
    public boolean handleNeutralTextWithCache(EtkMultiSprache multiLang, String tableDotFieldName) {
        clearWarningsAndInfos();
        Map<String, String> textEntryCache = getDictImportTextCache().getTextCache(getProject(), DictTextKindTypes.NEUTRAL_TEXT, infos);
        // Textart Sprachneutraler Text über Feldreferenz holen. Falls das Feld nicht für sprachneutrale Texte definiert wurde -> Fehler
        iPartsDictTextKindId txtKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(DictTextKindTypes.NEUTRAL_TEXT, tableDotFieldName);
        if ((txtKindId != null) && txtKindId.isValidId()) {
            String suchText = multiLang.getText(Language.DE.getCode());
            String dictTextId = null;
            if (!suchText.trim().isEmpty()) {
                // Check, ob Text-ID im Cache ist
                dictTextId = textEntryCache.get(suchText);
                if (dictTextId == null) {
                    // nicht gefunden -> neu Anlegen
                    dictTextId = DictHelper.buildDictTextId(StrUtils.makeGUID());
                    // bei Sprachneutrale Texte reicht DE
//                    multiLang.fillAllLanguages(getProject().getConfig().getDatabaseLanguages(), Language.DE);
                    textEntryCache.put(suchText, dictTextId);
                }
            } else {
                //kein Suchtext vorhanden
                //Fehlermeldung
                warnings.add(TranslationHandler.translate("!!Text-ID und Suchtext dürfen nicht leer sein. Textart: %1", DictTextKindTypes.NEUTRAL_TEXT.getMadId()));
            }
            if (dictTextId != null) {
                // in DA_DICT_META nach Eintrag suchen bzw. anlegen
                createOrUpdateDictMeta(txtKindId, dictTextId, "", iPartsImportDataOrigin.MAD.getOrigin(), multiLang, tableDotFieldName, true, warnings);
            }
        } else {
            warnings.add(TranslationHandler.translate("!!Feld \"%1\" ist nicht für sprachneutrale Texte definiert.", tableDotFieldName));
            return false;
        }
        return true;
    }

    public boolean handleFootNoteTextWithCache(EtkMultiSprache multiLang, String madForeignId, String madForeignSource, String tableDotFieldName) {
        clearWarningsAndInfos();
        Map<String, String> textEntryCache = getDictImportTextCache().getTextCache(getProject(), DictTextKindTypes.FOOTNOTE, infos);
        // Textart FootNote über Feldreferenz holen. Falls das Feld nicht für FootNote Texte definiert wurde -> Fehler
        iPartsDictTextKindId txtKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(DictTextKindTypes.FOOTNOTE, tableDotFieldName);
        if ((txtKindId != null) && txtKindId.isValidId()) {
            String dictTextId = null;
            if (madForeignId.isEmpty()) {
                String suchText = multiLang.getText(Language.DE.getCode());
                if (!suchText.trim().isEmpty()) {

                    boolean textTooLong = false;
                    // Erst ab der Version 6.2 kann die Benennung beliebig lang sein.
                    // In älteren Versionen muss sie weiterhin abgeschnitten werden.
                    if (getProject().getDB().configBase.getDataBaseVersion() < 6.2) {

                        if (suchText.length() > getProject().getConfig().getDBDescription().findField(TABLE_SPRACHE, FIELD_S_BENENN).getFieldSize()) {
                            // Text ist zu lang, diese ID muss später gelöscht werden
                            textTooLong = true;
                        }
                    }
                    // Check, ob Text-ID im Cache ist
                    dictTextId = textEntryCache.get(suchText);
                    if (dictTextId == null) {
                        // nicht gefunden -> neu Anlegen
                        String newId = StrUtils.makeGUID();
                        if (textTooLong) {
                            newId += iPartsDictPrefixAndSuffix.DICT_LONGTEXT_SUFFIX.getPrefixValue();
                        }
                        dictTextId = DictHelper.buildDictTextId(newId);
                        textEntryCache.put(suchText, dictTextId);
                    }
                } else {
                    //kein Suchtext vorhanden
                    //Fehlermeldung
                    warnings.add(TranslationHandler.translate("!!Text-ID und Suchtext dürfen nicht leer sein. Textart: %1", DictTextKindTypes.FOOTNOTE.getMadId()));
                }
            } else {
                //dictTextId bilden
                dictTextId = DictHelper.buildDictTextId(madForeignId);
            }
            if (dictTextId != null) {
                // in DA_DICT_META nach Eintrag suchen bzw. anlegen
                createOrUpdateDictMeta(txtKindId, dictTextId, madForeignId, madForeignSource, multiLang, tableDotFieldName, true, warnings);
            }
        } else {
            warnings.add(TranslationHandler.translate("!!Feld \"%1\" ist nicht für Fußnoten Texte definiert.", tableDotFieldName));
            return false;
        }
        return true;
    }

    public iPartsDataDictMeta handleDictTextIdWithoutSave(DictTextKindTypes importType, EtkMultiSprache multiLang,
                                                          String foreignSource, boolean dictTextIdCreationAllowed, String tableDotFieldName) {
        return handleDictTextIdWithoutSave(importType, multiLang, foreignSource, dictTextIdCreationAllowed, tableDotFieldName, Language.DE);
    }

    public iPartsDataDictMeta handleDictTextIdWithoutSave(DictTextKindTypes importType, EtkMultiSprache multiLang,
                                                          String foreignSource, boolean dictTextIdCreationAllowed,
                                                          String tableDotFieldName, Language lang) {
        clearWarningsAndInfos();
        iPartsDataDictMeta dataDictMeta = null;
        //Schritt 1: Bestimmung der DictGuid
        iPartsDictTextKindId txtKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(importType, tableDotFieldName);
        //nur etwas tun, falls eine txtKindId vorhanden
        if ((txtKindId != null) && txtKindId.isValidId()) {
            //Eintrag gehört zum Lexikon
            String dictTextId = null;
            //keine FremdId angegeben => Suche nach Text
            String suchText = multiLang.getText(lang.getCode());
            if (!suchText.trim().isEmpty()) {
                Set<String> textIds = getSearchHelper().findTextIdsInTableSprache(suchText, lang, DictHelper.buildIPARTSDictTextId("*"));

                if (!textIds.isEmpty()) {
                    //die richtige TextId im Lexikon finden
                    for (String helperDictTextId : textIds) {
                        dataDictMeta = new iPartsDataDictMeta(getProject(), null);
                        if (dataDictMeta.loadFromDB((new iPartsDictMetaId(txtKindId.getTextKindId(), helperDictTextId)))) {
                            if (dataDictMeta.getSource().equals(foreignSource)) {
                                dictTextId = helperDictTextId;
                                break;
                            }
                        }
                    }
                    if (dictTextId == null) {
                        dataDictMeta = null;
                        if (dictTextIdCreationAllowed) {
                            dictTextId = DictHelper.buildIPARTSDictTextId(StrUtils.makeGUID());
                        } else {
                            //kein Suchtext gefunden, oder darf in diesem Fall nicht neu angelegt werden.
                            //Fehlermeldung
                            warnings.add(TranslationHandler.translate("!!Keine Text-ID zu \"%1\" gefunden", suchText));
                        }
                    }
                } else {
                    if (dictTextIdCreationAllowed) {
                        dictTextId = DictHelper.buildIPARTSDictTextId(StrUtils.makeGUID());
                    } else {
                        //kein Suchtext gefunden, oder darf in diesem Fall nicht neu angelegt werden.
                        //Fehlermeldung
                        warnings.add(TranslationHandler.translate("!!Keine Text-ID zu \"%1\" gefunden", suchText));
                    }
                }
            } else {
                //kein Suchtext vorhanden
                //Fehlermeldung
                warnings.add(TranslationHandler.translate("!!Text-ID und Suchtext dürfen nicht leer sein. Textart: %1", importType.getMadId()));
            }
            if (dictTextId != null) {
                //Schritt 2: in DA_DICT_META nach Eintrag suchen
                dataDictMeta = createOrUpdateDictMetaWithoutSave(dataDictMeta, txtKindId, dictTextId,
                                                                 foreignSource, multiLang, tableDotFieldName,
                                                                 true, warnings);
            }


        } else {
            warnings.add(TranslationHandler.translate("!!\"%1\" ist keine gültige Textart für das Feld \"%2\"", importType.getMadId(), tableDotFieldName));
        }
        return dataDictMeta;
    }

    private iPartsDataDictMeta createOrUpdateDictMetaWithoutSave(iPartsDataDictMeta dataDictMeta, iPartsDictTextKindId textKindId, String dictTextId,
                                                                 String foreignSource, EtkMultiSprache multiLang, String tableDotFieldName,
                                                                 boolean createNewDictEntry, List<String> warnings) {
        //Schritt 2: in DA_DICT_META nach Eintrag suchen
        if (dataDictMeta == null) {
            iPartsDictMetaId dictMetaId = new iPartsDictMetaId(textKindId.getTextKindId(), dictTextId);
            dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
        }
        if (!dataDictMeta.existsInDB()) {
            if (createNewDictEntry) {
                //Text-Id in multiEdit eintragen
                multiLang.setTextId(dictTextId);
                //noch kein Lexikoneintrag vorhanden
                initDataDictMeta(dataDictMeta, "", foreignSource, multiLang);
                dataDictMeta.setNewMultiLang(multiLang);
                //neuen Dict-Eintrag speichern
//                saveDictMetaEntry(dataDictMeta, tableDotFieldName);
            } else {
                dataDictMeta = null;
            }
        } else {
            //DictMeta Eintrag ist vorhanden
            EtkMultiSprache dictMultiEdit = dataDictMeta.getMultiLang();
            if (!dictMultiEdit.getTextId().isEmpty()) {
                //Lexikon-Eintrag in SPRACHE ist vorhanden
                if (!dictMultiEdit.getTextId().equals(multiLang.getTextId())) {
                    //TextIds unterscheiden sich
                    if (multiLang.getTextId().isEmpty()) {
                        //neuer Eintrag besitzt keine TextId => Lexikon-Eintrag übernehmen
                        multiLang.assign(dictMultiEdit);
                    } else {
                        //Fehlermeldung
                        //warnings.add(TranslationHandler.translate("!!Unterschiedliche TextId (Lexikon %1) (Import %2)", dictMultiEdit.getTextId(), multiLang.getTextId()));
                        infos.add(TranslationHandler.translate("!!Unterschiedliche Text-ID (Lexikon %1) (Import %2)", dictMultiEdit.getTextId(), multiLang.getTextId()));
                        //foreignId (dictMultiEdit) gewinnt => Lexikon-Eintrag übernehmen
                        multiLang.assign(dictMultiEdit);
                    }
                }
            } else {
                // DictMeta Eintrag ist vorhanden, aber kein Lexikon-Eintrag in SPRACHE vorhanden => neuen anlegen
                multiLang.setTextId(dictTextId);
                dataDictMeta.setNewMultiLang(multiLang);
            }

//            updateDictMeta(dataDictMeta, multiLang, dictTextId, dialogID, eldasID, tableDotFieldName, true);
        }
        return dataDictMeta;
    }


    /**
     * Aufgrund des importTypes nach einem Dictionary-Eintrag suchen bzw anlegen und in multiEdit hinterlegen
     * Bei einem Fehler ist hasWarnings() == true
     *
     * @param importType                Textart
     * @param multiLang                 wenn madForeignId ungültig: suche nach DE Text wenn dieser gültig; andere Sprachen werden aus Wörterbuch befüllt bzw alle Sprachen mit DE Text wenn Neuanlage
     * @param madForeignId              über diese Id wird nach Dictionary-Eintrag gesucht; wenn leer wird nach DE Text aus multiLang gesucht
     * @param madForeignSource
     * @param dictTextIdCreationAllowed wenn true darf neue Text ID erzeugt werden wenn Text noch nicht vorhanden
     * @param tableDotFieldName
     * @param eldasID
     * @param dialogID
     * @return
     */
    public boolean handleDictTextId(DictTextKindTypes importType, EtkMultiSprache multiLang, String madForeignId,
                                    String madForeignSource, boolean dictTextIdCreationAllowed, String tableDotFieldName, String eldasID, String dialogID) {
        return handleDictTextId(importType, multiLang, madForeignId, madForeignSource, dictTextIdCreationAllowed, tableDotFieldName, eldasID, dialogID, false);
    }


    /**
     * Aufgrund des importTypes nach einem Dictionary-Eintrag suchen bzw anlegen und in multiEdit hinterlegen
     * Bei einem Fehler ist hasWarnings() == true
     *
     * @param importType                     Textart
     * @param multiLang                      wenn madForeignId ungültig: suche nach DE Text wenn dieser gültig; andere Sprachen werden aus Wörterbuch befüllt bzw alle Sprachen mit DE Text wenn Neuanlage
     * @param madForeignId                   über diese Id wird nach Dictionary-Eintrag gesucht; wenn leer wird nach DE Text aus multiLang gesucht
     * @param madForeignSource
     * @param dictTextIdCreationAllowed      wenn true darf neue Text ID erzeugt werden wenn Text noch nicht vorhanden
     * @param tableDotFieldName
     * @param eldasID
     * @param dialogID
     * @param dictTextIdUsageCreationAllowed wenn true darf neue Verwendung für bestehenden Text ID erzeugt werden
     * @return {@code false} falls die Textart nicht gefunden werden konnte und der Import deswegen abgebrochen werden muss
     */
    public boolean handleDictTextId(DictTextKindTypes importType, EtkMultiSprache multiLang, String madForeignId,
                                    String madForeignSource, boolean dictTextIdCreationAllowed, String tableDotFieldName, String eldasID, String dialogID,
                                    boolean dictTextIdUsageCreationAllowed) {
        clearWarningsAndInfos();
        //Schritt 1: Bestimmung der DictGuid
        iPartsDictTextKindId txtKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(importType, tableDotFieldName);
        //nur etwas tun, falls eine txtKindId vorhanden
        if ((txtKindId != null) && txtKindId.isValidId()) {
            //Eintrag gehört zum Lexikon
            String dictTextId = null;
            if (madForeignId.isEmpty()) {
                //keine FremdId angegeben => Suche nach Text
                String suchText = multiLang.getText(Language.DE.getCode());
                if (!suchText.trim().isEmpty()) {
                    Set<String> textIds = getSearchHelper().findRegularTextIdsInTableSprache(suchText, Language.DE);

                    if (!textIds.isEmpty()) {
                        if (importType == DictTextKindTypes.NEUTRAL_TEXT) {
                            //Feststellen, ob Text zu Sprachneutraler Textart gehört
                            for (String helperDictTextId : textIds) {
                                iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), null);
                                if (dataDictMeta.loadFromDB(new iPartsDictMetaId(txtKindId.getTextKindId(), helperDictTextId))) {
                                    //Eintrag gefunden
                                    dictTextId = helperDictTextId;
                                    break;
                                }
                            }
                            if (dictTextId == null) {
                                //Ausnahme für Sprachneutrale Texte: Neu anlegen
                                dictTextId = DictHelper.buildDictTextId(StrUtils.makeGUID());
                                multiLang.fillAllLanguages(getProject().getConfig().getDatabaseLanguages(), Language.DE);
                            }
                        } else {
                            //verhindern, dass Text zu einem Sprachneutralen Text gehört
                            iPartsDictTextKindId neutralTxtKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(DictTextKindTypes.NEUTRAL_TEXT, tableDotFieldName);
                            if ((neutralTxtKindId != null) && (neutralTxtKindId.isValidId())) {
                                for (String helperDictTextId : textIds) {
                                    iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), null);
                                    if (!dataDictMeta.loadFromDB(new iPartsDictMetaId(neutralTxtKindId.getTextKindId(), helperDictTextId))) {
                                        //kein Eintrag in Dictionary-Meta => nimm es
                                        dictTextId = helperDictTextId;
                                        break;
                                    }
                                }
                            } else {
                                //die richtige TextId im Lexikon finden
                                for (String helperDictTextId : textIds) {
                                    iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), null);
                                    if (dataDictMeta.loadFromDB((new iPartsDictMetaId(txtKindId.getTextKindId(), helperDictTextId)))) {
                                        dictTextId = helperDictTextId;
                                        break;
                                    }
                                }
                                if (dictTextId == null) {
                                    if ((textIds.size() > 0) && dictTextIdUsageCreationAllowed) {
                                        // Wir wollen eine neue Verwendung einer bestehenden Text ID hinzufügen.
                                        // Wir haben bei mehreren Text IDs keine Information welche Text ID die beste ist; wir nehmen die erste
                                        dictTextId = textIds.iterator().next();
                                    } else {
                                        //kein Suchtext gefunden, oder darf in diesem Fall nicht neu angelegt werden.
                                        //Fehlermeldung
                                        warnings.add(TranslationHandler.translate("!!Keine Text-ID zu \"%1\" gefunden", suchText));
                                    }
                                }
                            }
                        }
                    } else {
                        if (dictTextIdCreationAllowed) {
                            //Ausnahme für Sprachneutrale Texte: Neu anlegen
                            dictTextId = DictHelper.buildDictTextId(StrUtils.makeGUID());
                            // bei Sprachneutrale Texte reicht DE
//                            multiLang.fillAllLanguages(getProject().getConfig().getDatabaseLanguages(), Language.DE);
                        } else {
                            //kein Suchtext gefunden, oder darf in diesem Fall nicht neu angelegt werden.
                            //Fehlermeldung
                            warnings.add(TranslationHandler.translate("!!Keine Text-ID zu \"%1\" gefunden", suchText));
                        }
                    }
                } else {
                    //kein Suchtext vorhanden
                    //Fehlermeldung
                    warnings.add(TranslationHandler.translate("!!Text-ID und Suchtext dürfen nicht leer sein. Textart: %1", importType.getMadId()));
                }
            } else {
                //dictTextId bilden
                dictTextId = DictHelper.buildDictTextId(madForeignId);
            }
            if (dictTextId != null) {
                //Schritt 2: in DA_DICT_META nach Eintrag suchen
                createOrUpdateDictMeta(txtKindId, dictTextId, madForeignId, madForeignSource, multiLang, tableDotFieldName,
                                       eldasID, dialogID, null, null, true, warnings);
            }
        } else {
            warnings.add(TranslationHandler.translate("!!\"%1\" ist keine gültige Textart für das Feld \"%2\"", importType.getMadId(), tableDotFieldName));
            return false;
        }
        return true;
    }

    /**
     * Erzeugt oder aktualisiert Code-Benennungen aus dem PROVAL System
     *
     * @param multiLang
     * @return
     */
    public boolean handlePROVALCodeTextId(EtkMultiSprache multiLang) {
        DictTextKindTypes dictTextKindType = DictTextKindTypes.CODE_NAME;
        iPartsImportDataOrigin source = iPartsImportDataOrigin.PROVAL;
        String tableAndFieldName = TableAndFieldName.make(TABLE_DA_CODE, FIELD_DC_DESC);
        Language searchTextLanguage = Language.DE;
        return handleTextIdWithSource(dictTextKindType, multiLang, searchTextLanguage, tableAndFieldName, source);
    }

    public boolean handleSAPMBSCodeTextId(EtkMultiSprache multiLang) {
        DictTextKindTypes dictTextKindType = DictTextKindTypes.CODE_NAME;
        iPartsImportDataOrigin source = iPartsImportDataOrigin.SAP_MBS;
        String tableAndFieldName = TableAndFieldName.make(TABLE_DA_CODE, FIELD_DC_DESC);
        Language searchTextLanguage = Language.DE;
        return handleTextIdWithSource(dictTextKindType, multiLang, searchTextLanguage, tableAndFieldName, source);
    }

    /**
     * Suche nach DE-Text in TextArt ADD_TEXT.
     * Wenn der Text nicht vorhanden ist, lege neuen Lexikon-Eintrag und Source = Connect an.
     *
     * @param multiLang
     * @return
     */
    public boolean handleConnectTextId(EtkMultiSprache multiLang, boolean addTextToCache, boolean checkNotReleasedTexts) {
        DictTextKindTypes dictTextKindType = DictTextKindTypes.ADD_TEXT;
        String tableAndFieldName = TableAndFieldName.make(TABLE_DA_WIRE_HARNESS, FIELD_DWH_CONTACT_ADD_TEXT);
        return handleTextIdWithCache(multiLang, dictTextKindType, tableAndFieldName,
                                     iPartsImportDataOrigin.CONNECT, addTextToCache, checkNotReleasedTexts);
    }

    /**
     * Suche nach DE-Text in TextArt NEUTRAL_TEXT.
     * Wenn der Text nicht vorhanden ist, lege neuen Lexikon-Eintrag und Source = origin an.
     *
     * @param multiLang
     * @param origin
     * @param addTextToCache
     * @param checkNotReleasedTexts
     * @return
     */
    public boolean handleNeutralTextIdForCombText(EtkMultiSprache multiLang, iPartsImportDataOrigin origin, boolean addTextToCache,
                                                  boolean checkNotReleasedTexts, VarParam<iPartsDataDictMeta> foundDictMeta) {
        DictTextKindTypes dictTextKindType = DictTextKindTypes.NEUTRAL_TEXT;
        String tableAndFieldName = TableAndFieldName.make(TABLE_DA_COMB_TEXT, FIELD_DCT_DICT_TEXT);
        return handleTextIdWithCache(multiLang, dictTextKindType, tableAndFieldName,
                                     origin, addTextToCache, checkNotReleasedTexts, foundDictMeta);
    }

    /**
     * Suche nach DE-Text in TextArt ADD_TEXT.
     * Wenn der Text nicht vorhanden ist, lege neuen Lexikon-Eintrag und Source = origin an.
     *
     * @param multiLang
     * @param origin
     * @param addTextToCache
     * @param checkNotReleasedTexts
     * @return
     */
    public boolean handleTextIdForCombText(EtkMultiSprache multiLang, iPartsImportDataOrigin origin, boolean addTextToCache,
                                           boolean checkNotReleasedTexts, VarParam<iPartsDataDictMeta> foundDictMeta) {
        DictTextKindTypes dictTextKindType = DictTextKindTypes.ADD_TEXT;
        String tableAndFieldName = TableAndFieldName.make(TABLE_DA_COMB_TEXT, FIELD_DCT_DICT_TEXT);
        return handleTextIdWithCache(multiLang, dictTextKindType, tableAndFieldName,
                                     origin, addTextToCache, checkNotReleasedTexts, foundDictMeta);
    }

    private boolean handleTextIdWithCache(EtkMultiSprache multiLang, DictTextKindTypes dictTextKindType, String tableAndFieldName,
                                          iPartsImportDataOrigin origin, boolean addTextToCache, boolean checkNotReleasedTexts) {
        return handleTextIdWithCache(multiLang, dictTextKindType, tableAndFieldName, origin, addTextToCache, checkNotReleasedTexts, null);
    }

    /**
     * Suche nach DE-Text in TextArt dictTextKindType unter Benutzung des Text-Cache.
     * Wenn der Text nicht vorhanden ist, lege neuen Lexikon-Eintrag und Source = origin an.
     *
     * @param multiLang
     * @param dictTextKindType
     * @param tableAndFieldName
     * @param origin
     * @param addTextToCache
     * @param checkNotReleasedTexts
     * @return
     */
    private boolean handleTextIdWithCache(EtkMultiSprache multiLang, DictTextKindTypes dictTextKindType, String tableAndFieldName,
                                          iPartsImportDataOrigin origin, boolean addTextToCache, boolean checkNotReleasedTexts,
                                          VarParam<iPartsDataDictMeta> foundDictMeta) {
        String searchTextLanguage = Language.DE.getCode();

        // Bestimmung der Textart-ID
        iPartsDictTextKindId textKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(dictTextKindType,
                                                                                                      tableAndFieldName);

        // Suche Text im Lexikon-Cache
        String textinDE = multiLang.getText(searchTextLanguage);
        // Wenn nicht nur "freigegebene" Texte berücksichtigt werden sollen
        DictTextCache cache;
        if (checkNotReleasedTexts) {
            cache = DictTextCache.getInstanceWithAllTextStates(dictTextKindType, searchTextLanguage);
        } else {
            cache = DictTextCache.getInstance(dictTextKindType, searchTextLanguage);
        }
        Map<String, String> searchTexts = cache.searchTexts(textinDE);
        if (!searchTexts.isEmpty()) {
            // nur etwas tun, falls eine textKindId vorhanden
            if ((textKindId != null) && textKindId.isValidId()) {
                // mit gefundener Text-Id überprüfen, ob DictMeta existiert
                String textId = searchTexts.get(textinDE);
                iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), new iPartsDictMetaId(textKindId.getTextKindId(),
                                                                                                            textId));
                if (dataDictMeta.existsInDB()) {
                    // Eintrag gefunden
                    multiLang.assign(dataDictMeta.getMultiLang());

                    // Handelt es sich nicht um einen Connect-Eintrag, dann SPRACHE-Tabelle updaten
                    updateTableSpracheForConnect(dataDictMeta, multiLang, searchTextLanguage, tableAndFieldName);
                    if (foundDictMeta != null) {
                        foundDictMeta.setValue(dataDictMeta);
                    }
                    return true;
                }
            } else {
                warnings.add(TranslationHandler.translate("!!\"%1\" ist keine gültige Textart für das Feld \"%2\"",
                                                          TranslationHandler.translate(dictTextKindType.getTextKindName()),
                                                          tableAndFieldName));
                return false;
            }
        }

        // Neues iPartsDataDictMeta erzeugen, befüllen und speichern
        String dictTextId = DictHelper.buildIPARTSDictTextId();
        if (dictTextKindType == DictTextKindTypes.NEUTRAL_TEXT) {
            dictTextId = DictHelper.buildDictTextId(StrUtils.makeGUID());
        }
        iPartsDataDictMeta dictMeta = createConnectDictionaryEntry(multiLang, dictTextId, textKindId,
                                                                   origin,
                                                                   tableAndFieldName, (foundDictMeta == null));
        if (foundDictMeta != null) {
            foundDictMeta.setValue(dictMeta);
        }
        // Wenn gewünscht, den Text direkt im Cache ablegen
        if (addTextToCache) {
            // Wenn alle Status berücksichtigt werden sollen, dann arbeiten wir gerade mit temporärem Import Cache-Objekt,
            // das beim nächsten Durchlauf neu geladen wird. Texte, die neu sind, werden als "freigegeben" im Lexikon
            // abgelegt. Weil das länger dauern kann, als der nächste Durchlauf und der Cache mit den nicht freigegbenen
            // Texten eher als temporärer Cache bzw. als Zusatz zum "normalen" Cache betrachtet wird, muss der neue Text
            // hier in das Cache-Objekt gelegt werden, das die "freigegeben" Texte hält.
            DictClusterEventHelper.fireNewDictionaryClusterEvent(dictMeta, dictTextKindType, multiLang);
        }
        return true;
    }

    private iPartsDataDictMeta createConnectDictionaryEntry(EtkMultiSprache multiLang, String dictTextId, iPartsDictTextKindId textKindId,
                                                            iPartsImportDataOrigin source, String tableAndFieldName,
                                                            boolean withSave) {
        iPartsDictMetaId dictMetaId = new iPartsDictMetaId(textKindId.getTextKindId(), dictTextId);
        iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
        // Text-Id in multiEdit eintragen
        multiLang.setTextId(dictTextId);
        // Initialisieren und die Texte setzen
        initDataDictMeta(dataDictMeta, "", source.getOrigin(), multiLang, "", "");
        if (withSave) {
            // Eintrag speichern
            saveDictMetaEntry(dataDictMeta, tableAndFieldName, null);
        }
        return dataDictMeta;
    }

    private void updateTableSpracheForConnect(iPartsDataDictMeta dataDictMeta, EtkMultiSprache multiLang,
                                              String searchTextLanguage, String tableAndFieldName) {
        // nur überprüfen, wenn Source nicht CONNECT ist
        if (!dataDictMeta.getSource().equals(iPartsImportDataOrigin.CONNECT.getOrigin())) {
            // vorhandene SPRACHE-Einträge holen
            EtkDataTextEntryList searchList = new EtkDataTextEntryList();
            searchList.setSearchWithoutActiveChangeSets(true);
            String[] whereFields = new String[]{ FIELD_S_SPRACH, FIELD_S_TEXTID };
            String[] whereValues = new String[]{ searchTextLanguage, multiLang.getTextId() };
            searchList.searchSortAndFill(getProject(), TABLE_SPRACHE, whereFields, whereValues, null,
                                         DBDataObjectList.LoadType.ONLY_IDS, DBActionOrigin.FROM_DB);
            boolean doUpdate = true;
            // S_FELD mit tableAndFieldName bereits vorhanden?
            for (EtkDataTextEntry dataTextEntry : searchList) {
                if (dataTextEntry.getAsId().getField().equals(tableAndFieldName)) {
                    doUpdate = false;
                    break;
                }
            }
            if (doUpdate) {
                // In SPRACHE noch neue Einträge für tableAndFieldName
                getProject().getDbLayer().startTransaction();
                try {
                    getProject().getDbLayer().saveLanguageEntry(tableAndFieldName, multiLang.getTextId(), "", multiLang, null);
                    getProject().getDbLayer().commit();
                } catch (Exception e) {
                    getProject().getDbLayer().rollback();
                    warnings.add(e.getMessage());
                    Logger.getLogger().throwRuntimeException(e);
                }
            }
        }
    }

    /**
     * Erzeugt oder aktualisiert Lexikoneinträge. Wenn der Eintrag mit dem übergebenen Suchtext (multilang) in der
     * übergebenen Sprache nicht gefunden wird, wird ein neuer Lexikoneintrag angelegt. Ansonsten werden alle gefundenen
     * Lexikoneinträge mit dem neuen mutilang befüllt.
     *
     * @param dictTextKindType   Textart, nach der gesucht wird und die ggf. für den neuen Lexikoneintrag verwendet wird.
     * @param multiLang
     * @param searchTextLanguage Die Sprache deren Text aus dem multilang im Lexikon gesucht wird.
     * @param tableAndFieldName  Tabellen- und Feldname, nach denen gesucht wird und die ggf. für den neuen Lexikoneintrag verwendet werden.
     * @param source             Quelle, nach der gesucht wird und die ggf. für den neuen Lexikoneintrag verwendet wird.
     * @return
     */
    public boolean handleTextIdWithSource(DictTextKindTypes dictTextKindType, EtkMultiSprache multiLang, Language searchTextLanguage,
                                          String tableAndFieldName, iPartsImportDataOrigin source) {
        return handleTextIdWithSource(dictTextKindType, multiLang, searchTextLanguage, tableAndFieldName, source, null);
    }

    /**
     * Erzeugt oder aktualisiert Lexikoneinträge. Wenn der Eintrag mit dem übergebenen Suchtext (multilang) in der
     * übergebenen Sprache nicht gefunden wird, wird ein neuer Lexikoneintrag angelegt. Ansonsten werden alle gefundenen
     * Lexikoneinträge mit dem neuen mutilang befüllt.
     *
     * @param dictTextKindType      Textart, nach der gesucht wird und die ggf. für den neuen Lexikoneintrag verwendet wird.
     * @param multiLang
     * @param searchTextLanguage    Die Sprache deren Text aus dem multilang im Lexikon gesucht wird.
     * @param tableAndFieldName     Tabellen- und Feldname, nach denen gesucht wird und die ggf. für den neuen Lexikoneintrag verwendet werden.
     * @param source                Quelle, nach der gesucht wird und die ggf. für den neuen Lexikoneintrag verwendet wird.
     * @param dictTextIdForNewEntry Vorbereitete TextId oder null (für Obersetzungsprozess)
     * @return
     */
    public boolean handleTextIdWithSource(DictTextKindTypes dictTextKindType, EtkMultiSprache multiLang, Language searchTextLanguage,
                                          String tableAndFieldName, iPartsImportDataOrigin source, String dictTextIdForNewEntry) {

        clearWarningsAndInfos();
        String searchText = multiLang.getText(searchTextLanguage.getCode());
        if (searchText.trim().isEmpty()) {
            return false;
        }
        // Alle Einträge zum Suchtext suchen
        iPartsDataDictMetaList texts = getSearchHelper().searchDictTextsWithSource(searchText, searchTextLanguage, source, dictTextKindType, tableAndFieldName);
        if ((texts == null) || texts.isEmpty()) {
            // 1. Fall: In der DB existiert noch keine Datensatz, der den Suchtext enthält
            // Neues iPartsDataDictMeta erzeugen, befüllen und speichern
            String dictTextId;
            if (StrUtils.isValid(dictTextIdForNewEntry)) {
                dictTextId = dictTextIdForNewEntry;
            } else {
                dictTextId = DictHelper.buildDictTextId(StrUtils.makeGUID());
            }
            iPartsDictTextKindId textKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(dictTextKindType, tableAndFieldName);
            iPartsDictMetaId dictMetaId = new iPartsDictMetaId(textKindId.getTextKindId(), dictTextId);
            iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
            // Text-Id in multiEdit eintragen
            multiLang.setTextId(dictTextId);
            // Initialisieren und die Texte setzen
            initDataDictMeta(dataDictMeta, "", source.getOrigin(), multiLang, "", "");
            // Eintrag speichern
            saveDictMetaEntry(dataDictMeta, tableAndFieldName, null);
        } else {
            // 2.Fall: Es existiert mind. ein Treffer zum Suchtext
            if (texts.size() > 1) {
                infos.add(TranslationHandler.translate("!!Zum Suchtext \"%1\" wurden \"%2\" Lexikoneinträge gefunden.",
                                                       searchText, String.valueOf(texts.size())));
            }
            // Pro Treffer, die zu importierenden Texte setzen
            for (iPartsDataDictMeta dictMeta : texts) {
                // Das eigentliche Objekt aus der DB
                EtkMultiSprache originalMultiLangFromDB = dictMeta.getMultiLang();
                String textId = dictMeta.getAsId().getTextId();
                // Das neue Text-Objekt bekommt die ID aus der DB, damit die neuen Texte unter der alten ID abgelegt werden
                multiLang.setTextId(textId);
                multiLang.removeLanguagesWithEmptyTexts();
                checkTextConsistency(searchText, textId, multiLang, originalMultiLangFromDB);

                // Das neue Text-Objekt setzen
                dictMeta.setNewMultiLang(multiLang);
                saveDictMetaEntry(dictMeta, tableAndFieldName, originalMultiLangFromDB);
            }
        }
        return true;
    }

    /**
     * Überprüft die Konsistenz der Texte aus der DB und dem Importdatensatz. Der Importdatensatz ist das Objekt mit
     * dem aktuellen Stand, der später in der DB gespeichert werden soll.
     *
     * @param searchText
     * @param textId
     * @param importMultiLang
     * @param originalMultiLangFromDB
     */
    private void checkTextConsistency(String searchText, String textId, EtkMultiSprache importMultiLang,
                                      EtkMultiSprache originalMultiLangFromDB) {
        if (!importMultiLang.equalText(originalMultiLangFromDB)) {
            // Check, ob neue Texte hinzugekommen sind
            if (LOG_DICT_DIFFS_BETWEEN_IMPORT_AND_DB) {
                Map<String, String> additionalTexts = findAdditionalText(importMultiLang, originalMultiLangFromDB);
                additionalTexts.forEach((lang, text) -> {
                    infos.add(TranslationHandler.translate("!!Zum Suchtext \"%1\" und Sprache \"%2\" " +
                                                           "existiert in der Importdatei ein Text, der in der " +
                                                           "Datenbank nicht vorkommt. Importtext wird in die DB übernommen! " +
                                                           "TextId: %3, Text: %4",
                                                           searchText, lang, textId, text));
                });
            }
            Map<String, String> additionalTexts = findAdditionalText(originalMultiLangFromDB, importMultiLang);
            additionalTexts.forEach((lang, text) -> {
                // Text aus der DB, der im Importdatensatz nicht vorkommt, setzen
                importMultiLang.setText(lang, text);
                if (LOG_DICT_DIFFS_BETWEEN_IMPORT_AND_DB) {
                    infos.add(TranslationHandler.translate("!!Zum Suchtext \"%1\" und Sprache \"%2\" " +
                                                           "existiert in der Datenbank ein Text, der in der " +
                                                           "Importdatei nicht vorkommt. Der Text in der Datenbank bleibt " +
                                                           "erhalten! TextId: %3, Text: %4",
                                                           searchText, lang, textId, text));
                }
            });
            // Texte, die schon existierten aber nicht neu hinzugekommen sind, hinzufügen
            Set<String> langsWithDifferentTexts = findLanguagesWithDifferentTexts(originalMultiLangFromDB, importMultiLang);
            langsWithDifferentTexts.forEach(lang -> {
                String oldText = originalMultiLangFromDB.getText(lang);
                String newText = importMultiLang.getText(lang);
                infos.add(TranslationHandler.translate("!!Zum Suchtext \"%1\" wurden unterschiedliche " +
                                                       "Texte in Importdatei und Datenbank gefunden! Der Text in der " +
                                                       "Datenbank wird aktualisiert! TextId: %2, Sprache: %3," +
                                                       " Importtext: \"%4\", Datenbank: \"%5\"",
                                                       searchText, textId, lang, newText, oldText));
            });

        }
    }

    /**
     * Liefert die Sprachen, deren Texte in den übergebenen {@link EtkMultiSprache} Objekten unterschiedlich sind
     *
     * @param originalMultiLangFromDB
     * @param importMultiLang
     * @return
     */
    public Set<String> findLanguagesWithDifferentTexts(EtkMultiSprache originalMultiLangFromDB, EtkMultiSprache importMultiLang) {
        Set<String> sameLanguages = new HashSet<>(originalMultiLangFromDB.getSprachen());
        sameLanguages.retainAll(importMultiLang.getSprachen());
        return sameLanguages.stream().filter(lang -> {
            String oldText = originalMultiLangFromDB.getText(lang);
            String newText = importMultiLang.getText(lang);
            return !oldText.equals(newText);
        }).collect(Collectors.toSet());
    }

    /**
     * Liefert alle Texte, die im <code>multiLangWithTexts</code> aber nicht im <code>multiLangToCheck</code> Objekt vorkommen
     *
     * @param multiLangWithTexts
     * @param multiLangToCheck
     * @return
     */
    public Map<String, String> findAdditionalText(EtkMultiSprache multiLangWithTexts, EtkMultiSprache multiLangToCheck) {
        Map<String, String> result = new LinkedHashMap<>();
        multiLangWithTexts.getLanguagesAndTexts().forEach((lang, text) -> {
            if (!multiLangToCheck.spracheExists(lang) && StrUtils.isValid(text)) {
                result.put(lang, text);
            }
        });
        return result;
    }

    /**
     * Aufgrund des importTypes nach einem Dictionary-Eintrag suchen bzw anlegen und in multiEdit hinterlegen
     * Bei einem Fehler ist hasWarnings() == true
     *
     * @param importType              Textart
     * @param multiLang               wenn madForeignId ungültig: suche nach DE Text wenn dieser gültig; andere Sprachen werden aus Wörterbuch befüllt bzw alle Sprachen mit DE Text wenn Neuanlage
     * @param madForeignId            über diese Id wird nach Dictionary-Eintrag gesucht; wenn leer wird nach DE Text aus multiLang gesucht
     * @param madForeignSource
     * @param dictTextCreationAllowed
     * @param tableDotFieldName
     * @return {@code false} falls die Textart nicht gefunden werden konnte und der Import deswegen abgebrochen werden muss
     */
    public boolean handleDictTextId(DictTextKindTypes importType, EtkMultiSprache multiLang, String madForeignId,
                                    String madForeignSource, boolean dictTextCreationAllowed, String tableDotFieldName) {
        return handleDictTextId(importType, multiLang, madForeignId, madForeignSource, dictTextCreationAllowed, tableDotFieldName, null, null);
    }

    /**
     * Aufgrund des importTypes nach einem RSK-Dictionary-Eintrag suchen bzw anlegen und in multiEdit hinterlegen
     * Bei einem Fehler ist hasWarnings() == true
     *
     * @param importType
     * @param multiEdit         wird befüllt aus Lexikon
     * @param termId
     * @param rskForeignSource
     * @param tableDotFieldName
     * @return
     */
    public boolean handleDictTextId(DictTextKindRSKTypes importType, EtkMultiSprache multiEdit, String termId,
                                    String rskForeignSource, String tableDotFieldName) {
        warnings.clear();
        //Schritt 1: Bestimmung der DictGuid
        iPartsDictTextKindId txtKindId = DictTxtKindIdByMADId.getInstance(getProject()).getRSKTxtKindId(importType, tableDotFieldName);
        //nur etwas tun, falls eine txtKindId vorhanden
        if ((txtKindId != null) && txtKindId.isValidId()) {
            //Eintrag gehört zum Lexikon
            String dictTextId = null;
            if (termId.isEmpty()) {
                //keine TermId angegeben => Suche nach Text
                String suchText = multiEdit.getText(Language.DE.getCode());
                if (!suchText.trim().isEmpty()) {
                    Set<String> textIds = getSearchHelper().findTextIdsInTableSprache(suchText, Language.DE, importType);

                    if (!textIds.isEmpty()) {
                        //Text gefunden => nimm einfach den ersten
                        dictTextId = textIds.iterator().next();
                    } else {
                        //kein Suchtext gefunden
                        //Fehlermeldung
                        warnings.add(TranslationHandler.translate("!!Keine Text-ID zu \"%1\" gefunden", suchText));
                    }
                } else {
                    //kein Suchtext vorhanden
                    //Fehlermeldung
                    warnings.add(TranslationHandler.translate("!!Text-ID und Suchtext dürfen nicht leer sein. Textart: %1", importType.getRSKId()));
                }
            } else {
                //dictTextId bilden
                dictTextId = DictHelper.getRSKTextId(importType, termId);
            }
            if (dictTextId != null) {
                //Schritt 2: in DA_DICT_META nach Eintrag suchen
                createOrUpdateDictMeta(txtKindId, dictTextId, termId, rskForeignSource, multiEdit, tableDotFieldName, true, warnings);
            }
        } else {
            warnings.add(TranslationHandler.translate("!!\"%1\" ist keine gültige Textart für das Feld \"%2\"", importType.getRSKId(), tableDotFieldName));
            return false;
        }
        return true;
    }

    public boolean handleDictTextId(DictTextKindRSKTypes importType, EtkMultiSprache multiEdit, String termId,
                                    String rskForeignSource, String tableDotFieldName, iPartsDictTextKindId txtKindId, String dictPrefix) {
        warnings.clear();
        //nur etwas tun, falls eine txtKindId vorhanden
        if ((txtKindId != null) && txtKindId.isValidId()) {
            //Eintrag gehört zum Lexikon
            String dictTextId = null;
            if (termId.isEmpty()) {
                //keine TermId angegeben => Suche nach Text
                String suchText = multiEdit.getText(Language.DE.getCode());
                if (!suchText.trim().isEmpty()) {
                    String searchTextId = DictHelper.makeDictId(dictPrefix, "*");
                    Set<String> textIds = getSearchHelper().findTextIdsInTableSprache(suchText, Language.DE, searchTextId);

                    if (!textIds.isEmpty()) {
                        //Text gefunden => nimm einfach den ersten
                        dictTextId = textIds.iterator().next();
                    } else {
                        //kein Suchtext gefunden
                        //Fehlermeldung
                        warnings.add(TranslationHandler.translate("!!Keine Text-ID zu \"%1\" gefunden", suchText));
                    }
                } else {
                    //kein Suchtext vorhanden
                    //Fehlermeldung
                    warnings.add(TranslationHandler.translate("!!Text-ID und Suchtext dürfen nicht leer sein. Textart: %1", importType.getRSKId()));
                }
            } else {
                //dictTextId neu erzeugen bilden
                if (dictPrefix.equals(iPartsDictPrefixAndSuffix.DICT_INDISTINCT_TEXT_PREFIX.getPrefixValue())) {
                    // bei UNDEF Texten als TextID eine GUID erzeugen
                    dictTextId = DictHelper.buildTextId(dictPrefix, StrUtils.makeGUID());
                } else {
                    // sonst die TextID aus der TermID erstellen
                    dictTextId = DictHelper.buildTextId(dictPrefix, termId);
                }
            }
            if (dictTextId != null) {
                //Schritt 2: in DA_DICT_META nach Eintrag suchen
                createOrUpdateDictMeta(txtKindId, dictTextId, termId, rskForeignSource, multiEdit, tableDotFieldName, true, warnings);
            }
        } else {
            warnings.add(TranslationHandler.translate("!!\"%1\" ist keine gültige Textart für das Feld \"%2\"", importType.getRSKId(), tableDotFieldName));
            return false;
        }
        return true;
    }

    public boolean handleDictTextIdForProductRemarks(DictTextKindTypes importType, EtkMultiSprache multiText, String termId, String foreignSource, String tableDotFieldName) {
        warnings.clear();
        //Schritt 1: Bestimmung der DictGuid
        iPartsDictTextKindId txtKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(importType, tableDotFieldName);
        //nur etwas tun, falls eine txtKindId vorhanden
        if ((txtKindId != null) && txtKindId.isValidId()) {
            //Eintrag gehört zum Lexikon
            String dictTextId = null;
            iPartsDictTextKindId fallbackTxtKindId = null;
            String fallbackDictTextId = null;
            if (termId.isEmpty()) {
                //keine TermId angegeben => Suche nach Text
                String suchText = multiText.getText(Language.DE.getCode());
                if (!suchText.trim().isEmpty()) {
                    Set<String> textIds = getSearchHelper().findTextIdsInTableSprache(suchText, Language.DE, DictHelper.buildDictProductRemarksTextId("*"));

                    if (!textIds.isEmpty()) {
                        //die richtige TextId im Lexikon finden
                        for (String helperDictTextId : textIds) {
                            iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), null);
                            if (dataDictMeta.loadFromDB((new iPartsDictMetaId(txtKindId.getTextKindId(), helperDictTextId)))) {
                                dictTextId = helperDictTextId;
                                break;
                            }
                        }
                        if (dictTextId == null) {
                            warnings.add(TranslationHandler.translate("!!Keine Text-ID zu \"%1\" gefunden", suchText));
                        }
                    } else {
                        dictTextId = DictHelper.buildDictProductRemarksTextId(StrUtils.makeGUID());
                    }
                } else {
                    //kein Suchtext vorhanden
                    //Fehlermeldung
                    warnings.add(TranslationHandler.translate("!!Text-ID und Suchtext dürfen nicht leer sein. Textart: %1", importType.getTextKindName()));
                }
            } else {
                dictTextId = DictHelper.buildDictProductRemarksTextId(termId);
            }
            if (dictTextId != null) {
                //Schritt 2: in DA_DICT_META nach Eintrag suchen
                createOrUpdateDictMeta(txtKindId, dictTextId, termId, foreignSource, multiText, tableDotFieldName,
                                       null, null, fallbackTxtKindId, fallbackDictTextId, true, warnings);
            }
        } else {
            warnings.add(TranslationHandler.translate("!!\"%1\" ist keine gültige Textart für das Feld \"%2\"", importType.getMadId(), tableDotFieldName));
            return false;
        }
        return true;
    }

    /**
     * Aufgrund des importTypes nach einem PRIMUS-Dictionary-Eintrag suchen bzw anlegen und in multiEdit hinterlegen
     * Bei einem Fehler ist hasWarnings() == true
     *
     * @param importType
     * @param multiEdit
     * @param termId
     * @param primusForeignSource
     * @param tableDotFieldName
     * @return
     */
    public void handleDictTextId(DictTextKindPRIMUSTypes importType, iPartsDictTextKindId txtKindId,
                                 EtkMultiSprache multiEdit, String termId,
                                 String primusForeignSource, String tableDotFieldName) {
        warnings.clear();
        //Eintrag gehört zum Lexikon
        String dictTextId = null;
        iPartsDictTextKindId fallbackTxtKindId = null;
        String fallbackDictTextId = null;
        if (termId.isEmpty()) {
            //keine TermId angegeben => Suche nach Text
            String suchText = multiEdit.getText(Language.DE.getCode());
            if (!suchText.trim().isEmpty()) {
                Set<String> textIds = getSearchHelper().findPRIMUSTextIdsInTableSprache(suchText, Language.DE);

                if (!textIds.isEmpty()) {
                    //die richtige TextId im Lexikon finden
                    for (String helperDictTextId : textIds) {
                        iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), null);
                        if (dataDictMeta.loadFromDB((new iPartsDictMetaId(txtKindId.getTextKindId(), helperDictTextId)))) {
                            dictTextId = helperDictTextId;
                            break;
                        }
                    }
                    if (dictTextId == null) {
                        warnings.add(TranslationHandler.translate("!!Keine Text-ID zu \"%1\" gefunden", suchText));
                    }
                } else {
                    dictTextId = DictHelper.buildDictPRIMUSTextId(StrUtils.makeGUID());
                }
            } else {
                //kein Suchtext vorhanden
                //Fehlermeldung
                warnings.add(TranslationHandler.translate("!!Text-ID und Suchtext dürfen nicht leer sein. Textart: %1", importType.getPRIMUSId()));
            }
        } else {
            //dictTextId bilden: bei vorhandener TermId muss im RSK-Lexikon gesucht werden mit Fallback auf PRIMUS
            fallbackTxtKindId = txtKindId;
            fallbackDictTextId = DictHelper.buildDictPRIMUSTextId(termId);
            txtKindId = DictTxtKindIdByMADId.getInstance(getProject()).getRSKTxtKindId(DictTextKindRSKTypes.MAT_AFTER_SALES);
            dictTextId = DictHelper.buildDictRSKTextId(termId);
        }
        if (dictTextId != null) {
            //Schritt 2: in DA_DICT_META nach Eintrag suchen
            createOrUpdateDictMeta(txtKindId, dictTextId, termId, primusForeignSource, multiEdit, tableDotFieldName,
                                   null, null, fallbackTxtKindId, fallbackDictTextId, true, warnings);
        }
    }

    public iPartsDictTextKindId getDictTextKindIdForField(DictTextKindPRIMUSTypes importType, String tableDotFieldName) {
        warnings.clear();
        //Schritt 1: Bestimmung der DictGuid
        iPartsDictTextKindId txtKindId = DictTxtKindIdByMADId.getInstance(getProject()).getPRIMUSTxtKindId(importType, tableDotFieldName);
        //nur etwas tun, falls eine txtKindId vorhanden
        if ((txtKindId == null) || !txtKindId.isValidId()) {
            warnings.add(TranslationHandler.translate("!!\"%1\" ist keine gültige Textart für das Feld \"%2\"", importType.getPRIMUSId(), tableDotFieldName));
        }
        return txtKindId;
    }

    /**
     * Sucht nach EPC Texten mit Hilfe der übergebenen <code>termId</code> und befüllt das übergebene {@link EtkMultiSprache}
     * Objekt mit den gefundenen Texten
     *
     * @param importType
     * @param termId
     * @return
     */
    public EtkMultiSprache searchEPCTextWithEPCId(DictTextKindEPCTypes importType, String termId) {
        if (StrUtils.isValid(termId)) {
            EtkMultiSprache multiLangText = new EtkMultiSprache();
            if (handleDictTextIdForEPC(importType, multiLangText, termId, DictHelper.getEPCForeignSource(), null, false)) {
                if (multiLangText.getTextId().isEmpty()) {
                    return null;
                }
                return multiLangText;
            }
        }
        return null;
    }

    /**
     * Aufgrund des importTypes nach einem EPC-Dictionary-Eintrag suchen bzw. anlegen und in multiEdit hinterlegen
     * Bei einem Fehler ist hasWarnings() == true
     *
     * @param importType
     * @param multiEdit
     * @param termId
     * @param foreignSource
     * @param tableDotFieldName
     * @return
     */
    public boolean handleDictTextIdForEPC(DictTextKindEPCTypes importType, EtkMultiSprache multiEdit, String termId,
                                          String foreignSource, String tableDotFieldName, boolean createNewDictEntry) {
        warnings.clear();
        // Besimmung der Textart-Id
        iPartsDictTextKindId txtKindId = DictTxtKindIdByMADId.getInstance(getProject()).getEPCTxtKindId(importType);
        // nur etwas tun, falls eine txtKindId vorhanden
        if ((txtKindId != null) && txtKindId.isValidId()) {
            String dictTextId = null;
            if (StrUtils.isValid(termId)) {
                // Text ID bilden, sofern eine gültige TermId übergeben wurde
                dictTextId = DictHelper.buildEPCTextId(importType, termId);
            } else {
                // Es wurde keine gültige Term-Id übergeben, suche nach dem deutschen Text
                String germanText = multiEdit.getText(Language.DE.getCode());
                if (!germanText.trim().isEmpty()) {
                    Set<String> textIds = getSearchHelper().findTextIdsInTableSpracheForEPC(germanText, Language.DE, importType);
                    if (!textIds.isEmpty()) {
                        // Durchlaufe alle gefundenen Text-Ids und...
                        for (String helperDictTextId : textIds) {
                            iPartsDictMetaId dictMetaId = new iPartsDictMetaId(txtKindId.getTextKindId(), helperDictTextId);
                            iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
                            if (dataDictMeta.existsInDB()) {
                                // ... nehme die erste, die im Lexikon gefunden wurd
                                dictTextId = helperDictTextId;
                                break;
                            }
                        }
                        if (dictTextId == null) {
                            warnings.add(TranslationHandler.translate("!!Keine Text-ID zu \"%1\" gefunden", germanText));
                        }
                    } else {
                        dictTextId = DictHelper.buildEPCTextId(importType, StrUtils.makeGUID());
                    }
                } else {
                    //kein Suchtext vorhanden
                    //Fehlermeldung
                    warnings.add(TranslationHandler.translate("!!Text-ID und Suchtext dürfen nicht leer sein. Textart: %1", importType.getEpcId()));
                }
            }
            if (dictTextId != null) {
                createOrUpdateDictMeta(txtKindId, dictTextId, termId, foreignSource, multiEdit, tableDotFieldName, createNewDictEntry, warnings);
            }
        } else {
            if (tableDotFieldName == null) {
                warnings.add(TranslationHandler.translate("!!\"%1\" ist keine gültige Textart für die Suche nach EPC Texten mit der ID \"%2\"", importType.getEpcId(), termId));
            } else {
                warnings.add(TranslationHandler.translate("!!\"%1\" ist keine gültige Textart für das Feld \"%2\"", importType.getEpcId(), tableDotFieldName));
            }
            return false;
        }
        return true;
    }

    public boolean handleDictTextIdForEPC(DictTextKindEPCTypes importType, EtkMultiSprache multiEdit, String termId,
                                          String foreignSource, String tableDotFieldName) {
        return handleDictTextIdForEPC(importType, multiEdit, termId, foreignSource, tableDotFieldName, true);
    }

    /**
     * Updated das übergebene {@link EtkMultiSprache} Objekt mit den Texten und der Text-ID aus dem {@link iPartsDataDictMeta}
     * Objekt, das mit den übergebenen Parameter <code>textKindId</code> und <code>dictTextId</code> erzeugt wird
     *
     * @param textKindId
     * @param dictTextId
     * @param multiLang
     */
    private void updateMultiLangFromDictMeta(iPartsDictTextKindId textKindId, String dictTextId, EtkMultiSprache multiLang) {
        iPartsDictMetaId dictMetaId = new iPartsDictMetaId(textKindId.getTextKindId(), dictTextId);
        iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
        if (dataDictMeta.existsInDB()) {
            updateDictMeta(dataDictMeta, multiLang, dictTextId, null, null, null, false);
        }
    }


    /**
     * Löschen eines Lexikon Eintrags
     *
     * @param importType
     * @param tableDotFieldName
     * @param multilangFieldValue
     */
    public void deleteDictEntry(DictTextKindTypes importType, String tableDotFieldName, String multilangFieldValue) {
        //Schritt 1: Bestimmung der DictGuid
        iPartsDictTextKindId txtKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(importType, tableDotFieldName);
        deleteDictEntry(txtKindId, multilangFieldValue);
    }

    public void deleteDictEntry(iPartsDictTextKindId txtKindId, String multilangFieldValue) {
        //Schritt 1: Bestimmung der DictGuid
        if ((txtKindId != null) && txtKindId.isValidId()) {
            iPartsDictMetaId dictMetaId = new iPartsDictMetaId(txtKindId.getTextKindId(), multilangFieldValue);
            iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
            dataDictMeta.deleteFromDB(true);
        }
    }

    private void createOrUpdateDictMeta(iPartsDictTextKindId textKindId, String dictTextId, String madForeignId, String madForeignSource,
                                        EtkMultiSprache multiLang, String tableDotFieldName, boolean createNewDictEntry, List<String> warnings) {
        createOrUpdateDictMeta(textKindId, dictTextId, madForeignId, madForeignSource, multiLang, tableDotFieldName, null,
                               null, null, null, createNewDictEntry, warnings);
    }

    /**
     * Metadaten zu Dictionary-Eintrag (Schlüssel: Textart, Text-ID) erzeugen oder aktualisieren.
     *
     * @param textKindId
     * @param dictTextId
     * @param madForeignId
     * @param madForeignSource
     * @param multiLang
     * @param tableDotFieldName
     * @param eldasID
     * @param dialogID
     * @param fallbackRSKTextKindId
     * @param fallbackRSKDictTextId
     * @param warnings
     */
    private void createOrUpdateDictMeta(iPartsDictTextKindId textKindId, String dictTextId, String madForeignId, String madForeignSource,
                                        EtkMultiSprache multiLang, String tableDotFieldName, String eldasID, String dialogID,
                                        iPartsDictTextKindId fallbackRSKTextKindId, String fallbackRSKDictTextId,
                                        boolean createNewDictEntry, List<String> warnings) {
        //Schritt 2: in DA_DICT_META nach Eintrag suchen
        iPartsDictMetaId dictMetaId = new iPartsDictMetaId(textKindId.getTextKindId(), dictTextId);
        iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
        if (!dataDictMeta.loadFromDB(dictMetaId)) {
            if (createNewDictEntry) {
                if (DictHelper.isDictRSKTextId(dictTextId) || DictHelper.isDictRSKCDTextId(dictTextId)) {
                    // Fallback TextKindId und DictTextId vorhanden, um bei RSK mit diesen Daten einen Lexikon-Eintrag zu erzeugen
                    if ((fallbackRSKTextKindId != null) && !StrUtils.isEmpty(fallbackRSKDictTextId)) {
                        createOrUpdateDictMeta(fallbackRSKTextKindId, fallbackRSKDictTextId, madForeignId, madForeignSource, multiLang,
                                               tableDotFieldName, eldasID, dialogID, null, null, createNewDictEntry, warnings);
                    } else {
                        warnings.add(TranslationHandler.translate("!!Fehlender RSK-Eintrag im Lexikon! Darf nicht neu angelegt werden (%1)", dictTextId));
                    }
                    return;
                }
                //Text-Id in multiEdit eintragen
                multiLang.setTextId(dictTextId);
                //noch kein Lexikoneintrag vorhanden
                initDataDictMeta(dataDictMeta, madForeignId, madForeignSource, multiLang, eldasID, dialogID);
                //neuen Dict-Eintrag speichern
                saveDictMetaEntry(dataDictMeta, tableDotFieldName);
            }
        } else {
            updateDictMeta(dataDictMeta, multiLang, dictTextId, dialogID, eldasID, tableDotFieldName, true);
        }
    }

    private void updateDictMeta(iPartsDataDictMeta dataDictMeta, EtkMultiSprache multiLang, String dictTextId,
                                String dialogID, String eldasID, String tableDotFieldName, boolean createNewMultilang) {
        boolean updateDictMetaObject = false;
        if (!StrUtils.isEmpty(dialogID)) {
            updateDictMetaObject |= addDialogIdToDataObject(dataDictMeta, dialogID);
        }
        if (!StrUtils.isEmpty(eldasID)) {
            updateDictMetaObject |= addELDASIdToDataObject(dataDictMeta, eldasID);
        }
        //DictMeta Eintrag ist vorhanden
        EtkMultiSprache dictMultiEdit = dataDictMeta.getMultiLang();
        if (!dictMultiEdit.getTextId().isEmpty()) {
            //Lexikon-Eintrag in SPRACHE ist vorhanden
            if (!dictMultiEdit.getTextId().equals(multiLang.getTextId())) {
                //TextIds unterscheiden sich
                if (multiLang.getTextId().isEmpty()) {
                    //neuer Eintrag besitzt keine TextId => Lexikon-Eintrag übernehmen
                    multiLang.assign(dictMultiEdit);
                } else {
                    //Fehlermeldung
                    //warnings.add(TranslationHandler.translate("!!Unterschiedliche TextId (Lexikon %1) (Import %2)", dictMultiEdit.getTextId(), multiLang.getTextId()));
                    infos.add(TranslationHandler.translate("!!Unterschiedliche Text-ID (Lexikon %1) (Import %2)", dictMultiEdit.getTextId(), multiLang.getTextId()));
                    //foreignId (dictMultiEdit) gewinnt => Lexikon-Eintrag übernehmen
                    multiLang.assign(dictMultiEdit);
                }
            } else if (DictHelper.isDictEPCTextId(dictMultiEdit.getTextId())) {
                // überprüfen, ob neue/geänderte Texte vom Aufrufer kommen
                // Es werden geänderte Sprach-Texte und neue Sprach-Texte übernommen
                // bereits in der DB vorhandene Sprach-Texte, die nicht vom Aufrufer geliefert werden, bleiben unverändert
                boolean isModified = false;
                List<String> newLanguages = new DwList<>();
                if (multiLang.getSprachen().size() == dictMultiEdit.getSprachen().size()) {
                    // Anzahl Sprachen gleich => überprüfe Texte
                    // (leere Texte vom Aufrufer werden nicht übernommen)
                    for (String lang : dictMultiEdit.getSprachen()) {
                        String newTextValue = multiLang.getText(lang);
                        if (!dictMultiEdit.getText(lang).equals(newTextValue) && !newTextValue.isEmpty()) {
                            dictMultiEdit.setText(lang, newTextValue);
                            isModified = true;
                        }
                    }
                } else {
                    // Anzahl Sprachen ist unterschiedlich => übernehme nur neue Sprachen
                    Set<String> mergedLanguages = new HashSet<>();
                    mergedLanguages.addAll(dictMultiEdit.getSprachen());
                    mergedLanguages.addAll(multiLang.getSprachen());
                    for (String lang : mergedLanguages) {
                        if (dictMultiEdit.spracheExists(lang)) {
                            if (multiLang.spracheExists(lang)) {
                                // in beiden MultiLangs existiert die Sprache lang => Texte vergleichen
                                String newTextValue = multiLang.getText(lang);
                                if (!dictMultiEdit.getText(lang).equals(newTextValue) && !newTextValue.isEmpty()) {
                                    dictMultiEdit.setText(lang, newTextValue);
                                    isModified = true;
                                }
                            } else {
                                // Sprache existiert nur in DB => nichts tun
                            }
                        } else {
                            if (multiLang.spracheExists(lang)) {
                                // Sprache wird vom Aufrufer neu gesetzt => übernehmen, wenn nicht leer
                                String newTextValue = multiLang.getText(lang);
                                if (!newTextValue.isEmpty()) {
                                    // Text für Sprache lang kommt neu hinzu
                                    dictMultiEdit.setText(lang, newTextValue);
                                    newLanguages.add(lang);
                                    isModified = true;
                                }
                            }
                        }
                    }
                }
                if (isModified) {
                    // iPartsDataDictLanguageMeta für neue Sprachen erzeugen
                    for (String lang : newLanguages) {
                        dataDictMeta.addLanguage(lang, DBActionOrigin.FROM_EDIT);
                    }
                    // geändertes MultiLang setzen
                    dataDictMeta.setNewMultiLang(dictMultiEdit);
                    // und speichern
                    getProject().getDbLayer().startTransaction();
                    try {
                        dataDictMeta.saveToDB();
                        getProject().getDbLayer().commit();
                        updateDictMetaObject = false;
                    } catch (Exception e) {
                        getProject().getDbLayer().rollback();
                        warnings.add(e.getMessage());
                        Logger.getLogger().handleRuntimeException(e);
                    }
                }
                // TextIds sind gleich sich => Lexikon-Eintrag übernehmen
                multiLang.assign(dictMultiEdit);
            }
        } else {
            if (createNewMultilang) {
                // DictMeta Eintrag ist vorhanden, aber kein Lexikon-Eintrag in SPRACHE vorhanden => neuen anlegen
                multiLang.setTextId(dictTextId);
                dataDictMeta.setNewMultiLang(multiLang);
                //neuen Dict-Eintrag mit neuem Lexikon-Eintrag speichern
                updateDictMetaObject = true;
            }
        }
        if (updateDictMetaObject && StrUtils.isValid(tableDotFieldName)) {
            saveDictMetaEntry(dataDictMeta, tableDotFieldName);
        }
    }

    /**
     * Fügt dem DataObject eine ELDAS ID hinzu
     *
     * @param dataDictMeta
     * @param eldasID
     */
    private boolean addELDASIdToDataObject(iPartsDataDictMeta dataDictMeta, String eldasID) {
        String eldasIdValue = createNewIDValue(dataDictMeta.getELDASId(), eldasID);
        if (eldasIdValue != null) {
            dataDictMeta.setELDASId(eldasIdValue, DBActionOrigin.FROM_EDIT);
            return true;
        }
        return false;
    }

    /**
     * Fügt dem DataObject eine DIALOG ID hinzu
     *
     * @param dataDictMeta
     * @param dialogID
     */
    private boolean addDialogIdToDataObject(iPartsDataDictMeta dataDictMeta, String dialogID) {
        String dialogIdValue = createNewIDValue(dataDictMeta.getDIALOGId(), dialogID);
        if (dialogIdValue != null) {
            dataDictMeta.setDIALOGId(dialogIdValue, DBActionOrigin.FROM_EDIT);
            return true;
        }
        return false;
    }

    private String createNewIDValue(String currentID, String newID) {
        if (!StrUtils.isEmpty(newID)) {
            if (!StrUtils.isEmpty(currentID)) {
                if (!StrUtils.stringContains(currentID, newID)) {
                    currentID = StrUtils.addCharacterIfLastCharacterIsNot(currentID, ',');
                    return currentID + newID;
                }
            } else {
                return newID;
            }
        }
        return null;
    }

    public static void initDataDictMeta(iPartsDataDictMeta dataDictMeta, String foreignId, String foreignSource,
                                        EtkMultiSprache multiLang, String eldasID, String dialogID) {
        if (!dataDictMeta.existsInDB()) {
            dataDictMeta.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
            String state = DictHelper.getMADDictStatus();
            if (foreignSource.equals(DictHelper.getMADForeignSource())) {
                if (DictTxtKindIdByMADId.getInstance(dataDictMeta.getEtkProject()).isDictMetaIdOfMADTypeOf(dataDictMeta.getAsId(),
                                                                                                           DictTextKindTypes.ADD_TEXT,
                                                                                                           DictTextKindTypes.FOOTNOTE)) {
                    state = iPartsDictConst.DICT_STATUS_CONSOLIDATED;
                }
            }
            dataDictMeta.setState(state, DBActionOrigin.FROM_EDIT);
        }
        dataDictMeta.setForeignId(foreignId, DBActionOrigin.FROM_EDIT);
        dataDictMeta.setSource(foreignSource, DBActionOrigin.FROM_EDIT);
        dataDictMeta.setActCreationDate(DBActionOrigin.FROM_EDIT);
        // Mit Default-User initialisieren
        String userId = DictHelper.getMADUserId();
        // Ggf. den User passend zur Datenquelle überschreiben.
        if (foreignSource.equals(DictHelper.getRSKForeignSource())) {
            userId = DictHelper.getRSKUserId();
        } else if (foreignSource.equals(DictHelper.getPRIMUSForeignSource())) {
            userId = DictHelper.getPRIMUSUserId();
        } else if (foreignSource.equals(DictHelper.getEPCForeignSource())) {
            userId = DictHelper.getEPCUserId();
        } else if (foreignSource.equals(DictHelper.getPROVALForeignSource())) {
            userId = DictHelper.getPROVALUserId();
        } else if (foreignSource.equals(DictHelper.getSAPMBSForeignSource())) {
            userId = DictHelper.getSAPMBSUserId();
        } else if (foreignSource.equals(DictHelper.getPSKForeignSource())) {
            userId = DictHelper.getPSKUserId();
        } else if (foreignSource.equals(DictHelper.getConnectForeignSource())) {
            userId = DictHelper.getConnectUserId();
        } else if (!foreignSource.equals(DictHelper.getMADForeignSource())) {
            userId = iPartsUserAdminDb.getLoginUserName();
        }
        dataDictMeta.setUserId(userId, DBActionOrigin.FROM_EDIT);

        if (!StrUtils.isEmpty(eldasID)) {
            dataDictMeta.setELDASId(eldasID, DBActionOrigin.FROM_EDIT);
        }
        if (!StrUtils.isEmpty(dialogID)) {
            dataDictMeta.setDIALOGId(dialogID, DBActionOrigin.FROM_EDIT);
        }
        if (multiLang != null) {
            dataDictMeta.setNewMultiLang(multiLang);
        }
    }

    public static void initDataDictMeta(iPartsDataDictMeta dataDictMeta, String foreignId, String foreignSource, EtkMultiSprache multiLang) {
        initDataDictMeta(dataDictMeta, foreignId, foreignSource, multiLang, null, null);
    }

    private void saveDictMetaEntry(iPartsDataDictMeta dataDictMeta, String tableDotFieldName) {
        EtkMultiSprache multiLang = getProject().getDbLayer().getLanguagesTextsByTextId(dataDictMeta.getMultiLang().getTextId(), tableDotFieldName);
        saveDictMetaEntry(dataDictMeta, tableDotFieldName, multiLang);
    }

    private void saveDictMetaEntry(iPartsDataDictMeta dataDictMeta, String tableDotFieldName, EtkMultiSprache originalMultiLangFromDB) {
        DBDataObjectAttribute multiLanguageAttribute = new DBDataObjectAttribute(TableAndFieldName.getFieldName(tableDotFieldName),
                                                                                 DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, false);
        multiLanguageAttribute.setValueAsMultiLanguage(dataDictMeta.getMultiLang(), DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
        getProject().getDbLayer().startTransaction();
        try {
            // Die TextID wird auch für die TextNr verwendet, da diese eindeutig ist -> im multiLanguageAttribute muss
            // die TextNr gesetzt werden, damit diese in saveMultiLanguageContentForAttribute() als alte TextNr erkannt wird
            String textId = dataDictMeta.getMultiLang().getTextId();
            multiLanguageAttribute.setTextIdForMultiLanguage(textId, textId, DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);


            // multiLang == null kommt vor, wenn es für tableDotFieldName noch keinen Eintrag in der Sprach-Tabelle für die TextID gibt
            if ((originalMultiLangFromDB == null) || !originalMultiLangFromDB.equalContent(dataDictMeta.getMultiLang())) {
                getProject().getDbLayer().saveMultiLanguageContentForAttribute(multiLanguageAttribute, TableAndFieldName.getTableName(tableDotFieldName),
                                                                               textId, null);
            }
            dataDictMeta.clearMultiLang();
            if (dataDictMeta.isNew()) {
                dataDictMeta.saveToDB(false, DBDataObject.PrimaryKeyExistsInDB.FALSE);
            } else {
                dataDictMeta.saveToDB();
            }
            getProject().getDbLayer().commit();
        } catch (Exception e) {
            getProject().getDbLayer().rollback();
            warnings.add(e.getMessage());
            Logger.getLogger().handleRuntimeException(e);
        }
    }

    /**
     * Sucht nach dem Text in multiLang als RSK Text. Wird kein Eintrag gefunden, dann wird ein neuer Eintrag als Unbekannter
     * Text erzeugt. Dieser neue Text hat als TextID eine GUID und als FremdID die termID die vom Importer übergeben wurde
     *
     * @param multiLang         Enthält den Suchtext und dann auch das übergebene/ erzeugte Ergebnis
     * @param tableDotFieldName
     * @param searchTermId      TermID die vom Importer übergeben wird
     * @return
     */
    public boolean searchTextInRSKWithFallbackOnCreateUndef(EtkMultiSprache multiLang, String tableDotFieldName, String searchTermId, boolean removeLeadingZeros) {
        warnings.clear();
        String dictPrefixRSK = DictHelper.getRSKTextPrefix(DictTextKindRSKTypes.MAT_AFTER_SALES);
        String dictPrefixUndef = iPartsDictPrefixAndSuffix.DICT_INDISTINCT_TEXT_PREFIX.getPrefixValue();

        String termId;
        if (removeLeadingZeros) {
            termId = iPartsTermIdHandler.removeLeadingZerosFromTermId(searchTermId);
        } else {
            termId = searchTermId;
        }

        iPartsDictTextKindId txtKindIdRSK = DictTxtKindIdByMADId.getInstance(getProject()).getRSKTxtKindId(DictTextKindRSKTypes.MAT_AFTER_SALES, tableDotFieldName);
        iPartsDictTextKindId txtKindIdUndef = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(DictTextKindTypes.INDISTINCT_TEXT, tableDotFieldName);

        boolean success = handleDictTextId(DictTextKindRSKTypes.MAT_AFTER_SALES, multiLang, termId, DictHelper.getRSKForeignSource(),
                                           tableDotFieldName, txtKindIdRSK, dictPrefixRSK);
        if (hasWarnings()) {
            success = handleDictTextId(DictTextKindRSKTypes.MAT_AFTER_SALES, multiLang, termId, DictHelper.getRSKForeignSource(), tableDotFieldName, txtKindIdUndef, dictPrefixUndef);
        }
        return success;
    }

    public String getDictTextIdForDialogId(DictTextKindTypes textKindType, String dialogTextId) {
        return getDictImportTextCache().getDictTextIdForDialogId(getProject(), textKindType, dialogTextId, warnings);
    }

    public void clearCache() {
        getDictImportTextCache().clearCaches();
    }

    public String getDictTextIdForEldasId(DictTextKindTypes textKindType, String eldasTextId) {
        return getDictImportTextCache().getDictTextIdForEldasId(getProject(), textKindType, eldasTextId, warnings);
    }

    private DictImportTextCache getDictImportTextCache() {
        if (dictImportTextCache == null) {
            dictImportTextCache = DictImportTextCache.getInstance(getProject());
        }
        return dictImportTextCache;
    }

    private DictTextSearchHelper getSearchHelper() {
        if (dictTextSearchHelper == null) {
            dictTextSearchHelper = new DictTextSearchHelper(getProject());
        }
        return dictTextSearchHelper;
    }

    public void cancelRunningSearch() {
        if (dictTextSearchHelper != null) {
            dictTextSearchHelper.cancelSearch();
        }
    }

    public void clearWarningsAndInfos() {
        warnings.clear();
        infos.clear();
    }
}