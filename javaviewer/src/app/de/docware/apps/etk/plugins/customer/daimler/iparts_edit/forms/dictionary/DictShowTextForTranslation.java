/*
 * Copyright (c) 2023 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary;

import de.docware.apps.etk.base.config.db.datatypes.enumtypes.SetOfEnumDataType;
import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControlFactory;
import de.docware.apps.etk.base.forms.common.EditCreateMode;
import de.docware.apps.etk.base.forms.common.EtkMessageLogForm;
import de.docware.apps.etk.base.forms.common.components.AbstractGuiTableColumnFilterFactory;
import de.docware.apps.etk.base.forms.common.components.MenuRunnable;
import de.docware.apps.etk.base.forms.common.components.ToolbarButtonMenuHelper;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.base.GenericEtkDataObjectList;
import de.docware.apps.etk.base.project.events.ApplicationEvents;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsImportDataOrigin;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.changesets.iPartsChangeSetSource;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.dictionary.*;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTxtKindIdByMADId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.iPartsDictConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.iPartsDictPrefixAndSuffix;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit.DictMetaListContainer;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.transit.iPartsTransitMappingCache;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsClearTextCacheEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsRevisionChangeSet;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditField;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.EtkEditFields;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.EditUserMultiChangeControls;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.SimpleMasterDataSearchFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.SimpleMasterDataSearchResultGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditDataObjectGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.helper.EditToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.OnUpdateGridEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.workbasket.kemworkbasket.UpdateGridHelper;
import de.docware.framework.modules.config.common.Language;
import de.docware.framework.modules.config.db.EtkFieldType;
import de.docware.framework.modules.config.db.VirtualFieldsUtils;
import de.docware.framework.modules.db.*;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.controls.AbstractGuiControl;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.controls.table.GuiTable;
import de.docware.framework.modules.gui.controls.table.TableCellControlWithTextRepresentation;
import de.docware.framework.modules.gui.controls.toolbar.GuiToolbar;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.misc.logger.Logger;
import de.docware.framework.modules.gui.misc.threads.FrameworkRunnable;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.misc.id.IdWithType;
import de.docware.util.sql.SQLUtils;
import de.docware.util.sql.TableAndFieldName;
import de.docware.util.sql.terms.Condition;
import de.docware.util.sql.terms.Fields;
import de.docware.util.sql.terms.InnerJoin;
import org.apache.commons.collections4.list.TreeList;

import java.awt.*;
import java.util.List;
import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Anzeige der neu erstellten Texte, die zur Übersetzung sollen
 * Gesucht wird in TABLE_DA_DICT_SPRACHE nach "iParts.*" als TextId und leerer JobId
 * KONSEQUENZ: Sobald ein Text zur Übersetzung geht/war, darf die JobId nie mehr leer sein!!
 * anschließend wird über die TextId das DictMeta-Object bestimmt und an das Grid weitergegeben
 * Als Ergebnis des Dialogs erhält man eine Liste von DictMeta-Objecten (iPartsDataDictMetaList)
 */
public class DictShowTextForTranslation extends AbstractJavaViewerForm implements iPartsConst {

    private static final String PLACEHOLDER_TEXT_FIELD = VirtualFieldsUtils.addVirtualFieldMask("TEXT_FIELD"); // Platzhalter für den Text der angezeigt werden soll
    private static final String PLACEHOLDER_TEXT_LANGS = VirtualFieldsUtils.addVirtualFieldMask("TEXT_LANGS"); // Platzhalter für die Sprachen des Textes die angezeigt werden sollen
    private static final String PLACEHOLDER_TEXT_TEXTKIND = VirtualFieldsUtils.addVirtualFieldMask("TEXT_TEXTKIND"); // Platzhalter für die Sprachen des Textes die angezeigt werden sollen
    private static final String LANG_DELIMITER = ",";
    public static final String TITLE = "!!Neue Texte für Übersetzungsumfang"; // Platzhalter für die Sprachen des Textes die angezeigt werden sollen
    // Felder für speziellen Spaltenfilter
    private static final String[] SPECIAL_TABLEFILTER_FIELDS = new String[]{ PLACEHOLDER_TEXT_TEXTKIND, FIELD_DA_DICT_META_SOURCE,
                                                                             PLACEHOLDER_TEXT_LANGS };


    public static DictMetaListContainer showTexteForTranslation(AbstractJavaViewerForm owner) {
        // Aktive Form holen
        AbstractJavaViewerForm activeForm = owner.getConnector().getActiveForm();
        DictShowTextForTranslation dlg = new DictShowTextForTranslation(activeForm.getConnector(), activeForm);
        dlg.fillGrid();
        ModalResult result = dlg.showModal();
        // Es wurden einzelne Einträge selektiert
        if (result == ModalResult.OK) {
            return dlg.getSelectedDictMeta();
        }
        return null;
    }

    private DictionaryDataObjectGrid dataGrid;
    private GuiButtonOnPanel addAllButton;
    private EtkEditFields updateGridEditFields;
    private final boolean carAndVanInSession = iPartsRight.checkCarAndVanInSession();
    private final boolean truckAndBusInSession = iPartsRight.checkTruckAndBusInSession();


    /**
     * Erzeugt eine Instanz von DictShowTextForTranslation.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     */
    public DictShowTextForTranslation(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
        super(dataConnector, parentForm);
        $$internalCreateGui$$(null);
        postCreateGui();
        // Den Dialog größer machen
        doResizeWindow(SimpleMasterDataSearchResultGrid.SCREEN_SIZES.SCALE_FROM_PARENT);
    }

