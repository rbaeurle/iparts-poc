/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary;

import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EnumRComboBox;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.common.EtkDataTextEntry;
import de.docware.apps.etk.base.project.common.EtkDataTextEntryList;
import de.docware.apps.etk.base.project.mechanic.ids.TextEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.author.iPartsDataAuthorOrder;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.CongreeWebInterfaceHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditFormComboboxHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectList;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.controls.misc.DWFontStyle;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.misc.Constants;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.output.j2ee.dwr.DocwareDwrLogger;
import de.docware.framework.modules.gui.responsive.base.theme.ThemeManager;
import de.docware.framework.modules.gui.responsive.components.combobox.RComboBox;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;
import java.util.stream.Collectors;

public class DictCreateTextIdForm extends AbstractJavaViewerForm implements iPartsDictConst {

    private static boolean EDIT_TEXTID_ENABLED = false;

    private enum DOUBLE_TEXT_TYPE {
        EQUAL,
        SIMILAR
    }

    public static iPartsDictMetaId createNewTextId(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                   Set<DictTextKindTypes> allowedTextKindTypes, iPartsDictTextKindId textKindId) {
        return createNewTextId(dataConnector, parentForm, allowedTextKindTypes, textKindId, null);
    }

    public static iPartsDictMetaId createNewTextId(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                   Set<DictTextKindTypes> allowedTextKindTypes, iPartsDictTextKindId textKindId,
                                                   String foreignSource) {
        return createNewTextId(dataConnector, parentForm, allowedTextKindTypes, textKindId, foreignSource, null);
    }

