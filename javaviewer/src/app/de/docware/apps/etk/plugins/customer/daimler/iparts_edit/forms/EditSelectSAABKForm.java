/*
 * Copyright (c) 2018 Docware GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.partlist.EtkDisplayField;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.forms.AbstractJavaViewerFormIConnector;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.base.project.base.EtkDataObject;
import de.docware.apps.etk.base.project.base.EtkDataObjectFactory;
import de.docware.apps.etk.base.project.mechanic.EtkDataPart;
import de.docware.apps.etk.base.project.mechanic.ids.PartId;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsDataSaa;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.construction.iPartsSaaId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSAAModels;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataVirtualFieldsDefinition;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsSAAModelsId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.forms.EditSelectDataObjectsForm;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsNumberHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProduct;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsProductId;
import de.docware.framework.modules.config.db.EtkDatabaseTable;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.db.DBDataObjectAttribute;
import de.docware.framework.modules.db.DBDataSet;
import de.docware.framework.modules.db.DBSQLQuery;
import de.docware.framework.modules.db.etkrecord.EtkRecord;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiLabel;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTextField;
import de.docware.framework.modules.gui.dialogs.ModalResult;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogButtons;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialogIcon;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsBorder;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.threads.FrameworkThread;
import de.docware.framework.modules.gui.misc.translation.TranslationHandler;
import de.docware.framework.modules.gui.session.Session;
import de.docware.framework.utils.EtkMultiSprache;
import de.docware.util.StrUtils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.sql.SQLQuery;
import de.docware.util.sql.SQLUtils;
import de.docware.util.sql.terms.*;

import java.sql.SQLException;
import java.util.*;

import static de.docware.framework.modules.db.DBDatabaseDomain.MAIN;

/**
 * Formular für die Auswahl von SAA und BK Gültigkeiten einer Stücklistenposition
 */
public class EditSelectSAABKForm extends EditSelectDataObjectsForm {

    public static final int MAX_LENGTH_MODELS_FOR_TITLE = 100;
    public static final int SEARCH_DELAY_FOR_INPUT = 300;
    public static final int SEARCH_MIN_CHARACTERS = 4;
    public static final int MAX_SEARCH_RESULTS = 1000;
    public static final String SAA_NOT_EXIST_TEXT_KEY = "SAA_NOT_EXIST_TEXT_KEY";

    public static Collection<String> showSelectionSaaBk(AbstractJavaViewerForm parentForm, iPartsProductId productId,
                                                        Collection<String> modelList, Collection<String> selectedSaaBk, boolean showSearch) {
        EditSelectSAABKForm dlg = new EditSelectSAABKForm(parentForm.getConnector(), parentForm, productId);
        if ((modelList != null) && !modelList.isEmpty()) {
            dlg.setTitle(TranslationHandler.translate("!!SAA/BK auswählen (Verknüpfte Baumuster: \"%1\")",
                                                      StrUtils.makeAbbreviation(StrUtils.stringListToString(modelList, ", "),
                                                                                MAX_LENGTH_MODELS_FOR_TITLE)));
        } else if (productId != null) {
            dlg.setTitle(TranslationHandler.translate("!!SAA/BK auswählen (Alle Baumuster des Produkts \"%1\")",
                                                      productId.getProductNumber()));
        } else {
            dlg.setTitle(TranslationHandler.translate("!!Kein Produkt oder vorausgewählte Baumuster für die SAA/BK Auswahl vorhanden."));
        }
        dlg.setSAABKNumberSearchFieldVisible(showSearch);
        // Baumuster für das Auffinden aller SAA/BK definieren
        List<String> handledModelList = handleModelListForAvailableSaaOrBk(parentForm.getConnector().getProject(), productId, modelList);

        // Falls SAA/BK Gültigkeiten schon existieren, dann diese setzen
        if (selectedSaaBk == null) {
            selectedSaaBk = new DwList<String>();
        }
        dlg.fillSelectedSaaBks(selectedSaaBk);

        // Mögliche SAA/BK an das Grid übergeben
        dlg.fillAvailableSaaBks(handledModelList);

        if (dlg.showModal() == ModalResult.OK) {
            return dlg.getSelectedSaaBks();
        }
        return null;
    }

    /**
     * Gibt die Baumuster zurück, in denen nach SAA und BKs gesucht werden soll. Exitsieren schon Baumuster für die
     * Baumuster-Gültigkeit, werden diese herangezogen. Ansonsten werden alle Baumuster des Produkts verwendet.
     *
     * @param productId
     * @param project
     * @param modelList @return
     */
    private static List<String> handleModelListForAvailableSaaOrBk(EtkProject project, iPartsProductId productId, Collection<String> modelList) {
        if ((modelList != null) && !modelList.isEmpty()) {
            return new ArrayList<String>(modelList);
        } else if (productId != null) {
            return getModelsFromProduct(project, productId);
        } else {
            return null;
        }
    }