    /**
     * Hier kann eigener Code stehen der gerufen wird wenn die Instanz
     * erzeugt wurde.
     */
    private void postCreateGui() {
        mainWindow.setTitle(TITLE);
        mainWindow.title.setTitle("!!Lexikon-Einträge für den Übersetzungsumfang auswählen");
        dataGrid = new DictionaryDataObjectGrid(getConnector(), this);
        dataGrid.setDisplayFields(buildDisplayFields(), true);

        ConstraintsBorder constraints = new ConstraintsBorder(ConstraintsBorder.POSITION_CENTER);
        dataGrid.getGui().setConstraints(constraints);
        mainWindow.panelMain.addChild(dataGrid.getGui());

        // Button, um alle Einträge auszuwählen
        addAllButton = mainWindow.buttonpanel.addCustomButton(TranslationHandler.translate("!!Alle auswählen"));
        addAllButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                dataGrid.getTable().selectAllRows();
            }
        });

        // Diese Felder werden bei updateGrid aktualisiert
        updateGridEditFields = new EtkEditFields();
        addEditField(TABLE_DA_DICT_META, FIELD_DA_DICT_META_STATE, false, null, updateGridEditFields);
        addEditField(TABLE_DA_DICT_META, PLACEHOLDER_TEXT_FIELD, false, null, updateGridEditFields);
        addEditField(TABLE_DA_DICT_META, FIELD_DA_DICT_META_CHANGE, false, null, updateGridEditFields);
        addEditField(TABLE_DA_DICT_META, FIELD_DA_DICT_META_USERID, false, null, updateGridEditFields);
    }

    private EtkEditField addEditField(String tableName, String fieldName, boolean multiLanguage,
                                      String labelText, EtkEditFields editFields) {
        EtkEditField editField = new EtkEditField(tableName, fieldName, multiLanguage);
        if (labelText != null) {
            editField.setDefaultText(false);
            editField.setText(new EtkMultiSprache(labelText, new String[]{ TranslationHandler.getUiLanguage() }));
        }
        editFields.addFeld(editField);
        return editField;
    }

    public void doResizeWindow(SimpleMasterDataSearchResultGrid.SCREEN_SIZES kind) {
        switch (kind) {
            case MAXIMIZE:
                Dimension screenSize = FrameworkUtils.getScreenSize();
                mainWindow.setSize(screenSize.width - 20, screenSize.height - 20);
                break;
            case SCALE_FROM_PARENT:
                if (parentForm != null) {
                    int height = parentForm.getGui().getParentWindow().getHeight();
                    int width = parentForm.getGui().getParentWindow().getWidth();
                    mainWindow.setSize(width - CASCADING_WINDOW_OFFSET_WIDTH, height - CASCADING_WINDOW_OFFSET_HEIGHT);
                }
                break;
        }
    }

    private void doEnableButtons() {
        mainWindow.buttonpanel.setButtonEnabled(GuiButtonOnPanel.ButtonType.OK, !dataGrid.getTable().getSelectedRows().isEmpty());
        addAllButton.setEnabled(dataGrid.getTable().getRowCount() != dataGrid.getMultiSelection().size());
        dataGrid.doEnableToolbarButtons();
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

    /**
     * Liefert alle selektierten {@link iPartsDataDictMeta} Objekte aus dem Grid (sortiert nach ihren Gültigkeiten)
     *
     * @return
     */
    public DictMetaListContainer getSelectedDictMeta() {
        if (mainWindow.getModalResult() == ModalResult.OK) {
            DictMetaListContainer texts = new DictMetaListContainer();
            List<List<EtkDataObject>> selectedMultiObjects = dataGrid.getMultiSelection();
            for (List<EtkDataObject> selectedObjects : selectedMultiObjects) {
                for (EtkDataObject dataObject : selectedObjects) {
                    if (dataObject instanceof iPartsDataDictMeta) {
                        iPartsDataDictMeta dictMeta = (iPartsDataDictMeta)dataObject;
                        dictMeta.removeForeignTablesAttributes();
                        texts.addText(dictMeta, carAndVanInSession, truckAndBusInSession);
                        break;
                    }
                }
            }
            return texts;
        }
        return null;
    }

    /**
     * Liefert die Liste der selektierten iPartsDataDictMeta, ggf mit Vorbereitung für Unify (useClone = true)
     *
     * @param useClone
     * @return
     */
    public List<iPartsDataDictMeta> getSelectedDictMetaList(boolean useClone) {
        List<iPartsDataDictMeta> result = new DwList<>();
        List<List<EtkDataObject>> selectedMultiObjects = dataGrid.getMultiSelection();
        for (List<EtkDataObject> selectedObjects : selectedMultiObjects) {
            for (EtkDataObject dataObject : selectedObjects) {
                if (dataObject instanceof iPartsDataDictMeta) {
                    iPartsDataDictMeta dictMeta = (iPartsDataDictMeta)dataObject;
                    if (useClone) {
                        // clonen
                        iPartsDataDictMeta unifyDictMeta = dictMeta.cloneMe(getProject());
                        // Vorbereitung für Unify, damit der Default-Wert durchschlägt
                        unifyDictMeta.setFieldValue(FIELD_DA_DICT_META_STATE, "", DBActionOrigin.FROM_DB);
                        result.add(unifyDictMeta);
                    } else {
                        iPartsDataDictMeta loadedDictMeta = new iPartsDataDictMeta(getProject(), dictMeta.getAsId());
                        if (loadedDictMeta.existsInDB()) {
                            result.add(loadedDictMeta);
                        }
                    }
                }
            }
        }
        return result;
    }

    public void fillGrid() {
        dataGrid.clearGrid();
        dataGrid.getTable().switchOffEventListeners();
        // SelectFields für SPRACHE, DICT_SPRACHE und DICT_META
        EtkDisplayFields selectFields = getProject().getAllDisplayFieldsForTable(TABLE_DA_DICT_SPRACHE);
        selectFields.addFelder(getProject().getAllDisplayFieldsForTable(TABLE_DA_DICT_META));
        selectFields.addFelder(getProject().getAllDisplayFieldsForTable(TABLE_SPRACHE));
        final Fields fields = new Fields();
        for (EtkDisplayField selecField : selectFields.getFields()) {
            if (!selecField.getKey().getFieldName().equals(FIELD_STAMP)) { // Feld T_STAMP wird nicht benötigt und wäre auch mehrfach vorhanden (pro Tabelle einmal)
                fields.add(selecField.getKey().getName());
            }
        }
        // SQL Query mit Status in DICT Sprache ist leer und alle TextIds mit Präfix "IPARTS". Join auf Sprache um
        // das EtkMultiLang Objekt aufzubauen und nicht zu laden. Join auf DICT_META um die iPartsDataDictMeta Objekte aufzubauen.
        // Da wir in DA_DICT_SPRACHE sind brauchen wir die einzelnen Sprachen später nicht laden.
        final DBSQLQuery query = buildQuery(fields);

        final EtkMessageLogForm messageLogForm = new EtkMessageLogForm("!!Übersetzungstexte suchen",
                                                                       "!!Bitte warten...", null, true);
        messageLogForm.disableButtons(true);
        messageLogForm.showModal(new FrameworkRunnable() {
            @Override
            public void run(FrameworkThread thread) {
                try (DBDataSetCancelable dataSet = query.executeQueryCancelable()) {
                    // Query ausführen
                    if (dataSet != null) {
                        Map<String, iPartsDataDictMeta> metaMap = new HashMap<>();
                        Map<String, EtkMultiSprache> multiMap = new HashMap<>();
                        // Für jeden Treffer in DA_DICT_SPRACHE
                        collectResults(dataSet, metaMap, multiMap);

                        // Treffer gefunden
                        if (!metaMap.isEmpty()) {
                            DictTxtKindIdByMADId dictTxtKindIdInstance = DictTxtKindIdByMADId.getInstance(getProject());
                            for (Map.Entry<String, iPartsDataDictMeta> entry : metaMap.entrySet()) {
                                iPartsDataDictMeta dictMeta = entry.getValue();
                                EtkMultiSprache langAndText = multiMap.remove(entry.getKey());
                                // Existiert kein Text oder fehlen die DE und EN Einträge, dann darf der
                                // Übersetzungsprozess für diesen Text nicht angetriggert werden
                                if ((langAndText == null) || (iPartsDictTransJobHelper.findSourceLang(langAndText) == null)) {
                                    continue;
                                }
                                dictMeta.setNewMultiLang(langAndText);
                                DBDataObjectAttributes attributes = dictMeta.getAttributes();
                                // Attribute für die Anzeige erweitern
                                fillAttributesWithLangData(dictMeta, attributes, true);
                                // filtern nach Textarten, die auch für TRANSIT gültig sind
                                iPartsDataDictTextKind dataDictTextKind = dictTxtKindIdInstance.findDictTextKindByTextKindIdAndValidTransit(new iPartsDictTextKindId(dictMeta.getAsId().getTextKindId()),
                                                                                                                                            getProject());
                                if (dataDictTextKind != null) {
                                    String textKind = dataDictTextKind.getName(getProject().getDBLanguage());
                                    attributes.addField(PLACEHOLDER_TEXT_TEXTKIND, textKind, DBActionOrigin.FROM_DB);
                                    dataGrid.addObjectToGrid(dictMeta);
                                }
                            }
                        }
                    }
                } catch (CanceledException e) {
                    Logger.getLogger().throwRuntimeException(e);
                }
            }

            private void collectResults(DBDataSetCancelable dataSet, Map<String, iPartsDataDictMeta> metaMap,
                                        Map<String, EtkMultiSprache> multiMap) throws CanceledException {
                // Für jeden Treffer in DA_DICT_SPRACHE
                while (dataSet.next()) {
                    EtkRecord record = dataSet.getRecord(fields.getFieldList());
                    DBDataObjectAttributes attributes = DBDataObjectAttributes.getFromRecord(record, DBActionOrigin.FROM_DB);
                    // Text-ID bestimmen
                    String textId = attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_TEXTID));
                    String source = attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_SOURCE));

                    // Sichtbarkeiten der Texte prüfen
                    if (!isValidSource(source)) {
                        continue;
                    }
                    // Aktuelle Sprache
                    String language = attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_SPRACH));
                    // iPartsDataDictMeta Objekt zur ID holen
                    iPartsDataDictMeta dictMeta = metaMap.get(textId);
                    if (dictMeta == null) {
                        // Falls zur ID kein Objekt existiert -> iPartsDataDictMeta Objekt anlegen und befüllen (nicht aus DB laden)
                        dictMeta = createDictMeta(attributes, textId);
                        metaMap.put(textId, dictMeta);
                    } else if (dictMeta.getLanguages().findLanguage(language) != null) {
                        // Für die Sprache gibt es schon einen Eintrag (kann passieren, wenn eine Text-Id bei verschiedenen Textarten genutzt wird)
                        continue;
                    }
                    // Die aktuelle Sprache aus DA_DICT_SPRACHE zusammenbauen und den aktuellen Text für die Sprache im
                    // EtkMultiLang ablegen
                    fillLanguageAndMultiLang(dictMeta, attributes, multiMap, language);
                    // Hat die aktuelle TextId alle 24 Texte, dann kann eine Übersetzung für den Text nicht starten -> Objekte entfernen
                    if (dictMeta.getLanguages().size() >= iPartsTransitMappingCache.getInstance(getProject()).getIsoClmMapping().size()) {
                        metaMap.remove(dictMeta.getTextId());
                        multiMap.remove(dictMeta.getTextId());
                    }
                }
            }

            private boolean isValidSource(String source) {
                iPartsImportDataOrigin importDataOrigin = iPartsImportDataOrigin.getTypeFromCode(source);
                if (carAndVanInSession && DictMetaListContainer.isCarVanSourceValidForTranslation(importDataOrigin)) {
                    return true;
                } else if (truckAndBusInSession && DictMetaListContainer.isTruckBusSourceValidForTranslation(importDataOrigin)) {
                    return true;
                } else {
                    // Keine gültige Quelle
                    return false;
                }
            }

            private iPartsDataDictMeta createDictMeta(DBDataObjectAttributes attributes, String textId) {
                String textKind = attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_TXTKIND_ID));
                iPartsDataDictMeta dictMeta = new iPartsDataDictMeta(getProject(), new iPartsDictMetaId(textKind, textId));
                dictMeta.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                dictMeta.setForeignId(attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_FOREIGNID)), DBActionOrigin.FROM_DB);
                dictMeta.setSource(attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_SOURCE)), DBActionOrigin.FROM_DB);
                dictMeta.setState(attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_STATE)), DBActionOrigin.FROM_DB);
                dictMeta.setFieldValue(FIELD_DA_DICT_META_CREATE, attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_CREATE)), DBActionOrigin.FROM_DB);
                dictMeta.setFieldValue(FIELD_DA_DICT_META_CHANGE, attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_CHANGE)), DBActionOrigin.FROM_DB);
                dictMeta.setUserId(attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_USERID)), DBActionOrigin.FROM_DB);
                dictMeta.setDIALOGId(attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_DIALOGID)), DBActionOrigin.FROM_DB);
                dictMeta.setELDASId(attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_ELDASID)), DBActionOrigin.FROM_DB);
                return dictMeta;
            }
        });

        dataGrid.getTable().switchOnEventListeners();
        if (dataGrid.getTable().getRowCount() == 0) {
            dataGrid.showNoResultsLabel(dataGrid.getTable().getRowCount() == 0);
        } else {
            dataGrid.getTable().sortRowsAccordingToColumn(0, true);
            dataGrid.getTable().selectAllRows();
        }
        doEnableButtons();
    }

    private DBSQLQuery buildQuery(Fields fields) {
        // SQL Query mit Status in DICT Sprache ist leer und alle TextIds mit Präfix "IPARTS". Join auf Sprache um
        // das EtkMultiLang Objekt aufzubauen und nicht zu laden. Join auf DICT_META um die iPartsDataDictMeta Objekte aufzubauen.
        // Da wir in DA_DICT_SPRACHE sind brauchen wir die einzelnen Sprachen später nicht laden.
        DBSQLQuery query = getProject().getEtkDbs().getDBForDomain(MAIN).getNewQuery();
        query.select(fields).from(TABLE_DA_DICT_SPRACHE);
        Condition whereCondition = new Condition(FIELD_DA_DICT_SPRACHE_TRANS_STATE, Condition.OPERATOR_EQUALS, "");
        String likeValue = SQLUtils.wildcardExpressionToSQLLike(iPartsDictPrefixAndSuffix.DICT_IPARTS_PREFIX.getPrefixValue(), false, true, false);
        whereCondition.and(new Condition(FIELD_DA_DICT_SPRACHE_TEXTID, Condition.OPERATOR_LIKE, likeValue));
        whereCondition.and(new Condition(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_STATE), Condition.OPERATOR_EQUALS, iPartsDictConst.DICT_STATUS_RELEASED));
        query.where(whereCondition);
        Condition joinCondition = new Condition(TableAndFieldName.make(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_TEXTID), Condition.OPERATOR_EQUALS, new Fields(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_TEXTID)));
        joinCondition.and(new Condition(TableAndFieldName.make(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_SPRACH), Condition.OPERATOR_EQUALS, new Fields(TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_SPRACH))));
        query.join(new InnerJoin(TABLE_SPRACHE, joinCondition));
        joinCondition = new Condition(TableAndFieldName.make(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_TEXTID), Condition.OPERATOR_EQUALS, new Fields(TableAndFieldName.make(TABLE_DA_DICT_META, FIELD_DA_DICT_META_TEXTID)));
        query.join(new InnerJoin(TABLE_DA_DICT_META, joinCondition));
        query.orderBy(new String[]{ TableAndFieldName.make(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_TEXTID) });
        return query;
    }

    /**
     * Bestimmt die gefundenen Sprachen und setzt den Text danach (virtuelle Felder)
     *
     * @param dictMeta
     * @param attributes
     * @param fillTextLangs
     */
    private void fillAttributesWithLangData(iPartsDataDictMeta dictMeta, DBDataObjectAttributes attributes, boolean fillTextLangs) {
        // bei der Neuanlage einer Sprache verschieden DE wird im MultiLang ein leerer DE-Eintrag erzeugt
        List<String> langs = new DwList<>();
        for (iPartsDataDictLanguageMeta dataDictLangMeta : dictMeta.getLanguages()) {
            langs.add(dataDictLangMeta.getAsId().getLanguage());
        }
        if (fillTextLangs) {
            attributes.addField(PLACEHOLDER_TEXT_LANGS, StrUtils.stringListToString(langs, LANG_DELIMITER), DBActionOrigin.FROM_DB);
        }
        String lang;
        if (langs.contains(Language.DE.getCode())) {
            lang = Language.DE.getCode();
        } else {
            lang = Language.EN.getCode();
        }
        attributes.addField(PLACEHOLDER_TEXT_FIELD, dictMeta.getMultiLang().getTextByNearestLanguage(lang, langs), DBActionOrigin.FROM_DB);
    }

    private void fillLanguageAndMultiLang(iPartsDataDictMeta dictMeta, DBDataObjectAttributes attributes,
                                          Map<String, EtkMultiSprache> multiMap, String language) {
        String textId = dictMeta.getTextId();
        iPartsDataDictLanguageMeta dictLanguageMeta = new iPartsDataDictLanguageMeta(getProject(), new iPartsDictLanguageMetaId(textId, language));
        dictLanguageMeta.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        dictLanguageMeta.setFieldValue(FIELD_DA_DICT_SPRACHE_CREATE, attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_CREATE)), DBActionOrigin.FROM_DB);
        dictLanguageMeta.setFieldValue(FIELD_DA_DICT_SPRACHE_CHANGE, attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_CHANGE)), DBActionOrigin.FROM_DB);
        dictLanguageMeta.setState(attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_STATUS)), DBActionOrigin.FROM_DB);
        dictLanguageMeta.setFieldValue(FIELD_DA_DICT_SPRACHE_TRANS_JOBID, attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_TRANS_JOBID)), DBActionOrigin.FROM_DB);
        dictLanguageMeta.setFieldValue(FIELD_DA_DICT_SPRACHE_TRANS_STATE, attributes.getFieldValue(TableAndFieldName.make(TABLE_DA_DICT_SPRACHE, FIELD_DA_DICT_SPRACHE_TRANS_STATE)), DBActionOrigin.FROM_DB);
        dictMeta.addLanguageFromDB(dictLanguageMeta);

        EtkMultiSprache multiSprache = multiMap.get(dictMeta.getTextId());
        if (multiSprache == null) {
            multiSprache = new EtkMultiSprache();
            multiMap.put(textId, multiSprache);
            multiSprache.setTextId(textId);
        }
        // Wenn ein Langtext vorhanden ist, muss dieser verwendet werden
        multiSprache.setText(language, getProject().getEtkDbs().getLongTextFromAttributes(attributes,
                                                                                          TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN),
                                                                                          TableAndFieldName.make(TABLE_SPRACHE, FIELD_S_BENENN_LANG)));
    }

    /**
     * Erstellt die benötigten Anzeigefelder
     *
     * @return
     */
    private EtkDisplayFields buildDisplayFields() {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.addFeld(makeDisplayFieldWithText(PLACEHOLDER_TEXT_FIELD, "!!Text", true, false));
        displayFields.addFeld(makeDisplayFieldWithText(PLACEHOLDER_TEXT_LANGS, "!!Sprachen", false, false));
        displayFields.addFeld(makeDisplayFieldWithText(PLACEHOLDER_TEXT_TEXTKIND, "!!Textart", false, false));
        displayFields.addFeld(makeDisplayField(FIELD_DA_DICT_META_SOURCE, false, false));
        displayFields.addFeld(makeDisplayField(FIELD_DA_DICT_META_STATE, false, false));
        displayFields.addFeld(makeDisplayField(FIELD_DA_DICT_META_DIALOGID, false, false));
        displayFields.addFeld(makeDisplayField(FIELD_DA_DICT_META_ELDASID, false, false));
        displayFields.addFeld(makeDisplayField(FIELD_DA_DICT_META_CREATE, false, false));
        displayFields.addFeld(makeDisplayField(FIELD_DA_DICT_META_CHANGE, false, false));
        displayFields.addFeld(makeDisplayField(FIELD_DA_DICT_META_USERID, false, false));
        displayFields.addFeld(makeDisplayField(FIELD_DA_DICT_META_TEXTID, false, false));
        displayFields.loadStandards(getConfig());
        for (EtkDisplayField displayField : displayFields.getFields()) {
            if (displayField.getKey().getFieldName().equals(PLACEHOLDER_TEXT_FIELD)) {
                displayField.setWidth(40);
            } else {
                displayField.setWidth(0);
            }
        }
        return displayFields;
    }

    private EtkDisplayField makeDisplayField(String fieldName, boolean multiLang, boolean isArray) {
        EtkDisplayField displayField = new EtkDisplayField(TABLE_DA_DICT_META, fieldName, multiLang, isArray);
        displayField.setColumnFilterEnabled(true);
        return displayField;
    }

    private EtkDisplayField makeDisplayFieldWithText(String fieldName, String text, boolean multiLang, boolean isArray) {
        EtkDisplayField displayField = makeDisplayField(fieldName, multiLang, isArray);
        displayField.setDefaultText(false);
        displayField.setText(new EtkMultiSprache(text, new String[]{ TranslationHandler.getUiLanguage() }));
        return displayField;
    }

    private class DictionaryDataObjectGrid extends EditDataObjectGrid {

        private GuiMenuItem unifyMenuItem;
        private boolean hasEntries;

        public DictionaryDataObjectGrid(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm) {
            super(dataConnector, parentForm);
            setColumnFilterFactory(new ColumnFilterFactory(getProject()));
            hasEntries = false;
        }

        @Override
        protected void onTableSelectionChanged(Event event) {
            doEnableButtons();
        }

        @Override
        protected void onCellDblClicked(int row, int col, Event event) {
//                doEditTextId(event);
        }

        @Override
        protected void onHeaderDblClicked(int col, Event event) {
//                if (searchRunning) {
//                    stopSearch();
//                }
//                doGridHeaderDoubleClick(event);
        }


        @Override
        protected void createToolbarButtons(GuiToolbar toolbar) {
            ToolbarButtonMenuHelper.ToolbarMenuHolder holder;
            holder = getToolbarHelper().addToolbarButtonAndCreateMenu(EditToolbarButtonAlias.EDIT_WORK, getUITranslationHandler(), new MenuRunnable() {
                @Override
                public void run(Event event) {
                    doEditTextId(event);
                }
            });
            getContextMenu().addChild(holder.menuItem);
        }

        @Override
        protected void createContextMenuItems(GuiContextMenu contextMenu) {
            if (!contextMenu.getChildren().isEmpty()) {
                contextMenu.addChild(getToolbarHelper().createMenuSeparator("availableEntriesSeparator", getUITranslationHandler()));
            }
            unifyMenuItem = toolbarHelper.createMenuEntry("unify", "!!Vereinheitlichen...", null, new EventListener(Event.MENU_ITEM_EVENT) {
                @Override
                public void fire(Event event) {
                    doUnifyStatusValues();
                }
            }, getUITranslationHandler());
            contextMenu.addChild(unifyMenuItem);
        }

        public void doEnableToolbarButtons() {
            int selCount = getTable().getSelectedRowIndices().length;
            boolean singleSelected = selCount == 1;
            boolean multiSelected = selCount > 1;
            unifyMenuItem.setEnabled(multiSelected);
            getToolbarHelper().enableToolbarButtonAndMenu(EditToolbarButtonAlias.EDIT_WORK, getContextMenu(), singleSelected);
        }

        /**
         * Edit eines Eintrags (incl Speichern im CVhnageSet)
         * Update des Grids mit den veränderten Werten, incl neuer Sortierung (sortiert werden die Texte)
         * Bei Status-Änderungen wird der Eintrag entfernt
         *
         * @param event
         */
        private void doEditTextId(Event event) {
            List<IdWithType> dictMetaIds = dataGrid.getSelectedObjectIds(TABLE_DA_DICT_META);
            if (dictMetaIds.size() != 1) {
                return;
            }
            // DataDictMeta editieren
            iPartsDataDictMeta savedDataDictMeta = DictCreateTextIdForm.editNewTextIdForTransJob(DictShowTextForTranslation.this.getConnector(),
                                                                                                 DictShowTextForTranslation.this,
                                                                                                 (iPartsDictMetaId)dictMetaIds.get(0));
            if (savedDataDictMeta != null) {
                // Es wurde etwas verändert (Änderung bereits gespeichert)
                String metaStatus = savedDataDictMeta.getFieldValue(FIELD_DA_DICT_META_STATE);
                if (!metaStatus.equals(iPartsDictConst.DICT_STATUS_RELEASED)) {
                    // Status wurde geändert => die selektierte Zeile entfernen
                    deleteSelectedRows(true);
                } else {
                    // Die GuiTable updaten
                    DBDataObjectAttributes attributes = new DBDataObjectAttributes();
                    attributes.addField(FIELD_DA_DICT_META_STATE, metaStatus, DBActionOrigin.FROM_DB);
                    fillAttributesWithLangData(savedDataDictMeta, attributes, false);
                    attributes.addField(FIELD_DA_DICT_META_CHANGE, savedDataDictMeta.getFieldValue(FIELD_DA_DICT_META_CHANGE), DBActionOrigin.FROM_DB);
                    attributes.addField(FIELD_DA_DICT_META_USERID, savedDataDictMeta.getFieldValue(FIELD_DA_DICT_META_USERID), DBActionOrigin.FROM_DB);

                    updateGrid(updateGridEditFields, null, attributes);
                    // Neu sortieren und Selektion neu setzen (z.B. falls sich der Text am Anfang geändert hat)
                    List<IdWithType> selectedIds = getSelectedObjectIds(TABLE_DA_DICT_META);
                    getTable().sortRowsAccordingToColumn(0, true);
                    setSelectedObjectIds(selectedIds, TABLE_DA_DICT_META);
                }
            }
        }

        /**
         * Vereinheitlichen des Status. Veränderte Zeilen werden aus dem Grid entfernt (ohne neue Suche)
         */
        private void doUnifyStatusValues() {
            List<iPartsDataDictMeta> selectedList = getSelectedDictMetaList(true);
            EtkEditFields editFields = new EtkEditFields();
            addEditField(TABLE_DA_DICT_META, FIELD_DA_DICT_META_STATE, false, null, editFields);
            // Anzeige des Unify-Dialogs
            DBDataObjectAttributes attributes =
                    EditUserMultiChangeControls.showEditUserMultiChangeControlsForDictMeta(getConnector(), editFields,
                                                                                           selectedList);
            if (attributes != null) {
                String changedStatus = attributes.getField(FIELD_DA_DICT_META_STATE).getAsString();
                // Den Status nochmal abfragen
                if (!iPartsDictConst.DICT_STATUS_RELEASED.equals(changedStatus)) {
                    // DAIMLER-13234: im ChangeSet abspeichern
                    Set<DictTextKindTypes> dictTextKindTypes = new TreeSet<>();
                    EtkDataObjectList dataObjectList = changeDictMetaStatus(getSelectedDictMetaList(false), changedStatus, dictTextKindTypes);

                    getDbLayer().startTransaction();
                    try {
                        boolean saved = iPartsRevisionChangeSet.saveDataObjectListWithChangeSet(getProject(), dataObjectList, iPartsChangeSetSource.DICTIONARY);
                        if (saved) {
                            saved = dataObjectList.saveToDB(getProject());
                        }
                        if (saved) {
                            getDbLayer().commit();
                            deleteSelectedRows(true);

                            for (DictTextKindTypes textKindType : dictTextKindTypes) {
                                // Wurde ein Text hinzugefügt oder editiert, oder ein Status geändert, dann müssen die Caches geleert werden
                                ApplicationEvents.fireEventInAllProjectsAndAllClusters(new iPartsClearTextCacheEvent(textKindType));
                            }
                        } else {
                            getDbLayer().rollback();
                        }
                    } catch (Exception e) {
                        getDbLayer().rollback();
                        Logger.getLogger().handleRuntimeException(e);
                    }
                }
            }
        }

        private EtkDataObjectList changeDictMetaStatus(List<iPartsDataDictMeta> selectedList, String changedStatus,
                                                       Set<DictTextKindTypes> dictTextKindTypes) {
            EtkDataObjectList dataObjectList = new GenericEtkDataObjectList();
            dictTextKindTypes.clear();
            DictTxtKindIdByMADId dictTxtKindIdInstance = DictTxtKindIdByMADId.getInstance(getProject());
            for (iPartsDataDictMeta dictMeta : selectedList) {
                prepareDictMetaForSave(dictMeta, changedStatus, dictTxtKindIdInstance, dictTextKindTypes);
                dataObjectList.add(dictMeta, DBActionOrigin.FROM_EDIT);

                iPartsDictMetaId dictMetaId = dictMeta.getAsId();
                // Hier alle DictMeta Einträge mit gleicher Text-ID und anderer Textart suchen und ebenfalls anpassen, weil
                // es zu einer Text-ID mehrere Textarten geben kann
                iPartsDataDictMetaList allDictMetasForTextId = iPartsDataDictMetaList.loadMetaFromTextIdList(getProject(), dictMetaId.getTextId());
                allDictMetasForTextId.forEach(dictMetaWithSameTextId -> {
                    if (!dictMetaWithSameTextId.getAsId().equals(dictMetaId)) {
                        prepareDictMetaForSave(dictMetaWithSameTextId, changedStatus, dictTxtKindIdInstance, dictTextKindTypes);
                        dataObjectList.add(dictMetaWithSameTextId, DBActionOrigin.FROM_EDIT);
                    }
                });


            }
            return dataObjectList;
        }

        private void prepareDictMetaForSave(iPartsDataDictMeta dictMeta, String changedStatus,
                                            DictTxtKindIdByMADId dictTxtKindIdInstance, Set<DictTextKindTypes> dictTextKindTypes) {
            dictMeta.setState(changedStatus, DBActionOrigin.FROM_EDIT);
            for (iPartsDataDictLanguageMeta dataDictLanguageMeta : dictMeta.getLanguages()) {
                dictMeta.updateDictLanguageMeta(dataDictLanguageMeta);
            }
            dictMeta.removeForeignTablesAttributes();
            iPartsDataDictTextKind dataDictTextKind = dictTxtKindIdInstance.findDictTextKindByTextKindIdAndValidTransit(new iPartsDictTextKindId(dictMeta.getAsId().getTextKindId()), getProject());
            dictTextKindTypes.add(dataDictTextKind.getForeignTextKindType());
        }

        /**
         * Löschen der selektierten Zeilen aus dem Grid
         *
         * @param withSelectNew
         */
        private void deleteSelectedRows(boolean withSelectNew) {
            GuiTable table = getTable();
            int[] rowIndizes = table.getSelectedRowIndices();
            if (rowIndizes.length > 0) {
                // Table-Rows löschen
                table.clearSelection();
                List<IdWithType> idList = new DwList<>();
                table.switchOffEventListeners();
                try {
                    for (int lfdNr = rowIndizes.length - 1; lfdNr >= 0; lfdNr--) {
                        int index = rowIndizes[lfdNr];
                        EtkDataObject dataObject = getDataObjectForRowAndTable(lfdNr, TABLE_DA_DICT_META);
                        if (dataObject != null) {
                            idList.add(dataObject.getAsId());
                        }
                        table.removeRow(getTable().getRow(index));
                    }
                    // falls Spaltenfilterung an ist => ebenfalls bereinigen
                    removeEntries(TABLE_DA_DICT_META, idList);
                } finally {
                    table.switchOnEventListeners();
                }
                if (withSelectNew) {
                    table.setSelectedRow(rowIndizes[0], true);
                }
            }
        }

        /**
         * Entfernen der Einträge, falls der Spaltenfilter aktiv ist
         *
         * @param tableName
         * @param idList
         * @return
         */
        public boolean removeEntries(String tableName, List<IdWithType> idList) {
            if (hasEntries && !idList.isEmpty()) {
                // Spaltenfilter ist(war) aktiv => entferne die geforderten Zeilen
                List<GuiTableRowWithObjects> entries = super.getEntries();
                List<Integer> deleteIndizes = new TreeList();
                for (int lfdNr = 0; lfdNr < entries.size(); lfdNr++) {
                    GuiTableRowWithObjects row = entries.get(lfdNr);
                    EtkDataObject dataObject = row.getObjectForTable(tableName);
                    if ((dataObject != null) && idList.contains(dataObject.getAsId())) {
                        deleteIndizes.add(lfdNr);
                    }
                }
                if (!deleteIndizes.isEmpty()) {
                    for (int lfdNr = deleteIndizes.size() - 1; lfdNr >= 0; lfdNr--) {
                        entries.remove(entries.get(deleteIndizes.get(lfdNr)));
                    }
                    return true;
                }
            }
            return false;
        }

        /**
         * Update des Grids mit neuen Werten via UpdateGridHelper
         *
         * @param editFields
         * @param virtualFieldNamesList
         * @param attributes
         */
        protected void updateGrid(EtkEditFields editFields, List<String> virtualFieldNamesList, DBDataObjectAttributes attributes) {
            UpdateGridHelper helper = new UpdateGridHelper(getProject(), getTable(), getDisplayFields(), createUpdateEvent());
            helper.updateGrid(editFields, attributes, virtualFieldNamesList, null);
        }

        /**
         * Anlegen des Update-Events für UpdateGrid
         *
         * @return
         */
        protected OnUpdateGridEvent createUpdateEvent() {
            return new OnUpdateGridEvent() {

                @Override
                public DBDataObjectAttributes doCalculateVirtualFields(EtkProject project, DBDataObjectAttributes attributes) {
                    return attributes;
                }

                @Override
                public TableCellControlWithTextRepresentation doCalcGuiElemForCell(EtkProject project, EtkDisplayField field, DBDataObjectAttributes attributes) {
                    return calcGuiElemForCell(calcValue(field, attributes));
                }
            };
        }

        /**
         * Routine für die Bestimmung des Inhalts einer Zelle
         *
         * @param field
         * @param attributes
         * @return
         */
        protected String calcValue(EtkDisplayField field, DBDataObjectAttributes attributes) {
            String value = "";
            if (field.isVisible()) {
                String tableName = field.getKey().getTableName();
                String fieldName = field.getKey().getFieldName();
                value = getVisualValueOfFieldValue(tableName, fieldName, attributes.getField(fieldName));
            }
            return value;
        }

        /**
         * Einen Zelleninhalt anlegen
         *
         * @param value
         * @return
         */
        protected TableCellControlWithTextRepresentation calcGuiElemForCell(String value) {
            GuiLabel label = new GuiLabel(value);
            return new TableCellControlWithTextRepresentation(label, label::getTextRepresentation);
        }

        protected String getVisualValueOfFieldValue(String tableName, String fieldName, DBDataObjectAttribute fieldValue) {
            return getVisObject().asHtml(tableName, fieldName, fieldValue, getProject().getDBLanguage(), true).getStringResult();
        }

        @Override
        protected List<GuiTableRowWithObjects> getEntries() {
            List<GuiTableRowWithObjects> result = super.getEntries();
            hasEntries = (result != null);
            return result;
        }

        @Override
        protected void clearEntries() {
            hasEntries = false;
            super.clearEntries();
        }


        private class ColumnFilterFactory extends DataObjectColumnFilterFactory {

            public ColumnFilterFactory(EtkProject project) {
                super(project);
            }

            @Override
            protected boolean changeColumnTableFilterValues(int column, EditControlFactory editControl) {
                if (editControl.getValues().editCreateMode == EditCreateMode.ecmTableColumnFilter) {
                    String fieldName = editControl.getFieldName();
                    List<String> filterNames = new DwList<>(SPECIAL_TABLEFILTER_FIELDS);
                    if (filterNames.contains(fieldName)) {
                        // Trick um im Tabellenfilter ein SetOfEnum Eingabefeld zu erzeugen, dass als Tokens
                        // die Werte aus der zugehörigen Spalte der Tabelle enthält
                        editControl.getValues().field = editControl.getField().cloneMe();  // zur Sicherheit, damit die Originalwerte sich nicht ändern
                        editControl.getValues().field.setType(EtkFieldType.feSetOfEnum);  // behaupte, das Feld ist ein SetOfEnum
                        editControl.getOptions().handleAsSetOfEnum = true;  // und soll als SetOfEnum behandelt werden
                        editControl.getOptions().searchDisjunctive = true;
                        // Alles Weitere übernimmt EditControlFactory und das FilterInterface
                        AbstractGuiControl guiCtrl = EditControlFactory.doCreateEnumCheckBoxForTableColumnFilter(editControl.getValues(), editControl.getOptions());
                        if (guiCtrl != null) {
                            editControl.setControl(guiCtrl);
                        }
                        if (fieldName.equals(PLACEHOLDER_TEXT_LANGS)) {
                            // Spezialbehandlung für Sprachen, da hier die Sortierung anders ist
                            if (changeColumnTableFilterValuesForTextLangs(editControl)) {
                                // Spaltenfilterwerte speziell sortieren
                                SimpleMasterDataSearchFilterGrid.modifyLangTableFilterComboBox(getProject(), editControl.getControl(), false);
                                return true;
                            }
                            return false;
                        }
                    }
                }
                return super.changeColumnTableFilterValues(column, editControl);
            }
        }

        /**
         * Werte aus der Spalte Sprachen aufsammeln
         *
         * @param editControl
         * @return
         */
        protected boolean changeColumnTableFilterValuesForTextLangs(EditControlFactory editControl) {
            EtkFieldType fieldType = editControl.getField().getType();
            if (editControl.isHandleAsSetOfEnum() || (fieldType == EtkFieldType.feEnum) ||
                (fieldType == EtkFieldType.feSetOfEnum)) {
                Set<String> valueSet = new TreeSet<>();
                for (GuiTableRowWithObjects entry : getEntriesAsList()) {
                    EtkDataObject objectForTable = entry.getObjectForTable(editControl.getTableName());
                    if (objectForTable != null) {
                        String value = objectForTable.getFieldValue(editControl.getField().getName());
                        List<String> valueList = StrUtils.toStringList(value, LANG_DELIMITER, false, false);
                        for (String lang : valueList) {
                            if ((fieldType == EtkFieldType.feSetOfEnum) && !editControl.isHandleAsSetOfEnum()) {
                                valueSet.addAll(SetOfEnumDataType.parseSetofEnum(lang, true, false));
                            } else {
                                valueSet.add(lang);
                            }
                        }
                    }
                }
                AbstractGuiTableColumnFilterFactory.setColumnTableFilterValuesForEnum(editControl, valueSet, getProject());
                return true;
            }
            return false;
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
        private de.docware.framework.modules.gui.controls.GuiTitle title;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.GuiPanel panelMain;

        @SuppressWarnings({ "FieldCanBeLocal" })
        private de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel buttonpanel;

        private MainWindowClass(de.docware.framework.modules.gui.misc.translation.TranslationHandler translationHandler) {
            super();
            this.registerTranslationHandler(translationHandler);
            this.setScaleForResolution(true);
            this.setVisible(false);
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
            panelMain.setBorderWidth(4);
            de.docware.framework.modules.gui.layout.LayoutBorder panelMainLayout =
                    new de.docware.framework.modules.gui.layout.LayoutBorder();
            panelMain.setLayout(panelMainLayout);
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
            de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder buttonpanelConstraints =
                    new de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder();
            buttonpanelConstraints.setPosition("south");
            buttonpanel.setConstraints(buttonpanelConstraints);
            this.addChild(buttonpanel);
        }

    }
    /* END: DOCWARE GUI CODE, DO NOT MODIFY THIS COMMENT AND/OR CODE ABOVE THIS LINE */
    //</editor-fold>
}