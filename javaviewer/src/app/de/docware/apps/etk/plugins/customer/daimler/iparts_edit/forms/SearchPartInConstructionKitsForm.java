/*
 * Copyright (c) 2021 Quanos Service Solutions GmbH
 */
package de.docware.apps.etk.plugins.customer.daimler.iparts_edit.forms;

import de.docware.apps.etk.base.config.EtkConfig;
import de.docware.apps.etk.base.config.partlist.EtkDisplayFields;
import de.docware.apps.etk.base.forms.AbstractJavaViewerForm;
import de.docware.apps.etk.base.search.connector.SearchFormConnector;
import de.docware.apps.etk.base.search.forms.SearchMechanicForm;
import de.docware.apps.etk.base.search.model.EtkPartsSearch;
import de.docware.apps.etk.base.search.model.EtkPartsSearchDataset;
import de.docware.apps.etk.base.search.model.WildCardSettings;
import de.docware.apps.etk.plugins.customer.daimler.iparts.config.iPartsConst;
import de.docware.framework.modules.db.DBDataObjectAttributes;
import de.docware.framework.modules.db.DBDataObjectAttributesList;
import de.docware.framework.modules.gui.controls.GuiPanel;
import de.docware.framework.modules.gui.controls.GuiTitle;
import de.docware.framework.modules.gui.controls.GuiWindow;
import de.docware.framework.modules.gui.controls.buttonpanel.GuiButtonPanel;
import de.docware.framework.modules.gui.dialogs.GuiWindowForPanelWrapper;
import de.docware.framework.modules.gui.dialogs.messagedialog.MessageDialog;
import de.docware.framework.modules.gui.event.Event;
import de.docware.framework.modules.gui.event.EventListener;
import de.docware.framework.modules.gui.layout.LayoutBorder;
import de.docware.framework.utils.FrameworkUtils;
import de.docware.util.CanceledException;
import de.docware.util.StrUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Nicht-modaler Dialog für die Suche von Einzelteilen in Baukästen und deren Verwendungen.
 */
public class SearchPartInConstructionKitsForm implements iPartsConst {

    public static void showSearchPartInConstructionKitsForm(AbstractJavaViewerForm parentForm) {
        SearchFormConnector searchFormConnector = new SearchFormConnector(parentForm.getConnector());
        SearchMechanicForm searchMechanicForm = new SearchMechanicForm(searchFormConnector, parentForm) {

            {
                autoCompletionCategory = "SearchPartInConstructionKits";
            }

            @Override
            protected void createSearch() {
                search = new EtkPartsSearch(getProject(), false) {
                    @Override
                    protected void createModel() {
                        super.createModel();

                        // Hartcodiert nur die Einzelteilnummer als Suchfeld (M_BESTNR ist das reale Feld, das die Teilesuche
                        // später verwendet nach der Bestimmung der Baukästen über die Einzelteilnummer)
                        EtkDisplayFields searchFields = new EtkDisplayFields();
                        searchFields.addFeld(TABLE_MAT, FIELD_M_BESTNR, false, false, "!!Einzelteilnummer", getProject());
                        model.setSearchFields(searchFields);

                        // WildCardSettings explizit nicht laden, damit keine WildCards ergänzt werden
                        setWildCardSettings(new WildCardSettings() {
                            @Override
                            public void load(EtkConfig config) {
                            }
                        });
                    }

                    @Override
                    protected void searchWithFields(EtkPartsSearchDataset ds) throws CanceledException {
                        List<String> whereValues = getWhereValues();
                        String constKitSubPartNo = whereValues.get(0); // Es gibt nur einen Suchwert für die Einzelteilnummer
                        // DBDataObjectAttributesList verwenden wegen distinct alleine auf DCKC_PART_NO -> alle Baukästen
                        // für die Einzelteilnummer suchen
                        DBDataObjectAttributesList attributesList = getProject().getEtkDbs().getAttributesList(TABLE_DA_CONST_KIT_CONTENT,
                                                                                                               new String[]{ FIELD_DCKC_PART_NO },
                                                                                                               new String[]{ FIELD_DCKC_SUB_PART_NO },
                                                                                                               new String[]{ constKitSubPartNo },
                                                                                                               null, null, true);
                        if (!attributesList.isEmpty()) {
                            attributesList.sort(new String[]{ FIELD_DCKC_PART_NO });
                            for (DBDataObjectAttributes constKitPartNoAttributes : attributesList) {
                                if (Thread.currentThread().isInterrupted() || isCanceled()) {
                                    break;
                                }

                                // Nach den Verwendungen des Baukastens suchen
                                whereValues.set(0, constKitPartNoAttributes.getFieldValue(FIELD_DCKC_PART_NO));
                                setSearchConstraints(whereFields, whereValues);
                                super.searchWithFields(ds);
                            }
                        }
                    }
                };
            }

            @Override
            protected void btnStartSearchClick(Event event) {
                if (startButtonState == StartButtonState.CANCEL) {
                    // super.btnStartSearchClick() würde nur den Button in der Haupt-Suche finden
                    cancelSearch();
                } else {
                    // Suchfelder und Werte ermitteln, um nach Wildcards zu suchen
                    EtkDisplayFields searchFields = new EtkDisplayFields();
                    List<String> searchValues = new ArrayList<>();
                    fillSuchFelderAndFeldWerteForSearch(searchFields, searchValues);
                    String constKitSubPartNo = searchValues.get(0); // Es gibt nur einen Suchwert für die Einzelteilnummer
                    if (StrUtils.stringContainsWildcards(constKitSubPartNo)) {
                        MessageDialog.show("!!Wildcards sind nicht erlaubt.", "!!Einzelteilnummer");
                        return;
                    }

                    super.btnStartSearchClick(event);
                }
            }

            @Override
            public void setStatusText(String statusText) {
                // Keinen StatusText setzen
            }
        };
        searchFormConnector.setSearchResultProvider(searchMechanicForm);

        GuiPanel panel = new GuiPanel(new LayoutBorder());
        panel.addChildBorderNorth(new GuiTitle("!!Nach Einzelteilen in Baukästen suchen..."));
        panel.addChildBorderCenter(searchMechanicForm.getGui());

        GuiWindowForPanelWrapper wrapper = new GuiWindowForPanelWrapper(panel, "!!Nach Einzelteilen in Baukästen suchen...",
                                                                        GuiButtonPanel.DialogStyle.CLOSE, false) {
            @Override
            protected boolean okPressed() {
                return false;
            }

            @Override
            protected void cancelPressed() {
            }
        };

        wrapper.addEventListener(new EventListener(Event.SUB_WINDOW_CLOSED_EVENT) {
            @Override
            public void fire(Event event) {
                searchMechanicForm.cancelSearch();
                searchMechanicForm.dispose();
                searchFormConnector.dispose();
            }
        });

        Dimension screenSize = FrameworkUtils.getScreenSize();
        wrapper.setSize(screenSize.width - 20, screenSize.height - 20);
        wrapper.showNonModal(GuiWindow.NonModalStyle.OPEN_IN_NEW_WINDOW);
    }
}