/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */

package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.forms.common.EditControlDateTimeEditPanel;
import de.docware.apps.etk.base.project.EtkProject;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsDataSeriesExpireDate;
import de.docware.apps.etk.plugins.customer.daimler.iparts.dbobjects.iPartsSeriesExpireDateId;
import de.docware.apps.etk.plugins.customer.daimler.iparts.filter.iPartsFilterHelper;
import de.docware.apps.etk.plugins.customer.daimler.iparts.helper.iPartsToolbarButtonAlias;
import de.docware.apps.etk.plugins.customer.daimler.iparts.model.products.iPartsSeriesId;
import de.docware.framework.modules.db.DBActionOrigin;
import de.docware.framework.modules.gui.controls.GuiButton;
import de.docware.framework.modules.gui.controls.GuiComboBox;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutGridBag;
import de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag;
import de.docware.framework.modules.gui.misc.color.Colors;
import de.docware.util.StrUtils;
import de.docware.util.Utils;
import de.docware.util.collections.dwlist.DwList;
import de.docware.util.date.DateUtils;
import de.docware.util.sql.SQLStringConvert;

import java.util.*;

/**
 * Panel für die Zuordnung eines Auslauftermins zu einem Werk an einer Baureihe
 */
public class iPartsGuiFactoryExpireDatePanel extends GuiPanel {

    public static final String TYPE = "ipartsfactoryExpireDatpanel";

    // Defaultwerte
    private static final boolean READ_ONLY_DEFAULT_VALUE = false;

    // Spezifische Eigenschaften der Komponente
    private final boolean extendToFullHeight = false;
    private boolean readOnly = READ_ONLY_DEFAULT_VALUE;
    private boolean withButton = false;

    // Weitere benötigte Variablen
    private GuiComboBox<String> comboboxFactories;
    private EditControlDateTimeEditPanel dateTimeEditPanel;
    private GuiButton buttonEdit;
    private EtkProject project;
    private iPartsSeriesId seriesId;
    private String ausfuehrungsart;
    private Set<String> productFactories;
    private Map<String, String> expireDateMap;
    private Map<String, String> initialExpireDateMap;

    public iPartsGuiFactoryExpireDatePanel(EtkProject project, iPartsSeriesId seriesId, String ausfuehrungsart) {
        setType(TYPE);
        initGui();
        initPanelValues(project, seriesId, ausfuehrungsart);
    }

    public iPartsSeriesId getSeriesId() {
        return seriesId;
    }

    public String getSeriesAA() {
        return ausfuehrungsart;
    }

    public Map<String, String> getCurrentExpiredMap() {
        return expireDateMap;
    }

    public List<iPartsDataSeriesExpireDate> getExpireDatesForShowAsList() {
        return getInternalExpireDatesAsList(true);
    }

    public List<iPartsDataSeriesExpireDate> getExpireDatesAsList() {
        return getInternalExpireDatesAsList(false);
    }

    private List<iPartsDataSeriesExpireDate> getInternalExpireDatesAsList(boolean addAll) {
        List<iPartsDataSeriesExpireDate> resultList = new DwList<>();
        if (seriesId != null) {
            for (Map.Entry<String, String> entry : expireDateMap.entrySet()) {
                String dbDate = entry.getValue();
                if (StrUtils.isEmpty(dbDate) && !addAll) {
                    continue;
                }
                iPartsSeriesExpireDateId id = new iPartsSeriesExpireDateId(seriesId.getSeriesNumber(), ausfuehrungsart, entry.getKey());
                iPartsDataSeriesExpireDate data = new iPartsDataSeriesExpireDate(project, id);
                data.initAttributesWithEmptyValues(DBActionOrigin.FROM_DB);
                if (dbDate == null) {
                    dbDate = "";
                }
                data.setDbExpireDate(dbDate, DBActionOrigin.FROM_DB);
                resultList.add(data);
            }
        }
        return resultList;
    }

    public boolean isModified() {
        for (Map.Entry<String, String> entry : expireDateMap.entrySet()) {
            String initialDate = initialExpireDateMap.get(entry.getKey());
            String dbDate = entry.getValue();
            if (!Utils.objectEquals(initialDate, dbDate)) {
                return true;
            }
        }
        return false;
    }

    public void setExistingExpireList(List<iPartsDataSeriesExpireDate> seriesExpireDateList) {
        addExistingExpireDates(seriesExpireDateList);
        reloadValues();
    }