    public static iPartsDictMetaId createNewTextId(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                   Set<DictTextKindTypes> allowedTextKindTypes, iPartsDictTextKindId textKindId,
                                                   String foreignSource, List<String> allowedEditLangs) {
        DictCreateTextIdForm dlg = new DictCreateTextIdForm(dataConnector, parentForm, textKindId,
                                                            allowedTextKindTypes, null, true, true, true);
        dlg.setSelectedForeignSource(foreignSource);
        dlg.setAllowedEditLangs(allowedEditLangs);
        dlg.setTitle("!!Text anlegen");
        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getSavedDictMetaId();
        }
        return null;
    }

    public static iPartsDictMetaId editNewTextId(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                 Set<DictTextKindTypes> allowedTextKindTypes, Set<DictTextKindTypes> allowedTextKindTypesForEditMigration, iPartsDictTextKindId textKindId,
                                                 iPartsDataDictMeta dataDictMeta, boolean editAllowed,
                                                 boolean statusChangeAllowed) {
        DictCreateTextIdForm dlg = new DictCreateTextIdForm(dataConnector, parentForm, textKindId,
                                                            allowedTextKindTypes, allowedTextKindTypesForEditMigration, dataDictMeta, false,
                                                            editAllowed, statusChangeAllowed);
        dlg.setTitle(editAllowed ? "!!Text bearbeiten" : "!!Text anzeigen");
        if ((dlg.showModal() == ModalResult.OK) && editAllowed) {
            return dlg.getSavedDictMetaId();
        }
        return null;
    }

    public static iPartsDataDictMeta editNewTextIdForTransJob(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                              iPartsDictMetaId currentDictMetaId) {
        EtkProject project = dataConnector.getProject();
        Set<DictTextKindTypes> allowedTextKindTypes = Arrays.stream(DictTextKindTypes.values()).collect(Collectors.toSet());
        Set<DictTextKindTypes> allowedTextKindTypesForEditMigration = DictTextKindTypes.getEditMigrationTypes();

        DictTextKindTypes dictTextKindType = null;
        iPartsDictTextKindId textKindId = new iPartsDictTextKindId(currentDictMetaId.getTextKindId());
        for (DictTextKindTypes dictTextKindId : DictTextKindTypes.values()) {
            if (dictTextKindId.getTextKindName().equals(textKindId.getDescription())) {
                dictTextKindType = dictTextKindId;
                break;
            }
        }
        if (dictTextKindType != null) {
            allowedTextKindTypesForEditMigration.add(dictTextKindType);
        }

        iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(project, currentDictMetaId);
        if (dataDictMeta.existsInDB()) {
            DictCreateTextIdForm dlg = new DictCreateTextIdForm(dataConnector, parentForm, textKindId,
                                                                allowedTextKindTypes, allowedTextKindTypesForEditMigration, dataDictMeta, false,
                                                                true, true);
            dlg.setTitle("!!Text für Übersetzungszyklus bearbeiten");
            if ((dlg.showModal() == ModalResult.OK)) {
                return dlg.getSavedDataDictMeta();
            }
        }
        return null;
    }

    private RComboBox<iPartsDataDictTextKind> comboboxTextKind;
    private EnumRComboBox enumComboBoxState;
    private EnumRComboBox enumComboBoxForeignSource;
    private RComboBox comboboxUser;
    private iPartsDictTextKindId textKindId;
    protected iPartsDataDictMeta dataDictMeta;
    protected boolean isCreate;
    protected boolean checkExistence;
    private boolean isReadOnly;
    private iPartsDictMetaId savedDictMetaId = null;
    private iPartsDataDictMeta savedDataDictMeta = null;
    private boolean isMigrationText;
    private boolean editForMigrationAllowed;
    private boolean isSpecialHandlingForNeutText;
    private iPartsDictTextKindId footNoteTextKindId;
    private List<iPartsDictTextKindId> editForMigrationAllowedTextKindIds;
    private List<iPartsDictTextKindId> neutralTextKindIds;
    private final boolean carAndVanInSession = iPartsRight.checkCarAndVanInSession();
    private final boolean truckAndBusInSession = iPartsRight.checkTruckAndBusInSession();

    /**
     * Erzeugt eine Instanz von DictCreateTextId.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public DictCreateTextIdForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                iPartsDictTextKindId textKindId, Set<DictTextKindTypes> allowedTextKindTypes, iPartsDataDictMeta dataDictMeta,
                                boolean isCreate, boolean editAllowed, boolean statusChangeAllowed) {
        this(dataConnector, parentForm, textKindId, allowedTextKindTypes, null, dataDictMeta,
             isCreate, editAllowed, statusChangeAllowed);
    }

    public DictCreateTextIdForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                iPartsDictTextKindId textKindId, Set<DictTextKindTypes> allowedTextKindTypes,
                                Set<DictTextKindTypes> allowedTextKindTypesForEditMigration, iPartsDataDictMeta dataDictMeta,
                                boolean isCreate, boolean editAllowed, boolean statusChangeAllowed) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        this.isCreate = isCreate;
        this.isReadOnly = !editAllowed;
        this.textKindId = textKindId;
        this.dataDictMeta = dataDictMeta;
        this.isMigrationText = false;
        this.editForMigrationAllowed = false;
        this.isSpecialHandlingForNeutText = false;
        this.checkExistence = true;
        editForMigrationAllowedTextKindIds = new DwList<>();
        neutralTextKindIds = new DwList<>();
        postCreateGui(allowedTextKindTypes, allowedTextKindTypesForEditMigration, statusChangeAllowed);
        doEnableButtons();
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui(Set<DictTextKindTypes> allowedTextKindTypes, Set<DictTextKindTypes> allowedTextKindTypesForEditMigration, boolean statusChangeAllowed) {
        comboboxTextKind = RComboBox.replaceGuiComboBox(mainWindow.comboboxTextKind);
        comboboxUser = RComboBox.replaceGuiComboBox(mainWindow.comboboxUser);
        if (isCreate) {
            mainWindow.labelCreationDate.setVisible(false);
            mainWindow.datetimeeditpanelCreationDate.setVisible(false);

            mainWindow.labelChangeDate.setVisible(false);
            mainWindow.datetimeeditpanelChangeDate.setVisible(false);

            mainWindow.labelForeignId.setVisible(false);
            mainWindow.textfieldForeignId.setVisible(false);
        } else {
            if (dataDictMeta != null) {
                if (dataDictMeta.getChangeDate().isEmpty()) {
                    mainWindow.labelChangeDate.setVisible(false);
                    mainWindow.datetimeeditpanelChangeDate.setVisible(false);
                }
            }
        }
        if (mainWindow.datetimeeditpanelCreationDate.isVisible()) {
            mainWindow.datetimeeditpanelCreationDate.setEnabled(false);
        }
        if (mainWindow.datetimeeditpanelChangeDate.isVisible()) {
            mainWindow.datetimeeditpanelChangeDate.setEnabled(false);
        }

        mainWindow.buttonpanel.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, ModalResult.NONE);
        if (isReadOnly) {
            mainWindow.buttonpanel.setDialogStyle(GuiButtonPanel.DialogStyle.INFO);
        } else {
            mainWindow.buttonpanel.getButtonOnPanel(GuiButtonOnPanel.ButtonType.OK).setText("!!Speichern");
        }

        EtkMultiSprache multiEdit = new EtkMultiSprache();
        multiEdit.completeWithLanguages(getProject().getConfig().getDatabaseLanguages());
        mainWindow.multilangeditAndTextId.setStartLanguage(getProject().getDBLanguage());
        mainWindow.multilangeditAndTextId.setMultiLanguage(multiEdit);
        mainWindow.multilangeditAndTextId.setWithButton(true);
        mainWindow.multilangeditAndTextId.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                doEditTextId(event);
            }
        });
        mainWindow.multilangeditAndTextId.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                doEnableButtons();
            }
        });
        //Status ComboBox austauschen
        enumComboBoxState = EnumRComboBox.replaceEnumComboBox(EditFormComboboxHelper.replaceComboBoxByEnum(mainWindow.comboboxState,
                                                                                                           mainWindow.panelTextObject,
                                                                                                           null, getProject(),
                                                                                                           iPartsConst.TABLE_DA_DICT_META,
                                                                                                           iPartsConst.FIELD_DA_DICT_META_STATE,
                                                                                                           true));
        enumComboBoxState.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                doEnableButtons();
            }
        });
        //Foreign Source ComboBox austauschen
        enumComboBoxForeignSource = EnumRComboBox.replaceEnumComboBox(EditFormComboboxHelper.replaceComboBoxByEnum(mainWindow.comboboxForeignSource,
                                                                                                                   mainWindow.panelTextObject,
                                                                                                                   null, getProject(),
                                                                                                                   iPartsConst.TABLE_DA_DICT_META,
                                                                                                                   iPartsConst.FIELD_DA_DICT_META_SOURCE,
                                                                                                                   true));
        mainWindow.labelTextAndTextId.setFontStyle(DWFontStyle.BOLD);
        //mainWindow.labelState.setFontStyle(DWFontStyle.BOLD);
        //mainWindow.labelUser.setFontStyle(DWFontStyle.BOLD);
        footNoteTextKindId = null;
        if ((allowedTextKindTypes != null) && allowedTextKindTypes.contains(DictTextKindTypes.FOOTNOTE)) {
            footNoteTextKindId = DictTxtKindIdByMADId.getInstance(getProject()).getTxtKindId(DictTextKindTypes.FOOTNOTE);
        }
        comboboxTextKind.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                iPartsDataDictTextKind textKind = getSelectedTextKind();
                boolean wasSpecialHandlingForNeutText = isSpecialHandlingForNeutText;
                isSpecialHandlingForNeutText = (textKind != null) && neutralTextKindIds.contains(textKind.getAsId());
                if (isSpecialHandlingForNeutText != wasSpecialHandlingForNeutText) {
                    List<String> editLangs = new ArrayList<>();
                    EtkMultiSprache multiLang = mainWindow.multilangeditAndTextId.getMultiLanguage();
                    if (isSpecialHandlingForNeutText) {
                        editLangs.add(Language.DE.getCode());
                        String textDE = multiLang.getText(Language.DE.getCode());
                        multiLang = new EtkMultiSprache();
                        multiLang.setText(Language.DE, textDE);
                    } else {
                        editLangs.addAll(getProject().getConfig().getDatabaseLanguages());
                        for (String editLang : editLangs) {
                            if (!multiLang.spracheExists(editLang)) {
                                multiLang.setText(editLang, "");
                            }
                        }
                    }
                    mainWindow.multilangeditAndTextId.setAllowedEditLangs(editLangs);
                    mainWindow.multilangeditAndTextId.setMultiLanguage(multiLang);
                    if (isSpecialHandlingForNeutText) {
                        mainWindow.multilangeditAndTextId.setStartLanguage(Language.DE);
                    }
                }
                if (isCreate) {
                    // TextControl leeren -> dadurch sollten auch die alten Prüfergebnisse in der Congree Sidebar verschwinden
                    // Klappt aber effektiv erst dann, wenn weiter unten nochmal explizit ein onChange-Event gefeuert wird
                    mainWindow.multilangeditAndTextId.clearTexts();

                    boolean isMultiLineEdit = false;
                    if ((textKind != null) && (footNoteTextKindId != null) && footNoteTextKindId.equals(textKind.getAsId())) {
                        isMultiLineEdit = true;
                    }
                    if (mainWindow.multilangeditAndTextId.isMultiLine() != isMultiLineEdit) {
                        int previousHeight = mainWindow.multilangeditAndTextId.getPreferredHeight();
                        mainWindow.multilangeditAndTextId.setMultiLine(isMultiLineEdit);
                        int currentHeight = mainWindow.multilangeditAndTextId.getPreferredHeight();
                        mainWindow.setHeight(mainWindow.getHeight() + (currentHeight - previousHeight));
                    }

                    // Das TextControl fokussieren und warum auch immer nochmal explizit einen onChange-Event auslösen,
                    // damit die Congree Sidebar die alten Prüfergebnisse entfernt
                    mainWindow.multilangeditAndTextId.getTextControl().requestFocus();
                    Session.startChildThreadInSession(thread -> {
                        DocwareDwrLogger guiLogger = mainWindow.multilangeditAndTextId.getGuiLogger();
                        if (guiLogger != null) {
                            guiLogger.addAjaxCommand_evaluateJavascript("try { var multiLangControl = $('"
                                                                        + mainWindow.multilangeditAndTextId.getTextControl().getUniqueId()
                                                                        + "'); if ('createEvent' in document) {" +
                                                                        "    var evt = document.createEvent('HTMLEvents');" +
                                                                        "    evt.initEvent('change', false, true);" +
                                                                        "    multiLangControl.dispatchEvent(evt);" +
                                                                        "} else {" +
                                                                        "    multiLangControl.fireEvent('onchange');" +
                                                                        "} } catch (e) {}", false);
                        }
                    });
                } else {
                    editForMigrationAllowed = (textKind != null) && editForMigrationAllowedTextKindIds.contains(textKind.getAsId());
                }
                doEnableButtons();
            }
        });
        fillTextKinds(allowedTextKindTypes, allowedTextKindTypesForEditMigration, textKindId);
        fillUsers();
        if (!(statusChangeAllowed && !isCreate && !isReadOnly)) {
            editForMigrationAllowed = false;
        }
        fillFields(statusChangeAllowed, (allowedTextKindTypes != null) && (allowedTextKindTypes.size() == 1), editForMigrationAllowed);

        // Congree Web Interface Integration für multilangeditAndTextId
        CongreeWebInterfaceHelper.enableCongreeSidebarForMultiLangEdit(mainWindow.multilangeditAndTextId, getProject());

        mainWindow.multilangeditAndTextId.getTextControl().requestFocus();

        int width = mainWindow.getWidth(); // Breite merken und die Höhe automatisch durch pack() bestimmen lasssen
        ThemeManager.get().render(mainWindow);
        mainWindow.pack();
        mainWindow.setWidth(width);
    }

    @Override
    public AbstractGuiControl getGui() {
        return mainWindow.panelMain;
    }

    public ModalResult showModal() {
        ModalResult modalResult = mainWindow.showModal();
        super.close();
        return modalResult;
    }

    @Override
    public void close() {
        mainWindow.setVisible(false);
        super.close();
    }

    public void setTitle(String title) {
        mainWindow.title.setTitle(title);
    }

    public void setSubTitle(String subTitle) {
        mainWindow.title.setSubtitle(subTitle);
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setSelectedForeignSource(String foreignSource) {
        if (StrUtils.isValid(foreignSource)) {
            enumComboBoxForeignSource.setActToken(foreignSource);
        }
    }

    public void setAllowedEditLangs(List<String> allowedEditLangs) {
        mainWindow.multilangeditAndTextId.setAllowedEditLangs(allowedEditLangs);
    }

    public List<String> getAllowedEditLangs() {
        return mainWindow.multilangeditAndTextId.getAllowedEditLangs();
    }

    public iPartsDictMetaId getSavedDictMetaId() {
        return savedDictMetaId;
    }

    public iPartsDataDictMeta getSavedDataDictMeta() {
        return savedDataDictMeta;
    }

    public String getSelectedTextKindText(String language) {
        iPartsDataDictTextKind dataDictTextKind = getSelectedTextKind();
        if (dataDictTextKind != null) {
            return dataDictTextKind.getName(language);
        }
        return "";
    }

    protected iPartsDataDictTextKind getSelectedTextKind() {
        return comboboxTextKind.getSelectedUserObject();
    }

    protected void setSavedDataDictMeta(iPartsDataDictMeta dataDictMeta) {
        savedDataDictMeta = dataDictMeta;
        savedDictMetaId = dataDictMeta.getAsId();
    }

    protected String getEditTextId() {
        if (mainWindow.multilangeditAndTextId.getMultiLanguage().getTextId().isEmpty()) {
            EtkMultiSprache multi = mainWindow.multilangeditAndTextId.getMultiLanguage();
            multi.setTextId(DictHelper.buildIPARTSDictTextId());
            mainWindow.multilangeditAndTextId.setMultiLanguage(multi);
        }
        return mainWindow.multilangeditAndTextId.getMultiLanguage().getTextId();
    }

    protected boolean addWarning(List<String> warnings, String key, String... placeHolderTexts) {
        if (warnings != null) {
            warnings.add(TranslationHandler.translate(key, placeHolderTexts));
        }
        return false;
    }

    protected boolean checkData(List<String> warnings) {
        if (warnings != null) {
            warnings.clear();
        }
        boolean result = true;
        if (!isReadOnly) {
            //Textart
            if (comboboxTextKind.getSelectedIndex() == -1) {
                result = addWarning(warnings, "!!Textart auswählen");
            }
            if (warnings.isEmpty()) {
                //Text und TextId
                if (mainWindow.multilangeditAndTextId.getMultiLanguage().allStringsAreEmpty()) {
                    result = addWarning(warnings, "!!Text eingeben");
                }
                String textID = getEditTextId();
                if (textID.isEmpty()) {
                    result = addWarning(warnings, "!!Text-Id eingeben");
                } else if (textID.trim().isEmpty() || DictHelper.getDictId(textID).trim().isEmpty()) {
                    result = addWarning(warnings, "!!Text-Id darf nicht nur aus Leerzeichen bestehen!");
                }
            }
            if (result) {
                //Status
                if (enumComboBoxState.getSelectedIndex() <= 0) {
                    result = addWarning(warnings, "!!Status wählen");
                }
                //User
                if (comboboxUser.getSelectedIndex() == -1) {
                    result = addWarning(warnings, "!!Benutzer wählen");
                }
            }

            if (result) {
                //in Tabelle SPRACHE überprüfen
                EtkMultiSprache testMulti = getDbLayer().getLanguagesTextsByTextId(getEditTextId());
                if (isCreate) {
                    if ((testMulti != null) && !testMulti.allStringsAreEmpty()) {
                        result = addWarning(warnings, "!!Text-Id \"%1\" existiert bereits", getEditTextId());
                    }
                } else {
                    if ((testMulti == null) || testMulti.allStringsAreEmpty()) {
                        result = addWarning(warnings, "!!Text-Id \"%1\" existiert nicht", getEditTextId());
                    }
                }
            }

            if (result) {
                //User-ID überprüfen
                if (!isMigrationText) {
                    if (comboboxUser.getSelectedIndex() != -1) {
                        String userId = comboboxUser.getSelectedItem();
                        if (userId.equals(DictHelper.getMADUserId()) ||
                            userId.equals(DictHelper.getRSKUserId())) {
                            result = addWarning(warnings, "!!Der Benutzer \"%1\" ist für die Migration reserviert.", userId);
                        }
                    }
                } else {
                    //Test des Status bei isMigrationText
                    String token = enumComboBoxState.getActToken();
                    if (token.isEmpty() || token.equals(DICT_STATUS_NEW)) {
                        token = enumComboBoxState.getItem(enumComboBoxState.getSelectedIndex());
                        result = addWarning(warnings, "!!Der Status \"%1\" ist bei Migrationstexten nicht erlaubt.", token);
                    }
                }
            }
            if (result) {
                EtkMultiSprache multi = mainWindow.multilangeditAndTextId.getMultiLanguage();
                multi.removeLanguagesWithEmptyTexts();
                // Es muss mind. eine der Übersetzungsausgangssprachen vorkommen
                if (iPartsDictTransJobHelper.findSourceLang(multi) == null) {
                    String languages = iPartsDictTransJobHelper.getSourceLanguagesAsString();
                    result = addWarning(warnings, "!!Es muss eine der folgenden Sprachen ausgefüllt sein: %1", languages);
                }
            }
            if (result && mainWindow.multilangeditAndTextId.isMultiLine()) {
                // CRLF am Ende entfernen
                EtkMultiSprache multi = mainWindow.multilangeditAndTextId.getMultiLanguage();
                for (Language lang : multi.getLanguages()) {
                    multi.setText(lang, StrUtils.removeAllLastCharacterIfCharacterIs(multi.getText(lang.getCode()), "\n"));
                }
                mainWindow.multilangeditAndTextId.setMultiLanguage(multi);
            }
            // neue protected Variable checkExists, da checkData() überschrieben
            // Bei Neuanlage soll kontrolliert werden, ob zu dem Text, der Textart, der Company, der Sprache und der Quelle bereits ein Text besteht, der
            // freigegeben ist
            // Wenn ja soll im Lexikon ein Hinweis ausgegeben werden und der neue Text nicht angelegt werden
            // Im Tu bearbeiten soll ein Hinweis ausgegeben werden und der Anwender soll gefragt werden, ob er stattdessen den bestehenden Text
            // verwenden will
            // Achtung !! Warnings wird hier nicht gefüllt, da gleich eine eigene Warnung ausgegeben wird
            if (result && isCreate && checkExistence) {
                boolean inDictionary = parentForm instanceof DictShowTextKindForm;
                // Suche in Sprache nach Text => textId und dann diese Abfrage
                boolean searchCaseInsensitive = false;
                Map<String, Map<DOUBLE_TEXT_TYPE, List<iPartsDataDictMeta>>> resultMap = handleDoubleDictionaryText(searchCaseInsensitive);
                if (!resultMap.isEmpty()) {
                    iPartsDataDictMeta doubleDictMeta = showAndHandleDoubles(resultMap, inDictionary, searchCaseInsensitive);
                    if (doubleDictMeta != null) {
                        // Als Verwendung zur Verfügung stellen
                        setSavedDataDictMeta(doubleDictMeta);
                        mainWindow.setVisible(false);
                        mainWindow.setModalResult(ModalResult.OK);
                    }
                    result = false;
                } else {
                    // nur zum Testen!!
//                        addWarning(warnings, "!!nicht gefunden!");
//                        result = false;
                }
            }
        }
        return result;
    }

    /**
     * Überprüfung, Anzeige, falls Text bereits im Lexikon vorhanden, Vorbereitung für Weitergabe
     *
     * @param resultMap
     * @param inDictionary
     * @param searchCaseInsensitive
     * @return
     */
    private iPartsDataDictMeta showAndHandleDoubles(Map<String, Map<DOUBLE_TEXT_TYPE, List<iPartsDataDictMeta>>> resultMap,
                                                    boolean inDictionary, boolean searchCaseInsensitive) {
        // falls später gewünscht, können die SIMILAR Einträge mit ausgewertet werden
        StringBuilder messages = new StringBuilder();
        for (String language : resultMap.keySet()) {
            iPartsDataDictMeta preSelectDictMeta = null;
            Map<DOUBLE_TEXT_TYPE, List<iPartsDataDictMeta>> equalMap = resultMap.get(language);
            List<iPartsDataDictMeta> metaList = equalMap.get(DOUBLE_TEXT_TYPE.EQUAL);
            if (!metaList.isEmpty()) {
                if (preSelectDictMeta == null) {
                    preSelectDictMeta = metaList.get(0);
                    messages.append(TranslationHandler.translate("!!Der Text in der Textart \"%1\" (Sprache %2) existiert bereits!",
                                                                 getSelectedTextKindText(getProject().getViewerLanguage()), language));
                    if (Constants.DEVELOPMENT && !Constants.DEVELOPMENT_QFTEST) {
                        messages.append("\n");
                        messages.append("TextID: ");
                        messages.append(preSelectDictMeta.getAsId().getTextId());
                    }
                }
                if (metaList.size() > 1) {
                    messages.append("\n");
                    String key = "!!Zusätzlich gibt es noch einen weiteren Lexikon-Text!";
                    if (metaList.size() > 2) {
                        key = "!!Zusätzlich gibt es noch weitere Lexikon-Texte!";
                    }
                    messages.append(TranslationHandler.translate(key));
                }
            }
            if (preSelectDictMeta != null) {
                if (!inDictionary) {
                    messages.append("\n\n");
                    messages.append(TranslationHandler.translate("!!Wollen Sie den bestehenden Lexikon-Text verwenden?"));
                    if (MessageDialog.showYesNo(messages.toString()) == ModalResult.YES) {
                        preSelectDictMeta.removeForeignTablesAttributes();
                        return preSelectDictMeta;
                    }
                } else {
                    // Im Lexikon einfach nur en Hinweis auf doppelte Texte augeben
                    MessageDialog.showWarning(messages.toString());
                    return null;
                }
                messages = new StringBuilder();
            }
        }
        return null;
    }

    private Map<String, Map<DOUBLE_TEXT_TYPE, List<iPartsDataDictMeta>>> handleDoubleDictionaryText(boolean searchCaseInsensitive) {
        Map<String, Map<DOUBLE_TEXT_TYPE, List<iPartsDataDictMeta>>> resultMap = new HashMap<>();
        EtkMultiSprache multi = mainWindow.multilangeditAndTextId.getMultiLanguage();
        List<String> languages = getLanguagesForSearchText(multi);
        // Alle gefüllten Sprachen im Formular auf doppelte Texte prüfen in der Reihenfolge aus der Combobox
        for (String language : languages) {
            String searchText = multi.getText(language);
            iPartsDataDictMetaList list = searchDoubleDictionaryText(textKindId, searchText, language, searchCaseInsensitive);
            if (!list.isEmpty()) {
                if (searchCaseInsensitive) {
                    for (iPartsDataDictMeta dictMeta : list) {
                        DOUBLE_TEXT_TYPE type;
                        if (searchText.equals(dictMeta.getMultiLang().getText(language))) {
                            type = DOUBLE_TEXT_TYPE.EQUAL;
                        } else {
                            type = DOUBLE_TEXT_TYPE.SIMILAR;
                        }
                        List<iPartsDataDictMeta> metaList = getMetaListFromMap(language, type, resultMap);
                        metaList.add(dictMeta);
                    }
                } else {
                    List<iPartsDataDictMeta> metaList = getMetaListFromMap(language, DOUBLE_TEXT_TYPE.EQUAL, resultMap);
                    metaList.addAll(list.getAsList());
                }
            }
        }
        return resultMap;
    }

    private List<iPartsDataDictMeta> getMetaListFromMap(String language, DOUBLE_TEXT_TYPE type, Map<String, Map<DOUBLE_TEXT_TYPE, List<iPartsDataDictMeta>>> resultMap) {
        Map<DOUBLE_TEXT_TYPE, List<iPartsDataDictMeta>> equalMap = resultMap.computeIfAbsent(language, key -> new HashMap<>());
        List<iPartsDataDictMeta> metaList = equalMap.computeIfAbsent(type, key -> new DwList<>());
        return metaList;
    }

    /**
     * Alle Sprachen die in dem Objekt gefüllt sind suchen
     *
     * @param multiLang das Objekt
     * @return die Sprachen
     */
    private List<String> getLanguagesForSearchText(EtkMultiSprache multiLang) {
        multiLang.removeLanguagesWithEmptyTexts();
        List<String> usedEditLangs = new DwList<>(multiLang.getSprachen());
        if (usedEditLangs.isEmpty()) {
            return usedEditLangs;
        }
        List<String> allowedEditLangs = getAllowedEditLangs();
        if (allowedEditLangs == null) {
            allowedEditLangs = new DwList<>();
            for (Language lang : iPartsDictTransJobHelper.getSourceLanguages()) {
                allowedEditLangs.add(lang.getCode());
            }
        }
        Iterator<String> iter = usedEditLangs.iterator();
        while (iter.hasNext()) {
            if (!allowedEditLangs.contains(iter.next())) {
                iter.remove();
            }
        }
        return usedEditLangs;
    }


    /**
     * Suche nach Texten, die im Lexikon schon bestehen
     * Schlüssel sind Textart, Text, Sprache und Status freigegeben
     *
     * @param txtKindId             Textart
     * @param text                  Text
     * @param language              Sprache
     * @param searchCaseInsensitive Suche CaseSensitive oder nicht
     * @return Gefundene schon vorhandene Texte
     */
    public iPartsDataDictMetaList searchDoubleDictionaryText(iPartsDictTextKindId txtKindId, String text,
                                                             String language, boolean searchCaseInsensitive) {
        iPartsDataDictMetaList list = new iPartsDataDictMetaList();
        if ((txtKindId != null) && txtKindId.isValidId()) {
            String[] whereTableAndFields = new String[]{ TableAndFieldName.make(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_TXTKIND_ID),
                                                         TableAndFieldName.make(iPartsConst.TABLE_DA_DICT_META, iPartsConst.FIELD_DA_DICT_META_STATE),
                                                         TableAndFieldName.make(iPartsConst.TABLE_SPRACHE, iPartsConst.FIELD_S_BENENN),
                                                         TableAndFieldName.make(iPartsConst.TABLE_SPRACHE, iPartsConst.FIELD_S_SPRACH) };
            String[] whereValues = new String[]{ txtKindId.getTextKindId(),
                                                 iPartsDictConst.DICT_STATUS_RELEASED,
                                                 text,
                                                 language };
            EtkDisplayFields selectFields = null;

            // da in der Sprache alle S_FELD gefunden werden, wird nur das erste dataDictMeta übernommen
            Set<iPartsDictMetaId> usedMetaId = new HashSet<>();
            EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = new EtkDataObjectList.FoundAttributesCallback() {
                @Override
                public boolean foundAttributes(DBDataObjectAttributes attributes) {

                    // Sichtbarkeiten der Texte prüfen
                    String textId = attributes.getFieldValue(iPartsConst.FIELD_DA_DICT_META_TEXTID);
                    iPartsDictMetaId metaId = new iPartsDictMetaId(attributes.getFieldValue(iPartsConst.FIELD_DA_DICT_META_TXTKIND_ID), textId);
                    if (usedMetaId.contains(metaId)) {
                        // gleiche DictMetaId (nur S_FELD unterschiedlich) => ignorieren
                        return false;
                    }
                    String source = attributes.getFieldValue(iPartsConst.FIELD_DA_DICT_META_SOURCE);
                    DictTextCache.DictTextCacheEntry dictTextCacheEntry = new DictTextCache.DictTextCacheEntry(textId, source);
                    if (dictTextCacheEntry.checkVisibilityInSession(carAndVanInSession, truckAndBusInSession)) {
                        usedMetaId.add(metaId);
                        return true;
                    }
                    return false;
                }
            };
            EtkDataObjectList.JoinData joinData = new EtkDataObjectList.JoinData(iPartsConst.TABLE_SPRACHE,
                                                                                 new String[]{ TableAndFieldName.make(iPartsConst.TABLE_DA_DICT_META,
                                                                                                                      iPartsConst.FIELD_DA_DICT_META_TEXTID) },
                                                                                 new String[]{ iPartsConst.FIELD_S_TEXTID },
                                                                                 false, false);

            list.searchSortAndFillWithJoin(getProject(), null, selectFields, whereTableAndFields, whereValues,
                                           false, null, searchCaseInsensitive,
                                           false, foundAttributesCallback, joinData);
        }
        return list;
    }


    private void fillTextKinds(Set<DictTextKindTypes> allowedTextKindTypesList, Set<DictTextKindTypes> allowedTextKindTypesForEditMigration, iPartsDictTextKindId textKindId) {
        if (allowedTextKindTypesForEditMigration == null) {
            allowedTextKindTypesForEditMigration = new HashSet<>();
        }
        Set<DictTextKindTypes> isNeutralTextKindList = DictTextKindTypes.getNeutTextTypes();
        editForMigrationAllowedTextKindIds.clear();
        neutralTextKindIds.clear();

        comboboxTextKind.switchOffEventListeners();
        comboboxTextKind.removeAllItems();
        if ((allowedTextKindTypesList != null) && (allowedTextKindTypesList.size() > 1)) {
            comboboxTextKind.addItem(null, "");
        }
        TreeMap<String, iPartsDataDictTextKind> textKinds = iPartsDataDictTextKindList.loadAllTextKindListSortedByName(getProject());

        for (Map.Entry<String, iPartsDataDictTextKind> textKind : textKinds.entrySet()) {
            // was soll in die ComboBox?
            if (isAllowedTextKindToAdd(textKind, allowedTextKindTypesList, textKindId)) {
                comboboxTextKind.addItem(textKind.getValue(), textKind.getKey());
            }
            // Ist Edit trotz Migration erlaubt?
            if (isAllowedTextKindToAdd(textKind, allowedTextKindTypesForEditMigration, textKindId)) {
                editForMigrationAllowedTextKindIds.add(textKind.getValue().getAsId());
            }
            // handelt es sich um einen Neutral-Text?
            if (isAllowedTextKindToAdd(textKind, isNeutralTextKindList, textKindId)) {
                neutralTextKindIds.add(textKind.getValue().getAsId());
            }
        }
        comboboxTextKind.setSelectedIndex(-1); // Sonst löst der Aufruf von setSelectedUserObject() weiter unten u.U. keinen OnChangeEvent aus
        comboboxTextKind.switchOnEventListeners();
        if ((textKindId != null) && textKindId.isValidId()) {
            for (iPartsDataDictTextKind dataDictTextKind : comboboxTextKind.getUserObjects()) {
                if (dataDictTextKind != null) {
                    if (dataDictTextKind.getAsId().equals(textKindId)) {
                        comboboxTextKind.setSelectedUserObject(dataDictTextKind);
                        break;
                    }
                }
            }
        }
    }

    protected boolean isAllowedTextKindToAdd(Map.Entry<String, iPartsDataDictTextKind> textKind,
                                             Set<DictTextKindTypes> allowedTextKindTypesList, iPartsDictTextKindId textKindId) {
        if (allowedTextKindTypesList == null) {
            return true;
        }
        return allowedTextKindTypesList.contains(textKind.getValue().getForeignTextKindType());
    }

    private void doEnableButtons() {
        if (!isReadOnly) {
            boolean isTextKindSelected = getSelectedTextKind() != null;
            boolean hasText = !mainWindow.multilangeditAndTextId.getMultiLanguage().allStringsAreEmpty();
            if (hasText && !isCreate) {
                if (!mainWindow.multilangeditAndTextId.isReadOnly()) {
                    // Check, ob Control und DictMeta die gleichen Texte haben
                    EtkMultiSprache multiLangForCheck = mainWindow.multilangeditAndTextId.getMultiLanguage();
                    multiLangForCheck.removeLanguagesWithEmptyTexts();
                    if (isSpecialHandlingForNeutText) {
                        // nur den DE-Text vergleichen
                        hasText = !multiLangForCheck.getText(Language.DE.getCode()).equals(dataDictMeta.getMultiLang().getText(Language.DE.getCode()));
                    } else {
                        // MultiLang vergleichen
                        hasText = !dataDictMeta.getMultiLang().equalContent(multiLangForCheck);
                    }
                    if (editForMigrationAllowed && !enumComboBoxForeignSource.isEnabled()) {
                        // die Quelle bei Änderungen ggf ändern
                        enumComboBoxForeignSource.switchOffEventListeners();
                        if (hasText) {
                            enumComboBoxForeignSource.setActToken(DictHelper.getIPartsSourceForCurrentSession());
                        } else {
                            enumComboBoxForeignSource.setActToken(dataDictMeta.getSource());
                        }
                        enumComboBoxForeignSource.switchOnEventListeners();
                    }
                    if (enumComboBoxState.isEnabled()) {
                        // oder hat sich der Status geändert
                        hasText |= !dataDictMeta.getState().equals(enumComboBoxState.getActToken());
                    }
                } else {
                    if (!comboboxTextKind.isEnabled()) {
                        if (enumComboBoxState.isEnabled()) {
                            // hat sich der Status geändert
                            hasText = !dataDictMeta.getState().equals(enumComboBoxState.getActToken());
                        }
                    }
                }
            }
            mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, isTextKindSelected && hasText);
        }
    }

    private void doEditTextId(Event event) {
        boolean enabled = !isReadOnly;
        if (enabled) {
            enabled = isCreate;
        }
        if (!EDIT_TEXTID_ENABLED) {
            enabled = false;
        }
        windowTextId.buttonpanelTextId.setButtonVisible(GuiButtonOnPanel.ButtonType.CANCEL, enabled);
        windowTextId.buttonpanelTextId.setButtonModalResult(GuiButtonOnPanel.ButtonType.OK, enabled ? ModalResult.OK : ModalResult.CANCEL);
        windowTextId.textfieldTextId.setEnabled(enabled);
        windowTextId.textfieldTextId.setText(getEditTextId());
        windowTextId.titleTextId.setTitle("!!Anzeige der Text-Id");
        if (windowTextId.showModal() == ModalResult.OK) {
            EtkMultiSprache multi = mainWindow.multilangeditAndTextId.getMultiLanguage();
            // Prefix für TextIds, weil TextId nicht gelöscht werden
            String textId = windowTextId.textfieldTextId.getText();
            if (!DictHelper.isDictTextId(textId)) {
                textId = DictHelper.makeDictId(iPartsDictPrefixAndSuffix.DICT_PREFIX, textId);
            }
            multi.setTextId(textId);
            mainWindow.multilangeditAndTextId.setMultiLanguage(multi);
        }
    }

    private void fillUsers() {
        comboboxUser.switchOffEventListeners();
        comboboxUser.removeAllItems();
        comboboxUser.addItem(iPartsDataAuthorOrder.getLoginAcronym());
        comboboxUser.switchOnEventListeners();
    }

    protected void fillFields(boolean statusChangeAllowed, boolean isSingleTextKind, boolean editForMigrationAllowed) {
        if (dataDictMeta != null) {
            EtkMultiSprache testMulti = dataDictMeta.getMultiLang();
            if (testMulti.getTextId().isEmpty()) {
                testMulti.setTextId(dataDictMeta.getTextId());
            }
            if (isSpecialHandlingForNeutText) {
                // bei NeutText: nur DE befüllen und zum Edit freigeben
                Map<String, String> langAndTexts = new HashMap<>();
                String text = testMulti.getText(Language.DE.getCode());
                langAndTexts.put(Language.DE.getCode(), text);
                testMulti.setLanguagesAndTexts(langAndTexts);
            } else {
                // alle Sprachn eintragen
                testMulti.completeWithLanguages(getProject().getConfig().getDatabaseLanguages());
            }
            int previousHeight = mainWindow.multilangeditAndTextId.getPreferredHeight();
            boolean isMultiLine = (footNoteTextKindId != null);
            if (isMultiLine) {
                isMultiLine = footNoteTextKindId.getTextKindId().equals(dataDictMeta.getAsId().getTextKindId());
                if (!isMultiLine) {
                    isMultiLine = containsMultiLines(testMulti);
                }
            }
            mainWindow.multilangeditAndTextId.setMultiLine(isMultiLine);
            int currentHeight = mainWindow.multilangeditAndTextId.getPreferredHeight();
            currentHeight -= previousHeight;
            mainWindow.setHeight(mainWindow.getHeight() + currentHeight);
            mainWindow.multilangeditAndTextId.setMultiLanguage(testMulti);

            mainWindow.textfieldForeignId.setText(dataDictMeta.getForeignId());
            enumComboBoxForeignSource.setActToken(dataDictMeta.getSource());
            enumComboBoxState.setActToken(dataDictMeta.getState());
            mainWindow.datetimeeditpanelCreationDate.setDateTime(dataDictMeta.getCreationDate());
            mainWindow.datetimeeditpanelChangeDate.setDateTime(dataDictMeta.getChangeDate());
            if (comboboxUser.getIndexOfItem(dataDictMeta.getUserId()) == -1) {
                comboboxUser.addItem(dataDictMeta.getUserId());
            }
            comboboxUser.setSelectedItem(dataDictMeta.getUserId());
            isMigrationText = dataDictMeta.getUserId().equals(DictHelper.getMADUserId()) ||
                              dataDictMeta.getUserId().equals(DictHelper.getRSKUserId()) ||
                              dataDictMeta.getUserId().equals(DictHelper.getEPCUserId());
            if (isMigrationText) {
                setSubTitle("!!Migrationstext");
                boolean isReadOnly = !(statusChangeAllowed && editForMigrationAllowed);
                mainWindow.multilangeditAndTextId.setReadOnly(isReadOnly);
                mainWindow.textfieldForeignId.setEnabled(false);
                enumComboBoxForeignSource.setEnabled(false);
                comboboxUser.setEnabled(false);

                // Status "Neu" entfernen
                int statusNewIndex = enumComboBoxState.getIndexByToken(DICT_STATUS_NEW);
                if (statusNewIndex >= 0) {
                    enumComboBoxState.removeItem(statusNewIndex);
                }
            } else {
                // Migrations-Benutzer entfernen
            }

            setTextKindTitleAndEnabled("!!Textart", false);
        } else {
            if (isSingleTextKind) {
                setTextKindTitleAndEnabled("!!Textart", false);
            } else {
                setTextKindTitleAndEnabled("!!Textart Auswahl", true);
            }
            enumComboBoxState.setActToken(DICT_STATUS_NEW);
            comboboxUser.setSelectedIndex(0);

            // Migrations-Benutzer entfernen
            enumComboBoxForeignSource.setActToken(DictHelper.getIPartsSourceForCurrentSession());
            enumComboBoxState.setActToken(iPartsDictConst.DICT_STATUS_RELEASED);
            if (isSpecialHandlingForNeutText) {
                // auch bei Create und NeutText: nur DE besetzen und zum Edit freigeben
                EtkMultiSprache multi = new EtkMultiSprache();
                Map<String, String> langAndTexts = new HashMap<>();
                langAndTexts.put(Language.DE.getCode(), "");
                multi.setLanguagesAndTexts(langAndTexts);
                mainWindow.multilangeditAndTextId.setMultiLanguage(multi);
            }
        }

        comboboxUser.setEnabled(false);
        if (isReadOnly) {
            setTextKindTitleAndEnabled(null, false);
            mainWindow.multilangeditAndTextId.setReadOnly(true);
            mainWindow.textfieldForeignId.setEditable(false);
            enumComboBoxForeignSource.setEnabled(false);
            enumComboBoxState.setEnabled(statusChangeAllowed);
            mainWindow.datetimeeditpanelCreationDate.setEditable(false);
            mainWindow.datetimeeditpanelChangeDate.setEditable(false);
        } else {
            enumComboBoxForeignSource.setEnabled(false);
            if (isCreate) {
                enumComboBoxState.setEnabled(false);
            } else {
                enumComboBoxState.setEnabled(statusChangeAllowed);
            }
            mainWindow.textfieldForeignId.setEnabled(false);
        }
    }

    protected void setMultiLangReadOnly(boolean readOnly) {
        mainWindow.multilangeditAndTextId.setReadOnly(readOnly);
    }

    private boolean containsMultiLines(EtkMultiSprache multi) {
        for (String language : multi.getSprachen()) {
            if (multi.getText(language).contains("\n")) {
                return true;
            }
        }
        return false;
    }

    protected void setTextKindTitleAndEnabled(String text, boolean isEnabled) {
        if (StrUtils.isValid(text)) {
            mainWindow.panelTextKind.setTitle(text);
        }
        comboboxTextKind.setEnabled(isEnabled);
    }

    private void onButtonOKClicked(Event event) {
        if (!isReadOnly) {
            List<String> warnings = new DwList<String>();
            if (checkData(warnings)) {
                saveToDB();
                mainWindow.setModalResult(ModalResult.OK);
                mainWindow.setVisible(false);
            } else {
                if (!warnings.isEmpty()) {
                    MessageDialog.show(warnings);
                }
            }
        } else {
            mainWindow.setModalResult(ModalResult.CANCEL);
            mainWindow.setVisible(false);
        }
    }

    protected void saveToDB() {
        iPartsDataDictMeta saveDataDictMeta;
        iPartsDataDictTextKind dataTextKind = getSelectedTextKind();
        if (dataTextKind == null) {
            MessageDialog.showError("!!Es wurde keine Textart ausgewählt.", mainWindow.title.getTitle());
            return;
        }
        if (dataDictMeta == null) {
            saveDataDictMeta = createDataMetaKind(dataTextKind);
        } else {
            saveDataDictMeta = updateDataDictMeta(dataDictMeta);
        }
        EtkMultiSprache multi = mainWindow.multilangeditAndTextId.getMultiLanguage();
        // EtkMultiSprache am aktuellen DictMeta Objekt setzen
        handleMultiTextInDictMeta(saveDataDictMeta, multi);
        getDbLayer().startTransaction();
        try {
            // Anlegen/Bearbeiten im ChangeSet dokumentieren und direkt in der DB ausführen
            EtkDataObjectList dataObjectList = getEtkDataObjectList(saveDataDictMeta, multi);
            List<EtkDataObject> deletedList = dataObjectList.getDeletedList();
            boolean saved = iPartsRevisionChangeSet.saveDataObjectListWithChangeSet(getProject(), dataObjectList, iPartsChangeSetSource.DICTIONARY);
            if (saved) {
                // ChangeSet wurde erfolgreich angelegt. Jetzt alle DictMetas sammeln und speichern. Die Einträge für
                // SPRACHE werden indirekt über die DitMetas in der DB gespeichert (handleMultiTextInDictMeta())
                iPartsDataDictMetaList dictMetasForSave = new iPartsDataDictMetaList();
                dataObjectList.forEach(dataObject -> {
                    if (dataObject instanceof iPartsDataDictMeta) {
                        dictMetasForSave.add((iPartsDataDictMeta)dataObject, DBActionOrigin.FROM_EDIT);
                    }
                });
                // Gefundene DictMetas in der DB speichern
                saved = dictMetasForSave.saveToDB(getProject());
                if (saved && (deletedList != null) && !deletedList.isEmpty()) {
                    EtkDataObjectList dataDeletedObjectList = new GenericEtkDataObjectList();
                    dataDeletedObjectList.addAll(deletedList, DBActionOrigin.FROM_EDIT);
                    dataDeletedObjectList.deleteAll(DBActionOrigin.__INTERNAL_FROM_EDIT_DB_DATA_OBJECT);
                    saved = dataDeletedObjectList.saveToDB(getProject());
                }
                if (saved) {
                    getDbLayer().commit();
                    setSavedDataDictMeta(saveDataDictMeta);

                    // Wurde ein Text hinzugefügt oder editiert, dann müssen die Caches geleert werden
                    if (isCreate) {
                        DictClusterEventHelper.fireNewDictionaryClusterEvent(saveDataDictMeta, dataTextKind.getForeignTextKindType(), multi);
                    } else {
                        DictClusterEventHelper.fireChangedDictionaryClusterEvent(saveDataDictMeta, dataDictMeta, dataTextKind, multi);
                    }
                } else {
                    getDbLayer().rollback();
                }
            } else {
                getDbLayer().rollback();
            }
        } catch (Exception e) {
            getDbLayer().rollback();
            Logger.getLogger().handleRuntimeException(e);
        }
    }

    /**
     * Setzt das übergebene {@link EtkMultiSprache} am übergebenen {@link iPartsDataDictMeta}
     *
     * @param dictMeta
     * @param multi
     */
    private void handleMultiTextInDictMeta(iPartsDataDictMeta dictMeta, EtkMultiSprache multi) {
        if (!isSpecialHandlingForNeutText) {
            // das MultLang normal eintragen
            dictMeta.setNewMultiLang(multi);
        } else {
            // bei NeutText: eintragen und alle Meta-Sprachen außer DE löschen
            dictMeta.setNewMultiLangForNeutText(multi);
        }
    }

    /**
     * Liefert alle DataObjects, die ins ChangeSet und in die DB geschrieben werden
     *
     * @param saveDataDictMeta
     * @param multi
     * @return
     */
    protected EtkDataObjectList getEtkDataObjectList(iPartsDataDictMeta saveDataDictMeta, EtkMultiSprache multi) {
        EtkDataObjectList dataObjectList = new GenericEtkDataObjectList();
        dataObjectList.add(saveDataDictMeta, DBActionOrigin.FROM_EDIT);
        iPartsDictMetaId dictMetaId = saveDataDictMeta.getAsId();
        if (isCreate) {
            // Die erste echte Verwendung der Textart für ein Feld aus DA_DICT_TXTKIND_USAGE laden, um keinen unnötigen
            // Eintrag für MAT.M_TEXTNR zu erzeugen (das ist nur der Fallback, wenn es keine echte Verwendung gibt)
            String tableDotFieldName =
                    DictTxtKindIdByMADId.getInstance(getProject()).findTableAndFieldNameByTextKindId(new iPartsDictTextKindId(dictMetaId.getTextKindId()),
                                                                                                     getProject());
            addTextEntries(tableDotFieldName, multi, dataObjectList);
//            // wenn es sich um die Neuanlage einer KG- oder TU-Benennung handelt, dann müssen alle Verwendungen angelegt werden
            saveDataDictMeta.setSaveAllUsages(TableAndFieldName.getFieldName(tableDotFieldName).equals(iPartsConst.FIELD_DA_DKM_DESC));
        } else {
            // hole alle vorhandenen Sprach-Einträge zur TextId
            EtkDataTextEntryList list = getSprachListe(saveDataDictMeta.getAsId().getTextId(), "");
            Map<TextEntryId, EtkDataTextEntry> existing = new HashMap<>();
            Set<TextEntryId> usageGroup = new HashSet<>();  // Sprach-PK ohne Language
            for (EtkDataTextEntry dataTextEntry : list) {
                existing.put(dataTextEntry.getAsId(), dataTextEntry);
                TextEntryId textEntryId = new TextEntryId(dataTextEntry.getAsId().getField(), dataTextEntry.getAsId().getTextNr(), "");
                usageGroup.add(textEntryId);

            }
            // Erzeugt und sammelt die SPRACHE Objekte auf, die angepasst werden müssen
            addTextChangeSetEntry(saveDataDictMeta, multi, existing, usageGroup, dataObjectList);

            // Hier alle DictMeta Einträge mit gleicher Text-ID und anderer Textart suchen und ebenfalls anpassen, weil
            // es zu einer Text-ID mehrere Textarten geben kann
            iPartsDataDictMetaList allDictMetasForTextId = iPartsDataDictMetaList.loadMetaFromTextIdList(getProject(), dictMetaId.getTextId());
            allDictMetasForTextId.forEach(dictMetaWithSameTextId -> {
                if (!dictMetaWithSameTextId.getAsId().equals(dictMetaId)) {
                    iPartsDataDictMeta dictMetaClone = updateDataDictMeta(dictMetaWithSameTextId);
                    handleMultiTextInDictMeta(dictMetaClone, multi);
                    dataObjectList.add(dictMetaClone, DBActionOrigin.FROM_EDIT);
                    addTextChangeSetEntry(dictMetaClone, multi, existing, usageGroup, dataObjectList);
                }
            });

        }
        // bei Neutral-Text: Lösche alle Sprach-Entries außer DE
        deleteNeutTextTextEntries(saveDataDictMeta, dataObjectList);
        return dataObjectList;
    }

    /**
     * Erzeugt alle SPRACHE Einträge zum übergebenen Lexikon-Eintrag, passt sie an und sammelt sie auf
     *
     * @param dictMeta
     * @param multi
     * @param existing
     * @param usageGroup
     * @param dataObjectList
     */
    private void addTextChangeSetEntry(iPartsDataDictMeta dictMeta, EtkMultiSprache multi,
                                       Map<TextEntryId, EtkDataTextEntry> existing, Set<TextEntryId> usageGroup,
                                       EtkDataObjectList dataObjectList) {
        // hole alle Feld-Namen aus der Dict-Usage
        Set<String> tableDotFieldNames = getTableDotFieldNamesFromTextKindId(dictMeta.getAsId().getTextKindId());

        // behandle den aktuellen Multi-Text
        addTextEntriesExtra(multi, dataObjectList,
                            existing, tableDotFieldNames, usageGroup);
    }

    protected void addTextEntriesExtra(EtkMultiSprache multi, EtkDataObjectList dataObjectList,
                                       Map<TextEntryId, EtkDataTextEntry> existing, Set<String> tableDotFieldNames,
                                       Set<TextEntryId> usageGroup) {
        // Über alle zur TextId definierten TableDotFieldName
        for (String tableDotFieldName : tableDotFieldNames) {
            // gibt es eine Verwendung (sprchunabhängig)?
            TextEntryId textId = new TextEntryId(tableDotFieldName, multi.getTextId(), "");
            if (usageGroup.contains(textId)) {
                // es sind Einträge in der Sprach-Tabelle vorhanden => Abgleich mit jetziger Eingabe
                for (Map.Entry<String, String> languagesAndTexts : multi.getLanguagesAndTexts().entrySet()) {
                    // 1. Überprüfen, ob der Eintrag aus dem editierten MultiLang zur Sprache bereits in der DB existiert
                    TextEntryId textEntryId = new TextEntryId(tableDotFieldName, multi.getTextId(), languagesAndTexts.getKey());
                    EtkDataTextEntry dataTextEntry = existing.get(textEntryId);
                    if (dataTextEntry != null) {
                        // 2. Existiert bereits in der DB => hat er sich geändert?
                        if (!languagesAndTexts.getValue().equals(dataTextEntry.getFieldValue(iPartsConst.FIELD_S_BENENN))) {
                            // Änderung => speichern
                            dataTextEntry.setFieldValue(iPartsConst.FIELD_S_BENENN, languagesAndTexts.getValue(), DBActionOrigin.FROM_EDIT);
                            dataObjectList.add(dataTextEntry, DBActionOrigin.FROM_EDIT);
                        }
                    } else {
                        // neu anlegen
                        EtkDataTextEntry textEntry = EtkDataObjectFactory.createDataTextEntry(getProject(), textEntryId);
                        if (!textEntry.existsInDB()) {
                            textEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                            textEntry.setFieldValue(iPartsConst.FIELD_S_TEXTID, multi.getTextId(), DBActionOrigin.FROM_EDIT);
                        }
                        textEntry.setFieldValue(iPartsConst.FIELD_S_BENENN, languagesAndTexts.getValue(), DBActionOrigin.FROM_EDIT);
                        dataObjectList.add(textEntry, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
        }
    }

    protected void addTextEntries(String tableDotFieldName, EtkMultiSprache multi, EtkDataObjectList dataObjectList) {
        if (StrUtils.isEmpty(tableDotFieldName)) {
            tableDotFieldName = TableAndFieldName.make(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR);
        }

        for (Map.Entry<String, String> languagesAndTexts : multi.getLanguagesAndTexts().entrySet()) {
            if (!languagesAndTexts.getValue().isEmpty()) {
                // ACHTUNG: Eigentlich sollte hier die TextNr verwendet werden, allerdings verwenden wir hier
                // bewusst die TextId, da dieses Changeset nur zur Dokumentation verwendet wird, und in diesem Fall
                // die TextId bei der Recherche hilfreicher ist. Außerdem sollte in diesem Fall die TextId mit
                // der TextNr übereinstimmen.
                TextEntryId textEntryId = new TextEntryId(tableDotFieldName, multi.getTextId(), languagesAndTexts.getKey());
                EtkDataTextEntry textEntry = EtkDataObjectFactory.createDataTextEntry(getProject(), textEntryId);
                if (!textEntry.existsInDB()) {
                    textEntry.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
                    textEntry.setFieldValue(iPartsConst.FIELD_S_TEXTID, multi.getTextId(), DBActionOrigin.FROM_EDIT);
                }
                textEntry.setFieldValue(iPartsConst.FIELD_S_BENENN, languagesAndTexts.getValue(), DBActionOrigin.FROM_EDIT);
                dataObjectList.add(textEntry, DBActionOrigin.FROM_EDIT);
            }
        }
    }

    protected void deleteNeutTextTextEntries(iPartsDataDictMeta saveDataDictMeta, EtkDataObjectList dataObjectList) {
        if (!isCreate && isSpecialHandlingForNeutText && (saveDataDictMeta != null)) {
            // bei NeutText: SPRACHE-Elemente zum Löschen vorbereiten
            List<iPartsDataDictLanguageMeta> deletedList = saveDataDictMeta.getLanguages().getDeletedList();
            if (!deletedList.isEmpty()) {
                // hole alle Usages
                Set<String> tableDotFieldNames = getTableDotFieldNamesFromTextKindId(saveDataDictMeta.getAsId().getTextKindId());
                tableDotFieldNames.add(TableAndFieldName.make(iPartsConst.TABLE_MAT, iPartsConst.FIELD_M_TEXTNR));

                // Suche alle Sprach-Einträge außer DE
                EtkDataTextEntryList list = getSprachListe(saveDataDictMeta.getAsId().getTextId(), Language.DE.getCode());
                for (EtkDataTextEntry dataTextEntry : list) {
                    // reine Sicherheitsabfrage
                    if (tableDotFieldNames.contains(dataTextEntry.getAsId().getField())) {
                        // alle Sprach-Einträge außer DE Löschen
                        dataObjectList.delete(dataTextEntry, true, DBActionOrigin.FROM_EDIT);
                    }
                }
            }
        }
    }

    private Set<String> getTableDotFieldNamesFromTextKindId(String textKindId) {
        iPartsDataDictTextKindUsageList usages = DictTxtKindIdByMADId.getInstance(getProject()).getUsagesByTextKindId(new iPartsDictTextKindId(textKindId),
                                                                                                                      getProject());
        Set<String> tableDotFieldNames = new HashSet<>();
        if (usages != null) {
            for (iPartsDataDictTextKindUsage textKindUsage : usages) {
                tableDotFieldNames.add(textKindUsage.getFeld());
            }
        }
        return tableDotFieldNames;
    }

    private EtkDataTextEntryList getSprachListe(String textId, String ignoreLanguage) {
        EtkDataTextEntryList list = new EtkDataTextEntryList();
        String[] whereFields = new String[]{ iPartsConst.FIELD_S_TEXTID };
        String[] whereValues = new String[]{ textId };
        String[] whereNotFields = null;
        String[] whereNotValues = null;
        if (StrUtils.isValid(ignoreLanguage)) {
            whereNotFields = new String[]{ iPartsConst.FIELD_S_SPRACH };
            whereNotValues = new String[]{ ignoreLanguage };
        }

        list.searchSortAndFill(getProject(), iPartsConst.TABLE_SPRACHE, whereFields, whereValues,
                               whereNotFields, whereNotValues, null, DBDataObjectList.LoadType.COMPLETE,
                               DBActionOrigin.FROM_DB);
        return list;
    }

    protected iPartsDataDictMeta createDataMetaKind(iPartsDataDictTextKind dataTextKind) {
        iPartsDictMetaId dictMetaId = new iPartsDictMetaId(dataTextKind.getAsId().getTextKindId(), getEditTextId());
        iPartsDataDictMeta dataDictMeta = new iPartsDataDictMeta(getProject(), dictMetaId);
        dataDictMeta.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        return updateDataDictMeta(dataDictMeta);
    }

    private iPartsDataDictMeta updateDataDictMeta(iPartsDataDictMeta dataDictMeta) {
        iPartsDataDictMeta dataDictMetaToSave = dataDictMeta.cloneMe(getProject());

        dataDictMetaToSave.setForeignId(mainWindow.textfieldForeignId.getText(), DBActionOrigin.FROM_EDIT);
        if (enumComboBoxForeignSource.getSelectedIndex() != -1) {
            dataDictMetaToSave.setSource(enumComboBoxForeignSource.getActToken(), DBActionOrigin.FROM_EDIT);
        }
        dataDictMetaToSave.setState(enumComboBoxState.getActToken(), DBActionOrigin.FROM_EDIT);
        if (isCreate) {
            dataDictMetaToSave.setActCreationDate(DBActionOrigin.FROM_EDIT);
        } else if (!isMigrationText || editForMigrationAllowed) {
            // auch bei editForMigrationAllowed ChangeDate setzen, da Edit erlaubt
            dataDictMetaToSave.setActChangeDate(DBActionOrigin.FROM_EDIT);
        }
        // den aktuellen Benutzer eintragen
        dataDictMetaToSave.setUserId(iPartsDataAuthorOrder.getLoginAcronym(), DBActionOrigin.FROM_EDIT);

        dataDictMetaToSave.removeForeignTablesAttributes();
        return dataDictMetaToSave;
    }

    //<editor-fold defaultstate="collapsed" desc="Framework Gui Code">
    /* START: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE BELOW THIS LINE */
    protected void $$internalCreateGui$$(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
        mainWindow = new MainWindowClass(translationHandler);
        mainWindow.__internal_setGenerationDpi(96);
        windowTextId = new WindowTextIdClass(translationHandler);
        windowTextId.__internal_setGenerationDpi(96);
    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected MainWindowClass mainWindow;

    private class MainWindowClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelTextKind;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<iPartsDataDictTextKind> comboboxTextKind;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelTextObject;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelTextAndTextId;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiMultiLangEdit multilangeditAndTextId;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelForeignId;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldForeignId;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelForeignSource;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<String> comboboxForeignSource;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelState;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<String> comboboxState;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelCreationDate;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.formattedfields.GuiDateTimeEditPanel datetimeeditpanelCreationDate;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelChangeDate;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.formattedfields.GuiDateTimeEditPanel datetimeeditpanelChangeDate;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelUser;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiComboBox<String> comboboxUser;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(600);
            this.setHeight(440);
            de.docware.framework.modules.gui.layout.LayoutBorder mainWindowLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(mainWindowLayout);
            title = new de.docware.framework.modules.gui.controls.GuiTitle();
            title.setName("title");
            title.__internal_setGenerationDpi(96);
            title.registerTranslationHandler(translationHandler);
            title.setScaleForResolution(true);
            title.setMinimumWidth(10);
            title.setMinimumHeight(50);
            title.setTitle("...");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleConstraints.setPosition("north");
            title.setConstraints(titleConstraints);
            this.addChild(title);
            panelMain = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMain.setName("panelMain");
            panelMain.__internal_setGenerationDpi(96);
            panelMain.registerTranslationHandler(translationHandler);
            panelMain.setScaleForResolution(true);
            panelMain.setMinimumWidth(10);
            panelMain.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
            panelTextKind = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelTextKind.setName("panelTextKind");
            panelTextKind.__internal_setGenerationDpi(96);
            panelTextKind.registerTranslationHandler(translationHandler);
            panelTextKind.setScaleForResolution(true);
            panelTextKind.setMinimumWidth(10);
            panelTextKind.setMinimumHeight(10);
            panelTextKind.setBorderWidth(4);
            panelTextKind.setTitle("!!Textart Auswahl");
            de.docware.framework.modules.gui.layout.LayoutGridBag panelTextKindLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelTextKind.setLayout(panelTextKindLayout);
            comboboxTextKind = new de.docware.framework.modules.gui.controls.GuiComboBox<iPartsDataDictTextKind>();
            comboboxTextKind.setName("comboboxTextKind");
            comboboxTextKind.__internal_setGenerationDpi(96);
            comboboxTextKind.registerTranslationHandler(translationHandler);
            comboboxTextKind.setScaleForResolution(true);
            comboboxTextKind.setMinimumWidth(0);
            comboboxTextKind.setMinimumHeight(0);
            comboboxTextKind.setMaximumWidth(2147483647);
            comboboxTextKind.setMaximumHeight(2147483647);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxTextKindConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 4);
            comboboxTextKind.setConstraints(comboboxTextKindConstraints);
            panelTextKind.addChild(comboboxTextKind);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelTextKindConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelTextKindConstraints.setPosition("north");
            panelTextKind.setConstraints(panelTextKindConstraints);
            panelMain.addChild(panelTextKind);
            panelTextObject = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelTextObject.setName("panelTextObject");
            panelTextObject.__internal_setGenerationDpi(96);
            panelTextObject.registerTranslationHandler(translationHandler);
            panelTextObject.setScaleForResolution(true);
            panelTextObject.setMinimumWidth(10);
            panelTextObject.setMinimumHeight(10);
            panelTextObject.setBorderWidth(4);
            panelTextObject.setTitle("!!Text-Metadaten");
            de.docware.framework.modules.gui.layout.LayoutGridBag panelTextObjectLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelTextObjectLayout.setCentered(false);
            panelTextObject.setLayout(panelTextObjectLayout);
            labelTextAndTextId = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelTextAndTextId.setName("labelTextAndTextId");
            labelTextAndTextId.__internal_setGenerationDpi(96);
            labelTextAndTextId.registerTranslationHandler(translationHandler);
            labelTextAndTextId.setScaleForResolution(true);
            labelTextAndTextId.setMinimumWidth(10);
            labelTextAndTextId.setMinimumHeight(10);
            labelTextAndTextId.setText("!!Text und Text-Id");
            labelTextAndTextId.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelTextAndTextIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "ne", "n", 8, 4, 4, 4);
            labelTextAndTextId.setConstraints(labelTextAndTextIdConstraints);
            panelTextObject.addChild(labelTextAndTextId);
            multilangeditAndTextId = new de.docware.framework.modules.gui.controls.GuiMultiLangEdit();
            multilangeditAndTextId.setName("multilangeditAndTextId");
            multilangeditAndTextId.__internal_setGenerationDpi(96);
            multilangeditAndTextId.registerTranslationHandler(translationHandler);
            multilangeditAndTextId.setScaleForResolution(true);
            multilangeditAndTextId.setMinimumWidth(10);
            multilangeditAndTextId.setMinimumHeight(10);
            multilangeditAndTextId.setBackgroundColor(new java.awt.Color(255, 255, 255, 0));
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag multilangeditAndTextIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            multilangeditAndTextId.setConstraints(multilangeditAndTextIdConstraints);
            panelTextObject.addChild(multilangeditAndTextId);
            labelForeignId = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelForeignId.setName("labelForeignId");
            labelForeignId.__internal_setGenerationDpi(96);
            labelForeignId.registerTranslationHandler(translationHandler);
            labelForeignId.setScaleForResolution(true);
            labelForeignId.setMinimumWidth(10);
            labelForeignId.setMinimumHeight(10);
            labelForeignId.setText("!!Fremd-Id");
            labelForeignId.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelForeignIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 1, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelForeignId.setConstraints(labelForeignIdConstraints);
            panelTextObject.addChild(labelForeignId);
            textfieldForeignId = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldForeignId.setName("textfieldForeignId");
            textfieldForeignId.__internal_setGenerationDpi(96);
            textfieldForeignId.registerTranslationHandler(translationHandler);
            textfieldForeignId.setScaleForResolution(true);
            textfieldForeignId.setMinimumWidth(200);
            textfieldForeignId.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldForeignIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 1, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            textfieldForeignId.setConstraints(textfieldForeignIdConstraints);
            panelTextObject.addChild(textfieldForeignId);
            labelForeignSource = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelForeignSource.setName("labelForeignSource");
            labelForeignSource.__internal_setGenerationDpi(96);
            labelForeignSource.registerTranslationHandler(translationHandler);
            labelForeignSource.setScaleForResolution(true);
            labelForeignSource.setMinimumWidth(10);
            labelForeignSource.setMinimumHeight(10);
            labelForeignSource.setText("!!Fremd-Quelle");
            labelForeignSource.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelForeignSourceConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 2, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelForeignSource.setConstraints(labelForeignSourceConstraints);
            panelTextObject.addChild(labelForeignSource);
            comboboxForeignSource = new de.docware.framework.modules.gui.controls.GuiComboBox<String>();
            comboboxForeignSource.setName("comboboxForeignSource");
            comboboxForeignSource.__internal_setGenerationDpi(96);
            comboboxForeignSource.registerTranslationHandler(translationHandler);
            comboboxForeignSource.setScaleForResolution(true);
            comboboxForeignSource.setMinimumWidth(10);
            comboboxForeignSource.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxForeignSourceConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 2, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            comboboxForeignSource.setConstraints(comboboxForeignSourceConstraints);
            panelTextObject.addChild(comboboxForeignSource);
            labelState = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelState.setName("labelState");
            labelState.__internal_setGenerationDpi(96);
            labelState.registerTranslationHandler(translationHandler);
            labelState.setScaleForResolution(true);
            labelState.setMinimumWidth(10);
            labelState.setMinimumHeight(10);
            labelState.setText("!!Status");
            labelState.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelStateConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 3, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelState.setConstraints(labelStateConstraints);
            panelTextObject.addChild(labelState);
            comboboxState = new de.docware.framework.modules.gui.controls.GuiComboBox<String>();
            comboboxState.setName("comboboxState");
            comboboxState.__internal_setGenerationDpi(96);
            comboboxState.registerTranslationHandler(translationHandler);
            comboboxState.setScaleForResolution(true);
            comboboxState.setMinimumWidth(10);
            comboboxState.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxStateConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 3, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            comboboxState.setConstraints(comboboxStateConstraints);
            panelTextObject.addChild(comboboxState);
            labelCreationDate = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelCreationDate.setName("labelCreationDate");
            labelCreationDate.__internal_setGenerationDpi(96);
            labelCreationDate.registerTranslationHandler(translationHandler);
            labelCreationDate.setScaleForResolution(true);
            labelCreationDate.setMinimumWidth(10);
            labelCreationDate.setMinimumHeight(10);
            labelCreationDate.setText("!!Anlagedatum");
            labelCreationDate.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelCreationDateConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 4, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelCreationDate.setConstraints(labelCreationDateConstraints);
            panelTextObject.addChild(labelCreationDate);
            datetimeeditpanelCreationDate = new de.docware.framework.modules.gui.controls.formattedfields.GuiDateTimeEditPanel();
            datetimeeditpanelCreationDate.setName("datetimeeditpanelCreationDate");
            datetimeeditpanelCreationDate.__internal_setGenerationDpi(96);
            datetimeeditpanelCreationDate.registerTranslationHandler(translationHandler);
            datetimeeditpanelCreationDate.setScaleForResolution(true);
            datetimeeditpanelCreationDate.setMinimumWidth(10);
            datetimeeditpanelCreationDate.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag datetimeeditpanelCreationDateConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 4, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            datetimeeditpanelCreationDate.setConstraints(datetimeeditpanelCreationDateConstraints);
            panelTextObject.addChild(datetimeeditpanelCreationDate);
            labelChangeDate = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelChangeDate.setName("labelChangeDate");
            labelChangeDate.__internal_setGenerationDpi(96);
            labelChangeDate.registerTranslationHandler(translationHandler);
            labelChangeDate.setScaleForResolution(true);
            labelChangeDate.setMinimumWidth(10);
            labelChangeDate.setMinimumHeight(10);
            labelChangeDate.setText("!!Änderungsdatum");
            labelChangeDate.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelChangeDateConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 5, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelChangeDate.setConstraints(labelChangeDateConstraints);
            panelTextObject.addChild(labelChangeDate);
            datetimeeditpanelChangeDate = new de.docware.framework.modules.gui.controls.formattedfields.GuiDateTimeEditPanel();
            datetimeeditpanelChangeDate.setName("datetimeeditpanelChangeDate");
            datetimeeditpanelChangeDate.__internal_setGenerationDpi(96);
            datetimeeditpanelChangeDate.registerTranslationHandler(translationHandler);
            datetimeeditpanelChangeDate.setScaleForResolution(true);
            datetimeeditpanelChangeDate.setMinimumWidth(10);
            datetimeeditpanelChangeDate.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag datetimeeditpanelChangeDateConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 5, 1, 1, 0.0, 0.0, "c", "h", 4, 4, 4, 4);
            datetimeeditpanelChangeDate.setConstraints(datetimeeditpanelChangeDateConstraints);
            panelTextObject.addChild(datetimeeditpanelChangeDate);
            labelUser = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelUser.setName("labelUser");
            labelUser.__internal_setGenerationDpi(96);
            labelUser.registerTranslationHandler(translationHandler);
            labelUser.setScaleForResolution(true);
            labelUser.setMinimumWidth(10);
            labelUser.setMinimumHeight(10);
            labelUser.setText("!!Benutzer");
            labelUser.setHorizontalAlignment(de.docware.framework.modules.gui.controls.GuiLabel.HorizontalAlignment.RIGHT);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelUserConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 6, 1, 1, 0.0, 0.0, "e", "n", 4, 4, 4, 4);
            labelUser.setConstraints(labelUserConstraints);
            panelTextObject.addChild(labelUser);
            comboboxUser = new de.docware.framework.modules.gui.controls.GuiComboBox<String>();
            comboboxUser.setName("comboboxUser");
            comboboxUser.__internal_setGenerationDpi(96);
            comboboxUser.registerTranslationHandler(translationHandler);
            comboboxUser.setScaleForResolution(true);
            comboboxUser.setMinimumWidth(10);
            comboboxUser.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag comboboxUserConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 6, 1, 1, 100.0, 0.0, "c", "h", 4, 4, 4, 4);
            comboboxUser.setConstraints(comboboxUserConstraints);
            panelTextObject.addChild(comboboxUser);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelTextObjectConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelTextObject.setConstraints(panelTextObjectConstraints);
            panelMain.addChild(panelTextObject);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMain.setConstraints(panelMainConstraints);
            this.addChild(panelMain);
            buttonpanel = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanel.setName("buttonpanel");
            buttonpanel.__internal_setGenerationDpi(96);
            buttonpanel.registerTranslationHandler(translationHandler);
            buttonpanel.setScaleForResolution(true);
            buttonpanel.setMinimumWidth(10);
            buttonpanel.setMinimumHeight(10);
            buttonpanel.addEventListener(new de.docware.framework.modules.gui.event.EventListener("buttonOKActionPerformedEvent") {
                public void fire(de.docware.framework.modules.gui.event.Event event) {
                    onButtonOKClicked(event);
                }
            });
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }

    @SuppressWarnings({ "FieldCanBeLocal" })
    protected WindowTextIdClass windowTextId;

    private class WindowTextIdClass extends de.docware.framework.modules.gui.controls.GuiWindow {

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTitle titleTextId;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMainTextId;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiLabel labelTextId;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiTextField textfieldTextId;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanelTextId;

        private WindowTextIdClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
            this.setWidth(340);
            this.setHeight(160);
            de.docware.framework.modules.gui.layout.LayoutBorder windowTextIdLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            this.setLayout(windowTextIdLayout);
            titleTextId = new de.docware.framework.modules.gui.controls.GuiTitle();
            titleTextId.setName("titleTextId");
            titleTextId.__internal_setGenerationDpi(96);
            titleTextId.registerTranslationHandler(translationHandler);
            titleTextId.setScaleForResolution(true);
            titleTextId.setMinimumWidth(10);
            titleTextId.setMinimumHeight(50);
            titleTextId.setTitle("!!Eingabe der Text-Id");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder titleTextIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            titleTextIdConstraints.setPosition("north");
            titleTextId.setConstraints(titleTextIdConstraints);
            this.addChild(titleTextId);
            panelMainTextId = new de.docware.framework.modules.gui.controls.GuiPanel();
            panelMainTextId.setName("panelMainTextId");
            panelMainTextId.__internal_setGenerationDpi(96);
            panelMainTextId.registerTranslationHandler(translationHandler);
            panelMainTextId.setScaleForResolution(true);
            panelMainTextId.setMinimumWidth(10);
            panelMainTextId.setMinimumHeight(10);
            panelMainTextId.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutGridBag panelMainTextIdLayout =
                    new de.docware.framework.modules.gui.layout.LayoutGridBag();
            panelMainTextIdLayout.setCentered(false);
            panelMainTextId.setLayout(panelMainTextIdLayout);
            labelTextId = new de.docware.framework.modules.gui.controls.GuiLabel();
            labelTextId.setName("labelTextId");
            labelTextId.__internal_setGenerationDpi(96);
            labelTextId.registerTranslationHandler(translationHandler);
            labelTextId.setScaleForResolution(true);
            labelTextId.setMinimumWidth(10);
            labelTextId.setMinimumHeight(10);
            labelTextId.setText("!!Text-Id");
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag labelTextIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 0, 1, 1, 0.0, 0.0, "c", "n", 0, 4, 0, 4);
            labelTextId.setConstraints(labelTextIdConstraints);
            panelMainTextId.addChild(labelTextId);
            textfieldTextId = new de.docware.framework.modules.gui.controls.GuiTextField();
            textfieldTextId.setName("textfieldTextId");
            textfieldTextId.__internal_setGenerationDpi(96);
            textfieldTextId.registerTranslationHandler(translationHandler);
            textfieldTextId.setScaleForResolution(true);
            textfieldTextId.setMinimumWidth(200);
            textfieldTextId.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag textfieldTextIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(1, 0, 1, 1, 100.0, 0.0, "c", "h", 0, 4, 0, 4);
            textfieldTextId.setConstraints(textfieldTextIdConstraints);
            panelMainTextId.addChild(textfieldTextId);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder panelMainTextIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            panelMainTextId.setConstraints(panelMainTextIdConstraints);
            this.addChild(panelMainTextId);
            buttonpanelTextId = new de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel();
            buttonpanelTextId.setName("buttonpanelTextId");
            buttonpanelTextId.__internal_setGenerationDpi(96);
            buttonpanelTextId.registerTranslationHandler(translationHandler);
            buttonpanelTextId.setScaleForResolution(true);
            buttonpanelTextId.setMinimumWidth(10);
            buttonpanelTextId.setMinimumHeight(10);
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelTextIdConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelTextIdConstraints.setPosition("south");
            buttonpanelTextId.setConstraints(buttonpanelTextIdConstraints);
            this.addChild(buttonpanelTextId);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}