    public static List<String> getModelsFromProduct(EtkProject project, iPartsProductId productId) {
        // Umbau auf Product
        if (productId == null) {
            return null;
        }
        List<String> modelListFromProduct = new DwList<String>();
        iPartsProduct product = iPartsProduct.getInstance(project, productId);
        Set<String> modelList = product.getModelNumbers(project);
        for (String modelNumber : modelList) {
            modelListFromProduct.add(modelNumber);
        }
        return modelListFromProduct;
    }


    private GuiPanel panelInput;
    private GuiTextField textfieldAddValue;
    private GuiButton buttonAddValue;
    private GuiPanel numberSearchPanel;
    private iPartsGuiDelayTextField numberSearchTextField;
    private GuiButton searchButton;
    private GuiButton showAllButton; // Button um alle SAA/BK anzuzeigen (sofern mehr als 1000 vorhanden sind
    private iPartsNumberHelper numberHelper;
    private List<String> handledModelNoList;
    private iPartsProductId productId;
    private List<String> initialSaaBkValues;
    Map<String, EtkMultiSprache> descMap = new HashMap<String, EtkMultiSprache>();
    private volatile FrameworkThread searchThread = null;
    private String defaultTextNotFound = null;
    protected int maxResults = MAX_SEARCH_RESULTS;
    protected volatile boolean interruptedByMaxResults = false;
    private int saaBkCount = -1; // Anzahl SAA/BK für alle übergebenen Module

    /**
     * Erzeugt eine Instanz von EditSelectDataObjectsForm.
     * Den $$internalCreateGui$$() Aufruf nicht ändern!
     *
     * @param dataConnector
     * @param parentForm
     * @param productId
     */
    public EditSelectSAABKForm(AbstractJavaViewerFormIConnector dataConnector, AbstractJavaViewerForm parentForm, iPartsProductId productId) {
        super(dataConnector, parentForm, iPartsConst.TABLE_DA_EDS_SAA_MODELS, "", "");
        setName("SelectSAABKForm");
        this.numberHelper = new iPartsNumberHelper();
        this.productId = productId;
        this.initialSaaBkValues = new ArrayList<String>();
        this.handledModelNoList = new DwList<String>();
        addInputArea();
        setAvailableEntriesTitle("!!Verfügbare SAA/BK:");
        setSelectedEntriesTitle("!!Ausgewählte SAA/BK:");
        availableEntriesGrid.setNoResultsLabelText("!!Keine SAA/BK vorhanden");
        selectedEntriesGrid.setNoResultsLabelText("!!Keine SAA/BK ausgewählt");
        setWithDeleteEntry(false);
        setNoDoubles(true);
        setMoveEntriesVisible(false);
        availableEntriesGrid.setPageSplitNumberOfEntriesPerPage(iPartsConst.MAX_SELECT_SEARCH_RESULTS_SIZE);
    }

    /**
     * Definiert alle möglichen SAA und BKs, die für die Gültigkeit herangezogen werden können. Für jedes Baumuster
     * wird in DA_EDS_SAA_MODELS geschaut, ob zugehörige SAA bzw. BKs existieren. Falls ja, wird nach einer Fallunterscheidung
     * die zugehörige Benennung aus der DB geholt. Texte für SAAs sind in DA_SAA und für BKs in MAT enthalten.
     *
     * @param modelList
     */
    public void fillAvailableSaaBks(final List<String> modelList) {
        handledModelNoList = modelList;
        searchButton.setVisible(true);
        availableEntriesGrid.showNoResultsLabel(false);

        Session.invokeThreadSafeInSession(() -> {
            // Anzahl aller SAA/BK für die übergebene Modulliste berechnen
            if (saaBkCount < 0) {
                saaBkCount = getQueryCount(modelList);
            }
            // Button nur anzeigen, wenn mehr als "MAX_SEARCH_RESULTS" SAA/BK gefunden wurden
            showAllButton.setVisible(saaBkCount > MAX_SEARCH_RESULTS);
            if (saaBkCount > MAX_SEARCH_RESULTS) {
                showAllButton.setText(TranslationHandler.translate("!!Alle %1 SAA/BK anzeigen", String.valueOf(saaBkCount)));
                // Min. Anzahl Zeichen bei Suche nur einstellen, wenn mehr als "MAX_SEARCH_RESULTS" SAA/BK gefunden wurden
                numberSearchTextField.setMinCharForSearch(SEARCH_MIN_CHARACTERS);
                availableEntriesGrid.clearGrid();
                setNotFoundText("Zuviele Treffer beim direkten Laden. Benutzen Sie die Suche.");
                availableEntriesGrid.showNoResultsLabel(true);
            } else {
                // Bei weniger als "MAX_SEARCH_RESULTS" SAA/BK die Treffer gleich laden
                searchAndFillAvailableSAABKs();
            }
            searchThread = null;
            searchButton.setVisible(false);
            doEnableOKButton();
        });
    }

    protected void endSearch() {
        if (searchThread != null) {
            searchThread.cancel(); // cancel() wartet bis zur Beendigung vom Thread
            searchThread = null;
        }
        setNotFoundText("!!Keine SAA/BK vorhanden");
        searchButton.setVisible(false);
        doEnableOKButton();
    }