    public void clearExistingExpireDates() {
        expireDateMap.clear();
        for (String factory : productFactories) {
            expireDateMap.put(factory, null);
        }
        reloadValues();
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        comboboxFactories.setEnabled(!readOnly);
        dateTimeEditPanel.setEditable(!readOnly);
    }

    public void setWithButton(boolean withButton) {
        if (this.withButton != withButton) {
            this.withButton = withButton;
            buttonEdit.setVisible(withButton);
        }
    }

    private void initPanelValues(EtkProject project, iPartsSeriesId seriesId, String seriesAA) {
        this.project = project;
        this.seriesId = seriesId;
        this.ausfuehrungsart = seriesAA;
        this.productFactories = iPartsFilterHelper.getProductFactoriesForReferencedSeriesAndAA(project, seriesId, seriesAA);
        this.expireDateMap = new LinkedHashMap<>();
        this.initialExpireDateMap = new LinkedHashMap<>();
        fillMaps();
        reloadValues();
    }

    /**
     * Befüllt die Map mit den initialen Auslaufterminen und die Map mit den aktuellen Auslaufterminen
     */
    private void fillMaps() {
        if (productFactories != null) {
            expireDateMap.clear();
            initialExpireDateMap.clear();
            // hier aus neuer Tabelle die ExpireDates holen
            for (String factory : productFactories) {
                expireDateMap.putIfAbsent(factory, null);
                initialExpireDateMap.put(factory, null);
            }
        }
    }

    /**
     * Fügt schon vorhandene Auslauftermine hinzu
     *
     * @param seriesExpireDateList
     */
    private void addExistingExpireDates(List<iPartsDataSeriesExpireDate> seriesExpireDateList) {
        for (iPartsDataSeriesExpireDate dataSeriesExpireDate : seriesExpireDateList) {
            expireDateMap.put(dataSeriesExpireDate.getAsId().getSeriesFactoryNo(), dataSeriesExpireDate.getDbExpireDate());
            initialExpireDateMap.put(dataSeriesExpireDate.getAsId().getSeriesFactoryNo(), dataSeriesExpireDate.getDbExpireDate());
        }
    }


    private void dateTimeChanged(Event event) {
        Date date = dateTimeEditPanel.getDateTime();
        String factory = comboboxFactories.getSelectedItem();
        String dbDate = null;
        if (date != null) {
            dbDate = DateUtils.toyyyyMMddHHmmss_DateTime(date);
        }
        expireDateMap.put(factory, dbDate);
        if (hasEventListener(Event.ON_CHANGE_EVENT)) {
            Event onChangeEvent = new Event(Event.ON_CHANGE_EVENT);
            fireEvent(onChangeEvent);
        }
    }

    private void updateDateTime() {
        String factory = comboboxFactories.getSelectedItem();
        String dbDate = expireDateMap.get(factory);
        if (dbDate != null) {
            try {
                dateTimeEditPanel.switchOffEventListeners();
                dateTimeEditPanel.setDateTime(SQLStringConvert.ppDateTimeStringToCalendar(dbDate));
                dateTimeEditPanel.setEnabled(!isReadOnly());
            } finally {
                dateTimeEditPanel.switchOnEventListeners();
            }
            if (!isReadOnly()) {
                dateTimeEditPanel.requestFocus();
            }
        } else {
            dateTimeEditPanel.clearDateTime();
        }
    }


    private void reloadValues() {
        try {
            comboboxFactories.switchOffEventListeners();
            comboboxFactories.removeAllItems();
            if (productFactories != null) {
                // hier aus neuer Tabelle die ExpireDates holen
                for (String factory : productFactories) {
                    comboboxFactories.addItem(factory);
                }
                setReadOnly(productFactories.isEmpty());
            } else {
                setReadOnly(true);
            }
        } finally {
            comboboxFactories.switchOnEventListeners();
        }
        if ((productFactories != null) && !productFactories.isEmpty()) {
            comboboxFactories.setSelectedIndex(0);
        }
        if (project != null) {
            dateTimeEditPanel.init(project.getConfig(), iPartsConst.TABLE_DA_SERIES_SOP,
                                   iPartsConst.FIELD_DSP_START_OF_PROD, project.getViewerLanguage());
        }
    }

