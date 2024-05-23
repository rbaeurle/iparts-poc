package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.dictionary;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.forms.common.EditControl;
import de.docware.apps.etk.base.forms.events.OnDblClickEvent;
import de.docware.apps.etk.base.misc.images.DefaultImages;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectList;
import de.docware.apps.etk.base.project.common.EtkDataObjectArrayList;
import de.docware.apps.etk.base.project.mechanic.ids.PartListEntryId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsEditConfigConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsRight;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.footnotes.iPartsDataFootNoteCatalogueRefList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataCombTextList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataWireHarnessList;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dictionary.DictTextKindTypes;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.EditToolbarButtonMenuHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.events.OnEditChangeRecordEvent;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.SimpleMasterDataSearchFilterGrid;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.ExtendedRelatedInfoData;
import de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms.helper.iPartsGotoHelper;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonOnPanel;
import de.docware.framework.modules.gui.controls.menu.GuiContextMenu;
import de.docware.framework.modules.gui.controls.menu.GuiMenuItem;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.event.EventListenerOptions;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.util.ArrayUtil;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.TableAndFieldName;

import java.util.*;

/**
 * Verwendungssuche von SAA-Benennung und Ergänzungstexte
 */
public class DictDataSearchAddAndSAATextUsageForm extends SimpleMasterDataSearchFilterGrid {