    /**
     * Startet die Suche mit allen Vorbereitungen. Übergeben wird das Suchkriterium und die Option, ob bei "MAX_SEARCH_RESULTS"
     * die Suche abgebrochen werden soll
     *
     * @param modelList
     * @param searchValue
     * @param stopAtMaxResult
     */
    private void startSearch(final List<String> modelList, final String searchValue, final boolean stopAtMaxResult) {
        searchButton.setVisible(true);
        availableEntriesGrid.showNoResultsLabel(false);
        interruptedByMaxResults = false;
        showResultCount();
        availableEntriesGrid.clearGrid();
        originalList.clear();
        searchThread = Session.startChildThreadInSession(thread -> {
            int tempMaxResult = maxResults;
            // Falls nicht bei MAX_SEARCH_RESULTS abgebrochen werden soll, wird sich hier das Maximum gemerkt
            // und später wieder gesetzt
            if (!stopAtMaxResult) {
                maxResults = -1;
            }
            doSearch(thread, modelList, searchValue);
            maxResults = tempMaxResult;
        });
    }

    /**
     * Führt die Suche für die übergebenen Baumuster und das übergebene Suchkriterium durch.
     *
     * @param thread
     * @param modelList
     * @param searchValue
     */
    private void doSearch(FrameworkThread thread, List<String> modelList, String searchValue) {
        DBDataSet dbSet = null;
        int count = 0;
        try {
            DBSQLQuery query = buildQuery(modelList, searchValue, true, true, true);
            dbSet = query.executeQuery();
            String lastSaaBkNumber = "";
            while (!thread.wasCanceled() && dbSet.next()) {
                if ((maxResults > 0) && (count >= maxResults)) {
                    interruptedByMaxResults = true;
                    break;
                }
                EtkRecord rec = dbSet.getRecord(new String[]{ FIELD_DA_ESM_SAA_NO, FIELD_DA_ESM_MODEL_NO });
                String saabkNumber = rec.getField(FIELD_DA_ESM_SAA_NO).getAsString();
                if (lastSaaBkNumber.isEmpty()) {
                    lastSaaBkNumber = saabkNumber;
                } else {
                    if (lastSaaBkNumber.equals(saabkNumber)) {
                        continue;
                    } else {
                        addEntryThreadSafe(lastSaaBkNumber);
                        count++;
                        lastSaaBkNumber = saabkNumber;
                    }
                }
            }
            if (!lastSaaBkNumber.isEmpty()) {
                addEntryThreadSafe(lastSaaBkNumber);
                count++;
            }
        } finally {
            if (dbSet != null) {
                dbSet.close();
            }
            searchThread = null;
        }
        final int searchCount = count;
        Session.invokeThreadSafeInSession(() -> {
            searchButton.setVisible(false);
            setDefaultNotFoundText();
            availableEntriesGrid.showNoResultsLabel(searchCount == 0);
            showResultCount();
            doEnableOKButton();
        });
    }

    private void showResultCount() {
        String str = "";
        if (interruptedByMaxResults) {
            str = " (" + TranslationHandler.translate("!!Mehr als %1 Datensätze gefunden", Integer.toString(maxResults)) + ")";
        }
        setAvailableEntriesTitle(TranslationHandler.translate("!!Verfügbare SAA/BK: %1", str));
    }

    /**
     * Fügt einen ausgewählten Wert dem rechten Grid hinzu
     *
     * @param saaBkNumber
     */
    private void addEntryThreadSafe(String saaBkNumber) {
        final iPartsDataSAAModels saaOrBkModel = new iPartsDataSAAModels(getProject(), new iPartsSAAModelsId(saaBkNumber, ""));
        saaOrBkModel.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
        Session.invokeThreadSafeInSession(() -> addEntryToAvailable(saaOrBkModel));
    }