    /**
     * Initialisiert die GUI
     */
    private void initGui() {
        LayoutGridBag panelLayout = new LayoutGridBag();
        setLayout(panelLayout);
        removeAllChildren();
        // create Factory combobox
        super.setBackgroundColor(Colors.clTransparent.getColor());
        createFactoryComboBox();
        createDateTimePanel();
        createButtonEdit();
        if (!extendToFullHeight) {
            postLoad();
        }
        reloadValues();
        __internal_setTestNameOnControl();
    }

    /**
     * Erzeugt die ComboBox für die Werke
     */
    private void createFactoryComboBox() {
        comboboxFactories = new GuiComboBox();
        comboboxFactories.setMinimumWidth(51);
        comboboxFactories.setMinimumHeight(-1);
        ConstraintsGridBag comboboxLanguageConstraints = new ConstraintsGridBag(0, 0, 1, 1,
                                                                                0.0, 0.0,
                                                                                ConstraintsGridBag.ANCHOR_CENTER,
                                                                                ConstraintsGridBag.FILL_NONE,
                                                                                0, 0, 0, 1);
        comboboxFactories.setConstraints(comboboxLanguageConstraints);
        comboboxFactories.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            public void fire(Event event) {
                updateDateTime();
            }
        });
        addChild(comboboxFactories);
    }

    /**
     * Erzeugt das Datum und Uhrzeit Panel bzw. Control
     */
    private void createDateTimePanel() {
        dateTimeEditPanel = new EditControlDateTimeEditPanel();
        dateTimeEditPanel.setMinimumWidth(-1);
        dateTimeEditPanel.setMinimumHeight(-1);
        dateTimeEditPanel.clearDateTime();
        ConstraintsGridBag dateTimeConstraints = new ConstraintsGridBag(1, 0, 1, 1,
                                                                        0.0, 0.0, "nw", "h",
                                                                        0, 0, 0, 0);
        dateTimeEditPanel.setConstraints(dateTimeConstraints);
        dateTimeEditPanel.addEventListener(new EventListener(Event.ON_CHANGE_EVENT) {
            public void fire(Event event) {
                dateTimeChanged(event);
            }
        });
        addChild(dateTimeEditPanel);
    }

    /**
     * Erzeugt den Button zur Anzeige des Dialogs mit allen Auslaufterminen
     */
    private void createButtonEdit() {
        buttonEdit = new GuiButton();
        buttonEdit.setMinimumWidth(-1);
        buttonEdit.setMinimumHeight(-1);
        buttonEdit.setMaximumWidth(21);
        buttonEdit.setMaximumHeight(21);
        buttonEdit.setMnemonicEnabled(true);
        buttonEdit.setIcon(iPartsToolbarButtonAlias.SHOW_LIST.getImage());
        ConstraintsGridBag buttonEditConstraints = new ConstraintsGridBag(2, 0, 1, 1,
                                                                          0.0, 0.0,
                                                                          ConstraintsGridBag.ANCHOR_CENTER,
                                                                          ConstraintsGridBag.FILL_NONE,
                                                                          0, 0, 0, 0);
        buttonEdit.setConstraints(buttonEditConstraints);
        buttonEdit.addEventListener(new EventListener(Event.ACTION_PERFORMED_EVENT) {
            public void fire(Event event) {
                //Button wurde gedrückt, das Event ist aber für die GuiMultiLangEdit-Komponente bestimmt.
                if (hasEventListener(Event.ACTION_PERFORMED_EVENT)) {
                    fireEvent(event);
                }
            }
        });
        buttonEdit.setVisible(withButton);
        addChild(buttonEdit);
    }

    private void postLoad() {
        GuiPanel dummyPanel = new de.docware.framework.modules.gui.controls.GuiPanel();
        dummyPanel.setMinimumWidth(-1);
        dummyPanel.setMinimumHeight(-1);
        de.docware.framework.modules.gui.layout.LayoutBorder dummyPanelLayout =
                new de.docware.framework.modules.gui.layout.LayoutBorder();
        dummyPanel.setLayout(dummyPanelLayout);
        de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag dummyPanelConstraints =
                new de.docware.framework.modules.gui.layout.constraints.ConstraintsGridBag(0, 999, 2, 1, 100.0, 100.0, "c", "b", 0, 0, 0, 0);
        dummyPanel.setConstraints(dummyPanelConstraints);
        this.addChild(dummyPanel);
    }
}