    // K_VARI ist für den Sprung in die Retailstückliste zwingend erforderlich.
    // K_LFDNR ist ebenfalls für den Sprung erforderlich und wird bei Bedarf unsichtbar hinzugefügt.
    private static final String[] MUST_HAVE_FIELDS = new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VARI),
                                                                   TableAndFieldName.make(TABLE_KATALOG, FIELD_K_VER),
                                                                   TableAndFieldName.make(TABLE_KATALOG, FIELD_K_LFDNR) };

    public static void showUsage(String dictType, EtkDataObject searchValue, AbstractJavaViewerFormIConnector dataConnector,
                                 AbstractJavaViewerForm parentForm) {
        DictDataSearchAddAndSAATextUsageForm dlg = new DictDataSearchAddAndSAATextUsageForm(dataConnector, parentForm,
                                                                                            TABLE_KATALOG, null,
                                                                                            dictType, searchValue);
        dlg.showNonModal();
    }

    private final EtkDataObject searchValue;
    private final DictTextKindTypes dictType;
    private GuiMenuItem gotoUsageMenuItem;
    private GuiMenuItem gotoRetailMenuItem;
    private GuiMenuItem gotoWireHarnessMenuItem;
    private EventListener gotoRetailListener;
    private EventListener gotoWireHarnessListener;
    private final boolean isPSKAllowed = iPartsRight.checkPSKInSession();
    private final boolean hasBothCarAndTruckRights = iPartsRight.checkUserHasBothVehicleTypeRightsInSession();
    private final boolean hasNeitherCarNorTruckRights = !iPartsRight.checkCarAndVanInSession() && !iPartsRight.checkTruckAndBusInSession();
    private final HashMap<iPartsProductId, Boolean> productValidityMap;

    /**
     * Erzeugt eine Instanz von SimpleMasterDataSearchResultGrid.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector           Connector
     * @param parentForm              Dialog an dem dieser Dialog hängt
     * @param tableName               Tabellenname der Tabelle in der gesucht wird
     * @param onEditChangeRecordEvent Edit-Event
     * @param dictType                Textart zum unterscheiden, wie gesucht werden soll
     * @param searchValue             MultiLang-Objekt dessen Verwendung gesucht werden soll
     */
    public DictDataSearchAddAndSAATextUsageForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm,
                                                String tableName, OnEditChangeRecordEvent onEditChangeRecordEvent, String dictType,
                                                EtkDataObject searchValue) {
        super(dataConnector, parentForm, tableName, onEditChangeRecordEvent);
        getButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.OK, false);
        getButtonPanel().setButtonVisible(GuiButtonOnPanel.ButtonType.CLOSE, true);
        getButtonPanel().getButtonOnPanel(GuiButtonOnPanel.ButtonType.CLOSE).addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                close();
            }
        });

        OnDblClickEvent onDblClickEvent = this::goToUsage;

        // Der Doppelklick zum Öffnen der Konstruktions-Stückliste in einem nicht-modalen Fenster muss synchron abgearbeitet
        // werden, um Popup-Blocker zu vermeiden
        setOnDblClickEvent(onDblClickEvent);

        this.searchValue = searchValue;
        this.dictType = DictTextKindTypes.getTypeByName(dictType);
        productValidityMap = new HashMap<>();

        EtkDisplayFields searchFields = new EtkDisplayFields();
        EtkDisplayField searchField = createSearchField(getProject(), TABLE_SPRACHE, FIELD_S_BENENN,
                                                        true, false);
        searchFields.addFeld(searchField);
        searchField = createSearchField(getProject(), TABLE_DA_DICT_META, FIELD_DA_DICT_META_TEXTID,
                                        false, false);
        searchFields.addFeld(searchField);

        setSearchFields(searchFields);
        setDisplayResultFields(getDisplayFields(), true);
        setEditAllowed(false);
        setNewAllowed(false);
        setEditAllowed(false);
        showToolbar(false);

        setTitle(TranslationHandler.translate("!!Suche nach Verwendungen der %1", TranslationHandler.translate(this.dictType.getTextKindName())));
        setWindowName("DictDataSearchAddAndSAATextUsages");

        // Daten in die Suchfelder schreiben
        if (searchValue != null) {
            setSearchValues(searchValue.getAttributes());
        }
    }

    @Override
    protected void addToolbarButtonsAndContextMenuItems(EditToolbarButtonMenuHelper toolbarHelper, GuiContextMenu contextMenu) {
        super.addToolbarButtonsAndContextMenuItems(toolbarHelper, contextMenu);
        gotoRetailListener = new EventListener(Event.MENU_ITEM_EVENT, EventListenerOptions.SYNCHRON_EVENT) {
            @Override
            public void fire(Event event) {
                endSearch();
                goToUsage();
            }
        };
        gotoWireHarnessListener = new EventListener(Event.MENU_ITEM_EVENT, EventListenerOptions.SYNCHRON_EVENT) {
            @Override
            public void fire(Event event) {
                endSearch();
                goToWireHarness();
            }
        };
        gotoUsageMenuItem = toolbarHelper.createMenuEntry("goToUsage", "!!Gehe zur Verwendung...", DefaultImages.module.getImage(),
                                                          gotoRetailListener, getUITranslationHandler());
        contextMenu.addChild(gotoUsageMenuItem);
    }

    @Override
    protected void enableButtons() {
        super.enableButtons();
        int selectionRowCount = getTable().getSelectedRows().size();
        boolean singleSelection = selectionRowCount == 1;
        gotoUsageMenuItem.setEnabled(singleSelection);
        handleGotoUsageMenu(singleSelection);
    }

    private void handleGotoUsageMenu(boolean isEnabled) {
        if (isEnabled) {
            DBDataObjectAttributes attributes = getSelectedAttributes();
            if (attributes.fieldExists(FIELD_DWH_SUB_SNR)) {
                if (gotoUsageMenuItem.getChildren().isEmpty()) {
                    gotoUsageMenuItem.removeEventListener(gotoRetailListener);
                    gotoUsageMenuItem.removeAllChildren();
                    gotoUsageMenuItem.setText("!!Gehe zu...");
                    if (gotoRetailMenuItem == null) {
                        gotoRetailMenuItem = new GuiMenuItem();
                        gotoRetailMenuItem.setName("gotoRetailMenuItem");
                        gotoRetailMenuItem.setText("!!Verwendung");
                        gotoRetailMenuItem.addEventListener(gotoRetailListener);
                    }
                    gotoUsageMenuItem.addChild(gotoRetailMenuItem);
                    if (gotoWireHarnessMenuItem == null) {
                        gotoWireHarnessMenuItem = new GuiMenuItem();
                        gotoWireHarnessMenuItem.setName("gotoWireHarnessMenuItem");
                        gotoWireHarnessMenuItem.setText(RELATED_INFO_WIRE_HARNESS_TEXT);
                        gotoWireHarnessMenuItem.addEventListener(gotoWireHarnessListener);
                    }
                    gotoUsageMenuItem.addChild(gotoWireHarnessMenuItem);
                }
            } else {
                if (!gotoUsageMenuItem.getChildren().isEmpty()) {
                    gotoUsageMenuItem.removeAllChildren();
                    gotoUsageMenuItem.setText("!!Gehe zur Verwendung...");
                    gotoUsageMenuItem.addEventListener(gotoRetailListener);
                }
            }
        } else {
            gotoUsageMenuItem.setText("!!Gehe zur Verwendung...");
        }
    }

    @Override
    protected boolean executeExplicitSearch() {
        switch (dictType) {
            case SAA_NAME:
                searchDictSAATextUsages();
                break;
            case ADD_TEXT:
                searchDictAddTextUsages();
                break;
            case FOOTNOTE:
                searchDictFootNoteTextUsages();
                break;
            default:
                // Anzeige dictType not implemented
                clearGrid();
                break;
        }
        return true;
    }

    @Override
    protected void modifySearchControl(EtkDisplayField searchField, EditControl ctrl) {
        ctrl.getEditControl().setReadOnly(true);
    }

    @Override
    protected void prepareGuiForSearch() {
        super.prepareGuiForSearch();
        productValidityMap.clear();
    }

    @Override
    protected EtkDisplayFields getSelectFields() {
        EtkDisplayFields result = new EtkDisplayFields();
        for (EtkDisplayField displayField : getDisplayResultFields().getFields()) {
            result.addFeld(displayField.cloneMe());
        }
        return result;
    }

    private EtkDisplayFields getSpecialDisplayFields() {
        EtkDisplayFields result = new EtkDisplayFields();
        for (EtkDisplayField displayField : getDisplayResultFields().getFields()) {
            if (displayField.isArray() || displayField.isMultiLanguage()) {
                result.addFeld(displayField);
            }
        }
        return result;
    }

    private boolean needsTableInDisplayFields(String tableName) {
        for (EtkDisplayField displayField : getDisplayResultFields().getFields()) {
            if (displayField.getKey().getTableName().equals(tableName)) {
                return true;
            }
        }
        return false;
    }

    private EtkDisplayFields getDisplayFields() {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        displayFields.load(getConfig(), iPartsEditConfigConst.iPARTS_EDIT_TEXT_USAGE_LOCATION_KEY +
                                        iPartsEditConfigConst.REL_EDIT_MASTER_DISPLAYFIELDS);
        if (displayFields.size() == 0) {
            EtkDisplayField displayField = createDisplayFieldInternal(getProject(), TABLE_KATALOG, FIELD_K_VARI,
                                                                      0, false, false, true);
            displayFields.addFeld(displayField);

            // Width einschränken, bei den anderen besteht autoSize
            displayField = createDisplayFieldInternal(getProject(), TABLE_KATALOG, FIELD_K_LFDNR, 20,
                                                      false, false, false);
            displayFields.addFeld(displayField);
        }
        return displayFields;
    }

    private EtkDisplayField createDisplayFieldInternal(EtkProject project, String tableName, String fieldName, int width,
                                                       boolean mehrSprachig, boolean isArray,
                                                       boolean columnFilterEnabled) {
        EtkDisplayField displayField = createDisplayField(project, tableName, fieldName,
                                                          mehrSprachig, isArray);
        displayField.setColumnFilterEnabled(columnFilterEnabled);
        displayField.setDefaultWidth(false);
        displayField.setWidth(width);
        return displayField;
    }

    /**
     * Suche der Verwendungen von SAA-Benennungen in KATALOG über DWARRAY und dem K_SA_VALIDITY Feld
     */
    private void searchDictSAATextUsages() {
        EtkDataObjectList<? extends EtkDataObject> list = new EtkDataObjectArrayList();
        list.setSearchWithoutActiveChangeSets(false);
        list.clear(DBActionOrigin.FROM_DB);

        String saaTextId = searchValue.getFieldValue(FIELD_DA_DICT_META_TEXTID);
        boolean needsMatData = needsTableInDisplayFields(TABLE_MAT);
        EtkDataObjectList.JoinData[] joinData = buildJoinData(needsMatData, true, false);
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DWARRAY, FIELD_DWA_FELD),
                                                     TableAndFieldName.make(TABLE_DA_SAA, FIELD_DS_DESC) };
        String[] whereValues = new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_SA_VALIDITY),
                                             saaTextId };
        EtkDisplayFields selectFields = getSelectFields();
        addMustFields(selectFields);
        String[] sortFields = new String[]{ FIELD_K_VARI, FIELD_K_LFDNR };
        Map<String, Set<String>> moduleToLfdNr = new HashMap<>();
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = getFoundAttributesCallback(moduleToLfdNr, FIELD_K_VARI, FIELD_K_LFDNR);

        boolean[] searchCaseInsensitives = new boolean[whereTableAndFields.length];
        Arrays.fill(searchCaseInsensitives, false);

        list.searchSortAndFillWithJoin(getProject(), null,
                                       selectFields,
                                       whereTableAndFields,
                                       whereValues,
                                       false,
                                       sortFields, true, searchCaseInsensitives,
                                       true, true, false,
                                       foundAttributesCallback, true,
                                       joinData);
        handleResults(list, moduleToLfdNr);
    }


    /**
     * Suche der Verwendungen von Ergänzungstexten in KATALOG
     */
    private void searchDictAddTextUsages() {
        EtkDataObjectList<? extends EtkDataObject> list = new iPartsDataCombTextList();
        list.setSearchWithoutActiveChangeSets(false);
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_COMB_TEXT, FIELD_DCT_DICT_TEXT) };
        String[] whereValues = new String[]{ searchValue.getFieldValue(FIELD_DA_DICT_META_TEXTID) };
        String[] sortFields = new String[]{ FIELD_DCT_MODULE, FIELD_DCT_SEQNO };
        boolean needsMatData = needsTableInDisplayFields(TABLE_MAT);
        EtkDataObjectList.JoinData[] joinData = buildJoinData(needsMatData, false, false);
        EtkDisplayFields selectFields = getSelectFields();
        addMustFields(selectFields);

        Map<String, Set<String>> moduleToLfdNr = new HashMap<>();
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = getFoundAttributesCallback(moduleToLfdNr, FIELD_DCT_MODULE,
                                                                                                       FIELD_DCT_SEQNO);
        boolean[] searchCaseInsensitives = new boolean[whereTableAndFields.length];
        Arrays.fill(searchCaseInsensitives, false);
        list.searchSortAndFillWithJoin(getProject(), null,
                                       selectFields,
                                       whereTableAndFields,
                                       whereValues,
                                       false,
                                       sortFields,
                                       false, searchCaseInsensitives, true,
                                       true, false,
                                       foundAttributesCallback,
                                       true,
                                       joinData);
        if (!list.isEmpty()) {
            handleResults(list, moduleToLfdNr);
        }
        searchDictWireHarnessAddTextUsages();
    }

    private void searchDictWireHarnessAddTextUsages() {
        EtkDataObjectList<? extends EtkDataObject> list = new iPartsDataWireHarnessList();
        list.setSearchWithoutActiveChangeSets(false);
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_WIRE_HARNESS, FIELD_DWH_CONTACT_ADD_TEXT) };
        String[] whereValues = new String[]{ searchValue.getFieldValue(FIELD_DA_DICT_META_TEXTID) };
        boolean needsMatData = needsTableInDisplayFields(TABLE_MAT);
        EtkDataObjectList.JoinData[] joinData = buildJoinData(needsMatData, false, true);
        EtkDisplayFields selectFields = getSelectFields();
        addMustFields(selectFields);
        String[] sortFields = new String[]{ FIELD_K_VARI, FIELD_K_LFDNR };

        boolean[] searchCaseInsensitives = new boolean[whereTableAndFields.length];
        Arrays.fill(searchCaseInsensitives, false);
        Map<String, Map<String, Set<String>>> moduleToMatToLfdNrMap = new HashMap<>();
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = getFoundAttributesWireHarnessCallback(moduleToMatToLfdNrMap, FIELD_K_VARI,
                                                                                                                  FIELD_DWH_SNR, FIELD_K_LFDNR);

        list.searchSortAndFillWithJoin(getProject(), null,
                                       selectFields,
                                       whereTableAndFields,
                                       whereValues,
                                       false,
                                       sortFields,
                                       false, searchCaseInsensitives, true,
                                       true, false,
                                       foundAttributesCallback,
                                       true,
                                       joinData);
        handleWireHarnessResults(list, moduleToMatToLfdNrMap, FIELD_DWH_SNR);
    }

    /**
     * Suche der Verwendungen von Fußnoten in KATALOG
     */
    private void searchDictFootNoteTextUsages() {
        EtkDataObjectList<? extends EtkDataObject> list = new iPartsDataFootNoteCatalogueRefList();
        list.setSearchWithoutActiveChangeSets(false);
        String fnTextId = searchValue.getFieldValue(FIELD_DA_DICT_META_TEXTID);
        String fnId = getFnIdFromTextId(fnTextId);
        if (StrUtils.isEmpty(fnId)) {
            return;
        }
        String[] whereTableAndFields = new String[]{ TableAndFieldName.make(TABLE_DA_FN_KATALOG_REF, FIELD_DFNK_FNID) };
        String[] whereValues = new String[]{ fnId };
        String[] sortFields = new String[]{ FIELD_DFNK_MODULE, FIELD_DFNK_SEQNO, FIELD_DFNK_FN_SEQNO };
        boolean needsMatData = needsTableInDisplayFields(TABLE_MAT);
        EtkDataObjectList.JoinData[] joinData = buildJoinData(needsMatData, false, false);
        EtkDisplayFields selectFields = getSelectFields();
        addMustFields(selectFields);

        Map<String, Set<String>> moduleToLfdNr = new HashMap<>();
        EtkDataObjectList.FoundAttributesCallback foundAttributesCallback = getFoundAttributesCallback(moduleToLfdNr, FIELD_DFNK_MODULE,
                                                                                                       FIELD_DFNK_SEQNO);
        boolean[] searchCaseInsensitives = new boolean[whereTableAndFields.length];
        Arrays.fill(searchCaseInsensitives, false);
        list.searchSortAndFillWithJoin(getProject(), null,
                                       selectFields,
                                       whereTableAndFields,
                                       whereValues,
                                       false,
                                       sortFields,
                                       false, searchCaseInsensitives, true,
                                       true, false,
                                       foundAttributesCallback,
                                       true,
                                       joinData);

        if (!list.isEmpty()) {
            handleResults(list, moduleToLfdNr);
        }
    }

    private String getFnIdFromTextId(String textId) {
        String dbField = TableAndFieldName.make(TABLE_DA_FN_CONTENT, FIELD_DFNC_TEXT);
        String[] whereFields = new String[]{ TableAndFieldName.getFieldName(dbField) };
        String[] whereValues = new String[]{ textId };

        DBDataObjectAttributesList attributesList = getProject().getDbLayer().getAttributesList(TableAndFieldName.getTableName(dbField),
                                                                                                whereFields, whereValues);
        if (!attributesList.isEmpty()) {
            return attributesList.get(0).getField(FIELD_DFNC_FNID).getAsString();
        }
        return "";

    }

    private EtkDataObjectList.JoinData[] buildJoinData(boolean needsMatData, boolean needsSAAData, boolean isWireHarness) {
        List<EtkDataObjectList.JoinData> helper = new DwList<>();
        List<String> tableNames = new DwList<>();
        tableNames.add(TABLE_KATALOG);
        if (needsMatData) {
            tableNames.add(TABLE_MAT);
        }
        if (needsSAAData) {
            tableNames.add(TABLE_DA_SAA);
        }
        for (String tableName : tableNames) {
            EtkDataObjectList.JoinData joinData = getJoinData(tableName, isWireHarness);
            if (joinData != null) {
                helper.add(joinData);
            }
        }
        if (!helper.isEmpty()) {
            return ArrayUtil.toArray(helper);
        } else {
            return null;
        }
    }

    private EtkDataObjectList.JoinData getJoinData(String tableName, boolean isWireHarness) {
        switch (tableName) {
            case TABLE_KATALOG:
                switch (dictType) {
                    case SAA_NAME:
                        // Die Join-Felder aus KATALOG
                        return new EtkDataObjectList.JoinData(TABLE_KATALOG,
                                                              new String[]{ FIELD_DWA_ARRAYID },
                                                              new String[]{ FIELD_K_SA_VALIDITY },
                                                              false, false);
                    case ADD_TEXT:
                        if (!isWireHarness) {
                            // Die Join-Felder aus KATALOG
                            return new EtkDataObjectList.JoinData(TABLE_KATALOG,
                                                                  new String[]{ FIELD_DCT_MODULE, FIELD_DCT_SEQNO },
                                                                  new String[]{ FIELD_K_VARI, FIELD_K_LFDNR },
                                                                  false, false);
                        } else {
                            return new EtkDataObjectList.JoinData(TABLE_KATALOG,
                                                                  new String[]{ TableAndFieldName.make(TABLE_DA_WIRE_HARNESS, FIELD_DWH_SNR) },
                                                                  new String[]{ FIELD_K_MATNR },
                                                                  false, false);
                        }
                    case FOOTNOTE:
                        // Die Join-Felder aus KATALOG
                        return new EtkDataObjectList.JoinData(TABLE_KATALOG,
                                                              new String[]{ FIELD_DFNK_MODULE, FIELD_DFNK_SEQNO },
                                                              new String[]{ FIELD_K_VARI, FIELD_K_LFDNR },
                                                              false, false);
                }
                break;
            case TABLE_MAT:
                switch (dictType) {
                    case SAA_NAME:
                    case ADD_TEXT:
                    case FOOTNOTE:
                        // Die Join-Felder aus MAT
                        return new EtkDataObjectList.JoinData(TABLE_MAT,
                                                              new String[]{ TableAndFieldName.make(TABLE_KATALOG, FIELD_K_MATNR) },
                                                              new String[]{ FIELD_M_BESTNR },
                                                              false, false);
                }
                break;
            case TABLE_DA_SAA:
                switch (dictType) {
                    case SAA_NAME:
                        // Die Join-Felder aus DA_SAA
                        return new EtkDataObjectList.JoinData(TABLE_DA_SAA,
                                                              new String[]{ FIELD_DWA_TOKEN },
                                                              new String[]{ FIELD_DS_SAA },
                                                              true, false);
                }
                break;
        }
        return null;
    }

    /**
     * Verwendungen nur pro Modul ausgegeben und nicht pro lfdNr
     * Die verschiedenen lfdNr werden später komma-separiert in einem Feld angezeigt
     *
     * @param moduleToLfdNrMap Sammeln der LfdNr der Verwendung zum Modul
     * @param kVariField       Feldname zum Auslesen der KVari
     * @param kLfdNrField      Feldname zum Auslesen der KLfdNr
     * @return Der erstellte Callback wird zurückgegeben
     */
    private EtkDataObjectList.FoundAttributesCallback getFoundAttributesCallback(Map<String, Set<String>> moduleToLfdNrMap,
                                                                                 String kVariField, String kLfdNrField) {
        return new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                if (searchThread.wasCanceled() || !searchIsRunning) {
                    return false;
                }
                String moduleNo = attributes.getFieldValue(kVariField);
                String lfdNr = attributes.getFieldValue(kLfdNrField);
                Set<String> lfdNrs = moduleToLfdNrMap.computeIfAbsent(moduleNo, k -> new TreeSet<>());
                boolean result = lfdNrs.isEmpty(); // Neu gefundenes Modul
                lfdNrs.add(lfdNr);
                return result;
            }
        };
    }

    private EtkDataObjectList.FoundAttributesCallback getFoundAttributesWireHarnessCallback(Map<String, Map<String, Set<String>>> moduleToMatToLfdNrMap,
                                                                                            String kVariField, String matField, String kLfdNrField) {
        return new EtkDataObjectList.FoundAttributesCallback() {
            @Override
            public boolean foundAttributes(DBDataObjectAttributes attributes) {
                if (searchThread.wasCanceled() || !searchIsRunning) {
                    return false;
                }
                String moduleNo = attributes.getFieldValue(kVariField);
                String matNo = attributes.getFieldValue(matField);
                String lfdNr = attributes.getFieldValue(kLfdNrField);
                Map<String, Set<String>> matToLfdNrMap = moduleToMatToLfdNrMap.computeIfAbsent(moduleNo, k -> new HashMap<>());
                Set<String> lfdNrs = matToLfdNrMap.computeIfAbsent(matNo, k -> new TreeSet<>());
                boolean result = lfdNrs.isEmpty(); // Neu gefundenes Modul
                lfdNrs.add(lfdNr);
                return result;
            }
        };
    }

    /**
     * Prüfen, ob der Autor die gefundenen Module überhaupt sehen kan (PKW/Truck)
     * Feld der laufenden Nummern mit allen gefundenen Nummern auffüllen
     * Array Felder behandeln
     * Ergebnisse ins Grid schreiben
     *
     * @param list          Ergebnisliste
     * @param moduleToLfdNr Gesammelte LfdNr der Verwendung zum Modul
     */
    private void handleResults(EtkDataObjectList<? extends EtkDataObject> list, Map<String, Set<String>> moduleToLfdNr) {
        getTable().switchOffEventListeners();
        try {
            EtkDisplayFields specialDisplayFields = getSpecialDisplayFields();
            for (EtkDataObject dataObject : list) {
                String moduleNo = dataObject.getFieldValue(FIELD_K_VARI);
                boolean isModuleVisible = iPartsFilterHelper.isModuleVisibleForUserSession(getProject(), isPSKAllowed,
                                                                                           hasBothCarAndTruckRights,
                                                                                           hasNeitherCarNorTruckRights,
                                                                                           moduleNo, productValidityMap);
                if (isModuleVisible) {
                    fillFieldKLfdNr(dataObject, moduleToLfdNr, FIELD_K_VARI);
                    DBDataObjectAttributes attributes = handleSpecialFields(dataObject, specialDisplayFields);
                    addFoundAttributes(attributes);
                }
            }
        } finally {
            Runnable runnable = () -> {
                showNoResultsLabel(getTable().getRowCount() == 0, false);
                showResultCount();
            };
            Session.invokeThreadSafeInSession(runnable);
            getTable().switchOnEventListeners();
        }
    }

    private void handleWireHarnessResults(EtkDataObjectList<? extends EtkDataObject> list, Map<String, Map<String, Set<String>>> moduleToMatToLfdNrMap,
                                          String matNoField) {
        getTable().switchOffEventListeners();
        try {
            EtkDisplayFields specialDisplayFields = getSpecialDisplayFields();
            for (EtkDataObject dataObject : list) {
                String moduleNo = dataObject.getFieldValue(FIELD_K_VARI);
                boolean isModuleVisible = iPartsFilterHelper.isModuleVisibleForUserSession(getProject(), isPSKAllowed,
                                                                                           hasBothCarAndTruckRights,
                                                                                           hasNeitherCarNorTruckRights,
                                                                                           moduleNo, productValidityMap);
                if (isModuleVisible) {
                    Map<String, Set<String>> matNoToLfdNrMap = moduleToMatToLfdNrMap.get(moduleNo);
                    if (matNoToLfdNrMap != null) {
                        fillFieldKLfdNr(dataObject, matNoToLfdNrMap, matNoField);
                        DBDataObjectAttributes attributes = handleSpecialFields(dataObject, specialDisplayFields);
                        addFoundAttributes(attributes);
                    }
                }
            }

        } finally {
            Runnable runnable = () -> {
                showNoResultsLabel(getTable().getRowCount() == 0, false);
                showResultCount();
            };
            Session.invokeThreadSafeInSession(runnable);
            getTable().switchOnEventListeners();
        }
    }

    private DBDataObjectAttributes handleSpecialFields(EtkDataObject dataObject, EtkDisplayFields specialDisplayFields) {
        DBDataObjectAttributes attributes = dataObject.getAttributes();
        for (EtkDisplayField displayField : specialDisplayFields.getFields()) {
            if (displayField.isArray()) {
                String fieldName = displayField.getKey().getFieldName();
                String arrayId = attributes.getFieldValue(fieldName);
                attributes.getField(fieldName).setIdForArray(arrayId, DBActionOrigin.FROM_DB);
            } else if (displayField.isMultiLanguage()) {
                String fieldName = displayField.getKey().getFieldName();
                String textId = attributes.getFieldValue(fieldName);
                attributes.getField(fieldName).setTextIdForMultiLanguage(textId, textId, DBActionOrigin.FROM_DB);
            }
        }
        return attributes;
    }

    /**
     * Sicherstellen, dass die Primärschlüsselfelder für den Stücklisteneintrag vorhanden sind, um die Informationen
     * für den Sprung zu gewährleisten
     *
     * @param selectFields Die Felder, die schon vorhanden sind
     */
    private void addMustFields(EtkDisplayFields selectFields) {
        for (String tableAndFieldName : MUST_HAVE_FIELDS) {
            selectFields.addFeldIfNotExists(new EtkDisplayField(tableAndFieldName, false, false));
        }
    }

    /**
     * Alle lfdNr der Verwendungen werden komma-separiert in das Feld für die KLfdNr geschrieben
     *
     * @param dataObject         Das Objekt mit den auszugebenden Attributen
     * @param moduleToLfdNr      Gesammelte LfdNr der Verwendung zum Modul
     * @param moduleOrMatNoField
     */
    private void fillFieldKLfdNr(EtkDataObject dataObject, Map<String, Set<String>> moduleToLfdNr, String moduleOrMatNoField) {
        String moduleOrMatNo = dataObject.getFieldValue(moduleOrMatNoField);
        Set<String> lfdNrs = moduleToLfdNr.get(moduleOrMatNo);
        if (lfdNrs != null) {
            String lfdNrsString = StrUtils.stringListToString(lfdNrs, ", ");
            dataObject.setFieldValue(FIELD_K_LFDNR, lfdNrsString, DBActionOrigin.FROM_DB);
        }
    }

    /**
     * Der Sprung zur Verwendung
     */
    private void goToUsage() {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if ((attributes != null) && !attributes.isEmpty()) {
            String moduleNo = attributes.getFieldValue(FIELD_K_VARI);
            String lfdNr = "";
            List<String> kLfdNrs = StrUtils.toStringList(attributes.getFieldValue(FIELD_K_LFDNR), ", ", false, false);
            if (!kLfdNrs.isEmpty()) {
                lfdNr = kLfdNrs.get(0);
            }
            iPartsGotoHelper.gotoModuleWithDifferentOptions(getConnector(), this, getProject(), moduleNo, lfdNr);
        }
    }

    /**
     * Der Sprung zum Leitungssatzbaukasten
     */
    private void goToWireHarness() {
        DBDataObjectAttributes attributes = getSelectedAttributes();
        if ((attributes != null) && !attributes.isEmpty()) {
            String moduleNo = attributes.getFieldValue(FIELD_K_VARI);
            String lfdNr = "";
            List<String> kLfdNrs = StrUtils.toStringList(attributes.getFieldValue(FIELD_K_LFDNR), ", ", false, false);
            if (!kLfdNrs.isEmpty()) {
                lfdNr = kLfdNrs.get(0);
            }
            if (StrUtils.isEmpty(lfdNr)) {
                // ohne selektierten PartListEntry hat der Sprung zur RelatedInfo keinen Sinn
                iPartsGotoHelper.gotoModuleWithDifferentOptions(getConnector(), this, getProject(), moduleNo, lfdNr);
            } else {
                // DAIMLER-13841: Absprung anpassen
                ExtendedRelatedInfoData relatedInfoData = new ExtendedRelatedInfoData();
                relatedInfoData.setActiveInfo(CONFIG_KEY_RELATED_INFO_WIRE_HARNESS_DATA);
                relatedInfoData.setSelectTableAndFieldName(TableAndFieldName.make(TABLE_DA_WIRE_HARNESS, FIELD_DWH_SUB_SNR));
                relatedInfoData.addSelectValue(attributes.getFieldValue(FIELD_DWH_SUB_SNR));
                PartListEntryId partListEntryId = new PartListEntryId(moduleNo, "", lfdNr);

                iPartsGotoHelper.gotoPathNewWindowWithRelatedInfo(getConnector(), partListEntryId, relatedInfoData);
            }
        }
    }
}