    /**
     * Erstellt die Query für die Suche nach allen SAA und BK zu den übergebenen Baumuster und das übergebene Suchkriterium.
     * Zusätzlich kann übergeben werden, ob die Siche mit Wildcards und/oder "distinct" durchgeführt werden soll.
     *
     * @param modelList
     * @param searchValue
     * @param withWildCard
     * @param selectDistinct
     * @param withOrderBy
     * @return
     */
    private DBSQLQuery buildQuery(List<String> modelList, String searchValue, boolean withWildCard, boolean selectDistinct,
                                  boolean withOrderBy) {
        DBSQLQuery query = getProject().getDB().getDBForDomain(MAIN).getNewQuery();
        List<String> resultFields = new ArrayList<String>();
        resultFields.add(FIELD_DA_ESM_SAA_NO);
        resultFields.add(FIELD_DA_ESM_MODEL_NO);
        if (selectDistinct) {
            query.selectDistinct(new Fields(resultFields));
        } else {
            query.select(new Fields(resultFields));
        }
        query.from(new Tables(TABLE_DA_EDS_SAA_MODELS));
        if (withOrderBy) {
            query.orderBy(new String[]{ FIELD_DA_ESM_SAA_NO });
        }
        List<AbstractCondition> modelNumbers = new ArrayList<AbstractCondition>();
        if (modelList != null) {
            for (String model : modelList) {
                Condition condModel = new Condition(FIELD_DA_ESM_MODEL_NO, Condition.OPERATOR_EQUALS, model);
                modelNumbers.add(condModel);
            }
        }
        if (StrUtils.isValid(searchValue)) {
            String scndSearchValue = "";
            searchValue = numberHelper.unformatSaaBkForDB(getProject(), searchValue, false);
            if (searchValue.toUpperCase().startsWith("Z")) {
                String temp = searchValue.substring(1);
                if (temp.startsWith(" ")) {
                    scndSearchValue = "Z" + temp.substring(1);
                } else {
                    scndSearchValue = "Z" + " " + temp;
                }
            }
            WildCardSettings wildCardSettings = new WildCardSettings();
            wildCardSettings.addNoAutoWildCard();
            wildCardSettings.addWildCardEnd();
            if (withWildCard) {
                searchValue = wildCardSettings.makeWildCard(searchValue);
            }

            Condition searchConditon = new Condition(FIELD_DA_ESM_SAA_NO, Condition.OPERATOR_LIKE, SQLUtils.wildcardExpressionToSQLLike(searchValue, false, false, false));
            if (scndSearchValue.isEmpty()) {
                query.where(searchConditon.and(new ConditionList(modelNumbers, true)));
            } else {
                if (withWildCard) {
                    scndSearchValue = wildCardSettings.makeWildCard(scndSearchValue);
                }
                List<AbstractCondition> searches = new ArrayList<AbstractCondition>();
                searches.add(searchConditon);
                searchConditon = new Condition(FIELD_DA_ESM_SAA_NO, Condition.OPERATOR_LIKE, SQLUtils.wildcardExpressionToSQLLike(scndSearchValue, false, false, false));
                searches.add(searchConditon);
                query.where(new ConditionList(searches, true).and(new ConditionList(modelNumbers, true)));
            }
        } else {
            query.where(new ConditionList(modelNumbers, true));
        }
        return query;
    }

    /**
     * Liefert die Anzahl aller SAA und BK zu den übergebenen Baumuster
     *
     * @param modelList
     * @return
     */
    private int getQueryCount(List<String> modelList) {
        int overallCount = Integer.MAX_VALUE;
        DBSQLQuery queryCount = getProject().getDB().getDBForDomain(MAIN).getNewQuery();
        try {
            SQLQuery query = buildQuery(modelList, "", false, false, false);
            String queryString = query.toQueryString();
            String selectValue = "count(distinct " + FIELD_DA_ESM_SAA_NO.toLowerCase() + ")";
            queryString = StrUtils.replaceFirstSubstring(queryString, FIELD_DA_ESM_SAA_NO.toLowerCase() + ", " + FIELD_DA_ESM_MODEL_NO.toLowerCase(), selectValue);
            queryCount.initAsSimpleSQL(queryString);
            DBDataSet setCount = null;
            try {
                setCount = queryCount.executeQuery();
                if (setCount.next()) {
                    List<String> resultList = setCount.getStringList();
                    if ((resultList != null) && !resultList.isEmpty()) {
                        overallCount = StrUtils.strToIntDef(resultList.get(0), 0);
                    }
                }
            } finally {
                if (setCount != null) {
                    setCount.close();
                }
            }
        } catch (SQLException e) {
        }
        return overallCount;
    }

    @Override
    protected String getVisualValueOfFieldAvailable(String tableName, String fieldName, EtkDataObject objectForTable) {
        if (tableName.equals(searchTable) && fieldName.equals(iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION)) {
            boolean oldLogLoadFieldIfNeeded = objectForTable.isLogLoadFieldIfNeeded();
            try {
                objectForTable.setLogLoadFieldIfNeeded(false);
                if (objectForTable.getAttribute(iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION, false) == null) {
                    String saabkNumber = objectForTable.getFieldValue(iPartsConst.FIELD_DA_ESM_SAA_NO);
                    // Wenn SAA, dann DA_SAA. Wenn BK, dann MAT.
                    if (numberHelper.isValidSaa(saabkNumber)) {
                        fillFromSaaData((iPartsDataSAAModels)objectForTable, false);
                    } else {
                        fillFromPartData((iPartsDataSAAModels)objectForTable, false);
                    }
                }
            } finally {
                objectForTable.setLogLoadFieldIfNeeded(oldLogLoadFieldIfNeeded);
            }
        }
        return null;
    }

    @Override
    protected String getVisualValueOfFieldSelected(String tableName, String fieldName, EtkDataObject objectForTable) {
        return null;
    }

    private void setDefaultNotFoundText() {
        if (StrUtils.isValid(defaultTextNotFound)) {
            availableEntriesGrid.setNoResultsLabelText(defaultTextNotFound);
            defaultTextNotFound = null;
        }
    }

    private void setNotFoundText(String text) {
        setDefaultNotFoundText();
        defaultTextNotFound = availableEntriesGrid.getNoResultsLabelText();
        availableEntriesGrid.setNoResultsLabelText(text);
    }

    /**
     * Befüllt das {@link iPartsDataSAAModels} Objekt mit der Benennung aus der MAT Tabelle.
     *
     * @param saaOrBkModel
     * @return
     */
    private boolean fillFromPartData(iPartsDataSAAModels saaOrBkModel, boolean showIfNotExist) {
        EtkDataPart part = EtkDataObjectFactory.createDataPart(getProject(), new PartId(saaOrBkModel.getAsId().getSAANumber(), ""));
        return fillSaaModelData(saaOrBkModel, part, FIELD_M_TEXTNR, showIfNotExist);
    }

    /**
     * Befüllt das {@link iPartsDataSAAModels} Objekt mit der Benennung aus der DA_SAA Tabelle.
     *
     * @param saaOrBkModel
     * @return
     */
    private boolean fillFromSaaData(iPartsDataSAAModels saaOrBkModel, boolean showIfNotExist) {
        iPartsSaaId saaId = new iPartsSaaId(saaOrBkModel.getAsId().getSAANumber());
        iPartsDataSaa description = new iPartsDataSaa(getProject(), saaId);
        return fillSaaModelData(saaOrBkModel, description, FIELD_DS_DESC, showIfNotExist);
    }

    private boolean fillSaaModelData(iPartsDataSAAModels saaOrBkModel, EtkDataObject dataObject, String fieldName,
                                     boolean showIfNotExist) {
        EtkMultiSprache etkMultiSprache = null;
        if (dataObject.existsInDB()) {
            // Bestehendes Textobjekt verwenden
            String textId = dataObject.getFieldValue(fieldName);
            etkMultiSprache = descMap.get(textId);
            // Wenn bestehendes nicht vorhanden, dann neues Textobjekt erzeugen
            if (etkMultiSprache == null) {
                etkMultiSprache = dataObject.getFieldValueAsMultiLanguage(fieldName);
                if (!StrUtils.isEmpty(textId)) {
                    descMap.put(textId, etkMultiSprache);
                }
            }

        } else if (showIfNotExist) {
            etkMultiSprache = new EtkMultiSprache("!![Es existieren keine Stammdaten zur angegebenen SAA/BK]",
                                                  getProject().getConfig().getDatabaseLanguages());
            descMap.put(SAA_NOT_EXIST_TEXT_KEY, etkMultiSprache);
        }
        if (etkMultiSprache != null) {
            addDescriptionToSaaOrBk(saaOrBkModel, etkMultiSprache);
            return true;
        }
        return false;
    }

    /**
     * Fügt dem übergebenen {@link iPartsDataSAAModels} Objekt die übergebenen Benennung hinzu.
     *
     * @param saaOrBkModel
     * @param multiDesc
     */
    private void addDescriptionToSaaOrBk(iPartsDataSAAModels saaOrBkModel, EtkMultiSprache multiDesc) {
        DBDataObjectAttribute attribute = new DBDataObjectAttribute(iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION, DBDataObjectAttribute.TYPE.MULTI_LANGUAGE, true);
        attribute.setValueAsMultiLanguage(multiDesc, DBActionOrigin.FROM_DB);
        saaOrBkModel.getAttributes().addField(attribute, DBActionOrigin.FROM_DB);
    }

    /**
     * Befüllt die vorausgewählten SAA/BK Gültigkeiten und übergibt sie an das Grid. Um den Datensatz befüllen zu können
     * wird jede Konstellation Baumuster + SAA/BK überprüft. Sollte eine Konstellation ind er DB sein, wird die Benennung
     * aus der zugehörigen Tabelle geholt (bei SAA aus DA_SAA und bei BKs aus MAT).
     *
     * @param saaBkList
     */
    private void fillSelectedSaaBks(Collection<String> saaBkList) {
        List<EtkDataObject> selectedList = new DwList<EtkDataObject>();
        for (String saaOrBk : saaBkList) {
            iPartsDataSAAModels foundDataObject = makeDataObjectForSelectList(saaOrBk, true);
            if (foundDataObject != null) {
                selectedList.add(foundDataObject);
                initialSaaBkValues.add(saaOrBk);
            }
        }
        doAddEntries(selectedList);
        doEnableOKButton();
    }

    /**
     * Erzeugt ein {@link iPartsDataSAAModels} Objekt mit den geladenen Werten für die übergebene SAA oder BK Nummer.
     * Existieren in der DB keine Stammdaten zu der übergebenen SAA oder BK Nummern, dann wird <i>null</i> zurückgeliefert.
     *
     * @param saaOrBk
     * @return
     */
    private iPartsDataSAAModels makeDataObjectForSelectList(String saaOrBk, boolean showIfNotExist) {
        iPartsDataSAAModels dataSAAModels = new iPartsDataSAAModels(getProject(), new iPartsSAAModelsId(saaOrBk, ""));
        dataSAAModels.initAttributesWithEmptyValues(DBActionOrigin.FROM_EDIT);
        boolean found;
        if (numberHelper.isValidSaa(saaOrBk)) {
            found = fillFromSaaData(dataSAAModels, showIfNotExist);
        } else {
            found = fillFromPartData(dataSAAModels, showIfNotExist);
        }
        if (found) {
            return dataSAAModels;
        }
        return null;
    }

    @Override
    protected EtkDisplayFields buildDefaultDisplayFields(boolean forSelectedEntries) {
        EtkDisplayFields displayFields = new EtkDisplayFields();
        EtkDatabaseTable tableDef = getConfig().getDBDescription().findTable(searchTable);
        EtkDisplayField displayField = createDisplayField(tableDef, iPartsConst.FIELD_DA_ESM_SAA_NO);
        displayField.setColumnFilterEnabled(true);
        displayFields.addFeld(displayField);
        displayFields.addFeld(new EtkDisplayField(TABLE_DA_EDS_SAA_MODELS, iPartsDataVirtualFieldsDefinition.DESM_DESCRIPTION, true, false));
        displayFields.loadStandards(getConfig());
        return displayFields;
    }

    public Collection<String> getSelectedSaaBks() {
        List<String> resultList = new DwList<String>();
        List<EtkDataObject> list = getCompleteSelectedList();
        if (list != null) {
            for (EtkDataObject dataObject : list) {
                if (dataObject instanceof iPartsDataSAAModels) {
                    resultList.add(((iPartsDataSAAModels)dataObject).getAsId().getSAANumber());
                }
            }
        }
        return resultList;
    }

    /**
     * Flag, ob das Suchfeld für die Baumusternummer angezeigt werden soll.
     *
     * @param visible
     */
    public void setSAABKNumberSearchFieldVisible(boolean visible) {
        numberSearchPanel.setVisible(visible);
    }


    /**
     * Fügt den Bereich für die manuelle Eingabe hinzu
     */
    private void addInputArea() {
        panelInput = new GuiPanel();
        panelInput.setName("panelInput");
        panelInput.__internal_setGenerationDpi(96);
        panelInput.registerTranslationHandler(getUITranslationHandler());
        panelInput.setScaleForResolution(true);
        panelInput.setMinimumWidth(10);
        panelInput.setMinimumHeight(10);
        LayoutGridBag panelInputLayout = new LayoutGridBag();
        panelInput.setLayout(panelInputLayout);

        GuiLabel labelAddSaa = new GuiLabel();
        labelAddSaa.setName("labelAddSaa");
        labelAddSaa.__internal_setGenerationDpi(96);
        labelAddSaa.registerTranslationHandler(getUITranslationHandler());
        labelAddSaa.setScaleForResolution(true);
        labelAddSaa.setMinimumWidth(10);
        labelAddSaa.setMinimumHeight(10);
        labelAddSaa.setText("!!Manuelle Eingabe:");
        ConstraintsGridBag gridbagConstraints = new ConstraintsGridBag(0, 0, 1, 1, 100.0, 0.0, "e", "n", 8, 8, 8, 4);
        labelAddSaa.setConstraints(gridbagConstraints);
        panelInput.addChild(labelAddSaa);

        buttonAddValue = new GuiButton();
        buttonAddValue.setName("buttonAddValue");
        buttonAddValue.__internal_setGenerationDpi(96);
        buttonAddValue.registerTranslationHandler(getUITranslationHandler());
        buttonAddValue.setScaleForResolution(true);
        buttonAddValue.setMinimumWidth(100);
        buttonAddValue.setMinimumHeight(10);
        buttonAddValue.setMnemonicEnabled(true);
        buttonAddValue.setText("!!Hinzufügen");
        buttonAddValue.setModalResult(ModalResult.NONE);
        buttonAddValue.setEnabled(false);
        buttonAddValue.addEventListener(new de.docware.framework.modules.gui.event.EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                addAdditionalSaaOrBk(event);
            }
        });
        gridbagConstraints = new ConstraintsGridBag(2, 0, 1, 1, 0.0, 0.0, "w", "n", 8, 4, 8, 8);
        buttonAddValue.setConstraints(gridbagConstraints);
        panelInput.addChild(buttonAddValue);

        textfieldAddValue = new de.docware.framework.modules.gui.controls.GuiTextField();
        textfieldAddValue.setName("textfieldAddSaa");
        textfieldAddValue.__internal_setGenerationDpi(96);
        textfieldAddValue.registerTranslationHandler(getUITranslationHandler());
        textfieldAddValue.setScaleForResolution(true);
        textfieldAddValue.setMinimumWidth(200);
        textfieldAddValue.setMinimumHeight(10);
        gridbagConstraints = new ConstraintsGridBag(1, 0, 1, 1, 0.0, 0.0, "w", "n", 8, 4, 8, 4);
        textfieldAddValue.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                buttonAddValue.setEnabled(numberHelper.isValidSaaOrBk(textfieldAddValue.getText(), true));
            }
        });
        textfieldAddValue.setConstraints(gridbagConstraints);
        panelInput.addChild(textfieldAddValue);

        ConstraintsBorder panelInputConstraints = new ConstraintsBorder();
        panelInputConstraints.setPosition(ConstraintsBorder.POSITION_SOUTH);
        panelInput.setConstraints(panelInputConstraints);
        getPanelForFurtherElements().addChild(panelInput);

        // Panel für das Baumusternummer-Suchfeld erzeugen
        numberSearchPanel = new GuiPanel(new LayoutGridBag());

        GuiLabel modelNumberSearchLabel = new GuiLabel("!!SAA/BK Suche");
        modelNumberSearchLabel.setConstraints(new ConstraintsGridBag(0, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_WEST,
                                                                     ConstraintsGridBag.FILL_NONE, 4, 4, 4, 8));
        numberSearchPanel.addChild(modelNumberSearchLabel);

        numberSearchTextField = new iPartsGuiDelayTextField();
        numberSearchTextField.setName("numberSearchTextField");
        numberSearchTextField.setMinimumWidth(100);
        numberSearchTextField.setDelayMilliSec(SEARCH_DELAY_FOR_INPUT);
        numberSearchTextField.setAllowStarSearch(true, 0); // Sternsuche erlauben
        numberSearchTextField.setConstraints(new ConstraintsGridBag(1, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_WEST,
                                                                    ConstraintsGridBag.FILL_VERTICAL, 4, 0, 4, 4));
        numberSearchPanel.addChild(numberSearchTextField);

        numberSearchTextField.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            @Override
            public void fire(Event event) {
                searchAndFillAvailableSAABKs();
            }
        });
        // Zusätzlicher Listener für den Fall, dass wir mehr als "MAX_SEARCH_RESULTS" Saa oder Bk haben und das Suchkriterium
        // nicht die Mindestlänge hat. In diesem Fall wird das Grid geleert und ein Hinweis reingeschrieben. Sonst würde
        // unter Umständen das Suchergebnis nicht zum Suchkriterium passen
        numberSearchTextField.addEventListener(new EventListener(Event.KEY_RELEASED_EVENT) {
            @Override
            public void fire(Event event) {
                String text = numberSearchTextField.getText().trim();
                boolean lessCharThanMin = text.length() < SEARCH_MIN_CHARACTERS;
                boolean morePossibleResultThanMax = saaBkCount > MAX_SEARCH_RESULTS;
                boolean starSearch = StrUtils.stringContains(text, '*');
                if (lessCharThanMin && morePossibleResultThanMax && !starSearch) {
                    fillAvailableSaaBks(handledModelNoList);
                }
            }
        });

        searchButton = new GuiButton();
        searchButton.setName("searchButton");
        searchButton.setMinimumWidth(100);
        searchButton.setConstraints(new ConstraintsGridBag(2, 0, 1, 1, 0, 0, ConstraintsGridBag.ANCHOR_WEST,
                                                           ConstraintsGridBag.FILL_NONE, 4, 0, 4, 4));
        searchButton.setText("!!Suche abbrechen");
        numberSearchPanel.addChild(searchButton);
        searchButton.setVisible(false);
        searchButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                endSearch();
            }
        });

        GuiLabel fillLabel = new GuiLabel();
        fillLabel.setConstraints(new ConstraintsGridBag(3, 0, 1, 1, 1, 0, ConstraintsGridBag.ANCHOR_WEST,
                                                        ConstraintsGridBag.FILL_NONE, 4, 0, 4, 4));
        numberSearchPanel.addChild(fillLabel);

        showAllButton = getButtonPanel().addCustomButton("!!Alle SAA/BK anzeigen");
        showAllButton.setName("showAllButton");
        showAllButton.setMinimumWidth(100);
        showAllButton.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            @Override
            public void fire(Event event) {
                showAllAvailableEntries();
            }
        });

        numberSearchPanel.setConstraints(new ConstraintsBorder(ConstraintsBorder.POSITION_NORTH));
        getPanelForFurtherElements().addChild(numberSearchPanel);
    }

    /**
     * Zeigt im "verfügbarkeitsgrid" alle möglichen SAA und BK an
     */
    private void showAllAvailableEntries() {
        endSearch();
        numberSearchTextField.setText("");
        int oldMaxResults = maxResults;
        maxResults = -1;
        startSearch(handledModelNoList, "", false);
        maxResults = oldMaxResults;
    }

    /**
     * Sucht und füllt alle verfügbaren SAA und BK zum eingegebenen Suchkriterium
     */
    private void searchAndFillAvailableSAABKs() {
        String saabkSearchValue = numberSearchTextField.getText();
        endSearch();
        if (saaBkCount > 0) {
            startSearch(handledModelNoList, saabkSearchValue, true);
        }

    }

    /**
     * Fügt dem Grid mit den ausgewählten SAA/BK den manuell eingegebenen Wert hinzu.
     *
     * @param event
     */
    private void addAdditionalSaaOrBk(Event event) {
        String input = textfieldAddValue.getText();
        if (numberHelper.isValidSaaOrBk(input, true)) {
            endSearch();
            input = numberHelper.unformatSaaBkForDB(getProject(), input, false);
            boolean isSaa = numberHelper.isValidSaa(input);
            // incl Überprüfung ob SAA-Nummer in den Stammdaten vorhanden ist
            iPartsDataSAAModels dataObjectForSelectList = makeDataObjectForSelectList(input, false);
            // Hat die manuelle Eingabe dazugehörige Stammdaten?
            if (dataObjectForSelectList != null) {
                // Check, ob der manuell eingegebene Wert schon als Datensatz im Grid existiert
                if (checkIfSaaOrBkAlreadySelected(dataObjectForSelectList, isSaa)) {
                    return;
                }
                // Check, ob die eingegebene SAA/BK-Nummer einem Baumuster des Produkts zugeordnet ist + Abfrage, ob sie
                // bei Nicht-Zugehörigkeit trotzdem übernommen werden soll
                if ((handledModelNoList != null) && !checkSaaBkModelDependencies(input)) {
                    ModalResult result = MessageDialog.showYesNo(TranslationHandler.translate("!!%1 ist keinem Baumuster des Produkts \"%2\" zugeordnet.",
                                                                                              (isSaa ? "Die eingegebene SAA" : "Der eingegebene Baukasten"),
                                                                                              productId.getProductNumber()) + "\n\n" +
                                                                 TranslationHandler.translate("!!Möchten Sie %1 trotzdem übernehmen?",
                                                                                              (isSaa ? "die eingegebene SAA" : "den eingegebenen Baukasten")),
                                                                 "!!SAA/BK Gültigkeit");
                    if (result == ModalResult.NO) {
                        return;
                    }
                }
                doAddSingleSelectedEntry(dataObjectForSelectList);
                return;
            } else {
                String typeText = isSaa ? "Für die eingegebene SAA" : "Für den eingegebenen Baukasten";
                MessageDialog.show(TranslationHandler.translate("!!%1 existieren keine Stammdaten.", typeText)
                                   + "\nDie manuelle Eingabe kann nicht übernommen werden.",
                                   "!!SAA/BK Gültigkeit", MessageDialogIcon.INFORMATION, MessageDialogButtons.OK);
            }
        }
    }

    /**
     * Überprüft, ob die eingegebene SAS/BK Nummer einem Baumuster des Produkts zugeordnet ist
     *
     * @param input
     * @return
     */
    private boolean checkSaaBkModelDependencies(String input) {
        DBSQLQuery query = buildQuery(handledModelNoList, input, false, true, false);
        DBDataSet dbSet = null;
        try {
            dbSet = query.executeQuery();
            if (dbSet.next()) {
                return true;
            }
        } finally {
            if (dbSet != null) {
                dbSet.close();
            }
        }
        return false;
    }

    /**
     * Überprüft, ob die eingebenen SAA/BK Nummer schon ausgewählt wurde.
     *
     * @param dataSAAModels
     * @param isSaa
     * @return
     */
    private boolean checkIfSaaOrBkAlreadySelected(iPartsDataSAAModels dataSAAModels, boolean isSaa) {
        for (EtkDataObject selectEntry : getCompleteSelectedList()) {
            if (selectEntry instanceof iPartsDataSAAModels) {
                iPartsDataSAAModels existingEntry = (iPartsDataSAAModels)selectEntry;
                if (existingEntry.getAsId().getSAANumber().equals(dataSAAModels.getAsId().getSAANumber())) {
                    String type = isSaa ? "Die eingegebene SAA" : "Der eingegebene Baukasten";
                    MessageDialog.show(TranslationHandler.translate("!!%1 wurde bereits ausgewählt.", type),
                                       "!!SAA/BK Gültigkeit", MessageDialogIcon.INFORMATION, MessageDialogButtons.OK);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean areEntriesChanged() {
        Collection<String> selectedSaaBks = getSelectedSaaBks();
        if (initialSaaBkValues != null) {
            if (initialSaaBkValues.size() != selectedSaaBks.size()) {
                return true;
            } else {
                return !initialSaaBkValues.containsAll(selectedSaaBks);
            }
        }
        if (!selectedSaaBks.isEmpty()) {
            return true;
        }
        return false;
    }

    @Override
    protected void doAdd(Event event) {
        endSearch();
        super.doAdd(event);
    }

    protected void doRemove(Event event) {
        endSearch();
        super.doRemove(event);
    }

    @Override
    protected void doAddAll(Event event) {
        endSearch();
        super.doAddAll(event);
    }

    @Override
    protected void doRemoveAll(Event event) {
        endSearch();
        super.doRemoveAll(event);
    }

    @Override
    protected void doMoveUp(Event event) {
        endSearch();
        super.doMoveUp(event);
    }

    @Override
    protected void doMoveDown(Event event) {
        endSearch();
        super.doMoveDown(event);
    }
}
